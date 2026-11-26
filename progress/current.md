# Estado vigente de NextRole V2

**Actualizado:** 2026-11-26
**Rama activa:** `develop-v2`
**Checkpoint estable conocido:** `49e5e1a` — `Completa rediseño de Inicio y unifica el flujo de autoevaluaciones V2`
**Estado Git al inicio de esta ejecución:** HEAD estaba 3 commits por delante de la referencia local `origin/develop-v2`; no se realizó fetch. Los cambios locales previos permanecen sin descartar.

## Estado actual

- **Etapa:** implementación de `002-skill-gap-v2` completada y validada técnicamente.
- **Implementado:** servicio backend de lectura bajo demanda y endpoint REST V2 autenticado `GET /api/v2/assessment-attempts/{attemptId}/skill-gaps/{roleId}`. Usa el intento completado propiedad del usuario, las respuestas del intento, el catálogo/cuestionario y la matriz versionados, además de `GapPolicy.amount`. Devuelve la estructura aprobada por competencia y no persiste diagnósticos.
- **Límites preservados:** no se implementaron pantallas, componentes ni navegación V2; Skill Gap V1 permanece; no se modificaron Matching V2, fórmula de matching, competencias, ponderaciones, datos históricos ni esquema/migraciones.
- **Spec Kit:** `002-skill-gap-v2` mantiene el alcance aprobado y sus 10 tareas están completadas. T025 de `001-autoevaluacion-matching` se cerró con aserciones Testcontainers para un único borrador y conservación de versiones fijadas. T040 y T049 continúan pendientes de revisión visual manual, no relacionadas con esta API.
- **Próximo paso:** revisión del resultado por el usuario. No se realizaron commits ni push.
- **Decisiones/bloqueos:** no quedan decisiones funcionales ni bloqueos técnicos abiertos dentro del alcance de 002. Las pantallas consumidoras, priorización, Formación, IA y Learning permanecen para especificaciones futuras.

## Validaciones de esta etapa

- Backend completo: `mvn -f backend/pom.xml test` — **38 pruebas, 0 fallos, 0 errores, 0 omitidas** (2026-11-26). Incluye pruebas unitarias, API/contrato y Testcontainers; `AssessmentHistoryIntegrationTest` pasó con T025.
- Docker/Testcontainers: Docker Desktop 28.5.1 y PostgreSQL 16.11 detectados durante la suite. El primer intento de consultar Docker directamente desde el sandbox no tuvo acceso al socket; la suite se ejecutó con acceso aprobado y confirmó disponibilidad.
- Build backend: incluido en el ciclo Maven de compilación y pruebas, correcto.
- Frontend build/lint/tests: no ejecutados en esta etapa; no hubo cambios frontend ni se implementó interfaz V2.
- Validación visual/manual: no aplica al alcance backend de 002. Las revisiones manuales de etapas anteriores T040/T049 siguen pendientes.
- Consistencia documental: spec, plan, research, contrato, quickstart, tasks y progreso revisados; T025 ya no figura pendiente.
- `git diff --check`: pasa tras retirar espacios finales de este archivo; Git solo emite avisos de conversión de fin de línea en archivos existentes.

## Cambios locales que deben preservarse

Mantener todos los cambios previos que ya existían antes de la implementación de Skill Gap V2, entre ellos `AGENTS.md`, documentación README, `docker-compose.yml`, documentos de guía, `progress/current.md`, artefactos de `specs/001-autoevaluacion-matching/` y `testing/README.md`.

Archivos locales no rastreados que no deben eliminarse ni incorporarse automáticamente a commits:

- `package-lock.local-backup.json`
- `tatus`
- `progress/history.md`
- `docs/GUIA_SDD_NEXTROLE.md`

`docker-compose.yml` contiene configuración local y debe preservarse. No se realizaron commits, push ni operaciones destructivas.

## Historial

Los checkpoints y reportes cronológicos previos se conservan en [`progress/history.md`](history.md). Este resumen describe el estado vigente; verificar siempre Git antes de retomar trabajo.
