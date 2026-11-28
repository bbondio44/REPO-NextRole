---

description: "Implementation tasks for Explainability and role-specific Formation"
---

# Tasks: Explainability y Formación por rol

**Input**: Design documents from `specs/003-explainability-formacion/`

**Prerequisites**: `spec.md`, `plan.md`, `research.md`, `data-model.md`, `contracts/explainability-api.md`, `quickstart.md`

**Organization**: Tasks are grouped by the approved user stories. Tests are included because the specification and project harness require automated validation.

## Phase 1: Setup

**Purpose**: Confirm the additive migration slot and stable test inputs before implementation.

- [x] T001 Confirm the applied Flyway chain ends at V6 and V7 is unused using `backend/src/main/resources/db/migration/`; if this precondition has changed, stop before creating a migration and update the plan.
- [x] T002 Record the approved experimental prompt SHA-256 and input-contract reference from `experiments/explainability/prompts/explainability-v5.md` and `experiments/explainability/src/explainability/models.py` in the implementation test setup under `backend/src/test/resources/contracts/`; do not change experiment files or prompt content.

---

## Phase 2: Foundational (blocking prerequisites)

**Purpose**: Add the versioned role descriptions, canonical persisted state, and minimal secure prompt/provider configuration used by all stories.

- [x] T003 First add a failing disposable-PostgreSQL migration assertion in `backend/src/test/java/com/nextrole/assessment/ExplainabilityMigrationIntegrationTest.java`, then add the additive `backend/src/main/resources/db/migration/V7__explainability.sql` migration for official V2 role names/descriptions and `explainability_diagnostics`; keep V1–V6 immutable, preserve assessment rows, and implement every canonical field/constraint defined in `data-model.md`: `AssessmentRole.description`; `ExplainabilityDiagnostic.id`, `ExplainabilityDiagnostic.attempt_id`, `ExplainabilityDiagnostic.role_id`, `ExplainabilityDiagnostic.status`, `ExplainabilityDiagnostic.generation_token`, `ExplainabilityDiagnostic.lease_until`, `ExplainabilityDiagnostic.attempt_count`, `ExplainabilityDiagnostic.role_name_snapshot`, `ExplainabilityDiagnostic.role_description_snapshot`, `ExplainabilityDiagnostic.diagnosis_content`, `ExplainabilityDiagnostic.provider`, `ExplainabilityDiagnostic.model_requested`, `ExplainabilityDiagnostic.model_served`, `ExplainabilityDiagnostic.prompt_version`, `ExplainabilityDiagnostic.prompt_hash`, `ExplainabilityDiagnostic.context_hash`, `ExplainabilityDiagnostic.duration_ms`, `ExplainabilityDiagnostic.input_tokens`, `ExplainabilityDiagnostic.output_tokens`, `ExplainabilityDiagnostic.reasoning_tokens`, `ExplainabilityDiagnostic.provider_cost`, `ExplainabilityDiagnostic.failure_code`, `ExplainabilityDiagnostic.created_at`, `ExplainabilityDiagnostic.updated_at`, `ExplainabilityDiagnostic.succeeded_at`.
- [x] T004 First add failing mapping/repository tests in `backend/src/test/java/com/nextrole/explainability/ExplainabilityDiagnosticRepositoryTest.java`, then add role-description and diagnosis persistence mappings/repository under `backend/src/main/java/com/nextrole/assessment/` and `backend/src/main/java/com/nextrole/explainability/`; follow the field constraints and state invariants for `AssessmentRole.description` and each `ExplainabilityDiagnostic.*` field defined in `specs/003-explainability-formacion/data-model.md`.
- [x] T005 First add failing catalog tests in `backend/src/test/java/com/nextrole/assessment/RoleCatalogServiceTest.java` and `RoleCatalogApiIntegrationTest.java`, then add the centralized V2 role catalog service and authenticated read endpoint under `backend/src/main/java/com/nextrole/assessment/`; expose the six approved names/descriptions without changing the V1 `roles` catalog or its consumers.
- [x] T006 First add a failing hash/configuration test in `backend/src/test/java/com/nextrole/explainability/ExplainabilityPromptConfigurationTest.java`, then add the typed Explainability configuration and exact V5 classpath resource at `backend/src/main/resources/prompts/explainability-v5.md`; load only configured known prompt versions, obtain `OPENROUTER_API_KEY` from backend configuration, disable provider fallbacks, and test the SHA-256 against `experiments/explainability/prompts/explainability-v5.md`.
- [x] T007 Add repository integration tests in `backend/src/test/java/com/nextrole/explainability/` for the unique `(attempt_id, role_id)` row, allowed status transitions, success immutability, lease/token compare-and-set, and successful-role snapshots; run them against disposable PostgreSQL/Testcontainers without operating on development data.
- [x] T036 Extend `backend/src/test/java/com/nextrole/assessment/ExplainabilityMigrationIntegrationTest.java` with seeded Q1/Q2/M1 attempts, answers and matching results; prove V7 preserves those existing records in addition to the schema/catalog checks introduced by T003. This migration-history check is part of the foundational checkpoint and must run before V7 is applied to any shared development database.

**Checkpoint**: V7 and persistence foundations are additive, role descriptions are single-source in the backend, and the V5 resource is byte-identical to the experiment.

---

## Phase 3: User Story 1 — Consultar el diagnóstico de un rol (Priority: P1; MVP)

**Goal**: Generate on first access from deterministic attempt/matching/Skill Gap data, validate and persist a reusable diagnosis, and show the V2 role-specific Formation screen.

**Independent Test**: For an owned completed attempt and one role with no prior row, verify the exact persisted Matching percentage, relevant versioned competency context and priority, one fake-provider call, a valid saved diagnosis and the four sections; a second GET returns the same result without calling the fake again.

### Tests

- [x] T008 [P] [US1] Add context/priority unit tests in `backend/src/test/java/com/nextrole/explainability/ExplainabilityContextServiceTest.java` for essential/complementary weights, covered skills, a no-gap profile, excluded non-relevant skills, Q1/Q2 descriptors, persisted percentage, and the exact V5 property names/types.
- [x] T009 [P] [US1] Add contract compatibility fixtures and tests in `backend/src/test/resources/contracts/explainability-v5-context/` and `backend/src/test/java/com/nextrole/explainability/ExplainabilityContextContractTest.java`; compare the Java serialized context with the Python `Context` contract in `experiments/explainability/src/explainability/models.py`, including IDs, role name/description, numeric matching percentage, current/required level and description, classification/importance mapping, gap and `trainingPriority`, while excluding local fixture/attempt/questionnaire/matrix metadata.
- [x] T010 [P] [US1] Add service/controller contract tests in `backend/src/test/java/com/nextrole/explainability/ExplainabilityServiceTest.java`, `ExplainabilityServiceIntegrationTest.java` and `ExplainabilityApiIntegrationTest.java` for initial generation, strict diagnosis schema, highlighted competency membership, persisted-success reuse, authenticated ownership and no provider call on GET. The service and HTTP assertions, including GET purity, are covered by the unit, PostgreSQL integration and API integration tests.
- [ ] T011 [P] [US1] Add frontend journey tests in `frontend/src/features/explainability/RoleFormationPage.test.tsx` and `frontend/src/features/roles/AssessmentResultsRanking.test.tsx` for current/historical ranking access, exact attempt/role preservation, return target, active Formation navigation, deterministic header during generation, successful diagnosis rendering and no duplicate generation on remount; first confirm the unimplemented journey assertions fail.

### Implementation

- [x] T012 [US1] Implement the deterministic context projection in `backend/src/main/java/com/nextrole/explainability/ExplainabilityContextService.java`, reusing `AssessmentAttemptService`, persisted Matching results and `SkillGapV2Service` directly; associate historical answer-option descriptions and role requirements by stable competency ID and compute only `trainingPriority = max(0, requiredLevel - currentLevel) × (ESSENTIAL ? 2 : 1)`.
- [x] T013 [US1] Implement the typed context/competency DTOs and canonical JSON serialization in `backend/src/main/java/com/nextrole/explainability/ExplainabilityContext.java` and `ExplainabilityCompetencyContext.java`; make serialization satisfy the compatibility contract tests without exposing attempt/user/version metadata.
- [x] T014 [US1] Implement the provider boundary `DiagnosisGenerator` and result/usage types in `backend/src/main/java/com/nextrole/explainability/DiagnosisGenerator.java`; include requested/served model and nullable provider usage without leaking transport details into business services. Immediately afterward, add the basic valid-response `FakeDiagnosisGenerator` in the test support package before integration tests depend on it.
- [x] T015 [US1] Implement strict diagnosis parsing/validation in `backend/src/main/java/com/nextrole/explainability/DiagnosisValidator.java`; require exactly the five contract properties, four non-empty strings, a list of context competency IDs only, and reject additional, missing, repeated or unknown identifiers before persistence.
- [x] T016 [US1] First add mock-server request/error tests in `backend/src/test/java/com/nextrole/explainability/OpenRouterDiagnosisGeneratorTest.java`, then implement `OpenRouterDiagnosisGenerator` with Spring `RestClient` in `backend/src/main/java/com/nextrole/explainability/OpenRouterDiagnosisGenerator.java`; use configured endpoint/model, bearer secret, exact V5 prompt, no `models` alternatives, no paid fallback, no automatic retry, connection/response timeouts and configurable 1200-token output cap.
- [x] T017 [US1] Implement the synchronous initial-generation orchestration in `backend/src/main/java/com/nextrole/explainability/ExplainabilityService.java` and repository claim operation; create only from no prior row, perform provider I/O outside DB transactions, validate before success, persist operational metadata and role snapshots, and return existing success without regeneration.
- [x] T018 [US1] Implement authenticated diagnosis DTOs and endpoints in `backend/src/main/java/com/nextrole/explainability/ExplainabilityController.java` per `contracts/explainability-api.md`; GET remains read-only, unauthorized/non-owned attempts do not disclose ownership, and the successful response exposes `roleName`/`roleDescription` as siblings of the unchanged `diagnosis` object. Use the role catalog endpoint delivered by T005.
- [ ] T019 [US1] Add the frontend API/types and V2 route in `frontend/src/services/api.ts`, `frontend/src/types/domain.ts` and `frontend/src/App.tsx`; accept `roleId` plus `attemptId`, return to `/roles?attemptId=...`, keep V1 routes and `/formacion` intact, and disable automatic mutation retries/refetch-on-focus for generation.
- [ ] T020 [US1] Implement deterministic role header, loading state, mascot and success rendering in `frontend/src/features/explainability/` using the existing shell/components; render role name/description, junior level, persisted percentage, evaluation date, non-certification clarification, all four diagnosis fields and `pointing` mascot while keeping the header and future-plan card independent of AI response time.

**Checkpoint**: User Story 1 works end-to-end using a fake generator; no provider credentials or external calls are needed.

---

## Phase 4: User Story 2 — Consultar diagnósticos desde el historial (Priority: P1)

**Goal**: Preserve the historical attempt/role context and recover the exact diagnosis and role description previously used.

**Independent Test**: With two completed attempts that have different answers/versions, verify each selected attempt uses its own matching, historical option descriptors, requirements and saved snapshot; opening an existing diagnosis does not call the provider.

### Tests

- [x] T021 [P] [US2] Add historical isolation/persistence integration tests in `backend/src/test/java/com/nextrole/explainability/ExplainabilityApiIntegrationTest.java` covering Q1 and Q2/M1 attempts, separate rows by attempt/role, historical snapshot response, and prompt/model change without regeneration; ownership rejection is covered by T010.
- [ ] T022 [P] [US2] Add frontend history navigation tests in `frontend/src/features/explainability/RoleFormationPage.test.tsx` and `frontend/src/features/roles/AssessmentResultsRanking.test.tsx` proving old and current rankings pass their own `attemptId`, the return link restores that ranking, and V1 `RoleDetailPage`/`LearningPage` routes remain available.

### Implementation

- [x] T023 [US2] Complete historical context lookup and response mapping in `backend/src/main/java/com/nextrole/explainability/ExplainabilityContextService.java` and `ExplainabilityService.java`; always use the requested completed attempt’s pinned questionnaire/matrix and persisted Matching, and return a successful row’s saved `role_name_snapshot`/`role_description_snapshot` rather than current catalog text.
- [ ] T024 [US2] Complete the role-specific route and ranking links in `frontend/src/App.tsx`, `frontend/src/features/roles/AssessmentResultsRanking.tsx` and `frontend/src/features/explainability/RoleFormationPage.tsx`; preserve `attemptId` across current/history access and back navigation without deleting V1 route consumers.

**Checkpoint**: Historical diagnosis and role copy remain stable after a newer attempt or catalog description change.

---

## Phase 5: User Story 3 — Mantener disponible la pantalla ante errores de IA (Priority: P2)

**Goal**: Handle provider, validation, timeout and interrupted-generation failures without affecting deterministic results, and allow only explicit manual retries.

**Independent Test**: Simulate provider failure, invalid response, timeout, simultaneous requests and backend interruption; verify safe errors, no duplicate provider call, no auto-retry, lease recovery without provider call, and explicit retry from FAILED/expired only.

### Tests

- [x] T025 [P] [US3] Add state-machine, error-mapping and manual-retry unit tests in `backend/src/test/java/com/nextrole/explainability/ExplainabilityServiceTest.java` and `ExplainabilityRecoveryServiceTest.java` for NOT_STARTED, GENERATING, SUCCEEDED and FAILED; assert `generate` does not call the provider from existing FAILED, success is immutable, and `attempt_count` never enforces a quota.
- [x] T026 [P] [US3] Add PostgreSQL concurrency/recovery tests in `backend/src/test/java/com/nextrole/explainability/ExplainabilityConcurrencyIntegrationTest.java` for unique claim, compare-and-set, generation-token fencing, concurrent generate/retry, expired lease GET with no write/provider call, and expired lease claim only after manual retry.
- [ ] T027 [P] [US3] Add frontend failure/polling tests in `frontend/src/features/explainability/RoleFormationPage.test.tsx` for FAILED manual retry, 2-second status polling while GENERATING, 120-second visual wait limit and no retry on reload, navigation, focus, remount or network-client retry.

### Implementation

- [x] T028 [US3] Implement provider error and timeout translation in `backend/src/main/java/com/nextrole/explainability/OpenRouterDiagnosisGenerator.java` and `ExplainabilityService.java`; sanitize upstream errors, preserve deterministic role/matching data, record nullable usage/cost, and expose no credentials/raw response.
- [x] T029 [US3] Implement compare-and-set manual retry and lease recovery in `backend/src/main/java/com/nextrole/explainability/ExplainabilityService.java` and the diagnosis repository; `POST generate` only claims absent rows, `POST retry` only claims FAILED/expired rows after user action, GET derives an expired generation as interrupted without writing, and fencing prevents stale completion overwrites.
- [ ] T030 [US3] Implement explicit error/retry UI in `frontend/src/features/explainability/RoleFormationPage.tsx` and `frontend/src/features/explainability/RoleFormationPage.css`; keep deterministic header and plan visible, use the approved error text and button, and issue a retry request only from the user’s button action.
- [x] T031 [US3] Extend the basic fake from T014 with invalid, timeout, unavailable, rate-limited and delayed scenarios in `backend/src/test/java/com/nextrole/explainability/support/FakeDiagnosisGenerator.java`; ensure automated suites use the fake and never call OpenRouter.

**Checkpoint**: Failures remain isolated to the diagnosis; provider limits are reported as upstream errors, with no application-owned quota or automatic retry.

---

## Phase 6: User Story 4 — Distinguir el diagnóstico del futuro plan de formación (Priority: P2)

**Goal**: Keep the future Learning surface visually distinct and independent, and present the approved identity of all six roles accessibly.

**Independent Test**: For all six role IDs, verify the correct approved SVG/palette and contrast behavior; verify «Tu plan de formación» contains only «Próximamente» and is rendered independently in loading, success and error states.

### Tests

- [ ] T032 [US4] Add role-visual mapping and accessible component tests in `frontend/src/features/roles/roleVisuals.test.ts` and `frontend/src/features/explainability/RoleFormationPage.test.tsx` for all six stable IDs, labels/alt text, keyboard access, contrast token use and reduced-motion behavior.
- [ ] T033 [US4] After T032, add plan placeholder tests in `frontend/src/features/explainability/RoleFormationPage.test.tsx` verifying the independent «Tu plan de formación» card displays only «Próximamente» in loading, success and failure states and does not consume diagnosis text. These tasks run sequentially because they share the same test file.

### Implementation

- [ ] T034 [US4] Add the six official SVG assets to `frontend/src/assets/roles/` and centralize `roleId → emblem/name/palette` in `frontend/src/features/roles/roleVisuals.ts` after the user supplies the approved files; preserve SVG paths and original fills/strokes, and do not substitute generated or generic icons.
- [ ] T035 [US4] Implement the independent «Tu plan de formación» placeholder and role-styled diagnosis container in `frontend/src/features/explainability/RoleFormationPage.tsx` and `frontend/src/features/explainability/RoleFormationPage.css`; keep four sections visible, avoid accordions/AI icons/numbering, and honor responsive layout, focus, contrast and reduced motion.

**Checkpoint**: Learning remains a visual placeholder with no courses, Learning API, backend coupling or AI-derived plan.

---

## Phase 7: Polish and cross-cutting validation

**Purpose**: Verify migration safety, deterministic contracts, existing regressions and the eventual manual review gates.

- [ ] T037 [P] Add a separate reproducible Q2/M1 fixture-generation path for all six roles under `experiments/explainability/cases/q2-m1/` using backend deterministic results; preserve all existing Q1/M1 contexts/runs and verify each generated context’s matching percentage, competency gaps and priority against backend calculations.
- [x] T038 [P] Run and record backend unit/integration regression checks from `backend/pom.xml`, including PostgreSQL/Testcontainers, with the simulated provider and no real credentials. Checkpoint 3 validation: 86 tests, 0 failures, 0 errors, 0 skipped; PostgreSQL containers were disposable and no provider calls were made.
- [ ] T039 [P] Run and record frontend tests, build and lint using `frontend/package.json`; verify no regression in the current ranking, V1 role detail, general Formation entry or shared bottom navigation.
- [ ] T040 Perform documented manual mobile/desktop, keyboard, screen-reader and reduced-motion review for `frontend/src/features/explainability/RoleFormationPage.tsx` after the six official SVGs are available; keep visual acceptance open until actual rendered sizes are reviewed.
- [ ] T041 After separate explicit authorization, execute one controlled smoke request with the configured free model and record actual model, validation, latency and reported usage/cost in `progress/history.md`; verify no fallback/retry occurred and never treat unknown cost as zero. If authorization is not granted, leave this gated task unexecuted.

---

## Dependencies and execution order

### Dependencias y evidencia de finalización por tarea

La tabla explicita prerrequisitos y evidencia observable de cada ítem; la prueba de cada historia también valida el conjunto integrado al llegar a su checkpoint.

| Tarea | Depende de | Evidencia de finalización |
|---|---|---|
| T001 | Ninguna | V1–V6 presentes y slot V7 libre; si no, se detiene antes de migrar. |
| T002 | Ninguna | Hash SHA-256 coincide con el prompt experimental y referencia de contrato revisada; no cambian esos artefactos. |
| T003 | T001 | Migración V7 aplica desde V6 en PostgreSQL desechable y satisface el modelo. |
| T004 | T003 | Mapeos y operaciones de repositorio cumplen constraints/transiciones de `data-model.md`. |
| T005 | T003 | Pruebas de catálogo muestran los seis descriptores por ID y autenticación. |
| T006 | T002 | Test del recurso de classpath confirma bytes/hash V5 y configuración segura. |
| T007 | T003, T004 | Pruebas PostgreSQL confirman unicidad, compare-and-set, token y snapshot. |
| T036 | T003, T007 | Migración V7 preserva filas Q1/Q2/M1 sembradas antes de aplicarla. |
| T008 | T004, T005 | Pruebas unitarias verifican prioridad, historial, no-brechas y proyección de competencias. |
| T009 | T002, T004, T005 | Serialización Java valida el contrato Python y excluye metadatos locales. |
| T010 | T004, T006 | Pruebas de servicio/API confirman validación, autenticación, propiedad, generación, reutilización y GET sin efectos. |
| T011 | Diseño aprobado y componentes de ranking existentes | Tests frontend nuevos fallan antes de ruta/pantalla y luego pasan al completar US1. |
| T012 | T004, T005, T008 | Pruebas de contexto confirman servicios reutilizados y fórmula de prioridad exacta. |
| T013 | T009, T012 | JSON serializado pasa test de compatibilidad experimental. |
| T014 | T006 | Tests del puerto y fake básico validan intercambio tipado y metadatos de uso opcionales antes de las pruebas de integración. |
| T015 | T014 | Tests del validador aceptan solo el contrato exacto y rechazan entradas inválidas. |
| T016 | T006, T014 | Mock HTTP verifica modelo/configuración, Bearer no registrado, timeout y ausencia de fallback/retry. |
| T017 | T007, T012–T016 | Servicio probado con fake para generación inicial/reutilización, ausencia de llamadas duplicadas, respuesta válida/inválida, error y persistencia PostgreSQL; GET puro queda para T018. |
| T018 | T005, T017 | Tests API de auth/estado/schema satisfacen el contrato HTTP. |
| T019 | T011, T018 | Pruebas de navegación, estados y header determinístico pasan; V1 conserva rutas. |
| T020 | T019 | Prueba end-to-end con proveedor falso genera una vez y después reutiliza éxito. |
| T021 | T017, T018 | PostgreSQL demuestra aislamiento entre intentos/versiones y recuperación del snapshot histórico en la integración de API. |
| T022 | T019 | Vitest verifica intento actual/histórico, retorno exacto y rutas V1. |
| T023 | T012, T017 | Prueba histórica devuelve texto de rol guardado y no usa último intento/catálogo actual para éxito. |
| T024 | T019, T022 | Pruebas frontend comprueban links del ranking actual/histórico y back path. |
| T025 | T014, T017 | Unit tests prueban estados, errores y manual-only retry sin cuota propia. |
| T026 | T007, T017 | Testcontainers prueba concurrencia, lease, CAS y fencing sin llamada automática. |
| T027 | T019, T020 | Tests frontend prueban polling y ausencia de retry por remount/foco/recarga. |
| T028 | T015, T016 | Tests con fake/mocks validan mapeo saneado, timeout, proveedor 429 y metadatos nullables. |
| T029 | T007, T017, T026 | Tests prueban generate/retry por estado y que lease vencido no llama al proveedor. |
| T030 | T027, T029 | Tests UI muestran error aprobado y retry ocurre solo tras click explícito. |
| T031 | T014 | Fake ampliado cubre errores reproducibles; suites automatizadas no usan OpenRouter. |
| T032 | Role IDs, palette values and accessibility requirements in `spec.md` | Pruebas visuales/accesibilidad verifican seis asociaciones de emblema/paleta y fallan antes de implementar el mapa. |
| T033 | T019, T032 | Tests prueban placeholder separado en carga, éxito y error; secuencial respecto de T032 por archivo compartido. |
| T034 | Entrega de los seis SVG aprobados | Archivos/copias pasan revisión de identidad, fills/strokes y mapa por ID. |
| T035 | T019, T033; T034 para emblemas | Tests responsivos/semánticos verifican las secciones y el placeholder independiente. |
| T037 | T009, T012, T013 | Fixtures Q2/M1 de seis roles reproducibles coinciden con los motores determinísticos. |
| T038 | T003–T031, T036 | `mvn -f backend/pom.xml test` pasa sin credenciales ni llamadas externas. |
| T039 | T018–T035 | Tests, build y lint frontend pasan; consumidores V1/regresiones se mantienen. |
| T040 | T034, T035 | Revisión real de mobile/desktop, teclado, lector y movimiento reducido queda registrada. |
| T041 | T016 y aprobación expresa posterior | Smoke test real se ejecuta solo con autorización; cero fallback y cero retry automático. |

Las tareas `[P]` pueden prepararse en paralelo una vez satisfechas sus dependencias de fase; las no marcadas siguen el orden indicado por la fase y la tabla. En tareas que agregan pruebas, primero se confirma que el comportamiento nuevo falla antes de implementar; la evidencia listada corresponde a la condición de cierre.

### Phase dependencies

- Setup (T001–T002) precedes the Foundational phase. T001 is a gate: if V7 is no longer free, stop and revise the plan before implementation.
- Foundational (T003–T007 plus T036) blocks all user stories because API and UI depend on the role catalog, persistence invariants, secure configuration, exact V5 resource and verified history preservation. T036 runs immediately after T007, before any shared development migration.
- User Story 1 (P1) is the MVP and follows Foundational. It delivers deterministic context, validated synchronous generation, persisted reuse and the first Formation screen.
- User Story 2 (P1) follows the core context/API contract in US1; it completes historical snapshots and navigation semantics.
- User Story 3 (P2) follows the generation/state foundation in US1; it completes recoverable failures and explicit retry behavior.
- User Story 4 (P2) can proceed after the shared route/screen exists. T034 is gated only on receipt of the official SVG files; backend work is not blocked by them.
- Polish (T036–T041) follows the relevant stories; real-provider smoke testing additionally requires explicit authorization and is not an automated test.

### Parallel opportunities

- T002 and the V7 availability check T001 are independent setup reviews.
- In US1, T008–T011 are parallelizable test additions in separate test/fixture paths; the context, adapter, validator and UI tasks are otherwise sequenced by their dependencies.
- In US2, T021 and T022 can be prepared in parallel because they target separate backend/frontend test paths.
- In US3, T025–T027 can be prepared in parallel; provider error handling, state claiming and UI handling should integrate only after those contracts are stable.
- In US4, T032 and T033 must run sequentially because both modify `RoleFormationPage.test.tsx`; T033 depends on T032. The official-asset task T034 is independent of backend stories but cannot complete until the user supplies the SVGs.
- T036 (migration/history regression) runs in the foundational checkpoint after T003/T007 and before shared development migration. T037 (separate Python fixtures) can proceed independently after its prerequisites. After T036, T038 (full backend suite) and T039 (frontend test/build/lint) can run in parallel; T037 can run alongside them when its backend deterministic inputs are available.

## MVP and delivery strategy

The MVP is Phase 1, Phase 2 and User Story 1: one selected role and completed attempt produce a validated diagnosis from the existing deterministic results, persist and reuse it, and render the role-specific Formation view with a simulated provider in tests. Do not enable a real-provider run as part of implementation. Add historical access (US2), controlled manual recovery (US3), and the independent visual Learning placeholder (US4) as subsequent increments. Keep automated provider tests simulated throughout.

## Verification criteria per story

- **US1**: Independent test above passes; exact context compatibility to the Python experimental contract, priority, score, strict schema, one initial provider call and persisted reuse are covered.
- **US2**: Independent test above passes for distinct current/historical attempts and snapshots; ownership isolation is verified.
- **US3**: Independent test above passes for errors, concurrency and manual-only retries; no internal quota or auto-retry exists.
- **US4**: Independent test above passes for six-role asset mapping and separate placeholder; manual accessibility/responsive review is recorded or explicitly left open until assets arrive.

## Notes

- Tasks are unchecked and specify implementation work only; no implementation was performed while generating this plan.
- `[P]` means the task touches independent files and has no unmet dependency; `[USn]` maps to the corresponding story in `spec.md`.
- Official SVG receipt is a dependency only for T034 and the visual acceptance in T040.
- The V5 prompt, experimental Q1/M1 fixtures and results are immutable inputs. New Q2/M1 fixtures are separate.
- No task changes the Matching algorithm, Skill Gap V2 contract, question catalog, matrix, role exploration button design, V1 routes, prompt text, or local experimental results.
