# Codex continuity checkpoint

**Updated**: 2026-11-20

## Active application feature

- Feature: `001-autoevaluacion-matching`.
- Spec Kit state: Constitution, Specification, Clarify, Plan and Tasks exist for this feature. Spec Kit `tasks.md` remains authoritative for task status.
- This checkpoint is session context only. It is not a backlog and does not track individual tasks.

## Setup and Foundational checkpoint

- Work: implement Setup and Foundational tasks T001–T005 for `001-autoevaluacion-matching` only.
- Completed: T001–T005 are marked complete in `specs/001-autoevaluacion-matching/tasks.md`. V5 is additive, seeds 31 questions, 155 options, 186 requirements and six IDs into a separate `assessment_roles` catalog. V1–V4 and V1 role rows are unchanged. JPA mappings/repositories cover V2 catalogs, attempts, answers and results.
- Validation: `mvn -f backend/pom.xml test` passed (10 tests) against disposable PostgreSQL 16 Testcontainers; Flyway applied V1–V5 and the canonical catalog/matrix comparison passed. `npm.cmd test -- --passWithNoTests` exited successfully but found no frontend tests yet. Frontend build passed. Frontend lint exited successfully with one existing `react(only-export-components)` warning in `src/app/SessionContext.tsx`. No user database was used.
- Issue resolved: an initial V5 seed in the shared V1 `roles` table changed V1 matching output from 8 to 10 roles. Moved the approved six IDs to `assessment_roles`; the full suite now passes and V1 role behavior remains unchanged.
- Blockers: none for Setup and Foundational. T006 onward (US1) is pending and was not started.
- Next step at that checkpoint: await explicit authorization to begin US1; no later story was started at that time.

## Docker development environment update

- Work: adapt the existing Compose setup to run PostgreSQL, Spring Boot and React/Vite together; this is local development infrastructure and does not advance `001-autoevaluacion-matching` beyond T001–T005.
- Changes: added frontend dev image, backend healthcheck, Compose dependencies/healthchecks, configurable host ports, a Vite `/api` proxy, local environment template and Docker workflow guide. Kept the PostgreSQL volume name and previous local credentials as defaults.
- Validation: `docker-compose --env-file .env.example config`, frontend build and lint passed (lint reports its existing Fast Refresh warning). An isolated Docker Compose project built both images, reached healthy for all services, returned `UP` from Actuator and HTTP 200 from Vite, and applied Flyway V1–V5 to a new PostgreSQL volume. The proxied protected roles route reached the backend and returned 401 as expected without authentication. Test containers, network and test-only volumes were removed; no existing project volume was used.
- Final Docker configuration: PostgreSQL remains internal on 5432 with no host port published; backend maps host 8090 to container 8080; frontend maps 5173 to 5173. The active Compose instance was verified healthy on those ports.
- Next step: T006 (US1) remains pending and requires its separate authorization; no assessment implementation task was started here.

## US1 implementation session

- Work: implement only T006–T015 for Autoevaluación and draft management. US2 matching/completion and US3 history/reevaluation were not started.
- Backend: added version-backed catalog DTO mapping, authenticated start/recover/current/detail endpoints, one active draft locking using the existing user row plus the V5 unique partial index, and owned answer autosave/read. Answer writes are restricted to DRAFT attempts; level 0 persists as a row and missing answers remain absent. Attempts pin the current questionnaire and matrix versions.
- Frontend: connected login/register session establishment to the existing JWT endpoints (access token held in memory; refresh uses the existing HttpOnly cookie), added typed V2 assessment API contracts, and wired the 31-question page to load/recover a draft and autosave answers. The five catalog-provided options render verbatim, including level 0. Results remain disabled pending US2 completion.
- Validation: `mvn -f backend/pom.xml test` passed (15 tests), including V1 regression/integration tests and US1 API tests using disposable Testcontainers PostgreSQL; Flyway applied V1–V5. Frontend tests passed (5 tests), build passed, lint passed with one existing Fast Refresh warning in `SessionContext.tsx`; `git diff --check` passed. No persistent user database was used by tests.
- Next step: await authorization before starting US2. No commits, push or merges were performed.

## Previous session note

- Work: lightweight Harness guidance integration with Spec Kit.
- Changes: root `AGENTS.md`, `CHECKPOINTS.md`, and this continuity checkpoint.
- Validation: inspected Constitution 1.1.0, Spec Kit/Codex integration, repository structure, Harness guidance/scripts, Maven tests and frontend scripts. Ran the external `scripts/verify.py --mode inspect`: Java, Maven and Docker were found, but its app paths (`backend/pom.xml`, `docker-compose.yml`, migrations) are absent from the Harness snapshot. Did not run `harness_check.py` because it requires the parallel `feature_list.json` backlog.
- Application checks: not run; that request changed documentation/configuration guidance only.
