import httpService from "../../http.service";
import {AxiosInstance} from "axios";
import {Skill} from "@/components/empl/skills/skills.service";
import {SimpleDict} from "@/store/modules/dict";

/**
 * 33 fields (1 is read only, should not be attached to the AdminEmployeeForm#EmployeeFrom)
 * If new fields added please don't forgot to update AdminEmployeeForm#EmployeeFrom
 */
export interface EmployeeWithAllDetails {
    id: number,
    displayName: string,
    birthday: string,
    sex: string,
    email: string,
    phone: string,
    skype: string,
    telegram: string,
    skills: Skill[],
    active: boolean,
    currentProjectId?: number,
    currentProjectRole: string | null,
    /**
     * Business account - readonly field (depends on current project)
     */
    baId?: number,
    departmentId: number,
    organizationId: number | null,
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
    currentProjectId?: number | null,
    currentProjectRole: string | null,
    displayName: string,
    departmentId?: number | null,
    organizationId?: number | null,
    birthday?: string,
    sex?: string,
    email?: string,
    phone?: string,
    skype?: string,
    telegram: string,
    dateOfEmployment?: string,
    levelId?: number | null,
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
    positionId?: number | null,
    officeLocationId?: number | null
}

/**
 * DTO for employee child
 */
export interface EmployeeKid {
    id: number,
    displayName: string,
    birthday?: string,
    age?: number,
    parent: SimpleDict,
}

/**
 * DTO to create or update employee child
 */
export interface CreateOrUpdateEmployeeKidBody {
    displayName: string,
    birthday?: string
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


    /**
     * Export all employees to excel
     */
    export(includeFired: boolean): Promise<void>;

    findAllKids(): Promise<EmployeeKid[]>;

    /**
     * Create new employee kid record
     */
    createKid(employeeId: number, body: CreateOrUpdateEmployeeKidBody): Promise<number>;

    /**
     * Update existing employee record
     */
    updateKid(employeeId: number, kidId: number, body: CreateOrUpdateEmployeeKidBody): Promise<number>;
}


class RestAdminEmployeeService implements AdminEmployeeService {
    constructor(private httpService: AxiosInstance) {
    }

    findAll(includeFired = true): Promise<EmployeeWithAllDetails[]> {
        return httpService.get(`v1/admin/employees`, {params: {'includeFired': includeFired}}).then(response => {
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

    export(includeFired: boolean): Promise<void> {
        return httpService.get(`v1/admin/employees/export?includeFired=${includeFired}`, {
            responseType: 'arraybuffer',
        }).then(response => {
            const blob = new Blob([response.data], {type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'})
            const link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = `AllEmployees.xlsx`;
            link.click();
        });
    }

    findAllKids(): Promise<EmployeeKid[]> {
        return httpService.get(`v1/admin/employees/kids`).then(response => {
            return response.data;
        });
    }

    createKid(employeeId: number, body: CreateOrUpdateEmployeeKidBody): Promise<number> {
        return httpService.post(`v1/admin/employees/${employeeId}/kids`, body).then(response => {
            return response.data;
        });
    }

    updateKid(employeeId: number, kidId: number, body: CreateOrUpdateEmployeeKidBody): Promise<number> {
        return httpService.put(`v1/admin/employees/${employeeId}/kids/${kidId}`, body).then(response => {
            return response.data;
        });
    }
}


const adminEmployeeService: AdminEmployeeService = new RestAdminEmployeeService(httpService);

export default adminEmployeeService;

