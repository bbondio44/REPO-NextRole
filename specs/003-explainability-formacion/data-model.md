# Modelo de datos: Explainability

El detalle de estados y restricciones propuestos está en este documento; las rutas/respuestas HTTP están en [contracts/explainability-api.md](contracts/explainability-api.md).

## Cambios aditivos V7

### Catálogo V2 de roles

Ampliar `assessment_roles` (actualmente `id`, `name`) con `description TEXT NOT NULL`. Poblar las seis descripciones aprobadas para sus IDs y actualizar `name` a los títulos oficiales. La revisión de consumidores comprobó que pantallas existentes no leen esos nombres: el frontend usa el mapa `ASSESSMENT_ROLE_NAME`, los resultados V2 serializan `roleId`, y `RoleDetailPage` obtiene su nombre del catálogo V1 `roles`. Actualizar `name` V2 no cambia esas presentaciones; los checks de migración que verifican los nombres deben ajustarse. No tocar la tabla `roles` de V1 ni los IDs usados por Matching. La migración `V7__explainability.sql` agrega columna y valores; V1–V6 permanecen intactas.

### `explainability_diagnostics`

| Campo | Tipo sugerido | Restricción/uso |
|---|---|---|
| `id` | UUID | PK |
| `attempt_id` | UUID | FK `assessment_attempts(id)`, no nulo |
| `role_id` | VARCHAR(100) | FK `assessment_roles(id)`, no nulo |
| `status` | VARCHAR(16) | CHECK `GENERATING`, `SUCCEEDED`, `FAILED` |
| `generation_token` | UUID | Token de fencing para reclamar y finalizar solo el intento vigente |
| `lease_until` | TIMESTAMPTZ | Recuperación del estado GENERATING huérfano; NULL al finalizar |
| `attempt_count` | INTEGER | cantidad informativa de llamadas iniciadas; no limita ni rechaza solicitudes |
| `role_name_snapshot` | VARCHAR(180) | nombre exacto enviado al proveedor, obligatorio en éxito |
| `role_description_snapshot` | TEXT | descripción exacta enviada; obligatoria en éxito |
| `diagnosis_content` | JSONB | objeto de salida validado; obligatorio solo SUCCEEDED |
| `provider` | VARCHAR(40) | `openrouter` en primera versión |
| `model_requested` / `model_served` | VARCHAR(180) | solicitado obligatorio al éxito; servido puede ser NULL |
| `prompt_version` | VARCHAR(80) | por ejemplo `explainability-v5`, no nulo en éxito |
| `prompt_hash` | CHAR(64) | SHA-256 del recurso exacto |
| `context_hash` | CHAR(64) | hash del JSON de contexto canónico |
| `duration_ms` | BIGINT | nullable si no disponible |
| `input_tokens`, `output_tokens`, `reasoning_tokens` | BIGINT | nullable si proveedor no informa |
| `provider_cost` | NUMERIC | nullable si desconocido; cero solo si explícitamente informado |
| `failure_code` | VARCHAR(60) | código saneado; no guardar mensaje crudo upstream |
| `created_at`, `updated_at`, `succeeded_at` | TIMESTAMPTZ | auditoría operativa |

### Invariantes

- `UNIQUE(attempt_id, role_id)`; una fila canónica por combinación, incluyendo fallos. No se almacenan cuotas/ventanas ni se aplican límites propios.
- CHECK de éxito: `status='SUCCEEDED'` exige objeto `diagnosis_content`, nombre/descripción snapshot y version/model/prompt; status distinto de éxito no contiene `diagnosis_content` ni `succeeded_at`.
- El código solo hace transición a `SUCCEEDED` si estado no es éxito y `generation_token` coincide. No hay operación de regeneración de éxito.
- Estados permitidos: `GENERATING → SUCCEEDED | FAILED`; `FAILED → GENERATING` por reintento manual; `SUCCEEDED` terminal. GET deriva como interrumpido un `GENERATING` vencido sin persistir ni llamar al proveedor. Solo POST retry, tras acción explícita, puede reclamar FAILED o lease vencido.
- Cascada de borrado no se habilita. Conservación del diagnóstico histórico sigue al intento.

## Información que no se duplica

`assessment_attempts` conserva propietario, estado, IDs de versiones Q1/Q2 y M1; `assessment_answers` niveles numéricos; `assessment_answer_options` descripciones versionadas; `assessment_role_requirements` nivel e importancia por matriz; `assessment_result_sets` y `assessment_role_results` almacenan matching. `SkillGapV2Service` recalcula brechas de esa fuente versionada. Por eso no se persiste copia completa de respuestas, brechas ni matching en la fila de Explainability.

Se preserva snapshot de descripción/nombre del rol porque el catálogo actual no está versionado y ese texto debe ser recuperable aunque cambie posteriormente. El contrato de diagnóstico exitoso devuelve `roleName` y `roleDescription` fuera del objeto `diagnosis`; el encabezado usa el catálogo vigente antes del éxito y los snapshots después. Prompt se conserva como recurso backend versionado e inmutable con hash; no se guarda texto completo en cada fila. Context hash sirve para detectar cambios de proyección sin duplicar datos personales.

La carga inicial y las definiciones SQL completas corresponden a Tasks/Implement, no a este modelo conceptual.
