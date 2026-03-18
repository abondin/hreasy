# Vue 3 Migration Tracker

_Last updated: 2026-03-18_

This document records the current technical baseline and the roadmap for migrating the HREasy web client from Vue 2 to Vue 3, together with related ecosystem upgrades. Keep it updated as work progresses so future sessions can resume from the latest state.

Repository layout update (2026-03-03):
- Vue 3 app moved to repository root.
- Legacy Vue 2 app moved to `legacy/vue2`.
- Docker serves Vue 2 at `/` and Vue 3 at `/new/`.

## 1. Actual Migration Status (as of 2026-03-18)

### Fully ready in Vue 3 skeleton

- **Build/runtime platform**: Vue 3 app in repository root on Vite 7 + Vue 3.5 + Vuetify 3.11 + Pinia 3 + vue-router 4 + vue-i18n 11; production build works under `/new/` and is already packaged in Docker together with legacy Vue 2 build.
- **Application shell and auth flow**: Router, route guards, login/logout, authenticated layout, and not-found route are implemented and wired.
- **Core migrated functional blocks**:
  - Employees directory view with virtual table, filters, and expanded employee card details.
  - Profile main view with avatar editing, Telegram edit dialog, tech profiles card, and skills section.
  - Vacations module: list, summary, timeline, request/edit dialogs, and profile-side MyVacations block.
- **Primary service layer for migrated screens**: auth, employees, skills, vacations, projects, dictionary, office map, tech profile, overtime, assessment, salary, junior registry, and core admin services exist in Vue 3 skeleton.

### Implemented in Vue 3, but parity and regression coverage still incomplete

- **Overtimes**: Vue 3 summary page, employee details dialog, period switching, export, and period close/reopen flows are implemented.
- **Assessments**: Vue 3 list, employee details, and assessment details routes are implemented, but still require parity QA and broader test coverage.
- **Salary requests**: Vue 3 request list, latest requests view, details route, create flow, export, and period controls are implemented, but still require parity QA and broader test coverage.
- **UDR / Junior registry / Mentorship**: Vue 3 registry list and details flows are implemented, but still require parity QA and broader test coverage.
- **Admin employees and dictionaries**: Vue 3 employees admin, kids admin/import flows, and dictionaries area (including office locations/maps) are implemented, but still require parity QA and broader test coverage.
- **Admin projects**: Vue 3 projects list, details route, create/edit flow, and project-scoped managers CRUD are implemented, but still require parity QA and broader test coverage.
- **Admin managers and business accounts**: Vue 3 standalone managers page plus business accounts list/details/forms/positions flows are implemented, but still require parity QA and broader test coverage.
- **Admin users**: Vue 3 users/roles management page is implemented, but still requires parity QA and broader test coverage.
- **Articles/news**: Vue 3 admin articles CRUD and shared profile articles feed are implemented, but still require parity QA and broader test coverage. Remaining known gap: admin article image upload UI is not yet ported.
- **Telegram confirmation route**: dedicated Vue 3 confirmation view and route are implemented.

### Not ready at all in Vue 3 (no implemented module parity yet)

- **Timesheets**: no Vue 3 screens/services. This module is explicitly excluded from the current migration scope for now because the legacy Vue 2 implementation is itself not sufficiently worked through to serve as a stable parity target.
- **Global cutover readiness**: Vue 2 app remains the default app; there is no production switch-over to Vue 3 as primary frontend.

### In progress, but not complete

- **Testing**: Vue 3 has baseline Vitest/Playwright setup, but automated coverage is still minimal (single component unit test + single login redirect E2E).
- **State migration completeness**: auth is fully migrated; most remaining domain logic lives in composables/service orchestration, but broad store/composable parity and test coverage are still pending.
- **DI/plugin convergence**: shared dependency injection pattern between Vue 2 and Vue 3 is not finalized.
- **UI/UX parity**: migrated features still require side-by-side parity QA against legacy behavior, especially in overtime, salary, assessments, mentorship, and admin modules.

### Explicit scope decision: Timesheets

- **Do not migrate `timesheet` yet.**
- The legacy Vue 2 timesheet area (`legacy/vue2/src/components/ts/**`) is not treated as a mature or sufficiently validated baseline for parity work.
- Until the product expectations for timesheets are clarified, this module stays outside the active Vue 2 -> Vue 3 migration queue.
- Timesheet-related references may remain in planning or QA notes for historical context, but they must not drive near-term implementation priorities.

## 2. Current Stack Snapshot

- **Vue entrypoint**: Vue 2.7 + class components bootstrapped via `new Vue` with global singletons (`src/main.ts:1`).
- **App shell**: Vuetify 2.6 and vue-router 3 configured inside the root component; relies on `$vuetify` theme mutation and legacy slot APIs (`src/App.vue:212`, `src/router/index.ts:113`).
- **State management**: Vuex 3 modules with decorator accessors, plus direct store imports inside services (e.g. permissions) (`src/store/modules/auth.ts:27`, `src/store/modules/permission.service.ts:1`).
- **Internationalisation**: vue-i18n 8 using webpack `require.context` for JSON locales (`src/i18n.ts:8`).
- **HTTP layer**: Axios 0.21 singleton with custom error mapping classes (`src/components/http.service.ts:5`, `src/components/errors.ts:1`).
- **Date/time utilities**: Extensive Moment.js usage across vacation, overtime, and shared components (`src/components/datetimeutils.ts:1`, `src/components/vacations/VacationEditForm.vue:244`).
- **Tooling**: Vue CLI 5 (webpack), Jest 27, ESLint 7, TypeScript 4.5 (`package.json:1`, `tsconfig.json:3`).

## 3. Migration Goals

1. Upgrade core framework to Vue 3 (3.4+) with Composition API first patterns.
2. Replace Vuex 3 with Pinia 2 while preserving existing business logic.
3. Migrate Vuetify 2 components/layouts to Vuetify 3 equivalents.
4. Modernise supporting libraries (vue-router 4, vue-i18n 9, Axios 1.x, etc.).
5. Switch build tooling from Vue CLI/webpack to Vite 5, update TS/ESLint/Jest stacks.
6. Reduce bundle size by replacing Moment.js and other deprecated Vue 2 plugins.

### Migration Exception: Table UI Standardization (added 2026-03-05)

- Behavioral parity with Vue 2 remains mandatory.
- Table visual/layout parity is **not** required per-screen when legacy tables differ from each other.
- For Vue 3, table structure must be unified across modules (toolbar layout, filters placement, actions area, loading/disabled behavior, density/pagination defaults, empty/loading states).
- Reference pattern for this unification: Vue 2 page **"Повышения и бонусы"** (`legacy/vue2/src/components/salary/SalaryRequestsTable.vue`).
- If a legacy table conflicts with the standard pattern, keep business behavior and permissions, but align UI structure with the standard Vue 3 table pattern.
- If implementing a UI request would noticeably increase code size/complexity, pause and align with the user on scope before applying a large refactor.

## 4. Roadmap & Status

Use the checkboxes to mark completion; add notes/dates next to items as you progress.

### Phase A – Preparation

- [x] Capture baseline architecture, dependencies, and migration goals (this document).
- [x] Define environment prerequisites (Node 20+, package manager strategy) and update documentation (`README.md:9`).
- [x] Establish regression safety net: prioritised manual test plan + coverage gap assessment (see §6).
- [x] Identify critical components that may block Vuetify 3 upgrade (see §7).

### Phase B – Incremental Refactors on Vue 2.7

- [x] Introduce Composition API usage (`@vue/composition-api` or Vue 2.7 native) in new/isolated components (`src/components/PageNotFoundComponent.vue:12`).
- [ ] Wrap global singletons (logger, permissions, http service) into plugin factories to ease DI _(Vue 3 skeleton still exports Axios singleton from `src/lib/http.ts`; plan to promote to `app.provide` plugin so both builds can share DI/mocking)_.
- [ ] Start extracting domain-specific composables (auth, permissions, vacations) mirroring Vuex modules _(Auth store + `useEmployeeProfile` composable live in `src/stores/auth.ts` and `src/composables/useEmployeeProfile.ts`; need parity for vacations, overtime, salaries, dictionaries)_.
- [ ] Replace Moment.js usage with Day.js or date-fns while staying on Vue 2 where possible _(Vue 3 skeleton ships ad-hoc formatters in `src/lib/datetime.ts`, legacy modules still rely on Moment)_.
- [x] Implement Vue 3 employees directory: new `useEmployeesDirectory` composable + `EmployeesVirtualTable` + route/filters/dialog to browse all employees with card view. _Delivered in `src/views/employees/**`; still needs parity QA and broader regression testing._
- [x] Port vacations module: list/summary/timeline view + MyVacations + request/edit dialogs + date-range filters + export. _Delivered in `src/views/vacations/VacationsView.vue` and `src/components/vacations/**`; still needs parity QA and broader regression testing._
- [x] Port overtime module: summary page, employee details dialog, export, and period management. _Delivered in `src/views/overtimes/**` and `src/components/overtimes/**`; still needs parity QA and broader regression testing._
- [x] Port assessments module: summary, employee assessments, and assessment details flows. _Delivered in `src/views/assessment/**` and related services/components; still needs parity QA and broader regression testing._
- [x] Port salary module: requests list, latest requests, details, create flow, export, and period management. _Delivered in `src/views/salary/**` and related composables/services; still needs parity QA and broader regression testing._
- [x] Port mentorship / junior registry module. _Delivered in `src/views/mentorship/**` and related composables/services; still needs parity QA and broader regression testing._
- [x] Port admin employees and dictionaries modules. _Delivered in `src/views/admin/employees/**`, `src/views/admin/dicts/**`, and related services/components; still needs parity QA and broader regression testing._
- [x] Port admin projects module. _Delivered in `src/views/admin/projects/**` and related admin services/components; still needs parity QA and broader regression testing._
- [x] Port admin managers and business accounts modules. _Delivered in `src/views/admin/managers/**`, `src/views/admin/business-accounts/**`, and related services/components; still needs parity QA and broader regression testing._
- [x] Port admin users module. _Delivered in `src/views/admin/users/**` and related services/components; still needs parity QA and broader regression testing._
- [x] Port articles/news module. _Delivered in `src/views/admin/articles/**`, `src/components/article/SharedArticlesCard.vue`, and related services; still needs parity QA and broader regression testing. Known follow-up: admin article image upload UI parity._

### Phase C – Store & Routing Transition

- [ ] Scaffold Pinia stores equivalent to existing Vuex modules; provide adapters for legacy decorator access _(Auth store + permissions helpers migrated under `src/stores/auth.ts` & `src/lib/permissions.ts`; most new domain logic currently sits in composables; evaluate where shared state should move into Pinia next)_.
- [x] Refactor router setup to Vue 3-compatible factory (`createRouter`, `createWebHistory`), remove `Vue.use` _(Implemented in Vue 3 skeleton `src/router/index.ts`; legacy Vue 2 router still exists in the main app)_.
- [ ] Move guard logic to composables/testable units; ensure auth flow covered by unit/integration tests _(Route guard now centralised in Vue 3 skeleton; add unit specs + synchronise logout/login flows with Vue 2 app)_.

### Phase D – Tooling & Build Migration

- [x] Create Vite-based build (Vue 3, TypeScript 5, ESLint 9, Vitest/Jest 29) running side-by-side _(Vue 3 skeleton under `./` runs on Vite 7 + Pinia 3 + Vuetify 3; adds Vitest/Playwright configs)_.
- [x] Package Docker release with nginx serving Vue 2 at `/` and Vue 3 under `/new/` _(Dockerfile builds both bundles on `node:20-bullseye-slim` and ships them via an `nginx:alpine` runtime image; follow-up: add automated smoke check before publishing images)_.
- [ ] Update lint/test pipelines; ensure CI scripts support both builds during transition _(Vitest + Playwright commands exist; wire into CI and keep Vue CLI Jest until full cutover)_.
- [ ] Migrate webpack-specific helpers (e.g. `require.context` for locales) to Vite-friendly alternatives _(i18n ported via `import.meta.glob` in `src/i18n.ts`; remaining dynamic imports/asset helpers still depend on webpack in legacy code)_.

### Phase E – UI Stack Upgrade

- [ ] Upgrade Vuetify to v3 and refactor affected components/templates (navigation, forms, dialogs) _(Vue 3 skeleton already renders auth + profile flows on Vuetify 3; bulk port of legacy modules still pending)_.
- [ ] Replace deprecated Vue 2 plugins: `vue-clipboard2`, `vue-upload-component`, `vue-avatar-cropper` _(Custom replacements delivered in Vue 3 skeleton: `FileAttachmentsCard` + `FileUploadZone` + `ProfileAvatar`; vacations now use native Clipboard API)_.
- [ ] Validate styling/theme parity and rebuild global theming config via `createVuetify` _(Skeleton theme configured in `src/plugins/vuetify.ts`; align tokens with legacy before cutover)_.

### Phase F – Final Cutover & Cleanup

- [ ] Switch default build to Vue 3 + Vite; remove Vue CLI configuration and obsolete dependencies.
- [ ] Retire Vuex modules/decorators once Pinia adoption is complete.
- [ ] Update documentation (README, onboarding) with new commands and architecture notes.
- [ ] Perform post-migration audit (bundle size, performance, accessibility) and file follow-up tasks.

## 5. Open Questions / Risks

- Validate `vis-timeline` integration under Vue 3 and confirm performance with large datasets (timeline now ported in Vue 3 skeleton).
- Validate new uploader/cropper implementations (`src/components/FileUploadZone.vue`, `.../ProfileAvatar.vue`) against production API limits and security requirements.
- Assess backend API contract changes required (if any) when upgrading Axios/interceptors.
- Determine whether to adopt TypeScript strictness increases (e.g., `exactOptionalPropertyTypes`) during tooling upgrade.
- Clarify future product scope for timesheets before any Vue 3 implementation starts; current migration plan intentionally excludes this module.

Keep this file under version control to track progress between sessions.

## 6. Environment & Testing Notes

- **Runtime baseline**: Adopt Node.js 20 LTS (current CLI tested on 18.16, upgrade recommended) and npm 10+. Document any Docker changes under `devops/`.
- **Local services**: Frontend dev server expects `VITE_DEV_SERVER_PROXY` to point to the API (`README.md`); ensure backend mock or staging endpoint is available for migration smoke tests.
- **Local access**: Vue 3 dev app is served at `http://localhost:5173/`, Vue 2 dev app at `http://localhost:8080/` (from `legacy/vue2`). Both builds point to the same backend API. Test login credentials: `alexander.bondin` / `qwe123`.
- **Testing status**: Legacy Jest coverage is still sparse, and Vue 3 automated coverage is also still thin. Need manual regression checklist and targeted automated coverage for auth, vacations, overtime, salaries, assessments, mentorship, admin flows, and articles/news.
- **Short-term actions**: Draft target matrix of browsers/devices post-Vue 3 upgrade and decide on E2E tooling (Cypress or Playwright) before major refactors begin.

## 7. Regression Scenarios & QA Plan

Use this list as a smoke suite before/after significant migration steps. Expand with automated coverage as new tests are implemented.

- **Authentication**: Login with valid/invalid credentials, verify return path redirect and logout flow (`src/components/login/Login.vue:46`).
- **Employee profile & navigation**: Load profile dashboard, toggle navigation drawer, validate permission-gated menu visibility (`src/App.vue:24`, `src/store/modules/permission.service.ts:5`).
- **Vacations**: Request new vacation, edit/auto-calc dates, review timeline view and filters (now also in Vue 3 skeleton under `src/views/vacations/VacationsView.vue`).
- **Overtimes**: Add overtime entry, approve/reject, export summaries; ensure month navigation and formatting works (`src/components/overtimes/AddOvertimeItemDialog.vue:204`).
- **Salary requests**: Submit/approve flows, inspect overview tables (`src/components/salary/SalaryRequestsTable.vue`, `src/components/salary/details/SalaryRequestDetailsView.vue`).
- **Assessments**: Validate summary filters, scheduling, details view actions, attachments, and export behavior.
- **Mentorship / UDR**: Validate registry filtering, add/update flows, reports history, and graduation behavior.
- **Admin modules**: Projects CRUD, users/roles management, business account details, dictionary management, employees import workflows (`src/components/admin/**`).
- **Articles/news**: Validate admin article CRUD, markdown rendering, publication visibility, and profile-side shared articles feed.
- **Uploads & assets**: File uploader success/error states, avatar cropper, tech profile download/upload permissions (`src/components/shared/MyFileUploader.vue:45`).
- **Telegram confirmation**: Validate deep link route and success/failure messaging (`src/components/telegram/TelegramConfirmationPage.vue`).
- **Error handling**: Trigger forced backend errors to confirm global alert surface (`src/components/http.service.ts:14`, `src/App.vue:215`).
- **i18n toggles**: Switch locales and confirm translations load correctly once dynamic imports are introduced (`src/i18n.ts:8`).

_Automation gaps_: existing Jest specs cover only helper utilities (`tests/unit/datetimeutils.spec.ts`, etc.). Plan to add unit tests around new composables and Pinia stores, and introduce E2E (Cypress/Playwright) for the high-risk migrated flows above. Do not schedule timesheet automation work until the module is brought back into scope.

## 8. Vuetify & Legacy Plugin Hotspots

Focus migration spikes here before tackling the long tail.

- **Data tables everywhere**: `v-data-table` underpins employees, vacations, overtime, projects, assessments, admin lists (`src/components/empl/Employees.vue:40`, `src/components/vacations/VacationsList.vue:128`, `src/components/admin/project/AdminProjects.vue:53`, etc.). Need Vuetify 3 equivalents or custom table abstraction.
- **Timeline visualisation**: `vis-timeline` integration drives the vacations timeline with direct DOM manipulation and CSS overrides (`src/components/vacations/VacationsTimeline.vue:12`). Assess Vue 3 wrappers or alternative libs (e.g., `vis-timeline` + Composition API, `apexcharts`).
- **File/Media handling**: File uploader based on `vue-upload-component` (`src/components/shared/MyFileUploader.vue:45`) and avatar cropper (`src/components/empl/EmployeeAvatarUploader.vue:46`) are Vue 2 only. Vue 3 skeleton now provides `FileUploadZone` + `FileAttachmentsCard` + `ProfileAvatar`; verify they cover vacations/overtime flows before removing legacy plugins.
- **Clipboard utilities**: `vue-clipboard2` is globally registered through Vuetify plugin bootstrap (`src/plugins/vuetify.ts:5`) and used in vacations copy-to-clipboard (`src/components/vacations/VacationsList.vue:415`). Replace with native Clipboard API or VueUse `useClipboard`.
- **Moment-heavy formatting**: business-critical date logic in vacations, overtime, salary modules (`src/components/datetimeutils.ts:1`, `src/components/overtimes/overtime.service.ts:117`). Plan staged replacement with Day.js/date-fns before migrating to Vuetify 3 date pickers.

## 9. Vue 3 Workspace

- **Location**: repository root (`./`)
- **Stack**: Vite 7, Vue 3.5, Pinia 3, Vue Router 4.6, Vuetify 3.10 (`package.json`)
- **Quick start**:
  ```shell
  npm install
  # optional env vars for dev proxy / axios baseURL
  export VITE_DEV_SERVER_PROXY=http://localhost:8081
  export VITE_API_BASE_URL=/api/
  npm run dev
  ```
- **Current scope**: authenticated shell with guarded router (`src/router/index.ts`), Pinia auth store, Vuetify 3 navigation (`src/App.vue`). Employee profile view now ports avatar upload with cropper, Telegram edit dialog, tech profile attachments, office map preview, permissions logic, MyVacations, and shared articles feed (`src/views/profile/**`, `src/components/article/SharedArticlesCard.vue`). Vacations module now includes list/summary/timeline view + request/edit dialogs (`src/views/vacations/VacationsView.vue`, `src/components/vacations/**`).
- **Expanded migrated scope**: Vue 3 also includes overtime, assessments, salary requests, mentorship/junior registry, telegram confirmation, admin employees, admin dictionaries, admin projects, admin managers, admin business accounts, admin users, and admin articles flows under `src/views/**`.
- **Testing status**: Vitest configured but no component specs yet; Playwright E2E covers unauthenticated redirect (`e2e/login.spec.ts`). ResizeObserver polyfill wired via `tests/setup.ts`.
- **Next actions**:
  1. Run parity QA on already migrated Vue 3 modules: vacations, overtimes, assessments, salary, mentorship, admin employees, and admin dictionaries.
  2. Backfill unit tests for migrated composables and route-level flows; prioritize the highest-risk business logic before adding broad UI coverage.
  3. Run parity QA on the newly migrated admin/article areas: admin projects, admin managers, admin business accounts, admin users, admin articles, and profile-side shared articles.
  4. Close the remaining known article parity gap: admin article image upload UI.
  5. Keep `timesheet` out of implementation scope until product expectations and a reliable baseline are clarified.
  6. Ensure shared DI plugins (http, logger, permissions) can be consumed by both Vue 2 and Vue 3 builds; document deployment toggle and add CI jobs to run `npm run lint`, `npm run type-check`, and `npm run test:e2e` for the skeleton.
- **Tooling note**: project targets latest stable toolchain (Vite 7 + `@vitejs/plugin-vue` 6 + `vite-plugin-vuetify` 2.1 + optional devtools). Ensure Node 20+ before running installs; revisit dependencies if new breaking releases appear.
- **Docs note**: before implementing or adjusting Vuetify components, consult the upstream documentation mirrored under `migration/vuetify/packages/docs`; reference the full `migration/vuetify` repo for implementation details when needed.
