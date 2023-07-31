import {Moment} from "moment/moment";
import {TimesheetSummaryApiQueryFilterFilter} from "@/components/ts/timesheet.service";
import {DateTimeUtils} from "@/components/datetimeutils";
import moment from "moment";

/**
 * Timesheet report for one employee on one project
 */
export interface TimesheetAggregatedByEmployeeDay {
    date: Moment,
    workingDay: boolean,
    hoursSpent: number
}

/**
 * One row in timesheet table component (timesheet for one employee on one project for given period)
 *
 */
export interface TimesheetAggregatedByEmployee extends Record<string,any> {
    employee: number,
    employeeDisplayName: string,
    notWorkingDayKeys: Array<string>,
    ba: number,
    project: number | null,
    editMode : boolean;
}

export class TimesheetTableNavigatorData {
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
            DateTimeUtils.formatToIsoDate(now.startOf('week'))!,
            DateTimeUtils.formatToIsoDate(now.endOf('week'))!
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
    public timesheetSummaryApiQueryFilter(): TimesheetSummaryApiQueryFilterFilter {
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
