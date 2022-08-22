import httpService from "../../http.service";
import {AxiosInstance} from "axios";
import {SimpleDict} from "@/store/modules/dict";

export type ManagerResponsibilityType = 'technical' | 'organization' | 'hr';
export type ManagerResponsibilityObjectType = 'project' | 'business_account' | 'department';

export interface ManagerResponsibilityObject {
    id: number,
    type: ManagerResponsibilityObjectType;
    name: string;
    /**
     * relevant to project and business_account
     */
    baId?: number,
    /**
     * relevant to project and department
     */
    departmentId?: number|null;
}

export interface Manager {
    id: number,
    employee: SimpleDict,
    responsibilityObject: ManagerResponsibilityObject,
    responsibilityType: ManagerResponsibilityType,
    comment?: string,
    createdAt?: string,
    createdBy?: number
}

/**
 * DTO to create or update employee child
 */
export interface CreateOrManagerBody {
    employee: number,
    responsibilityObject: ManagerResponsibilityObject,
    responsibilityType: ManagerResponsibilityType,
    comment?: string,
}


export interface AdminManagerService {
    findAll(): Promise<Manager[]>;

    /**
     * Create new record
     */
    create(body: CreateOrManagerBody): Promise<Manager>;

    /**
     * Update existing record
     */
    update(managerId: number, body: CreateOrManagerBody): Promise<Manager>;

    delete(managerId: number[]): Promise<Array<any>>;
}


class RestAdminManagerService implements AdminManagerService {
    constructor(private httpService: AxiosInstance) {
    }

    findAll(): Promise<Manager[]> {
        return httpService.get(`v1/admin/managers`).then(response => {
            return response.data;
        });
    }

    create(body: CreateOrManagerBody): Promise<Manager> {
        return httpService.post(`v1/admin/managers`, body).then(response => {
            return response.data;
        });
    }

    update(managerId: number, body: CreateOrManagerBody): Promise<Manager> {
        return httpService.put(`v1/admin/managers/${managerId}`, body).then(response => {
            return response.data;
        });
    }

    delete(managerIds: number[]): Promise<Array<any>> {
        return Promise.all(
            managerIds.map(managerId => httpService.delete(`v1/admin/managers/${managerId}`))
        );
    }

}


const adminManagerService: AdminManagerService = new RestAdminManagerService(httpService);

export default adminManagerService;

