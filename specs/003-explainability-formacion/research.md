# Investigación de decisiones de Plan

Este documento registra solo decisiones que surgen de la implementación y alternativas necesarias para este plan.

## Integración de datos versionados

**Decisión**: invocar `AssessmentAttemptService` y `SkillGapV2Service` como servicios Spring internos; no usar HTTP entre módulos. El primero comprueba propiedad/expone intentos, respuestas, preguntas con todas sus opciones y resultados persistidos; el segundo vuelve a comprobar ownership/completitud y calcula gaps desde las versiones fijadas.

**Racional**: `SkillGapV2Service.calculate(userId, attemptId, roleId)` ya es `@Transactional(readOnly=true)`, valida intento completado y usa questionnaire/matrix versionados. Su `DiagnosticDto` incluye `declaredLevel`, `requiredLevel`, `importance` y `gap`, pero intencionalmente no incluye matching, prioridades ni descripciones; no alterar ese contrato. `AssessmentAttemptService.toDto` aporta las opciones con descripciones históricas y el resultado persistido. El contexto une ambas salidas por código de competencia y recorta a un único rol.

**Alternativas**: llamar al endpoint REST dentro de Spring (HTTP innecesario y auth interna); copiar repositorios/fórmulas (duplicación); agregar prioridad/descripciones a `SkillGapV2Dtos` (amplía contrato sin necesidad). Se descartan.

## Cliente de OpenRouter y formato

**Decisión**: `RestClient` de Spring MVC, POST no streaming a `https://openrouter.ai/api/v1/chat/completions`, Bearer de variable secreta, `model=baidu/ernie-5.5-flash-preview:free`, `provider.allow_fallbacks=false`. No se incluye array alternativo `models` ni retry policy. Se valida el JSON en backend.

**Racional actualizado durante troubleshooting de CP5 (2026-11-28)**: el adaptador Python de experimentación usa `response_format={"type":"json_object"}` y obtuvo salidas V5 válidas. La página oficial actual de [ERNIE 5.5 Flash Preview free](https://openrouter.ai/baidu/ernie-5.5-flash-preview:free) confirma precio gratuito y soporte de salidas estructuradas; se agrega el modo JSON simple del experimento para reducir diferencias entre adaptadores, sin relajar validación local. Una ejecución V5 válida del mismo fixture reportó 3389 completion tokens, incluidos 2866 tokens de razonamiento; el límite inicial Java de 1200 podía cortar la respuesta, así que se aumenta a 5000 configurable. La documentación vigente de [contabilidad de uso de OpenRouter](https://github.com/OpenRouterTeam/docs/blob/main/cookbook/administration/usage-accounting.mdx) señala que `usage: {include: true}` está obsoleto y no tiene efecto en Chat Completions; no se incorpora. La respuesta fallida inicial no guardó `finish_reason` ni el cuerpo upstream, por lo que el truncamiento es una causa plausible, no confirmada. La integración conserva `allow_fallbacks: false`, validación estricta y ningún reintento automático.

**Alternativas**: SDK de IA (dependencia desproporcionada); `WebClient`/stack reactivo (el servidor actual es MVC, sin necesidad de convertir este flujo); JSON Schema estricto en la petición inicial (el experimento aprobado utiliza `json_object`; la validación local mantiene el contrato exacto). Se descartan para este intento de recuperación.

## Síncrono, timeout y costo

**Decisión**: generar en request MVC, sin transacción abierta durante I/O, provider timeout conexión 3 s/lectura 45 s, 5000 tokens máximos configurables y lease de reclamo 2 min. Se agrega modo de respuesta JSON siguiendo el adaptador experimental y se comprueba `finish_reason=length`; la validación local sigue siendo obligatoria. La UI lee el encabezado determinístico aparte. Estado es consultable; navegación/recarga no vuelve a crear la generación.

**Evidencia**: las dos ejecuciones OpenRouter/ERNIE V5 existentes midieron 17,123 ms para `data-analyst-jr-intermedio` y 18,389 ms para `qa-jr-inicial`; dos muestras no establecen disponibilidad ni SLO. El timeout propuesto es un límite operativo revisable y el usuario puede abandonar la pantalla sin bloquear la aplicación.

**Reintentos**: `generate` solo crea desde ausencia de fila; no inicia llamadas para `GENERATING` ni `FAILED`. `retry` requiere acción explícita sobre `FAILED` o lease vencido. No hay cuotas internas ni limitación por usuario/rol/intento; `attempt_count` es operativo. No se configura retry automático en aplicación, HTTP, frontend o fallback del proveedor. Cuotas/429 externos se registran como errores del proveedor y esperan acción manual.

## Persistencia del contexto y salida

**Decisión**: salida validada como `JSONB`; metadatos de control en columnas; descripción y nombre de rol usados se copian al registro exitoso. El contrato de lectura devuelve ambos snapshots fuera del objeto `diagnosis`. Contexto se reconstruye desde attempt + versiones de catálogo/matriz, Matching persistido y Skill Gap V2. Prompt V5 se empaqueta en backend como copia exacta con prueba SHA-256 contra el archivo experimental.

**Alternativas**: guardar el contexto completo (duplica respuestas de evaluación y PII potencial); guardar prompt entero en fila (duplica recurso versionado); columnas por cada campo de respuesta (duplica contrato JSON). Se descartan. Hash de prompt/contexto permite diagnóstico técnico sin conservarlos completos. Coste se almacena nullable; desconocido no se transforma en cero.

## Catálogo de rol

**Decisión**: agregar descripción textual a `assessment_roles` en migración V7 y actualizar nombres abreviados a los nombres aprobados; usar el mismo servicio V2 para endpoint/pantalla/contexto. Cada éxito mantiene snapshot de nombre y descripción.

**Alternativas**: reutilizar `/api/v1/roles` (catálogo legado con semántica y datos distintos); duplicar descripción en frontend; sistema general de versiones (innecesario para seis textos). Se descartan.

## Navegación, rutas y consumidores

**Decisión**: nuevo destino V2 `/formacion/roles/:roleId?attemptId=...`, con regreso explícito a `/roles?attemptId=...`. Dejar intactos `/roles/:roleId`, `/roles/:roleId/formacion` (Learning V1), `/formacion` general y componentes V1; cambiar solo hrefs de `AssessmentResultsRanking` para resultados V2 actuales/históricos. El prefijo `/formacion` activa el `NavLink` existente.

**Evidencia**: `RoleDetailPage` mantiene rutas directas `/roles/:roleId`; `SkillGapPage` enlaza a `/roles/:roleId/formacion`; `LearningPage` sirve recomendaciones V1; `LearningIndexPage` sirve `/formacion`; el ranking compartido aparece en `RolesSuggestedPage` y `AssessmentAttemptDetailPage`. Nueva ruta evita reasignar una URI que hoy tiene consumidores V1.

## Reconciliación documental

La decisión 21 de `specs/001-autoevaluacion-matching/spec.md` y su párrafo `Roles/Formation state` en `plan.md` se reconciliaron con la decisión posterior de 003: solo los botones V2 «Explorar rol» abren `/formacion/roles/:roleId?attemptId=...`. Se mantienen `/formacion`, `/roles/:roleId`, `/roles/:roleId/formacion`, Learning V1 y sus consumidores. Los cambios locales paralelos en 001 se preservaron.

`002-skill-gap-v2` no presenta contradicción: preserva solo gaps versionados bajo demanda y explícitamente no añade prioridad; Explainability consume su servicio y calcula prioridad fuera de ese contrato.

La revisión de consumidores confirmó que `assessment_roles.name` no alimenta las vistas actuales: el frontend usa `ASSESSMENT_ROLE_NAME`; Matching/Skill Gap usan IDs; `RoleDetailPage` lee el catálogo legado V1 `roles`. V7 puede actualizar `name` a los títulos aprobados sin alterar ranking actual/histórico ni resultados persistidos. Los checks de integración de migración que verifican valores de `name` deben ajustarse; ninguna pantalla existente se modifica.

## Criterios que no se pueden verificar sin ejecución autorizada

- Disponibilidad actual/cuotas y respuesta real del modelo free.
- Soporte JSON estructurado en el endpoint/proveedor específico que OpenRouter seleccione.
- Latencia/capacidad bajo concurrencia de usuarios reales.
- Contraste visual final: los seis SVG todavía no se han proporcionado.

Estas limitaciones no impiden completar el diseño Plan; requieren pruebas simuladas y revisión visual antes de producción, y cualquier solicitud real requiere aprobación posterior.
