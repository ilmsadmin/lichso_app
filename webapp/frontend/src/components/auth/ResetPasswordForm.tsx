"use client";

import { useState } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { useAuth } from "@/hooks/useAuth";
import { ROUTES } from "@/lib/constants";
import type { ApiResponse } from "@/types/api";
import { AxiosError } from "axios";

export function ResetPasswordForm() {
  const searchParams = useSearchParams();
  const token = searchParams.get("token") || "";
  const { resetPassword, isResettingPassword } = useAuth();
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [showPassword, setShowPassword] = useState(false);

  if (!token) {
    return (
      <div className="w-full max-w-md">
        <div
          className="relative overflow-hidden rounded-[20px] p-8 backdrop-blur-[16px] sm:p-9"
          style={{
            background: "var(--ls-card-bg-strong)",
            border: "1px solid var(--ls-border-warm)",
            boxShadow: "0 8px 40px var(--ls-shadow-warm), 0 2px 0 rgba(255,255,255,0.8) inset",
          }}
        >
          <div className="text-center">
            <div className="mb-4 text-4xl">⚠️</div>
            <h2 className="text-danger text-xl font-[var(--font-lora)] font-semibold">
              Liên kết không hợp lệ
            </h2>
            <p className="text-text-soft mt-2 text-sm">
              Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.
            </p>
            <Link
              href={ROUTES.FORGOT_PASSWORD}
              className="text-warm-amber mt-4 inline-block text-sm font-medium hover:underline"
            >
              Yêu cầu liên kết mới
            </Link>
          </div>
        </div>
      </div>
    );
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setFieldErrors({});

    if (password !== confirmPassword) {
      setFieldErrors({ confirm_password: "Mật khẩu không khớp" });
      return;
    }

    if (password.length < 8) {
      setFieldErrors({ password: "Mật khẩu phải có ít nhất 8 ký tự" });
      return;
    }

    resetPassword(
      { token, password },
      {
        onError: (err) => {
          if (err instanceof AxiosError) {
            const data = err.response?.data as ApiResponse | undefined;
            setError(data?.message || "Đặt lại mật khẩu thất bại. Vui lòng thử lại.");
          } else {
            setError("Đã xảy ra lỗi không mong muốn.");
          }
        },
      }
    );
  };

  return (
    <div className="w-full max-w-md">
      <div
        className="relative overflow-hidden rounded-[20px] p-8 backdrop-blur-[16px] sm:p-9"
        style={{
          background: "var(--ls-card-bg-strong)",
          border: "1px solid var(--ls-border-warm)",
          boxShadow: "0 8px 40px var(--ls-shadow-warm), 0 2px 0 rgba(255,255,255,0.8) inset",
        }}
      >
        {/* Top shimmer line */}
        <div
          className="absolute inset-x-0 top-0 h-[2px]"
          style={{
            background: "linear-gradient(90deg, transparent, var(--warm-amber), transparent)",
          }}
        />

        {/* Watermark */}
        <div className="pointer-events-none absolute top-3 right-4 text-5xl opacity-[0.07] select-none">
          🔒
        </div>

        {/* Header */}
        <div className="mb-8 text-center">
          <h1
            className="text-2xl font-bold tracking-tight"
            style={{ color: "var(--ls-text-dark)", fontFamily: "var(--font-lora)" }}
          >
            Đặt lại mật khẩu
          </h1>
          <p className="mt-2 text-sm" style={{ color: "var(--ls-text-soft)" }}>
            Nhập mật khẩu mới cho tài khoản của bạn
          </p>
        </div>

        {/* Error message */}
        {error && (
          <div className="mb-6 rounded-xl border border-red-300/40 bg-red-50/60 p-3 text-sm text-red-700 dark:border-red-500/30 dark:bg-red-900/20 dark:text-red-300">
            {error}
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-5">
          {/* New Password */}
          <div>
            <label
              htmlFor="password"
              className="mb-1.5 block text-sm font-medium"
              style={{ color: "var(--ls-text-dark)" }}
            >
              Mật khẩu mới
            </label>
            <div className="relative">
              <input
                id="password"
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Tối thiểu 8 ký tự"
                required
                autoComplete="new-password"
                className="w-full rounded-xl px-4 py-2.5 pr-10 text-sm transition-all focus:ring-2 focus:outline-none"
                style={{
                  background: "var(--ls-card-bg)",
                  border: "1px solid var(--ls-border-warm)",
                  color: "var(--ls-text-dark)",
                  // @ts-expect-error CSS custom properties
                  "--tw-ring-color": "rgba(196,120,58,0.3)",
                }}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute top-1/2 right-3 -translate-y-1/2 transition-colors"
                style={{ color: "var(--ls-text-soft)" }}
              >
                {showPassword ? (
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                    <line x1="1" y1="1" x2="23" y2="23" />
                  </svg>
                ) : (
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                    <circle cx="12" cy="12" r="3" />
                  </svg>
                )}
              </button>
            </div>
            {fieldErrors.password && (
              <p className="mt-1 text-xs text-red-600 dark:text-red-400">{fieldErrors.password}</p>
            )}
          </div>

          {/* Confirm Password */}
          <div>
            <label
              htmlFor="confirm_password"
              className="mb-1.5 block text-sm font-medium"
              style={{ color: "var(--ls-text-dark)" }}
            >
              Xác nhận mật khẩu mới
            </label>
            <input
              id="confirm_password"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Nhập lại mật khẩu mới"
              required
              autoComplete="new-password"
              className="w-full rounded-xl px-4 py-2.5 text-sm transition-all focus:ring-2 focus:outline-none"
              style={{
                background: "var(--ls-card-bg)",
                border: "1px solid var(--ls-border-warm)",
                color: "var(--ls-text-dark)",
                // @ts-expect-error CSS custom properties
                "--tw-ring-color": "rgba(196,120,58,0.3)",
              }}
            />
            {fieldErrors.confirm_password && (
              <p className="mt-1 text-xs text-red-600 dark:text-red-400">
                {fieldErrors.confirm_password}
              </p>
            )}
          </div>

          {/* Submit */}
          <button
            type="submit"
            disabled={isResettingPassword}
            className="w-full rounded-xl px-4 py-2.5 text-sm font-semibold text-white shadow-md transition-all hover:brightness-105 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-50"
            style={{
              background: "linear-gradient(135deg, var(--warm-amber), var(--warm-gold))",
              boxShadow: "0 4px 16px var(--ls-shadow-warm)",
            }}
          >
            {isResettingPassword ? (
              <span className="flex items-center justify-center gap-2">
                <span className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
                Đang đặt lại...
              </span>
            ) : (
              "Đặt lại mật khẩu"
            )}
          </button>
        </form>

        {/* Footer */}
        <p className="mt-6 text-center text-sm" style={{ color: "var(--ls-text-soft)" }}>
          Nhớ mật khẩu rồi?{" "}
          <Link
            href={ROUTES.LOGIN}
            className="font-medium hover:underline"
            style={{ color: "var(--warm-amber)" }}
          >
            Đăng nhập
          </Link>
        </p>
      </div>
    </div>
  );
}
