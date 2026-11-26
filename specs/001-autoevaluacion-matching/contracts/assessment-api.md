# Contract: Assessment API V2

Proposed authenticated JSON API. All routes derive the user from the existing JWT/`CurrentUser`; clients cannot supply an authoritative `userId`. Keep the current `/api/v1` APIs for unrelated features. Authentication and transport use the existing API error envelope unless noted below.

## Shared representations

- Level: integer `0..4`; absent answer is represented by absence from the answer map, never by `0` or `null` as a saved level.
- Attempt status: `DRAFT`, `COMPLETED`, or `DISCARDED`.
- A draft response includes its pinned `questionnaireVersion` and `matrixVersion`; question text/options are returned from that pinned version.
- Every newly created attempt starts with an empty `answers` map and `answeredCount: 0`; a recovered draft returns only answers previously saved to that draft. The optional persisted source-attempt relationship does not prefill or merge answer data.
- The competency diagnosis reads the declared levels from the attempt's `answers` map. It remains distinct from role-matching results; no additional diagnosis endpoint or derived scoring metric is introduced by this contract.
- Each result includes stable `roleId`, `rankPosition`, exact score numerator/denominator as decimal strings, and `displayPercentage` as a one-decimal string formatted with `HALF_UP`.
- Results are returned in persisted `rankPosition` order. The client must not sort by displayed percentage.
- Ownership failures return 404 to avoid revealing another user's attempt. Invalid state transitions return 409.

## Endpoints

| Method and route | Purpose | Success |
|---|---|---|
| `GET /api/v2/assessment-catalog/current` | Read current questionnaire version, 31 questions and five options each. | `200` catalog DTO. |
| `POST /api/v2/assessment-attempts` | Start or recover the one active draft. New attempt pins both current versions. | `200` existing draft or `201` newly created draft. |
| `GET /api/v2/assessment-attempts/current` | Recover the authenticated user's active draft. | `200` draft DTO, or `204` if none. |
| `GET /api/v2/assessment-attempts/{attemptId}` | Read owned attempt, its pinned questions and answers; include results only if complete. | `200` detail DTO. |
| `PUT /api/v2/assessment-attempts/{attemptId}/answers/{competencyCode}` | Autosave or edit one answer on an owned draft. Body: `{"level":0}`. | `200` updated answer and progress count. |
| `POST /api/v2/assessment-attempts/{attemptId}/complete` | Validate 31 distinct answers and complete atomically. | `200` completed attempt and six persisted results. |
| `POST /api/v2/assessment-attempts/reevaluations` | Recover the active draft with its own saved answers if present; otherwise create a new unanswered draft on current versions. The latest completed attempt may be linked as source provenance but its answers are not copied. | `200` recovered draft or `201` new empty reevaluation draft. |
| `POST /api/v2/assessment-attempts/{attemptId}/discard` | Explicitly transition an owned draft to `DISCARDED`. Does not physically delete it. | `204`. |
| `GET /api/v2/assessment-history` | List completed attempts newest first with completion date, six results and detail link/ID. | `200` array; drafts excluded. |

## Representative DTOs

```json
{
  "attemptId": "uuid",
  "status": "DRAFT",
  "questionnaireVersion": "Q1",
  "matrixVersion": "M1",
  "answeredCount": 1,
  "totalQuestions": 31,
  "answers": {"programming-fundamentals": 0},
  "questions": [{
    "competencyCode": "programming-fundamentals",
    "position": 1,
    "title": "Fundamentos de programación",
    "question": "...",
    "options": [{"level": 0, "description": "..."}]
  }]
}
```

```json
{
  "attemptId": "uuid",
  "status": "COMPLETED",
  "completedAt": "2026-11-20T12:00:00Z",
  "questionnaireVersion": "Q1",
  "matrixVersion": "M1",
  "results": [{
    "roleId": "backend-dev-jr",
    "scoreNumerator": "817",
    "scoreDenominator": "12",
    "displayPercentage": "68.1",
    "rankPosition": 1
  }]
}
```

The exact fraction expresses the unrounded percentage. The decimal display is generated from it using one decimal and `HALF_UP`; it is never an input to sorting or tie detection.

## Validation and error contract

- Completion with missing answers returns `409` with code `INCOMPLETE_ASSESSMENT` and `details.missingCompetencies` listing stable competency codes; it creates no result set and leaves the draft editable.
- Saving a level outside 0–4, a competency not in the pinned questionnaire, or an answer to a non-draft returns `400`/`409` through the shared error envelope.
- Starting concurrent drafts relies on the unique active-draft invariant; the loser returns the winning active draft, not a duplicate or an opaque server error.
- Repeating completion for an already completed attempt returns the same persisted results and tie order. It does not recalculate or randomize again.
- History contains completed attempts only, newest first; detail returns the historical question/options, matrix version, answers and persisted results.
