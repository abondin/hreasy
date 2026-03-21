import http from "@/lib/http";

export interface Article {
  name: string;
  articleGroup: string;
  description?: string;
  content: string;
  updatedAt: string;
}

export const ARTICLE_SHARED_GROUP = "shared";
export const ALL_ARTICLES_GROUPS = [ARTICLE_SHARED_GROUP] as const;

/** Public/shared article feed used on profile screens. */
export async function listSharedArticles(): Promise<Article[]> {
  const response = await http.get<Article[]>("v1/article");
  return response.data.filter((item) => item.articleGroup === ARTICLE_SHARED_GROUP);
}
