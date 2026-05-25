"use client";

import type { DayResponse } from "@/types/calendar";
import { useEffect, useState } from "react";

interface BottomCardsProps {
  data: DayResponse;
}

/* ─── Solar Term Card ─── */
function SolarTermCard({ data }: BottomCardsProps) {
  const [barWidth, setBarWidth] = useState(0);

  useEffect(() => {
    const timer = setTimeout(() => {
      setBarWidth(data.tiet_khi.progress);
    }, 500);
    return () => clearTimeout(timer);
  }, [data.tiet_khi.progress]);

  return (
    <div
      className="relative overflow-hidden rounded-2xl p-5 backdrop-blur-[14px]"
      style={{
        background: "var(--ls-card-bg)",
        border: "1px solid var(--ls-border-soft)",
        boxShadow: "0 4px 20px var(--ls-shadow-warm)",
      }}
    >
      <div className="text-text-muted-ls mb-3 text-[10px] tracking-[2.5px] uppercase">
        🌱 Tiết Khí Hiện Tại
      </div>
      <div className="text-jade-teal mb-1 text-[22px] font-[var(--font-noto)] tracking-[2px]">
        {data.tiet_khi.current.name} · {data.tiet_khi.current.han_tu}
      </div>
      <div className="text-text-muted-ls mb-1 text-xs">{data.tiet_khi.current.date}</div>
      <div className="text-text-soft mb-3 text-[12.5px] leading-relaxed italic">
        {data.tiet_khi.description ? `"${data.tiet_khi.description}"` : ""}
      </div>
      <div
        className="h-1 overflow-hidden rounded-sm"
        style={{ background: "rgba(74,139,127,0.12)" }}
      >
        <div
          className="h-full rounded-sm transition-all duration-[1600ms] ease-[cubic-bezier(0.16,1,0.3,1)]"
          style={{
            width: `${barWidth}%`,
            background: "linear-gradient(90deg, var(--jade-soft), var(--warm-gold))",
          }}
        />
      </div>
      <div className="text-text-muted-ls mt-1.5 text-[11px]">
        Tiếp theo: {data.tiet_khi.next.name} ({data.tiet_khi.next.han_tu}) · Còn{" "}
        {data.tiet_khi.days_left} ngày
      </div>
    </div>
  );
}

/* ─── Compass Card ─── */
function CompassCard({ data }: BottomCardsProps) {
  const huong = data.phong_thuy.huong_xuat_hanh;
  return (
    <div
      className="relative overflow-hidden rounded-2xl p-5 backdrop-blur-[14px]"
      style={{
        background: "var(--ls-card-bg)",
        border: "1px solid var(--ls-border-soft)",
        boxShadow: "0 4px 20px var(--ls-shadow-warm)",
      }}
    >
      <div className="text-text-muted-ls mb-3 text-[10px] tracking-[2.5px] uppercase">
        🧭 La Bàn Hướng Tốt
      </div>
      <div className="flex flex-col items-center gap-2">
        <svg className="h-24 w-24" viewBox="0 0 96 96">
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
          {/* Good direction arrow - NE */}
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
          {/* South */}
          <line
            x1="48"
            y1="48"
            x2="48"
            y2="78"
            stroke="rgba(74,139,127,0.35)"
            strokeWidth="1.5"
            strokeLinecap="round"
          />
          {/* Bad - West */}
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
        <div className="text-jade-teal text-[13.5px] font-medium">
          {huong.huong_tot.join(" · ")}
        </div>
        <div className="text-danger text-[11.5px] opacity-70">
          Tránh: {huong.huong_xau.join(", ")}
        </div>
      </div>
    </div>
  );
}

/* ─── Activities Card ─── */
function ActivitiesCard({ data }: BottomCardsProps) {
  const { viec_nen, viec_khong } = data.phong_thuy;
  return (
    <div
      className="relative overflow-hidden rounded-2xl p-5 backdrop-blur-[14px]"
      style={{
        background: "var(--ls-card-bg)",
        border: "1px solid var(--ls-border-soft)",
        boxShadow: "0 4px 20px var(--ls-shadow-warm)",
      }}
    >
      <div className="text-text-muted-ls mb-3 text-[10px] tracking-[2.5px] uppercase">
        📋 Việc Nên & Không Nên
      </div>
      <ul className="flex flex-col gap-[7px]">
        {viec_nen.slice(0, 4).map((v) => (
          <li key={v} className="text-text-mid flex items-center gap-2.5 text-[13px]">
            <div className="bg-jade-soft h-1.5 w-1.5 shrink-0 rounded-full" />
            {v}
          </li>
        ))}
        {viec_khong.slice(0, 3).map((v) => (
          <li key={v} className="text-text-muted-ls flex items-center gap-2.5 text-[13px]">
            <div className="bg-danger h-1.5 w-1.5 shrink-0 rounded-full opacity-70" />
            {v}
          </li>
        ))}
      </ul>
    </div>
  );
}

export function BottomCards({ data }: BottomCardsProps) {
  return (
    <div className="mb-8 grid animate-[fadeUp_0.85s_ease-out_0.1s_both] grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <SolarTermCard data={data} />
      <CompassCard data={data} />
      <ActivitiesCard data={data} />
    </div>
  );
}
