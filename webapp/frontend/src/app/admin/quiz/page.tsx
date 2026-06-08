"use client";

import { useState, useCallback } from "react";
import Link from "next/link";
import { Plus, Pencil, Trash2, Trophy, CalendarDays, HelpCircle, Sparkles } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { SearchInput } from "@/components/shared/SearchInput";
import { Pagination } from "@/components/shared/Pagination";
import { PermissionGate } from "@/components/auth/PermissionGate";
import { useQuizQuestions, useDeleteQuizQuestion, useQuizLeaderboard } from "@/hooks/useQuiz";
import { ROUTES, DEFAULT_PAGE_SIZE } from "@/lib/constants";
import { QUIZ_CATEGORIES, QUIZ_DIFFICULTIES } from "@/types/quiz";
import type { QuizQuestion } from "@/types/quiz";
import { DailySetManager } from "./DailySetManager";
import AIQuizGenerator from "./AIQuizGenerator";
import { cn } from "@/lib/utils";

// ── helpers ──

function difficultyBadge(difficulty: string) {
  const map: Record<string, string> = {
    easy: "bg-green-100 text-green-700",
    medium: "bg-yellow-100 text-yellow-700",
    hard: "bg-red-100 text-red-700",
  };
  const labels: Record<string, string> = { easy: "Dễ", medium: "TB", hard: "Khó" };
  return (
    <span className={`rounded px-2 py-0.5 text-xs font-medium ${map[difficulty] ?? "bg-muted text-muted-foreground"}`}>
      {labels[difficulty] ?? difficulty}
    </span>
  );
}

function categoryLabel(cat: string) {
  return QUIZ_CATEGORIES.find((c) => c.value === cat)?.label ?? cat;
}

type Tab = "questions" | "daily-sets" | "leaderboard";

// ── Question list tab ──

function QuestionsTab() {
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(DEFAULT_PAGE_SIZE);
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("all");
  const [difficulty, setDifficulty] = useState("all");
  const [aiOpen, setAiOpen] = useState(false);

  const { data, isLoading } = useQuizQuestions({
    page,
    limit,
    search: search || undefined,
    category: category !== "all" ? category : undefined,
    difficulty: difficulty !== "all" ? difficulty : undefined,
  });

  const deleteQuestion = useDeleteQuizQuestion();
  const questions = data?.data ?? [];
  const meta = data?.meta;

  const handleSearch = useCallback((v: string) => { setSearch(v); setPage(1); }, []);
  const handleLimitChange = useCallback((n: number) => { setLimit(n); setPage(1); }, []);

  return (
    <div className="space-y-4">
      <AIQuizGenerator open={aiOpen} onClose={() => setAiOpen(false)} />

      <div className="flex flex-wrap gap-3">
        <SearchInput
          value={search}
          onChange={handleSearch}
          placeholder="Tìm câu hỏi..."
          className="w-full sm:max-w-xs"
        />
        <Select value={category} onValueChange={(v) => { setCategory(v); setPage(1); }}>
          <SelectTrigger className="w-[160px]">
            <SelectValue placeholder="Danh mục" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả danh mục</SelectItem>
            {QUIZ_CATEGORIES.map((c) => (
              <SelectItem key={c.value} value={c.value}>{c.label}</SelectItem>
            ))}
          </SelectContent>
        </Select>
        <Select value={difficulty} onValueChange={(v) => { setDifficulty(v); setPage(1); }}>
          <SelectTrigger className="w-[140px]">
            <SelectValue placeholder="Độ khó" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            {QUIZ_DIFFICULTIES.map((d) => (
              <SelectItem key={d.value} value={d.value}>{d.label}</SelectItem>
            ))}
          </SelectContent>
        </Select>
        <PermissionGate permission="content.create">
          <div className="ml-auto flex gap-2">
            <Button
              size="sm"
              variant="outline"
              className="border-purple-300 text-purple-700 hover:bg-purple-50"
              onClick={() => setAiOpen(true)}
            >
              <Sparkles className="mr-2 h-4 w-4" />
              Sinh bằng AI
            </Button>
            <Button size="sm" asChild>
              <Link href={`${ROUTES.ADMIN_QUIZ}/create`}>
                <Plus className="mr-2 h-4 w-4" />
                Thêm câu hỏi
              </Link>
            </Button>
          </div>
        </PermissionGate>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-16">
          <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
        </div>
      ) : questions.length === 0 ? (
        <div className="flex flex-col items-center justify-center gap-3 py-16 text-center">
          <HelpCircle className="text-muted-foreground h-12 w-12" />
          <p className="text-muted-foreground">Không có câu hỏi nào</p>
        </div>
      ) : (
        <>
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-12">ID</TableHead>
                  <TableHead>Nội dung</TableHead>
                  <TableHead className="w-36">Danh mục</TableHead>
                  <TableHead className="w-24">Độ khó</TableHead>
                  <TableHead className="w-20 text-center">Đáp án</TableHead>
                  <TableHead className="w-20 text-center">Trạng thái</TableHead>
                  <TableHead className="w-24 text-right">Hành động</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {questions.map((q: QuizQuestion) => (
                  <TableRow key={q.id}>
                    <TableCell className="text-muted-foreground text-xs">{q.id}</TableCell>
                    <TableCell>
                      <p className="line-clamp-2 text-sm font-medium">{q.content}</p>
                      {q.hint && (
                        <p className="text-muted-foreground mt-1 line-clamp-1 text-xs">
                          Gợi ý: {q.hint}
                        </p>
                      )}
                    </TableCell>
                    <TableCell>
                      <span className="text-xs">{categoryLabel(q.category)}</span>
                    </TableCell>
                    <TableCell>{difficultyBadge(q.difficulty)}</TableCell>
                    <TableCell className="text-center">
                      <span className="bg-primary/10 text-primary rounded px-2 py-0.5 text-sm font-bold uppercase">
                        {q.correct}
                      </span>
                    </TableCell>
                    <TableCell className="text-center">
                      {q.is_active ? (
                        <Badge variant="outline" className="border-green-500 text-green-600 text-xs">
                          Hoạt động
                        </Badge>
                      ) : (
                        <Badge variant="outline" className="text-muted-foreground text-xs">
                          Ẩn
                        </Badge>
                      )}
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-1">
                        <PermissionGate permission="content.update">
                          <Button variant="ghost" size="icon" className="h-7 w-7" asChild>
                            <Link href={`${ROUTES.ADMIN_QUIZ}/${q.id}`}>
                              <Pencil className="h-3.5 w-3.5" />
                            </Link>
                          </Button>
                        </PermissionGate>
                        <PermissionGate permission="content.delete">
                          <AlertDialog>
                            <AlertDialogTrigger asChild>
                              <Button variant="ghost" size="icon" className="h-7 w-7 text-red-500 hover:text-red-600">
                                <Trash2 className="h-3.5 w-3.5" />
                              </Button>
                            </AlertDialogTrigger>
                            <AlertDialogContent>
                              <AlertDialogHeader>
                                <AlertDialogTitle>Xóa câu hỏi?</AlertDialogTitle>
                                <AlertDialogDescription>
                                  Câu hỏi này sẽ bị xóa vĩnh viễn và không thể khôi phục.
                                </AlertDialogDescription>
                              </AlertDialogHeader>
                              <AlertDialogFooter>
                                <AlertDialogCancel>Hủy</AlertDialogCancel>
                                <AlertDialogAction
                                  onClick={() => deleteQuestion.mutate(q.id)}
                                  className="bg-red-600 hover:bg-red-700"
                                >
                                  Xóa
                                </AlertDialogAction>
                              </AlertDialogFooter>
                            </AlertDialogContent>
                          </AlertDialog>
                        </PermissionGate>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>

          {meta && (
            <Pagination
              page={meta.page}
              limit={meta.limit}
              total={meta.total}
              totalPages={meta.total_pages}
              onPageChange={setPage}
              onLimitChange={handleLimitChange}
            />
          )}
        </>
      )}
    </div>
  );
}

// ── Leaderboard tab ──

function LeaderboardTab() {
  const [period, setPeriod] = useState<"weekly" | "monthly" | "alltime">("weekly");
  const { data, isLoading } = useQuizLeaderboard(period);
  const scores = data?.data ?? [];
  const streakLabel = period === "alltime" ? "Chuỗi tốt nhất" : "Chuỗi hiện tại";

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <p className="text-muted-foreground text-sm">
          Bảng xếp hạng người chơi quiz trên ứng dụng mobile.
        </p>
        <Select value={period} onValueChange={(v) => setPeriod(v as typeof period)}>
          <SelectTrigger className="w-[140px]">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="weekly">Tuần này</SelectItem>
            <SelectItem value="monthly">Tháng này</SelectItem>
            <SelectItem value="alltime">Tất cả</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-16">
          <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
        </div>
      ) : scores.length === 0 ? (
        <div className="flex flex-col items-center justify-center gap-3 py-16">
          <Trophy className="text-muted-foreground h-12 w-12" />
          <p className="text-muted-foreground">Chưa có dữ liệu xếp hạng</p>
        </div>
      ) : (
        <div className="rounded-md border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-12 text-center">Hạng</TableHead>
                <TableHead>Người dùng</TableHead>
                <TableHead className="w-28 text-right">Điểm tuần</TableHead>
                <TableHead className="w-28 text-right">Điểm tháng</TableHead>
                <TableHead className="w-28 text-right">Tổng điểm</TableHead>
                <TableHead className="w-28 text-right">{streakLabel}</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {scores.map((s, i) => (
                <TableRow key={s.user_id}>
                  <TableCell className="text-center text-sm">
                    {i === 0 ? "🥇" : i === 1 ? "🥈" : i === 2 ? "🥉" : `#${i + 1}`}
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <div className="bg-primary/10 text-primary flex h-7 w-7 shrink-0 items-center justify-center rounded-full text-xs font-bold">
                        {s.display_name?.[0]?.toUpperCase() ?? "?"}
                      </div>
                      <span className="text-sm font-medium">{s.display_name || "—"}</span>
                    </div>
                  </TableCell>
                  <TableCell className="text-right text-sm font-medium">{s.week_score}</TableCell>
                  <TableCell className="text-right text-sm">{s.month_score}</TableCell>
                  <TableCell className="text-right text-sm font-bold">{s.total_score}</TableCell>
                  <TableCell className="text-right text-sm">
                    {(period === "alltime" ? s.best_streak : s.cur_streak) > 0
                      ? `🔥 ${period === "alltime" ? s.best_streak : s.cur_streak}`
                      : "—"}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
    </div>
  );
}

// ── Main page ──

const TABS: { id: Tab; label: string; icon: React.ComponentType<{ className?: string }> }[] = [
  { id: "questions", label: "Câu hỏi", icon: HelpCircle },
  { id: "daily-sets", label: "Bộ câu hỏi ngày", icon: CalendarDays },
  { id: "leaderboard", label: "Bảng xếp hạng", icon: Trophy },
];

export default function QuizAdminPage() {
  const [activeTab, setActiveTab] = useState<Tab>("questions");

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Quản lý Quiz</h1>
        <p className="text-muted-foreground">
          Câu hỏi, bộ câu hỏi ngày và bảng xếp hạng ứng dụng mobile.
        </p>
      </div>

      {/* Tabs */}
      <div className="border-b">
        <div className="flex gap-1">
          {TABS.map(({ id, label, icon: Icon }) => (
            <button
              key={id}
              onClick={() => setActiveTab(id)}
              className={cn(
                "flex items-center gap-2 border-b-2 px-4 py-2.5 text-sm font-medium transition-colors",
                activeTab === id
                  ? "border-primary text-primary"
                  : "border-transparent text-muted-foreground hover:text-foreground"
              )}
            >
              <Icon className="h-4 w-4" />
              {label}
            </button>
          ))}
        </div>
      </div>

      {activeTab === "questions" && <QuestionsTab />}
      {activeTab === "daily-sets" && <DailySetManager />}
      {activeTab === "leaderboard" && <LeaderboardTab />}
    </div>
  );
}
