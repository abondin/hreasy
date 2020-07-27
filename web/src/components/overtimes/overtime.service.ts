import httpService from "../http.service";
import {AxiosInstance} from "axios";
import moment from 'moment';
import {SimpleDict} from "@/store/modules/dict";


export interface OvertimeItem {
    /**
     * Generated on server side during persisting
     */
    id?: number,
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
    deletedAt?: Date;
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
    period: number,
    items: OvertimeItem[];
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

    public increment() {
        this.month++;
    }

    public decrement() {
        this.month--;
    }

    public daysCount(): number {
        return moment([this.year, this.month, 1]).daysInMonth();
    }

}


/**
 * Summary overtimes information for one employee and one period
 */
export interface OvertimeEmployeeSummary {
    employeeId: number,
    items: OvertimeDaySummary[]
}

export interface OvertimeDaySummary {
    date: string,
    projectId: number
    hours: number;
}


export interface OvertimeService {
    /**
     * Get report for logged user
     * @param employeeId
     * @param reportPeriod - report period in yyyymm format. For example 202006 for all overtimes, reported in June
     */
    get(employeeId: number, reportPeriod: number): Promise<OvertimeReport>;

    getSummary(reportPeriod: number): Promise<OvertimeEmployeeSummary[]>;

    /**
     * Add one item to the report
     * @param employeeId
     * @param reportPeriod
     * @param item
     */
    addItem(employeeId: number, reportPeriod: number, item: OvertimeItem): Promise<OvertimeReport>;

    /**
     * Delete one item from the report
     * @param employeeId
     * @param reportPeriod
     * @param itemId
     */
    deleteItem(employeeId: number, reportPeriod: number, itemId: number): Promise<OvertimeReport>;
}


class RestOvertimeService implements OvertimeService {
    constructor(private httpService: AxiosInstance) {
    }

    public getSummary(reportPeriod: number): Promise<OvertimeEmployeeSummary[]> {
        return httpService.get(`v1/overtimes/summary/${reportPeriod}`).then(response => {
            return response.data;
        });
    }

    public get(employeeId: number, reportPeriod: number): Promise<OvertimeReport> {
        return httpService.get(`v1/overtimes/${employeeId}/report/${reportPeriod}`).then(response => {
            return response.data;
        });
    }

    addItem(employeeId: number, reportPeriod: number, item: OvertimeItem): Promise<OvertimeReport> {
        const now = new Date();
        if (!item.createdAt) {
            item.createdAt = now;
        }
        return httpService.post(`v1/overtimes/${employeeId}/report/${reportPeriod}/item`, item).then(response => {
            return response.data;
        });
    }

    deleteItem(employeeId: number, reportPeriod: number, itemId: number): Promise<OvertimeReport> {
        return httpService.delete(`v1/overtimes/${employeeId}/report/${reportPeriod}/item/${itemId}`).then(response => {
            return response.data;
        });
    }
}

/**
 * Ready for view overtime summary for one employee.
 * Converted from OvertimeEmployeeSummary.
 *
 * For every day might be several overtime items exist.
 *
 * OvertimeSummaryContainerDay - summary for one day
 * OvertimeDaySummary - single overtime item
 *
 */
export class OvertimeSummaryContainer {
    public readonly days: OvertimeSummaryContainerDay[] = [];
    public totalHours = 0;

    constructor(public readonly employee: SimpleDict, private filter: OvertimeSummaryContainerFilter) {
    }

    public addDays(days: OvertimeDaySummary[]) {
        days.forEach(day => {
            if (this.filterDay(day)) {
                const existing = this.days.find(d => d.day == day.date);
                if (existing) {
                    existing.push(day);
                } else {
                    this.days.push(new OvertimeSummaryContainerDay(day));
                }
                this.totalHours += day.hours;
            }
        });
    }

    private filterDay(day: OvertimeDaySummary): boolean {
        let passed = true;
        if (this.filter.selectedProjects && this.filter.selectedProjects.length > 0) {
            return this.filter.selectedProjects.indexOf(day.projectId) >= 0;
        }
        return passed;
    }

}

export class OvertimeSummaryContainerDay {
    private items: OvertimeDaySummary[] = [];
    public day: string;

    constructor(day: OvertimeDaySummary) {
        this.day = day.date;
        this.items.push(day);
    }

    public push(day: OvertimeDaySummary) {
        this.items.push(day);
    }

    public totalHours(): number {
        let hours = 0;
        this.items.forEach(i => hours += i.hours);
        return hours;
    }
}

export interface OvertimeSummaryContainerFilter {
    selectedProjects: number[];
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

const overtimeService: OvertimeService = new RestOvertimeService(httpService);

export default overtimeService;

