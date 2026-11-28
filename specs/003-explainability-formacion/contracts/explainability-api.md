# Contratos HTTP de Explainability y Formación

Las rutas son propuestas V2 autenticadas. Se conserva el envelope de error `GlobalErrorHandler.ErrorBody` (`code`, `message`, `details`, `requestId`, `timestamp`). No se modifica el contrato de Skill Gap V2.

## Catálogo textual de rol

### `GET /api/v2/roles/{roleId}`

Endpoint de solo lectura usado por el encabezado, independiente de IA.

```json
{
  "roleId": "qa-jr",
  "name": "QA",
  "description": "Descripción oficial aprobada para el rol"
}
```

`200` rol; `404 NOT_FOUND` ID inexistente. Autenticación requerida según política existente. El frontend resuelve emblema y paleta localmente por ID.

## Estado y diagnóstico

Base de recurso: `/api/v2/assessment-attempts/{attemptId}/diagnoses/{roleId}`. No se acepta userId en path, query ni body. El servidor comprueba usuario autenticado, que sea propietario, intento COMPLETED y rol V2 válido.

### `GET` recurso — lectura pura

No genera ni persiste una fila. Sin fila devuelve estado `NOT_STARTED`; filas en curso/fallidas/exitosas se reflejan en la respuesta. Ejemplos:

```json
{"attemptId":"uuid","roleId":"qa-jr","status":"NOT_STARTED"}
```

```json
{
  "attemptId":"uuid",
  "roleId":"qa-jr",
  "status":"SUCCEEDED",
  "roleName":"QA",
  "roleDescription":"Descripción exacta utilizada al generar el diagnóstico",
  "diagnosis":{
    "currentSituation":"...",
    "recommendedFocus":"...",
    "importanceExplanation":"...",
    "professionalRelevance":"...",
    "highlightedCompetencyIds":["api-testing"]
  },
  "model":"baidu/ernie-5.5-flash-preview:free",
  "promptVersion":"explainability-v5",
  "generatedAt":"2026-11-27T12:00:00Z"
}
```

Para `GENERATING`: incluir `status` y `retryAfterSeconds` sugerido, sin porcentaje de progreso. Para `FAILED`: incluir código funcional saneado y `canRetry`; no exponer respuesta del modelo, URL upstream, token o excepción.

Si el lease de una fila `GENERATING` venció, GET puede representar ese estado como fallido/interrumpido, pero no modifica la fila ni inicia una llamada al proveedor. La operación de lectura permanece sin efectos persistentes.

### `POST .../generate` — operación con efectos

Sin body. Solo si no existe fila, crea el registro y ejecuta exactamente una llamada. Si ya existe `SUCCEEDED`, devuelve el diagnóstico persistido sin invocar al proveedor (`200`). Si existe `GENERATING`, devuelve el estado en curso (`202`) sin duplicar la llamada. Si existe `FAILED`, no inicia una llamada y devuelve `409 DIAGNOSIS_RETRY_REQUIRED`; el cliente debe esperar una acción explícita para llamar a `retry`. El frontend genera solo desde `NOT_STARTED`, consulta GET durante `GENERATING` y espera intervención manual en `FAILED`.

Errores: `400` entrada inválida; `401` no autenticado; `404` intento inexistente/no propio o rol inexistente (sin revelar ownership); `409 ATTEMPT_NOT_COMPLETED`, `INCONSISTENT_COMPLETED_ATTEMPT` o `DIAGNOSIS_RETRY_REQUIRED`; `502 INVALID_PROVIDER_RESPONSE`/`PROVIDER_ERROR`; `503 PROVIDER_UNAVAILABLE`/`PROVIDER_RATE_LIMITED`; `504 PROVIDER_TIMEOUT`. No existe `429` de cuota propia. Todos usan envelope común saneado. Una generación fallida queda en estado `FAILED`. El cliente nunca reintenta automáticamente.

El `failureCode` persistido y devuelto por GET permite diagnosticar la causa sin exponer el cuerpo upstream: `PROVIDER_ERROR`, `PROVIDER_UNAVAILABLE`, `PROVIDER_TIMEOUT`, `PROVIDER_RATE_LIMITED`, `PROVIDER_HTTP_ERROR`, `MALFORMED_PROVIDER_RESPONSE`, `EMPTY_PROVIDER_CONTENT`, `TRUNCATED_PROVIDER_RESPONSE`, `INVALID_DIAGNOSIS_JSON`, `INVALID_DIAGNOSIS_SCHEMA` o `INVALID_COMPETENCY_REFERENCE`. Los errores HTTP inmediatos mantienen mensajes genéricos y saneados; logs técnicos solo registran categoría y, ante error HTTP upstream, el código numérico HTTP. Nunca se registran credenciales, respuesta completa, contexto ni datos personales.

### `POST .../retry` — reintento manual explícito

Sin body. Solo una acción explícita del usuario puede reclamar `FAILED` o `GENERATING` con lease vencido, mediante compare-and-set y un `generation_token` nuevo. Un lease vencido por sí solo no inicia una llamada al proveedor. `200` indica resultado exitoso; `202` indica que otra solicitud ya reclamó el diagnóstico; `409 DIAGNOSIS_ALREADY_SUCCEEDED` impide regenerar un éxito. No hay cuotas propias; `attempt_count` es informativo. Los errores del proveedor se mapean igual que en `generate`. Si el estado es `NOT_STARTED`, el cliente debe usar `generate`, no `retry`.

## Generación del contexto (no es contrato HTTP)

El único contenido funcional hacia proveedor es system prompt V5 y user JSON: `roleId`, nombre/descripción, matchingPercentage, y competencias relevantes con id/nombre, current/required (level + description), classification, gap y trainingPriority. IDs locales de intento, identificador de usuario, fecha, fixture/categoría, versiones de catálogo, contexto de otros roles/evaluaciones y datos personales quedan excluidos. `attemptId` se usa solo internamente para aislamiento/persistencia.

La respuesta HTTP exitosa conserva el contrato experimental exacto dentro del objeto `diagnosis`; `roleName` y `roleDescription` son campos hermanos, procedentes de los snapshots históricos guardados. La validación estructural se hace antes de persistir; fidelidad semántica no se garantiza por el esquema.

## Encabezado determinístico

La UI compone encabezado consultando `GET /api/v2/roles/{roleId}` y el intento seleccionado `GET /api/v2/assessment-attempts/{attemptId}`. El score sale de sus `results[]` persistidos; fecha de `completedAt`. Estas consultas no esperan ni dependen del recurso de diagnóstico.
