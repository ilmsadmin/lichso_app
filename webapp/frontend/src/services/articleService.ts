import api from "@/lib/api";
import type { ApiResponse, PaginatedResponse } from "@/types/api";
import type {
  Article,
  ArticleSummary,
  CreateArticleRequest,
  UpdateArticleRequest,
  ArticleListParams,
  ArticleCategory,
  CreateCategoryRequest,
  UpdateCategoryRequest,
  CategoryListParams,
  ArticleTag,
  CreateTagRequest,
  UpdateTagRequest,
  TagListParams,
} from "@/types/article";

// ============================================
// Article API Service
// ============================================

/**
 * Get paginated articles (admin)
 */
export async function getArticles(
  params?: ArticleListParams
): Promise<PaginatedResponse<ArticleSummary>> {
  const response = await api.get<PaginatedResponse<ArticleSummary>>("/admin/articles", { params });
  return response.data;
}

/**
 * Get an article by ID
 */
export async function getArticle(id: string): Promise<ApiResponse<Article>> {
  const response = await api.get<ApiResponse<Article>>(`/admin/articles/${id}`);
  return response.data;
}

/**
 * Create a new article
 */
export async function createArticle(data: CreateArticleRequest): Promise<ApiResponse<Article>> {
  const response = await api.post<ApiResponse<Article>>("/admin/articles", data);
  return response.data;
}

/**
 * Update an article
 */
export async function updateArticle(
  id: string,
  data: UpdateArticleRequest
): Promise<ApiResponse<Article>> {
  const response = await api.put<ApiResponse<Article>>(`/admin/articles/${id}`, data);
  return response.data;
}

/**
 * Delete an article
 */
export async function deleteArticle(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/admin/articles/${id}`);
  return response.data;
}

// ============================================
// Category API Service
// ============================================

/**
 * Get paginated categories
 */
export async function getCategories(
  params?: CategoryListParams
): Promise<PaginatedResponse<ArticleCategory>> {
  const response = await api.get<PaginatedResponse<ArticleCategory>>("/admin/categories", {
    params,
  });
  return response.data;
}

/**
 * Get all categories (no pagination, for dropdowns)
 */
export async function getAllCategories(): Promise<ApiResponse<ArticleCategory[]>> {
  const response = await api.get<ApiResponse<ArticleCategory[]>>("/admin/categories", {
    params: { limit: 100 },
  });
  return response.data;
}

/**
 * Get a category by ID
 */
export async function getCategory(id: string): Promise<ApiResponse<ArticleCategory>> {
  const response = await api.get<ApiResponse<ArticleCategory>>(`/admin/categories/${id}`);
  return response.data;
}

/**
 * Create a new category
 */
export async function createCategory(
  data: CreateCategoryRequest
): Promise<ApiResponse<ArticleCategory>> {
  const response = await api.post<ApiResponse<ArticleCategory>>("/admin/categories", data);
  return response.data;
}

/**
 * Update a category
 */
export async function updateCategory(
  id: string,
  data: UpdateCategoryRequest
): Promise<ApiResponse<ArticleCategory>> {
  const response = await api.put<ApiResponse<ArticleCategory>>(`/admin/categories/${id}`, data);
  return response.data;
}

/**
 * Delete a category
 */
export async function deleteCategory(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/admin/categories/${id}`);
  return response.data;
}

// ============================================
// Tag API Service
// ============================================

/**
 * Get paginated tags
 */
export async function getTags(params?: TagListParams): Promise<PaginatedResponse<ArticleTag>> {
  const response = await api.get<PaginatedResponse<ArticleTag>>("/admin/tags", { params });
  return response.data;
}

/**
 * Get all tags (no pagination, for dropdowns)
 */
export async function getAllTags(): Promise<ApiResponse<ArticleTag[]>> {
  const response = await api.get<ApiResponse<ArticleTag[]>>("/admin/tags", {
    params: { limit: 200 },
  });
  return response.data;
}

/**
 * Get a tag by ID
 */
export async function getTag(id: string): Promise<ApiResponse<ArticleTag>> {
  const response = await api.get<ApiResponse<ArticleTag>>(`/admin/tags/${id}`);
  return response.data;
}

/**
 * Create a new tag
 */
export async function createTag(data: CreateTagRequest): Promise<ApiResponse<ArticleTag>> {
  const response = await api.post<ApiResponse<ArticleTag>>("/admin/tags", data);
  return response.data;
}

/**
 * Update a tag
 */
export async function updateTag(
  id: string,
  data: UpdateTagRequest
): Promise<ApiResponse<ArticleTag>> {
  const response = await api.put<ApiResponse<ArticleTag>>(`/admin/tags/${id}`, data);
  return response.data;
}

/**
 * Delete a tag
 */
export async function deleteTag(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/admin/tags/${id}`);
  return response.data;
}
