Component: src/components/vacations/VacationEditForm.vue

Purpose
- Dialog form to create or update a vacation entry (admin/manager side from VacationsList).

Template/UI
- v-form inside v-card.
- Title: "Добавление отпуска" if new, else "Изменение отпуска".
- Close button (mdi-close).
- Fields:
  - Employee (v-autocomplete, required, disabled when editing existing).
  - Year (v-select).
  - Start date (MyDateFormComponent, required, format validation).
  - End date (MyDateFormComponent, required, format validation).
  - Status (v-select).
  - Days number (v-slider 0..31).
  - Document (v-text-field, counter 255, validation).
  - Notes (v-textarea, counter 255, validation).
- Error block (v-alert) if error.
- Actions: Close, Create/Update.

Props
- input?: Vacation (optional)
- allStatuses: Array<any>
- allYears: Array<any>
- allEmployees: Array<SimpleDict>
- daysNotIncludedInVacations: string[]
- defaultYear?: number

State
- vacationForm (VacationForm): isNew, id, year, employeeId, startDate, endDate, plannedStartDate, plannedEndDate, status, notes, documents, daysNumber.
- error (string | null)
- defaultNumberOrDays = 14

Behavior
- reset(): initializes form; if input provided, populates fields. Also resets MyDateFormComponent refs.
  - For new vacation, if defaultYear is not current year, startDate defaults to Jan 1 of that year.
- submit(): validates form; builds CreateOrUpdateVacation body; calls vacationService.create or update; emits close on success; sets error on failure.
- watch startDate: auto-fill endDate = startDate + defaultNumberOrDays - 1, and update daysNumber.
- watch endDate: update daysNumber.
- updateDaysNumber(): uses DateTimeUtils.vacationDays(start, end, daysNotIncludedInVacations).

Services / Utilities
- vacationService.create / update
- DateTimeUtils.formatToIsoDate / validateFormattedDate / vacationDays
- moment for date math.
- errorUtils.shortMessage and logger.

Notes for migration
- Preserve auto-calc of endDate and daysNumber.
- Keep employee selector disabled during edit.
- Respect status list filtering (REQUESTED excluded unless editing REQUESTED; done in parent).
