"use client";

import { useState } from "react";
import Link from "next/link";
import { Search, MapPin, Calendar, Crown } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { usePublicFamousPeople } from "@/hooks/usePublicContent";
import { ResponsiveImage } from "@/components/shared/ResponsiveImage";
import type { FamousPersonListParams } from "@/types/famousPerson";
import { ROUTES } from "@/lib/constants";

export default function FamousPeoplePage() {
  const [params, setParams] = useState<FamousPersonListParams>({
    page: 1,
    limit: 20,
  });
  const [search, setSearch] = useState("");
  const [filterVietnamese, setFilterVietnamese] = useState<boolean | undefined>(undefined);

  const { data: peopleData, isLoading } = usePublicFamousPeople({
    ...params,
    search: search || undefined,
    is_vietnamese: filterVietnamese,
  });

  const people = peopleData?.data ?? [];
  const meta = peopleData?.meta;

  function formatLifespan(birth: string | null, death: string | null) {
    const parts: string[] = [];
    if (birth) parts.push(new Date(birth).getFullYear().toString());
    if (death) parts.push(new Date(death).getFullYear().toString());
    return parts.length > 0 ? parts.join(" – ") : null;
  }

  return (
    <div className="mx-auto max-w-[1180px] px-4 py-8 sm:px-6 lg:px-7">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-text-dark mb-2 text-3xl font-[var(--font-lora)] font-semibold">
          Người Nổi Tiếng
        </h1>
        <p className="text-text-soft">
          Các danh nhân, nhân vật lịch sử nổi tiếng Việt Nam và thế giới
        </p>
      </div>

      {/* Search & Filter */}
      <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center">
        <div className="relative flex-1">
          <Search className="text-text-muted-ls absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
          <Input
            placeholder="Tìm kiếm theo tên..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setParams((p) => ({ ...p, page: 1 }));
            }}
            className="pl-10"
          />
        </div>
        <div className="flex gap-2">
          <Button
            variant={filterVietnamese === undefined ? "default" : "outline"}
            size="sm"
            onClick={() => {
              setFilterVietnamese(undefined);
              setParams((p) => ({ ...p, page: 1 }));
            }}
            className="rounded-full text-xs"
          >
            Tất cả
          </Button>
          <Button
            variant={filterVietnamese === true ? "default" : "outline"}
            size="sm"
            onClick={() => {
              setFilterVietnamese(true);
              setParams((p) => ({ ...p, page: 1 }));
            }}
            className="rounded-full text-xs"
          >
            🇻🇳 Việt Nam
          </Button>
          <Button
            variant={filterVietnamese === false ? "default" : "outline"}
            size="sm"
            onClick={() => {
              setFilterVietnamese(false);
              setParams((p) => ({ ...p, page: 1 }));
            }}
            className="rounded-full text-xs"
          >
            🌍 Quốc tế
          </Button>
        </div>
      </div>

      {/* People Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {Array.from({ length: 8 }).map((_, i) => (
            <Skeleton key={i} className="h-64 rounded-2xl" />
          ))}
        </div>
      ) : people.length === 0 ? (
        <div className="py-16 text-center">
          <Crown className="text-text-muted-ls/50 mx-auto mb-4 h-12 w-12" />
          <p className="text-text-soft text-lg">Chưa có nhân vật nào</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {people.map((person) => (
            <Link
              key={person.id}
              href={`${ROUTES.FAMOUS_PEOPLE}/${person.id}`}
              className="group hover:border-warm-amber/30 overflow-hidden rounded-2xl border transition-all hover:shadow-lg"
              style={{
                background: "var(--ls-card-bg-solid)",
                borderColor: "var(--ls-border-warm)",
              }}
            >
              {/* Avatar */}
              <div className="bg-warm-cream/50 relative flex h-48 items-center justify-center overflow-hidden">
                {person.image_url ? (
                  <ResponsiveImage
                    src={person.image_url}
                    alt={person.name}
                    fill
                    sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 25vw"
                    imageClassName="transition-transform group-hover:scale-105"
                  />
                ) : (
                  <span className="text-5xl opacity-30">👤</span>
                )}
                {person.is_vietnamese && (
                  <Badge className="bg-warm-amber absolute top-3 left-3 border-0 text-[10px] text-white">
                    🇻🇳 Việt Nam
                  </Badge>
                )}
              </div>

              {/* Info */}
              <div className="p-4">
                <h2 className="text-text-dark group-hover:text-warm-amber mb-1 line-clamp-1 font-semibold transition-colors">
                  {person.name}
                </h2>
                {formatLifespan(person.birth_date, person.death_date) && (
                  <p className="text-text-muted-ls mb-2 flex items-center gap-1 text-xs">
                    <Calendar className="h-3 w-3" />
                    {formatLifespan(person.birth_date, person.death_date)}
                  </p>
                )}
                {person.nationality && (
                  <p className="text-text-soft mb-2 flex items-center gap-1 text-xs">
                    <MapPin className="h-3 w-3" />
                    {person.nationality}
                  </p>
                )}
                <p className="text-text-soft line-clamp-2 text-sm">{person.short_bio}</p>
                {person.category && (
                  <div className="mt-3 flex flex-wrap gap-1">
                    <Badge variant="secondary" className="text-[10px]">
                      {person.category}
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
