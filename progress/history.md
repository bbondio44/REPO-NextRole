# Historical session checkpoints

This file preserves the previous `progress/current.md` chronology as historical evidence. Its statements describe the repository at the dates recorded and may be superseded. For the current branch, checkpoint, pending validation and next step, use [`current.md`](current.md) and verify Git before acting.

---

# Codex continuity checkpoint

**Updated**: 2026-11-20

## Active application feature

- Feature: `001-autoevaluacion-matching`.
- Spec Kit state: Constitution, Specification, Clarify, Plan and Tasks exist for this feature. Spec Kit `tasks.md` remains authoritative for task status.
- This checkpoint is session context only. It is not a backlog and does not track individual tasks.

## Setup and Foundational checkpoint

- Work: implement Setup and Foundational tasks T001–T005 for `001-autoevaluacion-matching` only.
- Completed: T001–T005 are marked complete in `specs/001-autoevaluacion-matching/tasks.md`. V5 is additive, seeds 31 questions, 155 options, 186 requirements and six IDs into a separate `assessment_roles` catalog. V1–V4 and V1 role rows are unchanged. JPA mappings/repositories cover V2 catalogs, attempts, answers and results.
- Validation: `mvn -f backend/pom.xml test` passed (10 tests) against disposable PostgreSQL 16 Testcontainers; Flyway applied V1–V5 and the canonical catalog/matrix comparison passed. `npm.cmd test -- --passWithNoTests` exited successfully but found no frontend tests yet. Frontend build passed. Frontend lint exited successfully with one existing `react(only-export-components)` warning in `src/app/SessionContext.tsx`. No user database was used.
- Issue resolved: an initial V5 seed in the shared V1 `roles` table changed V1 matching output from 8 to 10 roles. Moved the approved six IDs to `assessment_roles`; the full suite now passes and V1 role behavior remains unchanged.
- Blockers: none for Setup and Foundational. T006 onward (US1) is pending and was not started.
- Next step at that checkpoint: await explicit authorization to begin US1; no later story was started at that time.

## Docker development environment update

- Work: adapt the existing Compose setup to run PostgreSQL, Spring Boot and React/Vite together; this is local development infrastructure and does not advance `001-autoevaluacion-matching` beyond T001–T005.
- Changes: added frontend dev image, backend healthcheck, Compose dependencies/healthchecks, configurable host ports, a Vite `/api` proxy, local environment template and Docker workflow guide. Kept the PostgreSQL volume name and previous local credentials as defaults.
- Validation: `docker-compose --env-file .env.example config`, frontend build and lint passed (lint reports its existing Fast Refresh warning). An isolated Docker Compose project built both images, reached healthy for all services, returned `UP` from Actuator and HTTP 200 from Vite, and applied Flyway V1–V5 to a new PostgreSQL volume. The proxied protected roles route reached the backend and returned 401 as expected without authentication. Test containers, network and test-only volumes were removed; no existing project volume was used.
- Final Docker configuration: PostgreSQL remains internal on 5432 with no host port published; backend maps host 8090 to container 8080; frontend maps 5173 to 5173. The active Compose instance was verified healthy on those ports.
- Next step: T006 (US1) remains pending and requires its separate authorization; no assessment implementation task was started here.

## US1 implementation session

- Work: implement only T006–T015 for Autoevaluación and draft management. US2 matching/completion and US3 history/reevaluation were not started.
- Backend: added version-backed catalog DTO mapping, authenticated start/recover/current/detail endpoints, one active draft locking using the existing user row plus the V5 unique partial index, and owned answer autosave/read. Answer writes are restricted to DRAFT attempts; level 0 persists as a row and missing answers remain absent. Attempts pin the current questionnaire and matrix versions.
- Frontend: connected login/register session establishment to the existing JWT endpoints (access token held in memory; refresh uses the existing HttpOnly cookie), added typed V2 assessment API contracts, and wired the 31-question page to load/recover a draft and autosave answers. The five catalog-provided options render verbatim, including level 0. Results remain disabled pending US2 completion.
- Validation: `mvn -f backend/pom.xml test` passed (15 tests), including V1 regression/integration tests and US1 API tests using disposable Testcontainers PostgreSQL; Flyway applied V1–V5. Frontend tests passed (5 tests), build passed, lint passed with one existing Fast Refresh warning in `SessionContext.tsx`; `git diff --check` passed. No persistent user database was used by tests.
- Next step: await authorization before starting US2. No commits, push or merges were performed.

## Previous session note

- Work: lightweight Harness guidance integration with Spec Kit.
- Changes: root `AGENTS.md`, `CHECKPOINTS.md`, and this continuity checkpoint.
- Validation: inspected Constitution 1.1.0, Spec Kit/Codex integration, repository structure, Harness guidance/scripts, Maven tests and frontend scripts. Ran the external `scripts/verify.py --mode inspect`: Java, Maven and Docker were found, but its app paths (`backend/pom.xml`, `docker-compose.yml`, migrations) are absent from the Harness snapshot. Did not run `harness_check.py` because it requires the parallel `feature_list.json` backlog.
- Application checks: not run; that request changed documentation/configuration guidance only.

## US2 implementation session

- Updated: 2026-11-21.
- Work: implemented only T016–T023 for completion, V2 matching and persisted result presentation. US3 (T024–T031) and cross-cutting closure tasks T032–T034 remain pending and were not started.
- Backend: added exact-rational matching using the approved essential/complementary weights and irrelevant-requirement exclusion; exact score ordering, random order within exact ties, one-decimal HALF_UP display, and algorithm version `matching-v2.0`. Completion locks the owned draft, requires all 31 saved answers and atomically persists completion plus six ranked results. Repeated completion returns those stored results. Answer autosaves serialize against completion. Added the completion endpoint and structured incomplete response.
- Frontend: added typed completion/result API calls. Finalization is enabled after all answers are present; the diagnosis displays saved self-assessed levels separately from matching. Suggested roles and role detail display the persisted six-role scores/order and approved labels without client-side sorting. Existing V1 matching service, score logic and migrations V1–V4 were not modified.
- Tests first: added T016 matching unit tests, T017 API contract tests and T018 Testcontainers integration coverage before implementation. Updated two existing US1 service-test mocks to use the new lock-aware attempt lookup; production behavior remains the same. Initial full-suite run exposed those stale mocks (2 errors); the corrected rerun passed.
- Validation: `mvn -f backend/pom.xml test` passed (24 tests; 0 failures/errors), including V1 `ScoringTest` and `ApiIntegrationTest`, plus V2 integration tests against disposable PostgreSQL 16 Testcontainers with Flyway V1–V5. `npm.cmd --prefix frontend test` passed (11 tests across 4 files). `npm.cmd --prefix frontend run build` passed. `npm.cmd --prefix frontend run lint` passed with the existing warning in `src/app/SessionContext.tsx:45` (`react(only-export-components)`). `git diff --check` passed; Git printed line-ending conversion notices only. Manual browser scenarios were not run; the workflow test T032 and closure task T033 are outside this authorization.
- Next step: T016–T023 are marked complete in `specs/001-autoevaluacion-matching/tasks.md`. Await separate authorization before starting US3 or T032–T034. No commit or push was made; the pre-existing `package-lock.local-backup.json` remains untouched.

## Assessment questionnaire UX refinement

- Updated: 2026-11-22.
- Work: redesigned only the V2 questionnaire presentation and question navigation. The approved catalog grouping was checked against V5 question positions: Development 1–16, QA 17–21, Data 22–26, Infrastructure/Cloud 27–31. The catalog has no separate thematic field, so the requested position ranges are presentation-only.
- UI: show one question at a time; compact horizontal named level choices; reveal only the selected catalog description; add previous/next navigation, thematic arrows and per-question status blocks for the current area. Existing V2 services, draft persistence, explicit level 0, completion guard and result navigation are retained. No API, backend, database, matching or US3 changes.
- Validation: frontend tests passed (14 tests across 4 files); frontend production build passed; lint passed with the existing `SessionContext.tsx` Fast Refresh warning only; `git diff --check` passed. The test suite covers question navigation, group boundaries, selection without auto-advance, stored response state, status blocks and finalization guard. No authenticated browser preview was run, so actual visual inspection at desktop/mobile viewport sizes remains for reviewer validation.
- Next step: review the UI in the local app at desktop and mobile widths. No commit or push was made. Existing untracked `package-lock.local-backup.json` and `tatus` were preserved.

## Assessment questionnaire UX refinement — iteration 2

- Updated: 2026-11-22.
- Work: refined the existing V2 questionnaire UI in response to mobile review. Removed numbered question blocks and manual area arrows; the thematic label now follows previous/next navigation automatically.
- Progress and completion: the slim whole-assessment progress bar counts competency keys present in the persisted answer map, including level 0. It does not count navigation or pending/failed saves, and changing an existing response does not increase the count. Finalization appears only on the last question after all questions are answered; otherwise a short notice directs the user back with Anterior. The existing completion API call/guard is unchanged.
- Touch layout: level options use three columns on narrow viewports and five from the `sm` breakpoint, with 44px minimum height and 14px text. Only the selected catalog description is shown. Same-question options are disabled while that answer is saving to avoid overlapping writes; failure leaves the saved selection/progress unchanged.
- Validation: `npm.cmd --prefix frontend test` passed (16 tests across 4 files); `npm.cmd --prefix frontend run build` passed; `npm.cmd --prefix frontend run lint` passed with the existing warning in `SessionContext.tsx:45`; `git diff --check` passed with line-ending notices only. No backend, API, database, matching or US3 files were changed. No browser viewport screenshot was taken; 375×812 and 1440×900 visual review remains pending.
- Next step: visually review the local questionnaire at mobile and desktop widths. No commit or push was made. Existing untracked `package-lock.local-backup.json` and `tatus` were preserved.

## Assessment responsive spacing adjustment

- Updated: 2026-11-22.
- Work: adjusted only the assessment response grid and content space reserved above the fixed bottom navigation. The existing level styles and labels remain unchanged; the grid uses two columns below 420px, three from 420px, and five at the existing desktop breakpoint. Assessment navigation stays in normal document flow, with flex spacing positioning it near the bottom when content fits and allowing page scrolling when it does not.
- Layout: added an opt-in compact bottom-navigation clearance to `AppShell`, used only by the assessment. It reserves the navigation height, a small gap and the device safe-area inset; the assessment's minimum height uses the matching viewport calculation. Other pages keep the existing `pb-24` spacing.
- Validation: frontend tests passed (16 tests across 4 files), build passed, lint passed with the existing `SessionContext.tsx:45` Fast Refresh warning, and `git diff --check` passed with line-ending conversion notices. No browser automation/preview was available in this session, so direct visual inspection at 375×812 and 1440×900 remains pending.
- No backend, API, database, matching or ranking changes; no commit or push. Existing uncommitted questionnaire UX changes and unrelated `package-lock.local-backup.json` and `tatus` were preserved.

## Assessment option overflow correction

- Updated: 2026-11-22.
- Work: removed the viewport `sm:grid-cols-5` rule because the assessment is capped at `max-w-md`; at desktop widths it forced five narrow columns into about 416px of content. The picker now stays at two columns below 420px and three columns above that. Wrapped each label in a shrinkable text span (`min-w-0`, normal wrapping and word breaks), retaining the existing button styles and 44px minimum height.
- Description: increased the existing gap from 12px to 16px and enabled wrapping for long descriptions without changing the vertical accent or typography.
- Validation: frontend tests passed (16 tests across 4 files), build passed, lint passed with the existing `SessionContext.tsx:45` Fast Refresh warning, and `git diff --check` passed with line-ending conversion notices. CSS breakpoints and wrapping behavior were checked in source/build output; no interactive browser was available for direct screenshots at 375×812 or 1440×900.
- No assessment navigation, persistence, completion, backend, API, database, matching or ranking changes. No commit or push; pre-existing uncommitted files were preserved.

## Assessment responsive balance adjustment

- Updated: 2026-11-22.
- Work: adjusted the five-level grid to two columns below 420px, a six-track layout at 420–639px that centers the final two equal-width options, and five columns from the desktop breakpoint. Labels retain wrapping and the 44px minimum height. Increased the mobile competency heading to 22px, response labels and selected description to 15px; the question remains 16px.
- Layout: moved the question content block down 8px. Increased the assessment-only bottom navigation clearance from 68px to 72px plus safe-area inset, with a matching minimum-height adjustment; navigation remains in document flow and scrollable content keeps its reserved clearance.
- Validation: frontend tests passed (16 tests across 4 files), build passed, lint passed with the existing `SessionContext.tsx:45` Fast Refresh warning, and `git diff --check` passed with line-ending conversion notices. Build CSS confirms the 420px and 640px breakpoints and centered second-row placement. No browser automation was available; direct visual inspection at 375×812, 430×932 and 1440×900 remains pending.
- Functional behavior and all pre-existing uncommitted files were preserved. No backend, API, database, matching or ranking changes; no commit or push.

## Assessment answer-before-advance validation

- Updated: 2026-11-22.
- Work: disabled Next unless the active competency is present in the persisted answer map and has no pending or failed save. Presence checks treat level 0 as answered. A locally selected level remains visible during a save and after a failure so the user can retry; previous navigation and persisted answers remain available. Finalization also waits for all 31 persisted responses and no in-flight or failed save.
- Tests: added coverage for an unanswered question, saved levels 0–4, pending/failed saves, retained selection after failure, advancing from a previously answered question, and incomplete finalization.
- Validation: `npm.cmd --prefix frontend test` passed (21 tests across 4 files); frontend lint passed with the existing `SessionContext.tsx:45` Fast Refresh warning; frontend build passed; `git diff --check` passed with line-ending conversion notices. An initial build caught a test-helper TypeScript typing issue; it was corrected and the final build passed.
- No backend/API/database changes, no commit or push. Existing local changes and unrelated files were preserved.

## Area-scoped assessment progress

- Updated: 2026-11-22.
- Work: `AssessmentProgress` now filters questions to the active area's approved position range, counts persisted answers in that range (including level 0), and computes the bar percentage and visible/accessible count against that area's total. The area name and answer map come from the existing question state; navigation and persistence are unchanged.
- Tests: verify area-specific totals and counts, reset on area change, restoration when returning to a previous area, and 100% when all questions in an area are answered.
- Validation: frontend tests passed (21 tests across 4 files); frontend lint and build passed. `git diff --check` on the changed assessment files passed. The global check still reports a trailing blank line in the pre-existing modified `docker-compose.yml`, which was left untouched.
- No backend, matching, API or persistence changes; no commit or push.

## Role results presentation hierarchy

- Updated: 2026-11-22.
- Work: restyled the first persisted rank as the larger “Mayor coincidencia” card, ranks 2–3 as responsive side-by-side alternatives when space permits, and ranks 4–6 inside the native “Ver otros roles” disclosure. All six detail links retain the same route and the preparation/non-certification clarification remains visible. The top role is selected from persisted `rankPosition` (no fixed role ID and no client sorting by rounded percentage), preserving the assessment API contract.
- Validation: `npm.cmd --prefix frontend test` passed (22 tests across 4 files); lint passed with the existing `SessionContext.tsx:45` Fast Refresh warning; build passed; scoped `git diff --check` passed with line-ending notices.
- No backend, matching, API, database or ranking changes. No commit or push; existing unrelated local changes were preserved.

## Assessment completion, reevaluation and history recovery

- Updated: 2026-11-25.
- Recovery: continued from the existing uncommitted changes after reviewing `git status`, `git diff`, the approved specification/plan/tasks and the frontend/backend contracts. Preserved the existing questionnaire UX, home/skills access panel work, completion screen, history endpoint, results presentation, and unrelated untracked `package-lock.local-backup.json` and `tatus`.
- UI: the area progress bar has no visible percentage or numeric counter; the “Mayor coincidencia” label is absent from role results. Home and Habilidades now expose resume/new evaluation, previous results and history links. Added completed-only history and historical detail views; detail reads stored scores, pinned versions and answers. Draft discard requires explicit confirmation and creates a separate reevaluation while retaining the discarded attempt.
- Backend: added authenticated reevaluation and discard routes. Reevaluation recovers one active draft or creates a new version-pinned draft and copies answers from the latest completed attempt by stable competency code. Discard transitions the draft to `DISCARDED` without deleting it. Completed history stays newest first and excludes drafts; the existing attempt detail returns the stored historical results and pinned questionnaire content. No changes to matching calculations or the 31-question catalog.
- Tests: added API contract, Testcontainers lifecycle, UI and full-workflow coverage. `npm.cmd --prefix frontend test` passed (7 files, 33 tests); `npm.cmd --prefix frontend run lint` passed with the existing `SessionContext.tsx:45` Fast Refresh warning; `npm.cmd --prefix frontend run build` passed. Backend `mvn -f backend/pom.xml -Dtest=AssessmentAttemptServiceTest,MatchingEngineV2Test,ScoringTest,GapPolicyTest test` passed (20 tests), and `mvn -f backend/pom.xml -DskipTests test` compiled all 12 test sources. The final full `mvn -f backend/pom.xml test` ran 28 tests: 20 unit tests passed, while eight Testcontainers-backed classes could not start because Docker is unavailable. Manual quickstart/browser scenarios were not run because the backend test database/runtime was unavailable.
- `git diff --check` reports the trailing blank line at EOF in the already-modified `docker-compose.yml`; it was preserved. The branch is still one commit behind `origin/develop-v2`. No commit, push, reset, restore, clean, or database mutation was performed.
- Next: rerun `mvn -f backend/pom.xml test` and the authenticated quickstart scenarios in an environment with Docker available; resolve the pre-existing Compose whitespace only if requested.

## US3 validation and GitHub sync checkpoint

- Updated: 2026-11-25.
- Docker: `docker version`/`docker info` confirmed Docker Desktop 28.5.1. The sandbox could not access its named pipe, but elevated Testcontainers execution connected successfully. The complete `mvn -f backend/pom.xml test` passed: 29 tests, 0 failures/errors/skips. This includes Flyway V1–V5 on disposable PostgreSQL 16 and tests for answer prefill, source immutability, draft discard, completed-only history, newest-first ordering and the end-to-end lifecycle.
- Frontend: `npm.cmd --prefix frontend test` passed (7 files, 33 tests); lint passed with the existing `SessionContext.tsx:45` warning; production build passed. These checks were rerun after the final UI error-state adjustment.
- Manual validation: authenticated browser walkthrough and desktop/mobile visual review have not been run. T033 and dependent T034 remain unchecked; they are not reported as complete.
- Git: fetched `origin/develop-v2`. Its only pending commit is `7927a90 Agrego pasos para ejecutar v2`, a README-only change with no overlapping local paths. No credentials/private configuration were found among feature changes. `package-lock.local-backup.json` and `tatus` remain untracked and excluded. The tracked `docker-compose.yml` edit changes the host binding to `0.0.0.0`; it is outside this feature and will be preserved locally but excluded from commits. Removed the extra leading/trailing blank lines called out by `git diff --check`.
- Sync: rebased the five functional commits onto `7927a90 Agrego pasos para ejecutar v2` without conflicts. Post-integration validation passed again: backend `mvn -f backend/pom.xml test` (29 tests, 0 failures/errors/skips), frontend tests (33 tests), lint, and build. Docker Desktop/Testcontainers executed against disposable PostgreSQL 16 databases and applied migrations V1–V5.
- Manual validation: authenticated browser walkthrough and desktop/mobile visual review remain unrun; T033 and dependent T034 remain unchecked. Automated validation does not change that status.
- Push: the five functional commits were pushed to `origin/develop-v2`; a subsequent fetch confirmed `HEAD` and `origin/develop-v2` both at `e1a888d74ebd32d9889c27237b7ad96bbb84ae08` (0 ahead, 0 behind). `git diff --check` is clean after removing the extra Compose blank lines.
- Preserved outside the commits: the local `docker-compose.yml` host-binding change to `0.0.0.0`, `package-lock.local-backup.json`, and `tatus`.
- Remaining: authenticated manual browser walkthrough and desktop/mobile visual review were not run. Keep T033 and dependent T034 unchecked until those scenarios are performed.

## Cierre de validación manual y ajuste de descarte

- Updated: 2026-11-25.
- UI: se quitó el botón permanente de descarte de `SkillAssessmentPage`. La acción secundaria `Descartar borrador` aparece solo desde Habilidades si hay un borrador, solicita confirmación y conserva el comportamiento de descarte US3 y creación de reevaluación. Se agregaron pruebas para la ausencia en preguntas, la ocultación por defecto (Inicio) y la confirmación desde Habilidades. Navegación de preguntas, autoguardado, persistencia y reglas de reevaluación no cambiaron.
- Validación técnica: Docker Desktop 28.5.1 disponible; `mvn -f backend/pom.xml test` pasó (29 pruebas, 0 fallos/errores/omitidas), con Testcontainers/PostgreSQL 16 y migraciones V1–V5. `npm.cmd --prefix frontend test` pasó (7 archivos, 35 pruebas); `npm.cmd --prefix frontend run lint` pasó con la advertencia existente `SessionContext.tsx:45`; `npm.cmd --prefix frontend run build` pasó. Un primer intento de la suite frontend en paralelo con build/lint tuvo un timeout en un test no relacionado; el test aislado y la suite completa secuencial pasaron.
- Validación manual: el usuario reporta satisfactoria la validación manual de NextRole V2, incluyendo el acceso móvil. No se atribuyen resultados individuales a los nueve escenarios del quickstart, ya que no se detallaron uno por uno.
- Spec Kit: T026 ahora apunta a las pruebas del panel de acceso donde reside el descarte. T033 y T034 quedaron completadas con el resultado global reportado; las comprobaciones individuales del quickstart no quedan certificadas en este registro. No hubo cambios en `MatchingEngineV2`, ponderaciones ni catálogo de 31 competencias.
- Próxima etapa: implementar el módulo de explicabilidad mediante IA. Después de esa etapa se planificará por separado una mejora amplia de experiencia de usuario, identidad visual y presentación de información para posicionar NextRole como plataforma de desarrollo profesional. Ninguna de esas etapas se inició.
- Pendiente: no se ejecutaron de nuevo escenarios manuales individuales del quickstart; no se agregan afirmaciones sobre sus resultados específicos.

## Unificación del historial y resultados V2

- Updated: 2026-11-25.
- Se eliminó el enlace separado a resultados anteriores de la tarjeta de autoevaluación y se retiraron del historial las acciones de continuar/iniciar una evaluación. El historial queda dedicado a evaluaciones completas y sus enlaces “Ver detalle”.
- Se extrajo el ranking visual de Roles a un componente compartido y se reutilizó en el detalle histórico. El detalle muestra la fecha y las versiones del intento seleccionado; las respuestas originales quedan bajo “Ver respuestas de esta evaluación”. Los enlaces de exploración conservan el mismo `attemptId`; el detalle histórico de rol no consulta matching ni brechas de la evaluación actual.
- Validación: `npm.cmd --prefix frontend test` pasó (9 archivos, 39 pruebas); `npm.cmd --prefix frontend run build` pasó; `npm.cmd --prefix frontend run lint` pasó con la advertencia preexistente `SessionContext.tsx:45`; `git diff --check` pasó. No se hizo inspección manual de navegador en esta sesión.
- No se modificaron matching, competencias, ponderaciones, persistencia ni base de datos. No se realizaron commits ni push.

## Recorrido animado de autoevaluación

- Updated: 2026-11-25.
- Spec Kit: se amplió la feature existente con US4, criterios FR-034–FR-039 y SC-015–SC-019. `plan.md` define un componente visual reutilizable y sin dependencia de la estructura del cuestionario; `tasks.md` registra T035–T041. La revisión cruzada confirmó el alcance acotado: un único camino continuo, sin checkpoints ni segunda barra, dentro de la tarjeta existente. No se requieren cambios de backend, API, modelo de datos ni migraciones.
- Implementación: `AssessmentJourney` recibe progreso normalizado y confirmación de finalización. `AssessmentAccessPanel` usa únicamente sus consultas actuales: borrador activo (respuestas guardadas / total), historial completado sin borrador (100%) o ausencia de ambos (0%). El nivel 0 cuenta mediante el conteo persistido del intento. Se agregaron el camino SVG responsive, el spritesheet, poses de reposo/señalamiento y celebración, porcentaje único, progreso accesible y soporte para `prefers-reduced-motion`. Los parámetros de cadencia y duración están centralizados; el desplazamiento es finito y no excede 1.76 segundos.
- Pruebas: `npm.cmd --prefix frontend test` pasó (10 archivos, 49 pruebas); `npm.cmd --prefix frontend run build` pasó; `npm.cmd --prefix frontend run lint` pasó con la advertencia preexistente de `SessionContext.tsx:45`. Tests de UI cubren 0%, ratio del borrador con nivel 0, 100% completado, borrador al 100% sin celebración, precedencia del borrador, movimiento reducido y sincronización/fin del recorrido.
- Aplicación: Docker Compose ya tenía backend, frontend y PostgreSQL saludables. `http://127.0.0.1:5173` y los módulos del recorrido respondieron HTTP 200; el frontend está publicado en `0.0.0.0:5173` para acceso LAN. No se reinició ni modificó Compose.
- Validación visual manual: el usuario confirmó que pudo visualizar la tarjeta del recorrido en la aplicación. Esta observación confirma que la tarjeta se muestra, pero no valida todos sus estados, la sincronización, el ritmo, el movimiento reducido ni la adaptación en móvil y escritorio. T040 sigue pendiente para esas comprobaciones. La aplicación queda disponible en `http://localhost:5173` (desde el celular, usar la IP local de esta computadora y el puerto 5173).
- No se modificaron el resto de Inicio, las acciones del panel, el descarte, navegación, historial, resultados, backend, MatchingEngineV2, competencias ni ponderaciones. No hubo commits ni push. Se preservaron `docker-compose.yml`, `package-lock.local-backup.json`, `tatus` y los recursos gráficos originales.

## Inicio por estado, descarte consistente y limpieza puntual de Skill Gap

- Actualizado: 2026-11-25.
- Specify/Clarify/Plan/Tasks/Analyze: se extendió la feature `001-autoevaluacion-matching` con US5 (Inicio), US6 (descarte), US7 (indicadores antiguos de Skill Gap), criterios de aceptación y escenarios manuales. El análisis cruzado no dejó decisiones funcionales bloqueantes. Se conservaron T040 y T049 pendientes porque la revisión visual de recorrido e Inicio en escritorio/móvil todavía no fue realizada.
- Implementación de Inicio: `HomePage` obtiene el borrador activo y el historial mediante las consultas y claves existentes. El recorrido aparece solo con un borrador; sin borrador se muestra la tarjeta motivacional nueva/recurrente y la tarjeta de última evaluación solo cuando hay historial. La última tarjeta usa fecha y resultado rank 1 persistidos, conserva `/roles?attemptId=…` y `/habilidades/historial`, sin recalcular resultados. Se quitó el saludo duplicado y la ruta de orientación antigua. No se agregó el botón inferior futuro.
- Descarte: se creó `useDiscardAssessmentAttempt`, compartido por Inicio y Habilidades. Solo actualiza el caché del borrador a `null` tras éxito del backend; errores mantienen visible el borrador. Ambos flujos piden confirmación, conservan resultados históricos, permanecen en su pantalla de origen y no crean una evaluación nueva automáticamente. El inicio explícito desde Habilidades sigue disponible.
- Skill Gap: se quitó únicamente el rótulo «PASO 5 DE 6» y su tira de seis segmentos. Se conservaron título, diagnóstico, barras por competencia, estado, solicitudes, navegación y acceso a Formación. Los pasos de registro/onboarding y el progreso propio de autoevaluación no se modificaron.
- Archivos de aplicación modificados: `frontend/src/features/home/HomePage.tsx`, `frontend/src/features/home/HomePage.css`, `frontend/src/features/home/HomePage.test.tsx`, `frontend/src/features/assessment/AssessmentAccessPanel.tsx`, `frontend/src/features/assessment/AssessmentAccessPanel.test.tsx`, `frontend/src/features/assessment/useDiscardAssessmentAttempt.ts`, `frontend/src/features/skill-gap/SkillGapPage.tsx`, `frontend/src/features/skill-gap/SkillGapPage.test.tsx`. Artefactos actualizados: `specs/001-autoevaluacion-matching/spec.md`, `plan.md`, `tasks.md`, `quickstart.md` y este checkpoint.
- Validación técnica: `npm.cmd --prefix frontend test` pasó (12 archivos, 61 pruebas); `npm.cmd --prefix frontend run build` pasó; `npm.cmd --prefix frontend run lint` terminó con la advertencia preexistente `SessionContext.tsx:45`; `git diff --check` pasó con avisos informativos de conversión LF/CRLF. No se ejecutó backend suite porque el alcance no cambió backend, contratos ni base de datos.
- Aplicación disponible: Docker Compose reporta PostgreSQL, backend y frontend `healthy`. `http://localhost:5173/` respondió HTTP 200; `http://localhost:8090/actuator/health` respondió HTTP 200. El módulo servido `http://localhost:5173/src/features/home/HomePage.tsx` respondió HTTP 200 e incluye `AssessmentJourney`, `assessment-history` y `useDiscardAssessmentAttempt`, sin el cálculo viejo de orientación. No se reiniciaron contenedores: el frontend monta `./frontend` y ya está sirviendo el código actualizado; no hizo falta modificar Compose ni reiniciar PostgreSQL/backend.
- Validación manual pendiente: revisión del nuevo Inicio en anchos móvil/escritorio (T049), así como revisión completa de estados y animación del recorrido (T040). También quedan pendientes los escenarios manuales de descarte, finalización y estados de error indicados en `quickstart.md`. Las pruebas automatizadas no se interpretan como validación manual.
- Git: no se hizo commit ni push. Se preservaron sin cambios `docker-compose.yml`, `package-lock.local-backup.json` y `tatus`.

## Correcciones visuales y funcionales previas al checkpoint de Inicio

- Actualizado: 2026-11-26.
- SDD: se amplió `001-autoevaluacion-matching` con la nueva decisión de reevaluaciones vacías, estados de Inicio/Habilidades, modal compartido, resultados directos en Roles y estado de entrada de Formación. Se actualizaron `spec.md`, `plan.md`, `tasks.md`, `quickstart.md`, `research.md`, `data-model.md`, `contracts/assessment-api.md` y checklist. El análisis cruzado cerró las contradicciones detectadas (visibilidad del recorrido en Habilidades y celebración al completar); no quedaron decisiones funcionales pendientes.
- Reevaluaciones: los intentos nuevos ya no copian respuestas. `source_attempt_id` permanece como procedencia; no se modificó esquema, migraciones ni información almacenada. La recuperación de un borrador sigue devolviendo solo las respuestas guardadas en él.
- Inicio/Habilidades: tarjeta azul con contenido y contraste explícitos, mensajes motivacionales deterministas y sin porcentaje numérico visible; el porcentaje accesible y el progreso interno permanecen. Habilidades muestra el recorrido solo con borrador y la mascota estática con acción explícita cuando no lo hay. Descarte compartido en modal centrado, con Escape, foco contenido, cancelación, error y protección contra envíos duplicados.
- Roles/Formación: `/roles` muestra el ranking persistido más reciente al abrir directamente, incluso con otro borrador; `attemptId` mantiene el intento histórico seleccionado. Los estados vacíos orientan a Habilidades/Roles sin rankings ficticios ni obligación de rol objetivo. La vista existente `/roles/:roleId/formacion` no fue modificada.
- Pruebas: backend `mvn -f backend/pom.xml test` pasó (29 tests, 0 fallos/errores/omitidos), con Testcontainers y PostgreSQL 16. Frontend `npm.cmd --prefix frontend test` pasó (13 archivos, 74 tests). `npm.cmd --prefix frontend run build` pasó. `npm.cmd --prefix frontend run lint` pasó con una advertencia preexistente en `SessionContext.tsx:45`. `git diff --check` no encontró whitespace inválido; solo informó conversiones LF/CRLF.
- Docker y disponibilidad: Docker Desktop 28.5.1 disponible. PostgreSQL, backend y frontend están saludables; reconstruí/reinicié solo backend para incluir la nueva lógica y reinicié solo Vite porque servía un módulo anterior en caché. PostgreSQL y su volumen no se recrearon. `http://localhost:5173/`, el módulo actual `HomePage.tsx` y `http://localhost:8090/actuator/health` responden HTTP 200. La fuente servida confirma el título actual, importa el modal nuevo y ya no contiene la referencia al CSS obsoleto ni al porcentaje visual.
- Validación manual reportada por el usuario: funcionan Inicio sin borrador, Inicio con historial sin borrador, descarte efectivo y conservación de evaluaciones anteriores, además de otros comportamientos revisados. Esta observación no es una revisión exhaustiva de estados, animación o tamaños de pantalla.
- Validación manual pendiente: T040 (recorrido completo en escritorio/móvil, animación, escala, accesibilidad y movimiento reducido) y T049 (revisión visual completa de Inicio, Habilidades, Roles y Formación en escritorio/móvil). También queda completar los escenarios manuales del quickstart no indicados expresamente como comprobados. Se mantienen sin marcar.
- Archivos locales preservados: `docker-compose.yml` (no editado por esta ampliación), `package-lock.local-backup.json` y `tatus`. Sin commit ni push.

## Aislamiento del contexto de evaluación en Roles y Formación

- Actualizado: 2026-11-26.
- Spec Kit: se ampliaron los escenarios y requisitos de US9 para distinguir el ranking de la última evaluación completada del borrador activo. Roles conserva el ranking persistido y muestra su contexto, pero oculta exploración mientras haya borrador. Formación prioriza el borrador y no reutiliza contenido de intentos anteriores. Se documentaron el retorno posterior al descarte y la continuidad explícita de `attemptId`; no cambian contratos ni backend.
- Implementación: `AssessmentResultsRanking` permite ocultar acciones de exploración sin alterar la presentación ni los valores persistidos. Roles aplica esa opción solo a la entrada directa con borrador; las rutas explícitas históricas conservan sus enlaces. Formación muestra el texto aprobado y «Continuar evaluación» con borrador, aun si hay historial; sin borrador, el flujo previo de invitar a explorar Roles se conserva cuando existe historial. El estado se deriva de las claves React Query existentes.
- Pruebas: `npm.cmd --prefix frontend test -- src/features/roles/RolesSuggestedPage.test.tsx src/features/learning/LearningIndexPage.test.tsx` pasó (2 archivos, 14 pruebas). `npm.cmd --prefix frontend run build` pasó. `npm.cmd --prefix frontend run lint` pasó con la advertencia preexistente de Fast Refresh en `SessionContext.tsx:45`. `git diff --check` pasó; Git solo notificó conversiones de fin de línea LF/CRLF.
- Consistencia SDD: FR-054/FR-055/FR-059, escenarios US9, decisiones, SC-031–SC-033, plan, tareas y quickstart documentan el mismo orden de precedencia (borrador en curso, ranking completado solo como referencia en Roles, sin contexto previo en Formación, navegación histórica fijada por `attemptId`, y exploración reactivada tras descarte). T064–T068 quedaron completadas. No se actualizaron contratos ni modelo de datos porque no cambian.
- Vite: servicio `next-role-frontend-1` saludable después de reiniciar únicamente frontend. `http://localhost:5173/` y los módulos transformados de Roles y Formación respondieron HTTP 200; la salida servida contiene la condición `showExploreActions: !hasDraft` y la rama de precedencia `hasDraft || !history.data?.length`. No se reconstruyó ni reinició backend o PostgreSQL.
- Navegación/recarga: las pruebas confirman la ruta directa `/roles`, el estado al actualizar la caché tras descarte y el fetch del `attemptId` histórico; queda pendiente la revisión manual de pestañas y resultados históricos en navegador. No se realizó una validación visual de este ajuste.
- Git/entorno: se preservan cambios locales preexistentes, incluido `docker-compose.yml`; no se realizaron commits ni push.

## Publicación del cuestionario Q2 sobre matriz M1

- Actualizado: 2026-11-26. Rama `develop-v2`, checkpoint de base `589da77`. Se retomó una ejecución interrumpida: `spec.md` y `plan.md` ya contenían la aclaración Q2 y el addendum. Se corrigió la numeración duplicada de criterios de éxito y se completaron las tareas SDD sin repetir los cambios parciales.
- Decisión vigente: Q2 es la versión actual del cuestionario; Q1 queda inmutable. Se conserva el código, la pregunta, posición y niveles de las 31 competencias; exactamente diez textos se actualizan y las otras 145 descripciones se copian directamente de Q1. M1 permanece única, vigente e intacta. No se alteraron Matching ni Skill Gap productivos.
- Redacción definitiva de Desarrollo de APIs REST nivel 1: «Comprendo cómo funciona una API REST desde la perspectiva de su desarrollo y qué función cumplen los endpoints, los métodos HTTP y las respuestas JSON.» Se incorporaron las otras nueve redacciones aprobadas sin cambio.
- Implementación: `backend/src/main/resources/db/migration/V6__publish_questionnaire_q2.sql` comprueba Q1 (31 preguntas/155 opciones) y M1 (186 requisitos), crea Q2 con UUID `00000000-0000-0000-0000-000000000003`, copia el catálogo, cambia solo las diez descripciones y mueve el indicador `is_current`. No se editaron V1–V5 ni se añadieron tablas/endpoints.
- Verificación de catálogo: `AssessmentMigrationIntegrationTest` confirma Q1/Q2 (31 preguntas y 155 opciones cada una), las diez diferencias exactas, los otros 145 textos idénticos, Q2 como única versión actual y M1 con 186 requisitos actuales.
- Compatibilidad histórica: `AssessmentQuestionnaireQ2MigrationIntegrationTest` ejecuta V1–V5 en PostgreSQL Testcontainers, inserta un borrador Q1 con respuestas y una evaluación Q1 completa con 31 respuestas y seis resultados persistidos, luego aplica V6. Confirma que las versiones fijadas, respuestas, descripciones Q1, resultados y M1 no cambian. `AssessmentHistoryIntegrationTest` recupera, guarda y completa un draft Q1 con Q2 actual; crea un intento Q2/M1; compara los resultados de Matching por rol y las brechas de Skill Gap por competencia para el mismo mapa de niveles.
- Documentación: actualizaron el catálogo normativo Q2/M1, contrato API, modelo de datos, quickstart y fase 17 de la especificación 001. T069–T074 están completadas. No se modificó la especificación 002.
- Validaciones: pruebas dirigidas pasaron (12 pruebas, 0 fallos/errores). Suite completa `mvn -f backend/pom.xml test` pasó (40 pruebas, 0 fallos/errores/omitidas). `git diff --check` pasó. Se usaron solo bases desechables Testcontainers; ninguna DB persistente fue migrada. No hubo solicitudes a IA.
- Estado: revisión del usuario pendiente. No se realizaron commits ni push. Se preservaron `docker-compose.yml`, `experiments/`, `package-lock.local-backup.json` y `tatus` preexistentes.

## Aplicación de Q2 en la base local y preparación del checkpoint

- Actualizado: 2026-11-26. Se verificó la base local antes del arranque: Flyway V5, Q1/M1 actuales, 30 intentos (17 completados, 8 borradores, 5 descartados), 693 respuestas, 17 conjuntos de resultados y 102 resultados por rol.
- Se ejecutó únicamente `docker compose up -d --build backend`; Flyway aplicó V6 sin recrear el volumen PostgreSQL. El backend quedó saludable y `/actuator/health` respondió `{"status":"UP"}`.
- Después de V6: Q1 dejó de ser actual pero conserva 31 preguntas/155 opciones; Q2 es la única actual con 31/155; M1 continúa actual con 186 requisitos. Los conteos de intentos, respuestas, conjuntos de resultados y resultados por rol permanecieron en 30/693/17/102. No hay intentos sin versión ni resultados huérfanos. La comparación viva Q1/Q2 encontró exactamente diez opciones distintas; las pruebas de migración verifican que las otras 145 son idénticas.
- Las pruebas dirigidas `mvn -f backend/pom.xml '-Dtest=AssessmentQuestionnaireQ2MigrationIntegrationTest,AssessmentHistoryIntegrationTest,AssessmentAttemptServiceTest,AssessmentDraftApiContractTest' test` pasaron: 11 pruebas, 0 fallos/errores/omitidas. Usaron PostgreSQL desechable de Testcontainers. El endpoint autenticado de catálogo devolvió 401 al llamarlo sin credenciales, como corresponde a su protección; las pruebas de integración cubren el contrato y los flujos autenticados de intentos/versiones.
- Checkpoint aprobado por el usuario: solo los 15 archivos listados para Q2/M1; mensaje `Publica cuestionario Q2 y preserva versiones históricas`. Se excluyen los cambios locales preexistentes y `experiments/`. No se realizó push.
