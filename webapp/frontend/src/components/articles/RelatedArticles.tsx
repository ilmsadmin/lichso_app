"use client";

import { BookOpen, RefreshCw, Layers } from "lucide-react";
import { Skeleton } from "@/components/ui/skeleton";
import { Separator } from "@/components/ui/separator";
import { ArticleSummaryCard } from "@/components/articles/ArticleSummaryCard";
import { useRelatedArticles } from "@/hooks/useV3";

interface RelatedArticlesProps {
  articleId: string;
  limit?: number;
}

/**
 * Displays related articles at the bottom of an article detail page.
 * Uses the V3 smart related articles engine (manual → category → tags → random).
 */
export function RelatedArticles({ articleId, limit = 6 }: RelatedArticlesProps) {
  const { data, isLoading, refetch, isFetching } = useRelatedArticles(articleId, limit);
  const response = data?.data;

  const relatedArticles = response?.related ?? [];
  const seriesArticles = response?.series ?? [];
  const randomPicks = response?.random_picks ?? [];

  const hasAny = relatedArticles.length > 0 || seriesArticles.length > 0 || randomPicks.length > 0;

  if (isLoading) {
    return (
      <div className="space-y-3">
        <Skeleton className="h-6 w-40" />
        <div className="grid gap-3 sm:grid-cols-2">
          {Array.from({ length: 4 }).map((_, i) => (
            <Skeleton key={i} className="h-20 rounded-xl" />
          ))}
        </div>
      </div>
    );
  }

  if (!hasAny) return null;

  return (
    <section className="space-y-5">
      <Separator />

      {/* Series articles */}
      {seriesArticles.length > 0 && (
        <div>
          <div className="mb-3 flex items-center gap-2.5">
            <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-teal-50 dark:bg-teal-900/30">
              <Layers className="text-jade-teal h-3.5 w-3.5" />
            </span>
            <h3 className="text-text-dark text-sm font-semibold tracking-wider uppercase">
              Cùng chuỗi bài viết
            </h3>
          </div>
          <div className="grid gap-2.5 sm:grid-cols-2">
            {seriesArticles.map((article) => (
              <ArticleSummaryCard key={article.id} article={article} compact />
            ))}
          </div>
        </div>
      )}

      {/* Related articles */}
      {relatedArticles.length > 0 && (
        <div>
          <div className="mb-3 flex items-center gap-2.5">
            <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-amber-50 dark:bg-amber-900/30">
              <BookOpen className="text-warm-amber h-3.5 w-3.5" />
            </span>
            <h3 className="text-text-dark text-sm font-semibold tracking-wider uppercase">
              Bài viết liên quan
            </h3>
          </div>
          <div className="grid gap-2.5 sm:grid-cols-2">
            {relatedArticles.map((article) => (
              <ArticleSummaryCard key={article.id} article={article} />
            ))}
          </div>
        </div>
      )}

      {/* Random picks */}
      {randomPicks.length > 0 && (
        <div>
          <div className="mb-3 flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-purple-50 dark:bg-purple-900/30">
                <span className="text-sm">✨</span>
              </span>
              <h3 className="text-text-dark text-sm font-semibold tracking-wider uppercase">
                Có thể bạn quan tâm
              </h3>
            </div>
            <button
              onClick={() => refetch()}
              disabled={isFetching}
              className="text-text-muted-ls hover:text-warm-amber flex items-center gap-1 text-xs transition-colors disabled:opacity-50"
            >
              <RefreshCw className={`h-3 w-3 ${isFetching ? "animate-spin" : ""}`} />
              Xem bài khác
            </button>
          </div>
          <div className="grid gap-2.5 sm:grid-cols-2">
            {randomPicks.map((article) => (
              <ArticleSummaryCard key={article.id} article={article} />
            ))}
          </div>
        </div>
      )}
    </section>
  );
}
