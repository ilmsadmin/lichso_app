"use client";

import { useState, useCallback, useMemo, lazy, Suspense } from "react";
import { useCalendarToday, useCalendarDate } from "@/hooks/useCalendar";
import { BackgroundLayer } from "@/components/lichso/BackgroundLayer";
import { DateMainCard } from "@/components/lichso/DateMainCard";
import { FloatingMenu, type MenuPanel } from "@/components/lichso/FloatingMenu";
import { FloatingPanel } from "@/components/lichso/FloatingPanel";
import { Skeleton } from "@/components/ui/skeleton";

// Lazy-load heavy panels — only load JS when user opens them
const InfoPanel = lazy(() => import("@/components/lichso/InfoPanel").then(m => ({ default: m.InfoPanel })));
const CalendarGrid = lazy(() => import("@/components/lichso/CalendarGrid").then(m => ({ default: m.CalendarGrid })));
const SearchBar = lazy(() => import("@/components/lichso/SearchBar").then(m => ({ default: m.SearchBar })));
const GoodDaysTab = lazy(() => import("@/components/lichso/GoodDaysTab").then(m => ({ default: m.GoodDaysTab })));
const SolarTermsTab = lazy(() => import("@/components/lichso/SolarTermsTab").then(m => ({ default: m.SolarTermsTab })));
const ConvertTab = lazy(() => import("@/components/lichso/ConvertTab").then(m => ({ default: m.ConvertTab })));
const ReminderPanel = lazy(() => import("@/components/lichso/ReminderPanel").then(m => ({ default: m.ReminderPanel })));
const ExportPanel = lazy(() => import("@/components/lichso/ExportPanel").then(m => ({ default: m.ExportPanel })));
const CountdownWidget = lazy(() => import("@/components/lichso/CountdownWidget").then(m => ({ default: m.CountdownWidget })));
const DayDetailModal = lazy(() => import("@/components/lichso/DayDetailModal").then(m => ({ default: m.DayDetailModal })));

/** Helper: format Date to YYYY-MM-DD */
function toDateStr(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

/** Helper: add/subtract days from a date string */
function shiftDate(dateStr: string, days: number): string {
  const d = new Date(dateStr + "T00:00:00");
  d.setDate(d.getDate() + days);
  return toDateStr(d);
}

function LoadingSkeleton() {
  return (
    <div className="flex items-center justify-center lg:min-h-[60vh]">
      <div
        className="w-full max-w-[400px] rounded-[28px] p-8"
        style={{
          background: "var(--ls-card-bg-solid)",
          border: "1.5px solid var(--ls-border-warm)",
          boxShadow: "0 0 0 6px var(--ls-card-ring), 0 24px 80px var(--ls-shadow-deep)",
        }}
      >
        <Skeleton className="mb-4 h-3 w-20" />
        <Skeleton className="mb-3 h-24 w-28" />
        <Skeleton className="mb-8 h-5 w-48" />
        <Skeleton className="mb-6 h-px w-full" />
        <Skeleton className="mb-4 h-3 w-16" />
        <Skeleton className="mb-2 h-7 w-40" />
        <Skeleton className="mb-5 h-4 w-52" />
        <div className="mb-5 flex gap-2">
          {[1, 2, 3, 4].map((i) => (
            <Skeleton key={i} className="h-7 w-24 rounded-full" />
          ))}
        </div>
        <Skeleton className="h-2 w-full" />
      </div>
    </div>
  );
}

export default function HomeClient({ initialTodayData }: { initialTodayData?: import("@/types/calendar").DayResponse | null }) {
  const { data: todayData, isLoading, error } = useCalendarToday(initialTodayData ?? undefined);
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [activePanel, setActivePanel] = useState<MenuPanel>(null);

  // Track the "main card" date — null means "today"
  const [mainCardDate, setMainCardDate] = useState<string | null>(null);
  const todayStr = useMemo(() => toDateStr(new Date()), []);
  const isViewingOtherDate = mainCardDate !== null && mainCardDate !== todayStr;

  const { data: dateData } = useCalendarDate(mainCardDate ?? "", isViewingOtherDate);

  const mainCardData = isViewingOtherDate ? dateData : todayData;

  const handlePrevDay = useCallback(() => {
    setMainCardDate((prev) => shiftDate(prev ?? todayStr, -1));
  }, [todayStr]);

  const handleNextDay = useCallback(() => {
    setMainCardDate((prev) => shiftDate(prev ?? todayStr, 1));
  }, [todayStr]);

  const handleDateSelect = useCallback((day: number, month: number, year: number) => {
    const dateStr = `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
    setSelectedDate(dateStr);
  }, []);

  const handleSearch = useCallback((query: string) => {
    const dateMatch = query.match(/^(\d{1,2})[\/\-\.](\d{1,2})[\/\-\.](\d{4})$/);
    if (dateMatch) {
      const [, d, m, y] = dateMatch;
      setSelectedDate(`${y}-${m.padStart(2, "0")}-${d.padStart(2, "0")}`);
      setActivePanel(null);
      return;
    }
    const isoMatch = query.match(/^(\d{4})[\/\-\.](\d{1,2})[\/\-\.](\d{1,2})$/);
    if (isoMatch) {
      const [, y, m, d] = isoMatch;
      setSelectedDate(`${y}-${m.padStart(2, "0")}-${d.padStart(2, "0")}`);
      setActivePanel(null);
      return;
    }
    const dayOnly = parseInt(query);
    if (dayOnly >= 1 && dayOnly <= 31) {
      const now = new Date();
      setSelectedDate(
        `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(dayOnly).padStart(2, "0")}`
      );
      setActivePanel(null);
    }
  }, []);

  const handlePanelChange = useCallback((panel: MenuPanel) => {
    setActivePanel(panel);
  }, []);

  const closePanel = useCallback(() => {
    setActivePanel(null);
  }, []);

  /* Determine panel side */
  const panelSide = ((): "left" | "right" => {
    if (activePanel === "calendar" || activePanel === "compass" || activePanel === "good-days")
      return "left";
    return "right";
  })();

  return (
    <>
      <BackgroundLayer />

      {/* ───── Main calendar container ───── */}
      <div className="relative z-[1] flex min-h-[calc(100svh-72px)] flex-col items-center justify-center px-5 py-6 sm:py-10">
        {/* Error state */}
        {error && (
          <div
            className="mb-6 w-full max-w-md rounded-2xl p-8 text-center"
            style={{
              background: "var(--ls-card-bg)",
              border: "1px solid var(--ls-border-warm)",
            }}
          >
            <p className="text-text-mid mb-2 text-lg">⚠ Không thể kết nối đến máy chủ</p>
            <p className="text-text-soft text-sm">
              Vui lòng kiểm tra kết nối mạng hoặc khởi động backend server.
            </p>
          </div>
        )}

        {/* Loading */}
        {isLoading && <LoadingSkeleton />}

        {/* ── The Main Day Card — hero center ── */}
        {mainCardData && (
          <div className="relative w-full max-w-[400px] animate-[fadeIn_0.8s_cubic-bezier(0.34,1.56,0.64,1)_both]">
            <DateMainCard data={mainCardData} onPrevDay={handlePrevDay} onNextDay={handleNextDay} />

            {/* ── Mobile: grid menu below card ── */}
            <div className="lg:hidden">
              <FloatingMenu activePanel={activePanel} onPanelChange={handlePanelChange} />
            </div>

            {/* "Back to today" pill */}
            {isViewingOtherDate && (
              <div className="mt-4 flex justify-center">
                <button
                  onClick={() => setMainCardDate(null)}
                  className="text-warm-amber rounded-full px-5 py-2 text-xs font-medium backdrop-blur-xl transition-all hover:scale-105 active:scale-95"
                  style={{
                    background: "var(--ls-card-bg)",
                    border: "1px solid var(--ls-border-warm)",
                    boxShadow: "0 4px 16px var(--ls-shadow-warm)",
                  }}
                >
                  ↩ Quay về hôm nay
                </button>
              </div>
            )}
          </div>
        )}

        {/* ── Floating panels — lazy loaded ── */}
        <Suspense fallback={null}>
        {activePanel === "info" && mainCardData && (
          <FloatingPanel panel={activePanel} onClose={closePanel} side="right">
            <div className="text-text-muted-ls mb-3 text-[10px] tracking-[2.5px] uppercase">
              ✦ Thông tin chi tiết
            </div>
            <InfoPanel data={mainCardData} />
          </FloatingPanel>
        )}

        {activePanel === "calendar" && (
          <FloatingPanel panel={activePanel} onClose={closePanel} side="left">
            <CalendarGrid onDateSelect={handleDateSelect} />
            <CountdownWidget />
          </FloatingPanel>
        )}

        {activePanel === "activities" && mainCardData && (
          <FloatingPanel panel={activePanel} onClose={closePanel} side="right">
            <div className="text-text-muted-ls mb-3 text-[10px] tracking-[2.5px] uppercase">
              📋 Việc nên & không nên
            </div>
            <ActivitiesContent data={mainCardData} />
          </FloatingPanel>
        )}

        {activePanel === "compass" && mainCardData && (
          <FloatingPanel panel={activePanel} onClose={closePanel} side="left">
            <div className="text-text-muted-ls mb-3 text-[10px] tracking-[2.5px] uppercase">
              🧭 Hướng xuất hành
            </div>
            <CompassContent data={mainCardData} />
          </FloatingPanel>
        )}

        {activePanel === "search" && (
          <FloatingPanel panel={activePanel} onClose={closePanel} side="right">
            <div className="text-text-muted-ls mb-3 text-[10px] tracking-[2.5px] uppercase">
              🔍 Tra cứu ngày
            </div>
            <SearchBar onSearch={handleSearch} />
          </FloatingPanel>
        )}

        {activePanel === "good-days" && (
          <FloatingPanel panel={activePanel} onClose={closePanel} side="left">
            <GoodDaysTab />
          </FloatingPanel>
        )}

        {activePanel === "solar-terms" && (
          <FloatingPanel panel={activePanel} onClose={closePanel} side="left">
            <SolarTermsTab />
          </FloatingPanel>
        )}

        {activePanel === "convert" && (
          <FloatingPanel panel={activePanel} onClose={closePanel} side="right">
            <ConvertTab />
          </FloatingPanel>
        )}

        {activePanel === "bookmarks" && mainCardData && (
          <FloatingPanel panel={activePanel} onClose={closePanel} side="right">
            <div className="text-text-muted-ls mb-3 text-[10px] tracking-[2.5px] uppercase">
              ★ Bookmark
            </div>
            <BookmarkListContent />
          </FloatingPanel>
        )}

        {activePanel === "reminders" && (
          <FloatingPanel panel={activePanel} onClose={closePanel} side="right">
            <ReminderPanel />
          </FloatingPanel>
        )}

        {activePanel === "export" && (
          <FloatingPanel panel={activePanel} onClose={closePanel} side="right">
            <ExportPanel />
          </FloatingPanel>
        )}
        </Suspense>
      </div>

      {/* ── Desktop: Floating bubble field (viewport-level, fixed position) ── */}
      {mainCardData && (
        <div className="hidden lg:block">
          <FloatingMenu activePanel={activePanel} onPanelChange={handlePanelChange} />
        </div>
      )}

      {/* Day Detail Modal */}
      <Suspense fallback={null}>
        {selectedDate && <DayDetailModal date={selectedDate} onClose={() => setSelectedDate(null)} />}
      </Suspense>
    </>
  );
}

/* ─────────────────────────────────────────
   Inline panel content components
   ───────────────────────────────────────── */
import type { DayResponse } from "@/types/calendar";
import { useBookmarks, useDeleteBookmark } from "@/hooks/useBookmarks";
import { useAuthStore } from "@/stores/authStore";
import { toast } from "sonner";

function ActivitiesContent({ data }: { data: DayResponse }) {
  const { viec_nen, viec_khong } = data.phong_thuy;
  return (
    <div className="space-y-3">
      <div>
        <div className="text-jade-teal mb-2 text-[11px] font-medium tracking-wide">✦ Nên làm</div>
        <ul className="space-y-1.5">
          {viec_nen.map((v) => (
            <li key={v} className="text-text-mid flex items-center gap-2 text-[13px]">
              <div className="bg-jade-soft h-1.5 w-1.5 shrink-0 rounded-full" />
              {v}
            </li>
          ))}
        </ul>
      </div>
      <div className="h-px" style={{ background: "var(--ls-border-soft)" }} />
      <div>
        <div className="text-danger mb-2 text-[11px] font-medium tracking-wide">⚠ Không nên</div>
        <ul className="space-y-1.5">
          {viec_khong.map((v) => (
            <li key={v} className="text-text-muted-ls flex items-center gap-2 text-[13px]">
              <div className="bg-danger h-1.5 w-1.5 shrink-0 rounded-full opacity-60" />
              {v}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

function CompassContent({ data }: { data: DayResponse }) {
  const huong = data.phong_thuy.huong_xuat_hanh;
  return (
    <div className="space-y-4">
      <div className="flex flex-col items-center gap-3">
        <svg className="h-28 w-28" viewBox="0 0 96 96">
          <circle
            cx="48"
            cy="48"
            r="42"
            fill="none"
            stroke="rgba(74,139,127,0.2)"
            strokeWidth="1.2"
          />
          <circle
            cx="48"
            cy="48"
            r="30"
            fill="none"
            stroke="rgba(196,120,58,0.15)"
            strokeWidth="1"
          />
          <circle cx="48" cy="48" r="4" fill="rgba(196,120,58,0.8)" />
          <circle cx="48" cy="48" r="2" fill="var(--warm-amber)" />
          <text
            x="48"
            y="11"
            textAnchor="middle"
            fill="var(--ls-text-soft)"
            fontSize="9"
            fontFamily="serif"
          >
            B
          </text>
          <text
            x="48"
            y="90"
            textAnchor="middle"
            fill="var(--ls-text-muted)"
            fontSize="9"
            fontFamily="serif"
          >
            N
          </text>
          <text
            x="88"
            y="52"
            textAnchor="middle"
            fill="var(--ls-text-muted)"
            fontSize="9"
            fontFamily="serif"
          >
            Đ
          </text>
          <text
            x="8"
            y="52"
            textAnchor="middle"
            fill="var(--ls-text-muted)"
            fontSize="9"
            fontFamily="serif"
          >
            T
          </text>
          <line
            x1="48"
            y1="48"
            x2="72"
            y2="22"
            stroke="var(--jade-soft)"
            strokeWidth="2.5"
            strokeLinecap="round"
          />
          <polygon points="72,22 64,28 76,34" fill="var(--jade-soft)" />
          <line
            x1="48"
            y1="48"
            x2="48"
            y2="78"
            stroke="rgba(74,139,127,0.35)"
            strokeWidth="1.5"
            strokeLinecap="round"
          />
          <line
            x1="48"
            y1="48"
            x2="18"
            y2="48"
            stroke="rgba(192,96,96,0.4)"
            strokeWidth="1.5"
            strokeDasharray="3,2"
            strokeLinecap="round"
          />
        </svg>
        <div className="text-center">
          <div className="text-jade-teal mb-1 text-[14px] font-medium">
            {huong.huong_tot.join(" · ")}
          </div>
          <div className="text-text-soft text-[12px]">
            Tài thần: {huong.tai_than} · Hỷ thần: {huong.hy_than}
          </div>
          <div className="text-danger mt-1 text-[11px] opacity-70">
            Tránh: {huong.huong_xau.join(", ")}
          </div>
        </div>
      </div>
    </div>
  );
}

function BookmarkListContent() {
  const { isAuthenticated } = useAuthStore();
  const { data: bookmarks, isLoading } = useBookmarks();
  const deleteBookmark = useDeleteBookmark();

  if (!isAuthenticated) {
    return (
      <div className="py-6 text-center">
        <div className="text-text-muted-ls mb-2 text-[13px]">
          Vui lòng đăng nhập để sử dụng tính năng Bookmark
        </div>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-8">
        <div className="h-5 w-5 animate-spin rounded-full border-2 border-amber-500/30 border-t-amber-500" />
      </div>
    );
  }

  const list = bookmarks ?? [];

  if (list.length === 0) {
    return (
      <div className="py-6 text-center">
        <div className="mb-2 text-2xl">📌</div>
        <div className="text-text-muted-ls text-[13px]">Chưa có bookmark nào</div>
        <div className="text-text-muted-ls/60 mt-1 text-[11px]">
          Nhấn nút ★ trên mỗi ngày để lưu lại
        </div>
      </div>
    );
  }

  const handleDelete = (id: string) => {
    deleteBookmark.mutate(id, {
      onSuccess: () => toast.success("Đã xoá bookmark"),
    });
  };

  const colorMap: Record<string, string> = {
    red: "bg-red-500",
    blue: "bg-blue-500",
    green: "bg-emerald-500",
    yellow: "bg-amber-500",
    purple: "bg-purple-500",
    default: "bg-slate-400",
  };

  return (
    <div className="custom-scrollbar max-h-[50vh] space-y-2 overflow-y-auto pr-1">
      {list.map((bm) => (
        <div
          key={bm.id}
          className="group flex items-start gap-2.5 rounded-lg bg-white/5 p-2.5 transition-colors hover:bg-white/10"
        >
          <div
            className={`mt-1.5 h-2 w-2 shrink-0 rounded-full ${colorMap[bm.color] ?? colorMap.default}`}
          />
          <div className="min-w-0 flex-1">
            <div className="text-text-mid truncate text-[13px] font-medium">
              {bm.title || bm.solar_date}
            </div>
            <div className="text-text-muted-ls text-[11px]">{bm.solar_date}</div>
            {bm.note && (
              <div className="text-text-muted-ls/70 mt-0.5 line-clamp-2 text-[11px]">{bm.note}</div>
            )}
          </div>
          <button
            onClick={() => handleDelete(bm.id)}
            className="mt-0.5 shrink-0 text-[11px] text-red-400 opacity-0 transition-opacity group-hover:opacity-100 hover:text-red-300"
            title="Xoá"
          >
            ✕
          </button>
        </div>
      ))}
    </div>
  );
}
