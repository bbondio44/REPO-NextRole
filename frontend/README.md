# NextRole V2 — Frontend

React 19, TypeScript, Vite, TanStack Query and Tailwind CSS. The interface contains both migrated V2 flows and legacy V1 screens; each feature's current spec and API usage define which source of data applies.

## Run

With the backend and local environment configured, run the complete stack through Docker Compose from the repository root. For frontend-only development:

```powershell
npm ci
npm run dev
```

Vite defaults to `http://localhost:5173`. Set `VITE_API_PROXY_TARGET` when the API is not at `http://localhost:8080`. In Compose, the proxy targets `backend:8080`; the host mapping is normally `localhost:8090`.

## Current data boundaries

- Assessment questions, active drafts, saved answers, completion and historical results use authenticated `/api/v2` assessment contracts. Attempts retain their questionnaire/matrix versions; new reevaluations start unanswered and recovery returns only the active draft's answers.
- Authentication/profile and remaining legacy V1 screens continue to use their existing `/api/v1` services where applicable. Some older client-side helpers and local storage remain for those flows; they are not the source of truth for V2 assessment attempts/results.
- The V2 assessment catalogue contains 31 competencies and six roles. It is separate from the legacy V1 skills and roles catalogues.

See `../specs/001-autoevaluacion-matching/` for approved behavior and `../docs/V2/entorno-docker-desarrollo.md` for the current stack.

## Checks

```powershell
npm test
npm run build
npm run lint
```

Vitest is the configured test runner. The build runs TypeScript project checks and Vite production compilation. Run checks relevant to the change and report the exact outcome.
