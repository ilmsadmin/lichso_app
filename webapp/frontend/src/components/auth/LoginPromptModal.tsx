"use client";

import { useEffect, useState, useCallback } from "react";
import { useAuthStore } from "@/stores/authStore";
import { GoogleLoginButton } from "@/components/auth/GoogleLoginButton";
import Link from "next/link";

const SESSION_KEY = "ls_login_prompt_dismissed";
const DELAY_MS = 3500;

export function LoginPromptModal() {
  const { isAuthenticated, isLoading } = useAuthStore();
  const [open, setOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!mounted || isLoading || isAuthenticated) return;

    // Only show once per session
    if (sessionStorage.getItem(SESSION_KEY)) return;

    const timer = setTimeout(() => {
      setOpen(true);
    }, DELAY_MS);

    return () => clearTimeout(timer);
  }, [mounted, isLoading, isAuthenticated]);

  const dismiss = useCallback(() => {
    setOpen(false);
    sessionStorage.setItem(SESSION_KEY, "1");
  }, []);

  // Close on backdrop click
  const handleBackdrop = useCallback(
    (e: React.MouseEvent<HTMLDivElement>) => {
      if (e.target === e.currentTarget) dismiss();
    },
    [dismiss]
  );

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-[999] flex items-end justify-center sm:items-center"
      style={{ background: "rgba(30,18,8,0.55)", backdropFilter: "blur(6px)" }}
      onClick={handleBackdrop}
    >
      {/* Modal panel */}
      <div
        className="relative w-full max-w-sm rounded-t-[28px] px-7 pb-10 pt-8 sm:rounded-[28px]"
        style={{
          background: "var(--ls-card-bg-solid, #fdf8f0)",
          border: "1.5px solid var(--ls-border-warm, rgba(200,144,42,0.25))",
          boxShadow:
            "0 0 0 6px var(--ls-card-ring, rgba(200,144,42,0.04)), 0 32px 80px rgba(30,18,8,0.35)",
        }}
      >
        {/* Top shimmer */}
        <div
          className="absolute top-0 right-[20%] left-[20%] h-px rounded-full"
          style={{
            background:
              "linear-gradient(90deg, transparent, rgba(196,120,58,0.5), transparent)",
          }}
        />

        {/* Corner ornaments */}
        <span
          className="pointer-events-none absolute top-3 left-4 select-none text-[11px]"
          style={{ color: "rgba(200,144,42,0.35)" }}
        >
          ✦
        </span>
        <span
          className="pointer-events-none absolute right-4 bottom-3 select-none text-[11px]"
          style={{ color: "rgba(200,144,42,0.35)" }}
        >
          ✦
        </span>

        {/* Close button */}
        <button
          type="button"
          onClick={dismiss}
          className="absolute top-3.5 right-4 flex h-7 w-7 items-center justify-center rounded-full text-[18px] transition-colors"
          style={{ color: "var(--ls-text-muted)", background: "transparent" }}
          aria-label="Đóng"
        >
          ×
        </button>

        {/* Lotus watermark */}
        <span
          className="pointer-events-none absolute right-4 bottom-10 select-none text-[90px] leading-none opacity-[0.035]"
          style={{ color: "#c4783a" }}
        >
          🌸
        </span>

        {/* Icon */}
        <div className="mb-4 flex justify-center">
          <div
            className="flex h-14 w-14 items-center justify-center rounded-full text-3xl"
            style={{
              background: "rgba(200,144,42,0.1)",
              border: "1.5px solid rgba(200,144,42,0.22)",
            }}
          >
            🗓
          </div>
        </div>

        {/* Title */}
        <h2
          className="mb-1.5 text-center font-[var(--font-playfair)] text-[22px] font-bold leading-tight"
          style={{ color: "var(--ls-text-dark, #2d1a06)" }}
        >
          Lịch Số dành riêng cho bạn
        </h2>

        {/* Subtitle */}
        <p
          className="mb-6 text-center text-[13.5px] leading-relaxed"
          style={{ color: "var(--ls-text-soft, #7a5c3a)" }}
        >
          Đăng nhập để lưu nhắc nhở, đánh dấu ngày tốt
          <br className="hidden sm:block" /> và cá nhân hoá lịch của bạn.
        </p>

        {/* Features list */}
        <div className="mb-6 space-y-2">
          {[
            { icon: "🔖", text: "Lưu bookmark ngày & sự kiện" },
            { icon: "⏰", text: "Đặt nhắc nhở quan trọng" },
            { icon: "✨", text: "Nội dung được cá nhân hoá" },
          ].map(({ icon, text }) => (
            <div key={text} className="flex items-center gap-2.5">
              <span
                className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full text-[13px]"
                style={{
                  background: "rgba(61,128,112,0.08)",
                  border: "1px solid rgba(61,128,112,0.16)",
                }}
              >
                {icon}
              </span>
              <span
                className="text-[13px]"
                style={{ color: "var(--ls-text-mid, #5a3e20)" }}
              >
                {text}
              </span>
            </div>
          ))}
        </div>

        {/* Divider */}
        <div className="mb-5 flex items-center gap-3">
          <span
            className="h-px flex-1"
            style={{
              background:
                "linear-gradient(90deg, transparent, rgba(200,144,42,0.3), transparent)",
            }}
          />
          <span className="text-[11px]" style={{ color: "rgba(200,144,42,0.5)" }}>
            ✦
          </span>
          <span
            className="h-px flex-1"
            style={{
              background:
                "linear-gradient(90deg, transparent, rgba(200,144,42,0.3), transparent)",
            }}
          />
        </div>

        {/* Google login button */}
        {error && (
          <p className="mb-3 text-center text-[12px]" style={{ color: "#c0392b" }}>
            {error}
          </p>
        )}
        <GoogleLoginButton onError={setError} />

        {/* Alt: email login */}
        <div className="mt-4 text-center">
          <Link
            href="/dang-nhap"
            onClick={dismiss}
            className="text-[13px] underline-offset-2 hover:underline"
            style={{ color: "var(--ls-text-muted, #a07850)" }}
          >
            Đăng nhập bằng email
          </Link>
        </div>

        {/* Dismiss link */}
        <div className="mt-3 text-center">
          <button
            type="button"
            onClick={dismiss}
            className="text-[12px] transition-opacity hover:opacity-70"
            style={{ color: "var(--ls-text-muted, #a07850)" }}
          >
            Để sau
          </button>
        </div>
      </div>
    </div>
  );
}
