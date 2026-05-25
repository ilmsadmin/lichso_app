// ============================================
// Notification Types
// ============================================

/**
 * Notification type enum
 */
export type NotificationType = "info" | "success" | "warning" | "error";

/**
 * Notification source reference type
 */
export type NotificationRefType = "reminder" | "bookmark" | "";

/**
 * Notification from the API
 */
export interface Notification {
  id: string;
  user_id: string;
  title: string;
  message: string;
  type: NotificationType;
  is_read: boolean;
  link?: string;
  ref_type?: NotificationRefType;
  ref_id?: string;
  created_at: string;
}

/**
 * List notifications response
 */
export interface NotificationListResponse {
  notifications: Notification[];
  meta: {
    page: number;
    limit: number;
    total: number;
    total_pages: number;
  };
}

/**
 * Unread count response
 */
export interface UnreadCountResponse {
  unread_count: number;
}

/**
 * WebSocket message from the server
 */
export interface WSMessage {
  type: "notification" | "notification_count" | "notification_read_all";
  data: Notification | { unread_count: number } | null;
}
