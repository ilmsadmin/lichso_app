"use client";

import { useState } from "react";
import Link from "next/link";
import { useAuth } from "@/hooks/useAuth";
import { ROUTES, APP_NAME, ENABLE_SOCIAL_LOGIN } from "@/lib/constants";
import { GoogleLoginButton } from "@/components/auth/GoogleLoginButton";
import type { ApiResponse } from "@/types/api";
import { AxiosError } from "axios";

export function LoginForm() {
  const { login, isLoggingIn } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    login(
      { email, password },
      {
        onError: (err) => {
          if (err instanceof AxiosError) {
            const data = err.response?.data as ApiResponse | undefined;
            setError(data?.message || "Login failed. Please try again.");
          } else {
            setError("An unexpected error occurred.");
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
          className="absolute top-0 right-[20%] left-[20%] h-px"
          style={{
            background: "linear-gradient(90deg, transparent, rgba(196,120,58,0.4), transparent)",
          }}
        />

        {/* Decorative watermark */}
        <span
          className="pointer-events-none absolute -right-2 -bottom-4 text-[100px] leading-none select-none"
          style={{ color: "rgba(196,120,58,0.04)" }}
        >
          🔐
        </span>

        {/* Header */}
        <div className="mb-8 text-center">
          <div className="mb-3 text-4xl">🏮</div>
          <h1 className="text-text-dark text-2xl font-[var(--font-lora)] font-semibold tracking-wide">
            Chào mừng trở lại
          </h1>
          <p className="text-text-soft mt-2 text-sm">Đăng nhập vào tài khoản {APP_NAME}</p>
        </div>

        {/* Error message */}
        {error && (
          <div
            className="text-danger mb-6 rounded-xl p-3 text-sm"
            style={{
              background: "rgba(192,96,96,0.08)",
              border: "1px solid rgba(192,96,96,0.2)",
            }}
          >
            {error}
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Email */}
          <div>
            <label htmlFor="email" className="text-text-mid mb-1.5 block text-[13px] font-medium">
              Email
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
              required
              autoComplete="email"
              className="text-text-dark placeholder:text-text-muted-ls focus:ring-warm-amber/30 w-full rounded-xl px-4 py-2.5 text-sm backdrop-blur-sm transition-all focus:ring-2 focus:outline-none"
              style={{
                background: "var(--ls-card-bg)",
                border: "1px solid var(--ls-border-warm)",
              }}
            />
          </div>

          {/* Password */}
          <div>
            <div className="mb-1.5 flex items-center justify-between">
              <label htmlFor="password" className="text-text-mid block text-[13px] font-medium">
                Mật khẩu
              </label>
              <Link
                href={ROUTES.FORGOT_PASSWORD}
                className="text-warm-amber text-xs hover:underline"
              >
                Quên mật khẩu?
              </Link>
            </div>
            <div className="relative">
              <input
                id="password"
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Nhập mật khẩu"
                required
                autoComplete="current-password"
                className="text-text-dark placeholder:text-text-muted-ls focus:ring-warm-amber/30 w-full rounded-xl px-4 py-2.5 pr-10 text-sm backdrop-blur-sm transition-all focus:ring-2 focus:outline-none"
                style={{
                  background: "var(--ls-card-bg)",
                  border: "1px solid var(--ls-border-warm)",
                }}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="text-text-soft hover:text-warm-amber absolute top-1/2 right-3 -translate-y-1/2 transition-colors"
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
          </div>

          {/* Submit */}
          <button
            type="submit"
            disabled={isLoggingIn}
            className="w-full rounded-xl px-4 py-2.5 text-sm font-semibold text-white transition-all hover:shadow-lg disabled:cursor-not-allowed disabled:opacity-50"
            style={{
              background: "linear-gradient(135deg, var(--warm-amber), var(--warm-gold))",
              boxShadow: "0 4px 16px rgba(196,120,58,0.3)",
            }}
          >
            {isLoggingIn ? (
              <span className="flex items-center justify-center gap-2">
                <span className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
                Đang đăng nhập...
              </span>
            ) : (
              "Đăng nhập"
            )}
          </button>
        </form>

        {/* Social Login */}
        {ENABLE_SOCIAL_LOGIN && (
          <>
            <div className="relative my-6">
              <div className="absolute inset-0 flex items-center">
                <div
                  className="h-px w-full"
                  style={{
                    background:
                      "linear-gradient(90deg, transparent, var(--ls-border-warm), transparent)",
                  }}
                />
              </div>
              <div className="relative flex justify-center text-xs uppercase">
                <span
                  className="text-text-muted-ls px-3 text-[11px] tracking-[2px]"
                  style={{ background: "var(--ls-card-bg-strong)" }}
                >
                  Hoặc
                </span>
              </div>
            </div>

            <GoogleLoginButton onError={(msg) => setError(msg)} />
          </>
        )}

        {/* Footer */}
        <p className="text-text-soft mt-6 text-center text-sm">
          Chưa có tài khoản?{" "}
          <Link href={ROUTES.REGISTER} className="text-warm-amber font-medium hover:underline">
            Tạo tài khoản
          </Link>
        </p>
      </div>
    </div>
  );
}
