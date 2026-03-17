---
name: hreasy-vue3-development
description: Develop and refactor features in the HREasy Vue 3 application (root project) using project conventions, Vuetify 3, Pinia, Vue Router 4, and vue-i18n. Use for any Vue 3 coding task in this repository, including new pages, component updates, service/store changes, bug fixes, and UI behavior changes.
---

# HREasy Vue3 Development

## Overview

Implement Vue 3 tasks in this repository with stable architecture and minimal regressions.
Apply a reuse-first strategy: extend existing components, composables, stores, and services before creating new ones.

## Non-Negotiable Rules

1. Reuse existing code first.
2. Create new component/service/composable/store only when reuse is clearly unsuitable.
3. Keep all user-facing text in i18n keys (`src/locales/*.json`).
4. Keep comments and doc comments in English.
5. Keep `type-check` and `lint` clean for Vue 3 app.
6. For data tables, prefer project-wide UI consistency over legacy per-page visual differences.
7. Keep implementation transparent and simple; if a requested change pushes toward a large or complex refactor, stop and confirm scope with the user first.
8. Prefer built-in Vuetify component capabilities over custom CSS/JS workarounds whenever possible.
9. For key interactive UI controls used in user flows, add stable E2E markers (`data-testid`) and keep marker naming semantic and reusable.
10. Before changing page layout or grid behavior, verify official Vuetify docs first (especially Grid usage: `https://vuetifyjs.com/en/components/grids/#usage`) and prefer `v-container`/`v-row`/`v-col` props (`auto`, breakpoint columns, alignment) over custom CSS layout fixes.
11. Detail pages (route `.../:id` style pages) must follow one shared visual structure across modules; do not invent per-page layouts.
12. On Windows, all edited files must remain UTF-8 (prefer no BOM); avoid PowerShell 5.1 write commands with implicit encoding (`Set-Content`, `Add-Content`, `Out-File`, `>`, `>>`) unless encoding is explicitly controlled.

### Stop Rule: Non-ASCII File Safety

- If a file contains Cyrillic or any non-ASCII text, do not use PowerShell regex/content rewrites, here-strings, pipeline-based text transforms, or intermediate shell variables to rewrite the file.
- Use `apply_patch` by default for non-ASCII source/markdown files.
- If a scripted edit is unavoidable, use a Unicode-safe writer and prefer ASCII-only literals with `\uXXXX` escapes for inserted non-ASCII text.
- Do not trust console output as encoding verification.
- If you already performed an unsafe rewrite on a non-ASCII file, stop and repair the file before continuing with feature work.
- After any scripted edit of a non-ASCII file, run an explicit encoding sanity check before `type-check`/`lint`/tests.

Table standardization note:

- Use the Vue 2 "Raises and Bonuses" table (`legacy/vue2/src/components/salary/SalaryRequestsTable.vue`) as a structural reference for Vue 3 table UI.
- Keep business behavior/permissions intact while aligning table skeleton (toolbar, action placement, filters block, loading/disabled/empty states).
- Before adding custom behavior/styles, check Vuetify component docs first: `https://vuetifyjs.com/en/components/`.
- For employee list pages (for example `/employees`, `/admin/employees`, and similar directories), use `src/components/shared/HREasyTableBase.vue` by default. Differences between pages should be expressed via columns, filters, and row-click behavior rather than custom table layouts.

Detail page standardization note:

- Use a centered content frame with limited width (target around `max-width: 1280-1360px`) for all detail pages.
- First detail card must use a two-column layout:
  - left column: standard profile block (`ProfileSummary` or equivalent reusable profile card) with natural/fixed width (`lg="auto"`).
  - right column: all domain-specific fields taking the remaining width (`lg` grow column).
- Keep section ordering consistent: summary/info -> domain actions (implementation/approvals/etc.) -> history/related entities.
- Prefer `v-row`/`v-col` responsive composition over ad-hoc CSS widths.
- If detail pages in multiple modules share the same skeleton, extract/reuse a shared layout wrapper in `src/components/shared` instead of duplicating structure.

## Reuse-First Protocol

Before implementing any Vue 3 task, do this in order:

1. Search for existing UI blocks in `src/components/**` and `src/views/**`.
2. Search for existing data access in `src/services/**`.
3. Search for existing state/composables in `src/stores/**` and `src/composables/**`.
4. Prefer extension points:
   - add props/events/slots
   - extract shared logic to composables
   - add optional behavior flags
5. Only create new files when the feature is truly new or would overcomplicate existing modules.

If reuse is not chosen, explicitly state why in task output.

## Implementation Workflow

### 1. Scope and impact

- Identify target route/page and dependent modules.
- Identify whether change is UI-only, domain-logic, or both.
- Check permission and navigation impact.

### 2. Service and domain logic

- Keep API interactions in `src/services/*.service.ts`.
- Keep shared state in Pinia `src/stores/*.ts`.
- Keep page-specific orchestration in composables.

### 3. UI implementation

- Route-level components in `src/views/**`.
- Reusable blocks in `src/components/**`.
- Use Vuetify 3 patterns consistent with existing project code.
- Do not hardcode user text.
- For controls that E2E scenarios interact with (auth actions, primary navigation, submit/cancel buttons, critical dialogs/forms), prefer explicit `data-testid` markers over text-based selectors.
- Keep shared selector ids centralized in `e2e/support/selectors.ts` when they are reused across specs.

### 4. Routing and access

- Update `src/router/index.ts` when route behavior changes.
- Keep route guards and menu visibility consistent with permissions.

### 5. Validation

Run after substantial changes:

- `npm run type-check`
- `npm run lint`
- `npm run test:unit` when logic changed

## Project-Specific Constraints

- Main active app is Vue 3 in repository root.
- Legacy app lives in `legacy/vue2/` and should not be modified unless explicitly requested.
- Root lint/type-check should target Vue 3 codebase, not legacy.
- If scripted file writes are needed on Windows, use explicit UTF-8 no BOM writes:
  - `[System.IO.File]::WriteAllText($path, $text, [System.Text.UTF8Encoding]::new($false))`

## Required Output Format

When executing this skill, report:

1. Reuse decisions (what was reused/extended).
2. New files added and why they were necessary.
3. Behavior/access impacts.
4. Validation results (`type-check`, `lint`, tests).
5. Follow-up risks or parity gaps.
