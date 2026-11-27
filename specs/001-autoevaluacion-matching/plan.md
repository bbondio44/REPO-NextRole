# Implementation Plan: Autoevaluación técnica y matching de roles IT

**Branch**: `develop-v2` (feature: `001-autoevaluacion-matching`) | **Date**: 2026-11-20 | **Spec**: [spec.md](spec.md)

**Input**: Functional specification in `specs/001-autoevaluacion-matching/spec.md`.

## Summary

Replace the V1 skill-level/mock matching flow with a versioned assessment workflow on the existing Spring Boot API and React application. Persist one active draft per authenticated user, immutable completed attempts and six deterministic, precisely ranked role results. Implement the approved formula in a backend application service, expose authenticated V2 contracts, and connect the existing assessment/results UI to them. Use additive Flyway migrations; do not convert or delete V1 answers.

The approved UI extension derives Home and Habilidades from existing active-draft/history queries, presents a shared accessible discard dialog, shows the latest persisted results on direct Roles navigation, and adds explicit no-history/role-exploration states to Roles and the Formation index. New reevaluations keep provenance but begin with no answers, which requires a focused backend service change and lifecycle-test updates. No schema, migration or new endpoint is needed; Learning's existing per-role recommendation page remains unchanged.

All architecture, persistence, API and identifier choices in this Plan are technical proposals for implementation review; they do not add or change functional decisions.

## Technical Context

**Language/Version**: Java 21; TypeScript 7 / React 19.

**Primary Dependencies**: Spring Boot 3.5.16, Spring MVC, Bean Validation, Spring Data JPA, Spring Security/JWT; React Router 7, TanStack Query 5, Vite 8.

**Storage**: PostgreSQL 16; existing Flyway history currently ends at V4.

**Testing**: JUnit 5 / Spring Boot Test / Testcontainers PostgreSQL; frontend uses Vitest and Testing Library for component/service tests, plus the configured production build and Oxlint commands.

**Target Platform**: Browser client and authenticated Spring Boot web API.

**Project Type**: Web application with modular monolith backend.

**Performance Goals**: No new latency or throughput target is established by the functional specification. Finalizing one attempt evaluates 31 competencies against six roles and persists six results in one transaction.

**Constraints**: Matching requires all 31 answers; level 0 is present data. Keep questionnaire and matrix versions independent and pinned to drafts. Use exact score comparisons before display rounding. Reuse current-attempt and history queries, API routes and shared query keys. A newly created reevaluation has no answers; return an existing active draft with its own saved answers. Keep `source_attempt_id` as lineage only. Preserve the one-active-draft invariant and completed history. No schema/migration changes, automatic V1 answer conversion or destructive data operation.

**Scale/Scope**: One shared 31-question catalog, six V2 roles, one active draft per user, completed-only history. Skill Gap V2, AI explanations, Learning V2, changes to per-role Learning recommendations, graphical comparison and role preference scoring remain outside this implementation. Existing Skill Gap receives only the already-approved marker removal; the existing Formation index receives the newly approved empty states/navigation only.

## Constitution Check

| Principle | Gate result |
|---|---|
| I. Modular architecture | PASS — extend the existing modular monolith; keep React, API, business logic and persistence responsibilities separate. |
| II. Approved requirements | PASS — the approved Home extension supersedes the earlier restriction to the journey-only Home change. |
| III. Specify and design first | PASS — this plan and its design artifacts precede Tasks and implementation. |
| IV. Controlled V1 evolution | PASS — additive Flyway migration; no V1 answer conversion or deletion. |
| V. Historical meaning | PASS — immutable attempts/results retain independent questionnaire and matrix versions. |
| VI. Quality and contracts | PASS — define API contracts and unit, integration, frontend and contract validation. |
| VII. Consistency and evolution | PASS — current code was inspected; compatible components are reused and V1 behavior to replace is identified. |

No Constitution violations require complexity justification.

## Component Impact

| Classification | Current components | Plan |
|---|---|---|
| Reuse | `AppShell`, `Card`, `Button`, `Badge`, React Router, TanStack Query; Spring Security/JWT, `CurrentUser`, Bean Validation, JPA/Flyway conventions, `ApiProblem` envelope | Keep layout, navigation/query tooling and authenticated user derivation. Reuse common error response conventions. The journey does not reuse the separate `ProgressBar` visual. |
| Modify (approved UI extension) | `frontend/src/features/home/HomePage.tsx`, `frontend/src/features/assessment/AssessmentAccessPanel.tsx`, `frontend/src/features/assessment/AssessmentJourney.tsx`, `frontend/src/features/roles/RolesSuggestedPage.tsx`, `frontend/src/features/learning/LearningIndexPage.tsx`, `frontend/src/features/skill-gap/SkillGapPage.tsx`, related tests | Show the journey only for an active draft in Home/Habilidades, remove its visible number but keep progress ARIA, add static mascot Habilidades states, persisted latest-results default for direct Roles, and responsive empty states for Roles/Formation. Preserve per-role Learning V1 and Skill Gap content other than its old orientation markers. |
| Add | Backend `assessment` module, V2 role catalog, V2 matching engine, versioned entities/repositories, V2 controller/DTOs, `AssessmentHistoryPage` and attempt detail UI, migration V5, API/unit/integration/frontend tests | Own the attempt lifecycle, snapshots, completion transaction, version context and completed-only history. Keep V2 role records out of the shared V1 role catalog to avoid changing existing V1 responses. |
| Modify (approved journey addition) | `frontend/src/features/assessment/AssessmentAccessPanel.tsx`, `AssessmentJourney.tsx` and UI tests | Show the journey only for the active draft, with its saved answer count/total; otherwise show the new/recurrent static mascot state. Keep percentage accessible but visually hidden. Do not add queries. |
| Add (approved journey addition) | `frontend/src/features/assessment/AssessmentJourney.tsx`, local journey styles, `AssessmentJourney.test.tsx` | Reusable visual-only component driven by target progress and completion state. One SVG path represents both completed and pending progress; the mascot position is computed along that same path. No questionnaire, competency, area, role or fixed-count dependency; no API, backend, data model or migration change. |
| Add (shared UI) | `frontend/src/components/ui/ConfirmDiscardDialog.tsx`, `EmptyStateCard.tsx` and tests | Reuse one accessible modal for both discard actions and one consistent icon/title/description/action empty-state presentation for Roles and Formation. |
| Add (assessment lifecycle adjustment) | `backend/src/test/java/com/nextrole/assessment/AssessmentAttemptServiceTest.java`, `AssessmentHistoryIntegrationTest.java`, `AssessmentHistoryApiContractTest.java`, `backend/src/test/java/com/nextrole/integration/AssessmentWorkflowIntegrationTest.java` | Verify new/reevaluation drafts start with an empty answer map while `source_attempt_id` remains provenance, existing draft recovery preserves its own answers, and completed source answers/results stay unchanged. Remove unused answer-copy logic; no schema changes. |

The existing `integration/frontend-api-adapter.ts` is a reference, not a drop-in: it is outside the active client path, targets `/api/v1`, and models legacy affinity results. `storage.ts` must stop being authoritative for V2 answers, but existing local data is not cleared.

## Design Decisions

- Keep the V1 API operational for unrelated modules. Add `/api/v2` assessment endpoints rather than silently changing `/api/v1/matching` behavior.
- Use new assessment-specific versioned catalog tables; do not treat `skills`, `role_skills` or `user_skills` as the V2 catalog or attempt to map all 31 competencies onto the 15 V1 skills.
- Seed an assessment-specific `assessment_roles` catalog with the six approved IDs and labels. Reuse the stable identifiers `backend-dev-jr`, `frontend-dev-jr`, `fullstack-dev-jr`, `data-analyst-jr`, `qa-jr`, and `cloud-jr`; keep V1 `roles` rows and endpoint behavior unchanged.
- The six role IDs are approved: reuse the four IDs above; QA is `qa-jr` and Cloud is `cloud-jr`. Add their V2 records without repurposing the narrower legacy QA Automation or DevOps/Cloud roles.
- Represent irrelevant matrix cells explicitly as `NOT_RELEVANT` with no expected level. Represent relevant expected levels as 1–4; seed the approved matrix exactly as provided (currently 1–3).
- Calculate exact rational scores in Java and persist numerator/denominator. Compare scores by cross multiplication. Assign randomized positions only within exact tie groups once at completion and persist rank positions. Format presentation with `BigDecimal` scale 1 and `HALF_UP`; never use the formatted value for ranking.
- Enforce one active draft per user with a PostgreSQL partial unique index and transactional recovery when concurrent starts race. Discard is a state transition, not a delete.
- Treat new assessment data as V2 data. Do not migrate V1 answers. Keep current V1 tables and rows untouched.
- For Home and Habilidades, derive state from the existing current-attempt and completed-history queries and query keys. Do not treat loading or failed queries as empty. Mount `AssessmentJourney` only for an active `DRAFT` and use persisted `answeredCount / totalQuestions`, including saved level 0. Do not render a visible numeric percentage; preserve `role=progressbar`, value and value text for assistive technology.
- In Home, show progress copy deterministically from the saved-answer ratio: distinct zero-progress copy for first assessment versus a reevaluation (presence of completed history), then the approved 1–39%, 40–74%, 75–99% and 100% messages. Keep the raw ratio for path/mask/mascot sync; display no range/percentage number.
- Without a draft, show the Home motivation variant based on whether completed history exists; render the latest-result card only when it does. Use `history[0]` (backend newest-first) and the stored rank-1 result, date and display percentage; do not recalculate or reorder results. Keep `/roles?attemptId={attemptId}` and `/habilidades/historial`.
- Keep a shared discard mutation for Home and `AssessmentAccessPanel`. Call the existing endpoint, then set current-attempt cache to `null` only after backend success. Reuse one responsive `ConfirmDiscardDialog`; disable duplicate submission, retain the open modal and draft on failure, and keep callers on the origin route. Do not refetch completed history or create a replacement attempt.
- Reevaluate on the backend by pinning current versions, preserving `source_attempt_id` to the latest completed source, and not calling answer-copy logic. Existing active draft recovery remains unchanged. `source_attempt_id` expresses provenance only; keep the column/FK and applied migrations, and do not modify old drafts or completed records.
- In Habilidades, show the journey only with a draft. Without a draft, use the existing mascot asset in a static empty/access card; vary title/copy and explicit start button by history, and retain history access. Do not show the prior completed attempt as a finished journey.
- In Roles without `attemptId`, use the existing completed-history response newest-first to present `history[0]`'s stored results; independently read current draft. When a draft exists, add the approved context note and pass a presentation flag that hides all role-exploration links while leaving persisted rank/percentages intact. After discard updates the existing current-attempt query cache to `null`, exploration becomes available again. With `attemptId`, continue retrieving that exact historical attempt with its existing `attemptId` links. Never invoke matching locally or replace a historical URL with latest.
- In `/formacion`, derive entry states from existing current-attempt/history queries. A draft takes precedence even when completed history exists: show the approved empty-state copy, offer continuation, and no role-exploration action or previous-attempt context. Without a draft and without completed history, use the same copy and link to Habilidades; with history and no draft, invite exploring Roles. The shared discard cache update transitions the page back to the history state. Preserve `LearningPage` at `/roles/:roleId/formacion`, its API and V1 recommendations.
- Explicit historical routes preserve the selected `attemptId` through results and related navigation; queries and displays must remain scoped to that attempt rather than using the current draft, another historical attempt, or a remembered role selection. No API or backend contract change is needed.
- Remove the old orientation calculations, cards and CTA from `HomePage`; preserve shared V1 helpers with active consumers, `learning_steps`, other tabs and bottom navigation. Add no placeholder for the future Home start button. In Skill Gap remove only the two confirmed old-orientation markers.
- In `SkillGapPage`, remove only the `PASO 5 DE 6` orientation label and its six-segment strip. Keep `ProgressBar` because it represents the real level of each competency, and preserve the title, gap calculation, data, training link and navigation. Keep registration, profile onboarding and assessment-specific progress indicators because they belong to separate flows.
- Use existing design tokens and UI primitives. Reuse the current responsive `AppShell` width and preserve its mobile safe-area spacing and bottom navigation; do not change other screens' shared layout.
- Keep `AssessmentJourney` independent of assessment structure: its inputs are a normalized target progress and a confirmed-completion flag. Animate one shared progress value so path coloring, mascot position and accessible progress stay synchronized. The rendered percentage number is removed. Keep sprite cadence and maximum travel duration adjustable and finite (1.76 seconds); reduced-motion users see the target directly.
- Render one continuous curved SVG path with no checkpoints, divisions or intermediate milestones. The path itself is the only visual progress indicator; the flag marks the final goal. Home and Habilidades mount that component only for an active draft; Habilidades uses the static mascot access card without a draft.

## Project Structure

### Documentation (this feature)

```text
specs/001-autoevaluacion-matching/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/
    └── assessment-api.md
```

### Source Code (repository root)

```text
backend/src/main/java/com/nextrole/
├── assessment/       # New V2 catalog, attempt, history and completion API/application services
├── matching/         # Replace V1 scoring only for the V2 assessment completion path
├── data/             # New JPA entities/repositories for versioned assessment data
└── common/           # Reuse API error envelope; extend only if V2 needs structured missing-code details
backend/src/main/resources/db/migration/
└── V5__assessment_v2.sql  # Proposed additive migration; never edit V1–V4
backend/src/test/java/com/nextrole/
├── assessment/       # Domain/service unit and API contract tests
└── integration/      # Extend API/database integration coverage using Testcontainers PostgreSQL
frontend/src/
├── features/home/       # Replace the obsolete orientation composition with approved assessment-aware Home states
├── features/assessment/ # Update assessment, summary and add completed history/detail views
├── features/roles/      # Update suggestions display to consume V2 results, preserve rank
├── services/            # Replace assessment mock calls with authenticated HTTP calls
├── types/               # Add V2 assessment/catalog/result contracts
└── components/ui/       # Reuse and adapt LevelPicker, progress, cards and buttons
```

`AssessmentJourney` remains reusable and is mounted in Home/Habilidades only for an active draft. The extension uses current/history endpoints as-is and adds no endpoint or migration. A focused backend service behavior changes reevaluation answer creation; `source_attempt_id` stays as lineage.

**Structure Decision**: Extend the existing frontend/backend layout. A dedicated backend `assessment` module owns versioned questions, attempts, answers and history; the matching domain service computes scores during completion. Existing authentication, CurrentUser, security configuration, UI shell and general UI primitives are reused. Detailed entities, invariants and endpoints are in [data-model.md](data-model.md) and [contracts/assessment-api.md](contracts/assessment-api.md).

## Blockers, Risks, and Dependencies

- **Functional blockers**: None for the approved extension. The exact empty-state copy, journey visibility, modal content, reevaluation policy and persisted Roles source are specified.
- **Role IDs**: All six role IDs and labels are approved and seeded in the assessment-specific catalog. V1 role records remain untouched.
- **Future version evolution**: If a later questionnaire changes competency codes, answer transfer for added/removed codes requires an explicit rule before publishing that changed version. This does not block the initial fixed 31-code catalog or same-version reevaluation.
- **Current frontend data seam**: Assessment flows, Home's active-draft/latest-history states, direct Roles results and the general Formation entry state use authenticated V2 assessment queries/contracts. Legacy V1 services remain for features not migrated in this scope; do not treat their mocks/localStorage as V2 assessment state. The old Home orientation consumers were removed while shared legacy helpers with other active consumers were preserved.
- **Seed drift**: 31 question snapshots, 155 answer-level texts and 186 matrix cells must be mechanically compared with the canonical V2 Markdown during implementation/migration validation.
- **Exact scores**: API serialization must preserve exact rational score components; clients render the supplied one-decimal string and rank position without sorting rounded values.
- **Migration safety**: V5 is additive. Test against a disposable Testcontainers database; do not rewrite V1–V4 or delete/migrate legacy answers.
- **Frontend tests**: Vitest and Testing Library are already configured. No new testing dependencies are required.
- **Source lineage**: `assessment_attempts.source_attempt_id` remains an optional historical link; reevaluation does not read/copy its answer rows. No persistent schema or already-applied migration changes are proposed.
- **Roles/Formation state**: existing current-attempt and completed-history queries are sufficient. `/roles?attemptId=...` remains the historical detail path; direct `/roles` uses the newest persisted history item. `/formacion` changes only its entry/empty state; role-specific `LearningPage` stays untouched.

## Implementation and validation status

The sequence below is an implementation summary, not a command to repeat completed work. The current task state and validation evidence are recorded in `tasks.md`, `quickstart.md` and `progress/current.md`.

1. Additive V5 schema, versioned catalogs, transactional draft lifecycle, exact-rational matching, persisted results, authenticated V2 API and history were implemented while preserving V1 routes and data.
2. The React assessment/results/history flow was connected to persisted V2 contracts. A prior answer-prefill proposal for reevaluations was superseded on 2026-11-25: new reevaluations now start unanswered, retain `source_attempt_id` only as provenance, and active draft recovery returns that draft's own answers.
3. The journey, Home/Habilidades states, shared discard behavior, direct Roles results, Formation entry states and narrow Skill Gap orientation-marker removal were implemented using existing contracts. Per-role Learning V1 remains intact.
4. Automated checks were recorded after implementation. Final verification appears after implementation phases in `tasks.md`; T040 and T049 remain separate manual visual reviews. T025 was initially left open for its specifically identified Testcontainers assertions; those assertions were subsequently added and passed in the 2026-11-26 full backend suite, as recorded on the task.

The original V2 assessment implementation and US4 journey are already present. This extension adds the approved Home redesign, no-prefill reevaluation, shared discard dialog, Habilidades states and direct Roles/Formation entry states; the existing journey-specific visual review T040 remains separately pending.

## Approved Addendum — Questionnaire Q2 (2026-11-26)

This approved change publishes questionnaire version Q2 with exactly ten answer-option descriptions changed from Q1. The exact copy and self-assessment writing criterion are recorded in the clarification and FR-060 of `spec.md`. Q1 remains immutable; Q2 reuses all 31 competency codes, question texts, positions and 155 levels, and copies the other 145 descriptions byte-for-byte. No competency, role requirement, essential/complementary classification, matching formula or skill-gap calculation changes.

Add the additive migration `V6__publish_questionnaire_q2.sql` with a fixed questionnaire-version UUID and code `Q2`. Within the migration transaction, copy Q1 questions/options into Q2, replace only the ten approved descriptions, then switch the current questionnaire from Q1 to Q2. Do not alter V5 or any earlier migration. Leave matrix version M1 and all 186 requirement rows untouched and current. Existing attempts retain their questionnaire and matrix foreign keys: Q1 drafts continue saving and completing against Q1; completed Q1 answers, stored results and historical descriptions remain unchanged. New attempts pin Q2/M1 through the existing current-version lookup; no application architecture or production endpoint changes are expected.

Update the normative current catalog document and its questionnaire-version examples to identify Q2/M1; retain Q1 as the immutable source snapshot in V5 and document that historical attempts load their pinned version. Add migration/integration assertions that compare Q1 and Q2, verify the exact ten-description delta and unchanged 145 options, confirm M1 is identical, preserve a completed Q1 attempt and an active Q1 draft across the migration, and verify new attempts use Q2/M1. For identical numeric answer maps, verify the existing deterministic Matching and Skill Gap results remain the same under unchanged M1 and unchanged algorithms. Tests must use disposable Testcontainers PostgreSQL only. No frontend production edit is planned because options are supplied by the versioned catalog API; update frontend fixtures only if a test hard-codes Q1.

The approved implementation has no unresolved product decision. Any implementation evidence that the existing pinned-version behavior or additive migration cannot meet these invariants must be surfaced before taking an incompatible approach.
