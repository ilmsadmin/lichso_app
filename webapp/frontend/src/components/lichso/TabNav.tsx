"use client";

import { cn } from "@/lib/utils";

const TABS = [
  "Lịch Tháng",
  "Tháng Âm Lịch",
  "Ngày Tốt Tháng",
  "24 Tiết Khí",
  "Đổi Lịch Âm / Dương",
];

interface TabNavProps {
  activeTab: number;
  onTabChange: (index: number) => void;
}

export function TabNav({ activeTab, onTabChange }: TabNavProps) {
  return (
    <div
      className="mt-5 mb-6 flex gap-0.5 overflow-x-auto"
      style={{ borderBottom: "1px solid var(--ls-border-soft)" }}
    >
      {TABS.map((tab, i) => (
        <button
          key={tab}
          onClick={() => onTabChange(i)}
          className={cn(
            "-mb-px border-b-2 px-4 py-2.5 text-[13px] tracking-wide whitespace-nowrap transition-all",
            "font-[var(--font-vietnam)]",
            i === activeTab
              ? "text-warm-amber border-warm-amber font-medium"
              : "text-text-muted-ls hover:text-warm-amber border-transparent"
          )}
          style={{ background: "none" }}
        >
          {tab}
        </button>
      ))}
    </div>
  );
}
