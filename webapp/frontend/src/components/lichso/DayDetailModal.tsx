"use client";

import { useCalendarDate } from "@/hooks/useCalendar";
import { useDayContent } from "@/hooks/useV3";
import { Skeleton } from "@/components/ui/skeleton";
import { useEffect, useRef, useCallback } from "react";
import { BookmarkButton } from "./BookmarkButton";
import { DailyQuoteCard } from "./DailyQuoteCard";
import { HistoricalEventsTimeline } from "./HistoricalEventsTimeline";
import { ArticleBriefCard } from "./ArticleBriefCard";
import { FestivalCard } from "./FestivalCard";
import { BirthdayBadge } from "./BirthdayBadge";
import { ShareCardGenerator } from "./ShareCardGenerator";

interface DayDetailModalProps {
  date: string; // YYYY-MM-DD
  onClose: () => void;
}

export function DayDetailModal({ date, onClose }: DayDetailModalProps) {
  const { data, isLoading } = useCalendarDate(date);
  const {
    data: dayContentData,
    isLoading: contentLoading,
    refetch: refetchContent,
    isFetching: contentFetching,
  } = useDayContent(date);
  const dayContent = dayContentData?.data;
  const overlayRef = useRef<HTMLDivElement>(null);

  // Close on Escape
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    document.addEventListener("keydown", handler);
    return () => document.removeEventListener("keydown", handler);
  }, [onClose]);

  // Close on overlay click
  const handleOverlayClick = (e: React.MouseEvent) => {
    if (e.target === overlayRef.current) onClose();
  };

  return (
    <div
      ref={overlayRef}
      onClick={handleOverlayClick}
      className="fixed inset-0 z-[200] flex animate-[fadeIn_0.2s_ease-out_both] items-center justify-center p-4"
      style={{ background: "rgba(0,0,0,0.4)", backdropFilter: "blur(4px)" }}
    >
      <div
        className="relative max-h-[85vh] w-full max-w-[520px] animate-[fadeUp_0.35s_ease-out_both] overflow-y-auto rounded-2xl"
        style={{
          background: "var(--ls-card-bg-strong, rgba(255,252,248,0.95))",
          border: "1px solid var(--ls-border-warm)",
          boxShadow: "0 20px 60px rgba(0,0,0,0.15), 0 2px 0 rgba(255,255,255,0.8) inset",
        }}
      >
        {/* Bookmark + Close buttons */}
        <div className="absolute top-4 right-4 z-10 flex items-center gap-1">
          <BookmarkButton date={date} className="h-8 w-8" />
          <button
            onClick={onClose}
            className="text-text-muted-ls hover:text-text-dark hover:bg-warm-amber/10 flex h-8 w-8 items-center justify-center rounded-lg transition-all"
          >
            ✕
          </button>
        </div>

        {isLoading ? (
          <div className="p-8">
            <Skeleton className="mb-4 h-8 w-40" />
            <Skeleton className="mb-3 h-4 w-64" />
            <Skeleton className="mb-3 h-4 w-48" />
            <Skeleton className="mb-3 h-4 w-56" />
            <Skeleton className="mb-6 h-4 w-40" />
            <div className="grid grid-cols-2 gap-3">
              <Skeleton className="h-20 rounded-xl" />
              <Skeleton className="h-20 rounded-xl" />
              <Skeleton className="h-20 rounded-xl" />
              <Skeleton className="h-20 rounded-xl" />
            </div>
          </div>
        ) : data ? (
          <>
            {/* Header section */}
            <div
              className="px-7 pt-7 pb-5"
              style={{ borderBottom: "1px solid var(--ls-border-soft)" }}
            >
              {/* Solar date */}
              <div className="text-text-muted-ls mb-1 flex items-center gap-2 text-[10px] tracking-[2.5px] uppercase">
                <span className="h-px w-4" style={{ background: "var(--ls-border-warm)" }} />
                Dương Lịch
              </div>
              <div className="mb-1 flex items-baseline gap-3">
                <span className="text-warm-amber text-[48px] leading-none font-[var(--font-lora)] font-semibold -tracking-[2px]">
                  {String(data.solar_day).padStart(2, "0")}
                </span>
                <div>
                  <div className="text-text-mid text-[15px] font-light tracking-wide">
                    Tháng {data.solar_month} · {data.solar_year}
                  </div>
                  <div className="text-text-soft text-[13px]">{data.day_of_week}</div>
                </div>
              </div>

              {/* Divider */}
              <div className="my-3 flex items-center gap-2 opacity-40">
                <span className="h-px flex-1" style={{ background: "var(--ls-border-warm)" }} />
                <span className="text-warm-gold text-[11px] font-[var(--font-noto)]">✦</span>
                <span className="h-px flex-1" style={{ background: "var(--ls-border-warm)" }} />
              </div>

              {/* Lunar date */}
              <div className="text-text-muted-ls mb-1 flex items-center gap-2 text-[10px] tracking-[2.5px] uppercase">
                <span className="h-px w-4" style={{ background: "var(--ls-border-warm)" }} />
                Âm Lịch
              </div>
              <div className="mb-3 flex items-baseline gap-3">
                <span className="text-jade-teal text-xl font-[var(--font-noto)] tracking-wider">
                  {data.lunar_day_name}
                </span>
                <span className="text-text-soft text-sm">
                  {data.lunar_month_name} · Năm {data.tu_tru.nam.can_chi}
                </span>
              </div>

              {/* Moon phase */}
              <div className="text-text-muted-ls mb-3 flex items-center gap-2 text-[13px]">
                <span className="text-lg">{data.phong_thuy.moon_phase.emoji}</span>
                <span>{data.phong_thuy.moon_phase.desc}</span>
              </div>

              {/* Can Chi badges */}
              <div className="flex flex-wrap gap-1.5">
                <span className="bg-jade-teal/10 border-jade-teal/25 text-jade-teal rounded-full border px-2.5 py-0.5 text-[11px] font-medium">
                  Năm {data.tu_tru.nam.can_chi}
                </span>
                <span className="bg-warm-amber/10 border-warm-amber/25 text-warm-amber rounded-full border px-2.5 py-0.5 text-[11px] font-medium">
                  Tháng {data.tu_tru.thang.can_chi}
                </span>
                <span className="bg-text-dark/5 border-text-dark/15 text-text-mid rounded-full border px-2.5 py-0.5 text-[11px] font-medium">
                  Ngày {data.tu_tru.ngay.can_chi}
                </span>
                <span className="rounded-full border border-[rgba(100,170,180,0.25)] bg-[rgba(100,170,180,0.1)] px-2.5 py-0.5 text-[11px] font-medium text-[#4A8080]">
                  Giờ {data.tu_tru.gio.can_chi}
                </span>
              </div>
            </div>

            {/* Score + assessment */}
            <div className="px-7 py-4" style={{ borderBottom: "1px solid var(--ls-border-soft)" }}>
              {/* Events / Holidays */}
              {data.events && data.events.length > 0 && (
                <div className="mb-4">
                  <div className="text-text-muted-ls mb-2 text-[10px] tracking-[2.5px] uppercase">
                    🎊 Sự Kiện / Ngày Lễ
                  </div>
                  <div className="flex flex-col gap-1.5">
                    {data.events.map((ev, idx) => (
                      <div
                        key={idx}
                        className={`flex items-center gap-2 rounded-lg px-2.5 py-1.5 text-[12px] ${
                          ev.is_off
                            ? "bg-danger/8 border-danger/15 text-danger border"
                            : "bg-warm-amber/5 border-warm-amber/15 text-text-mid border"
                        }`}
                      >
                        <span>{ev.emoji}</span>
                        <span className="font-medium">{ev.name}</span>
                        {ev.is_off && (
                          <span className="bg-danger/10 ml-auto rounded-full px-1.5 py-0.5 text-[9px] tracking-wider uppercase">
                            Nghỉ lễ
                          </span>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <div className="mb-2 flex items-center justify-between">
                <span className="text-text-muted-ls text-[10px] tracking-[2.5px] uppercase">
                  Đánh Giá Ngày
                </span>
                <span
                  className={`text-[13px] font-semibold ${
                    data.phong_thuy.chi_so_ngay >= 70
                      ? "text-jade-teal"
                      : data.phong_thuy.chi_so_ngay >= 40
                        ? "text-warm-amber"
                        : "text-danger"
                  }`}
                >
                  {data.phong_thuy.danh_gia} · {data.phong_thuy.chi_so_ngay}%
                </span>
              </div>
              <div
                className="h-1.5 overflow-hidden rounded-sm"
                style={{ background: "rgba(196,120,58,0.1)" }}
              >
                <div
                  className="h-full rounded-sm transition-all duration-1000"
                  style={{
                    width: `${data.phong_thuy.chi_so_ngay}%`,
                    background: "linear-gradient(90deg, var(--jade-soft), var(--warm-gold))",
                  }}
                />
              </div>
            </div>

            {/* Info cards grid */}
            <div className="grid grid-cols-2 gap-3 px-7 py-5">
              {/* Trực Ngày */}
              <MiniCard
                title="✦ Trực Ngày"
                value={data.phong_thuy.truc_ngay.name}
                sub={data.phong_thuy.truc_ngay.danh_gia}
                accent={data.phong_thuy.truc_ngay.danh_gia === "Tốt" ? "jade" : "amber"}
              />

              {/* Sao Chiếu */}
              <MiniCard
                title="⭐ Sao Chiếu"
                value={data.phong_thuy.sao_chieu.name}
                sub={data.phong_thuy.sao_chieu.tot_xau}
                accent={data.phong_thuy.sao_chieu.tot_xau === "Tốt" ? "jade" : "amber"}
              />

              {/* Hướng Tốt */}
              <MiniCard
                title="🧭 Hướng Tốt"
                value={data.phong_thuy.huong_xuat_hanh.huong_tot.join(", ")}
                sub={`Tài thần: ${data.phong_thuy.huong_xuat_hanh.tai_than}`}
                accent="jade"
              />

              {/* Tiết Khí */}
              <MiniCard
                title="🌿 Tiết Khí"
                value={`${data.tiet_khi.current.name} · ${data.tiet_khi.current.han_tu}`}
                sub={`Còn ${data.tiet_khi.days_left} ngày → ${data.tiet_khi.next.name}`}
                accent="amber"
              />
            </div>

            {/* Giờ Hoàng Đạo */}
            <div className="px-7 pb-4">
              <div className="text-text-muted-ls mb-2 text-[10px] tracking-[2.5px] uppercase">
                Giờ Hoàng Đạo
              </div>
              <div className="flex flex-wrap gap-1.5">
                {data.gio_hoang_dao.map((gio) => (
                  <span
                    key={gio.name}
                    className={`rounded-lg border px-2 py-0.5 text-[11px] ${
                      gio.is_hoang_dao
                        ? "bg-jade-teal/10 border-jade-teal/20 text-jade-teal font-medium"
                        : "text-text-muted-ls border-transparent bg-transparent"
                    }`}
                  >
                    {gio.name} {gio.range}
                  </span>
                ))}
              </div>
            </div>

            {/* Activities */}
            <div className="grid grid-cols-2 gap-4 px-7 pb-7">
              {/* Việc Nên */}
              <div>
                <div className="text-jade-teal mb-2 text-[10px] font-semibold tracking-[2px] uppercase">
                  ✓ Việc Nên Làm
                </div>
                <ul className="flex flex-col gap-1.5">
                  {data.phong_thuy.viec_nen.map((v) => (
                    <li key={v} className="text-text-mid flex items-center gap-2 text-[12px]">
                      <div className="bg-jade-soft h-1.5 w-1.5 shrink-0 rounded-full" />
                      {v}
                    </li>
                  ))}
                </ul>
              </div>

              {/* Việc Không */}
              <div>
                <div className="text-danger mb-2 text-[10px] font-semibold tracking-[2px] uppercase">
                  ✕ Không Nên Làm
                </div>
                <ul className="flex flex-col gap-1.5">
                  {data.phong_thuy.viec_khong.map((v) => (
                    <li key={v} className="text-text-muted-ls flex items-center gap-2 text-[12px]">
                      <div className="bg-danger/70 h-1.5 w-1.5 shrink-0 rounded-full" />
                      {v}
                    </li>
                  ))}
                </ul>
              </div>
            </div>

            {/* ============================== */}
            {/* V3: Daily Content Section      */}
            {/* ============================== */}
            {contentLoading ? (
              <div className="space-y-3 px-7 pb-5">
                <Skeleton className="h-20 rounded-xl" />
                <Skeleton className="h-16 rounded-xl" />
              </div>
            ) : dayContent ? (
              <div className="space-y-4 px-7 pb-6">
                {/* Divider */}
                <div className="flex items-center gap-2 opacity-40">
                  <span className="h-px flex-1" style={{ background: "var(--ls-border-warm)" }} />
                  <span className="text-warm-gold text-[11px] font-[var(--font-noto)]">✦</span>
                  <span className="h-px flex-1" style={{ background: "var(--ls-border-warm)" }} />
                </div>

                {/* Daily Quote */}
                {dayContent.quotes &&
                  dayContent.quotes.length > 0 &&
                  (() => {
                    const q = dayContent.quotes[0] as {
                      quote?: string;
                      author?: string;
                      author_title?: string;
                    };
                    return q.quote ? (
                      <DailyQuoteCard
                        quote={q.quote}
                        author={q.author || "Khuyết danh"}
                        authorTitle={q.author_title}
                      />
                    ) : null;
                  })()}

                {/* Historical Events */}
                {dayContent.events && dayContent.events.length > 0 && (
                  <HistoricalEventsTimeline
                    events={
                      dayContent.events as Array<{
                        id?: string;
                        title: string;
                        description?: string;
                        event_year?: number;
                        emoji?: string;
                      }>
                    }
                  />
                )}

                {/* Folk Festivals */}
                {dayContent.festivals && dayContent.festivals.length > 0 && (
                  <FestivalCard
                    festivals={
                      dayContent.festivals as Array<{
                        id?: string;
                        name: string;
                        description?: string;
                        region?: string;
                      }>
                    }
                  />
                )}

                {/* Famous Birthdays */}
                {dayContent.famous_people && dayContent.famous_people.length > 0 && (
                  <BirthdayBadge
                    people={
                      dayContent.famous_people as Array<{
                        id?: string;
                        name: string;
                        title?: string;
                        birth_year?: number;
                        death_year?: number;
                      }>
                    }
                  />
                )}

                {/* Articles */}
                {dayContent.articles && dayContent.articles.length > 0 && (
                  <ArticleBriefCard
                    articles={dayContent.articles}
                    onRefresh={() => refetchContent()}
                    isRefreshing={contentFetching}
                  />
                )}
              </div>
            ) : null}

            {/* ============================== */}
            {/* Share Card Generator            */}
            {/* ============================== */}
            <div className="px-7 pb-6">
              <div className="mb-3 flex items-center gap-2 opacity-40">
                <span className="h-px flex-1" style={{ background: "var(--ls-border-warm)" }} />
                <span className="text-warm-gold text-[11px] font-[var(--font-noto)]">✦</span>
                <span className="h-px flex-1" style={{ background: "var(--ls-border-warm)" }} />
              </div>
              <ShareCardGenerator
                data={{
                  solarDay: data.solar_day,
                  solarMonth: data.solar_month,
                  solarYear: data.solar_year,
                  lunarDay: data.lunar_day_name,
                  lunarMonth: data.lunar_month_name,
                  dayOfWeek: data.day_of_week,
                  canChi: data.tu_tru.ngay.can_chi,
                  trucNgay: data.phong_thuy.truc_ngay.name,
                  chiSoNgay: data.phong_thuy.chi_so_ngay,
                  quote: dayContent?.quotes?.[0]
                    ? (dayContent.quotes[0] as { quote?: string }).quote
                    : undefined,
                  quoteAuthor: dayContent?.quotes?.[0]
                    ? (dayContent.quotes[0] as { author?: string }).author
                    : undefined,
                }}
              />
            </div>
          </>
        ) : null}
      </div>
    </div>
  );
}

function MiniCard({
  title,
  value,
  sub,
  accent,
}: {
  title: string;
  value: string;
  sub: string;
  accent: "jade" | "amber";
}) {
  const borderColor = accent === "jade" ? "rgba(74,139,127,0.15)" : "rgba(196,120,58,0.15)";
  const bgColor = accent === "jade" ? "rgba(74,139,127,0.04)" : "rgba(196,120,58,0.04)";

  return (
    <div
      className="rounded-xl p-3"
      style={{
        background: bgColor,
        border: `1px solid ${borderColor}`,
      }}
    >
      <div className="text-text-muted-ls mb-1 text-[9px] tracking-[2px] uppercase">{title}</div>
      <div className="text-text-dark mb-0.5 truncate text-[13px] font-medium">{value}</div>
      <div className="text-text-soft truncate text-[11px]">{sub}</div>
    </div>
  );
}
