Component: src/components/PageNotFoundComponent.vue

Purpose
- Simple 404 page with warning alert and link back to home.

Template/UI
- Vuetify v-card containing v-alert (warning, outlined, colored border).
- Shows i18n keys: Запрошенная страница не найдена, Вернуться на главную.
- router-link to '/'.

State & Props
- No props, no local state.

Permissions / Visibility
- No explicit permission checks.

Notes for Vue 3 migration
- Preserve alert styling and route link.
- Keep i18n keys unchanged.
