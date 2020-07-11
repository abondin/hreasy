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

