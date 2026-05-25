// ============================================
// Quote Types
// ============================================

/**
 * Quote data returned from the API (matches QuoteResponse DTO)
 */
export interface Quote {
  id: string;
  quote: string;
  original_quote: string;
  original_language: string;
  author: string;
  author_bio: string;
  author_birth_year?: number | null;
  author_death_year?: number | null;
  author_nationality: string;
  author_image_url: string;
  tags: string[];
  day_of_year?: number | null;
  is_active: boolean;
  created_at: string;
}

/**
 * Create quote request payload (matches CreateQuoteRequest DTO)
 */
export interface CreateQuoteRequest {
  quote: string;
  original_quote?: string;
  original_language?: string;
  author: string;
  author_bio?: string;
  author_birth_year?: number;
  author_death_year?: number;
  author_nationality?: string;
  author_image_url?: string;
  tags?: string[];
  day_of_year?: number;
}

/**
 * Update quote request payload (matches UpdateQuoteRequest DTO)
 */
export interface UpdateQuoteRequest {
  quote?: string;
  original_quote?: string;
  original_language?: string;
  author?: string;
  author_bio?: string;
  author_birth_year?: number;
  author_death_year?: number;
  author_nationality?: string;
  author_image_url?: string;
  tags?: string[];
  day_of_year?: number;
  is_active?: boolean;
}

/**
 * Quote list query parameters
 */
export interface QuoteListParams {
  page?: number;
  limit?: number;
  search?: string;
  sort_by?: string;
  sort_order?: "asc" | "desc";
  is_active?: boolean;
  author?: string;
}
