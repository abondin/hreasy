import {SimpleDict} from "@/store/modules/dict";
import {AxiosInstance} from "axios";
import httpService from "@/components/http.service";
import {DictAdminService} from "@/components/admin/dict/dict.admin.service";
import {WithId} from "@/components/shared/table/TableComponentDataContainer";

export enum SalaryRequestStat {
    CREATED = 0,
    IN_PROGRESS = 1,
    IMPLEMENTED = 2
}

export enum SalaryRequestType {
    SALARY_INCREASE = 1,
    BONUS = 2
}

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
    stat: SalaryRequestStat;
    salaryIncrease: number;
    /**
     * YYYYMM period. Month starts with 0. 202308 - September of 2023
     */
    increaseStartPeriod: number;
    assessment: SimpleDict | null;
    employeeDepartment: SimpleDict;
    reason: string;
    comment: string | null;
    createdAt: string;
    createdBy: SimpleDict;
    inprogressAt: string|null;
    inprogressBy: SimpleDict|null;
    implementedAt: string|null;
    implementedBy: SimpleDict|null;
}

export interface SalaryService {
    loadAllSalaryRequests(): Promise<Array<SalaryRequest>>;

    reportSalaryRequest(body: SalaryRequestReportBody): Promise<number>;
}

class RestSalaryService implements SalaryService {
    constructor(private httpService: AxiosInstance) {
    }

    loadAllSalaryRequests(): Promise<Array<SalaryRequest>> {
        return httpService.get("v1/salaries/requests").then(response => response.data);
    }

    reportSalaryRequest(body: SalaryRequestReportBody): Promise<number> {
        return httpService.post("v1/salaries/requests", body);
    }
}

const salaryService: SalaryService = new RestSalaryService(httpService);

export default salaryService;