import api from "@/lib/api";
import type { ApiResponse, PaginatedResponse } from "@/types/api";
import type {
  HistoricalEvent,
  EventSummary,
  CreateEventRequest,
  UpdateEventRequest,
  EventListParams,
} from "@/types/event";

// ============================================
// Event API Service
// ============================================

/**
 * Get paginated events (admin)
 */
export async function getEvents(
  params?: EventListParams
): Promise<PaginatedResponse<EventSummary>> {
  const response = await api.get<PaginatedResponse<EventSummary>>("/admin/events", { params });
  return response.data;
}

/**
 * Get an event by ID
 */
export async function getEvent(id: string): Promise<ApiResponse<HistoricalEvent>> {
  const response = await api.get<ApiResponse<HistoricalEvent>>(`/admin/events/${id}`);
  return response.data;
}

/**
 * Create a new event
 */
export async function createEvent(data: CreateEventRequest): Promise<ApiResponse<HistoricalEvent>> {
  const response = await api.post<ApiResponse<HistoricalEvent>>("/admin/events", data);
  return response.data;
}

/**
 * Update an event
 */
export async function updateEvent(
  id: string,
  data: UpdateEventRequest
): Promise<ApiResponse<HistoricalEvent>> {
  const response = await api.put<ApiResponse<HistoricalEvent>>(`/admin/events/${id}`, data);
  return response.data;
}

/**
 * Delete an event
 */
export async function deleteEvent(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/admin/events/${id}`);
  return response.data;
}
