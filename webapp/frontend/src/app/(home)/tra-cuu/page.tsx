"use client";

import { useState, useCallback } from "react";
import {
  Search,
  CalendarDays,
  Sunrise,
  CalendarRange,
  CalendarPlus,
  CalendarCheck,
  Sparkles,
  Flag,
  Trophy,
  Lightbulb,
  FileText,
  CalendarClock,
  Hash,
  Info,
} from "lucide-react";
import { BackgroundLayer } from "@/components/lichso/BackgroundLayer";
import { SearchBar } from "@/components/lichso/SearchBar";
import { DayDetailModal } from "@/components/lichso/DayDetailModal";

export default function TraCuuPage() {
  const [selectedDate, setSelectedDate] = useState<string | null>(null);

  const handleSearch = useCallback((query: string) => {
    // Parse DD/MM/YYYY
    const dateMatch = query.match(/^(\d{1,2})[\/\-\.](\d{1,2})[\/\-\.](\d{4})$/);
    if (dateMatch) {
      const [, d, m, y] = dateMatch;
      setSelectedDate(`${y}-${m.padStart(2, "0")}-${d.padStart(2, "0")}`);
      return;
    }
    // Parse YYYY-MM-DD
    const isoMatch = query.match(/^(\d{4})[\/\-\.](\d{1,2})[\/\-\.](\d{1,2})$/);
    if (isoMatch) {
      const [, y, m, d] = isoMatch;
      setSelectedDate(`${y}-${m.padStart(2, "0")}-${d.padStart(2, "0")}`);
      return;
    }
    // Try single day
    const dayOnly = parseInt(query);
    if (dayOnly >= 1 && dayOnly <= 31) {
      const now = new Date();
      setSelectedDate(
        `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(dayOnly).padStart(2, "0")}`
      );
    }
  }, []);

  return (
    <>
      <BackgroundLayer />
      <div className="relative z-[1] mx-auto max-w-[1180px] px-4 pb-16 sm:px-7">
        {/* Page header */}
        <div className="pt-10 pb-8 text-center">
          <div
            className="mb-4 inline-flex h-12 w-12 items-center justify-center rounded-2xl"
            style={{
              background: "linear-gradient(135deg, var(--warm-amber), var(--warm-gold))",
              boxShadow: "0 4px 16px rgba(196,120,58,0.25)",
            }}
          >
            <Search className="h-6 w-6 text-white" />
          </div>
          <h1 className="text-text-dark mb-3 text-3xl font-[var(--font-lora)] font-semibold">
            Tra Cứu Ngày
          </h1>
          <p className="text-text-soft mx-auto max-w-md text-sm">
            Nhập ngày dương lịch (DD/MM/YYYY) để xem thông tin âm lịch, phong thủy, ngày tốt xấu chi
            tiết.
          </p>
        </div>

        {/* Search */}
        <SearchBar onSearch={handleSearch} />

        {/* Quick date selection */}
        <div className="mt-8">
          <h2 className="text-text-dark mb-4 flex items-center gap-2 text-lg font-[var(--font-lora)]">
            <span
              className="h-4 w-1 rounded-sm"
              style={{
                background: "linear-gradient(to bottom, var(--warm-amber), var(--warm-gold))",
              }}
            />
            Chọn nhanh
          </h2>
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">
            {getQuickDates().map((item) => (
              <button
                key={item.label}
                onClick={() => setSelectedDate(item.date)}
                className="group rounded-2xl p-4 text-left backdrop-blur-[14px] transition-all hover:translate-y-[-2px]"
                style={{
                  background: "var(--ls-card-bg)",
                  border: "1px solid var(--ls-border-soft)",
                  boxShadow: "0 2px 12px var(--ls-shadow-warm)",
                }}
              >
                <div
                  className="mb-2 flex h-8 w-8 items-center justify-center rounded-xl transition-colors"
                  style={{ background: item.iconBg }}
                >
                  <item.icon className="h-4 w-4" style={{ color: item.iconColor }} />
                </div>
                <div className="text-text-dark text-sm font-[var(--font-lora)] font-medium">
                  {item.label}
                </div>
                <div className="text-text-muted-ls mt-0.5 text-[11px]">{item.desc}</div>
              </button>
            ))}
          </div>
        </div>

        {/* Usage guide */}
        <div
          className="mt-10 rounded-2xl p-6 backdrop-blur-[14px]"
          style={{
            background: "var(--ls-card-bg)",
            border: "1px solid var(--ls-border-soft)",
          }}
        >
          <h3 className="text-text-dark mb-3 flex items-center gap-2 text-base font-[var(--font-lora)]">
            <Lightbulb className="text-warm-amber h-4 w-4" />
            Hướng dẫn tra cứu
          </h3>
          <ul className="text-text-mid space-y-2 text-sm">
            <li className="flex items-start gap-2.5">
              <FileText className="text-warm-amber mt-0.5 h-3.5 w-3.5 shrink-0" />
              <span>
                Nhập ngày theo định dạng <strong>DD/MM/YYYY</strong> (ví dụ: 15/01/2026)
              </span>
            </li>
            <li className="flex items-start gap-2.5">
              <CalendarClock className="text-warm-amber mt-0.5 h-3.5 w-3.5 shrink-0" />
              <span>
                Hoặc nhập <strong>YYYY-MM-DD</strong> (ví dụ: 2026-01-15)
              </span>
            </li>
            <li className="flex items-start gap-2.5">
              <Hash className="text-warm-amber mt-0.5 h-3.5 w-3.5 shrink-0" />
              <span>Nhập chỉ số ngày (1-31) để tra cứu ngày đó trong tháng hiện tại</span>
            </li>
            <li className="flex items-start gap-2.5">
              <Info className="text-warm-amber mt-0.5 h-3.5 w-3.5 shrink-0" />
              <span>
                Năm hỗ trợ tra cứu: <strong>1900 – 2100</strong>
              </span>
            </li>
          </ul>
        </div>
      </div>

      {selectedDate && <DayDetailModal date={selectedDate} onClose={() => setSelectedDate(null)} />}
    </>
  );
}

function getQuickDates() {
  const now = new Date();
  const fmt = (d: Date) =>
    `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;

  const tomorrow = new Date(now);
  tomorrow.setDate(now.getDate() + 1);

  const nextWeek = new Date(now);
  nextWeek.setDate(now.getDate() + 7);

  const firstDayNextMonth = new Date(now.getFullYear(), now.getMonth() + 1, 1);
  const lastDayMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0);

  const newYear = new Date(now.getFullYear() + 1, 0, 1);

  return [
    {
      label: "Hôm nay",
      date: fmt(now),
      icon: CalendarDays,
      iconBg: "rgba(196,120,58,0.12)",
      iconColor: "var(--warm-amber)",
      desc: formatViDate(now),
    },
    {
      label: "Ngày mai",
      date: fmt(tomorrow),
      icon: Sunrise,
      iconBg: "rgba(234,179,8,0.12)",
      iconColor: "#d97706",
      desc: formatViDate(tomorrow),
    },
    {
      label: "Tuần sau",
      date: fmt(nextWeek),
      icon: CalendarRange,
      iconBg: "rgba(59,130,246,0.12)",
      iconColor: "#3b82f6",
      desc: formatViDate(nextWeek),
    },
    {
      label: "Đầu tháng tới",
      date: fmt(firstDayNextMonth),
      icon: CalendarPlus,
      iconBg: "rgba(16,185,129,0.12)",
      iconColor: "#10b981",
      desc: formatViDate(firstDayNextMonth),
    },
    {
      label: "Cuối tháng này",
      date: fmt(lastDayMonth),
      icon: CalendarCheck,
      iconBg: "rgba(139,92,246,0.12)",
      iconColor: "#8b5cf6",
      desc: formatViDate(lastDayMonth),
    },
    {
      label: "Tết Dương lịch",
      date: fmt(newYear),
      icon: Sparkles,
      iconBg: "rgba(239,68,68,0.12)",
      iconColor: "#ef4444",
      desc: `01/01/${now.getFullYear() + 1}`,
    },
    {
      label: "Quốc Khánh",
      date: `${now.getFullYear()}-09-02`,
      icon: Flag,
      iconBg: "rgba(220,38,38,0.12)",
      iconColor: "#dc2626",
      desc: `02/09/${now.getFullYear()}`,
    },
    {
      label: "Giải phóng 30/4",
      date: `${now.getFullYear()}-04-30`,
      icon: Trophy,
      iconBg: "rgba(245,158,11,0.12)",
      iconColor: "#f59e0b",
      desc: `30/04/${now.getFullYear()}`,
    },
  ];
}

function formatViDate(d: Date) {
  return `${String(d.getDate()).padStart(2, "0")}/${String(d.getMonth() + 1).padStart(2, "0")}/${d.getFullYear()}`;
}
