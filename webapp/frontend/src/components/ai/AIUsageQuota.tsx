"use client";

import { AIUsageQuotaResponse } from "@/types/ai";
import { Skeleton } from "@/components/ui/skeleton";
import { Clock, Zap } from "lucide-react";
import { cn } from "@/lib/utils";

interface AIUsageQuotaProps {
  quota?: AIUsageQuotaResponse;
  isLoading?: boolean;
  className?: string;
}

export function AIUsageQuota({ quota, isLoading, className }: AIUsageQuotaProps) {
  if (isLoading) {
    return <Skeleton className={cn("h-7 w-48 rounded-full", className)} />;
  }

  if (!quota) return null;

  const { remaining, limit } = quota;
  const pct = limit > 0 ? (remaining / limit) * 100 : 0;

  const color =
    remaining === 0
      ? "bg-red-100 text-red-700 border-red-200"
      : pct <= 25
      ? "bg-orange-100 text-orange-700 border-orange-200"
      : "bg-emerald-100 text-emerald-700 border-emerald-200";

  const icon =
    remaining === 0 ? (
      <Clock className="w-3.5 h-3.5" />
    ) : (
      <Zap className="w-3.5 h-3.5" />
    );

  const resetTime = new Date(quota.reset_at).toLocaleTimeString("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
  });

  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 px-3 py-1 rounded-full border text-xs font-medium",
        color,
        className
      )}
    >
      {icon}
      {remaining === 0 ? (
        <>Hết lượt · Reset {resetTime}</>
      ) : (
        <>
          Còn {remaining}/{limit} lượt · Reset {resetTime}
        </>
      )}
    </span>
  );
}
