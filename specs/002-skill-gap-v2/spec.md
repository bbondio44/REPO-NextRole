# Feature Specification: Diagnóstico Skill Gap V2

**Feature Branch**: `develop-v2` (feature spec: `002-skill-gap-v2`; no branch change)
**Created**: 2026-11-26
**Status**: Implemented — backend validation passed 2026-11-26; no frontend UI is included
**Input**: Exponer un cálculo determinista y contextual de brechas para consumo por módulos futuros de NextRole.

## Clarifications

### Session 2026-11-26 — decisiones funcionales

- Solo evaluaciones efectivamente completadas pueden originar brechas. Un borrador completamente respondido no es aplicable.
- El llamador selecciona explícitamente cualquiera de los seis roles V2 asociados a la evaluación; el servicio no elige automáticamente el rol de mayor posición ni persiste un rol objetivo.
- El contexto del cálculo es siempre el par de evaluación completada y rol indicado. Los módulos consumidores deben conservarlo, incluso cuando la evaluación proviene del historial.
- Por competencia se exponen identificador y nombre del catálogo V2, nivel declarado, nivel requerido, importancia esencial/complementaria y brecha `max(0, nivel requerido - nivel declarado)`. Se incluyen brechas cero y se excluye `NOT_RELEVANT`.
- No se incluyen porcentajes, puntajes agregados, prioridades nuevas ni descripción independiente; tampoco se reutiliza el texto de la pregunta como descripción. No se modifican competencias, matriz ni base de datos para incorporar una descripción. Una futura especificación de IA evaluará preguntas y descriptores de niveles como contexto adicional.
- **Decisión de alcance aprobada posteriormente**: mantener un servicio backend y un endpoint REST V2 autenticado, preparado para consumo interno futuro de IA/Learning y posible exposición futura. Diferir pantallas, componentes visuales y navegación específica de Skill Gap V2. Esto sustituye la propuesta anterior de integrar ahora una pantalla V2 y enlaces desde Roles/Formación; no cambia la navegación ni las funcionalidades existentes.

## User Scenarios & Testing

### User Story 1 — Obtener brechas deterministas para un rol (Priority: P1)

Como módulo consumidor de NextRole, quiero obtener las brechas de una evaluación completada y un rol explícito, para usar la información en futuras experiencias de desarrollo profesional sin duplicar el cálculo.

**Why this priority**: IA y Learning podrán consumir un cálculo backend común y coherente con la historia V2.

**Independent Test**: Una solicitud autenticada para un intento propio completado y un rol V2 devuelve todas las competencias relevantes con sus niveles, importancia y brecha exacta, sin alterar datos persistidos.

**Acceptance Scenarios**:

1. **Given** un intento completado propio y un rol V2, **When** consulto el endpoint contextual, **Then** recibo código/nombre V2, nivel declarado/requerido, importancia y brecha de cada competencia relevante.
2. **Given** una respuesta declarada de nivel 0, **When** consulto brechas, **Then** el nivel 0 se conserva como respuesta y la brecha se calcula desde ese valor.
3. **Given** competencias cubiertas y no requeridas, **When** consulto brechas, **Then** las cubiertas aparecen con brecha cero y las `NOT_RELEVANT` no aparecen.
4. **Given** cualquiera de los seis roles V2, **When** el llamador consulta ese rol, **Then** el cálculo no depende de su ranking, no selecciona otro rol y no guarda un objetivo profesional.

### User Story 2 — Preservar el significado histórico (Priority: P1)

Como módulo consumidor, quiero solicitar brechas de un intento histórico específico, para que una reevaluación o nuevas versiones no cambien su diagnóstico.

**Why this priority**: Los intentos completados son inmutables y las respuestas, preguntas y matriz deben interpretarse con las versiones fijadas al intento.

**Independent Test**: Dos intentos completados con respuestas o versiones distintas producen resultados aislados al consultar cada `attemptId`, incluso si existe un borrador o se publica una matriz más reciente.

**Acceptance Scenarios**:

1. **Given** dos intentos completados con respuestas distintas, **When** consulto el mismo rol con cada ID, **Then** cada respuesta contiene solo niveles del intento solicitado.
2. **Given** un intento creado con una matriz anterior y una matriz actual más nueva, **When** consulto sus brechas, **Then** se usan los requisitos de su matriz fijada.
3. **Given** un borrador activo junto a un intento completado, **When** consulto el ID completado, **Then** no se usan respuestas del borrador.
4. **Given** un intento de otro usuario, **When** se solicita su diagnóstico, **Then** la API no revela ni expone sus datos.

### User Story 3 — Rechazar contextos no aplicables (Priority: P2)

Como módulo consumidor, quiero distinguir un diagnóstico no aplicable o inconsistente de una brecha real, para no entregar información inventada.

**Independent Test**: Borradores, intentos descartados, propietarios incorrectos, roles inválidos y respuestas históricas inconsistentes no producen brechas ni sustituyen el contexto.

**Acceptance Scenarios**:

1. **Given** un borrador, intento descartado o inexistente, **When** solicito brechas, **Then** la API responde con el error correspondiente y no consulta otro intento.
2. **Given** un intento completado con una respuesta relevante ausente, **When** solicito brechas, **Then** la API informa inconsistencia en vez de interpretarla como nivel cero.
3. **Given** un rol V2 sin requisitos relevantes en la matriz del intento, **When** solicito brechas, **Then** recibo una colección vacía sin filas ficticias.

## Edge Cases

- Un nivel cero guardado es distinto de una respuesta ausente.
- Una matriz/rol sin requisitos relevantes no genera filas ficticias.
- Un error de lectura no debe reemplazarse con datos V1, el último intento o un borrador.
- El servicio no escribe resultados derivados ni altera respuestas o resultados de matching.
- Las versiones de cuestionario y matriz son independientes y ambas se toman del intento seleccionado.

## Requirements

### Functional Requirements

- **FR-001**: El endpoint MUST calcular brechas solo para una evaluación V2 completada. Un borrador, aunque esté completamente respondido, MUST NOT producir un diagnóstico.
- **FR-002**: El endpoint MUST aceptar explícitamente cualquiera de los seis roles del catálogo V2. MUST NOT elegir el rol mejor posicionado ni guardar un rol objetivo permanente. La experiencia que presenta la selección queda para la especificación del módulo consumidor.
- **FR-003**: Para cada competencia relevante, el resultado MUST contener `max(0, nivel requerido - nivel declarado)`, incluidas las competencias cubiertas con brecha cero. MUST NOT agregar porcentajes, un puntaje global ni categorías de prioridad.
- **FR-004**: Cada elemento MUST incluir identificador y nombre existentes en el catálogo V2, nivel declarado, nivel requerido e importancia esencial/complementaria. MUST NOT incluir descripción independiente ni reutilizar el texto de la pregunta como descripción.
- **FR-005**: El resultado MUST incluir todas las competencias requeridas por el rol y MUST excluir las filas `NOT_RELEVANT`.
- **FR-006**: El cálculo MUST residir en el backend y la salida MUST ser estructurada e independiente de una presentación visual para que otros módulos puedan consumirla sin duplicar la fórmula.
- **FR-007**: El cálculo MUST usar las respuestas del intento seleccionado y los requisitos de su `matrixVersion`. MUST NOT recalcular ni modificar resultados persistidos del matching.
- **FR-008**: La consulta MUST conservar el contexto explícito de evaluación y rol en su entrada y respuesta; MUST NOT mezclar datos de distintos intentos. No se incluye navegación frontend específica en esta feature.
- **FR-009**: Una respuesta de nivel 0 MUST distinguirse de una respuesta ausente; la ausencia MUST NOT convertirse en cero.
- **FR-010**: La API MUST distinguir intento no aplicable, propietario no autorizado/no encontrado, rol no válido, datos históricos inconsistentes y cálculo disponible sin recurrir a datos sustitutos.
- **FR-011**: La API MUST derivar la identidad del usuario de la autenticación existente y respetar la propiedad del intento.
- **FR-012**: El cálculo MUST ser bajo demanda y MUST NOT persistir diagnósticos ni modificar el esquema.
- **FR-013**: Esta especificación MUST preservar Matching V2, las 31 competencias, la matriz/requisitos, ponderaciones, resultados históricos y funcionalidades V1. No incluye pantallas, componentes visuales, navegación específica, priorización, IA, recomendaciones Learning ni rediseño de Formación.

### Key Entities

- **Evaluación seleccionada**: intento V2 completado que aporta respuestas y versiones fijadas.
- **Rol solicitado**: uno de los seis roles V2 proporcionado explícitamente por el llamador.
- **Competencia relevante**: competencia del catálogo V2 con requisito esencial o complementario en la matriz fijada.
- **Brecha calculada**: diferencia no negativa entre nivel requerido y nivel declarado para una competencia relevante.

## Success Criteria

### Measurable Outcomes

- **SC-001**: Cada respuesta válida incluye todos los requisitos relevantes del rol con identificador/nombre, niveles, importancia y brecha exacta; conserva brechas cero y excluye `NOT_RELEVANT`.
- **SC-002**: Consultas de intentos históricos distintos no mezclan respuestas ni requisitos, incluso si hay borrador o una versión más reciente.
- **SC-003**: Nivel cero y respuesta ausente son diferenciables; la ausencia nunca se computa como cero.
- **SC-004**: Intento no aplicable, error o rol sin requisitos no produce datos simulados ni de otro contexto.
- **SC-005**: Consultar el servicio no cambia respuestas, estado, versiones ni resultados del matching y no crea una persistencia de diagnóstico.
- **SC-006**: Consumidores backend reciben una estructura calculada sin depender de una presentación frontend y sin fórmula duplicada.

## Assumptions

- La evaluación y rol los seleccionará explícitamente el módulo consumidor de acuerdo con sus reglas de experiencia; el servicio solo valida el contexto y calcula.
- Las respuestas/nombres provienen de la versión de cuestionario del intento y los requisitos de su matriz fijada.
- El endpoint REST autenticado habilita integración futura, pero no implica crear ahora una pantalla ni una navegación dedicada.
- Skill Gap V1, Learning V1 y su navegación continúan sin cambios.
- Una futura especificación de IA puede evaluar preguntas y descriptores de nivel como contexto adicional; una futura especificación Learning puede consumir esta salida. No se implementan aquí.
