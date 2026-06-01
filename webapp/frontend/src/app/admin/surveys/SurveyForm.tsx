"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import {
  ArrowLeft,
  Plus,
  Trash2,
  ArrowUp,
  ArrowDown,
  AlignLeft,
  ListTodo,
  CheckSquare,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { useCreateSurvey, useUpdateSurvey } from "@/hooks/useSurveys";
import { ROUTES } from "@/lib/constants";
import type { Survey, CreateSurveyRequest, SurveyQuestion } from "@/types/survey";

interface SurveyFormProps {
  survey?: Survey;
  isEdit?: boolean;
}

export default function SurveyForm({ survey, isEdit }: SurveyFormProps) {
  const router = useRouter();
  const createSurvey = useCreateSurvey();
  const updateSurvey = useUpdateSurvey(survey?.id ?? "");

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [isActive, setIsActive] = useState(true);
  const [questions, setQuestions] = useState<SurveyQuestion[]>([]);

  useEffect(() => {
    if (survey) {
      setTitle(survey.title);
      setDescription(survey.description ?? "");
      setIsActive(survey.is_active);
      setQuestions(survey.questions);
    }
  }, [survey]);

  const handleAddQuestion = () => {
    setQuestions((prev) => [
      ...prev,
      {
        title: "",
        type: "single_choice",
        options: ["Lựa chọn 1", "Lựa chọn 2"],
        required: true,
      },
    ]);
  };

  const handleRemoveQuestion = (index: number) => {
    setQuestions((prev) => prev.filter((_, i) => i !== index));
  };

  const handleQuestionChange = (index: number, updated: Partial<SurveyQuestion>) => {
    setQuestions((prev) =>
      prev.map((q, i) => (i === index ? { ...q, ...updated } : q))
    );
  };

  const handleAddOption = (questionIndex: number) => {
    setQuestions((prev) =>
      prev.map((q, i) => {
        if (i === questionIndex) {
          const currentOptions = q.options ?? [];
          return {
            ...q,
            options: [...currentOptions, `Lựa chọn ${currentOptions.length + 1}`],
          };
        }
        return q;
      })
    );
  };

  const handleRemoveOption = (questionIndex: number, optionIndex: number) => {
    setQuestions((prev) =>
      prev.map((q, i) => {
        if (i === questionIndex) {
          const currentOptions = q.options ?? [];
          return {
            ...q,
            options: currentOptions.filter((_, oi) => oi !== optionIndex),
          };
        }
        return q;
      })
    );
  };

  const handleOptionChange = (
    questionIndex: number,
    optionIndex: number,
    value: string
  ) => {
    setQuestions((prev) =>
      prev.map((q, i) => {
        if (i === questionIndex) {
          const currentOptions = [...(q.options ?? [])];
          currentOptions[optionIndex] = value;
          return { ...q, options: currentOptions };
        }
        return q;
      })
    );
  };

  const moveQuestion = (index: number, direction: "up" | "down") => {
    if (direction === "up" && index === 0) return;
    if (direction === "down" && index === questions.length - 1) return;

    const targetIndex = direction === "up" ? index - 1 : index + 1;
    const newQuestions = [...questions];
    const temp = newQuestions[index];
    newQuestions[index] = newQuestions[targetIndex];
    newQuestions[targetIndex] = temp;
    setQuestions(newQuestions);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!title.trim()) return;

    // Filter choices options in case text is chosen
    const formattedQuestions = questions.map((q) => {
      if (q.type === "text") {
        return { title: q.title, type: q.type, required: q.required };
      }
      return q;
    });

    const payload: CreateSurveyRequest = {
      title: title.trim(),
      description: description.trim() || undefined,
      questions: formattedQuestions,
      is_active: isActive,
    };

    try {
      if (isEdit) {
        const res = await updateSurvey.mutateAsync(payload);
        if (res.success) router.push(ROUTES.ADMIN_SURVEYS);
      } else {
        const res = await createSurvey.mutateAsync(payload);
        if (res.success) router.push(ROUTES.ADMIN_SURVEYS);
      }
    } catch {
      // Handled by hook
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Link href={ROUTES.ADMIN_SURVEYS}>
          <Button variant="ghost" size="sm">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Quay lại
          </Button>
        </Link>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">
            {isEdit ? "Sửa khảo sát" : "Tạo khảo sát mới"}
          </h1>
          <p className="text-muted-foreground">
            {isEdit
              ? "Chỉnh sửa câu hỏi và cấu hình khảo sát."
              : "Thiết lập khảo sát thu thập ý kiến người dùng trên ứng dụng di động Lịch Số."}
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Basic Meta Card */}
        <Card>
          <CardHeader>
            <CardTitle>Thông tin khảo sát</CardTitle>
            <CardDescription>
              Thiết lập thông tin chung để hiển thị đầu màn hình khảo sát.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="title">Tiêu đề khảo sát *</Label>
              <Input
                id="title"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="VD: Khảo sát trải nghiệm người dùng Quý 2/2026"
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="description">Mô tả khảo sát (tùy chọn)</Label>
              <Textarea
                id="description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="VD: Ý kiến của bạn sẽ giúp chúng tôi hoàn thiện ứng dụng hơn. Xin cảm ơn!"
                rows={3}
              />
            </div>
            <div className="flex items-center gap-3 pt-2">
              <Switch
                id="is_active"
                checked={isActive}
                onCheckedChange={setIsActive}
              />
              <Label htmlFor="is_active">Kích hoạt cuộc khảo sát này</Label>
            </div>
          </CardContent>
        </Card>

        {/* Dynamic Questions Builder */}
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold tracking-tight">Danh sách câu hỏi ({questions.length})</h2>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handleAddQuestion}
              className="gap-1.5"
            >
              <Plus className="h-4 w-4" />
              Thêm câu hỏi
            </Button>
          </div>

          {questions.length === 0 ? (
            <div className="flex flex-col items-center justify-center rounded-lg border border-dashed py-12 bg-muted/20">
              <p className="text-muted-foreground text-sm mb-4">
                Chưa có câu hỏi nào. Nhấn nút bên dưới để bắt đầu thêm câu hỏi.
              </p>
              <Button type="button" onClick={handleAddQuestion} size="sm">
                <Plus className="mr-2 h-4 w-4" /> Thêm câu hỏi đầu tiên
              </Button>
            </div>
          ) : (
            <div className="space-y-4">
              {questions.map((q, qIndex) => (
                <Card key={qIndex} className="relative overflow-hidden group">
                  <div className="absolute left-0 top-0 bottom-0 w-1 bg-primary" />
                  <CardHeader className="pb-3 flex flex-row items-start justify-between gap-4 space-y-0">
                    <div className="flex items-center gap-2">
                      <span className="bg-primary/10 text-primary text-xs font-semibold px-2 py-0.5 rounded-full">
                        Câu {qIndex + 1}
                      </span>
                    </div>
                    {/* Controls */}
                    <div className="flex items-center gap-1">
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        onClick={() => moveQuestion(qIndex, "up")}
                        disabled={qIndex === 0}
                        className="h-8 w-8 text-muted-foreground"
                      >
                        <ArrowUp className="h-4 w-4" />
                      </Button>
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        onClick={() => moveQuestion(qIndex, "down")}
                        disabled={qIndex === questions.length - 1}
                        className="h-8 w-8 text-muted-foreground"
                      >
                        <ArrowDown className="h-4 w-4" />
                      </Button>
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        onClick={() => handleRemoveQuestion(qIndex)}
                        className="h-8 w-8 text-destructive"
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="grid gap-4 md:grid-cols-3">
                      {/* Question Title */}
                      <div className="space-y-2 md:col-span-2">
                        <Label>Tiêu đề câu hỏi *</Label>
                        <Input
                          value={q.title}
                          onChange={(e) =>
                            handleQuestionChange(qIndex, { title: e.target.value })
                          }
                          placeholder="VD: Bạn đánh giá thế nào về tốc độ phản hồi của trợ lý AI?"
                          required
                        />
                      </div>
                      {/* Question Type */}
                      <div className="space-y-2">
                        <Label>Loại câu hỏi</Label>
                        <Select
                          value={q.type}
                          onValueChange={(val) =>
                            handleQuestionChange(qIndex, {
                              type: val as any,
                              options:
                                val === "text"
                                  ? undefined
                                  : q.options || ["Lựa chọn 1", "Lựa chọn 2"],
                            })
                          }
                        >
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="single_choice">
                              <span className="flex items-center gap-2">
                                <ListTodo className="h-4 w-4 text-sky-500" />
                                Lựa chọn duy nhất
                              </span>
                            </SelectItem>
                            <SelectItem value="multiple_choice">
                              <span className="flex items-center gap-2">
                                <CheckSquare className="h-4 w-4 text-emerald-500" />
                                Lựa chọn nhiều
                              </span>
                            </SelectItem>
                            <SelectItem value="text">
                              <span className="flex items-center gap-2">
                                <AlignLeft className="h-4 w-4 text-amber-500" />
                                Tự luận ngắn (Văn bản)
                              </span>
                            </SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                    </div>

                    {/* Options Editor (Only if choices question) */}
                    {q.type !== "text" && (
                      <div className="space-y-3 pl-4 border-l-2 border-muted bg-muted/10 p-3 rounded-lg">
                        <div className="flex items-center justify-between">
                          <Label className="text-xs font-semibold">Tùy chọn trả lời</Label>
                          <Button
                            type="button"
                            variant="ghost"
                            size="sm"
                            onClick={() => handleAddOption(qIndex)}
                            className="h-7 text-xs text-primary font-medium"
                          >
                            + Thêm tùy chọn
                          </Button>
                        </div>
                        <div className="space-y-2">
                          {(q.options ?? []).map((opt, oIndex) => (
                            <div key={oIndex} className="flex items-center gap-2">
                              <span className="text-xs text-muted-foreground w-4">{oIndex + 1}.</span>
                              <Input
                                value={opt}
                                onChange={(e) =>
                                  handleOptionChange(qIndex, oIndex, e.target.value)
                                }
                                placeholder={`Tùy chọn ${oIndex + 1}`}
                                className="h-8 text-sm"
                                required
                              />
                              <Button
                                type="button"
                                variant="ghost"
                                size="icon"
                                onClick={() => handleRemoveOption(qIndex, oIndex)}
                                disabled={(q.options ?? []).length <= 1}
                                className="h-8 w-8 text-muted-foreground hover:text-destructive"
                              >
                                <Trash2 className="h-4.5 w-4.5" />
                              </Button>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}

                    {/* Required switch */}
                    <div className="flex items-center gap-2 pt-2 border-t text-xs text-muted-foreground">
                      <Switch
                        id={`req-${qIndex}`}
                        checked={q.required}
                        onCheckedChange={(val) =>
                          handleQuestionChange(qIndex, { required: val })
                        }
                      />
                      <Label htmlFor={`req-${qIndex}`} className="cursor-pointer text-xs">
                        Bắt buộc người dùng trả lời câu hỏi này
                      </Label>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>

        {/* Submit Section */}
        <div className="flex items-center gap-3 pt-6 border-t">
          <Button
            type="submit"
            disabled={
              createSurvey.isPending ||
              updateSurvey.isPending ||
              !title.trim() ||
              questions.length === 0
            }
          >
            {createSurvey.isPending || updateSurvey.isPending
              ? "Đang lưu..."
              : isEdit
                ? "Lưu cập nhật"
                : "Tạo khảo sát"}
          </Button>
          <Link href={ROUTES.ADMIN_SURVEYS}>
            <Button variant="outline" type="button">
              Hủy
            </Button>
          </Link>
        </div>
      </form>
    </div>
  );
}
