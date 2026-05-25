"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { ArrowLeft, Save, Loader2, BookTemplate } from "lucide-react";
import * as aiService from "@/services/aiService";
import type { AIPromptTemplateRequest } from "@/types/ai";

const MODELS = [
  "openai/gpt-4o-mini",
  "openai/gpt-4o",
  "deepseek/deepseek-chat",
  "anthropic/claude-haiku-4.5",
  "anthropic/claude-sonnet-4",
  "google/gemini-flash-1.5",
];

const TYPES = [
  { value: "article", label: "Bài viết" },
  { value: "horoscope", label: "Tử vi" },
  { value: "chat", label: "Chat" },
];

interface PromptFormProps {
  mode: "create" | "edit";
  promptId?: number;
  onSuccess?: () => void;
  onCancel?: () => void;
}

const defaultForm: AIPromptTemplateRequest = {
  name: "",
  type: "article",
  system_prompt: "",
  user_prompt: "",
  model: "openai/gpt-4o-mini",
  max_tokens: 2048,
  temperature: 0.7,
  is_active: true,
};

export function PromptForm({ mode, promptId, onSuccess, onCancel }: PromptFormProps) {
  const router = useRouter();
  const [form, setForm] = useState<AIPromptTemplateRequest>(defaultForm);
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(mode === "edit");

  // Load existing prompt for edit mode
  useEffect(() => {
    if (mode === "edit" && promptId) {
      setFetching(true);
      aiService
        .getPromptTemplate(promptId)
        .then((resp) => {
          const p = resp?.data;
          if (p) {
            setForm({
              name: p.name,
              type: p.type,
              system_prompt: p.system_prompt,
              user_prompt: p.user_prompt,
              model: p.model ?? "openai/gpt-4o-mini",
              max_tokens: p.max_tokens ?? 2048,
              temperature: p.temperature ?? 0.7,
              is_active: p.is_active,
            });
          }
        })
        .catch(() => toast.error("Không tải được prompt template"))
        .finally(() => setFetching(false));
    }
  }, [mode, promptId]);

  const handleChange = <K extends keyof AIPromptTemplateRequest>(
    key: K,
    val: AIPromptTemplateRequest[K]
  ) => setForm((prev) => ({ ...prev, [key]: val }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.name.trim()) return toast.error("Vui lòng nhập tên template");
    if (!form.system_prompt.trim()) return toast.error("Vui lòng nhập System Prompt");
    if (!form.user_prompt.trim()) return toast.error("Vui lòng nhập User Prompt");

    setLoading(true);
    try {
      if (mode === "create") {
        await aiService.createPromptTemplate(form);
        toast.success("Đã tạo prompt template");
      } else if (promptId) {
        await aiService.updatePromptTemplate(promptId, form);
        toast.success("Đã cập nhật prompt template");
      }
      onSuccess?.();
    } catch {
      toast.error(mode === "create" ? "Lỗi tạo template" : "Lỗi cập nhật template");
    } finally {
      setLoading(false);
    }
  };

  if (fetching) {
    return (
      <div className="mx-auto max-w-3xl p-6">
        <div className="flex items-center justify-center py-20">
          <Loader2 className="h-6 w-6 animate-spin text-indigo-500" />
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl space-y-6 p-6">
      {/* Header */}
      <div className="flex items-center gap-3">
        <button
          onClick={() => onCancel ? onCancel() : router.back()}
          className="rounded-xl p-2 text-gray-400 hover:bg-gray-100 transition-colors"
        >
          <ArrowLeft className="h-4 w-4" />
        </button>
        <div>
          <h1 className="flex items-center gap-2 text-xl font-bold text-gray-900">
            <BookTemplate className="h-5 w-5 text-indigo-500" />
            {mode === "create" ? "Tạo Prompt Template" : "Chỉnh sửa Prompt Template"}
          </h1>
          <p className="mt-0.5 text-xs text-gray-500">
            {mode === "create"
              ? "Tạo mẫu prompt mới cho AI tạo nội dung"
              : `Đang chỉnh sửa template #${promptId}`}
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-5">
        {/* Basic info */}
        <div className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm space-y-4">
          <h2 className="text-sm font-semibold text-gray-700">Thông tin cơ bản</h2>

          <div className="grid gap-4 sm:grid-cols-2">
            {/* Name */}
            <div className="sm:col-span-2">
              <label className="mb-1 block text-xs font-medium text-gray-600">
                Tên template <span className="text-red-500">*</span>
              </label>
              <input
                value={form.name}
                onChange={(e) => handleChange("name", e.target.value)}
                placeholder="VD: Bài viết phong thuỷ căn bản"
                required
                className="w-full rounded-xl border border-gray-200 px-3 py-2.5 text-sm outline-none focus:border-indigo-300 focus:ring-2 focus:ring-indigo-50"
              />
            </div>

            {/* Type */}
            <div>
              <label className="mb-1 block text-xs font-medium text-gray-600">Loại</label>
              <select
                value={form.type}
                onChange={(e) => handleChange("type", e.target.value as AIPromptTemplateRequest["type"])}
                className="w-full rounded-xl border border-gray-200 px-3 py-2.5 text-sm outline-none focus:border-indigo-300"
              >
                {TYPES.map((t) => (
                  <option key={t.value} value={t.value}>{t.label}</option>
                ))}
              </select>
            </div>

            {/* Model */}
            <div>
              <label className="mb-1 block text-xs font-medium text-gray-600">Model AI</label>
              <select
                value={form.model ?? ""}
                onChange={(e) => handleChange("model", e.target.value)}
                className="w-full rounded-xl border border-gray-200 px-3 py-2.5 text-sm outline-none focus:border-indigo-300"
              >
                {MODELS.map((m) => (
                  <option key={m} value={m}>{m}</option>
                ))}
              </select>
            </div>

            {/* Max tokens */}
            <div>
              <label className="mb-1 block text-xs font-medium text-gray-600">
                Max tokens <span className="text-gray-400">(100–8000)</span>
              </label>
              <input
                type="number"
                min={100}
                max={8000}
                step={100}
                value={form.max_tokens ?? 2048}
                onChange={(e) => handleChange("max_tokens", parseInt(e.target.value))}
                className="w-full rounded-xl border border-gray-200 px-3 py-2.5 text-sm outline-none focus:border-indigo-300"
              />
            </div>

            {/* Temperature */}
            <div>
              <label className="mb-1 flex items-center justify-between text-xs font-medium text-gray-600">
                Temperature
                <span className="font-mono text-indigo-600">{form.temperature?.toFixed(2)}</span>
              </label>
              <input
                type="range"
                min={0}
                max={1}
                step={0.01}
                value={form.temperature ?? 0.7}
                onChange={(e) => handleChange("temperature", parseFloat(e.target.value))}
                className="w-full accent-indigo-500"
              />
              <div className="mt-1 flex justify-between text-[10px] text-gray-400">
                <span>Chính xác (0)</span>
                <span>Sáng tạo (1)</span>
              </div>
            </div>

            {/* Is active */}
            <div className="flex items-center gap-3 sm:col-span-2">
              <button
                type="button"
                onClick={() => handleChange("is_active", !form.is_active)}
                className={`relative inline-flex h-5 w-9 items-center rounded-full transition-colors ${
                  form.is_active ? "bg-indigo-500" : "bg-gray-200"
                }`}
              >
                <span
                  className={`inline-block h-4 w-4 transform rounded-full bg-white shadow transition-transform ${
                    form.is_active ? "translate-x-4" : "translate-x-0.5"
                  }`}
                />
              </button>
              <label className="text-sm text-gray-600">
                {form.is_active ? "Đang hoạt động" : "Đã tắt"}
              </label>
            </div>
          </div>
        </div>

        {/* Prompts */}
        <div className="rounded-2xl border border-gray-100 bg-white p-6 shadow-sm space-y-4">
          <h2 className="text-sm font-semibold text-gray-700">Nội dung Prompt</h2>

          {/* System prompt */}
          <div>
            <label className="mb-1 block text-xs font-medium text-gray-600">
              System Prompt <span className="text-red-500">*</span>
            </label>
            <p className="mb-2 text-[11px] text-gray-400">
              Vai trò và phong cách viết của AI. Không thay đổi theo từng bài.
            </p>
            <textarea
              value={form.system_prompt}
              onChange={(e) => handleChange("system_prompt", e.target.value)}
              placeholder="VD: Bạn là chuyên gia phong thuỷ người Việt Nam, viết bài chuyên sâu, chính xác, dễ hiểu..."
              rows={6}
              required
              className="w-full rounded-xl border border-gray-200 px-3 py-2.5 font-mono text-xs leading-relaxed outline-none focus:border-indigo-300 focus:ring-2 focus:ring-indigo-50"
            />
          </div>

          {/* User prompt */}
          <div>
            <label className="mb-1 block text-xs font-medium text-gray-600">
              User Prompt <span className="text-red-500">*</span>
            </label>
            <p className="mb-2 text-[11px] text-gray-400">
              Template prompt cho từng bài. Dùng biến:{" "}
              {["{{topic}}", "{{length}}", "{{style}}", "{{keyword}}", "{{seo}}"].map((v) => (
                <code key={v} className="mx-0.5 rounded bg-gray-100 px-1 py-0.5 text-indigo-600">{v}</code>
              ))}
            </p>
            <textarea
              value={form.user_prompt}
              onChange={(e) => handleChange("user_prompt", e.target.value)}
              placeholder="VD: Viết bài về: {{topic}}&#10;Độ dài: {{length}}&#10;Phong cách: {{style}}"
              rows={8}
              required
              className="w-full rounded-xl border border-gray-200 px-3 py-2.5 font-mono text-xs leading-relaxed outline-none focus:border-indigo-300 focus:ring-2 focus:ring-indigo-50"
            />
          </div>
        </div>

        {/* Actions */}
        <div className="flex items-center justify-end gap-3">
          <button
            type="button"
            onClick={() => onCancel ? onCancel() : router.back()}
            className="rounded-xl border border-gray-200 px-5 py-2.5 text-sm font-medium text-gray-600 hover:bg-gray-50 transition-colors"
          >
            Hủy
          </button>
          <button
            type="submit"
            disabled={loading}
            className="flex items-center gap-2 rounded-xl bg-indigo-500 px-5 py-2.5 text-sm font-medium text-white hover:bg-indigo-600 disabled:opacity-50 transition-colors"
          >
            {loading ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <Save className="h-4 w-4" />
            )}
            {mode === "create" ? "Tạo template" : "Lưu thay đổi"}
          </button>
        </div>
      </form>
    </div>
  );
}
