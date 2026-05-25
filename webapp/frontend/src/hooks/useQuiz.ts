"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import * as quizService from "@/services/quizService";
import type {
  CreateQuizQuestionRequest,
  UpdateQuizQuestionRequest,
  QuizQuestionListParams,
  SetDailySetRequest,
} from "@/types/quiz";
import { toast } from "sonner";

const QUESTIONS_KEY = "quiz-questions";
const DAILY_SETS_KEY = "quiz-daily-sets";
const LEADERBOARD_KEY = "quiz-leaderboard";

// ── Questions ──

export function useQuizQuestions(params?: QuizQuestionListParams) {
  return useQuery({
    queryKey: [QUESTIONS_KEY, params],
    queryFn: () => quizService.getQuestions(params),
  });
}

export function useQuizQuestion(id: number) {
  return useQuery({
    queryKey: [QUESTIONS_KEY, id],
    queryFn: () => quizService.getQuestion(id),
    enabled: !!id,
  });
}

export function useCreateQuizQuestion() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateQuizQuestionRequest) => quizService.createQuestion(data),
    onSuccess: (res) => {
      if (res.success) {
        toast.success("Tạo câu hỏi thành công");
        queryClient.invalidateQueries({ queryKey: [QUESTIONS_KEY] });
      }
    },
    onError: () => toast.error("Không thể tạo câu hỏi"),
  });
}

export function useUpdateQuizQuestion(id: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UpdateQuizQuestionRequest) => quizService.updateQuestion(id, data),
    onSuccess: (res) => {
      if (res.success) {
        toast.success("Cập nhật câu hỏi thành công");
        queryClient.invalidateQueries({ queryKey: [QUESTIONS_KEY] });
      }
    },
    onError: () => toast.error("Không thể cập nhật câu hỏi"),
  });
}

export function useDeleteQuizQuestion() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => quizService.deleteQuestion(id),
    onSuccess: (res) => {
      if (res.success) {
        toast.success("Xóa câu hỏi thành công");
        queryClient.invalidateQueries({ queryKey: [QUESTIONS_KEY] });
      }
    },
    onError: () => toast.error("Không thể xóa câu hỏi"),
  });
}

// ── Daily Sets ──

export function useQuizDailySets() {
  return useQuery({
    queryKey: [DAILY_SETS_KEY],
    queryFn: () => quizService.getDailySets(),
  });
}

export function useSetDailySet() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: SetDailySetRequest) => quizService.setDailySet(data),
    onSuccess: (res) => {
      if (res.success) {
        toast.success("Cập nhật bộ câu hỏi ngày thành công");
        queryClient.invalidateQueries({ queryKey: [DAILY_SETS_KEY] });
      }
    },
    onError: () => toast.error("Không thể cập nhật bộ câu hỏi ngày"),
  });
}

// ── Leaderboard ──

export function useQuizLeaderboard(period: "weekly" | "monthly" | "alltime" = "weekly") {
  return useQuery({
    queryKey: [LEADERBOARD_KEY, period],
    queryFn: () => quizService.getLeaderboard(period),
  });
}
