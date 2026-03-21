# Repository Guidelines

## Project Structure & Module Organization
- App code in `src/` (Vue 3 + TypeScript): `components/`, `views/`, `stores/`, `composables/`, `router/`, `plugins/`, `locales/`.
- Static assets in `public/`; built artifacts in `dist/`.
- Unit/integration tests in `tests/`; E2E tests in `e2e/`.
- Dev tooling: `devops/` (Docker scripts), `.localdev/` (local docker-compose), root configs (`vite.config.ts`, `tsconfig*.json`, `eslint.config.ts`, `vitest.config.ts`).

## Build, Test, and Development Commands
- `npm run dev` (or `npm run serve`) — start Vite dev server. Example: `VITE_DEV_SERVER_PROXY=http://localhost:8081 npm run dev`.
- `npm run build` — type-check and production build to `dist/`.
- `npm run build-only` — only production build.
- `npm run type-check` — run `vue-tsc` checks.
- `npm run lint` — run ESLint checks.
- `npm run test:unit` — run Vitest unit tests.
- `npm run test:e2e` — run Playwright E2E tests.

## Coding Style & Naming Conventions
- TypeScript + Vue SFCs, 2-space indentation, semicolons required.
- Components: PascalCase file names (e.g., `SharedArticlesCard.vue`). Utilities/composables: `camelCase` or `kebab-case` `.ts` as in repo.
- Follow ESLint flat config in `eslint.config.ts`. Keep lints clean.
- Prefer `@/` alias for imports.

## TypeScript Style Rules
- Avoid trivial annotations for inferred types.
- Add explicit types when inference is unclear (public APIs, unions, generics) or improves readability.
- Keep service DTOs and composable contracts typed.

## UI Stack & Key Libraries
- UI: Vuetify 4 (`src/plugins/vuetify.ts`), icons via `@mdi/font` and Font Awesome.
- Routing/State: `vue-router` 4, `pinia`.
- HTTP: `axios` with shared client in `src/lib/http.ts`.
- Dates: `moment` for parsing/formatting; prefer ISO strings at boundaries.
- i18n: `vue-i18n` configured in `src/i18n.ts` with JSON locales under `src/locales/`.
- Visualization: `vis-timeline` and `svg-pan-zoom`.
- Security: `dompurify` for sanitizing user/content HTML/SVG.

## Testing Guidelines
- Unit tests: Vitest + Vue Test Utils.
- E2E tests: Playwright.
- Place tests under `tests/` and `e2e/`.
- Run `npm run type-check`, `npm run lint`, `npm run test:unit` before pushing.

## Commit & Pull Request Guidelines
- Commits: short, imperative summaries (e.g., "Fix vacation filtering"). Reference issues/PRs (e.g., `#82`) when relevant.
- PRs: include description, linked issues, screenshots/GIFs for UI changes, and verification steps.
- Keep diffs focused and lint-clean. Update i18n keys and docs when applicable.

## Security & Configuration Tips
- Backend proxy URL in dev via `VITE_DEV_SERVER_PROXY`.
- Docker runtime upstream via `HREASY_API_HOST`.
- Avoid committing secrets; use `.env` for local-only values.

## Product Constraints
- Timesheet module is currently inactive for end users and should be treated as internal/inactive unless explicitly requested.
