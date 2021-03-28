import httpService from "../http.service";
import {AxiosInstance} from "axios";
import {Skill} from "@/components/empl/skills/skills.service";

export class Dict {
    constructor(public id: number, public name: string) {
    }
}

export interface Employee {
    id: number,
    displayName: string,
    currentProject?: Dict,
    lastname: string,
    firstname: string,
    patronymicName: string,
    birthday: string,
    sex: string,
    department: Dict,
    position: Dict,
    officeLocation: Dict,
    email: string,
    phone: string,
    skype: string,
    hasAvatar: boolean,
    skills: Skill[]
}


export interface EmployeeService {
    findAll(): Promise<Employee[]>;

    find(id: number): Promise<Employee>;

    getAvatarUrl(employeeId: number): any;

    getAvatarUploadUrl(employeeId: number): any;

    updateCurrentProject(employeeId: number, projectId: number | null): Promise<number>;
}

class RestEmployeeService implements EmployeeService {
    constructor(private httpService: AxiosInstance) {
    }

    public findAll(): Promise<Employee[]> {
        return httpService.get("v1/employee").then(response => {
            return response.data;
        });
    }

    public find(id: number): Promise<Employee> {
        return httpService.get(`v1/employee/${id}`).then(response => {
            return response.data;
        });
    }

    getAvatarUrl(employeeId: number): any {
        return `${httpService.defaults.baseURL}v1/fs/avatar/${employeeId}`;
    }

    getAvatarUploadUrl(employeeId: number): any {
        return `${httpService.defaults.baseURL}v1/fs/avatar/${employeeId}/upload`;
    }

    updateCurrentProject(employeeId: number, projectId: number | null): Promise<number> {
        return httpService.put(`v1/employee/${employeeId}/currentProject/${projectId ? projectId : 'reset'}`).then(response => {
            return response.data;
        });

    }
}

const employeeService: EmployeeService = new RestEmployeeService(httpService);

export default employeeService;

