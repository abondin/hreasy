import {Moment} from "moment/moment";
import {TimesheetRecord, TimesheetSummaryFilter} from "@/components/ts/timesheet.service";
import {DateTimeUtils} from "@/components/datetimeutils";
import moment from "moment";

/**
 * Information about employee including all not working days (vacations and holidays)
 */
export interface EmployeeWithNotWorkingDays {
    id: number,
    displayName: string,
    notWorkingDays: Array<Moment>
}


/**
 * Timesheet report for one employee on one project
 */
export interface EmployeeOneDayTimesheet {
    employee: EmployeeWithNotWorkingDays,
    record: TimesheetRecord,
    workingDay: boolean,
}

/**
 *
 */
export interface TimesheetAggregatedByEmployee {
    employee: EmployeeWithNotWorkingDays,
    dates: { [key: string]: EmployeeOneDayTimesheet }
    editMode: boolean,
    total: {
        hoursSpent: number
    }
}

export class TimesheetTableFilterData {
    private _year: number;
    public selectedDates: Array<string> = [];
    public ba: number | null = null;
    public project: number | null = null;

    public constructor() {
        const now = DateTimeUtils.now();
        this._year = now.year();
        this.updateDatesOnYearChange();
    }

    private updateDatesOnYearChange() {
        const now = DateTimeUtils.now().set({year: this._year});
        this.selectedDates = [
            DateTimeUtils.formatToIsoDate(now.startOf('month'))!,
            DateTimeUtils.formatToIsoDate(now.endOf('month'))!
        ];
    }

    public get from(): Moment {
        return moment(this.fromStr());
    }

    public get to(): Moment {
        return moment(this.toStr());
    }

    public get year() {
        return this._year;
    }

    public set year(year: number) {
        this._year = year;
        this.updateDatesOnYearChange();
    }

    /**
     * Filter to provide in API request
     */
    public timesheetSummaryFilter(): TimesheetSummaryFilter {
        return {
            from: this.fromStr(),
            to: this.toStr()
        }
    }

    public isReady() {
        return this.from && this.to && this.ba;
    }

    private fromStr(): string {
        return this.selectedDates[0];
    }

    private toStr(): string {
        return this.selectedDates.length > 1 ? this.selectedDates[1] : this.selectedDates[0]
    }

}
