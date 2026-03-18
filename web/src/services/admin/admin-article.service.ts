import http from "@/lib/http";

export interface ArticleFull {
  id: number;
  name: string;
  articleGroup: string;
  description?: string;
  content?: string;
  createdAt: string;
  updatedAt: string;
  moderated: boolean;
  archived: boolean;
}

export interface CreateOrUpdateArticleBody {
  articleGroup: string;
  name: string;
  description?: string;
  content: string;
  moderated: boolean;
  archived: boolean;
}

export interface UploadArticleAttachmentResponse {
  relativeAttachmentUrl: string;
}

/** Admin CRUD contract for articles/news management. */
export async function listAdminArticles(): Promise<ArticleFull[]> {
  const response = await http.get<ArticleFull[]>("v1/admin/article");
  return response.data;
}

export async function createAdminArticle(body: CreateOrUpdateArticleBody): Promise<number> {
  const response = await http.post<number>("v1/admin/article", body);
  return response.data;
}

export async function updateAdminArticle(
  articleId: number,
  body: CreateOrUpdateArticleBody,
): Promise<number> {
  const response = await http.put<number>(`v1/admin/article/${articleId}`, body);
  return response.data;
}

export function getAdminArticleImageUploadUrl(articleId: number): string {
  return `${normalizeBaseUrl(http.defaults.baseURL)}v1/admin/article/${articleId}/attachment`;
}

/**
 * Maps the upload response to an absolute backend URL so the markdown image remains resolvable.
 */
export function parseAdminArticleImageUploadResponse(payload: unknown): string | null {
  if (!payload || typeof payload !== "object" || !("relativeAttachmentUrl" in payload)) {
    return null;
  }

  const relativeAttachmentUrl = (payload as UploadArticleAttachmentResponse).relativeAttachmentUrl;
  if (typeof relativeAttachmentUrl !== "string" || relativeAttachmentUrl.length === 0) {
    return null;
  }

  if (/^(https?:)?\/\//.test(relativeAttachmentUrl)) {
    return relativeAttachmentUrl;
  }

  return `${normalizeBaseUrl(http.defaults.baseURL)}${relativeAttachmentUrl.replace(/^\/+/, "")}`;
}

function normalizeBaseUrl(baseUrl: string | undefined): string {
  const value = baseUrl ?? "/api/";
  const normalized = value.endsWith("/") ? value : `${value}/`;
  return normalized.startsWith("http") ? normalized : `${window.location.origin}${normalized}`;
}
