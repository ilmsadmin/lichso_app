"use client";

import { Suspense, lazy } from "react";
import { useCalendarToday } from "@/hooks/useCalendar";
import type { DayResponse } from "@/types/calendar";

// Direct imports — critical path
import { V2Hero } from "@/components/lichso/V2Hero";
import { V2QuoteCard } from "@/components/lichso/V2QuoteCard";
import { V2MiniCalendar } from "@/components/lichso/V2MiniCalendar";

// Lazy imports — non-critical
const V2HistoryCard = lazy(() =>
  import("@/components/lichso/V2HistoryCard").then((m) => ({ default: m.V2HistoryCard }))
);
const V2FestivalsCard = lazy(() =>
  import("@/components/lichso/V2FestivalsCard").then((m) => ({ default: m.V2FestivalsCard }))
);
const V2SolarTermWidget = lazy(() =>
  import("@/components/lichso/V2SolarTermWidget").then((m) => ({ default: m.V2SolarTermWidget }))
);
const V2QuickSearch = lazy(() =>
  import("@/components/lichso/V2QuickSearch").then((m) => ({ default: m.V2QuickSearch }))
);
const V2RemindersWidget = lazy(() =>
  import("@/components/lichso/V2RemindersWidget").then((m) => ({ default: m.V2RemindersWidget }))
);

function CardSkeleton() {
  return (
    <div
      className="animate-pulse rounded-xl p-6"
      style={{
        background: "var(--v2-bg-card)",
        border: "1px solid var(--v2-border-primary)",
      }}
    >
      <div className="h-4 w-40 rounded" style={{ background: "var(--v2-bg-hover)" }} />
      <div className="mt-4 h-16 w-full rounded" style={{ background: "var(--v2-bg-hover)" }} />
    </div>
  );
}

export default function V2HomeClient({
  initialTodayData,
}: {
  initialTodayData?: DayResponse | null;
}) {
  const { data: todayData, isLoading, error } = useCalendarToday(initialTodayData ?? undefined);

  return (
    <div style={{ background: "var(--v2-bg-primary)" }} className="min-h-screen">
      <div className="mx-auto max-w-[1400px] px-4 py-7 sm:px-8">
        {/* Error state */}
        {error && (
          <div
            className="mb-6 rounded-xl p-8 text-center"
            style={{
              background: "var(--v2-bg-card)",
              border: "1px solid var(--v2-border-primary)",
            }}
          >
            <p className="mb-2 text-lg" style={{ color: "var(--v2-text-secondary)" }}>
              ⚠ Không thể kết nối đến máy chủ
            </p>
            <p className="text-sm" style={{ color: "var(--v2-text-muted)" }}>
              Vui lòng kiểm tra kết nối mạng hoặc khởi động backend server.
            </p>
          </div>
        )}

        {/* Loading */}
        {isLoading && !todayData && (
          <div className="space-y-6">
            <div
              className="h-48 animate-pulse rounded-[28px]"
              style={{ background: "var(--v2-bg-accent-soft)" }}
            />
            <div className="grid gap-6 lg:grid-cols-[1fr_340px]">
              <div className="space-y-5">
                <CardSkeleton />
                <CardSkeleton />
              </div>
              <div className="space-y-5">
                <CardSkeleton />
                <CardSkeleton />
              </div>
            </div>
          </div>
        )}

        {todayData && (
          <>
            {/* ═══════ HERO ═══════ */}
            <V2Hero data={todayData} />

            {/* Slogan */}
            <div className="relative mb-6 text-center">
              <p className="text-[13px] font-medium tracking-wider" style={{ color: "var(--v2-text-muted)" }}>
                <span className="font-semibold" style={{ color: "var(--v2-text-accent)" }}>
                  Lịch Số
                </span>
                {" "}— Nơi truyền thống gặp gỡ công nghệ, giữ hồn Việt trong thời đại số
              </p>
            </div>

            {/* ═══════ CONTENT GRID ═══════ */}
            <div className="grid gap-6 lg:grid-cols-[1fr_340px]">
              {/* Main content */}
              <div>
                {/* Quote */}
                <V2QuoteCard />

                {/* History events */}
                <Suspense fallback={<CardSkeleton />}>
                  <V2HistoryCard
                    month={todayData.solar_month}
                    day={todayData.solar_day}
                    lunarMonth={todayData.lunar_month}
                    lunarDay={todayData.lunar_day}
                  />
                </Suspense>

                {/* Festivals */}
                <Suspense fallback={<CardSkeleton />}>
                  <V2FestivalsCard />
                </Suspense>

                {/* Ornament */}
                <div
                  className="my-3 text-center text-[12px] opacity-30"
                  style={{ color: "var(--v2-text-muted)", letterSpacing: "10px" }}
                >
                  ◆ ◆ ◆
                </div>
              </div>

              {/* ═══════ SIDEBAR ═══════ */}
              <aside className="flex flex-col gap-5">
                {/* Mini Calendar */}
                <V2MiniCalendar />

                {/* Solar Term */}
                <Suspense fallback={<CardSkeleton />}>
                  <V2SolarTermWidget solarTerm={todayData.tiet_khi} />
                </Suspense>

                {/* Quick Search */}
                <Suspense fallback={<CardSkeleton />}>
                  <V2QuickSearch />
                </Suspense>

                {/* Reminders */}
                <Suspense fallback={<CardSkeleton />}>
                  <V2RemindersWidget />
                </Suspense>
              </aside>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
