import httpService from "../http.service";
import {AxiosInstance} from "axios";
import moment from 'moment';


export interface OvertimeItem {
    /**
     * YYYY-MM-DD format
     */
    date: string,
    /**
     * Undefined only when adding new item form opened
     */
    projectId?: number,
    hours: number,
    notes?: string,
    createdAt?: Date,
    updatedAt?: Date;
}

export interface OvertimeReport {
    employeeId: number,
    /**
     * Undefined for new report
     */
    id?: number,
    /**
     * Overtime report period in yyyymm format. For example 202005 for all overtimes, reported in June
     * @see ReportPeriod
     */
    reportPeriod: number,
    overtimes: OvertimeItem[];
}

/**
 * Represent overtime period.
 * <b>Keep in mind that month starts with 0</b>
 *
 */
export class ReportPeriod {

    public static fromPeriodId(periodId: number) {
        return new ReportPeriod(periodId / 100, periodId - periodId / 100);
    }

    /**
     * Employee works with overtimes for previous month before the salary calculation
     * (hardcoded value - 5th day of every month)
     */
    static currentPeriod(): ReportPeriod {
        let m = moment();
        if (m.date() < 5) {
            m = m.add(-1, "month");
        }
        return new ReportPeriod(m.year(), m.month());
    }

    constructor(private year: number, private month: number) {
    }


    public periodId(): number {
        return this.year * 100 + (this.month);
    }

    public toString() {
        return moment([this.year, this.month, 1]).format("MMMM YYYY");
    }

    public increment(){
        this.month++;
    }
    public decrement(){
        this.month--;
    }

}

export interface OvertimeService {
    /**
     * Get report for logged user
     * @param employeeId
     * @param reportPeriod - report period in yyyymm format. For example 202006 for all overtimes, reported in June
     */
    get(employeeId: number, reportPeriod: number): Promise<OvertimeReport>;

    /**
     * Add one item to the report
     * @param employeeId
     * @param reportPeriod
     * @param item
     */
    addItem(employeeId: number, reportPeriod: number, item: OvertimeItem): Promise<OvertimeReport>;
}

class RestOvertimeService implements OvertimeService {
    constructor(private httpService: AxiosInstance) {
    }

    public get(employeeId: number, reportPeriod: number): Promise<OvertimeReport> {
        return httpService.get(`v1/overtimes/${employeeId}/report/${reportPeriod}`).then(response => {
            return response.data;
        });
    }

    addItem(employeeId: number, reportPeriod: number, item: OvertimeItem): Promise<OvertimeReport> {
        const now = new Date();
        item.updatedAt = now;
        if (!item.createdAt) {
            item.createdAt = now;
        }
        return httpService.post(`v1/overtimes/${employeeId}/report/${reportPeriod}`, item).then(response => {
            return response.data;
        });
    }
}

class MockOvertimeService implements OvertimeService {

    private myReports: OvertimeReport[] = []

    public get(employeeId: number, reportPeriod: number): Promise<OvertimeReport> {
        const reps = this.myReports.filter(r => r.reportPeriod == reportPeriod);
        if (reps && reps.length >= 1) {
            return Promise.resolve(reps[0]);
        }
        const emptyReport: OvertimeReport = {
            employeeId: employeeId,
            reportPeriod: reportPeriod,
            overtimes: []
        };
        this.myReports.push(emptyReport)
        return Promise.resolve(emptyReport);
    }

    addItem(employeeId: number, reportPeriod: number, item: OvertimeItem): Promise<OvertimeReport> {
        return this.get(employeeId, reportPeriod).then(report => {
            const now = new Date();
            item.updatedAt = now;
            if (!item.createdAt) {
                item.createdAt = now;
            }
            report.overtimes.push(item);
            return report;
        });
    }
}

export class OvertimeUtils {

    static formatDate(date: Date): string | undefined {
        if (date) {
            return moment(date).format('LL');
        } else {
            return undefined;
        }
    }

    static formatDateTime(date: Date): string | undefined {
        if (date) {
            return moment(date).format('LLLL');
        } else {
            return undefined;
        }
    }
}

const overtimeService: OvertimeService = new MockOvertimeService();

export default overtimeService;

