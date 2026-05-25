"use client";

import { useState } from "react";
import { useSubscribeNewsletter, useUnsubscribeNewsletter } from "@/hooks/useV3";
import { Mail, Check, Loader2 } from "lucide-react";

interface NewsletterSubscribeProps {
  /** Prefill email if user is logged in */
  defaultEmail?: string;
  /** Compact mode for sidebar */
  compact?: boolean;
}

export function NewsletterSubscribe({ defaultEmail, compact = false }: NewsletterSubscribeProps) {
  const [email, setEmail] = useState(defaultEmail || "");
  const [name, setName] = useState("");
  const [submitted, setSubmitted] = useState(false);

  const subscribe = useSubscribeNewsletter();
  const unsubscribe = useUnsubscribeNewsletter();

  const handleSubscribe = (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) return;
    subscribe.mutate(
      { email, name: name || undefined },
      {
        onSuccess: () => setSubmitted(true),
      }
    );
  };

  const handleUnsubscribe = () => {
    if (!email) return;
    unsubscribe.mutate(email, {
      onSuccess: () => setSubmitted(false),
    });
  };

  // Success state
  if (submitted) {
    return (
      <div
        className={`overflow-hidden rounded-2xl ${compact ? "p-4" : "p-6"}`}
        style={{
          background: "linear-gradient(135deg, rgba(74,139,127,0.08), rgba(196,120,58,0.04))",
          border: "1px solid rgba(74,139,127,0.2)",
        }}
      >
        <div className="text-center">
          <div
            className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-full"
            style={{
              background: "rgba(74,139,127,0.15)",
              border: "2px solid rgba(74,139,127,0.3)",
            }}
          >
            <Check className="text-jade-teal h-6 w-6" />
          </div>
          <h4 className="text-text-dark mb-1 text-[15px] font-[var(--font-lora)] font-medium">
            Đăng ký thành công! 🎉
          </h4>
          <p className="text-text-muted-ls mb-3 text-[12px]">
            Bạn sẽ nhận bản tin về lịch vạn niên & văn hoá truyền thống mỗi ngày.
          </p>
          <button
            onClick={handleUnsubscribe}
            className="text-text-muted-ls hover:text-text-mid text-[11px] underline transition-colors"
          >
            Huỷ đăng ký
          </button>
        </div>
      </div>
    );
  }

  return (
    <div
      className={`overflow-hidden rounded-2xl ${compact ? "p-4" : "p-6"}`}
      style={{
        background: "linear-gradient(135deg, var(--warm-cream) 0%, var(--warm-peach) 100%)",
        border: "1px solid var(--ls-border-warm)",
      }}
    >
      {/* Header */}
      <div className="mb-3 flex items-center gap-2">
        <Mail className="text-warm-amber h-4 w-4" />
        <span className="text-text-dark text-[15px] font-[var(--font-lora)] font-medium">
          Bản Tin Lịch Số
        </span>
      </div>

      <p className="text-text-soft mb-4 text-[12px] leading-relaxed">
        Nhận danh ngôn hay, sự kiện lịch sử, tử vi & phong thuỷ mỗi ngày qua email.
      </p>

      <form onSubmit={handleSubscribe} className="flex flex-col gap-2.5">
        {!compact && (
          <input
            type="text"
            placeholder="Tên của bạn"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="text-text-dark placeholder:text-text-muted-ls/50 w-full rounded-lg px-3 py-2.5 text-sm outline-none"
            style={{
              background: "rgba(255,255,255,0.7)",
              border: "1px solid var(--ls-border-soft)",
            }}
          />
        )}
        <input
          type="email"
          required
          placeholder="Email của bạn"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="text-text-dark placeholder:text-text-muted-ls/50 w-full rounded-lg px-3 py-2.5 text-sm outline-none"
          style={{
            background: "rgba(255,255,255,0.7)",
            border: "1px solid var(--ls-border-soft)",
          }}
        />
        <button
          type="submit"
          disabled={subscribe.isPending || !email}
          className="flex w-full items-center justify-center gap-2 rounded-lg py-2.5 text-[13px] font-medium text-white transition-all hover:opacity-90 disabled:opacity-50"
          style={{
            background: "linear-gradient(135deg, var(--jade-teal), var(--jade-soft))",
          }}
        >
          {subscribe.isPending ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              Đang đăng ký...
            </>
          ) : (
            <>
              <Mail className="h-4 w-4" />
              Đăng ký nhận bản tin
            </>
          )}
        </button>
      </form>

      <p className="text-text-muted-ls mt-2 text-center text-[10px]">
        Miễn phí · Huỷ đăng ký bất cứ lúc nào
      </p>
    </div>
  );
}
