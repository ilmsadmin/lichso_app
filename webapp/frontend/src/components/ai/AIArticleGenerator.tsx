"use client";

import { useState, useEffect } from "react";
import { useStreamArticleGeneration, useQuickDraft, usePromptTemplates } from "@/hooks/useAI";
import { useAllCategories } from "@/hooks/useArticles";
import { AIArticleGenerateRequest, ArticleTargetLength, ArticleWritingStyle } from "@/types/ai";
import { AIPromptTemplate } from "@/types/ai";
import { AIModelSelector } from "./AIModelSelector";
import { AIStreamingText } from "./AIStreamingText";
import { Wand2, Loader2, Zap, ExternalLink, BookTemplate, Info, X } from "lucide-react";
import { toast } from "sonner";
import Link from "next/link";

const TARGET_LENGTHS: { value: ArticleTargetLength; label: string; desc: string }[] = [
  { value: "short", label: "Ngắn", desc: "~500 từ" },
  { value: "medium", label: "Vừa", desc: "~1000 từ" },
  { value: "long", label: "Dài", desc: "~2000 từ" },
];

const WRITING_STYLES: { value: ArticleWritingStyle; label: string }[] = [
  { value: "popular", label: "Phổ thông" },
  { value: "academic", label: "Hàn lâm" },
  { value: "storytelling", label: "Kể chuyện" },
  { value: "listicle", label: "Danh sách" },
];

export function AIArticleGenerator({ onDraftCreated, initialTopic }: { onDraftCreated?: () => void; initialTopic?: string }) {
  const { data: categoriesResp } = useAllCategories();
  const categories = categoriesResp?.data ?? [];
  const { data: promptsResp } = usePromptTemplates("article");
  const prompts = promptsResp?.data ?? [];
  const [selectedPromptId, setSelectedPromptId] = useState<number | undefined>();
  const [selectedPrompt, setSelectedPrompt] = useState<AIPromptTemplate | null>(null);

  const handleSelectPrompt = (p: AIPromptTemplate | null) => {
    setSelectedPrompt(p);
    setSelectedPromptId(p?.id);
    if (p?.model) {
      setForm((prev) => ({ ...prev, model: p.model! }));
    }
  };

  const [form, setForm] = useState<AIArticleGenerateRequest>({
    topic: initialTopic ?? "",
    category_id: undefined,
    target_length: "medium",
    writing_style: "popular",
    generate_seo: true,
    lunar_context: true,
    model: "openai/gpt-4o-mini",
  });

  const [quickTopic, setQuickTopic] = useState("");

  // Sync initialTopic → form.topic whenever parent selects a new topic
  useEffect(() => {
    if (initialTopic) {
      setForm((prev) => ({ ...prev, topic: initialTopic }));
      setQuickTopic(initialTopic);
    }
  }, [initialTopic]);

  const { isStreaming, streamText, draftResult, error, startGeneration, reset } =
    useStreamArticleGeneration();
  const result = draftResult?.result;
  const quickDraft = useQuickDraft();

  const handleChange = <K extends keyof typeof form>(key: K, val: (typeof form)[K]) => {
    setForm((prev) => ({ ...prev, [key]: val }));
  };

  const handleGenerate = (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.topic.trim()) {
      toast.error("Vui lòng nhập chủ đề bài viết");
      return;
    }
    reset();
    startGeneration({ ...form, category_id: form.category_id || undefined, prompt_id: selectedPromptId });
  };

  const handleQuickDraft = () => {
    if (!quickTopic.trim()) {
      toast.error("Vui lòng nhập chủ đề");
      return;
    }
    quickDraft.mutate(
      { topic: quickTopic, category_id: form.category_id || undefined },
      {
        onSuccess: (res) => {
          const draft = res?.data;
          if (draft) {
            toast.success(`Đã tạo nháp: "${draft.title}"`);
            onDraftCreated?.();
          }
        },
      }
    );
  };

  return (
    <div className="space-y-6">
      {/* Prompt template selector */}
      {prompts.length > 0 && (
        <div className="space-y-2">
          <label className="mb-2 flex items-center gap-1.5 text-[11px] font-medium uppercase tracking-[2px] text-gray-500">
            <BookTemplate className="h-3.5 w-3.5" />
            Mẫu prompt
          </label>
          <div className="flex flex-wrap gap-2">
            {/* Default chip */}
            <button
              type="button"
              onClick={() => handleSelectPrompt(null)}
              className={`rounded-xl border px-3 py-1.5 text-xs font-medium transition-all ${
                !selectedPromptId
                  ? "border-indigo-400 bg-indigo-50 text-indigo-700 shadow-sm"
                  : "border-gray-200 text-gray-500 hover:border-gray-300 hover:text-gray-700"
              }`}
            >
              Mặc định
            </button>
            {(prompts as AIPromptTemplate[]).map((p) => (
              <button
                key={p.id}
                type="button"
                onClick={() => handleSelectPrompt(p)}
                className={`rounded-xl border px-3 py-1.5 text-xs font-medium transition-all ${
                  selectedPromptId === p.id
                    ? "border-indigo-400 bg-indigo-50 text-indigo-700 shadow-sm"
                    : "border-gray-200 text-gray-500 hover:border-gray-300 hover:text-gray-700"
                }`}
              >
                {p.name}
              </button>
            ))}
          </div>

          {/* Selected prompt info panel */}
          {selectedPrompt && (
            <div
              className="relative rounded-xl p-3 text-xs space-y-2"
              style={{
                background: "rgba(99,102,241,0.04)",
                border: "1px solid rgba(99,102,241,0.18)",
              }}
            >
              {/* Close / deselect */}
              <button
                type="button"
                onClick={() => handleSelectPrompt(null)}
                className="absolute right-2.5 top-2.5 rounded-md p-0.5 text-gray-400 hover:text-gray-600"
              >
                <X className="h-3.5 w-3.5" />
              </button>

              {/* Header */}
              <div className="flex items-center gap-1.5 font-semibold text-indigo-700 pr-5">
                <Info className="h-3.5 w-3.5 shrink-0" />
                {selectedPrompt.name}
              </div>

              {/* Meta row */}
              <div className="flex flex-wrap gap-3 text-gray-500">
                {selectedPrompt.model && (
                  <span className="flex items-center gap-1">
                    <span className="font-medium text-gray-600">Model:</span>
                    <span className="rounded bg-gray-100 px-1.5 py-0.5 font-mono text-[10px]">
                      {selectedPrompt.model}
                    </span>
                  </span>
                )}
                <span className="flex items-center gap-1">
                  <span className="font-medium text-gray-600">Max tokens:</span>
                  {selectedPrompt.max_tokens.toLocaleString()}
                </span>
                <span className="flex items-center gap-1">
                  <span className="font-medium text-gray-600">Temperature:</span>
                  {selectedPrompt.temperature}
                </span>
              </div>

              {/* System prompt preview */}
              <div>
                <p className="font-medium text-gray-600 mb-0.5">System prompt:</p>
                <p className="line-clamp-2 text-gray-500 leading-relaxed">
                  {selectedPrompt.system_prompt}
                </p>
              </div>

              {/* User prompt preview */}
              <div>
                <p className="font-medium text-gray-600 mb-0.5">User prompt template:</p>
                <p className="line-clamp-2 text-gray-500 leading-relaxed font-mono text-[10px]">
                  {selectedPrompt.user_prompt}
                </p>
              </div>

              {selectedPrompt.model && (
                <p className="text-indigo-500 flex items-center gap-1">
                  <span>✓</span> Đã áp dụng model <strong>{selectedPrompt.model}</strong> vào form
                </p>
              )}
            </div>
          )}
        </div>
      )}

      {/* Quick Draft bar */}
      <div
        className="flex items-center gap-3 rounded-2xl p-4"
        style={{
          background: "rgba(99,102,241,0.05)",
          border: "1px solid rgba(99,102,241,0.15)",
        }}
      >
        <Zap className="h-4 w-4 shrink-0 text-indigo-500" />
        <input
          value={quickTopic}
          onChange={(e) => setQuickTopic(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleQuickDraft()}
          placeholder="Quick draft: Nhập chủ đề và Enter…"
          className="flex-1 bg-transparent text-sm outline-none placeholder:text-text-soft"
        />
        <button
          onClick={handleQuickDraft}
          disabled={quickDraft.isPending}
          className="rounded-lg px-4 py-1.5 text-xs font-medium text-white disabled:opacity-50"
          style={{ background: "linear-gradient(135deg, #6366f1, #a855f7)" }}
        >
          {quickDraft.isPending ? (
            <Loader2 className="h-3.5 w-3.5 animate-spin" />
          ) : (
            "Tạo nháp"
          )}
        </button>
      </div>

      {/* Full generator form */}
      <form onSubmit={handleGenerate} className="space-y-4">
        {/* Topic */}
        <div>
          <label className="text-text-soft mb-1 block text-[11px] tracking-[2px] uppercase">
            Chủ đề bài viết *
          </label>
          <textarea
            value={form.topic}
            onChange={(e) => handleChange("topic", e.target.value)}
            placeholder="Ví dụ: Ý nghĩa của ngày Rằm tháng Giêng trong văn hóa Việt Nam"
            rows={2}
            required
            className="text-text-dark w-full rounded-xl px-3 py-2.5 text-sm outline-none resize-none"
            style={{ background: "rgba(255,252,248,0.5)", border: "1px solid var(--ls-border-warm)" }}
          />
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {/* Category */}
          <div>
            <label className="text-text-soft mb-1 block text-[11px] tracking-[2px] uppercase">
              Danh mục
            </label>
            <select
              value={form.category_id ?? ""}
              onChange={(e) => {
                const val = e.target.value;
                handleChange("category_id", val || undefined);
              }}
              className="text-text-dark w-full rounded-xl px-3 py-2.5 text-sm outline-none"
              style={{ background: "rgba(255,252,248,0.7)", border: "1px solid var(--ls-border-warm)" }}
            >
              <option value="">— Chọn danh mục —</option>
              {/* eslint-disable-next-line @typescript-eslint/no-explicit-any */}
              {(categories as any[]).map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>

          {/* Target keyword */}
          <div>
            <label className="text-text-soft mb-1 block text-[11px] tracking-[2px] uppercase">
              Từ khóa SEO chính
            </label>
            <input
              value={form.target_keyword ?? ""}
              onChange={(e) => handleChange("target_keyword", e.target.value)}
              placeholder="Ví dụ: ngày rằm tháng giêng"
              className="text-text-dark w-full rounded-xl px-3 py-2.5 text-sm outline-none"
              style={{ background: "rgba(255,252,248,0.5)", border: "1px solid var(--ls-border-warm)" }}
            />
          </div>
        </div>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          {/* Length */}
          <div>
            <label className="text-text-soft mb-2 block text-[11px] tracking-[2px] uppercase">
              Độ dài
            </label>
            <div className="flex gap-2">
              {TARGET_LENGTHS.map((l) => (
                <button
                  key={l.value}
                  type="button"
                  onClick={() => handleChange("target_length", l.value)}
                  className={`flex-1 rounded-xl px-2 py-2 text-center text-xs transition-all ${
                    form.target_length === l.value
                      ? "text-white"
                      : "text-text-mid"
                  }`}
                  style={{
                    background:
                      form.target_length === l.value
                        ? "linear-gradient(135deg, #6366f1, #a855f7)"
                        : "rgba(255,252,248,0.5)",
                    border: "1px solid var(--ls-border-warm)",
                  }}
                >
                  <span className="block font-medium">{l.label}</span>
                  <span className="block opacity-70">{l.desc}</span>
                </button>
              ))}
            </div>
          </div>

          {/* Style */}
          <div>
            <label className="text-text-soft mb-1 block text-[11px] tracking-[2px] uppercase">
              Phong cách viết
            </label>
            <select
              value={form.writing_style}
              onChange={(e) => handleChange("writing_style", e.target.value as ArticleWritingStyle)}
              className="text-text-dark w-full rounded-xl px-3 py-2.5 text-sm outline-none"
              style={{ background: "rgba(255,252,248,0.7)", border: "1px solid var(--ls-border-warm)" }}
            >
              {WRITING_STYLES.map((s) => (
                <option key={s.value} value={s.value}>
                  {s.label}
                </option>
              ))}
            </select>
          </div>

          {/* Model */}
          <AIModelSelector
            value={form.model}
            onChange={(m) => handleChange("model", m)}
          />
        </div>

        {/* Options */}
        <div className="flex flex-wrap gap-4">
          <label className="flex cursor-pointer items-center gap-2 text-sm text-text-mid">
            <input
              type="checkbox"
              checked={form.generate_seo}
              onChange={(e) => handleChange("generate_seo", e.target.checked)}
              className="accent-indigo-500"
            />
            Tạo meta SEO tự động
          </label>
          <label className="flex cursor-pointer items-center gap-2 text-sm text-text-mid">
            <input
              type="checkbox"
              checked={form.lunar_context}
              onChange={(e) => handleChange("lunar_context", e.target.checked)}
              className="accent-indigo-500"
            />
            Thêm ngữ cảnh âm lịch
          </label>
        </div>

        {error && (
          <p className="rounded-xl bg-red-50 px-3 py-2 text-sm text-red-600 border border-red-100">
            {error}
          </p>
        )}

        <button
          type="submit"
          disabled={isStreaming}
          className="flex w-full items-center justify-center gap-2 rounded-xl py-3 text-sm font-medium text-white transition-all hover:opacity-90 disabled:opacity-50"
          style={{
            background: "linear-gradient(135deg, #6366f1, #a855f7)",
            boxShadow: "0 4px 16px rgba(99,102,241,0.3)",
          }}
        >
          {isStreaming ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              Đang tạo bài viết…
            </>
          ) : (
            <>
              <Wand2 className="h-4 w-4" />
              Tạo bài viết với AI
            </>
          )}
        </button>
      </form>

      {/* Streaming output */}
      {(streamText || result) && (
        <div
          className="rounded-2xl p-5"
          style={{
            background: "rgba(99,102,241,0.03)",
            border: "1px solid rgba(99,102,241,0.15)",
          }}
        >
          {result ? (
            <div className="space-y-3">
              <div className="flex items-start justify-between gap-2">
                <div>
                  <h3 className="text-text-dark font-semibold text-base">{result.title}</h3>
                  <p className="text-text-soft text-xs mt-0.5">{result.excerpt}</p>
                </div>
                <Link
                  href={`/admin/articles/${result.article_id}/edit`}
                  className="flex shrink-0 items-center gap-1 rounded-lg px-3 py-1.5 text-xs font-medium text-white"
                  style={{ background: "linear-gradient(135deg, #6366f1, #a855f7)" }}
                >
                  <ExternalLink className="h-3 w-3" />
                  Chỉnh sửa
                </Link>
              </div>
              <div className="flex flex-wrap gap-2 text-[11px] text-text-soft">
                <span>{result.tokens_used.toLocaleString()} tokens</span>
                <span>·</span>
                <span>${result.cost_usd.toFixed(4)}</span>
                <span>·</span>
                <span>{result.reading_time} phút đọc</span>
                <span>·</span>
                <span className="capitalize">{result.ai_model}</span>
              </div>
            </div>
          ) : (
            <>
              <p className="text-text-soft mb-3 text-[11px] uppercase tracking-wider">
                Đang tạo nội dung…
              </p>
              <AIStreamingText
                text={streamText}
                isStreaming={isStreaming}
                className="text-text-mid max-h-80 overflow-y-auto"
              />
            </>
          )}
        </div>
      )}
    </div>
  );
}
