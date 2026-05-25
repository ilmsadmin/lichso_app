"use client";

import { useState, useEffect, useRef } from "react";
import { useStreamHoroscope, useHoroscopeQuota } from "@/hooks/useAI";
import type { HoroscopeAIRequest, HoroscopeReadingType, NguHanhBalance, BatTuInfo } from "@/types/ai";
import { AIStreamingText } from "./AIStreamingText";
import { TuViChart } from "./TuViChart";
import { Loader2 } from "lucide-react";

// ─── Icon System ──────────────────────────────────────────────────────────────
// Bộ icon SVG inline, stroke-based, phong cách huyền học / tử vi
// Dùng thống nhất toàn wizard, kích thước và màu truyền qua props

type IconProps = { size?: number; color?: string; strokeWidth?: number };

// Ngôi sao 8 cánh kiểu bát quái
function IconOctagram({ size = 20, color = "currentColor", strokeWidth = 1.5 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round">
      <polygon points="12,2 14.5,9.5 22,12 14.5,14.5 12,22 9.5,14.5 2,12 9.5,9.5" />
      <circle cx="12" cy="12" r="2.5" fill={color} stroke="none" />
    </svg>
  );
}

// Người — silhouette nhẹ, thanh lịch
function IconPerson({ size = 20, color = "currentColor", strokeWidth = 1.5 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="7" r="3.5" />
      <path d="M5 21c0-4 3.1-7 7-7s7 3 7 7" />
    </svg>
  );
}

// Lịch / ngày sinh — vuông với điểm tháng ngày
function IconCalendar({ size = 20, color = "currentColor", strokeWidth = 1.5 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="5" width="18" height="16" rx="3" />
      <path d="M8 3v4M16 3v4" />
      <path d="M3 10h18" />
      <circle cx="8" cy="15" r="1" fill={color} stroke="none" />
      <circle cx="12" cy="15" r="1" fill={color} stroke="none" />
      <circle cx="16" cy="15" r="1" fill={color} stroke="none" />
    </svg>
  );
}

// Cuộn giấy / lá số
function IconScroll({ size = 20, color = "currentColor", strokeWidth = 1.5 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round">
      <path d="M6 20h12a2 2 0 0 0 2-2V7l-5-5H6a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2z" />
      <path d="M14 2v5h5" />
      <path d="M8 13h8M8 17h5" />
    </svg>
  );
}

// Tinh tú / luận giải — hình ngôi sao + sóng AI
function IconConstellation({ size = 20, color = "currentColor", strokeWidth = 1.5 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="5" cy="6" r="1.5" fill={color} stroke="none" />
      <circle cx="12" cy="3" r="1.5" fill={color} stroke="none" />
      <circle cx="19" cy="7" r="1.5" fill={color} stroke="none" />
      <circle cx="15" cy="14" r="1.5" fill={color} stroke="none" />
      <circle cx="8" cy="17" r="1.5" fill={color} stroke="none" />
      <path d="M5 6l7-3 7 4M19 7l-4 7M15 14l-7 3M8 17L5 6" strokeWidth={1} opacity={0.6} />
    </svg>
  );
}

// ─── Feature Chip Icons (Welcome Step) ────────────────────────────────────────

// Tia sét / AI realtime
function IconChipBolt({ size = 14, color = "currentColor" }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 16 16" fill="none" stroke={color} strokeWidth={1.6} strokeLinecap="round" strokeLinejoin="round">
      <path d="M9.5 1.5L4 9h4l-1 5.5L13 7H9l.5-5.5z" />
    </svg>
  );
}

// La bàn / Bát Tự chính xác
function IconChipCompass({ size = 14, color = "currentColor" }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 16 16" fill="none" stroke={color} strokeWidth={1.5} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="8" cy="8" r="6.5" />
      <polygon points="8,3.5 9.5,7 8,12.5 6.5,9" fill={color} opacity={0.25} stroke={color} strokeWidth={1.2} />
      <circle cx="8" cy="8" r="1" fill={color} stroke="none" />
    </svg>
  );
}

// Ngũ hành / giọt nước + lửa — tượng trưng ngũ hành
function IconChipElements({ size = 14, color = "currentColor" }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 16 16" fill="none" stroke={color} strokeWidth={1.5} strokeLinecap="round" strokeLinejoin="round">
      <path d="M8 2C8 2 12.5 6.5 12.5 9.5C12.5 12 10.5 14 8 14C5.5 14 3.5 12 3.5 9.5C3.5 6.5 8 2 8 2Z" />
      <path d="M8 8.5C8 8.5 9.8 10 9.8 11C9.8 12 9 12.8 8 12.8C7 12.8 6.2 12 6.2 11C6.2 10 8 8.5 8 8.5Z" opacity={0.5} />
    </svg>
  );
}

// Cuộn sách / luận giải chuyên sâu
function IconChipBook({ size = 14, color = "currentColor" }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 16 16" fill="none" stroke={color} strokeWidth={1.5} strokeLinecap="round" strokeLinejoin="round">
      <path d="M2.5 2.5C2.5 2.5 5 1.5 8 3C11 1.5 13.5 2.5 13.5 2.5V12.5C13.5 12.5 11 11.5 8 13C5 11.5 2.5 12.5 2.5 12.5V2.5Z" />
      <path d="M8 3v10" />
    </svg>
  );
}

// Tinh cầu / welcome orb — vòng tròn + chữ thập năng lượng
function IconCrystalOrb({ size = 48, color = "#fff", strokeWidth = 1.2 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 48 48" fill="none" stroke={color} strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="24" cy="24" r="18" />
      <ellipse cx="24" cy="24" rx="9" ry="18" />
      <ellipse cx="24" cy="24" rx="18" ry="7" />
      <path d="M24 6v36M6 24h36" strokeWidth={0.8} opacity={0.4} />
      <circle cx="24" cy="24" r="4" fill={color} opacity={0.9} stroke="none" />
      <circle cx="18" cy="14" r="2" fill={color} opacity={0.5} stroke="none" />
    </svg>
  );
}

// Người dùng — header Step 1
function IconUserLarge({ size = 52, color = "#fff", strokeWidth = 1.2 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 52 52" fill="none" stroke={color} strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="26" cy="16" r="8" />
      <path d="M10 44c0-8.8 7.2-16 16-16s16 7.2 16 16" />
      <path d="M26 8 L26 4M20 10 L16 7M32 10 L36 7" strokeWidth={0.8} opacity={0.5} />
    </svg>
  );
}

// Lịch âm — header Step 2
function IconCalendarLarge({ size = 52, color = "#fff", strokeWidth = 1.2 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 52 52" fill="none" stroke={color} strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round">
      <rect x="6" y="11" width="40" height="35" rx="5" />
      <path d="M6 22h40" />
      <path d="M17 6v10M35 6v10" strokeWidth={1.4} />
      <circle cx="17" cy="32" r="2" fill={color} stroke="none" opacity={0.8} />
      <circle cx="26" cy="32" r="2" fill={color} stroke="none" opacity={0.8} />
      <circle cx="35" cy="32" r="2" fill={color} stroke="none" opacity={0.8} />
      <circle cx="17" cy="40" r="2" fill={color} stroke="none" opacity={0.5} />
      <circle cx="26" cy="40" r="2" fill={color} stroke="none" opacity={0.5} />
    </svg>
  );
}

// Bát quái / lá số — header Step 3
function IconBaguaLarge({ size = 52, color = "#fff", strokeWidth = 1 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 52 52" fill="none" stroke={color} strokeWidth={strokeWidth} strokeLinecap="round">
      <circle cx="26" cy="26" r="20" />
      <circle cx="26" cy="26" r="8" />
      {/* 8 trigrams lines */}
      {[0,1,2,3,4,5,6,7].map(i => {
        const a = (i * 45 - 90) * Math.PI / 180;
        const x1 = 26 + 11 * Math.cos(a), y1 = 26 + 11 * Math.sin(a);
        const x2 = 26 + 18 * Math.cos(a), y2 = 26 + 18 * Math.sin(a);
        return <line key={i} x1={x1} y1={y1} x2={x2} y2={y2} strokeWidth={1.5} opacity={0.7} />;
      })}
      <circle cx="26" cy="26" r="3" fill={color} opacity={0.9} stroke="none" />
    </svg>
  );
}

// Tia sáng AI / luận giải — header Step 4
function IconAIStarsLarge({ size = 52, color = "#fff", strokeWidth = 1.2 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 52 52" fill="none" stroke={color} strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round">
      {/* Ngôi sao chính */}
      <path d="M26 6 L28.5 20 L42 22.5 L28.5 25 L26 39 L23.5 25 L10 22.5 L23.5 20 Z" />
      {/* Ngôi sao nhỏ */}
      <path d="M40 8 L41 13 L46 14 L41 15 L40 20 L39 15 L34 14 L39 13 Z" opacity={0.7} />
      <path d="M10 34 L10.8 37.5 L14.5 38.3 L10.8 39 L10 42.5 L9.2 39 L5.5 38.3 L9.2 37.5 Z" opacity={0.5} />
      <circle cx="26" cy="22.5" r="3" fill={color} opacity={0.9} stroke="none" />
    </svg>
  );
}

// Icons cho READING_TYPES — nhỏ, đồng bộ stroke style
function IconOverview({ size = 18, color = "currentColor" }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 20 20" fill="none" stroke={color} strokeWidth={1.6} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="10" cy="10" r="7.5" />
      <path d="M10 2.5v15M2.5 10h15" opacity={0.5} />
      <circle cx="10" cy="10" r="3" />
    </svg>
  );
}
function IconYearly({ size = 18, color = "currentColor" }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 20 20" fill="none" stroke={color} strokeWidth={1.6} strokeLinecap="round" strokeLinejoin="round">
      <path d="M10 2 Q17 6 17 10 Q17 16 10 18 Q3 16 3 10 Q3 6 10 2Z" />
      <path d="M10 6v5l3 2" />
    </svg>
  );
}
function IconMonthly({ size = 18, color = "currentColor" }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 20 20" fill="none" stroke={color} strokeWidth={1.6} strokeLinecap="round" strokeLinejoin="round">
      <path d="M10 2.5 C10 2.5 17 7 17 12 C17 15.5 13.9 17.5 10 17.5 C6.1 17.5 3 15.5 3 12 C3 7 10 2.5 10 2.5Z" />
      <path d="M10 2.5 Q10 10 10 17.5" opacity={0.4} />
    </svg>
  );
}
function IconQuestion({ size = 18, color = "currentColor" }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 20 20" fill="none" stroke={color} strokeWidth={1.6} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="10" cy="10" r="7.5" />
      <path d="M7.5 8C7.5 6.6 8.6 5.5 10 5.5C11.4 5.5 12.5 6.6 12.5 8C12.5 9 12 9.5 11 10.2C10.3 10.7 10 11.2 10 12" />
      <circle cx="10" cy="14.5" r="0.8" fill={color} stroke="none" />
    </svg>
  );
}
function IconCompatibility({ size = 18, color = "currentColor" }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 20 20" fill="none" stroke={color} strokeWidth={1.6} strokeLinecap="round" strokeLinejoin="round">
      <circle cx="6.5" cy="7" r="3.5" />
      <circle cx="13.5" cy="7" r="3.5" />
      <path d="M3 17c0-3 1.6-5 3.5-5" />
      <path d="M17 17c0-3-1.6-5-3.5-5" />
      <path d="M10 14.5 L10 17" strokeWidth={1.2} />
    </svg>
  );
}
function IconChooseDate({ size = 18, color = "currentColor" }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 20 20" fill="none" stroke={color} strokeWidth={1.6} strokeLinecap="round" strokeLinejoin="round">
      <rect x="2.5" y="4" width="15" height="13" rx="2.5" />
      <path d="M2.5 9h15" />
      <path d="M6.5 2v4M13.5 2v4" />
      <path d="M7 13l2 2 4-4" />
    </svg>
  );
}

// Checkmark hoàn thành trong step indicator
function IconCheck({ size = 12, color = "#fff" }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 12 12" fill="none" stroke={color} strokeWidth={2} strokeLinecap="round" strokeLinejoin="round">
      <path d="M2 6l3 3 5-5" />
    </svg>
  );
}

// Mũi tên phải — điều hướng
function IconChevronRight({ size = 16, color = "currentColor" }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 16 16" fill="none" stroke={color} strokeWidth={2} strokeLinecap="round" strokeLinejoin="round">
      <path d="M6 4l4 4-4 4" />
    </svg>
  );
}

// Mũi tên trái — điều hướng
function IconChevronLeft({ size = 16, color = "currentColor" }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 16 16" fill="none" stroke={color} strokeWidth={2} strokeLinecap="round" strokeLinejoin="round">
      <path d="M10 4L6 8l4 4" />
    </svg>
  );
}

// Tia sáng AI — bắt đầu luận giải
function IconSparkle({ size = 16, color = "currentColor" }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 16 16" fill="none" stroke={color} strokeWidth={1.5} strokeLinecap="round" strokeLinejoin="round">
      <path d="M8 1 L9 6 L14 7 L9 8 L8 13 L7 8 L2 7 L7 6 Z" />
      <path d="M13 1 L13.5 3 L15 3.5 L13.5 4 L13 6 L12.5 4 L11 3.5 L12.5 3 Z" opacity={0.7} />
    </svg>
  );
}

// Não AI → dùng icon tinh tú nhỏ cho inline card title
function IconAIInline({ size = 14, color = "currentColor" }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 14 14" fill="none" stroke={color} strokeWidth={1.5} strokeLinecap="round" strokeLinejoin="round">
      <path d="M7 1 L8 5.5 L12.5 6.5 L8 7.5 L7 12 L6 7.5 L1.5 6.5 L6 5.5 Z" />
      <circle cx="7" cy="6.5" r="1.2" fill={color} stroke="none" opacity={0.8} />
    </svg>
  );
}

// Quay lại / reset
function IconReset({ size = 14, color = "currentColor" }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 14 14" fill="none" stroke={color} strokeWidth={1.6} strokeLinecap="round" strokeLinejoin="round">
      <path d="M2.5 7 A4.5 4.5 0 1 0 4 3.5" />
      <path d="M2.5 2v2.5H5" />
    </svg>
  );
}

// ─── Constants ─────────────────────────────────────────────────────────────
// ─── Reading types với mô tả chi tiết ──────────────────────────────────────
const READING_TYPES: { value: HoroscopeReadingType; label: string; svgIcon: React.ReactNode; desc: string; color: string }[] = [
  { value: "overview",      label: "Tổng quan bản mệnh",   svgIcon: <IconOverview     size={22} color="#fff" />, desc: "Phân tích toàn diện: tính cách, số phận, bản mệnh ngũ hành",    color: "#7c3aed" },
  { value: "yearly",        label: "Vận hạn năm 2026",     svgIcon: <IconYearly       size={22} color="#fff" />, desc: "Vận may, tài lộc, sự nghiệp, tình duyên trong năm hiện tại",    color: "#d97706" },
  { value: "monthly",       label: "Vận hạn tháng này",    svgIcon: <IconMonthly      size={22} color="#fff" />, desc: "Dự báo chi tiết cho tháng hiện tại theo từng lĩnh vực",         color: "#2563eb" },
  { value: "question",      label: "Hỏi cụ thể",           svgIcon: <IconQuestion     size={22} color="#fff" />, desc: "Đặt câu hỏi về tình yêu, công việc, sức khỏe, tài chính...",    color: "#dc2626" },
  { value: "compatibility",  label: "Hợp / Khắc tuổi",    svgIcon: <IconCompatibility size={22} color="#fff" />, desc: "Phân tích mức độ hợp khắc với người thân, đối tác, bạn đời",   color: "#db2777" },
  { value: "choose_date",   label: "Chọn ngày tốt",        svgIcon: <IconChooseDate   size={22} color="#fff" />, desc: "Tư vấn ngày giờ tốt để xuất hành, ký kết, cưới hỏi, khai trương", color: "#16a34a" },
];

const MONTH_NAMES = [
  "Tháng 1","Tháng 2","Tháng 3","Tháng 4","Tháng 5","Tháng 6",
  "Tháng 7","Tháng 8","Tháng 9","Tháng 10","Tháng 11","Tháng 12",
];

const GMT_OFFSETS = [
  { label: "GMT -12", value: -12 }, { label: "GMT -11", value: -11 },
  { label: "GMT -10", value: -10 }, { label: "GMT -9",  value: -9  },
  { label: "GMT -8",  value: -8  }, { label: "GMT -7",  value: -7  },
  { label: "GMT -6",  value: -6  }, { label: "GMT -5",  value: -5  },
  { label: "GMT -4",  value: -4  }, { label: "GMT -3",  value: -3  },
  { label: "GMT -2",  value: -2  }, { label: "GMT -1",  value: -1  },
  { label: "GMT +0",  value: 0   }, { label: "GMT +1",  value: 1   },
  { label: "GMT +2",  value: 2   }, { label: "GMT +3",  value: 3   },
  { label: "GMT +4",  value: 4   }, { label: "GMT +5",  value: 5   },
  { label: "GMT +5:30", value: 5.5 }, { label: "GMT +6", value: 6  },
  { label: "GMT +7",  value: 7   }, { label: "GMT +8",  value: 8   },
  { label: "GMT +9",  value: 9   }, { label: "GMT +10", value: 10  },
  { label: "GMT +11", value: 11  }, { label: "GMT +12", value: 12  },
];

const currentYear = new Date().getFullYear();

function getDaysInMonth(month: number, year: number) {
  return new Date(year, month, 0).getDate();
}

// ─── Step Indicator ─────────────────────────────────────────────────────────
const STEPS = [
  { icon: IconOctagram,      label: "Chào" },
  { icon: IconPerson,        label: "Họ tên" },
  { icon: IconCalendar,      label: "Ngày sinh" },
  { icon: IconScroll,        label: "Lá số" },
  { icon: IconConstellation, label: "Luận giải" },
];

function StepIndicator({ current }: { current: number }) {
  return (
    <div className="flex items-center justify-center gap-0 mb-8">
      {STEPS.map((s, i) => {
        const Icon = s.icon;
        const done = i < current;
        const active = i === current;
        return (
          <div key={i} className="flex items-center">
            <div className="flex flex-col items-center gap-1">
              <div
                style={{
                  width: 36,
                  height: 36,
                  borderRadius: "50%",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  transition: "all 0.3s ease",
                  background: done
                    ? "linear-gradient(135deg,#c4783a,#3d806e)"
                    : active
                    ? "linear-gradient(135deg,#a855f7,#6366f1)"
                    : "rgba(196,120,58,0.1)",
                  border: active ? "2px solid rgba(168,85,247,0.4)" : "2px solid transparent",
                  boxShadow: active ? "0 0 16px rgba(168,85,247,0.3)" : "none",
                  transform: active ? "scale(1.15)" : "scale(1)",
                }}
              >
                {done ? (
                  <IconCheck size={12} color="#fff" />
                ) : (
                  <Icon
                    size={15}
                    color={done || active ? "#fff" : "rgba(196,120,58,0.5)"}
                  />
                )}
              </div>
              <span
                style={{
                  fontSize: 10,
                  fontWeight: active ? 700 : 500,
                  color: active ? "var(--ls-text-dark)" : "var(--ls-text-muted)",
                  transition: "color 0.3s",
                }}
              >
                {s.label}
              </span>
            </div>
            {i < STEPS.length - 1 && (
              <div
                style={{
                  width: 32,
                  height: 2,
                  marginBottom: 16,
                  background: done
                    ? "linear-gradient(90deg,#c4783a,#3d806e)"
                    : "rgba(196,120,58,0.15)",
                  transition: "background 0.4s ease",
                }}
              />
            )}
          </div>
        );
      })}
    </div>
  );
}

// ─── Step 0: Welcome ─────────────────────────────────────────────────────────
function StepWelcome({ onNext }: { onNext: () => void }) {
  const [visible, setVisible] = useState(false);
  useEffect(() => { setTimeout(() => setVisible(true), 100); }, []);

  return (
    <div
      style={{
        opacity: visible ? 1 : 0,
        transform: visible ? "translateY(0)" : "translateY(24px)",
        transition: "opacity 0.7s ease, transform 0.7s ease",
        textAlign: "center",
        paddingTop: 8,
        paddingBottom: 8,
      }}
    >
      {/* Animated orb */}
      <div style={{ position: "relative", display: "inline-block", marginBottom: 28 }}>
        <div
          style={{
            width: 100,
            height: 100,
            borderRadius: "50%",
            background: "linear-gradient(135deg, #a855f7 0%, #6366f1 50%, #c4783a 100%)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            boxShadow: "0 0 40px rgba(168,85,247,0.4), 0 0 80px rgba(168,85,247,0.15)",
            animation: "orbPulse 3s ease-in-out infinite",
            margin: "0 auto",
          }}
        >
          <span style={{ fontSize: 44 }}><IconCrystalOrb size={48} color="#fff" /></span>
        </div>
        {/* Orbiting dots */}
        <div
          style={{
            position: "absolute",
            inset: -12,
            borderRadius: "50%",
            border: "1.5px dashed rgba(168,85,247,0.3)",
            animation: "spin 8s linear infinite",
          }}
        />
        <div
          style={{
            position: "absolute",
            top: "50%",
            left: -12,
            width: 8,
            height: 8,
            borderRadius: "50%",
            background: "#a855f7",
            marginTop: -4,
            boxShadow: "0 0 8px #a855f7",
            animation: "orbitDot 8s linear infinite",
          }}
        />
      </div>

      <h1
        style={{
          fontSize: "clamp(22px, 5vw, 30px)",
          fontFamily: "var(--font-lora)",
          fontWeight: 700,
          color: "var(--ls-text-dark)",
          marginBottom: 12,
          lineHeight: 1.3,
        }}
      >
        Khám Phá Lá Số Tử Vi
        <br />
        <span
          style={{
            background: "linear-gradient(135deg, #a855f7, #6366f1)",
            WebkitBackgroundClip: "text",
            WebkitTextFillColor: "transparent",
          }}
        >
          Bằng Trí Tuệ Nhân Tạo
        </span>
      </h1>

      <p
        style={{
          color: "var(--ls-text-soft)",
          fontSize: 14,
          lineHeight: 1.7,
          maxWidth: 400,
          margin: "0 auto 28px",
        }}
      >
        Phân tích Bát Tự (Tứ Trụ) — Ngũ Hành cân bằng, vận hạn, tình duyên,
        sự nghiệp dựa trên ngày tháng năm giờ sinh của bạn.
      </p>

      {/* Feature chips */}
      <div style={{ display: "flex", flexWrap: "wrap", gap: 8, justifyContent: "center", marginBottom: 32 }}>
        {([
          { icon: <IconChipBolt size={13} color="#7c3aed" />, label: "Phân tích AI realtime" },
          { icon: <IconChipCompass size={13} color="#7c3aed" />, label: "Bát Tự chính xác" },
          { icon: <IconChipElements size={13} color="#7c3aed" />, label: "Ngũ hành chi tiết" },
          { icon: <IconChipBook size={13} color="#7c3aed" />, label: "Luận giải chuyên sâu" },
        ] as const).map(f => (
          <span
            key={f.label}
            style={{
              fontSize: 11,
              fontWeight: 600,
              padding: "4px 12px",
              borderRadius: 20,
              background: "rgba(168,85,247,0.08)",
              border: "1px solid rgba(168,85,247,0.2)",
              color: "#7c3aed",
              display: "inline-flex",
              alignItems: "center",
              gap: 5,
            }}
          >
            {f.icon}
            {f.label}
          </span>
        ))}
      </div>

      <button
        onClick={onNext}
        style={{
          padding: "14px 40px",
          borderRadius: 16,
          border: "none",
          cursor: "pointer",
          fontSize: 15,
          fontWeight: 700,
          color: "#fff",
          background: "linear-gradient(135deg, #a855f7, #6366f1)",
          boxShadow: "0 6px 24px rgba(168,85,247,0.4)",
          transition: "transform 0.2s, box-shadow 0.2s",
          display: "inline-flex",
          alignItems: "center",
          gap: 8,
        }}
        onMouseEnter={e => {
          (e.currentTarget as HTMLButtonElement).style.transform = "scale(1.04)";
          (e.currentTarget as HTMLButtonElement).style.boxShadow = "0 8px 32px rgba(168,85,247,0.5)";
        }}
        onMouseLeave={e => {
          (e.currentTarget as HTMLButtonElement).style.transform = "scale(1)";
          (e.currentTarget as HTMLButtonElement).style.boxShadow = "0 6px 24px rgba(168,85,247,0.4)";
        }}
      >
        Bắt đầu xem tử vi
        <IconChevronRight size={18} color="#fff" />
      </button>
    </div>
  );
}

// ─── Step 1: Name ─────────────────────────────────────────────────────────────
function StepName({
  name,
  gender,
  onChange,
  onNext,
  onBack,
}: {
  name: string;
  gender: "male" | "female";
  onChange: (name: string, gender: "male" | "female") => void;
  onNext: () => void;
  onBack: () => void;
}) {
  const [localName, setLocalName] = useState(name);
  const [localGender, setLocalGender] = useState<"male" | "female">(gender);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => { inputRef.current?.focus(); }, []);

  const handleNext = () => {
    if (!localName.trim()) return;
    onChange(localName.trim(), localGender);
    onNext();
  };

  return (
    <div>
      <div style={{ textAlign: "center", marginBottom: 28 }}>
        <div style={{
          width: 72, height: 72, borderRadius: "50%", margin: "0 auto 12px",
          background: "linear-gradient(135deg, #a855f7, #6366f1)",
          display: "flex", alignItems: "center", justifyContent: "center",
          boxShadow: "0 0 28px rgba(168,85,247,0.35)",
          animation: "bounceIn 0.5s cubic-bezier(0.34,1.56,0.64,1)",
        }}>
          <IconUserLarge size={44} color="#fff" />
        </div>
        <h2 style={{ fontSize: 22, fontFamily: "var(--font-lora)", fontWeight: 700, color: "var(--ls-text-dark)", marginBottom: 8 }}>
          Bạn tên gì?
        </h2>
        <p style={{ color: "var(--ls-text-soft)", fontSize: 13 }}>
          AI sẽ xưng hô và phân tích theo tên của bạn
        </p>
      </div>

      <div
        style={{
          background: "var(--ls-card-bg-strong)",
          border: "1px solid var(--ls-border-warm)",
          borderRadius: 20,
          padding: "28px 24px",
          boxShadow: "0 4px 24px var(--ls-shadow-warm)",
        }}
      >
        {/* Name input */}
        <div style={{ marginBottom: 24 }}>
          <label style={{ display: "block", fontSize: 11, fontWeight: 700, letterSpacing: "0.1em", textTransform: "uppercase", color: "var(--ls-text-soft)", marginBottom: 8 }}>
            Họ và tên
          </label>
          <input
            ref={inputRef}
            type="text"
            value={localName}
            onChange={e => setLocalName(e.target.value)}
            onKeyDown={e => e.key === "Enter" && handleNext()}
            placeholder="Ví dụ: Nguyễn Văn A"
            style={{
              width: "100%",
              padding: "14px 16px",
              borderRadius: 12,
              border: "1.5px solid var(--ls-border-warm)",
              background: "rgba(255,252,248,0.6)",
              fontSize: 16,
              color: "var(--ls-text-dark)",
              outline: "none",
              transition: "border-color 0.2s",
              boxSizing: "border-box",
            }}
            onFocus={e => (e.currentTarget.style.borderColor = "rgba(168,85,247,0.5)")}
            onBlur={e => (e.currentTarget.style.borderColor = "var(--ls-border-warm)")}
          />
        </div>

        {/* Gender */}
        <div style={{ marginBottom: 8 }}>
          <label style={{ display: "block", fontSize: 11, fontWeight: 700, letterSpacing: "0.1em", textTransform: "uppercase", color: "var(--ls-text-soft)", marginBottom: 12 }}>
            Giới tính
          </label>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12 }}>
            {([
              ["male",   "Nam", "M"] as const,
              ["female", "Nữ",  "F"] as const,
            ]).map(([val, label, _]) => (
              <button
                key={val}
                type="button"
                onClick={() => setLocalGender(val)}
                style={{
                  padding: "14px",
                  borderRadius: 12,
                  border: localGender === val
                    ? "2px solid rgba(168,85,247,0.6)"
                    : "2px solid var(--ls-border-soft)",
                  background: localGender === val
                    ? "rgba(168,85,247,0.08)"
                    : "rgba(255,252,248,0.4)",
                  cursor: "pointer",
                  fontSize: 14,
                  fontWeight: 600,
                  color: localGender === val ? "#7c3aed" : "var(--ls-text-mid)",
                  transition: "all 0.2s",
                  boxShadow: localGender === val ? "0 0 12px rgba(168,85,247,0.2)" : "none",
                  display: "flex", alignItems: "center", justifyContent: "center", gap: 8,
                }}
              >
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none"
                  stroke={localGender === val ? "#7c3aed" : "var(--ls-text-soft)"}
                  strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  {val === "male" ? (
                    <>
                      <circle cx="7" cy="11" r="5" />
                      <path d="M11 7l4-4M15 3h-4M15 3v4" />
                    </>
                  ) : (
                    <>
                      <circle cx="9" cy="7.5" r="4.5" />
                      <path d="M9 12v4M6.5 14.5h5" />
                    </>
                  )}
                </svg>
                {label}
              </button>
            ))}
          </div>
        </div>
      </div>

      <NavButtons
        onBack={onBack}
        onNext={handleNext}
        nextDisabled={!localName.trim()}
        nextLabel="Tiếp theo"
      />
    </div>
  );
}

// ─── Step 2: Birth Date ───────────────────────────────────────────────────────
function StepBirthDate({
  form,
  onChange,
  onNext,
  onBack,
}: {
  form: { birth_year: number; birth_month: number; birth_day: number; birth_hour?: number };
  onChange: (f: typeof form) => void;
  onNext: () => void;
  onBack: () => void;
}) {
  const [yearStr, setYearStr] = useState(String(form.birth_year));
  const [month, setMonth]     = useState(form.birth_month);
  const [day, setDay]         = useState(form.birth_day);
  const [hourStr, setHourStr] = useState(form.birth_hour !== undefined ? String(form.birth_hour) : "");

  const parsedYear = parseInt(yearStr);
  const validYear  = !isNaN(parsedYear) && parsedYear >= 1900 && parsedYear <= currentYear;
  const maxDay     = validYear ? getDaysInMonth(month, parsedYear) : 31;

  // Nếu ngày vượt max khi đổi tháng/năm → clamp
  const safeDay = Math.min(day, maxDay);

  const handleNext = () => {
    if (!validYear) return;
    const parsedHour = hourStr !== "" ? parseInt(hourStr) : undefined;
    onChange({
      birth_year:  parsedYear,
      birth_month: month,
      birth_day:   safeDay,
      birth_hour:  parsedHour !== undefined && !isNaN(parsedHour) ? parsedHour : undefined,
    });
    onNext();
  };

  const selectStyle: React.CSSProperties = {
    ...inputStyle,
    appearance: "none",
    WebkitAppearance: "none",
    backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='8' viewBox='0 0 12 8' fill='none'%3E%3Cpath d='M1 1l5 5 5-5' stroke='%23a07050' strokeWidth='1.5' strokeLinecap='round'/%3E%3C/svg%3E")`,
    backgroundRepeat: "no-repeat",
    backgroundPosition: "right 12px center",
    paddingRight: 32,
    cursor: "pointer",
  };

  return (
    <div>
      <div style={{ textAlign: "center", marginBottom: 28 }}>
        <div style={{
          width: 72, height: 72, borderRadius: "50%", margin: "0 auto 12px",
          background: "linear-gradient(135deg, #c4783a, #d97706)",
          display: "flex", alignItems: "center", justifyContent: "center",
          boxShadow: "0 0 28px rgba(196,120,58,0.35)",
          animation: "bounceIn 0.5s cubic-bezier(0.34,1.56,0.64,1)",
        }}>
          <IconCalendarLarge size={44} color="#fff" />
        </div>
        <h2 style={{ fontSize: 22, fontFamily: "var(--font-lora)", fontWeight: 700, color: "var(--ls-text-dark)", marginBottom: 8 }}>
          Ngày tháng năm sinh
        </h2>
        <p style={{ color: "var(--ls-text-soft)", fontSize: 13 }}>
          Thông tin dùng để tính Bát Tự (Tứ Trụ) chính xác
        </p>
      </div>

      <div style={{
        background: "var(--ls-card-bg-strong)",
        border: "1px solid var(--ls-border-warm)",
        borderRadius: 20,
        padding: "28px 24px",
        boxShadow: "0 4px 24px var(--ls-shadow-warm)",
      }}>
        {/* Năm — text input để gõ tự do */}
        <div style={{ marginBottom: 20 }}>
          <FieldBox label="Năm sinh">
            <input
              type="text"
              inputMode="numeric"
              pattern="[0-9]*"
              maxLength={4}
              value={yearStr}
              onChange={e => {
                const v = e.target.value.replace(/\D/g, "");
                setYearStr(v);
              }}
              placeholder={`1900 – ${currentYear}`}
              style={{
                ...inputStyle,
                borderColor: yearStr && !validYear ? "rgba(239,68,68,0.5)" : "var(--ls-border-warm)",
              }}
              onFocus={e => (e.currentTarget.style.borderColor = "rgba(168,85,247,0.5)")}
              onBlur={e => (e.currentTarget.style.borderColor = yearStr && !validYear ? "rgba(239,68,68,0.5)" : "var(--ls-border-warm)")}
            />
            {yearStr && !validYear && (
              <p style={{ fontSize: 11, color: "#dc2626", marginTop: 4, marginBottom: 0 }}>
                Năm hợp lệ: 1900 – {currentYear}
              </p>
            )}
          </FieldBox>
        </div>

        {/* Tháng + Ngày — select dropdown */}
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12, marginBottom: 20 }}>
          <FieldBox label="Tháng">
            <select
              value={month}
              onChange={e => setMonth(parseInt(e.target.value))}
              style={selectStyle}
            >
              {Array.from({ length: 12 }, (_, i) => i + 1).map(m => (
                <option key={m} value={m}>Tháng {m}</option>
              ))}
            </select>
          </FieldBox>
          <FieldBox label="Ngày">
            <select
              value={safeDay}
              onChange={e => setDay(parseInt(e.target.value))}
              style={selectStyle}
            >
              {Array.from({ length: maxDay }, (_, i) => i + 1).map(d => (
                <option key={d} value={d}>Ngày {d}</option>
              ))}
            </select>
          </FieldBox>
        </div>

        {/* Giờ — text input, tuỳ chọn */}
        <FieldBox label="Giờ sinh (0–23) · Tùy chọn">
          <div style={{ position: "relative" }}>
            <input
              type="text"
              inputMode="numeric"
              pattern="[0-9]*"
              maxLength={2}
              value={hourStr}
              placeholder="Để trống nếu không biết"
              onChange={e => {
                const v = e.target.value.replace(/\D/g, "");
                if (v === "" || (parseInt(v) >= 0 && parseInt(v) <= 23)) setHourStr(v);
              }}
              style={{ ...inputStyle, paddingRight: hourStr ? 36 : 14 }}
              onFocus={e => (e.currentTarget.style.borderColor = "rgba(168,85,247,0.5)")}
              onBlur={e => (e.currentTarget.style.borderColor = "var(--ls-border-warm)")}
            />
            {hourStr && (
              <button
                type="button"
                onClick={() => setHourStr("")}
                style={{
                  position: "absolute", right: 10, top: "50%", transform: "translateY(-50%)",
                  background: "none", border: "none", cursor: "pointer",
                  color: "var(--ls-text-soft)", fontSize: 14, padding: 2, lineHeight: 1,
                }}
              >✕</button>
            )}
          </div>
        </FieldBox>
      </div>

      <NavButtons
        onBack={onBack}
        onNext={handleNext}
        nextDisabled={!validYear}
        nextLabel="Lập lá số →"
      />
    </div>
  );
}

// ─── Frontend Bát Tự calculation (mirrors backend calculateBatTu) ─────────────
const STEMS    = ["Giáp","Ất","Bính","Đinh","Mậu","Kỷ","Canh","Tân","Nhâm","Quý"] as const;
const BRANCHES = ["Tý","Sửu","Dần","Mão","Thìn","Tỵ","Ngọ","Mùi","Thân","Dậu","Tuất","Hợi"] as const;
const STEM_ELEMENTS: Record<string, string> = {
  "Giáp":"Mộc","Ất":"Mộc","Bính":"Hoả","Đinh":"Hoả",
  "Mậu":"Thổ","Kỷ":"Thổ","Canh":"Kim","Tân":"Kim",
  "Nhâm":"Thuỷ","Quý":"Thuỷ",
};
const BRANCH_ELEMENTS: Record<string, string> = {
  "Tý":"Thuỷ","Sửu":"Thổ","Dần":"Mộc","Mão":"Mộc",
  "Thìn":"Thổ","Tỵ":"Hoả","Ngọ":"Hoả","Mùi":"Thổ",
  "Thân":"Kim","Dậu":"Kim","Tuất":"Thổ","Hợi":"Thuỷ",
};

function calcBatTuFrontend(
  f: { birth_year: number; birth_month: number; birth_day: number; birth_hour?: number }
): BatTuInfo {
  const { birth_year, birth_month, birth_day, birth_hour } = f;

  // Năm trụ
  const yStemIdx   = ((birth_year - 4) % 10 + 10) % 10;
  const yBranchIdx = ((birth_year - 4) % 12 + 12) % 12;
  const yearStem   = STEMS[yStemIdx];
  const yearBranch = BRANCHES[yBranchIdx];

  // Tháng trụ — công thức: stem = (năm_stem_index * 2 + tháng) % 10, branch = (tháng + 1) % 12
  const mStemIdx   = ((yStemIdx * 2 + birth_month) % 10 + 10) % 10;
  const mBranchIdx = ((birth_month + 1) % 12 + 12) % 12;
  const monthStem   = STEMS[mStemIdx];
  const monthBranch = BRANCHES[mBranchIdx];

  // Ngày trụ — dùng Julian Day Number đơn giản
  const a = Math.floor((14 - birth_month) / 12);
  const y = birth_year + 4800 - a;
  const m = birth_month + 12 * a - 3;
  const jdn = birth_day + Math.floor((153 * m + 2) / 5) + 365 * y + Math.floor(y / 4) - Math.floor(y / 100) + Math.floor(y / 400) - 32045;
  const dStemIdx   = ((jdn - 1) % 10 + 10) % 10;
  const dBranchIdx = ((jdn - 1) % 12 + 12) % 12;
  const dayStem   = STEMS[dStemIdx];
  const dayBranch = BRANCHES[dBranchIdx];

  // Giờ trụ
  let hourStem:   string = STEMS[0];
  let hourBranch: string = BRANCHES[0];
  if (birth_hour !== undefined) {
    const hi = Math.floor(((birth_hour + 1) % 24) / 2) % 12;
    hourBranch = BRANCHES[hi] as string;
    hourStem   = STEMS[((dStemIdx * 2 + hi) % 10 + 10) % 10] as string;
  }

  const pillar = (stem: string, branch: string) => ({
    heavenly_stem:  stem,
    earthly_branch: branch,
    element:        STEM_ELEMENTS[stem] ?? "Thổ",
  });

  return {
    year_pillar:  pillar(yearStem,  yearBranch),
    month_pillar: pillar(monthStem, monthBranch),
    day_pillar:   pillar(dayStem,   dayBranch),
    hour_pillar:  pillar(hourStem,  hourBranch),
  };
}

function calcNguHanhFrontend(batTu: BatTuInfo): NguHanhBalance {
  const counts: Record<string, number> = { "Mộc": 0, "Hoả": 0, "Thổ": 0, "Kim": 0, "Thuỷ": 0 };
  const pillars = [batTu.year_pillar, batTu.month_pillar, batTu.day_pillar, batTu.hour_pillar];
  for (const p of pillars) {
    const stemEl   = STEM_ELEMENTS[p.heavenly_stem];
    const branchEl = BRANCH_ELEMENTS[p.earthly_branch];
    if (stemEl)   counts[stemEl]   = (counts[stemEl]   ?? 0) + 1;
    if (branchEl) counts[branchEl] = (counts[branchEl] ?? 0) + 1;
  }
  const total = Object.values(counts).reduce((a, b) => a + b, 0) || 1;
  const sorted = Object.entries(counts).sort((a, b) => b[1] - a[1]);
  // Map Vietnamese element names → NguHanhBalance fields
  const elMap: Record<string, keyof NguHanhBalance> = {
    "Kim": "Kim", "Mộc": "Moc", "Thuỷ": "Thuy", "Hoả": "Hoa", "Thổ": "Tho",
  };
  const balance: NguHanhBalance = {
    Kim:      parseFloat(((counts["Kim"]   ?? 0) / total * 100).toFixed(1)),
    Moc:      parseFloat(((counts["Mộc"]  ?? 0) / total * 100).toFixed(1)),
    Thuy:     parseFloat(((counts["Thuỷ"] ?? 0) / total * 100).toFixed(1)),
    Hoa:      parseFloat(((counts["Hoả"]  ?? 0) / total * 100).toFixed(1)),
    Tho:      parseFloat(((counts["Thổ"]  ?? 0) / total * 100).toFixed(1)),
    strongest: elMap[sorted[0][0]] as string ?? "Tho",
    weakest:   elMap[sorted[sorted.length - 1][0]] as string ?? "Moc",
  };
  return balance;
}

// ─── Step 3: Lá Số (chart only, no AI) ───────────────────────────────────────
function StepChart({
  name,
  form,
  onNext,
  onBack,
}: {
  name: string;
  form: { birth_year: number; birth_month: number; birth_day: number; birth_hour?: number; gender: "male" | "female" };
  onNext: () => void;
  onBack: () => void;
}) {
  // Tính BatTu phía frontend (cùng công thức backend) để hiển thị ngay
  const batTu = calcBatTuFrontend(form);
  const nguHanh = calcNguHanhFrontend(batTu);

  return (
    <div style={{ animation: "fadeUp 0.4s ease-out both" }}>
      <div style={{ textAlign: "center", marginBottom: 20 }}>
        <div style={{
          width: 68, height: 68, borderRadius: "50%", margin: "0 auto 10px",
          background: "linear-gradient(135deg, #3d806e, #2563eb)",
          display: "flex", alignItems: "center", justifyContent: "center",
          boxShadow: "0 0 24px rgba(61,128,110,0.35)",
        }}>
          <IconBaguaLarge size={44} color="#fff" />
        </div>
        <h2 style={{ fontSize: 20, fontFamily: "var(--font-lora)", fontWeight: 700, color: "var(--ls-text-dark)", marginBottom: 4 }}>
          Lá Số Tử Vi của {name || "bạn"}
        </h2>
        <p style={{ fontSize: 13, color: "var(--ls-text-soft)" }}>
          Bát Tự đã được tính — chọn luận giải để AI phân tích sâu
        </p>
      </div>

      <TuViChart
        batTu={batTu}
        nguHanh={nguHanh}
        name={name}
        birthDay={form.birth_day}
        birthMonth={form.birth_month}
        birthYear={form.birth_year}
        birthHour={form.birth_hour}
        gender={form.gender}
      />

      <NavButtons
        onBack={onBack}
        onNext={onNext}
        nextLabel="Luận giải AI →"
      />
    </div>
  );
}

// ─── Step 4: Chọn loại luận giải → AI stream ──────────────────────────────────
function StepAnalysis({
  name,
  form,
  summary,
  streamText,
  isStreaming,
  error,
  hasStarted,
  readingType,
  question,
  onStart,
  onReset,
  onBack,
}: {
  name: string;
  form: HoroscopeAIRequest;
  summary: { bat_tu?: BatTuInfo; ngu_hanh?: NguHanhBalance; quota_remaining?: number; tokens_used?: number } | null;
  streamText: string;
  isStreaming: boolean;
  error: string | null;
  hasStarted: boolean;
  readingType: HoroscopeReadingType;
  question: string;
  onStart: (rt: HoroscopeReadingType, q: string) => void;
  onReset: () => void;
  onBack: () => void;
}) {
  const [selectedRT, setSelectedRT] = useState<HoroscopeReadingType>(readingType);
  const [localQ, setLocalQ] = useState(question);

  // Nếu chưa bắt đầu: hiển thị bộ chọn
  if (!hasStarted) {
    return (
      <div style={{ animation: "fadeUp 0.4s ease-out both" }}>
        <div style={{ textAlign: "center", marginBottom: 24 }}>
          <div style={{
          width: 68, height: 68, borderRadius: "50%", margin: "0 auto 10px",
          background: "linear-gradient(135deg, #a855f7, #6366f1)",
          display: "flex", alignItems: "center", justifyContent: "center",
          boxShadow: "0 0 24px rgba(168,85,247,0.35)",
        }}>
          <IconAIStarsLarge size={44} color="#fff" />
        </div>
          <h2 style={{ fontSize: 20, fontFamily: "var(--font-lora)", fontWeight: 700, color: "var(--ls-text-dark)", marginBottom: 4 }}>
            Chọn loại luận giải
          </h2>
          <p style={{ fontSize: 13, color: "var(--ls-text-soft)" }}>
            AI sẽ phân tích lá số của <strong>{name || "bạn"}</strong> theo chủ đề bạn chọn
          </p>
        </div>

        {/* Cards chọn loại */}
        <div style={{ display: "flex", flexDirection: "column", gap: 10, marginBottom: 20 }}>
          {READING_TYPES.map(rt => {
            const isSelected = selectedRT === rt.value;
            return (
              <button
                key={rt.value}
                type="button"
                onClick={() => setSelectedRT(rt.value)}
                style={{
                  display: "flex",
                  alignItems: "flex-start",
                  gap: 14,
                  padding: "14px 16px",
                  borderRadius: 14,
                  border: isSelected
                    ? `2px solid ${rt.color}`
                    : "2px solid var(--ls-border-soft)",
                  background: isSelected
                    ? `color-mix(in srgb, ${rt.color} 8%, transparent)`
                    : "var(--ls-card-bg-strong)",
                  cursor: "pointer",
                  textAlign: "left",
                  transition: "all 0.2s",
                  boxShadow: isSelected ? `0 0 16px color-mix(in srgb, ${rt.color} 20%, transparent)` : "none",
                }}
              >
                {/* Radio dot */}
                <div style={{
                  width: 18, height: 18, borderRadius: "50%", flexShrink: 0, marginTop: 2,
                  border: `2px solid ${isSelected ? rt.color : "var(--ls-border-soft)"}`,
                  background: isSelected ? rt.color : "transparent",
                  display: "flex", alignItems: "center", justifyContent: "center",
                  transition: "all 0.2s",
                }}>
                  {isSelected && <div style={{ width: 6, height: 6, borderRadius: "50%", background: "#fff" }} />}
                </div>

                {/* Icon + text */}
                <div style={{ flex: 1 }}>
                  <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 3 }}>
                    <div style={{
                      width: 30, height: 30, borderRadius: 8, flexShrink: 0,
                      background: isSelected ? rt.color : `${rt.color}22`,
                      display: "flex", alignItems: "center", justifyContent: "center",
                      transition: "all 0.2s",
                    }}>
                      <div style={{ color: isSelected ? "#fff" : rt.color, display: "flex" }}>
                        {rt.svgIcon}
                      </div>
                    </div>
                    <span style={{
                      fontSize: 14, fontWeight: 700,
                      color: isSelected ? rt.color : "var(--ls-text-dark)",
                      transition: "color 0.2s",
                    }}>
                      {rt.label}
                    </span>
                  </div>
                  <p style={{ fontSize: 12, color: "var(--ls-text-soft)", margin: 0, lineHeight: 1.5 }}>
                    {rt.desc}
                  </p>
                </div>
              </button>
            );
          })}
        </div>

        {/* Câu hỏi khi chọn "question" */}
        {selectedRT === "question" && (
          <div style={{ marginBottom: 16 }}>
            <FieldBox label="Câu hỏi của bạn">
              <textarea
                value={localQ}
                onChange={e => setLocalQ(e.target.value)}
                placeholder="Ví dụ: Năm nay tôi có nên đổi việc không? Tình duyên của tôi thế nào?"
                rows={3}
                style={{ ...inputStyle, resize: "none", lineHeight: 1.6 }}
              />
            </FieldBox>
          </div>
        )}

        {/* Buttons */}
        <div style={{ display: "flex", gap: 12 }}>
          <button
            type="button"
            onClick={onBack}
            style={{
              flex: "0 0 auto",
              padding: "12px 18px",
              borderRadius: 12,
              border: "1.5px solid var(--ls-border-warm)",
              background: "transparent",
              cursor: "pointer",
              fontSize: 13, fontWeight: 600,
              color: "var(--ls-text-mid)",
              display: "flex", alignItems: "center", gap: 6,
            }}
          >
            <IconChevronLeft size={16} color="var(--ls-text-mid)" /> Lá số
          </button>
          <button
            type="button"
            disabled={selectedRT === "question" && !localQ.trim()}
            onClick={() => onStart(selectedRT, localQ)}
            style={{
              flex: 1,
              padding: "13px 20px",
              borderRadius: 12, border: "none",
              cursor: (selectedRT === "question" && !localQ.trim()) ? "not-allowed" : "pointer",
              fontSize: 14, fontWeight: 700, color: "#fff",
              background: (selectedRT === "question" && !localQ.trim())
                ? "rgba(168,85,247,0.3)"
                : "linear-gradient(135deg, #a855f7, #6366f1)",
              boxShadow: "0 4px 16px rgba(168,85,247,0.35)",
              display: "flex", alignItems: "center", justifyContent: "center", gap: 8,
            }}
          >
            <IconSparkle size={16} color="#fff" /> Bắt đầu luận giải AI
          </button>
        </div>
      </div>
    );
  }

  // Đã bắt đầu: hiển thị kết quả streaming
  return (
    <div style={{ animation: "fadeUp 0.4s ease-out both" }}>
      {/* Tiêu đề */}
      <div style={{ textAlign: "center", marginBottom: 16 }}>
        <div style={{
          width: 52, height: 52, borderRadius: "50%", margin: "0 auto 10px",
          background: `linear-gradient(135deg, ${READING_TYPES.find(r => r.value === readingType)?.color ?? "#7c3aed"}cc, ${READING_TYPES.find(r => r.value === readingType)?.color ?? "#7c3aed"}66)`,
          display: "flex", alignItems: "center", justifyContent: "center",
          boxShadow: `0 0 20px ${READING_TYPES.find(r => r.value === readingType)?.color ?? "#7c3aed"}44`,
        }}>
          {READING_TYPES.find(r => r.value === readingType)?.svgIcon}
        </div>
        <h2 style={{ fontSize: 18, fontFamily: "var(--font-lora)", fontWeight: 700, color: "var(--ls-text-dark)", marginBottom: 4 }}>
          {READING_TYPES.find(r => r.value === readingType)?.label}
        </h2>
        <p style={{ fontSize: 12, color: "var(--ls-text-soft)" }}>
          {form.birth_day}/{form.birth_month}/{form.birth_year} · {form.gender === "male" ? "Nam" : "Nữ"}
        </p>
      </div>

      {/* AI result card */}
      <div style={{ ...cardStyle }}>
        <h3 style={{ ...cardTitleStyle, borderBottom: "1px solid var(--ls-border-soft)", paddingBottom: 10, marginBottom: 12 }}>
          <IconAIInline size={14} color="#a855f7" /> Luận Giải AI
          {isStreaming && (
            <span style={{ marginLeft: "auto", fontSize: 11, color: "#a855f7", display: "flex", alignItems: "center", gap: 4, fontWeight: 600 }}>
              <Loader2 size={12} style={{ animation: "spin 1s linear infinite" }} />
              Đang phân tích…
            </span>
          )}
        </h3>

        {error ? (
          <div style={{ padding: "12px 16px", borderRadius: 10, background: "rgba(239,68,68,0.06)", border: "1px solid rgba(239,68,68,0.2)", color: "#dc2626", fontSize: 13 }}>
            {error}
          </div>
        ) : streamText || isStreaming ? (
          <div style={{ fontSize: 14, lineHeight: 1.85, color: "var(--ls-text-dark)" }}>
            <AIStreamingText text={streamText} isStreaming={isStreaming} />
          </div>
        ) : (
          <div style={{ display: "flex", alignItems: "center", gap: 8, color: "var(--ls-text-muted)", fontSize: 13 }}>
            <Loader2 size={14} style={{ animation: "spin 1s linear infinite" }} />
            Đang kết nối AI…
          </div>
        )}
      </div>

      {/* Actions sau khi xong */}
      {!isStreaming && (
        <div style={{ marginTop: 16, display: "flex", flexDirection: "column", gap: 10 }}>
          {/* Chọn luận giải khác */}
          {!error && (
            <div style={{
              padding: "14px 16px",
              borderRadius: 14,
              background: "var(--ls-card-bg-strong)",
              border: "1px solid var(--ls-border-warm)",
            }}>
              <p style={{ fontSize: 12, fontWeight: 700, color: "var(--ls-text-soft)", marginBottom: 10, textTransform: "uppercase", letterSpacing: "0.06em" }}>
                Xem thêm chủ đề khác
              </p>
              <div style={{ display: "flex", flexWrap: "wrap", gap: 8 }}>
                {READING_TYPES.filter(r => r.value !== readingType).map(rt => (
                  <button
                    key={rt.value}
                    type="button"
                    onClick={() => onStart(rt.value, "")}
                    style={{
                      padding: "6px 12px",
                      borderRadius: 10,
                      border: `1.5px solid ${rt.color}30`,
                      background: `color-mix(in srgb, ${rt.color} 8%, transparent)`,
                      cursor: "pointer",
                      fontSize: 12,
                      fontWeight: 600,
                      color: rt.color,
                      display: "flex", alignItems: "center", gap: 5,
                      transition: "all 0.2s",
                    }}
                  >
                    <div style={{ display: "flex", alignItems: "center" }}>{rt.svgIcon}</div>
                    {rt.label}
                  </button>
                ))}
              </div>
            </div>
          )}

          <div style={{ display: "flex", justifyContent: "center" }}>
            <button
              onClick={onReset}
              style={{
                display: "inline-flex", alignItems: "center", gap: 6,
                padding: "10px 24px", borderRadius: 12,
                border: "1.5px solid var(--ls-border-warm)",
                background: "transparent", cursor: "pointer",
                fontSize: 13, fontWeight: 600, color: "var(--ls-text-mid)",
              }}
            >
              <IconReset size={14} color="var(--ls-text-mid)" /> Xem lại cho người khác
            </button>
          </div>
        </div>
      )}

      {summary?.quota_remaining !== undefined && (
        <p style={{ textAlign: "center", fontSize: 11, color: "var(--ls-text-muted)", marginTop: 12 }}>
          Còn {summary.quota_remaining} lượt luận giải hôm nay
          {summary.tokens_used ? ` · ${summary.tokens_used} tokens` : ""}
        </p>
      )}
    </div>
  );
}

// ─── Shared UI helpers ────────────────────────────────────────────────────────
const inputStyle: React.CSSProperties = {
  width: "100%",
  padding: "12px 14px",
  borderRadius: 10,
  border: "1.5px solid var(--ls-border-warm)",
  background: "rgba(255,252,248,0.6)",
  fontSize: 14,
  color: "var(--ls-text-dark)",
  outline: "none",
  boxSizing: "border-box",
};

const labelStyle: React.CSSProperties = {
  display: "block",
  fontSize: 11,
  fontWeight: 700,
  letterSpacing: "0.1em",
  textTransform: "uppercase",
  color: "var(--ls-text-soft)",
  marginBottom: 8,
};

function FieldBox({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <label style={labelStyle}>{label}</label>
      {children}
    </div>
  );
}

const cardStyle: React.CSSProperties = {
  background: "var(--ls-card-bg-strong)",
  border: "1px solid var(--ls-border-warm)",
  borderRadius: 20,
  padding: "20px",
  boxShadow: "0 4px 24px var(--ls-shadow-warm)",
};

const cardTitleStyle: React.CSSProperties = {
  display: "flex",
  alignItems: "center",
  gap: 7,
  fontSize: 13,
  fontWeight: 700,
  color: "var(--ls-text-dark)",
  marginBottom: 14,
  fontFamily: "var(--font-lora)",
};

const pillarCardStyle: React.CSSProperties = {
  background: "rgba(255,252,248,0.6)",
  border: "1px solid var(--ls-border-soft)",
  borderRadius: 12,
  padding: "10px 6px",
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
};

const pillarLabelStyle: React.CSSProperties = {
  fontSize: 10,
  fontWeight: 700,
  letterSpacing: "0.08em",
  textTransform: "uppercase" as const,
  color: "var(--ls-text-muted)",
  marginBottom: 6,
};

function NavButtons({
  onBack,
  onNext,
  nextDisabled = false,
  nextLabel = "Tiếp theo",
}: {
  onBack?: () => void;
  onNext?: () => void;
  nextDisabled?: boolean;
  nextLabel?: string;
}) {
  return (
    <div style={{ display: "flex", gap: 12, marginTop: 20 }}>
      {onBack && (
        <button
          type="button"
          onClick={onBack}
          style={{
            flex: "0 0 auto",
            padding: "12px 18px",
            borderRadius: 12,
            border: "1.5px solid var(--ls-border-warm)",
            background: "transparent",
            cursor: "pointer",
            fontSize: 13,
            fontWeight: 600,
            color: "var(--ls-text-mid)",
            display: "flex",
            alignItems: "center",
            gap: 6,
            transition: "all 0.2s",
          }}
        >
          <IconChevronLeft size={16} color="var(--ls-text-mid)" /> Quay lại
        </button>
      )}
      {onNext && (
        <button
          type="button"
          onClick={onNext}
          disabled={nextDisabled}
          style={{
            flex: 1,
            padding: "13px 20px",
            borderRadius: 12,
            border: "none",
            cursor: nextDisabled ? "not-allowed" : "pointer",
            fontSize: 14,
            fontWeight: 700,
            color: "#fff",
            background: nextDisabled
              ? "rgba(168,85,247,0.3)"
              : "linear-gradient(135deg, #a855f7, #6366f1)",
            boxShadow: nextDisabled ? "none" : "0 4px 16px rgba(168,85,247,0.35)",
            transition: "all 0.2s",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            gap: 8,
          }}
        >
          {nextLabel}
          <IconChevronRight size={16} color="#fff" />
        </button>
      )}
    </div>
  );
}

// ─── CSS keyframes (injected once) ────────────────────────────────────────────
const GLOBAL_STYLES = `
@keyframes orbPulse {
  0%, 100% { box-shadow: 0 0 40px rgba(168,85,247,0.4), 0 0 80px rgba(168,85,247,0.15); }
  50% { box-shadow: 0 0 60px rgba(168,85,247,0.6), 0 0 100px rgba(168,85,247,0.25); }
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
@keyframes orbitDot {
  from { transform: rotate(0deg) translateX(62px) rotate(0deg); left: 50%; top: 50%; margin: -4px 0 0 -4px; }
  to { transform: rotate(360deg) translateX(62px) rotate(-360deg); left: 50%; top: 50%; margin: -4px 0 0 -4px; }
}
@keyframes bounceIn {
  0% { transform: scale(0.5); opacity: 0; }
  70% { transform: scale(1.15); }
  100% { transform: scale(1); opacity: 1; }
}
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
`;

// ─── Main Wizard ──────────────────────────────────────────────────────────────
export function TuViAIWizard() {
  const [step, setStep] = useState(0);
  const [name, setName] = useState("");
  const [gender, setGender] = useState<"male" | "female">("male");
  const [birthForm, setBirthForm] = useState({
    birth_year: currentYear - 30,
    birth_month: 1,
    birth_day: 1,
    birth_hour: undefined as number | undefined,
  });
  const [readingType, setReadingType] = useState<HoroscopeReadingType>("overview");
  const [question, setQuestion] = useState("");
  const [hasStarted, setHasStarted] = useState(false);

  const { isStreaming, streamText, summary, error, startStream, reset } = useStreamHoroscope();
  const { data: quotaResponse } = useHoroscopeQuota();
  const quota = quotaResponse?.data;
  const exhausted = quota && quota.remaining === 0;

  const handleReset = () => {
    reset();
    setStep(0);
    setName("");
    setGender("male");
    setBirthForm({ birth_year: currentYear - 30, birth_month: 1, birth_day: 1, birth_hour: undefined });
    setReadingType("overview");
    setQuestion("");
    setHasStarted(false);
  };

  const handleStartAnalysis = (rt: HoroscopeReadingType, q: string) => {
    reset();
    setReadingType(rt);
    setQuestion(q);
    setHasStarted(true);
    startStream({
      ...birthForm,
      gender,
      reading_type: rt,
      question: rt === "question" ? q : undefined,
      depth: "standard",
      stream: true,
    });
  };

  const fullForm: HoroscopeAIRequest = {
    ...birthForm,
    gender,
    reading_type: readingType,
    question: readingType === "question" ? question : undefined,
    depth: "standard",
    stream: true,
  };

  return (
    <>
      {/* Inject global keyframes once */}
      <style dangerouslySetInnerHTML={{ __html: GLOBAL_STYLES }} />

      <div
        style={{
          maxWidth: 520,
          margin: "0 auto",
          padding: "0 0 40px",
        }}
      >
        {/* Quota warning */}
        {exhausted && step < 4 && (
          <div style={{
            marginBottom: 16,
            padding: "12px 16px",
            borderRadius: 12,
            background: "rgba(239,68,68,0.06)",
            border: "1px solid rgba(239,68,68,0.2)",
            fontSize: 13,
            color: "#dc2626",
            textAlign: "center",
          }}>
            ⚠️ Bạn đã dùng hết lượt xem tử vi AI hôm nay. Hãy quay lại ngày mai.
          </div>
        )}

        {/* Step indicator (hide on welcome) */}
        {step > 0 && <StepIndicator current={step} />}

        {/* Step content with slide animation */}
        <div key={step} style={{ animation: "fadeUp 0.35s ease-out both" }}>
          {step === 0 && (
            <StepWelcome onNext={() => setStep(1)} />
          )}
          {step === 1 && (
            <StepName
              name={name}
              gender={gender}
              onChange={(n, g) => { setName(n); setGender(g); }}
              onNext={() => setStep(2)}
              onBack={() => setStep(0)}
            />
          )}
          {step === 2 && (
            <StepBirthDate
              form={birthForm}
              onChange={(f) => setBirthForm({ ...f, birth_hour: f.birth_hour ?? undefined })}
              onNext={() => setStep(3)}
              onBack={() => setStep(1)}
            />
          )}
          {step === 3 && (
            <StepChart
              name={name}
              form={{ ...birthForm, gender }}
              onNext={() => { if (!exhausted) setStep(4); }}
              onBack={() => setStep(2)}
            />
          )}
          {step === 4 && (
            <StepAnalysis
              name={name}
              form={fullForm}
              summary={summary}
              streamText={streamText}
              isStreaming={isStreaming}
              error={error}
              hasStarted={hasStarted}
              readingType={readingType}
              question={question}
              onStart={handleStartAnalysis}
              onReset={handleReset}
              onBack={() => {
                reset();
                setHasStarted(false);
                setStep(3);
              }}
            />
          )}
        </div>
      </div>
    </>
  );
}
