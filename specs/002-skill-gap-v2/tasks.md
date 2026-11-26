# Tasks: Diagnóstico Skill Gap V2

**Input**: Design documents in `specs/002-skill-gap-v2/`.
**Prerequisites**: `spec.md`, `plan.md`, `research.md`, `data-model.md`, `contracts/skill-gap-api.md`, `quickstart.md`.
**Tests**: Required for calculation rules, API contract, ownership and historical behavior.

## Phase 1: Prerequisite — V2 history validation

- [X] T001 Complete the T025 Testcontainers assertions in `backend/src/test/java/com/nextrole/assessment/AssessmentHistoryIntegrationTest.java`: assert one persisted DRAFT per user and that an existing draft retains its questionnaire/matrix versions after newer current versions appear. T025 is marked complete in `specs/001-autoevaluacion-matching/tasks.md` after the test passed.

## Phase 2: Foundational

No new infrastructure, database model, migration, dependency or frontend setup is required.

## Phase 3: User Story 1 — Entregar cálculo determinista bajo demanda (Priority: P1) 🎯 MVP

**Goal**: Obtener brechas de un intento completado propio y rol V2 sin persistir resultados.

**Independent Test**: El servicio/endpoint entrega todos los campos aprobados, incluye brecha cero, excluye `NOT_RELEVANT`, aplica `GapPolicy.amount` y no altera matching ni datos almacenados.

### Tests first

- [X] T002 [P] [US1] Add unit tests for exact gap calculation, level zero, covered level, importance and irrelevant-row filtering in `backend/src/test/java/com/nextrole/skillgap/SkillGapV2ServiceTest.java`.
- [X] T003 [US1] Add REST contract tests for response fields, authentication, owner scoping, errors and non-persistent reads in `backend/src/test/java/com/nextrole/skillgap/SkillGapV2ApiIntegrationTest.java`.

### Implementation

- [X] T004 [US1] Implement a read-only V2 projection in `backend/src/main/java/com/nextrole/skillgap/SkillGapV2Service.java`, using `data-model.md` (`assessment_attempts.id/user_id/status/questionnaire_version_id/matrix_version_id`, `assessment_answers.attempt_id/competency_code/level`, `assessment_questions.questionnaire_version_id/competency_code/title`, `assessment_role_requirements.matrix_version_id/role_id/competency_code/importance/expected_level`, `assessment_roles.id/name`) and only `GapPolicy.amount`; reject incomplete data instead of converting it to zero.
- [X] T005 [US1] Define the internal result records in `backend/src/main/java/com/nextrole/skillgap/SkillGapV2Dtos.java` and authenticated `GET /api/v2/assessment-attempts/{attemptId}/skill-gaps/{roleId}` in `backend/src/main/java/com/nextrole/skillgap/SkillGapV2Controller.java`; do not write `GapRun`/`GapItem` or expose priority, description, percentage, question text or matching results.

## Phase 4: User Story 2 — Preservar el contexto histórico (Priority: P1)

**Goal**: Aislar respuestas y requisitos de cada intento completado, aunque coexista un borrador o cambie la versión actual.

**Independent Test**: Para dos intentos y matrices diferentes, la API devuelve los requisitos históricos propios del ID solicitado; una matriz nueva no cambia el resultado anterior.

### Tests

- [X] T006 [US2] Add Testcontainers coverage for two completed attempts with different answers/matrix versions, newer current versions, draft isolation, owner isolation, and no diagnostic writes in `backend/src/test/java/com/nextrole/skillgap/SkillGapV2ApiIntegrationTest.java`. Depend on T001/T025 before declaring historical validation complete; do not duplicate T025's draft assertions.

## Phase 5: User Story 3 — Rechazar contexto no aplicable o inconsistente (Priority: P2)

**Goal**: No emitir brechas a partir de borradores, intentos descartados o datos incompletos.

**Independent Test**: Los casos no aplicables generan errores explícitos y nunca sustituyen el intento ni convierten respuesta ausente a nivel cero.

### Tests

- [X] T007 [US3] Add service/API tests for draft, discarded, missing/foreign attempt, invalid V2 role, missing relevant answer, and no relevant requirements in `backend/src/test/java/com/nextrole/skillgap/SkillGapV2ServiceTest.java` and `backend/src/test/java/com/nextrole/skillgap/SkillGapV2ApiIntegrationTest.java`.

### Implementation

- [X] T008 [US3] Return the agreed `ATTEMPT_NOT_COMPLETED` and `INCONSISTENT_COMPLETED_ATTEMPT` errors and empty results for roles without relevant requirements in `backend/src/main/java/com/nextrole/skillgap/SkillGapV2Service.java` and `backend/src/main/java/com/nextrole/skillgap/SkillGapV2Controller.java`; use the existing API error envelope and never fall back to V1/current data.

## Phase 6: Polish and validation

- [X] T009 Update `specs/002-skill-gap-v2/quickstart.md` with exact runnable backend validation and T025 dependency evidence; do not add frontend/manual UI scenarios.
- [X] T010 Run focused and full backend tests plus `git diff --check`; record actual results, Docker/Testcontainers availability, pending manual/UI work and preserved local files in `progress/current.md`.

## Dependencies and execution order

- T001/T025 validates the existing assessment version-pinning foundation before historical Skill Gap validation.
- US1 provides the calculation/REST contract required by US2 and US3.
- T006 depends on T001 and the US1 service/endpoint.
- T009/T010 follow implementation and applicable tests.
- No task changes frontend/navigation, Learning V1, persistent schema, role requirements, competencies, matching weights/results or `MatchingEngineV2`.

## Parallel opportunities

- T002 can be authored independently from the REST contract tests in T003; both test files are distinct.
- T006 and T007 share the integration test file and must be coordinated rather than run in parallel.
- Backend service and controller are coupled by the internal result contract; implement together after tests specify it.

## Implementation strategy

Completed T025, delivered US1 as the authenticated API, and validated historical isolation (US2) and invalid contexts (US3). Focused and full backend suites passed. Frontend presentation and navigation remain deferred to consumer features; no commit or push is implied.
