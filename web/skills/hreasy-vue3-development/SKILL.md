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

## Required Output Format

When executing this skill, report:

1. Reuse decisions (what was reused/extended).
2. New files added and why they were necessary.
3. Behavior/access impacts.
4. Validation results (`type-check`, `lint`, tests).
5. Follow-up risks or parity gaps.
