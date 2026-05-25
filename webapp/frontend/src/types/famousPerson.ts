// ============================================
// Famous Person Types
// ============================================

/**
 * Famous person data returned from the API
 */
export interface FamousPerson {
  id: string;
  name: string;
  original_name?: string;
  birth_date: string | null;
  birth_day?: number | null;
  birth_month?: number | null;
  birth_year?: number | null;
  death_date: string | null;
  nationality: string;
  occupation?: string;
  category: string;
  short_bio: string;
  image_url: string;
  tags: string[];
  is_vietnamese: boolean;
  is_active: boolean;
  article_id: string | null;
  created_at: string;
}

/**
 * Famous person summary (for list views)
 */
export interface FamousPersonSummary {
  id: string;
  name: string;
  original_name?: string;
  birth_date: string | null;
  death_date: string | null;
  nationality: string;
  short_bio: string;
  image_url: string;
  category: string;
  is_vietnamese: boolean;
  is_active: boolean;
  created_at: string;
}

/**
 * Create famous person request payload
 */
export interface CreateFamousPersonRequest {
  name: string;
  original_name?: string;
  birth_date?: string;
  death_date?: string;
  nationality?: string;
  occupation?: string;
  category: string;
  short_bio?: string;
  image_url?: string;
  tags?: string[];
  is_vietnamese?: boolean;
  article_id?: string;
}

/**
 * Update famous person request payload
 */
export interface UpdateFamousPersonRequest {
  name?: string;
  original_name?: string;
  birth_date?: string;
  death_date?: string;
  nationality?: string;
  occupation?: string;
  category?: string;
  short_bio?: string;
  image_url?: string;
  tags?: string[];
  is_vietnamese?: boolean;
  article_id?: string;
}

/**
 * Famous person list query parameters
 */
export interface FamousPersonListParams {
  page?: number;
  limit?: number;
  search?: string;
  sort_by?: string;
  sort_order?: "asc" | "desc";
  nationality?: string;
  is_vietnamese?: boolean;
  category?: string;
}
