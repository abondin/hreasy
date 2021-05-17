import httpService from "../../http.service";
import {AxiosInstance} from "axios";


export interface ArticleFull {
    name: string,
    articleGroup: string,
    description?: string,
    content?: string,
    createdAt: Date,
    updatedAt: Date,
    moderated: boolean,
    archived: boolean
}

export interface CreateEmptyArticleBody {
    articleGroup: string
    name: string,
    description?: string
}

export interface UpdateArticleBody {
    articleGroup: string
    name: string,
    description?: string
    content: string,
    moderated: boolean,
    archived: boolean
}


export interface AdminArticleService {
    getAll(): Promise<ArticleFull[]>;

    create(body: CreateEmptyArticleBody): Promise<number>;

    update(articleId: number, body: UpdateArticleBody): Promise<number>;
}

class RestAdminArticleService implements AdminArticleService {
    constructor(private httpService: AxiosInstance) {
    }

    getAll(): Promise<ArticleFull[]> {
        return httpService.get(`v1/admin/article`).then((response: { data: ArticleFull[]; }) => {
            return response.data;
        });
    }

    create(body: CreateEmptyArticleBody): Promise<number> {
        return httpService.post(`v1/admin/article`).then((response) => {
            return response.data;
        });
    }

    update(articleId: number, body: UpdateArticleBody): Promise<number> {
        return httpService.put(`v1/admin/article/${articleId}`).then((response) => {
            return response.data;
        });
    }
}

const articleAdminService: AdminArticleService = new RestAdminArticleService(httpService);

export default articleAdminService;

