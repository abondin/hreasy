import {AxiosInstance} from "axios";
import httpService from "@/components/http.service";
import {Moment} from "moment";
import {Employee} from "@/components/empl/employee.service";
import {RowGroup} from "@/components/ts/TimesheetTableComponent.vue";

export interface TimesheetRecord {
    id?: number,
    employee: number,
    businessAccount: number,
    project?: number,
    date: string,
    hoursPlanned?: number,
    hoursSpent?: number
}

export interface TimesheetAggregated {
    employee: RowGroup,
    project: RowGroup,
    dates: {date:Moment, hours: number}[]
}


export interface TimesheetReportBody {
    businessAccount: number,
    project?: number,
    date: string,
    hoursPlanned?: number,
    hoursSpent?: number
}

export interface TimesheetSummaryFilter {
    from: string,
    to: string
}


export interface TimesheetService {
    timesheetSummary(filter: TimesheetSummaryFilter): Promise<Array<TimesheetRecord>>;

    report(employeeId: number, body: TimesheetReportBody): Promise<number>;

    delete(employeeId: number, recordId: number): Promise<Array<number>>;

}

class RestTimesheetService implements TimesheetService {
    constructor(private httpService: AxiosInstance) {
    }


    timesheetSummary(filter: TimesheetSummaryFilter): Promise<Array<TimesheetRecord>> {
        return httpService.get("v1/timesheet", {params: filter}).then(response => response.data);
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
