import httpService from "../http.service";
import {AxiosInstance} from "axios";
import {SimpleDict} from "@/store/modules/dict";


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
    findAll(): Promise<Vacation[]>;

    myFutureVacations(): Promise<MyVacation[]>;

    /**
     * Create new vacation entry
     */
    create(employeeId: number, body: CreateOrUpdateVacation): Promise<number>;

    /**
     * Update vacation entry
     */
    update(employeeId: number, vacationId: number, body: CreateOrUpdateVacation): Promise<number>;
}

class RestVacationService implements VacationService {
    constructor(private httpService: AxiosInstance) {
    }

    public findAll(): Promise<Vacation[]> {
        return httpService.get("v1/vacations").then(response => {
            return response.data;
        });
    }

    myFutureVacations(): Promise<Vacation[]> {
        return httpService.get("v1/vacations/my").then(response => {
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
}

const vacationService: VacationService = new RestVacationService(httpService);

export default vacationService;

