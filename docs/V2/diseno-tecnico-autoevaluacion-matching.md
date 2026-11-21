# NextRole V2 — Diseño técnico de autoevaluación y matching

**Estado**: Propuesta técnica para revisión; no implementada.\
**Rama verificada**: develop-v2\
**Fecha del relevamiento**: 2026-11-20

## Objetivo y fuentes

Este documento diseña la evolución del flujo existente para usar el catálogo de 31 competencias, guardar intentos incompletos, finalizar la evaluación y conservar los resultados técnicos de seis roles con su contexto histórico. Es una propuesta de implementación: no modifica las decisiones funcionales aprobadas.

Fuentes de verdad funcional:

- docs/V2/autoevaluacion-matriz-vigente.md
- specs/001-autoevaluacion-matching/spec.md
- .specify/memory/constitution.md

Código e interfaz inspeccionados:

- Frontend: frontend/src/features/assessment, frontend/src/features/roles, frontend/src/services, frontend/src/types/domain.ts, frontend/src/app/router.tsx.
- Adaptador HTTP de referencia: integration/frontend-api-adapter.ts.
- Backend: backend/src/main/java/com/nextrole/{skill,matching,role,data,auth,config}.
- Persistencia y pruebas: backend/src/main/resources/db/migration y backend/src/test/java.

La documentación V2 prevalece sobre catálogos, fórmulas y datos V1. Las propuestas técnicas de este documento no son decisiones aprobadas.

## 1. Estado actual verificado

Existe frontend React y backend Spring Boot, pero la interfaz usa actualmente mocks y localStorage para la autoevaluación y el matching. El adaptador HTTP de integration es un prototipo separado, no conectado a frontend/src/services/api.ts.

| Área | Hallazgo actual | Consecuencia para V2 |
|---|---|---|
| SkillAssessmentPage | Presenta habilidades como tarjetas por categoría y guarda cada nivel mediante el servicio simulado. | Reutilizar la estructura visual; cargar y responder preguntas versionadas V2. |
| LevelPicker | Ofrece niveles 0, 2, 3 y 4; omite 1 y usa etiquetas genéricas. | Debe presentar los cinco niveles 0–4 con la descripción específica aprobada para cada pregunta. |
| SkillsIndexPage | Determina “completo” contando claves en el mapa de habilidades. | Debe consultar el estado del intento; el conteo por sí solo no finaliza ni calcula. |
| SkillSummaryPage | Calcula promedios generales y por categoría con las respuestas disponibles. | Sustituir por seis porcentajes V2 y resultados históricos; el promedio no es matching aprobado. |
| RolesSuggestedPage | Muestra “afinidad”, obtiene resultados del mock y mantiene selección de rol. | Mostrar preparación técnica del intento completo y respetar el orden persistido por backend. |
| RoleDetailPage | Combina descripción estática, afinidad, Skill Gap y selección de rol. | Puede conservar la información estática; el score viene del resultado versionado. Skill Gap/selección no participan en el cálculo. |
| mockData.ts | Define 15 skills y ocho roles, con una matriz distinta a V2. | No usar como catálogo o matriz de matching V2. |
| frontend/src/services/api.ts y storage.ts | Simulan API y persistencia en localStorage. El matching se calcula en cliente y admite respuestas parciales. | Reemplazar este flujo por llamadas a backend; localStorage no será la fuente de verdad del intento. |
| integration/frontend-api-adapter.ts | Contiene un adaptador HTTP de ejemplo para /api/v1; convierte afinidad nula en 0 y tiene TODOs de contrato. | Sirve como referencia de autenticación/errores, pero requiere contratos V2. |
| SkillController | Expone catálogo, niveles vigentes y actualización individual bajo /api/v1/skills. Usa CurrentUser para asociar datos al usuario. | No representa intentos ni sus versiones; crear API de evaluaciones V2. |
| MatchingController/MatchingService | POST /api/v1/matching/analyze calcula y persiste un run; GET results/latest devuelve el último. | Reemplazar el análisis bajo demanda por cálculo atómico al completar una evaluación. |
| MatchingService actual | Mezcla intereses y preparación, calcula affinity/confidence/coverage, renormaliza por requisitos contestados y ordena por afinidad redondeada. | Este comportamiento V1 debe reemplazarse, no tratarse como decisión pendiente. |
| Modelo actual | user_skills conserva una fila vigente por usuario/skill. matching_runs/results no apuntan a intento, cuestionario o matriz. | Añadir persistencia versionada de intentos, respuestas y resultados. |
| Seed V2 actual | V2__catalog_seed.sql registra ocho roles y requisitos sobre skills que no equivalen a las 31 competencias. | Cargar la matriz V2 aprobada; limitar resultados V2 a los seis roles definidos. |
| Seguridad y pruebas | Spring Security/JWT protege endpoints; CurrentUser.id() obtiene el UUID del subject. Hay JUnit y Testcontainers; ApiIntegrationTest permite una respuesta y espera ocho resultados. No se encontró harness frontend en package.json. | Reutilizar identidad/infraestructura; actualizar integración y definir estrategia de pruebas UI. |

El cálculo de frontend tampoco es autoritativo: aunque usa pesos y saturación, puntúa solo requisitos respondidos y redondea a enteros. La fórmula V2 se ejecutará en backend únicamente después de validar las 31 respuestas.

## 2. Reutilización y cambios

| Componente | Decisión de diseño |
|---|---|
| AppShell, TopBar, Card, Button, Badge, ProgressBar | Reutilizar como componentes visuales. |
| SkillAssessmentPage | Reutilizar la ruta y estructura visual; rehacer el flujo para las 31 preguntas, navegación y autosave. |
| LevelPicker | Reutilizar solo tras ampliar a niveles 0–4 y recibir descripciones por pregunta. |
| SkillsIndexPage | Cambiar la condición de redirección para basarla en estado del intento del servidor. |
| SkillSummaryPage | Reutilizar el contenedor; eliminar promedio general y por categoría, mostrar resultados de matching. |
| RolesSuggestedPage | Reutilizar tarjetas y exploración; eliminar affinity y no reordenar con score ya redondeado. |
| RoleDetailPage | Reutilizar descripción del rol. No incorporar su selección o Skill Gap al porcentaje. |
| TanStack Query y React Router | Reutilizar para catálogo, intento, guardado, finalización e historial. |
| CurrentUser, JWT, Spring MVC, validación, JPA y transacciones | Reutilizar para propiedad del intento, validación, persistencia y finalización atómica. |
| Scoring actual | No reutilizar afinidad, confidence ni renormalización por evidencia faltante. La operación matemática de aporte limitado debe expresarse en un nuevo servicio V2. |
| Role/Skill/RoleSkill y repositorios | Pueden orientar el catálogo, pero no sustituyen las definiciones versionadas de 31 competencias y 186 celdas. |
| MatchingRun/MatchingResult | Reutilizar el concepto de conjunto de cálculo; ampliar o reemplazar su representación porque no guarda contexto V2 ni ranking persistido. |

## 3. Flujo V2

1. El frontend carga una versión vigente del catálogo: 31 competencias en orden y cinco opciones textuales 0–4 por competencia.
2. El usuario inicia un intento autenticado, vinculado a la versión de cuestionario y a la de matriz.
3. Cada selección se persiste automáticamente. Nivel 0 es un valor explícito; una respuesta ausente sigue ausente.
4. Al volver, el usuario recupera y puede editar el intento en curso.
5. Al finalizar, el backend verifica una respuesta para cada una de las 31 competencias. Si falta alguna, devuelve las pendientes y no produce resultados.
6. En una transacción, el backend bloquea cambios, marca el intento completo, calcula los seis roles y persiste los resultados y el orden de empates.
7. El frontend consulta y muestra esos resultados persistidos; no vuelve a calcularlos.
8. Reevaluarse crea otro intento precargado con las respuestas de la última evaluación completa. La copia se puede editar sin alterar el original.
9. El perfil técnico vigente se deriva de la evaluación completa más reciente; un borrador no lo reemplaza.

El progreso visible puede mostrar respondidas de 31. Este dato es solo navegación y no un factor de puntuación. No hay matching parcial.

## 4. Endpoints y contratos conceptuales

Se propone usar rutas bajo /api/v2 durante la transición. Es un contrato técnico conceptual; no está implementado.

| Método y ruta | Propósito y resultado conceptual |
|---|---|
| GET /api/v2/assessment-catalog/current | Devuelve las 31 competencias ordenadas con versión de cuestionario, código estable, título, pregunta y cinco niveles 0–4. |
| POST /api/v2/assessment-attempts | Si hay borrador activo, devuelve ese borrador; de lo contrario crea uno y fija las versiones vigentes de cuestionario y matriz. Devuelve id, estado, versiones, progreso y respuestas. No recibe userId. |
| GET /api/v2/assessment-attempts/current | Devuelve el único borrador activo del usuario o ausencia de borrador; lo usa la ruta de autoevaluación para recuperar progreso. |
| GET /api/v2/assessment-history | Lista solo evaluaciones completas, más reciente primero, con fecha, resultados y acceso al detalle. Excluye borradores. |
| GET /api/v2/assessment-attempts/{attemptId} | Recupera preguntas/versiones y respuestas de un intento propio. Los resultados solo aparecen si está completo. |
| PUT /api/v2/assessment-attempts/{attemptId}/answers/{competencyCode} | Guarda una respuesta idempotente, level entero 0–4. Rechaza intento ajeno, competencia fuera de versión o intento no editable. |
| POST /api/v2/assessment-attempts/{attemptId}/complete | Valida 31 respuestas y finaliza atómicamente. Si faltan, identifica pendientes y no genera resultados. |
| POST /api/v2/assessment-attempts/reevaluations | Si hay borrador activo devuelve ese borrador y no duplica; sin borrador crea uno nuevo precargado con la última evaluación completa. |
| POST /api/v2/assessment-attempts/{attemptId}/discard | Descarte explícito de un borrador propio; propuesta: estado discarded sin borrado físico, que permite iniciar otro. |
| GET /api/v2/assessment-attempts/{attemptId}/results | Consulta los seis resultados existentes con score visible, posición, fecha y versiones. No vuelve a calcular. |

Todos los endpoints requieren autenticación y derivan el usuario del JWT mediante CurrentUser. El servidor valida propietario y estado. No acepta userId como autoridad del cliente.

El DTO incluye roleCode, porcentaje, rankPosition persistido, fecha, questionnaireVersion, matrixVersion y algorithmVersion. No expone intereses, objetivos, confidence, coverage o affinity como componentes del porcentaje. El historial inicial muestra fecha, seis resultados y acceso al detalle; no incorpora comparaciones gráficas. Paginación y formato común de errores se fijan durante el Plan técnico.

### Invariantes

- Solo un borrador activo por usuario; el inicio devuelve el existente en lugar de crear uno duplicado.
- Comenzar una evaluación nueva mientras hay borrador requiere descartarlo explícitamente.
- Una finalización incompleta no cambia el estado ni produce resultados.
- Una finalización completa persiste seis resultados y las posiciones de desempate.
- Repetir finalización/consulta devuelve resultados persistidos y no sortea empates de nuevo.
- El historial excluye borradores y se ordena por finalización descendente.

## 5. Modelo de datos conceptual

Propuesta aditiva para diseño posterior; no se fijan entidades JPA, tipos SQL ni migraciones. Las migraciones aplicadas no se editan.

| Entidad lógica | Campos/contenido | Invariantes |
|---|---|---|
| QuestionnaireVersion | ID/código, estado, fecha. | Cambia al cambiar preguntas, opciones o competencias; independiente de matriz. |
| CompetencyQuestionVersion | Versión, competencyCode, orden, título, enunciado. | 31 competencias por versión publicada. |
| AnswerLevelOption | Pregunta versionada, nivel y descripción. | Cinco opciones 0–4 por pregunta. |
| RoleMatrixVersion | ID/código y estado de publicación. | Cambia cuando cambian requisitos de roles; independiente de cuestionario. |
| RoleRequirementVersion | Versión, roleCode, competencyCode, importancia E/C/no relevante, nivel esperado. | 186 filas, seis roles × 31 competencias, incluidas las no relevantes. Para requisitos relevantes admite nivel esperado 1–4; la matriz vigente asigna 1–3. No relevantes se excluyen. |
| AssessmentAttempt | ID, userId, estado in_progress/completed/discarded, versiones de cuestionario y matriz, fechas y referencia de intento fuente. | Máximo un in_progress por usuario; versiones fijadas al crear; completed inmutable; descarte explícito. |
| AssessmentAnswer | Intento, competencyCode, nivel 0–4, updatedAt. | Única por intento/competencia. Fila ausente = pendiente; level 0 = respuesta válida. Editable solo en borrador. |
| MatchingResultSet | Intento, algorithmVersion, fecha. | Como máximo uno por intento completo. |
| RoleMatchingResult | ResultSet, roleCode, score exacto, score presentado, rankPosition. | Seis filas por conjunto; posición conserva ranking y desempate. |

Cada borrador conserva las versiones de cuestionario y matriz que recibió al crearse, aunque se publiquen otras. Los resultados completos conservan sus versiones. Cambios de preguntas, opciones o competencias generan versión nueva de cuestionario; cambios en requisitos generan versión nueva de matriz.

### Identificadores estables de los seis roles

Se reutilizan IDs existentes solo cuando el rol del seed es equivalente sin ambigüedad. Los dos IDs nuevos son propuestas técnicas para revisión, no datos aprobados para cargar:

| Rol V2 | ID estable propuesto | Tratamiento |
|---|---|---|
| Backend | backend-dev-jr | Reutilizar: equivalencia directa. |
| Frontend | frontend-dev-jr | Reutilizar: equivalencia directa. |
| Full Stack | fullstack-dev-jr | Reutilizar: equivalencia directa. |
| QA | qa-jr | Nuevo propuesto: qa-automation-jr identifica el subtipo de automatización, no necesariamente QA general. |
| Data Analyst | data-analyst-jr | Reutilizar: equivalencia directa. |
| Cloud | cloud-jr | Nuevo propuesto: devops-jr identifica DevOps/Cloud combinado, no Cloud inequívocamente. |

No se modifica la base. Los IDs existentes de QA Automation y DevOps/Cloud, y otros roles ajenos a V2, no se eliminan ni migran en este trabajo. Confirmar qa-jr y cloud-jr antes de persistirlos.

### Datos históricos V1

No se requiere migrar, convertir ni conservar las respuestas históricas V1 como parte del modelo funcional V2. No se diseñan mecanismos de conversión ni se usan esas respuestas en scoring V2. Este requerimiento no modifica ni borra físicamente registros actuales; cualquier acción destructiva necesita autorización explícita.

## 6. Matching V2

Reglas funcionales aprobadas, sin rediseño:

1. Exigir 31 respuestas antes de calcular.
2. Para cada rol, incluir solo competencias esenciales y complementarias; excluir no relevantes.
3. Aporte = min(nivel declarado / nivel esperado, 1).
4. Porcentaje = 100 × suma(peso × aporte) / suma(pesos), con esencial=2 y complementaria=1.
5. Calcular los seis roles; nivel superior al esperado no suma.
6. Ordenar de mayor a menor usando precisión completa.
7. Ante empate exacto de precisión completa, aleatorizar una sola vez y persistir el orden.
8. Mostrar un decimal con HALF_UP. El redondeo es solo visual y no participa en score, ranking o empate.

Los requisitos relevantes pueden tener nivel esperado 1–4. La matriz V2 actualmente aprobada conserva exactamente sus valores 1–3; admitir nivel 4 no cambia la fórmula ni los pesos. Los requisitos no relevantes continúan excluidos.

Intereses, preferencias, objetivos profesionales, proyectos, rol seleccionado y completitud como proporción no son entradas del algoritmo. Completitud es un gate binario de las 31 respuestas antes del cálculo.

MatchingService actual mezcla intereses y preparación en affinity, calcula confidence, omite requisitos sin respuesta, renormaliza el denominador y ordena por afinidad entera. Ese comportamiento se reemplaza. La interfaz simulada tampoco genera resultados autoritativos.

## 7. Estrategia frontend

- Reutilizar React Router, TanStack Query, AppShell, tarjetas, botones y progreso.
- La entrada a autoevaluación consulta el borrador activo: si existe, lo recupera; de lo contrario inicia uno.
- Cada selección se guarda en backend. Al volver, se cargan las versiones y respuestas del único borrador. Nivel 0 permanece explícito.
- Permitir descartar el borrador de forma explícita; después puede iniciarse uno nuevo. No presentar dos borradores activos.
- Bloquear finalización con pendientes y mostrar cuáles faltan; el servidor vuelve a validar.
- Reemplazar promedios de SkillSummaryPage por los seis scores completos, descendentes y presentados a un decimal HALF_UP. La interfaz respeta rankPosition.
- Mostrar historial solo de intentos completos, más reciente primero, con fecha, resultados y detalle. No mostrar borradores en el historial ni implementar comparaciones gráficas.
- Mantener información estática de roles reutilizable. Skill Gap y Learning siguen fuera de esta funcionalidad.

## 8. Pruebas y validación integral propuestas

### Unitarias backend

- Nivel 0 aporta cero; nivel igual/superior al esperado aporta uno.
- Un requisito relevante con nivel esperado 4 es válido y se calcula con la misma fórmula; no altera la matriz vigente.
- Esencial pesa 2, complementaria 1 y no relevante no afecta numerador ni denominador.
- La fórmula usa el denominador de todos los requisitos relevantes y produce seis resultados.
- Cambiar intereses, objetivo, proyectos o rol seleccionado sin cambiar respuestas no cambia scores.
- Los scores se ordenan de mayor a menor sin redondear; un empate exacto se aleatoriza una vez y mantiene la posición en lecturas posteriores.
- La presentación usa un decimal HALF_UP; comprobar que esta regla no afecta score, orden ni empate exacto.

### Integración backend/base de datos

- Autosave, recuperación, nivel 0 y aislamiento entre usuarios autenticados.
- Intento ajeno, competencia no incluida en la versión y edición tras finalizar son rechazados.
- Con 30 respuestas no se finaliza ni persiste ningún resultado; la respuesta de error identifica pendientes.
- Con 31 respuestas se guardan un intento finalizado y seis scores en una transacción.
- Reintentar complete lee el resultado existente y no cambia el desempate.
- Reevaluación copia las respuestas de la última completa y no altera intento/resultados fuente.
- Un borrador no reemplaza el perfil técnico actual.
- Versiones independientes incrementan ante los cambios aprobados; un borrador conserva las versiones asignadas al crearse y una publicación posterior no las cambia.
- Una nueva versión no altera resultados finalizados previos.
- Un usuario nunca tiene más de un borrador activo; volver recupera el actual y descartarlo explícitamente permite crear uno nuevo.
- El historial contiene solo evaluaciones completas ordenadas por fecha descendente, con fecha, seis resultados y detalle; no mezcla borradores.
- Carga de matriz verifica 31 × 6 = 186 celdas contra autoevaluacion-matriz-vigente.md.
- Actualizar ApiIntegrationTest: actualmente acepta una respuesta y espera ocho roles.

### Frontend/extremo a extremo

- Selector muestra niveles 0–4 con texto exacto de la pregunta.
- Recarga/reingreso recupera borrador desde backend y una selección se persiste.
- Una pregunta pendiente no presenta resultados; las 31 respondidas permiten finalizar.
- El cliente conserva el ranking descendente del servidor aunque dos porcentajes se vean iguales tras redondeo HALF_UP.
- Volver recupera el único borrador; descartarlo y comenzar crea uno nuevo sin duplicados.
- El historial muestra completadas solamente, recientes primero, con fecha, resultados y detalle, sin comparación gráfica.
- Consultar un resultado histórico no vuelve a calcularlo.
- Flujo completo verifica seis roles, contexto de versión e inicio de reevaluación.

El proyecto tiene pruebas JUnit/Spring y Testcontainers PostgreSQL en backend. No se halló harness frontend definido en package.json. No se ejecutaron pruebas: este entregable es documental y no se modificó código.

## 9. Dependencias y decisiones pendientes

### Dependencias observadas

- Backend: Java 21, Spring Boot 3.5, Spring MVC, Bean Validation, Spring Data JPA, Spring Security/JWT, PostgreSQL y Flyway.
- Frontend: React 19, TypeScript, React Router, TanStack Query y Vite.
- Identidad: CurrentUser deriva usuario del JWT.
- API existente: rutas /api/v1; adaptador real separado del cliente activo.
- Catálogo actual: ocho roles y 15 skills; catálogo V2: 31 competencias y seis roles.

### Decisiones aprobadas

Ranking descendente a precisión completa, empate aleatorio persistido, HALF_UP solo para presentación, versiones independientes y fijadas al crear borrador, borrador único recuperable/descartable, no migración/conversión V1, IDs estables con reutilización inequívoca, historial de completas reciente-primero y sin comparaciones gráficas.

### Únicas cuestiones pendientes

1. Confirmar los IDs nuevos propuestos qa-jr y cloud-jr antes de incorporarlos a la base o contratos.
2. Los niveles esperados admitidos son 1–4; la matriz vigente mantiene sus asignaciones aprobadas de 1–3. No hay decisión pendiente sobre este rango.

Ninguna de las siete decisiones recién aprobadas vuelve a quedar pendiente. No se modifica la matriz V2.

## 10. Orden recomendado de implementación

1. Confirmar qa-jr y cloud-jr antes de crear referencias persistidas; aplicar las decisiones funcionales aprobadas.
2. Definir códigos estables de competencia/rol y cotejar catálogo y 186 valores de matriz.
3. Definir el modelo versionado y sus invariantes sin conversión automática de respuestas V1; preservar los datos existentes y requerir autorización explícita para cualquier operación destructiva.
4. Preparar migración Flyway aditiva y persistencia JPA V2; no editar migraciones ya aplicadas.
5. Implementar ciclo de intentos: inicio, usuario propietario, autosave, recuperación, copia de reevaluación, completitud e inmutabilidad.
6. Implementar servicio de matching y finalización transaccional con seis resultados, versiones y desempate persistido.
7. Publicar contratos API V2 y conectar el adaptador frontend, retirando mocks del flujo de evaluación.
8. Adaptar cuestionario, resultados e historial reutilizando componentes compatibles.
9. Añadir pruebas unitarias, integración PostgreSQL y validación de frontend/extremo a extremo.
10. Revisar rutas legacy sin incorporar Skill Gap ni Learning a esta funcionalidad.

## Clasificación de decisiones

- **Aprobado**: reglas funcionales provenientes del catálogo vigente y la Specification.
- **Propuesto**: API V2, persistencia por intento/versiones, scoring autoritativo en backend y reutilización visual descrita arriba.
- **Pendiente**: confirmar los IDs técnicos propuestos `qa-jr` y `cloud-jr` antes de persistirlos. El rango permitido de niveles esperados 1–4 está aprobado; la matriz vigente mantiene sus valores 1–3.

No se modificó código, entidades, migraciones, SQL, endpoints existentes ni pruebas.
