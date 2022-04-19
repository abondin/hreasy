import {AxiosInstance} from "axios";
import httpService from "@/components/http.service";

export interface TechProfile {
    filename: string,
    accessToken: string
}

export interface TechProfileService {
    find(employeeId: number): Promise<TechProfile[]>;

    getTechProfileDownloadUrl(employeeId: number, filename: string, accessToken: string): string;

    getTechProfileUploadUrl(employeeId: number): string;

    delete(employeeId: number, filename: string): Promise<void>;
}

class RestTechProfileService implements TechProfileService {
    constructor(private httpService: AxiosInstance) {
    }

    find(employeeId: number): Promise<TechProfile[]> {
        return httpService.get(`v1/techprofile/${employeeId}`).then(response => {
            return response.data;
        });
    }

    my(): Promise<TechProfile[]> {
        return httpService.get(`v1/techprofile`).then(response => {
            return response.data;
        });
    }

    delete(employeeId: number, filename: string) : Promise<void>{
        return httpService.delete(`v1/techprofile/${employeeId}/file/${filename}`);
    }

    getTechProfileDownloadUrl(employeeId: number, filename: string, accessToken: string): string {
        return `${httpService.defaults.baseURL}v1/fs/techprofile/${employeeId}/${filename}/${accessToken}`;
    }

    getTechProfileUploadUrl(employeeId: number): string {
        return `${httpService.defaults.baseURL}v1/techprofile/${employeeId}/file`;
    }
}

const techprofileService: TechProfileService = new RestTechProfileService(httpService);

export default techprofileService;
