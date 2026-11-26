# Implementation Plan: Diagnóstico Skill Gap V2

**Branch**: `develop-v2` (feature spec: `002-skill-gap-v2`) | **Date**: 2026-11-26 | **Spec**: [spec.md](spec.md)

## Summary

Implementar una consulta backend V2 autenticada que calcule, bajo demanda, las brechas para un intento completado y un rol indicado. Reutilizar `GapPolicy.amount` y los registros V2 ya versionados. La salida estructurada podrá ser consumida por módulos backend futuros; el endpoint también queda disponible para una eventual integración frontend. No se agregan persistencia, migraciones, pantallas, navegación ni una fuente de verdad adicional.

## Technical Context

**Language/Version**: Java 21 / Spring Boot 3.5.16.
**Primary Dependencies**: Spring Web, Validation, Data JPA y Security existentes.
**Storage**: PostgreSQL existente; lectura solamente, sin migración.
**Testing**: JUnit/Spring Boot Test y Testcontainers PostgreSQL.
**Target Platform**: API web autenticada existente.
**Project Type**: Backend de aplicación web modular monolith.
**Performance Goals**: Cálculo bajo demanda para hasta 31 competencias; no se establece un nuevo SLO.
**Constraints**: Intento propio y `COMPLETED`; usar questionnaire/matrix fijados; nivel ausente no equivale a cero. No modificar Matching V2, datos históricos, V1 ni cliente frontend.
**Scale/Scope**: Seis roles V2; respuesta calculada no persistida.

## Constitution Check

- **I. Arquitectura modular**: servicio Spring y contrato REST existente; no se añade otro módulo ni dependencia.
- **II. Requisitos aprobados**: evaluación completada, rol explícito, salida exacta, sin descripción ni prioridad.
- **III. SDD**: alcance actualizado tras aprobación; Analyze precede a Implement.
- **IV. Evolución controlada**: el cálculo V1 aporta solo `GapPolicy.amount`; datos y tablas V2 son la fuente canónica.
- **V. Historia**: intento y versiones fijadas determinan el diagnóstico; cero sigue siendo respuesta válida.
- **VI. Verificación**: pruebas unitarias, de API/contrato y Testcontainers previstas.
- **VII. Consistencia**: V1, Matching V2, catálogo y matriz no se alteran.

**Resultado**: sin violaciones constitucionales ni modificaciones de modelo necesarias.

## Design

### Servicio y consulta

1. El controlador obtiene el usuario de `CurrentUser`; el cliente no envía `userId`.
2. El servicio verifica que el intento pertenece al usuario, existe y está `COMPLETED`; verifica que el rol pertenezca a `assessment_roles`.
3. Lee la versión de cuestionario y su `title` desde el intento; lee respuestas de ese intento y requisitos del `matrixVersionId` fijado, para el rol solicitado.
4. Excluye `NOT_RELEVANT`; calcula brecha solo con `GapPolicy.amount`. Una respuesta relevante ausente o un requisito relevante inconsistente devuelve error explícito, nunca cero.
5. Retorna DTOs con intento, rol, versiones y campos aprobados; sin prioridad, estado categórico, porcentaje, pregunta, matching ni escrituras.
6. Expone `GET /api/v2/assessment-attempts/{attemptId}/skill-gaps/{roleId}`. No modifica navegación o frontend.

No se modifica `MatchingEngineV2`, `AssessmentCompletionService`, `SkillGapService` V1 ni `GapPolicy` más allá de reutilizar su método aritmético.

### Estructura de proyecto afectada

```text
backend/src/main/java/com/nextrole/skillgap/
├── SkillGapV2Service.java
├── SkillGapV2Controller.java
└── SkillGapV2Dtos.java
backend/src/test/java/com/nextrole/skillgap/
├── SkillGapV2ServiceTest.java
└── SkillGapV2ApiIntegrationTest.java
backend/src/test/java/com/nextrole/assessment/
└── AssessmentHistoryIntegrationTest.java  # completar T025 como dependencia
```

Se reutilizan entidades y repositorios V2 existentes para intentos, respuestas, preguntas, roles y requisitos. No se cambian frontend ni modelo de persistencia.

## Complexity Tracking

No hay excepciones a la constitución. No se agrega persistencia, migración, dependencia ni motor de matching.
