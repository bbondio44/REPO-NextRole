---
description: "Dependency-ordered implementation tasks for V2 assessment and role matching"
---

# Tasks: Autoevaluación técnica y matching de roles IT

**Input**: Design documents from `/specs/001-autoevaluacion-matching/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/assessment-api.md`, `quickstart.md`.

**Tests**: Included as explicitly requested. Write contract/unit/integration tests before their implementing tasks and verify behavior through existing Maven/JUnit/Testcontainers plus the frontend test setup created in Phase 1.

**Organization**: Tasks follow the three user stories in `spec.md`, after shared setup and persistence foundations.

## Format: `[ID] [P?] [Story] Description`

- `[P]` marks work that can run in parallel without editing the same files or depending on unfinished tasks.
- `[US1]`, `[US2]`, `[US3]` map to the specification's user stories. Setup, foundational, and polish tasks have no story label.
- Every task names its target file path and any task dependencies.

## Phase 1: Setup

**Purpose**: Configure the missing frontend test capability; backend JUnit and Testcontainers are already present.

- [X] T001 Add Vitest, Testing Library and jsdom configuration plus a `test` script in `frontend/package.json`, `frontend/package-lock.json`, `frontend/vite.config.ts`, and `frontend/src/test/setup.ts`.

## Phase 2: Foundational

**Purpose**: Establish V2 database invariants and persistence mappings used by all stories.

- [X] T002 [P] Add a Testcontainers migration/seed verification test in `backend/src/test/java/com/nextrole/assessment/AssessmentMigrationIntegrationTest.java`; compare every canonical field against `docs/V2/autoevaluacion-matriz-vigente.md`: all 31 competency titles and question texts, all 155 level descriptions (five per competency), and all 186 role/competency assignments including importance, relevance and expected level. Also assert counts and the six approved role IDs; a count-only check is insufficient.
- [X] T003 Implement additive `backend/src/main/resources/db/migration/V5__assessment_v2.sql` for all versioned tables, constraints/indexes, and exact initial seeds. Seed the six approved roles in `assessment_roles`, separate from V1 `roles`; preserve all V1 role rows and behavior. Seed the 31 questions/options and all 186 matrix cells exactly from the approved Markdown. Resolve every canonical field constraint in `data-model.md`: `assessment_questionnaire_versions.id`, `assessment_questionnaire_versions.version_code`, `assessment_questionnaire_versions.published_at`, `assessment_questionnaire_versions.is_current`, `assessment_questions.questionnaire_version_id`, `assessment_questions.competency_code`, `assessment_questions.position`, `assessment_questions.title`, `assessment_questions.question_text`, `assessment_answer_options.questionnaire_version_id`, `assessment_answer_options.competency_code`, `assessment_answer_options.level`, `assessment_answer_options.description`, `assessment_matrix_versions.id`, `assessment_matrix_versions.version_code`, `assessment_matrix_versions.published_at`, `assessment_matrix_versions.is_current`, `assessment_role_requirements.matrix_version_id`, `assessment_role_requirements.role_id`, `assessment_role_requirements.competency_code`, `assessment_role_requirements.importance`, `assessment_role_requirements.expected_level`, `assessment_attempts.id`, `assessment_attempts.user_id`, `assessment_attempts.status`, `assessment_attempts.questionnaire_version_id`, `assessment_attempts.matrix_version_id`, `assessment_attempts.created_at`, `assessment_attempts.completed_at`, `assessment_attempts.discarded_at`, `assessment_attempts.source_attempt_id`, `assessment_answers.attempt_id`, `assessment_answers.questionnaire_version_id`, `assessment_answers.competency_code`, `assessment_answers.level`, `assessment_answers.updated_at`, `assessment_result_sets.attempt_id`, `assessment_result_sets.algorithm_version`, `assessment_result_sets.calculated_at`, `assessment_role_results.result_set_id`, `assessment_role_results.role_id`, `assessment_role_results.score_numerator`, `assessment_role_results.score_denominator`, `assessment_role_results.display_percentage`, and `assessment_role_results.rank_position`. `assessment_answers.questionnaire_version_id` is persisted as the composite-FK discriminator that must match its attempt's pinned version. Depends on T002.
- [X] T004 [P] Add V2 role-catalog, questionnaire and matrix JPA entities/repositories under `backend/src/main/java/com/nextrole/data/` from the field constraints in `data-model.md`, including `assessment_roles.id/name`, `assessment_questionnaire_versions.id/version_code/published_at/is_current`, `assessment_questions.questionnaire_version_id/competency_code/position/title/question_text`, `assessment_answer_options.questionnaire_version_id/competency_code/level/description`, `assessment_matrix_versions.id/version_code/published_at/is_current`, and `assessment_role_requirements.matrix_version_id/role_id/competency_code/importance/expected_level`. Depends on T003.
- [X] T005 [P] Add attempt, answer, result-set and role-result JPA entities/repositories under `backend/src/main/java/com/nextrole/data/` from the field constraints in `data-model.md`: `assessment_attempts.id`, `assessment_attempts.user_id`, `assessment_attempts.status`, `assessment_attempts.questionnaire_version_id`, `assessment_attempts.matrix_version_id`, `assessment_attempts.created_at`, `assessment_attempts.completed_at`, `assessment_attempts.discarded_at`, `assessment_attempts.source_attempt_id`, `assessment_answers.attempt_id`, `assessment_answers.questionnaire_version_id`, `assessment_answers.competency_code`, `assessment_answers.level`, `assessment_answers.updated_at`, `assessment_result_sets.attempt_id`, `assessment_result_sets.algorithm_version`, `assessment_result_sets.calculated_at`, `assessment_role_results.result_set_id`, `assessment_role_results.role_id`, `assessment_role_results.score_numerator`, `assessment_role_results.score_denominator`, `assessment_role_results.display_percentage`, and `assessment_role_results.rank_position`. Depends on T003.

**Checkpoint**: V5 applies on the disposable Testcontainers database; entities map the versioned catalog, matrix and attempt/result relationships; no V1–V4 migration or answer data is changed.

## Phase 3: User Story 1 — Completar la autoevaluación (Priority: P1)

**Goal**: Load the approved questionnaire, create/recover one user-owned draft and persist editable answers, preserving level 0 as an explicit response.

**Independent Test**: An authenticated user loads 31 questions with five options each, creates or recovers one draft, saves level 0 and other answers, then reloads and sees the same answers; another user cannot read or edit that draft. Missing answers remain absent.

### Tests for User Story 1

- [X] T006 [P] [US1] Add unit tests for draft creation/recovery, single-active-draft behavior, level 0 versus absent answer, editability and version pinning in `backend/src/test/java/com/nextrole/assessment/AssessmentAttemptServiceTest.java`.
- [X] T007 [P] [US1] Add API contract tests for current catalog, start/current attempt, answer autosave, authentication and ownership errors in `backend/src/test/java/com/nextrole/assessment/AssessmentDraftApiContractTest.java`, following `contracts/assessment-api.md`.
- [X] T008 [P] [US1] Add UI tests for catalog rendering, all five level choices, level 0 selection and persisted draft recovery in `frontend/src/features/assessment/SkillAssessmentPage.test.tsx`.

### Implementation for User Story 1

- [X] T009 [US1] Implement version-pinned current catalog and question/option mapping in `backend/src/main/java/com/nextrole/assessment/AssessmentCatalogService.java`; use the immutable rows defined by `data-model.md` (`assessment_questions.questionnaire_version_id/competency_code/position/title/question_text` and `assessment_answer_options.level/description`). Depends on T004.
- [X] T010 [US1] Implement authenticated draft start/recovery in `backend/src/main/java/com/nextrole/assessment/AssessmentAttemptService.java` and `backend/src/main/java/com/nextrole/data/AssessmentAttemptRepository.java`; pin current questionnaire/matrix and handle the unique-active-draft race using `data-model.md` (`assessment_attempts.user_id/status/questionnaire_version_id/matrix_version_id/created_at`). Depends on T004 and T005.
- [X] T011 [US1] Implement owned-draft answer autosave/read in `backend/src/main/java/com/nextrole/assessment/AssessmentAnswerService.java`; validate question membership and state while preserving level 0 and absent-row semantics from `data-model.md` (`assessment_answers.attempt_id/competency_code/level/updated_at`). Depends on T005 and T010.
- [X] T012 [US1] Expose `GET /api/v2/assessment-catalog/current`, `POST /api/v2/assessment-attempts`, `GET /api/v2/assessment-attempts/current`, `GET /api/v2/assessment-attempts/{attemptId}` and `PUT .../answers/{competencyCode}` with DTO validation in `backend/src/main/java/com/nextrole/assessment/AssessmentController.java` and `backend/src/main/java/com/nextrole/assessment/AssessmentDtos.java`. Derive the user from `CurrentUser`; follow `contracts/assessment-api.md`. Depends on T009–T011.
- [X] T013 [P] [US1] Add typed V2 catalog, attempt and answer contracts plus authenticated request functions in `frontend/src/types/domain.ts` and `frontend/src/services/api.ts`, matching `contracts/assessment-api.md` and preserving absence separately from level 0.
- [X] T014 [P] [US1] Adapt `frontend/src/components/ui/LevelPicker.tsx` to render all five version-provided level descriptions (0–4) and preserve explicit selection of level 0.
- [X] T015 [US1] Connect `frontend/src/features/assessment/SkillAssessmentPage.tsx` and `frontend/src/features/assessment/SkillsIndexPage.tsx` to catalog/current-draft APIs; autosave answers, recover progress on return, show answered count and block navigation to results until complete. Depends on T012–T014.

**Checkpoint**: Draft and answer behavior passes unit, API and UI tests. No partial matching is exposed.

## Phase 4: User Story 2 — Conocer preparación para seis roles (Priority: P1)

**Goal**: Finalize only complete attempts, calculate all six role scores using the approved weighted formula and persist exact score order and one-time exact-tie order.

**Independent Test**: With 30 answers completion is rejected and no result rows exist. With 31 answers the attempt produces six results; scores match the formula at full precision, sort descending, exact ties keep their persisted randomized order, and displayed percentages use one decimal HALF_UP without affecting rank.

### Tests for User Story 2

- [X] T016 [P] [US2] Add exact-matching unit tests for expected levels 1–4, level 0, capped contribution, essential/complementary weights, irrelevant exclusion, all-six-role denominator, exact ordering/ties and HALF_UP display in `backend/src/test/java/com/nextrole/matching/MatchingEngineV2Test.java`.
- [X] T017 [P] [US2] Add completion/results API contract tests for incomplete rejection, six-result response, exact score fields and persisted rank order in `backend/src/test/java/com/nextrole/assessment/AssessmentCompletionApiContractTest.java`, following `contracts/assessment-api.md`.
- [X] T018 [P] [US2] Add Testcontainers integration tests for atomic 31-answer completion, no partial results, repeat-completion idempotence and persisted tie order in `backend/src/test/java/com/nextrole/assessment/AssessmentCompletionIntegrationTest.java`. Include a regression case with an existing completed assessment: starting and editing a subsequent draft must leave the user's latest completed attempt and its visible results unchanged.

### Implementation for User Story 2

- [X] T019 [US2] Implement the exact-rational V2 scoring engine in `backend/src/main/java/com/nextrole/matching/MatchingEngineV2.java`; apply `data-model.md` matrix constraints (`assessment_role_requirements.importance/expected_level`) and compute only relevant requirements with the approved weights/formula. Do not use interests, preferences, projects or answer-count proportions.
- [X] T020 [US2] Implement transactional completion in `backend/src/main/java/com/nextrole/assessment/AssessmentCompletionService.java`; lock an owned draft, require 31 saved answers, persist the result set, six exact role scores and rank positions, then freeze the attempt. Resolve persisted field rules from `data-model.md`: `assessment_result_sets.attempt_id/algorithm_version/calculated_at`, `assessment_role_results.result_set_id/role_id/score_numerator/score_denominator/display_percentage/rank_position`, and `assessment_attempts.status/completed_at`.
- [X] T021 [US2] Add `POST /api/v2/assessment-attempts/{attemptId}/complete` and the structured `INCOMPLETE_ASSESSMENT` response in `backend/src/main/java/com/nextrole/assessment/AssessmentController.java` and `backend/src/main/java/com/nextrole/common/GlobalErrorHandler.java`; return persisted results on retries and do not rerandomize ranks. Depends on T019 and T020.
- [X] T022 [US2] Add typed result retrieval/mapping in `frontend/src/services/api.ts` and `frontend/src/types/domain.ts`; consume server `displayPercentage` and `rankPosition` without client-side score sorting. Depends on T021.
- [X] T023 [US2] Keep `frontend/src/features/assessment/SkillSummaryPage.tsx` as a competency diagnosis showing the user's declared V2 levels (not role scores or newly derived metrics); connect its data to the V2 assessment. Display six V2 matching results, approved role labels and persisted ranking in `frontend/src/features/roles/RolesSuggestedPage.tsx`; update `frontend/src/features/roles/RoleDetailPage.tsx` to display its persisted V2 score alongside existing role details without using role selection in scoring. Add/update `SkillSummaryPage.test.tsx` and `RolesSuggestedPage.test.tsx` to verify the diagnosis/matching separation and that results state the levels are self-assessed and are not a certification. Avoid an unnecessary visual redesign. Depends on T022.

**Checkpoint**: Completion is atomic and produces six immutable scores. Interests and incomplete-response coverage do not enter scoring; displayed rounding cannot change sorting or ties.

## Phase 5: User Story 3 — Reevaluarse y conservar historial (Priority: P2)

**Goal**: Start a new version-pinned reevaluation from the latest completed answers, explicitly discard drafts, and browse immutable completed attempts newest first with detail.

**Independent Test**: A user can inspect history containing only completed attempts in descending completion time, open a versioned detail, start a new draft prefilled from the latest complete without changing it, and explicitly discard a draft before creating another.

### Tests for User Story 3

- [ ] T024 [P] [US3] Add API contract tests for reevaluation, explicit discard, completed-only history and historical detail in `backend/src/test/java/com/nextrole/assessment/AssessmentHistoryApiContractTest.java`, following `contracts/assessment-api.md`.
- [ ] T025 [P] [US3] Add Testcontainers tests for answer prefill, source-attempt immutability, one active draft, version pinning, discarded-state retention and newest-first completed history in `backend/src/test/java/com/nextrole/assessment/AssessmentHistoryIntegrationTest.java`.
- [ ] T026 [P] [US3] Add UI tests for completed-only history, historical detail, recovered draft and explicit discard flow in `frontend/src/features/assessment/AssessmentHistoryPage.test.tsx` and `frontend/src/features/assessment/SkillAssessmentPage.test.tsx`.

### Implementation for User Story 3

- [ ] T027 [US3] Implement reevaluation creation in `backend/src/main/java/com/nextrole/assessment/AssessmentAttemptService.java`; return an existing draft if present, otherwise pin current versions and copy the latest completed answers by stable competency code when available. Keep the source immutable using `data-model.md` (`assessment_attempts.source_attempt_id/questionnaire_version_id/matrix_version_id` and `assessment_answers.attempt_id/competency_code/level`). Depends on T010, T011 and T020.
- [ ] T028 [US3] Implement explicit draft discard as a `DISCARDED` state transition in `backend/src/main/java/com/nextrole/assessment/AssessmentAttemptService.java` and expose `POST .../{attemptId}/discard` through `backend/src/main/java/com/nextrole/assessment/AssessmentController.java`; do not delete rows. Resolve state/date rules from `data-model.md` (`assessment_attempts.status/discarded_at/user_id`). Depends on T010.
- [ ] T029 [US3] Implement owned completed-history/detail queries in `backend/src/main/java/com/nextrole/assessment/AssessmentHistoryService.java` and `backend/src/main/java/com/nextrole/assessment/AssessmentController.java`; return completion date, six stored results and pinned questionnaire/matrix content, exclude drafts, sort by `completed_at DESC`. Use `data-model.md` (`assessment_attempts.status/completed_at/questionnaire_version_id/matrix_version_id` and `assessment_result_sets.attempt_id/calculated_at`). Depends on T020 and T028 to avoid overlapping controller edits.
- [ ] T030 [US3] Add completed-history and historical-attempt detail views/routes in `frontend/src/features/assessment/AssessmentHistoryPage.tsx`, `frontend/src/features/assessment/AssessmentAttemptDetailPage.tsx` and `frontend/src/app/router.tsx`; display stored scores and versions without recalculation. Depends on T029.
- [ ] T031 [US3] Add explicit discard and restart controls to `frontend/src/features/assessment/SkillAssessmentPage.tsx` and connect them to the V2 API; after discard, recover/create one new draft and keep discarded attempts out of history. Depends on T015 and T028.

**Checkpoint**: Re-evaluation never mutates its source, completed history is immutable and draft discard is explicit/non-destructive.

## Phase 6: Polish and Cross-Cutting Validation

**Purpose**: Verify the integrated V2 flow and report evidence without adding out-of-scope modules.

- [ ] T032 Add an authenticated end-to-end API workflow test from catalog through draft, completion, results, reevaluation and history in `backend/src/test/java/com/nextrole/integration/AssessmentWorkflowIntegrationTest.java` using Testcontainers PostgreSQL. Depends on T023, T030 and T031.
- [ ] T033 Run the applicable backend suite `mvn -f backend/pom.xml test`, frontend tests `npm --prefix frontend test`, build `npm --prefix frontend run build`, lint `npm --prefix frontend run lint`, and the manual browser scenarios in `specs/001-autoevaluacion-matching/quickstart.md`; correct failures within this feature and record checks not runnable (including Docker/Testcontainers availability). Depends on T032.
- [ ] T034 Record actual changed files, test/build/lint outcomes, blockers and next authorized step in `progress/current.md`; do not mark unrun checks as passed or advance to Implement beyond the implementation authorization. Depends on T033.

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: T001 creates the frontend test runner required by UI tests.
- **Foundational (Phase 2)**: T002 precedes T003; T003 precedes T004–T005. Complete the foundation before story implementation.
- **Role IDs**: The IDs `qa-jr` and `cloud-jr` are approved and may be seeded additively by T003; no role-ID confirmation gate remains.
- **US1 (Phase 3)**: Depends on T001–T005. It establishes the catalog and draft/answer flow.
- **US2 (Phase 4)**: Depends on US1's versioned answers and completion endpoint path; results are created only as part of complete evaluation.
- **US3 (Phase 5)**: Depends on US1 draft/answer behavior and US2 immutable completed results.
- **Polish (Phase 6)**: Depends on all three stories.

### User Story Dependencies

- **US1 (P1)**: Starts after foundation; delivers question/answer and recoverable draft behavior.
- **US2 (P1)**: Starts after US1; adds completion, matching and visible role results. US1 alone is an assessment-input increment, not the complete matching release.
- **US3 (P2)**: Starts after US1 and US2; needs completed source attempts for reevaluation and history.

### Parallel Opportunities

- T001 and T002 can proceed in parallel; T002 is backend-only and T001 is frontend test setup.
- After T003, T004 and T005 touch separate entity/repository files and can proceed in parallel.
- In US1, T006–T008 are separate test files and can be authored in parallel; T013 and T014 can proceed independently once the contract is fixed.
- In US2, T016–T018 are separate unit, API-contract and integration test files and can be authored in parallel.
- In US3, T024–T026 are separate contract, integration and UI test files and can be authored in parallel.
- Do not parallelize tasks that edit the same migration, controller, service, or page files; honor the explicit task dependencies above.

### Parallel Example: User Story 1

```text
After T001–T005:
Task: T006 draft service unit tests in backend/src/test/java/com/nextrole/assessment/AssessmentAttemptServiceTest.java
Task: T007 draft API contract tests in backend/src/test/java/com/nextrole/assessment/AssessmentDraftApiContractTest.java
Task: T008 draft UI tests in frontend/src/features/assessment/SkillAssessmentPage.test.tsx
```

### Parallel Example: User Story 2

```text
After US1 is complete:
Task: T016 matching unit tests in backend/src/test/java/com/nextrole/matching/MatchingEngineV2Test.java
Task: T017 completion contract tests in backend/src/test/java/com/nextrole/assessment/AssessmentCompletionApiContractTest.java
Task: T018 completion integration tests in backend/src/test/java/com/nextrole/assessment/AssessmentCompletionIntegrationTest.java
```

### Parallel Example: User Story 3

```text
After US1 and US2 are complete:
Task: T024 history API contract tests in backend/src/test/java/com/nextrole/assessment/AssessmentHistoryApiContractTest.java
Task: T025 history integration tests in backend/src/test/java/com/nextrole/assessment/AssessmentHistoryIntegrationTest.java
Task: T026 history/draft UI tests in frontend/src/features/assessment/AssessmentHistoryPage.test.tsx
```

## Implementation Strategy

### Incremental MVP

1. Complete Setup and Foundational phases; validate V5 on disposable Testcontainers PostgreSQL.
2. Deliver US1 as a testable questionnaire/draft increment.
3. Add US2 before calling the assessment-and-matching feature releasable; it supplies the approved six-role matching value.
4. Add US3 history and reevaluation; finish with the end-to-end and quickstart checks.

## Notes

- `[P]` means no shared-file edits or dependency on unfinished work; `[USn]` traces tasks to `spec.md` user stories.
- Every implementing task that enforces data constraints cites `data-model.md` and the canonical table/field names.
- No task includes Skill Gap, Learning, V1 answer conversion, destructive data operations, commits, or changes to existing migrations V1–V4.
- Spec Kit Tasks ends here; this file does not authorize or execute Implement.
