import {AxiosInstance} from "axios";
import httpService from "@/components/http.service";
import {WithId} from "@/components/shared/table/TableComponentDataContainer";
import {SimpleDict} from "@/store/modules/dict";
import {SalaryRequestImplementationState} from "@/components/admin/salary/admin.salary.service";
import {CurrentProjectDict} from "@/components/empl/employee.service";
import {SalaryRequestUpdateBody} from "@/components/salary/details/info/salary-request.update.action";

export const enum SalaryRequestType {
    SALARY_INCREASE = 1,
    BONUS = 2
}

export const salaryRequestTypes = [
    SalaryRequestType.SALARY_INCREASE,
    SalaryRequestType.BONUS
];

export const enum SalaryApprovalState {
    COMMENT = 1,
    APPROVE = 2,
    DECLINE = 3
}

export const salaryApprovalStates = [
    SalaryApprovalState.COMMENT,
    SalaryApprovalState.APPROVE,
    SalaryApprovalState.DECLINE
];

export interface SalaryRequestReportBody {
    employeeId: number;
    type: SalaryRequestType;
    budgetBusinessAccount: number | null | undefined;
    budgetExpectedFundingUntil: string | null;
    increaseAmount: number | null;

    //TODO After salary storing feature implemented populate this field automatically
    currentSalaryAmount: number | null;

    //TODO After salary storing feature implemented populate this field automatically
    previousSalaryIncreaseDate: string | null;
    previousSalaryIncreaseText: string | null;

    plannedSalaryAmount: number | null;
    /**
     * YYYYMM period. Month starts with 0. 202308 - September of 2023
     */
    increaseStartPeriod: number;
    assessmentId: number | null;
    reason: string | null;
    comment: string | null;
}

export interface ClosedSalaryRequestPeriod {
    period: number,
    closedBy: number,
    closedAt: Date,
    comment: string
}

export interface SalaryRequestCommentBody {
    comment: string | null;
}

export interface SalaryRequestApproveBody {
    comment: string | null;
}

export interface SalaryRequestDeclineBody {
    comment: string | null;
}

export interface SalaryService {
    reportSalaryRequest(body: SalaryRequestReportBody): Promise<number>;

    getClosedSalaryRequestPeriods(): Promise<Array<ClosedSalaryRequestPeriod>>;

    deleteSalaryRequest(id: number): Promise<number>;

    updateSalaryRequest(id: number, body: SalaryRequestUpdateBody): Promise<number>;

    load(period: number): Promise<Array<SalaryIncreaseRequest>>;

    get(period: number, requestId: number): Promise<SalaryIncreaseRequest>;

    approve(requestId: number, body: SalaryRequestApproveBody): Promise<number>;

    decline(requestId: number, body: SalaryRequestDeclineBody): Promise<number>;

    comment(requestId: number, body: SalaryRequestCommentBody): Promise<number>;

    deleteApproval(requestId: number, approvalId: number): Promise<number>;

    getApprovals(requestId: number): Promise<Array<SalaryRequestApproval>>;
}

class RestSalaryService implements SalaryService {
    constructor(private httpService: AxiosInstance) {
    }

    load(period: number): Promise<Array<SalaryIncreaseRequest>> {
        return httpService.get(`v1/salaries/requests/${period}`).then(response => {
            return response.data;
        });
    }

    get(period: number, requestId: number): Promise<SalaryIncreaseRequest> {
        return httpService.get(`v1/salaries/requests/${period}/${requestId}`).then(response => {
            return response.data;
        });
    }

    reportSalaryRequest(body: SalaryRequestReportBody): Promise<number> {
        return httpService.post("v1/salaries/requests", body);
    }

    deleteSalaryRequest(requestId: number): Promise<number> {
        return httpService.delete(`v1/salaries/requests/${requestId}`);
    }

    updateSalaryRequest(requestId: number, body: SalaryRequestUpdateBody): Promise<number> {
        return httpService.put(`v1/salaries/requests/${requestId}`, body);
    }

    getClosedSalaryRequestPeriods(): Promise<Array<ClosedSalaryRequestPeriod>> {
        return httpService.get("v1/salaries/requests/periods").then(response => {
            return response.data;
        });
    }

    approve(requestId: number, body: SalaryRequestApproveBody): Promise<number> {
        return httpService.post(`v1/salaries/requests/${requestId}/approvals/approve`, body);
    }

    decline(requestId: number, body: SalaryRequestDeclineBody): Promise<number> {
        return httpService.post(`v1/salaries/requests/${requestId}/approvals/decline`, body);
    }

    comment(requestId: number, body: SalaryRequestCommentBody): Promise<number> {
        return httpService.post(`v1/salaries/requests/${requestId}/approvals/comment`, body);
    }

    deleteApproval(requestId: number, approvalId: number): Promise<number> {
        return httpService.delete(`v1/salaries/requests/${requestId}/approvals/${approvalId}`);
    }

    getApprovals(requestId: number): Promise<Array<SalaryRequestApproval>> {
        return httpService.get(`v1/salaries/requests/${requestId}/approvals`).then(response => {
            return response.data;
        });
    }
}

const salaryService: SalaryService = new RestSalaryService(httpService);

export default salaryService;

export interface SalaryIncreaseRequest extends WithId {
    id: number;
    employee: SimpleDict;
    type: SalaryRequestType;
    budgetBusinessAccount: SimpleDict;
    budgetExpectedFundingUntil: string | null;
    createdAt: string;
    createdBy: SimpleDict;
    assessment: SimpleDict | null;
    employeeInfo: {
        currentProject: CurrentProjectDict | null;
        dateOfEmployment: string | null;
        ba: SimpleDict | null;
        position: SimpleDict | null;
        currentSalaryAmount: number | null;
        previousSalaryIncreaseText: string | null;
        previousSalaryIncreaseDate: string | null;
    }
    req: {
        increaseAmount: number;
        plannedSalaryAmount: number | null;
        /**
         * YYYYMM period. Month starts with 0. 202308 - September of 2023
         */
        increaseStartPeriod: number;
        newPosition: SimpleDict | null;
        reason: string;
        comment: string | null;
    },
    impl?: {
        implementedAt: string | null;
        implementedBy: SimpleDict | null;
        state: SalaryRequestImplementationState;
        newPosition: SimpleDict | null;
        increaseAmount: number;
        salaryAmount: number | null;
        increaseStartPeriod: number;
        rejectReason: string;
        comment: string | null;
    };
    approvals: SalaryRequestApproval[]
}

/**
 * One approve, decline or comment
 */
export interface SalaryRequestApproval {
    id: number;
    requestId: number;
    state: SalaryApprovalState;
    comment: string | null;
    createdAt: string;
    createdBy: SimpleDict;
}
