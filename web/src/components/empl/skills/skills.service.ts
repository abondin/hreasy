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
     * From 1 to 5
     */
    averageRating?: number

    ratingsCount?: number
}

export interface AddSkillBody {
    groupId: number,
    name: string,
    rating: SkillRatingBody | null
}

export interface SkillRatingBody {
    rating: number,
    notes: string
}

export interface UpdateRatingValue {
    rating: number
}

export interface SkillsService {
    my(): Promise<Skill[]>;

    addMySkill(body: AddSkillBody): Promise<number>;

    updateRating(id: number, rating: UpdateRatingValue): Promise<number>;
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

    updateRating(id: number, rating: UpdateRatingValue): Promise<number> {
        return httpService.put(`v1/employee/skills/${id}/rating`, rating).then(response => {
            return response.data;
        });
    }
}


const skillsService: SkillsService = new RestSkillsService(httpService);

export default skillsService;

