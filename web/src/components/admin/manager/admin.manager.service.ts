import httpService from "../../http.service";
import {AxiosInstance} from "axios";
import {SimpleDict} from "@/store/modules/dict";

export const managerResponsibilityTypes = ['technical', 'organization', 'hr'];
export const managerResponsibilityObjectTypes = ['project', 'business_account', 'department'];
export type ManagerResponsibilityType = typeof managerResponsibilityTypes[number];
export type ManagerResponsibilityObjectType = typeof managerResponsibilityObjectTypes[number];

export interface ManagerResponsibilityObjectId {
    id: number,
    type: ManagerResponsibilityObjectType;
}

export interface ManagerResponsibilityObject extends ManagerResponsibilityObjectId {
    name: string;
    /**
     * relevant to project and business_account
     */
    baId?: number,
    /**
     * relevant to project and department
     */
    departmentId?: number | null;
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
 * DTO to create manager link
 */
export interface CreateManagerBody {
    employee: number | null,
    responsibilityObjectType: ManagerResponsibilityObjectType,
    responsibilityObjectId: number | null,
    responsibilityType?: ManagerResponsibilityType | null,
    comment?: string,
}

/**
 * DTO to update manager link
 */
export interface UpdateManagerBody {
    responsibilityObjectType: ManagerResponsibilityObjectType,
    responsibilityObjectId: number,
    responsibilityType: ManagerResponsibilityType,
    comment?: string,
}


export interface AdminManagerService {
    findByObject(selectedObject: ManagerResponsibilityObjectId): | Promise<Manager[]>;

    findAll(): Promise<Manager[]>;

    /**
     * Create new record
     */
    create(body: CreateManagerBody): Promise<number>;

    /**
     * Update existing record
     */
    update(managerId: number, body: UpdateManagerBody): Promise<number>;

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

    findByObject(selectedObject: ManagerResponsibilityObjectId): Promise<Manager[]> {
        return httpService.get(`v1/admin/managers/object/${selectedObject.type}/${selectedObject.id}`).then(response => {
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

    delete(managerIds: number[]): Promise<Array<any>> {
        return Promise.all(
            managerIds.map(managerId => httpService.delete(`v1/admin/managers/${managerId}`))
        );
    }

}


const adminManagerService: AdminManagerService = new RestAdminManagerService(httpService);

export default adminManagerService;

