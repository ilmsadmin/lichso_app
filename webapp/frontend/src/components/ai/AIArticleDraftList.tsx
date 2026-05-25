"use client";

import { useState } from "react";
import { useAIArticles, useUpdateAIArticleStatus } from "@/hooks/useAI";
import type { AIArticleItem } from "@/types/ai";
import {
  FileText,
  Rocket,
  Pencil,
  Loader2,
  RefreshCw,
  ChevronLeft,
  ChevronRight,
  CheckCircle2,
} from "lucide-react";
import Link from "next/link";
import { toast } from "sonner";

export function AIArticleDraftList() {
  const [page, setPage] = useState(1);
  const { data: resp, isLoading, refetch } = useAIArticles("draft", page, 15);
  const updateStatus = useUpdateAIArticleStatus();

  const articles = resp?.data?.data ?? [];
  const total = resp?.data?.total ?? 0;
  const pageSize = resp?.data?.page_size ?? 15;
  const totalPages = Math.ceil(total / pageSize);

  const handlePublish = (article: AIArticleItem) => {
    updateStatus.mutate(
      { id: article.id, status: "published" },
      {
        onSuccess: () => toast.success(`Đã xuất bản: "${article.title}"`),
      }
    );
  };

  const handleMoveToReview = (article: AIArticleItem) => {
    updateStatus.mutate(
      { id: article.id, status: "review" },
      {
        onSuccess: () => toast.success(`Đã chuyển sang Đang duyệt`),
      }
    );
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-20 text-gray-400">
        <Loader2 className="h-6 w-6 animate-spin" />
        <span className="ml-2 text-sm">Đang tải...</span>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Toolbar */}
      <div className="flex items-center justify-between">
        <p className="text-sm text-gray-500">
          <span className="font-semibold text-gray-900">{total}</span> bài nháp chờ xuất bản
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
          <FileText className="mb-3 h-10 w-10 text-gray-300" />
          <p className="font-medium">Chưa có bài nháp nào</p>
          <p className="mt-1 text-sm">Sử dụng &quot;Viết chi tiết&quot; để chuyển bài từ tab Chờ xử lý</p>
        </div>
      ) : (
        <>
          {/* Table */}
          <div className="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm">
            <table className="w-full text-sm">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-5 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
                    Tiêu đề
                  </th>
                  <th className="hidden px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-500 sm:table-cell">
                    Danh mục
                  </th>
                  <th className="hidden px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-500 md:table-cell">
                    Chi phí
                  </th>
                  <th className="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-gray-500">
                    Hành động
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {articles.map((article) => (
                  <DraftRow
                    key={article.id}
                    article={article}
                    isUpdating={updateStatus.isPending}
                    onPublish={() => handlePublish(article)}
                    onReview={() => handleMoveToReview(article)}
                  />
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-3">
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
        </>
      )}
    </div>
  );
}

// ─── Row ──────────────────────────────────────────────────────────────────────

function DraftRow({
  article,
  isUpdating,
  onPublish,
  onReview,
}: {
  article: AIArticleItem;
  isUpdating: boolean;
  onPublish: () => void;
  onReview: () => void;
}) {
  return (
    <tr className="group hover:bg-gray-50 transition-colors">
      <td className="px-5 py-3.5">
        <div>
          <p className="font-medium text-gray-900 line-clamp-1">{article.title}</p>
          <p className="mt-0.5 text-xs text-gray-400 line-clamp-1">{article.excerpt}</p>
        </div>
      </td>
      <td className="hidden px-4 py-3.5 sm:table-cell">
        {article.category ? (
          <span className="rounded-full bg-indigo-50 px-2.5 py-1 text-xs font-medium text-indigo-600">
            {article.category.name}
          </span>
        ) : (
          <span className="text-xs text-gray-400">—</span>
        )}
      </td>
      <td className="hidden px-4 py-3.5 text-xs text-gray-500 md:table-cell">
        <div>
          {article.tokens_used && (
            <span className="block">{article.tokens_used.toLocaleString()} tokens</span>
          )}
          {article.cost_usd && <span className="block">${article.cost_usd.toFixed(4)}</span>}
        </div>
      </td>
      <td className="px-4 py-3.5">
        <div className="flex items-center justify-end gap-2">
          {/* Edit */}
          <Link
            href={`/admin/articles/${article.id}/edit`}
            className="flex items-center gap-1 rounded-lg border border-gray-200 px-2.5 py-1.5 text-xs font-medium text-gray-600 hover:bg-gray-50 transition-colors"
          >
            <Pencil className="h-3.5 w-3.5" />
            Sửa
          </Link>

          {/* Move to review */}
          <button
            onClick={onReview}
            disabled={isUpdating}
            className="flex items-center gap-1 rounded-lg border border-amber-200 bg-amber-50 px-2.5 py-1.5 text-xs font-medium text-amber-700 hover:bg-amber-100 disabled:opacity-50 transition-colors"
          >
            <CheckCircle2 className="h-3.5 w-3.5" />
            Duyệt
          </button>

          {/* Publish directly */}
          <button
            onClick={onPublish}
            disabled={isUpdating}
            className="flex items-center gap-1 rounded-lg px-2.5 py-1.5 text-xs font-medium text-white disabled:opacity-50 transition-opacity hover:opacity-90"
            style={{ background: "linear-gradient(135deg, #6366f1, #a855f7)" }}
          >
            <Rocket className="h-3.5 w-3.5" />
            Xuất bản
          </button>
        </div>
      </td>
    </tr>
  );
}
