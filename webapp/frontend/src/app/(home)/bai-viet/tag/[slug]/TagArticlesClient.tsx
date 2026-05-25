"use client";

import { useState } from "react";
import Link from "next/link";
import { ArrowLeft, Clock, Eye, Newspaper, Sparkles, Hash } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { usePublicTagBySlug } from "@/hooks/usePublicContent";
import { useQuery } from "@tanstack/react-query";
import * as publicContent from "@/services/publicContentService";
import { ResponsiveImage } from "@/components/shared/ResponsiveImage";
import { ROUTES } from "@/lib/constants";

export default function TagArticlesClient({ slug }: { slug: string }) {
  const [page, setPage] = useState(1);
  const { data: tagData, isLoading: tagLoading } = usePublicTagBySlug(slug);
  const tag = tagData?.data;

  // Only fetch articles once we have the tag's ID
  const { data: articlesData, isLoading: articlesLoading } = useQuery({
    queryKey: ["public-articles", { page, limit: 12, tag_id: tag?.id }],
    queryFn: () =>
      publicContent.getPublicArticles({
        page,
        limit: 12,
        status: "published",
        tag_id: tag!.id,
      }),
    enabled: !!tag?.id,
  });

  const articles = articlesData?.data ?? [];
  const meta = articlesData?.meta;
  const isLoading = tagLoading || (!!tag && articlesLoading);

  if (!tagLoading && !tag) {
    return (
      <div className="mx-auto max-w-[800px] px-4 py-16 text-center">
        <div className="bg-warm-amber/10 text-warm-amber mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl">
          <Hash className="h-7 w-7" />
        </div>
        <h1 className="text-text-dark mb-2 text-2xl font-semibold">Không tìm thấy tag</h1>
        <p className="text-text-soft mb-6">Tag bạn tìm kiếm không tồn tại.</p>
        <Button asChild>
          <Link href={ROUTES.ARTICLES}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            Quay lại danh sách bài viết
          </Link>
        </Button>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-[1180px] px-4 py-6 sm:px-6 sm:py-8 lg:px-7">
      {/* Back link */}
      <div className="mb-5">
        <Link
          href={ROUTES.ARTICLES}
          className="text-text-soft hover:text-warm-amber inline-flex items-center gap-1.5 text-sm transition-colors"
        >
          <ArrowLeft className="h-3.5 w-3.5" />
          Tất cả bài viết
        </Link>
      </div>

      {/* Header */}
      <div className="mb-6 sm:mb-10 sm:text-center">
        {tagLoading ? (
          <div className="flex flex-col items-center gap-3">
            <Skeleton className="h-14 w-14 rounded-2xl" />
            <Skeleton className="h-8 w-48" />
          </div>
        ) : (
          <>
            <div className="bg-warm-amber/10 text-warm-amber mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl">
              <Hash className="h-7 w-7" />
            </div>
            <div className="mb-3 flex items-center justify-center gap-2 sm:justify-center">
              <span className="text-text-soft text-sm">Tag:</span>
              <Badge
                variant="secondary"
                className="border-warm-amber/30 bg-warm-amber/10 text-warm-amber px-3 py-1 text-base font-semibold"
              >
                #{tag?.name}
              </Badge>
            </div>
            {tag?.article_count !== undefined && (
              <p className="text-text-soft text-sm">
                {tag.article_count} bài viết với tag này
              </p>
            )}
          </>
        )}
      </div>

      {/* Result count */}
      {meta && !isLoading && (
        <div className="text-text-muted-ls mb-6 text-sm">
          <strong className="text-text-dark">{meta.total}</strong> bài viết với tag{" "}
          <span className="text-warm-amber font-medium">#{tag?.name}</span>
        </div>
      )}

      {/* Article Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 sm:gap-6 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <ArticleSkeleton key={i} />
          ))}
        </div>
      ) : articles.length === 0 ? (
        <div className="py-16 text-center">
          <div className="bg-warm-amber/10 text-warm-amber mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl">
            <Newspaper className="h-7 w-7" />
          </div>
          <p className="text-text-dark mb-1 text-lg font-medium">Chưa có bài viết nào</p>
          <p className="text-text-muted-ls mb-4 text-sm">
            Chưa có bài viết nào được gắn tag này.
          </p>
          <Button asChild variant="outline" size="sm">
            <Link href={ROUTES.ARTICLES}>Xem tất cả bài viết</Link>
          </Button>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 sm:gap-6 lg:grid-cols-3">
          {articles.map((article) => (
            <Link
              key={article.id}
              href={`${ROUTES.ARTICLES}/${article.slug}`}
              className="group overflow-hidden rounded-xl sm:rounded-2xl border transition-all hover:-translate-y-0.5 hover:shadow-lg"
              style={{
                background: "var(--ls-card-bg-solid)",
                borderColor: "var(--ls-border-warm)",
              }}
            >
              <div className="flex sm:block">
                {/* Image */}
                <div className="bg-warm-cream/50 relative w-28 flex-shrink-0 sm:w-auto overflow-hidden">
                  <div className="relative h-full w-full sm:hidden" style={{ aspectRatio: "1/1" }}>
                    {article.featured_image ? (
                      <ResponsiveImage
                        src={article.featured_image}
                        alt={article.title}
                        fill
                        sizes="112px"
                        imageClassName="transition-transform group-hover:scale-105"
                      />
                    ) : (
                      <div className="flex h-full items-center justify-center">
                        <Newspaper className="text-warm-amber/20 h-8 w-8" />
                      </div>
                    )}
                  </div>
                  <div className="relative hidden sm:block aspect-[16/10]">
                    {article.featured_image ? (
                      <ResponsiveImage
                        src={article.featured_image}
                        alt={article.title}
                        fill
                        sizes="(max-width: 1024px) 50vw, 33vw"
                        imageClassName="transition-transform group-hover:scale-105"
                      />
                    ) : (
                      <div className="flex h-full items-center justify-center">
                        <Newspaper className="text-warm-amber/20 h-10 w-10" />
                      </div>
                    )}
                  </div>
                  {article.is_featured && (
                    <Badge className="bg-warm-amber absolute top-2 left-2 border-0 text-white shadow-sm text-[10px] px-1.5 py-0 sm:top-3 sm:left-3 sm:text-xs sm:px-2">
                      <Sparkles className="mr-0.5 h-2.5 w-2.5 sm:mr-1 sm:h-3 sm:w-3" />
                      <span className="hidden sm:inline">Nổi bật</span>
                      <span className="sm:hidden">★</span>
                    </Badge>
                  )}
                </div>

                {/* Content */}
                <div className="flex min-w-0 flex-1 flex-col justify-center p-3 sm:p-5">
                  {article.category && (
                    <span className="text-warm-amber mb-1 block text-[11px] font-medium sm:mb-2 sm:text-xs">
                      {article.category.name}
                    </span>
                  )}
                  <h2 className="text-text-dark group-hover:text-warm-amber mb-1 line-clamp-2 text-sm font-semibold leading-snug transition-colors sm:mb-2 sm:text-lg">
                    {article.title}
                  </h2>
                  <p className="text-text-soft mb-2 line-clamp-1 text-xs leading-relaxed sm:mb-4 sm:line-clamp-2 sm:text-sm">
                    {article.excerpt}
                  </p>
                  <div className="text-text-muted-ls flex items-center gap-3 text-[11px] sm:justify-between sm:text-xs">
                    <span className="flex items-center gap-1">
                      <Clock className="h-3 w-3" />
                      {article.reading_time} phút
                    </span>
                    <span className="flex items-center gap-1">
                      <Eye className="h-3 w-3" />
                      {article.view_count}
                    </span>
                    {article.published_at && (
                      <time className="hidden sm:block ml-auto">
                        {new Date(article.published_at).toLocaleDateString("vi-VN")}
                      </time>
                    )}
                  </div>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}

      {/* Pagination */}
      {meta && meta.total_pages > 1 && (
        <div className="mt-10 flex items-center justify-center gap-2">
          <Button
            variant="outline"
            size="sm"
            disabled={page === 1}
            onClick={() => setPage((p) => p - 1)}
          >
            Trang trước
          </Button>
          <span className="text-text-soft px-4 text-sm">
            Trang {meta.page} / {meta.total_pages}
          </span>
          <Button
            variant="outline"
            size="sm"
            disabled={meta.page >= meta.total_pages}
            onClick={() => setPage((p) => p + 1)}
          >
            Trang sau
          </Button>
        </div>
      )}
    </div>
  );
}

function ArticleSkeleton() {
  return (
    <div
      className="overflow-hidden rounded-xl sm:rounded-2xl border"
      style={{ borderColor: "var(--ls-border-warm)" }}
    >
      <div className="flex sm:hidden">
        <Skeleton className="h-[88px] w-28 flex-shrink-0" />
        <div className="flex flex-col justify-center gap-2 p-3 flex-1">
          <Skeleton className="h-2.5 w-16" />
          <Skeleton className="h-4 w-full" />
          <Skeleton className="h-4 w-3/4" />
          <Skeleton className="h-2.5 w-24" />
        </div>
      </div>
      <div className="hidden sm:block">
        <Skeleton className="aspect-[16/10] w-full" />
        <div className="space-y-3 p-5">
          <Skeleton className="h-3 w-20" />
          <Skeleton className="h-5 w-full" />
          <Skeleton className="h-5 w-3/4" />
          <Skeleton className="h-3 w-full" />
          <Skeleton className="h-3 w-2/3" />
        </div>
      </div>
    </div>
  );
}
