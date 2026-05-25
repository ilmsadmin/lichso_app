// ============================================
// Article Types
// ============================================

/**
 * Article status enum
 */
export type ArticleStatus = "draft" | "published" | "archived";

/**
 * Article category data
 */
export interface ArticleCategory {
  id: string;
  name: string;
  slug: string;
  description: string;
  parent_id: string | null;
  icon: string;
  sort_order: number;
  is_active: boolean;
  created_at: string;
  updated_at: string;
  children?: ArticleCategory[];
  parent?: ArticleCategory;
  article_count?: number;
}

/**
 * Article tag data
 */
export interface ArticleTag {
  id: string;
  name: string;
  slug: string;
  description?: string;
  created_at: string;
  updated_at: string;
  article_count?: number;
}

/**
 * Article data returned from the API
 */
export interface Article {
  id: string;
  title: string;
  slug: string;
  excerpt: string;
  content: string;
  category_id: string;
  author_id: string;
  status: ArticleStatus;
  featured_image: string;
  images: string[];
  tags_cache: string[];
  meta_title: string;
  meta_description: string;
  view_count: number;
  reading_time: number;
  is_featured: boolean;
  published_at: string | null;
  created_at: string;
  updated_at: string;
  category?: ArticleCategory;
  author?: {
    id: string;
    full_name: string;
    avatar: string;
  };
  tags?: ArticleTag[];
}

/**
 * Article summary (for list views)
 */
export interface ArticleSummary {
  id: string;
  title: string;
  slug: string;
  excerpt: string;
  category_id: string;
  author_id: string;
  status: ArticleStatus;
  featured_image: string;
  view_count: number;
  reading_time: number;
  is_featured: boolean;
  published_at: string | null;
  created_at: string;
  category?: ArticleCategory;
  author?: {
    id: string;
    full_name: string;
    avatar: string;
  };
}

/**
 * Create article request payload
 */
export interface CreateArticleRequest {
  title: string;
  excerpt?: string;
  content: string;
  category_id?: string;
  status?: ArticleStatus;
  featured_image?: string;
  images?: string[];
  tag_ids?: string[];
  meta_title?: string;
  meta_description?: string;
  is_featured?: boolean;
  published_at?: string;
}

/**
 * Update article request payload
 */
export interface UpdateArticleRequest {
  title?: string;
  excerpt?: string;
  content?: string;
  category_id?: string;
  status?: ArticleStatus;
  featured_image?: string;
  images?: string[];
  tag_ids?: string[];
  meta_title?: string;
  meta_description?: string;
  is_featured?: boolean;
}

/**
 * Create category request payload
 */
export interface CreateCategoryRequest {
  name: string;
  description?: string;
  parent_id?: string;
  icon?: string;
  sort_order?: number;
  is_active?: boolean;
}

/**
 * Update category request payload
 */
export interface UpdateCategoryRequest {
  name?: string;
  description?: string;
  parent_id?: string;
  icon?: string;
  sort_order?: number;
  is_active?: boolean;
}

/**
 * Create tag request payload
 */
export interface CreateTagRequest {
  name: string;
  slug?: string;
  description?: string;
}

/**
 * Update tag request payload
 */
export interface UpdateTagRequest {
  name?: string;
  slug?: string;
  description?: string;
}

/**
 * Article list query parameters
 */
export interface ArticleListParams {
  page?: number;
  limit?: number;
  search?: string;
  sort_by?: string;
  sort_order?: "asc" | "desc";
  status?: string;
  category_id?: string;
  tag_id?: string;
  is_featured?: boolean;
}

/**
 * Category list query parameters
 */
export interface CategoryListParams {
  page?: number;
  limit?: number;
  search?: string;
  is_active?: boolean;
}

/**
 * Tag list query parameters
 */
export interface TagListParams {
  page?: number;
  limit?: number;
  search?: string;
}
