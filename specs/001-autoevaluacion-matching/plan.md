# Implementation Plan: Autoevaluación técnica y matching de roles IT

**Branch**: `develop-v2` (feature: `001-autoevaluacion-matching`) | **Date**: 2026-11-20 | **Spec**: [spec.md](spec.md)

**Input**: Functional specification in `specs/001-autoevaluacion-matching/spec.md`.

## Summary

Replace the V1 skill-level/mock matching flow with a versioned assessment workflow on the existing Spring Boot API and React application. Persist one active draft per authenticated user, immutable completed attempts and six deterministic, precisely ranked role results. Implement the approved formula in a backend application service, expose authenticated V2 contracts, and connect the existing assessment/results UI to them. Use additive Flyway migrations; do not convert or delete V1 answers.

All architecture, persistence, API and identifier choices in this Plan are technical proposals for implementation review; they do not add or change functional decisions.

## Technical Context

**Language/Version**: Java 21; TypeScript 7 / React 19.

**Primary Dependencies**: Spring Boot 3.5.16, Spring MVC, Bean Validation, Spring Data JPA, Spring Security/JWT; React Router 7, TanStack Query 5, Vite 8.

**Storage**: PostgreSQL 16; existing Flyway history currently ends at V4.

**Testing**: JUnit 5 / Spring Boot Test / Testcontainers PostgreSQL; frontend has build/lint but no test runner configured. Add frontend component/service tests with a Vite-compatible runner (Vitest and Testing Library proposed).

**Target Platform**: Browser client and authenticated Spring Boot web API.

**Project Type**: Web application with modular monolith backend.

**Performance Goals**: No new latency or throughput target is established by the functional specification. Finalizing one attempt evaluates 31 competencies against six roles and persists six results in one transaction.

**Constraints**: Matching requires all 31 answers; level 0 is present data. Keep questionnaire and matrix versions independent and pinned to drafts. Use exact score comparisons before display rounding. Preserve applied migrations; add a new migration. No automatic V1 answer conversion or destructive data operation.

**Scale/Scope**: One shared 31-question catalog, six V2 roles, one active draft per user, completed-only history. Skill Gap, AI explanations, Learning, graphical comparison and role preference scoring remain outside this implementation.

## Constitution Check

| Principle | Gate result |
|---|---|
| I. Modular architecture | PASS — extend the existing modular monolith; keep React, API, business logic and persistence responsibilities separate. |
| II. Approved requirements | PASS — the V2 specification/catalog supersede V1 mocks, seed values and scoring. |
| III. Specify and design first | PASS — this plan and its design artifacts precede Tasks and implementation. |
| IV. Controlled V1 evolution | PASS — additive Flyway migration; no V1 answer conversion or deletion. |
| V. Historical meaning | PASS — immutable attempts/results retain independent questionnaire and matrix versions. |
| VI. Quality and contracts | PASS — define API contracts and unit, integration, frontend and contract validation. |
| VII. Consistency and evolution | PASS — current code was inspected; compatible components are reused and V1 behavior to replace is identified. |

No Constitution violations require complexity justification.

## Component Impact

| Classification | Current components | Plan |
|---|---|---|
| Reuse | `AppShell`, `Card`, `Button`, `Badge`, `ProgressBar`, React Router, TanStack Query; Spring Security/JWT, `CurrentUser`, Bean Validation, JPA/Flyway conventions, `ApiProblem` envelope | Keep layout, navigation/query tooling and authenticated user derivation. Reuse common error response conventions. |
| Modify | `SkillAssessmentPage`, `SkillsIndexPage`, `SkillSummaryPage`, `RolesSuggestedPage`, `LevelPicker`, `frontend/src/services/api.ts`, `frontend/src/types/domain.ts`; V2 completion path in `MatchingService`/controller; integration tests | Show catalog text levels 0–4, save/recover server draft, gate completion on 31 answers, preserve the competency diagnosis using V2 declared levels, and show separate V2 role-matching results/history with persisted ranks. Replace V1 scoring only for V2; do not break other V1 routes. Connect service calls to authenticated API. No visual redesign is part of this change. |
| Add | Backend `assessment` module, V2 role catalog, V2 matching engine, versioned entities/repositories, V2 controller/DTOs, `AssessmentHistoryPage` and attempt detail UI, migration V5, API/unit/integration/frontend tests | Own the attempt lifecycle, snapshots, completion transaction, version context and completed-only history. Keep V2 role records out of the shared V1 role catalog to avoid changing existing V1 responses. |

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
├── features/assessment/ # Update assessment, summary and add completed history/detail views
├── features/roles/      # Update suggestions display to consume V2 results, preserve rank
├── services/            # Replace assessment mock calls with authenticated HTTP calls
├── types/               # Add V2 assessment/catalog/result contracts
└── components/ui/       # Reuse and adapt LevelPicker, progress, cards and buttons
```

**Structure Decision**: Extend the existing frontend/backend layout. A dedicated backend `assessment` module owns versioned questions, attempts, answers and history; the matching domain service computes scores during completion. Existing authentication, CurrentUser, security configuration, UI shell and general UI primitives are reused. Detailed entities, invariants and endpoints are in [data-model.md](data-model.md) and [contracts/assessment-api.md](contracts/assessment-api.md).

## Blockers, Risks, and Dependencies

- **Functional blockers**: None for the initial V2 release. The approved questionnaire, matrix, scoring, ordering, versioning, draft, history, diagnosis/matching separation and role IDs are sufficiently defined.
- **Role IDs**: All six role IDs and labels are approved and seeded in the assessment-specific catalog. V1 role records remain untouched.
- **Future version evolution**: If a later questionnaire changes competency codes, answer transfer for added/removed codes requires an explicit rule before publishing that changed version. This does not block the initial fixed 31-code catalog or same-version reevaluation.
- **Frontend authentication seam**: Active UI services are mocks, while the HTTP adapter is a disconnected V1 prototype. The V2 flow depends on wiring authenticated requests to existing JWT/session behavior.
- **Seed drift**: 31 question snapshots, 155 answer-level texts and 186 matrix cells must be mechanically compared with the canonical V2 Markdown during implementation/migration validation.
- **Exact scores**: API serialization must preserve exact rational score components; clients render the supplied one-decimal string and rank position without sorting rounded values.
- **Migration safety**: V5 is additive. Test against a disposable Testcontainers database; do not rewrite V1–V4 or delete/migrate legacy answers.
- **Frontend test tooling**: No frontend test runner is configured. Add a Vite-compatible component/service test setup during implementation; backend JUnit/Testcontainers already exist.

## Implementation Sequence (planning only)

1. Add the V2 catalog/matrix seed and attempt/result schema through a new additive Flyway migration.
2. Add immutable published version records, repositories and transactional draft lifecycle services.
3. Add exact-rational matching and completed-result persistence.
4. Add authenticated V2 API contracts, API contract tests and integration tests; retain V1 routes for other features.
5. Connect the React assessment and results/history flow to the API; remove V1 localStorage/mocks from this flow without deleting stored local data.
6. Validate the end-to-end workflow and regression contracts described in `quickstart.md`.

Tasks and implementation are separate Spec Kit phases and were not run.
