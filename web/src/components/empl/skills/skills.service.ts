import httpService from "../../http.service";
import {AxiosInstance} from "axios";
import {SimpleDict} from "@/store/modules/dict";


export interface Skill {
    id: number,
    /**
     * Programming language, framework, foreign languages, etc.
     */
    group: SimpleDict,
    /**
     * Java, Spring, French, etc
     */
    name: string,
    /**
     * From 1 to 10
     */
    rating?: number
}

export interface AddSkillBody {
    groupId: number,
    name: string,
    rating: SkillRatingBody|null
}

export interface SkillRatingBody {
    rating: number,
    notes: string
}


export interface SkillsService {
    my(): Promise<Skill[]>;

    addMySkill(body: AddSkillBody): Promise<number>;
}


class RestSkillsService implements SkillsService {
    constructor(private httpService: AxiosInstance) {
    }

    my(): Promise<Skill[]> {
        return httpService.get(`v1/employee/skills`).then(response => {
            return response.data;
        });
    }

    addMySkill(body: AddSkillBody): Promise<number> {
        return httpService.post(`v1/employee/skills`, body).then(response => {
            return response.data;
        });
    }
}


const skillsService: SkillsService = new RestSkillsService(httpService);

export default skillsService;

