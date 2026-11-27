# Feature Specification: Explainability y Formación por rol

**Feature Branch**: `[003-explainability-formacion]`

**Created**: 2026-11-27

**Status**: Tasks — pendiente de revisión antes de Implement

**Input**: Solicitud de especificación funcional del módulo Explainability y su presentación en Formación.

## Clarifications

### Session 2026-11-27

- Q: ¿Cómo se debe llegar a Formación desde el ranking de Roles sin cambiar el diseño de «Explorar rol»? → A: Mantener intactos el diseño, texto y distribución de esos botones y cambiar únicamente su destino para abrir directamente Formación del rol con el mismo `attemptId` y `roleId`; desde allí se vuelve al ranking de esa evaluación. La pantalla intermedia no debe aparecer en ese flujo V2, y sus rutas/componentes existentes se revisarán durante Plan antes de decidir su destino.
- Q: ¿Cuál es la fuente autorizada de las descripciones de rol y cómo se preserva su uso histórico? → A: Usar las seis descripciones aprobadas en esta especificación, centralizadas en el backend para la pantalla y el contexto del modelo. Cada diagnóstico exitoso debe permitir recuperar la descripción exacta utilizada; el mecanismo sencillo para preservarla se definirá durante Plan, sin crear un sistema complejo de versionado solo para estos textos.
- Q: ¿Qué modelo y prompt se usarán en la primera integración? → A: OpenRouter con `baidu/ernie-5.5-flash-preview:free` y el contenido existente e inmutable de `experiments/explainability/prompts/explainability-v5.md`. Modelo y versión del prompt serán configurables y se registrarán en cada diagnóstico exitoso. No se permiten modelos pagos ni fallback a otro modelo sin autorización; esta selección inicial no crea una dependencia permanente del modelo.

## User Scenarios & Testing

### User Story 1 — Consultar el diagnóstico de un rol (Priority: P1)

Como persona que completó una autoevaluación, quiero consultar una explicación personalizada para el rol que estoy explorando, para comprender cómo mis capacidades se relacionan con sus requisitos y cuáles sería valioso desarrollar.

**Why this priority**: El diagnóstico personalizado constituye el valor central de Explainability.

**Independent Test**: Con un intento completado propio y un rol V2, abrir el acceso al diagnóstico y comprobar que se muestra el porcentaje persistido y una respuesta estructurada basada únicamente en las competencias de ese rol.

**Acceptance Scenarios**:

1. **Given** un ranking de una evaluación propia completada y un rol válido sin diagnóstico previo, **When** la persona usa «Explorar rol», **Then** se abre directamente Formación para ese rol e intento, se solicita una única generación y se presenta el encabezado, el diagnóstico y el estado separado del plan de formación.
2. **Given** un diagnóstico exitoso ya persistido para el mismo intento y rol, **When** la persona vuelve a consultarlo, **Then** se reutiliza el diagnóstico existente sin generar uno nuevo.
3. **Given** una autoevaluación completada nueva, **When** la persona consulta un rol, **Then** se utiliza el contexto del intento nuevo y el diagnóstico se identifica independientemente de diagnósticos de intentos anteriores.
4. **Given** un rol consultado y el mismo intento completado, **When** se presenta el matching, **Then** se muestra el porcentaje determinístico persistido sin recalcularlo ni alterarlo.
5. **Given** la persona abrió Formación desde un ranking histórico, **When** selecciona volver, **Then** regresa al ranking del mismo `attemptId`, no al ranking más reciente.

### User Story 2 — Consultar diagnósticos desde el historial (Priority: P1)

Como persona que revisa una evaluación histórica, quiero consultar el diagnóstico asociado al intento y al rol que seleccioné, para que la explicación no se mezcle con mis resultados más recientes.

**Why this priority**: NextRole conserva evaluaciones versionadas; un diagnóstico histórico debe mantener el significado de sus resultados originales.

**Independent Test**: Con dos intentos completados con respuestas distintas y resultados para un mismo rol, abrir el contexto de cada intento y verificar que cada diagnóstico usa el matching, respuestas y requisitos de su propio intento.

**Acceptance Scenarios**:

1. **Given** una evaluación histórica completada y un rol explorado, **When** se consulta un diagnóstico ya guardado, **Then** se devuelve el diagnóstico de esa combinación exacta de intento y rol.
2. **Given** una evaluación histórica completada sin diagnóstico guardado, **When** se solicita por primera vez su diagnóstico, **Then** se puede generar con las respuestas, cuestionario y matriz fijados a ese intento y con la versión de prompt vigente en esa generación.
3. **Given** que cambian posteriormente el prompt o el modelo, **When** se vuelve a consultar un diagnóstico exitoso, **Then** se conserva y devuelve el diagnóstico previamente generado.
4. **Given** un intento histórico que no pertenece a la persona autenticada, **When** se solicita su diagnóstico, **Then** no se revela el intento ni se genera contenido a partir de él.

### User Story 3 — Mantener disponible la pantalla ante errores de IA (Priority: P2)

Como persona que consulta Formación, quiero que un problema del proveedor de IA no afecte mis resultados determinísticos y que se me permita reintentar una generación fallida de manera controlada.

**Why this priority**: Matching y brechas siguen siendo información determinística independiente; un proveedor externo no debe impedir el resto de la experiencia.

**Independent Test**: Simular una indisponibilidad o respuesta inválida del proveedor y comprobar que el porcentaje y encabezado permanecen disponibles, no se guarda un diagnóstico inválido como exitoso y aparece el mensaje de error con una acción explícita de reintento.

**Acceptance Scenarios**:

1. **Given** un diagnóstico aún no existente, **When** se está generando, **Then** el encabezado del rol y el plan futuro permanecen visibles junto al estado de carga aprobado, sin porcentaje de progreso ficticio.
2. **Given** que el proveedor falla, expira o devuelve una respuesta inválida, **When** finaliza el intento, **Then** se muestran el mensaje aprobado y la acción «Volver a intentar», mientras los resultados determinísticos siguen disponibles.
3. **Given** una generación fallida, **When** la persona activa el reintento, **Then** se realiza un nuevo intento controlado; no hay reintentos automáticos ni se reemplaza una respuesta exitosa previa.
4. **Given** dos solicitudes concurrentes para la misma combinación de intento y rol, **When** ambas llegan antes de que exista un diagnóstico exitoso, **Then** no se ejecutan generaciones duplicadas y las solicitudes reciben el resultado común o un error explícito.

### User Story 4 — Distinguir el diagnóstico del futuro plan de formación (Priority: P2)

Como persona que consulta Formación, quiero encontrar un espacio claro para un futuro plan de formación, sin que las recomendaciones dependan del texto de Explainability.

**Why this priority**: La pantalla comparte el espacio visual, pero Explainability y Learning deben poder evolucionar y operar de manera independiente.

**Independent Test**: Consultar un diagnóstico exitoso y verificar que sus cuatro apartados y el módulo «Tu plan de formación» son secciones visuales separadas, y que el módulo futuro solo muestra «Próximamente».

**Acceptance Scenarios**:

1. **Given** un diagnóstico exitoso, **When** se presenta la pantalla de Formación del rol, **Then** se muestran «Tu situación actual», «En qué deberías enfocarte», «Por qué es importante» y «Cómo te acerca al mundo laboral».
2. **Given** que todavía no existe funcionalidad de Learning para esa pantalla, **When** se presenta el módulo del plan, **Then** se muestran únicamente el título «Tu plan de formación» y el texto «Próximamente», sin cursos ficticios ni recomendaciones derivadas del texto de IA.
3. **Given** que un diagnóstico contiene una respuesta inválida, **When** la pantalla muestra el error, **Then** el módulo del plan permanece independiente y visible.

### Edge Cases

- El intento es inexistente, descartado, incompleto, ajeno al usuario o no tiene respuestas requeridas coherentes.
- El rol no pertenece al catálogo V2 o no tiene requisitos relevantes en la matriz fijada.
- Hay una respuesta explícita de nivel cero; no debe confundirse con una respuesta ausente.
- Dos peticiones simultáneas intentan crear el diagnóstico de la misma combinación.
- El proveedor no está disponible, supera el tiempo de espera o responde con JSON inválido, campos vacíos, campos adicionales o IDs de competencias fuera del contexto.
- El usuario reintenta una generación fallida; una generación exitosa no se puede regenerar manualmente.
- No existen brechas para el rol; el diagnóstico reconoce el cumplimiento de los requisitos evaluados sin inventar deficiencias.
- Existe un borrador activo junto a intentos completados; el borrador no sustituye el contexto explícitamente seleccionado desde el historial.
- Cambian posteriormente las versiones vigentes de prompt o modelo; un diagnóstico exitoso no se reemplaza automáticamente.
- La identidad cromática de un rol no ofrece contraste suficiente en una combinación propuesta para texto o controles.

## Requirements

### Functional Requirements

- **FR-001**: La funcionalidad MUST generar un diagnóstico solo bajo demanda cuando se consulte por primera vez un rol asociado a un intento V2 completado propio. MUST NOT generarlo al completar la autoevaluación, calcular el Matching, consultar el ranking ni para roles distintos del solicitado.
- **FR-002**: La generación y recuperación MUST quedar identificadas por la combinación exacta de `attemptId` y `roleId`. Un intento nuevo es un contexto independiente y MUST NOT sobrescribir diagnósticos anteriores.
- **FR-003**: Un diagnóstico exitoso MUST persistirse y reutilizarse en consultas posteriores para la misma combinación, sin regeneración manual ni regeneración automática por cambios posteriores del modelo o prompt.
- **FR-004**: Si un diagnóstico histórico no existe, el sistema MUST poder generarlo con las respuestas, la versión de cuestionario y la matriz fijadas al intento histórico, además de la versión de prompt vigente al generarlo. MUST NOT sustituirlas por la evaluación más reciente o por un borrador.
- **FR-005**: El contexto enviado al modelo MUST limitarse al rol consultado, su descripción, el porcentaje persistido del Matching y todas las competencias relevantes con los niveles actuales y requeridos, sus descripciones, clasificación, brecha y prioridad determinística. MUST NOT incluir datos personales innecesarios, respuestas originales completas, otros intentos, competencias ajenas al rol, ofertas ni recursos formativos.
- **FR-006**: La prioridad de cada competencia MUST calcularse de forma determinística como `max(0, nivelRequerido - nivelActual) × pesoImportancia`, con peso 2 para esencial y 1 para complementaria. MUST ser independiente del porcentaje de Matching y MUST NOT modificar Matching ni el cálculo de Skill Gap V2.
- **FR-007**: El cálculo de brechas MUST reutilizar el comportamiento determinístico de Skill Gap V2 y los datos versionados del intento. La prioridad de Explainability MUST ser un dato de su contexto y no se presentará como un campo que ya exista en la respuesta de Skill Gap V2.
- **FR-008**: El modelo MUST recibir únicamente competencias relevantes para el rol consultado. MUST preservar todos los valores determinísticos y utilizarlos solo para interpretar y comunicar resultados.
- **FR-009**: La respuesta MUST cumplir el contrato estructurado `currentSituation`, `recommendedFocus`, `importanceExplanation`, `professionalRelevance` y `highlightedCompetencyIds`. Los cuatro campos de texto MUST ser significativos y no vacíos; los identificadores destacados MUST pertenecer a las competencias del contexto; no se permiten campos adicionales.
- **FR-010**: La validación estructural MUST NOT presentarse como garantía absoluta de fidelidad semántica. Respuestas inválidas MUST NOT persistirse como diagnósticos exitosos ni reemplazar un diagnóstico existente.
- **FR-011**: Una generación concurrente para una misma combinación de intento y rol MUST evitar invocaciones duplicadas y MUST devolver un único resultado exitoso reutilizable o comunicar el fallo de forma explícita.
- **FR-012**: El sistema MUST registrar con el diagnóstico exitoso el rol, intento, versión de prompt y modelo utilizado. MUST conservar información operativa disponible —duración, tokens y cantidad de intentos— sin registrar credenciales ni incluir información personal innecesaria en los registros.
- **FR-013**: Las generaciones fallidas MUST permitir reintento exclusivamente mediante una acción explícita del usuario. El sistema MUST NOT aplicar límites propios por usuario, intento o rol ni reintentos automáticos; MUST conservar `attempt_count` solo como metadato operativo. Los límites impuestos por el proveedor externo MUST tratarse como errores del proveedor y no habilitar fallback automático.
- **FR-014**: La indisponibilidad del modelo MUST NOT impedir la consulta de resultados determinísticos ni cambiar estados, respuestas, versiones o resultados de Matching/Skill Gap.
- **FR-015**: La solicitud de diagnóstico MUST requerir un intento completado perteneciente al usuario autenticado y un rol válido del catálogo V2. Intentos incompletos, ajenos, inválidos o históricamente inconsistentes MUST producir un estado de error explícito, sin fallback a otros datos.
- **FR-016**: La pantalla por rol MUST mostrar el emblema, nombre profesional, nivel Junior, porcentaje de preparación, barra azul de progreso, descripción breve del rol, fecha de la evaluación consultada y la aclaración «Autoevaluación declarada; no es una certificación.»; todos los datos MUST corresponder al mismo `attemptId` y `roleId`.
- **FR-017**: Las descripciones aprobadas de los seis roles MUST tener una fuente centralizada en el backend y utilizarse tanto en Formación como en el contexto enviado al modelo. Cada diagnóstico exitoso MUST conservar la descripción exacta usada para que pueda recuperarse junto con el diagnóstico. Las descripciones generales MUST delimitar el rol y MUST NOT agregar competencias o requisitos fuera del contexto determinístico.
- **FR-018**: El emblema y la paleta de rol MUST asociarse de forma consistente a los IDs estables `backend-dev-jr`, `frontend-dev-jr`, `fullstack-dev-jr`, `qa-jr`, `data-analyst-jr` y `cloud-jr`. Los emblemas SVG aprobados MUST utilizarse sin modificar sus trazados y no comunicar certificación, logros, nivel ni posición en ranking.
- **FR-019**: La pantalla MUST presentar el diagnóstico en un recuadro exterior de color intenso de rol, con título «Tu diagnóstico.» y mascota. Los cuatro apartados MUST estar completamente visibles en un interior blanco, con títulos en el color del rol, texto oscuro, separadores discretos y espacio suficiente. MUST NOT usar numeración, barras verticales decorativas, acordeones ni símbolos genéricos de IA.
- **FR-020**: En carga, la pantalla MUST mantener visibles encabezado y plan futuro; dentro del diagnóstico MUST mostrar la pose `thinking`, «Estamos preparando tu diagnóstico» y «Analizamos cómo tus habilidades se relacionan con este rol profesional.», sin porcentaje ficticio.
- **FR-021**: En éxito, la pantalla MUST mostrar el diagnóstico completo y la pose `pointing`. Un diagnóstico reutilizado se muestra sin nueva generación.
- **FR-022**: En error, la pantalla MUST mantener el resto funcional y mostrar «No pudimos preparar tu diagnóstico», «Tus resultados están guardados. Podés volver a intentarlo.» y el botón «Volver a intentar».
- **FR-023**: Debajo del diagnóstico MUST existir una sección separada «Tu plan de formación» que muestre solamente «Próximamente». Explainability MUST NOT crear cursos ficticios ni decidir qué competencias formarán parte del futuro plan a partir del texto producido por el modelo.
- **FR-024**: Ante un perfil sin brechas, el diagnóstico MUST reconocer que la persona cumple los requisitos evaluados y orientar el mensaje hacia consolidar, integrar o demostrar sus capacidades, sin inventar deficiencias.
- **FR-025**: El diagnóstico MUST dirigirse directamente a la persona, con español y voseo, tono profesional, cercano y natural. MUST respetar capacidades, brechas, prioridades y clasificación; MUST NOT inventar experiencia, conocimientos, competencias, dependencias, demanda laboral o garantías de empleabilidad. MUST NOT mencionar innecesariamente la autoevaluación, exponer IDs/niveles internos, recomendar cursos específicos ni repetir brechas sin aportar valor.
- **FR-026**: La presentación MUST respetar el diseño mobile-first de NextRole, Plus Jakarta Sans, navegación inferior con Formación activa, accesibilidad de teclado y tecnologías de asistencia, movimiento reducido y contraste legible para cada color de rol.
- **FR-027**: Los botones «Explorar rol» del ranking V2 MUST conservar íntegramente su diseño, texto, distribución y comportamiento visual; su único cambio MUST ser navegar directamente a Formación del `roleId` seleccionado y el `attemptId` del ranking, sin pasar por el detalle intermedio. El comportamiento MUST ser igual para rankings actuales e históricos. La pantalla MUST permitir volver a `/roles?attemptId={attemptId}`. La entrada general `/formacion`, los demás elementos de navegación, Learning V1 y sus accesos MUST permanecer sin cambios. La exploración MUST NOT persistir un rol objetivo profesional.
- **FR-028**: Para todos los roles la descripción MUST ser exactamente el contenido aprobado en «Catálogo de descripciones profesionales» de esta especificación. La descripción MUST contextualizar la profesión y MUST NOT prevalecer sobre la matriz, niveles, brechas y prioridades al explicar capacidades individuales.
- **FR-029**: El contenido y la operación de la pantalla MUST conservar el mismo `attemptId` y `roleId` durante la consulta del diagnóstico, sin recurrir al intento más reciente si se abrió un ranking histórico explícito.
- **FR-030**: La primera integración MUST utilizar OpenRouter con el modelo inicial configurable `baidu/ernie-5.5-flash-preview:free` y la versión inicial configurable del prompt V5 existente, cuyo contenido MUST permanecer sin modificar. Modelo y versión del prompt MUST registrarse junto con cada diagnóstico exitoso, incluyendo modelo efectivamente servido y hash del prompt cuando estén disponibles. MUST NOT habilitar modelos pagos, fallbacks a modelos pagos ni reintentos automáticos; los fallbacks de proveedor de OpenRouter deberán deshabilitarse.
- **FR-031**: GET de estado MUST ser de solo lectura. En `NOT_STARTED`, Formación MUST solicitar la generación inicial; en `GENERATING`, MUST consultar solo el estado; en `SUCCEEDED`, MUST recuperar el diagnóstico y snapshots persistidos; en `FAILED`, MUST esperar una acción manual. POST `generate` MUST iniciar una llamada únicamente si no existe fila, devolver el éxito existente sin llamar al proveedor, devolver estado en curso sin duplicar llamada, y no reintentar filas fallidas. POST `retry` MUST ser la única operación que reclame una fila `FAILED` o `GENERATING` con lease vencido; vencer un lease no llama al proveedor. Recargar, volver a entrar, recuperar foco o remontar componentes MUST NOT reintentar automáticamente. No se aplican cuotas propias.

### Business Rules

- Matching V2 y brechas Skill Gap V2 permanecen determinísticos y sin modificaciones funcionales.
- Skill Gap V2 calcula todas las brechas relevantes para el intento y rol consultados; su contrato no incluye prioridad, porcentaje, descripciones de nivel ni resultados Matching.
- Explainability puede calcular la prioridad aprobada sobre la salida versionada de brechas sin cambiar el contrato de Skill Gap V2. Para pesos, `ESSENTIAL = 2` y `COMPLEMENTARY = 1`; `NOT_RELEVANT` se excluye.
- El contexto de evaluación se obtiene del intento explícito: respuestas y descripciones desde su versión de cuestionario; requisitos e importancia desde su versión de matriz; porcentaje desde el Matching persistido del mismo intento y rol.
- La primera integración usa OpenRouter y `baidu/ernie-5.5-flash-preview:free` con el prompt V5 existente e inmutable. El modelo y la versión del prompt se configuran y registran; no hay fallback pago ni reintento automático. Esta decisión inicial no fija una dependencia permanente ni impide evaluar otro modelo en una etapa futura aprobada.
- La extensión debe ser concisa, pero esta especificación no fija un límite de palabras hasta refinar el prompt.
- La prioridad formativa es independiente de Learning. El futuro módulo Learning no depende de `recommendedFocus` ni de ninguna otra frase generada por IA.

### Catálogo de descripciones profesionales

Las siguientes descripciones son contenido oficial de NextRole, centralizado en el backend y utilizado en Formación y en el contexto del modelo. Delimitan la interpretación general del rol, pero no agregan requisitos al contexto de competencias.

| Rol | Identificador | Descripción oficial |
|---|---|---|
| Backend Developer | `backend-dev-jr` | Se encarga de desarrollar y mantener la lógica interna de las aplicaciones. Sus responsabilidades incluyen implementar reglas de negocio, desarrollar APIs, gestionar bases de datos e integrar servicios externos. Contribuye a que los sistemas procesen información de manera segura, eficiente y confiable. |
| Frontend Developer | `frontend-dev-jr` | Se encarga de desarrollar las interfaces visuales e interactivas de las aplicaciones. Sus responsabilidades incluyen construir componentes, integrar APIs y gestionar el comportamiento de la interfaz. Busca garantizar una experiencia de usuario intuitiva, accesible, eficiente y adaptable a diferentes dispositivos. |
| Full Stack Developer | `fullstack-dev-jr` | Participa en el desarrollo integral de aplicaciones, trabajando tanto en sus interfaces como en su lógica interna. Sus responsabilidades incluyen construir funcionalidades, desarrollar e integrar APIs, gestionar bases de datos y conectar los distintos componentes del sistema. Su visión integral le permite comprender cómo interactúan las diferentes capas de una aplicación. |
| QA | `qa-jr` | Se encarga de contribuir a la calidad del software mediante la planificación, el diseño y la ejecución de pruebas. Sus responsabilidades incluyen verificar funcionalidades, identificar y documentar defectos, validar correcciones y comprobar que las aplicaciones cumplan sus requisitos. Colabora con los equipos de desarrollo para mejorar la confiabilidad del producto y prevenir errores. |
| Data Analyst | `data-analyst-jr` | Se encarga de transformar datos en información útil para la toma de decisiones. Sus responsabilidades incluyen obtener, limpiar, procesar y analizar datos, identificar patrones e interpretar indicadores de negocio. Utiliza técnicas estadísticas y herramientas de visualización para comunicar resultados, fundamentar conclusiones y contribuir a la resolución de problemas. |
| Cloud | `cloud-jr` | Se encarga de implementar, administrar y mantener infraestructura y servicios en la nube. Sus responsabilidades incluyen configurar recursos de procesamiento, almacenamiento y redes, automatizar tareas operativas y monitorear el funcionamiento de los sistemas. Contribuye a que las soluciones sean seguras, confiables, escalables y eficientes en el uso de recursos. |

### Identidad visual de roles

La asociación de emblemas y paletas vive exclusivamente en el frontend. Los SVG oficiales se proporcionarán antes de Implement y se utilizarán sin modificar sus trazados.

| Rol | Identificador | Contorno original | Relleno original | Encabezado | Diagnóstico |
|---|---|---|---|---|---|
| Backend Developer | `backend-dev-jr` | `#0065BE` | `#50B1F3` | `#E8F4FC` | `#0065BE` |
| Frontend Developer | `frontend-dev-jr` | `#6B209E` | `#CB91E5` | `#F5EBFC` | `#6B209E` |
| Full Stack Developer | `fullstack-dev-jr` | `#007B89` | `#2BCDCE` | `#E5F8F8` | `#007B89` |
| QA | `qa-jr` | `#F86E00` | `#FECD83` | `#FFF3E1` | `#B95408` |
| Data Analyst | `data-analyst-jr` | `#4F9918` | `#D0F493` | `#F0F8E5` | `#397A20` |
| Cloud | `cloud-jr` | `#3C5978` | `#9DADC1` | `#EAF0F5` | `#3C5978` |

Las paletas deben probarse con el texto y los controles reales para validar contraste y accesibilidad; esos ajustes no deben alterar los SVG originales.

### Quality, Security & Accessibility Requirements

- El acceso a intento y diagnóstico respeta autenticación, propiedad del usuario y aislamiento entre intentos.
- Solo se envía el contexto mínimo permitido al proveedor; las credenciales nunca se incluyen en contenido ni registros visibles.
- No se filtran datos personales, otros intentos ni datos de mercado al proveedor.
- El diagnóstico y las cuatro secciones son navegables por teclado y legibles por tecnologías de asistencia; estados de carga y error se anuncian semánticamente.
- Los colores de texto y superficies cumplen contraste WCAG 2.1 AA sin alterar los SVG originales; movimiento no esencial respeta `prefers-reduced-motion`.
- Los errores del proveedor no bloquean ni modifican resultados determinísticos.

### Key Entities

- **Diagnóstico de rol**: resultado estructurado persistido para un intento completado y un rol; contiene sus cuatro textos, competencias destacadas, versión de prompt/modelo y metadatos operativos disponibles.
- **Contexto de diagnóstico**: proyección de lectura derivada del intento, matching y brechas versionadas para un único rol; no contiene datos personales innecesarios.
- **Prioridad formativa**: valor determinístico por competencia calculado para el contexto de Explainability según brecha e importancia; no es una entidad ni un campo de Skill Gap V2.
- **Presentación de rol**: emblema, colores, nombre, nivel y descripción breve asociados a un ID estable; no constituye selección permanente del objetivo profesional.
- **Plan de formación futuro**: módulo visual separado, sin recomendaciones funcionales en esta versión.

## Success Criteria

### Measurable Outcomes

- **SC-001**: Para una combinación válida sin diagnóstico, una consulta genera como máximo un resultado exitoso persistido; consultas posteriores de esa combinación reutilizan exactamente ese resultado sin nuevas llamadas al modelo.
- **SC-002**: Una respuesta estructuralmente inválida nunca se muestra ni persiste como un diagnóstico exitoso; las cuatro secciones válidas se muestran completas.
- **SC-003**: En todas las consultas históricas, el rol, porcentaje, respuestas, requisitos, descripciones de niveles, brechas e importancia corresponden al mismo `attemptId` y sus versiones fijadas.
- **SC-004**: Los seis roles V2 cuentan con una asociación inequívoca entre ID, emblema y paleta; ningún emblema se interpreta como logro, nivel o ranking.
- **SC-005**: La indisponibilidad del modelo no altera el Matching, Skill Gap, estado del intento ni el acceso al resto de la pantalla.
- **SC-006**: La pantalla distingue visualmente y en su estructura accesible carga, éxito, error, ausencia de brechas y estado futuro del plan; no muestra porcentajes de carga ficticios ni cursos inventados.
- **SC-007**: Para cada competencia relevante, la prioridad enviada al modelo coincide con `max(0, requerido - actual) × 2` para esencial o `× 1` para complementaria; las brechas cubiertas tienen prioridad cero.
- **SC-008**: Todos los enlaces habilitados al diagnóstico conservan la combinación de intento y rol elegida y no usan automáticamente el intento más reciente cuando la persona abrió un resultado histórico explícito.
- **SC-009**: La identidad visual cumple los criterios de contraste WCAG 2.1 AA en los fondos y textos usados y funciona en anchos móviles sin desbordamiento horizontal.
- **SC-010**: Cada botón «Explorar rol» del ranking V2 conserva su presentación y abre directamente Formación con el rol e intento del resultado; volver abre el ranking de ese mismo intento.
- **SC-011**: Las seis descripciones presentadas y enviadas al modelo coinciden exactamente con el catálogo oficial de esta especificación, y cada diagnóstico persistido permite recuperar la descripción que se utilizó.

## Assumptions

- La persona está autenticada y el backend existente controla propiedad y estado del intento.
- Solo son elegibles evaluaciones V2 completadas y roles del catálogo V2.
- Las tablas/servicios de Matching y Skill Gap V2 conservan sus contratos actuales; la prioridad aprobada pertenece al contexto de Explainability.
- Los recursos SVG de rol serán proporcionados antes de la implementación y se usarán sin modificar trazados.
- Las descripciones oficiales de rol son una fuente centralizada de backend; los emblemas y paletas se asocian a los IDs estables exclusivamente en frontend.
- El módulo y pantalla quedan dentro de la aplicación modular existente; decisiones de servicios, persistencia, transacciones, concurrencia y contratos de transporte se definen en Plan. La decisión inicial de modelo/proveedor y prompt está registrada en Clarifications y FR-030.
- La nueva pantalla no rediseña Inicio, el contenido ni la presentación del ranking de Roles, la entrada general a Formación ni la navegación inferior. Solo cambia el destino de los botones V2 «Explorar rol» según FR-027.

## Dependencies and Constraints

- Autoevaluación V2 debe haber completado y persistido el intento, sus versiones, respuestas y resultados de Matching.
- Skill Gap V2 debe permanecer como fuente determinística de brechas, sin ampliación tácita de su DTO.
- La generación requiere conectividad con OpenRouter; su falla no afecta datos determinísticos. La primera integración no deriva solicitudes a modelos pagos ni otros proveedores.
- Los archivos de emblemas oficiales todavía no están presentes y serán entregados antes de la implementación.
- La pantalla intermedia de detalle del rol tiene rutas y consumidores actuales. El destino V2 cambia solo para los botones «Explorar rol» del ranking; Plan revisó sus dependencias y conserva la pantalla/rutas para los consumidores existentes. No se eliminan rutas V1.
- No se incluyen migraciones, contratos HTTP ni detalles de entidades de persistencia en esta especificación funcional; se evaluarán durante Plan.

## Cross-Specification Compatibility

- La decisión posterior de `003-explainability-formacion` reemplaza únicamente el destino de «Explorar rol» para rankings V2. La decisión 21 y el plan de `001-autoevaluacion-matching` ya se reconciliaron para documentar el acceso directo a Formación y el retorno al ranking del mismo intento; se preservaron sus cambios locales paralelos. La entrada general `/formacion`, la pantalla intermedia y los enlaces V1 permanecen intactos.
- `002-skill-gap-v2` excluye expresamente prioridad, UI y persistencia de brechas. La prioridad determinística de esta especificación pertenece a Explainability y se calcula con los niveles, brechas e importancia entregados por Skill Gap V2; no amplía su contrato ni cambia sus cálculos.
- La reconciliación documental de la decisión 21 y la sección de navegación de `001-autoevaluacion-matching/plan.md` se realizó durante Plan, después de la aprobación explícita; no se modificó `002-skill-gap-v2`.

## Preconditions for Implementation

- El Plan técnico está cerrado y los artefactos Tasks deben revisarse antes de Implement.
- Los seis SVG oficiales deben recibirse antes de ejecutar las tareas de identidad visual; su ausencia no bloquea backend ni generación de Tasks.
- Cualquier validación contra OpenRouter real requiere autorización expresa posterior; las pruebas automatizadas usan proveedor simulado.
- La validación visual de paletas/contraste requiere los emblemas y debe realizarse en móvil y escritorio.
