import {AxiosInstance} from "axios";
import httpService from "@/components/http.service";
import {SimpleDict} from "@/store/modules/dict";

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

export interface DictOrganization {
    id: number,
    name: string,
    description: string | undefined,
    archived: boolean
}

export interface DictLevelUpdateBody {
    name: string,
    weight: number | undefined,
    archived: boolean
}

export interface DictOrganizationUpdateBody {
    name: string,
    description: string | undefined,
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

export interface DictOffice {
    id: number,
    name: string,
    description: string | undefined,
    address: string | undefined,
    archived: boolean
}

export interface DictOfficeLocation {
    id: number,
    name: string,
    description: string | undefined,
    office: SimpleDict | undefined,
    archived: boolean
}

export interface DictOfficeUpdateBody {
    name: string,
    description: string | undefined,
    address: string | undefined,
    archived: boolean
}

export interface DictOfficeLocationUpdateBody {
    name: string,
    description: string | undefined,
    officeId: number | undefined,
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


    loadOffices(): Promise<Array<DictOffice>>;

    createOffice(body: DictOfficeUpdateBody): Promise<number>;

    updateOffice(id: number, body: DictOfficeUpdateBody): Promise<number>;

    loadOfficeLocations(): Promise<Array<DictOfficeLocation>>;

    createOfficeLocation(body: DictOfficeLocationUpdateBody): Promise<number>;

    updateOfficeLocation(id: number, body: DictOfficeLocationUpdateBody): Promise<number>;

    loadOrganizations(): Promise<Array<DictOrganization>>;

    createOrganization(body: DictOrganizationUpdateBody): Promise<DictOrganization>;

    updateOrganization(id: number, body: DictOrganizationUpdateBody): Promise<DictOrganization>;
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

    createOffice(body: DictOfficeUpdateBody): Promise<number> {
        return httpService.post("v1/admin/dict/offices", body);
    }

    createOfficeLocation(body: DictOfficeLocationUpdateBody): Promise<number> {
        return httpService.post("v1/admin/dict/office_locations", body);
    }

    createPosition(body: DictPositionUpdateBody): Promise<DictPosition> {
        return httpService.post("v1/admin/dict/positions", body);
    }

    createOrganization(body: DictOrganizationUpdateBody): Promise<DictOrganization> {
        return httpService.post("v1/admin/dict/organizations", body);
    }

    loadDepartments(): Promise<Array<DictDepartment>> {
        return httpService.get("v1/admin/dict/departments").then(response => response.data);
    }

    loadLevels(): Promise<Array<DictLevel>> {
        return httpService.get("v1/admin/dict/levels").then(response => response.data);
    }

    loadOffices(): Promise<Array<DictOffice>> {
        return httpService.get("v1/admin/dict/offices").then(response => response.data);
    }

    loadOfficeLocations(): Promise<Array<DictOfficeLocation>> {
        return httpService.get("v1/admin/dict/office_locations").then(response => response.data);
    }

    loadPositions(): Promise<Array<DictPosition>> {
        return httpService.get("v1/admin/dict/positions").then(response => response.data);
    }

    loadOrganizations(): Promise<Array<DictOrganization>> {
        return httpService.get("v1/admin/dict/organizations").then(response => response.data);
    }

    updateDepartment(id: number, body: DictDepartmentUpdateBody): Promise<DictDepartment> {
        return httpService.put(`v1/admin/dict/departments/${id}`, body);
    }

    updateLevel(id: number, body: DictLevelUpdateBody): Promise<DictLevel> {
        return httpService.put(`v1/admin/dict/levels/${id}`, body);
    }

    updateOffice(id: number, body: DictOfficeUpdateBody): Promise<number> {
        return httpService.put(`v1/admin/dict/offices/${id}`, body);
    }

    updateOfficeLocation(id: number, body: DictOfficeLocationUpdateBody): Promise<number> {
        return httpService.put(`v1/admin/dict/office_locations/${id}`, body);
    }

    updatePosition(id: number, body: DictPositionUpdateBody): Promise<DictPosition> {
        return httpService.put(`v1/admin/dict/positions/${id}`, body);
    }

    updateOrganization(id: number, body: DictOrganizationUpdateBody): Promise<DictOrganization> {
        return httpService.put(`v1/admin/dict/organizations/${id}`, body);
    }
}

const dictAdminService: DictAdminService = new RestDictAdminService(httpService);

export default dictAdminService;
