# Arquitectura técnica — entrega del núcleo

Este backend sigue el **monolito modular** propuesto en la Guía de Desarrollo NextRole 1.0. El frontend no debe acceder directamente a PostgreSQL; todo pasa por `/api/v1`.

```text
React + TanStack Query
        │ HTTPS JSON + JWT
        ▼
Spring Security (Bearer JWT)
        │
        ├── auth         registro, login, refresh rotativo, logout
        ├── profile      perfil, proyectos, rol objetivo, cuenta
        ├── skill        catálogo, autoevaluación, diagnóstico por área
        ├── interests    preferencias del usuario
        ├── role         catálogo, detalle y requisitos
        ├── matching     afinidad, cobertura, preparación y confianza
        ├── skillgap     brechas, estados, prioridades, snapshots
        ├── learning     cursos verificados y plan por pasos
        ├── common       errores y validaciones transversales
        ├── config       JWT, CORS, seguridad
        └── data         entidades JPA y repositorios
        │
        ▼
PostgreSQL (Flyway V1, V2, V3)
```

## Identificadores

- UUID para usuarios, sesiones, diagnósticos y planes.
- Slugs de catálogo para roles y habilidades porque ya son identificadores públicos consumidos por el frontend actual.
- Integridad relacional, FK y restricciones CHECK en las migraciones.

## Modelo de recomendación

- Afinidad: combinación normalizada de **intereses (peso nominal 50)** y **competencias (peso nominal 30)**.
- Evidencia adicional del **20** está excluida del cálculo mientras no haya una implementación verificable de microevaluaciones/GitHub.
- Se redistribuyen los pesos sólo entre las dimensiones disponibles.
- `preparation`: coincidencia técnica entre habilidades *evaluadas* y los requisitos del rol.
- `coverage`: fracción ponderada de los requisitos del rol para los que existe una respuesta.
- `confidence`: indicador orientativo dependiente de cobertura y presencia de intereses; no es una probabilidad validada.
- `matching-v1.0` queda persistido junto con cada ejecución y sus resultados. Las fórmulas de `Scoring` tienen pruebas unitarias.

**Interpretación imprescindible:** 90% de afinidad con 15% de cobertura técnica NO significa tener preparación técnica suficiente, y nunca se informa como probabilidad de obtener empleo.

## Skill Gap

- `no_evaluada`: nivel de conocimiento desconocido. No se usa `0` de forma implícita.
- `faltante`: respondió explícitamente `0` y el rol requiere más.
- `parcial`: nivel actual mayor que 0 pero inferior al requerido.
- `cubierta`: nivel actual igual o superior al requerido.
- Prioridad `pendiente`: antes de priorizar como brecha se requiere evaluar.
- Para diferencias conocidas se calcula `max(0, requerido - actual)` y se combina con la importancia esencial/complementaria.

## Integraciones futuras

Los siguientes dominios se incorporan sin microservicios al monolito modular después de aprobar el alcance: `assessment` (microevaluaciones), `github` (sólo repositorios públicos consentidos), `market` (fuentes autorizadas, tareas programadas, normalización), `admin` (contenido y auditoría). Redis, LLM y plataformas laborales no son prerrequisitos para validar las 11 HU documentales.

## Seguridad y operación

- Cuenta única por correo normalizado, contraseña BCrypt y access tokens firmados HS256 con clave aleatoria de al menos 256 bits.
- Refresh tokens aleatorios almacenados únicamente como SHA-256, rotación al renovar y revocación al cerrar sesión.
- Refresh token en cookie HttpOnly/SameSite Strict; exige `Origin` autorizado en `refresh`/`logout`; ajustar política si el despliegue usa dominios distintos.
- SQL con parámetros generados por Spring Data JPA, rutas privadas por JWT y recursos ligados al `userId` autenticado.
- OpenAPI/Swagger UI, salud de Actuator, configuración por variables de entorno.
- Riesgos de producción y obligaciones pendientes: ver `ALCANCE_Y_PENDIENTES.md`.
