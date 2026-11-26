# Feature Specification: Autoevaluación técnica y matching de roles IT

**Feature Branch**: [001-autoevaluacion-matching]
**Created**: 2026-11-20
**Status**: Approved — incluye la ampliación aprobada para Inicio y descarte consistente. Los niveles esperados admitidos son 1–4; la matriz V2 vigente conserva sus asignaciones aprobadas de 1–3.

## Clarifications

### Session 2026-11-25

- No quedan ambigüedades funcionales abiertas para esta ampliación. Inicio muestra el recorrido solo con borrador activo; sin borrador muestra una tarjeta motivacional y, si hay historial, la última evaluación.
- El descarte desde Inicio y Habilidades requiere confirmación, descarta únicamente el borrador y no inicia otro intento. Se conserva el aspecto actual de Habilidades y su acceso explícito para comenzar.
- La última evaluación usa fecha y resultados persistidos; conserva los enlaces existentes a resultados por `attemptId` e historial. Se reutilizan los contratos actuales y no se modifican backend ni persistencia.
- La antigua orientación se elimina de Inicio y solo mediante el rótulo y la tira indicados en Skill Gap. Se conservan servicios compartidos vigentes, el resto del diagnóstico y Learning; en Habilidades el recorrido se presenta solo mientras exista un borrador activo.
- Se eliminan también de Skill Gap únicamente el rótulo «PASO 5 DE 6» y su tira de seis segmentos. Se conserva el diagnóstico completo; los pasos independientes de registro, onboarding y el progreso propio de la autoevaluación no forman parte de la ruta antigua y permanecen.

### Session 2026-11-25 — Revisión manual y ampliación aprobada

- **Decisión sustituida (2026-11-25)**: la propuesta anterior de precargar en una reevaluación las respuestas de la última evaluación completa ya no aplica. La decisión aprobada vigente es que toda evaluación nueva comienza vacía. Se conserva el `source_attempt_id` solo como relación de procedencia; no se borran columnas, migraciones ni datos históricos.
- El recorrido se muestra solo para un borrador activo tanto en Inicio como en Habilidades. El porcentaje numérico deja de ser visible, pero el progreso sigue disponible para accesibilidad y continúa controlando el camino y la posición de la mascota.
- La tarjeta azul de Inicio necesita mostrar el título, mensajes deterministas según el progreso, la descripción, continuar y descartar. El descarte se confirma con un modal compartido.
- Habilidades reemplaza el recorrido cuando no hay borrador por una presentación estática con mascota y CTA según exista historial. Los estados vacíos de Roles y Formación y la ruta directa de Roles para el último resultado persistido se incorporan a esta ampliación.
- Formación se limita a estado vacío e invitación a Roles. Las recomendaciones existentes por rol, Learning V1, Skill Gap, la navegación general y las demás funcionalidades no se rediseñan.
- Las observaciones manuales reportadas se registran como feedback de una revisión, no como validación exhaustiva de estados, animación o tamaños.
- Copys de progreso aprobados para Inicio: 0% primera evaluación «¡Empezá a descubrir tu potencial!» / «Conocé tus habilidades y descubrí qué roles se adaptan a vos.»; 0% con historial «¡Un nuevo comienzo!» / «Volvé a evaluar tus habilidades y descubrí cuánto avanzaste.»; 1–39% «¡Cada paso cuenta!» / «Seguí descubriendo tus habilidades.»; 40–74% «¡Vas por buen camino!» / «Seguí evaluando tus habilidades para descubrir tu perfil profesional.»; 75–99% «¡Ya casi llegás!» / «Te falta poco para conocer tus resultados.»; 100% «¡Llegaste a la meta!» / «Finalizá tu autoevaluación para conocer tus resultados.»

## User Scenarios & Testing

### User Story 1 — Completar la autoevaluación (Priority: P1)

Como estudiante que busca su primera oportunidad IT, quiero responder una pregunta por competencia con niveles que describan comprensión, aplicación y criterio para declarar mi perfil sin elegir rol por adelantado ni acreditar empleo.

**Acceptance Scenarios**:
1. **Given** un intento nuevo, **When** se muestra el cuestionario, **Then** contiene 31 competencias, una pregunta y cinco niveles 0–4 cada una.
2. **Given** una pregunta vacía, **When** el estudiante elige nivel 0, **Then** queda respondida y se distingue de no respondida.
3. **Given** un intento incompleto asociado al usuario, **When** guarda una respuesta o regresa, **Then** el progreso persiste, se recupera y sigue editable.
4. **Given** que no tiene empleo, **When** responde, **Then** puede basarse en comprensión y aplicación; colaborar incluye proyectos académicos, personales o independientes con otras personas.
5. **Given** respuestas guardadas, **When** consulta su diagnóstico de competencias, **Then** ve los niveles 0–4 que declaró en la autoevaluación, diferenciados de los porcentajes de matching por rol.

### User Story 2 — Conocer preparación para seis roles (Priority: P1)

Como estudiante, quiero reutilizar mis respuestas técnicas para conocer mi porcentaje de preparación para seis roles IT.

**Acceptance Scenarios**:
1. **Given** las 31 respuestas, **When** se finaliza, **Then** se obtiene un porcentaje para Backend, Frontend, Full Stack, QA, Data Analyst y Cloud.
2. **Given** una competencia compartida, **When** se calcula cada rol, **Then** se reutiliza la respuesta y se aplican los requisitos del rol.
3. **Given** una pregunta vacía, **When** se solicitan resultados, **Then** no se calcula matching.
4. **Given** una competencia no relevante, **When** se calcula el rol, **Then** se excluye.
5. **Given** nivel superior al esperado, **When** se calcula el aporte, **Then** no supera 1.
6. **Given** resultados distintos, **When** se ordenan, **Then** se usa precisión completa y se muestra un decimal.
7. **Given** empate exacto, **When** se ordena, **Then** se aleatoriza una vez y se persiste el orden empatado.
8. **Given** mismas respuestas y distintos intereses, objetivos o proyectos, **When** se calcula, **Then** los porcentajes no cambian.

### User Story 3 — Reevaluarse y conservar historial (Priority: P2)

Como estudiante, quiero crear intentos nuevos y conservar resultados anteriores con el contexto de versiones que los originó.

**Acceptance Scenarios**:
1. **Given** una evaluación completa y sin borrador activo, **When** inicia una reevaluación, **Then** crea un intento nuevo vacío, asociado a la completa de origen solo como referencia, sin copiar sus respuestas.
2. **Given** que existe un borrador activo, **When** el usuario vuelve a la autoevaluación, **Then** recupera ese borrador sin crear otro.
3. **Given** un borrador activo, **When** el usuario lo descarta explícitamente, **Then** queda descartado sin crear otro intento; una evaluación nueva requiere una acción explícita posterior.
4. **Given** una evaluación completa, **When** el historial se consulta, **Then** aparece de la más reciente a la más antigua con fecha, resultados y acceso al detalle.
5. **Given** una evaluación incompleta, **When** se guarda, **Then** no reemplaza el perfil vigente ni aparece en el historial de completas.
6. **Given** un resultado histórico, **When** se conserva, **Then** queda asociado a las versiones de cuestionario y matriz.

### User Story 4 — Visualizar el progreso en un recorrido animado (Priority: P2)

Como estudiante que retoma o completa su autoevaluación, quiero ver el progreso como un recorrido continuo para reconocer visualmente cuánto avancé.

**Acceptance Scenarios**:
1. **Given** que existe un borrador activo, **When** se muestra el recorrido en Inicio o Habilidades, **Then** el progreso y la pose corresponden exclusivamente al borrador actual.
2. **Given** un borrador activo, **When** se muestra el recorrido, **Then** el progreso corresponde a las respuestas guardadas divididas por el total del mismo intento; el nivel 0 cuenta como respuesta guardada.
3. **Given** una evaluación completada y ningún borrador activo, **When** se muestran Inicio o Habilidades, **Then** no se muestra camino, bandera, animación de recorrido ni porcentaje del recorrido.
4. **Given** que un borrador tiene todas sus respuestas guardadas pero no fue finalizado, **When** se muestra el recorrido, **Then** el camino alcanza el final sin celebrar la finalización.
5. **Given** evaluaciones anteriores y un borrador activo, **When** se muestra el recorrido, **Then** representa solo el progreso del borrador.
6. **Given** que está activada la preferencia de movimiento reducido, **When** se muestra el recorrido, **Then** la mascota aparece directamente en la posición correspondiente sin desplazamiento animado.
7. **Given** cualquier estado donde el recorrido sea visible, **When** se muestra, **Then** hay un solo camino continuo sin hitos, el tramo coloreado termina junto a la mascota y el porcentaje solo está disponible de forma accesible, no como texto numérico visible.

### User Story 5 — Consultar Inicio según el estado de la autoevaluación (Priority: P1)

Como estudiante, quiero que Inicio muestre solo las acciones y los resultados que corresponden a mis evaluaciones actuales y completadas.

**Acceptance Scenarios**:
1. **Given** que no hay borrador ni historial completado, **When** se carga Inicio, **Then** veo mi saludo y la tarjeta motivacional para usuario nuevo; no se muestra recorrido ni tarjeta de última evaluación.
2. **Given** que existe un borrador, **When** se carga Inicio, **Then** veo la tarjeta azul con `AssessmentJourney`, progreso del intento activo, y las acciones para continuar o descartar; no veo la tarjeta motivacional.
3. **Given** que existe una evaluación completada y no hay borrador, **When** se carga Inicio, **Then** veo la tarjeta motivacional recurrente y la tarjeta de la última evaluación completada, sin el recorrido de una evaluación anterior.
4. **Given** que existe un borrador y evaluaciones completadas, **When** se carga Inicio, **Then** el recorrido representa solo el borrador y la tarjeta de última evaluación conserva los datos persistidos más recientes.
5. **Given** un borrador con todas sus respuestas guardadas pero no finalizado, **When** se carga Inicio, **Then** el recorrido muestra 100%, mantiene continuar y descartar, y no representa finalización ni celebración.
6. **Given** que el estado de evaluación o historial se está cargando o falló, **When** se muestra Inicio, **Then** no se interpreta la ausencia de datos como estado vacío y no se ofrecen acciones incompatibles.
7. **Given** que existe historial, **When** se muestra la última evaluación, **Then** la fecha, el rol mejor posicionado y el porcentaje provienen de los resultados persistidos del intento más reciente; sus accesos abren `/roles?attemptId=…` y `/habilidades/historial`.
8. **Given** que se confirma la finalización de una evaluación, **When** se vuelve a Inicio, **Then** ya no se muestra el borrador, la última evaluación corresponde al intento recién completado y no se inicia otra evaluación automáticamente.
9. **Given** un borrador activo, **When** se muestra la tarjeta azul, **Then** su fondo, título, mensaje de progreso, descripción y acciones son legibles; no muestra porcentaje numérico.
10. **Given** una reevaluación vacía, **When** se muestra Inicio, **Then** el mensaje de inicio reconoce que ya existen evaluaciones anteriores y no altera sus resultados.

### User Story 6 — Descartar el borrador sin iniciar otra evaluación (Priority: P1)

Como estudiante, quiero descartar explícitamente el borrador activo desde Inicio o Habilidades sin perder mis evaluaciones completadas ni comenzar otro intento automáticamente.

**Acceptance Scenarios**:
1. **Given** que hay un borrador activo, **When** elijo descartarlo desde Inicio o Habilidades, **Then** se abre un modal centrado con el título «¿Descartar autoevaluación?», la pérdida de progreso explicada y las acciones Cancelar y Descartar.
2. **Given** la confirmación de descarte, **When** cancelo, **Then** el borrador, su progreso y la presentación de la pantalla permanecen intactos.
3. **Given** que confirmo el descarte, **When** el backend responde con éxito, **Then** el borrador deja de estar activo, se conserva el historial, no se crea otra evaluación y desaparece el recorrido de Inicio y Habilidades.
4. **Given** un descarte exitoso desde Inicio o Habilidades, **When** termina la operación, **Then** permanezco en la pantalla desde la que inicié el descarte.
5. **Given** que el backend rechaza o no completa el descarte, **When** se informa el error en el modal, **Then** el borrador y su presentación siguen disponibles y no se simula éxito.
6. **Given** que el descarte está en curso, **When** se solicita confirmarlo otra vez, **Then** se evita una segunda solicitud.

### User Story 7 — Consultar el diagnóstico sin indicadores de orientación obsoletos (Priority: P2)

Como estudiante que consulta las brechas de un rol, quiero ver el diagnóstico sin indicadores de una antigua ruta de orientación que no representa el progreso de esa pantalla.

**Acceptance Scenarios**:
1. **Given** que abro el diagnóstico de brechas, **When** se muestra Skill Gap, **Then** no veo «PASO 5 DE 6» ni la tira de seis segmentos de orientación.
2. **Given** que el diagnóstico se muestra, **When** consulto sus brechas, **Then** se conservan el título, los datos, las barras por competencia, la navegación y el acceso a formación.
3. **Given** que uso el registro, el onboarding de perfil o la autoevaluación, **When** esos flujos muestran su propio progreso, **Then** sus indicadores independientes permanecen sin cambios.

### User Story 8 — Consultar Habilidades según el estado de evaluación (Priority: P1)

Como estudiante, quiero que Habilidades muestre el recorrido únicamente para mi borrador activo y una invitación clara para empezar o reevaluarme cuando no exista borrador.

**Acceptance Scenarios**:
1. **Given** que no tengo borrador ni historial, **When** abro Habilidades, **Then** veo una mascota estática, «Descubrí tus habilidades», un mensaje invitando a completar mi primera autoevaluación y «Comenzar evaluación», sin recorrido ni porcentaje.
2. **Given** que tengo historial y no tengo borrador, **When** abro Habilidades, **Then** veo una mascota estática, «Seguí evolucionando», un mensaje invitando a reevaluar, «Comenzar nueva evaluación» y el acceso al historial, sin recorrido ni porcentaje.
3. **Given** que tengo un borrador activo, **When** abro Habilidades, **Then** veo su recorrido, continuar y descartar; el historial continúa accesible cuando corresponda.
4. **Given** que descarto el único borrador con éxito y existe historial, **When** termina la operación, **Then** el recorrido desaparece; no se representa el historial como un recorrido completado.

### User Story 9 — Consultar Roles y Formación según evaluaciones persistidas (Priority: P1)

Como estudiante, quiero que Roles y Formación orienten el siguiente acceso usando mis evaluaciones reales, sin rankings ficticios ni exigir un rol objetivo permanente.

**Acceptance Scenarios**:
1. **Given** que no completé evaluaciones, **When** abro Roles, **Then** veo «Descubrí tu próximo rol», «Completá tu autoevaluación para conocer qué perfiles profesionales se adaptan mejor a tus habilidades.», y «Ir a Habilidades», sin resultados ficticios.
2. **Given** que tengo un borrador y ninguna completa, **When** abro Roles, **Then** veo una invitación para continuar la evaluación activa.
3. **Given** que hay una evaluación completada, **When** ingreso a Roles sin `attemptId`, **Then** veo el ranking persistido más reciente aunque haya otro borrador activo.
4. **Given** un enlace de Roles con `attemptId`, **When** lo abro, **Then** veo los resultados persistidos de ese intento histórico, no los últimos resultados sustituidos silenciosamente.
5. **Given** que no completé evaluaciones, **When** abro Formación, **Then** veo «Prepará tu próximo paso», «Conocé tus habilidades para descubrir qué necesitás aprender y seguir creciendo profesionalmente.», y «Ir a Habilidades», sin referencia a elegir rol ni accesos a ranking.
6. **Given** que hay una evaluación completada pero no exploré un rol, **When** abro Formación, **Then** veo una invitación a explorar roles, sin exigir elegir un objetivo permanente.
7. **Given** que tengo una evaluación completada y un borrador activo, **When** abro Roles directamente, **Then** veo el ranking persistido de la última evaluación con una aclaración de contexto, pero sin acciones «Explorar rol» ni accesos directos a su diagnóstico, brechas o formación.
8. **Given** que tengo historial y un borrador activo, **When** abro Formación, **Then** veo «Prepará tu próximo paso» y «Completá tu autoevaluación para descubrir qué habilidades podés seguir desarrollando», con acceso para continuar el borrador y sin «Explorar roles» ni contenido asociado a intentos anteriores.
9. **Given** que consulto explícitamente un resultado histórico mediante `attemptId`, **When** navego sus resultados, diagnóstico o accesos asociados, **Then** el contexto permanece fijado a ese intento y no se mezcla con otro intento ni con el borrador actual.
10. **Given** que descarto el borrador y hay una evaluación completada, **When** vuelvo a Roles o Formación, **Then** Roles vuelve a permitir explorar el último ranking y Formación vuelve a invitar a explorar roles, sin restaurar un rol o diagnóstico seleccionado previamente.

### Edge Cases

- Nivel 0 es válido; una pregunta vacía impide calcular.
- Competencias no relevantes quedan fuera del numerador y denominador.
- Cambios de versión no reinterpretan silenciosamente resultados históricos.
- Al volver a la autoevaluación se recupera el único borrador activo; debe descartarse explícitamente antes de que el usuario pueda iniciar otro, y el descarte no inicia por sí mismo una nueva evaluación. El historial lista solo evaluaciones completas. Las comparaciones gráficas entre evaluaciones quedan fuera del alcance actual.

## Requirements

### Functional Requirements

- **FR-001**: MUST existir un cuestionario de 31 competencias, una pregunta y cinco niveles 0–4 por competencia, según docs/V2/autoevaluacion-matriz-vigente.md.
- **FR-002**: Cada competencia MUST responderse una vez y reutilizarse entre roles.
- **FR-003**: Nivel 0 MUST ser válido y distinto de no respondida.
- **FR-004**: Los niveles MUST reflejar comprensión, aplicación y criterio técnico sin exigir empleo profesional.
- **FR-005**: MUST incluir exactamente Backend, Frontend, Full Stack, QA, Data Analyst y Cloud.
- **FR-006**: MUST utilizar la matriz definitiva del Anexo B y el catálogo V2 vigente.
- **FR-007**: Esencial MUST pesar 2; complementaria MUST pesar 1; no relevante MUST quedar excluida.
- **FR-008**: Matching MUST medir solo preparación técnica; intereses, objetivos y proyectos MUST NOT modificarlo.
- **FR-009**: MUST calcularse solo con las 31 preguntas respondidas.
- **FR-010**: Aporte individual MUST ser min(nivel declarado / nivel esperado, 1).
- **FR-011**: Porcentaje por rol MUST ser 100 × suma(peso × aporte) / suma(pesos), sobre competencias relevantes.
- **FR-012**: Superar nivel esperado MUST NOT aumentar aporte.
- **FR-013**: Una evaluación completa MUST producir seis porcentajes y ordenarlos de mayor a menor con precisión completa. El porcentaje mostrado MUST redondearse a un decimal mediante HALF_UP; ese redondeo MUST NOT intervenir en puntuación, orden ni detección de empate exacto.
- **FR-014**: Ante empate exacto de precisión completa, el orden MUST aleatorizarse una sola vez y persistirse.
- **FR-015**: Mismos datos/versiones MUST producir mismos porcentajes; el orden persistido de empates se conserva.
- **FR-016**: Respuestas incompletas MUST guardarse automáticamente, persistentemente y asociadas al usuario; progreso recuperable al volver.
- **FR-017**: Respuestas MUST ser editables durante el intento y los finalizados MUST ser inmutables.
- **FR-018**: Cuando no hay borrador activo y se crea un intento nuevo, sus respuestas MUST comenzar vacías aunque exista historial. Una reevaluación MUST NOT copiar ni mostrar como seleccionadas respuestas anteriores. Si existe una completa de origen, `source_attempt_id` puede conservar la procedencia del intento sin implicar copia; completar o editar el borrador nuevo MUST NOT modificar datos ni resultados anteriores. Si ya existe un borrador, la acción MUST recuperarlo con las respuestas propias que tenga guardadas en vez de crear un segundo intento.
- **FR-019**: Intentos finalizados y resultados MUST conservarse inmutables asociados a versiones de cuestionario y matriz.
- **FR-020**: Un incompleto MUST NOT reemplazar el perfil técnico vigente.
- **FR-021**: Resultados MUST presentarse como autoevaluación declarada, no certificación.
- **FR-022**: Matching y Skill Gap MUST usar reglas deterministas; la IA posterior puede explicar, pero MUST NOT alterar porcentajes, brechas ni prioridades calculadas por Java.
- **FR-023**: Skill Gap V2 y explicabilidad con IA se especificarán después. Se conserva el diagnóstico existente como visualización de los niveles declarados en la autoevaluación V2, separado del matching por rol; no se incorporan métricas diagnósticas nuevas. Esta ampliación cubre Inicio, la presentación de Habilidades, el modal de descarte, estados vacíos de Roles y la entrada general a Formación. No implementa Learning V2 ni modifica recomendaciones de formación por rol.
- **FR-024**: Decisiones V2 y catálogo/matriz vigentes prevalecen sobre PDF históricos y referencias V1.
- **FR-025**: No se definen entidades JPA, tablas SQL, endpoints ni migraciones; corresponden al Plan.
- **FR-026**: El versionado del cuestionario y de la matriz MUST ser independiente. Cambios en preguntas, opciones o competencias generan nueva versión de cuestionario; cambios en requisitos de roles generan nueva versión de matriz.
- **FR-027**: Cada borrador MUST conservar las versiones de cuestionario y matriz asignadas al crearse, aunque se publiquen versiones posteriores. Los intentos finalizados MUST ser inmutables.
- **FR-028**: MUST existir como máximo un borrador activo por usuario. Al volver se MUST recuperar ese borrador. Descartarlo MUST requerir confirmación y solo cambiar su estado; MUST NOT crear automáticamente otra evaluación. Una evaluación nueva solo se crea mediante una acción explícita posterior, sin duplicar borradores activos.
- **FR-029**: El historial MUST incluir solo evaluaciones completas, ordenadas de la más reciente a la más antigua, con fecha, resultados y acceso al detalle. Los borradores se recuperan desde el flujo de autoevaluación. Las comparaciones gráficas entre evaluaciones MUST quedar fuera de esta fase.
- **FR-030**: No se requiere migrar, convertir ni conservar respuestas históricas V1 como datos de evaluación V2. MUST NOT diseñarse conversión automática; no se borrarán datos ni se ejecutarán operaciones destructivas sin autorización explícita.
- **FR-031**: Cada uno de los seis roles MUST tener un identificador estable, independiente de su etiqueta visible.
- **FR-032**: El nivel esperado de cada competencia relevante MUST poder ser 1, 2, 3 o 4. La matriz V2 actualmente aprobada MUST conservar exactamente sus 186 asignaciones, que utilizan niveles esperados de 1 a 3; una competencia no relevante MUST seguir excluida del cálculo.
- **FR-033**: El diagnóstico de competencias MUST mostrar los niveles declarados 0–4 del cuestionario V2, separado de los porcentajes y el ranking de matching. MUST NOT sustituirlo por matching ni derivar métricas nuevas; la fuente de datos debe ser la evaluación V2 vigente.
- **FR-034**: El recorrido visual MUST representar el progreso como un único camino continuo, sin checkpoints, divisiones o hitos asociados a roles, áreas, porcentajes intermedios, orden de preguntas o cantidad fija de competencias. El tramo coloreado MUST terminar en la posición de la mascota y ambos MUST corresponder al mismo progreso.
- **FR-035**: El progreso interno del recorrido MUST derivarse exclusivamente de las respuestas guardadas y el total de preguntas del borrador activo; una respuesta explícita de nivel 0 MUST contar como guardada. Sin borrador no se representa el recorrido, tanto si no existe historial como si hay evaluaciones completadas.
- **FR-036**: La confirmación de finalización MUST depender del estado `COMPLETED`, no de que el borrador tenga todas sus respuestas guardadas. Si hay un borrador activo, su progreso MUST prevalecer visualmente sobre resultados completados anteriores.
- **FR-037**: El recorrido MUST NOT mostrar un porcentaje numérico visible ni incorporar otra barra de progreso. El progreso calculado MUST permanecer disponible mediante los atributos accesibles del indicador; el camino coloreado y la posición de la mascota MUST mantener contraste y representar ese mismo progreso.
- **FR-038**: El componente del recorrido MUST poder reutilizarse sin depender de preguntas, competencias, áreas o roles; recibirá el progreso objetivo y el estado de finalización. MUST respetar `prefers-reduced-motion`, mostrando directamente el estado objetivo cuando esté activado.
- **FR-039**: La animación de entrada MUST ser finita, ajustar el desplazamiento al ciclo de caminata, detenerse al alcanzar la posición objetivo y mantener una duración máxima configurable que no exceda 1.76 segundos. Los controles visuales deben permanecer dentro de la tarjeta actual y responder a anchos móviles y de escritorio sin desbordamiento.
- **FR-040**: Inicio MUST derivar su presentación del estado independiente de borrador activo e historial completado, usando los datos y contratos existentes. Mientras se cargan los datos o si no se pueden obtener, MUST NOT tratarse la situación como ausencia de información ni habilitar acciones incompatibles.
- **FR-041**: La tarjeta azul de autoevaluación y su recorrido animado MUST mostrarse en Inicio solo si existe un borrador activo. Debe mostrar título legible, paisaje, camino, meta, mascota, mensaje determinista según progreso, descripción breve y las acciones «Continuar evaluación» y «Descartar borrador». No MUST mostrar porcentaje numérico. Continuar MUST recuperar el borrador activo y MUST NOT crear otro intento.
- **FR-042**: En Inicio, un borrador con todas las respuestas guardadas pero aún no finalizado MUST seguir mostrándose como borrador al 100%; MUST conservar continuar y descartar y MUST NOT mostrar estado completado ni celebración.
- **FR-043**: Cuando Inicio no tiene borrador activo MUST mostrar una tarjeta motivacional con contenido distinto según exista historial completado. Esa tarjeta MUST ser independiente de cualquier ruta o estado de orientación anterior.
- **FR-044**: Si y solo si existe historial completado, Inicio MUST mostrar una tarjeta de la evaluación completada más reciente. La fecha, el rol en primer puesto y su porcentaje MUST provenir de los resultados persistidos de ese intento, sin recalcular matching, reordenar resultados ni mezclar datos del borrador.
- **FR-045**: La tarjeta de última evaluación MUST enlazar a `/roles?attemptId={attemptId}` para ver sus resultados y a `/habilidades/historial` para consultar el historial. Las rutas existentes MUST conservar su comportamiento.
- **FR-046**: El descarte desde Inicio y Habilidades MUST usar un modal centrado reutilizable con título «¿Descartar autoevaluación?», descripción «¿Estás seguro? Se perderá el progreso de este borrador.» y acciones «Cancelar» y «Descartar». La acción destructiva MUST distinguirse visualmente. MUST afectar solo el borrador activo y preservar evaluaciones completadas. Cancelar o fallar el backend MUST mantener intacto el estado visible; el error se comunica en el modal. Mientras se procesa MUST evitar solicitudes duplicadas. Tras confirmación exitosa MUST NOT crearse otra evaluación automáticamente; Inicio permanece en Inicio y Habilidades permanece en Habilidades.
- **FR-047**: Después de que el backend confirme la finalización, el borrador deja de estar activo; al volver a Inicio, se muestra la tarjeta motivacional recurrente y la tarjeta de última evaluación actualizada. MUST NOT iniciarse otro intento automáticamente ni cambiar la pantalla de finalización actual.
- **FR-048**: La nueva presentación de Inicio MUST eliminar la antigua ruta de orientación, su cálculo de progreso, la etiqueta «Paso sugerido» y los accesos que dependan de esos estados obsoletos. También MUST retirar de Skill Gap exclusivamente el rótulo «PASO 5 DE 6» y la tira de seis segmentos asociados a orientación. MUST conservar el diagnóstico, su navegación, las barras por competencia, los servicios compartidos aún usados por otras pantallas, `learning_steps` y el recorrido animado. MUST NOT retirar los indicadores independientes de registro, onboarding o progreso de autoevaluación. No se requieren cambios de backend, contratos, persistencia ni migraciones.
- **FR-049**: En Habilidades, el recorrido MUST mostrarse exclusivamente con un borrador activo y MUST representar solo ese intento. Sin borrador MUST ocultarse camino, bandera, animación y porcentaje; debe mostrarse una mascota estática y una tarjeta de acceso a evaluación cuyo título, descripción y acción distingan usuario nuevo de usuario con historial. Tras descartar con éxito, un intento completado anterior MUST NOT mostrarse como recorrido.
- **FR-050**: Inicio MUST conservar el saludo una sola vez, la barra de navegación inferior y un layout responsive, accesible y sin overflow horizontal. No MUST mostrar un botón provisional ni placeholder visible para el futuro acceso de comenzar evaluación; ese acceso permanece disponible en Habilidades.
- **FR-051**: En `SkillGapPage`, la eliminación MUST limitarse al rótulo de etapa «PASO 5 DE 6» y a su tira de seis segmentos. El título, contenido, cálculo y barras de progreso por competencia, botones, rutas y comportamiento del diagnóstico MUST permanecer sin cambios.
- **FR-052**: Los mensajes en la tarjeta de recorrido de Inicio MUST ser deterministas y basarse en el progreso guardado: 0% sin historial («¡Empezá a descubrir tu potencial!»); 0% con historial («¡Un nuevo comienzo!»); 1–39%, 40–74%, 75–99% y 100% MUST usar los mensajes aprobados en esta especificación. Los rangos son internos y MUST NOT mostrarse como porcentajes.
- **FR-053**: Habilidades MUST conservar la recuperación de respuestas al volver a un borrador. Crear una primera evaluación o reevaluación debe iniciar con respuestas vacías; con historial, la mascota estática, título, mensaje, acción de nueva evaluación y acceso al historial deben permanecer disponibles.
- **FR-054**: Roles abierto sin `attemptId` MUST mostrar el ranking persistido de la evaluación completa más reciente si existe, aun cuando haya un borrador, e indicar brevemente que corresponde a la última evaluación completada. Mientras haya borrador MUST ocultar «Explorar rol» y todo acceso directo a diagnóstico, brechas o formación asociado a ese ranking. Tras descartar exitosamente el borrador, la exploración del último ranking vuelve a estar disponible. Con `attemptId`, MUST conservar el intento histórico solicitado y sus accesos asociados durante toda esa navegación. MUST NOT recalcular matching ni mezclar datos entre intentos.
- **FR-055**: La ruta general Formación MUST mostrar «Prepará tu próximo paso» y la descripción «Completá tu autoevaluación para descubrir qué habilidades podés seguir desarrollando» cuando no exista una evaluación completada aplicable al contexto o haya un borrador activo. En ese estado MUST NOT mostrar «Explorar roles» ni recuperar automáticamente roles, diagnósticos, brechas o recomendaciones de otros intentos; si hay borrador puede ofrecer continuarlo. Con historial completo y sin borrador MUST invitar a explorar Roles. Las recomendaciones existentes de `LearningPage` por rol MUST permanecer sin cambios; no se implementa Learning V2.
- **FR-056**: El estado vacío compartido de Roles y Formación MUST usar una presentación clara, coherente con NextRole, con icono o ilustración, título, descripción breve y acción; MUST NOT mostrar rankings ficticios, mensajes técnicos ni la acción «Ver roles sugeridos» para usuarios sin una evaluación completada.
- **FR-057**: `source_attempt_id` MUST conservarse como referencia opcional a la evaluación completa de origen cuando aplique; su existencia MUST NOT provocar copia de respuestas. No se elimina ni altera la columna, las migraciones aplicadas ni los borradores existentes.
- **FR-058**: Roles y Formación MUST distinguir los estados de carga/error de un vacío real. Una consulta pendiente MUST NOT mostrar estados vacíos y un fallo MUST comunicar que los datos no pudieron obtenerse en lugar de fingir ausencia de evaluaciones.
- **FR-059**: La selección histórica explícita MUST conservar su `attemptId` a través de las rutas de resultados y vistas asociadas. Cada ranking, diagnóstico, brecha o recomendación consultada debe derivarse exclusivamente del intento seleccionado; el borrador actual o la última evaluación de otro contexto MUST NOT sustituirlo.

## Decisiones aprobadas

1. Los seis roles se ordenan de mayor a menor porcentaje con precisión completa. El empate exacto se aleatoriza una vez y su orden se persiste.
2. Cuestionario y matriz tienen versiones independientes: preguntas, opciones o competencias nuevas generan nueva versión de cuestionario; cambios de requisitos generan nueva versión de matriz. Borradores conservan las dos versiones de creación y las evaluaciones finalizadas son inmutables.
3. Se permite un solo borrador activo por usuario; se recupera al volver y se descarta explícitamente para comenzar otro, sin duplicados.
4. No es necesario migrar, convertir ni conservar respuestas históricas V1. No se diseña conversión automática. No se ejecutan borrados u otras operaciones destructivas sin autorización explícita.
5. Cada rol utiliza un ID estable. Se reutilizan `backend-dev-jr`, `frontend-dev-jr`, `fullstack-dev-jr` y `data-analyst-jr`; QA usa `qa-jr` y Cloud usa `cloud-jr`. Los seis IDs quedan aprobados para el diseño V2.
6. El historial inicial muestra solo evaluaciones completas de la más reciente a la más antigua, con fecha, resultados y detalle. No mezcla borradores ni incluye comparaciones gráficas.
7. El porcentaje visible se redondea a un decimal con HALF_UP; redondear no interviene en cálculo, orden o empate exacto.
8. Los niveles esperados admitidos para competencias relevantes son 1–4. La matriz V2 vigente conserva sus valores aprobados actuales, todos 1–3; esta decisión no altera la fórmula ni incluye competencias no relevantes.
9. Se conservan por separado el diagnóstico de competencias (niveles declarados en V2) y el matching (porcentajes calculados para seis roles). La ampliación incorpora Inicio, Habilidades, los estados de Roles y la entrada general de Formación según US5, US6, US8 y US9. Si existe un borrador, Roles directo mantiene el ranking persistido de la última evaluación completa pero oculta sus acciones de exploración; Formación muestra el estado de evaluación pendiente, sin recuperar contexto de intentos anteriores. Al descartar el borrador se habilita nuevamente la exploración del último ranking, sin restaurar un rol o diagnóstico seleccionado. Skill Gap solo pierde los indicadores obsoletos descritos en US7; la vista de recomendaciones por rol y las demás pestañas conservan su presentación.
10. El progreso del recorrido se calcula solo para el borrador activo. Si no existe borrador activo no se monta el recorrido, independientemente de que haya una evaluación completa; los resultados anteriores no desplazan el progreso de un borrador nuevo.
11. El recorrido es una visualización continua sin puntos de control. El mismo progreso controla su tramo coloreado, la posición de la mascota y el indicador accesible; no se muestra un porcentaje numérico.
12. La celebración nunca se infiere de respuestas completas mientras el intento siga en `DRAFT`. Cuando el backend confirma `COMPLETED`, Inicio y Habilidades dejan de mostrar el recorrido y no trasladan la celebración a otra pantalla en este alcance.
13. En Inicio, el recorrido solo se muestra para un borrador activo; con historial pero sin borrador se muestra la tarjeta motivacional recurrente y la última evaluación.
14. Descartar un borrador desde Inicio o Habilidades solo descarta el intento actual. No inicia otro; el usuario debe usar después el acceso explícito disponible en Habilidades.
15. Inicio lee fecha y resultados de la última evaluación desde el historial persistido y mantiene sus rutas actuales de resultados e historial.
16. El rediseño de Inicio no requiere nuevos endpoints. Se modifica la lógica de creación de reevaluaciones para dejar sus respuestas vacías, conservando `source_attempt_id` como procedencia y sin tocar esquema ni migraciones. Las recomendaciones existentes de Learning por rol se conservan.
17. En Skill Gap se retiran solo el rótulo y la tira de progreso de la antigua orientación. Registro, onboarding y progreso interno de la autoevaluación son flujos independientes y se conservan.
18. Una evaluación nueva, incluida la reevaluación, comienza sin respuestas. La recuperación del borrador actual devuelve sus respuestas guardadas. `source_attempt_id` es una relación de origen que no copia datos.
19. El recorrido solo se monta mientras existe un borrador activo en Inicio o Habilidades; el progreso sigue expuesto a tecnologías de asistencia, pero no aparece un porcentaje numérico visible.
20. Descartar muestra un único modal reusable en ambas pantallas y solo cambia el estado/caché después del éxito del backend.
21. La vista principal de Roles usa el historial completado más reciente y respeta explícitamente `attemptId`; la entrada general de Formación deja de redirigir según un rol objetivo permanente, pero la vista por rol existente no se altera.
22. El borrador activo prevalece como contexto de trabajo: en Roles conserva visible el último ranking completado solo como referencia, sin acciones que abran su diagnóstico; en Formación impide aplicar historial previo y ofrece continuar el borrador. Descartarlo reabre la exploración del último ranking, pero nunca restaura una selección de rol anterior. Los enlaces históricos explícitos conservan el `attemptId` de origen.

También siguen aprobadas la fórmula, los pesos, la exclusión de no relevantes, el tope del aporte, el requisito de 31 respuestas, la exclusión de intereses/objetivos/proyectos/completitud como factores y la matriz vigente.

## Propuestas pendientes de validación

No quedan propuestas funcionales pendientes para el alcance actual.

## Cuestiones abiertas que requieren aprobación

No quedan cuestiones funcionales abiertas que bloqueen la implementación de esta feature.

## Success Criteria

- **SC-001**: Hay exactamente 31 preguntas, con cinco niveles 0–4 cada una.
- **SC-002**: Hay seis roles y 186 valores iguales a la matriz definitiva; en la matriz V2 vigente ningún nivel esperado supera 3, aunque los niveles esperados admitidos sean 1–4.
- **SC-003**: Nivel 0 se distingue de no respondida.
- **SC-004**: Borradores se guardan, recuperan, editan y no generan resultados ni reemplazan perfil.
- **SC-005**: Una evaluación completa genera seis porcentajes según fórmula, se ordenan de mayor a menor con precisión completa y se muestran a un decimal HALF_UP.
- **SC-006**: El empate exacto se aleatoriza una vez y persiste; el redondeo no cambia orden ni crea empates.
- **SC-010**: Existe como máximo un borrador por usuario; al volver se recupera y el descarte explícito lo cierra sin crear otro automáticamente.
- **SC-011**: Cada borrador conserva las versiones de cuestionario y matriz con que se creó.
- **SC-012**: El historial lista solo evaluaciones completas de más reciente a más antigua, con fecha, resultados y detalle; no incluye gráficos comparativos.
- **SC-013**: V1 no se migra ni convierte; no se ejecutan operaciones destructivas sin autorización.
- **SC-007**: Intereses, objetivos y proyectos no afectan porcentajes.
- **SC-008**: Competencias 2, 3, 15, 16 y 30 conservan cambios aprobados y las demás preguntas no se alteran.
- **SC-009**: Learning queda fuera del alcance.
- **SC-014**: El diagnóstico presenta los niveles declarados de la autoevaluación V2 y permanece diferenciado de los porcentajes y el ranking de matching; no se agregan métricas diagnósticas nuevas. Las mejoras visuales de Inicio quedan acotadas a US5.
- **SC-015**: Con un borrador activo, el progreso interno coincide con respuestas guardadas/total de preguntas y cuenta nivel 0 como respuesta. Sin borrador no se muestra el recorrido independientemente del historial.
- **SC-016**: Durante el desplazamiento, el extremo coloreado del camino y la ubicación de la mascota corresponden al mismo valor de progreso. No hay checkpoints, hitos ni una segunda barra.
- **SC-017**: Un borrador con todas las respuestas guardadas no muestra celebración. Al confirmarse la finalización, el recorrido se oculta de Inicio y Habilidades; la celebración no se traslada a otra pantalla en este alcance.
- **SC-018**: El movimiento normal termina en un máximo de 1.76 segundos y no se repite al quedar en reposo; con movimiento reducido, el estado objetivo aparece sin desplazamiento animado.
- **SC-019**: El camino, la mascota y la meta permanecen visibles y legibles en móvil y escritorio, sin recorte ni desbordamiento horizontal; el progreso se expone de forma accesible sin número visible.
- **SC-020**: Para las cuatro combinaciones de borrador e historial, Inicio muestra las tarjetas correspondientes y nunca confunde datos aún no disponibles con un historial vacío.
- **SC-021**: Cuando existe historial, la tarjeta de última evaluación coincide con la evaluación completada más reciente y conserva su rol, porcentaje, orden y versiones persistidos; sus dos accesos abren resultados de esa evaluación y el historial correcto.
- **SC-022**: El descarte cancelado o fallido conserva el borrador; el descarte exitoso desde Inicio o Habilidades descarta solo ese borrador, mantiene historial, conserva la pantalla de origen y no crea otra evaluación.
- **SC-023**: Al volver a Inicio tras finalizar correctamente, desaparece la tarjeta del borrador y la tarjeta de última evaluación representa el nuevo resultado; no se crea una evaluación nueva.
- **SC-024**: El diseño de Inicio no presenta saludo duplicado, CTA provisional ni orientación antigua; no altera el menú inferior y no produce overflow horizontal en anchos móviles.
- **SC-025**: Skill Gap ya no presenta «PASO 5 DE 6» ni la tira de seis segmentos; conserva su título, barras de progreso por competencia, resultados, navegación y acceso a formación. Los indicadores independientes de registro, onboarding y autoevaluación siguen disponibles.
- **SC-026**: Una reevaluación nueva devuelve cero respuestas guardadas y progreso 0%; la fuente histórica conserva sus 31 respuestas y resultados. Una petición posterior recupera el mismo borrador y las respuestas que se guarden en él.
- **SC-027**: En Inicio y Habilidades el recorrido existe únicamente para un borrador activo. El texto porcentual no aparece visualmente; `role=progressbar`, `aria-valuenow` y `aria-valuetext` siguen describiendo el progreso real.
- **SC-028**: Los mensajes de la tarjeta azul coinciden con historial/reevaluación y los rangos internos 0, 1–39, 40–74, 75–99, 100, sin exponer el número como texto.
- **SC-029**: Cancelar, éxito y error del modal cumplen el flujo de descarte; mientras se procesa se emite como máximo una petición. El éxito elimina el recorrido de ambas pantallas sin reemplazarlo por el de una evaluación anterior.
- **SC-030**: Habilidades sin borrador muestra mascota estática y acción según historial; no incluye path, meta ni porcentaje. El borrador conserva su recorrido y sus respuestas al volver.
- **SC-031**: Roles directo muestra el último ranking persistido; con borrador añade la aclaración de contexto y no presenta enlaces de exploración, mientras que sin borrador permite explorar. El `attemptId` histórico conserva el intento solicitado; sin historial se muestra el vacío adaptado al borrador, sin resultados ficticios.
- **SC-032**: Formación sin evaluación aplicable o con borrador muestra el texto aprobado y no ofrece «Explorar roles» ni reutiliza contexto de otro intento; con historial y sin borrador invita a Roles. No exige objetivo permanente ni cambia recomendaciones existentes por rol.
- **SC-033**: Tras descartar un borrador con éxito, Roles vuelve a permitir explorar la última evaluación completada y Formación vuelve a invitar a explorar Roles; no se restaura una selección anterior. Al navegar desde el historial, cada resultado, diagnóstico, brecha o recomendación mantiene el mismo `attemptId` y nunca mezcla datos de otro intento.

## Assumptions

- Actor principal: estudiante o persona que busca su primera oportunidad IT.
- docs/V2/autoevaluacion-matriz-vigente.md es referencia canónica de catálogo y matriz.
- La Specification es funcional; el diseño técnico queda para Plan.

## Anexo A — Catálogo vigente

Las 31 preguntas y opciones están en docs/V2/autoevaluacion-matriz-vigente.md, referencia normativa de este anexo.

## Anexo B — Matriz definitiva V2

| Nº | Competencia | Backend | Frontend | Full Stack | QA | Data Analyst | Cloud |
|---:|---|---:|---:|---:|---:|---:|---:|
| 1 | Fundamentos de programación | E3 | E3 | E3 | C2 | C1 | C2 |
| 2 | Algoritmos y resolución eficiente de problemas | E2 | E2 | E2 | C1 | C1 | C1 |
| 3 | Estructuras de datos | E3 | E2 | E3 | — | C2 | — |
| 4 | Programación orientada a objetos | E3 | C2 | E3 | — | — | — |
| 5 | Principios de diseño y arquitectura básica | E2 | E2 | E2 | — | — | C2 |
| 6 | Consultas SQL | E3 | — | E2 | C1 | E3 | C1 |
| 7 | Modelado de bases de datos | E3 | — | E3 | — | C2 | C1 |
| 8 | Consumo de APIs REST | E2 | E3 | E3 | E2 | C1 | C1 |
| 9 | Desarrollo de APIs REST | E3 | C1 | E3 | C1 | — | — |
| 10 | Implementación de reglas de negocio | E3 | C2 | E3 | C2 | C1 | — |
| 11 | Desarrollo de interfaces web | — | E3 | E3 | C1 | — | — |
| 12 | Gestión de estado | — | E3 | E3 | — | — | — |
| 13 | Depuración y resolución de errores | E3 | E3 | E3 | C2 | C2 | E2 |
| 14 | Git y control de versiones | E3 | E3 | E3 | C2 | C1 | C2 |
| 15 | Integración y colaboración en proyectos | E2 | E2 | E2 | E2 | E2 | E2 |
| 16 | Docker y contenedores | C2 | C1 | C2 | — | — | E2 |
| 17 | Fundamentos y tipos de testing | E2 | E2 | E2 | E3 | — | C1 |
| 18 | Diseño de casos de prueba | C1 | C1 | C1 | E3 | — | — |
| 19 | Ejecución y documentación de pruebas | C1 | C1 | C1 | E3 | C1 | C1 |
| 20 | Gestión de defectos | C1 | C1 | C1 | E3 | — | C1 |
| 21 | Pruebas de APIs | E2 | C2 | E2 | E3 | — | C1 |
| 22 | Análisis exploratorio de datos | — | — | — | — | E3 | C1 |
| 23 | Limpieza y preparación de datos | — | — | — | — | E3 | — |
| 24 | Visualización de datos y dashboards | — | — | — | — | E3 | C1 |
| 25 | Estadística aplicada | — | — | — | — | E3 | — |
| 26 | Interpretación de métricas de negocio | C1 | C1 | C1 | C1 | E3 | C1 |
| 27 | Fundamentos de Cloud Computing | C1 | C1 | C1 | — | C1 | E3 |
| 28 | Linux y sistemas operativos | C1 | — | C1 | — | — | E3 |
| 29 | Fundamentos de redes | C1 | — | C1 | C1 | — | E3 |
| 30 | Automatización de tareas mediante scripts (Bash, PowerShell o Python) | C1 | — | C1 | C1 | C2 | E3 |
| 31 | Monitoreo y diagnóstico de infraestructura | C2 | — | C1 | C1 | — | E3 |

### Prioridad documental

Las decisiones V2 y el catálogo/matriz vigentes prevalecen sobre PDF históricos. La Constitution gobierna; V1 es solo referencia de implementación.
