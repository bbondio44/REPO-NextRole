# Estado vigente de NextRole V2

**Actualizado:** 2026-11-26
**Rama activa:** `develop-v2`
**Checkpoint base al iniciar:** `589da77` — `Implementa servicio Skill Gap V2 y valida contexto histórico`
**Estado Git al inicio:** `develop-v2` coincidía con la referencia local `origin/develop-v2`; no se realizó fetch. Los cambios locales previos permanecen sin descartar.

## Estado actual — publicación de cuestionario Q2

- **Actualizado:** 2026-11-26. **Rama:** `develop-v2`. **Checkpoint base al iniciar:** `589da77` (`Implementa servicio Skill Gap V2 y valida contexto histórico`). No se cambió de rama.
- **Etapa:** Q2/M1 implementado, aplicado y validado en la base local de desarrollo; checkpoint aprobado y preparado.
- **Implementado:** migración aditiva V6 publica Q2 como única versión actual del cuestionario. Q2 conserva los 31 códigos, preguntas y niveles; cambia exactamente las diez descripciones aprobadas y copia las otras 145 desde Q1. M1 y el código productivo de Matching/Skill Gap no cambian.
- **Historial:** Q1 y los intentos/drafts fijados a Q1 permanecen disponibles. Testcontainers verificó un intento Q1 completado con respuestas/resultados, un borrador con respuestas tras la actualización, su recuperación/guardado/finalización, y un nuevo intento Q2/M1. Para las mismas respuestas se mantuvieron los puntajes por rol y brechas por competencia.
- **Spec Kit:** fase 17 añadida a `001-autoevaluacion-matching`; T069–T074 completadas tras validaciones. T040 y T049 siguen pendientes, no pertenecen a esta modificación.
- **Base local:** antes de V6, Flyway V5, Q1/M1 actuales, Q1 con 31 preguntas/155 opciones y 30 intentos, 693 respuestas, 17 conjuntos de resultados y 102 resultados por rol. Tras `docker compose up -d --build backend`, Flyway quedó en V6; Q2 es la única versión actual (31/155), Q1 permanece disponible, M1 sigue actual con 186 requisitos y los cuatro conteos de registros históricos permanecen idénticos. No se recreó el volumen.
- **Validaciones:** las pruebas previas siguen registradas: suite completa (40 pruebas) y validaciones dirigidas (12 pruebas). En este cierre, `mvn -f backend/pom.xml '-Dtest=AssessmentQuestionnaireQ2MigrationIntegrationTest,AssessmentHistoryIntegrationTest,AssessmentAttemptServiceTest,AssessmentDraftApiContractTest' test` pasó (11 pruebas, 0 fallos/errores/omitidas) en bases PostgreSQL desechables de Testcontainers. El backend reportó `/actuator/health` UP; el catálogo y compatibilidad histórica quedaron cubiertos por las pruebas de integración. El GET local sin autenticación al catálogo devolvió 401, esperado por la protección del endpoint. `git diff --check` pasó.
- **No ejecutado:** pruebas frontend (sin cambios frontend), solicitudes a proveedores de IA ni operaciones sobre datos experimentales. No se recreó el volumen PostgreSQL ni se ejecutó ninguna operación destructiva.
- **Archivos de alcance:** `V6__publish_questionnaire_q2.sql`, pruebas de evaluación/versionado/migración, catálogo normativo y artefactos de `specs/001-autoevaluacion-matching/`.
- **Próximo paso:** revisión del checkpoint de Q2/M1 creado en esta sesión. Push no autorizado.
## Cambios locales que deben preservarse

Mantener todos los cambios previos que ya existían antes de la implementación de Skill Gap V2, entre ellos `AGENTS.md`, documentación README, `docker-compose.yml`, documentos de guía, `progress/current.md`, artefactos de `specs/001-autoevaluacion-matching/` y `testing/README.md`.

Archivos locales no rastreados que no deben eliminarse ni incorporarse automáticamente a commits:

- `package-lock.local-backup.json`
- `tatus`
- `experiments/`
- `progress/history.md`
- `docs/GUIA_SDD_NEXTROLE.md`

`docker-compose.yml` contiene configuración local y debe preservarse. No se realizaron commits, push ni operaciones destructivas.

## Historial

Los checkpoints y reportes cronológicos previos se conservan en [`progress/history.md`](history.md). Este resumen describe el estado vigente; verificar siempre Git antes de retomar trabajo.
