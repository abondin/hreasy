import httpService from "../http.service";
import {AxiosInstance} from "axios";


export interface AdminService {
    /**
     * Close overtime period for editing
     * @param periodId - report period in yyyymm format. For example 202006 for all overtimes, reported in June
     * @param comment - optional comment
     */
    closeOvertimePeriod(periodId: number, comment?: string): Promise<any>;

    /**
     * Reopen overtime period for editing
     * @param periodId - report period in yyyymm format. For example 202006 for all overtimes, reported in June
     * @param comment - optional comment
     */
    reopenOvertimePeriod(periodId: number, comment?: string): Promise<any>;

}


class RestAdminService implements AdminService {
    constructor(private httpService: AxiosInstance) {
    }

    closeOvertimePeriod(periodId: number, comment?: string): Promise<any> {
        return httpService.post(`v1/admin/overtimes/${periodId}/close`, {comment: comment}).then(response => {
            return response.data;
        });
    }

    reopenOvertimePeriod(periodId: number, comment?: string): Promise<any> {
        return httpService.post(`v1/admin/overtimes/${periodId}/reopen`, {comment: comment}).then(response => {
            return response.data;
        });
    }
}


const adminService: AdminService = new RestAdminService(httpService);

export default adminService;

