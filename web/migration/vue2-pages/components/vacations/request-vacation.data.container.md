Helper: src/components/vacations/request-vacation.data.container.ts

Purpose
- Data container for request/update planned vacations (used by MyVacations + RequestVacationsFormFields).

Extends
- InDialogActionDataContainer<number, RequestOrUpdateMyVacationForm>

Form model
- year
- dates: string[] (start/end in ISO)
- notes
- daysNumber

Behavior
- openRequestVacationDialog(year):
  - resets manual flag
  - defaults dates to first day of year + 14 days
  - daysNumber = 14
  - calls openDialog(null, formData) and updateDaysNumber()
- openUpdateVacationDialog(vacation): opens dialog for existing vacation (id).
- datesUpdated():
  - swaps start/end if end < start
  - recalculates days number
- updateDaysNumber(): DateTimeUtils.vacationDays(start, end, daysNotIncludedInVacations)
- formattedDates(): returns "DD.MM.YYYY - DD.MM.YYYY" or empty string.

API actions
- If id provided -> vacationService.updatePlanningVacation(id, request)
- Else -> vacationService.requestVacation(request)

Dependencies
- vacationService
- DateTimeUtils, moment
- InDialogActionDataContainer
