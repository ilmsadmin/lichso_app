"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import * as v3Service from "@/services/v3Service";
import type {
  CreateArticleRelationRequest,
  BatchCreateArticleRelationsRequest,
  UpdateArticleRelationRequest,
  AutoFillRequest,
  CreateDailyContentRequest,
  UpdateDailyContentRequest,
  CreateUserNoteRequest,
  UpdateUserNoteRequest,
  CreateUserCountdownRequest,
  UpdateUserCountdownRequest,
  SubscribeRequest,
  UpdatePreferencesRequest,
} from "@/types/v3";
import { toast } from "sonner";

// ============================================
// Query Keys
// ============================================
const RELATED_ARTICLES_KEY = "related-articles";
const ARTICLE_RELATIONS_KEY = "article-relations";
const DAY_CONTENT_KEY = "day-content";
const DAILY_CONTENT_SCHEDULES_KEY = "daily-content-schedules";
const USER_NOTES_KEY = "user-notes";
const USER_COUNTDOWNS_KEY = "user-countdowns";
const HOROSCOPE_KEY = "horoscope";
const GOOD_DAYS_PURPOSE_KEY = "good-days-purpose";
const STREAK_KEY = "user-streak";
const ACHIEVEMENTS_KEY = "user-achievements";
const NEWSLETTER_KEY = "newsletter";

// ============================================
// Related Articles Hooks
// ============================================

/** Hook for fetching related articles (public) */
export function useRelatedArticles(articleId: string, limit?: number) {
  return useQuery({
    queryKey: [RELATED_ARTICLES_KEY, articleId, limit],
    queryFn: () => v3Service.getRelatedArticles(articleId, limit),
    enabled: !!articleId,
  });
}

/** Hook for fetching article relations (admin) */
export function useArticleRelations(articleId: string, type?: string) {
  return useQuery({
    queryKey: [ARTICLE_RELATIONS_KEY, articleId, type],
    queryFn: () => v3Service.getArticleRelations(articleId, type),
    enabled: !!articleId,
  });
}

/** Hook for creating an article relation */
export function useCreateArticleRelation(articleId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateArticleRelationRequest) =>
      v3Service.createArticleRelation(articleId, data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã thêm bài viết liên quan");
        queryClient.invalidateQueries({
          queryKey: [ARTICLE_RELATIONS_KEY, articleId],
        });
        queryClient.invalidateQueries({
          queryKey: [RELATED_ARTICLES_KEY, articleId],
        });
      }
    },
    onError: () => {
      toast.error("Không thể thêm bài viết liên quan");
    },
  });
}

/** Hook for batch creating article relations */
export function useBatchCreateArticleRelations(articleId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: BatchCreateArticleRelationsRequest) =>
      v3Service.batchCreateArticleRelations(articleId, data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã thêm các bài viết liên quan");
        queryClient.invalidateQueries({
          queryKey: [ARTICLE_RELATIONS_KEY, articleId],
        });
      }
    },
    onError: () => {
      toast.error("Không thể thêm bài viết liên quan");
    },
  });
}

/** Hook for updating an article relation */
export function useUpdateArticleRelation(articleId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      relationId,
      data,
    }: {
      relationId: string;
      data: UpdateArticleRelationRequest;
    }) => v3Service.updateArticleRelation(relationId, data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã cập nhật quan hệ");
        queryClient.invalidateQueries({
          queryKey: [ARTICLE_RELATIONS_KEY, articleId],
        });
      }
    },
    onError: () => {
      toast.error("Không thể cập nhật quan hệ");
    },
  });
}

/** Hook for deleting an article relation */
export function useDeleteArticleRelation(articleId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (relationId: string) => v3Service.deleteArticleRelation(relationId),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã xóa bài viết liên quan");
        queryClient.invalidateQueries({
          queryKey: [ARTICLE_RELATIONS_KEY, articleId],
        });
        queryClient.invalidateQueries({
          queryKey: [RELATED_ARTICLES_KEY, articleId],
        });
      }
    },
    onError: () => {
      toast.error("Không thể xóa bài viết liên quan");
    },
  });
}

// ============================================
// Day Content Hooks
// ============================================

/** Hook for fetching today's content (public) */
export function useDayContentToday() {
  return useQuery({
    queryKey: [DAY_CONTENT_KEY, "today"],
    queryFn: () => v3Service.getDayContentToday(),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
}

/** Hook for fetching content for a specific date (public) */
export function useDayContent(date: string) {
  return useQuery({
    queryKey: [DAY_CONTENT_KEY, date],
    queryFn: () => v3Service.getDayContent(date),
    enabled: !!date,
  });
}

/** Hook for fetching monthly content summary — lightweight counts per day (public) */
export function useMonthContentSummary(year: number, month: number) {
  return useQuery({
    queryKey: [DAY_CONTENT_KEY, "month-summary", year, month],
    queryFn: () => v3Service.getMonthContentSummary(year, month),
    staleTime: 10 * 60 * 1000, // 10 minutes
    enabled: year > 0 && month > 0,
  });
}

/** Hook for fetching daily content stats (admin) */
export function useDailyContentStats(year?: number) {
  return useQuery({
    queryKey: [DAILY_CONTENT_SCHEDULES_KEY, "stats", year],
    queryFn: () => v3Service.getDailyContentStats(year),
    staleTime: 5 * 60 * 1000,
  });
}

/** Hook for auto-filling daily content (admin) */
export function useAutoFillDailyContent() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: AutoFillRequest) => v3Service.autoFillDailyContent(data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success(
          `Đã tự động điền ${response.data?.items_created ?? 0} nội dung cho ${response.data?.filled_days ?? 0} ngày`
        );
        queryClient.invalidateQueries({ queryKey: [DAILY_CONTENT_SCHEDULES_KEY] });
        queryClient.invalidateQueries({ queryKey: [DAY_CONTENT_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể tự động điền nội dung");
    },
  });
}

/** Hook for fetching daily content schedules (admin) */
export function useDailyContentSchedules(params?: {
  page?: number;
  limit?: number;
  content_type?: string;
}) {
  return useQuery({
    queryKey: [DAILY_CONTENT_SCHEDULES_KEY, params],
    queryFn: () => v3Service.getDailyContentSchedules(params),
  });
}

/** Hook for fetching a single daily content schedule by ID (admin) */
export function useDailyContentSchedule(id?: string | null) {
  return useQuery({
    queryKey: [DAILY_CONTENT_SCHEDULES_KEY, "detail", id],
    queryFn: () => v3Service.getDailyContentSchedule(id!),
    enabled: !!id,
    staleTime: 0,
  });
}

/** Hook for creating a daily content schedule */
export function useCreateDailyContent() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateDailyContentRequest) => v3Service.createDailyContent(data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã tạo lịch nội dung");
        queryClient.invalidateQueries({
          queryKey: [DAILY_CONTENT_SCHEDULES_KEY],
        });
      }
    },
    onError: () => {
      toast.error("Không thể tạo lịch nội dung");
    },
  });
}

/** Hook for updating a daily content schedule */
export function useUpdateDailyContent() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateDailyContentRequest }) =>
      v3Service.updateDailyContent(id, data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã cập nhật lịch nội dung");
        queryClient.invalidateQueries({
          queryKey: [DAILY_CONTENT_SCHEDULES_KEY],
        });
      }
    },
    onError: () => {
      toast.error("Không thể cập nhật lịch nội dung");
    },
  });
}

/** Hook for deleting a daily content schedule */
export function useDeleteDailyContent() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => v3Service.deleteDailyContent(id),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã xóa lịch nội dung");
        queryClient.invalidateQueries({
          queryKey: [DAILY_CONTENT_SCHEDULES_KEY],
        });
      }
    },
    onError: () => {
      toast.error("Không thể xóa lịch nội dung");
    },
  });
}

// ============================================
// User Notes Hooks
// ============================================

/** Hook for fetching user notes (paginated) */
export function useUserNotes(params?: { page?: number; limit?: number }) {
  return useQuery({
    queryKey: [USER_NOTES_KEY, params],
    queryFn: () => v3Service.getUserNotes(params),
  });
}

/** Hook for fetching notes by date */
export function useNotesByDate(date: string) {
  return useQuery({
    queryKey: [USER_NOTES_KEY, "date", date],
    queryFn: () => v3Service.getNotesByDate(date),
    enabled: !!date,
  });
}

/** Hook for fetching notes by date range */
export function useNotesByDateRange(startDate: string, endDate: string) {
  return useQuery({
    queryKey: [USER_NOTES_KEY, "range", startDate, endDate],
    queryFn: () => v3Service.getNotesByDateRange(startDate, endDate),
    enabled: !!startDate && !!endDate,
  });
}

/** Hook for creating a user note */
export function useCreateNote() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateUserNoteRequest) => v3Service.createNote(data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã tạo ghi chú");
        queryClient.invalidateQueries({ queryKey: [USER_NOTES_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể tạo ghi chú");
    },
  });
}

/** Hook for updating a user note */
export function useUpdateNote() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateUserNoteRequest }) =>
      v3Service.updateNote(id, data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã cập nhật ghi chú");
        queryClient.invalidateQueries({ queryKey: [USER_NOTES_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể cập nhật ghi chú");
    },
  });
}

/** Hook for deleting a user note */
export function useDeleteNote() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => v3Service.deleteNote(id),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã xóa ghi chú");
        queryClient.invalidateQueries({ queryKey: [USER_NOTES_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể xóa ghi chú");
    },
  });
}

// ============================================
// User Countdowns Hooks
// ============================================

/** Hook for fetching user countdowns (paginated) */
export function useUserCountdowns(params?: { page?: number; limit?: number }) {
  return useQuery({
    queryKey: [USER_COUNTDOWNS_KEY, params],
    queryFn: () => v3Service.getUserCountdowns(params),
  });
}

/** Hook for fetching active countdowns */
export function useActiveCountdowns() {
  return useQuery({
    queryKey: [USER_COUNTDOWNS_KEY, "active"],
    queryFn: () => v3Service.getActiveCountdowns(),
  });
}

/** Hook for fetching upcoming countdowns */
export function useUpcomingCountdowns(days?: number) {
  return useQuery({
    queryKey: [USER_COUNTDOWNS_KEY, "upcoming", days],
    queryFn: () => v3Service.getUpcomingCountdowns(days),
  });
}

/** Hook for creating a countdown */
export function useCreateCountdown() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateUserCountdownRequest) => v3Service.createCountdown(data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã tạo đếm ngược");
        queryClient.invalidateQueries({ queryKey: [USER_COUNTDOWNS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể tạo đếm ngược");
    },
  });
}

/** Hook for updating a countdown */
export function useUpdateCountdown() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateUserCountdownRequest }) =>
      v3Service.updateCountdown(id, data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã cập nhật đếm ngược");
        queryClient.invalidateQueries({ queryKey: [USER_COUNTDOWNS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể cập nhật đếm ngược");
    },
  });
}

/** Hook for deleting a countdown */
export function useDeleteCountdown() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => v3Service.deleteCountdown(id),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã xóa đếm ngược");
        queryClient.invalidateQueries({ queryKey: [USER_COUNTDOWNS_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể xóa đếm ngược");
    },
  });
}

// ============================================
// Horoscope Hooks — Phase 23
// ============================================

/** Hook for fetching all zodiac horoscopes for a date */
export function useAllHoroscopes(year: number, month: number, day: number) {
  return useQuery({
    queryKey: [HOROSCOPE_KEY, "all", year, month, day],
    queryFn: () => v3Service.getAllHoroscopes(year, month, day),
    staleTime: 60 * 60 * 1000, // 1 hour — horoscopes are daily
    enabled: year > 0 && month > 0 && day > 0,
  });
}

/** Hook for fetching horoscope by zodiac index */
export function useHoroscopeByZodiac(year: number, month: number, day: number, zodiac: number) {
  return useQuery({
    queryKey: [HOROSCOPE_KEY, "zodiac", year, month, day, zodiac],
    queryFn: () => v3Service.getHoroscopeByZodiac(year, month, day, zodiac),
    staleTime: 60 * 60 * 1000,
    enabled: year > 0 && month > 0 && day > 0 && zodiac >= 0,
  });
}

/** Hook for fetching horoscope by birth year */
export function useHoroscopeByBirthYear(birthYear: number) {
  return useQuery({
    queryKey: [HOROSCOPE_KEY, "birth-year", birthYear],
    queryFn: () => v3Service.getHoroscopeByBirthYear(birthYear),
    staleTime: 60 * 60 * 1000,
    enabled: birthYear >= 1900 && birthYear <= 2100,
  });
}

/** Hook for calculating lunar age */
export function useLunarAge(birthYear: number) {
  return useQuery({
    queryKey: [HOROSCOPE_KEY, "lunar-age", birthYear],
    queryFn: () => v3Service.calculateLunarAge(birthYear),
    staleTime: 24 * 60 * 60 * 1000, // 24 hours
    enabled: birthYear >= 1900 && birthYear <= 2100,
  });
}

// ============================================
// Good Days Purpose Hooks — Phase 23
// ============================================

/** Hook for fetching available purpose types */
export function useGoodDayPurposes() {
  return useQuery({
    queryKey: [GOOD_DAYS_PURPOSE_KEY, "purposes"],
    queryFn: () => v3Service.getGoodDayPurposes(),
    staleTime: 24 * 60 * 60 * 1000,
  });
}

/** Hook for fetching good days for a specific purpose */
export function useGoodDaysForPurpose(
  year: number,
  month: number,
  purpose: string,
  birthYear?: number,
  spouseYear?: number
) {
  return useQuery({
    queryKey: [GOOD_DAYS_PURPOSE_KEY, year, month, purpose, birthYear, spouseYear],
    queryFn: () => v3Service.getGoodDaysForPurpose(year, month, purpose, birthYear, spouseYear),
    staleTime: 10 * 60 * 1000,
    enabled: year > 0 && month > 0 && !!purpose,
  });
}

// ============================================
// Streaks & Achievements Hooks — Phase 24
// ============================================

/** Hook for recording a daily visit */
export function useRecordVisit() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => v3Service.recordVisit(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [STREAK_KEY] });
      queryClient.invalidateQueries({ queryKey: [ACHIEVEMENTS_KEY] });
    },
  });
}

/** Hook for fetching streak info */
export function useStreak() {
  return useQuery({
    queryKey: [STREAK_KEY],
    queryFn: () => v3Service.getStreak(),
    staleTime: 5 * 60 * 1000,
  });
}

/** Hook for fetching achievements */
export function useAchievements() {
  return useQuery({
    queryKey: [ACHIEVEMENTS_KEY],
    queryFn: () => v3Service.getAchievements(),
    staleTime: 5 * 60 * 1000,
  });
}

/** Hook for fetching user progress */
export function useUserProgress(enabled = true) {
  return useQuery({
    queryKey: [STREAK_KEY, "progress"],
    queryFn: () => v3Service.getUserProgress(),
    staleTime: 5 * 60 * 1000,
    enabled,
    retry: false,
  });
}

/** Hook for fetching streak leaderboard */
export function useLeaderboard(limit?: number) {
  return useQuery({
    queryKey: [STREAK_KEY, "leaderboard", limit],
    queryFn: () => v3Service.getLeaderboard(limit),
    staleTime: 5 * 60 * 1000,
  });
}

// ============================================
// Newsletter Hooks — Phase 24
// ============================================

/** Hook for subscribing to newsletter */
export function useSubscribeNewsletter() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: SubscribeRequest) => v3Service.subscribeNewsletter(data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đăng ký nhận bản tin thành công! 📬");
        queryClient.invalidateQueries({ queryKey: [NEWSLETTER_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể đăng ký nhận bản tin");
    },
  });
}

/** Hook for unsubscribing from newsletter */
export function useUnsubscribeNewsletter() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (email: string) => v3Service.unsubscribeNewsletter(email),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã huỷ đăng ký bản tin");
        queryClient.invalidateQueries({ queryKey: [NEWSLETTER_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể huỷ đăng ký");
    },
  });
}

/** Hook for getting newsletter status */
export function useNewsletterStatus(email: string) {
  return useQuery({
    queryKey: [NEWSLETTER_KEY, email],
    queryFn: () => v3Service.getNewsletterStatus(email),
    enabled: !!email,
  });
}

/** Hook for updating newsletter preferences */
export function useUpdateNewsletterPreferences() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: UpdatePreferencesRequest) => v3Service.updateNewsletterPreferences(data),
    onSuccess: (response) => {
      if (response.success) {
        toast.success("Đã cập nhật tuỳ chọn bản tin");
        queryClient.invalidateQueries({ queryKey: [NEWSLETTER_KEY] });
      }
    },
    onError: () => {
      toast.error("Không thể cập nhật tuỳ chọn");
    },
  });
}
