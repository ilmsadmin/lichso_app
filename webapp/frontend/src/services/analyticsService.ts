import api from "@/lib/api";
import type { ApiResponse } from "@/types/api";

export interface UserGrowthEntry {
  date: string;
  count: number;
}

export interface ActiveUserEntry {
  date: string;
  users: number;
  guests: number;
}

export interface ProviderDistribution {
  provider: string;
  count: number;
}

export interface PlatformDistribution {
  platform: string;
  count: number;
}

export interface TopDeviceEntry {
  device_name: string;
  count: number;
}

export interface AppVersionEntry {
  app_version: string;
  count: number;
}

export interface StreakRanges {
  range_0: number;
  range_1_3: number;
  range_4_7: number;
  range_8_14: number;
  range_15_plus: number;
}

export interface TopPointEarnedEntry {
  user_id: string;
  email: string;
  first_name: string;
  lastName?: string; // wait, let's match the backend json structure
  first_name_raw?: string;
  last_name?: string;
  points: number;
}

export interface PointsSummary {
  total_wallets: number;
  total_points: number;
  average_points: number;
  top_earners: TopPointEarnedEntry[];
}

export interface EngagementSummary {
  total_notes: number;
  total_bookmarks: number;
  total_reminders: number;
}

export interface UserAnalyticsData {
  growth_30d: UserGrowthEntry[];
  active_users_30d: ActiveUserEntry[];
  providers: ProviderDistribution[];
  platforms: PlatformDistribution[];
  top_devices: TopDeviceEntry[];
  app_versions: AppVersionEntry[];
  streaks: StreakRanges;
  points: PointsSummary;
  engagement: EngagementSummary;
}

/**
 * Fetch enriched user analytics data for admin dashboard
 */
export async function getUserAnalytics(): Promise<ApiResponse<UserAnalyticsData>> {
  const response = await api.get<ApiResponse<UserAnalyticsData>>("/admin/analytics/users");
  return response.data;
}
