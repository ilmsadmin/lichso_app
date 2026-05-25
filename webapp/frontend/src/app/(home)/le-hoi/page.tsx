"use client";

import { useState } from "react";
import Link from "next/link";
import { Search, Sparkles, MapPin, Moon } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { usePublicFolkFestivals } from "@/hooks/usePublicContent";
import type { FolkFestivalListParams, FestivalType } from "@/types/festival";
import { ROUTES } from "@/lib/constants";

const festivalTypeLabels: Record<FestivalType, string> = {
  folk_festival: "Lễ hội dân gian",
  religion: "Tôn giáo",
  national_holiday: "Quốc lễ",
  seasonal: "Theo mùa",
  other: "Khác",
};

const festivalTypeEmojis: Record<FestivalType, string> = {
  folk_festival: "🏮",
  religion: "🙏",
  national_holiday: "🇻�",
  seasonal: "🌸",
  other: "🎊",
};

export default function FestivalsPage() {
  const [params, setParams] = useState<FolkFestivalListParams>({
    page: 1,
    limit: 20,
  });
  const [search, setSearch] = useState("");
  const [selectedType, setSelectedType] = useState<string | null>(null);

  const { data: festivalsData, isLoading } = usePublicFolkFestivals({
    ...params,
    search: search || undefined,
    festival_type: selectedType || undefined,
  });

  const festivals = festivalsData?.data ?? [];
  const meta = festivalsData?.meta;

  return (
    <div className="mx-auto max-w-[1180px] px-4 py-8 sm:px-6 lg:px-7">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-text-dark mb-2 text-3xl font-[var(--font-lora)] font-semibold">
          Lễ Hội Dân Gian
        </h1>
        <p className="text-text-soft">
          Các lễ hội dân gian, phong tục truyền thống Việt Nam qua từng mùa
        </p>
      </div>

      {/* Search & Filter */}
      <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center">
        <div className="relative flex-1">
          <Search className="text-text-muted-ls absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
          <Input
            placeholder="Tìm kiếm lễ hội..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setParams((p) => ({ ...p, page: 1 }));
            }}
            className="pl-10"
          />
        </div>
        <div className="flex flex-wrap gap-2">
          <Button
            variant={selectedType === null ? "default" : "outline"}
            size="sm"
            onClick={() => {
              setSelectedType(null);
              setParams((p) => ({ ...p, page: 1 }));
            }}
            className="rounded-full text-xs"
          >
            Tất cả
          </Button>
          {(Object.keys(festivalTypeLabels) as FestivalType[]).map((type) => (
            <Button
              key={type}
              variant={selectedType === type ? "default" : "outline"}
              size="sm"
              onClick={() => {
                setSelectedType(type);
                setParams((p) => ({ ...p, page: 1 }));
              }}
              className="rounded-full text-xs"
            >
              {festivalTypeEmojis[type]} {festivalTypeLabels[type]}
            </Button>
          ))}
        </div>
      </div>

      {/* Festivals Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <Skeleton key={i} className="h-72 rounded-2xl" />
          ))}
        </div>
      ) : festivals.length === 0 ? (
        <div className="py-16 text-center">
          <Sparkles className="text-text-muted-ls/50 mx-auto mb-4 h-12 w-12" />
          <p className="text-text-soft text-lg">Chưa có lễ hội nào</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {festivals.map((festival) => (
            <Link
              key={festival.id}
              href={`${ROUTES.FESTIVALS}/${festival.slug}`}
              className="group hover:border-warm-amber/30 overflow-hidden rounded-2xl border transition-all hover:shadow-lg"
              style={{
                background: "var(--ls-card-bg-solid)",
                borderColor: "var(--ls-border-warm)",
              }}
            >
              {/* Visual Header */}
              <div className="from-warm-cream to-warm-peach/50 relative flex h-32 items-center justify-center overflow-hidden bg-gradient-to-br">
                <span className="text-5xl opacity-60">
                  {festivalTypeEmojis[festival.festival_type as FestivalType]}
                </span>
                <Badge
                  variant="outline"
                  className="absolute top-3 left-3 bg-white/80 text-[10px] backdrop-blur-sm"
                >
                  {festivalTypeLabels[festival.festival_type as FestivalType]}
                </Badge>
              </div>

              {/* Content */}
              <div className="p-5">
                <h2 className="text-text-dark group-hover:text-warm-amber mb-2 line-clamp-1 text-lg font-semibold transition-colors">
                  {festival.name}
                </h2>

                {/* Date info */}
                <div className="text-text-muted-ls mb-3 flex flex-wrap items-center gap-3 text-xs">
                  {(festival.lunar_month ?? 0) > 0 && (festival.lunar_day ?? 0) > 0 && (
                    <span className="flex items-center gap-1">
                      <Moon className="h-3 w-3" />
                      {festival.lunar_day}/{festival.lunar_month} ÂL
                    </span>
                  )}
                </div>

                {/* Region */}
                {festival.region && (
                  <div className="flex flex-wrap items-center gap-1">
                    <MapPin className="text-text-muted-ls h-3 w-3" />
                    <Badge variant="secondary" className="text-[10px]">
                      {festival.region}
                    </Badge>
                  </div>
                )}
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
