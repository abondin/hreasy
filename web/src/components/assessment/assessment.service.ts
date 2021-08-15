import httpService from "../http.service";
import {AxiosInstance} from "axios";


export interface AssessmentShortInfo {
    employeeId: number;
    displayName: string;
    lastAssessmentId: number | null;
    lastAssessmentDate: Date | null;
    lastAssessmentCompletedDate: Date | null;
    employeeDateOfEmployment: Date | null;
    latestActivity: Date|null;
}

export interface AssessmentService {
    allNotFiredEmployeesWithLatestAssessment(): Promise<AssessmentShortInfo[]>;
}

class RestAssessmentService implements AssessmentService {
    constructor(private httpService: AxiosInstance) {
    }

    allNotFiredEmployeesWithLatestAssessment(): Promise<AssessmentShortInfo[]> {
        return httpService.get(`v1/assessment`).then(response => {
            return (response.data as AssessmentShortInfo[]);
        });
    }
}

const assessmentService: AssessmentService = new RestAssessmentService(httpService);

export default assessmentService;

