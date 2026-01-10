Helper: src/components/vacations/employeeVacationSummaryService.ts

Purpose
- Aggregates vacations into per-employee summaries for the "Сводная по сотрудникам" tab.

Classes
- EmployeeVacationSummary: employee id, display name, current project, year, upcomingVacation, vacationsNumber, vacationsTotalDays.

Mapping logic
- Groups input vacations by employee, ignoring statuses other than PLANNED/TAKEN/COMPENSATION/REQUESTED.
- For each employee:
  - vacationsNumber++
  - vacationsTotalDays += daysNumber
  - upcomingVacation = nearest upcoming vacation (including ongoing)

Upcoming vacation selection
- Ignores vacations that already ended (now after start and end).
- Chooses earliest start date among upcoming candidates.

Dependencies
- DateTimeUtils for date comparisons and now().
- CurrentProjectDict from employee service.

Notes for migration
- Keep status filtering and upcoming vacation selection logic.
