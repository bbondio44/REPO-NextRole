# NextRole V2 — Backend

Spring Boot 3, Java 21, Maven, PostgreSQL, JPA and Flyway. The backend preserves V1 APIs used by legacy screens and adds authenticated `/api/v2` contracts for the versioned assessment lifecycle.

## Run locally

The recommended development environment is Docker Compose from the repository root; see [`docs/V2/entorno-docker-desarrollo.md`](../docs/V2/entorno-docker-desarrollo.md). For direct development, start a compatible PostgreSQL instance, configure datasource, JWT and allowed-origin settings from `backend/.env.example` in the current shell, then run:

```powershell
mvn -f backend/pom.xml spring-boot:run
```

Compose maps the backend to host port 8090 by default and runs the service on port 8080 inside Docker. Health and API documentation are available at `/actuator/health` and `/swagger-ui.html` respectively.

## V2 assessment boundary

The assessment module owns the versioned questionnaire, role matrix, attempts, answers, completion and persisted ranking. A user has at most one active draft; discard is a state transition, and completed results retain their original data and versions. Reevaluations start without copied answers. The matching implementation and canonical competency/matrix catalogue are documented in `specs/001-autoevaluacion-matching/` and `docs/V2/autoevaluacion-matriz-vigente.md`.

Other modules still use V1 contracts where they have not been migrated. Do not infer that every legacy screen uses the V2 assessment model.

## Tests

```powershell
mvn -f backend/pom.xml test
```

The suite includes unit, API contract and Testcontainers integration tests. Integration tests need an available Docker runtime. Flyway migrations that have been applied are immutable; schema changes require an approved additive migration and disposable-database validation.
