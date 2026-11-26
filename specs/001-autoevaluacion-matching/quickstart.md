# Quickstart: V2 Assessment Validation

Validation guide for implementation. It describes repeatable scenarios, not evidence that any scenario was run; execution results and the verified/pending manual subset belong in `progress/current.md` and its historical checkpoints. The approved reevaluation rule is to start every new attempt unanswered; the earlier prefill proposal is superseded and retained in `spec.md` clarification history.

## Prerequisites

- Java 21 and Maven.
- Node.js/npm compatible with the repository's Vite toolchain.
- Docker running for Testcontainers PostgreSQL integration tests.
- Local PostgreSQL and JWT/origin settings from the backend's existing development configuration for manual end-to-end runs.

## Automated validation

1. Run backend unit, API contract and integration suite: `mvn -f backend/pom.xml test`. Expected: matching domain tests pass, V2 routes/DTOs/auth/error responses conform to `contracts/assessment-api.md`, migration V5 applies on a clean PostgreSQL test database, and lifecycle tests cover drafts, authorization, completion, exact ordering and history. Seed validation must compare all 31 competency titles/questions, all 155 option descriptions and all 186 role-matrix cells (importance/relevance/expected level) with `docs/V2/autoevaluacion-matriz-vigente.md`, not just record counts.
2. Run frontend checks: `npm --prefix frontend run build` and `npm --prefix frontend run lint`.
3. Run frontend tests: `npm --prefix frontend test`. Expected: five-level selector, explicit zero, pending state, autosave/recovery, persisted result order, completed-only history, a competency diagnosis sourced from declared V2 levels and separated from role matching, and result copy identifying the scores as self-assessed rather than a certification.

## End-to-end scenarios

1. Authenticate as a user and open autoevaluation. API returns or creates one draft pinned to Q1/M1.
2. Save level 0 on one competency, reload, and verify that 0 is selected and the question is answered. Save several other levels; verify progress survives re-entry.
3. Attempt completion with 30 answers. Expect `INCOMPLETE_ASSESSMENT`, missing competency codes, editable draft and no results.
4. Answer all 31, complete, and verify six roles, exact score fractions, one-decimal HALF_UP strings, descending exact ranking and persisted order for exact ties.
5. View the competency diagnosis and verify it shows the declared V2 levels without replacing them with role scores or introducing new diagnostic metrics. View role matching separately and verify its results identify themselves as self-assessed, not certification.
6. Open history. Verify the completed attempt appears newest first with date/results/detail while the draft does not appear.
7. Start reevaluation. Verify it has an empty answers map and zero saved answers while the completed source still has its original answers and persisted results. Confirm source provenance remains recorded, and re-opening the active draft preserves only answers saved to that new draft.
8. Discard a draft from Habilidades. Cancel once and verify it remains; confirm once and verify it is discarded, no replacement is created, history remains, and the screen stays on Habilidades. Then start a new attempt using the separate explicit action.
9. Attempt to read/edit another user's attempt; verify no data is disclosed and the operation is rejected.

## Home and discard validation

Use accounts or persisted test states covering no draft/no history, draft/no history, no draft/history, and draft/history. Verify Home displays the matching motivation/draft/latest-result cards, shows the active draft's saved-answer ratio (including level 0), preserves stored latest-result date/rank/percentage and opens `/roles?attemptId=…` and `/habilidades/historial`. Verify a fully answered but unfinalized draft stays a draft at 100% without celebration.

With a draft, open the shared centered discard modal from Home and Habilidades. Verify its title, copy and destructive action; cancel and verify no state changes. Confirm discard and verify the source screen remains open, the draft journey disappears only after backend success, no new draft is created, the old history remains, and Habilidades does not show a journey for that older completion. Simulate a failed discard and verify the modal/error and draft remain visible. Complete an attempt successfully, return to Home and verify the draft card is replaced by recurrent motivation and the new persisted latest-result card, without auto-starting another attempt.

For Home's active journey, verify the existing path/mascot remain synchronized and the numeric percentage is not visually rendered while the progressbar's accessible value still reflects saved answers. Check the approved deterministic copy at 0% (first evaluation vs. reevaluation), 1–39%, 40–74%, 75–99% and 100% without exposing these numbers in the UI. Confirm the blue card title, message, description and actions are legible.

Open Habilidades without a draft: without history, verify the static mascot, first-evaluation copy and “Comenzar evaluación”; with history, verify the static mascot, recurrent copy, “Comenzar nueva evaluación” and history link. Neither state should render a path, goal, journey animation or journey percentage.

Open Roles directly (without `attemptId`) with completed history and verify it shows the newest persisted ranking. With a separate draft, verify the context note says the ranking is from the last completed evaluation and all “Explorar rol” actions are hidden; cancel/discard the draft and verify exploration returns only after backend-confirmed discard. Open a historical `/roles?attemptId=…` link and verify it remains pinned to that attempt, including links from its ranking. Without completed history, verify the Roles empty state links to Habilidades and adapts to an active draft without fake rankings.

Open Formación directly. Without completed history, verify the approved “Prepará tu próximo paso” copy and Habilidades action, with no role-selection prompt or suggested-role ranking. With history and no draft, verify the invitation to Roles. When a new draft coexists with history, verify the approved assessment-pending copy, a continuation action, and no “Explorar roles” action or restored previous role/context. After confirmed discard, verify the Roles invitation returns without restoring a previously explored role. Check that `/roles/:roleId/formacion` still uses its existing role-based resources.

From history, select more than one completed attempt and follow each explicit `attemptId` to results and associated diagnosis/gap/recommendation links where available. Verify the selected attempt remains in the URL and no values from another evaluation or a current draft are substituted.

Open Skill Gap for a role. Verify the old «PASO 5 DE 6» label and six-segment orientation strip are absent while the diagnostic title, competency rows and their progress bars, status labels, role context and training link remain unchanged. Also verify registration, profile onboarding and the assessment's own progress indicators remain available.

While either Home status request is pending or fails, verify the page does not show empty-state cards or incompatible controls. At mobile and desktop widths, verify card order, spacing above bottom navigation, contrast, touch targets, reduced-motion behavior and no horizontal overflow. This visual review does not close the separate journey-specific T040 until its criteria are fully verified.

## Database validation

On a disposable empty test database only, verify Flyway applies V1–V5 in order, publishes 31 questions × five options and 6 × 31 matrix cells, and enforces one active draft per user. Do not point migration validation at a user database or perform a destructive reset.
