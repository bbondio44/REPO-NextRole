# Estado vigente de NextRole V2

**Actualizado:** 2026-11-27
**Rama activa:** `develop-v2`
**HEAD/checkpoint:** `1409ec1` — `feat: implementa el motor de Explainability`; incluye el commit selectivo de Checkpoint 2. Checkpoint 1 permanece en `9f90c9af9aafd46226ee8a2f76accfff65299a68`.
**Estado Git:** `develop-v2` está cuatro commits por delante y dos por detrás de `origin/develop-v2`. El Checkpoint 3 está sin commit; permanecen modificaciones locales paralelas y el staging debe revisarse antes de cualquier operación Git.

## Estado actual — Implement, Explainability Checkpoint 3

- Checkpoint 1 quedó en `9f90c9a`; Checkpoint 2 se publicó localmente en `1409ec1` (`feat: implementa el motor de Explainability`). En Checkpoint 3 se completaron T010, T018, T021, T023, T025, T026, T028, T029, T031 y T038.
- La API autenticada GET/generate/retry valida propiedad, intento COMPLETED y rol vigente. GET es lectura pura: no crea ni actualiza filas ni invoca al proveedor, incluso ante lease vencido. Generate solo inicia si no existe fila; reusa éxitos, no reintenta fallos, y devuelve GENERATING sin duplicar llamadas. Retry exige acción explícita y reclama solo FAILED o GENERATING vencido mediante CAS.
- Se cubrieron intentos Q1/Q2 separados, respuestas versionadas, matching persistido, snapshots históricos de rol y reutilización inmutable tras cambiar catálogo/modelo/prompt. No se recalcula matching ni Skill Gap; generación conserva llamadas directas a servicios internos y prioridad local determinística.
- Recuperación y concurrencia PostgreSQL verificaron reclamaciones únicas para generación/retry, GET de lease vencido sin escritura, fencing de respuestas tardías y contador de intentos informativo. Los errores de timeout, proveedor/rate limit y respuesta inválida se traducen a errores HTTP seguros; metadatos ausentes siguen nulos.
- Fake de pruebas cubre respuesta válida/inválida, timeout, indisponibilidad, rate limit y demora sincronizada. El test-first dejó fallos iniciales por ausencia de API/recovery/escenarios fake; las fallas posteriores de pruebas fueron del fixture (propiedades singleton no reiniciadas y binding de `Instant` en JdbcTemplate), corregidas y verificadas.
- `mvn -f backend/pom.xml test`: 86 pruebas, 0 fallos, 0 errores, 0 omitidas. Pruebas de persistencia, migración, API y concurrencia utilizaron PostgreSQL desechable/Testcontainers.
- Prompt V5 permanece íntegro, SHA-256 `84C45ABE0D028E4ADB7376E387597993D196F5F2C8990596D853D11FDCF23AF5`. No se migró la base de desarrollo, no se modificó frontend/Docker ni los experimentos, y no se realizaron llamadas reales a IA ni costos.

## Próximo paso — revisión del Checkpoint 3

Revisar el diff de Checkpoint 3 y esperar la aprobación antes de crear otro commit o comenzar Checkpoint 4. No incluir ni alterar cambios paralelos ni publicar commits.

## Trabajo paralelo de Inicio e identidad visual

- Se preservan los cambios locales de Inicio, Habilidades, navegación inferior y el recorrido de autoevaluación, junto con las adiciones paralelas de `specs/001-autoevaluacion-matching/`. No forman parte del commit de Explainability.
- El registro de `specs/001` documenta como completadas las fases visuales 19–21 y sus validaciones automatizadas. Quedan abiertas para revisión visual manual T085 y T090; en fase 21 quedan pendientes T092 (preview del recurso animado) y T097 (revisión visual/responsive del diálogo). No inferir que esas revisiones manuales ocurrieron.
- La validación automática más reciente registrada por esa instancia es: suite frontend 15 archivos/90 pruebas, build exitoso y lint exitoso con advertencia existente en `SessionContext.tsx:45`. Docker/Vite se inspeccionaron sin reiniciar servicios en el último registro.

## Cambios locales que deben preservarse

Mantener las modificaciones paralelas de frontend/identidad visual, `docker-compose.yml`, `specs/001-autoevaluacion-matching/`, `experiments/` y demás archivos locales. No incorporar el entorno experimental ni cambios ajenos a este checkpoint en staging.

Los checkpoints cronológicos previos están en [`progress/history.md`](history.md). Verificar siempre Git y el estado real antes de continuar.
