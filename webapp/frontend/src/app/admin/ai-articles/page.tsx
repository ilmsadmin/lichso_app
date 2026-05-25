"use client";

import { useState, useCallback } from "react";
import { AIArticleGenerator } from "@/components/ai/AIArticleGenerator";
import { AIArticlePendingQueue } from "@/components/ai/AIArticlePendingQueue";
import { AIArticleDraftList } from "@/components/ai/AIArticleDraftList";
import { AILowQualityArticles } from "@/components/ai/AILowQualityArticles";
import { useAllCategories } from "@/hooks/useArticles";
import * as aiService from "@/services/aiService";
import { Wand2, Lightbulb, RefreshCw, Clock, FileText, ChevronDown, AlertTriangle } from "lucide-react";
import { Skeleton } from "@/components/ui/skeleton";

type TabId = "generate" | "pending" | "drafts" | "low-quality";

const TABS: { id: TabId; label: string; icon: React.ReactNode; desc: string }[] = [
  {
    id: "generate",
    label: "Tạo bài",
    icon: <Wand2 className="h-4 w-4" />,
    desc: "Tạo bài viết mới với AI",
  },
  {
    id: "pending",
    label: "Chờ xử lý",
    icon: <Clock className="h-4 w-4" />,
    desc: "Bài AI tạo sơ bộ, chờ viết chi tiết",
  },
  {
    id: "drafts",
    label: "Nháp",
    icon: <FileText className="h-4 w-4" />,
    desc: "Bài đã viết chi tiết, chờ xuất bản",
  },
  {
    id: "low-quality",
    label: "Chưa chất lượng",
    icon: <AlertTriangle className="h-4 w-4" />,
    desc: "Bài viết dưới 500 từ, cần AI viết lại",
  },
];

const TOPIC_MODELS = [
  { value: "openai/gpt-4o-mini", label: "GPT-4o Mini (nhanh)" },
  { value: "openai/gpt-4o", label: "GPT-4o (tốt hơn)" },
  { value: "deepseek/deepseek-chat", label: "DeepSeek (tiết kiệm)" },
  { value: "google/gemini-2.0-flash-001", label: "Gemini 2.0 Flash" },
];

export default function AIArticlesPage() {
  const [activeTab, setActiveTab] = useState<TabId>("generate");
  const [selectedTopic, setSelectedTopic] = useState<string>("");
  const [topics, setTopics] = useState<string[]>([]);
  const [isFetching, setIsFetching] = useState(false);

  // Filters cho gợi ý chủ đề
  const [topicCategoryId, setTopicCategoryId] = useState<string>("");
  const [topicModel, setTopicModel] = useState("openai/gpt-4o-mini");

  const { data: categoriesResp } = useAllCategories();
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const categories = (categoriesResp?.data ?? []) as any[];

  const fetchTopics = useCallback(async () => {
    setIsFetching(true);
    setTopics([]);
    try {
      const resp = await aiService.suggestArticleTopics(
        topicCategoryId || undefined,
        topicModel
      );
      const list = resp?.data?.topics ?? [];
      setTopics(list);
    } catch (e) {
      console.error("Lỗi gợi ý chủ đề:", e);
    } finally {
      setIsFetching(false);
    }
  }, [topicCategoryId, topicModel]);

  return (
    <div className="space-y-6 p-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="flex items-center gap-2 text-2xl font-bold text-gray-900">
            <Wand2 className="h-6 w-6 text-indigo-500" />
            AI Tạo Bài Viết
          </h1>
          <p className="mt-1 text-sm text-gray-500">
            Tạo, tinh chỉnh và xuất bản bài viết với AI.
          </p>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-1">
          {TABS.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`group flex items-center gap-2 border-b-2 px-5 py-3 text-sm font-medium transition-colors ${
                activeTab === tab.id
                  ? "border-indigo-500 text-indigo-600"
                  : "border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700"
              }`}
            >
              <span
                className={`transition-colors ${
                  activeTab === tab.id ? "text-indigo-500" : "text-gray-400 group-hover:text-gray-500"
                }`}
              >
                {tab.icon}
              </span>
              {tab.label}
            </button>
          ))}
        </nav>
      </div>

      {/* Tab Content */}
      {activeTab === "generate" && (
        <div className="space-y-4">
          {/* Topic suggestions panel */}
          <div className="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
            <div className="flex items-center justify-between mb-3">
              <h3 className="flex items-center gap-1.5 text-sm font-semibold text-gray-700">
                <Lightbulb className="h-4 w-4 text-amber-500" />
                Gợi ý chủ đề
              </h3>
              <button
                onClick={fetchTopics}
                disabled={isFetching}
                className="flex items-center gap-1.5 rounded-xl border border-gray-200 px-3 py-1.5 text-xs font-medium text-gray-600 hover:bg-gray-50 disabled:opacity-50 transition-colors"
              >
                <RefreshCw className={`h-3 w-3 ${isFetching ? "animate-spin" : ""}`} />
                {isFetching ? "Đang tạo…" : "Gợi ý"}
              </button>
            </div>

            {/* Filters row */}
            <div className="mb-3 flex flex-wrap gap-2">
              {/* Category filter */}
              <div className="relative">
                <select
                  value={topicCategoryId}
                  onChange={(e) => {
                    setTopicCategoryId(e.target.value);
                    setTopics([]);
                  }}
                  className="appearance-none rounded-xl border border-gray-200 bg-gray-50 pl-3 pr-7 py-1.5 text-xs text-gray-700 outline-none focus:border-indigo-300 focus:bg-white transition-colors"
                >
                  <option value="">Tất cả danh mục</option>
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
                <ChevronDown className="pointer-events-none absolute right-2 top-1/2 -translate-y-1/2 h-3 w-3 text-gray-400" />
              </div>

              {/* AI model filter */}
              <div className="relative">
                <select
                  value={topicModel}
                  onChange={(e) => {
                    setTopicModel(e.target.value);
                    setTopics([]);
                  }}
                  className="appearance-none rounded-xl border border-gray-200 bg-gray-50 pl-3 pr-7 py-1.5 text-xs text-gray-700 outline-none focus:border-indigo-300 focus:bg-white transition-colors"
                >
                  {TOPIC_MODELS.map((m) => (
                    <option key={m.value} value={m.value}>
                      {m.label}
                    </option>
                  ))}
                </select>
                <ChevronDown className="pointer-events-none absolute right-2 top-1/2 -translate-y-1/2 h-3 w-3 text-gray-400" />
              </div>
            </div>

            {isFetching ? (
              <div className="flex flex-wrap gap-2">
                {[...Array(8)].map((_, i) => (
                  <Skeleton key={i} className="h-7 w-40 rounded-full" />
                ))}
              </div>
            ) : topics.length > 0 ? (
              <div className="flex flex-wrap gap-2">
                {topics.map((t, i) => (
                  <button
                    key={i}
                    onClick={() => setSelectedTopic(t)}
                    className={`rounded-full border px-3 py-1.5 text-xs font-medium transition-all ${
                      selectedTopic === t
                        ? "border-indigo-400 bg-indigo-50 text-indigo-700 shadow-sm"
                        : "border-gray-200 text-gray-600 hover:border-indigo-300 hover:bg-indigo-50 hover:text-indigo-600"
                    }`}
                  >
                    {t}
                  </button>
                ))}
              </div>
            ) : (
              <p className="text-xs text-gray-400">
                Chọn danh mục và model rồi nhấn &ldquo;Gợi ý&rdquo; để AI đề xuất 10 chủ đề
              </p>
            )}
          </div>

          {/* Generator */}
          <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
            <AIArticleGenerator
              onDraftCreated={() => setActiveTab("pending")}
              initialTopic={selectedTopic}
            />
          </div>
        </div>
      )}

      {activeTab === "pending" && (
        <AIArticlePendingQueue onRefineSuccess={() => setActiveTab("drafts")} />
      )}

      {activeTab === "drafts" && <AIArticleDraftList />}

      {activeTab === "low-quality" && <AILowQualityArticles />}
    </div>
  );
}
