# Quickstart: V2 Assessment Validation

Validation guide for implementation. Commands below are not run as part of Plan.

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
7. Start reevaluation. Verify a new attempt is prefilled from the latest completed attempt; edit it and verify the source remains unchanged. Confirm the new draft does not replace the user's current completed results.
8. Discard a draft explicitly and start again. Verify one active draft exists and discarded data is not physically removed.
9. Attempt to read/edit another user's attempt; verify no data is disclosed and the operation is rejected.

## Database validation

On a disposable empty test database only, verify Flyway applies V1–V5 in order, publishes 31 questions × five options and 6 × 31 matrix cells, and enforces one active draft per user. Do not point migration validation at a user database or perform a destructive reset.
