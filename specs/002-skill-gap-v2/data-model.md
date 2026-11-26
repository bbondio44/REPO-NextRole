# Data Model: Skill Gap V2

Esta feature no agrega entidades persistidas ni modifica el esquema. El diagnóstico es una proyección calculada bajo demanda de registros V2 existentes.

## Fuentes existentes

| Concepto | Fuente canónica | Uso en el diagnóstico |
|---|---|---|
| Evaluación | `assessment_attempts` (`id`, `user_id`, `status`, `questionnaire_version_id`, `matrix_version_id`) | Identifica el intento elegido, verifica propietario y finalización y fija las dos versiones históricas. |
| Nivel declarado | `assessment_answers` (`attempt_id`, `competency_code`, `level`) | Respuesta guardada del intento. El nivel `0` es válido; fila ausente no equivale a cero. |
| Identificador y nombre | `assessment_questions` (`questionnaire_version_id`, `competency_code`, `title`) | Código estable y nombre de catálogo del intento. `question_text` no se incluye ni se reutiliza como descripción. |
| Nivel requerido e importancia | `assessment_role_requirements` (`matrix_version_id`, `role_id`, `competency_code`, `importance`, `expected_level`) | Requisitos del rol consultado en la matriz fijada al intento. Excluir `NOT_RELEVANT`. |
| Rol V2 | `assessment_roles` (`id`, `name`) | Verifica que el identificador consultado pertenece al catálogo de seis roles V2. |

Las tablas y restricciones completas están definidas en [001 data-model.md](../001-autoevaluacion-matching/data-model.md). No se agregan índices, migraciones, campos ni relaciones persistentes.

## Proyección de respuesta (no persistida)

- Contexto: `attemptId`, `roleId`, `questionnaireVersion`, `matrixVersion`.
- Cada competencia: `competencyCode`, `competencyName`, `declaredLevel`, `requiredLevel`, `importance`, `gap`.
- `importance`: valores de dominio `ESSENTIAL` o `COMPLEMENTARY`; filas `NOT_RELEVANT` no se devuelven.
- `gap`: `max(0, requiredLevel - declaredLevel)`.
- No incluye descripción, estado categórico, prioridad, porcentaje ni datos del matching.

## Reglas de integridad en lectura

- El intento debe existir, pertenecer al usuario autenticado y tener estado `COMPLETED`.
- Se leen respuestas y nombres desde `questionnaire_version_id` del intento y requisitos desde su `matrix_version_id`.
- Una respuesta de nivel cero se mantiene como valor declarado. Una respuesta relevante ausente en un intento completado indica inconsistencia y no se sustituye por cero.
- El rol debe existir en `assessment_roles`; no se consulta el catálogo V1 `roles` ni se guarda un objetivo.
- Una matriz/rol sin requisitos relevantes devuelve una colección vacía explicable; no se inventan filas.
