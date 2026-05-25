"use client";

import { useState } from "react";
import { Search, MessageSquareQuote, RefreshCw, Sparkles } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { usePublicQuotes, useQuoteOfTheDay, useRandomQuote } from "@/hooks/usePublicContent";
import type { QuoteListParams } from "@/types/quote";
import { useQueryClient } from "@tanstack/react-query";

export default function QuotesPage() {
  const [params, setParams] = useState<QuoteListParams>({
    page: 1,
    limit: 20,
  });
  const [search, setSearch] = useState("");

  const { data: todayData, isLoading: todayLoading } = useQuoteOfTheDay();
  const { data: quotesData, isLoading } = usePublicQuotes({
    ...params,
    search: search || undefined,
  });
  const queryClient = useQueryClient();

  const todayQuote = todayData?.data;
  const quotes = quotesData?.data ?? [];
  const meta = quotesData?.meta;

  return (
    <div className="mx-auto max-w-[1180px] px-4 py-8 sm:px-6 lg:px-7">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-text-dark mb-2 text-3xl font-[var(--font-lora)] font-semibold">
          Câu Nói Nổi Tiếng
        </h1>
        <p className="text-text-soft">
          Những danh ngôn truyền cảm hứng từ các danh nhân Việt Nam và thế giới
        </p>
      </div>

      {/* Quote of the Day */}
      {todayLoading ? (
        <Skeleton className="mb-8 h-48 w-full rounded-2xl" />
      ) : todayQuote ? (
        <div
          className="relative mb-10 overflow-hidden rounded-2xl p-8"
          style={{
            background: "linear-gradient(135deg, var(--warm-cream) 0%, var(--warm-peach) 100%)",
            border: "1px solid var(--ls-border-warm)",
          }}
        >
          <Sparkles className="text-warm-amber/50 absolute top-4 right-4 h-5 w-5" />
          <p className="text-warm-amber mb-3 text-xs font-medium tracking-widest uppercase">
            Danh ngôn hôm nay
          </p>
          <blockquote className="text-text-dark mb-4 text-xl leading-relaxed font-[var(--font-lora)] italic sm:text-2xl">
            &ldquo;{todayQuote.quote}&rdquo;
          </blockquote>
          {todayQuote.original_quote && todayQuote.original_quote !== todayQuote.quote && (
            <p className="text-text-soft mb-3 text-sm italic">
              &ldquo;{todayQuote.original_quote}&rdquo;
            </p>
          )}
          <div className="flex items-center gap-2">
            <span className="text-text-dark font-medium">— {todayQuote.author}</span>
            {todayQuote.author_bio && (
              <span className="text-text-soft text-sm">({todayQuote.author_bio})</span>
            )}
          </div>
        </div>
      ) : null}

      {/* Search */}
      <div className="mb-8 flex gap-4">
        <div className="relative flex-1">
          <Search className="text-text-muted-ls absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
          <Input
            placeholder="Tìm theo tác giả hoặc nội dung..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setParams((p) => ({ ...p, page: 1 }));
            }}
            className="pl-10"
          />
        </div>
        <Button
          variant="outline"
          onClick={() => queryClient.invalidateQueries({ queryKey: ["public-quotes", "random"] })}
          title="Danh ngôn ngẫu nhiên"
        >
          <RefreshCw className="mr-1.5 h-4 w-4" />
          Ngẫu nhiên
        </Button>
      </div>

      {/* Quotes Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          {Array.from({ length: 6 }).map((_, i) => (
            <Skeleton key={i} className="h-40 rounded-2xl" />
          ))}
        </div>
      ) : quotes.length === 0 ? (
        <div className="py-16 text-center">
          <MessageSquareQuote className="text-text-muted-ls/50 mx-auto mb-4 h-12 w-12" />
          <p className="text-text-soft text-lg">Chưa có danh ngôn nào</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          {quotes.map((quote) => (
            <div
              key={quote.id}
              className="hover:border-warm-amber/30 rounded-2xl border p-6 transition-colors"
              style={{
                background: "var(--ls-card-bg-solid)",
                borderColor: "var(--ls-border-warm)",
              }}
            >
              <MessageSquareQuote className="text-warm-amber/40 mb-3 h-5 w-5" />
              <blockquote className="text-text-dark mb-4 leading-relaxed font-[var(--font-lora)] italic">
                &ldquo;{quote.quote}&rdquo;
              </blockquote>
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-text-dark text-sm font-medium">— {quote.author}</p>
                  {quote.author_bio && <p className="text-text-soft text-xs">{quote.author_bio}</p>}
                </div>
                {quote.tags && quote.tags.length > 0 && (
                  <div className="flex flex-wrap gap-1">
                    {quote.tags.slice(0, 2).map((tag) => (
                      <Badge key={tag} variant="secondary" className="text-[10px]">
                        {tag}
                      </Badge>
                    ))}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Pagination */}
      {meta && meta.total_pages > 1 && (
        <div className="mt-10 flex items-center justify-center gap-2">
          <Button
            variant="outline"
            size="sm"
            disabled={params.page === 1}
            onClick={() => setParams((p) => ({ ...p, page: (p.page ?? 1) - 1 }))}
          >
            Trang trước
          </Button>
          <span className="text-text-soft px-4 text-sm">
            Trang {meta.page} / {meta.total_pages}
          </span>
          <Button
            variant="outline"
            size="sm"
            disabled={meta.page >= meta.total_pages}
            onClick={() => setParams((p) => ({ ...p, page: (p.page ?? 1) + 1 }))}
          >
            Trang sau
          </Button>
        </div>
      )}
    </div>
  );
}
