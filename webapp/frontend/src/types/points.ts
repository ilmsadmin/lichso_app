// ============================================
// Admin points management types (mirror backend DTOs)
// ============================================

export interface AdminUserPointsRow {
  user_id: string;
  display_name: string;
  email: string;
  avatar: string;
  balance: number;
  lifetime_earned: number;
  lifetime_spent: number;
  quiz_total_score: number;
  quiz_week_score: number;
  quiz_month_score: number;
  cur_streak: number;
  earned_today: number;
  sessions_today: number;
}

export interface PointWallet {
  user_id: string;
  balance: number;
  lifetime_earned: number;
  lifetime_spent: number;
  updated_at: string;
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
  last_quiz?: string;
  xp: number;
  updated_at: string;
}

export interface PointTransaction {
  id: number;
  user_id: string;
  amount: number;
  direction: "earn" | "spend";
  source: string;
  source_id?: string;
  created_at: string;
}

export interface AdminQuizSessionBrief {
  id: string;
  session_type: string;
  score: number;
  score_v2: number;
  total: number;
  completed: boolean;
  finished_at?: string;
}

export interface AdminUserPointsDetail {
  user_id: string;
  display_name: string;
  email: string;
  avatar: string;
  wallet: PointWallet | null;
  quiz_score: QuizScore | null;
  transactions: PointTransaction[];
  sessions: AdminQuizSessionBrief[];
}

export interface AdminDailyPointsRow {
  date: string;
  app_points_earned: number;
  app_points_spent: number;
  quiz_score: number;
  quiz_sessions: number;
}

export interface AdminPointsListParams {
  page?: number;
  limit?: number;
  search?: string;
  sort_by?: "total" | "week" | "month" | "balance" | "lifetime" | "earned_today" | "sessions_today";
}

export interface AdjustUserPointsRequest {
  wallet_delta: number;
  reset_quiz_score: boolean;
  reason: string;
}

export interface AdjustUserPointsResult {
  wallet: PointWallet | null;
  quiz_score: QuizScore | null;
}
