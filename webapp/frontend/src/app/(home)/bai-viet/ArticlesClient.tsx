"use client";

import { useState, useCallback, useRef, useEffect } from "react";
import Link from "next/link";
import { Search, Clock, Eye, X, SlidersHorizontal, Newspaper, Sparkles, Hash } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { usePublicArticles, usePublicCategories, usePublicTags } from "@/hooks/usePublicContent";
import { getCategoryIcon } from "@/lib/categoryIcons";
import { ResponsiveImage } from "@/components/shared/ResponsiveImage";
import type { ArticleListParams } from "@/types/article";
import { ROUTES } from "@/lib/constants";

// ============================================
// Debounce hook
// ============================================
function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState(value);
  useEffect(() => {
    const timer = setTimeout(() => setDebouncedValue(value), delay);
    return () => clearTimeout(timer);
  }, [value, delay]);
  return debouncedValue;
}

export default function ArticlesClient() {
  const [params, setParams] = useState<ArticleListParams>({
    page: 1,
    limit: 12,
    status: "published",
  });
  const [search, setSearch] = useState("");
  const debouncedSearch = useDebounce(search, 400);
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [showFilters, setShowFilters] = useState(true);
  const inputRef = useRef<HTMLInputElement>(null);

  const { data: articlesData, isLoading } = usePublicArticles({
    ...params,
    search: debouncedSearch || undefined,
    category_id: selectedCategory || undefined,
  });
  const { data: categoriesData } = usePublicCategories({ limit: 50 });
  const { data: tagsData } = usePublicTags({ limit: 30 });

  const articles = articlesData?.data ?? [];
  const categories = categoriesData?.data ?? [];
  const tags = tagsData?.data ?? [];
  const meta = articlesData?.meta;

  const clearSearch = useCallback(() => {
    setSearch("");
    setParams((p) => ({ ...p, page: 1 }));
    inputRef.current?.focus();
  }, []);

  const clearAll = useCallback(() => {
    setSearch("");
    setSelectedCategory(null);
    setParams((p) => ({ ...p, page: 1 }));
  }, []);

  const hasActiveFilters = !!debouncedSearch || !!selectedCategory;

  return (
    <div className="mx-auto max-w-[1180px] px-4 py-6 sm:px-6 sm:py-8 lg:px-7">
      {/* Hero Header — compact on mobile */}
      <div className="mb-5 sm:mb-8 sm:text-center">
        <div className="hidden sm:flex bg-warm-amber/10 text-warm-amber mx-auto mb-4 h-14 w-14 items-center justify-center rounded-2xl">
          <Newspaper className="h-7 w-7" />
        </div>
        <h1 className="text-text-dark mb-1 text-2xl font-[var(--font-lora)] font-bold sm:mb-3 sm:text-4xl">
          Bài Viết
        </h1>
        <p className="text-text-soft hidden sm:block mx-auto max-w-lg">
          Khám phá các bài viết về lịch sử, văn hóa, phong thủy và truyền thống Việt Nam
        </p>
      </div>

      {/* Search Bar */}
      <div className="mb-4 sm:mb-6">
        <div
          className="focus-within:border-warm-amber/60 relative rounded-xl sm:rounded-2xl border-2 sm:mx-auto sm:max-w-2xl shadow-sm transition-colors"
          style={{
            background: "var(--ls-card-bg-solid)",
            borderColor: "var(--ls-border-warm)",
          }}
        >
          <Search className="text-warm-amber/60 pointer-events-none absolute top-1/2 left-4 h-4 w-4 -translate-y-1/2 sm:h-5 sm:w-5" />
          <Input
            ref={inputRef}
            placeholder="Tìm kiếm bài viết..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setParams((p) => ({ ...p, page: 1 }));
            }}
            className="placeholder:text-text-muted-ls/70 h-10 sm:h-12 border-0 bg-transparent pr-10 pl-10 sm:pr-12 sm:pl-12 text-sm sm:text-base focus-visible:ring-0 focus-visible:ring-offset-0"
          />
          {search && (
            <button
              onClick={clearSearch}
              className="text-text-muted-ls hover:bg-warm-cream/80 hover:text-text-dark absolute top-1/2 right-3 sm:right-4 -translate-y-1/2 rounded-full p-1 transition-colors"
              aria-label="Xóa tìm kiếm"
            >
              <X className="h-4 w-4" />
            </button>
          )}
        </div>

        {/* Filter toggle — only on mobile, desktop always shows */}
        <div className="mt-2 flex items-center justify-between sm:hidden">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setShowFilters((v) => !v)}
            className="text-text-soft hover:text-text-dark gap-1.5 text-xs"
          >
            <SlidersHorizontal className="h-3.5 w-3.5" />
            {showFilters ? "Ẩn bộ lọc" : "Hiện bộ lọc"}
          </Button>
          {hasActiveFilters && (
            <Button
              variant="ghost"
              size="sm"
              onClick={clearAll}
              className="text-warm-amber hover:text-warm-amber/80 text-xs"
            >
              Xóa bộ lọc
            </Button>
          )}
        </div>
        {/* Desktop filter toggle */}
        <div className="mt-4 hidden sm:flex items-center justify-center gap-2">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setShowFilters((v) => !v)}
            className="text-text-soft hover:text-text-dark gap-1.5 text-xs"
          >
            <SlidersHorizontal className="h-3.5 w-3.5" />
            {showFilters ? "Ẩn bộ lọc" : "Hiện bộ lọc"}
          </Button>
          {hasActiveFilters && (
            <Button
              variant="ghost"
              size="sm"
              onClick={clearAll}
              className="text-warm-amber hover:text-warm-amber/80 text-xs"
            >
              Xóa bộ lọc
            </Button>
          )}
        </div>
      </div>

      {/* Category Filter Pills — horizontal scroll on mobile, wrap on desktop */}
      {showFilters && categories.length > 0 && (
        <div className="mb-5 sm:mb-8 -mx-4 sm:mx-0">
          <div className="flex gap-2 overflow-x-auto px-4 pb-1 sm:flex-wrap sm:justify-center sm:overflow-x-visible sm:px-0 sm:pb-0 scrollbar-none">
            <Button
              variant={selectedCategory === null ? "default" : "outline"}
              size="sm"
              onClick={() => {
                setSelectedCategory(null);
                setParams((p) => ({ ...p, page: 1 }));
              }}
              className="h-8 flex-shrink-0 rounded-full px-4 text-xs"
            >
              Tất cả
            </Button>
            {categories.map((cat) => {
              const CatIcon = getCategoryIcon(cat.slug);
              return (
                <Button
                  key={cat.id}
                  variant={selectedCategory === cat.id ? "default" : "outline"}
                  size="sm"
                  onClick={() => {
                    setSelectedCategory(cat.id);
                    setParams((p) => ({ ...p, page: 1 }));
                  }}
                  className="h-8 flex-shrink-0 rounded-full px-4 text-xs"
                >
                  <CatIcon className="mr-1 h-3.5 w-3.5" />
                  {cat.name}
                </Button>
              );
            })}
          </div>
        </div>
      )}

      {/* Tags cloud — show when no category selected and no search */}
      {showFilters && !hasActiveFilters && tags.length > 0 && (
        <div className="mb-5 sm:mb-8">
          <div className="mb-2 flex items-center gap-1.5 sm:justify-center">
            <Hash className="text-text-muted-ls h-3.5 w-3.5" />
            <span className="text-text-muted-ls text-xs font-medium">Tags nổi bật</span>
          </div>
          <div className="flex flex-wrap gap-2 sm:justify-center">
            {tags.map((tag) => (
              <Link key={tag.id} href={`${ROUTES.ARTICLE_TAG}/${tag.slug}`}>
                <Badge
                  variant="outline"
                  className="text-xs cursor-pointer hover:bg-warm-amber/10 hover:border-warm-amber/40 hover:text-warm-amber transition-colors"
                >
                  #{tag.name}
                  {tag.article_count !== undefined && tag.article_count > 0 && (
                    <span className="text-text-muted-ls ml-1 text-[10px]">({tag.article_count})</span>
                  )}
                </Badge>
              </Link>
            ))}
          </div>
        </div>
      )}

      {/* Result count */}
      {meta && !isLoading && (
        <div className="text-text-muted-ls mb-6 text-sm">
          {hasActiveFilters ? (
            <span>
              Tìm thấy <strong className="text-text-dark">{meta.total}</strong> bài viết
              {debouncedSearch && (
                <>
                  {" "}
                  cho &ldquo;
                  <span className="text-warm-amber font-medium">{debouncedSearch}</span>
                  &rdquo;
                </>
              )}
            </span>
          ) : (
            <span>
              <strong className="text-text-dark">{meta.total}</strong> bài viết
            </span>
          )}
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
            <Search className="h-7 w-7" />
          </div>
          <p className="text-text-dark mb-1 text-lg font-medium">Không tìm thấy bài viết</p>
          <p className="text-text-muted-ls mb-4 text-sm">
            {debouncedSearch
              ? `Không có bài viết nào phù hợp với "${debouncedSearch}"`
              : "Hãy quay lại sau để xem các bài viết mới nhất"}
          </p>
          {hasActiveFilters && (
            <Button variant="outline" size="sm" onClick={clearAll}>
              Xóa bộ lọc
            </Button>
          )}
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
              {/* Mobile: horizontal layout | Desktop: vertical layout */}
              <div className="flex sm:block">
                {/* Image */}
                <div className="bg-warm-cream/50 relative w-28 flex-shrink-0 sm:w-auto sm:aspect-[16/10] overflow-hidden">
                  {/* Mobile aspect ratio wrapper */}
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
                  {/* Desktop image */}
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
                    <Link
                      href={`${ROUTES.ARTICLE_CATEGORY}/${article.category.slug}`}
                      onClick={(e) => e.stopPropagation()}
                      className="text-warm-amber hover:underline mb-1 block text-[11px] font-medium sm:mb-2 sm:text-xs w-fit"
                    >
                      {article.category.name}
                    </Link>
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
            disabled={params.page === 1}
            onClick={() => setParams((p) => ({ ...p, page: (p.page ?? 1) - 1 }))}
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
            onClick={() => setParams((p) => ({ ...p, page: (p.page ?? 1) + 1 }))}
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
      {/* Mobile: horizontal */}
      <div className="flex sm:hidden">
        <Skeleton className="h-[88px] w-28 flex-shrink-0" />
        <div className="flex flex-col justify-center gap-2 p-3 flex-1">
          <Skeleton className="h-2.5 w-16" />
          <Skeleton className="h-4 w-full" />
          <Skeleton className="h-4 w-3/4" />
          <Skeleton className="h-2.5 w-24" />
        </div>
      </div>
      {/* Desktop: vertical */}
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
