"use client";

import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import type { ApiResponse } from "@/types/api";
import type { Article } from "@/types/article";

/**
 * Fetch a public article by its ID.
 * Used to display the linked article on event / festival detail pages.
 */
export function useLinkedArticle(articleId: string | null | undefined) {
  return useQuery({
    queryKey: ["linked-article", articleId],
    queryFn: async () => {
      const response = await api.get<ApiResponse<Article>>(`/articles/${articleId}`);
      return response.data;
    },
    enabled: !!articleId,
  });
}
