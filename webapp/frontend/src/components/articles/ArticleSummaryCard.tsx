"use client";

import Link from "next/link";
import { Clock } from "lucide-react";
import { ROUTES } from "@/lib/constants";
import { ResponsiveImage } from "@/components/shared/ResponsiveImage";
import type { ArticleSummary } from "@/types/article";

interface ArticleSummaryCardProps {
  article: ArticleSummary;
  /** Compact mode hides excerpt */
  compact?: boolean;
}

/**
 * A card component showing article thumbnail, title, excerpt, and reading time.
 * Used in RelatedArticles, DayDetail, and calendar views.
 */
export function ArticleSummaryCard({ article, compact = false }: ArticleSummaryCardProps) {
  return (
    <Link
      href={`${ROUTES.ARTICLES}/${article.slug || article.id}`}
      className="group hover:bg-warm-amber/5 flex gap-3 rounded-xl p-2.5 transition-all"
      style={{ border: "1px solid var(--ls-border-soft)" }}
    >
      {/* Thumbnail */}
      {article.featured_image && (
        <div className="relative h-16 w-16 shrink-0 overflow-hidden rounded-lg">
          <ResponsiveImage
            src={article.featured_image}
            alt={article.title}
            fill
            sizes="64px"
            imageClassName="transition-transform group-hover:scale-105"
          />
        </div>
      )}

      {/* Content */}
      <div className="flex min-w-0 flex-1 flex-col justify-center">
        <h4 className="text-text-dark group-hover:text-warm-amber line-clamp-2 text-[13px] leading-snug font-medium transition-colors">
          {article.title}
        </h4>
        {!compact && article.excerpt && (
          <p className="text-text-soft mt-0.5 line-clamp-1 text-[11px]">{article.excerpt}</p>
        )}
        <div className="text-text-muted-ls mt-1 flex items-center gap-2 text-[10px]">
          {article.reading_time > 0 && (
            <span className="flex items-center gap-0.5">
              <Clock className="h-3 w-3" />
              {article.reading_time} phút
            </span>
          )}
          {article.category && (
            <span className="bg-warm-amber/8 text-warm-amber rounded px-1.5 py-0.5">
              {article.category.name}
            </span>
          )}
        </div>
      </div>
    </Link>
  );
}
