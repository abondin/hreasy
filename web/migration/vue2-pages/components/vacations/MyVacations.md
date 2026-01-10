Component: src/components/vacations/MyVacations.vue

Purpose
- Shows current user's planned/requested vacations and allows requesting a vacation for open planning years and withdrawing pending requests.

Template/UI
- v-card with title "Планируемые отпуска".
- Action menu (v-menu) "Запланировать" shown only if openedPeriods exists and length > 0.
  - Menu lists years from openedPeriods; clicking a year opens request dialog.
- v-data-table shows filtered vacations (only PLANNED and REQUESTED).
  - Columns: Год, Начало, Окончание, Статус, Примечание.
  - startDate/endDate formatted via DateTimeUtils.formatFromIso.
  - status rendered via i18n key VACATION_STATUS_ENUM.<status>.
  - Notes column includes delete icon (mdi-delete) to withdraw request if allowed.
- Two InDialogForm dialogs:
  - "Запланировать отпуск на X год" uses RequestVacationsFormFields.
  - "Отозвать отпуск" confirmation dialog.

State
- headers: DataTableHeader[]
- loading: boolean
- vacations: MyVacation[]
- openedPeriods: VacPlanningPeriod[]
- requestAction: RequestOrUpdateVacationActionDataContainer
- rejectRequestAction: InDialogActionDataContainer<number, void>
- allStatuses, allYears, allMonths (prepared in created; used for support data).

Services / Data
- vacationService.openPlanningPeriods() -> openedPeriods.
- dictService.daysNotIncludedInVacations(allYears) -> requestAction.daysNotIncludedInVacations.
- vacationService.myFutureVacations() -> vacations (filtered to items with startDate/endDate).
- vacationService.rejectVacationRequest(id) via rejectRequestAction.

Filtering/Behavior
- filteredItems(): keeps only status in ['PLANNED', 'REQUESTED'].
- vacationCanBeRejected(): only when status === 'REQUESTED' AND year is within openedPeriods.

Permissions / Visibility
- No explicit permissionService checks. Withdraw button is shown only if vacationCanBeRejected().

Lifecycle
- created(): initializes status/year/month lists, headers, calls fetchData().

Notes for migration
- Keep "Запланировать" menu and opened planning years logic.
- Request dialog uses RequestVacationsFormFields and RequestOrUpdateVacationActionDataContainer.
- Withdraw action uses InDialogForm + InDialogActionDataContainer.
- Maintain hover behavior: delete icon only visible on row hover.
