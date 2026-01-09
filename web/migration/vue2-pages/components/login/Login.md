Component: src/components/login/Login.vue

Purpose
- Login form for the app; submits credentials and routes to return path or root.

Template/UI
- Vuetify layout: centered card with v-form, two v-text-field inputs (email/login + password), error text, submit button.
- Uses i18n keys: Вход_в_систему, E-mail, Пароль, Войти.
- Shows responseError inside v-card-text with class error--text.
- v-btn disabled when loading.

State & Props
- Local reactive fields: loginField (string), passwordField (string).
- Local state: loading (boolean), responseError (string).
- Vuex state: authState from module auth (unused in template).

Actions / Services
- Vuex action: auth/login (loginAction).
- Creates LoginRequest(loginField, passwordField) from store/modules/auth.service.
- Error formatting via errorUtils.shortMessage.

Routing
- On successful login, pushes router to returnPath query param if present, else '/'.

Permissions / Visibility
- No explicit permission checks in this component.

Notes for Vue 3 migration
- Keep submit handling identical (prevent default, set loading, clear error).
- Preserve returnPath redirect behavior.
- Keep i18n keys the same.
- Ensure errorUtils.shortMessage mapping stays consistent.
