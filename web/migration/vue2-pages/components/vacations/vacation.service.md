Service: src/components/vacations/vacation.service.ts

Purpose
- API client for vacations (all vacations, my vacations, planning requests, export).

Types
- VacationStatus enum: PLANNED, TAKEN, COMPENSATION, CANCELED, REJECTED, REQUESTED.
- Vacation: full vacation entry incl. employee, project, dates, status, documents, daysNumber.
- MyVacation: simplified for current user.
- EmployeeVacationShort: for current/future vacations.
- RequestOrUpdateMyVacation: request body for planning.
- CreateOrUpdateVacation: body for create/update admin vacation.
- VacPlanningPeriod: year.

Endpoints
- GET v1/vacations?years=... -> findAll(years)
- GET v1/vacations/my -> myFutureVacations()
- GET v1/vacations/{employeeId}/currentOrFuture -> currentOrFutureVacations(employeeId)
- POST v1/vacations/{employeeId} -> create(employeeId, body)
- PUT v1/vacations/{employeeId}/{vacationId} -> update(employeeId, vacationId, body)
- GET v1/vacations/export?years=... -> export(years) downloads XLSX
- GET v1/vacations/planning-period -> openPlanningPeriods()
- POST v1/vacations/request -> requestVacation(body)
- DELETE v1/vacations/my/{vacationId} -> rejectVacationRequest(vacationId)
- PUT v1/vacations/request/{vacationId} -> updatePlanningVacation(vacationId, body)

Other logic
- isNotWorkingDays(vacation): true for PLANNED or TAKEN.

Notes for migration
- Preserve export behavior (arraybuffer -> Blob -> download Vacations.xlsx).
- Keep status set and type names; several components depend on VACATION_STATUS_ENUM keys.
