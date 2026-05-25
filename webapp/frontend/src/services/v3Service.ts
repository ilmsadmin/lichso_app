import api from "@/lib/api";
import type { ApiResponse, PaginatedResponse } from "@/types/api";
import type {
  ArticleRelation,
  RelatedArticlesResponse,
  CreateArticleRelationRequest,
  BatchCreateArticleRelationsRequest,
  UpdateArticleRelationRequest,
  DailyContentSchedule,
  DayContentResponse,
  MonthContentSummaryResponse,
  AutoFillRequest,
  AutoFillResult,
  DailyContentStatsResponse,
  CreateDailyContentRequest,
  UpdateDailyContentRequest,
  UserNote,
  CreateUserNoteRequest,
  UpdateUserNoteRequest,
  UserCountdown,
  CreateUserCountdownRequest,
  UpdateUserCountdownRequest,
  AllZodiacHoroscope,
  DailyHoroscope,
  LunarAge,
  PurposeGoodDaysResult,
  PurposeInfo,
  UserStreak,
  UserAchievement,
  UserProgress,
  NewsletterSubscription,
  SubscribeRequest,
  UpdatePreferencesRequest,
} from "@/types/v3";

// ============================================
// Article Relations API
// ============================================

/**
 * Get related articles for a specific article (public)
 */
export async function getRelatedArticles(
  articleId: string,
  limit?: number
): Promise<ApiResponse<RelatedArticlesResponse>> {
  const response = await api.get<ApiResponse<RelatedArticlesResponse>>(
    `/articles/${articleId}/related`,
    { params: { limit } }
  );
  return response.data;
}

/**
 * Get article relations (admin)
 */
export async function getArticleRelations(
  articleId: string,
  type?: string
): Promise<ApiResponse<ArticleRelation[]>> {
  const response = await api.get<ApiResponse<ArticleRelation[]>>(
    `/admin/articles/${articleId}/relations`,
    { params: { type } }
  );
  return response.data;
}

/**
 * Create an article relation (admin)
 */
export async function createArticleRelation(
  articleId: string,
  data: CreateArticleRelationRequest
): Promise<ApiResponse<ArticleRelation>> {
  const response = await api.post<ApiResponse<ArticleRelation>>(
    `/admin/articles/${articleId}/relations`,
    data
  );
  return response.data;
}

/**
 * Batch create article relations (admin)
 */
export async function batchCreateArticleRelations(
  articleId: string,
  data: BatchCreateArticleRelationsRequest
): Promise<ApiResponse<ArticleRelation[]>> {
  const response = await api.post<ApiResponse<ArticleRelation[]>>(
    `/admin/articles/${articleId}/relations/batch`,
    data
  );
  return response.data;
}

/**
 * Update an article relation (admin)
 */
export async function updateArticleRelation(
  relationId: string,
  data: UpdateArticleRelationRequest
): Promise<ApiResponse<ArticleRelation>> {
  const response = await api.put<ApiResponse<ArticleRelation>>(
    `/admin/articles/relations/${relationId}`,
    data
  );
  return response.data;
}

/**
 * Delete an article relation (admin)
 */
export async function deleteArticleRelation(relationId: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/admin/articles/relations/${relationId}`);
  return response.data;
}

// ============================================
// Daily Content API
// ============================================

/**
 * Get content for today (public)
 */
export async function getDayContentToday(): Promise<ApiResponse<DayContentResponse>> {
  const response = await api.get<ApiResponse<DayContentResponse>>("/day-content/today");
  return response.data;
}

/**
 * Get content for a specific date (public)
 */
export async function getDayContent(date: string): Promise<ApiResponse<DayContentResponse>> {
  const response = await api.get<ApiResponse<DayContentResponse>>(`/day-content/${date}`);
  return response.data;
}

/**
 * Get monthly content summary — lightweight counts per day (public)
 */
export async function getMonthContentSummary(
  year: number,
  month: number
): Promise<ApiResponse<MonthContentSummaryResponse>> {
  const response = await api.get<ApiResponse<MonthContentSummaryResponse>>(
    `/day-content/month/${year}/${month}`
  );
  return response.data;
}

/**
 * Get daily content stats (admin)
 */
export async function getDailyContentStats(
  year?: number
): Promise<ApiResponse<DailyContentStatsResponse>> {
  const response = await api.get<ApiResponse<DailyContentStatsResponse>>(
    "/admin/daily-content/stats",
    { params: year ? { year } : undefined }
  );
  return response.data;
}

/**
 * Auto-fill daily content for a date range (admin)
 */
export async function autoFillDailyContent(
  data: AutoFillRequest
): Promise<ApiResponse<AutoFillResult>> {
  const response = await api.post<ApiResponse<AutoFillResult>>(
    "/admin/daily-content/auto-fill",
    data
  );
  return response.data;
}

/**
 * Get daily content schedules (admin)
 */
export async function getDailyContentSchedules(params?: {
  page?: number;
  limit?: number;
  content_type?: string;
}): Promise<PaginatedResponse<DailyContentSchedule>> {
  const response = await api.get<PaginatedResponse<DailyContentSchedule>>("/admin/daily-content", {
    params,
  });
  return response.data;
}

/**
 * Get a daily content schedule by ID (admin)
 */
export async function getDailyContentSchedule(
  id: string
): Promise<ApiResponse<DailyContentSchedule>> {
  const response = await api.get<ApiResponse<DailyContentSchedule>>(`/admin/daily-content/${id}`);
  return response.data;
}

/**
 * Create a daily content schedule (admin)
 */
export async function createDailyContent(
  data: CreateDailyContentRequest
): Promise<ApiResponse<DailyContentSchedule>> {
  const response = await api.post<ApiResponse<DailyContentSchedule>>("/admin/daily-content", data);
  return response.data;
}

/**
 * Update a daily content schedule (admin)
 */
export async function updateDailyContent(
  id: string,
  data: UpdateDailyContentRequest
): Promise<ApiResponse<DailyContentSchedule>> {
  const response = await api.put<ApiResponse<DailyContentSchedule>>(
    `/admin/daily-content/${id}`,
    data
  );
  return response.data;
}

/**
 * Delete a daily content schedule (admin)
 */
export async function deleteDailyContent(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/admin/daily-content/${id}`);
  return response.data;
}

// ============================================
// User Notes API
// ============================================

/**
 * Get user notes (paginated)
 */
export async function getUserNotes(params?: {
  page?: number;
  limit?: number;
}): Promise<PaginatedResponse<UserNote>> {
  const response = await api.get<PaginatedResponse<UserNote>>("/notes", {
    params,
  });
  return response.data;
}

/**
 * Get notes for a specific date
 */
export async function getNotesByDate(date: string): Promise<ApiResponse<UserNote[]>> {
  const response = await api.get<ApiResponse<UserNote[]>>(`/notes/date/${date}`);
  return response.data;
}

/**
 * Get notes within a date range
 */
export async function getNotesByDateRange(
  startDate: string,
  endDate: string
): Promise<ApiResponse<UserNote[]>> {
  const response = await api.get<ApiResponse<UserNote[]>>("/notes/range", {
    params: { start_date: startDate, end_date: endDate },
  });
  return response.data;
}

/**
 * Get a note by ID
 */
export async function getNote(id: string): Promise<ApiResponse<UserNote>> {
  const response = await api.get<ApiResponse<UserNote>>(`/notes/${id}`);
  return response.data;
}

/**
 * Create a user note
 */
export async function createNote(data: CreateUserNoteRequest): Promise<ApiResponse<UserNote>> {
  const response = await api.post<ApiResponse<UserNote>>("/notes", data);
  return response.data;
}

/**
 * Update a user note
 */
export async function updateNote(
  id: string,
  data: UpdateUserNoteRequest
): Promise<ApiResponse<UserNote>> {
  const response = await api.put<ApiResponse<UserNote>>(`/notes/${id}`, data);
  return response.data;
}

/**
 * Delete a user note
 */
export async function deleteNote(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/notes/${id}`);
  return response.data;
}

// ============================================
// User Countdowns API
// ============================================

/**
 * Get user countdowns (paginated)
 */
export async function getUserCountdowns(params?: {
  page?: number;
  limit?: number;
}): Promise<PaginatedResponse<UserCountdown>> {
  const response = await api.get<PaginatedResponse<UserCountdown>>("/countdowns", { params });
  return response.data;
}

/**
 * Get active countdowns
 */
export async function getActiveCountdowns(): Promise<ApiResponse<UserCountdown[]>> {
  const response = await api.get<ApiResponse<UserCountdown[]>>("/countdowns/active");
  return response.data;
}

/**
 * Get upcoming countdowns
 */
export async function getUpcomingCountdowns(days?: number): Promise<ApiResponse<UserCountdown[]>> {
  const response = await api.get<ApiResponse<UserCountdown[]>>("/countdowns/upcoming", {
    params: { days },
  });
  return response.data;
}

/**
 * Get a countdown by ID
 */
export async function getCountdown(id: string): Promise<ApiResponse<UserCountdown>> {
  const response = await api.get<ApiResponse<UserCountdown>>(`/countdowns/${id}`);
  return response.data;
}

/**
 * Create a user countdown
 */
export async function createCountdown(
  data: CreateUserCountdownRequest
): Promise<ApiResponse<UserCountdown>> {
  const response = await api.post<ApiResponse<UserCountdown>>("/countdowns", data);
  return response.data;
}

/**
 * Update a user countdown
 */
export async function updateCountdown(
  id: string,
  data: UpdateUserCountdownRequest
): Promise<ApiResponse<UserCountdown>> {
  const response = await api.put<ApiResponse<UserCountdown>>(`/countdowns/${id}`, data);
  return response.data;
}

/**
 * Delete a user countdown
 */
export async function deleteCountdown(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/countdowns/${id}`);
  return response.data;
}

// ============================================
// Horoscope API — Phase 23
// ============================================

/**
 * Get horoscopes for all 12 zodiac signs for a given date
 */
export async function getAllHoroscopes(
  year: number,
  month: number,
  day: number
): Promise<ApiResponse<AllZodiacHoroscope>> {
  const response = await api.get<ApiResponse<AllZodiacHoroscope>>(
    `/horoscope/daily/${year}/${month}/${day}`
  );
  return response.data;
}

/**
 * Get horoscope for a specific zodiac sign
 */
export async function getHoroscopeByZodiac(
  year: number,
  month: number,
  day: number,
  zodiac: number
): Promise<ApiResponse<DailyHoroscope>> {
  const response = await api.get<ApiResponse<DailyHoroscope>>(
    `/horoscope/daily/${year}/${month}/${day}/${zodiac}`
  );
  return response.data;
}

/**
 * Get horoscope by birth year
 */
export async function getHoroscopeByBirthYear(
  birthYear: number
): Promise<ApiResponse<DailyHoroscope>> {
  const response = await api.get<ApiResponse<DailyHoroscope>>(`/horoscope/birth-year/${birthYear}`);
  return response.data;
}

/**
 * Calculate lunar age
 */
export async function calculateLunarAge(birthYear: number): Promise<ApiResponse<LunarAge>> {
  const response = await api.get<ApiResponse<LunarAge>>(`/horoscope/lunar-age/${birthYear}`);
  return response.data;
}

// ============================================
// Good Days for Purpose API — Phase 23
// ============================================

/**
 * Get available purpose types
 */
export async function getGoodDayPurposes(): Promise<ApiResponse<PurposeInfo[]>> {
  const response = await api.get<ApiResponse<PurposeInfo[]>>("/good-days/purposes");
  return response.data;
}

/**
 * Get good days for a specific purpose in a month
 */
export async function getGoodDaysForPurpose(
  year: number,
  month: number,
  purpose: string,
  birthYear?: number,
  spouseYear?: number
): Promise<ApiResponse<PurposeGoodDaysResult>> {
  const response = await api.get<ApiResponse<PurposeGoodDaysResult>>(
    `/good-days/${year}/${month}/${purpose}`,
    {
      params: {
        ...(birthYear ? { birth_year: birthYear } : {}),
        ...(spouseYear ? { spouse_year: spouseYear } : {}),
      },
    }
  );
  return response.data;
}

// ============================================
// Streaks & Achievements API — Phase 24
// ============================================

/**
 * Record a daily visit
 */
export async function recordVisit(): Promise<ApiResponse<UserStreak>> {
  const response = await api.post<ApiResponse<UserStreak>>("/streak/visit");
  return response.data;
}

/**
 * Get current streak info
 */
export async function getStreak(): Promise<ApiResponse<UserStreak>> {
  const response = await api.get<ApiResponse<UserStreak>>("/streak");
  return response.data;
}

/**
 * Get user achievements
 */
export async function getAchievements(): Promise<ApiResponse<UserAchievement[]>> {
  const response = await api.get<ApiResponse<UserAchievement[]>>("/achievements");
  return response.data;
}

/**
 * Get combined user progress (streak + achievements)
 */
export async function getUserProgress(): Promise<ApiResponse<UserProgress>> {
  const response = await api.get<ApiResponse<UserProgress>>("/progress");
  return response.data;
}

/**
 * Get streak leaderboard
 */
export async function getLeaderboard(limit?: number): Promise<ApiResponse<UserStreak[]>> {
  const response = await api.get<ApiResponse<UserStreak[]>>("/streak/leaderboard", {
    params: limit ? { limit } : undefined,
  });
  return response.data;
}

// ============================================
// Newsletter API — Phase 24
// ============================================

/**
 * Subscribe to newsletter
 */
export async function subscribeNewsletter(
  data: SubscribeRequest
): Promise<ApiResponse<NewsletterSubscription>> {
  const response = await api.post<ApiResponse<NewsletterSubscription>>(
    "/newsletter/subscribe",
    data
  );
  return response.data;
}

/**
 * Unsubscribe from newsletter
 */
export async function unsubscribeNewsletter(email: string): Promise<ApiResponse<null>> {
  const response = await api.post<ApiResponse<null>>("/newsletter/unsubscribe", {
    email,
  });
  return response.data;
}

/**
 * Get newsletter subscription status
 */
export async function getNewsletterStatus(
  email: string
): Promise<ApiResponse<NewsletterSubscription>> {
  const response = await api.get<ApiResponse<NewsletterSubscription>>("/newsletter/status", {
    params: { email },
  });
  return response.data;
}

/**
 * Update newsletter preferences
 */
export async function updateNewsletterPreferences(
  data: UpdatePreferencesRequest
): Promise<ApiResponse<NewsletterSubscription>> {
  const response = await api.put<ApiResponse<NewsletterSubscription>>(
    "/newsletter/preferences",
    data
  );
  return response.data;
}
