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
  locations: string[];
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
  locations?: string[];
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
  locations?: string[];
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
  { value: "quiz_home", label: "Đố vui (riêng màn Quiz)", color: "#1B5E20" },
  { value: "ai", label: "AI", color: "#4A148C" },
  { value: "promo", label: "Khuyến mãi", color: "#E65100" },
  { value: "custom", label: "Tùy chỉnh", color: "#37474F" },
] as const;

export const APP_ROUTES = [
  { value: "home", label: "Trang chủ" },
  { value: "calendar", label: "Lịch tháng" },
  { value: "gooddays", label: "Ngày tốt/xấu" },
  { value: "knowledge_feed", label: "Bài viết khám phá" },
  { value: "quiz_home", label: "Đố vui" },
  { value: "survey", label: "Khảo sát ý kiến" },
  { value: "chat", label: "AI Tử Vi" },
  { value: "tools", label: "Tiện ích (Chung)" },
  { value: "tools_calendar", label: "Tiện ích - Lịch & Ngày tốt" },
  { value: "tools_feng_shui", label: "Tiện ích - Phong thủy & Nghi lễ" },
  { value: "tools_utility", label: "Tiện ích - Công cụ thực dụng" },
  { value: "tools_collection", label: "Tiện ích - Kho & Khám phá" },
  { value: "prayers", label: "Văn khấn" },
  { value: "history", label: "Ngày này năm xưa" },
  { value: "profile", label: "Hồ sơ" },
  { value: "bookmarks", label: "Ngày đã lưu" },
  { value: "tasks", label: "Ghi chú" },
  { value: "countdown", label: "Đếm ngày" },
  { value: "familytree", label: "Cây gia phả" },
  { value: "oracle_draw", label: "Rút thẻ" },
  { value: "daily_store", label: "Cửa hàng ngày" },
  { value: "ledger", label: "Lịch sử điểm" },
  { value: "zodiac_collection", label: "Bộ sưu tập con giáp" },
  { value: "date_picker", label: "Chọn ngày đẹp" },
  { value: "tiet_khi", label: "Tiết khí" },
  { value: "date_math", label: "Tính ngày" },
  { value: "birth_planner", label: "Kế hoạch sinh" },
  { value: "cycle_tracker", label: "Theo dõi chu kỳ" },
  { value: "world_clock", label: "Giờ thế giới" },
  { value: "widget_manager", label: "Quản lý widget" },
  { value: "leaderboard", label: "Bảng xếp hạng" },
  { value: "quiz_session", label: "Đố vui - Chơi bộ đề hôm nay" },
] as const;
