import api from "@/lib/api";
import type {
  Bookmark,
  CreateBookmarkRequest,
  UpdateBookmarkRequest,
  Reminder,
  CreateReminderRequest,
  UpdateReminderRequest,
} from "@/types/bookmark";

// ============================================
// Bookmark API Service
// ============================================

export async function createBookmark(data: CreateBookmarkRequest): Promise<Bookmark> {
  const res = await api.post("/bookmarks", data);
  return res.data.data;
}

export async function getBookmarks(): Promise<Bookmark[]> {
  const res = await api.get("/bookmarks");
  return res.data.data;
}

export async function getBookmarksByMonth(year: number, month: number): Promise<Bookmark[]> {
  const res = await api.get(`/bookmarks/month/${year}/${month}`);
  return res.data.data;
}

export async function getBookmarksByDate(date: string): Promise<Bookmark[]> {
  const res = await api.get(`/bookmarks/date/${date}`);
  return res.data.data;
}

export async function updateBookmark(id: string, data: UpdateBookmarkRequest): Promise<Bookmark> {
  const res = await api.put(`/bookmarks/${id}`, data);
  return res.data.data;
}

export async function deleteBookmark(id: string): Promise<void> {
  await api.delete(`/bookmarks/${id}`);
}

// ============================================
// Reminder API Service
// ============================================

export async function createReminder(data: CreateReminderRequest): Promise<Reminder> {
  const res = await api.post("/reminders", data);
  return res.data.data;
}

export async function getReminders(activeOnly = false): Promise<Reminder[]> {
  const res = await api.get("/reminders", { params: activeOnly ? { active: "true" } : {} });
  return res.data.data;
}

export async function updateReminder(id: string, data: UpdateReminderRequest): Promise<Reminder> {
  const res = await api.put(`/reminders/${id}`, data);
  return res.data.data;
}

export async function deleteReminder(id: string): Promise<void> {
  await api.delete(`/reminders/${id}`);
}

// ============================================
// Export API Service
// ============================================

export function getExportICalUrl(year: number, month: number): string {
  const baseUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";
  return `${baseUrl}/export/ical?year=${year}&month=${month}`;
}

export function getExportTextUrl(year: number, month: number): string {
  const baseUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";
  return `${baseUrl}/export/text?year=${year}&month=${month}`;
}
