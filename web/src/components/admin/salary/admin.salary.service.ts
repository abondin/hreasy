import {AxiosInstance} from "axios";
import httpService from "@/components/http.service";
import {SalaryIncreaseRequest} from "@/components/salary/salary.service";

export interface SalaryRequestImplementBody {
    salaryIncrease: number;
    increaseStartPeriod: number;
    newPosition: number | null;
    reason: string;
    comment: string | null;
}

export interface SalaryRequestRejectBody {
    reason: string;
    comment: string | null;
}

export const enum SalaryRequestImplementationState {
    IMPLEMENTED = 1,
    REJECTED = -1
}

export const salaryRequestImplementationStates = [
    SalaryRequestImplementationState.IMPLEMENTED,
    SalaryRequestImplementationState.REJECTED
];

export interface AdminSalaryService {
    loadAllSalaryRequests(periodId: number): Promise<Array<SalaryIncreaseRequest>>;

    markAsImplemented(requestId: number, body: SalaryRequestImplementBody): Promise<number>;
    reject(requestId: number, body: SalaryRequestRejectBody): Promise<number>;


    /**
     * Close salary requests period for editing
     * @param periodId - report period in yyyymm format. For example 202006 for requests reported in June
     * @param comment - optional comment
     */
    closeReportPeriod(periodId: number, comment?: string): Promise<any>;

    /**
     * Reopen salary requests for editing
     * @param periodId - report period in yyyymm format. For example 202006 for requests reported in June
     * @param comment - optional comment
     */
    reopenReportPeriod(periodId: number, comment?: string): Promise<any>;
}

class RestAdminSalaryService implements AdminSalaryService {
    constructor(private httpService: AxiosInstance) {
    }

    loadAllSalaryRequests(periodId: number): Promise<Array<SalaryIncreaseRequest>> {
        return httpService.get(`v1/admin/salaries/requests/${periodId}`).then(response => response.data);
    }

    markAsImplemented(requestId: number, body: SalaryRequestImplementBody): Promise<number> {
        return httpService.put(`v1/admin/salaries/requests/${requestId}/implement`, body).then(response => {
            return response.data;
        });
    }

    reject(requestId: number, body: SalaryRequestRejectBody): Promise<number> {
        return httpService.put(`v1/admin/salaries/requests/${requestId}/reject`, body).then(response => {
            return response.data;
        });
    }

    closeReportPeriod(periodId: number, comment?: string): Promise<any> {
        return httpService.post(`v1/admin/salaries/requests/periods/${periodId}/close`, {comment: comment}).then(response => {
            return response.data;
        });
    }

    reopenReportPeriod(periodId: number, comment?: string): Promise<any> {
        return httpService.post(`v1/admin/salaries/requests/periods/${periodId}/reopen`, {comment: comment}).then(response => {
            return response.data;
        });
    }


}

const salaryAdminService: AdminSalaryService = new RestAdminSalaryService(httpService);

export default salaryAdminService;
