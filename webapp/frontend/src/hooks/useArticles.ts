"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import * as articleService from "@/services/articleService";
import type {
  CreateArticleRequest,
  UpdateArticleRequest,
  ArticleListParams,
  CreateCategoryRequest,
  UpdateCategoryRequest,
  CategoryListParams,
  CreateTagRequest,
  UpdateTagRequest,
  TagListParams,
} from "@/types/article";
import { toast } from "sonner";

// ============================================
// Article Hooks
// ============================================

const ARTICLES_KEY = "articles";
const CATEGORIES_KEY = "categories";
const TAGS_KEY = "tags";

/** Hook for fetching paginated articles */
export function useArticles(params?: ArticleListParams) {
  return useQuery({
    queryKey: [ARTICLES_KEY, params],
    queryFn: () => articleService.getArticles(params),
  });
}

/** Hook for fetching a single article */
export function useArticle(id: string) {
  return useQuery({
    queryKey: [ARTICLES_KEY, id],
    queryFn: () => articleService.getArticle(id),
    enabled: !!id,
  });
}

/** Hook for creating an article */
export function useCreateArticle() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateArticleRequest) => articleService.createArticle(data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Tạo bài viết thành công");
        queryClient.invalidateQueries({ queryKey: [ARTICLES_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể tạo bài viết");
    },
  });
}

/** Hook for updating an article */
export function useUpdateArticle(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UpdateArticleRequest) => articleService.updateArticle(id, data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Cập nhật bài viết thành công");
        queryClient.invalidateQueries({ queryKey: [ARTICLES_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể cập nhật bài viết");
    },
  });
}

/** Hook for deleting an article */
export function useDeleteArticle() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => articleService.deleteArticle(id),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Xóa bài viết thành công");
        queryClient.invalidateQueries({ queryKey: [ARTICLES_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể xóa bài viết");
    },
  });
}

// ============================================
// Category Hooks
// ============================================

/** Hook for fetching paginated categories */
export function useCategories(params?: CategoryListParams) {
  return useQuery({
    queryKey: [CATEGORIES_KEY, params],
    queryFn: () => articleService.getCategories(params),
  });
}

/** Hook for fetching all categories (for dropdowns) */
export function useAllCategories() {
  return useQuery({
    queryKey: [CATEGORIES_KEY, "all"],
    queryFn: () => articleService.getAllCategories(),
  });
}

/** Hook for creating a category */
export function useCreateCategory() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateCategoryRequest) => articleService.createCategory(data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Tạo danh mục thành công");
        queryClient.invalidateQueries({ queryKey: [CATEGORIES_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể tạo danh mục");
    },
  });
}

/** Hook for updating a category */
export function useUpdateCategory(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UpdateCategoryRequest) => articleService.updateCategory(id, data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Cập nhật danh mục thành công");
        queryClient.invalidateQueries({ queryKey: [CATEGORIES_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể cập nhật danh mục");
    },
  });
}

/** Hook for deleting a category */
export function useDeleteCategory() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => articleService.deleteCategory(id),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Xóa danh mục thành công");
        queryClient.invalidateQueries({ queryKey: [CATEGORIES_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể xóa danh mục");
    },
  });
}

// ============================================
// Tag Hooks
// ============================================

/** Hook for fetching paginated tags */
export function useTags(params?: TagListParams) {
  return useQuery({
    queryKey: [TAGS_KEY, params],
    queryFn: () => articleService.getTags(params),
  });
}

/** Hook for fetching all tags (for dropdowns) */
export function useAllTags() {
  return useQuery({
    queryKey: [TAGS_KEY, "all"],
    queryFn: () => articleService.getAllTags(),
  });
}

/** Hook for creating a tag */
export function useCreateTag() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateTagRequest) => articleService.createTag(data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Tạo tag thành công");
        queryClient.invalidateQueries({ queryKey: [TAGS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể tạo tag");
    },
  });
}

/** Hook for updating a tag */
export function useUpdateTag(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UpdateTagRequest) => articleService.updateTag(id, data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Cập nhật tag thành công");
        queryClient.invalidateQueries({ queryKey: [TAGS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể cập nhật tag");
    },
  });
}

/** Hook for deleting a tag */
export function useDeleteTag() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => articleService.deleteTag(id),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Xóa tag thành công");
        queryClient.invalidateQueries({ queryKey: [TAGS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể xóa tag");
    },
  });
}
