import httpService from "../../http.service";
import {AxiosInstance} from "axios";
import {SimpleDict} from "@/store/modules/dict";

export interface CreateOrUpdateBusinessAccount {
    name: string;
    description?: string;
    responsibleEmployeeId?: number
}

export interface BusinessAccount {
    id: number,
    name: string;
    responsibleEmployee?: SimpleDict,
    description?: string,
    createdBy?: number,
    createdAt?: Date,
    archived: boolean
}


export interface AdminBAService {
    /**
     * Create new Business Account
     */
    create(body: CreateOrUpdateBusinessAccount): Promise<number>;

    /**
     * Update existing Business Account
     */
    update(baId: number, body: CreateOrUpdateBusinessAccount): Promise<number>;

    /**
     * Archive Business Account
     */
    archive(baId: number): Promise<number>;


    findAll(): Promise<BusinessAccount[]>;

}


class RestAdminBAService implements AdminBAService {
    constructor(private httpService: AxiosInstance) {
    }

    create(body: CreateOrUpdateBusinessAccount): Promise<number> {
        return httpService.post(`v1/admin/business_accounts`, body).then(response => {
            return response.data;
        });
    }

    update(baId: number, body: CreateOrUpdateBusinessAccount): Promise<number> {
        return httpService.put(`v1/admin/business_accounts/${baId}`, body).then(response => {
            return response.data;
        });
    }

    archive(baId: number): Promise<number> {
        return httpService.delete(`v1/admin/business_accounts/${baId}`).then(response => {
            return response.data;
        });
    }

    findAll(): Promise<BusinessAccount[]> {
        return httpService.get(`v1/admin/business_accounts?includeArchived=true`).then(response => {
            return response.data;
        });
    }
}


const adminBaService: AdminBAService = new RestAdminBAService(httpService);

export default adminBaService;

