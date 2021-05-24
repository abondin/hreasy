import httpService from "../../http.service";
import {AxiosInstance} from "axios";


export interface ArticleFull {
    id: number,
    name: string,
    articleGroup: string,
    description?: string,
    content?: string,
    createdAt: Date,
    updatedAt: Date,
    moderated: boolean,
    archived: boolean
}


export interface CreateOrUpdateArticleBody {
    articleGroup: string
    name: string,
    description?: string
    content: string,
    moderated: boolean,
    archived: boolean
}


export interface AdminArticleService {
    getAll(): Promise<ArticleFull[]>;

    create(body: CreateOrUpdateArticleBody): Promise<number>;

    update(articleId: number, body: CreateOrUpdateArticleBody): Promise<number>;
}

class RestAdminArticleService implements AdminArticleService {
    constructor(private httpService: AxiosInstance) {
    }

    getAll(): Promise<ArticleFull[]> {
        return httpService.get(`v1/admin/article`).then((response: { data: ArticleFull[]; }) => {
            return response.data;
        });
    }

    create(body: CreateOrUpdateArticleBody): Promise<number> {
        return httpService.post(`v1/admin/article`, body).then((response) => {
            return response.data;
        });
    }

    update(articleId: number, body: CreateOrUpdateArticleBody): Promise<number> {
        return httpService.put(`v1/admin/article/${articleId}`, body).then((response) => {
            return response.data;
        });
    }
}

const articleAdminService: AdminArticleService = new RestAdminArticleService(httpService);

export default articleAdminService;

