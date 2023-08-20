import {AxiosInstance} from "axios";
import httpService from "@/components/http.service";
import {DateTimeUtils} from "@/components/datetimeutils";

export interface EmployeeShortForTimesheetSummary {
    id: number,
    displayName: string,
    currentProject?: number,
    currentProjectBa?: number,
}

export interface TimesheetRecord {
    id: number,
    date: string,
    businessAccount: number,
    project: number | null,
    hoursSpent: number,
    comment: string | null
}

/**
 * DTO from backend API.
 * All not dismissed employees with aggregated timesheet and vacations by given filter
 */
export interface TimesheetSummary {
    employee: EmployeeShortForTimesheetSummary,
    vacations: string[],
    /**
     * Merge all timesheet by day to show in one column
     */
    timesheet: TimesheetGroupedByDay
}

interface TimesheetGroupedByDay {
    [key: string]: {
        records: TimesheetRecord[],
        totalHours: number
    }
}
interface TimesheetSummaryApiResponse {
    employee: EmployeeShortForTimesheetSummary,
    vacations: string[],
    timesheet: TimesheetRecord[]
}

/**
 * DTO for filter for the api request
 */
export interface TimesheetSummaryApiQueryFilterFilter {
    from: string,
    to: string,
    ba: number | null,
    project: number | null
}

export interface TimesheetReportBody {
    businessAccount: number,
    project: number | null,
    hours: OneDayReport[],
    comment: string | null
}

export interface OneDayReport {
    date: string,
    hoursSpent: number,
}


export interface TimesheetService {
    timesheetSummary(filter: TimesheetSummaryApiQueryFilterFilter): Promise<Array<TimesheetSummary>>;

    report(employeeId: number, body: TimesheetReportBody): Promise<number>;

    delete(employeeId: number, recordId: number): Promise<Array<number>>;

}

class RestTimesheetService implements TimesheetService {
    constructor(private httpService: AxiosInstance) {
    }


    timesheetSummary(filter: TimesheetSummaryApiQueryFilterFilter): Promise<Array<TimesheetSummary>> {
        return httpService.get("v1/timesheet", {params: filter})
            .then(response => (response.data as Array<TimesheetSummaryApiResponse>).map(r => {
                const timesheet:TimesheetGroupedByDay={};
                r.timesheet.forEach(record=>{
                    const k: string | undefined = DateTimeUtils.formatToDayKey(DateTimeUtils.dateTimeFromIsoString(record.date));
                    if (!k) return;
                    let t = timesheet[k];
                    if (!t){
                        t = {records: [],totalHours: 0};
                        timesheet[k] = t;
                    }
                    t.records.push(record);
                    t.totalHours+=record.hoursSpent;
                })
                return {
                    employee: r.employee,
                    vacations: r.vacations,
                    timesheet: timesheet
                } as TimesheetSummary;
            }))
    }

    report(employeeId: number, body: TimesheetReportBody): Promise<number> {
        return httpService.post(`v1/timesheet/${employeeId}`, body).then(response => response.data);
    }

    delete(employeeId: number, recordId: number): Promise<Array<number>> {
        return httpService.delete(`v1/timesheet/${employeeId}/${recordId}`).then(response => response.data);
    }
}

const timesheetService: TimesheetService = new RestTimesheetService(httpService);

export default timesheetService;
