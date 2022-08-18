import httpService from "../../http.service";
import {AxiosInstance} from "axios";

export type ManagerResponsibilityType = 'technical' | 'organization' | 'hr';

export interface ManagerResponsibilityObject {
    id: number,
    type: 'project' | 'business_account' | 'department';
}

export interface Manager {
    id: number,
    employee: number,
    responsibilityObject: ManagerResponsibilityObject,
    responsibilityType: ManagerResponsibilityType,
    comment?: string,
    createdAt?: string,
    createdBy?: number
}

/**
 * DTO to create or update employee child
 */
export interface CreateManagerBody {
    employee: number,
    responsibilityObject: ManagerResponsibilityObject,
    responsibilityType: ManagerResponsibilityType,
    comment?: string,
}

export interface UpdateManagerBody {
    responsibilityObject: ManagerResponsibilityObject,
    responsibilityType: ManagerResponsibilityType,
    comment?: string,
}

export interface AdminManagerService {
    findAll(): Promise<Manager[]>;

    /**
     * Create new record
     */
    create(body: CreateManagerBody): Promise<number>;

    /**
     * Update existing record
     */
    update(managerId: number, body: UpdateManagerBody): Promise<number>;

    delete(managerId: number): Promise<void>;
}


class RestAdminManagerService implements AdminManagerService {
    constructor(private httpService: AxiosInstance) {
    }

    findAll(): Promise<Manager[]> {
        return httpService.get(`v1/admin/managers`).then(response => {
            return response.data;
        });
    }

    create(body: CreateManagerBody): Promise<number> {
        return httpService.post(`v1/admin/managers`, body).then(response => {
            return response.data;
        });
    }

    update(managerId: number, body: UpdateManagerBody): Promise<number> {
        return httpService.put(`v1/admin/managers/${managerId}`, body).then(response => {
            return response.data;
        });
    }

    delete(managerId: number): Promise<void> {
        return httpService.delete(`v1/admin/managers/${managerId}`);
    }

}


const adminManagerService: AdminManagerService = new RestAdminManagerService(httpService);

export default adminManagerService;

