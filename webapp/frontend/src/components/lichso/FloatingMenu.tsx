"use client";

import { useState, useCallback } from "react";
import {
  IconCalendar,
  IconSearch,
  IconInfo,
  IconGoodDays,
  IconActivities,
  IconSolarTerm,
  IconConvert,
  IconCompass,
  IconBookmark,
  IconReminder,
  IconExport,
} from "./icons";

export type MenuPanel =
  | "info"
  | "calendar"
  | "activities"
  | "compass"
  | "search"
  | "good-days"
  | "solar-terms"
  | "convert"
  | "bookmarks"
  | "reminders"
  | "export"
  | null;

interface FloatingMenuProps {
  onPanelChange: (panel: MenuPanel) => void;
  activePanel: MenuPanel;
}

/* ─────────────────────────────────────────
   Icon component lookup
   ───────────────────────────────────────── */
const ICON_MAP: Record<string, React.FC<{ className?: string; style?: React.CSSProperties }>> = {
  calendar: IconCalendar,
  search: IconSearch,
  info: IconInfo,
  "good-days": IconGoodDays,
  activities: IconActivities,
  "solar-terms": IconSolarTerm,
  convert: IconConvert,
  compass: IconCompass,
  bookmarks: IconBookmark,
  reminders: IconReminder,
  export: IconExport,
};

/* ─────────────────────────────────────────
   Bubble config — LARGE desktop bubbles
   Sized 140-180px for full readability
   Spread wide to fill the viewport
   ───────────────────────────────────────── */
type FloatAnim = "bubbleFloat1" | "bubbleFloat2" | "bubbleFloat3";

interface BubbleItem {
  id: MenuPanel;
  label: string;
  name: string;
  size: number;
  iconSize: number;
  textSize: number;
  gradient: string;
  shadowColor: string;
  labelColor: string;
  float: FloatAnim;
  floatDuration: string;
  floatDelay: string;
  /** Fixed position offsets from viewport center: calc(50% + offsetX) */
  offsetX: number;
  offsetY: number;
  appearDelay: string;
}

const BUBBLES: BubbleItem[] = [
  {
    id: "calendar",
    label: "LỊCH THÁNG",
    name: "Lịch Tháng",
    size: 170,
    iconSize: 40,
    textSize: 14,
    gradient: "radial-gradient(circle at 35% 35%, #f5edd5, #d4b06a)",
    shadowColor: "rgba(200,144,42,0.35)",
    labelColor: "#c8902a",
    float: "bubbleFloat1",
    floatDuration: "7s",
    floatDelay: "0s",
    offsetX: -390,
    offsetY: -200,
    appearDelay: "0.1s",
  },
  {
    id: "search",
    label: "TRA CỨU",
    name: "Tra Cứu",
    size: 140,
    iconSize: 34,
    textSize: 13,
    gradient: "radial-gradient(circle at 35% 35%, #d8f0e8, #5aa08a)",
    shadowColor: "rgba(61,128,112,0.35)",
    labelColor: "#3d8070",
    float: "bubbleFloat2",
    floatDuration: "8s",
    floatDelay: "0.5s",
    offsetX: 130,
    offsetY: -340,
    appearDelay: "0.2s",
  },
  {
    id: "compass",
    label: "HƯỚNG ĐI",
    name: "Hướng Đi",
    size: 155,
    iconSize: 36,
    textSize: 14,
    gradient: "radial-gradient(circle at 35% 35%, #fde8cc, #c86020)",
    shadowColor: "rgba(200,96,32,0.3)",
    labelColor: "#c86020",
    float: "bubbleFloat3",
    floatDuration: "9s",
    floatDelay: "1s",
    offsetX: 430,
    offsetY: -60,
    appearDelay: "0.3s",
  },
  {
    id: "solar-terms",
    label: "TIẾT KHÍ",
    name: "Tiết Khí",
    size: 160,
    iconSize: 38,
    textSize: 14,
    gradient: "radial-gradient(circle at 35% 35%, #e8f4d8, #6a9a40)",
    shadowColor: "rgba(80,140,40,0.3)",
    labelColor: "#5a8a30",
    float: "bubbleFloat1",
    floatDuration: "7.5s",
    floatDelay: "1.5s",
    offsetX: 400,
    offsetY: 230,
    appearDelay: "0.4s",
  },
  {
    id: "activities",
    label: "NÊN / KỴ",
    name: "Nên / Kỵ",
    size: 150,
    iconSize: 36,
    textSize: 13,
    gradient: "radial-gradient(circle at 35% 35%, #e8e4fc, #7060c0)",
    shadowColor: "rgba(96,80,192,0.28)",
    labelColor: "#7060c0",
    float: "bubbleFloat2",
    floatDuration: "8.5s",
    floatDelay: "0.8s",
    offsetX: -440,
    offsetY: 100,
    appearDelay: "0.5s",
  },
  {
    id: "good-days",
    label: "NGÀY TỐT",
    name: "Ngày Tốt",
    size: 145,
    iconSize: 34,
    textSize: 13,
    gradient: "radial-gradient(circle at 35% 35%, #fddada, #c03030)",
    shadowColor: "rgba(192,48,48,0.3)",
    labelColor: "#c03030",
    float: "bubbleFloat3",
    floatDuration: "7s",
    floatDelay: "0.3s",
    offsetX: 20,
    offsetY: 360,
    appearDelay: "0.6s",
  },
  {
    id: "convert",
    label: "ĐỔI LỊCH",
    name: "Đổi Lịch",
    size: 130,
    iconSize: 30,
    textSize: 12,
    gradient: "radial-gradient(circle at 35% 35%, #f8e8c8, #b07828)",
    shadowColor: "rgba(176,120,40,0.28)",
    labelColor: "#a07020",
    float: "bubbleFloat1",
    floatDuration: "9s",
    floatDelay: "2s",
    offsetX: -200,
    offsetY: -340,
    appearDelay: "0.7s",
  },
  {
    id: "info",
    label: "CHI TIẾT",
    name: "Chi Tiết",
    size: 135,
    iconSize: 32,
    textSize: 12,
    gradient: "radial-gradient(circle at 35% 35%, #d8e8f8, #3060a0)",
    shadowColor: "rgba(48,96,160,0.28)",
    labelColor: "#3060a0",
    float: "bubbleFloat2",
    floatDuration: "8s",
    floatDelay: "1.2s",
    offsetX: -380,
    offsetY: 300,
    appearDelay: "0.8s",
  },
  {
    id: "bookmarks",
    label: "BOOKMARK",
    name: "Bookmark",
    size: 130,
    iconSize: 30,
    textSize: 12,
    gradient: "radial-gradient(circle at 35% 35%, #fef3c7, #d97706)",
    shadowColor: "rgba(217,119,6,0.28)",
    labelColor: "#d97706",
    float: "bubbleFloat3",
    floatDuration: "7.5s",
    floatDelay: "1.8s",
    offsetX: 260,
    offsetY: -320,
    appearDelay: "0.9s",
  },
  {
    id: "reminders",
    label: "NHẮC NHỞ",
    name: "Nhắc Nhở",
    size: 125,
    iconSize: 28,
    textSize: 12,
    gradient: "radial-gradient(circle at 35% 35%, #fce4ec, #c62828)",
    shadowColor: "rgba(198,40,40,0.25)",
    labelColor: "#c62828",
    float: "bubbleFloat1",
    floatDuration: "8.5s",
    floatDelay: "2.5s",
    offsetX: -160,
    offsetY: 350,
    appearDelay: "1.0s",
  },
  {
    id: "export",
    label: "XUẤT LỊCH",
    name: "Xuất Lịch",
    size: 120,
    iconSize: 28,
    textSize: 11,
    gradient: "radial-gradient(circle at 35% 35%, #e8eaf6, #3949ab)",
    shadowColor: "rgba(57,73,171,0.25)",
    labelColor: "#3949ab",
    float: "bubbleFloat2",
    floatDuration: "9s",
    floatDelay: "3s",
    offsetX: 330,
    offsetY: 330,
    appearDelay: "1.1s",
  },
];

/* ─────────────────────────────────────────
   Component
   ───────────────────────────────────────── */
export function FloatingMenu({ onPanelChange, activePanel }: FloatingMenuProps) {
  const [hoveredId, setHoveredId] = useState<MenuPanel>(null);

  const handleClick = useCallback(
    (id: MenuPanel) => {
      onPanelChange(activePanel === id ? null : id);
    },
    [activePanel, onPanelChange]
  );

  return (
    <>
      {/* ── Desktop: large floating gradient bubbles ── */}
      <div className="hidden lg:block">
        {BUBBLES.map((bubble) => {
          const isActive = activePanel === bubble.id;
          const isHovered = hoveredId === bubble.id;
          const isVivid = isActive || isHovered;
          const Icon = ICON_MAP[bubble.id as string];

          return (
            <div
              key={bubble.id}
              className="fixed z-30"
              style={{
                top: `calc(50% + ${bubble.offsetY}px)`,
                left: `calc(50% + ${bubble.offsetX}px)`,
                marginLeft: -(bubble.size / 2),
                marginTop: -(bubble.size / 2),
                animation: `bubbleAppear 0.6s cubic-bezier(0.34,1.56,0.64,1) ${bubble.appearDelay} both`,
              }}
            >
              {/* Float wrapper */}
              <div
                style={{
                  animation: `${bubble.float} ${bubble.floatDuration} ease-in-out ${bubble.floatDelay} infinite`,
                }}
              >
                <button
                  onClick={() => handleClick(bubble.id)}
                  onMouseEnter={() => setHoveredId(bubble.id)}
                  onMouseLeave={() => setHoveredId(null)}
                  className="relative flex cursor-pointer flex-col items-center justify-center rounded-full select-none"
                  style={{
                    width: bubble.size,
                    height: bubble.size,
                    background: bubble.gradient,
                    boxShadow: isVivid
                      ? `0 14px 48px ${bubble.shadowColor}, inset 0 2px 0 rgba(255,255,255,0.5)`
                      : `0 6px 24px ${bubble.shadowColor}, inset 0 1px 0 rgba(255,255,255,0.4)`,
                    opacity: isVivid ? 1 : 0.25,
                    filter: isVivid ? "blur(0px) saturate(1.1)" : "blur(0.5px) saturate(0.55)",
                    transform: isVivid ? "scale(1.08)" : "scale(0.95)",
                    transition: "all 0.45s cubic-bezier(0.34, 1.56, 0.64, 1)",
                    overflow: "hidden",
                  }}
                  aria-label={bubble.label}
                >
                  {/* Glossy highlight */}
                  <div
                    className="pointer-events-none absolute inset-0 rounded-full"
                    style={{
                      background:
                        "radial-gradient(ellipse 60% 50% at 35% 25%, rgba(255,255,255,0.45), transparent 70%)",
                    }}
                  />
                  {/* Icon */}
                  {Icon && (
                    <Icon
                      className="relative z-[1] text-white/90 drop-shadow-sm"
                      style={
                        {
                          width: bubble.iconSize,
                          height: bubble.iconSize,
                        } as React.CSSProperties
                      }
                    />
                  )}
                  {/* Name text inside bubble */}
                  <span
                    className="relative z-[1] mt-1.5 font-semibold"
                    style={{
                      fontSize: bubble.textSize,
                      letterSpacing: "0.5px",
                      color: "rgba(255,255,255,0.95)",
                      textShadow: "0 1px 4px rgba(0,0,0,0.25)",
                    }}
                  >
                    {bubble.name}
                  </span>
                </button>

                {/* Tooltip label below bubble */}
                <span
                  className="pointer-events-none absolute left-1/2 -translate-x-1/2 font-bold tracking-[2px] whitespace-nowrap uppercase transition-opacity duration-300"
                  style={{
                    bottom: -28,
                    fontSize: "11px",
                    color: bubble.labelColor,
                    opacity: isVivid ? 1 : 0,
                  }}
                >
                  {bubble.label}
                </span>
              </div>
            </div>
          );
        })}
      </div>

      {/* ── Mobile: grid menu ── */}
      <div className="mt-6 w-full lg:hidden">
        <div className="mx-auto grid max-w-[420px] grid-cols-4 gap-2.5 px-2">
          {BUBBLES.map((bubble) => {
            const isActive = activePanel === bubble.id;
            const Icon = ICON_MAP[bubble.id as string];
            return (
              <button
                key={bubble.id}
                onClick={() => handleClick(bubble.id)}
                className="flex flex-col items-center gap-1.5 rounded-2xl px-2 py-3 text-center transition-all duration-250 active:scale-95"
                style={{
                  background: isActive ? "var(--ls-card-bg-solid)" : "var(--ls-card-bg-solid)",
                  border: isActive
                    ? `1.5px solid ${bubble.labelColor}40`
                    : "1px solid var(--ls-border-soft)",
                  boxShadow: isActive
                    ? `0 4px 16px ${bubble.shadowColor}`
                    : "0 4px 12px rgba(100,60,20,0.08)",
                  transform: isActive ? "translateY(-2px)" : "none",
                }}
                aria-label={bubble.label}
              >
                {Icon && (
                  <Icon
                    className="transition-colors"
                    style={
                      {
                        width: 22,
                        height: 22,
                        color: isActive ? bubble.labelColor : "var(--ls-text-soft)",
                      } as React.CSSProperties
                    }
                  />
                )}
                <span
                  className="text-[10px] leading-tight font-semibold"
                  style={{
                    color: isActive ? bubble.labelColor : "var(--ls-text-soft)",
                  }}
                >
                  {bubble.name}
                </span>
              </button>
            );
          })}
        </div>
      </div>
    </>
  );
}
