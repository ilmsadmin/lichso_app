import api from "@/lib/api";
import type { ApiResponse, PaginatedResponse } from "@/types/api";
import type {
  QuizQuestion,
  QuizDailySet,
  QuizScore,
  CreateQuizQuestionRequest,
  UpdateQuizQuestionRequest,
  QuizQuestionListParams,
  SetDailySetRequest,
} from "@/types/quiz";

// ── Questions ──

export async function getQuestions(
  params?: QuizQuestionListParams
): Promise<PaginatedResponse<QuizQuestion>> {
  const response = await api.get<PaginatedResponse<QuizQuestion>>("/admin/quiz/questions", {
    params,
  });
  return response.data;
}

export async function getQuestion(id: number): Promise<ApiResponse<QuizQuestion>> {
  const response = await api.get<ApiResponse<QuizQuestion>>(`/admin/quiz/questions/${id}`);
  return response.data;
}

export async function createQuestion(
  data: CreateQuizQuestionRequest
): Promise<ApiResponse<QuizQuestion>> {
  const response = await api.post<ApiResponse<QuizQuestion>>("/admin/quiz/questions", data);
  return response.data;
}

export async function updateQuestion(
  id: number,
  data: UpdateQuizQuestionRequest
): Promise<ApiResponse<QuizQuestion>> {
  const response = await api.put<ApiResponse<QuizQuestion>>(`/admin/quiz/questions/${id}`, data);
  return response.data;
}

export async function deleteQuestion(id: number): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/admin/quiz/questions/${id}`);
  return response.data;
}

// ── Daily Sets ──

export async function getDailySets(): Promise<ApiResponse<QuizDailySet[]>> {
  const response = await api.get<ApiResponse<QuizDailySet[]>>("/admin/quiz/daily-sets");
  return response.data;
}

export async function setDailySet(data: SetDailySetRequest): Promise<ApiResponse<unknown>> {
  const response = await api.post<ApiResponse<unknown>>("/admin/quiz/daily-sets", data);
  return response.data;
}

// ── Leaderboard (public endpoint) ──

export async function getLeaderboard(
  period: "weekly" | "monthly" | "alltime" = "weekly",
  limit = 50
): Promise<ApiResponse<QuizScore[]>> {
  const response = await api.get<ApiResponse<QuizScore[]>>("/quiz/leaderboard", {
    params: { period, limit },
  });
  return response.data;
}
