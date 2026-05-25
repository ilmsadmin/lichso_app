// ============================================
// API Response Types
// ============================================

/**
 * Standard API response from the backend
 */
export interface ApiResponse<T = unknown> {
  success: boolean;
  message: string;
  data?: T;
  error?: ApiError;
  meta?: PaginationMeta;
}

/**
 * API error detail
 */
export interface ApiError {
  code: number;
  detail?: string;
  fields?: Record<string, string>;
}

/**
 * Pagination metadata
 */
export interface PaginationMeta {
  page: number;
  limit: number;
  total: number;
  total_pages: number;
}

/**
 * Paginated response
 */
export interface PaginatedResponse<T> extends ApiResponse<T[]> {
  meta: PaginationMeta;
}

/**
 * Pagination request params
 */
export interface PaginationParams {
  page?: number;
  limit?: number;
  search?: string;
  sort_by?: string;
  sort_order?: "asc" | "desc";
}

/**
 * Health check response
 */
export interface HealthCheckData {
  status: "healthy" | "degraded" | "unhealthy";
  version: string;
  uptime: string;
  env: string;
  databases: {
    postgres: DatabaseHealth;
    mongodb: DatabaseHealth;
    redis: DatabaseHealth;
  };
}

/**
 * Database health info
 */
export interface DatabaseHealth {
  status: "connected" | "disconnected";
  latency?: string;
  error?: string;
}
