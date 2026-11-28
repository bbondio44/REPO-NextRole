# NextRole

NextRole es una plataforma de orientación profesional para personas que buscan iniciar o desarrollar su carrera en IT.

Su objetivo es ayudar a los usuarios a conocer sus habilidades, identificar los roles profesionales que mejor se ajustan a su perfil, detectar brechas de conocimiento y acceder a recomendaciones de formación.

El repositorio conserva NextRole V1 y su evolución V2.

## Versiones

- **V1:** está preservada en `main`. Algunos flujos y catálogos heredados siguen utilizándose en pantallas que todavía no migraron.
- **V2:** se desarrolla e integra en `develop-v2`. Su flujo de autoevaluación utiliza un backend Spring Boot, un catálogo versionado de 31 competencias y respuestas persistidas por intento. Los resultados completados conservan su ranking y contexto de versión.

La rama `develop-v2` es la base de trabajo actual.

## Desarrollo local

**Requisitos:** Git y Docker Desktop o Docker Engine con Compose v2.

Desde la raíz del repositorio, ejecutar:

```powershell
Copy-Item .env.example .env
docker compose up -d
```

No es necesario modificar los valores del archivo `.env` para el desarrollo local. El primer comando solo debe ejecutarse si el archivo todavía no existe.

### Servicios disponibles

| Servicio | Dirección local |
|---|---|
| Aplicación React/Vite | http://localhost:5173 |
| API Spring Boot | http://localhost:8090 |
| Salud de la API | http://localhost:8090/actuator/health |

## Guías de trabajo

- [Metodología SDD y recuperación de sesiones](docs/GUIA_SDD_NEXTROLE.md)
- [Trabajo con Git y GitHub](docs/GUIA_GIT_NEXTROLE.md)
