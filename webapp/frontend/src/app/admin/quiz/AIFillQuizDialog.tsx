"use client";

import { useState, useEffect } from "react";
import { Sparkles, CheckCircle2, ChevronDown, ChevronUp, Wand2, Loader2, BookOpen, FileText } from "lucide-react";
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
import { QUIZ_CATEGORIES, QUIZ_DIFFICULTIES, type GeneratedQuizQuestion } from "@/types/quiz";
import { useGenerateQuizQuestions } from "@/hooks/useQuiz";
import { getArticle } from "@/services/articleService";
import { toast } from "sonner";

interface AIFillQuizDialogProps {
  open: boolean;
  onClose: () => void;
  onApply: (question: GeneratedQuizQuestion) => void;
  defaultCategory?: string;
  defaultDifficulty?: string;
  currentArticleId?: number | null;
}

const CORRECT_LABELS: Record<string, string> = { a: "A", b: "B", c: "C", d: "D" };
const OPTION_KEYS = ["option_a", "option_b", "option_c", "option_d"] as const;
const OPTION_LETTERS = ["a", "b", "c", "d"] as const;

export default function AIFillQuizDialog({
  open,
  onClose,
  onApply,
  defaultCategory = "history_vn",
  defaultDifficulty = "medium",
  currentArticleId,
}: AIFillQuizDialogProps) {
  const [inputMode, setInputMode] = useState<"topic" | "text" | "article">("topic");
  const [topic, setTopic] = useState("");
  const [text, setText] = useState("");
  const [articleId, setArticleId] = useState<string>("");
  const [category, setCategory] = useState(defaultCategory);
  const [difficulty, setDifficulty] = useState(defaultDifficulty);

  const [loadingArticle, setLoadingArticle] = useState(false);
  const [generatedQuestion, setGeneratedQuestion] = useState<GeneratedQuizQuestion | null>(null);
  const [previewExpanded, setPreviewExpanded] = useState(true);

  const generateMutation = useGenerateQuizQuestions();

  // Keep state sync when dialog opens
  useEffect(() => {
    if (open) {
      setCategory(defaultCategory);
      setDifficulty(defaultDifficulty);
      if (currentArticleId) {
        setArticleId(String(currentArticleId));
        setInputMode("article");
      } else {
        setArticleId("");
        setInputMode("topic");
      }
      setGeneratedQuestion(null);
      setTopic("");
      setText("");
    }
  }, [open, defaultCategory, defaultDifficulty, currentArticleId]);

  async function handleGenerate() {
    let sourceText = "";

    if (inputMode === "topic") {
      if (!topic.trim()) {
        toast.error("Vui lòng nhập chủ đề");
        return;
      }
    } else if (inputMode === "text") {
      if (!text.trim()) {
        toast.error("Vui lòng dán nội dung văn bản nguồn");
        return;
      }
      sourceText = text.trim();
    } else if (inputMode === "article") {
      if (!articleId.trim()) {
        toast.error("Vui lòng nhập ID bài viết liên quan");
        return;
      }
      setLoadingArticle(true);
      try {
        const res = await getArticle(articleId.trim());
        if (res.success && res.data) {
          const articleContent = res.data.content || "";
          // Strip basic HTML tags if any to clean it up for the LLM
          const cleanText = articleContent.replace(/<[^>]*>/g, "");
          // Limit to first 4000 chars to avoid model context overflow
          sourceText = cleanText.substring(0, 4000);
          if (!sourceText.trim()) {
            toast.error("Bài viết liên quan không có nội dung văn bản");
            setLoadingArticle(false);
            return;
          }
        } else {
          toast.error("Không tìm thấy bài viết hoặc không thể tải nội dung");
          setLoadingArticle(false);
          return;
        }
      } catch (err) {
        toast.error("Lỗi khi tải bài viết: " + (err instanceof Error ? err.message : ""));
        setLoadingArticle(false);
        return;
      }
      setLoadingArticle(false);
    }

    try {
      const res = await generateMutation.mutateAsync({
        topic: inputMode === "topic" ? topic.trim() : undefined,
        text: sourceText ? sourceText : undefined,
        count: 1, // Generate exactly 1 question to fill the form
        category,
        difficulty,
      });

      if (res.success && res.data && res.data.length > 0) {
        setGeneratedQuestion(res.data[0]);
        setPreviewExpanded(true);
      } else {
        toast.error("Không nhận được câu hỏi hợp lệ từ AI");
      }
    } catch {
      // Errors handled by useGenerateQuizQuestions hook
    }
  }

  function handleApply() {
    if (!generatedQuestion) return;
    onApply({
      ...generatedQuestion,
      // Pass back article_id if generating from an article
      article_id: inputMode === "article" ? Number(articleId) : undefined,
    } as unknown as GeneratedQuizQuestion);
    handleClose();
  }

  function handleClose() {
    setGeneratedQuestion(null);
    onClose();
  }

  const isPending = generateMutation.isPending || loadingArticle;

  return (
    <Dialog open={open} onOpenChange={(v) => !v && handleClose()}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-xl font-bold">
            <Sparkles className="h-5 w-5 text-purple-600 animate-pulse" />
            Sinh câu hỏi với AI
          </DialogTitle>
        </DialogHeader>

        {/* ── Input Form ── */}
        <div className="space-y-4 py-2">
          {/* Mode Switcher */}
          <div className="flex gap-1 rounded-lg border p-1 w-fit bg-muted/30">
            <button
              type="button"
              onClick={() => setInputMode("topic")}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium transition-all ${
                inputMode === "topic"
                  ? "bg-white text-purple-700 shadow-sm border border-purple-100"
                  : "text-muted-foreground hover:text-foreground"
              }`}
            >
              <Wand2 className="h-3.5 w-3.5" />
              Theo chủ đề
            </button>
            <button
              type="button"
              onClick={() => setInputMode("text")}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium transition-all ${
                inputMode === "text"
                  ? "bg-white text-purple-700 shadow-sm border border-purple-100"
                  : "text-muted-foreground hover:text-foreground"
              }`}
            >
              <FileText className="h-3.5 w-3.5" />
              Dán văn bản
            </button>
            <button
              type="button"
              onClick={() => setInputMode("article")}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium transition-all ${
                inputMode === "article"
                  ? "bg-white text-purple-700 shadow-sm border border-purple-100"
                  : "text-muted-foreground hover:text-foreground"
              }`}
            >
              <BookOpen className="h-3.5 w-3.5" />
              Bài viết liên quan
            </button>
          </div>

          {/* Mode-specific input */}
          {inputMode === "topic" && (
            <div className="space-y-1.5">
              <Label className="text-sm font-semibold">Chủ đề câu hỏi</Label>
              <Input
                placeholder="Ví dụ: Chiến dịch Điện Biên Phủ 1954, Triều đại nhà Lý, Sự tích Hồ Gươm..."
                value={topic}
                onChange={(e) => setTopic(e.target.value)}
                disabled={isPending}
              />
            </div>
          )}

          {inputMode === "text" && (
            <div className="space-y-1.5">
              <Label className="text-sm font-semibold">Văn bản nguồn</Label>
              <Textarea
                placeholder="Dán đoạn văn bản, tư liệu lịch sử... AI sẽ dựa vào đây để thiết kế câu hỏi trắc nghiệm."
                rows={5}
                value={text}
                onChange={(e) => setText(e.target.value)}
                disabled={isPending}
              />
            </div>
          )}

          {inputMode === "article" && (
            <div className="space-y-1.5">
              <Label className="text-sm font-semibold">ID Bài viết liên quan</Label>
              <div className="flex gap-2">
                <Input
                  type="number"
                  placeholder="Nhập ID bài viết (ví dụ: 12)"
                  value={articleId}
                  onChange={(e) => setArticleId(e.target.value)}
                  disabled={isPending}
                  className="max-w-[200px]"
                />
                <span className="text-xs text-muted-foreground flex items-center">
                  Hệ thống sẽ tự động tải bài viết này để tạo nội dung câu hỏi phù hợp.
                </span>
              </div>
            </div>
          )}

          {/* Category & Difficulty */}
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <Label className="text-sm font-semibold">Danh mục</Label>
              <Select value={category} onValueChange={setCategory} disabled={isPending}>
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

            <div className="space-y-1.5">
              <Label className="text-sm font-semibold">Độ khó</Label>
              <Select value={difficulty} onValueChange={setDifficulty} disabled={isPending}>
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
            type="button"
            onClick={handleGenerate}
            disabled={isPending}
            className="w-full bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 text-white font-medium shadow-md transition-all duration-200 gap-2"
          >
            {isPending ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                Đang xử lý và sinh câu hỏi...
              </>
            ) : (
              <>
                <Sparkles className="h-4 w-4" />
                Sinh câu hỏi ngay
              </>
            )}
          </Button>
        </div>

        {/* ── Preview Panel ── */}
        {generatedQuestion && (
          <div className="border-t pt-4 mt-2 space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-bold text-purple-700 flex items-center gap-1">
                <Sparkles className="h-4 w-4" />
                Bản xem trước câu hỏi do AI tạo
              </h3>
              <button
                type="button"
                onClick={() => setPreviewExpanded(!previewExpanded)}
                className="text-xs text-muted-foreground hover:text-foreground flex items-center gap-0.5"
              >
                {previewExpanded ? "Thu gọn" : "Mở rộng"}
                {previewExpanded ? <ChevronUp className="h-3 w-3" /> : <ChevronDown className="h-3 w-3" />}
              </button>
            </div>

            {previewExpanded && (
              <div className="bg-purple-50/50 border border-purple-100 rounded-xl p-4 space-y-3">
                <div>
                  <p className="text-sm font-semibold text-foreground">
                    {generatedQuestion.content}
                  </p>
                </div>

                <div className="grid grid-cols-2 gap-2">
                  {OPTION_LETTERS.map((letter, li) => {
                    const key = OPTION_KEYS[li];
                    const isCorrect = generatedQuestion.correct === letter;
                    return (
                      <div
                        key={letter}
                        className={`flex items-start gap-2 rounded-lg p-2.5 text-xs transition-all ${
                          isCorrect
                            ? "bg-green-100/70 border border-green-200 text-green-800 font-medium"
                            : "bg-white border border-gray-150 text-muted-foreground"
                        }`}
                      >
                        <span className={`font-bold w-4 flex-shrink-0 text-center ${isCorrect ? "text-green-700" : "text-gray-400"}`}>
                          {letter.toUpperCase()}.
                        </span>
                        <span>{generatedQuestion[key]}</span>
                      </div>
                    );
                  })}
                </div>

                {generatedQuestion.hint && (
                  <div className="rounded-lg bg-amber-50 border border-amber-100 px-3 py-2 text-xs">
                    <span className="font-bold text-amber-700">Gợi ý:</span>{" "}
                    <span className="text-amber-800">{generatedQuestion.hint}</span>
                  </div>
                )}

                {generatedQuestion.explanation && (
                  <div className="rounded-lg bg-blue-50 border border-blue-100 px-3 py-2 text-xs">
                    <span className="font-bold text-blue-700">Giải thích:</span>{" "}
                    <span className="text-blue-800">{generatedQuestion.explanation}</span>
                  </div>
                )}
              </div>
            )}
          </div>
        )}

        <DialogFooter className="border-t pt-4 gap-2 sm:gap-0">
          <Button type="button" variant="outline" onClick={handleClose} disabled={isPending}>
            Hủy bỏ
          </Button>
          {generatedQuestion && (
            <Button
              type="button"
              onClick={handleApply}
              disabled={isPending}
              className="bg-green-600 hover:bg-green-700 text-white font-medium"
            >
              Áp dụng vào Form
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
