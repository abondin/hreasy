import httpService from "../http.service";
import {AxiosInstance} from "axios";
import {SimpleDict} from "@/store/modules/dict";
import {CurrentProjectDict} from "@/components/empl/employee.service";


export interface EmployeeAssessmentsSummary {
    employeeId: number;
    displayName: string;
    lastAssessmentId: number | null;
    lastAssessmentDate: Date | null;
    lastAssessmentCompletedDate: Date | null;
    employeeDateOfEmployment: Date | null;
    latestActivity: Date | null;
    currentProject: CurrentProjectDict | null;
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

export interface NewAssessmentBody {
    plannedDate: string;
    managers: number[];
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

    /**
     *
     * @param employeeId
     * @param body
     * @return id of the created assessment
     */
    scheduleNewAssessment(employeeId: number, body: NewAssessmentBody): Promise<number>;

    cancelAssessment(employeeId: number, assessmentId: number): Promise<any>;

    completeAssessment(employeeId: number, assessmentId: number): Promise<any>;

    getUploadAttachmentUrl(employeeId: number, assessmentId: number): string;

    getAttachmentPath(employeeId: number, assessmentId: number, attachmentFilename: string, accessToken: string): string;

    deleteAttachment(employeeId: number, assessmentId: number, attachmentFilename: string): Promise<DeleteAssessmentAttachmentResponse>;

    export(): Promise<void>;
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

    scheduleNewAssessment(employeeId: number, body: NewAssessmentBody): Promise<number> {
        return httpService.post(`v1/assessment/${employeeId}`, body).then(response => {
            return (response.data as number);
        });
    }

    cancelAssessment(employeeId: number, assessmentId: number): Promise<any> {
        return httpService.delete(`v1/assessment/${employeeId}/${assessmentId}`).then(response => {
            return response.data;
        });
    }

    completeAssessment(employeeId: number, assessmentId: number): Promise<any> {
        return httpService.post(`v1/assessment/${employeeId}/${assessmentId}/complete`).then(response => {
            return response.data;
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

    export(): Promise<void> {
        return httpService.get('v1/assessment/export', {
            responseType: 'arraybuffer',
        }).then(response => {
            const blob = new Blob([response.data], {type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'})
            const link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = `AssessmentsSummary.xlsx`;
            link.click();
        });
    }
}

const assessmentService: AssessmentService = new RestAssessmentService(httpService);

export default assessmentService;

