"use client";

import { useState } from "react";
import Link from "next/link";
import { useAuth } from "@/hooks/useAuth";
import { ROUTES, APP_NAME } from "@/lib/constants";
import type { ApiResponse } from "@/types/api";
import { AxiosError } from "axios";

export function ForgotPasswordForm() {
  const { forgotPassword, isSendingReset } = useAuth();
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    forgotPassword(
      { email },
      {
        onSuccess: () => {
          setSuccess(true);
        },
        onError: (err) => {
          if (err instanceof AxiosError) {
            const data = err.response?.data as ApiResponse | undefined;
            setError(data?.message || "Request failed. Please try again.");
          } else {
            setError("An unexpected error occurred.");
          }
        },
      }
    );
  };

  if (success) {
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
            <div className="mb-4 text-5xl">📬</div>
            <h2 className="text-text-dark text-xl font-[var(--font-lora)] font-semibold">
              Kiểm tra email
            </h2>
            <p className="text-text-soft mt-2 text-sm leading-relaxed">
              Nếu tài khoản với email đó tồn tại, chúng tôi đã gửi hướng dẫn đặt lại mật khẩu.
            </p>
            <Link
              href={ROUTES.LOGIN}
              className="text-warm-amber mt-6 inline-block text-sm font-medium hover:underline"
            >
              ← Quay lại đăng nhập
            </Link>
          </div>
        </div>
      </div>
    );
  }

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
          🔑
        </span>

        {/* Header */}
        <div className="mb-8 text-center">
          <div className="mb-3 text-4xl">🧭</div>
          <h1 className="text-text-dark text-2xl font-[var(--font-lora)] font-semibold tracking-wide">
            Quên mật khẩu?
          </h1>
          <p className="text-text-soft mt-2 text-sm leading-relaxed">
            Nhập email của bạn, chúng tôi sẽ gửi liên kết đặt lại mật khẩu
          </p>
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

          <button
            type="submit"
            disabled={isSendingReset}
            className="w-full rounded-xl px-4 py-2.5 text-sm font-semibold text-white transition-all hover:shadow-lg disabled:cursor-not-allowed disabled:opacity-50"
            style={{
              background: "linear-gradient(135deg, var(--warm-amber), var(--warm-gold))",
              boxShadow: "0 4px 16px rgba(196,120,58,0.3)",
            }}
          >
            {isSendingReset ? (
              <span className="flex items-center justify-center gap-2">
                <span className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
                Đang gửi...
              </span>
            ) : (
              "Gửi liên kết đặt lại"
            )}
          </button>
        </form>

        {/* Footer */}
        <p className="text-text-soft mt-6 text-center text-sm">
          Nhớ mật khẩu rồi?{" "}
          <Link href={ROUTES.LOGIN} className="text-warm-amber font-medium hover:underline">
            Đăng nhập
          </Link>
        </p>
      </div>
    </div>
  );
}
