import httpService from "../http.service";
import {AxiosInstance} from "axios";
import moment from 'moment';


export interface OvertimeItem {
    date: Date,
    projectId: number,
    hours: number,
    notes?: string
}

export interface OvertimeReport {
    employeeId: number,
    /**
     * Undefined for new report
     */
    id?: number,
    /**
     * Updates any time on publishing overtimes.
     * Undefined for new report
     */
    currentVersion?: number,
    /**
     * Overtime report period in yyyymm format. For example 202006 for all overtimes, reported in June
     */
    reportPeriod: number,
    overtimes: OvertimeItem[];
}

export interface OvertimeService {
    /**
     * Get report for logged user
     * @param employeeId
     * @param reportPeriod - report period in yyyymm format. For example 202006 for all overtimes, reported in June
     */
    get(employeeId: number, reportPeriod: number): Promise<OvertimeReport>;
}

class RestOvertimeService implements OvertimeService {
    constructor(private httpService: AxiosInstance) {
    }

    public get(employeeId: number, reportPeriod: number): Promise<OvertimeReport> {
        return httpService.get(`v1/overtimes/report/${reportPeriod}`).then(response => {
            return response.data;
        });
    }
}

class MockOvertimeService implements OvertimeService {

    private myReports: OvertimeReport[] = [
        {
            id: 1,
            employeeId: 1,
            currentVersion: 1,
            reportPeriod: 202007,
            overtimes: [
                {
                    date: new Date(2020, 7, 1),
                    hours: 8,
                    projectId: 2
                }
            ]

        }
    ]

    public get(employeeId: number, reportPeriod: number): Promise<OvertimeReport> {
        const reps = this.myReports.filter(r => r.reportPeriod == reportPeriod);
        if (reps && reps.length >= 1) {
            return Promise.resolve(reps[0]);
        }
        return Promise.reject({
            code: "report.not.found",
            message: "Report for given employee and period not found"
        });
    }
}

export class OvertimeUtils {
    static getPeriod(date: Date): number {
        return date.getFullYear() * 100 + (date.getMonth() + 1);
    }

    static formatDate(date: Date): string | undefined {
        if (date) {
            return moment(date).format('LL');
        } else {
            return undefined;
        }
    }
}

const overtimeService: OvertimeService = new MockOvertimeService();

export default overtimeService;

