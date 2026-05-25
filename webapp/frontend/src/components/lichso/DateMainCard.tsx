"use client";

import { useEffect, useState, useRef, useCallback, useMemo } from "react";
import Link from "next/link";
import { useSwipe } from "@/hooks/useSwipe";
import { ClockWidget } from "@/components/lichso/ClockWidget";
import { useRandomArticles, useEventsByDate } from "@/hooks/usePublicContent";
import { ROUTES } from "@/lib/constants";
import type { DayResponse } from "@/types/calendar";

type FlipDirection = "left" | "right" | null;
type FlipPhase = "idle" | "peeling" | "settled";

interface DateMainCardProps {
  data: DayResponse;
  onPrevDay?: () => void;
  onNextDay?: () => void;
}

export function DateMainCard({ data, onPrevDay, onNextDay }: DateMainCardProps) {
  const [flipDirection, setFlipDirection] = useState<FlipDirection>(null);
  const [flipPhase, setFlipPhase] = useState<FlipPhase>("idle");

  // Two layers: the "old" page that peels away, and the "new" page revealed underneath
  const [oldData, setOldData] = useState<DayResponse>(data);
  const [showingData, setShowingData] = useState<DayResponse>(data);

  const pendingDataRef = useRef<DayResponse>(data);
  const isFlippingRef = useRef(false);

  // Keep pending data in sync
  useEffect(() => {
    pendingDataRef.current = data;
    if (!isFlippingRef.current) {
      setShowingData(data);
      setOldData(data);
    }
  }, [data]);

  const triggerFlip = useCallback((direction: FlipDirection, action?: () => void) => {
    if (isFlippingRef.current) return;
    isFlippingRef.current = true;

    // Capture current data as the "old page"
    setOldData(pendingDataRef.current);
    setFlipDirection(direction);

    // Fire the navigation immediately so new data starts loading
    action?.();

    // Start the peel animation
    // Small delay to ensure the DOM has the old snapshot
    requestAnimationFrame(() => {
      setFlipPhase("peeling");
    });

    // At ~55% of animation (page is mostly turned), swap content underneath
    setTimeout(() => {
      setShowingData(pendingDataRef.current);
    }, 380);

    // Animation done → settle
    setTimeout(() => {
      setFlipPhase("settled");
    }, 700);

    // Clean up
    setTimeout(() => {
      setFlipDirection(null);
      setFlipPhase("idle");
      isFlippingRef.current = false;
      // Final sync
      const latest = pendingDataRef.current;
      setShowingData(latest);
      setOldData(latest);
    }, 780);
  }, []);

  const handleSwipeLeft = useCallback(() => {
    triggerFlip("left", onNextDay);
  }, [triggerFlip, onNextDay]);

  const handleSwipeRight = useCallback(() => {
    triggerFlip("right", onPrevDay);
  }, [triggerFlip, onPrevDay]);

  const swipeHandlers = useSwipe({
    onSwipeLeft: handleSwipeLeft,
    onSwipeRight: handleSwipeRight,
  });

  const isFlipping = flipPhase === "peeling";

  return (
    <div className="relative" style={{ perspective: "1400px" }} {...swipeHandlers}>
      {/* ── Layer 0: New page revealed underneath ── */}
      <div
        className="relative overflow-hidden rounded-[28px] px-10 pt-6 pb-10 backdrop-blur-[16px] sm:px-12 sm:pt-7 sm:pb-12"
        style={{
          background: "var(--ls-card-bg-solid)",
          border: "1.5px solid var(--ls-border-warm)",
          boxShadow:
            "0 0 0 6px var(--ls-card-ring), 0 0 0 12px var(--ls-card-ring-outer), 0 24px 80px var(--ls-shadow-deep), 0 8px 24px var(--ls-shadow-warm)",
        }}
      >
        {/* Corner ornaments */}
        <span
          className="pointer-events-none absolute top-3.5 left-[18px] text-[11px] select-none"
          style={{ color: "rgba(200,144,42,0.35)" }}
        >
          ✦
        </span>
        <span
          className="pointer-events-none absolute right-[18px] bottom-3.5 text-[11px] select-none"
          style={{ color: "rgba(200,144,42,0.35)" }}
        >
          ✦
        </span>
        <CardContent
          data={showingData}
          onPrev={() => triggerFlip("right", onPrevDay)}
          onNext={() => triggerFlip("left", onNextDay)}
        />

        {/* ── Clock inside card ── */}
        <ClockWidget />
      </div>

      {/* ── Layer 1: Old page that peels away ── */}
      {isFlipping && (
        <div
          className="absolute inset-0 z-10 overflow-hidden rounded-[28px]"
          style={{
            perspective: "1400px",
            pointerEvents: "none",
          }}
        >
          {/* The peeling page */}
          <div
            className={`absolute inset-0 rounded-[28px] px-10 pt-6 pb-10 backdrop-blur-[16px] sm:px-12 sm:pt-7 sm:pb-12 ${
              flipDirection === "left" ? "animate-pagePeelLeft" : "animate-pagePeelRight"
            }`}
            style={{
              background: "var(--ls-card-bg-solid)",
              border: "1.5px solid var(--ls-border-warm)",
              transformOrigin: flipDirection === "left" ? "right center" : "left center",
              backfaceVisibility: "hidden",
              willChange: "transform",
            }}
          >
            <CardContent
              data={oldData}
            />

            {/* Page curl gradient overlay — simulates paper curling/shadow */}
            <div
              className={`absolute inset-0 rounded-[28px] ${
                flipDirection === "left" ? "animate-curlShadowLeft" : "animate-curlShadowRight"
              }`}
              style={{
                background:
                  flipDirection === "left"
                    ? "linear-gradient(to left, rgba(0,0,0,0.06) 0%, transparent 40%)"
                    : "linear-gradient(to right, rgba(0,0,0,0.06) 0%, transparent 40%)",
                pointerEvents: "none",
              }}
            />
          </div>

          {/* Sweep shadow on the base card as the page lifts */}
          <div
            className={`absolute inset-0 rounded-[28px] ${
              flipDirection === "left" ? "animate-sweepShadowLeft" : "animate-sweepShadowRight"
            }`}
            style={{ pointerEvents: "none" }}
          />
        </div>
      )}
    </div>
  );
}

/* ─────────────────────────────────────────
   Extracted card content to avoid duplication
   ───────────────────────────────────────── */
function CardContent({
  data,
  onPrev,
  onNext,
}: {
  data: DayResponse;
  onPrev?: () => void;
  onNext?: () => void;
}) {
  // Stable date seed — changes only when the date changes
  const dateSeed = `${data.solar_year}-${data.solar_month}-${data.solar_day}`;

  return (
    <div className="flex flex-col items-center text-center">
      {/* Top shimmer line */}
      <div
        className="absolute top-0 right-[15%] left-[15%] h-px"
        style={{
          background: "linear-gradient(90deg, transparent, rgba(196,120,58,0.4), transparent)",
        }}
      />

      {/* Lotus watermark — deferred để không ảnh hưởng LCP */}
      <span
        className="pointer-events-none absolute right-5 -bottom-5 text-[120px] leading-none select-none"
        style={{ color: "rgba(196,120,58,0.04)", contentVisibility: "hidden" }}
        aria-hidden="true"
      >
        🌸
      </span>

      {/* Big solar day number — Playfair Display */}
      <div
        className="font-playfair leading-[0.85] -tracking-[5px]"
        style={{
          fontSize: "clamp(100px, 28vw, 140px)",
          color: "var(--ls-text-dark)",
          fontWeight: 700,
        }}
      >
        {String(data.solar_day).padStart(2, "0")}
      </div>

      {/* Weekday */}
      <div
        className="mt-1 uppercase"
        style={{
          fontSize: "15px",
          letterSpacing: "3px",
          color: "var(--ls-text-soft)",
          fontWeight: 500,
        }}
      >
        {data.day_of_week}
      </div>

      {/* Month & Year — Playfair Display bold */}
      <div
        className="font-playfair mt-1.5 mb-6"
        style={{
          fontSize: "18px",
          fontWeight: 700,
          letterSpacing: "0.5px",
          color: "var(--ls-text-mid)",
        }}
      >
        Tháng {data.solar_month} · {data.solar_year}
      </div>

      {/* ── Divider with navigation arrows ── */}
      <div className="mb-6 flex w-full items-center">
        {onPrev && (
          <button
            type="button"
            onClick={onPrev}
            className="text-text-muted-ls hover:text-warm-amber hover:bg-warm-amber/10 flex h-7 w-7 items-center justify-center rounded-full text-[15px] transition-all select-none active:scale-90"
            style={{ border: "1px solid rgba(200,144,42,0.2)" }}
            aria-label="Ngày trước"
          >
            ‹
          </button>
        )}
        <span
          className="mx-3 h-px flex-1"
          style={{
            background: "linear-gradient(90deg, transparent, rgba(200,144,42,0.3), transparent)",
          }}
        />
        <span className="text-[13px]" style={{ color: "rgba(200,144,42,0.55)" }}>
          ✦
        </span>
        <span
          className="mx-3 h-px flex-1"
          style={{
            background: "linear-gradient(90deg, transparent, rgba(200,144,42,0.3), transparent)",
          }}
        />
        {onNext && (
          <button
            type="button"
            onClick={onNext}
            className="text-text-muted-ls hover:text-warm-amber hover:bg-warm-amber/10 flex h-7 w-7 items-center justify-center rounded-full text-[15px] transition-all select-none active:scale-90"
            style={{ border: "1px solid rgba(200,144,42,0.2)" }}
            aria-label="Ngày sau"
          >
            ›
          </button>
        )}
      </div>

      {/* ── ÂM LỊCH ── */}
      <div
        className="mb-2 text-[11px] tracking-[4px] uppercase"
        style={{ color: "var(--ls-text-muted)", fontWeight: 600, letterSpacing: "4px" }}
      >
        Âm Lịch
      </div>

      {/* Lunar day — large serif jade */}
      <div
        className="font-[var(--font-lora)]"
        style={{
          fontSize: "clamp(30px, 8vw, 42px)",
          fontWeight: 700,
          letterSpacing: "1px",
          color: "var(--jade-teal)",
          lineHeight: 1.1,
        }}
      >
        {data.lunar_day_name}
      </div>

      {/* Lunar month & year */}
      <div
        className="mt-2 leading-relaxed"
        style={{ fontSize: "15px", color: "var(--ls-text-soft)", fontWeight: 400 }}
      >
        {data.lunar_month_name} · Năm {data.tu_tru.nam.can_chi}
      </div>

      {/* Can Chi badges — centered, larger */}
      <div className="mb-5 flex flex-wrap justify-center gap-2">
        <span
          className="rounded-full px-4 py-1.5 font-semibold"
          style={{
            fontSize: "13px",
            letterSpacing: "0.3px",
            background: "rgba(200,144,42,0.1)",
            color: "#c8902a",
            border: "1px solid rgba(200,144,42,0.22)",
          }}
        >
          {data.tu_tru.nam.can_chi}
        </span>
        <span
          className="rounded-full px-4 py-1.5 font-semibold"
          style={{
            fontSize: "13px",
            letterSpacing: "0.3px",
            background: "rgba(61,128,112,0.08)",
            color: "var(--jade-teal)",
            border: "1px solid rgba(61,128,112,0.2)",
          }}
        >
          {data.tu_tru.thang.can_chi}
        </span>
        <span
          className="rounded-full px-4 py-1.5 font-semibold"
          style={{
            fontSize: "13px",
            letterSpacing: "0.3px",
            background: "rgba(100,60,20,0.06)",
            color: "var(--ls-text-mid)",
            border: "1px solid rgba(100,60,20,0.14)",
          }}
        >
          {data.tu_tru.ngay.can_chi}
        </span>
      </div>

      {/* ── SỰ KIỆN NGÀY NÀY ── */}
      <DayEvents month={data.solar_month} day={data.solar_day} lunarMonth={data.lunar_month} lunarDay={data.lunar_day} />

      {/* ── BÀI VIẾT HAY ── */}
      <RandomArticle dateSeed={dateSeed} />
    </div>
  );
}

/* ─────────────────────────────────────────
   Sự kiện của ngày
   ───────────────────────────────────────── */
function DayEvents({ month, day, lunarMonth, lunarDay }: { month: number; day: number; lunarMonth?: number; lunarDay?: number }) {
  const { data, isLoading } = useEventsByDate(month, day, lunarMonth, lunarDay);
  const events = data?.data ?? [];

  if (isLoading || events.length === 0) return null;

  return (
    <div className="mb-3 w-full text-left">
      <div className="mb-2 flex items-center gap-1.5">
        <span className="text-[11px]" style={{ color: "rgba(200,144,42,0.7)" }}>✦</span>
        <span
          className="text-[11px] font-semibold tracking-[2.5px] uppercase"
          style={{ color: "var(--ls-text-muted)" }}
        >
          Sự kiện ngày này
        </span>
      </div>
      <div className="space-y-1.5">
        {events.slice(0, 3).map((event) => (
          <Link
            key={event.id}
            href={`${ROUTES.EVENTS}/${event.slug}`}
            className="flex items-start gap-2.5 rounded-xl px-3 py-2 transition-colors"
            style={{
              background: "rgba(61,128,112,0.06)",
              border: "1px solid rgba(61,128,112,0.14)",
            }}
            onMouseEnter={(e) => {
              (e.currentTarget as HTMLElement).style.background = "rgba(61,128,112,0.12)";
            }}
            onMouseLeave={(e) => {
              (e.currentTarget as HTMLElement).style.background = "rgba(61,128,112,0.06)";
            }}
          >
            <span className="mt-0.5 shrink-0 text-[12px]">🗓</span>
            <span
              className="flex-1 line-clamp-2 text-[12.5px] leading-snug"
              style={{ color: "var(--ls-text-mid)" }}
            >
              {event.title}
            </span>
            {event.event_year && (
              <span
                className="shrink-0 text-[11px] font-medium"
                style={{ color: "var(--jade-teal)" }}
              >
                {event.event_year}
              </span>
            )}
          </Link>
        ))}
      </div>
    </div>
  );
}

/* ─────────────────────────────────────────
   1 bài viết ngẫu nhiên (stable per day)
   ───────────────────────────────────────── */
function RandomArticle({ dateSeed }: { dateSeed: string }) {
  const { data, isLoading } = useRandomArticles(5, dateSeed);

  // Pick 1 article deterministically from the list using the date seed
  const article = useMemo(() => {
    const list = data?.data ?? [];
    if (list.length === 0) return null;
    // Simple hash of dateSeed → stable index per day
    const hash = dateSeed.split("").reduce((acc, c) => acc + c.charCodeAt(0), 0);
    return list[hash % list.length];
  }, [data, dateSeed]);

  if (isLoading || !article) return null;

  return (
    <div className="w-full text-left">
      <div className="mb-2 flex items-center gap-1.5">
        <span className="text-[11px]" style={{ color: "rgba(200,144,42,0.7)" }}>✦</span>
        <span
          className="text-[11px] font-semibold tracking-[2.5px] uppercase"
          style={{ color: "var(--ls-text-muted)" }}
        >
          Bài viết hay
        </span>
      </div>
      <Link
        href={`${ROUTES.ARTICLES}/${article.slug}`}
        className="flex items-start gap-2.5 rounded-xl px-3 py-2.5 transition-colors"
        style={{
          background: "rgba(200,144,42,0.05)",
          border: "1px solid rgba(200,144,42,0.12)",
        }}
        onMouseEnter={(e) => {
          (e.currentTarget as HTMLElement).style.background = "rgba(200,144,42,0.11)";
        }}
        onMouseLeave={(e) => {
          (e.currentTarget as HTMLElement).style.background = "rgba(200,144,42,0.05)";
        }}
      >
        <span className="mt-0.5 shrink-0 text-[14px]">📖</span>
        <span
          className="line-clamp-2 text-[12.5px] leading-snug"
          style={{ color: "var(--ls-text-mid)" }}
        >
          {article.title}
        </span>
      </Link>
    </div>
  );
}
