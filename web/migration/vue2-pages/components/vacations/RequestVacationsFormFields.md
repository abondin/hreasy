Component: src/components/vacations/RequestVacationsFormFields.vue

Purpose
- Form fields for requesting or updating planned vacations for next year (used inside InDialogForm).

Template/UI
- v-date-picker (range, landscape, first-day-of-week=1, no-title) bound to data.formData.dates.
- Displays formatted date range from data.formattedDates() or error text "Выберите даты".
- Shows computed days number; highlights in warning when > 28.
- v-checkbox "Скорректировать количество дней вручную" toggles manual days editing.
- v-slider (0..31) shown when daysNumberSetManually is true.
- v-textarea for notes (counter 255, validation).

Props / State
- data: RequestOrUpdateVacationActionDataContainer (required).

Behavior
- Watcher on data.formData.dates triggers data.datesUpdated().
- Days number and formatted dates come from data container.

Dependencies
- RequestOrUpdateVacationActionDataContainer
- DateTimeUtils (validateDate, though not used in template)

Notes for migration
- Keep date picker range behavior and days calculation workflow via container.
- Respect warning style when daysNumber > 28.
- Keep manual days override toggle and slider range.
