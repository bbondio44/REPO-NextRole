# NextRole

NextRole es una plataforma de orientación profesional para personas que buscan iniciar o desarrollar su carrera en IT.

Su objetivo es ayudar a los usuarios a conocer sus habilidades, identificar los roles profesionales que mejor se ajustan a su perfil, detectar brechas de conocimiento y acceder a recomendaciones de formación.

El repositorio conserva NextRole V1 y su evolución V2.

## Versiones

- **V1:** está preservada en `main`. Algunos flujos y catálogos heredados siguen utilizándose en pantallas que todavía no migraron.
- **V2:** se desarrolla e integra en `develop-v2`. Su flujo de autoevaluación utiliza un backend Spring Boot, un catálogo versionado de 31 competencias y respuestas persistidas por intento. Los resultados completados conservan su ranking y contexto de versión.

La rama `develop-v2` es la base de trabajo actual.

## Desarrollo local

### Requisitos

- Git.
- Docker Desktop o Docker Engine con Compose v2.

Desde la raíz del repositorio, ejecutar:

```powershell
Copy-Item .env.example .env
docker compose up -d
```

Revisar el archivo `.env.example` y configurar las variables de entorno necesarias para el desarrollo local.

No publicar el archivo `.env` ni compartir credenciales.

### Servicios disponibles

| Servicio | Dirección local |
|---|---|
| Aplicación React/Vite | http://localhost:5173 |
| API Spring Boot | http://localhost:8090 |
| Salud de la API | http://localhost:8090/actuator/health |

## Guías de trabajo

El proyecto utiliza una metodología de desarrollo basada en especificaciones (SDD) y un flujo de trabajo organizado mediante Git y GitHub.

- [Metodología SDD y recuperación de sesiones](https://github.com/bbondio44/REPO-NextRole/blob/develop-v2/docs/GUIA_SDD_NEXTROLE.md)
- [Trabajo con Git y GitHub](https://github.com/bbondio44/REPO-NextRole/blob/develop-v2/docs/GUIA_GIT_NEXTROLE.md)
