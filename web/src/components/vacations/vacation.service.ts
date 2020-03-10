import httpService from "../http.service";
import {AxiosInstance} from "axios";


export interface Vacation {
    id: number,
    employee: number,
    emplyeeDisplayName: String,
    year: number,
    startDate?: Date,
    endDate?: Date,
    notes: String
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

