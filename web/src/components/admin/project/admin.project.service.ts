import httpService from "../../http.service";
import {AxiosInstance} from "axios";
import {SimpleDict} from "@/store/modules/dict";

export interface CreateOrUpdateProject {
    name: string;
    startDate?: string,
    endDate?: string,
    customer?: string,
    departmentId: number,
    baId: number,
    info?: string
}

export interface ProjectFullInfo {
    id: number,
    name: string;
    startDate?: string,
    endDate?: string,
    customer?: string,
    department: SimpleDict,
    businessAccount: SimpleDict
    createdBy?: number,
    createdAt?: Date,
    info?: string,
    active: boolean
}



export interface AdminProjectService {
    /**
     * Create new project
     */
    create(body: CreateOrUpdateProject): Promise<number>;

    /**
     * Update existing project
     */
    update(projectId: number, body: CreateOrUpdateProject): Promise<number>;


    findAll(): Promise<ProjectFullInfo[]>;

    get(projectId: number): Promise<ProjectFullInfo>;
}

export class ProjectCreatedEvent {
    public constructor(public projectId: number) {
    }

}


class RestAdminProjectService implements AdminProjectService {
    constructor(private httpService: AxiosInstance) {
    }

    create(body: CreateOrUpdateProject): Promise<number> {
        return httpService.post(`v1/admin/projects`, body).then(response => {
            return response.data;
        });
    }

    update(projectId: number, body: CreateOrUpdateProject): Promise<number> {
        return httpService.put(`v1/admin/projects/${projectId}`, body).then(response => {
            return response.data;
        });
    }

    findAll(): Promise<ProjectFullInfo[]> {
        return httpService.get(`v1/admin/projects`).then(response => {
            return response.data;
        });
    }

    get(projectId: number): Promise<ProjectFullInfo> {
        return httpService.get(`v1/admin/projects/${projectId}`).then(response => {
            return response.data;
        });
    }
}


const adminProjectService: AdminProjectService = new RestAdminProjectService(httpService);

export default adminProjectService;

