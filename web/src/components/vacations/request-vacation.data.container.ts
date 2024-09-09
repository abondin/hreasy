import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";
import vacationService, {RequestOrUpdateMyVacation, Vacation} from "@/components/vacations/vacation.service";
import {DateTimeUtils} from "@/components/datetimeutils";

export class RequestOrUpdateVacationActionDataContainer extends InDialogActionDataContainer<number, RequestOrUpdateMyVacation> {
    constructor() {
        super((id, request) => {
            if (id) {
                return vacationService.updatePlanningVacation(id, request!);
            } else {
                return vacationService.requestVacation(request!);
            }
        });
    }

    openRequestVacationDialog(year: number) {
        const formData = {
            year: year,
            startDate: DateTimeUtils.formatToIsoDate(DateTimeUtils.firstDayOfYear(year)),
            endDate: '',
            daysNumber: 0,
            notes: ''
        } as RequestOrUpdateMyVacation;
        super.openDialog(null, formData);
    }

    openUpdateVacationDialog(vacation: Vacation) {
        const formData = {
            year: vacation.year,
            startDate: vacation.startDate,
            endDate: vacation.endDate,
            daysNumber: vacation.daysNumber,
            notes: vacation.notes,
        } as RequestOrUpdateMyVacation;
        super.openDialog(vacation.id, formData);
    }

}
