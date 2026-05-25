"use client";

import { useState, useMemo, useCallback } from "react";
import Link from "next/link";
import { useLowQualityArticles, useLowQualityCount, useBulkRewriteArticles } from "@/hooks/useAI";
import { useAllCategories } from "@/hooks/useArticles";
import { AIModelSelector } from "./AIModelSelector";
import { AlertTriangle, CheckCircle2, XCircle, Loader2, RefreshCw, Wand2, ChevronLeft, ChevronRight, Search, ChevronDown, ExternalLink, Pencil } from "lucide-react";
import type { AILowQualityArticle, AIBulkRewriteRequest, AIBulkRewriteResponse } from "@/types/ai";
import { toast } from "sonner";

const STATUS_OPTIONS = [
  { value: "", label: "Tất cả trạng thái" },
  { value: "published", label: "Đã xuất bản" },
  { value: "draft", label: "Nháp" },
  { value: "ai_pending", label: "AI chờ xử lý" },
  { value: "review", label: "Chờ duyệt" },
];

const WRITING_STYLES = [
  { value: "popular", label: "Phổ thông" },
  { value: "academic", label: "Hàn lâm" },
  { value: "storytelling", label: "Kể chuyện" },
  { value: "listicle", label: "Danh sách" },
];

const TARGET_LENGTHS = [
  { value: "medium", label: "Vừa (~1500 từ)" },
  { value: "long", label: "Dài (~2500 từ)" },
];

const STATUS_BADGE: Record<string, { bg: string; text: string; label: string }> = {
  published:  { bg: "bg-green-50 border-green-100", text: "text-green-700", label: "Đã xuất bản" },
  draft:      { bg: "bg-gray-50 border-gray-200",   text: "text-gray-600", label: "Nháp" },
  ai_pending: { bg: "bg-yellow-50 border-yellow-100", text: "text-yellow-700", label: "AI chờ xử lý" },
  review:     { bg: "bg-blue-50 border-blue-100",   text: "text-blue-700", label: "Chờ duyệt" },
};

export function AILowQualityArticles() {
  // Pagination
  const [page, setPage] = useState(1);
  const [limit] = useState(20);
  const maxWords = 500;

  // Filters — same as admin/articles
  const [filterStatus, setFilterStatus] = useState<string>("");
  const [filterSearch, setFilterSearch] = useState<string>("");
  const [filterCategoryId, setFilterCategoryId] = useState<string>("");

  // AI rewrite config
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [model, setModel] = useState("openai/gpt-4o-mini");
  const [targetLength, setTargetLength] = useState<string>("medium");
  const [writingStyle, setWritingStyle] = useState<string>("popular");
  const [rewriteResult, setRewriteResult] = useState<AIBulkRewriteResponse | null>(null);

  // Categories for filter dropdown
  const { data: categoriesResp } = useAllCategories();
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const categories = (categoriesResp?.data ?? []) as any[];

  // API queries
  const queryParams = useMemo(() => ({
    max_words: maxWords,
    page,
    limit,
    status: filterStatus || undefined,
    search: filterSearch || undefined,
    category_id: filterCategoryId || undefined,
  }), [page, limit, filterStatus, filterSearch, filterCategoryId]);

  const { data: articlesResp, isLoading, refetch } = useLowQualityArticles(queryParams);
  const { data: countResp } = useLowQualityCount(maxWords);
  const bulkRewrite = useBulkRewriteArticles();

  const articles: AILowQualityArticle[] = useMemo(
    () => (articlesResp?.data as unknown as { data: AILowQualityArticle[] })?.data ?? [],
    [articlesResp]
  );
  const total = (articlesResp?.data as unknown as { total: number })?.total ?? 0;
  const lowQualityCount = (countResp?.data as unknown as { count: number })?.count ?? 0;
  const totalPages = Math.ceil(total / limit);

  const toggleSelect = (id: string) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const toggleSelectAll = () => {
    if (selectedIds.size === articles.length) {
      setSelectedIds(new Set());
    } else {
      setSelectedIds(new Set(articles.map((a) => a.article_id)));
    }
  };

  const handleFilterChange = useCallback(() => {
    setPage(1);
    setSelectedIds(new Set());
  }, []);

  const handleBulkRewrite = async () => {
    if (selectedIds.size === 0) {
      toast.error("Vui lòng chọn ít nhất 1 bài viết");
      return;
    }

    const req: AIBulkRewriteRequest = {
      article_ids: Array.from(selectedIds),
      target_length: targetLength as "short" | "medium" | "long",
      writing_style: writingStyle as "academic" | "popular" | "storytelling" | "listicle",
      model,
    };

    try {
      const result = await bulkRewrite.mutateAsync(req);
      setRewriteResult(result?.data ?? null);
      setSelectedIds(new Set());
      refetch();
    } catch {
      // error handled by hook
    }
  };

  const getStatusBadge = (status: string) => {
    const badge = STATUS_BADGE[status] ?? { bg: "bg-gray-50 border-gray-200", text: "text-gray-600", label: status };
    return badge;
  };

  return (
    <div className="space-y-4">
      {/* Summary card */}
      <div className="rounded-2xl border border-amber-200 bg-amber-50 p-4">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-100">
            <AlertTriangle className="h-5 w-5 text-amber-600" />
          </div>
          <div>
            <h3 className="text-sm font-semibold text-amber-800">
              Bài viết chưa chất lượng
            </h3>
            <p className="text-xs text-amber-600">
              Có <span className="font-bold text-amber-800">{lowQualityCount}</span> bài viết trong hệ thống có nội dung dưới {maxWords} từ, chưa chuẩn SEO.
              Sử dụng AI để viết lại và cải thiện chất lượng nội dung.
            </p>
          </div>
        </div>
      </div>

      {/* Filters bar — same as admin/articles */}
      <div className="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
        <h4 className="mb-3 text-xs font-semibold uppercase tracking-wider text-gray-500">
          Bộ lọc bài viết
        </h4>
        <div className="flex flex-wrap items-end gap-3">
          {/* Search */}
          <div className="flex-1 min-w-[200px]">
            <label className="mb-1 block text-[11px] uppercase tracking-wider text-gray-400">
              Tìm kiếm
            </label>
            <div className="relative">
              <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-gray-400" />
              <input
                type="text"
                value={filterSearch}
                onChange={(e) => {
                  setFilterSearch(e.target.value);
                  handleFilterChange();
                }}
                placeholder="Tìm theo tiêu đề bài viết..."
                className="w-full rounded-xl border border-gray-200 bg-gray-50 py-2 pl-8 pr-3 text-xs text-gray-700 outline-none focus:border-indigo-300 focus:bg-white"
              />
            </div>
          </div>

          {/* Status */}
          <div>
            <label className="mb-1 block text-[11px] uppercase tracking-wider text-gray-400">
              Trạng thái
            </label>
            <div className="relative">
              <select
                value={filterStatus}
                onChange={(e) => {
                  setFilterStatus(e.target.value);
                  handleFilterChange();
                }}
                className="appearance-none rounded-xl border border-gray-200 bg-gray-50 pl-3 pr-7 py-2 text-xs text-gray-700 outline-none focus:border-indigo-300 focus:bg-white"
              >
                {STATUS_OPTIONS.map((o) => (
                  <option key={o.value} value={o.value}>{o.label}</option>
                ))}
              </select>
              <ChevronDown className="pointer-events-none absolute right-2 top-1/2 -translate-y-1/2 h-3 w-3 text-gray-400" />
            </div>
          </div>

          {/* Category */}
          <div>
            <label className="mb-1 block text-[11px] uppercase tracking-wider text-gray-400">
              Danh mục
            </label>
            <div className="relative">
              <select
                value={filterCategoryId}
                onChange={(e) => {
                  setFilterCategoryId(e.target.value);
                  handleFilterChange();
                }}
                className="appearance-none rounded-xl border border-gray-200 bg-gray-50 pl-3 pr-7 py-2 text-xs text-gray-700 outline-none focus:border-indigo-300 focus:bg-white"
              >
                <option value="">Tất cả danh mục</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
              <ChevronDown className="pointer-events-none absolute right-2 top-1/2 -translate-y-1/2 h-3 w-3 text-gray-400" />
            </div>
          </div>
        </div>
      </div>

      {/* Config bar — AI rewrite settings */}
      <div className="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
        <h4 className="mb-3 text-xs font-semibold uppercase tracking-wider text-gray-500">
          Cấu hình viết lại bằng AI
        </h4>
        <div className="flex flex-wrap items-end gap-3">
          <div>
            <label className="mb-1 block text-[11px] uppercase tracking-wider text-gray-400">
              Độ dài mục tiêu
            </label>
            <select
              value={targetLength}
              onChange={(e) => setTargetLength(e.target.value)}
              className="rounded-xl border border-gray-200 bg-gray-50 px-3 py-2 text-xs text-gray-700 outline-none focus:border-indigo-300 focus:bg-white"
            >
              {TARGET_LENGTHS.map((t) => (
                <option key={t.value} value={t.value}>{t.label}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="mb-1 block text-[11px] uppercase tracking-wider text-gray-400">
              Phong cách viết
            </label>
            <select
              value={writingStyle}
              onChange={(e) => setWritingStyle(e.target.value)}
              className="rounded-xl border border-gray-200 bg-gray-50 px-3 py-2 text-xs text-gray-700 outline-none focus:border-indigo-300 focus:bg-white"
            >
              {WRITING_STYLES.map((s) => (
                <option key={s.value} value={s.value}>{s.label}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="mb-1 block text-[11px] uppercase tracking-wider text-gray-400">
              AI Model
            </label>
            <AIModelSelector value={model} onChange={setModel} />
          </div>
          <button
            onClick={handleBulkRewrite}
            disabled={selectedIds.size === 0 || bulkRewrite.isPending}
            className="flex items-center gap-2 rounded-xl px-4 py-2 text-xs font-medium text-white transition-all hover:opacity-90 disabled:opacity-50"
            style={{
              background: "linear-gradient(135deg, #6366f1, #a855f7)",
              boxShadow: "0 4px 16px rgba(99,102,241,0.3)",
            }}
          >
            {bulkRewrite.isPending ? (
              <Loader2 className="h-3.5 w-3.5 animate-spin" />
            ) : (
              <Wand2 className="h-3.5 w-3.5" />
            )}
            {bulkRewrite.isPending
              ? `Đang viết lại ${selectedIds.size} bài…`
              : `Viết lại ${selectedIds.size} bài đã chọn`}
          </button>
        </div>
      </div>

      {/* Rewrite results */}
      {rewriteResult && (
        <div className="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
          <h4 className="mb-3 flex items-center gap-2 text-sm font-semibold text-gray-700">
            <CheckCircle2 className="h-4 w-4 text-green-500" />
            Kết quả viết lại
          </h4>
          <div className="mb-3 flex gap-4 text-xs">
            <span className="text-gray-500">
              Tổng: <span className="font-semibold text-gray-700">{rewriteResult.total_requested}</span>
            </span>
            <span className="text-green-600">
              Thành công: <span className="font-semibold">{rewriteResult.succeeded}</span>
            </span>
            {rewriteResult.failed > 0 && (
              <span className="text-red-600">
                Thất bại: <span className="font-semibold">{rewriteResult.failed}</span>
              </span>
            )}
          </div>
          <div className="max-h-60 overflow-y-auto space-y-1">
            {rewriteResult.results.map((r) => (
              <div
                key={r.article_id}
                className={`flex items-center justify-between rounded-xl px-3 py-2 text-xs ${
                  r.status === "success"
                    ? "bg-green-50 text-green-800"
                    : "bg-red-50 text-red-800"
                }`}
              >
                <div className="flex items-center gap-2 min-w-0 flex-1">
                  {r.status === "success" ? (
                    <CheckCircle2 className="h-3.5 w-3.5 flex-shrink-0 text-green-500" />
                  ) : (
                    <XCircle className="h-3.5 w-3.5 flex-shrink-0 text-red-500" />
                  )}
                  <span className="font-medium truncate">{r.title}</span>
                  {r.status === "success" && (
                    <Link
                      href={`/admin/articles/${r.article_id}/edit`}
                      className="inline-flex items-center gap-1 rounded-lg bg-indigo-100 px-2 py-0.5 text-[10px] font-medium text-indigo-700 hover:bg-indigo-200 transition-colors flex-shrink-0"
                    >
                      <Pencil className="h-2.5 w-2.5" />
                      Sửa bài
                    </Link>
                  )}
                </div>
                <div className="flex items-center gap-3 text-[11px] flex-shrink-0 ml-2">
                  {r.status === "success" ? (
                    <>
                      <span className="text-green-600 font-semibold">{r.new_word_count} từ</span>
                      <span>{r.tokens_used} tokens</span>
                      <span>${r.cost_usd?.toFixed(4)}</span>
                    </>
                  ) : (
                    <span>{r.error}</span>
                  )}
                </div>
              </div>
            ))}
          </div>
          <button
            onClick={() => setRewriteResult(null)}
            className="mt-2 text-xs text-gray-400 hover:text-gray-600"
          >
            Đóng kết quả
          </button>
        </div>
      )}

      {/* Article list */}
      <div className="rounded-2xl border border-gray-200 bg-white shadow-sm">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
          <div className="flex items-center gap-3">
            <label className="flex items-center gap-2 text-xs text-gray-500">
              <input
                type="checkbox"
                checked={articles.length > 0 && selectedIds.size === articles.length}
                onChange={toggleSelectAll}
                className="accent-indigo-500"
              />
              Chọn tất cả
            </label>
            <span className="text-xs text-gray-400">
              {total} bài viết dưới {maxWords} từ
            </span>
          </div>
          <button
            onClick={() => refetch()}
            disabled={isLoading}
            className="flex items-center gap-1 text-xs text-gray-400 hover:text-gray-600"
          >
            <RefreshCw className={`h-3 w-3 ${isLoading ? "animate-spin" : ""}`} />
            Làm mới
          </button>
        </div>

        {/* List */}
        {isLoading ? (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="h-6 w-6 animate-spin text-indigo-400" />
          </div>
        ) : articles.length === 0 ? (
          <div className="py-12 text-center">
            <CheckCircle2 className="mx-auto h-10 w-10 text-green-400" />
            <p className="mt-2 text-sm font-medium text-gray-600">
              Không tìm thấy bài viết chưa chất lượng
            </p>
            <p className="text-xs text-gray-400">
              Tất cả bài viết đều có nội dung ≥ {maxWords} từ{filterStatus ? ` (trạng thái: ${STATUS_OPTIONS.find(o => o.value === filterStatus)?.label})` : ""}
            </p>
          </div>
        ) : (
          <div className="divide-y divide-gray-50">
            {articles.map((article) => {
              const badge = getStatusBadge(article.status);
              return (
                <div
                  key={article.article_id}
                  className={`flex items-center gap-3 px-4 py-3 transition-colors hover:bg-gray-50 ${
                    selectedIds.has(article.article_id) ? "bg-indigo-50/50" : ""
                  }`}
                >
                  <input
                    type="checkbox"
                    checked={selectedIds.has(article.article_id)}
                    onChange={() => toggleSelect(article.article_id)}
                    className="accent-indigo-500"
                  />
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium text-gray-800">
                      {article.title}
                    </p>
                    <div className="mt-0.5 flex flex-wrap items-center gap-1.5 text-[11px] text-gray-400">
                      <span className={`rounded border px-1.5 py-0.5 ${badge.bg} ${badge.text}`}>
                        {badge.label}
                      </span>
                      {article.category_name && (
                        <span className="rounded bg-gray-100 px-1.5 py-0.5 text-gray-500">
                          {article.category_name}
                        </span>
                      )}
                      <span>{article.created_at?.split("T")[0]}</span>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 text-right">
                    <span className="inline-flex items-center gap-1 rounded-full bg-red-50 px-2.5 py-1 text-[11px] font-semibold text-red-600 border border-red-100">
                      <AlertTriangle className="h-3 w-3" />
                      {article.word_count} từ
                    </span>
                    <Link
                      href={`/admin/articles/${article.article_id}/edit`}
                      className="inline-flex items-center gap-1 rounded-lg border border-gray-200 bg-white px-2 py-1 text-[11px] text-gray-500 hover:bg-gray-50 hover:text-indigo-600 transition-colors"
                      title="Sửa bài viết"
                    >
                      <ExternalLink className="h-3 w-3" />
                      Sửa
                    </Link>
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between border-t border-gray-100 px-4 py-3">
            <span className="text-xs text-gray-400">
              Trang {page}/{totalPages}
            </span>
            <div className="flex gap-1">
              <button
                onClick={() => setPage((p) => Math.max(1, p - 1))}
                disabled={page === 1}
                className="rounded-lg border border-gray-200 p-1.5 text-gray-400 hover:bg-gray-50 disabled:opacity-40"
              >
                <ChevronLeft className="h-3.5 w-3.5" />
              </button>
              <button
                onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                disabled={page === totalPages}
                className="rounded-lg border border-gray-200 p-1.5 text-gray-400 hover:bg-gray-50 disabled:opacity-40"
              >
                <ChevronRight className="h-3.5 w-3.5" />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
