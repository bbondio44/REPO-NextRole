# NextRole

NextRole es una plataforma de orientación profesional para personas que buscan iniciar o desarrollar su carrera en IT. El repositorio conserva NextRole V1 y su evolución V2.

## Versiones

- **V1** está preservada en `main`; algunos flujos y catálogos heredados siguen utilizándose en pantallas que todavía no migraron.
- **V2** se desarrolla e integra en `develop-v2`. Su flujo de autoevaluación usa el backend Spring Boot, el catálogo versionado de 31 competencias y respuestas persistidas por intento. Los resultados completados conservan su ranking y contexto de versión.

La rama `develop-v2` es la base de trabajo actual. La guía de SDD describe cómo recuperar contexto y seguir los artefactos de Spec Kit. La guía de Git cubre ramas, revisión, commits autorizados y publicación.

## Desarrollo local

Requisitos: Docker Desktop/Engine con Compose v2, y Git. Desde la raíz:

```powershell
Copy-Item .env.example .env
docker compose up -d
```

Revisá `.env.example` y configurá el secreto local de desarrollo según `docs/V2/entorno-docker-desarrollo.md`. No publiques `.env` ni credenciales.

| Servicio | Dirección local |
|---|---|
| Aplicación React/Vite | http://localhost:5173 |
| API Spring Boot | http://localhost:8090 |
| Salud de la API | http://localhost:8090/actuator/health |

Compose monta el código de `frontend/` para que Vite actualice los módulos durante el desarrollo. No hace falta reconstruir imágenes por cada edición de código; consultá la guía de entorno para comprobar HTTP, frescura y reiniciar solo el servicio necesario.

## Validaciones

Desde la raíz del repositorio:

```powershell
mvn -f backend/pom.xml test
npm --prefix frontend test
npm --prefix frontend run build
npm --prefix frontend run lint
```

Las pruebas de integración Testcontainers requieren Docker disponible. Ejecutá solo las comprobaciones afectadas por el cambio y registrá explícitamente las que no se realizaron.

## Estado y especificaciones

`specs/001-autoevaluacion-matching/` es la especificación base del flujo V2 implementado. Las funcionalidades mayores futuras tendrán especificaciones independientes vinculadas a sus contratos existentes. `progress/current.md` resume el estado vigente y `progress/history.md` conserva checkpoints anteriores.

## Guías

- [Metodología SDD y recuperación de sesiones](docs/GUIA_SDD_NEXTROLE.md)
- [Trabajo con Git y GitHub](docs/GUIA_GIT_NEXTROLE.md)
- [Entorno Docker Compose V2](docs/V2/entorno-docker-desarrollo.md)
- [README del frontend](frontend/README.md)
- [README del backend](backend/README.md)
