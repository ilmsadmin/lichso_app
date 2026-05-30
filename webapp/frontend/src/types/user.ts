// ============================================
// User Management Types
// ============================================

export interface UserRoleBrief {
  id: string;
  name: string;
  display_name: string;
}

/** Base user — returned in list and simple detail views */
export interface User {
  id: string;
  email: string;
  first_name: string;
  last_name: string;
  full_name: string;
  avatar: string;
  phone: string;
  provider: string; // "local" | "google"
  is_active: boolean;
  last_login: string | null;
  created_at: string;
  updated_at: string;
  roles: UserRoleBrief[];
  // enriched list fields
  device_count: number;
  platforms: string;       // e.g. "android,ios"
  latest_version: string;  // latest app version
}

export interface DeviceTokenBrief {
  platform: string;
  app_version: string;
  device_id: string;
  device_name: string;
  last_seen: string;
  created_at: string;
}

export interface UserStats {
  bookmark_count: number;
  note_count: number;
  device_count: number;
  streak_days: number;
  points: number;
}

/** Full enriched user returned by /admin/users/:id/detail */
export interface UserDetail extends User {
  provider_id: string;
  devices: DeviceTokenBrief[];
  stats: UserStats;
}

export interface CreateUserRequest {
  email: string;
  password: string;
  first_name: string;
  last_name: string;
  phone?: string;
  is_active?: boolean;
  role_ids?: string[];
}

export interface UpdateUserRequest {
  email?: string;
  first_name?: string;
  last_name?: string;
  phone?: string;
  avatar?: string;
  is_active?: boolean;
  password?: string;
}

export interface ToggleUserStatusRequest {
  is_active: boolean;
}

export interface SetUserRolesRequest {
  role_ids: string[];
}

export interface UserListParams {
  page?: number;
  limit?: number;
  search?: string;
  sort_by?: string;
  sort_order?: "asc" | "desc";
  status?: string;
  role?: string;
  has_device?: string; // "yes" | "no" | undefined = all
}
