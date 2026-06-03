"use client";

import { useState } from "react";
import { Plus, X, Shuffle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import { useQuizDailySets, useSetDailySet, useQuizQuestions, useRandomizeDailySet } from "@/hooks/useQuiz";
import type { QuizDailySet } from "@/types/quiz";
import { PermissionGate } from "@/components/auth/PermissionGate";
import { toast } from "sonner";

export function DailySetManager() {
  const { data: setsData, isLoading } = useQuizDailySets();
  const setDailySet = useSetDailySet();
  const randomizeDailySet = useRandomizeDailySet();
  const { data: questionsData } = useQuizQuestions({ limit: 200 });

  const sets = (setsData?.data ?? []).sort(
    (a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()
  );
  const allQuestions = questionsData?.data ?? [];

  const todayStr = new Date().toISOString().slice(0, 10);
  const [selectedDate, setSelectedDate] = useState(todayStr);
  const [questionIds, setQuestionIds] = useState<number[]>([]);
  const [inputId, setInputId] = useState("");
  const [fromId, setFromId] = useState("");
  const [toId, setToId] = useState("");

  const handleAddId = () => {
    const id = parseInt(inputId.trim(), 10);
    if (!isNaN(id) && !questionIds.includes(id)) {
      setQuestionIds((prev) => [...prev, id]);
    }
    setInputId("");
  };

  const handleRemoveId = (id: number) => {
    setQuestionIds((prev) => prev.filter((x) => x !== id));
  };

  const handleToggleFromList = (id: number) => {
    setQuestionIds((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
    );
  };

  const handleSave = () => {
    if (!selectedDate || questionIds.length === 0) return;
    setDailySet.mutate({ date: selectedDate, question_ids: questionIds });
  };

  const handleRandomize = () => {
    if (!selectedDate) return;
    
    let fVal: number | undefined;
    let tVal: number | undefined;

    if (fromId.trim()) {
      fVal = parseInt(fromId, 10);
      if (isNaN(fVal)) {
        toast.error("ID bắt đầu không hợp lệ");
        return;
      }
    }

    if (toId.trim()) {
      tVal = parseInt(toId, 10);
      if (isNaN(tVal)) {
        toast.error("ID kết thúc không hợp lệ");
        return;
      }
    }

    if (fVal !== undefined && tVal !== undefined && fVal > tVal) {
      toast.error("ID bắt đầu không được lớn hơn ID kết thúc");
      return;
    }

    randomizeDailySet.mutate(
      { 
        date: selectedDate, 
        count: 20,
        from_id: fVal,
        to_id: tVal
      },
      {
        onSuccess: (res) => {
          if (res.success && res.data?.question_ids) {
            setQuestionIds(res.data.question_ids);
          }
        },
      }
    );
  };

  const handleLoadSet = (set: QuizDailySet) => {
    setSelectedDate(set.date.slice(0, 10));
    setQuestionIds(set.question_ids);
  };

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      {/* Editor */}
      <PermissionGate permission="content.create">
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Thiết lập bộ câu hỏi</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {/* Date */}
            <div className="space-y-1.5">
              <Label htmlFor="set-date">Ngày</Label>
              <Input
                id="set-date"
                type="date"
                value={selectedDate}
                onChange={(e) => setSelectedDate(e.target.value)}
              />
            </div>

            {/* Randomize Limits */}
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <Label htmlFor="from-id">Từ ID câu hỏi</Label>
                <Input
                  id="from-id"
                  type="number"
                  placeholder="Ví dụ: 1"
                  value={fromId}
                  onChange={(e) => setFromId(e.target.value)}
                  min={0}
                />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="to-id">Đến ID câu hỏi</Label>
                <Input
                  id="to-id"
                  type="number"
                  placeholder="Ví dụ: 100"
                  value={toId}
                  onChange={(e) => setToId(e.target.value)}
                  min={0}
                />
              </div>
            </div>

            {/* Randomize */}
            <Button
              variant="outline"
              className="w-full gap-2"
              onClick={handleRandomize}
              disabled={!selectedDate || randomizeDailySet.isPending}
            >
              <Shuffle className="h-4 w-4" />
              {randomizeDailySet.isPending ? "Đang chọn ngẫu nhiên..." : "Chọn ngẫu nhiên 20 câu hỏi"}
            </Button>

            {/* Add question by ID */}
            <div className="space-y-1.5">
              <Label>Thêm câu hỏi theo ID</Label>
              <div className="flex gap-2">
                <Input
                  placeholder="Nhập ID câu hỏi..."
                  value={inputId}
                  onChange={(e) => setInputId(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && handleAddId()}
                  className="flex-1"
                />
                <Button variant="outline" size="icon" onClick={handleAddId}>
                  <Plus className="h-4 w-4" />
                </Button>
              </div>
              {questionIds.length > 0 && (
                <div className="flex flex-wrap gap-1.5 pt-1">
                  {questionIds.map((id) => {
                    const q = allQuestions.find((x) => x.id === id);
                    return (
                      <Badge key={id} variant="secondary" className="gap-1 max-w-[200px]">
                        <span className="font-mono text-xs shrink-0">#{id}</span>
                        {q && (
                          <span className="truncate text-xs opacity-70">
                            {q.content.slice(0, 25)}
                          </span>
                        )}
                        <button
                          onClick={() => handleRemoveId(id)}
                          className="ml-1 opacity-60 hover:opacity-100 shrink-0"
                        >
                          <X className="h-3 w-3" />
                        </button>
                      </Badge>
                    );
                  })}
                </div>
              )}
            </div>

            {/* Pick from list */}
            {allQuestions.length > 0 && (
              <div className="space-y-1.5">
                <Label>Chọn từ danh sách câu hỏi</Label>
                <div className="max-h-52 overflow-y-auto rounded border">
                  {allQuestions.slice(0, 50).map((q) => {
                    const selected = questionIds.includes(q.id);
                    return (
                      <button
                        key={q.id}
                        onClick={() => handleToggleFromList(q.id)}
                        className={cn(
                          "flex w-full items-center gap-2 px-3 py-2 text-left text-sm transition-colors",
                          selected
                            ? "bg-primary/5 text-primary"
                            : "hover:bg-accent"
                        )}
                      >
                        <span className="text-muted-foreground font-mono text-xs w-8 shrink-0">
                          #{q.id}
                        </span>
                        <span className="line-clamp-1 flex-1">{q.content}</span>
                        {selected && <span className="text-primary text-xs font-bold shrink-0">✓</span>}
                      </button>
                    );
                  })}
                </div>
              </div>
            )}

            <Button
              className="w-full"
              onClick={handleSave}
              disabled={!selectedDate || questionIds.length === 0 || setDailySet.isPending}
            >
              {setDailySet.isPending ? "Đang lưu..." : `Lưu ${questionIds.length} câu hỏi cho ngày ${selectedDate}`}
            </Button>
          </CardContent>
        </Card>
      </PermissionGate>

      {/* Existing sets list */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Bộ câu hỏi đã thiết lập</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex items-center justify-center py-8">
              <div className="border-primary h-6 w-6 animate-spin rounded-full border-4 border-t-transparent" />
            </div>
          ) : sets.length === 0 ? (
            <p className="text-muted-foreground py-8 text-center text-sm">
              Chưa có bộ câu hỏi nào
            </p>
          ) : (
            <div className="max-h-[420px] overflow-y-auto space-y-2">
              {sets.map((set) => (
                <div key={set.id} className="flex items-center gap-3 rounded-lg border p-3">
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium">
                      {new Date(set.date).toLocaleDateString("vi-VN")}
                    </p>
                    <p className="text-muted-foreground text-xs truncate">
                      {set.question_ids.length} câu hỏi · IDs:{" "}
                      {set.question_ids.slice(0, 6).join(", ")}
                      {set.question_ids.length > 6 && "..."}
                    </p>
                  </div>
                  <PermissionGate permission="content.update">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleLoadSet(set)}
                      className="text-xs shrink-0"
                    >
                      Sửa
                    </Button>
                  </PermissionGate>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
