"use client";

import Link from "next/link";
import { usePublicFolkFestivals } from "@/hooks/usePublicContent";
import { ROUTES } from "@/lib/constants";
import { ArrowRight } from "lucide-react";

export function V2FestivalsCard() {
  const { data, isLoading } = usePublicFolkFestivals({ page: 1, limit: 4 });
  const festivals = data?.data ?? [];

  if (isLoading) {
    return (
      <div
        className="mb-5 animate-pulse rounded-xl p-6"
        style={{ background: "var(--v2-bg-card)", border: "1px solid var(--v2-border-primary)" }}
      >
        <div className="h-4 w-40 rounded" style={{ background: "var(--v2-bg-hover)" }} />
        <div className="mt-4 space-y-3">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-14 w-full rounded-xl" style={{ background: "var(--v2-bg-hover)" }} />
          ))}
        </div>
      </div>
    );
  }

  if (festivals.length === 0) return null;

  return (
    <div
      className="v2-card mb-5 rounded-xl p-6"
      style={{
        background: "var(--v2-bg-card)",
        border: "1px solid var(--v2-border-primary)",
        boxShadow: "var(--v2-shadow-xs)",
      }}
    >
      {/* Header */}
      <div className="mb-4 flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <div
            className="flex h-8 w-8 items-center justify-center rounded-lg"
            style={{ background: "var(--v2-bg-gold-soft)", color: "var(--v2-text-gold)" }}
          >
            🐉
          </div>
          <h3 className="text-[16px] font-bold" style={{ color: "var(--v2-text-primary)" }}>
            Lễ hội & Văn hoá
          </h3>
        </div>
        <Link
          href={ROUTES.FESTIVALS}
          className="flex items-center gap-1 text-[12px] font-medium transition-all hover:gap-2"
          style={{ color: "var(--v2-text-accent)" }}
        >
          Xem tất cả <ArrowRight className="h-3 w-3" />
        </Link>
      </div>

      {/* Festival items */}
      <div className="space-y-1.5">
        {festivals.map((festival) => (
          <Link
            key={festival.id}
            href={`${ROUTES.FESTIVALS}/${festival.slug}`}
            className="flex items-center gap-3.5 rounded-xl px-3.5 py-3 transition-all"
            style={{ color: "var(--v2-text-primary)" }}
            onMouseEnter={(e) => {
              (e.currentTarget as HTMLElement).style.background = "var(--v2-bg-hover)";
              (e.currentTarget as HTMLElement).style.transform = "translateX(4px)";
            }}
            onMouseLeave={(e) => {
              (e.currentTarget as HTMLElement).style.background = "transparent";
              (e.currentTarget as HTMLElement).style.transform = "translateX(0)";
            }}
          >
            <div
              className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl text-xl"
              style={{ background: "var(--v2-bg-accent-soft)" }}
            >
              {"🎭"}
            </div>
            <div className="min-w-0 flex-1">
              <h4 className="text-[14px] font-semibold">{festival.name}</h4>
              <p className="truncate text-[12px]" style={{ color: "var(--v2-text-muted)" }}>
                {festival.region || festival.country}
              </p>
            </div>
            {festival.lunar_month && festival.lunar_day && (
              <span
                className="shrink-0 rounded-full px-2.5 py-0.5 text-[11px] font-medium whitespace-nowrap"
                style={{ background: "var(--v2-bg-gold-soft)", color: "var(--v2-text-gold)" }}
              >
                {festival.lunar_day}/{festival.lunar_month} ÂL
              </span>
            )}
          </Link>
        ))}
      </div>
    </div>
  );
}
