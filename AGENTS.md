# Codex guidance for NextRole

## Recover context first

- Start with `git status --short --branch`, recent commits, and unstaged/staged diffs. Preserve pre-existing work; identify local-only files before editing or staging.
- Read `.specify/memory/constitution.md`, then the relevant feature's approved `spec.md`, `plan.md`, `tasks.md`, and `progress/current.md`. Read linked contracts, data model, quickstart, and code/tests only as needed for the task.
- Treat `progress/current.md` as a concise session snapshot, `progress/history.md` as historical evidence, Spec Kit tasks as task status, specs as approved behavior, plans as technical approach, and contracts as interface truth. Verify each against current Git and code; documents do not prove current state by themselves.
- On an interrupted task, inspect the actual repository before resuming. Continue from existing edits; do not replay completed work or validations unless later changes affect them.

## Requirements and workflow

- Significant features follow the relevant Spec Kit stages: Specify/Clarify, Plan/Tasks/Analyze, Implement, Validate, and handoff. A user request may authorize several stages at once; do not ask again between them unless a material decision, contradiction, or real blocker appears.
- Small fixes may use a proportionate update to the existing spec and focused checks. Do not create a parallel backlog or new specification for every change.
- Approved user decisions take precedence over earlier decisions and implementation. When a decision is superseded, update affected artifacts and retain a short record of what changed and when. Never silently change business rules.
- Inspect relevant implementation, interfaces and tests before significant edits. Keep changes within scope and reuse the established architecture.

## Safety and authorization

- Preserve all unrelated modifications, untracked files, local configuration, credentials and user data. Never use `git reset`, `git restore`, `git clean`, force push, or destructive database operations unless specifically authorized; do not alter applied Flyway migrations.
- Do not delete or rewrite persistent assessment data, historical results or pinned version context without explicit functional approval. Database changes use additive Flyway migrations and approved requirements.
- A user must explicitly authorize commits and pushes. Stage only reviewed, in-scope paths. Do not change branches or publish changes unless authorized.
- User authorization establishes functional scope; it does not grant sandbox or system privileges. If a command is blocked by sandbox policy, request the required tool escalation instead of circumventing it. Never change system security policies.
- Keep secrets and machine-specific settings out of tracked changes. Report unrelated local files; do not remove them.

## Validation and completion

- Run checks proportional to the changes. Current application commands from the repository root:
  - Backend tests, including configured Testcontainers tests: `mvn -f backend/pom.xml test`.
  - Frontend tests: `npm --prefix frontend test`.
  - Frontend build: `npm --prefix frontend run build`.
  - Frontend lint: `npm --prefix frontend run lint`.
  - Whitespace check: `git diff --check`.
- For documentation-only edits, review links, commands, claims and cross-document consistency; run `git diff --check`, not application suites unless code is also affected.
- Report exact checks and outcomes as passed, failed, blocked or not run. Do not infer test, visual, mobile or manual validation from code review, build, or another person's unreported checks.
- Update the relevant Spec Kit task status only with evidence. Update `progress/current.md` for multi-step work with current branch/checkpoint, implementation state, next step, validations, open decisions/blockers, manual checks and files to preserve. Keep detailed past checkpoints in `progress/history.md`.
- A task is complete when requested in-scope edits are done, affected checks are recorded, docs and task status agree, local changes are preserved, and the user receives remaining limitations and the next authorized step. Keep unverified manual acceptance criteria open.

## Project-specific references

- Workflow and session recovery: `docs/GUIA_SDD_NEXTROLE.md`.
- Branch, staging and publication guidance: `docs/GUIA_GIT_NEXTROLE.md`.
- Docker, HTTP, Vite freshness and service restart guidance: `docs/V2/entorno-docker-desarrollo.md`.
- The current frontend test runner is Vitest, configured in `frontend/package.json`.
