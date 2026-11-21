# Codex guidance for NextRole

## Source of truth and workflow

- For new features and significant changes, use the existing GitHub Spec Kit flow. Read `.specify/memory/constitution.md` and the feature's approved `spec.md`, `plan.md` and `tasks.md` when present. Those artifacts, plus the existing code and interfaces, are the working context; do not create a parallel backlog.
- Run only the Spec Kit phases requested. Do not advance automatically from Specify/Plan to Tasks or Implement.
- Before significant changes, inspect relevant code, interfaces, dependencies and tests. Preserve pre-existing work and report unrelated dirty files rather than reverting them.
- Keep implementation within the requested scope. Reuse compatible components and established architecture; do not copy the external Harness or treat its old product rules as NextRole requirements.

## Decisions and autonomy

- Follow approved functional decisions. Do not silently reinterpret or replace business rules.
- If a requested behavior materially contradicts an approved decision, explain the conflict and likely impact, then ask the contributor who made the request to confirm. Any project contributor may confirm the change; no specific owner approval is required. Once confirmed, update affected specifications and related artifacts consistently before implementing the changed behavior.
- Resolve routine technical choices autonomously when compatible with approved behavior, architecture and contracts. Do not ask for approval per file, endpoint or task when the request is clear.
- Ask for specific confirmation before destructive or difficult-to-reverse actions, including data deletion or overwriting existing work. Never run database resets or alter persistent data without that confirmation.

## Implementation and verification

- Implement and correct issues within the authorized scope. Validate affected frontend/backend contracts and review related modules for regressions.
- Run relevant configured checks when implementation is requested. From the repository root, current commands are:
  - Backend tests: `mvn -f backend/pom.xml test` (integration tests use Testcontainers and may require Docker).
  - Frontend build: `npm --prefix frontend run build`.
  - Frontend lint: `npm --prefix frontend run lint`.
- The frontend currently has no configured test script or test runner. Do not report frontend automated tests as run unless one is added and executed.
- Report the exact checks run and their outcomes. Distinguish passed, failed, blocked and not run; do not imply that inspection or compilation proves unrun tests passed.
- Use `CHECKPOINTS.md` proportionally. Mark irrelevant checks N/A; small documentation-only changes do not require application tests or independent reviewers when those add no value.
- For multi-step work, update `progress/current.md` with a concise session checkpoint. Spec Kit `tasks.md`, when present, remains the source of task status; the checkpoint summarizes session context and does not replace or duplicate task tracking.

Do not assume external Harness roles or automatic subagents are available. Use only collaboration capabilities actually present in the current environment.
