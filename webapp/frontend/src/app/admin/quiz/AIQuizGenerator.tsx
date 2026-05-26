"use client";

import { useState } from "react";
import { Sparkles, CheckCircle2, ChevronDown, ChevronUp, Save, Loader2, Lightbulb, X, RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Badge } from "@/components/ui/badge";
import { QUIZ_CATEGORIES, QUIZ_DIFFICULTIES, type GeneratedQuizQuestion, type GeneratedQuizTopic } from "@/types/quiz";
import { useGenerateQuizQuestions, useCreateQuizQuestion, useGenerateQuizTopics } from "@/hooks/useQuiz";
import { toast } from "sonner";

interface AIQuizGeneratorProps {
  open: boolean;
  onClose: () => void;
}

const CORRECT_LABELS: Record<string, string> = { a: "A", b: "B", c: "C", d: "D" };
const OPTION_KEYS = ["option_a", "option_b", "option_c", "option_d"] as const;
const OPTION_LETTERS = ["a", "b", "c", "d"] as const;

export default function AIQuizGenerator({ open, onClose }: AIQuizGeneratorProps) {
  const [inputMode, setInputMode] = useState<"topic" | "text">("topic");
  const [topic, setTopic] = useState("");
  const [text, setText] = useState("");
  const [count, setCount] = useState(5);
  const [category, setCategory] = useState("history_vn");
  const [difficulty, setDifficulty] = useState("medium");

  const [results, setResults] = useState<GeneratedQuizQuestion[]>([]);
  const [selected, setSelected] = useState<Set<number>>(new Set());
  const [expanded, setExpanded] = useState<Set<number>>(new Set([0]));
  const [saving, setSaving] = useState(false);
  const [suggestedTopics, setSuggestedTopics] = useState<GeneratedQuizTopic[]>([]);

  const generateMutation = useGenerateQuizQuestions();
  const topicMutation = useGenerateQuizTopics();
  const createQuestion = useCreateQuizQuestion();

  function toggleSelect(i: number) {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(i)) {
        next.delete(i);
      } else {
        next.add(i);
      }
      return next;
    });
  }

  function toggleExpand(i: number) {
    setExpanded((prev) => {
      const next = new Set(prev);
      if (next.has(i)) {
        next.delete(i);
      } else {
        next.add(i);
      }
      return next;
    });
  }

  function selectAll() {
    setSelected(new Set(results.map((_, i) => i)));
  }

  async function handleSuggestTopics() {
    const res = await topicMutation.mutateAsync({
      category,
      count: 8,
      existing_topics: suggestedTopics.map((t) => t.title),
    });

    if (res.success && res.data) {
      setSuggestedTopics(res.data);
      if (!topic.trim() && res.data[0]?.title) {
        setTopic(res.data[0].title);
      }
    }
  }

  function applySuggestedTopic(value: string) {
    setInputMode("topic");
    setTopic(value);
  }

  function removeSuggestedTopic(index: number) {
    setSuggestedTopics((prev) => prev.filter((_, i) => i !== index));
  }

  async function handleGenerate() {
    if (inputMode === "topic" && !topic.trim()) {
      toast.error("Vui lòng nhập chủ đề");
      return;
    }
    if (inputMode === "text" && !text.trim()) {
      toast.error("Vui lòng dán nội dung văn bản");
      return;
    }

    const res = await generateMutation.mutateAsync({
      topic: inputMode === "topic" ? topic : undefined,
      text: inputMode === "text" ? text : undefined,
      count,
      category,
      difficulty,
    });

    if (res.success && res.data) {
      setResults(res.data);
      setSelected(new Set(res.data.map((_, i) => i)));
      setExpanded(new Set([0]));
    }
  }

  async function handleSaveSelected() {
    if (selected.size === 0) {
      toast.error("Chưa chọn câu hỏi nào");
      return;
    }
    setSaving(true);
    let savedCount = 0;
    for (const i of selected) {
      const q = results[i];
      try {
        await createQuestion.mutateAsync({
          content: q.content,
          option_a: q.option_a,
          option_b: q.option_b,
          option_c: q.option_c,
          option_d: q.option_d,
          correct: q.correct,
          hint: q.hint,
          explanation: q.explanation,
          category: q.category,
          difficulty: q.difficulty,
          is_active: true,
        });
        savedCount++;
      } catch {
        // individual failures are caught; continue saving others
      }
    }
    setSaving(false);
    toast.success(`Đã lưu ${savedCount}/${selected.size} câu hỏi`);
    if (savedCount === selected.size) {
      handleClose();
    }
  }

  function handleClose() {
    setResults([]);
    setSelected(new Set());
    setExpanded(new Set([0]));
    setTopic("");
    setText("");
    setSuggestedTopics([]);
    onClose();
  }

  const isGeneratingTopics = topicMutation.isPending;

  return (
    <Dialog open={open} onOpenChange={(v) => !v && handleClose()}>
      <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Sparkles className="h-5 w-5 text-purple-500" />
            Sinh câu hỏi bằng AI
          </DialogTitle>
        </DialogHeader>

        {/* ── Input panel ── */}
        <div className="space-y-4">
          {/* Mode toggle */}
          <div className="flex gap-1 rounded-md border p-1 w-fit">
            <button
              onClick={() => setInputMode("topic")}
              className={`px-3 py-1 rounded text-sm font-medium transition-colors ${
                inputMode === "topic" ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:text-foreground"
              }`}
            >
              Nhập chủ đề
            </button>
            <button
              onClick={() => setInputMode("text")}
              className={`px-3 py-1 rounded text-sm font-medium transition-colors ${
                inputMode === "text" ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:text-foreground"
              }`}
            >
              Dán văn bản
            </button>
          </div>

          {inputMode === "topic" ? (
            <div className="space-y-1">
              <div className="flex items-center justify-between gap-3">
                <Label>Chủ đề</Label>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={handleSuggestTopics}
                  disabled={isGeneratingTopics || generateMutation.isPending}
                  className="h-8 gap-1.5 text-xs"
                >
                  {isGeneratingTopics ? (
                    <Loader2 className="h-3.5 w-3.5 animate-spin" />
                  ) : suggestedTopics.length > 0 ? (
                    <RefreshCw className="h-3.5 w-3.5" />
                  ) : (
                    <Lightbulb className="h-3.5 w-3.5" />
                  )}
                  {suggestedTopics.length > 0 ? "Gợi ý lại" : "Gợi ý chủ đề"}
                </Button>
              </div>
              <Input
                placeholder="Ví dụ: Chiến thắng Điện Biên Phủ 1954, Nhà Nguyễn, Văn hóa Chăm Pa…"
                value={topic}
                onChange={(e) => setTopic(e.target.value)}
              />
              {suggestedTopics.length > 0 && (
                <div className="rounded-lg border bg-muted/20 p-3">
                  <div className="mb-2 flex items-center justify-between">
                    <p className="text-xs font-medium text-muted-foreground">
                      Chủ đề AI gợi ý theo danh mục
                    </p>
                    <button
                      type="button"
                      onClick={() => setSuggestedTopics([])}
                      className="text-xs text-muted-foreground hover:text-foreground"
                    >
                      Xóa tất cả
                    </button>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {suggestedTopics.map((item, index) => (
                      <div
                        key={`${item.title}-${index}`}
                        className="group flex max-w-full items-center gap-1.5 rounded-full border bg-white px-2.5 py-1.5 text-xs shadow-sm"
                        title={item.description || item.title}
                      >
                        <button
                          type="button"
                          onClick={() => applySuggestedTopic(item.title)}
                          className="max-w-[260px] truncate text-left font-medium text-foreground hover:text-primary"
                        >
                          {item.title}
                        </button>
                        <button
                          type="button"
                          onClick={() => removeSuggestedTopic(index)}
                          className="rounded-full p-0.5 text-muted-foreground hover:bg-muted hover:text-destructive"
                          aria-label={`Xóa chủ đề ${item.title}`}
                        >
                          <X className="h-3 w-3" />
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          ) : (
            <div className="space-y-1">
              <Label>Nội dung văn bản nguồn</Label>
              <Textarea
                placeholder="Dán đoạn văn bản, bài viết, tài liệu… AI sẽ sinh câu hỏi dựa trên nội dung này"
                rows={6}
                value={text}
                onChange={(e) => setText(e.target.value)}
              />
            </div>
          )}

          <div className="grid grid-cols-3 gap-3">
            <div className="space-y-1">
              <Label>Số câu</Label>
              <Select value={String(count)} onValueChange={(v) => setCount(Number(v))}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {[1, 2, 3, 5, 7, 10].map((n) => (
                    <SelectItem key={n} value={String(n)}>
                      {n} câu
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-1">
              <Label>Danh mục</Label>
              <Select
                value={category}
                onValueChange={(value) => {
                  setCategory(value);
                  setSuggestedTopics([]);
                }}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {QUIZ_CATEGORIES.map((c) => (
                    <SelectItem key={c.value} value={c.value}>
                      {c.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-1">
              <Label>Độ khó</Label>
              <Select value={difficulty} onValueChange={setDifficulty}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {QUIZ_DIFFICULTIES.map((d) => (
                    <SelectItem key={d.value} value={d.value}>
                      {d.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <Button
            onClick={handleGenerate}
            disabled={generateMutation.isPending}
            className="w-full bg-purple-600 hover:bg-purple-700"
          >
            {generateMutation.isPending ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Đang sinh câu hỏi…
              </>
            ) : (
              <>
                <Sparkles className="mr-2 h-4 w-4" />
                Sinh câu hỏi
              </>
            )}
          </Button>
        </div>

        {/* ── Results panel ── */}
        {results.length > 0 && (
          <div className="space-y-3 border-t pt-4">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium">
                {results.length} câu hỏi được sinh —{" "}
                <span className="text-muted-foreground">đã chọn {selected.size}</span>
              </span>
              <button
                onClick={selectAll}
                className="text-xs text-primary hover:underline"
              >
                Chọn tất cả
              </button>
            </div>

            <div className="space-y-2">
              {results.map((q, i) => (
                <GeneratedQuestionCard
                  key={i}
                  index={i}
                  question={q}
                  isSelected={selected.has(i)}
                  isExpanded={expanded.has(i)}
                  onToggleSelect={() => toggleSelect(i)}
                  onToggleExpand={() => toggleExpand(i)}
                />
              ))}
            </div>
          </div>
        )}

        <DialogFooter className="gap-2">
          <Button variant="outline" onClick={handleClose}>
            Đóng
          </Button>
          {results.length > 0 && (
            <Button
              onClick={handleSaveSelected}
              disabled={saving || selected.size === 0}
            >
              {saving ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Đang lưu…
                </>
              ) : (
                <>
                  <Save className="mr-2 h-4 w-4" />
                  Lưu {selected.size} câu hỏi
                </>
              )}
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

// ── Card for each generated question ──

interface GeneratedQuestionCardProps {
  index: number;
  question: GeneratedQuizQuestion;
  isSelected: boolean;
  isExpanded: boolean;
  onToggleSelect: () => void;
  onToggleExpand: () => void;
}

function GeneratedQuestionCard({
  index,
  question,
  isSelected,
  isExpanded,
  onToggleSelect,
  onToggleExpand,
}: GeneratedQuestionCardProps) {
  return (
    <div
      className={`rounded-lg border transition-colors ${
        isSelected ? "border-primary bg-primary/5" : "border-border bg-card"
      }`}
    >
      {/* Header row */}
      <div className="flex items-start gap-3 p-3">
        <button
          onClick={onToggleSelect}
          className={`mt-0.5 flex-shrink-0 rounded-full p-0.5 transition-colors ${
            isSelected ? "text-primary" : "text-muted-foreground"
          }`}
        >
          <CheckCircle2 className="h-5 w-5" />
        </button>

        <div className="flex-1 min-w-0">
          <p className="text-sm font-medium leading-snug line-clamp-2">
            {index + 1}. {question.content}
          </p>
          <div className="flex items-center gap-2 mt-1">
            <Badge variant="outline" className="text-xs">
              Đáp án: {CORRECT_LABELS[question.correct] ?? question.correct.toUpperCase()}
            </Badge>
          </div>
        </div>

        <button
          onClick={onToggleExpand}
          className="flex-shrink-0 text-muted-foreground hover:text-foreground"
        >
          {isExpanded ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
        </button>
      </div>

      {/* Expanded details */}
      {isExpanded && (
        <div className="px-3 pb-3 space-y-3 border-t pt-3">
          <div className="grid grid-cols-2 gap-1.5">
            {OPTION_LETTERS.map((letter, li) => {
              const key = OPTION_KEYS[li];
              const isCorrect = question.correct === letter;
              return (
                <div
                  key={letter}
                  className={`flex items-start gap-2 rounded p-2 text-sm ${
                    isCorrect
                      ? "bg-green-50 border border-green-200 text-green-800"
                      : "bg-muted/50 text-muted-foreground"
                  }`}
                >
                  <span className="font-semibold w-4 flex-shrink-0">{letter.toUpperCase()}.</span>
                  <span>{question[key]}</span>
                </div>
              );
            })}
          </div>

          {question.hint && (
            <div className="rounded bg-yellow-50 border border-yellow-200 px-3 py-2 text-sm">
              <span className="font-medium text-yellow-700">Gợi ý:</span>{" "}
              <span className="text-yellow-800">{question.hint}</span>
            </div>
          )}

          {question.explanation && (
            <div className="rounded bg-blue-50 border border-blue-200 px-3 py-2 text-sm">
              <span className="font-medium text-blue-700">Giải thích:</span>{" "}
              <span className="text-blue-800">{question.explanation}</span>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
