"use client";

import { useState } from "react";
import { useGoodDays } from "@/hooks/useCalendar";
import { Skeleton } from "@/components/ui/skeleton";

const MONTHS_VI = [
  "Tháng 1",
  "Tháng 2",
  "Tháng 3",
  "Tháng 4",
  "Tháng 5",
  "Tháng 6",
  "Tháng 7",
  "Tháng 8",
  "Tháng 9",
  "Tháng 10",
  "Tháng 11",
  "Tháng 12",
];

export function GoodDaysTab() {
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);

  const { data, isLoading } = useGoodDays({ year, month });

  const prev = () => {
    if (month === 1) {
      setMonth(12);
      setYear((y) => y - 1);
    } else setMonth((m) => m - 1);
  };

  const next = () => {
    if (month === 12) {
      setMonth(1);
      setYear((y) => y + 1);
    } else setMonth((m) => m + 1);
  };

  return (
    <div className="animate-[fadeUp_0.65s_ease-out_both]">
      {/* Header */}
      <div className="mb-5 flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <span
            className="h-4 w-1 rounded-sm"
            style={{
              background: "linear-gradient(to bottom, var(--jade-teal), var(--jade-soft))",
            }}
          />
          <span className="text-text-dark text-base font-[var(--font-lora)] tracking-wide">
            Ngày Tốt {MONTHS_VI[month - 1]}
          </span>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={prev}
            className="text-warm-amber hover:bg-warm-amber/10 flex h-8 w-8 items-center justify-center rounded-lg transition-all"
            style={{
              background: "var(--ls-card-bg)",
              border: "1px solid var(--ls-border-warm)",
            }}
          >
            ‹
          </button>
          <span className="text-text-mid min-w-[180px] text-center text-[15px] font-[var(--font-lora)]">
            {MONTHS_VI[month - 1]} · {year}
          </span>
          <button
            onClick={next}
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

      {/* Summary */}
      {data && (
        <div
          className="mb-5 flex items-center justify-between rounded-xl px-5 py-3"
          style={{
            background: "linear-gradient(135deg, rgba(74,139,127,0.08), rgba(196,120,58,0.06))",
            border: "1px solid var(--ls-border-soft)",
          }}
        >
          <span className="text-text-mid text-[13px]">
            Tìm thấy <strong className="text-jade-teal">{data.length}</strong> ngày tốt trong tháng
          </span>
          <span className="text-text-muted-ls text-[11px]">Chỉ số ≥ 65%</span>
        </div>
      )}

      {/* Loading */}
      {isLoading && (
        <div className="flex flex-col gap-3">
          {Array.from({ length: 5 }).map((_, i) => (
            <div
              key={i}
              className="rounded-2xl p-5"
              style={{ background: "var(--ls-card-bg)", border: "1px solid var(--ls-border-soft)" }}
            >
              <div className="flex gap-4">
                <Skeleton className="h-14 w-14 rounded-xl" />
                <div className="flex-1">
                  <Skeleton className="mb-2 h-4 w-40" />
                  <Skeleton className="mb-1.5 h-3 w-56" />
                  <Skeleton className="h-3 w-32" />
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Good days list */}
      {data && (
        <div className="flex flex-col gap-3">
          {data.length === 0 ? (
            <div
              className="rounded-2xl p-10 text-center"
              style={{ background: "var(--ls-card-bg)", border: "1px solid var(--ls-border-soft)" }}
            >
              <p className="text-text-muted-ls text-sm">Không có ngày tốt nào trong tháng này</p>
            </div>
          ) : (
            data.map((day) => {
              const scoreColor =
                day.chi_so_ngay >= 85
                  ? "text-jade-teal"
                  : day.chi_so_ngay >= 70
                    ? "text-jade-soft"
                    : "text-warm-amber";

              return (
                <div
                  key={day.solar_day}
                  className="group cursor-pointer rounded-2xl p-5 backdrop-blur-[14px] transition-all duration-300 hover:translate-x-1"
                  style={{
                    background: "var(--ls-card-bg)",
                    border: "1px solid var(--ls-border-soft)",
                    boxShadow: "0 2px 12px var(--ls-shadow-warm)",
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.borderColor = "rgba(74,139,127,0.3)";
                    e.currentTarget.style.boxShadow = "0 4px 20px rgba(74,139,127,0.1)";
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.borderColor = "var(--ls-border-soft)";
                    e.currentTarget.style.boxShadow = "0 2px 12px var(--ls-shadow-warm)";
                  }}
                >
                  <div className="flex items-start gap-4">
                    {/* Day number badge */}
                    <div
                      className="flex h-14 w-14 flex-shrink-0 flex-col items-center justify-center rounded-xl"
                      style={{
                        background:
                          "linear-gradient(135deg, rgba(74,139,127,0.1), rgba(74,139,127,0.05))",
                        border: "1px solid rgba(74,139,127,0.2)",
                      }}
                    >
                      <span className="text-jade-teal text-xl leading-none font-[var(--font-lora)] font-semibold">
                        {String(day.solar_day).padStart(2, "0")}
                      </span>
                      <span className="text-text-muted-ls mt-0.5 text-[9px]">
                        {day.day_of_week.replace("Thứ ", "T").replace("Chủ Nhật", "CN")}
                      </span>
                    </div>

                    {/* Info */}
                    <div className="min-w-0 flex-1">
                      <div className="mb-1 flex items-center gap-2">
                        <span className="text-text-dark text-sm font-medium">
                          Ngày {day.solar_day}/{month}/{year}
                        </span>
                        <span className="text-text-muted-ls text-[10px]">·</span>
                        <span className="text-text-soft text-[12px]">
                          Âm lịch: Mồng {day.lunar_day > 10 ? day.lunar_day : day.lunar_day}
                        </span>
                      </div>

                      <div className="mb-1.5 flex items-center gap-2">
                        <span className="bg-warm-amber/10 border-warm-amber/25 text-warm-amber rounded-full border px-2 py-0.5 text-[11px] font-medium">
                          {day.day_can_chi}
                        </span>
                        <span className="bg-jade-teal/10 border-jade-teal/25 text-jade-teal rounded-full border px-2 py-0.5 text-[11px]">
                          {day.truc_ngay}
                        </span>
                      </div>

                      {/* Score bar */}
                      <div className="flex items-center gap-2">
                        <div
                          className="h-1 max-w-[200px] flex-1 overflow-hidden rounded-sm"
                          style={{ background: "rgba(74,139,127,0.1)" }}
                        >
                          <div
                            className="h-full rounded-sm"
                            style={{
                              width: `${day.chi_so_ngay}%`,
                              background:
                                "linear-gradient(90deg, var(--jade-soft), var(--warm-gold))",
                            }}
                          />
                        </div>
                        <span className={`text-[12px] font-semibold ${scoreColor}`}>
                          {day.danh_gia} · {day.chi_so_ngay}%
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              );
            })
          )}
        </div>
      )}
    </div>
  );
}
