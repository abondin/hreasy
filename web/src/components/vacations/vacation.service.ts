import httpService from "../http.service";
import {AxiosInstance} from "axios";
import {SimpleDict} from "@/store/modules/dict";

export interface EmployeeVacationShort {
    id: number,
    year: number,
    startDate: string,
    endDate: string,
    current: boolean
}

export interface Vacation {
    id: number,
    employee: number,
    employeeDisplayName: string,
    employeeCurrentProject: SimpleDict,
    year: number,
    startDate: string,
    endDate: string,
    notes: string,
    canceled: boolean,
    plannedStartDate?: string,
    plannedEndDate?: string,
    status: 'PLANNED' | 'TAKEN' | 'COMPENSATION' | 'CANCELED' | 'REJECTED',
    documents: string,
    daysNumber: number
}

export interface MyVacation {
    id: number,
    year: number,
    startDate: string,
    endDate: string,
    notes: string,
    plannedStartDate?: string,
    plannedEndDate?: string,
    status: 'PLANNED' | 'TAKEN' | 'COMPENSATION' | 'CANCELED' | 'REJECTED',
    documents: string,
    daysNumber: number
}


export interface CreateOrUpdateVacation {
    year: number,
    startDate: string,
    endDate: string,
    plannedStartDate: string,
    plannedEndDate: string,
    status: string,
    notes: string,
    documents: string,
    daysNumber: number
}

export interface VacationService {
    findAll(years: number[]): Promise<Vacation[]>;

    myFutureVacations(): Promise<MyVacation[]>;

    currentOrFutureVacations(employeeId: number): Promise<EmployeeVacationShort[]>;

    /**
     * Create new vacation entry
     */
    create(employeeId: number, body: CreateOrUpdateVacation): Promise<number>;

    /**
     * Update vacation entry
     */
    update(employeeId: number, vacationId: number, body: CreateOrUpdateVacation): Promise<number>;

    export(selectedYears: Array<number>): Promise<any>;
}

class RestVacationService implements VacationService {
    constructor(private httpService: AxiosInstance) {
    }

    public findAll(years: number[]): Promise<Vacation[]> {
        return httpService.get("v1/vacations", {params: {"years": years}}).then(response => {
            return response.data;
        });
    }

    myFutureVacations(): Promise<Vacation[]> {
        return httpService.get("v1/vacations/my").then(response => {
            return response.data;
        });
    }

    currentOrFutureVacations(employeeId: number): Promise<EmployeeVacationShort[]> {
        return httpService.get(`v1/vacations/${employeeId}/currentOrFuture`).then(response => {
            return response.data;
        });
    }

    public create(employeeId: number, body: CreateOrUpdateVacation): Promise<number> {
        return httpService.post(`v1/vacations/${employeeId}`, body).then(response => {
            return response.data;
        });
    }

    public update(employeeId: number, vacationId: number, body: CreateOrUpdateVacation): Promise<number> {
        return httpService.put(`v1/vacations/${employeeId}/${vacationId}`, body).then(response => {
            return response.data;
        });
    }

    public export(selectedYears: Array<number>): Promise<any> {
        return httpService.get(`v1/vacations/export`, {
            params: {years: selectedYears ? selectedYears.join(',') : undefined},
            responseType: 'arraybuffer',
        }).then(response => {
            const blob = new Blob([response.data], {type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'})
            const link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = `Vacations.xlsx`;
            link.click();
        });
    }
}

const vacationService: VacationService = new RestVacationService(httpService);

export default vacationService;

