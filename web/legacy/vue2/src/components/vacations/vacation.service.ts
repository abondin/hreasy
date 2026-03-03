import httpService from "../http.service";
import {AxiosInstance} from "axios";
import {CurrentProjectDict} from "@/components/empl/employee.service";

export interface EmployeeVacationShort {
    id: number,
    year: number,
    startDate: string,
    endDate: string,
    current: boolean
}

export interface VacPlanningPeriod {
    year: number
}

export const vacationStatuses = ['PLANNED', 'TAKEN', 'COMPENSATION', 'CANCELED', 'REJECTED', 'REQUESTED'] as const;
export type VacationStatus = typeof vacationStatuses[number];

export interface Vacation {
    id: number,
    employee: number,
    employeeDisplayName: string,
    employeeCurrentProject: CurrentProjectDict,
    year: number,
    startDate: string,
    endDate: string,
    notes: string,
    canceled: boolean,
    plannedStartDate?: string,
    plannedEndDate?: string,
    status: VacationStatus,
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
    status: VacationStatus,
    documents: string,
    daysNumber: number
}


export interface RequestOrUpdateMyVacation {
    year: number,
    startDate: string,
    endDate: string,
    notes: string,
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

    isNotWorkingDays(vacation: Vacation): boolean;

    openPlanningPeriods(): Promise<Array<VacPlanningPeriod>>;

    requestVacation(body: RequestOrUpdateMyVacation): Promise<number>;

    rejectVacationRequest(vacationId: number): Promise<number>;

    updatePlanningVacation(vacationId: number, body: RequestOrUpdateMyVacation): Promise<number>;
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

    openPlanningPeriods(): Promise<Array<VacPlanningPeriod>> {
        return httpService.get(`v1/vacations/planning-period`).then(response => {
            return response.data;
        });
    }

    requestVacation(body: RequestOrUpdateMyVacation): Promise<number> {
        return httpService.post('v1/vacations/request', body).then(response => {
            return response.data;
        });
    }

    rejectVacationRequest(vacationId: number): Promise<number> {
        return httpService.delete(`v1/vacations/my/${vacationId}`).then(response => response.data);
    }

    updatePlanningVacation(vacationId: number, body: RequestOrUpdateMyVacation): Promise<number> {
        return httpService.put(`v1/vacations/request/${vacationId}`, body).then(response => {
            return response.data;
        });
    }

    /**
     *
     * @param vacation
     * @return true if employee doesn't work in the vacation
     */
    public isNotWorkingDays(vacation: Vacation): boolean {
        return vacation && (vacation.status == "PLANNED" || vacation.status == "TAKEN");
    }
}

const vacationService: VacationService = new RestVacationService(httpService);

export default vacationService;

