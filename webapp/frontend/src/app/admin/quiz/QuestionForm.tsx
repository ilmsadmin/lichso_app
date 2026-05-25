"use client";

import { useForm } from "react-hook-form";
import { useRouter } from "next/navigation";
import { ArrowLeft } from "lucide-react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Switch } from "@/components/ui/switch";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ROUTES } from "@/lib/constants";
import {
  QUIZ_CATEGORIES,
  QUIZ_DIFFICULTIES,
  QUIZ_CORRECT_OPTIONS,
  type QuizQuestion,
  type CreateQuizQuestionRequest,
} from "@/types/quiz";
import { useCreateQuizQuestion, useUpdateQuizQuestion } from "@/hooks/useQuiz";

interface QuestionFormProps {
  defaultValues?: QuizQuestion;
}

export function QuestionForm({ defaultValues }: QuestionFormProps) {
  const router = useRouter();
  const isEditing = !!defaultValues;

  const createQuestion = useCreateQuizQuestion();
  const updateQuestion = useUpdateQuizQuestion(defaultValues?.id ?? 0);

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<CreateQuizQuestionRequest>({
    defaultValues: {
      content: defaultValues?.content ?? "",
      option_a: defaultValues?.option_a ?? "",
      option_b: defaultValues?.option_b ?? "",
      option_c: defaultValues?.option_c ?? "",
      option_d: defaultValues?.option_d ?? "",
      correct: defaultValues?.correct ?? "a",
      explanation: defaultValues?.explanation ?? "",
      category: defaultValues?.category ?? "history_vn",
      difficulty: defaultValues?.difficulty ?? "medium",
      article_id: defaultValues?.article_id ?? undefined,
      is_active: defaultValues?.is_active ?? true,
    },
  });

  const correct = watch("correct");
  const isActive = watch("is_active");
  const difficulty = watch("difficulty");
  const category = watch("category");

  const onSubmit = async (data: CreateQuizQuestionRequest) => {
    const mutation = isEditing ? updateQuestion : createQuestion;
    await mutation.mutateAsync(data, {
      onSuccess: (res) => {
        if (res.success) router.push(ROUTES.ADMIN_QUIZ);
      },
    });
  };

  const isPending = createQuestion.isPending || updateQuestion.isPending;

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" asChild>
          <Link href={ROUTES.ADMIN_QUIZ}>
            <ArrowLeft className="h-4 w-4" />
          </Link>
        </Button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">
            {isEditing ? "Chỉnh sửa câu hỏi" : "Thêm câu hỏi mới"}
          </h1>
          <p className="text-muted-foreground text-sm">
            {isEditing ? `ID: ${defaultValues.id}` : "Tạo câu hỏi quiz cho ứng dụng mobile"}
          </p>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Left: Content + Options */}
        <div className="space-y-6 lg:col-span-2">
          <Card>
            <CardHeader><CardTitle className="text-base">Nội dung câu hỏi</CardTitle></CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-1.5">
                <Label htmlFor="content">Câu hỏi *</Label>
                <Textarea
                  id="content"
                  rows={3}
                  placeholder="Nhập nội dung câu hỏi..."
                  {...register("content", { required: "Vui lòng nhập nội dung câu hỏi" })}
                />
                {errors.content && (
                  <p className="text-destructive text-xs">{errors.content.message}</p>
                )}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader><CardTitle className="text-base">Các lựa chọn</CardTitle></CardHeader>
            <CardContent className="space-y-3">
              {(["a", "b", "c", "d"] as const).map((opt) => (
                <div key={opt} className="flex items-center gap-3">
                  <button
                    type="button"
                    onClick={() => setValue("correct", opt)}
                    className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-sm font-bold transition-colors ${
                      correct === opt
                        ? "bg-green-500 text-white"
                        : "bg-muted text-muted-foreground hover:bg-green-100"
                    }`}
                  >
                    {opt.toUpperCase()}
                  </button>
                  <Input
                    placeholder={`Lựa chọn ${opt.toUpperCase()}...`}
                    {...register(`option_${opt}` as keyof CreateQuizQuestionRequest, {
                      required: "Vui lòng nhập lựa chọn",
                    })}
                  />
                </div>
              ))}
              <p className="text-muted-foreground text-xs pt-1">
                Nhấn vào chữ cái để chọn đáp án đúng (hiện tại: <strong className="text-green-600 uppercase">{correct}</strong>)
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader><CardTitle className="text-base">Giải thích</CardTitle></CardHeader>
            <CardContent>
              <Textarea
                rows={3}
                placeholder="Giải thích tại sao đáp án đúng (tuỳ chọn)..."
                {...register("explanation")}
              />
            </CardContent>
          </Card>
        </div>

        {/* Right: Settings */}
        <div className="space-y-4">
          <Card>
            <CardHeader><CardTitle className="text-base">Phân loại</CardTitle></CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-1.5">
                <Label>Danh mục</Label>
                <Select value={category} onValueChange={(v) => setValue("category", v)}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {QUIZ_CATEGORIES.map((c) => (
                      <SelectItem key={c.value} value={c.value}>{c.label}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-1.5">
                <Label>Độ khó</Label>
                <Select value={difficulty} onValueChange={(v) => setValue("difficulty", v)}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {QUIZ_DIFFICULTIES.map((d) => (
                      <SelectItem key={d.value} value={d.value}>{d.label}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="article_id">ID bài viết liên quan</Label>
                <Input
                  id="article_id"
                  type="number"
                  placeholder="Tuỳ chọn..."
                  {...register("article_id", { valueAsNumber: true })}
                />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader><CardTitle className="text-base">Hiển thị</CardTitle></CardHeader>
            <CardContent>
              <div className="flex items-center justify-between">
                <Label htmlFor="is_active" className="cursor-pointer">Kích hoạt</Label>
                <Switch
                  id="is_active"
                  checked={isActive}
                  onCheckedChange={(v) => setValue("is_active", v)}
                />
              </div>
              <p className="text-muted-foreground mt-1 text-xs">
                Câu hỏi chỉ xuất hiện khi được kích hoạt
              </p>
            </CardContent>
          </Card>

          <Button type="submit" className="w-full" disabled={isPending}>
            {isPending ? "Đang lưu..." : isEditing ? "Lưu thay đổi" : "Tạo câu hỏi"}
          </Button>
        </div>
      </div>
    </form>
  );
}
