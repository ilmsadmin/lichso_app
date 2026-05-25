// ============================================
// Bookmark Types
// ============================================

export interface Bookmark {
  id: string;
  solar_date: string; // YYYY-MM-DD
  title: string;
  note: string;
  color: BookmarkColor;
  is_recurring: boolean;
  created_at: string;
}

export type BookmarkColor = "amber" | "jade" | "red" | "gold" | "blue" | "purple";

export interface CreateBookmarkRequest {
  solar_date: string;
  title: string;
  note?: string;
  color?: BookmarkColor;
  is_recurring?: boolean;
}

export interface UpdateBookmarkRequest {
  title?: string;
  note?: string;
  color?: BookmarkColor;
  is_recurring?: boolean;
}

// ============================================
// Reminder Types
// ============================================

export type ReminderType = "holiday" | "anniversary" | "birthday" | "gio" | "custom";

export interface Reminder {
  id: string;
  title: string;
  description: string;
  reminder_type: ReminderType;
  is_lunar: boolean;
  solar_day?: number;
  solar_month?: number;
  lunar_day?: number;
  lunar_month?: number;
  is_recurring: boolean;
  remind_before_days: number;
  notify_email: boolean;
  notify_push: boolean;
  is_active: boolean;
  created_at: string;
}

export interface CreateReminderRequest {
  title: string;
  description?: string;
  reminder_type: ReminderType;
  is_lunar: boolean;
  solar_day?: number;
  solar_month?: number;
  lunar_day?: number;
  lunar_month?: number;
  is_recurring?: boolean;
  remind_before_days?: number;
  notify_email?: boolean;
  notify_push?: boolean;
}

export interface UpdateReminderRequest {
  title?: string;
  description?: string;
  reminder_type?: ReminderType;
  is_lunar?: boolean;
  solar_day?: number;
  solar_month?: number;
  lunar_day?: number;
  lunar_month?: number;
  is_recurring?: boolean;
  remind_before_days?: number;
  notify_email?: boolean;
  notify_push?: boolean;
  is_active?: boolean;
}
