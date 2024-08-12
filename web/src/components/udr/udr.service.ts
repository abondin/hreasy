import {AxiosInstance} from "axios";
import httpService from "@/components/http.service";
import {SimpleDict, ValueWithStatus} from "@/store/modules/dict";
import {CurrentProjectDict} from "@/components/empl/employee.service";
import {CreateBody, UpdateBody, WithId} from "@/components/shared/table/TableComponentDataContainer";
import {DateTimeUtils} from "@/components/datetimeutils";

export const enum JuniorProgressType {
    DEGRADATION = 1,
    NO_PROGRESS = 2,
    PROGRESS = 3,
    GOOD_PROGRESS = 4
}


export const juniorProgressTypes = [
    JuniorProgressType.DEGRADATION,
    JuniorProgressType.NO_PROGRESS,
    JuniorProgressType.PROGRESS,
    JuniorProgressType.GOOD_PROGRESS
];


export interface UpdateJuniorRegistryBody extends UpdateBody {
    mentorId: number | null;
    role: string;
    budgetingAccount: number | null;
}

export interface GraduateBody {
    comment: string | null;
}

export interface AddJuniorRegistryBody extends CreateBody {
    juniorEmplId: number | null,
    mentorId: number | null;
    role: string | null;
    budgetingAccount: number | null;
}

export interface JuniorGraduation {
    graduatedAt: string;
    graduatedBy: SimpleDict;
    comment: string;
}


export interface JuniorReport {
    id: number;
    progress: JuniorProgressType,
    createdAt: string;
    createdBy: SimpleDict;
    comment: string;
    ratings: JuniorReportRatings;
}

export interface AddOrUpdateJuniorReportBody {
    progress: JuniorProgressType;
    comment: string;
    ratings: JuniorReportRatings;
}

export interface JuniorReportRatings {
    competence: number,
    process: number,
    teamwork: number,
    contribution: number,
    motivation: number
}

export interface JuniorDto extends WithId {
    juniorEmpl: SimpleDict;
    juniorDateOfEmployment: string,
    juniorInCompanyMonths: ValueWithStatus<number>,
    monthsWithoutReport: ValueWithStatus<number>,
    mentor?: SimpleDict;
    role: string;
    currentProject?: CurrentProjectDict;
    budgetingAccount?: SimpleDict;
    createdAt: string;
    createdBy: SimpleDict;
    graduation?: JuniorGraduation;
    reports: JuniorReport[];
    latestReport: JuniorReport;
}


export interface JuniorRegistryService {
    addToRegistry(body: AddJuniorRegistryBody): Promise<number>;

    updateInRegistry(juniorRegistyId: number, body: UpdateJuniorRegistryBody): Promise<number>;

    deleteFromRegistry(id: number): Promise<number>;

    juniors(): Promise<Array<JuniorDto>>;

    export(includeGraduated: boolean): Promise<any>;

    juniorDetails(juniorRegistryId: number): Promise<JuniorDto>;

    graduate(juniorRegistyId: number, body: GraduateBody | null): Promise<number>;

    cancelGraduation(id: number): Promise<any> | Promise<number>;

    /**
     *
     * @param juniorId
     * @param body
     * @return reportId
     */
    createReport(juniorId: number, body: AddOrUpdateJuniorReportBody): Promise<number>;

    /**
     *
     * @param juniorId
     * @param reportId
     * @param body
     * @return reportId
     */
    updateReport(juniorId: number, reportId: number, body: AddOrUpdateJuniorReportBody): Promise<number>;

    deleteReport(juniorId: number, reportId: number): Promise<number>;
}

class RestJuniorRegistryService implements JuniorRegistryService {
    constructor(private httpService: AxiosInstance) {
    }

    juniors(): Promise<Array<JuniorDto>> {
        return httpService.get(`v1/udr/juniors`).then(response => {
            return response.data;
        });
    }

    juniorDetails(juniorRegistryId: number): Promise<JuniorDto> {
        return httpService.get(`v1/udr/juniors/${juniorRegistryId}`).then(response => {
            return response.data;
        });
    }

    addToRegistry(body: AddJuniorRegistryBody): Promise<number> {
        return httpService.post(`v1/udr/juniors`, body).then(response => {
            return response.data;
        });
    }

    updateInRegistry(juniorRegistyId: number, body: UpdateJuniorRegistryBody): Promise<number> {
        return httpService.put(`v1/udr/juniors/${juniorRegistyId}`, body).then(response => {
            return response.data;
        });
    }


    deleteFromRegistry(id: number): Promise<number> {
        return httpService.delete(`v1/udr/juniors/${id}`).then(response => {
            return response.data;
        });
    }

    graduate(juniorRegistyId: number, body: GraduateBody | null): Promise<number> {
        return httpService.put(`v1/udr/juniors/${juniorRegistyId}/graduate`, body).then(response => {
            return response.data;
        });
    }

    cancelGraduation(id: number): Promise<any> | Promise<number> {
        return httpService.delete(`v1/udr/juniors/${id}/graduate`).then(response => {
            return response.data;
        });
    }

    export(includeGraduated: boolean): Promise<any> {
        return httpService.get(`v1/udr/juniors/export`, {
            params: {
                includeGraduated: includeGraduated
            },
            responseType: 'arraybuffer',
        }).then(response => {
            const blob = new Blob([response.data], {type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'})
            const link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = `JuniorsRegistry_${DateTimeUtils.nowDateIso()}.xlsx`;
            link.click();
        });
    }

    createReport(juniorId: number, body: AddOrUpdateJuniorReportBody): Promise<number> {
        return httpService.post(`v1/udr/juniors/${juniorId}/reports`, body).then(response => {
            return response.data;
        });
    }

    updateReport(juniorId: number, reportId: number, body: AddOrUpdateJuniorReportBody): Promise<number> {
        return httpService.put(`v1/udr/juniors/${juniorId}/reports/${reportId}`, body).then(response => {
            return response.data;
        });
    }

    deleteReport(juniorId: number, reportId: number): Promise<number> {
        return httpService.delete(`v1/udr/juniors/${juniorId}/reports/${reportId}`).then(response => {
            return response.data;
        });
    }

}

const juniorService: JuniorRegistryService = new RestJuniorRegistryService(httpService);

export default juniorService;
