import httpService from "../../components/http.service";
import {AxiosInstance} from "axios";
import {SimpleDict} from "@/store/modules/dict";

export interface SharedSkillName {
    groupId: number,
    name: string
}

export interface DictService {
    loadAllProjects(): Promise<Array<SimpleDict>>;
    loadAllBusinessAccounts(): Promise<Array<SimpleDict>>;
    loadAllDepartments(): Promise<Array<SimpleDict>>;
    loadAllPositions(): Promise<Array<SimpleDict>>;
    loadAllLevels(): Promise<Array<SimpleDict>>;
    loadAllOfficeLocations(): Promise<Array<SimpleDict>>;

    loadAllSkillGroups():  Promise<Array<SimpleDict>>;

    loadSharedSkills():  Promise<Array<SharedSkillName>>;
}

class RestDictService implements DictService {
    constructor(private httpService: AxiosInstance) {
    }

    public loadAllProjects(): Promise<Array<SimpleDict>> {
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

    public loadAllBusinessAccounts(): Promise<Array<SimpleDict>> {
        return httpService.get("v1/business_account").then(response => response.data);
    }
}



const dictService: DictService = new RestDictService(httpService);

export default dictService;






