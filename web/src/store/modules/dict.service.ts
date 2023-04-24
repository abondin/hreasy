import httpService from "../../components/http.service";
import {AxiosInstance} from "axios";
import {CurrentProjectRole, ManagerOfObject, ProjectDictDto, SimpleDict} from "@/store/modules/dict";

export interface SharedSkillName {
    groupId: number,
    name: string
}

export interface ProjectInfo {
    id: number,
    name: string;
    startDate?: string,
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

    loadAllPositions(): Promise<Array<SimpleDict>>;

    loadAllLevels(): Promise<Array<SimpleDict>>;

    loadAllOfficeLocations(): Promise<Array<SimpleDict>>;

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

    loadAllPositions(): Promise<Array<SimpleDict>> {
        return httpService.get("v1/dict/positions").then(response => response.data);
    }

    loadAllLevels(): Promise<Array<SimpleDict>> {
        return httpService.get("v1/dict/levels").then(response => response.data);
    }

    loadAllOfficeLocations(): Promise<Array<SimpleDict>> {
        return httpService.get("v1/dict/office_locations").then(response => response.data);
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






