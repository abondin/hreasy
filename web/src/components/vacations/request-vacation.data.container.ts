import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";
import vacationService, {RequestOrUpdateMyVacation, Vacation} from "@/components/vacations/vacation.service";
import {DateTimeUtils} from "@/components/datetimeutils";
import VacationEditForm, {VacationForm} from "@/components/vacations/VacationEditForm.vue";
import moment from "moment";

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
            startDate: DateTimeUtils.firstDayOfYearStr(year),
            endDate: DateTimeUtils.firstDayOfYearStr(year),
        } as RequestOrUpdateMyVacation;
        super.openDialog(null, formData);
    }

    public static applyVacationToForm(sourceForm: VacationForm, vacationToApply?: Vacation, defaultYear?: number): void {
        const defaultStartDate = (defaultYear && defaultYear != new Date().getFullYear()) ?
            moment({year: defaultYear, month: 1, day: 1}) : undefined;
                sourceForm.isNew = true;
        sourceForm.id = undefined;
        sourceForm.year = defaultYear ? defaultYear : new Date().getFullYear();
        sourceForm.employeeId = undefined;
        sourceForm.startDate = defaultStartDate ? DateTimeUtils.formatToIsoDate(defaultStartDate)! : '';
        sourceForm.endDate = '';
        sourceForm.plannedStartDate = '';
        sourceForm.plannedEndDate = '';
        sourceForm.status = 'PLANNED';
        sourceForm.notes = '';
        sourceForm.documents = '';
        sourceForm.daysNumber = 0;

        if (vacationToApply) {
            sourceForm.isNew = false;
            sourceForm.id = vacationToApply.id;
            sourceForm.year = vacationToApply.year;
            sourceForm.employeeId = vacationToApply.employee;
            sourceForm.startDate = vacationToApply.startDate;
            sourceForm.endDate = vacationToApply.endDate;
            sourceForm.plannedStartDate = vacationToApply.plannedStartDate ? vacationToApply.plannedStartDate : '';
            sourceForm.plannedEndDate = vacationToApply.plannedEndDate ? vacationToApply.plannedEndDate : '';
            sourceForm.status = vacationToApply.status;
            sourceForm.notes = vacationToApply.notes;
            sourceForm.documents = vacationToApply.documents;
            sourceForm.daysNumber = vacationToApply.daysNumber;
        }
    }
}
