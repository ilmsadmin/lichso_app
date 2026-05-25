"use client";

import { useState } from "react";
import { useAIStats, useAILogs, usePromptTemplates, useDeletePromptTemplate } from "@/hooks/useAI";
import { Skeleton } from "@/components/ui/skeleton";
import { Bot, TrendingUp, FileText, MessageSquare, DollarSign, Zap, Trash2, Plus, Edit } from "lucide-react";
import Link from "next/link";
import { AIPromptTemplate, AILogEntry } from "@/types/ai";

function StatCard({
  label,
  value,
  sub,
  icon: Icon,
  color,
}: {
  label: string;
  value: string | number;
  sub?: string;
  icon: typeof Bot;
  color: string;
}) {
  return (
    <div className="rounded-2xl bg-white p-5 shadow-sm border border-gray-100">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-medium text-gray-500">{label}</p>
          <p className="mt-1 text-2xl font-bold text-gray-900">{value}</p>
          {sub && <p className="mt-0.5 text-xs text-gray-400">{sub}</p>}
        </div>
        <div className={`flex h-10 w-10 items-center justify-center rounded-xl ${color}`}>
          <Icon className="h-5 w-5 text-white" />
        </div>
      </div>
    </div>
  );
}

export default function AIDashboardPage() {
  const [days, setDays] = useState(30);
  const [logsPage, setLogsPage] = useState(1);

  const { data: statsResp, isLoading: statsLoading } = useAIStats(days);
  const stats = statsResp?.data;

  const { data: logsResp, isLoading: logsLoading } = useAILogs({ page: logsPage, limit: 20 });
  const logs: AILogEntry[] = logsResp?.data?.data ?? [];
  const logsTotal = logsResp?.data?.total ?? 0;

  const { data: promptsResp } = usePromptTemplates();
  const prompts = (promptsResp?.data ?? []) as AIPromptTemplate[];

  const deletePrompt = useDeletePromptTemplate();

  return (
    <div className="mx-auto max-w-6xl space-y-8 p-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="flex items-center gap-2 text-2xl font-bold text-gray-900">
            <Bot className="h-6 w-6 text-indigo-500" />
            AI Dashboard
          </h1>
          <p className="mt-1 text-sm text-gray-500">Thống kê sử dụng AI và quản lý prompt templates.</p>
        </div>
        <select
          value={days}
          onChange={(e) => setDays(parseInt(e.target.value))}
          className="rounded-xl border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 outline-none"
        >
          <option value={7}>7 ngày</option>
          <option value={30}>30 ngày</option>
          <option value={90}>90 ngày</option>
        </select>
      </div>

      {/* Stats grid */}
      {statsLoading ? (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-6">
          {[...Array(6)].map((_, i) => <Skeleton key={i} className="h-28 rounded-2xl" />)}
        </div>
      ) : stats ? (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-6">
          <StatCard label="Tổng yêu cầu" value={(stats.total_requests ?? 0).toLocaleString()} icon={Zap} color="bg-indigo-500" />
          <StatCard label="Tổng tokens" value={((stats.total_tokens ?? 0) / 1000).toFixed(1) + "K"} icon={TrendingUp} color="bg-purple-500" />
          <StatCard label="Chi phí" value={"$" + (stats.total_cost_usd ?? 0).toFixed(4)} icon={DollarSign} color="bg-green-500" />
          <StatCard label="Bài viết AI" value={stats.articles_generated ?? 0} icon={FileText} color="bg-amber-500" />
          <StatCard label="Xem tử vi" value={stats.horoscopes_read ?? 0} icon={Bot} color="bg-pink-500" />
          <StatCard label="Chat" value={stats.chat_messages ?? 0} icon={MessageSquare} color="bg-sky-500" />
        </div>
      ) : null}

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Cost by day chart */}
        {stats?.cost_by_day && stats.cost_by_day.length > 0 && (
          <div className="rounded-2xl bg-white p-5 shadow-sm border border-gray-100">
            <h3 className="mb-4 text-sm font-semibold text-gray-700">Chi phí theo ngày</h3>
            <div className="space-y-2 max-h-64 overflow-y-auto">
              {stats.cost_by_day.slice(-14).map((d) => {
                const maxCost = Math.max(...stats.cost_by_day.map((x) => x.cost_usd), 0.001);
                return (
                  <div key={d.date} className="flex items-center gap-3">
                    <span className="w-24 text-xs text-gray-500">{d.date}</span>
                    <div className="h-2 flex-1 rounded-full bg-gray-100">
                      <div
                        className="h-full rounded-full bg-indigo-400"
                        style={{ width: `${Math.min(100, (d.cost_usd / maxCost) * 100)}%` }}
                      />
                    </div>
                    <span className="w-20 text-right text-xs text-gray-500">${d.cost_usd.toFixed(4)}</span>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Requests by model */}
        {stats?.requests_by_model && stats.requests_by_model.length > 0 && (
          <div className="rounded-2xl bg-white p-5 shadow-sm border border-gray-100">
            <h3 className="mb-4 text-sm font-semibold text-gray-700">Sử dụng theo model</h3>
            <div className="space-y-3">
              {stats.requests_by_model.map((m) => {
                const maxReq = Math.max(...stats.requests_by_model.map((x) => x.requests), 1);
                return (
                  <div key={m.model}>
                    <div className="flex justify-between text-xs text-gray-500 mb-1">
                      <span className="truncate max-w-[180px]">{m.model}</span>
                      <span>{m.requests} req · ${m.cost_usd.toFixed(4)}</span>
                    </div>
                    <div className="h-1.5 rounded-full bg-gray-100">
                      <div
                        className="h-full rounded-full bg-purple-400"
                        style={{ width: `${Math.min(100, (m.requests / maxReq) * 100)}%` }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>

      {/* Prompt Templates */}
      <div className="rounded-2xl bg-white p-5 shadow-sm border border-gray-100">
        <div className="mb-4 flex items-center justify-between">
          <h3 className="text-sm font-semibold text-gray-700">Prompt Templates</h3>
          <Link
            href="/admin/ai-dashboard/prompts/new"
            className="flex items-center gap-1.5 rounded-xl bg-indigo-500 px-3 py-1.5 text-xs font-medium text-white hover:bg-indigo-600 transition-colors"
          >
            <Plus className="h-3.5 w-3.5" /> Tạo mới
          </Link>
        </div>
        {prompts.length === 0 ? (
          <p className="text-center text-sm text-gray-400 py-6">Chưa có template nào</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-100 text-left text-xs font-medium text-gray-400">
                  <th className="pb-2 pr-4">Tên</th>
                  <th className="pb-2 pr-4">Loại</th>
                  <th className="pb-2 pr-4">Model</th>
                  <th className="pb-2 pr-4">Trạng thái</th>
                  <th className="pb-2"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {prompts.map((p) => (
                  <tr key={p.id} className="group">
                    <td className="py-3 pr-4 font-medium text-gray-800">{p.name}</td>
                    <td className="py-3 pr-4">
                      <span className={`rounded-full px-2 py-0.5 text-[10px] font-medium ${
                        p.type === "horoscope" ? "bg-purple-100 text-purple-700"
                          : p.type === "article" ? "bg-indigo-100 text-indigo-700"
                          : "bg-green-100 text-green-700"
                      }`}>
                        {p.type}
                      </span>
                    </td>
                    <td className="py-3 pr-4 text-xs text-gray-400 max-w-[140px] truncate">
                      {p.model ?? "default"}
                    </td>
                    <td className="py-3 pr-4">
                      <span className={`rounded-full px-2 py-0.5 text-[10px] font-medium ${
                        p.is_active ? "bg-emerald-100 text-emerald-700" : "bg-gray-100 text-gray-500"
                      }`}>
                        {p.is_active ? "Hoạt động" : "Tắt"}
                      </span>
                    </td>
                    <td className="py-3">
                      <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                        <Link
                          href={`/admin/ai-dashboard/prompts/${p.id}`}
                          className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-50 hover:text-indigo-500 transition-colors"
                        >
                          <Edit className="h-3.5 w-3.5" />
                        </Link>
                        <button
                          onClick={() => deletePrompt.mutate(p.id)}
                          className="rounded-lg p-1.5 text-gray-400 hover:bg-red-50 hover:text-red-500 transition-colors"
                        >
                          <Trash2 className="h-3.5 w-3.5" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Recent Logs */}
      <div className="rounded-2xl bg-white p-5 shadow-sm border border-gray-100">
        <h3 className="mb-4 text-sm font-semibold text-gray-700">Nhật ký AI gần đây</h3>
        {logsLoading ? (
          <div className="space-y-2">
            {[...Array(5)].map((_, i) => <Skeleton key={i} className="h-10 rounded-lg" />)}
          </div>
        ) : logs.length === 0 ? (
          <p className="text-center text-sm text-gray-400 py-6">Chưa có nhật ký nào</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs text-gray-500">
              <thead>
                <tr className="border-b border-gray-100 text-left font-medium text-gray-400">
                  <th className="pb-2 pr-4">Loại</th>
                  <th className="pb-2 pr-4">Model</th>
                  <th className="pb-2 pr-4">Tokens</th>
                  <th className="pb-2 pr-4">Chi phí</th>
                  <th className="pb-2 pr-4">Trạng thái</th>
                  <th className="pb-2">Thời gian</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {logs.map((log) => (
                  <tr key={log.id}>
                    <td className="py-2.5 pr-4">
                      <span className="rounded-full bg-gray-100 px-2 py-0.5 text-[10px]">
                        {log.generation_type}
                      </span>
                    </td>
                    <td className="py-2.5 pr-4 max-w-[140px] truncate">{log.model_used}</td>
                    <td className="py-2.5 pr-4">{(log.total_tokens ?? 0).toLocaleString()}</td>
                    <td className="py-2.5 pr-4">${(log.cost_usd ?? 0).toFixed(5)}</td>
                    <td className="py-2.5 pr-4">
                      <span className={`rounded-full px-2 py-0.5 text-[10px] font-medium ${
                        log.status === "success" ? "bg-emerald-100 text-emerald-700"
                          : log.status === "error" ? "bg-red-100 text-red-600"
                          : "bg-gray-100 text-gray-500"
                      }`}>
                        {log.status}
                      </span>
                    </td>
                    <td className="py-2.5">
                      {new Date(log.created_at).toLocaleString("vi-VN", {
                        month: "2-digit",
                        day: "2-digit",
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {/* Pagination */}
        <div className="mt-4 flex items-center justify-between text-xs text-gray-400">
          <button
            onClick={() => setLogsPage((p) => Math.max(1, p - 1))}
            disabled={logsPage === 1}
            className="rounded-lg border border-gray-200 px-3 py-1.5 hover:bg-gray-50 disabled:opacity-40 transition-colors"
          >
            ← Trước
          </button>
          <span>Trang {logsPage} · {logsTotal} nhật ký</span>
          <button
            onClick={() => setLogsPage((p) => p + 1)}
            disabled={logsPage * 20 >= logsTotal}
            className="rounded-lg border border-gray-200 px-3 py-1.5 hover:bg-gray-50 disabled:opacity-40 transition-colors"
          >
            Sau →
          </button>
        </div>
      </div>
    </div>
  );
}
