# Estado vigente de NextRole V2

**Actualizado:** 2026-11-28
**Rama activa:** `develop-v2`
**HEAD/checkpoint:** `3b0e415bc52703944ef942fd590884f460256878` — `feat: incorpora API, historial y recuperación de Explainability` (Checkpoint 3).
**Estado Git:** `develop-v2` está cinco commits por delante y dos por detrás de `origin/develop-v2`. No se sincronizó con el remoto. Los dos commits remotos conocidos modifican únicamente README.md. Checkpoint 4 permanece local y sin commit.

## Estado actual — Implement, Explainability Checkpoint 4

- Se implementó la pantalla V2 `/formacion/roles/:roleId?attemptId=...`, manteniendo `/formacion`, `/roles/:roleId`, `/roles/:roleId/formacion` y los demás consumidores V1. Los botones «Explorar rol» del ranking V2 conservan su apariencia y ahora abren directamente Formación. La flecha vuelve a `/roles?attemptId=...`.
- El encabezado consulta el rol y el intento seleccionado de forma independiente al diagnóstico. Usa porcentaje y fecha persistidos, muestra cuestionario/matriz, aplica snapshots de nombre/descripción en diagnósticos exitosos y conserva la aclaración de que la autoevaluación no es una certificación.
- El diagnóstico presenta cuatro secciones en una tarjeta de rol; estados de generación, éxito, error, polling cada dos segundos, límite visual de 120 segundos y reintento solo por acción manual. Un GET no genera; las claves están aisladas por intento/rol y el remount/StrictMode no duplica la generación inicial.
- El plan de formación permanece como tarjeta independiente «Próximamente». No se implementó Learning ni contenido ficticio.
- El mapa `roleId → emblema/paleta` utiliza los seis SVG originales de `frontend/src/assets/logos-roles/`. Los archivos no se movieron ni modificaron; el build confirma su importación. Las paletas aprobadas pasan la comprobación de contraste AA usada en pruebas. La mascota original utiliza poses `thinking` y `pointing`, con movimiento reducido respetado.
- Se completaron y verificaron T011, T019, T020, T022, T024, T027, T030, T032, T033, T034, T035 y T039. T040 continúa pendiente porque no se realizó una revisión visual manual en móvil/escritorio con lector de pantalla.
- Harness: las pruebas iniciales fallaron por falta de RoleFormationPage/mapa y por destinos antiguos; tras implementar y actualizar expectativas obsoletas del ranking, pasaron. `npm.cmd --prefix frontend test`: 22 archivos, 109 pruebas aprobadas. `npm.cmd --prefix frontend run build`: aprobado; Vite informa un aviso de chunk JS >500 kB. `npm.cmd --prefix frontend run lint`: aprobado con una advertencia preexistente en `SessionContext.tsx:45`.
- No se modificó backend, Matching, Skill Gap, base de datos, Docker, prompt V5 ni experimentos. No se realizaron llamadas a proveedores de IA ni se generaron costos. No se realizó validación visual manual ni captura de pantalla.

## Próximo paso — revisión del Checkpoint 4

Revisar los cambios locales del frontend y documentación, así como la lista de pruebas y el estado de T040. No crear otro commit ni hacer push hasta recibir la aprobación del usuario.
## Trabajo paralelo de Inicio e identidad visual

- Se preservan los cambios locales de Inicio, Habilidades, navegación inferior y el recorrido de autoevaluación, junto con las adiciones paralelas de `specs/001-autoevaluacion-matching/`. No forman parte del commit de Explainability.
- El registro de `specs/001` documenta como completadas las fases visuales 19–21 y sus validaciones automatizadas. Quedan abiertas para revisión visual manual T085 y T090; en fase 21 quedan pendientes T092 (preview del recurso animado) y T097 (revisión visual/responsive del diálogo). No inferir que esas revisiones manuales ocurrieron.
- La validación automática más reciente registrada por esa instancia es: suite frontend 15 archivos/90 pruebas, build exitoso y lint exitoso con advertencia existente en `SessionContext.tsx:45`. Docker/Vite se inspeccionaron sin reiniciar servicios en el último registro.

## Cambios locales que deben preservarse

Mantener las modificaciones paralelas de frontend/identidad visual, `docker-compose.yml`, `specs/001-autoevaluacion-matching/`, `experiments/` y demás archivos locales. No incorporar el entorno experimental ni cambios ajenos a este checkpoint en staging.

Los checkpoints cronológicos previos están en [`progress/history.md`](history.md). Verificar siempre Git y el estado real antes de continuar.
