import httpService from "../../http.service";
import {AxiosInstance} from "axios";
import {ManagerOfObject} from "@/store/modules/dict";

export interface CreateOrUpdateBusinessAccount {
    name: string;
    description?: string;
    archived: boolean
}

export interface BusinessAccount {
    id: number,
    name: string;
    description?: string,
    managers: ManagerOfObject[],
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


    findAll(): Promise<BusinessAccount[]>;

    get(baId: number): Promise<BusinessAccount>;
}


class RestAdminBAService implements AdminBAService {
    constructor(private httpService: AxiosInstance) {
    }

    create(body: CreateOrUpdateBusinessAccount): Promise<number> {
        return httpService.post(`v1/admin/business_account`, body).then(response => {
            return response.data;
        });
    }

    update(baId: number, body: CreateOrUpdateBusinessAccount): Promise<number> {
        return httpService.put(`v1/admin/business_account/${baId}`, body).then(response => {
            return response.data;
        });
    }

    archive(baId: number): Promise<number> {
        return httpService.delete(`v1/admin/business_account/${baId}`).then(response => {
            return response.data;
        });
    }

    findAll(): Promise<BusinessAccount[]> {
        return httpService.get(`v1/admin/business_account?includeArchived=true`).then(response => {
            return response.data;
        });
    }

    get(baId: number): Promise<BusinessAccount> {
        return httpService.get(`v1/admin/business_account/${baId}`).then(response => {
            return response.data;
        });
    }


}


const adminBaService: AdminBAService = new RestAdminBAService(httpService);

export default adminBaService;

