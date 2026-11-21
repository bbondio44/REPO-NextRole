# Alcance implementado y trazabilidad al Workspace v1.0 (26/10/2026)

La sección 3 del documento distingue **MVP documental HU-01–11** de **MVP de producción propuesto**. Las ampliaciones requieren aprobación de Product Backlog; NO se consideran automáticamente contratadas.

## Núcleo entregado (requiere verificación real)

- Auth local con JWT/Bcrypt/refresh rotativo y roles STUDENT/ADMIN para autorización.
- Perfil académico, proyectos opcionales, perfil técnico por competencias y autovaloración.
- Catálogo de nueve áreas, 15 competencias, ocho roles con requisitos y ocho recursos formativos de documentación verificable.
- Diagnóstico por áreas; faltante, parcial, cubierta y no evaluada diferenciados.
- Preferencias profesionales simplificadas por dimensión.
- Matching determinista v1.0: 50% intereses + 30% competencias. El 20% de evidencia adicional del informe no se evalúa sin microevaluaciones/GitHub; los pesos existentes se renormalizan. Se informa confianza y cobertura.
- Resultados históricos de matching, brechas y plan de formación almacenados en PostgreSQL.
- Endpoints OpenAPI, validación, manejo de errores y esquema Flyway.

## Desviaciones o aspectos que necesitan desarrollo adicional

### Prioridad inmediata para validar las 11 HU

- Ejecutar `mvn verify` y pruebas con PostgreSQL real. No fue posible aquí por ausencia de Maven/red externa.
- Armar las pruebas de integración con Testcontainers y recorridos E2E (registro → evaluación → recomendación → plan → relogin).
- Acordar contratos de API con el responsable de frontend e integrar TanStack Query; el frontend todavía usa mocks.
- Refinar HU-03: campos completos de perfil técnico/profesional y clasificación de evidencias declaradas vs evaluadas.
- Refinar HU-10: demanda laboral real y dependencias/prerrequisitos aún NO inciden en la priorización; la fórmula actual usa nivel de brecha e importancia.
- Refinar HU-11: roadmap ordenado en base a prioridad, sin grafo de prerrequisitos ni seguimiento de avance; faltan recursos verificados para siete habilidades.
- Persistir y exponer versiones completas del catálogo, evaluación y evidencias que alimentan el matching; actualmente se conserva fecha y versión del algoritmo.

### Propuestas de producción no incluidas todavía

- Recuperación y verificación de correo (requieren servicio transaccional y flujos de token de un solo uso).
- Edición avanzada de usuario/contraseña, consentimientos específicos y políticas legales revisadas.
- Microevaluaciones deterministas con banco de preguntas, intentos y rúbrica.
- Ingesta pública de GitHub con consentimiento, límites y evidencia no concluyente.
- Inteligencia de mercado laboral argentino (fuentes autorizadas, normalización, programador de recolección, cobertura y métricas verificables).
- Administración CRUD de catálogos, permisos de administración y registro de auditoría de cambios.
- Tareas programadas, métricas avanzadas, monitorización externa, alertas, backups y restauraciones.
- Seguridad de producción: review OWASP, rate limiting distribuido, validación CSP y cookies cross-domain según hosting, auditoría y pruebas de ataque.
- Sistema de correo real y despliegue cloud/CI-CD.

## Notas de seguridad

- **No ejecutar públicamente** con la contraseña local del `docker-compose.yml` ni `COOKIE_SECURE=false`.
- La cookie de refresh sólo es para `/api/v1/auth`; `SameSite=Strict` y controles explícitos de `Origin` mitigan CSRF de esos dos endpoints. Confirmar comportamiento si frontend y backend terminan en dominios *cross-site* distintos y ajustar una estrategia de CSRF completa antes de producción.
- El limitador de intentos de login es **en memoria**; no sirve como protección multinodo/redis ni elimina automáticamente todas las claves antiguas. Reemplazar para producción.
- En su estado actual, la rama Spring Boot 3.5.x tiene soporte comunitario finalizado según anuncio oficial del 25/06/2026. Elegir una versión 4.x vigente y validarla antes del despliegue público, o disponer de soporte comercial para 3.5.x. La elección de 3.5.16 aquí sigue la rama mencionada en el informe para minimizar incompatibilidades de implementación inicial.

## Hitos recomendados de backend

1. **DB + Core Auth:** validar Flyway, registro/login, autorización, sesiones.
2. **Perfil + skills:** persistencia, diagnosis, pruebas de aislamiento por usuario.
3. **Roles + matching:** pruebas matemáticas de scoring y baja cobertura.
4. **Skill Gap + learning:** historial y plan; pertinencia, priorización y recursos.
5. **Intereses + evaluación técnica:** ampliar dimensión del matching con evidencia real.
6. **Integraciones / panel admin:** sólo después de aprobar alcance y fuentes.
7. **Hardening:** integración con React, CI, pruebas, monitoreo y backups.
