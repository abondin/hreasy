Component: src/components/telegram/TelegramConfirmationPage.vue

Purpose
- Confirms a Telegram account via backend call using provided props (employeeId, telegramAccount, confirmationCode).

Template/UI
- Vuetify card with title and status message.
- Shows loading message while request in-flight.
- Shows error alert if error set; otherwise success paragraph.
- Router link back to '/'.
- i18n keys: Подтверждение Telegram Account, Отправляем ваш запрос на сервер, Поздравляем. Теперь вы можете пользоваться Telegram Bot HR Easy!, Вернуться на главную.

Props
- employeeId (number, required).
- telegramAccount (string, required).
- confirmationCode (string, required).

State
- loading (boolean).
- error (string | null).

Actions / Services
- telegramConfirmationService.confirmTelegramAccount(employeeId, telegramAccount, confirmationCode).
- errorUtils.shortMessage used for error mapping.

Lifecycle
- created(): triggers confirmation request, sets loading/error flags.

Permissions / Visibility
- No explicit permission checks in component.

Notes for Vue 3 migration
- Ensure props are required and passed from route params in router config.
- Preserve success/error/loading display logic and i18n keys.
- Keep error formatting via errorUtils.shortMessage.
