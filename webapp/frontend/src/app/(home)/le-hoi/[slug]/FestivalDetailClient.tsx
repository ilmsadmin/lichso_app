"use client";

import Link from "next/link";
import { ArrowLeft, Moon, Sun, MapPin, Heart, Hash, Share2, BookOpen, Clock, ArrowRight } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Separator } from "@/components/ui/separator";
import { usePublicFolkFestivalBySlug } from "@/hooks/usePublicContent";
import { useLinkedArticle } from "@/hooks/useLinkedArticle";
import { ResponsiveImage } from "@/components/shared/ResponsiveImage";
import { AdminEditButton } from "@/components/shared/AdminEditButton";
import { ROUTES } from "@/lib/constants";
import type { FestivalType } from "@/types/festival";

const festivalTypeLabels: Record<FestivalType, string> = {
  folk_festival: "Lễ hội dân gian",
  religion: "Tôn giáo",
  national_holiday: "Quốc lễ",
  seasonal: "Theo mùa",
  other: "Khác",
};

export default function FestivalDetailClient({ slug }: { slug: string }) {
  const { data, isLoading, error } = usePublicFolkFestivalBySlug(slug);
  const festival = data?.data;
  const { data: linkedArticleData, isLoading: articleLoading } = useLinkedArticle(
    festival?.article_id
  );
  const linkedArticle = linkedArticleData?.data;

  if (isLoading) return <DetailSkeleton />;

  if (error || !festival) {
    return (
      <div className="mx-auto max-w-[800px] px-4 py-16 text-center">
        <p className="mb-4 text-5xl">🏮</p>
        <h1 className="text-text-dark mb-2 text-2xl font-semibold">Không tìm thấy lễ hội</h1>
        <p className="text-text-soft mb-6">Lễ hội bạn tìm kiếm không tồn tại hoặc đã bị xóa.</p>
        <Button asChild>
          <Link href={ROUTES.FESTIVALS}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            Quay lại danh sách
          </Link>
        </Button>
      </div>
    );
  }

  return (
    <article className="mx-auto max-w-[800px] px-4 py-8 sm:px-6">
      <AdminEditButton href={`/admin/festivals/${festival.id}/edit`} label="Sửa lễ hội" />
      {/* Breadcrumb nav */}
      <div className="mb-6 flex items-center gap-2 text-sm">
        <Link
          href={ROUTES.FESTIVALS}
          className="text-text-soft hover:text-warm-amber inline-flex items-center gap-1.5 transition-colors"
        >
          <ArrowLeft className="h-3.5 w-3.5" />
          Tất cả lễ hội
        </Link>
        <span className="text-text-muted-ls">/</span>
        <Badge variant="outline" className="border-warm-amber/30 text-warm-amber">
          {festivalTypeLabels[festival.festival_type as FestivalType]}
        </Badge>
      </div>

      {/* Title */}
      <h1 className="text-text-dark mb-4 text-3xl leading-tight font-[var(--font-lora)] font-bold sm:text-4xl">
        {festival.name}
      </h1>

      {/* Description */}
      {festival.short_description && (
        <p className="text-text-soft mb-6 text-lg leading-relaxed">{festival.short_description}</p>
      )}

      {/* Image */}
      {festival.image_url && (
        <div className="mb-6 overflow-hidden rounded-xl">
          <img
            src={festival.image_url}
            alt={festival.name}
            className="max-h-96 w-full object-cover"
          />
        </div>
      )}

      {/* Date & Location */}
      <div className="text-text-muted-ls mb-6 flex flex-wrap items-center gap-4 text-sm">
        {(festival.lunar_month ?? 0) > 0 && (festival.lunar_day ?? 0) > 0 && (
          <span className="flex items-center gap-1.5">
            <Moon className="h-4 w-4" />
            Mùng {festival.lunar_day} tháng {festival.lunar_month} Âm lịch
          </span>
        )}
        {(festival.solar_month ?? 0) > 0 && (festival.solar_day ?? 0) > 0 && (
          <span className="flex items-center gap-1.5">
            <Sun className="h-4 w-4" />
            Ngày {festival.solar_day}/{festival.solar_month} Dương lịch
          </span>
        )}
      </div>

      {/* Region */}
      {festival.region && (
        <div className="mb-6 flex flex-wrap items-center gap-2">
          <MapPin className="text-text-muted-ls h-4 w-4" />
          <Badge variant="secondary" className="text-xs">
            {festival.region}
          </Badge>
        </div>
      )}

      <Separator className="mb-8" />

      {/* Traditions */}
      {festival.traditions && festival.traditions.length > 0 && (
        <div className="mb-8">
          <h2 className="text-text-dark mb-4 flex items-center gap-2 text-lg font-[var(--font-lora)] font-semibold">
            <Heart className="text-warm-amber h-5 w-5" />
            Phong tục
          </h2>
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            {festival.traditions.map((tradition: string, i: number) => (
              <div
                key={i}
                className="rounded-xl border p-4"
                style={{
                  background: "var(--ls-card-bg)",
                  borderColor: "var(--ls-border-warm)",
                }}
              >
                <span className="text-text-mid text-sm">{tradition}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Gallery */}
      {festival.gallery_urls && festival.gallery_urls.length > 0 && (
        <div className="mb-8">
          <h2 className="text-text-dark mb-4 text-lg font-[var(--font-lora)] font-semibold">
            Hình ảnh
          </h2>
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
            {festival.gallery_urls.map((img: string, i: number) => (
              <div key={i} className="relative aspect-[4/3] overflow-hidden rounded-xl">
                <ResponsiveImage
                  src={img}
                  alt={`${festival.name} - ${i + 1}`}
                  fill
                  sizes="(max-width: 640px) 50vw, 33vw"
                />
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Tags */}
      {festival.tags && festival.tags.length > 0 && (
        <div className="mb-8 flex flex-wrap items-center gap-2">
          <Hash className="text-text-muted-ls h-4 w-4" />
          {festival.tags.map((tag) => (
            <Badge key={tag} variant="secondary" className="text-xs">
              {tag}
            </Badge>
          ))}
        </div>
      )}

      {/* Linked Article */}
      {festival.article_id && (
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
            <a
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
            </a>
          ) : null}
        </div>
      )}

      {/* Share */}
      <div className="flex items-center gap-3">
        <span className="text-text-soft text-sm">Chia sẻ:</span>
        <Button
          variant="outline"
          size="sm"
          onClick={() => {
            if (navigator.share) {
              navigator.share({
                title: festival.name,
                text: festival.short_description,
                url: window.location.href,
              });
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
      <Skeleton className="mb-4 h-6 w-24" />
      <Skeleton className="mb-2 h-10 w-full" />
      <Skeleton className="mb-4 h-10 w-3/4" />
      <Skeleton className="mb-6 h-5 w-full" />
      <div className="mb-6 flex gap-4">
        <Skeleton className="h-4 w-48" />
        <Skeleton className="h-4 w-40" />
      </div>
      <div className="mt-8 space-y-4">
        <Skeleton className="h-4 w-full" />
        <Skeleton className="h-4 w-full" />
        <Skeleton className="h-4 w-5/6" />
      </div>
    </div>
  );
}
