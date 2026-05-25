// ============================================
// Dashboard Types
// ============================================

/**
 * Dashboard statistics response
 */
export interface DashboardStats {
  total_users: number;
  active_users: number;
  inactive_users: number;
  total_roles: number;
  new_users_today: number;
  new_users_week: number;
  new_users_month: number;
  recent_activity: ActivityEntry[];
  action_counts: Record<string, number>;
  module_counts: Record<string, number>;
}

/**
 * Activity log entry
 */
export interface ActivityEntry {
  id: string;
  user_email: string;
  action: string;
  module: string;
  description: string;
  status: string;
  created_at: string;
  metadata?: Record<string, unknown>;
  user_id?: string;
  ip_address?: string;
  user_agent?: string;
}

/**
 * Activity log list query parameters
 */
export interface ActivityLogParams {
  page?: number;
  limit?: number;
  user_id?: string;
  action?: string;
  module?: string;
  status?: string;
  search?: string;
  start_date?: string;
  end_date?: string;
  sort_by?: string;
  sort_order?: "asc" | "desc";
}
