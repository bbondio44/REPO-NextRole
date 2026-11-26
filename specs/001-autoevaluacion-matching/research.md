# Research: Autoevaluación técnica y matching de roles IT

## Decision: V2 assessment data has a separate versioned model

**Finding**: Existing persistence has `user_skills` with one current level per user/skill, `role_skills` over the V1 skills catalog, and `matching_runs`/`matching_results` without questionnaire or matrix version references. Current seed data has 15 skills and eight roles.

**Decision**: Add V2-specific questionnaire, matrix, attempt, answer and result tables. Use the approved V2 Markdown as the only seed source. Do not map V1 skill rows to V2 competencies or migrate old answers.

**Rationale**: The V1 model cannot express 31 question snapshots, independent versions, incomplete attempts, immutable completed attempts or the 186-cell V2 matrix. This follows the approved non-migration rule and avoids changing V1 behavior for unrelated features.

**Alternatives considered**: Reuse `user_skills` and `role_skills` (rejected: no versioned attempt/catalog model); convert V1 rows (rejected: not required or approved).

## Decision: Exact score representation precedes display rounding

**Finding**: V1 `Scoring.percent` rounds to an integer and `MatchingService` drops missing requirements, renormalizes over observed requirements, includes interests in affinity, and sorts by affinity. These behaviors contradict V2.

**Decision**: Calculate each V2 role score as an exact rational percentage. Compare numerator/denominator pairs by cross multiplication, persist the final rank, and use decimal `HALF_UP` at scale one only for display.

**Rationale**: Exact rational comparison distinguishes true ties from values that merely render identically. Relevant expected levels are bounded to 1–4, so the score is finite-rational and can be represented without arbitrary display-scale rounding.

**Alternatives considered**: Java `double` (rejected for exact-tie detection); rounded decimal ranking (rejected because display rounding must not affect order); V1 scoring (rejected as incompatible).

## Decision: Draft uniqueness is enforced in persistence and service logic

**Finding**: Current V1 skills are saved independently as user-level rows; there is no draft or attempt state. PostgreSQL is already the production persistence target and Testcontainers is used for integration tests.

**Decision**: Add a partial unique index for one `DRAFT` attempt per user. Start/recovery runs transactionally; if concurrent creation loses the unique-index race, load and return the winning draft. Completion locks the attempt, validates 31 answers and writes status and all six results atomically. Discard changes state; it does not physically delete the draft.

**Rationale**: The database invariant closes the duplicate-draft race while the service provides the approved recover-existing UX.

**Alternatives considered**: Service-only check (rejected: concurrent requests could create duplicates); physical delete on discard (rejected: unnecessary destructive operation).

## Decision: Add V2 contracts without replacing unrelated V1 APIs

**Finding at feature start**: The backend already had `/api/v1` controllers and authentication. The frontend service layer included mock/localStorage helpers, and `integration/frontend-api-adapter.ts` was not wired into the build and assumed legacy V1 types.

**Decision**: Add authenticated `/api/v2` assessment endpoints and connect the assessment flow to a shared HTTP client in the current frontend service layer. Keep existing V1 endpoints for profile, Skill Gap and Learning unless separately migrated later.

**Current implementation status**: The V2 assessment screens use authenticated `/api/v2` requests and persisted attempt/history contracts. Authentication/profile and legacy Skill Gap/per-role Learning flows continue to use applicable V1 services; legacy helpers may still use localStorage. Do not describe the whole frontend as mock-only or imply that the V2 assessment source of truth is localStorage.

**Rationale**: This creates a bounded compatibility seam for the feature and avoids a broad route break.

**Alternatives considered**: Modify `/api/v1/matching/analyze` in place (rejected: breaks old contract and other V1 consumers); use the integration adapter unchanged (rejected: not active and has incompatible V1 assumptions).

## Decision: Use V2 stable role identifiers

**Approved decision**: The V2 assessment role catalog contains the six stable IDs `backend-dev-jr`, `frontend-dev-jr`, `fullstack-dev-jr`, `qa-jr`, `data-analyst-jr`, and `cloud-jr`. Keep this catalog separate from V1 `roles`; do not repurpose `qa-automation-jr` or `devops-jr`, whose scopes are narrower or combined. This preserves V1 role listing behavior while exposing the approved V2 labels.

**Rationale**: This matches the six approved role concepts and preserves the legacy catalog.

## Decision: Add one migration after the current Flyway history

**Finding**: Existing migrations are V1 through V4; schema and catalog seeds are in V1/V2.

**Decision**: Add V5 for V2-specific tables, constraints, indexes and initial immutable questionnaire/matrix version seeds. Do not alter applied migrations or delete legacy rows. Avoid any V1 answer migration.

## Unresolved future evolution (not a baseline implementation blocker)

Approved reevaluation policy (2026-11-25): every new attempt starts unanswered; source answers are never copied, even when competency codes are unchanged. `source_attempt_id` remains useful provenance linking a reevaluation to the completed attempt from which the user chose to reevaluate, without affecting state or scoring. If a future questionnaire changes the competency set, this provenance link remains valid and requires no answer-mapping rule. The active draft remains recoverable with its own saved answers.
