# Entorno local de desarrollo con Docker Compose

El Compose de la raíz inicia PostgreSQL 16, la API Spring Boot y el servidor de desarrollo React/Vite. La base conserva sus datos en el volumen `nextrole-postgres`; Flyway aplica V1–V5 al iniciar el backend sobre una base nueva.

## Requisitos

- Docker Desktop o Docker Engine en ejecución.
- Docker Compose v2 (`docker compose`).

## Iniciar

Desde la raíz del repositorio, crear el archivo local ignorado por Git y configurar una clave JWT de desarrollo válida en Base64 (al menos 32 bytes una vez decodificada):

```bash
cp .env.example .env
```

En PowerShell:

```powershell
Copy-Item .env.example .env
```

`.env.example` contiene valores locales únicamente. La clave incluida es ficticia para desarrollo; reemplazala antes de compartir el entorno o utilizarlo fuera de una máquina local. No guardes secretos reales en el repositorio.

Iniciá los tres servicios:

```bash
docker compose up --build
```

Compose espera a que PostgreSQL esté saludable antes de iniciar Spring Boot y a que el endpoint de salud del backend responda antes de iniciar Vite.

| Servicio | Dirección desde el equipo | Uso |
|---|---|---|
| Frontend | `http://localhost:5173` | React/Vite con recarga en caliente |
| Backend | `http://localhost:8090` | API Spring Boot |
| Salud del backend | `http://localhost:8090/actuator/health` | Estado de Spring Boot |
| Swagger UI | `http://localhost:8090/swagger-ui.html` | Contratos disponibles |
| PostgreSQL | Solo dentro de la red Docker, en `postgres:5432` | Sin puerto publicado hacia Windows |

Los puertos host predeterminados del backend y frontend se pueden cambiar en `.env` (`BACKEND_PORT`, `FRONTEND_PORT`) si otro proceso los está usando. PostgreSQL no publica un puerto al equipo host; las conexiones entre contenedores usan `postgres:5432` y `backend:8080`.

Dentro de la red Compose, el backend usa `postgres:5432` como host de base y el proxy de Vite dirige `/api` a `backend:8080`. El cliente React actual todavía usa sus servicios mock; el proxy deja preparado el canal local para la integración API sin cambiar el comportamiento funcional de la aplicación.

## Detener y reconstruir

Para detener y retirar contenedores y red, conservando los datos PostgreSQL:

```bash
docker compose down
```

Para reconstruir imágenes después de cambios en dependencias o Dockerfiles:

```bash
docker compose up --build
```

El código del frontend se monta desde `frontend/`, por lo que Vite detecta cambios. El contenedor ejecuta `npm ci` al iniciar para sincronizar `node_modules` con el lockfile.

El volumen PostgreSQL es persistente y se reutiliza entre reinicios. No elimines el volumen para resolver problemas de inicio: eso borraría los datos locales. La configuración inicial de usuario/contraseña de PostgreSQL solo se aplica al crear el volumen por primera vez. Si ya existe uno, conservá las credenciales con las que fue creado. El Compose mantiene `nextrole_local` como valor predeterminado histórico de desarrollo para seguir usando volúmenes creados con la configuración previa.

## Diagnóstico

Ver estado y healthchecks:

```bash
docker compose ps
```

Consultar logs de un servicio o de todos:

```bash
docker compose logs -f postgres
docker compose logs -f backend
docker compose logs -f frontend
```

Comprobar API y base desde el equipo:

```bash
curl http://localhost:8090/actuator/health
docker compose exec postgres pg_isready -U nextrole -d nextrole
```

Si el puerto `8090` o `5173` ya está ocupado, liberalo o elegí otro puerto host en `.env`. PostgreSQL no requiere un puerto host disponible. Si el backend no inicia, revisá primero los logs de PostgreSQL, luego que `.env` tenga una `JWT_SECRET` Base64 válida y que las credenciales coincidan con las del volumen persistido. Flyway reporta en esos logs cualquier error al aplicar V1–V5.

## Desarrollo sin Docker

El entorno local sigue disponible: ejecutar PostgreSQL localmente, configurar las variables de `backend/.env.example` en la sesión y arrancar Spring Boot con Maven; en otra terminal, usar `npm install` y `npm run dev` desde `frontend/`. El backend conecta a `localhost:5432` fuera de Compose. No es necesario ejecutar Compose para compilar ni correr las pruebas unitarias; las pruebas Testcontainers necesitan un runtime Docker operativo.
