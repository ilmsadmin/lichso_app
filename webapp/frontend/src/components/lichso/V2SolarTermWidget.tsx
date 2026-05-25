"use client";

import type { CurrentSolarTermInfo } from "@/types/calendar";
import { Sprout, Clock } from "lucide-react";

interface V2SolarTermWidgetProps {
  solarTerm: CurrentSolarTermInfo;
}

export function V2SolarTermWidget({ solarTerm }: V2SolarTermWidgetProps) {
  return (
    <div
      className="v2-card rounded-xl p-5"
      style={{
        background: "var(--v2-bg-card)",
        border: "1px solid var(--v2-border-primary)",
        boxShadow: "var(--v2-shadow-xs)",
      }}
    >
      {/* Header */}
      <div className="mb-3.5 flex items-center gap-2.5">
        <div
          className="flex h-8 w-8 items-center justify-center rounded-lg"
          style={{ background: "rgba(34, 139, 34, 0.08)", color: "#228B22" }}
        >
          <Sprout className="h-4 w-4" />
        </div>
        <h3 className="text-[16px] font-bold" style={{ color: "var(--v2-text-primary)" }}>
          Tiết Khí
        </h3>
      </div>

      {/* Current Solar Term */}
      <div
        className="mb-3.5 flex items-center gap-3 rounded-xl p-3.5"
        style={{
          background: "linear-gradient(135deg, rgba(34,139,34,0.06), rgba(200,168,78,0.06))",
        }}
      >
        <div
          className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl text-xl text-white"
          style={{ background: "linear-gradient(135deg, #228B22, #2EAA2E)" }}
        >
          🌸
        </div>
        <div>
          <div
            className="font-playfair text-[16px] font-bold"
            style={{ color: "var(--v2-text-primary)" }}
          >
            {solarTerm.current.name}
            {solarTerm.current.han_tu && (
              <span className="ml-1.5 text-[13px] font-normal" style={{ color: "var(--v2-text-muted)" }}>
                ({solarTerm.current.han_tu})
              </span>
            )}
          </div>
          <div className="text-[12px]" style={{ color: "var(--v2-text-muted)" }}>
            {solarTerm.current.date}
          </div>
        </div>
      </div>

      {/* Progress bar */}
      <div className="mb-3">
        <div
          className="h-1.5 w-full overflow-hidden rounded-full"
          style={{ background: "var(--v2-bg-hover)" }}
        >
          <div
            className="h-full rounded-full transition-all"
            style={{
              width: `${solarTerm.progress * 100}%`,
              background: "linear-gradient(90deg, #228B22, var(--v2-bg-gold))",
            }}
          />
        </div>
      </div>

      {/* Next Solar Term */}
      <div
        className="flex items-center justify-between pt-3"
        style={{ borderTop: "1px solid var(--v2-border-light)" }}
      >
        <div>
          <div className="text-[11px]" style={{ color: "var(--v2-text-muted)" }}>
            Tiết khí tiếp theo
          </div>
          <div className="text-[13px] font-semibold" style={{ color: "var(--v2-text-primary)" }}>
            {solarTerm.next.name}
            {solarTerm.next.han_tu && (
              <span className="ml-1 text-[12px] font-normal" style={{ color: "var(--v2-text-muted)" }}>
                ({solarTerm.next.han_tu})
              </span>
            )}
          </div>
        </div>
        <span
          className="flex items-center gap-1 rounded-full px-2.5 py-1 text-[11px] font-semibold"
          style={{ background: "var(--v2-bg-accent-soft)", color: "var(--v2-text-accent)" }}
        >
          <Clock className="h-3 w-3" />
          {solarTerm.days_left} ngày
        </span>
      </div>
    </div>
  );
}
