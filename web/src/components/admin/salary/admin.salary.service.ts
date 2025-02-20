import {AxiosInstance} from "axios";
import httpService from "@/components/http.service";
import {SalaryIncreaseRequest} from "@/components/salary/salary.service";

export interface SalaryRequestImplementBody {
    increaseAmount: number;

    salaryAmount: number|null;
    increaseStartPeriod: number;
    newPosition: number | null;
    reason: string;
    comment: string | null;
}

export interface SalaryRequestUpdateImplIncreaseTextBody {
    increaseText: string | null;
}
export interface SalaryRequestRejectBody {
    reason: string;
    comment: string | null;
}




export const enum SalaryRequestImplementationState {
    IMPLEMENTED = 1,
    REJECTED = 2
}

export const salaryRequestImplementationStates = [
    SalaryRequestImplementationState.IMPLEMENTED,
    SalaryRequestImplementationState.REJECTED
];

export interface AdminSalaryService {
    loadAllSalaryRequests(periodId: number): Promise<Array<SalaryIncreaseRequest>>;

    /**
     * Load all requests for all period for given employee.
     * Accessible only with admin_salary_request permission
     * @param employeeId
     */
    loadEmployeeSalaryRequestsForAllPeriods(employeeId: number): Promise<Array<SalaryIncreaseRequest>>;

    markAsImplemented(requestId: number, body: SalaryRequestImplementBody): Promise<number>;
    updateImplIncreaseText(requestId: number, body: SalaryRequestUpdateImplIncreaseTextBody): Promise<number>;

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

    export(periodId: number): Promise<any>;

    /**
     * Clear all information about salary request implementation
     * @param id
     */
    resetImplementation(id: number): Promise<any>;
}

class RestAdminSalaryService implements AdminSalaryService {
    constructor(private httpService: AxiosInstance) {
    }

    loadAllSalaryRequests(periodId: number): Promise<Array<SalaryIncreaseRequest>> {
        return httpService.get(`v1/admin/salaries/requests/${periodId}`).then(response => response.data);
    }

    loadEmployeeSalaryRequestsForAllPeriods(employeeId: number): Promise<Array<SalaryIncreaseRequest>> {
        return httpService.get(`v1/admin/salaries/requests/employee/${employeeId}`).then(response => response.data);
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


    updateImplIncreaseText(requestId: number, body: SalaryRequestUpdateImplIncreaseTextBody): Promise<number> {
        return httpService.put(`v1/admin/salaries/requests/${requestId}/impl/increase-text`, body).then(response => {
            return response.data;
        });
    }

    resetImplementation(requestId: number): Promise<any> {
        return httpService.delete(`v1/admin/salaries/requests/${requestId}/implementation`).then(response => {
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

    export(periodId: number): Promise<void> {
        return httpService.get(`v1/admin/salaries/requests/${periodId}/export`, {
            responseType: 'arraybuffer',
        }).then(response => {
            const blob = new Blob([response.data], {type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'})
            const link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = `SalaryRequest_${periodId}.xlsx`;
            link.click();
        });
    }

}

const salaryAdminService: AdminSalaryService = new RestAdminSalaryService(httpService);

export default salaryAdminService;
