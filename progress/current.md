# Estado vigente de NextRole V2

**Actualizado:** 2026-11-27
**Rama activa:** `develop-v2`
**HEAD/checkpoint:** `2deff66c7dfd395f0bc28df61f46f04075c6b363` — `docs: define Explainability y Formación y prepara su implementación`; Checkpoint 1 de Explainability listo para commit selectivo.
**Estado Git:** rama un commit por delante de `origin/develop-v2`; hay cambios locales del Checkpoint 1 y del trabajo visual paralelo, aún sin staging.

## Estado actual — Implement, Explainability Checkpoint 1

- Se completaron T001–T007 y T036 de `specs/003-explainability-formacion/tasks.md`. No se avanzó a construcción de contexto, proveedor HTTP, generación de diagnósticos ni frontend.
- V7 es aditiva y parte de V6. Agrega las descripciones y nombres profesionales oficiales en `assessment_roles` y `explainability_diagnostics` con estados, snapshots, JSONB, metadatos operativos, unicidad, claves foráneas y restricciones. V1–V6 no se modificaron.
- El catálogo autenticado V2 está disponible en `GET /api/v2/roles/{roleId}`. El catálogo V1 y sus rutas no cambiaron.
- Se incorporaron el mapeo y repositorio de diagnósticos con operaciones compare-and-set/fencing, configuración tipada, selección de la versión conocida V5 y el recurso de prompt. No hay adaptador OpenRouter ni generación conectada.
- SHA-256 del recurso V5: `84C45ABE0D028E4ADB7376E387597993D196F5F2C8990596D853D11FDCF23AF5`, igual al aprobado y al prompt experimental. El contrato Python queda referenciado en los recursos de prueba.
- V7 se ejecutó únicamente en PostgreSQL desechable mediante Testcontainers. No se migró la base de desarrollo ni se reiniciaron servicios.
- Validación: `mvn -f backend/pom.xml test` finalizó con 48 pruebas, 0 fallos y 0 errores. Después se ejecutó otra vez el test de catálogo/configuración con API key explícitamente vacía (3 pruebas, 0 fallos), para verificar binding y arranque sin credencial.
- Los tests demuestran que PostgreSQL impone unicidad, claves foráneas y checks de estado/payload; el repositorio impone las transiciones compare-and-set, fencing por token y no sobrescritura de éxitos. Las garantías de autorización y orquestación quedan para tareas posteriores.
- No hubo llamadas a proveedores, costos, commits ni push.

## Próximo paso — Explainability

Crear el commit selectivo del Checkpoint 1 aprobado; después avanzar únicamente con T008–T010, T012–T017 y el fake básico asociado a T014. V7 no debe aplicarse a la base compartida.

## Trabajo paralelo de Inicio e identidad visual

- Se preservan los cambios locales de Inicio, Habilidades, navegación inferior y el recorrido de autoevaluación, junto con las adiciones paralelas de `specs/001-autoevaluacion-matching/`. No forman parte del commit de Explainability.
- El registro de `specs/001` documenta como completadas las fases visuales 19–21 y sus validaciones automatizadas. Quedan abiertas para revisión visual manual T085 y T090; en fase 21 quedan pendientes T092 (preview del recurso animado) y T097 (revisión visual/responsive del diálogo). No inferir que esas revisiones manuales ocurrieron.
- La validación automática más reciente registrada por esa instancia es: suite frontend 15 archivos/90 pruebas, build exitoso y lint exitoso con advertencia existente en `SessionContext.tsx:45`. Docker/Vite se inspeccionaron sin reiniciar servicios en el último registro.

## Cambios locales que deben preservarse

Mantener las modificaciones paralelas de frontend/identidad visual, `docker-compose.yml`, `specs/001-autoevaluacion-matching/`, `experiments/` y demás archivos locales. No incorporar el entorno experimental ni cambios ajenos a este checkpoint en staging.

Los checkpoints cronológicos previos están en [`progress/history.md`](history.md). Verificar siempre Git y el estado real antes de continuar.
