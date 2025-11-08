# Repository Guidelines

## Project Structure & Module Organization
- Application code lives under `src/` (Vue 2 + TypeScript): `components/`, `store/`, `router/`, `plugins/`, `locales/`.
- Static assets belong to `public/`; build artifacts go to `dist/`.
- Unit tests reside in `tests/unit/` and follow the `*.spec.ts` naming.
- Dev tooling sits in `devops/` (Docker scripts), `.localdev/` (local docker-compose), and root configs such as `tsconfig.json`, `.eslintrc.js`, `jest.config.js`.

## Build, Test, and Development Commands
- `npm run serve` — start the Vue CLI dev server (set `BACKEND_API_BASE_URL` when needed).
- `npm run build` — create the production bundle in `dist/`.
- `npm run test:unit` — execute Jest unit tests.
- `npm run lint` — run ESLint.
- `npm run i18n-add-missing-keys` — sync i18n keys into `src/locales/*.json`.

## Coding Style & Naming Conventions
- Vue SFCs use TypeScript, 2-space indentation, mandatory semicolons.
- Components follow PascalCase (e.g. `SharedArticlesWindow.vue`). Utilities/constants use kebab-case or camelCase `.ts` files as in the repo.
- Respect the ESLint config (`plugin:vue/essential`, `@vue/typescript/recommended`). Fix lint issues before committing.
- Prefer the `@/` alias for imports.
- **All inline comments and doc comments must be written in English.**

## i18n Usage
- Use `vue-i18n` for every user-facing string—no hardcoded text in templates or scripts.
- Templates: `{{ $t('KEY') }}` or bindings such as `:label="$t('KEY')"`; scripts: `this.$t('KEY')`.
- Add and maintain keys in `src/locales/*.json`; run `npm run i18n-add-missing-keys` to sync.

## TypeScript Style Rules
- Avoid trivial annotations (let inference work). Example: `let ready = false`, not `let ready: boolean = false`.
- Add types when inference is unclear (public APIs, unions, generics) or when readability improves.
- Vue 2 + class components: declare reactive fields as public properties and initialize derived-from-props fields inside `created()` to keep reactivity with `useDefineForClassFields`.

## UI Stack & Key Libraries
- UI: Vuetify 2 (legacy) via `src/plugins/vuetify.ts`, icons from `@mdi/font` with `mdiSvg`.
- Routing/State: `vue-router`, `vuex` + `vuex-class`; class-based components with `vue-class-component` + `vue-property-decorator`.
- HTTP: Axios singleton in `src/components/http.service.ts`.
- Dates: Moment.js currently; prefer ISO strings at boundaries.
- i18n: `vue-i18n` configured in `src/i18n.ts` with JSON locales under `src/locales/`.
- Rich text: `vue2-editor` (Quill) with image modules.
- Visualization: `vis-timeline` (vacations) and `svg-pan-zoom` (maps).
- UX helpers: `vue-clipboard2`, `vue-avatar-cropper`.
- Security: `dompurify` for sanitizing HTML/SVG.

## Testing Guidelines
- Framework: Jest via `@vue/test-utils` (`@vue/cli-plugin-unit-jest`).
- Tests belong to `tests/unit/` and use the `xxx.spec.ts` naming.
- Target meaningful coverage on new/changed code.
- Run `npm run test:unit` before pushing.

## Commit & Pull Request Guidelines
- Commits: short imperative summaries (e.g. “Fix vacation filtering”). Reference issues/PRs when helpful.
- PRs must describe changes, link issues, include screenshots/GIFs for UI updates, and list test steps.
- Keep diffs minimal and lint-clean. Update i18n keys/docs when needed.

## Security & Configuration Tips
- Backend URL comes from `BACKEND_API_BASE_URL` (see README). Docker builds rely on `devops/build.sh` and `HREASY_API_HOST`.
- Never commit secrets; use `.env` for local-only values.

## Agent Notes
- Do not leave generated `vue-tsc` artifacts (`*.js`, `*.d.ts`) near sources; use `--noEmit` or clean them afterward.
- When touching `vue-router` in the Vue 3 skeleton, remember the legacy app still runs `vue-router@3`. Access `$router`/`$route` via `getCurrentInstance().appContext.config.globalProperties` and run `npx vue-tsc --noEmit` to catch type conflicts.
- Vue 3 components should follow the formatting used in `ProfileSummaryCard.vue`: file-level comment, `<editor-fold>` wrappers, closing tags on the same line.
- Before adding/updating dependencies, inspect their latest stable versions with `npm view <package> version` and pin exactly that.
- After substantial code changes, always run `npm run type-check` and `npm run lint`.
- For interactive chips (tech profiles, skills, etc.) never rely on `v-chip`’s `closable` flag. Render a dedicated close button, show a confirmation dialog, and delete the record only after the user confirms.
