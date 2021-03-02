import httpService from "../http.service";
import {AxiosInstance} from "axios";
import {SimpleDict} from "@/store/modules/dict";


export interface Vacation {
    id: number,
    employee: number,
    employeeDisplayName: string,
    employeeCurrentProject: SimpleDict,
    year: number,
    startDate?: string,
    endDate?: string,
    notes: string,
    canceled: boolean,
    plannedStartDate?: string,
    plannedEndDate?: string,
    status: 'PLANNED'|'TAKEN'|'CANCELED',
    documents: string,
    daysNumber: number
}

export interface VacationService {
    findAll(): Promise<Vacation[]>;
}

class RestVacationService implements VacationService {
    constructor(private httpService: AxiosInstance) {
    }

    public findAll(): Promise<Vacation[]> {
        return httpService.get("v1/vacation").then(response => {
            return response.data;
        });
    }
}

const vacationService: VacationService = new RestVacationService(httpService);

export default vacationService;

