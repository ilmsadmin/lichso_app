"use client";

import { useQuery } from "@tanstack/react-query";
import { getArticles, getCategories, getTags } from "@/services/articleService";
import { getQuotes } from "@/services/quoteService";
import { getFamousPeople } from "@/services/famousPersonService";
import { getEvents } from "@/services/eventService";
import { getFolkFestivals } from "@/services/festivalService";

export interface ContentStats {
  totalArticles: number;
  totalCategories: number;
  totalTags: number;
  totalQuotes: number;
  totalFamousPeople: number;
  totalEvents: number;
  totalFestivals: number;
}

/**
 * Hook for fetching aggregate content counts for the dashboard.
 * Uses limit=1 on each list API to minimise payload — only meta.total is needed.
 */
export function useContentStats() {
  return useQuery<ContentStats>({
    queryKey: ["admin", "content-stats"],
    queryFn: async () => {
      const [articles, categories, tags, quotes, famousPeople, events, festivals] =
        await Promise.all([
          getArticles({ limit: 1 }),
          getCategories({ limit: 1 }),
          getTags({ limit: 1 }),
          getQuotes({ limit: 1 }),
          getFamousPeople({ limit: 1 }),
          getEvents({ limit: 1 }),
          getFolkFestivals({ limit: 1 }),
        ]);

      return {
        totalArticles: articles.meta?.total ?? 0,
        totalCategories: categories.meta?.total ?? 0,
        totalTags: tags.meta?.total ?? 0,
        totalQuotes: quotes.meta?.total ?? 0,
        totalFamousPeople: famousPeople.meta?.total ?? 0,
        totalEvents: events.meta?.total ?? 0,
        totalFestivals: festivals.meta?.total ?? 0,
      };
    },
    staleTime: 60 * 1000, // 1 minute
  });
}
