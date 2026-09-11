# Repository Guidelines

## Project Structure & Module Organization
- App code in `src/` (Vue 3 + TypeScript): `components/`, `views/`, `stores/`, `composables/`, `router/`, `plugins/`, `locales/`.
- Static assets in `src/assets/` and root files such as `favicon.png`; built artifacts in `dist/`.
- Unit/integration tests in `tests/`; E2E tests in `e2e/`.
- Dev tooling: `devops/` (Docker scripts), `.localdev/` (local docker-compose), root configs (`vite.config.ts`, `tsconfig*.json`, `eslint.config.ts`, `vitest.config.ts`).

## Build, Test, and Development Commands
- `npm run dev` — start Vite dev server. Example: `VITE_DEV_SERVER_PROXY=http://localhost:8081 npm run dev`.
- `npm run build` — type-check and production build to `dist/`.
- `npm run build-only` — only production build.
- `npm run type-check` — run `vue-tsc` checks.
- `npm run lint` — run ESLint without modifying files; use `npm run lint:fix` to apply fixes.
- `npm run test:unit -- --run` — run Vitest unit tests once.
- `npm run test:e2e` — run Playwright E2E tests.

## Coding Style & Naming Conventions
- TypeScript + Vue SFCs, 2-space indentation, semicolons required.
- Components: PascalCase file names (e.g., `SharedArticlesCard.vue`). Utilities/composables: `camelCase` or `kebab-case` `.ts` as in repo.
- Follow ESLint flat config in `eslint.config.ts`. Keep lints clean.
- Prefer `@/` alias for imports.
- Keep user-facing text in i18n keys under `src/locales/`.
- Reuse existing components, composables, stores, and services before creating new ones.
- Prefer Vuetify capabilities and responsive grid props over custom CSS or JavaScript layout workarounds.

## TypeScript Style Rules
- Avoid trivial annotations for inferred types.
- Add explicit types when inference is unclear (public APIs, unions, generics) or improves readability.
- Keep service DTOs and composable contracts typed.

## UI Stack & Key Libraries
- UI: Vuetify 4 (`src/plugins/vuetify.ts`), icons via `@mdi/font` and Font Awesome.
- Routing/State: `vue-router` 5, `pinia`.
- HTTP: `axios` with shared client in `src/lib/http.ts`.
- Dates: use native date handling and existing local helpers; prefer ISO strings at API boundaries.
- i18n: `vue-i18n` configured in `src/i18n.ts` with JSON locales under `src/locales/`.
- Visualization: `vis-timeline` and `svg-pan-zoom`.
- Security: `dompurify` for sanitizing user/content HTML/SVG.

## Testing Guidelines
- Unit tests: Vitest + Vue Test Utils.
- E2E tests: Playwright.
- Place tests under `tests/` and `e2e/`.
- Prefer semantic `data-testid` selectors for critical flows and centralize reused ids in `e2e/support/selectors.ts`.
- Mocked API data, fixtures, screenshots, and test harness datasets must not contain real employee/customer names, surnames, emails, phone numbers, Telegram accounts, or other personal data. Use neutral English test identities such as `Alex Morgan` and `alex.morgan@example.test`.
- Test fixtures and configs committed to the repository must not reference local-machine absolute paths such as `C:\Users\...` or files outside the repository. Put required files in the repo with sanitized synthetic data and reference them via repo-relative paths.
- Run `npm run type-check`, `npm run lint`, and `npm run test:unit -- --run` before pushing.

## Commit & Pull Request Guidelines
- Commits: short, imperative summaries (e.g., "Fix vacation filtering"). Reference issues/PRs (e.g., `#82`) when relevant.
- PRs: include description, linked issues, screenshots/GIFs for UI changes, and verification steps.
- Keep diffs focused and lint-clean. Update i18n keys and docs when applicable.

## Security & Configuration Tips
- Backend proxy URL in dev via `VITE_DEV_SERVER_PROXY`.
- Docker runtime upstream via `HREASY_API_HOST`.
- Avoid committing secrets; use `.env` for local-only values.
- Keep edited text files valid UTF-8 and prefer no BOM. On Windows, use `apply_patch` for files containing non-ASCII text instead of PowerShell 5.1 content-rewrite commands.

## Project UI Conventions

- Use `src/components/shared/HREasyTableBase.vue` for employee-style list pages unless the required behavior does not fit it.
- Keep detail pages consistent: centered bounded content, profile summary first, domain actions next, and history or related entities last.
- Preserve backend permission behavior when changing routes, menus, actions, or visibility.

## Product Constraints
- Timesheet module is currently inactive for end users and should be treated as internal/inactive unless explicitly requested.

## Dual-Version Production Note
- Vue 3 is the main active UI, but the legacy Vue 2 UI is also deployed on a separate production URL.
- Do not propose removing the footer link to the old UI by default.
- Transitioning from the new UI to the old UI remains a supported production fallback scenario.
