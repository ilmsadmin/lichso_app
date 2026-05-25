"use client";

import type { DayResponse } from "@/types/calendar";
import { BookmarkButton } from "@/components/lichso/BookmarkButton";
import { Share2, Bell } from "lucide-react";
import Link from "next/link";

interface V2HeroProps {
  data: DayResponse;
}

export function V2Hero({ data }: V2HeroProps) {
  const dateStr = `${data.solar_year}-${String(data.solar_month).padStart(2, "0")}-${String(data.solar_day).padStart(2, "0")}`;

  return (
    <section
      className="relative mb-7 overflow-hidden rounded-[28px] px-6 py-8 text-white sm:px-11 sm:py-10"
      style={{
        background: "var(--v2-bg-hero)",
        boxShadow: "var(--v2-shadow-glow)",
      }}
    >
      {/* Glow */}
      <div
        className="pointer-events-none absolute inset-0"
        style={{ background: "var(--v2-bg-hero-glow)" }}
      />
      {/* Decorative circle — trống đồng */}
      <div
        className="pointer-events-none absolute -top-15 -right-10 h-80 w-80 rounded-full border-2"
        style={{
          borderColor: "rgba(200, 168, 78, 0.06)",
          boxShadow:
            "0 0 0 24px rgba(200, 168, 78, 0.03), 0 0 0 48px rgba(200, 168, 78, 0.02), 0 0 0 72px rgba(200, 168, 78, 0.01)",
        }}
      />

      {/* Top row */}
      <div className="relative z-10 flex items-start justify-between">
        <div className="flex items-center gap-5 sm:gap-6">
          {/* Big day number */}
          <div
            className="font-playfair text-[60px] leading-none font-bold sm:text-[88px]"
            style={{ color: "var(--v2-bg-gold)", textShadow: "0 4px 20px rgba(0,0,0,0.3)" }}
          >
            {String(data.solar_day).padStart(2, "0")}
          </div>
          <div className="flex flex-col gap-0.5">
            <div className="text-lg font-semibold sm:text-xl" style={{ letterSpacing: "0.5px" }}>
              {data.day_of_week}
            </div>
            <div className="text-sm opacity-70 font-light">
              Tháng {data.solar_month}, {data.solar_year}
            </div>
            <div
              className="mt-1.5 inline-flex items-center gap-2 rounded-full border px-3.5 py-1 text-[13px] backdrop-blur-sm"
              style={{
                background: "rgba(255,255,255,0.1)",
                borderColor: "rgba(255,255,255,0.08)",
              }}
            >
              <span style={{ color: "var(--v2-bg-gold)" }}>☾</span>
              {data.lunar_day_name} · {data.lunar_month_name} · Năm {data.tu_tru.nam.can_chi}
            </div>
          </div>
        </div>

        {/* Action buttons */}
        <div className="relative z-10 flex gap-2">
          <BookmarkButton date={dateStr} />
          <button
            className="flex h-[38px] w-[38px] items-center justify-center rounded-lg border text-[15px] text-white/80 transition-all hover:-translate-y-0.5 hover:bg-white/15 hover:text-[var(--v2-bg-gold)]"
            style={{ borderColor: "rgba(255,255,255,0.15)", background: "rgba(255,255,255,0.06)" }}
            title="Nhắc nhở"
          >
            <Bell className="h-4 w-4" />
          </button>
          <button
            className="flex h-[38px] w-[38px] items-center justify-center rounded-lg border text-[15px] text-white/80 transition-all hover:-translate-y-0.5 hover:bg-white/15 hover:text-[var(--v2-bg-gold)]"
            style={{ borderColor: "rgba(255,255,255,0.15)", background: "rgba(255,255,255,0.06)" }}
            title="Chia sẻ"
          >
            <Share2 className="h-4 w-4" />
          </button>
        </div>
      </div>

      {/* Meta chips */}
      <div className="relative z-10 mt-6 flex flex-col gap-2 sm:mt-7 sm:flex-row sm:gap-4">
        <MetaChip icon="🐴" label="Con giáp" value={`${data.tu_tru.nam.chi} (${data.tu_tru.nam.con_giap})`} />
        <MetaChip icon="🔥" label="Ngũ hành" value={data.tu_tru.ngay.ngu_hanh} />
        <MetaChip
          icon="🧭"
          label="Hướng tốt"
          value={data.phong_thuy.huong_xuat_hanh.huong_tot.slice(0, 2).join(", ")}
        />
      </div>
    </section>
  );
}

function MetaChip({ icon, label, value }: { icon: string; label: string; value: string }) {
  return (
    <div
      className="flex flex-1 items-center gap-2.5 rounded-xl border px-3.5 py-2.5 backdrop-blur-sm sm:gap-3 sm:px-4 sm:py-3"
      style={{
        background: "rgba(255,255,255,0.06)",
        borderColor: "rgba(255,255,255,0.06)",
      }}
    >
      <div
        className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-[16px]"
        style={{ background: "rgba(200, 168, 78, 0.15)", color: "var(--v2-bg-gold)" }}
      >
        {icon}
      </div>
      <div>
        <div className="text-[10px] font-medium uppercase tracking-wider opacity-50">
          {label}
        </div>
        <div className="text-[13px] font-semibold">{value}</div>
      </div>
    </div>
  );
}
