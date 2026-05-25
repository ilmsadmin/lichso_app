// ============================================
// V3: Article Relation Types
// ============================================

import type { ArticleSummary } from "./article";

/**
 * Article relation type enum
 */
export type ArticleRelationType = "related" | "series" | "reference" | "translation";

/**
 * Article relation data
 */
export interface ArticleRelation {
  id: string;
  source_article_id: string;
  target_article_id: string;
  relation_type: ArticleRelationType;
  sort_order: number;
  is_bidirectional: boolean;
  target_article?: ArticleSummary;
  created_at: string;
}

/**
 * Related articles response (public API)
 */
export interface RelatedArticlesResponse {
  related?: ArticleSummary[];
  series?: ArticleSummary[];
  references?: ArticleSummary[];
  random_picks?: ArticleSummary[];
}

/**
 * Create article relation request
 */
export interface CreateArticleRelationRequest {
  target_article_id: string;
  relation_type: ArticleRelationType;
  sort_order?: number;
  is_bidirectional?: boolean;
}

/**
 * Batch create article relations request
 */
export interface BatchCreateArticleRelationsRequest {
  relations: CreateArticleRelationRequest[];
}

/**
 * Update article relation request
 */
export interface UpdateArticleRelationRequest {
  relation_type?: ArticleRelationType;
  sort_order?: number;
  is_bidirectional?: boolean;
}

// ============================================
// V3: Daily Content Types
// ============================================

/**
 * Content counts for a single day in the month summary
 */
export interface DayContentSummary {
  day: number;
  quotes: number;
  events: number;
  articles: number;
  famous_people: number;
  festivals: number;
  custom: number;
  total: number;
}

/**
 * Monthly content summary response
 */
export interface MonthContentSummaryResponse {
  year: number;
  month: number;
  days: DayContentSummary[];
}

/**
 * Auto-fill request
 */
export interface AutoFillRequest {
  start_date: string;
  end_date: string;
  content_types: string[];
  skip_existing: boolean;
}

/**
 * Auto-fill result
 */
export interface AutoFillResult {
  total_days: number;
  filled_days: number;
  skipped_days: number;
  items_created: number;
}

/**
 * Daily content stats response
 */
export interface DailyContentStatsResponse {
  total_schedules: number;
  active_schedules: number;
  by_type: Record<string, number>;
  by_mode: Record<string, number>;
  coverage_summary: {
    year: number;
    total_days: number;
    covered_days: number;
    empty_days: number;
    coverage_rate: number;
  };
}

/**
 * Daily content type enum
 */
export type DailyContentType =
  | "quote"
  | "event"
  | "article"
  | "famous_person"
  | "folk_festival"
  | "custom";

/**
 * Schedule mode enum
 */
export type ScheduleMode = "fixed_date" | "recurring_annual" | "day_of_year" | "lunar_date";

/**
 * Daily content schedule data
 */
export interface DailyContentSchedule {
  id: string;
  content_type: DailyContentType;
  content_id?: string;
  custom_title?: string;
  custom_content?: string;
  custom_image?: string;
  schedule_mode: ScheduleMode;
  fixed_date?: string;
  day_of_year?: number;
  recurring_month?: number;
  recurring_day?: number;
  lunar_month?: number;
  lunar_day?: number;
  year_filter?: number;
  display_priority: number;
  display_section: string;
  is_active: boolean;
  start_date?: string;
  end_date?: string;
  content?: unknown;
  created_at: string;
  updated_at: string;
}

/**
 * Day content response (all content for a specific day)
 */
export interface DayContentResponse {
  date: string;
  quotes?: unknown[];
  events?: unknown[];
  articles?: ArticleSummary[];
  famous_people?: unknown[];
  festivals?: unknown[];
  custom?: DailyContentSchedule[];
}

/**
 * Create daily content request
 */
export interface CreateDailyContentRequest {
  content_type: DailyContentType;
  content_id?: string;
  custom_title?: string;
  custom_content?: string;
  custom_image?: string;
  schedule_mode: ScheduleMode;
  fixed_date?: string;
  day_of_year?: number;
  recurring_month?: number;
  recurring_day?: number;
  lunar_month?: number;
  lunar_day?: number;
  year_filter?: number;
  display_priority?: number;
  display_section?: string;
  start_date?: string;
  end_date?: string;
}

/**
 * Update daily content request
 */
export interface UpdateDailyContentRequest {
  content_type?: DailyContentType;
  content_id?: string;
  custom_title?: string;
  custom_content?: string;
  custom_image?: string;
  schedule_mode?: ScheduleMode;
  fixed_date?: string;
  day_of_year?: number;
  recurring_month?: number;
  recurring_day?: number;
  lunar_month?: number;
  lunar_day?: number;
  year_filter?: number;
  display_priority?: number;
  display_section?: string;
  is_active?: boolean;
  start_date?: string;
  end_date?: string;
}

// ============================================
// V3: User Note Types
// ============================================

/**
 * User note data
 */
export interface UserNote {
  id: string;
  note_date: string;
  title: string;
  content?: string;
  color: string;
  is_pinned: boolean;
  created_at: string;
  updated_at: string;
}

/**
 * Create user note request
 */
export interface CreateUserNoteRequest {
  note_date: string;
  title: string;
  content?: string;
  color?: string;
  is_pinned?: boolean;
}

/**
 * Update user note request
 */
export interface UpdateUserNoteRequest {
  note_date?: string;
  title?: string;
  content?: string;
  color?: string;
  is_pinned?: boolean;
}

// ============================================
// V3: User Countdown Types
// ============================================

/**
 * User countdown data
 */
export interface UserCountdown {
  id: string;
  title: string;
  description?: string;
  target_date: string;
  target_time?: string;
  color: string;
  icon: string;
  is_recurring: boolean;
  recurring_type?: "yearly" | "monthly";
  notify_before_days: number;
  days_remaining: number;
  is_active: boolean;
  created_at: string;
  updated_at: string;
}

/**
 * Create user countdown request
 */
export interface CreateUserCountdownRequest {
  title: string;
  description?: string;
  target_date: string;
  target_time?: string;
  color?: string;
  icon?: string;
  is_recurring?: boolean;
  recurring_type?: "yearly" | "monthly";
  notify_before_days?: number;
}

/**
 * Update user countdown request
 */
export interface UpdateUserCountdownRequest {
  title?: string;
  description?: string;
  target_date?: string;
  target_time?: string;
  color?: string;
  icon?: string;
  is_recurring?: boolean;
  recurring_type?: "yearly" | "monthly";
  notify_before_days?: number;
  is_active?: boolean;
}

// ============================================
// V3: Horoscope Types — Phase 23
// ============================================

export interface ZodiacSign {
  index: number;
  name: string;
  animal: string;
  emoji: string;
  character: string;
}

export interface HoroscopeRating {
  category: string;
  stars: number;
  advice: string;
  emoji: string;
}

export interface DailyHoroscope {
  date: string;
  zodiac: ZodiacSign;
  overall: number;
  overall_text: string;
  ratings: HoroscopeRating[];
  lucky_color: string[];
  lucky_number: number[];
  lucky_hour: string[];
  direction: string;
  advice: string;
  compatibility: string;
}

export interface AllZodiacHoroscope {
  date: string;
  day_can_chi: string;
  horoscopes: DailyHoroscope[];
}

export interface LunarAge {
  birth_year: number;
  current_year: number;
  tuoi_duong: number;
  tuoi_am: number;
  tuoi_mu: number;
  con_giap: string;
  con_giap_emoji: string;
  can_chi: string;
  ngu_hanh: string;
  menh: string;
}

// ============================================
// V3: Good Days for Purpose — Phase 23
// ============================================

export interface PurposeGoodDay {
  solar_day: number;
  solar_month: number;
  solar_year: number;
  lunar_day: number;
  lunar_month: number;
  day_of_week: string;
  day_can_chi: string;
  truc_ngay: string;
  score: number;
  reasons: string[];
  viec_nen: string[];
  gio_tot: string[];
}

export interface PurposeGoodDaysResult {
  year: number;
  month: number;
  purpose: string;
  purpose_name: string;
  good_days: PurposeGoodDay[];
  total: number;
}

export interface PurposeInfo {
  key: string;
  name: string;
  emoji: string;
}

// ============================================
// V3: Streaks & Achievements — Phase 24
// ============================================

export interface UserStreak {
  id: string;
  user_id: string;
  current_streak: number;
  longest_streak: number;
  last_visit_date?: string;
  total_visits: number;
  created_at: string;
  updated_at: string;
}

export interface UserAchievement {
  id: string;
  user_id: string;
  achievement_key: string;
  achievement_name: string;
  description?: string;
  badge: string;
  progress: number;
  target: number;
  unlocked: boolean;
  unlocked_at?: string;
  created_at: string;
  updated_at: string;
}

export interface UserProgress {
  streak: UserStreak | null;
  achievements: UserAchievement[];
  total_achievements: number;
  unlocked_count: number;
}

// ============================================
// V3: Newsletter — Phase 24
// ============================================

export interface NewsletterSubscription {
  id: string;
  email: string;
  name: string;
  frequency: string;
  preferences: Record<string, unknown>;
  is_active: boolean;
  confirmed_at?: string;
  unsubscribed_at?: string;
  last_sent_at?: string;
  created_at: string;
}

export interface SubscribeRequest {
  email: string;
  name?: string;
  frequency?: string;
}

export interface UpdatePreferencesRequest {
  email: string;
  frequency?: string;
  preferences?: Record<string, unknown>;
}
