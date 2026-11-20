# API del núcleo NextRole — versión 1

**Base URL:** `/api/v1`. Todos los endpoints privados usan `Authorization: Bearer <accessToken>`.

**Convenciones**

- Las competencias y roles se identifican por sus **códigos públicos** existentes en el frontend, por ejemplo `react`, `data-analyst-jr`. Los usuarios y análisis usan UUID.
- Un `204 No Content` en `GET /profile` significa perfil todavía no creado.
- Los errores de negocio incluyen `{ "code", "message", "details", "requestId", "timestamp" }`.
- `affinity` y `preparation` son mediciones distintas; una calificación sin datos suficientes es `null`, no cero.
- La última ejecución de matching o Skill Gap es una **instantánea**. Reanalizar luego de cambiar habilidades o intereses.

| HU | Endpoint principal | Comentario |
|---|---|---|
| 01 | `POST /auth/register` | Cuenta + hash BCrypt + cookie de refresh y access JWT |
| 02 | `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`, `GET /auth/me` | Sesión renovable; logout revoca refresh token |
| 03 | `GET/PUT /profile`, `GET/POST /profile/projects`, `DELETE /profile/projects/{projectId}` | Datos académicos + proyectos, con habilidades declaradas aparte |
| 04 | `GET /skills`, `GET /skills/levels`, `PUT /skills/{skillId}/level` (aliases `GET/PUT /profile/skills`) | Nivel 0 a 4; omitir = no evaluado |
| 05 | `GET /skills/diagnosis` | Resumen por área con cobertura |
| 06 | `POST /matching/analyze`, `GET /matching/results/latest` | Ranking persistido, explicaciones y confianza |
| 07 | `GET /roles`, `GET /roles/{roleId}` | Listado y requisitos |
| 08 | `PUT /profile/target-role`, `GET /profile/target-role` | Persistencia de rol elegido |
| 09 | `POST /skill-gap/analyze` | Cálculo de diferencias respecto del nivel requerido |
| 10 | `GET /skill-gap/latest?roleId=...` | Clasificación y priorización |
| 11 | `GET /learning/resources?roleId=...`, `POST /learning/plans`, `GET /learning/plans/current` | Formación recomendada y plan persistente |

**Ampliación experimental incluida**

- `GET /interests/questions` y `POST /interests/answers`, `GET /interests/results`.
- `DELETE /account`: eliminación de cuenta con cascada SQL para datos asociados.

## Ejemplos principales

**Guardar perfil** (`PUT /profile`)

```json
{"career":"Ingeniería en Informática","stage":"Cursando materias intermedias","objective":"Conseguir mi primera experiencia IT","university":"UADE","graduationYear":2028}
```

**Evaluar una competencia** (`PUT /skills/react/level`)

```json
{"level":2}
```

**Cargar intereses** (`POST /interests/answers`)

```json
{"answers":[{"dimension":"frontend","score":4},{"dimension":"data","score":1}]}
```

**Ejecutar matching** (`POST /matching/analyze`; body vacío)

```json
[
  {
    "role":{"id":"frontend-dev-jr","name":"Frontend Developer Trainee / Jr","area":"Desarrollo","icon":"code","description":"...","responsibilities":["..."],"demandTag":"Catálogo orientativo"},
    "affinity":88,
    "preparation":65,
    "confidence":66,
    "coverage":80,
    "matchedSkillNames":["React.js básico"],
    "explanation":"Estimación ..."
  }
]
```

Este ejemplo es **ilustrativo**, no una respuesta verificada por ejecución real del backend.

**Seleccionar rol** (`PUT /profile/target-role`)

```json
{"roleId":"frontend-dev-jr"}
```

**Diagnosticar brechas** (`POST /skill-gap/analyze`)

```json
{"roleId":"frontend-dev-jr"}
```

**Crear plan de preparación** (`POST /learning/plans`)

```json
{"roleId":"frontend-dev-jr"}
```

## Consideraciones de integración con React

El frontend actual invoca `registerAccount`, `login`, `getProfile`, `setSkillLevel`, `getRoleMatching`, `getRoleById`, `getSkillGap` y `getRecommendedResources`. Puede conservar esas firmas mediante un adaptador HTTP. Hay un ejemplo en `integration/frontend-api-adapter.ts`.

Diferencias a resolver con el equipo frontend:

1. Login/registro devuelven `{accessToken, user}` y no una cuenta plana.
2. `logout` pasa a ser **asíncrono** en el servidor; gestionar invalidación local aun cuando falle la red.
3. La sesión se restaura con cookie de refresh; access token debe quedarse en memoria, no en localStorage.
4. `GET /roles/{id}` devuelve `{ role, requirements }`; desempaquetar `role`.
5. `POST /skill-gap/analyze` devuelve `{ analysisId, roleId, gaps }`; desempaquetar `gaps`.
6. `GET /learning/resources` devuelve pasos agrupados y cada paso tiene `resources`; aplanar si la pantalla mantiene la estructura anterior.
7. Los estados de Skill Gap incluyen `pendiente` como prioridad cuando **no hay evaluación**; la UI debe mostrarlo explícitamente.
8. Los porcentajes de afinidad pueden ser `null` y deben mostrarse como «Sin datos», nunca como «0%».
9. Deben invalidarse consultas de TanStack Query al guardar perfil, intereses o habilidades.
