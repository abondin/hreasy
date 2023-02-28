import {AxiosInstance} from "axios";
import httpService from "@/components/http.service";
import {Employee} from "@/components/empl/employee.service";

export interface TimesheetRecord extends TimesheetHours {
    id: number,
    employee: number,
    businessAccount: number,
    project?: number,
    date: string,
}

export interface TimesheetHours {
    id: number|string,
    hoursPlanned?: number,
    hoursSpent?: number
    billable: boolean,
    description?: string,
    workingDay: boolean
}

export interface TimesheetAggregatedByEmployee {
    employee: Employee,
    dates: { [key: string]: TimesheetHours }
    total: {
        hoursPlanned: number,
        hoursSpentBillable: number,
        hoursSpentNonBillable: number
    }
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
