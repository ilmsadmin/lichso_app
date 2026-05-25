// ============================================
// Event Types
// ============================================

/**
 * Event type enum - matches backend oneof validation
 */
export type EventType =
  | "historical_event"
  | "national_day"
  | "world_day"
  | "anniversary"
  | "cultural"
  | "military";

/**
 * Event importance enum
 */
export type EventImportance = "high" | "medium" | "low";

/**
 * Event data returned from the API (matches EventResponse DTO)
 */
export interface HistoricalEvent {
  id: string;
  title: string;
  slug: string;
  event_date: string;
  event_day: number;
  event_month: number;
  event_year?: number | null;
  is_lunar: boolean;
  is_recurring: boolean;
  event_type: EventType;
  country: string;
  country_code?: string;
  flag_emoji?: string;
  short_description: string;
  image_url: string;
  article_id: string | null;
  importance: EventImportance;
  tags: string[];
  is_active: boolean;
  created_at: string;
}

/**
 * Event summary (for list views)
 */
export interface EventSummary {
  id: string;
  title: string;
  slug: string;
  event_date: string;
  event_day: number;
  event_month: number;
  event_type: EventType;
  importance: EventImportance;
  country: string;
  image_url?: string;
  is_lunar: boolean;
  is_active: boolean;
  created_at: string;
}

/**
 * Create event request payload (matches CreateEventRequest DTO)
 */
export interface CreateEventRequest {
  title: string;
  event_date?: string;
  event_day: number;
  event_month: number;
  event_year?: number;
  is_lunar?: boolean;
  is_recurring?: boolean;
  event_type: string;
  country?: string;
  country_code?: string;
  flag_emoji?: string;
  short_description?: string;
  image_url?: string;
  article_id?: string;
  importance?: string;
  tags?: string[];
}

/**
 * Update event request payload (matches UpdateEventRequest DTO)
 */
export interface UpdateEventRequest {
  title?: string;
  event_date?: string;
  event_day?: number;
  event_month?: number;
  event_year?: number;
  is_lunar?: boolean;
  is_recurring?: boolean;
  event_type?: string;
  country?: string;
  country_code?: string;
  flag_emoji?: string;
  short_description?: string;
  image_url?: string;
  article_id?: string;
  importance?: string;
  tags?: string[];
}

/**
 * Event list query parameters
 */
export interface EventListParams {
  page?: number;
  limit?: number;
  search?: string;
  sort_by?: string;
  sort_order?: "asc" | "desc";
  event_type?: string;
  importance?: string;
  country?: string;
}
