import httpService from "../http.service";
import {AxiosInstance} from "axios";


export interface ArticleMeta {
    name: string
}


export interface ArticleService {
    listShared(): Promise<ArticleMeta[]>;

    getSharedArticleUrl(articleName: string): string;

    getSharedArticleContent(articleName: string): Promise<string>;
}

class RestArticleService implements ArticleService {
    constructor(private httpService: AxiosInstance) {
    }

    listShared(): Promise<ArticleMeta[]> {
        return httpService.get(`v1/article/shared`).then(response => {
            return response.data;
        });
    }

    getSharedArticleContent(articleName: string): Promise<string> {
        return httpService.get(`v1/article/shared/${articleName}`).then(response => {
            return response.data;
        });
    }

    getSharedArticleUrl(articleName: string): string {
        return `${httpService.defaults.baseURL}v1/article/shared/${articleName}`;
    }
}

const articleService: ArticleService = new RestArticleService(httpService);

export default articleService;

