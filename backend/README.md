# NextRole — Entrega de backend (núcleo del MVP)

Código fuente inicial para incorporar **dentro del repositorio** `REPO-NextRole`, sin sobrescribir el frontend.

**Qué incluye**

- `backend/`: Spring Boot (Java 21), Maven, JWT con access token y refresh token rotativo, bcrypt, PostgreSQL/JPA, Flyway, API REST, validación, manejo de errores, Actuator y OpenAPI.
- `docker-compose.yml`: PostgreSQL local, con datos persistentes en volumen.
- `backend/src/main/resources/db/migration/`: esquema relacional y carga inicial de roles, competencias, niveles y documentación formativa.
- `docs/`: correspondencia HU → endpoints, instrucciones de integración, pendientes y consideraciones de seguridad.
- `integration/`: adaptador de referencia para conectar el frontend actual sin cambiar el stack.

## 1. Preparar el entorno

Requisitos: **JDK 21**, **Maven 3.9+** y **Docker** (o PostgreSQL 16+).

Desde la carpeta raíz de esta entrega:

```bash
docker compose up -d postgres
```

En otra terminal, preparar las variables de entorno (no se carga `.env` automáticamente):

**PowerShell**

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/nextrole"
$env:SPRING_DATASOURCE_USERNAME="nextrole"
$env:SPRING_DATASOURCE_PASSWORD="nextrole_local"
$env:JWT_SECRET="$(python -c 'import secrets,base64; print(base64.b64encode(secrets.token_bytes(48)).decode())')"
$env:ALLOWED_ORIGINS="http://localhost:5173"
$env:COOKIE_SECURE="false"
cd backend
mvn spring-boot:run
```

**Linux/macOS**

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/nextrole
export SPRING_DATASOURCE_USERNAME=nextrole
export SPRING_DATASOURCE_PASSWORD=nextrole_local
export JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')
export ALLOWED_ORIGINS=http://localhost:5173
export COOKIE_SECURE=false
cd backend
mvn spring-boot:run
```

> No utilizar las contraseñas locales del ejemplo en producción. Un `JWT_SECRET` nuevo invalidará las sesiones anteriores; guardarlo en un gestor de secretos.

## 2. Comprobar funcionamiento

- Salud: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- API base: `http://localhost:8080/api/v1`

Crear una cuenta desde Postman, Bruno o terminal:

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Ana","lastName":"Pérez","email":"ana.prueba@example.com","password":"Segura-12345!","confirmPassword":"Segura-12345!","acceptTerms":true}'
```

El registro devuelve `accessToken` (Bearer, 15 minutos) y configura una cookie `nr_refresh` HttpOnly, de 14 días. Los endpoints privados requieren `Authorization: Bearer <accessToken>`.

El endpoint `/auth/refresh` **exige** encabezado `Origin` autorizado y cookie de refresh; el navegador envía el origen automáticamente. CORS permite credenciales únicamente desde orígenes definidos en `ALLOWED_ORIGINS`.

## 3. Validar pruebas y generar artefacto

```bash
cd backend
mvn clean verify
mvn spring-boot:run
```

**Importante:** en el entorno donde preparé esta entrega no estaba disponible Maven ni se podía acceder a Maven Central, por lo que **no se pudo ejecutar `mvn verify` ni levantar PostgreSQL**. Sí se compilaron con JDK 21 las clases puras de scoring y clasificación de brechas. La compilación integral, pruebas de integración y validación real de endpoints están pendientes y deben realizarse antes de integrar.

## 4. Cómo añadirlo al repositorio

Con el equipo:

1. Partir de `develop` actualizado y crear `feature/backend-mvp-core`.
2. Copiar `backend/` y `docker-compose.yml` a la raíz del repositorio; fusionar `.gitignore` sin perder reglas existentes.
3. Ejecutar `mvn clean verify` y comprobar el esquema Flyway contra PostgreSQL local.
4. Integrar el frontend usando los contratos de `docs/API_CONTRATOS.md`.
5. Abrir PR **hacia `develop`**, adjuntando resultados de pruebas.

No contiene modificaciones al repositorio remoto y no incluye secretos reales.

## 5. Alcance y límites

Esta entrega es una **primera implementación del núcleo HU-01…HU-11**. Integra rutas para registro, autenticación, perfil y proyectos, niveles de habilidades, diagnóstico por área, intereses, ranking de roles, selección de rol, Skill Gap, cursos y planes de aprendizaje. Los catálogos se sirven desde PostgreSQL, no desde mocks.

**No es una versión lista para producción**: faltan recuperación/verificación de email, pruebas reales de integración y E2E, microevaluaciones técnicas, gestión completa administrativa, pipeline de mercado laboral, GitHub, monitorización externa, backups y despliegue. El informe trata varias de esas ampliaciones como *propuestas pendientes de aprobación*; ver `docs/ALCANCE_Y_PENDIENTES.md`.

Las ponderaciones del matching son hipótesis de trabajo y necesitan validación con estudiantes. Los datos del catálogo de roles son orientativos, no estadísticas del mercado argentino.
