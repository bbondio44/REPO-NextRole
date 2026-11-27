# Plan técnico: Explainability y Formación por rol

**Branch de feature**: `003-explainability-formacion` (la rama de trabajo observada sigue siendo `develop-v2`)
**Fecha**: 2026-11-27
**Spec**: [spec.md](spec.md)

## Resumen

Agregar Explainability como módulo del monolito Spring Boot, con generación síncrona bajo demanda por combinación de intento completado y rol, usando el Matching persistido, Skill Gap V2 invocado directamente, descripciones históricas de niveles y el catálogo backend de roles. Persistir el diagnóstico estructurado exitoso de manera inmutable. La pantalla V2 de Formación consulta primero sus datos determinísticos y luego genera/recupera el diagnóstico; conserva independiente la entrada general a Formación y el flujo V1.

La primera configuración será OpenRouter + `baidu/ernie-5.5-flash-preview:free` + prompt V5 sin cambios. No se ejecutan solicitudes reales en Plan ni se incorporan fallbacks pagos.

## Contexto técnico

- **Lenguaje/plataforma**: Java 21, Spring Boot 3.5.16; frontend React 19, TypeScript y React Router 7.
- **Dependencias existentes**: Spring MVC, Validation, Data JPA, Security/OAuth2 Resource Server, PostgreSQL y Flyway. No hay SDK de IA ni cliente HTTP Spring dedicado; Spring MVC permite usar `RestClient` sin añadir dependencia.
- **Persistencia**: PostgreSQL; migraciones publicadas hasta V6. Siguiente migración propuesta: V7, aditiva.
- **Pruebas**: Maven/JUnit/MockMvc/Testcontainers; frontend Vitest. Proveedor simulado en pruebas.
- **Rendimiento observado**: dos generaciones OpenRouter/ERNIE V5 registradas respondieron en 17,123 y 18,389 ms. Muestra demasiado pequeña para una garantía de latencia; se propone un timeout explícito y se revisará la experiencia real durante validación.
- **Restricciones**: conservar Matching y contrato Skill Gap V2; sin HTTP interno, colas, cambios de Learning, migraciones destructivas, llamadas reales durante desarrollo ni fallback/modelo pago.
- **Alcance**: seis roles, contexto Q1/Q2 según intento, una solicitud por acceso explícito al rol, datos determinísticos visibles aunque falle la IA.

## Revisión de arquitectura y límites entre módulos

### Backend

Ubicar el módulo en `com.nextrole.explainability` con responsabilidades concretas y acotadas:

| Componente propuesto | Responsabilidad y dependencias |
|---|---|
| `ExplainabilityController` | Rutas autenticadas, extrae `CurrentUser.id()`, traduce el estado del servicio a HTTP. No construye prompts ni accede a repositorios. |
| `ExplainabilityService` | Orquesta lectura, reclamo único de generación, llamada al proveedor, validación, registro de error o persistencia inmutable. Transacciones breves separadas de la llamada externa. |
| `ExplainabilityContextService` | Proyecta solo datos del intento/rol solicitado y calcula prioridad. No consulta directamente repositorios de otros dominios. |
| Puerto mínimo `DiagnosisGenerator` | Entrada: versión/hash de prompt y contexto tipado. Salida: texto original, modelo servido y uso reportado. Es la única frontera necesaria para reemplazar proveedor y sustituirlo por un fake en tests. |
| `OpenRouterDiagnosisGenerator` | Adaptador HTTP sin reglas de negocio; obtiene endpoint, token y modelo de configuración. No tiene estrategia de fallback ni reintento. |
| `DiagnosisValidator` | Parseo estricto del JSON de salida, propiedades exactas, cuatro textos no vacíos e IDs destacados pertenecientes al contexto. Validez estructural no implica fidelidad semántica. |
| Repositorio/entidad de diagnóstico | Almacena estados de reclamo y resultado exitoso. Una restricción única es la garantía final contra duplicación. |

`AssessmentAttemptService.owned()` y `toDto()` ya resuelven propiedad, estado/versiones, respuestas, opciones históricas y resultados guardados; su DTO expone la descripción de cada opción por nivel y el porcentaje del Matching persistido. Se reutilizan como servicios internos, filtrando en el context builder las competencias ajenas al rol antes de transmitir. `SkillGapV2Service.calculate(userId, attemptId, roleId)` se invoca directamente desde Java: no usar el endpoint REST ni copiar el cálculo. `RoleCatalogService` nuevo en el dominio `assessment` entrega nombre/descripción de los seis roles V2.

El servicio de contexto valida que el resultado persistido contenga el rol, obtiene `DiagnosticDto` de Skill Gap y asocia cada nivel actual/requerido con su descripción del cuestionario fijado al intento. La prioridad se calcula una sola vez en Explainability como `gap × (ESSENTIAL ? 2 : 1)`. `NOT_RELEVANT` se excluye. El modelo recibe solo la proyección del rol actual. No se propaga la descripción general del rol como requisito de competencia.

La composición implica lecturas repetidas de intento/datos entre `AssessmentAttemptService` y `SkillGapV2Service`, que ya valida propiedad y reconstituye las brechas. La opción inicial privilegia encapsulación y reutilización sobre acceso a repositorios de `data` desde Explainability. Si una medición muestra coste significativo, una proyección interna compartida en el módulo de Assessment/Skill Gap puede evitarlo; no ampliar ahora el DTO HTTP de Skill Gap ni mover módulos.

### Fuente de roles y conservación histórica

La V5 creó `assessment_roles` con nombres abreviados y sin descripción. La migración aditiva V7 añadirá `description` y actualizará los seis `name` a los nombres profesionales aprobados de la especificación. `RoleCatalogService` será la fuente de lectura usada por UI y contexto, expuesta por un endpoint V2 autenticado. No se modifica `roles` V1, su endpoint ni sus datos.

El diagnóstico exitoso guardará `role_name_snapshot` y `role_description_snapshot` exactamente como se enviaron. No se crea una tabla de versiones de descripciones. Los diagnósticos existentes devuelven su snapshot; una generación aún no exitosa usa el catálogo V2 vigente capturado al construir ese contexto.

### Frontend y rutas

Crear la nueva pantalla y componentes bajo `frontend/src/features/explainability/` o la convención de features equivalente, usando `AppShell`, TopBar, mascota y primitives vigentes; centralizar emblema SVG y paleta por `roleId` en `frontend/src/features/roles/roleVisuals.ts` (ubicación concreta a validar cuando se reciban los SVG). Cada imagen oficial se agrega bajo `frontend/src/assets/roles/` sin editar sus trazados.

Los enlaces compartidos del ranking actual e histórico hoy apuntan a `/roles/:roleId?attemptId=...`. Su destino V2 propuesto es `/formacion/roles/:roleId?attemptId=...`; la nueva ruta cae bajo `/formacion` para activar la pestaña Formación y conserva ambos IDs. Regresar usa `/roles?attemptId=...`. Este destino evita reemplazar el consumidor existente `/roles/:roleId/formacion`, que sigue atendiendo `LearningPage` V1, y conserva `/roles/:roleId`/`RoleDetailPage` mientras lo use algún acceso directo o flujo V1. La ruta general `/formacion` sigue intacta. Los botones mantienen texto, orden y estilo; solo cambia el `to` para resultados V2.

La pantalla realiza primero consultas independientes del diagnóstico para encabezado: detalle V2 del rol y el `AssessmentAttemptDto` seleccionado (fecha y score persistido). Luego consulta el estado de diagnóstico y crea el diagnóstico solo si no existe. El request de generación no bloquea el render ni la barra determinística. Error de IA deja visibles encabezado y plan «Próximamente». Un query de estado se refresca cada 2 s mientras haya GENERATING, con límite visual de 120 s; `App.tsx` desactiva retry y refetch-on-focus. No se aborta ni reinicia una generación por navegar fuera; al volver se recupera estado desde API.

## Flujo de generación y concurrencia

1. El usuario abre la ruta V2 con `attemptId` y `roleId`; la UI conserva el origen histórico y consulta datos de rol/intento sin llamar IA.
2. Backend autentica, comprueba propiedad (`findByIdAndUserId`), estado `COMPLETED`, rol y score persistido para esa pareja. Intento ajeno/inexistente se trata como 404 para no revelar existencia; incompleto/inconsistente, 409.
3. `GET diagnosis` retorna `NOT_STARTED`, `GENERATING`, `FAILED` o `SUCCEEDED` sin escribir. Ante `NOT_STARTED`, y solo entonces, el frontend inicia `POST generate`; `GENERATING` se consulta mediante GET, `SUCCEEDED` se muestra desde persistencia, y `FAILED` espera acción manual.
4. En una transacción breve, `POST generate` inserta una fila `GENERATING` solo si no hay fila previa, con `lease_until`, `generation_token` y contador informativo. `UNIQUE(attempt_id, role_id)` protege el reclamo. Ante conflicto: `SUCCEEDED` devuelve existente; `GENERATING` devuelve estado 202; `FAILED` devuelve estado fallido/409 de retry requerido sin invocar al proveedor.
5. Fuera de toda transacción/bloqueo SQL se construye el contexto versionado y se llama una vez a OpenRouter. Navegación/cierre de pestaña no cancela ni duplica el trabajo del servidor.
6. Al recibir respuesta, el adaptador extrae modelo y uso si los hay; validador rechaza JSON inválido, claves extras/faltantes, textos vacíos, IDs desconocidos o repetidos. No se guarda salida inválida como éxito ni se intenta automáticamente otra generación.
7. Una transacción corta actualiza condicionalmente la misma fila por `generation_token`: a `SUCCEEDED` con el diagnóstico/context metadata, o a `FAILED` con código saneado. Una condición `status <> SUCCEEDED` y la invariancia del servicio impiden sobrescribir un éxito.
8. Si el proceso muere y deja `GENERATING`, al vencer un lease de 2 min el siguiente GET deriva `FAILED/INTERRUPTED` sin escribir ni invocar IA. Solo el endpoint `POST retry`, tras acción explícita, puede reclamar la fila fallida o vencida mediante compare-and-set y token nuevo. El token de fencing evita que un proceso anterior tarde pueda sobrescribir el nuevo intento. Recarga, reentrada, foco o remount no ejecutan retry.

Es síncrono dentro del request Spring MVC para la primera versión: la operación solo se ejecuta al abrir un rol y no se mantiene una transacción durante la llamada. Los dos resultados ERNIE observados duraron 17–18 s; se configura timeout de conexión 3 s y de respuesta 45 s, sin promesa de SLA. Si pruebas de capacidad muestran saturación, revisar antes de introducir cola/worker. La UI conserva su carga aprobada y ofrece reintento tras error.

## Persistencia propuesta

Crear con V7 `explainability_diagnostics` con FK a `assessment_attempts` y `assessment_roles`, PK UUID, `UNIQUE(attempt_id, role_id)`, `status` check (`GENERATING`, `SUCCEEDED`, `FAILED`), `generation_token`, `lease_until`, `attempt_count`, timestamps, `failure_code` saneado, versión/hash de prompt, proveedor, modelo pedido/servido, hashes de contexto, duración, tokens de entrada/salida/razonamiento y coste nullable reportados. `diagnosis_content JSONB` guarda un único objeto con el contrato ya validado; se agrega `role_name_snapshot` y `role_description_snapshot`. CHECK exige JSONB objeto no nulo para `SUCCEEDED`; el validador de aplicación comprueba claves y referencias.

Se usa JSONB porque el contenido ya es un contrato estructurado y se recupera como conjunto; las columnas individuales duplicarían el contrato y aumentarían migraciones si evoluciona. Metadatos de consulta/índice, estado, modelo, tiempos, uso y descripción histórica permanecen en columnas. No se guarda el prompt completo, el contexto completo, la respuesta cruda inválida ni datos personales: el contexto se puede reconstruir determinísticamente desde el intento Q1/Q2 y M1; los hashes permiten detectar divergencia. Matching ya está persistido en `assessment_result_sets` / `assessment_role_results`; Skill Gap se reconstruye mediante V2. La referencia del intento proporciona versión de cuestionario/matriz y sus catálogos históricos.

## API propuesta

Contrato detallado en [contracts/explainability-api.md](contracts/explainability-api.md). Todas las rutas quedan autenticadas por Spring Security y consultan propiedad con usuario de token; ningún `userId` se recibe del cliente.

- `GET /api/v2/roles/{roleId}`: nombre y descripción oficial del rol para encabezado/contexto, sin matching.
- `GET /api/v2/assessment-attempts/{attemptId}/diagnoses/{roleId}`: consulta pura de estado/diagnóstico.
- `POST /api/v2/assessment-attempts/{attemptId}/diagnoses/{roleId}/generate`: acción con posible escritura/llamada externa solo desde ausencia de fila; éxito/reutilización 200, generación concurrente 202, fallo existente 409 sin nueva llamada.
- `POST /api/v2/assessment-attempts/{attemptId}/diagnoses/{roleId}/retry`: solo reclama FAILED/interrumpido tras acción manual; no puede regenerar SUCCEEDED. Sin cuotas propias.

Rutas del diagnóstico mantienen el patrón existente de Skill Gap V2 pero no cambian su GET ni DTO. GET rol es una ampliación API V2 independiente del catálogo V1. Encabezado utiliza rol y evaluación separados del servicio de IA.

## Configuración, privacidad y proveedor

Propiedades `nextrole.explainability.enabled`, `provider=openrouter`, `openrouter.base-url` (default `https://openrouter.ai/api/v1`), `OPENROUTER_API_KEY`, `model=baidu/ernie-5.5-flash-preview:free`, `prompt-version=explainability-v5`, `connect-timeout=3s`, `read-timeout=45s`, `max-output-tokens=1200`, `lease=2m`, `poll=2s`, `max-wait=120s`. Valores operativos configurables; propiedad de fallback queda fija en false para MVP. `attempt_count` solo informa cuántos llamados se iniciaron y no restringe solicitudes. No se agrega limitación propia por usuario, intento o rol.

Usar `RestClient` de Spring con cliente/RequestFactory configurado y bloqueo síncrono, sin SDK. Solicitud Chat Completions usa system=prompt V5, user=JSON mínimo de contexto, `stream=false`, ID exacto del modelo y `provider.allow_fallbacks=false`; no se envía una lista `models` alternativa. No enviar `response_format` inicialmente: OpenRouter documenta que salida estructurada depende del soporte por modelo **y por endpoint/proveedor**, y ese soporte específico de ERNIE/free no se ha verificado sin una solicitud externa. Se mantiene validación local obligatoria del contrato; luego puede evaluarse una opción estructurada sin relajar el validador.

Empaquetar V5 como recurso de classpath del backend, copiando bytes exactos desde el artefacto experimental y probando que SHA-256 coincide; el runner Python no se incorpora al runtime. `prompt-version` selecciona únicamente recursos empaquetados conocidos y no rutas arbitrarias. El proveedor no recibe IDs de intento, userId, fecha, intentos ajenos, fixtures, ni información personal.

Credencial exclusivamente de variable/secret de backend, nunca Vite, frontend, logs, respuestas, Git o datos de excepción. Sanitizar códigos/respuestas upstream antes de guardarlos o devolverlos; no persistir headers, bearer, prompt/context completos o body crudo de respuesta. Coste `NULL` si no reportado; cero solo si el proveedor informa efectivamente cero. No asumir que la variante gratuita elimina cuotas o disponibilidad.

### Reintentos, expiración y límites externos

- Sin retry automático en aplicación, cliente HTTP, React Query, navegación o proveedor hacia otro modelo/proveedor.
- No hay cuotas internas por usuario, intento o rol. `attempt_count` es informativo. Una repetición solo comienza con acción manual sobre FAILED o lease vencido.
- El proveedor puede imponer sus propios límites; se registran como error externo saneado y no habilitan reintento/fallback automático. La indisponibilidad no modifica Matching ni brechas.
- Timeout conexión 3 s, lectura 45 s, salida máxima 1200 tokens, lease 2 min y polling 2 s hasta tope visual de 120 s. Son límites operativos, no cuotas ni SLO.

## Identidad visual y navegación

La pantalla V2 reutiliza `AppShell`, `TopBar`, `BottomNav`, `Mascot`, `Card` y `ProgressBar`; la pestaña Formación coincide por prefijo `/formacion`. La identidad específica se resuelve con un mapa del frontend `roleId → nombre visual/SVG/paleta`; backend solo expone nombre y descripción textual. No se incorporan archivos SVG hasta que el usuario entregue los seis recursos. El módulo Learning V1 y la ruta `/roles/:roleId/formacion` permanecen con sus consumidores actuales.

## Reconciliación documental aplicada

La decisión 21 de `specs/001-autoevaluacion-matching/spec.md` y la sección `Roles/Formation state` de su `plan.md` ahora registran la decisión posterior aprobada en 003: únicamente los botones «Explorar rol» del ranking V2 abren `/formacion/roles/:roleId?attemptId=...` y el regreso conserva `/roles?attemptId=...`. La entrada general `/formacion`, `RoleDetailPage`, `/roles/:roleId/formacion`, Learning V1 y sus consumidores permanecen sin cambios. Se preservaron las modificaciones locales paralelas de 001; no se modificó `002-skill-gap-v2`.

## Estrategia de pruebas y harness

**Backend unitario**: prioridad por esencial/complementaria/cubierta; armado de contexto con actual/requerido y sus descripciones Q1/Q2; catálogo oficial; validación estricta JSON/IDs; timeouts y traducción de errores con fake generator.

**Backend integración (Testcontainers/MockMvc)**: V7 aditiva; propiedad/401/404/409; score persistido por attempt+role; diagnóstico histórico; unique pair; POST concurrente produce una llamada; éxito inmutable; `generate` desde ausencia solamente; FAILED exige retry manual; lease expirado no llama al proveedor hasta acción manual; tokens/cost nullable; proveedor falso y encabezados sin credenciales.

**Frontend Vitest**: ruta directa desde ranking actual e histórico conserva IDs, retorna al ranking del intento; ruta V1 detail/Learning/index intacta; header independiente de IA; carga/éxito/error/retry/diagnóstico guardado/sin brechas; no mutación duplicada, estado de generación consultado cada 2 s; navegación activa, seis mapas de rol, contraste/aria/reduced-motion unitarios.

**Integración posterior**: ejecutar con OpenRouter real requiere autorización expresa aparte. Comparar solo modelo/prompt aprobados, una solicitud controlada y sin reintentos; nunca incluir credenciales en logs. No forma parte de Plan.

**Fixtures**: conservar `experiments/explainability/cases/contexts/` y runs Q1/M1 intactos. En una tarea futura ampliar generador a seis roles y Q2/M1 bajo un directorio/manifest separado, con datos desde `V6__publish_questionnaire_q2.sql` + M1 versionada y validación contra motores determinísticos reales (mismos niveles, scores y gaps). No generar respuestas de IA ni fixtures aquí.

## Puerta de Constitución

- Modular monolith Java/Spring, React, PostgreSQL/Flyway se conserva.
- Matching V2 y contrato Skill Gap V2 se reutilizan sin cambios.
- Migración V7 es aditiva; nunca editar V1–V6.
- Datos y catálogos históricos se preservan; estado/versiones se validan en integración.
- API y salida estructurada tienen pruebas de contrato; proveedor se simula en suites automatizadas.
- No hay excepciones arquitectónicas; no se introduce cola, SDK especializado ni infraestructura nueva.

## Estructura prevista

```text
backend/src/main/java/com/nextrole/
├── assessment/                 # catálogo V2 y lectura interna existente
├── explainability/              # API, orquestación, context builder, prioridad, validador
└── data/                        # entidad/repositorio de diagnóstico (según convención actual)
backend/src/main/resources/
├── db/migration/V7__explainability.sql
└── prompts/explainability-v5.md # copia inmutable byte a byte del V5 experimental
frontend/src/
├── features/explainability/     # página y estados diagnóstico
├── features/roles/roleVisuals.ts
└── assets/roles/*.svg           # recursos oficiales a recibir antes de Implement
specs/003-explainability-formacion/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/explainability-api.md
```

## Diseño y complejidad

Se usa solo el puerto mínimo de proveedor, una implementación HTTP y un fake. El proceso queda síncrono en el monolito con deduplicación por base de datos. No se proponen colas, event bus, provider registry, motor de prompt general, versionado de descripciones ni un módulo Learning anticipado.

**Plan cerrado para revisión antes de Implement**: límites operativos de timeout/tokens/polling, rutas, columnas V7, salida estructurada y concurrencia quedaron definidos conforme a la aclaración aprobada. No existen cuotas propias de Explainability; el cliente reintenta solo por acción explícita y `attempt_count` es informativo. La ruta V2 y los descriptores de roles se reconciliaron con 001; no se modifica 002. Ninguna solicitud al proveedor se ejecutó durante Plan.
