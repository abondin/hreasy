import httpService from "../http.service";
import {AxiosInstance} from "axios";
import {SimpleDict} from "@/store/modules/dict";


export interface UserSecurityInfo {
    employee: SimpleDict,
    userId? : number,
    departmentId?: number,
    currentProjectId?: number,
    roles: string[],
    accessibleDepartments: number[],
    accessibleProjects: number[],
    dateOfDismissal: string
}


export interface AdminUserService {
    findAll(): Promise<UserSecurityInfo[]>;
}


class RestAdminUserService implements AdminUserService {
    constructor(private httpService: AxiosInstance) {
    }

    findAll(): Promise<UserSecurityInfo[]> {
        return httpService.get(`v1/admin/users`).then(response => {
            return response.data;
        });
    }
}


const adminUserService: AdminUserService = new RestAdminUserService(httpService);

export default adminUserService;

