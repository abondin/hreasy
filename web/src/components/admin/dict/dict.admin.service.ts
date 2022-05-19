import {AxiosInstance} from "axios";
import httpService from "@/components/http.service";

export interface DictDepartment {
    id: number,
    name: string,
    archived: boolean
}

export interface DictDepartmentUpdateBody {
    name: string,
    archived: boolean
}

export interface DictLevel {
    id: number,
    name: string,
    weight: number | undefined,
    archived: boolean
}

export interface DictLevelUpdateBody {
    name: string,
    weight: number | undefined,
    archived: boolean
}

export interface DictPosition {
    id: number,
    name: string,
    category: string | undefined,
    archived: boolean
}

export interface DictPositionUpdateBody {
    name: string,
    category: string | undefined,
    archived: boolean
}

export interface DictOfficeLocation {
    id: number,
    name: string,
    description: string | undefined,
    office: string | undefined,
    archived: boolean
}

export interface DictOfficeLocationUpdateBody {
    name: string,
    description: string | undefined,
    office: string | undefined,
    archived: boolean
}

export interface DictAdminService {
    loadDepartments(): Promise<Array<DictDepartment>>;

    createDepartment(body: DictDepartmentUpdateBody): Promise<DictDepartment>;

    updateDepartment(id: number, body: DictDepartmentUpdateBody): Promise<DictDepartment>;

    loadLevels(): Promise<Array<DictLevel>>;

    createLevel(body: DictLevelUpdateBody): Promise<DictLevel>;

    updateLevel(id: number, body: DictLevelUpdateBody): Promise<DictLevel>;

    loadPositions(): Promise<Array<DictPosition>>;

    createPosition(body: DictPositionUpdateBody): Promise<DictPosition>;

    updatePosition(id: number, body: DictPositionUpdateBody): Promise<DictPosition>;


    loadOfficeLocations(): Promise<Array<DictOfficeLocation>>;

    createOfficeLocation(body: DictOfficeLocationUpdateBody): Promise<DictOfficeLocation>;

    updateOfficeLocation(id: number, body: DictOfficeLocationUpdateBody): Promise<DictOfficeLocation>;
}

class RestDictAdminService implements DictAdminService {
    constructor(private httpService: AxiosInstance) {
    }

    createDepartment(body: DictDepartmentUpdateBody): Promise<DictDepartment> {
        return httpService.post("v1/admin/dict/departments", body);
    }

    createLevel(body: DictLevelUpdateBody): Promise<DictLevel> {
        return httpService.post("v1/admin/dict/levels", body);
    }

    createOfficeLocation(body: DictOfficeLocationUpdateBody): Promise<DictOfficeLocation> {
        return httpService.post("v1/admin/dict/office_locations", body);
    }

    createPosition(body: DictPositionUpdateBody): Promise<DictPosition> {
        return httpService.post("v1/admin/dict/positions", body);
    }

    loadDepartments(): Promise<Array<DictDepartment>> {
        return httpService.get("v1/admin/dict/departments").then(response => response.data);
    }

    loadLevels(): Promise<Array<DictLevel>> {
        return httpService.get("v1/admin/dict/levels").then(response => response.data);
    }

    loadOfficeLocations(): Promise<Array<DictOfficeLocation>> {
        return httpService.get("v1/admin/dict/office_locations").then(response => response.data);
    }

    loadPositions(): Promise<Array<DictPosition>> {
        return httpService.get("v1/admin/dict/positions").then(response => response.data);
    }

    updateDepartment(id: number, body: DictDepartmentUpdateBody): Promise<DictDepartment> {
        return httpService.put(`v1/admin/dict/departments/${id}`, body);
    }

    updateLevel(id: number, body: DictLevelUpdateBody): Promise<DictLevel> {
        return httpService.put(`v1/admin/dict/levels/${id}`, body);
    }

    updateOfficeLocation(id: number, body: DictOfficeLocationUpdateBody): Promise<DictOfficeLocation> {
        return httpService.put(`v1/admin/dict/office_locations/${id}`, body);
    }

    updatePosition(id: number, body: DictPositionUpdateBody): Promise<DictPosition> {
        return httpService.put(`v1/admin/dict/positions/${id}`, body);
    }
}

const dictAdminService: DictAdminService = new RestDictAdminService(httpService);

export default dictAdminService;
