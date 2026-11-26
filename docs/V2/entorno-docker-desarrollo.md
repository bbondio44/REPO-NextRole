# Entorno local de desarrollo con Docker Compose

Compose inicia PostgreSQL 16, el backend Spring Boot y el frontend React/Vite. PostgreSQL usa el volumen persistente `nextrole-postgres`. El backend ejecuta Flyway al iniciar; no borres el volumen ni reinicies la base para resolver problemas de aplicación.

## Requisitos y configuración

- Docker Desktop o Docker Engine en ejecución, con Docker Compose v2.
- Archivo `.env` local creado a partir de `.env.example`, con una clave JWT válida para desarrollo. `.env` debe permanecer fuera de Git.

En PowerShell, desde la raíz:

```powershell
Copy-Item .env.example .env
```

Revisá las variables y configurá el secreto local antes de iniciar. No compartas ni commits `.env` o credenciales. El Compose predeterminado publica los puertos de la máquina host en loopback; acceso desde un celular requiere una configuración LAN explícita, una red compartida y reglas de firewall apropiadas. No cambies la configuración local sin autorización.

## Iniciar y consultar

```powershell
docker compose up -d
docker compose ps
```

| Servicio | Host local | Uso |
|---|---|---|
| Frontend | `http://localhost:5173` | React/Vite con recarga en caliente |
| Backend | `http://localhost:8090` | API Spring Boot (internamente `backend:8080`) |
| Salud del backend | `http://localhost:8090/actuator/health` | Healthcheck de Spring Boot |
| PostgreSQL | `postgres:5432` dentro de la red Compose | No se publica un puerto host por defecto |

Vite reenvía `/api` a `backend:8080`. El flujo V2 de autoevaluación usa los endpoints autenticados `/api/v2`; pantallas heredadas todavía consumen contratos V1.

## Verificar disponibilidad y módulos actuales de Vite

Comprobar que Compose muestre servicios en ejecución y que frontend y API respondan HTTP:

```powershell
Invoke-WebRequest http://localhost:5173/ -UseBasicParsing
Invoke-WebRequest http://localhost:8090/actuator/health -UseBasicParsing
```

Para comprobar la fuente transformada actual, obtener del HTML de Vite una ruta de módulo importada por la pantalla que cambió y consultarla directamente. Por ejemplo:

```powershell
$page = Invoke-WebRequest http://localhost:5173/ -UseBasicParsing
$page.Content
Invoke-WebRequest http://localhost:5173/src/main.tsx -UseBasicParsing
```

Si el módulo devuelto no contiene los cambios esperados, revisar `docker compose logs --tail=100 frontend` y que `frontend/` esté montado en el contenedor. La respuesta HTTP 200 comprueba disponibilidad; la comparación del contenido con la fuente local es la que ayuda a detectar una versión obsoleta. Una captura o inspección del navegador sigue siendo necesaria para validar la interfaz visualmente.

## Reinicios y reconstrucciones

El código frontend está montado desde `frontend/`; las ediciones normales deben actualizarse mediante Vite sin reconstrucción. Si el servidor quedó obsoleto, reiniciá solo frontend:

```powershell
docker compose restart frontend
```

Para cambios en código del backend, reconstruí solo ese servicio:

```powershell
docker compose up -d --build backend
```

Reconstruí frontend únicamente cuando cambien sus dependencias o Dockerfile. No reinicies PostgreSQL para actualizar código. `docker compose down` detiene el stack pero conserva el volumen; no uses `down -v`.

## Diagnóstico

```powershell
docker compose logs --tail=100 frontend
docker compose logs --tail=100 backend
docker compose logs --tail=100 postgres
docker compose exec postgres pg_isready -U nextrole -d nextrole
```

Si falla la API, comprobar salud/logs del backend y PostgreSQL. Flyway aplica migraciones al inicio; no edites migraciones ya aplicadas ni elimines datos para solucionar un error. Los cambios de esquema requieren una migración aditiva aprobada y validación en una base desechable.

## Pruebas locales

`mvn -f backend/pom.xml test` ejecuta pruebas backend; las de integración Testcontainers requieren Docker. `npm --prefix frontend test`, `npm --prefix frontend run build` y `npm --prefix frontend run lint` validan el frontend. No hace falta levantar o reconstruir Docker para build, lint ni pruebas unitarias sin dependencias externas.
