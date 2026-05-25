"use client";

import { useState, useEffect } from "react";
import { useSolarTerms } from "@/hooks/useCalendar";
import { Skeleton } from "@/components/ui/skeleton";

export function SolarTermsTab() {
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const { data, isLoading } = useSolarTerms(year);
  const [activeIdx, setActiveIdx] = useState<number | null>(null);

  // Find the current solar term
  useEffect(() => {
    if (!data) return;
    const today = new Date();
    for (let i = data.length - 1; i >= 0; i--) {
      const termDate = new Date(data[i].year, data[i].month - 1, data[i].day);
      if (termDate <= today) {
        setActiveIdx(i);
        break;
      }
    }
  }, [data]);

  // Season grouping
  const seasons = [
    {
      name: "Xuân",
      icon: "🌸",
      range: [2, 7],
      color: "rgba(74,139,127,0.12)",
      borderColor: "rgba(74,139,127,0.25)",
    },
    {
      name: "Hạ",
      icon: "☀️",
      range: [8, 13],
      color: "rgba(212,149,106,0.12)",
      borderColor: "rgba(212,149,106,0.25)",
    },
    {
      name: "Thu",
      icon: "🍂",
      range: [14, 19],
      color: "rgba(196,120,58,0.10)",
      borderColor: "rgba(196,120,58,0.22)",
    },
    {
      name: "Đông",
      icon: "❄️",
      range: [20, 23, 0, 1],
      color: "rgba(80,128,160,0.10)",
      borderColor: "rgba(80,128,160,0.22)",
    },
  ];

  function getSeasonForIndex(idx: number) {
    if (idx >= 2 && idx <= 7) return 0;
    if (idx >= 8 && idx <= 13) return 1;
    if (idx >= 14 && idx <= 19) return 2;
    return 3; // 20-23, 0-1
  }

  return (
    <div className="animate-[fadeUp_0.65s_ease-out_both]">
      {/* Header */}
      <div className="mb-5 flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <span
            className="h-4 w-1 rounded-sm"
            style={{
              background: "linear-gradient(to bottom, var(--warm-amber), var(--jade-teal))",
            }}
          />
          <span className="text-text-dark text-base font-[var(--font-lora)] tracking-wide">
            24 Tiết Khí
          </span>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={() => setYear((y) => y - 1)}
            className="text-warm-amber hover:bg-warm-amber/10 flex h-8 w-8 items-center justify-center rounded-lg transition-all"
            style={{
              background: "var(--ls-card-bg)",
              border: "1px solid var(--ls-border-warm)",
            }}
          >
            ‹
          </button>
          <span className="text-text-mid min-w-[80px] text-center text-[15px] font-[var(--font-lora)]">
            {year}
          </span>
          <button
            onClick={() => setYear((y) => y + 1)}
            className="text-warm-amber hover:bg-warm-amber/10 flex h-8 w-8 items-center justify-center rounded-lg transition-all"
            style={{
              background: "var(--ls-card-bg)",
              border: "1px solid var(--ls-border-warm)",
            }}
          >
            ›
          </button>
        </div>
      </div>

      {/* Season legend */}
      <div className="mb-5 flex flex-wrap gap-2">
        {seasons.map((s) => (
          <span
            key={s.name}
            className="rounded-full px-3 py-1 text-[12px] font-medium"
            style={{ background: s.color, border: `1px solid ${s.borderColor}` }}
          >
            {s.icon} {s.name}
          </span>
        ))}
      </div>

      {isLoading && (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 12 }).map((_, i) => (
            <div
              key={i}
              className="rounded-xl p-4"
              style={{ background: "var(--ls-card-bg)", border: "1px solid var(--ls-border-soft)" }}
            >
              <Skeleton className="mb-2 h-5 w-32" />
              <Skeleton className="mb-1 h-3 w-24" />
              <Skeleton className="h-3 w-40" />
            </div>
          ))}
        </div>
      )}

      {data && (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {data.map((term, i) => {
            const seasonIdx = getSeasonForIndex(i);
            const season = seasons[seasonIdx];
            const isCurrent = activeIdx === i;
            const isPast = activeIdx !== null && i < activeIdx;

            return (
              <div
                key={i}
                className={`relative rounded-xl p-4 backdrop-blur-[12px] transition-all duration-300 ${
                  isCurrent ? "ring-warm-amber/40 ring-2" : ""
                } ${isPast ? "opacity-60" : ""}`}
                style={{
                  background: isCurrent ? "var(--ls-card-bg-strong)" : "var(--ls-card-bg)",
                  border: `1px solid ${isCurrent ? "rgba(196,120,58,0.35)" : "var(--ls-border-soft)"}`,
                  boxShadow: isCurrent
                    ? "0 4px 20px rgba(196,120,58,0.12)"
                    : "0 2px 8px var(--ls-shadow-warm)",
                }}
              >
                {isCurrent && (
                  <div
                    className="absolute top-0 right-0 left-0 h-0.5 rounded-t-xl"
                    style={{
                      background: "linear-gradient(90deg, var(--warm-amber), var(--warm-gold))",
                    }}
                  />
                )}

                <div className="mb-1.5 flex items-start justify-between">
                  <div className="flex items-center gap-2">
                    <span className="text-sm">{season.icon}</span>
                    <span className="text-text-dark text-[15px] font-[var(--font-noto)] font-medium tracking-wide">
                      {term.name}
                    </span>
                    <span className="text-text-soft text-[13px] font-[var(--font-noto)]">
                      {term.han_tu}
                    </span>
                  </div>
                  {isCurrent && (
                    <span className="bg-warm-amber/15 text-warm-amber border-warm-amber/25 rounded-full border px-2 py-0.5 text-[10px] font-semibold">
                      Hiện tại
                    </span>
                  )}
                </div>

                <div className="text-text-mid mb-1 text-[13px]">
                  {String(term.day).padStart(2, "0")}/{String(term.month).padStart(2, "0")}/
                  {term.year}
                </div>

                <div className="text-text-muted-ls text-[11px]">
                  Kinh độ Mặt Trời: {term.sun_long}°
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
