import api from "@/lib/api";
import type { ApiResponse, PaginatedResponse } from "@/types/api";
import type { Quote, CreateQuoteRequest, UpdateQuoteRequest, QuoteListParams } from "@/types/quote";

// ============================================
// Quote API Service
// ============================================

/**
 * Get paginated quotes (admin)
 */
export async function getQuotes(params?: QuoteListParams): Promise<PaginatedResponse<Quote>> {
  const response = await api.get<PaginatedResponse<Quote>>("/admin/quotes", { params });
  return response.data;
}

/**
 * Get a quote by ID
 */
export async function getQuote(id: string): Promise<ApiResponse<Quote>> {
  const response = await api.get<ApiResponse<Quote>>(`/admin/quotes/${id}`);
  return response.data;
}

/**
 * Create a new quote
 */
export async function createQuote(data: CreateQuoteRequest): Promise<ApiResponse<Quote>> {
  const response = await api.post<ApiResponse<Quote>>("/admin/quotes", data);
  return response.data;
}

/**
 * Update a quote
 */
export async function updateQuote(
  id: string,
  data: UpdateQuoteRequest
): Promise<ApiResponse<Quote>> {
  const response = await api.put<ApiResponse<Quote>>(`/admin/quotes/${id}`, data);
  return response.data;
}

/**
 * Delete a quote
 */
export async function deleteQuote(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/admin/quotes/${id}`);
  return response.data;
}
