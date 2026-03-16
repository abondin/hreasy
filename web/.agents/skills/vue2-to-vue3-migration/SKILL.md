---
name: vue2-to-vue3-migration
description: Migrate frontend modules from Vue 2 to Vue 3 with behavior parity, including router/store/service/component/test updates. Use when requests mention porting pages from legacy Vue 2 codebases (Vue Router 3, Vuex, Vuetify 2) into Vue 3 stacks (Vite, Vue Router 4, Pinia, Vuetify 3), or when planning/implementing a phased migration.
---

# Vue2 To Vue3 Migration

## Overview

Execute safe, incremental page migration from Vue 2 to Vue 3.
Preserve behavior parity first, then improve structure and UX.
Treat user-facing behavior and access-control behavior as strict parity targets by default.

## Quick Start

1. Inspect legacy route and all directly related components/services/store modules.
2. Port contracts and API calls to Vue 3 `services/` first.
3. Port state to Pinia/composables.
4. Port UI to Vue 3/Vuetify 3 and wire route guards/permissions.
5. Run parity checks and minimum tests before marking done.

For a detailed checklist and mapping table, read `references/hreasy-migration-checklist.md`.

## Non-Negotiable Migration Rules

1. Keep user-facing functionality as close as possible to legacy behavior by default.
2. Keep access restrictions and permission checks as close as possible to legacy behavior by default.
3. Keep route visibility, action visibility, and server-side action eligibility aligned with legacy intent.
4. Do not silently simplify or drop behavior just because implementation is large.
5. If as-is migration is expensive or risky, stop and ask the user before proceeding with a reduced implementation.
6. Exception for table UI: when legacy tables differ across modules, unify Vue 3 table structure to the shared project standard instead of copying each legacy variant.
7. If implementation required to satisfy a UX request significantly increases code size or conceptual complexity, pause and align with the user before proceeding.
8. When migrating Vuetify UI, prefer built-in Vuetify features/props/slots over custom CSS/JS overrides unless there is a documented blocker.
9. Before migrating or adjusting layout/grid behavior, check official Vuetify Grid docs first (`https://vuetifyjs.com/en/components/grids/#usage`) and prefer `v-container`/`v-row`/`v-col` props (`auto`, breakpoint columns, alignment) over custom CSS-based layout overrides.
10. Detail pages must be visually standardized across domains; when legacy pages differ, migrate behavior but align to one shared Vue 3 detail-page layout pattern.
11. On Windows, preserve UTF-8 encoding for all migrated files (prefer no BOM); avoid PowerShell 5.1 implicit-encoding write commands (`Set-Content`, `Add-Content`, `Out-File`, `>`, `>>`) for source files.

Table UI standard reference:

- Source pattern: `legacy/vue2/src/components/salary/SalaryRequestsTable.vue` ("Raises and Bonuses").
- Preserve table behavior parity (data, permissions, actions, filters logic), but align Vue 3 table layout/interaction patterns across modules (toolbar/actions/filters/loading/empty states).
- Verify component capabilities in Vuetify docs before custom implementation: `https://vuetifyjs.com/en/components/`.

Detail page UI standard reference:

- Use a centered and width-limited detail container (target around `max-width: 1280-1360px`).
- In the primary info card, keep a stable two-column layout:
  - left: reusable employee/profile summary block with natural/fixed width.
  - right: business/domain details filling remaining width.
- Keep card sequence consistent across migrated modules: primary info -> action cards -> history/relations.
- Prefer extracting a shared detail layout wrapper in `src/components/shared` when 2+ modules follow this pattern.
- Treat current mentorship detail page proportions as the baseline visual rhythm for future detail pages.

When asking the user, present explicit options and tradeoffs:

1. Full parity now (slower, larger diff, lower regression risk).
2. Phased parity (deliver core flow now, close remaining gaps in planned follow-up steps).
3. Temporary simplification with documented functional gaps.

## Migration Workflow

### 1. Capture legacy behavior before coding

- Read route entry in `legacy/vue2/src/router/index.ts`.
- List all dependencies for the page:
  - `legacy/vue2/src/components/**`
  - `legacy/vue2/src/store/modules/**`
  - shared helpers (`datetimeutils`, table helpers, permissions, error mapping)
- Record behavior-critical details:
  - required permissions
  - who can see route/menu items
  - who can execute create/edit/delete/approve actions
  - filtering/sorting defaults
  - error/empty/loading states
  - dialogs and confirmation steps

### 2. Port API contracts first

- Create or update `src/services/<domain>.service.ts`.
- Keep endpoint paths and payload/response shape parity.
- Reuse shared Axios client (`src/lib/http.ts`) and shared error classes.
- Return typed results from service functions.

### 3. Port state and domain logic

- Move Vuex module logic to Pinia store where state is shared.
- Move page-local behavior to composables.
- Keep transformations pure and typed.
- Avoid mixing HTTP calls directly inside view components.

### 4. Port UI structure to Vue 3

- Build route-level page in `src/views/<domain>/`.
- Extract reusable blocks into `src/components/<domain>/`.
- Replace legacy Vuetify 2 patterns with Vuetify 3 equivalents.
- Keep all user text in i18n keys; do not hardcode strings.

### 5. Wire routing and permissions

- Add/adjust route in `src/router/index.ts`.
- Keep guard semantics equivalent to legacy.
- Keep navigation visibility aligned with permission checks.
- Match access behavior at three layers:
  - route access (guard)
  - UI affordances (buttons/menu/actions visibility)
  - action execution constraints (service calls and backend error handling)

### 6. Keep i18n and locales clean

- Store Vue 3 locale files in `src/locales/*.json`.
- Use `useI18n` (`t`) in script setup and `$t` in templates.
- Add missing keys during migration instead of inlining text.

### 7. Validate parity

- Run:
  - `npm run type-check`
  - `npm run lint`
  - targeted unit tests (`npm run test:unit`)
- Run manual smoke checks for migrated route:
  - auth + permission gating
  - list/details/actions parity
  - error and empty state behavior

### 8. Mark completion criteria

Consider page migration complete only when:

- Route is available in Vue 3 app.
- Legacy-equivalent core actions work.
- Permission gating matches legacy intent.
- User-facing behavior differences are either removed or explicitly approved by the user.
- i18n keys are complete for all user-visible text.
- Type-check and lint are clean.

If full as-is parity is not feasible in current scope:

1. Pause implementation at the nearest safe checkpoint.
2. Report exactly what cannot be ported directly and why.
3. Ask the user to choose one of the predefined options (full parity, phased parity, temporary simplification).
4. Proceed only after explicit user choice.

## HREasy Project Conventions

- Main app: Vue 3 in repo root.
- Legacy app: `legacy/vue2/`.
- Use this migration order unless told otherwise:
  1. Overtimes
  2. Salary requests
  3. Timesheets and assessments
  4. Juniors registry
  5. Admin modules
- Keep comments/doc comments in English.
- Prefer `@/` imports.
- For scripted writes on Windows, enforce UTF-8 explicitly:
  - `[System.IO.File]::WriteAllText($path, $text, [System.Text.UTF8Encoding]::new($false))`

## Output Format for Migration Tasks

When executing this skill, respond with:

1. Migration scope (legacy files and target Vue 3 files).
2. Concrete implementation steps.
3. What was changed.
4. Validation results (`type-check`, `lint`, tests/manual checks).
5. Remaining parity gaps, if any.
