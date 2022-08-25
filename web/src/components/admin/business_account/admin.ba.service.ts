import httpService from "../../http.service";
import {AxiosInstance} from "axios";
import {SimpleDict} from "@/store/modules/dict";

export interface CreateOrUpdateBusinessAccount {
    name: string;
    description?: string;
    archived: boolean
}

export interface BusinessAccount {
    id: number,
    name: string;
    description?: string,
    createdBy?: number,
    createdAt?: Date,
    archived: boolean
}

export interface BusinessAccountPosition {
    id: number,
    name: string;
    description?: string,
    archived: boolean
    rate: number
}

export interface CreateOrUpdateBAPosition {
    name: string;
    description?: string;
    rate: number,
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

    findPositions(baId: number): Promise<BusinessAccountPosition[]>;

    /**
     * Create new Business Account Position
     */
    createPosition(baId: number, body: CreateOrUpdateBAPosition): Promise<number>;

    /**
     * Update existing Business Account Position
     */
    updatePosition(baId: number, positionId: number, body: CreateOrUpdateBAPosition): Promise<number>;
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

    findPositions(baId: number): Promise<BusinessAccountPosition[]> {
        return httpService.get(`v1/admin/business_account/${baId}/positions`).then(response => {
            return response.data;
        });
    }

    createPosition(baId: number, body: CreateOrUpdateBAPosition): Promise<number> {
        return httpService.post(`v1/admin/business_account/${baId}/positions`, body).then(response => {
            return response.data;
        });
    }

    updatePosition(baId: number, positionId: number, body: CreateOrUpdateBAPosition): Promise<number> {
        return httpService.put(`v1/admin/business_account/${baId}/positions/${positionId}`, body).then(response => {
            return response.data;
        });
    }

    archivePosition(baId: number, positionId: number): Promise<number> {
        return httpService.delete(`v1/admin/business_account/${baId}/positions/${positionId}`).then(response => {
            return response.data;
        });
    }
}


const adminBaService: AdminBAService = new RestAdminBAService(httpService);

export default adminBaService;

