# Estado vigente de NextRole V2

**Actualizado:** 2026-11-27
**Rama activa:** `develop-v2`
**Checkpoint:** `766429b` — `Mejora la bienvenida de Inicio e incorpora acceso directo a la autoevaluación`; HEAD y `origin/develop-v2` alineados al verificar.
**Estado Git:** se preservan cambios locales preexistentes en `docker-compose.yml`, `experiments/`, `package-lock.local-backup.json` y `tatus`. No se cambió de rama.

## Estado actual — Plan cerrado y Tasks de Explainability/Formation

- **Etapa:** Specify y Clarify de `specs/003-explainability-formacion` aprobadas; Plan corregido según decisiones aprobadas y `tasks.md` generado. Pendiente revisión del usuario antes de Implement. No se inició implementación.
- **Modelo inicial aprobado:** OpenRouter con `baidu/ernie-5.5-flash-preview:free` y prompt V5 existente e inmutable. Configurable y registrado. Sin llamadas reales, modelos pagos, fallbacks ni reintentos automáticos.
- **Diseño:** monolito modular; reutilización directa de `AssessmentAttemptService` y `SkillGapV2Service`; Matching persistido; prioridad calculada solo por Explainability; V7 aditiva propuesta; persistencia JSONB y snapshots históricos de rol; contexto compatible con el contrato experimental Python.
- **Ciclo corregido:** `generate` solo desde ausencia de fila; `GENERATING` se consulta; `SUCCEEDED` se reutiliza; `FAILED` requiere acción manual por `retry`. Un lease vencido no llama al proveedor. No hay cuotas propias; `attempt_count` es informativo y los límites upstream siguen siendo errores del proveedor.
- **Navegación reconciliada:** botones V2 «Explorar rol» llevan a `/formacion/roles/:roleId?attemptId=...` y regreso a `/roles?attemptId=...`. Entrada general, rutas/consumidores V1 y `002-skill-gap-v2` quedan intactos.
- **Roles:** revisión de consumidores comprobó que actualizar `assessment_roles.name` en V7 no modifica los nombres mostrados en ranking actual/histórico, resultados persistidos ni pantallas V1; el frontend usa su mapa de nombres y RoleDetail consulta el catálogo V1. Los tests de migración deberán reflejar los nombres oficiales.
- **Validación documental:** coherencia revisada entre spec, plan, research, data model, contratos, quickstart y checklist; el prompt V5 y su contrato experimental permanecen intactos. No se realizaron llamadas a proveedores ni pruebas de aplicación/migraciones.
- **Próximo paso:** revisar `tasks.md`; Implement solo después de aprobación explícita.

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
