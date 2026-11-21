# Context-sensitive quality checkpoints

Apply the checks relevant to the requested change. Mark unrelated items N/A rather than creating extra work.

- [ ] Scope and acceptance criteria are clear and traceable to the request and approved Spec Kit artifacts.
- [ ] Relevant specifications, implementation, interfaces, dependencies and tests were inspected; material differences were addressed or surfaced.
- [ ] Functional decisions were preserved. Any material contradiction was explained and confirmed by the requesting contributor before changing behavior.
- [ ] Frontend-backend contracts and affected modules were reviewed for compatibility and regressions.
- [ ] Relevant configured unit, integration, build or lint checks were run; outcomes and unavailable/skipped checks are recorded.
- [ ] Database and migration changes follow approved requirements, use a new Flyway migration where needed, and do not rewrite applied migrations.
- [ ] Destructive or difficult-to-reverse operations had specific confirmation before execution.
- [ ] Changes outside the request were avoided; pre-existing work was preserved.
- [ ] For multi-step work, `progress/current.md` summarizes the session and points to Spec Kit for feature/task state.

Completion reports identify files changed, validations actually run, failures or blockers, and checks not run. Do not require every checkpoint for a small change; do not mark an inapplicable item as a failure.
