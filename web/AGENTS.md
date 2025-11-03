# Repository Guidelines

## Project Structure & Module Organization
- App code in `src/` (Vue 2 + TypeScript): `components/`, `store/`, `router/`, `plugins/`, `locales/`.
- Static assets in `public/`; built artifacts in `dist/`.
- Unit tests in `tests/unit/` with `*.spec.ts` files.
- Dev tooling: `devops/` (Docker scripts), `.localdev/` (local docker-compose), root configs (`tsconfig.json`, `.eslintrc.js`, `jest.config.js`).

## Build, Test, and Development Commands
- `npm run serve` — start local dev server. Example: `BACKEND_API_BASE_URL=http://localhost:8081 npm run serve`.
- `npm run build` — production build to `dist/`.
- `npm run test:unit` — run Jest unit tests.
- `npm run lint` — run ESLint checks and report issues.
- `npm run i18n-add-missing-keys` — add missing i18n keys to `src/locales/*.json`.

## Coding Style & Naming Conventions
- TypeScript + Vue SFCs, 2-space indentation, semicolons required.
- Components: PascalCase file names (e.g., `SharedArticlesWindow.vue`). Utilities/constants: `kebab-case` or `camelCase` `.ts` as in repo.
- Follow ESLint config (`plugin:vue/essential`, `@vue/typescript/recommended`). Fix lints before PRs.
- Prefer `@/` alias for imports (configured via `tsconfig.json`).

## i18n Usage
- Use `vue-i18n` for all user-facing text. Do not hardcode strings in templates or scripts.
- Templates: `{{ $t('KEY') }}` or `:label="$t('KEY')"`. Scripts: `this.$t('KEY')`.
- Add/maintain keys in `src/locales/*.json`. Use `npm run i18n-add-missing-keys` to sync.

## TypeScript Style Rules
- Avoid trivial annotations: do not annotate types that are inferred.
  - Prefer `let ready = false` over `let ready: boolean = false`.
  - Prefer `let text = ''` over `let text: string = ''`.
- Add types when inference is unclear (public APIs, unions, generics) or improves readability.
- Vue 2 + class components: declare reactive fields as public class properties and initialize in `created()` when derived from props to ensure reactivity with `useDefineForClassFields`.

## UI Stack & Key Libraries
- UI: Vuetify 2 as the base component framework and grid (`src/plugins/vuetify.ts`), icons via `@mdi/font` with `mdiSvg`.
- Routing/State: `vue-router`, `vuex` + `vuex-class`; class-based components via `vue-class-component` + `vue-property-decorator`.
- HTTP: `axios` with a shared client in `src/components/http.service.ts`.
- Dates: `moment` for parsing/formatting; prefer ISO strings at boundaries.
- i18n: `vue-i18n` configured in `src/i18n.ts` with JSON locales under `src/locales/`.
- Rich text: `vue2-editor` (Quill) with image drop/resize modules.
- Visualization: `vis-timeline` for vacation timelines; `svg-pan-zoom` for SVG maps.
- UX helpers: `vue-clipboard2` for clipboard, `vue-avatar-cropper` for avatar upload/crop.
- Security: `dompurify` for sanitizing user/content HTML/SVG.

## Testing Guidelines
- Framework: Jest with `@vue/test-utils` (`@vue/cli-plugin-unit-jest`).
- Place tests under `tests/unit/` and name as `xxx.spec.ts`.
- Write focused unit tests for components, stores, and utils. No hard coverage gate enforced; aim for meaningful coverage on new/changed code.
- Run `npm run test:unit` locally before pushing.

## Commit & Pull Request Guidelines
- Commits: short, imperative summaries (e.g., "Fix vacation filtering"). Reference issues/PRs (e.g., `#82`) when relevant. Group logical changes.
- PRs: include description, linked issues, screenshots/GIFs for UI changes, and test notes/steps to verify.
- Keep diffs minimal and lint-clean. Update i18n keys and docs when applicable.

## Security & Configuration Tips
- Backend URL via `BACKEND_API_BASE_URL` (see README). For Docker, use `devops/build.sh` and run with `HREASY_API_HOST` as needed.
- Avoid committing secrets; use `.env` for local-only values (already gitignored).

## Agent Notes
- Не оставлять сгенерированные `vue-tsc` артефакты (`*.js`, `*.d.ts`) рядом с исходниками; после проверок типов обязательно чистить или настраивать `--noEmit`.
- При работе с `vue-router` в Vue 3-скелете проверять версии пакета: пока основной проект на `vue-router@3`, использовать доступ через `getCurrentInstance().appContext.config.globalProperties.$router`/`$route` и гонять `npx vue-tsc --noEmit`, чтобы исключить конфликт типов.
- Для Vue 3 компонентов придерживаемся HTML-форматирования как в `ProfileSummaryCard.vue`: верхний многострочный комментарий с описанием, блоки обёрнуты в `<!--<editor-fold desc="…">--> … <!-- </editor-fold> -->` и тег закрывается на той же строке, где открыт.
- Перед добавлением или обновлением зависимостей сначала уточнять их актуальные стабильные версии (`npm view <package> version`) и фиксировать именно их.
