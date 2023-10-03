import {SimpleDict} from "@/store/modules/dict";
import {AxiosInstance} from "axios";
import httpService from "@/components/http.service";
import {DictAdminService} from "@/components/admin/dict/dict.admin.service";
import {WithId} from "@/components/shared/table/TableComponentDataContainer";

export const enum SalaryRequestImplementationState {
    IMPLEMENTED = 1,
    REJECTED = -1
}
export const salaryRequestImplementationStates = [
    SalaryRequestImplementationState.IMPLEMENTED,
    SalaryRequestImplementationState.REJECTED
];

export const enum SalaryRequestType {
    SALARY_INCREASE = 1,
    BONUS = 2
}
export const salaryRequestTypes = [
    SalaryRequestType.SALARY_INCREASE,
    SalaryRequestType.BONUS
];

export interface SalaryRequestReportBody {
    employeeId: number;
    type: SalaryRequestType;
    budgetBusinessAccount: number;
    budgetExpectedFundingUntil: string | null;
    salaryIncrease: number;
    /**
     * YYYYMM period. Month starts with 0. 202308 - September of 2023
     */
    increaseStartPeriod: number;
    assessmentId: number | null;
    reason: string;
    comment: string | null;
}

export interface SalaryRequest extends WithId{
    id: number;
    employee: SimpleDict;
    type: SalaryRequestType;
    budgetBusinessAccount: SimpleDict;
    budgetExpectedFundingUntil: string | null;
    createdAt: string;
    createdBy: SimpleDict;
    assessment: SimpleDict | null;
    employeeDepartment: SimpleDict;
    req: {
        salaryIncrease: number;
        /**
         * YYYYMM period. Month starts with 0. 202308 - September of 2023
         */
        increaseStartPeriod: number;
        reason: string;
        comment: string | null;
    },
    impl: {
        implementedAt: string|null;
        implementedBy: SimpleDict|null;
        state: SalaryRequestImplementationState;
        newPosition: SimpleDict|null;
        salaryIncrease: number;
        increaseStartPeriod: number;
        reason: string;
        comment: string | null;
    }
}

export interface ClosedSalaryRequestPeriod {
    period: number,
    closedBy: number,
    closedAt: Date,
    comment: string
}

export interface SalaryService {
    reportSalaryRequest(body: SalaryRequestReportBody): Promise<number>;

    getClosedSalaryRequestPeriods(): Promise<Array<ClosedSalaryRequestPeriod>>;

    deleteSalaryRequest(ids: number[]): Promise<Array<any>>;
}

class RestSalaryService implements SalaryService {
    constructor(private httpService: AxiosInstance) {
    }

    reportSalaryRequest(body: SalaryRequestReportBody): Promise<number> {
        return httpService.post("v1/salaries/requests", body);
    }

    deleteSalaryRequest(ids: number[]): Promise<Array<any>> {
        return Promise.all(
            ids.map(requestId => httpService.delete(`v1/admin/salaries/requests/${requestId}`))
        );
    }

    getClosedSalaryRequestPeriods(): Promise<Array<ClosedSalaryRequestPeriod>> {
        return httpService.get("v1/salaries/requests/periods").then(response => {
            return response.data;
        });
    }

}

const salaryService: SalaryService = new RestSalaryService(httpService);

export default salaryService;
