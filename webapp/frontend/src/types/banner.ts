// ============================================
// Banner Types
// ============================================

export type Platform = "all" | "android" | "ios";

export interface Banner {
  id: string;
  title: string;
  subtitle?: string;
  image_url?: string;
  icon_url?: string;
  icon_key?: string;
  cta_text?: string;
  cta_type?: "route" | "url";
  cta_route?: string;
  bg_color?: string;
  type: string;
  platform: Platform;
  is_active: boolean;
  sort_order: number;
  start_date?: string;
  end_date?: string;
  created_at: string;
  updated_at: string;
}

export interface CreateBannerRequest {
  title: string;
  subtitle?: string;
  image_url?: string;
  icon_url?: string;
  icon_key?: string;
  cta_text?: string;
  cta_type?: "route" | "url";
  cta_route?: string;
  bg_color?: string;
  type?: string;
  platform?: Platform;
  is_active?: boolean;
  sort_order?: number;
  start_date?: string;
  end_date?: string;
}

export interface UpdateBannerRequest {
  title?: string;
  subtitle?: string;
  image_url?: string;
  icon_url?: string;
  icon_key?: string;
  cta_text?: string;
  cta_type?: "route" | "url";
  cta_route?: string;
  bg_color?: string;
  type?: string;
  platform?: Platform;
  is_active?: boolean;
  sort_order?: number;
  start_date?: string;
  end_date?: string;
}

export interface BannerListParams {
  page?: number;
  limit?: number;
  search?: string;
}

export interface BannerOrder {
  id: string;
  sort_order: number;
}

export const PLATFORM_OPTIONS = [
  { value: "all", label: "Cả hai (Android & iOS)", short: "Cả hai" },
  { value: "android", label: "Chỉ Android", short: "Android" },
  { value: "ios", label: "Chỉ iOS", short: "iOS" },
] as const;

export const BANNER_TYPES = [
  { value: "feature", label: "Tính năng", color: "#BF360C" },
  { value: "content", label: "Bài viết", color: "#1565C0" },
  { value: "quiz", label: "Đố vui", color: "#2E7D32" },
  { value: "ai", label: "AI", color: "#4A148C" },
  { value: "promo", label: "Khuyến mãi", color: "#E65100" },
  { value: "custom", label: "Tùy chỉnh", color: "#37474F" },
] as const;
