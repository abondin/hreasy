---
name: hreasy-vue3-development
description: Develop and refactor the HREasy Vue application under web/ using its Vue 3, Vuetify 4, Pinia, Vue Router 5, and vue-i18n conventions. Use for Vue feature work, component changes, service or store changes, and UI bug fixes in this repository.
---

# HREasy Vue Development

Follow `web/AGENTS.md`. Before editing:

1. Search `web/src/views`, `components`, `composables`, `stores`, `services`, and `lib` for an existing implementation.
2. Extend the closest existing component or module when that stays simpler than adding a parallel one.
3. Check route, menu, and backend-permission impact.

Keep API calls in existing service modules, shared state in Pinia stores, and page-specific orchestration local to the page or a composable. Use Vuetify primitives before custom layout code and keep user-facing text in i18n.

Read `references/reuse-checklist.md` only when the reuse decision is unclear.

After meaningful changes, run the narrowest relevant commands from `web/`:

- `npm run type-check`
- `npm run lint`
- `npm run test:unit -- --run` when logic changed
- `npm run check:win-text-integrity` after risky non-ASCII edits
