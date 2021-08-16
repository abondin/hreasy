import httpService from "../http.service";
import {AxiosInstance} from "axios";
import {SimpleDict} from "@/store/modules/dict";


export interface EmployeeAssessmentsSummary {
    employeeId: number;
    displayName: string;
    lastAssessmentId: number | null;
    lastAssessmentDate: Date | null;
    lastAssessmentCompletedDate: Date | null;
    employeeDateOfEmployment: Date | null;
    latestActivity: Date | null;
    currentProject: SimpleDict | null;
    daysWithoutAssessment: number | null;
}

export interface AssessmentBase {
    id: number;
    plannedDate: Date | null;
    createdAt: Date | null;
    createdBy: SimpleDict | null;
    completedAt: Date | null;
    completedBy: SimpleDict | null;
    canceledAt: Date | null;
    canceledBy: SimpleDict | null;
}

export interface AssessmentService {
    allNotFiredEmployeesWithLatestAssessment(): Promise<EmployeeAssessmentsSummary[]>;

    employeeAssessments(employeeId: number): Promise<AssessmentBase[]>;
}

class RestAssessmentService implements AssessmentService {
    constructor(private httpService: AxiosInstance) {
    }

    allNotFiredEmployeesWithLatestAssessment(): Promise<EmployeeAssessmentsSummary[]> {
        return httpService.get(`v1/assessment`).then(response => {
            return (response.data as EmployeeAssessmentsSummary[]);
        });
    }

    employeeAssessments(employeeId: number): Promise<AssessmentBase[]> {
        return httpService.get(`v1/assessment/${employeeId}`).then(response => {
            return (response.data as AssessmentBase[]);
        });
    }
}

const assessmentService: AssessmentService = new RestAssessmentService(httpService);

export default assessmentService;

