---
description: "Dependency-ordered implementation tasks for V2 assessment and role matching"
---

# Tasks: Autoevaluación técnica y matching de roles IT

**Input**: Design documents from `/specs/001-autoevaluacion-matching/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/assessment-api.md`, `quickstart.md`.

**Tests**: Included as explicitly requested. Write contract/unit/integration tests before their implementing tasks and verify behavior through existing Maven/JUnit/Testcontainers plus the frontend test setup created in Phase 1.

**Organization**: Tasks follow the seven user stories in `spec.md`, after shared setup and persistence foundations. The US6 shared discard behavior precedes US5 Home integration because the Home draft card reuses it.

## Format: `[ID] [P?] [Story] Description`

- `[P]` marks work that can run in parallel without editing the same files or depending on unfinished tasks.
- `[US1]`–`[US7]` map to the specification's user stories. Setup, foundational, and polish tasks have no story label.
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

**Goal**: Start a new empty version-pinned reevaluation while retaining source provenance, explicitly discard drafts, and browse immutable completed attempts newest first with detail.

**Independent Test**: A user can inspect completed-only newest-first history, open a versioned detail, start an unanswered reevaluation linked to but independent from the latest complete, recover that draft with its own answers, and explicitly discard it without changing earlier data.

### Tests for User Story 3

- [X] T024 [P] [US3] Add API contract tests for reevaluation, explicit discard, completed-only history and historical detail in `backend/src/test/java/com/nextrole/assessment/AssessmentHistoryApiContractTest.java`, following `contracts/assessment-api.md`.
- [X] T025 [P] [US3] Close the remaining Testcontainers assertion gaps in `backend/src/test/java/com/nextrole/assessment/AssessmentHistoryIntegrationTest.java`: directly assert that only one DRAFT row exists for the user and that an already-created draft retains its questionnaire/matrix versions when newer current versions exist. Existing integration coverage already checks empty answers, source lineage/immutability, draft-answer recovery, discard and newest-first history; do not duplicate those assertions. No implementation or schema change is implied. Verified by the full backend suite on 2026-11-26 (38 tests, 0 failures/errors).
- [X] T026 [P] [US3] Add UI tests for completed-only history, historical detail, recovered draft and explicit discard flow in `frontend/src/features/assessment/AssessmentHistoryPage.test.tsx` and `frontend/src/features/assessment/AssessmentAccessPanel.test.tsx`; verify the question screen does not expose discard in `frontend/src/features/assessment/SkillAssessmentPage.test.tsx`.

### Implementation for User Story 3

- [X] T027 [US3] Implement reevaluation creation in `backend/src/main/java/com/nextrole/assessment/AssessmentAttemptService.java`; return an existing draft with its own saved answers if present, otherwise pin current versions and retain only the latest completed source reference. Do not copy `assessment_answers`. Keep the source immutable using `data-model.md` (`assessment_attempts.source_attempt_id/questionnaire_version_id/matrix_version_id` and `assessment_answers.attempt_id/competency_code/level`). Depends on T010, T011 and T020.
- [X] T028 [US3] Implement explicit draft discard as a `DISCARDED` state transition in `backend/src/main/java/com/nextrole/assessment/AssessmentAttemptService.java` and expose `POST .../{attemptId}/discard` through `backend/src/main/java/com/nextrole/assessment/AssessmentController.java`; do not delete rows. Resolve state/date rules from `data-model.md` (`assessment_attempts.status/discarded_at/user_id`). Depends on T010.
- [X] T029 [US3] Implement owned completed-history/detail queries in `backend/src/main/java/com/nextrole/assessment/AssessmentAttemptService.java` and `backend/src/main/java/com/nextrole/assessment/AssessmentController.java`; return completion date, six stored results and pinned questionnaire/matrix content, exclude drafts, sort by `completed_at DESC`. Use `data-model.md` (`assessment_attempts.status/completed_at/questionnaire_version_id/matrix_version_id` and `assessment_result_sets.attempt_id/calculated_at`). Depends on T020 and T028 to avoid overlapping controller edits.
- [X] T030 [US3] Add completed-history and historical-attempt detail views/routes in `frontend/src/features/assessment/AssessmentHistoryPage.tsx`, `frontend/src/features/assessment/AssessmentAttemptDetailPage.tsx` and `frontend/src/app/router.tsx`; display stored scores and versions without recalculation. Depends on T029.
- [X] T031 [US3] Add explicit discard and restart controls to `frontend/src/features/assessment/SkillAssessmentPage.tsx` and connect them to the V2 API; after discard, recover/create one new draft and keep discarded attempts out of history. Depends on T015 and T028.

**Checkpoint**: Re-evaluation never mutates its source, completed history is immutable and draft discard is explicit/non-destructive.

## Phase 6: Core workflow checkpoint (US1–US3)

**Purpose**: Record the initial US1–US3 core-flow validation. Later UI/lifecycle extensions are validated separately in the final verification phase.

- [X] T033 Run the applicable backend suite `mvn -f backend/pom.xml test`, frontend tests `npm --prefix frontend test`, build `npm --prefix frontend run build`, lint `npm --prefix frontend run lint`, and record manual validation of the application as reported by the user, including mobile access. Individual quickstart scenario outcomes are not asserted unless explicitly recorded. Correct failures within this feature and record checks not runnable (including Docker/Testcontainers availability). Depends on T032.
- [X] T034 Record actual changed files, test/build/lint outcomes, blockers and next authorized step in `progress/current.md`; do not mark unrun checks as passed or advance to Implement beyond the implementation authorization. Depends on T033.

## Phase 7: User Story 4 — Visualizar el progreso en un recorrido animado

**Goal**: Show the current assessment progress as a continuous, accessible path animation in the existing assessment card, without changing assessment behavior or the rest of Home.

**Independent Test**: Only an active draft renders the path. Its accessible progress and mascot endpoint match the saved-answer ratio (including saved level 0), with or without older history. No draft—whether there are no attempts or completed history—renders no journey. A fully answered but uncompleted draft does not celebrate. Reduced motion shows the target state directly.

### Tests for User Story 4

- [X] T035 [P] [US4] Add component tests in `frontend/src/features/assessment/AssessmentJourney.test.tsx` for target progress, continuous path endpoint/percentage consistency, confirmed completion versus a fully answered draft, finite entry motion and reduced-motion rendering. Keep the component inputs independent of questionnaire structure.
- [X] T036 [P] [US4] Update `frontend/src/features/assessment/AssessmentAccessPanel.test.tsx` so only a current draft renders the journey and its saved-answer ratio (including explicit level 0); no draft with either empty or completed history renders no journey. Verify existing queries suffice and no extra request is issued.

### Implementation for User Story 4

- [X] T037 [US4] Update `frontend/src/features/assessment/AssessmentJourney.tsx` and `AssessmentJourney.css` so no numeric percentage is visible while its progressbar role, `aria-valuenow` and `aria-valuetext` remain accurate; preserve synchronized path/mascot progress, supplied animation assets and reduced motion. Depends on T035.
- [X] T038 [US4] In `frontend/src/features/assessment/AssessmentAccessPanel.tsx`, mount `AssessmentJourney` only for the current `DRAFT`; never derive or show a journey from completed history or an empty state. Keep existing query keys and pass the active draft's saved ratio. Habilidades's static no-draft content is handled by US8. Depends on T036 and T037.

### Validation for User Story 4

- [X] T039 [US4] Run frontend tests, build and lint after integration; record failures and warnings. Depends on T038.
- [X] T041 Record changed files, validation outcomes, manual checks actually completed and remaining review in `progress/current.md`. Keep this checkpoint separate from task status. Depends on T039.

## Phase 8: User Story 6 — Descartar el borrador sin iniciar otra evaluación

**Goal**: Make discard behavior identical on Home and Habilidades: confirm, discard only the active draft, preserve completed history, create no replacement attempt, and keep the user on the originating screen.

**Independent Test**: On Habilidades, cancel leaves the draft unchanged; confirmed backend success clears the current-draft state without calling either create endpoint; backend failure leaves the draft and prior history visible with an error. Home uses the same mutation and remains on `/` after success.

### Tests for User Story 6

- [X] T042 [US6] Update `frontend/src/features/assessment/AssessmentAccessPanel.test.tsx` and `frontend/src/features/home/HomePage.test.tsx` for the shared dialog's exact title/copy, Cancelar/Descartar actions, cancel/success/failure, keyboard dismissal, disabled duplicate submission, preserved history, no automatic create call, and origin-screen retention.

### Implementation for User Story 6

- [X] T043 [US6] Add reusable `frontend/src/components/ui/ConfirmDiscardDialog.tsx` and integrate it in `HomePage.tsx` and `AssessmentAccessPanel.tsx` with `useDiscardAssessmentAttempt.ts`. Keep the existing mutation/cache semantics; render errors in the modal, disable duplicate submits, and set the current-attempt query to null only after API success. Depends on T042.

## Phase 9: User Story 5 — Consultar Inicio según el estado de la autoevaluación

**Goal**: Replace the obsolete orientation UI with responsive Home content derived from the active draft and persisted completed history, while preserving current navigation and the Habilidades entry point for starting an assessment.

**Independent Test**: Verify the four combinations of draft/history, a full but unfinalized draft, loading/error behavior, last-result details and links, and a successful completion refresh. Home shows the journey only for an active draft, and no evaluation is created automatically.

### Tests for User Story 5

- [X] T044 [P] [US5] Update `frontend/src/features/home/HomePage.test.tsx` for draft/history states, an unfinalized full draft, loading/error states, one greeting, persisted latest date/rank/percentage, approved deterministic progress messages, visible-copy absence of a numeric journey percentage, result/history links, discard states and post-completion refresh. Reuse existing query keys.

### Implementation for User Story 5

- [X] T045 [US5] Fix the draft card visual styles in `frontend/src/features/home/HomePage.tsx`; make the blue background and white Continue button explicit against shared Card/Button styles. Show legible title, journey, message/description and secondary discard action. Choose deterministic copy from raw progress and history without showing a numeric percentage. Integrate the shared dialog from T043 and preserve persisted latest results/routes. Depends on T043 and T044.

## Phase 10: User Story 7 — Consultar Skill Gap sin indicadores de orientación obsoletos

**Goal**: Remove only the old orientation stage label and six-segment strip from Skill Gap while preserving the diagnostic and every independent progress flow.

**Independent Test**: Skill Gap still renders its title, competency gaps, per-competency progress bars, role context and training link, but does not render «PASO 5 DE 6» or the six-segment strip. Registration, profile onboarding and assessment progress remain unchanged.

### Tests for User Story 7

- [X] T046 [P] [US7] Add `frontend/src/features/skill-gap/SkillGapPage.test.tsx` to assert the obsolete label and six-segment strip are absent while the diagnostic title, gap content, competency progress bars and training navigation remain. Cover only the existing page behavior; do not introduce a redesign.

### Implementation for User Story 7

- [X] T047 [US7] Remove only the orientation label and six-segment markup from `frontend/src/features/skill-gap/SkillGapPage.tsx`. Preserve `ProgressBar`, status badges, gap calculation, data requests, `AppShell`, routes and all diagnostic content. Depends on T046.

## Phase 11: Home Validation and Checkpoint

**Purpose**: Validate the approved Home/discard behavior, record limitations precisely, and keep the animation's separate manual-review status accurate.

- [X] T048 Update and review `specs/001-autoevaluacion-matching/quickstart.md` Home/discard/Skill Gap scenarios against the implementation; include reevaluation without prefill, Home/Habilidades no-journey states, modal outcomes, Roles latest/historical results and Formation empty states. Depends on T045 and T047.
- [X] T050 Run `npm --prefix frontend test`, `npm --prefix frontend run build`, `npm --prefix frontend run lint`, and `git diff --check`; resolve in-scope failures and record warnings. No backend suite is needed unless implementation unexpectedly changes backend contracts or code. Depends on T045-T048; T049 is a separate manual review in the final phase.
- [X] T051 Update `progress/current.md` with actual changed files and validation outcomes; report automated checks separately from visual review. Leave T040 unchecked until its full journey visual criteria are personally verified. Depends on T050.

## Phase 12: User Story 3 — Crear reevaluaciones independientes

**Goal**: Preserve source provenance without answer prefill, recover only an existing draft's answers, and leave completed attempts immutable.

**Independent Test**: Starting reevaluation after completion returns an empty answer map and zero answered count; the source retains all saved answers/results. Saving one answer and fetching the same draft returns only that saved answer; another create request recovers it, and the one-draft constraint remains.

- [X] T052 [US3] Update unit and API contract assertions in `backend/src/test/java/com/nextrole/assessment/AssessmentAttemptServiceTest.java` and `AssessmentHistoryApiContractTest.java` for empty reevaluations, source immutability, saved draft recovery, one active draft and pinned versions.
- [X] T053 [US3] Remove `copyAnswers` use from `AssessmentAttemptService.reevaluate` and its now-unused implementation from `AssessmentAnswerService`; preserve `source_attempt_id` provenance and existing schema/data. Depends on T052.
- [X] T032 Update the authenticated end-to-end Testcontainers workflow in `backend/src/test/java/com/nextrole/integration/AssessmentWorkflowIntegrationTest.java` to prove a reevaluation is empty, recovers only its own saved answers, and does not replace completed source results. Depends on T023, T030, T031 and T053.

## Phase 13: User Story 8 — Ajustar el recorrido y los estados de Habilidades

**Goal**: Show no visible numeric journey percentage and render the journey only while a draft is active; show a static mascot access card otherwise.

**Independent Test**: Habilidades with no history shows the first-assessment copy/action; with history and no draft shows reevaluation and history; with a draft shows that draft's path only. After discard, the path disappears even when completed history remains. Screen readers can still read the saved-answer percentage.

- [X] T054 [P] [US8] Update `frontend/src/features/assessment/AssessmentJourney.test.tsx` to assert the visible percentage node is absent while the accessible progress value remains correct, including reduced motion and full draft behavior.
- [X] T055 [US8] Update `frontend/src/features/assessment/AssessmentAccessPanel.test.tsx` for first-user, recurrent-user, active-draft, and discarded-draft-with-history states. Confirm no journey/flag/number when no draft and no extra data requests.
- [X] T056 [US8] Update `frontend/src/features/assessment/AssessmentAccessPanel.tsx` to show a static branded mascot with the approved Habilidades title/copy/start/history actions when no draft exists. Preserve explicit start/re-evaluation and saved-draft recovery; never show old completed progress as a journey. Journey mount policy is handled by T038 and percentage visibility by T037. Depends on T054 and T055.

## Phase 14: User Story 9 — Orientar Roles y Formación sin rol permanente

**Goal**: Present real latest persisted rankings or useful empty states from existing assessment state, while preserving historical `attemptId` navigation and per-role Learning V1.

**Independent Test**: Direct Roles with history shows `history[0]` persisted ranks, with or without another draft; historical `attemptId` still loads its requested attempt. With no history, Roles and Formation show the approved empty actions. Formation with completed history invites role exploration; per-role resources remain unchanged.

- [X] T057 [P] [US9] Add/update `frontend/src/features/roles/RolesSuggestedPage.test.tsx` for direct latest results from history, active-draft coexistence, empty state with/without draft, loading/errors, and unchanged historical `attemptId` detail behavior.
- [X] T058 [P] [US9] Add `frontend/src/features/learning/LearningIndexPage.test.tsx` for empty without history, continuation CTA for an uncompleted draft, role-exploration invitation with history, loading/error handling and no permanent-role redirect.
- [X] T059 [US9] Implement a reusable styled `frontend/src/components/ui/EmptyStateCard.tsx` and update `RolesSuggestedPage.tsx` to use the newest persisted results for direct `/roles`, preserve `attemptId` fetches, and show the approved no-results states without fake ranks. Depends on T057.
- [X] T060 [US9] Update only `frontend/src/features/learning/LearningIndexPage.tsx` to use existing assessment current/history queries and the reusable empty state. Remove selected-role dependency only from this index route; leave `LearningPage.tsx`, its per-role APIs/resources and role-specific navigation unchanged. Depends on T058 and T059.

## Phase 15: Preserve evaluation context in Roles and Formación

**Goal**: Keep completed results and explicit historical navigation isolated from an active draft, and restore current exploration only after the draft is discarded.

**Independent Test**: With a completed attempt and a new draft, direct Roles keeps the stored latest ranking and context note but hides exploration links; Formación shows the approved assessment-pending state with no prior role CTA. After the draft query becomes null, Roles exploration and Formación's Roles invitation return. An explicit historical `attemptId` continues to render its requested attempt.

- [X] T064 [P] [US9] Update `frontend/src/features/roles/RolesSuggestedPage.test.tsx` for the latest persisted ranking with a draft, context note, hidden exploration links, and exploration reenabled after the existing current-attempt cache is cleared.
- [X] T065 [P] [US9] Update `frontend/src/features/learning/LearningIndexPage.test.tsx` for active-draft precedence despite completed history, approved copy/continue action with no Roles link, and return to role exploration after discard; retain no-history, loading, error and history-without-draft coverage.
- [X] T066 [US9] Add an optional exploration-action visibility prop to `AssessmentResultsRanking` and hide its links only for the unscoped latest ranking while a draft is active. Preserve historical `attemptId` detail and default exploration behavior.
- [X] T067 [US9] Update `RolesSuggestedPage.tsx` and `LearningIndexPage.tsx` to apply draft precedence using the existing current/history query keys; keep persisted ranking values and historical navigation unchanged. No backend changes.
- [X] T068 Review the US9 acceptance criteria and `specs/001-autoevaluacion-matching/quickstart.md` for draft/history coexistence, discard state transition, and explicit historical `attemptId`; record tests and pending manual review in `progress/current.md`.

## Phase 16: Verification and review handoff

**Purpose**: Run final technical verification after all implementation stories, record evidence and leave unperformed manual reviews open.

**Manual review**: the following tasks remain open until the specified browser checks are actually performed.

- [ ] T040 [US4] Visually review the journey at mobile and desktop sizes for clipping, path/mascot synchronization, sprite cadence, scale, contrast, accessibility and reduced-motion behavior. Record unavailable checks without marking them passed. Depends on T038.
- [ ] T049 Review current Home, Habilidades, Roles and Formation in a browser at mobile and desktop sizes, including modal, spacing, touch targets and bottom navigation. Record which states are actually checked. Keep T040's journey-specific visual review separate. Depends on T068.
**Purpose**: Revalidate backend/frontend behavior and leave the app available without claiming manual checks the user has not performed.

- [X] T061 Run `mvn -f backend/pom.xml test`, `npm --prefix frontend test`, `npm --prefix frontend run build`, `npm --prefix frontend run lint`, and `git diff --check`; record exact results and any environment blockers. Do not change matching or competency data. Depends on T032, T053, T056, T059, T060 and T068. T025 was subsequently closed by its dedicated Testcontainers assertions and the 2026-11-26 full backend suite.
- [X] T062 Review `specs/001-autoevaluacion-matching/quickstart.md` against implementation, record user-reported checks separately from unperformed scenarios, and keep T040/T049 unchecked until their respective visual reviews occur. Depends on T061.
- [X] T063 Update `progress/current.md` with actual files, test outcomes, service availability and pending manual checks. Preserve local files; no commit or push is implied. Depends on T062.

## Dependencies & Execution Order

### Phase Dependencies

- Phases 1–5 cover the core assessment/matching flow. Phase 6 is its historical core-flow checkpoint, not final validation of later extensions. Phases 7–15 contain the subsequently approved journey, Home, discard, and context-preservation work. Phase 16 is the final technical validation and review handoff after those implementation phases.
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
- **US3 (P2)**: Starts after US1 and US2; reevaluations keep a link to the completed source but create an empty answer map.
- **US4 (P2)**: Uses the existing draft/history contracts from US1/US3. It changes only presentation in the assessment access card; it adds no API, data model, backend or questionnaire dependency.
- **US6 (P1)**: Reuses the existing discard endpoint from US3, runs before US5, and creates no replacement attempt.
- **US5 (P1)**: Uses existing current-attempt/history data from US1/US3 and the shared discard action from US6; it adds no API, backend, database or matching dependency.
- **US7 (P2)**: Removes only two obsolete Skill Gap orientation indicators and has no dependency on the Home layout, API, backend or database. Its tests ensure the existing diagnosis remains intact.
- **US8 (P1)**: Depends on US3 active-draft/history contracts and US6 shared discard; the journey displays only for the active draft.
- **US9 (P1)**: Reuses existing current-attempt, history, attempt-detail and role-resource contracts; direct Roles preserves `attemptId`, while `/formacion` entry states no longer depend on a persistent selected role.

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
5. Add US4's isolated assessment journey visual after the existing assessment and history behavior is stable.
6. Implement US6's consistent discard-only behavior, then US5's assessment-aware Home composition.
7. Implement US7's narrowly scoped Skill Gap indicator removal, preserving all diagnostic content and per-competency progress.
8. Complete the US3 empty-reevaluation correction while preserving source lineage and immutable history.
9. Implement the US6 shared modal, US8 draft-only Habilidades journey and US9 direct Roles/Formation states.
10. Run automated checks, keep T040/T049 as user visual reviews, and leave the current app ready for iteration.

## Notes

- `[P]` means no shared-file edits or dependency on unfinished work; `[USn]` traces tasks to `spec.md` user stories.
- Every implementing task that enforces data constraints cites `data-model.md` and the canonical table/field names.
- No task includes Skill Gap V2, Learning V2 or per-role Learning recommendation changes; the narrowly approved Skill Gap marker removal and `/formacion` empty state are included. No V1 answer conversion, destructive data operations, commits or changes to existing migrations V1–V4 are included.
- Spec Kit Tasks ends here; this file does not authorize or execute Implement.

## Phase 17: Publish questionnaire Q2 (approved 2026-11-26)

**Goal**: Publish the ten approved option-description changes in an immutable Q2 snapshot, retain Q1 and all pinned attempt history, and reuse M1 unchanged.

**Independent Test**: On disposable PostgreSQL, upgrade V5 to V6 with one completed Q1 attempt and one active Q1 draft already present. Verify both remain pinned and unchanged; a Q1 draft can still save and complete through the existing API, while a new draft pins Q2/M1. Verify exact catalog delta (10 changed, 145 identical), Q2 as sole current questionnaire, and identical M1 requirements.

- [X] T069 Record the approved Q2/Q1/M1 behavior, exact ten descriptions and self-assessment wording criterion in `spec.md`; add the compatible additive migration approach to `plan.md`; add this phase before implementation. Keep the Spec Kit change scoped to the existing feature.
- [X] T070 Update `docs/V2/autoevaluacion-matriz-vigente.md` as the normative Q2 catalog and update the questionnaire-version examples/semantics in `contracts/assessment-api.md`, `quickstart.md` and `data-model.md`. Keep Q1 source history immutable and identify M1 as unchanged.
- [X] T071 Extend/add Testcontainers migration coverage that migrates through V5, seeds a completed Q1 attempt and a Q1 draft with answers/results, then applies V6 and asserts preserved versions, Q1 descriptions, answers and results, exact 10/145 catalog diff, sole current Q2, and unchanged M1.
- [X] T072 Add API/service integration coverage proving a Q1 draft can be recovered, saved and completed using its pinned catalog after Q2 is current; verify a new draft uses Q2/M1. Assert identical numeric answers with unchanged M1 yield unchanged Matching and Skill Gap calculations. Do not change matching or gap production code.
- [X] T073 Add `V6__publish_questionnaire_q2.sql` to copy Q1 to Q2 and replace only the approved ten texts; do not edit V1–V5 or add schema changes. Switch current questionnaire atomically and leave M1 unchanged.
- [X] T074 Run targeted migration/history tests and `mvn -f backend/pom.xml test`; run `git diff --check`. Record actual results and update progress/history. Preserve unrelated local changes; no commit or push.

**Execution order**: T069/T070 establish approved requirements and normative documentation. T073 adds the immutable Q2 publication; T071/T072 validate migration and compatibility behavior; T074 records actual validation and handoff.

## Phase 18: First-visit Home welcome and direct assessment start (approved 2026-11-27)

**Goal**: Improve only the no-draft/no-history Home state with a branded welcome, four noninteractive explanatory cards and a direct accessible Play action. Reuse the Habilidades start flow and preserve all other Home states.

**Independent Test**: The first-visit state shows the greeting, original mascot, four informational steps and Play action; steps are not controls. Starting creates one initial attempt and opens the questionnaire after success, while a pending request cannot be repeated and an error remains visible with retry available. Draft/history states retain their existing content and actions.

- [X] T075 Update this feature's `spec.md` and `plan.md` with the approved first-visit Home state, direct start behavior and the superseded Habilidades-only entry decision. Keep progress snapshots and shared docs untouched.
- [X] T076 Update `frontend/src/features/home/HomePage.test.tsx` for all four step descriptions, the initial-only Play action, successful direct navigation, pending duplicate prevention, backend error/retry, and absence of the new onboarding content in draft/history states.
- [X] T077 Redesign only the first-visit Home state in `HomePage.tsx`/`HomePage.css`. Extract and reuse the existing start behavior from `AssessmentAccessPanel` so initial/reevaluation choice, query-cache update, error, loading, duplicate guard and questionnaire navigation stay consistent. Preserve draft/history UI and bottom navigation.
- [X] T078 Run focused Home/AssessmentAccessPanel tests (2 files, 28 passed), frontend suite (13 files, 77 passed), build (passed), lint (passed with the existing `SessionContext.tsx:45` Fast Refresh warning) and `git diff --check` (passed). After verifying the sole frontend container belongs to this checkout, restarted only that container; it is healthy and the Home module/CSS return HTTP 200 with the new markers. Visual review is tracked separately.
- [ ] T079 Review the Home first-visit, draft, history and discard states at narrow mobile and desktop viewport sizes. The user approved the current visual design on 2026-11-27; keep this task open for actual viewport review and unverified responsive/accessibility checks.

**Execution order**: T075 approves and records the revised requirements. T076 adds behavioral coverage, T077 implements the scoped UI/shared hook, T078 validates automated checks, and T079 tracks manual browser review separately.
