"use client";

import { useState } from "react";
import { useAIArticles, useRefineAIArticle, useUpdateAIArticleStatus, usePromptTemplates } from "@/hooks/useAI";
import type { AIArticleItem, AIArticleRefineRequest } from "@/types/ai";
import {
  Sparkles,
  Pencil,
  Loader2,
  Clock,
  RefreshCw,
  ChevronLeft,
  ChevronRight,
  X,
} from "lucide-react";
import Link from "next/link";
import { toast } from "sonner";

// ─── Refine Dialog ───────────────────────────────────────────────────────────

interface RefineDialogProps {
  article: AIArticleItem;
  onClose: () => void;
  onSuccess: () => void;
}

function RefineDialog({ article, onClose, onSuccess }: RefineDialogProps) {
  const { data: promptsResp } = usePromptTemplates("article");
  const prompts = promptsResp?.data ?? [];
  const refine = useRefineAIArticle();
  const updateStatus = useUpdateAIArticleStatus();

  const [form, setForm] = useState<AIArticleRefineRequest>({
    instruction: "",
    target_length: "medium",
    writing_style: "popular",
    prompt_id: undefined,
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    // Đóng popup ngay lập tức — AI sẽ xử lý nền
    onClose();
    toast.info(`⏳ AI đang viết chi tiết bài "${article.title}". Vui lòng chờ vài phút…`, {
      duration: 8000,
    });

    refine.mutate(
      { id: article.id, req: form },
      {
        onSuccess: () => {
          toast.success(`✅ Đã viết xong "${article.title}" — kiểm tra tab Nháp!`);
          onSuccess();
        },
        onError: (err: unknown) => {
          const msg =
            (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
            "AI gặp lỗi khi viết chi tiết";
          toast.error(`❌ ${msg}`);
          // Cập nhật status bài về ai_pending để thử lại sau
          updateStatus.mutate({ id: article.id, status: "ai_pending" });
        },
      }
    );
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="w-full max-w-lg rounded-2xl bg-white shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-gray-100 p-5">
          <div>
            <h3 className="flex items-center gap-2 text-base font-semibold text-gray-900">
              <Sparkles className="h-4 w-4 text-indigo-500" />
              AI Viết Chi Tiết
            </h3>
            <p className="mt-0.5 text-xs text-gray-500 line-clamp-1">{article.title}</p>
          </div>
          <button
            onClick={onClose}
            className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 transition-colors"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4 p-5">
          {/* Prompt template selector */}
          {prompts.length > 0 && (
            <div>
              <label className="mb-1.5 block text-xs font-medium text-gray-600 uppercase tracking-wide">
                Mẫu prompt
              </label>
              <div className="grid grid-cols-2 gap-2">
                <button
                  type="button"
                  onClick={() => setForm((f) => ({ ...f, prompt_id: undefined }))}
                  className={`rounded-xl border px-3 py-2 text-left text-xs transition-all ${
                    !form.prompt_id
                      ? "border-indigo-300 bg-indigo-50 text-indigo-700"
                      : "border-gray-200 text-gray-600 hover:border-gray-300 hover:bg-gray-50"
                  }`}
                >
                  <span className="block font-medium">Mặc định</span>
                  <span className="block text-[10px] opacity-70">Prompt chuẩn</span>
                </button>
                {prompts.map((p) => (
                  <button
                    key={p.id}
                    type="button"
                    onClick={() => setForm((f) => ({ ...f, prompt_id: p.id }))}
                    className={`rounded-xl border px-3 py-2 text-left text-xs transition-all ${
                      form.prompt_id === p.id
                        ? "border-indigo-300 bg-indigo-50 text-indigo-700"
                        : "border-gray-200 text-gray-600 hover:border-gray-300 hover:bg-gray-50"
                    }`}
                  >
                    <span className="block font-medium line-clamp-1">{p.name}</span>
                    <span className="block text-[10px] opacity-70">{p.model?.split("/")[1] ?? p.model}</span>
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Optional instruction */}
          <div>
            <label className="mb-1.5 block text-xs font-medium text-gray-600 uppercase tracking-wide">
              Hướng dẫn thêm (tuỳ chọn)
            </label>
            <textarea
              value={form.instruction ?? ""}
              onChange={(e) => setForm((f) => ({ ...f, instruction: e.target.value }))}
              placeholder="Ví dụ: Thêm ví dụ thực tế, viết theo góc nhìn phong thuỷ hiện đại..."
              rows={3}
              className="w-full rounded-xl border border-gray-200 bg-gray-50 px-3 py-2.5 text-sm text-gray-800 outline-none focus:border-indigo-300 focus:bg-white resize-none"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            {/* Target length */}
            <div>
              <label className="mb-1.5 block text-xs font-medium text-gray-600 uppercase tracking-wide">
                Độ dài mục tiêu
              </label>
              <select
                value={form.target_length}
                onChange={(e) =>
                  setForm((f) => ({
                    ...f,
                    target_length: e.target.value as AIArticleRefineRequest["target_length"],
                  }))
                }
                className="w-full rounded-xl border border-gray-200 bg-gray-50 px-3 py-2.5 text-sm text-gray-800 outline-none focus:border-indigo-300"
              >
                <option value="short">Ngắn (~500 từ)</option>
                <option value="medium">Vừa (~1000 từ)</option>
                <option value="long">Dài (~2000 từ)</option>
              </select>
            </div>

            {/* Writing style */}
            <div>
              <label className="mb-1.5 block text-xs font-medium text-gray-600 uppercase tracking-wide">
                Phong cách
              </label>
              <select
                value={form.writing_style}
                onChange={(e) =>
                  setForm((f) => ({
                    ...f,
                    writing_style: e.target.value as AIArticleRefineRequest["writing_style"],
                  }))
                }
                className="w-full rounded-xl border border-gray-200 bg-gray-50 px-3 py-2.5 text-sm text-gray-800 outline-none focus:border-indigo-300"
              >
                <option value="popular">Phổ thông</option>
                <option value="academic">Hàn lâm</option>
                <option value="storytelling">Kể chuyện</option>
                <option value="listicle">Danh sách</option>
              </select>
            </div>
          </div>

          <div className="flex gap-3 pt-1">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 rounded-xl border border-gray-200 py-2.5 text-sm font-medium text-gray-600 hover:bg-gray-50 transition-colors"
            >
              Huỷ
            </button>
            <button
              type="submit"
              className="flex flex-1 items-center justify-center gap-2 rounded-xl py-2.5 text-sm font-medium text-white transition-opacity hover:opacity-90"
              style={{ background: "linear-gradient(135deg, #6366f1, #a855f7)" }}
            >
              <Sparkles className="h-4 w-4" />
              Viết chi tiết
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

// ─── Main Component ───────────────────────────────────────────────────────────

interface AIArticlePendingQueueProps {
  onRefineSuccess?: () => void;
}

export function AIArticlePendingQueue({ onRefineSuccess }: AIArticlePendingQueueProps) {
  const [page, setPage] = useState(1);
  const [refineTarget, setRefineTarget] = useState<AIArticleItem | null>(null);
  const { data: resp, isLoading, refetch } = useAIArticles("ai_pending", page, 15);

  const articles = resp?.data?.data ?? [];
  const total = resp?.data?.total ?? 0;
  const pageSize = resp?.data?.page_size ?? 15;
  const totalPages = Math.ceil(total / pageSize);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-20 text-gray-400">
        <Loader2 className="h-6 w-6 animate-spin" />
        <span className="ml-2 text-sm">Đang tải...</span>
      </div>
    );
  }

  return (
    <>
      {/* Refine dialog */}
      {refineTarget && (
        <RefineDialog
          article={refineTarget}
          onClose={() => setRefineTarget(null)}
          onSuccess={() => {
            setRefineTarget(null);
            onRefineSuccess?.();
          }}
        />
      )}

      <div className="space-y-4">
        {/* Toolbar */}
        <div className="flex items-center justify-between">
          <p className="text-sm text-gray-500">
            <span className="font-semibold text-gray-900">{total}</span> bài chờ xử lý
          </p>
          <button
            onClick={() => refetch()}
            className="flex items-center gap-1.5 rounded-xl border border-gray-200 bg-white px-3 py-2 text-xs font-medium text-gray-600 hover:bg-gray-50 transition-colors"
          >
            <RefreshCw className="h-3.5 w-3.5" />
            Làm mới
          </button>
        </div>

        {articles.length === 0 ? (
          <div className="flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-gray-200 py-20 text-gray-400">
            <Clock className="mb-3 h-10 w-10 text-gray-300" />
            <p className="font-medium">Chưa có bài nào đang chờ xử lý</p>
            <p className="mt-1 text-sm">Tạo bài mới ở tab &quot;Tạo bài&quot;</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {articles.map((article) => (
              <ArticlePendingCard
                key={article.id}
                article={article}
                onRefine={() => setRefineTarget(article)}
              />
            ))}
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-center gap-3 pt-2">
            <button
              onClick={() => setPage((p) => Math.max(1, p - 1))}
              disabled={page === 1}
              className="rounded-lg border border-gray-200 p-2 text-gray-500 hover:bg-gray-50 disabled:opacity-40 transition-colors"
            >
              <ChevronLeft className="h-4 w-4" />
            </button>
            <span className="text-sm text-gray-600">
              Trang {page} / {totalPages}
            </span>
            <button
              onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
              disabled={page === totalPages}
              className="rounded-lg border border-gray-200 p-2 text-gray-500 hover:bg-gray-50 disabled:opacity-40 transition-colors"
            >
              <ChevronRight className="h-4 w-4" />
            </button>
          </div>
        )}
      </div>
    </>
  );
}

// ─── Card ─────────────────────────────────────────────────────────────────────

function ArticlePendingCard({
  article,
  onRefine,
}: {
  article: AIArticleItem;
  onRefine: () => void;
}) {
  return (
    <div className="group flex flex-col rounded-2xl border border-gray-200 bg-white p-4 shadow-sm transition-shadow hover:shadow-md">
      {/* Status badge */}
      <div className="mb-2 flex items-center gap-2">
        <span className="inline-flex items-center gap-1 rounded-full bg-amber-100 px-2.5 py-0.5 text-[11px] font-medium text-amber-700">
          <Clock className="h-3 w-3" />
          Chờ xử lý
        </span>
        {article.category && (
          <span className="rounded-full bg-gray-100 px-2 py-0.5 text-[11px] text-gray-500">
            {article.category.name}
          </span>
        )}
      </div>

      {/* Title */}
      <h4 className="flex-1 text-sm font-semibold text-gray-900 leading-snug line-clamp-2 mb-2">
        {article.title}
      </h4>

      {/* Excerpt */}
      <p className="mb-3 text-xs text-gray-500 line-clamp-2">{article.excerpt}</p>

      {/* Meta */}
      <div className="mb-3 flex flex-wrap gap-2 text-[11px] text-gray-400">
        {article.tokens_used && <span>{article.tokens_used.toLocaleString()} tokens</span>}
        {article.cost_usd && <span>· ${article.cost_usd.toFixed(4)}</span>}
        {article.reading_time > 0 && <span>· {article.reading_time} phút đọc</span>}
      </div>

      {/* Actions */}
      <div className="flex gap-2">
        <button
          onClick={onRefine}
          className="flex flex-1 items-center justify-center gap-1.5 rounded-xl py-2 text-xs font-medium text-white transition-opacity hover:opacity-90"
          style={{ background: "linear-gradient(135deg, #6366f1, #a855f7)" }}
        >
          <Sparkles className="h-3.5 w-3.5" />
          Viết chi tiết
        </button>
        <Link
          href={`/admin/articles/${article.id}/edit`}
          className="flex items-center justify-center gap-1.5 rounded-xl border border-gray-200 px-3 py-2 text-xs font-medium text-gray-600 hover:bg-gray-50 transition-colors"
        >
          <Pencil className="h-3.5 w-3.5" />
          Sửa
        </Link>
      </div>
    </div>
  );
}
