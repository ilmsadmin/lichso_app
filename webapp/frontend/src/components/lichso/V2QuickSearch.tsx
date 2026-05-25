"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Search, Calendar } from "lucide-react";

const quickTags = [
  { label: "Hôm qua", href: "#" },
  { label: "Ngày mai", href: "#" },
  { label: "Giỗ Tổ Hùng Vương", href: "/su-kien" },
  { label: "Rằm tháng 7", href: "/su-kien" },
  { label: "Tết 2027", href: "/su-kien" },
];

export function V2QuickSearch() {
  const [query, setQuery] = useState("");
  const router = useRouter();

  const handleSearch = () => {
    if (query.trim()) {
      router.push(`/tra-cuu?q=${encodeURIComponent(query.trim())}`);
    }
  };

  return (
    <div
      className="v2-card rounded-xl p-5"
      style={{
        background: "var(--v2-bg-card)",
        border: "1px solid var(--v2-border-primary)",
        boxShadow: "var(--v2-shadow-xs)",
      }}
    >
      {/* Header */}
      <div className="mb-3.5 flex items-center gap-2.5">
        <div
          className="flex h-8 w-8 items-center justify-center rounded-lg"
          style={{ background: "rgba(30, 100, 200, 0.08)", color: "#1E64C8" }}
        >
          <Search className="h-4 w-4" />
        </div>
        <h3 className="text-[16px] font-bold" style={{ color: "var(--v2-text-primary)" }}>
          Tra cứu nhanh
        </h3>
      </div>

      {/* Search input */}
      <div
        className="mb-2.5 flex items-center gap-2 rounded-xl px-3.5 py-2.5 transition-all focus-within:ring-2"
        style={{
          background: "var(--v2-bg-input)",
          border: "1px solid var(--v2-border-primary)",
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          ["--tw-ring-color" as any]: "var(--v2-bg-accent-soft)",
        }}
      >
        <Calendar className="h-3.5 w-3.5 shrink-0" style={{ color: "var(--v2-text-muted)" }} />
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleSearch()}
          placeholder="Nhập ngày (VD: 01/01/2000)"
          className="flex-1 border-none bg-transparent text-[13px] outline-none"
          style={{ color: "var(--v2-text-primary)" }}
        />
      </div>

      {/* Quick tags */}
      <div className="flex flex-wrap gap-1.5">
        {quickTags.map((tag) => (
          <button
            key={tag.label}
            className="rounded-full border-none px-3 py-1 text-[11px] transition-all"
            style={{ background: "var(--v2-bg-tag)", color: "var(--v2-text-secondary)" }}
            onMouseEnter={(e) => {
              (e.currentTarget as HTMLElement).style.background = "var(--v2-bg-accent)";
              (e.currentTarget as HTMLElement).style.color = "white";
            }}
            onMouseLeave={(e) => {
              (e.currentTarget as HTMLElement).style.background = "var(--v2-bg-tag)";
              (e.currentTarget as HTMLElement).style.color = "var(--v2-text-secondary)";
            }}
            onClick={() => router.push(tag.href)}
          >
            {tag.label}
          </button>
        ))}
      </div>
    </div>
  );
}
