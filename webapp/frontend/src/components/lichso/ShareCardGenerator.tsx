"use client";

import { useRef, useState, useCallback } from "react";
import { Download, Share2, Copy, Check } from "lucide-react";

interface ShareCardData {
  solarDay: number;
  solarMonth: number;
  solarYear: number;
  lunarDay: string;
  lunarMonth: string;
  dayOfWeek: string;
  canChi: string;
  trucNgay?: string;
  chiSoNgay?: number;
  quote?: string;
  quoteAuthor?: string;
}

interface ShareCardGeneratorProps {
  data: ShareCardData;
}

const CARD_THEMES = [
  {
    key: "jade",
    name: "Ngọc bích",
    bg: "linear-gradient(135deg, #2d6a5e 0%, #4a8b7f 50%, #a7c4b5 100%)",
    text: "#fff",
    accent: "#ffd89b",
  },
  {
    key: "amber",
    name: "Hổ phách",
    bg: "linear-gradient(135deg, #8b5e3c 0%, #c4783a 50%, #f5d4a8 100%)",
    text: "#fff",
    accent: "#ffefc3",
  },
  {
    key: "ink",
    name: "Mực tàu",
    bg: "linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)",
    text: "#e8e8e8",
    accent: "#e2b049",
  },
  {
    key: "lotus",
    name: "Hoa sen",
    bg: "linear-gradient(135deg, #e8b4b8 0%, #ee7d8a 50%, #bf4356 100%)",
    text: "#fff",
    accent: "#ffeab5",
  },
];

export function ShareCardGenerator({ data }: ShareCardGeneratorProps) {
  const cardRef = useRef<HTMLDivElement>(null);
  const [themeIdx, setThemeIdx] = useState(0);
  const [copied, setCopied] = useState(false);
  const [downloading, setDownloading] = useState(false);

  const theme = CARD_THEMES[themeIdx];

  const shareUrl = `https://lichso.vn/ngay/${data.solarYear}-${String(data.solarMonth).padStart(2, "0")}-${String(data.solarDay).padStart(2, "0")}`;

  const getShareText = useCallback(
    (includeUrl = true) => {
      return [
        `📅 ${data.dayOfWeek}, ${data.solarDay}/${data.solarMonth}/${data.solarYear}`,
        `🌙 Âm lịch: ${data.lunarDay} ${data.lunarMonth}`,
        `🏮 ${data.canChi}`,
        data.trucNgay ? `📋 Trực: ${data.trucNgay}` : "",
        data.chiSoNgay ? `⭐ Chỉ số ngày: ${data.chiSoNgay}%` : "",
        data.quote ? `\n💬 "${data.quote}"` : "",
        data.quoteAuthor ? `— ${data.quoteAuthor}` : "",
        "",
        includeUrl ? `🔗 ${shareUrl}` : "🔗 Xem thêm tại lichso.vn",
      ]
        .filter(Boolean)
        .join("\n");
    },
    [data, shareUrl]
  );

  const handleCopyText = useCallback(async () => {
    const text = getShareText(false);
    try {
      await navigator.clipboard.writeText(text);
    } catch {
      // Fallback for non-secure contexts or when permission is denied
      const textarea = document.createElement("textarea");
      textarea.value = text;
      textarea.style.position = "fixed";
      textarea.style.left = "-9999px";
      textarea.style.opacity = "0";
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand("copy");
      document.body.removeChild(textarea);
    }
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }, [getShareText]);

  const handleDownload = useCallback(async () => {
    if (!cardRef.current) return;
    setDownloading(true);

    try {
      const html2canvas = (await import("html2canvas")).default;
      const canvas = await html2canvas(cardRef.current, {
        scale: 3,
        backgroundColor: null,
        useCORS: true,
      });

      const fileName = `lichso-${data.solarDay}-${data.solarMonth}-${data.solarYear}.png`;

      // Convert to blob for better mobile compatibility
      const blob = await new Promise<Blob | null>((resolve) => canvas.toBlob(resolve, "image/png"));

      if (!blob) throw new Error("Failed to create image blob");

      // On mobile: try sharing the image directly (saves to Photos / share sheet)
      if (
        typeof navigator.share === "function" &&
        navigator.canShare?.({
          files: [new File([blob], fileName, { type: "image/png" })],
        })
      ) {
        await navigator.share({
          files: [new File([blob], fileName, { type: "image/png" })],
        });
        return;
      }

      // On desktop: download via blob URL
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.download = fileName;
      link.href = url;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
    } catch {
      // Fallback: copy text
      handleCopyText();
    } finally {
      setDownloading(false);
    }
  }, [data, handleCopyText]);

  const handleShare = useCallback(async () => {
    if (typeof navigator.share !== "function") {
      handleCopyText();
      return;
    }

    const shareTitle = `Lịch ngày ${data.solarDay}/${data.solarMonth}/${data.solarYear} — ${data.dayOfWeek}`;
    const shareText = getShareText(true);

    // Try to share as image first (better for social media)
    try {
      if (cardRef.current) {
        const html2canvas = (await import("html2canvas")).default;
        const canvas = await html2canvas(cardRef.current, {
          scale: 3,
          backgroundColor: null,
          useCORS: true,
        });
        const blob = await new Promise<Blob | null>((resolve) =>
          canvas.toBlob(resolve, "image/png")
        );
        if (
          blob &&
          navigator.canShare?.({
            files: [new File([blob], "lichso.png", { type: "image/png" })],
          })
        ) {
          await navigator.share({
            title: shareTitle,
            text: shareText,
            files: [
              new File([blob], `lichso-${data.solarDay}-${data.solarMonth}-${data.solarYear}.png`, {
                type: "image/png",
              }),
            ],
          });
          return;
        }
      }
    } catch {
      // Image share not supported or cancelled, fall through to text share
    }

    // Fallback: share text only (do NOT pass url separately — many apps ignore text when url is present)
    try {
      await navigator.share({
        title: shareTitle,
        text: shareText,
      });
    } catch {
      handleCopyText();
    }
  }, [data, getShareText, handleCopyText]);

  return (
    <div className="animate-[fadeUp_0.65s_ease-out_both]">
      {/* Header */}
      <div className="mb-4 flex items-center gap-2.5">
        <Share2 className="text-warm-amber h-4 w-4" />
        <span className="text-text-dark text-base font-[var(--font-lora)] tracking-wide">
          Chia Sẻ Lịch Ngày
        </span>
      </div>

      {/* Theme selector */}
      <div className="mb-4 flex items-center gap-2">
        <span className="text-text-muted-ls text-[11px]">Chủ đề:</span>
        {CARD_THEMES.map((t, idx) => (
          <button
            key={t.key}
            onClick={() => setThemeIdx(idx)}
            className={`h-7 w-7 rounded-full transition-all ${
              themeIdx === idx ? "ring-jade-teal scale-110 ring-2 ring-offset-2" : ""
            }`}
            style={{ background: t.bg }}
            title={t.name}
          />
        ))}
      </div>

      {/* Card preview */}
      <div
        ref={cardRef}
        className="relative overflow-hidden rounded-2xl p-6"
        style={{
          background: theme.bg,
          color: theme.text,
          minHeight: 260,
        }}
      >
        {/* Decorative pattern */}
        <div
          className="absolute inset-0 opacity-[0.06]"
          style={{
            backgroundImage:
              "url(\"data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='1'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E\")",
          }}
        />

        {/* Top branding */}
        <div className="relative z-10">
          <div className="mb-4 text-[10px] tracking-[3px] uppercase opacity-60">
            Lịch Số · lichso.vn
          </div>

          {/* Day number hero */}
          <div className="mb-3 flex items-end gap-3">
            <span
              className="text-7xl leading-none font-[var(--font-lora)] font-bold"
              style={{ color: theme.accent }}
            >
              {String(data.solarDay).padStart(2, "0")}
            </span>
            <div className="pb-2">
              <span className="block text-[13px] opacity-80">{data.dayOfWeek}</span>
              <span className="text-xl font-[var(--font-lora)] font-semibold">
                Tháng {data.solarMonth}, {data.solarYear}
              </span>
            </div>
          </div>

          {/* Divider */}
          <div className="mb-3 h-px opacity-20" style={{ background: theme.text }} />

          {/* Lunar & Can Chi */}
          <div className="mb-3 flex items-center gap-4">
            <div>
              <span className="block text-[10px] opacity-50">Âm lịch</span>
              <span className="text-[14px] font-medium">
                {data.lunarDay} {data.lunarMonth}
              </span>
            </div>
            <div className="h-6 w-px opacity-20" style={{ background: theme.text }} />
            <div>
              <span className="block text-[10px] opacity-50">Can Chi</span>
              <span className="text-[14px] font-medium" style={{ color: theme.accent }}>
                {data.canChi}
              </span>
            </div>
            {data.trucNgay && (
              <>
                <div className="h-6 w-px opacity-20" style={{ background: theme.text }} />
                <div>
                  <span className="block text-[10px] opacity-50">Trực</span>
                  <span className="text-[14px] font-medium">{data.trucNgay}</span>
                </div>
              </>
            )}
          </div>

          {/* Day score */}
          {data.chiSoNgay !== undefined && (
            <div className="mb-3">
              <div className="flex items-center gap-2">
                <div
                  className="h-1 flex-1 overflow-hidden rounded-full opacity-30"
                  style={{ background: theme.text }}
                >
                  <div
                    className="h-full rounded-full"
                    style={{
                      width: `${data.chiSoNgay}%`,
                      background: theme.accent,
                    }}
                  />
                </div>
                <span className="text-[11px] font-semibold" style={{ color: theme.accent }}>
                  {data.chiSoNgay}%
                </span>
              </div>
            </div>
          )}

          {/* Quote */}
          {data.quote && (
            <div className="mt-3 opacity-80">
              <p className="text-[12px] leading-relaxed italic">&ldquo;{data.quote}&rdquo;</p>
              {data.quoteAuthor && (
                <span className="mt-1 block text-[10px] opacity-60">— {data.quoteAuthor}</span>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Action buttons */}
      <div className="mt-4 flex gap-2">
        <button
          onClick={handleDownload}
          disabled={downloading}
          className="flex flex-1 items-center justify-center gap-2 rounded-lg py-2.5 text-[13px] font-medium text-white transition-all hover:opacity-90 disabled:opacity-50"
          style={{
            background: "linear-gradient(135deg, var(--jade-teal), var(--jade-soft))",
          }}
        >
          <Download className="h-4 w-4" />
          {downloading ? "Đang tải..." : ""}
        </button>
        <button
          onClick={handleCopyText}
          className="hover:bg-warm-cream/50 flex items-center justify-center gap-2 rounded-lg px-4 py-2.5 text-[13px] font-medium transition-all"
          style={{
            background: "var(--ls-card-bg)",
            border: "1px solid var(--ls-border-soft)",
            color: copied ? "var(--jade-teal)" : "var(--text-mid)",
          }}
        >
          {copied ? <Check className="h-4 w-4" /> : <Copy className="h-4 w-4" />}
          {copied ? "Đã sao chép" : "Sao chép"}
        </button>
        <button
          onClick={handleShare}
          className="hover:bg-warm-amber/10 flex items-center justify-center gap-2 rounded-lg px-4 py-2.5 text-[13px] font-medium transition-all"
          style={{
            background: "var(--ls-card-bg)",
            border: "1px solid var(--ls-border-warm)",
            color: "var(--warm-amber)",
          }}
        >
          <Share2 className="h-4 w-4" />
          Chia sẻ
        </button>
      </div>
    </div>
  );
}
