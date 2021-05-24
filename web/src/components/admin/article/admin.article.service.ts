import httpService from "../../http.service";
import {AxiosInstance, AxiosResponse} from "axios";


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

interface UploadArticleAttachmentResponse {
    relativeAttachmentUrl: string;
}


export interface AdminArticleService {
    getAll(): Promise<ArticleFull[]>;

    create(body: CreateOrUpdateArticleBody): Promise<number>;

    update(articleId: number, body: CreateOrUpdateArticleBody): Promise<number>;

    /**
     *
     * @param articleId
     * @param formData
     * return image full URL
     */
    uploadImage(articleId: number, formData: FormData): Promise<string>;

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

    uploadImage(articleId: number, formData: FormData): Promise<string> {
        return httpService.post(`v1/admin/article/${articleId}/attachment`, formData)
            .then((response: AxiosResponse<UploadArticleAttachmentResponse>) => {
                return this.getImagePath(response.data.relativeAttachmentUrl);
            });
    }

    private getImagePath(imageRelativePath: string): string {
        return httpService.defaults.baseURL + imageRelativePath;
    }

}

const articleAdminService: AdminArticleService = new RestAdminArticleService(httpService);

export default articleAdminService;

