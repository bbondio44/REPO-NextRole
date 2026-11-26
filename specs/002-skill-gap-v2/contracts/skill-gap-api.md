# Contract: Skill Gap API V2

API autenticada de solo lectura. La identidad se obtiene del JWT y `CurrentUser`; el cliente no proporciona un `userId` autoritativo. Convive con `/api/v1/skill-gap`, que permanece para usos V1.

## Consulta contextual

| Método y ruta | Propósito | Éxito |
|---|---|---|
| `GET /api/v2/assessment-attempts/{attemptId}/skill-gaps/{roleId}` | Calcular brechas para el rol V2 indicado usando una evaluación completada propiedad del usuario y sus versiones fijadas. | `200` con el diagnóstico de ese intento y rol. |

`attemptId` y `roleId` son identificadores existentes; el endpoint no crea intentos, no guarda selección de rol, no escribe resultados de diagnóstico ni recalcula matching.

## Respuesta

```json
{
  "attemptId": "uuid",
  "roleId": "backend-dev-jr",
  "questionnaireVersion": "Q1",
  "matrixVersion": "M1",
  "competencies": [
    {
      "competencyCode": "programming-fundamentals",
      "competencyName": "Fundamentos de programación",
      "declaredLevel": 0,
      "requiredLevel": 2,
      "importance": "ESSENTIAL",
      "gap": 2
    }
  ]
}
```

- `competencies` incluye todas las filas relevantes de la matriz para el rol y el intento; una fila cubierta permanece con `gap: 0`.
- Se excluyen los requisitos `NOT_RELEVANT`.
- `importance` usa `ESSENTIAL` o `COMPLEMENTARY`.
- `competencyName` proviene de `title` en la versión de cuestionario del intento. No se retorna ni reutiliza `question` como descripción.
- No se retornan porcentajes, prioridad, categorías, status, explanation ni resultados de matching.
- Si el rol existente no tiene requisitos relevantes en esa matriz, se retorna `200` con `competencies: []`; no se inventan requisitos.

## Errores y límites

- `404` si el intento no existe/no pertenece al usuario, o si el rol no es del catálogo V2. La respuesta no debe revelar propiedad de intentos ajenos.
- `409` con código `ATTEMPT_NOT_COMPLETED` si el intento no está `COMPLETED`; el consumidor no recibe brechas y gestiona el estado según su propio alcance.
- `409` con código `INCONSISTENT_COMPLETED_ATTEMPT` si faltan respuestas relevantes para un intento completado; nunca interpretar ausencia como nivel cero.
- Los errores usan el sobre de error existente. No se sustituye el contexto por el borrador actual, la evaluación más reciente u otra versión.

## Consumo

El servicio backend y el endpoint entregan el mismo cálculo contextual. Los módulos consumidores deben pasar explícitamente el `attemptId` y el `roleId`; no hay selección automática ni objetivo persistente. La experiencia visual y la navegación que invoquen esta API se definen en las especificaciones de cada consumidor.
