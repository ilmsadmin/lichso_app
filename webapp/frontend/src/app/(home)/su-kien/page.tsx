"use client";

import { useState } from "react";
import Link from "next/link";
import { Search, Calendar, MapPin, AlertCircle, Flag, Globe, Landmark } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { usePublicEvents } from "@/hooks/usePublicContent";
import type { EventListParams, EventType, EventImportance } from "@/types/event";
import { ROUTES } from "@/lib/constants";

const eventTypeLabels: Record<EventType, string> = {
  historical_event: "Lịch sử",
  national_day: "Ngày quốc gia",
  world_day: "Ngày quốc tế",
  anniversary: "Kỷ niệm",
  cultural: "Văn hóa",
  military: "Quân sự",
};

const eventTypeIcons: Record<EventType, string> = {
  historical_event: "📜",
  national_day: "🇻🇳",
  world_day: "🌍",
  anniversary: "🎉",
  cultural: "🎭",
  military: "⚔️",
};

const importanceBadge: Record<EventImportance, { label: string; className: string }> = {
  high: { label: "Quan trọng", className: "bg-red-100 text-red-700 border-red-200" },
  medium: { label: "Trung bình", className: "bg-yellow-100 text-yellow-700 border-yellow-200" },
  low: { label: "Thường", className: "bg-gray-100 text-gray-600 border-gray-200" },
};

export default function EventsPage() {
  const [params, setParams] = useState<EventListParams>({
    page: 1,
    limit: 20,
  });
  const [search, setSearch] = useState("");
  const [selectedType, setSelectedType] = useState<string | null>(null);

  const { data: eventsData, isLoading } = usePublicEvents({
    ...params,
    search: search || undefined,
    event_type: selectedType || undefined,
  });

  const events = eventsData?.data ?? [];
  const meta = eventsData?.meta;

  return (
    <div className="mx-auto max-w-[1180px] px-4 py-8 sm:px-6 lg:px-7">
      {/* Header */}
      <div className="mb-8">
        <div className="bg-warm-amber/10 text-warm-amber mb-4 inline-flex h-12 w-12 items-center justify-center rounded-2xl">
          <Landmark className="h-6 w-6" />
        </div>
        <h1 className="text-text-dark mb-2 text-3xl font-[var(--font-lora)] font-semibold">
          Sự Kiện Lịch Sử
        </h1>
        <p className="text-text-soft">
          Các sự kiện lịch sử quan trọng, ngày lễ quốc gia và quốc tế
        </p>
      </div>

      {/* Search & Filter */}
      <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center">
        <div className="relative flex-1">
          <Search className="text-text-muted-ls absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
          <Input
            placeholder="Tìm kiếm sự kiện..."
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
          {(Object.keys(eventTypeLabels) as EventType[]).map((type) => (
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
              {eventTypeIcons[type]} {eventTypeLabels[type]}
            </Button>
          ))}
        </div>
      </div>

      {/* Events List */}
      {isLoading ? (
        <div className="space-y-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <Skeleton key={i} className="h-28 rounded-2xl" />
          ))}
        </div>
      ) : events.length === 0 ? (
        <div className="py-16 text-center">
          <Landmark className="text-text-muted-ls/50 mx-auto mb-4 h-12 w-12" />
          <p className="text-text-soft text-lg">Chưa có sự kiện nào</p>
        </div>
      ) : (
        <div className="space-y-3">
          {events.map((event) => (
            <Link
              key={event.id}
              href={`${ROUTES.EVENTS}/${event.slug}`}
              className="group hover:border-warm-amber/30 flex items-start gap-4 rounded-2xl border p-5 transition-all hover:shadow-md"
              style={{
                background: "var(--ls-card-bg-solid)",
                borderColor: "var(--ls-border-warm)",
              }}
            >
              {/* Date Badge */}
              <div className="bg-warm-cream/80 border-warm-amber/10 flex h-16 w-16 flex-shrink-0 flex-col items-center justify-center rounded-xl border text-center">
                <span className="text-warm-amber text-lg font-bold">
                  {(() => {
                    const d = new Date(event.event_date);
                    return isNaN(d.getTime()) ? "—" : d.getDate();
                  })()}
                </span>
                <span className="text-text-soft text-[10px] uppercase">
                  {(() => {
                    const d = new Date(event.event_date);
                    return isNaN(d.getTime())
                      ? ""
                      : d.toLocaleDateString("vi-VN", { month: "short" });
                  })()}
                </span>
              </div>

              {/* Content */}
              <div className="min-w-0 flex-1">
                <div className="mb-1 flex flex-wrap items-center gap-2">
                  <span className="text-sm">{eventTypeIcons[event.event_type]}</span>
                  <Badge variant="outline" className="text-[10px]">
                    {eventTypeLabels[event.event_type]}
                  </Badge>
                  <Badge
                    variant="outline"
                    className={`text-[10px] ${importanceBadge[event.importance].className}`}
                  >
                    {importanceBadge[event.importance].label}
                  </Badge>
                </div>
                <h2 className="text-text-dark group-hover:text-warm-amber mb-1 line-clamp-1 font-semibold transition-colors">
                  {event.title}
                </h2>
                <div className="text-text-muted-ls flex items-center gap-3 text-xs">
                  <span className="flex items-center gap-1">
                    <Calendar className="h-3 w-3" />
                    {(() => {
                      const d = new Date(event.event_date);
                      return isNaN(d.getTime())
                        ? event.event_date
                        : d.toLocaleDateString("vi-VN", {
                            year: "numeric",
                            month: "long",
                            day: "numeric",
                          });
                    })()}
                  </span>
                  {event.country && (
                    <span className="flex items-center gap-1">
                      <MapPin className="h-3 w-3" />
                      {event.country}
                    </span>
                  )}
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
