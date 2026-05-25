"use client";

import Link from "next/link";
import { ArrowLeft, Calendar, MapPin, Hash, Share2 } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { Separator } from "@/components/ui/separator";
import { usePublicFamousPerson } from "@/hooks/usePublicContent";
import { ResponsiveImage } from "@/components/shared/ResponsiveImage";
import { AdminEditButton } from "@/components/shared/AdminEditButton";
import { ROUTES } from "@/lib/constants";

export default function FamousPersonClient({ id }: { id: string }) {
  const { data, isLoading, error } = usePublicFamousPerson(id);
  const person = data?.data;

  if (isLoading) return <DetailSkeleton />;

  if (error || !person) {
    return (
      <div className="mx-auto max-w-[800px] px-4 py-16 text-center">
        <p className="mb-4 text-5xl">👤</p>
        <h1 className="text-text-dark mb-2 text-2xl font-semibold">Không tìm thấy nhân vật</h1>
        <p className="text-text-soft mb-6">Nhân vật bạn tìm kiếm không tồn tại hoặc đã bị xóa.</p>
        <Button asChild>
          <Link href={ROUTES.FAMOUS_PEOPLE}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            Quay lại danh sách
          </Link>
        </Button>
      </div>
    );
  }

  const lifespan = [
    person.birth_date
      ? new Date(person.birth_date).toLocaleDateString("vi-VN", {
          year: "numeric",
          month: "long",
          day: "numeric",
        })
      : null,
    person.death_date
      ? new Date(person.death_date).toLocaleDateString("vi-VN", {
          year: "numeric",
          month: "long",
          day: "numeric",
        })
      : null,
  ]
    .filter(Boolean)
    .join(" — ");

  return (
    <div className="mx-auto max-w-[800px] px-4 py-8 sm:px-6">
      <AdminEditButton href={`/admin/famous-people/${person.id}/edit`} label="Sửa nhân vật" />
      {/* Breadcrumb nav */}
      <div className="mb-6 flex items-center gap-2 text-sm">
        <Link
          href={ROUTES.FAMOUS_PEOPLE}
          className="text-text-soft hover:text-warm-amber inline-flex items-center gap-1.5 transition-colors"
        >
          <ArrowLeft className="h-3.5 w-3.5" />
          Tất cả nhân vật
        </Link>
        {person.category && (
          <>
            <span className="text-text-muted-ls">/</span>
            <Badge variant="outline" className="border-warm-amber/30 text-warm-amber">
              {person.category}
            </Badge>
          </>
        )}
      </div>

      {/* Person Header */}
      <div className="mb-8 flex flex-col gap-6 sm:flex-row">
        {/* Avatar */}
        <div className="bg-warm-cream/50 relative mx-auto h-40 w-40 flex-shrink-0 overflow-hidden rounded-2xl sm:mx-0">
          {person.image_url ? (
            <ResponsiveImage src={person.image_url} alt={person.name} fill sizes="160px" priority />
          ) : (
            <div className="flex h-full items-center justify-center">
              <span className="text-6xl opacity-30">👤</span>
            </div>
          )}
        </div>

        {/* Info */}
        <div className="flex-1 text-center sm:text-left">
          <div className="mb-2 flex flex-wrap items-center justify-center gap-2 sm:justify-start">
            {person.is_vietnamese && (
              <Badge className="bg-warm-amber border-0 text-xs text-white">🇻🇳 Việt Nam</Badge>
            )}
            {person.category && (
              <Badge variant="secondary" className="text-xs">
                {person.category}
              </Badge>
            )}
          </div>

          <h1 className="text-text-dark mb-3 text-3xl font-[var(--font-lora)] font-bold">
            {person.name}
          </h1>

          {person.short_bio && <p className="text-text-soft mb-3 text-lg">{person.short_bio}</p>}

          <div className="text-text-muted-ls flex flex-wrap items-center justify-center gap-4 text-sm sm:justify-start">
            {lifespan && (
              <span className="flex items-center gap-1.5">
                <Calendar className="h-4 w-4" />
                {lifespan}
              </span>
            )}
            {person.nationality && (
              <span className="flex items-center gap-1.5">
                <MapPin className="h-4 w-4" />
                {person.nationality}
              </span>
            )}
          </div>
        </div>
      </div>

      <Separator className="mb-8" />

      {/* Images Gallery - removed: backend doesn't have images array */}

      {/* Tags */}
      {person.tags && person.tags.length > 0 && (
        <div className="mb-8 flex flex-wrap items-center gap-2">
          <Hash className="text-text-muted-ls h-4 w-4" />
          {person.tags.map((tag) => (
            <Badge key={tag} variant="secondary" className="text-xs">
              {tag}
            </Badge>
          ))}
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
                title: person.name,
                text: person.short_bio,
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
    </div>
  );
}

function DetailSkeleton() {
  return (
    <div className="mx-auto max-w-[800px] px-4 py-8 sm:px-6">
      <Skeleton className="mb-6 h-4 w-32" />
      <div className="mb-8 flex flex-col gap-6 sm:flex-row">
        <Skeleton className="mx-auto h-40 w-40 rounded-2xl sm:mx-0" />
        <div className="flex-1 space-y-3">
          <Skeleton className="h-6 w-24" />
          <Skeleton className="h-8 w-64" />
          <Skeleton className="h-5 w-full" />
          <Skeleton className="h-4 w-48" />
        </div>
      </div>
      <div className="space-y-4">
        <Skeleton className="h-4 w-full" />
        <Skeleton className="h-4 w-full" />
        <Skeleton className="h-4 w-5/6" />
        <Skeleton className="h-4 w-full" />
      </div>
    </div>
  );
}
