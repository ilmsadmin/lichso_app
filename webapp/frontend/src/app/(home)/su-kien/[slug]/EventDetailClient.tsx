"use client";

import { use } from "react";
import Link from "next/link";
import { ArrowLeft, Calendar, MapPin, Hash, Share2, BookOpen, Clock, ArrowRight } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Separator } from "@/components/ui/separator";
import { usePublicEventBySlug } from "@/hooks/usePublicContent";
import { useLinkedArticle } from "@/hooks/useLinkedArticle";
import { ResponsiveImage } from "@/components/shared/ResponsiveImage";
import { AdminEditButton } from "@/components/shared/AdminEditButton";
import { ROUTES } from "@/lib/constants";
import type { EventType, EventImportance } from "@/types/event";

const eventTypeLabels: Record<EventType, string> = {
  historical_event: "Lịch sử",
  national_day: "Ngày quốc gia",
  world_day: "Ngày quốc tế",
  anniversary: "Kỷ niệm",
  cultural: "Văn hóa",
  military: "Quân sự",
};

const importanceBadge: Record<EventImportance, { label: string; className: string }> = {
  high: { label: "Quan trọng", className: "bg-red-100 text-red-700 border-red-200" },
  medium: { label: "Trung bình", className: "bg-yellow-100 text-yellow-700 border-yellow-200" },
  low: { label: "Thường", className: "bg-gray-100 text-gray-600 border-gray-200" },
};

export default function EventDetailClient({ slug }: { slug: string }) {
  const { data, isLoading, error } = usePublicEventBySlug(slug);
  const event = data?.data;
  const { data: linkedArticleData, isLoading: articleLoading } = useLinkedArticle(
    event?.article_id
  );
  const linkedArticle = linkedArticleData?.data;

  if (isLoading) return <DetailSkeleton />;

  if (error || !event) {
    return (
      <div className="mx-auto max-w-[800px] px-4 py-16 text-center">
        <p className="mb-4 text-5xl">📅</p>
        <h1 className="text-text-dark mb-2 text-2xl font-semibold">Không tìm thấy sự kiện</h1>
        <p className="text-text-soft mb-6">Sự kiện bạn tìm kiếm không tồn tại hoặc đã bị xóa.</p>
        <Button asChild>
          <Link href={ROUTES.EVENTS}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            Quay lại danh sách
          </Link>
        </Button>
      </div>
    );
  }

  return (
    <article className="mx-auto max-w-[800px] px-4 py-8 sm:px-6">
      <AdminEditButton href={`/admin/events/${event.id}/edit`} label="Sửa sự kiện" />
      {/* Breadcrumb nav */}
      <div className="mb-6 flex items-center gap-2 text-sm">
        <Link
          href={ROUTES.EVENTS}
          className="text-text-soft hover:text-warm-amber inline-flex items-center gap-1.5 transition-colors"
        >
          <ArrowLeft className="h-3.5 w-3.5" />
          Tất cả sự kiện
        </Link>
        <span className="text-text-muted-ls">/</span>
        <Badge variant="outline" className="border-warm-amber/30 text-warm-amber">
          {eventTypeLabels[event.event_type]}
        </Badge>
      </div>

      {/* Importance Badge */}
      <div className="mb-4">
        <Badge
          variant="outline"
          className={`text-xs ${importanceBadge[event.importance].className}`}
        >
          {importanceBadge[event.importance].label}
        </Badge>
      </div>

      {/* Title */}
      <h1 className="text-text-dark mb-4 text-3xl leading-tight font-[var(--font-lora)] font-bold sm:text-4xl">
        {event.title}
      </h1>

      {/* Description */}
      {event.short_description && (
        <p className="text-text-soft mb-6 text-lg leading-relaxed">{event.short_description}</p>
      )}

      {/* Image */}
      {event.image_url && (
        <div className="relative mb-6 aspect-[16/9] overflow-hidden rounded-xl">
          <ResponsiveImage
            src={event.image_url}
            alt={event.title}
            fill
            sizes="(max-width: 800px) 100vw, 800px"
            priority
          />
        </div>
      )}

      {/* Meta */}
      <div className="text-text-muted-ls mb-6 flex flex-wrap items-center gap-4 text-sm">
        <span className="flex items-center gap-1.5">
          <Calendar className="h-4 w-4" />
          {new Date(event.event_date).toLocaleDateString("vi-VN", {
            year: "numeric",
            month: "long",
            day: "numeric",
          })}
        </span>
        {event.country && (
          <span className="flex items-center gap-1.5">
            <MapPin className="h-4 w-4" />
            {event.flag_emoji && `${event.flag_emoji} `}
            {event.country}
          </span>
        )}
      </div>

      <Separator className="mb-8" />

      {/* Tags */}
      {event.tags && event.tags.length > 0 && (
        <div className="mb-8 flex flex-wrap items-center gap-2">
          <Hash className="text-text-muted-ls h-4 w-4" />
          {event.tags.map((tag) => (
            <Badge key={tag} variant="secondary" className="text-xs">
              {tag}
            </Badge>
          ))}
        </div>
      )}

      {/* Linked Article */}
      {event.article_id && (
        <div className="mb-8">
          <h2 className="text-text-dark mb-4 flex items-center gap-2.5 text-lg font-[var(--font-lora)] font-semibold">
            <span className="flex h-8 w-8 items-center justify-center rounded-xl bg-amber-50 dark:bg-amber-900/30">
              <BookOpen className="text-warm-amber h-4 w-4" />
            </span>
            Bài viết liên quan
          </h2>
          {articleLoading ? (
            <Skeleton className="h-32 rounded-2xl" />
          ) : linkedArticle ? (
            <Link
              href={`${ROUTES.ARTICLES}/${linkedArticle.slug}`}
              className="group relative flex gap-4 overflow-hidden rounded-2xl border p-5 transition-all hover:shadow-lg"
              style={{
                background: "linear-gradient(135deg, var(--ls-card-bg-solid), var(--ls-card-bg))",
                borderColor: "var(--ls-border-warm)",
              }}
            >
              {/* Decorative accent */}
              <div className="bg-warm-amber/20 absolute top-0 left-0 h-full w-1 transition-all group-hover:w-1.5 group-hover:bg-amber-400" />

              {linkedArticle.featured_image && (
                <div className="relative h-28 w-28 flex-shrink-0 overflow-hidden rounded-xl">
                  <ResponsiveImage
                    src={linkedArticle.featured_image}
                    alt={linkedArticle.title}
                    fill
                    sizes="112px"
                    className="transition-transform duration-300 group-hover:scale-105"
                  />
                </div>
              )}
              <div className="min-w-0 flex-1 py-0.5">
                <div className="mb-1.5 flex items-center gap-1.5">
                  <BookOpen className="text-warm-amber h-3.5 w-3.5" />
                  <span className="text-warm-amber text-[11px] font-medium uppercase tracking-wider">
                    Đọc thêm
                  </span>
                </div>
                <h3 className="text-text-dark group-hover:text-warm-amber mb-2 line-clamp-2 text-base font-semibold leading-snug transition-colors">
                  {linkedArticle.title}
                </h3>
                {linkedArticle.excerpt && (
                  <p className="text-text-soft mb-3 line-clamp-2 text-sm leading-relaxed">
                    {linkedArticle.excerpt}
                  </p>
                )}
                <div className="flex items-center gap-3">
                  {linkedArticle.reading_time > 0 && (
                    <span className="text-text-muted-ls flex items-center gap-1 text-xs">
                      <Clock className="h-3 w-3" />
                      {linkedArticle.reading_time} phút đọc
                    </span>
                  )}
                  <span className="text-warm-amber flex items-center gap-1 text-xs font-medium opacity-0 transition-opacity group-hover:opacity-100">
                    Xem bài viết <ArrowRight className="h-3 w-3" />
                  </span>
                </div>
              </div>
            </Link>
          ) : null}
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
                  title: event.title,
                  text: event.short_description,
                  url: window.location.href,
                });
              } catch {
                // User cancelled share dialog — ignore
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
    </article>
  );
}

function DetailSkeleton() {
  return (
    <div className="mx-auto max-w-[800px] px-4 py-8 sm:px-6">
      <Skeleton className="mb-6 h-4 w-32" />
      <div className="mb-4 flex gap-2">
        <Skeleton className="h-6 w-20" />
        <Skeleton className="h-6 w-20" />
      </div>
      <Skeleton className="mb-2 h-10 w-full" />
      <Skeleton className="mb-4 h-10 w-3/4" />
      <Skeleton className="mb-6 h-5 w-full" />
      <div className="mb-6 flex gap-4">
        <Skeleton className="h-4 w-36" />
        <Skeleton className="h-4 w-28" />
      </div>
      <div className="space-y-4">
        <Skeleton className="h-4 w-full" />
        <Skeleton className="h-4 w-full" />
        <Skeleton className="h-4 w-5/6" />
      </div>
    </div>
  );
}
