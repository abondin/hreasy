import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";
import vacationService, {RequestOrUpdateMyVacation, Vacation} from "@/components/vacations/vacation.service";
import {DateTimeUtils} from "@/components/datetimeutils";
import moment from "moment";

export class RequestOrUpdateVacationActionDataContainer extends InDialogActionDataContainer<number, RequestOrUpdateMyVacation> {
    private _defaultNumberOrDays = 14;
    private _daysNotIncludedInVacations: Array<string> = [];
    private _daysNumberSetManually=false;

    constructor() {
        super((id, request) => {
            if (id) {
                return vacationService.updatePlanningVacation(id, request!);
            } else {
                return vacationService.requestVacation(request!);
            }
        });
    }

    get daysNumberSetManually() {
        return this._daysNumberSetManually;
    }

    set daysNumberSetManually(daysNumberSetManually) {
        this._daysNumberSetManually = daysNumberSetManually;
    }

    get defaultNumberOrDays() {
        return this._defaultNumberOrDays;
    }

    get daysNotIncludedInVacations() {
        return this._daysNotIncludedInVacations;
    }

    set daysNotIncludedInVacations(daysNotIncludedInVacations) {
        this._daysNotIncludedInVacations = daysNotIncludedInVacations;
    }

    openRequestVacationDialog(year: number) {
        this.daysNumberSetManually=false;
        const start = DateTimeUtils.firstDayOfYear(year);
        const end = moment(start).add(14, 'days');
        const formData = {
            year: year,
            startDate: DateTimeUtils.formatToIsoDate(start),
            endDate: DateTimeUtils.formatToIsoDate(end),
            daysNumber: this.defaultNumberOrDays,
            notes: ''
        } as RequestOrUpdateMyVacation;
        super.openDialog(null, formData);
        this.updateDaysNumber();
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

    startDateUpdated() {
        if (this.formData) {
            const startDate = moment(this.formData.startDate, moment.HTML5_FMT.DATE, true);
            let updateEndDateRequired = false;
            if (startDate.isValid()) {
                if (this.formData.endDate) {
                    const endDate = moment(this.formData.endDate, moment.HTML5_FMT.DATE, true);
                    if (!endDate.isValid() || endDate.isBefore(startDate)) {
                        updateEndDateRequired = true;
                    }
                } else {
                    updateEndDateRequired = true;
                }
                if (updateEndDateRequired) {
                    this.formData.endDate = startDate.add(this.defaultNumberOrDays - 1, "days").format(moment.HTML5_FMT.DATE);
                }
                this.updateDaysNumber();
            }
        }
    }


    endDateUpdated() {
        const endDate = moment(this.formData?.endDate, moment.HTML5_FMT.DATE, true);
        if (endDate.isValid()) {
            this.updateDaysNumber();
        }
    }

    private updateDaysNumber() {
        if (this.formData?.startDate && this.formData?.endDate) {
            const start = moment(this.formData.startDate, moment.HTML5_FMT.DATE, true);
            const end = moment(this.formData.endDate, moment.HTML5_FMT.DATE, true);
            if (start.isValid() && end.isValid()) {
                this.formData.daysNumber = DateTimeUtils.vacationDays(start, end, this.daysNotIncludedInVacations);
            }
        }
    }

}
