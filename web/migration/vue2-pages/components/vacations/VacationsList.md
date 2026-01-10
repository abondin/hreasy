Component: src/components/vacations/VacationsList.vue

Purpose
- Main vacations module with three tabs: all vacations table, per-employee summary, and vacation timeline (calendar). Provides filters, export, and create/edit vacation dialog.

Template/UI
- v-tabs with 3 tabs: "Все отпуска", "Сводная по сотрудникам", "График отпусков".
- Filter area (v-row) with:
  - Action buttons (refresh, add, export) shown only if permitted.
  - Selected year (v-select) bound to selectedYear.
  - Date range filter (MyDateRangeComponent) bound to filter.selectedDates.
  - Status filter (v-select, multiple) bound to filter.selectedStatuses.
  - Search (v-text-field) bound to filter.search.
  - Current project filter (v-autocomplete, multiple) bound to filter.selectedProjects.
  - Current project role filter (v-autocomplete, multiple) bound to filter.selectedProjectRoles.
- Tab 1 (All vacations): v-data-table with headers, multi-sort, dense, pagination.
  - Employee name cell renders a menu with copy and edit actions.
  - Dates formatted via DateTimeUtils.formatFromIso.
  - Status translated via VACATION_STATUS_ENUM.<status>.
- Tab 2 (Summary): v-data-table with aggregated per-employee data.
  - Employee name button sets search filter and switches to tab 0.
  - Upcoming vacation shown as date range + status.
- Tab 3 (Calendar): VacationsTimeline component with year navigation.
- Vacation edit dialog (v-dialog) with VacationEditForm; on close reloads data.

State
- headers, summaryHeaders (DataTableHeader[])
- loading, vacations (Vacation[])
- selectedTab (number)
- selectedYear (number)
- filter (Filter class): selectedStatuses, search, selectedProjects, selectedProjectRoles, selectedDates.
- allStatuses, allEmployees, allProjectRoles, allYears, daysNotIncludedInVacations.
- vacationDialog (boolean), selectedVacation (Vacation | null)
- snackbarNotification, snackbarMessage

Services / Data
- vacationService.findAll([selectedYear]) -> vacations (filtered to items with startDate/endDate/employeeDisplayName).
- vacationService.export([selectedYear]) -> downloads XLSX.
- employeeService.findAll() -> allEmployees list and unique project roles.
- dictService.daysNotIncludedInVacations(allYears) -> daysNotIncludedInVacations.
- dict store reloadProjects.
- employeeVacationSummaryMapper.map(vacations) -> summary rows.

Permissions / Visibility
- Actions gated by permissionService:
  - canEditVacations(): permissionService.canEditAllVacations()
  - canExportVacations(): permissionService.canExportAllVacations()
- Action buttons (refresh/add/export) shown only when permitted.

Filtering / Search
- filterItem() uses searchUtils + TextFilterBuilder:
  - project id, project role, status (for Vacation), date range for Vacation.
  - text search over employeeDisplayName + current project role.
- Date range filter resets to full year on fetchData(resetFilter=true).
- Status filter default: ['PLANNED', 'TAKEN', 'REQUESTED'].

Dialogs / Actions
- openVacationDialog(vacation?) opens VacationEditForm; resets form via ref.
- VacationEditForm receives employees, statusesForForm (excludes REQUESTED unless editing REQUESTED), years, default year, daysNotIncludedInVacations.
- copyToClipboard uses $copyText and snackbar.

Timeline
- VacationsTimeline emits year-navigation -> updates selectedYear and reloads data.

Notes for migration
- Preserve tab structure and filters.
- Keep permission gating for add/export/refresh.
- Maintain year selection logic + date range default to full year.
- Preserve copy-to-clipboard action and snackbar messaging.
