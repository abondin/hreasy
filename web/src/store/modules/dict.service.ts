import httpService from "../../components/http.service";
import {AxiosInstance} from "axios";
import {
    CurrentProjectRole,
    ManagerOfObject,
    OfficeLocationDict,
    ProjectDictDto,
    SimpleDict
} from "@/store/modules/dict";
import DOMPurify from 'dompurify';

export interface SharedSkillName {
    groupId: number,
    name: string
}

export interface ProjectInfo {
    id: number,
    name: string;
    startDate?: string,
    planStartDate?: string,
    planEndDate?: string,
    endDate?: string,
    customer?: string,
    department: SimpleDict,
    businessAccount: SimpleDict
    info?: string,
    managers: ManagerOfObject[]
}

export interface DictService {
    loadAllProjects(): Promise<Array<ProjectDictDto>>;

    loadAllCurrentProjectRoles(): Promise<Array<CurrentProjectRole>>;

    loadAllBusinessAccounts(): Promise<Array<SimpleDict>>;

    loadAllDepartments(): Promise<Array<SimpleDict>>;

    loadAllOrganizations(): Promise<Array<SimpleDict>>;

    loadAllPositions(): Promise<Array<SimpleDict>>;

    loadAllLevels(): Promise<Array<SimpleDict>>;

    loadAllOfficeLocations(): Promise<Array<OfficeLocationDict>>;

    loadAllOffices(): Promise<Array<SimpleDict>>;

    getOfficeLocationMap(officeLocationId: number): Promise<string>;

    loadAllSkillGroups(): Promise<Array<SimpleDict>>;

    loadSharedSkills(): Promise<Array<SharedSkillName>>;

    getProjectCard(projectId: number): Promise<ProjectInfo>;

    notWorkingDays(year: number): Promise<Array<string>>;

    daysNotIncludedInVacations(years: number[]): Promise<Array<string>>;
}

class RestDictService implements DictService {
    constructor(private httpService: AxiosInstance) {
    }

    public loadAllProjects(): Promise<Array<ProjectDictDto>> {
        return httpService.get("v1/dict/projects").then(response => response.data);
    }


    public loadAllDepartments(): Promise<Array<SimpleDict>> {
        return httpService.get("v1/dict/departments").then(response => response.data);
    }

    loadAllOrganizations(): Promise<Array<SimpleDict>> {
        return httpService.get("v1/dict/organizations").then(response => response.data);
    }

    loadAllPositions(): Promise<Array<SimpleDict>> {
        return httpService.get("v1/dict/positions").then(response => response.data);
    }

    loadAllLevels(): Promise<Array<SimpleDict>> {
        return httpService.get("v1/dict/levels").then(response => response.data);
    }

    loadAllOffices(): Promise<Array<SimpleDict>> {
        return httpService.get("v1/dict/offices").then(response => response.data);
    }

    loadAllOfficeLocations(): Promise<Array<SimpleDict>> {
        return httpService.get("v1/dict/office_locations").then(response => response.data);
    }

    getOfficeLocationMap(officeLocationId: number): Promise<string> {
        return httpService.get(`v1/dict/office_locations/${officeLocationId}/map`, {responseType: 'text'}).then(response => {
            const svgText = response.data;
            return DOMPurify.sanitize(svgText, { USE_PROFILES: { svg: true } });
        });
    }


    public loadAllSkillGroups(): Promise<Array<SimpleDict>> {
        return httpService.get("/v1/employee/skills/groups").then(response => response.data);
    }

    public loadSharedSkills(): Promise<Array<SharedSkillName>> {
        return httpService.get("/v1/employee/skills/shared/names").then(response => response.data);
    }

    public loadAllCurrentProjectRoles(): Promise<Array<CurrentProjectRole>> {
        return httpService.get("v1/employee/current_project_roles").then(response => response.data);
    }

    public loadAllBusinessAccounts(): Promise<Array<SimpleDict>> {
        return httpService.get("v1/business_account").then(response => response.data);
    }

    getProjectCard(projectId: number): Promise<ProjectInfo> {
        return httpService.get(`v1/projects/${projectId}`).then(response => response.data);
    }

    daysNotIncludedInVacations(years: number[]): Promise<Array<string>> {
        return httpService.get(`v1/dict/calendar/days_not_included_in_vacations/${years.join(',')}`).then(response => response.data);
    }

    notWorkingDays(year: number): Promise<Array<string>> {
        return httpService.get(`v1/dict/calendar/not_working_days/${year}`).then(response => response.data);
    }


}


const dictService: DictService = new RestDictService(httpService);

export default dictService;






