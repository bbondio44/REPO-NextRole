# Data Model: Autoevaluación técnica y matching de roles

This document records the implemented V2 data model. V5 established the versioned assessment schema after V1–V4. V6 publishes questionnaire Q2 without schema changes. Existing V1 tables remain available and are not converted or deleted.

## Catalog and independent versions

| Entity/table concept | Key fields | Relationships and invariants |
|---|---|---|
| `assessment_roles` | `id`, `name` | V2 role catalog isolated from the V1 `roles` table so adding QA and Cloud does not change V1 role listings. Contains the six approved stable IDs and labels: `backend-dev-jr`/Backend, `frontend-dev-jr`/Frontend, `fullstack-dev-jr`/Full Stack, `qa-jr`/QA, `data-analyst-jr`/Data Analyst, and `cloud-jr`/Cloud. |
| `assessment_questionnaire_versions` | `id`, `version_code`, `published_at`, `is_current` | Questionnaire version is independent of role matrix version. Published versions are immutable. At most one current questionnaire version. |
| `assessment_questions` | `questionnaire_version_id`, `competency_code`, `position`, `title`, `question_text` | Composite PK `(questionnaire_version_id, competency_code)`; exactly 31 ordered competencies in the initial published version. Codes use stable lowercase kebab-case technical identifiers; source text comes verbatim from the V2 catalog. |
| `assessment_answer_options` | `questionnaire_version_id`, `competency_code`, `level`, `description` | Composite FK to question; PK includes level; each question has exactly five levels 0–4. Published option text is immutable. |
| `assessment_matrix_versions` | `id`, `version_code`, `published_at`, `is_current` | Independent of questionnaire version. At most one current matrix version. Published rows are immutable. |
| `assessment_role_requirements` | `matrix_version_id`, `role_id`, `competency_code`, `importance`, `expected_level` | 186 rows per complete matrix (six roles × 31 competencies), including irrelevant cells. Unique matrix/role/competency. `role_id` references `assessment_roles`, never the V1 `roles` catalog. `importance` is `ESSENTIAL`, `COMPLEMENTARY`, or `NOT_RELEVANT`; expected level is 1–4 for relevant cells and NULL for irrelevant cells. Initial seed exactly matches the approved matrix and therefore uses expected levels 1–3. |

Suggested competency codes for the 31 canonical catalog entries: `programming-fundamentals`, `algorithms`, `data-structures`, `object-oriented-programming`, `basic-architecture`, `sql-queries`, `database-modeling`, `rest-api-consumption`, `rest-api-development`, `business-rules`, `web-interface-development`, `state-management`, `debugging`, `git-version-control`, `integration-collaboration`, `docker-containers`, `testing-fundamentals`, `test-case-design`, `test-execution-documentation`, `defect-management`, `api-testing`, `exploratory-data-analysis`, `data-cleaning-preparation`, `data-visualization-dashboards`, `applied-statistics`, `business-metrics-interpretation`, `cloud-computing-fundamentals`, `linux-operating-systems`, `networking-fundamentals`, `scripting-automation`, and `infrastructure-monitoring-diagnostics`. These are technical identifiers; display text remains the catalog text.

## User attempts and history

| Entity/table concept | Key fields | Relationships and invariants |
|---|---|---|
| `assessment_attempts` | `id`, `user_id`, `status`, `questionnaire_version_id`, `matrix_version_id`, `created_at`, `completed_at`, `discarded_at`, `source_attempt_id` | FK to existing user and pinned version rows. Status: `DRAFT`, `COMPLETED`, `DISCARDED`. Partial unique index on `user_id WHERE status='DRAFT'`. Completion freezes answers and results. Discard is an explicit state transition. `source_attempt_id` links reevaluation to the completed source without mutating it. |
| `assessment_answers` | `attempt_id`, `questionnaire_version_id`, `competency_code`, `level`, `updated_at` | PK `(attempt_id, competency_code)`. `questionnaire_version_id` duplicates the attempt's pinned version as a relational key discriminator and is constrained with a composite FK to `(assessment_attempts.id, assessment_attempts.questionnaire_version_id)`; `(questionnaire_version_id, competency_code)` is a composite FK to the pinned questionnaire question. This makes the version/competency invariant enforceable by PostgreSQL without changing the answer API. Level 0–4. Missing row means unanswered; level 0 is an answer. Editable only while attempt is `DRAFT`. |
| `assessment_result_sets` | `attempt_id`, `algorithm_version`, `calculated_at` | One result set per completed attempt; FK to attempt; completion transaction writes it with status transition. |
| `assessment_role_results` | `result_set_id`, `role_id`, `score_numerator`, `score_denominator`, `display_percentage`, `rank_position` | Unique result-set/role. Six rows for the six matrix roles. Numerator and denominator use arbitrary-precision integer-compatible `NUMERIC` columns; display is one decimal `NUMERIC(4,1)`. Rank is persisted after exact ranking and one-time tie randomization. |

### Relationships

```text
User 1 ── * AssessmentAttempt * ── 1 QuestionnaireVersion
                            * ── 1 RoleMatrixVersion
AssessmentAttempt 1 ── * AssessmentAnswer * ── 1 AssessmentQuestion
AssessmentAttempt 1 ── 0..1 MatchingResultSet 1 ── 6 RoleMatchingResult * ── 1 Role
QuestionnaireVersion 1 ── 31 AssessmentQuestion 1 ── 5 AnswerLevelOption
RoleMatrixVersion 1 ── 186 RoleRequirement (6 roles × 31 competencies)
```

## Published questionnaire versions after Q2 approval

The implemented version history now consists of immutable questionnaire Q1 (seeded by V5) and Q2 (published additively by V6). Q2 copies the same 31 competency codes, question text, positions and 155 levels, changing only the ten approved option descriptions; the other 145 descriptions are unchanged. Q2 is current. Matrix M1 remains the current, unchanged set of 186 requirements. Each attempt continues referencing its original questionnaire and matrix versions, so Q1 answers, their displayed option descriptions, and persisted results remain tied to Q1. No table, entity or API shape changes are needed to publish Q2.

## Database constraints and transaction boundaries

- Use composite foreign keys so answers reference competency codes in the attempt's pinned questionnaire version and result roles exist in `assessment_roles`. The answer's `questionnaire_version_id` is a denormalized FK discriminator that must match its attempt. Keep V2 role records isolated from the V1 `roles` table so V1 endpoints retain their existing catalog behavior.
- Check `level BETWEEN 0 AND 4`; check importance values. Enforce `(NOT_RELEVANT AND expected_level IS NULL) OR (relevant AND expected_level BETWEEN 1 AND 4)`.
- Unique partial index permits at most one active draft per user. Start is transactional and handles a uniqueness race by returning the already-created draft.
- Completion locks the owned draft, validates exactly 31 answers, reads its pinned immutable versions, calculates six results, persists tie order, and marks the attempt complete in one transaction. An incomplete attempt creates no results and remains editable.
- A reevaluation creates a fresh unanswered draft pinned to current versions and never edits the source. It may set `assessment_attempts.source_attempt_id` to the latest completed attempt as provenance; this FK does not copy or expose source answers in the new draft. Recovering an existing draft returns only that draft's saved answers. No schema change is required for this behavior.
- History selects only completed attempts and orders by `completed_at DESC`; detail reads persisted scores and pinned catalog/matrix versions. Drafts are fetched through the current-attempt workflow.
- Do not add triggers or service paths that delete/convert V1 answer data. A future destructive operation requires specific authorization.

## Initial migration and seed

`V5__assessment_v2.sql` created the V2 role catalog and versioned questionnaire, matrix, attempt, answer and result tables and seeded Q1/M1. `V6__publish_questionnaire_q2.sql` copies Q1 to Q2 and updates exactly ten approved option descriptions, preserving the other 145 descriptions, all 31 questions, and the independent M1 matrix. Attempts retain the questionnaire and matrix versions assigned at creation; published snapshots are immutable. No V1 role catalog, Q1 row or existing user answer is changed.
