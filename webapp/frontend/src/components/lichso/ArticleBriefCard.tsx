"use client";

import { useState, useCallback } from "react";
import Link from "next/link";
import { Clock, RefreshCw } from "lucide-react";
import { ROUTES } from "@/lib/constants";
import { ResponsiveImage } from "@/components/shared/ResponsiveImage";
import { getRandomArticles } from "@/services/publicContentService";
import type { ArticleSummary } from "@/types/article";

interface ArticleBriefCardProps {
  articles: ArticleSummary[];
  onRefresh?: () => void;
  isRefreshing?: boolean;
}

/**
 * Brief article cards for DayDetailModal showing random/featured articles.
 * Includes a "Xem bài khác" (refresh) button that fetches new random articles.
 */
export function ArticleBriefCard({
  articles: initialArticles,
  onRefresh,
  isRefreshing: externalRefreshing,
}: ArticleBriefCardProps) {
  const [articles, setArticles] = useState(initialArticles);
  const [isRefreshing, setIsRefreshing] = useState(false);

  const handleRefresh = useCallback(async () => {
    setIsRefreshing(true);
    try {
      const res = await getRandomArticles(5);
      if (res.data && res.data.length > 0) {
        setArticles(res.data);
      }
    } catch {
      // If the random API fails, try the parent's refetch
      onRefresh?.();
    } finally {
      setIsRefreshing(false);
    }
  }, [onRefresh]);

  const refreshing = isRefreshing || externalRefreshing;

  if (!articles || articles.length === 0) return null;

  return (
    <div>
      <div className="mb-2.5 flex items-center justify-between">
        <div className="flex items-center gap-1.5">
          <span className="text-sm">📰</span>
          <span className="text-warm-amber text-[9px] font-semibold tracking-[2px] uppercase">
            Bài viết hay
          </span>
        </div>
        {onRefresh && (
          <button
            onClick={handleRefresh}
            disabled={refreshing}
            className="text-text-muted-ls hover:text-warm-amber flex items-center gap-1 text-[10px] transition-colors disabled:opacity-50"
          >
            <RefreshCw className={`h-3 w-3 ${refreshing ? "animate-spin" : ""}`} />
            Xem bài khác
          </button>
        )}
      </div>

      <div className="space-y-2">
        {articles.slice(0, 3).map((article) => (
          <Link
            key={article.id}
            href={`${ROUTES.ARTICLES}/${article.slug || article.id}`}
            className="group hover:bg-warm-amber/5 flex gap-2.5 rounded-lg p-2 transition-all"
            style={{ border: "1px solid var(--ls-border-soft)" }}
          >
            {article.featured_image && (
              <div className="relative h-12 w-12 shrink-0 overflow-hidden rounded-md">
                <ResponsiveImage
                  src={article.featured_image}
                  alt={article.title}
                  fill
                  sizes="48px"
                />
              </div>
            )}
            <div className="flex min-w-0 flex-1 flex-col justify-center">
              <h5 className="text-text-dark group-hover:text-warm-amber line-clamp-2 text-[12px] leading-snug font-medium transition-colors">
                {article.title}
              </h5>
              {article.reading_time > 0 && (
                <span className="text-text-muted-ls mt-0.5 flex items-center gap-0.5 text-[10px]">
                  <Clock className="h-2.5 w-2.5" />
                  {article.reading_time} phút đọc
                </span>
              )}
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
