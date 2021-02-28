import httpService from "../../components/http.service";
import {AxiosInstance} from "axios";
import {SimpleDict} from "@/store/modules/dict";


export interface DictService {
    loadAllProjects(): Promise<Array<SimpleDict>>;
    loadAllDepartments(): Promise<Array<SimpleDict>>;
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
}



const dictService: DictService = new RestDictService(httpService);

export default dictService;






