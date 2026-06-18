"use client";

import { Suspense, lazy, useMemo, useState, useCallback } from "react";
import { useCalendarToday, useCalendarDate } from "@/hooks/useCalendar";
import type { DayResponse } from "@/types/calendar";

// Direct imports — critical path
import { DailyBlocSheet } from "@/components/lichso/DailyBlocSheet";
import { MonthCalendar } from "@/components/lichso/MonthCalendar";
import { V2QuoteCard } from "@/components/lichso/V2QuoteCard";

// Lazy imports — non-critical
const CultureSection = lazy(() =>
  import("@/components/lichso/CultureSection").then((m) => ({ default: m.CultureSection }))
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

function pad(n: number) {
  return String(n).padStart(2, "0");
}

/** Cộng/trừ ngày trên chuỗi YYYY-MM-DD (an toàn theo local time). */
function shiftDate(dateStr: string, delta: number): string {
  const [y, m, d] = dateStr.split("-").map(Number);
  const dt = new Date(y, m - 1, d + delta);
  return `${dt.getFullYear()}-${pad(dt.getMonth() + 1)}-${pad(dt.getDate())}`;
}

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

  // Ngày hôm nay (chuỗi) — ưu tiên từ data, fallback đồng hồ máy
  const todayStr = useMemo(() => {
    if (todayData) {
      return `${todayData.solar_year}-${pad(todayData.solar_month)}-${pad(todayData.solar_day)}`;
    }
    const now = new Date();
    return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
  }, [todayData]);

  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const activeDate = selectedDate ?? todayStr;
  const isToday = activeDate === todayStr;

  // Khi xem ngày khác hôm nay → fetch riêng; ngày hôm nay dùng todayData
  const { data: otherDayData, isFetching: otherFetching } = useCalendarDate(activeDate, !isToday);
  const dayData: DayResponse | undefined = isToday ? todayData : otherDayData;

  const goPrev = useCallback(() => setSelectedDate(shiftDate(activeDate, -1)), [activeDate]);
  const goNext = useCallback(() => setSelectedDate(shiftDate(activeDate, 1)), [activeDate]);
  const goToday = useCallback(() => setSelectedDate(null), []);

  return (
    <div style={{ background: "var(--v2-bg-primary)" }} className="min-h-screen">
      <div className="mx-auto max-w-[1400px] px-4 py-7 sm:px-8">
        {/* Error state */}
        {error && !todayData && (
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
          <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_380px]">
            <div
              className="h-[520px] animate-pulse rounded-[26px]"
              style={{ background: "var(--v2-bg-accent-soft)" }}
            />
            <div
              className="h-[420px] animate-pulse rounded-2xl"
              style={{ background: "var(--v2-bg-hover)" }}
            />
          </div>
        )}

        {(dayData || todayData) && (
          <>
            {/* ═══════ HÀNG CHÍNH: Tờ lịch ngày × Lịch tháng ═══════ */}
            <div className="grid items-start gap-6 lg:grid-cols-[minmax(0,1fr)_380px]">
              {/* Tờ lịch ngày — centerpiece */}
              <div className="relative" style={{ opacity: !isToday && otherFetching ? 0.6 : 1 }}>
                {dayData ? (
                  <DailyBlocSheet
                    data={dayData}
                    isToday={isToday}
                    onPrevDay={goPrev}
                    onNextDay={goNext}
                    onToday={goToday}
                  />
                ) : (
                  <div
                    className="h-[520px] animate-pulse rounded-[26px]"
                    style={{ background: "var(--v2-bg-accent-soft)" }}
                  />
                )}
              </div>

              {/* Cột phải: Lịch tháng + tiết khí + tìm kiếm + nhắc nhở */}
              <aside className="flex flex-col gap-5">
                <MonthCalendar selectedDate={activeDate} onSelectDate={setSelectedDate} />
                {dayData?.tiet_khi && (
                  <Suspense fallback={<CardSkeleton />}>
                    <V2SolarTermWidget solarTerm={dayData.tiet_khi} />
                  </Suspense>
                )}
                <Suspense fallback={<CardSkeleton />}>
                  <V2QuickSearch />
                </Suspense>
                <Suspense fallback={<CardSkeleton />}>
                  <V2RemindersWidget />
                </Suspense>
              </aside>
            </div>

            {/* Slogan */}
            <div className="relative my-7 text-center">
              <p
                className="text-[13px] font-medium tracking-wider"
                style={{ color: "var(--v2-text-muted)" }}
              >
                <span className="font-semibold" style={{ color: "var(--v2-text-accent)" }}>
                  Lịch Số
                </span>{" "}
                — Nơi truyền thống gặp gỡ công nghệ, giữ hồn Việt trong thời đại số
              </p>
            </div>

            {/* Danh ngôn hôm nay — dải toàn chiều rộng */}
            <V2QuoteCard />

            {/* ═══════ ĐẶC TRƯNG VĂN HÓA VIỆT ═══════ */}
            <Suspense fallback={<CardSkeleton />}>
              <CultureSection />
            </Suspense>
          </>
        )}
      </div>
    </div>
  );
}
