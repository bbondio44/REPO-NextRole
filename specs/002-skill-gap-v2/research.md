# Research: Diagnóstico Skill Gap V2

## Decisión: reutilizar solo el cálculo aritmético de GapPolicy

**Hallazgo**: `backend/src/main/java/com/nextrole/skillgap/SkillGapService.java` lee `UserSkill`, `RoleSkill`, el catálogo V1 y persiste `GapRun`/`GapItem`. `frontend/src/services/api.ts` también calcula estados y prioridades desde datos de sesión y matrices V1. Ninguno está ligado a un `attemptId` o a una matriz histórica V2.

**Decisión**: implementar una ruta de cálculo V2 de solo lectura que consulte el intento V2 seleccionado y reutilice `GapPolicy.amount` únicamente para `max(0, requerido - declarado)`. No reutilizar el servicio V1 completo ni la priorización/estados de `GapPolicy`.

**Razón**: conserva la fórmula existente sin heredar fuentes de datos, almacenamiento, prioridad ni semántica V1. La salida permanece conforme a 002 y no requiere un motor general nuevo.

**Alternativas**: reusar íntegramente `SkillGapService` (descartada: usa fuente y persistencia V1); duplicar la fórmula en frontend/backend (descartada: dos fuentes de verdad); modificar `MatchingEngineV2` (descartada: calcula afinidad/ranking, no brechas, y está fuera de alcance).

## Decisión: usar el intento y sus versiones fijadas como contexto

**Hallazgo**: `AssessmentAttempt` guarda propietario, estado, `questionnaireVersionId` y `matrixVersionId`. `AssessmentAnswer` guarda los niveles por código; `AssessmentQuestion.title` es el nombre de competencia disponible en la versión del cuestionario; `AssessmentRoleRequirement` almacena requisitos por versión de matriz, rol, código, importancia y nivel esperado. La finalización ya valida todas las respuestas, incluida una respuesta de nivel 0.

**Decisión**: aceptar `attemptId` y `roleId`; validar que el intento pertenece al usuario y está `COMPLETED`; leer respuestas y nombres de la versión de cuestionario fijada y requisitos relevantes de la matriz fijada. `NOT_RELEVANT` se excluye. Si un intento completado presenta una respuesta relevante ausente, no inferir cero: informar inconsistencia de datos.

**Razón**: las versiones de cuestionario y matriz son independientes; ambas se obtienen del mismo intento para preservar su significado histórico. Los resultados de matching persistidos no son entrada ni se recalculan.

**Alternativas**: usar las versiones actuales (descartada: alteraría diagnósticos históricos); usar el intento actual o `UserSkill` (descartada: mezclaría borrador o V1); usar el ranking como fuente de brecha (descartada: el porcentaje no contiene niveles por competencia).

## Decisión: API REST V2 y servicio preparado para consumidores internos

**Decisión**: mantener una consulta autenticada `GET /api/v2/assessment-attempts/{attemptId}/skill-gaps/{roleId}` y un resultado estructurado desacoplado de la interfaz. El servicio backend puede ser invocado por futuros módulos de IA/Learning; el endpoint REST también habilita una eventual visualización frontend. La implementación actual no crea pantallas, enlaces ni navegación de Skill Gap V2. El DTO contiene contexto de intento/rol/versiones y elementos con código, nombre, nivel declarado, nivel requerido, importancia y brecha. No incluye descripción, prioridad, estado categórico, porcentaje ni resultados de matching.

**Razón**: el diagnóstico depende de una evaluación concreta. Una ruta de lectura anidada hace explícito su contexto; compartir el cálculo en backend evita duplicarlo y permite posponer la forma de presentación hasta que se especifique cada consumidor.

**Alternativas**: endpoint global por usuario/rol (descartado: pierde contexto histórico); almacenar resultados en tablas V1 (descartado: duplicaría datos y no soporta la inmutabilidad/versionado requeridos); endpoint de cálculo genérico usado por el cliente (descartado: el backend debe ser la fuente de verdad).

## Decisión: preservar V1 y diferir la integración de UI V2

**Decisión**: preservar la pantalla, los servicios y la navegación existentes de Skill Gap V1. No cambiar `SkillGapPage`, Roles, Formación/Learning ni crear una ruta frontend V2 en esta feature. Las futuras especificaciones consumidoras decidirán cómo presentar/solicitar el diagnóstico y conservar el contexto en su UX.

**Razón**: evita agregar navegación o UI sin un consumidor aprobado, conserva V1 y mantiene la responsabilidad backend de cálculo.

**Validación histórica**: T025 de `001-autoevaluacion-matching/tasks.md` exigió aserciones Testcontainers para unicidad del borrador y conservación de versiones fijadas. Se implementaron en `AssessmentHistoryIntegrationTest` y la suite completa pasó el 2026-11-26; T025 está cerrada. Las pruebas de 002 verifican además la lectura histórica del diagnóstico sin duplicar esas aserciones.
