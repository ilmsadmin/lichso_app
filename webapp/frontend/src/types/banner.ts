// ============================================
// Banner Types
// ============================================

export interface Banner {
  id: string;
  title: string;
  subtitle?: string;
  image_url?: string;
  cta_text?: string;
  cta_route?: string;
  bg_color?: string;
  type: string;
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
  cta_text?: string;
  cta_route?: string;
  bg_color?: string;
  type?: string;
  is_active?: boolean;
  sort_order?: number;
  start_date?: string;
  end_date?: string;
}

export interface UpdateBannerRequest {
  title?: string;
  subtitle?: string;
  image_url?: string;
  cta_text?: string;
  cta_route?: string;
  bg_color?: string;
  type?: string;
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

export const BANNER_TYPES = [
  { value: "feature", label: "Tính năng", color: "#BF360C" },
  { value: "content", label: "Bài viết", color: "#1565C0" },
  { value: "quiz", label: "Đố vui", color: "#2E7D32" },
  { value: "ai", label: "AI", color: "#4A148C" },
  { value: "promo", label: "Khuyến mãi", color: "#E65100" },
  { value: "custom", label: "Tùy chỉnh", color: "#37474F" },
] as const;
