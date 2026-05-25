// ============================================
// Folk Festival Types
// ============================================

/**
 * Festival type enum - matches backend oneof validation
 */
export type FestivalType = "folk_festival" | "religion" | "national_holiday" | "seasonal" | "other";

/**
 * Folk festival data returned from the API (matches FolkFestivalResponse DTO)
 */
export interface FolkFestival {
  id: string;
  name: string;
  slug: string;
  alternate_name?: string;
  calendar_type: string;
  lunar_day?: number | null;
  lunar_month?: number | null;
  solar_day?: number | null;
  solar_month?: number | null;
  duration_days: number;
  festival_type: string;
  region: string;
  country: string;
  short_description: string;
  traditions: string[];
  image_url: string;
  gallery_urls: string[];
  article_id: string | null;
  importance: string;
  tags: string[];
  is_active: boolean;
  created_at: string;
}

/**
 * Folk festival summary (for list views)
 */
export interface FolkFestivalSummary {
  id: string;
  name: string;
  slug: string;
  alternate_name?: string;
  calendar_type: string;
  lunar_day?: number | null;
  lunar_month?: number | null;
  festival_type: string;
  region: string;
  country: string;
  image_url?: string;
  importance: string;
  is_active: boolean;
  created_at: string;
}

/**
 * Create folk festival request payload (matches CreateFolkFestivalRequest DTO)
 */
export interface CreateFolkFestivalRequest {
  name: string;
  alternate_name?: string;
  calendar_type: string;
  lunar_day?: number;
  lunar_month?: number;
  solar_day?: number;
  solar_month?: number;
  duration_days?: number;
  festival_type: string;
  region?: string;
  country?: string;
  short_description?: string;
  traditions?: string[];
  image_url?: string;
  gallery_urls?: string[];
  article_id?: string;
  importance?: string;
  tags?: string[];
}

/**
 * Update folk festival request payload (matches UpdateFolkFestivalRequest DTO)
 */
export interface UpdateFolkFestivalRequest {
  name?: string;
  alternate_name?: string;
  calendar_type?: string;
  lunar_day?: number;
  lunar_month?: number;
  solar_day?: number;
  solar_month?: number;
  duration_days?: number;
  festival_type?: string;
  region?: string;
  country?: string;
  short_description?: string;
  traditions?: string[];
  image_url?: string;
  gallery_urls?: string[];
  article_id?: string;
  importance?: string;
  tags?: string[];
}

/**
 * Folk festival list query parameters
 */
export interface FolkFestivalListParams {
  page?: number;
  limit?: number;
  search?: string;
  sort_by?: string;
  sort_order?: "asc" | "desc";
  festival_type?: string;
  region?: string;
}
