import {AxiosInstance} from "axios";
import httpService from "@/components/http.service";

export interface DictDepartment {
    id: number,
    name: string
}

export interface DictDepartmentUpdateBody {
    name: string
}

export interface DictLevel {
    id: number,
    name: string,
    weight: number | undefined
}

export interface DictLevelUpdateBody {
    name: string,
    weight: number | undefined
}

export interface DictPosition {
    id: number,
    name: string,
    category: string | undefined
}

export interface DictPositionUpdateBody {
    name: string,
    category: string | undefined
}

export interface DictOfficeLocation {
    id: number,
    name: string,
    description: string | undefined,
    office: string | undefined
}

export interface DictOfficeLocationUpdateBody {
    name: string,
    description: string | undefined,
    office: string | undefined
}

export interface DictAdminService {
    loadDepartments(): Promise<Array<DictDepartment>>;

    createDepartment(body: DictDepartmentUpdateBody): Promise<DictDepartment>;

    updateDepartment(id: number, body: DictDepartmentUpdateBody): Promise<DictDepartment>;

    deleteDepartment(id: number): Promise<Number>;

    loadLevels(): Promise<Array<DictLevel>>;

    createLevel(body: DictLevelUpdateBody): Promise<DictLevel>;

    updateLevel(id: number, body: DictLevelUpdateBody): Promise<DictLevel>;

    deleteLevel(id: number): Promise<Number>;

    loadPositions(): Promise<Array<DictPosition>>;

    createPosition(body: DictPositionUpdateBody): Promise<DictPosition>;

    updatePosition(id: number, body: DictPositionUpdateBody): Promise<Position>;

    deletePosition(id: number): Promise<Number>;

    loadOfficeLocations(): Promise<Array<DictOfficeLocation>>;

    createOfficeLocation(body: DictOfficeLocationUpdateBody): Promise<DictOfficeLocation>;

    updateOfficeLocation(id: number, body: DictOfficeLocationUpdateBody): Promise<DictOfficeLocation>;

    deleteOfficeLocation(id: number): Promise<Number>;
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

    deleteDepartment(id: number): Promise<Number> {
        return httpService.delete(`v1/admin/dict/departments/${id}`);
    }

    deleteLevel(id: number): Promise<Number> {
        return httpService.delete(`v1/admin/dict/levels/${id}`);
    }

    deleteOfficeLocation(id: number): Promise<Number> {
        return httpService.delete(`v1/admin/dict/office_locations/${id}`);
    }

    deletePosition(id: number): Promise<Number> {
        return httpService.delete(`v1/admin/dict/positions/${id}`);
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

    updatePosition(id: number, body: DictPositionUpdateBody): Promise<Position> {
        return httpService.put(`v1/admin/dict/positions/${id}`, body);
    }
}

const dictAdminService: DictAdminService = new RestDictAdminService(httpService);

export default dictAdminService;
