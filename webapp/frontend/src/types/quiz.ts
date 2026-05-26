// ============================================
// Quiz Types
// ============================================

export interface QuizQuestion {
  id: number;
  content: string;
  option_a: string;
  option_b: string;
  option_c: string;
  option_d: string;
  correct: string; // "a"|"b"|"c"|"d"
  hint?: string;
  explanation?: string;
  category: string;
  difficulty: string;
  article_id?: number | null;
  is_active: boolean;
  created_by?: string | null;
  created_at: string;
  updated_at: string;
}

export interface QuizDailySet {
  id: number;
  date: string;
  question_ids: number[];
  created_by?: string | null;
  created_at: string;
}

export interface QuizScore {
  user_id: string;
  display_name: string;
  avatar_url?: string;
  total_score: number;
  week_score: number;
  month_score: number;
  best_streak: number;
  cur_streak: number;
  last_quiz?: string | null;
  updated_at: string;
}

export interface CreateQuizQuestionRequest {
  content: string;
  option_a: string;
  option_b: string;
  option_c: string;
  option_d: string;
  correct: string;
  hint?: string;
  explanation?: string;
  category?: string;
  difficulty?: string;
  article_id?: number | null;
  is_active?: boolean;
}

export type UpdateQuizQuestionRequest = Partial<CreateQuizQuestionRequest>;

export interface QuizQuestionListParams {
  page?: number;
  limit?: number;
  search?: string;
  category?: string;
  difficulty?: string;
}

export interface SetDailySetRequest {
  date: string;
  question_ids: number[];
}

export const QUIZ_CATEGORIES = [
  { value: "history_vn", label: "Lịch sử Việt Nam" },
  { value: "history_world", label: "Lịch sử Thế giới" },
  { value: "culture", label: "Văn hóa" },
  { value: "geography", label: "Địa lý" },
  { value: "general", label: "Kiến thức chung" },
] as const;

export const QUIZ_DIFFICULTIES = [
  { value: "easy", label: "Dễ" },
  { value: "medium", label: "Trung bình" },
  { value: "hard", label: "Khó" },
] as const;

export const QUIZ_CORRECT_OPTIONS = [
  { value: "a", label: "A" },
  { value: "b", label: "B" },
  { value: "c", label: "C" },
  { value: "d", label: "D" },
] as const;

// ============================================
// AI Generation Types
// ============================================

export interface GenerateQuizQuestionsRequest {
  topic?: string;
  text?: string;
  count: number;
  category: string;
  difficulty: string;
}

export interface GenerateQuizTopicsRequest {
  category: string;
  count: number;
  existing_topics?: string[];
}

export interface GeneratedQuizTopic {
  title: string;
  description?: string;
}

export interface GeneratedQuizQuestion {
  content: string;
  option_a: string;
  option_b: string;
  option_c: string;
  option_d: string;
  correct: string;
  hint: string;
  explanation: string;
  category: string;
  difficulty: string;
}
