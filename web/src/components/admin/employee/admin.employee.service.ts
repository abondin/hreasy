import httpService from "../../http.service";
import {AxiosInstance} from "axios";
import {Employee} from "@/components/empl/employee.service";
import {CreateOrUpdateBusinessAccount} from "@/components/admin/business_account/admin.ba.service";


export interface EmployeeWithAllDetails extends Employee {
    id: number,
    displayName: string,
    currentProjectId?: number,
    lastname: string,
    firstname: string,
    patronymicName: string,
    birthday: string,
    sex: string,
    email: string,
    phone: string,
    skype: string,
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

export interface CreateOrUpdateEmployeeBody{
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

