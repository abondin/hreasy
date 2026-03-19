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
- Add concise comments to all key domain structures and core abstractions (public interfaces, shared helpers, composables with business logic, and reusable components) describing intent and usage constraints.
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

## Text Encoding (Windows)
- Keep source and markdown files in `UTF-8` (prefer no BOM).
- In Windows PowerShell 5.1, do not use `Set-Content`, `Add-Content`, `Out-File`, `>` or `>>` for source file writes because defaults may produce UTF-16 LE or mixed encodings.
- For scripted writes, use explicit UTF-8 no BOM:
  - `[System.IO.File]::WriteAllText($path, $text, [System.Text.UTF8Encoding]::new($false))`
- If a file was scripted or rewritten, normalize encoding to UTF-8 before `type-check`/`lint`.
- Before commit, run an encoding sanity check on changed files.

### Critical Encoding Rule
- For any source or markdown file containing non-ASCII text, treat PowerShell content rewrites as unsafe by default.
- Do not use PowerShell here-strings, regex replacements, pipeline-based text transforms, or intermediate shell variables to rewrite `.vue`, `.ts`, `.js`, `.json`, `.md`, or other source files with Cyrillic/non-ASCII text.
- `apply_patch` is the default edit path for such files.
- If `apply_patch` is not feasible, use a Unicode-safe writer and prefer ASCII-only scripted edits with `\\uXXXX` escapes for non-ASCII literals.
- Do not trust PowerShell console output as proof that encoding is correct.
- If you accidentally used an unsafe rewrite on a non-ASCII file, stop and repair the file encoding/content before any further edits.
- After any scripted edit of a non-ASCII file, explicitly verify:
  - file encoding is UTF-8 (no BOM preferred)
  - there is no UTF-16 LE/BOM regression
  - there are no replacement characters, question marks, or mojibake sequences in place of Cyrillic
  - only then run `type-check`, `lint`, or tests

## UI Stack & Libraries (Vue 3)
- UI: Vuetify 4 (`src/plugins/vuetify.ts`), icons from `@mdi/font`.
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
