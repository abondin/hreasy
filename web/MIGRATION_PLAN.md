# Vue 3 Migration Tracker

_Last updated: 2025-11-05_

This document records the current technical baseline and the roadmap for migrating the HREasy web client from Vue 2 to Vue 3, together with related ecosystem upgrades. Keep it updated as work progresses so future sessions can resume from the latest state.

## 1. Current Stack Snapshot

- **Vue entrypoint**: Vue 2.7 + class components bootstrapped via `new Vue` with global singletons (`src/main.ts:1`).
- **App shell**: Vuetify 2.6 and vue-router 3 configured inside the root component; relies on `$vuetify` theme mutation and legacy slot APIs (`src/App.vue:212`, `src/router/index.ts:113`).
- **State management**: Vuex 3 modules with decorator accessors, plus direct store imports inside services (e.g. permissions) (`src/store/modules/auth.ts:27`, `src/store/modules/permission.service.ts:1`).
- **Internationalisation**: vue-i18n 8 using webpack `require.context` for JSON locales (`src/i18n.ts:8`).
- **HTTP layer**: Axios 0.21 singleton with custom error mapping classes (`src/components/http.service.ts:5`, `src/components/errors.ts:1`).
- **Date/time utilities**: Extensive Moment.js usage across vacation, overtime, and shared components (`src/components/datetimeutils.ts:1`, `src/components/vacations/VacationEditForm.vue:244`).
- **Tooling**: Vue CLI 5 (webpack), Jest 27, ESLint 7, TypeScript 4.5 (`package.json:1`, `tsconfig.json:3`).

## 2. Migration Goals

1. Upgrade core framework to Vue 3 (3.4+) with Composition API first patterns.
2. Replace Vuex 3 with Pinia 2 while preserving existing business logic.
3. Migrate Vuetify 2 components/layouts to Vuetify 3 equivalents.
4. Modernise supporting libraries (vue-router 4, vue-i18n 9, Axios 1.x, etc.).
5. Switch build tooling from Vue CLI/webpack to Vite 5, update TS/ESLint/Jest stacks.
6. Reduce bundle size by replacing Moment.js and other deprecated Vue 2 plugins.

## 3. Roadmap & Status

Use the checkboxes to mark completion; add notes/dates next to items as you progress.

### Phase A – Preparation

- [x] Capture baseline architecture, dependencies, and migration goals (this document).
- [x] Define environment prerequisites (Node 20+, package manager strategy) and update documentation (`README.md:9`).
- [x] Establish regression safety net: prioritised manual test plan + coverage gap assessment (see §6).
- [x] Identify critical components that may block Vuetify 3 upgrade (see §7).

### Phase B – Incremental Refactors on Vue 2.7

- [x] Introduce Composition API usage (`@vue/composition-api` or Vue 2.7 native) in new/isolated components (`src/components/PageNotFoundComponent.vue:12`).
- [ ] Wrap global singletons (logger, permissions, http service) into plugin factories to ease DI _(Vue 3 skeleton still exports Axios singleton from `migration/vue3-skeleton/src/lib/http.ts`; plan to promote to `app.provide` plugin so both builds can share DI/mocking)_.
- [ ] Start extracting domain-specific composables (auth, permissions, vacations) mirroring Vuex modules _(Auth store + `useEmployeeProfile` composable live in `migration/vue3-skeleton/src/stores/auth.ts` and `migration/vue3-skeleton/src/composables/useEmployeeProfile.ts`; need parity for vacations, overtime, salaries, dictionaries)_.
- [ ] Replace Moment.js usage with Day.js or date-fns while staying on Vue 2 where possible _(Vue 3 skeleton ships ad-hoc formatters in `migration/vue3-skeleton/src/lib/datetime.ts`, legacy modules still rely on Moment)_.
- [ ] Implement Vue 3 employees directory: new `useEmployeesDirectory` composable + `EmployeesVirtualTable` (based on Vuetify virtual table docs) + route/filters/dialog to browse all employees with card view. Document progress here as work lands.

### Phase C – Store & Routing Transition

- [ ] Scaffold Pinia stores equivalent to existing Vuex modules; provide adapters for legacy decorator access _(Auth store + permissions helpers migrated under `migration/vue3-skeleton/src/stores/auth.ts` & `src/lib/permissions.ts`; next modules: vacations, overtime, salaries, admin dictionaries)_.
- [ ] Refactor router setup to Vue 3-compatible factory (`createRouter`, `createWebHistory`), remove `Vue.use` _(Vue 3 skeleton router uses guarded `createRouter` in `migration/vue3-skeleton/src/router/index.ts`; need transition strategy for legacy bootstrapping and shared navigation state)_.
- [ ] Move guard logic to composables/testable units; ensure auth flow covered by unit/integration tests _(Route guard now centralised in Vue 3 skeleton; add unit specs + synchronise logout/login flows with Vue 2 app)_.

### Phase D – Tooling & Build Migration

- [x] Create Vite-based build (Vue 3, TypeScript 5, ESLint 9, Vitest/Jest 29) running side-by-side _(Vue 3 skeleton under `migration/vue3-skeleton` runs on Vite 7 + Pinia 3 + Vuetify 3; adds Vitest/Playwright configs)_.
- [x] Package Docker release with nginx serving Vue 2 at `/` and Vue 3 under `/app-v3/` _(Dockerfile builds both bundles on `node:20-bullseye-slim` and ships them via an `nginx:alpine` runtime image; follow-up: add automated smoke check before publishing images)_.
- [ ] Update lint/test pipelines; ensure CI scripts support both builds during transition _(Vitest + Playwright commands exist; wire into CI and keep Vue CLI Jest until full cutover)_.
- [ ] Migrate webpack-specific helpers (e.g. `require.context` for locales) to Vite-friendly alternatives _(i18n ported via `import.meta.glob` in `migration/vue3-skeleton/src/i18n.ts`; remaining dynamic imports/asset helpers still depend on webpack in legacy code)_.

### Phase E – UI Stack Upgrade

- [ ] Upgrade Vuetify to v3 and refactor affected components/templates (navigation, forms, dialogs) _(Vue 3 skeleton already renders auth + profile flows on Vuetify 3; bulk port of legacy modules still pending)_.
- [ ] Replace deprecated Vue 2 plugins: `vue-clipboard2`, `vue-upload-component`, `vue-avatar-cropper` _(Custom replacements delivered in Vue 3 skeleton: `FileAttachmentsCard` + `FileUploadZone` + `ProfileAvatar`; migrate vacations clipboard usage next)_.
- [ ] Validate styling/theme parity and rebuild global theming config via `createVuetify` _(Skeleton theme configured in `migration/vue3-skeleton/src/plugins/vuetify.ts`; align tokens with legacy before cutover)_.

### Phase F – Final Cutover & Cleanup

- [ ] Switch default build to Vue 3 + Vite; remove Vue CLI configuration and obsolete dependencies.
- [ ] Retire Vuex modules/decorators once Pinia adoption is complete.
- [ ] Update documentation (README, onboarding) with new commands and architecture notes.
- [ ] Perform post-migration audit (bundle size, performance, accessibility) and file follow-up tasks.

## 4. Open Questions / Risks

- Confirm availability of Vue 3 compatible replacements for vacations timeline (`vis-timeline`) and clipboard helpers; evaluate whether custom Composition API wrappers or alternative libraries are needed.
- Validate new uploader/cropper implementations (`migration/vue3-skeleton/src/components/FileUploadZone.vue`, `.../ProfileAvatar.vue`) against production API limits and security requirements.
- Assess backend API contract changes required (if any) when upgrading Axios/interceptors.
- Determine whether to adopt TypeScript strictness increases (e.g., `exactOptionalPropertyTypes`) during tooling upgrade.

Keep this file under version control to track progress between sessions.

## 5. Environment & Testing Notes

- **Runtime baseline**: Adopt Node.js 20 LTS (current CLI tested on 18.16, upgrade recommended) and npm 10+. Document any Docker changes under `devops/`.
- **Local services**: Frontend dev server expects `BACKEND_API_BASE_URL` to point to the API (`README.md`); ensure backend mock or staging endpoint is available for migration smoke tests.
- **Testing status**: Only three Jest unit specs exist (`tests/unit/*.spec.ts`); integration coverage absent. Need manual regression checklist for auth, vacations, overtime, salaries, admin flows.
- **Short-term actions**: Draft target matrix of browsers/devices post-Vue 3 upgrade and decide on E2E tooling (Cypress or Playwright) before major refactors begin.

## 6. Regression Scenarios & QA Plan

Use this list as a smoke suite before/after significant migration steps. Expand with automated coverage as new tests are implemented.

- **Authentication**: Login with valid/invalid credentials, verify return path redirect and logout flow (`src/components/login/Login.vue:46`).
- **Employee profile & navigation**: Load profile dashboard, toggle navigation drawer, validate permission-gated menu visibility (`src/App.vue:24`, `src/store/modules/permission.service.ts:5`).
- **Vacations**: Request new vacation, edit/auto-calc dates, review timeline view and filters (high Moment.js dependency) (`src/components/vacations/VacationEditForm.vue:244`, `src/components/vacations/VacationsTimeline.vue:90`).
- **Overtimes & Timesheets**: Add overtime entry, approve/reject, export summaries; ensure month navigation and formatting works (`src/components/overtimes/AddOvertimeItemDialog.vue:204`, `src/components/ts/TimesheetTableComponent.vue:68`).
- **Salary requests**: Submit/approve flows, inspect overview tables (`src/components/salary/SalaryRequestsTable.vue`, `src/components/salary/details/SalaryRequestDetailsView.vue`).
- **Admin modules**: Projects CRUD, users/roles management, business account details, dictionary management, employees import workflows (`src/components/admin/**`).
- **Uploads & assets**: File uploader success/error states, avatar cropper, tech profile download/upload permissions (`src/components/shared/MyFileUploader.vue:45`).
- **Telegram confirmation**: Validate deep link route and success/failure messaging (`src/components/telegram/TelegramConfirmationPage.vue`).
- **Error handling**: Trigger forced backend errors to confirm global alert surface (`src/components/http.service.ts:14`, `src/App.vue:215`).
- **i18n toggles**: Switch locales and confirm translations load correctly once dynamic imports are introduced (`src/i18n.ts:8`).

_Automation gaps_: existing Jest specs cover only helper utilities (`tests/unit/datetimeutils.spec.ts`, etc.). Plan to add unit tests around new composables and Pinia stores, and introduce E2E (Cypress/Playwright) for the high-risk flows above.

## 7. Vuetify & Legacy Plugin Hotspots

Focus migration spikes here before tackling the long tail.

- **Data tables everywhere**: `v-data-table` underpins employees, vacations, overtime, projects, assessments, admin lists (`src/components/empl/Employees.vue:40`, `src/components/vacations/VacationsList.vue:128`, `src/components/admin/project/AdminProjects.vue:53`, etc.). Need Vuetify 3 equivalents or custom table abstraction.
- **Timeline visualisation**: `vis-timeline` integration drives the vacations timeline with direct DOM manipulation and CSS overrides (`src/components/vacations/VacationsTimeline.vue:12`). Assess Vue 3 wrappers or alternative libs (e.g., `vis-timeline` + Composition API, `apexcharts`).
- **File/Media handling**: File uploader based on `vue-upload-component` (`src/components/shared/MyFileUploader.vue:45`) and avatar cropper (`src/components/empl/EmployeeAvatarUploader.vue:46`) are Vue 2 only. Vue 3 skeleton now provides `FileUploadZone` + `FileAttachmentsCard` + `ProfileAvatar`; verify they cover vacations/overtime flows before removing legacy plugins.
- **Clipboard utilities**: `vue-clipboard2` is globally registered through Vuetify plugin bootstrap (`src/plugins/vuetify.ts:5`) and used in vacations copy-to-clipboard (`src/components/vacations/VacationsList.vue:415`). Replace with native Clipboard API or VueUse `useClipboard`.
- **Moment-heavy formatting**: business-critical date logic in vacations, overtime, salary modules (`src/components/datetimeutils.ts:1`, `src/components/overtimes/overtime.service.ts:117`). Plan staged replacement with Day.js/date-fns before migrating to Vuetify 3 date pickers.

## 8. Vue 3 Skeleton Workspace

- **Location**: `migration/vue3-skeleton`
- **Stack**: Vite 7, Vue 3.5, Pinia 3, Vue Router 4.6, Vuetify 3.10 (`migration/vue3-skeleton/package.json`)
- **Quick start**:
  ```shell
  cd migration/vue3-skeleton
  npm install
  # optional env vars for dev proxy / axios baseURL
  export VITE_DEV_SERVER_PROXY=http://localhost:8081
  export VITE_API_BASE_URL=/api/
  npm run dev
  ```
- **Current scope**: authenticated shell with guarded router (`migration/vue3-skeleton/src/router/index.ts`), Pinia auth store, Vuetify 3 navigation (`migration/vue3-skeleton/src/App.vue`). Employee profile view now ports avatar upload with cropper, Telegram edit dialog, tech profile attachments, office map preview, and permissions logic (`migration/vue3-skeleton/src/views/profile/**`, `src/components/FileAttachmentsCard.vue`, `src/components/office-map/OfficeMapPreviewDialog.vue`).
- **Testing status**: Vitest configured but no component specs yet; Playwright E2E covers unauthenticated redirect (`migration/vue3-skeleton/e2e/login.spec.ts`). ResizeObserver polyfill wired via `tests/setup.ts`.
- **Next actions**:
  1. Port vacations and overtime flows into composables/Pinia stores, replacing legacy placeholders in `ProfileMainView.vue` and validating Moment.js-free date helpers.
  2. Backfill unit tests for new profile components (avatar, telegram, attachments, permissions) using Vitest + Vue Test Utils; decide on API mocking strategy for HTTP services.
  3. Implement clipboard replacement (likely VueUse `useClipboard`) and ensure shared DI plugins (http, logger, permissions) can be consumed by both Vue 2 and Vue 3 builds.
  4. Document deployment toggle and add CI jobs to run `npm run lint`, `npm run type-check`, and `npm run test:e2e` for the skeleton.
- **Tooling note**: project targets latest stable toolchain (Vite 7 + `@vitejs/plugin-vue` 6 + `vite-plugin-vuetify` 2.1 + optional devtools). Ensure Node 20+ before running installs; revisit dependencies if new breaking releases appear.
- **Docs note**: before implementing or adjusting Vuetify components, consult the upstream documentation mirrored under `migration/vuetify/packages/docs`; reference the full `migration/vuetify` repo for implementation details when needed.
