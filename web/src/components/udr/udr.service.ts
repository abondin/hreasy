import {AxiosInstance} from "axios";
import httpService from "@/components/http.service";
import {SimpleDict} from "@/store/modules/dict";
import {CurrentProjectDict} from "@/components/empl/employee.service";
import {WithId} from "@/components/shared/table/TableComponentDataContainer";
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

export interface AddToJuniorRegistryBody {
    juniorId: number | null,
    mentorId: number | null;
    role: string | null;
    budgetingAccount: number | null;
}

export interface UpdateJuniorRegistryBody {
    mentorId: number | null;
    role: string;
    budgetingAccount: number | null;
}

export interface JuniorGraduation {
    graduatedAt: Date;
    graduatedBy: SimpleDict;
    comment: string;
}


export interface JuniorReport {
    id: number;
    name: string;
    createdAt: Date;
    createdBy: SimpleDict;
    comment: string;
}

export interface JuniorDto extends WithId {
    junior: SimpleDict;
    mentor?: SimpleDict;
    role: string;
    currentProject?: CurrentProjectDict;
    budgetingAccount?: SimpleDict;
    createdAt: Date;
    createdBy: SimpleDict;
    graduation?: JuniorGraduation;
    reports: JuniorReport[];
}


export interface JuniorRegistryService {
    addToRegistry(body: AddToJuniorRegistryBody): Promise<number>;

    updateInRegistry(juniorEmployeeId: number, body: UpdateJuniorRegistryBody): Promise<number>;

    deleteFromRegistry(ids: number[]): Promise<Array<number>>;

    juniors(): Promise<Array<JuniorDto>>;

    export(): Promise<any>;
}

class RestJuniorRegistryService implements JuniorRegistryService {
    constructor(private httpService: AxiosInstance) {
    }

    juniors(): Promise<Array<JuniorDto>> {
        return httpService.get(`v1/udr/juniors`).then(response => {
            return response.data;
        });
    }

    addToRegistry(body: AddToJuniorRegistryBody): Promise<number> {
        return httpService.post(`v1/udr/juniors`, body).then(response => {
            return response.data;
        });
    }

    updateInRegistry(juniorEmployeeId: number, body: UpdateJuniorRegistryBody): Promise<number> {
        return httpService.put(`v1/udr/juniors/${juniorEmployeeId}`, body).then(response => {
            return response.data;
        });
    }


    deleteFromRegistry(ids: number[]): Promise<Array<number>> {
        return Promise.all(
            ids.map(id => httpService.delete(`v1/udr/juniors/${id}`).then(response => {
                return response.data;
            }))
        );
    }

    export(): Promise<any> {
        return httpService.get(`v1/udr/juniors/export`, {
            responseType: 'arraybuffer',
        }).then(response => {
            const blob = new Blob([response.data], {type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'})
            const link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = `JuniorsRegistry_${DateTimeUtils.nowDateIso()}.xlsx`;
            link.click();
        });
    }
}

const juniorService: JuniorRegistryService = new RestJuniorRegistryService(httpService);

export default juniorService;
