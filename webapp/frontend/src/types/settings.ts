// ============================================
// Settings Types
// ============================================

/**
 * Setting response from the API
 */
export interface SettingResponse {
  id: string;
  key: string;
  value: unknown;
  group: string;
  description?: string;
  updated_by?: string;
  updated_at: string;
}

/**
 * Grouped settings response
 */
export type GroupedSettings = Record<string, SettingResponse[]>;

/**
 * Update a single setting
 */
export interface UpdateSettingRequest {
  key: string;
  value: unknown;
}

/**
 * Update a group of settings
 */
export interface UpdateSettingsGroupRequest {
  group: string;
  settings: UpdateSettingRequest[];
}

/**
 * Create a new setting
 */
export interface CreateSettingRequest {
  key: string;
  value: unknown;
  group: string;
  description?: string;
}

/**
 * Screen background response from the API
 */
export interface ScreenBackgroundResponse {
  id: string;
  screen_key: string;
  screen_name: string;
  image_url?: string;
  is_active: boolean;
  updated_by?: string;
  updated_at: string;
}
