// ============================================
// Popup Types
// ============================================

export interface Popup {
  id: string;
  title: string;
  image_url: string;
  cta_type?: "route" | "url";
  cta_route?: string;
  position: "center_left" | "center_right" | "top_left" | "top_right" | "bottom_left" | "bottom_right";
  is_active: boolean;
  start_date?: string;
  end_date?: string;
  created_at: string;
  updated_at: string;
}

export interface CreatePopupRequest {
  title: string;
  image_url: string;
  cta_type?: "route" | "url";
  cta_route?: string;
  position?: "center_left" | "center_right" | "top_left" | "top_right" | "bottom_left" | "bottom_right";
  is_active?: boolean;
  start_date?: string;
  end_date?: string;
}

export interface UpdatePopupRequest {
  title?: string;
  image_url?: string;
  cta_type?: "route" | "url";
  cta_route?: string;
  position?: "center_left" | "center_right" | "top_left" | "top_right" | "bottom_left" | "bottom_right";
  is_active?: boolean;
  start_date?: string;
  end_date?: string;
}

export interface PopupListParams {
  page?: number;
  limit?: number;
  search?: string;
}

export const POPUP_POSITIONS = [
  { value: "center_left", label: "Giữa bên trái" },
  { value: "center_right", label: "Giữa bên phải" },
  { value: "top_left", label: "Góc trên bên trái" },
  { value: "top_right", label: "Góc trên bên phải" },
  { value: "bottom_left", label: "Góc dưới bên trái" },
  { value: "bottom_right", label: "Góc dưới bên phải" },
] as const;
