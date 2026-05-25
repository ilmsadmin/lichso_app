"use client";

import Link from "next/link";
import { ArrowLeft, Clock, Eye, Calendar, User, Hash, Share2, Newspaper } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Separator } from "@/components/ui/separator";
import { usePublicArticleBySlug } from "@/hooks/usePublicContent";
import { RelatedArticles } from "@/components/articles/RelatedArticles";
import { ArticleContent } from "@/components/articles/ArticleContent";
import { ResponsiveImage } from "@/components/shared/ResponsiveImage";
import { AdminEditButton } from "@/components/shared/AdminEditButton";
import { ROUTES } from "@/lib/constants";

export default function ArticleDetailClient({ slug }: { slug: string }) {
  const { data, isLoading, error } = usePublicArticleBySlug(slug);
  const article = data?.data;

  if (isLoading) return <ArticleDetailSkeleton />;

  if (error || !article) {
    return (
      <div className="mx-auto max-w-[800px] px-4 py-16 text-center">
        <div className="bg-warm-amber/10 text-warm-amber mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl">
          <Newspaper className="h-7 w-7" />
        </div>
        <h1 className="text-text-dark mb-2 text-2xl font-semibold">Không tìm thấy bài viết</h1>
        <p className="text-text-soft mb-6">Bài viết bạn tìm kiếm không tồn tại hoặc đã bị xóa.</p>
        <Button asChild>
          <Link href={ROUTES.ARTICLES}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            Quay lại danh sách
          </Link>
        </Button>
      </div>
    );
  }

  return (
    <article className="mx-auto max-w-[800px] px-4 py-8 sm:px-6">
      <AdminEditButton href={`/admin/articles/${article.id}/edit`} label="Sửa bài viết" />
      {/* Breadcrumb nav */}
      <div className="mb-6 flex items-center gap-2 text-sm">
        <Link
          href={ROUTES.ARTICLES}
          className="text-text-soft hover:text-warm-amber inline-flex items-center gap-1.5 transition-colors"
        >
          <ArrowLeft className="h-3.5 w-3.5" />
          Tất cả bài viết
        </Link>
        {article.category && (
          <>
            <span className="text-text-muted-ls">/</span>
            <Link href={`${ROUTES.ARTICLE_CATEGORY}/${article.category.slug}`}>
              <Badge
                variant="outline"
                className="border-warm-amber/30 text-warm-amber hover:bg-warm-amber/10 transition-colors cursor-pointer"
              >
                {article.category.name}
              </Badge>
            </Link>
          </>
        )}
      </div>

      {/* Title */}
      <h1 className="text-text-dark mb-4 text-3xl leading-tight font-[var(--font-lora)] font-bold sm:text-4xl">
        {article.title}
      </h1>

      {/* Excerpt */}
      {article.excerpt && (
        <p className="text-text-soft mb-6 text-lg leading-relaxed">{article.excerpt}</p>
      )}

      {/* Meta */}
      <div className="text-text-muted-ls mb-6 flex flex-wrap items-center gap-4 text-sm">
        {article.author && (
          <span className="flex items-center gap-1.5">
            <User className="h-4 w-4" />
            {article.author.full_name}
          </span>
        )}
        {article.published_at && (
          <span className="flex items-center gap-1.5">
            <Calendar className="h-4 w-4" />
            {new Date(article.published_at).toLocaleDateString("vi-VN", {
              year: "numeric",
              month: "long",
              day: "numeric",
            })}
          </span>
        )}
        <span className="flex items-center gap-1.5">
          <Clock className="h-4 w-4" />
          {article.reading_time} phút đọc
        </span>
        <span className="flex items-center gap-1.5">
          <Eye className="h-4 w-4" />
          {article.view_count} lượt xem
        </span>
      </div>

      <Separator className="mb-8" />

      {/* Featured Image */}
      {article.featured_image && (
        <div className="relative mb-8 aspect-[16/9] overflow-hidden rounded-2xl">
          <ResponsiveImage
            src={article.featured_image}
            alt={article.title}
            fill
            priority
            sizes="(max-width: 800px) 100vw, 800px"
            aspectRatio="16/9"
            className="h-full w-full"
            imageClassName="rounded-2xl"
          />
        </div>
      )}

      {/* Article Content */}
      <ArticleContent content={article.content} />

      <Separator className="my-8" />

      {/* Tags */}
      {article.tags && article.tags.length > 0 && (
        <div className="mb-8 flex flex-wrap items-center gap-2">
          <Hash className="text-text-muted-ls h-4 w-4" />
          {article.tags.map((tag) => (
            <Link key={tag.id} href={`${ROUTES.ARTICLE_TAG}/${tag.slug}`}>
              <Badge
                variant="secondary"
                className="text-xs hover:bg-warm-amber/10 hover:text-warm-amber hover:border-warm-amber/30 transition-colors cursor-pointer"
              >
                #{tag.name}
              </Badge>
            </Link>
          ))}
        </div>
      )}

      {/* Share */}
      <div className="flex items-center gap-3">
        <span className="text-text-soft text-sm">Chia sẻ:</span>
        <Button
          variant="outline"
          size="sm"
          onClick={async () => {
            if (navigator.share) {
              try {
                await navigator.share({
                  title: article.title,
                  text: article.excerpt,
                  url: window.location.href,
                });
              } catch {
                // User cancelled share — ignore
              }
            } else {
              navigator.clipboard.writeText(window.location.href);
            }
          }}
        >
          <Share2 className="mr-1.5 h-4 w-4" />
          Chia sẻ
        </Button>
      </div>

      {/* Related Articles (V3) */}
      <div className="mt-8">
        <RelatedArticles articleId={article.id} />
      </div>
    </article>
  );
}

function ArticleDetailSkeleton() {
  return (
    <div className="mx-auto max-w-[800px] px-4 py-8 sm:px-6">
      <Skeleton className="mb-6 h-4 w-32" />
      <Skeleton className="mb-4 h-6 w-24" />
      <Skeleton className="mb-2 h-10 w-full" />
      <Skeleton className="mb-4 h-10 w-3/4" />
      <Skeleton className="mb-6 h-5 w-full" />
      <div className="mb-6 flex gap-4">
        <Skeleton className="h-4 w-28" />
        <Skeleton className="h-4 w-28" />
        <Skeleton className="h-4 w-20" />
      </div>
      <Skeleton className="mb-8 aspect-[16/9] w-full rounded-2xl" />
      <div className="space-y-4">
        <Skeleton className="h-4 w-full" />
        <Skeleton className="h-4 w-full" />
        <Skeleton className="h-4 w-5/6" />
        <Skeleton className="h-4 w-full" />
        <Skeleton className="h-4 w-4/5" />
      </div>
    </div>
  );
}
