# Feature Specification: Autoevaluación técnica y matching de roles IT

**Feature Branch**: [001-autoevaluacion-matching]
**Created**: 2026-11-20
**Status**: Approved — las decisiones funcionales aprobadas están incorporadas. Los niveles esperados admitidos son 1–4; la matriz V2 vigente conserva sus asignaciones aprobadas de 1–3.

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
1. **Given** una evaluación completa y sin borrador activo, **When** inicia una reevaluación, **Then** crea un intento nuevo precargado desde la última completa.
2. **Given** que existe un borrador activo, **When** el usuario vuelve a la autoevaluación, **Then** recupera ese borrador sin crear otro.
3. **Given** un borrador activo, **When** el usuario lo descarta explícitamente y comienza otro, **Then** se crea un único borrador nuevo.
4. **Given** una evaluación completa, **When** el historial se consulta, **Then** aparece de la más reciente a la más antigua con fecha, resultados y acceso al detalle.
5. **Given** una evaluación incompleta, **When** se guarda, **Then** no reemplaza el perfil vigente ni aparece en el historial de completas.
6. **Given** un resultado histórico, **When** se conserva, **Then** queda asociado a las versiones de cuestionario y matriz.

### User Story 4 — Visualizar el progreso en un recorrido animado (Priority: P2)

Como estudiante que retoma o completa su autoevaluación, quiero ver el progreso como un recorrido continuo para reconocer visualmente cuánto avancé.

**Acceptance Scenarios**:
1. **Given** que no hay evaluación previa ni borrador activo, **When** se muestra Inicio, **Then** el recorrido indica 0% y la mascota permanece al inicio.
2. **Given** un borrador activo, **When** se muestra Inicio, **Then** el progreso corresponde a las respuestas guardadas divididas por el total del mismo intento; el nivel 0 cuenta como respuesta guardada.
3. **Given** una evaluación completada y ningún borrador activo, **When** se muestra Inicio, **Then** el recorrido indica 100% y la mascota celebra.
4. **Given** que un borrador tiene todas sus respuestas guardadas pero no fue finalizado, **When** se muestra Inicio, **Then** el recorrido puede indicar 100%, pero la mascota permanece en reposo y no celebra.
5. **Given** evaluaciones anteriores y un borrador activo, **When** se muestra Inicio, **Then** el recorrido representa solo el progreso del borrador.
6. **Given** que está activada la preferencia de movimiento reducido, **When** se muestra el recorrido, **Then** la mascota aparece directamente en la posición correspondiente sin desplazamiento animado.
7. **Given** cualquier estado, **When** se muestra el recorrido, **Then** hay un solo camino continuo sin hitos y su tramo coloreado termina junto a la mascota en el porcentaje general correspondiente.

### Edge Cases

- Nivel 0 es válido; una pregunta vacía impide calcular.
- Competencias no relevantes quedan fuera del numerador y denominador.
- Cambios de versión no reinterpretan silenciosamente resultados históricos.
- Al volver a la autoevaluación se recupera el único borrador activo; debe descartarse explícitamente para comenzar otro. El historial lista solo evaluaciones completas. Las comparaciones gráficas entre evaluaciones quedan fuera del alcance actual.

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
- **FR-018**: Reevaluar MUST crear un intento nuevo precargado con la última completa si existe.
- **FR-019**: Intentos finalizados y resultados MUST conservarse inmutables asociados a versiones de cuestionario y matriz.
- **FR-020**: Un incompleto MUST NOT reemplazar el perfil técnico vigente.
- **FR-021**: Resultados MUST presentarse como autoevaluación declarada, no certificación.
- **FR-022**: Matching y Skill Gap MUST usar reglas deterministas; la IA posterior puede explicar, pero MUST NOT alterar porcentajes, brechas ni prioridades calculadas por Java.
- **FR-023**: Skill Gap y explicabilidad con IA se especificarán después. Se conserva el diagnóstico existente como visualización de los niveles declarados en la autoevaluación V2, separado del matching por rol; no se incorporan métricas diagnósticas nuevas ni un rediseño general de Inicio, salvo el recorrido animado acotado a la tarjeta de autoevaluación. Landing page, ruta de aprendizaje y módulo Learning están fuera de esta feature.
- **FR-024**: Decisiones V2 y catálogo/matriz vigentes prevalecen sobre PDF históricos y referencias V1.
- **FR-025**: No se definen entidades JPA, tablas SQL, endpoints ni migraciones; corresponden al Plan.
- **FR-026**: El versionado del cuestionario y de la matriz MUST ser independiente. Cambios en preguntas, opciones o competencias generan nueva versión de cuestionario; cambios en requisitos de roles generan nueva versión de matriz.
- **FR-027**: Cada borrador MUST conservar las versiones de cuestionario y matriz asignadas al crearse, aunque se publiquen versiones posteriores. Los intentos finalizados MUST ser inmutables.
- **FR-028**: MUST existir como máximo un borrador activo por usuario. Al volver se MUST recuperar ese borrador; MUST permitirse descartarlo explícitamente para crear otro, sin duplicar borradores activos.
- **FR-029**: El historial MUST incluir solo evaluaciones completas, ordenadas de la más reciente a la más antigua, con fecha, resultados y acceso al detalle. Los borradores se recuperan desde el flujo de autoevaluación. Las comparaciones gráficas entre evaluaciones MUST quedar fuera de esta fase.
- **FR-030**: No se requiere migrar, convertir ni conservar respuestas históricas V1 como datos de evaluación V2. MUST NOT diseñarse conversión automática; no se borrarán datos ni se ejecutarán operaciones destructivas sin autorización explícita.
- **FR-031**: Cada uno de los seis roles MUST tener un identificador estable, independiente de su etiqueta visible.
- **FR-032**: El nivel esperado de cada competencia relevante MUST poder ser 1, 2, 3 o 4. La matriz V2 actualmente aprobada MUST conservar exactamente sus 186 asignaciones, que utilizan niveles esperados de 1 a 3; una competencia no relevante MUST seguir excluida del cálculo.
- **FR-033**: El diagnóstico de competencias MUST mostrar los niveles declarados 0–4 del cuestionario V2, separado de los porcentajes y el ranking de matching. MUST NOT sustituirlo por matching ni derivar métricas nuevas; la fuente de datos debe ser la evaluación V2 vigente.
- **FR-034**: El recorrido visual MUST representar el progreso como un único camino continuo, sin checkpoints, divisiones o hitos asociados a roles, áreas, porcentajes intermedios, orden de preguntas o cantidad fija de competencias. El tramo coloreado MUST terminar en la posición de la mascota y ambos MUST corresponder al mismo progreso.
- **FR-035**: El porcentaje del recorrido MUST derivarse exclusivamente de la cantidad de respuestas guardadas y el total de preguntas de la evaluación correspondiente. Una respuesta explícita de nivel 0 MUST contar como guardada. Sin evaluación ni borrador, el progreso MUST ser 0%; con un borrador activo MUST usar los conteos de ese intento; con una evaluación completada y sin borrador activo MUST ser 100%.
- **FR-036**: La confirmación de finalización MUST depender del estado `COMPLETED`, no de que el borrador tenga todas sus respuestas guardadas. Si hay un borrador activo, su progreso MUST prevalecer visualmente sobre resultados completados anteriores.
- **FR-037**: El recorrido MUST mostrar un único porcentaje general y no incorporar otra barra de progreso. El camino y sus tramos completado y pendiente MUST mantener contraste y una descripción accesible del progreso.
- **FR-038**: El componente del recorrido MUST poder reutilizarse sin depender de preguntas, competencias, áreas o roles; recibirá el progreso objetivo y el estado de finalización. MUST respetar `prefers-reduced-motion`, mostrando directamente el estado objetivo cuando esté activado.
- **FR-039**: La animación de entrada MUST ser finita, ajustar el desplazamiento al ciclo de caminata, detenerse al alcanzar la posición objetivo y mantener una duración máxima configurable que no exceda 1.76 segundos. Los controles visuales deben permanecer dentro de la tarjeta actual y responder a anchos móviles y de escritorio sin desbordamiento.

## Decisiones aprobadas

1. Los seis roles se ordenan de mayor a menor porcentaje con precisión completa. El empate exacto se aleatoriza una vez y su orden se persiste.
2. Cuestionario y matriz tienen versiones independientes: preguntas, opciones o competencias nuevas generan nueva versión de cuestionario; cambios de requisitos generan nueva versión de matriz. Borradores conservan las dos versiones de creación y las evaluaciones finalizadas son inmutables.
3. Se permite un solo borrador activo por usuario; se recupera al volver y se descarta explícitamente para comenzar otro, sin duplicados.
4. No es necesario migrar, convertir ni conservar respuestas históricas V1. No se diseña conversión automática. No se ejecutan borrados u otras operaciones destructivas sin autorización explícita.
5. Cada rol utiliza un ID estable. Se reutilizan `backend-dev-jr`, `frontend-dev-jr`, `fullstack-dev-jr` y `data-analyst-jr`; QA usa `qa-jr` y Cloud usa `cloud-jr`. Los seis IDs quedan aprobados para el diseño V2.
6. El historial inicial muestra solo evaluaciones completas de la más reciente a la más antigua, con fecha, resultados y detalle. No mezcla borradores ni incluye comparaciones gráficas.
7. El porcentaje visible se redondea a un decimal con HALF_UP; redondear no interviene en cálculo, orden o empate exacto.
8. Los niveles esperados admitidos para competencias relevantes son 1–4. La matriz V2 vigente conserva sus valores aprobados actuales, todos 1–3; esta decisión no altera la fórmula ni incluye competencias no relevantes.
9. Se conservan por separado el diagnóstico de competencias (niveles declarados en V2) y el matching (porcentajes calculados para seis roles). No se hace un rediseño general de Inicio en esta feature; se aprueba únicamente integrar el recorrido animado de progreso en la tarjeta de autoevaluación existente.
10. El progreso del recorrido se calcula para el intento activo. Si no existe borrador activo y sí una evaluación completa, se representa el 100% confirmado por el sistema; los resultados anteriores no desplazan el progreso de un borrador nuevo.
11. El recorrido es una visualización continua sin puntos de control. El mismo progreso controla su tramo coloreado, la posición de la mascota y el único porcentaje visible.
12. La celebración depende del estado confirmado `COMPLETED`, incluso si un borrador activo tiene todas sus respuestas guardadas.

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
- **SC-010**: Existe como máximo un borrador por usuario; al volver se recupera y descartarlo explícitamente permite iniciar otro.
- **SC-011**: Cada borrador conserva las versiones de cuestionario y matriz con que se creó.
- **SC-012**: El historial lista solo evaluaciones completas de más reciente a más antigua, con fecha, resultados y detalle; no incluye gráficos comparativos.
- **SC-013**: V1 no se migra ni convierte; no se ejecutan operaciones destructivas sin autorización.
- **SC-007**: Intereses, objetivos y proyectos no afectan porcentajes.
- **SC-008**: Competencias 2, 3, 15, 16 y 30 conservan cambios aprobados y las demás preguntas no se alteran.
- **SC-009**: Learning queda fuera del alcance.
- **SC-014**: El diagnóstico presenta los niveles declarados de la autoevaluación V2 y permanece diferenciado de los porcentajes y el ranking de matching; no se agregan métricas diagnósticas nuevas. El único ajuste visual de esta ampliación es el recorrido animado de US4.
- **SC-015**: En los estados sin evaluación, borrador activo y evaluación completada sin borrador, el porcentaje coincide respectivamente con 0%, respuestas guardadas/total del intento y 100%; nivel 0 cuenta como guardado.
- **SC-016**: Durante el desplazamiento, el extremo coloreado del camino y la ubicación de la mascota corresponden al mismo valor de progreso. No hay checkpoints, hitos ni una segunda barra.
- **SC-017**: Un borrador con todas las respuestas guardadas no muestra celebración; una evaluación confirmada como completa y sin borrador sí la muestra.
- **SC-018**: El movimiento normal termina en un máximo de 1.76 segundos y no se repite al quedar en reposo; con movimiento reducido, el estado objetivo aparece sin desplazamiento animado.
- **SC-019**: El camino, la mascota, la meta y el porcentaje permanecen visibles y legibles en móvil y escritorio, sin recorte ni desbordamiento horizontal.

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
