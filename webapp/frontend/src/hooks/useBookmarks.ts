import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  createBookmark,
  getBookmarks,
  getBookmarksByMonth,
  getBookmarksByDate,
  updateBookmark,
  deleteBookmark,
  createReminder,
  getReminders,
  updateReminder,
  deleteReminder,
} from "@/services/bookmarkService";
import type {
  CreateBookmarkRequest,
  UpdateBookmarkRequest,
  CreateReminderRequest,
  UpdateReminderRequest,
} from "@/types/bookmark";
import { useAuthStore } from "@/stores/authStore";

// ============================================
// Query Keys
// ============================================

export const bookmarkKeys = {
  all: ["bookmarks"] as const,
  list: () => [...bookmarkKeys.all, "list"] as const,
  month: (year: number, month: number) => [...bookmarkKeys.all, "month", year, month] as const,
  date: (date: string) => [...bookmarkKeys.all, "date", date] as const,
};

export const reminderKeys = {
  all: ["reminders"] as const,
  list: () => [...reminderKeys.all, "list"] as const,
  active: () => [...reminderKeys.all, "active"] as const,
};

// ============================================
// Bookmark Hooks
// ============================================

export function useBookmarks() {
  const { isAuthenticated } = useAuthStore();
  return useQuery({
    queryKey: bookmarkKeys.list(),
    queryFn: getBookmarks,
    enabled: isAuthenticated,
    staleTime: 5 * 60 * 1000,
  });
}

export function useBookmarksByMonth(year: number, month: number) {
  const { isAuthenticated } = useAuthStore();
  return useQuery({
    queryKey: bookmarkKeys.month(year, month),
    queryFn: () => getBookmarksByMonth(year, month),
    enabled: isAuthenticated,
    staleTime: 5 * 60 * 1000,
  });
}

export function useBookmarksByDate(date: string) {
  const { isAuthenticated } = useAuthStore();
  return useQuery({
    queryKey: bookmarkKeys.date(date),
    queryFn: () => getBookmarksByDate(date),
    enabled: isAuthenticated && !!date,
    staleTime: 5 * 60 * 1000,
  });
}

export function useCreateBookmark() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateBookmarkRequest) => createBookmark(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: bookmarkKeys.all });
    },
  });
}

export function useUpdateBookmark() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateBookmarkRequest }) =>
      updateBookmark(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: bookmarkKeys.all });
    },
  });
}

export function useDeleteBookmark() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteBookmark(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: bookmarkKeys.all });
    },
  });
}

// ============================================
// Reminder Hooks
// ============================================

export function useReminders(activeOnly = false) {
  const { isAuthenticated } = useAuthStore();
  return useQuery({
    queryKey: activeOnly ? reminderKeys.active() : reminderKeys.list(),
    queryFn: () => getReminders(activeOnly),
    enabled: isAuthenticated,
    staleTime: 5 * 60 * 1000,
  });
}

export function useCreateReminder() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateReminderRequest) => createReminder(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: reminderKeys.all });
    },
  });
}

export function useUpdateReminder() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateReminderRequest }) =>
      updateReminder(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: reminderKeys.all });
    },
  });
}

export function useDeleteReminder() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteReminder(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: reminderKeys.all });
    },
  });
}
