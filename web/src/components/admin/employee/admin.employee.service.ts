import httpService from "../../http.service";
import {AxiosInstance} from "axios";
import {Dict, Employee} from "@/components/empl/employee.service";
import {Skill} from "@/components/empl/skills/skills.service";


/**
 * 32 fields
 * If new fields added please don't forgot to update AdminEmployeeForm#EmployeeFrom
 */
export interface EmployeeWithAllDetails {
    id: number,
    lastname: string,
    firstname: string,
    patronymicName: string,
    birthday: string,
    sex: string,
    email: string,
    phone: string,
    skype: string,
    skills: Skill[],
    active: boolean,
    displayName: string,
    currentProjectId?: number,
    departmentId: number,
    dateOfEmployment?: string,
    levelId?: number,
    workType?: string,
    workDay?: string,
    registrationAddress?: string,
    documentSeries?: string,
    documentNumber?: string,
    documentIssuedBy?: string,
    documentIssuedDate?: string,
    foreignPassport?: string,
    cityOfResidence?: string,
    englishLevel?: string,
    familyStatus?: string,
    spouseName?: string,
    children?: string,
    dateOfDismissal?: string,
    documentFull?: string,
    positionId?: number,
    officeLocationId?: number
}

export interface CreateOrUpdateEmployeeBody {
    email: string,
    //TODO Add fields
}

export interface AdminEmployeeService {
    findAll(): Promise<EmployeeWithAllDetails[]>;

    /**
     * Create new employee record
     */
    create(body: CreateOrUpdateEmployeeBody): Promise<number>;

    /**
     * Update existing employee record
     */
    update(employeeId: number, body: CreateOrUpdateEmployeeBody): Promise<number>;
}


class RestAdminEmployeeService implements AdminEmployeeService {
    constructor(private httpService: AxiosInstance) {
    }

    findAll(): Promise<EmployeeWithAllDetails[]> {
        return httpService.get(`v1/admin/employees`).then(response => {
            return response.data;
        });
    }

    create(body: CreateOrUpdateEmployeeBody): Promise<number> {
        return httpService.post(`v1/admin/employees`, body).then(response => {
            return response.data;
        });
    }

    update(employeeId: number, body: CreateOrUpdateEmployeeBody): Promise<number> {
        return httpService.put(`v1/admin/employees/${employeeId}`, body).then(response => {
            return response.data;
        });
    }

}


const adminEmployeeService: AdminEmployeeService = new RestAdminEmployeeService(httpService);

export default adminEmployeeService;

