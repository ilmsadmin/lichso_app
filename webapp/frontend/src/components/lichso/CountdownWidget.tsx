"use client";

import { useState, useEffect } from "react";
import { Timer, ChevronRight, Plus } from "lucide-react";
import Link from "next/link";
import { useActiveCountdowns } from "@/hooks/useV3";
import { useAuthStore } from "@/stores/authStore";
import { ROUTES } from "@/lib/constants";

export function CountdownWidget() {
  const { isAuthenticated } = useAuthStore();
  const { data, isLoading } = useActiveCountdowns();
  const countdowns = data?.data ?? [];

  // Only show if user is logged in
  if (!isAuthenticated) return null;

  // Take top 3 upcoming countdowns
  const topCountdowns = countdowns
    .filter((c) => c.days_remaining > 0)
    .sort((a, b) => a.days_remaining - b.days_remaining)
    .slice(0, 3);

  if (isLoading || topCountdowns.length === 0) return null;

  return (
    <div className="mb-6">
      <div className="mb-3 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span
            className="h-4 w-1 rounded-sm"
            style={{
              background: "linear-gradient(to bottom, var(--warm-amber), var(--warm-gold))",
            }}
          />
          <span className="text-text-dark text-base font-[var(--font-lora)] tracking-wide">
            Đếm Ngược
          </span>
        </div>
        <Link
          href={ROUTES.PROFILE_COUNTDOWNS}
          className="text-warm-amber flex items-center gap-0.5 text-xs hover:underline"
        >
          Xem tất cả
          <ChevronRight className="h-3 w-3" />
        </Link>
      </div>

      <div className="space-y-2">
        {topCountdowns.map((cd) => (
          <CountdownWidgetItem key={cd.id} countdown={cd} />
        ))}
      </div>
    </div>
  );
}

function CountdownWidgetItem({
  countdown,
}: {
  countdown: {
    title: string;
    target_date: string;
    target_time?: string;
    icon: string;
    color: string;
    days_remaining: number;
  };
}) {
  const [timeLeft, setTimeLeft] = useState("");

  useEffect(() => {
    const calculate = () => {
      const target = new Date(countdown.target_date);
      if (countdown.target_time) {
        const [h, m] = countdown.target_time.split(":");
        target.setHours(Number(h), Number(m));
      }
      const now = new Date();
      const diff = target.getTime() - now.getTime();

      if (diff <= 0) {
        setTimeLeft("Đã đến!");
        return;
      }

      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));

      if (days > 0) {
        setTimeLeft(`${days} ngày ${hours} giờ`);
      } else {
        const mins = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
        setTimeLeft(`${hours} giờ ${mins} phút`);
      }
    };

    calculate();
    const interval = setInterval(calculate, 60000);
    return () => clearInterval(interval);
  }, [countdown.target_date, countdown.target_time]);

  return (
    <div
      className="flex items-center gap-3 rounded-xl px-3 py-2.5 backdrop-blur-lg transition-all hover:scale-[1.01]"
      style={{
        background: "var(--ls-card-bg)",
        border: "1px solid var(--ls-border-soft)",
      }}
    >
      <div
        className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-lg"
        style={{ backgroundColor: countdown.color + "20" }}
      >
        {countdown.icon}
      </div>
      <div className="min-w-0 flex-1">
        <p className="text-text-dark truncate text-sm font-medium">{countdown.title}</p>
        <p className="text-text-muted-ls text-[11px]">
          {new Date(countdown.target_date).toLocaleDateString("vi-VN", {
            day: "2-digit",
            month: "2-digit",
          })}
        </p>
      </div>
      <span className="text-sm font-bold whitespace-nowrap" style={{ color: countdown.color }}>
        {timeLeft}
      </span>
    </div>
  );
}
