# Estado vigente de NextRole V2

**Actualizado:** 2026-11-27
**Rama activa:** `develop-v2`
**HEAD/checkpoint:** `55ff734` — `Reorganiza Inicio e integra la introducción de autoevaluación`; Checkpoint 1 de Explainability permanece en `9f90c9af9aafd46226ee8a2f76accfff65299a68`.
**Estado Git:** `develop-v2` está tres commits por delante y dos por detrás de `origin/develop-v2`. El staging estaba vacío al preparar el cierre selectivo de Checkpoint 2; quedan cambios locales paralelos que deben preservarse.

## Estado actual — Implement, Explainability Checkpoint 2

- Checkpoint 1 quedó en `9f90c9a`. En este checkpoint se completaron T008, T009 y T012–T017: contexto histórico determinístico, DTOs serializables, validación estricta, puerto/fake, adaptador OpenRouter con RestClient y servicio de generación inicial.
- T010 está parcialmente verificada: los tests del servicio pasan; las pruebas dependientes del controller/contrato HTTP se conservan para el checkpoint que implemente el controller. T031 permanece abierta: está agregado el fake válido básico de T014; faltan sus escenarios extendidos de error.
- El contexto toma el intento completado propietario, descripciones de niveles de su cuestionario fijado, matching persistido, y las brechas e importancia de Skill Gap V2 mediante llamadas directas a servicios Spring. Solo calcula `trainingPriority`; no recalcula matching ni gaps.
- El JSON backend tiene orden de propiedades explícito, números/valores compatibles con el contrato Pydantic y excluye identificadores/versions locales. `context_hash` es SHA-256 de los bytes UTF-8 del JSON determinístico enviado por Java. Python define equivalencia semántica, pero no una canonicalización byte-a-byte compartida; no se afirma que los hashes entre lenguajes coincidan.
- La generación inicial valida propiedad y estado COMPLETED; reclama una fila única antes de invocar al proveedor fuera de transacción; valida antes de persistir y finaliza mediante token/CAS. Reutiliza éxito y no llama de nuevo para estados GENERATING o FAILED. No se añadieron rutas públicas, retry manual ni recuperación de leases.
- V5 continúa íntegro, SHA-256 `84C45ABE0D028E4ADB7376E387597993D196F5F2C8990596D853D11FDCF23AF5`.
- `mvn -f backend/pom.xml test`: 69 pruebas, 0 fallos, 0 errores, 0 omitidas. Incluyó migraciones y persistencia en PostgreSQL/Testcontainers desechable; no se migró la base de desarrollo.
- No se realizaron llamadas reales a OpenRouter ni otros proveedores, costos, cambios frontend, cambios Docker, commits de Checkpoint 2 ni push.

## Próximo paso — Checkpoint 3

Crear el commit selectivo aprobado del motor de Explainability; después avanzar exclusivamente con las tareas de API, historial y recuperación autorizadas para Checkpoint 3. No incluir ni alterar cambios paralelos ni publicar commits.

## Trabajo paralelo de Inicio e identidad visual

- Se preservan los cambios locales de Inicio, Habilidades, navegación inferior y el recorrido de autoevaluación, junto con las adiciones paralelas de `specs/001-autoevaluacion-matching/`. No forman parte del commit de Explainability.
- El registro de `specs/001` documenta como completadas las fases visuales 19–21 y sus validaciones automatizadas. Quedan abiertas para revisión visual manual T085 y T090; en fase 21 quedan pendientes T092 (preview del recurso animado) y T097 (revisión visual/responsive del diálogo). No inferir que esas revisiones manuales ocurrieron.
- La validación automática más reciente registrada por esa instancia es: suite frontend 15 archivos/90 pruebas, build exitoso y lint exitoso con advertencia existente en `SessionContext.tsx:45`. Docker/Vite se inspeccionaron sin reiniciar servicios en el último registro.

## Cambios locales que deben preservarse

Mantener las modificaciones paralelas de frontend/identidad visual, `docker-compose.yml`, `specs/001-autoevaluacion-matching/`, `experiments/` y demás archivos locales. No incorporar el entorno experimental ni cambios ajenos a este checkpoint en staging.

Los checkpoints cronológicos previos están en [`progress/history.md`](history.md). Verificar siempre Git y el estado real antes de continuar.
