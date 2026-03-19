# HREasy Vue2 -> Vue3 Migration Checklist

## File Mapping

- Legacy route: `legacy/vue2/src/router/index.ts`
- Vue 3 route: `src/router/index.ts`
- Legacy store: `legacy/vue2/src/store/modules/*.ts`
- Vue 3 store: `src/stores/*.ts`
- Legacy API/services: `legacy/vue2/src/components/**/**.service.ts`
- Vue 3 API/services: `src/services/*.service.ts`
- Vue 3 i18n: `src/locales/*.json`

## Library Mapping

- `vue-router@3` -> `vue-router@4`
- `vuex@3` -> `pinia`
- `vuetify@2` -> `vuetify@4`
- Vue class components -> Composition API (`script setup` + composables)

## Per-Page Porting Checklist

1. Locate legacy route and component tree.
2. List all API methods used by the page.
3. Port service contracts into `src/services`.
4. Port state logic into Pinia/composables.
5. Port page UI into `src/views` + `src/components`.
6. Port guards/permissions/navigation links.
7. Port i18n keys (no inline user-facing text).
8. Add at least one unit test for non-trivial logic.
9. Run `npm run type-check` and `npm run lint`.
10. Manually verify core happy path + error path.
11. For table-heavy pages: keep behavior parity, but align table UI to the shared Vue 3 standard (reference: `legacy/vue2/src/components/salary/SalaryRequestsTable.vue`).

## Parity Risks to Check Explicitly

- Permission mismatch (buttons visible but action forbidden, or opposite).
- Silent differences in date formatting/period boundaries.
- Missing confirmation dialogs before destructive actions.
- Missing loading and empty states.
- Changed table sorting/filtering defaults.
- Diverging table layout patterns between migrated pages (toolbar/actions/filters placement mismatch).
- Differences in file upload and avatar workflows.

## Definition of Done

- Functional parity for critical user flows is verified.
- No TypeScript errors in Vue 3 app.
- No ESLint errors in Vue 3 app.
- Route is discoverable from UI navigation.
- Known gaps are documented in task output.
