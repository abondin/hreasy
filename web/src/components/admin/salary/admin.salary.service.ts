import {AxiosInstance} from "axios";
import httpService from "@/components/http.service";
import {SalaryRequestFullInfo} from "@/components/salary/salary.service";

export interface AdminSalaryService {
    loadAllSalaryRequests(periodId: number): Promise<Array<SalaryRequestFullInfo>>;

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

    loadAllSalaryRequests(periodId: number): Promise<Array<SalaryRequestFullInfo>> {
        return httpService.get(`v1/admin/salaries/requests/${periodId}`).then(response => response.data);
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
