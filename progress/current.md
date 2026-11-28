# Estado vigente de NextRole V2

**Actualizado:** 2026-11-28
**Rama activa:** `develop-v2`
**HEAD/checkpoint:** `2ee7dd9f5b69a5e923c07ee0fbd740405d1bc680` — `feat: integra Explainability en la pantalla de Formación` (Checkpoint 4). No se crearon commits nuevos.
**Estado Git:** seis commits locales por delante y dos por detrás de `origin/develop-v2`; no hubo sincronización. Checkpoint 5 sigue sin commit.

## Estado actual — Implement, Explainability Checkpoint 5

- T037 completada: 24 fixtures Q2/M1 (seis roles × cuatro perfiles), verificados contra Matching y SkillGapV2 reales en PostgreSQL desechable; las fixtures históricas Q1/M1 y sus resultados siguen preservados. `pytest`: 23 aprobadas; integración dirigida: 3 aprobadas.
- V7 ya estaba aplicada y no se repitió. Flyway registra `7:true`, seis roles tienen descripción y el esquema sigue saludable. La base es de desarrollo; se aceptó que variaran sus conteos y no se restauró el respaldo ni se eliminaron datos.
- Verificación de codificación: la migración y los seis textos de rol no contienen U+FFFD; la consulta SQL encontró cero descripciones con carácter de reemplazo. La visualización anterior de PowerShell fue engañosa.
- Compose ya entrega `OPENROUTER_API_KEY` al backend desde el entorno local sin imprimir ni versionar el valor. El backend, frontend y PostgreSQL permanecen activos y saludables. Frontend `http://localhost:5173/`; backend health `http://localhost:8090/actuator/health`.
- T038 completada: `mvn -f backend/pom.xml test` pas? al finalizar el troubleshooting ? 91 pruebas, 0 fallos, 0 errores, 0 omitidas; PostgreSQL/Testcontainers y proveedor simulado.
- T039 pendiente de cierre: la suite frontend actual tuvo 108 aprobadas y 2 fallidas de 110. Las dos fallas son las nuevas expectativas de símbolos simples de `CompetencyAreas.test.tsx` y `AssessmentResultsRanking.test.tsx`, conservadas para que la otra instancia implemente esos cambios visuales. Build aprobado; lint aprobado con advertencia preexistente en `SessionContext.tsx:45`.
- T040 pendiente: no se pudo realizar revisión visual manual. El host del navegador no estuvo disponible y el control de Chrome fue denegado; no se usó otra vía de automatización. No se afirma validación manual móvil, escritorio, teclado o lector de pantalla.
- T041 completada tras una solicitud real adicional autorizada. La solicitud inicial del CP5 (`7593cac9-b7b1-4f69-97ac-f9fc60ffbb4e`) sigue como FAILED/INVALID_PROVIDER_RESPONSE; no se reintent? esa cuenta porque no hab?a sesi?n de autenticaci?n disponible. Para comprobar el flujo se us? un nuevo intento sint?tico Q2/M1 de Data Analyst (`b8ce48ed-ccbd-451f-a3ed-787d35b9b7b6`, 80.0%). Una generaci?n real al modelo gratuito termin? SUCCEEDED en 20 047 ms, con 3 792 tokens de entrada, 3 614 de salida (3 170 de razonamiento), costo informado 0 y `attempt_count=1`; modelo solicitado/servido `baidu/ernie-5.5-flash-preview:free`. Prompt V5 SHA-256 `84C45ABE0D028E4ADB7376E387597993D196F5F2C8990596D853D11FDCF23AF5`; contexto `A0CAEEF71427FBC5F227B7693AC670ACD9BB6C5326B095C39645B7E09482B75F`. GET recuper? el registro; POST generate posterior reutiliz? el ?xito y no caus? otra llamada.
- El catálogo oficial de OpenRouter consultado el 2026-11-28 mostraba `baidu/ernie-5.5-flash-preview:free` como gratuito. El adaptador mantiene `allow_fallbacks=false`; el costo de esta solicitud específica sigue desconocido porque no hubo métricas de uso.
- El intento sintético y el registro fallido se conservan. No se restauró respaldo ni se borraron datos. No se modificó prompt V5, backend funcional, frontend de Formación, Matching, Skill Gap ni migraciones. No hubo commits ni push.

## Próximo paso

Esperar la implementación paralela de los símbolos visuales para revalidar la suite frontend, y una nueva autorización antes de repetir una solicitud real. Realizar revisión visual de Formación cuando el navegador de inspección esté disponible. Mantener los cambios locales y el entorno experimental sin staging.

## Checkpoint 4 — resultado previo

- Se implementó la pantalla V2 `/formacion/roles/:roleId?attemptId=...`, manteniendo `/formacion`, `/roles/:roleId`, `/roles/:roleId/formacion` y los demás consumidores V1. Los botones «Explorar rol» del ranking V2 conservan su apariencia y ahora abren directamente Formación. La flecha vuelve a `/roles?attemptId=...`.
- El encabezado consulta el rol y el intento seleccionado de forma independiente al diagnóstico. Usa porcentaje y fecha persistidos, muestra cuestionario/matriz, aplica snapshots de nombre/descripción en diagnósticos exitosos y conserva la aclaración de que la autoevaluación no es una certificación.
- El diagnóstico presenta cuatro secciones en una tarjeta de rol; estados de generación, éxito, error, polling cada dos segundos, límite visual de 120 segundos y reintento solo por acción manual. Un GET no genera; las claves están aisladas por intento/rol y el remount/StrictMode no duplica la generación inicial.
- El plan de formación permanece como tarjeta independiente «Próximamente». No se implementó Learning ni contenido ficticio.
- El mapa `roleId → emblema/paleta` utiliza los seis SVG originales de `frontend/src/assets/logos-roles/`. Los archivos no se movieron ni modificaron; el build confirma su importación. Las paletas aprobadas pasan la comprobación de contraste AA usada en pruebas. La mascota original utiliza poses `thinking` y `pointing`, con movimiento reducido respetado.
- Se completaron y verificaron T011, T019, T020, T022, T024, T027, T030, T032, T033, T034, T035 y T039. T040 continúa pendiente porque no se realizó una revisión visual manual en móvil/escritorio con lector de pantalla.
- Harness: las pruebas iniciales fallaron por falta de RoleFormationPage/mapa y por destinos antiguos; tras implementar y actualizar expectativas obsoletas del ranking, pasaron. `npm.cmd --prefix frontend test`: 22 archivos, 109 pruebas aprobadas. `npm.cmd --prefix frontend run build`: aprobado; Vite informa un aviso de chunk JS >500 kB. `npm.cmd --prefix frontend run lint`: aprobado con una advertencia preexistente en `SessionContext.tsx:45`.
- No se modificó backend, Matching, Skill Gap, base de datos, Docker, prompt V5 ni experimentos. No se realizaron llamadas a proveedores de IA ni se generaron costos. No se realizó validación visual manual ni captura de pantalla.

## Trabajo paralelo de Inicio e identidad visual

- Se preservan los cambios locales de Inicio, Habilidades, navegación inferior y el recorrido de autoevaluación, junto con las adiciones paralelas de `specs/001-autoevaluacion-matching/`. No forman parte del commit de Explainability.
- El registro de `specs/001` documenta como completadas las fases visuales 19–21 y sus validaciones automatizadas. Quedan abiertas para revisión visual manual T085 y T090; en fase 21 quedan pendientes T092 (preview del recurso animado) y T097 (revisión visual/responsive del diálogo). No inferir que esas revisiones manuales ocurrieron.
- La validación automática más reciente registrada por esa instancia es: suite frontend 15 archivos/90 pruebas, build exitoso y lint exitoso con advertencia existente en `SessionContext.tsx:45`. Docker/Vite se inspeccionaron sin reiniciar servicios en el último registro.

## Cambios locales que deben preservarse

Mantener las modificaciones paralelas de frontend/identidad visual, `docker-compose.yml`, `specs/001-autoevaluacion-matching/`, `experiments/` y demás archivos locales. No incorporar el entorno experimental ni cambios ajenos a este checkpoint en staging.

Los checkpoints cronológicos previos están en [`progress/history.md`](history.md). Verificar siempre Git y el estado real antes de continuar.
