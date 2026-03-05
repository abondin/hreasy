# Repository Guidelines

## Project Structure & Module Organization
- Main application code (Vue 3 + TypeScript) lives in `src/`: `views/`, `components/`, `composables/`, `stores/`, `services/`, `router/`, `plugins/`, `locales/`.
- Legacy Vue 2 application is isolated in `legacy/vue2/` and documented in `legacy/README.md`.
- End-to-end tests live in `e2e/`, unit tests in `tests/`.
- Static entry files/configs are in repository root (`vite.config.ts`, `eslint.config.ts`, `tsconfig*.json`, `playwright.config.ts`, `vitest.config.ts`).

## Build, Test, and Development Commands
- `npm run dev` — start Vue 3 Vite dev server.
- `npm run build` — type-check and produce Vue 3 production bundle.
- `npm run type-check` — run `vue-tsc --build`.
- `npm run lint` — run ESLint for the Vue 3 codebase.
- `npm run test:unit` — run Vitest unit tests.
- `npm run test:e2e` — run Playwright E2E tests.

## Coding Style & Naming Conventions
- Vue SFCs use TypeScript, 2-space indentation, mandatory semicolons.
- Components use PascalCase filenames (for example `ProfileSummaryCard.vue`).
- Prefer `@/` imports from `src`.
- Keep comments concise and in English.
- Do not hardcode user-facing text; use i18n keys.

## i18n Usage
- Use `vue-i18n` for all user-facing strings.
- In templates: `{{ $t('KEY') }}` or bindings like `:label="$t('KEY')"`.
- In scripts: `t('KEY')` from `useI18n`.
- Vue 3 locales are stored in `src/locales/*.json`.

## TypeScript & Vue Rules
- Avoid redundant type annotations when inference is clear.
- Add explicit types for public APIs, unions, generics, and complex return values.
- Prefer Composition API patterns (`composables`, `script setup`) for Vue 3 code.

## UI Stack & Libraries (Vue 3)
- UI: Vuetify 3 (`src/plugins/vuetify.ts`), icons from `@mdi/font`.
- Routing/State: `vue-router@4`, `pinia`.
- HTTP: Axios instance in `src/lib/http.ts`.
- i18n: `vue-i18n@11` via `src/i18n.ts`.
- Rich text: Markdown editor/renderer components in `src/components/shared`.

## Testing Guidelines
- Unit tests: Vitest + Vue Test Utils.
- E2E tests: Playwright.
- Add or update tests for every non-trivial feature migration.
- Before PR: run `npm run type-check`, `npm run lint`, and relevant tests.

## Migration Workflow (Vue 2 -> Vue 3)
- Treat `legacy/vue2` as source of behavior and API contracts.
- Port by domains: `services` -> `stores/composables` -> `views/components` -> `router`.
- Preserve route and permission behavior parity unless change is explicitly requested.
- Keep Vue 3 and legacy docs in sync when moving modules.

## Security & Config
- API base URL for Vue 3: `VITE_API_BASE_URL`.
- Dev proxy target: `VITE_DEV_SERVER_PROXY`.
- Do not commit secrets. Use local env variables.

## Agent Notes
- Do not run root lint/type-check against `legacy/**`; legacy is maintained separately.
- Do not generate emitted TS artifacts (`*.js`, `*.d.ts`) inside `src/`.
- If you change shared docs or commands, update both root `README.md` and `legacy/README.md` when relevant.

## Skills Policy
- Use `$hreasy-vue3-development` by default for all Vue 3 implementation/refactor/bugfix tasks in this repository.
- Use `$vue2-to-vue3-migration` only when the task explicitly includes migration/parity from `legacy/vue2`.
- For migration work, combine both skills: `$hreasy-vue3-development` + `$vue2-to-vue3-migration`.
- Follow reuse-first behavior from `$hreasy-vue3-development`: extend existing components/services/composables/stores before creating new ones.

## Context Hygiene
- Keep working context lean: load only files directly related to the current task.
- Read skill `references/` files only when needed for the current step; avoid bulk-loading all references.
- Prefer targeted searches (`rg` by domain/path) over broad scans of the whole repository.
- Do not restate large docs in chat; keep summaries short and actionable.
