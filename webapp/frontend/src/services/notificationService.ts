import api from "@/lib/api";
import type { ApiResponse } from "@/types/api";
import type { NotificationListResponse, UnreadCountResponse } from "@/types/notification";

// ============================================
// Notification API Service
// ============================================

/**
 * Get notifications with pagination
 */
export async function getNotifications(
  page = 1,
  limit = 20
): Promise<ApiResponse<NotificationListResponse>> {
  const response = await api.get<ApiResponse<NotificationListResponse>>("/notifications", {
    params: { page, limit },
  });
  return response.data;
}

/**
 * Get unread notification count
 */
export async function getUnreadCount(): Promise<ApiResponse<UnreadCountResponse>> {
  const response = await api.get<ApiResponse<UnreadCountResponse>>("/notifications/unread-count");
  return response.data;
}

/**
 * Mark a notification as read
 */
export async function markAsRead(id: string): Promise<ApiResponse<null>> {
  const response = await api.patch<ApiResponse<null>>(`/notifications/${id}/read`);
  return response.data;
}

/**
 * Mark all notifications as read
 */
export async function markAllAsRead(): Promise<ApiResponse<null>> {
  const response = await api.patch<ApiResponse<null>>("/notifications/read-all");
  return response.data;
}

/**
 * Delete a notification
 */
export async function deleteNotification(id: string): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>(`/notifications/${id}`);
  return response.data;
}

/**
 * Delete all notifications
 */
export async function deleteAllNotifications(): Promise<ApiResponse<null>> {
  const response = await api.delete<ApiResponse<null>>("/notifications/all");
  return response.data;
}
