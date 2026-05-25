"use client";

import { useState } from "react";
import { Search } from "lucide-react";

interface SearchBarProps {
  onSearch?: (query: string) => void;
}

export function SearchBar({ onSearch }: SearchBarProps) {
  const [query, setQuery] = useState("");

  const handleSearch = () => {
    if (onSearch && query.trim()) {
      onSearch(query.trim());
    }
  };

  return (
    <div className="mt-5 mb-0 flex gap-2.5">
      <input
        type="text"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        onKeyDown={(e) => e.key === "Enter" && handleSearch()}
        placeholder="Tra cứu ngày âm dương, xem ngày tốt xấu... (VD: 15/7/2025 · Giáp Tý · Ngày cưới tháng 8)"
        className="flex-1 rounded-[10px] px-4 py-2.5 text-[13.5px] backdrop-blur-lg transition-all outline-none"
        style={{
          background: "var(--ls-card-bg)",
          border: "1px solid var(--ls-border-warm)",
          color: "var(--ls-text-dark)",
          boxShadow: "0 2px 12px var(--ls-shadow-warm)",
          fontFamily: "var(--font-vietnam), var(--font-geist-sans), sans-serif",
        }}
        onFocus={(e) => {
          e.currentTarget.style.borderColor = "rgba(196,120,58,0.45)";
          e.currentTarget.style.background = "var(--ls-card-bg-strong)";
          e.currentTarget.style.boxShadow = "0 4px 20px rgba(196,120,58,0.1)";
        }}
        onBlur={(e) => {
          e.currentTarget.style.borderColor = "var(--ls-border-warm)";
          e.currentTarget.style.background = "var(--ls-card-bg)";
          e.currentTarget.style.boxShadow = "0 2px 12px var(--ls-shadow-warm)";
        }}
      />
      <button
        onClick={handleSearch}
        className="flex items-center gap-1.5 rounded-[10px] px-5 py-2.5 text-[13px] font-medium tracking-wide whitespace-nowrap text-white transition-all hover:-translate-y-px"
        style={{
          background: "linear-gradient(135deg, var(--warm-amber), var(--warm-gold))",
          boxShadow: "0 4px 14px rgba(196,120,58,0.3)",
          fontFamily: "var(--font-vietnam), var(--font-geist-sans), sans-serif",
        }}
      >
        <Search className="h-3.5 w-3.5" />
        Tra Cứu
      </button>
    </div>
  );
}
