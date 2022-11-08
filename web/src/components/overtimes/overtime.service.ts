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

export interface ApprovalDecision {
    /**
     * Generated on server side during persisting
     */
    id: number,
    /**
     * YYYY-MM-DD format
     */
    decisionTime: string,

    decision: 'APPROVED' | 'DECLINED';

    /**
     * Id of approver
     */
    approver: number,
    /**
     * Display Name of approver
     */
    approverDisplayName: string,

    comment?: string,

    /**
     * true if any overtime items were added after approval decision time
     */
    outdated: boolean
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
    approvals: ApprovalDecision[];

    lastUpdate: string | null;
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

    private closed = false;

    public periodId(): number {
        return this.year * 100 + (this.month);
    }

    public toString() {
        return moment([this.year, this.month, 1]).format("MMMM YYYY");
    }

    public increment() {
        this.month++;
        if (this.month >= 12) {
            this.month = 0;
            this.year++;
        }
    }

    public decrement() {
        this.month--;
        if (this.month < 0) {
            this.month = 11;
            this.year--;
        }
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
    items: OvertimeDaySummary[],
    commonApprovalStatus: CommonApprovalStatusEnum;
}

export interface ClosedOvertimePeriod {
    period: number,
    closedBy: number,
    closedAt: Date,
    comment: string
}

export type CommonApprovalStatusEnum =
    'NO_DECISIONS' | 'DECLINED' | 'APPROVED_NO_DECLINED' | 'APPROVED_OUTDATED';


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

    getClosedOvertimes(): Promise<ClosedOvertimePeriod[]>;

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

    /**
     * Approve overtime report
     * @param employeeId
     * @param reportPeriod
     * @param comment
     * @param previousDecision
     */
    approve(employeeId: number, reportPeriod: number, comment: string | null, previousDecision: number | null): Promise<OvertimeReport>;

    /**
     * Decline overtime report
     * @param employeeId
     * @param reportPeriod
     * @param comment
     * @param previousDecision
     */
    decline(employeeId: number, reportPeriod: number, comment: string, previousDecision: number | null): Promise<OvertimeReport>;

    export(reportPeriod: number): Promise<void>;
}


class RestOvertimeService implements OvertimeService {
    constructor(private httpService: AxiosInstance) {
    }

    public getSummary(reportPeriod: number): Promise<OvertimeEmployeeSummary[]> {
        return httpService.get(`v1/overtimes/summary/${reportPeriod}`).then(response => {
            return response.data;
        });
    }

    public getClosedOvertimes(): Promise<ClosedOvertimePeriod[]> {
        return httpService.get(`v1/overtimes/closed-periods`).then(response => {
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

    approve(employeeId: number, reportPeriod: number, comment: string | null, previousDecision: number | null): Promise<OvertimeReport> {
        return httpService.post(`v1/overtimes/${employeeId}/report/${reportPeriod}/approve`, {
            comment: comment,
            previousApprovalId: previousDecision
        }).then(response => {
            return response.data;
        });
    }

    decline(employeeId: number, reportPeriod: number, comment: string, previousDecision: number | null): Promise<OvertimeReport> {
        return httpService.post(`v1/overtimes/${employeeId}/report/${reportPeriod}/decline`, {
            comment: comment,
            previousApprovalId: previousDecision
        }).then(response => {
            return response.data;
        });
    }

    export(reportPeriod: number): Promise<void> {
        return httpService.get(`v1/overtimes/summary/${reportPeriod}/export`, {
            responseType: 'arraybuffer',
        }).then(response => {
            const blob = new Blob([response.data], {type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'})
            const link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = `Overtimes-${reportPeriod + 1}.xlsx`;
            link.click();
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
    public commonApprovalStatus: CommonApprovalStatusEnum = "NO_DECISIONS";

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
        if (this.filter.selectedProjects && this.filter.selectedProjects.length > 0) {
            return this.filter.selectedProjects.indexOf(day.projectId) >= 0;
        }
        return true;
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

    /**
     * @deprecated use DateTimeService
     * @param date
     */
    static formatDate(date: Date | null): string | undefined {
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

    static formatDateTimeShort(date: Date): string | undefined {
        if (date) {
            return moment(date).format('LLL');
        } else {
            return undefined;
        }
    }

    static totalHours(overtimeReport: OvertimeReport | null): number {
        return overtimeReport && overtimeReport.items && overtimeReport.items.length > 0
            ? overtimeReport.items.map(i => i.hours).reduce((s, c) => s + c) : 0;
    }

    static totalHoursForSummary(overtimeSummaryReports: OvertimeSummaryContainer[]): number {
        return overtimeSummaryReports.length > 0
            ? overtimeSummaryReports.map(s => s.totalHours).reduce((s, c) => s + c) : 0;
    }

}

const overtimeService: OvertimeService = new RestOvertimeService(httpService);

export default overtimeService;

