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
    employee: SimpleDict | null;
    createdBy: SimpleDict | null;
    completedAt: Date | null;
    completedBy: SimpleDict | null;
    canceledAt: Date | null;
    canceledBy: SimpleDict | null;
}


export interface AssessmentWithFormsAndFiles extends AssessmentBase {
    forms: AssessmentForm[];
    attachmentsFilenames: string[];
    attachmentsAccessToken: string;
}

export interface AssessmentForm {
    id: number;
    owner: number;
    formType: number;
    content: string | null;
    completedAt: Date | null;
    completedBy: number | null;
}

export interface UploadAssessmentAttachmentResponse {

}

export interface DeleteAssessmentAttachmentResponse {
    deleted: boolean;
}

export interface AssessmentService {
    allNotFiredEmployeesWithLatestAssessment(): Promise<EmployeeAssessmentsSummary[]>;

    assessmentWithFormsAndFiles(employeeId: number, assessmentId: number): Promise<AssessmentWithFormsAndFiles>;

    employeeAssessments(employeeId: number): Promise<AssessmentBase[]>;

    getUploadAttachmentUrl(employeeId: number, assessmentId: number): string;

    getAttachmentPath(employeeId: number, assessmentId: number, attachmentFilename: string, accessToken: string): string;

    deleteAttachment(employeeId: number, assessmentId: number, attachmentFilename: string): Promise<DeleteAssessmentAttachmentResponse>;
}

class RestAssessmentService implements AssessmentService {
    constructor(private httpService: AxiosInstance) {
    }

    allNotFiredEmployeesWithLatestAssessment(): Promise<EmployeeAssessmentsSummary[]> {
        return httpService.get(`v1/assessment`).then(response => {
            return (response.data as EmployeeAssessmentsSummary[]);
        });
    }

    assessmentWithFormsAndFiles(employeeId: number, assessmentId: number): Promise<AssessmentWithFormsAndFiles> {
        return httpService.get(`v1/assessment/${employeeId}/${assessmentId}`).then(response => {
            return (response.data as AssessmentWithFormsAndFiles);
        });
    }

    employeeAssessments(employeeId: number): Promise<AssessmentBase[]> {
        return httpService.get(`v1/assessment/${employeeId}`).then(response => {
            return (response.data as AssessmentBase[]);
        });
    }

    getUploadAttachmentUrl(employeeId: number, assessmentId: number): string {
        return `${httpService.defaults.baseURL}v1/assessment/${employeeId}/${assessmentId}/attachment`;
    }

    deleteAttachment(employeeId: number, assessmentId: number, attachmentFilename: string): Promise<DeleteAssessmentAttachmentResponse> {
        return httpService.delete(`v1/assessment/${employeeId}/${assessmentId}/attachment/${attachmentFilename}`)
            .then(response => {
                return (response.data as DeleteAssessmentAttachmentResponse);
            });
    }


    getAttachmentPath(employeeId: number, assessmentId: number, attachmentFilename: string, accessToken: string): string {
        return `${httpService.defaults.baseURL}v1/fs/assessment/${employeeId}/${assessmentId}/${attachmentFilename}/${accessToken}`;
    }
}

const assessmentService: AssessmentService = new RestAssessmentService(httpService);

export default assessmentService;

