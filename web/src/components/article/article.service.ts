import httpService from "../http.service";
import {AxiosInstance} from "axios";


export interface Article {
    name: string,
    articleGroup: string,
    description: string,
    content: string,
    updatedAt: Date
}

const SHARED_GROUP='shared';
export const ALL_ARTICLES_GROUPS = [SHARED_GROUP];

export interface ArticleService {
    getShared(): Promise<Article[]>;
}

class RestArticleService implements ArticleService {
    constructor(private httpService: AxiosInstance) {
    }

    getShared(): Promise<Article[]> {
        return httpService.get(`v1/article`).then(response => {
            return (response.data as Article[]).filter(a=>a.articleGroup==SHARED_GROUP);
        });
    }
}

const articleService: ArticleService = new RestArticleService(httpService);

export default articleService;

