"use client";

import { useEffect } from "react";
import Link from "next/link";
import { useUserProgress, useRecordVisit } from "@/hooks/useV3";
import { useAuthStore } from "@/stores/authStore";
import { Skeleton } from "@/components/ui/skeleton";
import { Flame, Trophy, Star, Zap, Lock } from "lucide-react";
import type { UserAchievement } from "@/types/v3";

// ============================================
// Sub-components
// ============================================

function StreakFire({ streak }: { streak: number }) {
  const intensity =
    streak >= 30 ? "text-red-500" : streak >= 7 ? "text-orange-500" : "text-warm-amber";

  return (
    <div className="flex flex-col items-center">
      <Flame className={`h-10 w-10 ${intensity} mb-1`} />
      <span className="text-text-dark text-3xl leading-none font-[var(--font-lora)] font-bold">
        {streak}
      </span>
      <span className="text-text-muted-ls mt-0.5 text-[10px]">ngày liên tiếp</span>
    </div>
  );
}

function StatCard({
  label,
  value,
  icon,
}: {
  label: string;
  value: number | string;
  icon: React.ReactNode;
}) {
  return (
    <div
      className="rounded-xl p-3 text-center"
      style={{
        background: "var(--ls-card-bg)",
        border: "1px solid var(--ls-border-soft)",
      }}
    >
      <div className="text-jade-teal mb-1 flex items-center justify-center">{icon}</div>
      <span className="text-text-dark block text-lg font-[var(--font-lora)] font-bold">
        {value}
      </span>
      <span className="text-text-muted-ls text-[10px]">{label}</span>
    </div>
  );
}

function AchievementBadge({ achievement }: { achievement: UserAchievement }) {
  const pct = Math.min(
    100,
    achievement.target > 0 ? Math.round((achievement.progress / achievement.target) * 100) : 0
  );

  return (
    <div
      className={`flex items-center gap-3 rounded-xl p-3 transition-all ${
        achievement.unlocked ? "opacity-100" : "opacity-70"
      }`}
      style={{
        background: achievement.unlocked
          ? "linear-gradient(135deg, rgba(74,139,127,0.08), rgba(196,120,58,0.05))"
          : "var(--ls-card-bg)",
        border: achievement.unlocked
          ? "1px solid rgba(74,139,127,0.25)"
          : "1px solid var(--ls-border-soft)",
      }}
    >
      {/* Badge icon */}
      <span className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-lg text-2xl"
        style={{ background: "var(--ls-bg-soft, rgba(74,139,127,0.07))" }}>
        {achievement.badge}
      </span>

      {/* Content */}
      <div className="min-w-0 flex-1">
        <div className="mb-0.5 flex items-center gap-2">
          <span className="text-text-dark text-[13px] font-medium leading-snug">
            {achievement.achievement_name}
          </span>
          {achievement.unlocked && (
            <span className="text-jade-teal bg-jade-teal/10 flex-shrink-0 rounded-full px-1.5 py-0.5 text-[9px] font-semibold">
              ✓ Đạt
            </span>
          )}
        </div>
        <p className="text-text-muted-ls mb-2 text-[11px] leading-relaxed">
          {achievement.description}
        </p>
        {/* Progress bar + counter */}
        <div className="flex items-center gap-2">
          <div
            className="h-1.5 flex-1 overflow-hidden rounded-full"
            style={{ background: "rgba(74,139,127,0.12)" }}
          >
            {pct > 0 && (
              <div
                className="h-full rounded-full transition-all duration-500"
                style={{
                  width: `${pct}%`,
                  background: achievement.unlocked
                    ? "linear-gradient(90deg, var(--jade-teal), var(--warm-gold))"
                    : "var(--jade-soft)",
                }}
              />
            )}
          </div>
          <span className="text-text-muted-ls min-w-[36px] flex-shrink-0 text-right text-[11px] font-medium tabular-nums">
            {achievement.progress}/{achievement.target}
          </span>
        </div>
      </div>
    </div>
  );
}

// ============================================
// Unauthenticated state
// ============================================

function UnauthenticatedState() {
  return (
    <div
      className="flex flex-col items-center gap-3 rounded-2xl px-6 py-8 text-center"
      style={{
        background: "var(--ls-card-bg)",
        border: "1px solid var(--ls-border-soft)",
      }}
    >
      <div
        className="flex h-12 w-12 items-center justify-center rounded-full"
        style={{ background: "rgba(74,139,127,0.08)" }}
      >
        <Lock className="text-jade-teal h-5 w-5" />
      </div>
      <div>
        <p className="text-text-dark mb-1 text-sm font-medium">Đăng nhập để theo dõi thành tích</p>
        <p className="text-text-muted-ls text-[12px]">
          Chuỗi hoạt động và thành tựu chỉ dành cho thành viên đã đăng nhập
        </p>
      </div>
      <Link
        href="/auth/login"
        className="text-jade-teal mt-1 rounded-lg px-4 py-2 text-[13px] font-medium transition-colors"
        style={{ background: "rgba(74,139,127,0.1)", border: "1px solid rgba(74,139,127,0.2)" }}
      >
        Đăng nhập ngay
      </Link>
    </div>
  );
}

// ============================================
// Main Component
// ============================================

export function StreakWidget() {
  const { isAuthenticated } = useAuthStore();
  const { data, isLoading } = useUserProgress(isAuthenticated);
  const recordVisit = useRecordVisit();

  const progress = data?.data;
  const streak = progress?.streak;
  const achievements = progress?.achievements ?? [];
  const unlockedCount = progress?.unlocked_count ?? 0;
  const totalAchievements = progress?.total_achievements ?? 0;

  // Auto-record visit on mount (only when authenticated)
  useEffect(() => {
    if (isAuthenticated) {
      recordVisit.mutate();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated]);

  // Not logged in
  if (!isAuthenticated) {
    return (
      <div className="animate-[fadeUp_0.65s_ease-out_both]">
        <div className="mb-4 flex items-center gap-2.5">
          <Trophy className="text-warm-amber h-4 w-4" />
          <span className="text-text-dark text-base font-[var(--font-lora)] tracking-wide">
            Thành Tích
          </span>
        </div>
        <UnauthenticatedState />
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="animate-[fadeUp_0.65s_ease-out_both]">
        <div className="mb-4 flex items-center gap-2.5">
          <Flame className="text-warm-amber h-4 w-4" />
          <span className="text-text-dark text-base font-[var(--font-lora)]">Hoạt Động</span>
        </div>
        <div
          className="mb-4 rounded-2xl p-6"
          style={{ background: "var(--ls-card-bg)", border: "1px solid var(--ls-border-soft)" }}
        >
          <Skeleton className="mx-auto mb-3 h-14 w-14 rounded-full" />
          <Skeleton className="mx-auto mb-2 h-6 w-16" />
          <Skeleton className="mx-auto h-3 w-32" />
        </div>
        <div className="mb-4 grid grid-cols-3 gap-2">
          {[0, 1, 2].map((i) => (
            <Skeleton key={i} className="h-16 rounded-xl" />
          ))}
        </div>
        <div className="flex flex-col gap-2">
          {[0, 1, 2].map((i) => (
            <Skeleton key={i} className="h-16 rounded-xl" />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="animate-[fadeUp_0.65s_ease-out_both]">
      {/* Header */}
      <div className="mb-4 flex items-center gap-2.5">
        <Flame className="text-warm-amber h-4 w-4" />
        <span className="text-text-dark text-base font-[var(--font-lora)] tracking-wide">
          Hoạt Động & Thành Tích
        </span>
      </div>

      {/* Streak hero */}
      <div
        className="mb-4 rounded-2xl p-6 text-center"
        style={{
          background: "linear-gradient(135deg, rgba(196,120,58,0.06), rgba(74,139,127,0.04))",
          border: "1px solid var(--ls-border-warm)",
        }}
      >
        <StreakFire streak={streak?.current_streak ?? 0} />
      </div>

      {/* Stats grid */}
      <div className="mb-5 grid grid-cols-3 gap-2">
        <StatCard
          label="Kỷ lục"
          value={streak?.longest_streak ?? 0}
          icon={<Star className="h-4 w-4" />}
        />
        <StatCard
          label="Tổng truy cập"
          value={streak?.total_visits ?? 0}
          icon={<Zap className="h-4 w-4" />}
        />
        <StatCard
          label="Thành tích"
          value={`${unlockedCount}/${totalAchievements}`}
          icon={<Trophy className="h-4 w-4" />}
        />
      </div>

      {/* Achievements list */}
      {achievements.length > 0 ? (
        <>
          <div className="mb-3 flex items-center gap-2">
            <Trophy className="text-warm-amber h-3.5 w-3.5" />
            <span className="text-text-mid text-[13px] font-medium">Danh sách thành tích</span>
            <span
              className="text-text-muted-ls ml-auto rounded-full px-2 py-0.5 text-[11px]"
              style={{ background: "var(--ls-card-bg)", border: "1px solid var(--ls-border-soft)" }}
            >
              {unlockedCount}/{totalAchievements} đạt
            </span>
          </div>
          <div className="flex flex-col gap-2">
            {achievements.map((ach) => (
              <AchievementBadge key={ach.achievement_key} achievement={ach} />
            ))}
          </div>
        </>
      ) : (
        <div
          className="rounded-xl p-4 text-center"
          style={{ background: "var(--ls-card-bg)", border: "1px solid var(--ls-border-soft)" }}
        >
          <p className="text-text-muted-ls text-[12px]">Chưa có thành tích nào. Hãy tiếp tục truy cập mỗi ngày!</p>
        </div>
      )}
    </div>
  );
}
