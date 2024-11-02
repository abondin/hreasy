import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";
import vacationService, {RequestOrUpdateMyVacation, Vacation} from "@/components/vacations/vacation.service";
import {DateTimeUtils} from "@/components/datetimeutils";
import moment from "moment";

export interface RequestOrUpdateMyVacationForm {
    year: number,
    dates: string[],
    notes: string,
    daysNumber: number
}

export class RequestOrUpdateVacationActionDataContainer extends InDialogActionDataContainer<number, RequestOrUpdateMyVacationForm> {
    private _defaultNumberOrDays = 14;
    private _daysNotIncludedInVacations: Array<string> = [];
    private _daysNumberSetManually = false;

    constructor() {
        super((id, formData) => {
            if (formData) {
                const request = {
                    year: formData.year,
                    startDate: formData.dates[0],
                    endDate: formData.dates[1],
                    notes: formData.notes,
                    daysNumber: formData.daysNumber,
                } as RequestOrUpdateMyVacation;
                if (id) {
                    return vacationService.updatePlanningVacation(id, request!);
                } else {
                    return vacationService.requestVacation(request!);
                }
            } else {
                return Promise.resolve();
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
        this.daysNumberSetManually = false;
        const start = DateTimeUtils.firstDayOfYear(year);
        const end = moment(start).add(14, 'days');
        const formData = {
            year: year,
            dates: [DateTimeUtils.formatToIsoDate(start), DateTimeUtils.formatToIsoDate(end)],
            daysNumber: this.defaultNumberOrDays,
            notes: ''
        } as RequestOrUpdateMyVacationForm;
        super.openDialog(null, formData);
        this.updateDaysNumber();
    }

    openUpdateVacationDialog(vacation: Vacation) {
        const formData = {
            year: vacation.year,
            dates: [vacation.startDate, vacation.endDate],
            endDate: vacation.endDate,
            daysNumber: vacation.daysNumber,
            notes: vacation.notes,
        } as RequestOrUpdateMyVacationForm;
        super.openDialog(vacation.id, formData);
    }

    datesUpdated() {
        if (this.formData?.dates && this.formData.dates.length === 2) {
            const start = moment(this.formData?.dates[0], moment.HTML5_FMT.DATE, true);
            const end = moment(this.formData?.dates[1], moment.HTML5_FMT.DATE, true);
            if (start.isValid() && end.isValid()) {
                if (end.isBefore(start)) {
                    this.formData.dates = [this.formData.dates[1], this.formData.dates[0]];
                }
            }
        }
        this.updateDaysNumber();
    }

    private updateDaysNumber() {
        if (this.formData?.dates && this.formData.dates.length === 2) {
            const start = moment(this.formData?.dates[0], moment.HTML5_FMT.DATE, true);
            const end = moment(this.formData?.dates[1], moment.HTML5_FMT.DATE, true);
            if (start.isValid() && end.isValid()) {
                this.formData.daysNumber = DateTimeUtils.vacationDays(start, end, this.daysNotIncludedInVacations);
            }
        }
    }

    public formattedDates(): string {
        if (this.formData?.dates && this.formData.dates.length === 2) {
            return `${DateTimeUtils.formatFromIso(this.formData.dates[0])} - ${DateTimeUtils.formatFromIso(this.formData.dates[1])}`;
        } else {
            return '';
        }
    }

}
