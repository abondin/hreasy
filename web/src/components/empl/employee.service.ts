import httpService from "../http.service";
import {AxiosInstance} from "axios";

export class Dict {
    constructor(id: number, name: string) {
    }
}

export interface Employee {
    id: number,
    displayName: string,
    currentProject: Dict,
    lastname: string,
    firstname: string,
    patronymicName: string,
    birthday: string,
    sex: string,
    department: Dict,
    position: Dict,
    email: string,
    phone: string,
    skype: string
}

export interface EmployeeService {
    findAll(): Promise<Employee[]>;

    getAvatarUrl(employeeId: number): any;
}

class RestEmployeeService implements EmployeeService {
    constructor(private httpService: AxiosInstance) {
    }

    public findAll(): Promise<Employee[]> {
        return httpService.get("v1/employee").then(response => {
            return response.data;
        });
    }

    getAvatarUrl(employeeId: number): any {
        return `${httpService.defaults.baseURL}v1/fs/avatar/${employeeId}`;
    }
}

const employeeService: EmployeeService = new RestEmployeeService(httpService);

export default employeeService;

