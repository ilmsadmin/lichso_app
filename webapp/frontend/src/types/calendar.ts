// Calendar & Feng Shui Types for Lịch Số

// ============================================
// Can Chi (Heavenly Stems & Earthly Branches)
// ============================================

export interface CanChi {
  can: string;
  chi: string;
  can_chi: string;
  ngu_hanh: string;
  am_duong: string;
  con_giap: string;
}

export interface TuTru {
  nam: CanChi;
  thang: CanChi;
  ngay: CanChi;
  gio: CanChi;
}

export interface GioCanChi {
  name: string;
  range: string;
  can_chi: string;
  is_hoang_dao: boolean;
}

// ============================================
// Solar Terms (Tiết Khí)
// ============================================

export interface SolarTerm {
  index: number;
  name: string;
  han_tu: string;
  date: string;
  sun_long: number;
}

export interface SolarTermDate extends SolarTerm {
  day: number;
  month: number;
  year: number;
}

export interface CurrentSolarTermInfo {
  current: SolarTermDate;
  next: SolarTermDate;
  progress: number;
  days_left: number;
  description: string;
}

// ============================================
// Feng Shui (Phong Thuỷ)
// ============================================

export interface TrucNgay {
  name: string;
  danh_gia: string;
  mo_ta: string;
}

export interface HuongXuatHanh {
  tai_than: string;
  hy_than: string;
  hac_than: string;
  huong_tot: string[];
  huong_xau: string[];
}

export interface SaoChieuMenh {
  name: string;
  tot_xau: string;
  mo_ta: string;
}

export interface MoonPhase {
  phase: string;
  emoji: string;
  desc: string;
}

export interface PhongThuyInfo {
  chi_so_ngay: number;
  danh_gia: string;
  truc_ngay: TrucNgay;
  huong_xuat_hanh: HuongXuatHanh;
  sao_chieu: SaoChieuMenh;
  moon_phase: MoonPhase;
  viec_nen: string[];
  viec_khong: string[];
}

// ============================================
// Events (Ngày lễ / Sự kiện)
// ============================================

export interface CalendarEvent {
  name: string;
  type: string; // "national" | "traditional" | "international" | "memorial"
  is_off: boolean;
  is_lunar: boolean;
  day: number;
  month: number;
  emoji: string;
  desc: string;
  category: string; // "tet" | "quoc-le" | "truyen-thong" | "quoc-te" | "gio"
}

// ============================================
// Calendar Responses
// ============================================

export interface DayResponse {
  // Solar
  solar_day: number;
  solar_month: number;
  solar_year: number;
  day_of_week: string;
  day_of_week_n: number;

  // Lunar
  lunar_day: number;
  lunar_day_name: string;
  lunar_month: number;
  lunar_month_name: string;
  lunar_year: number;
  is_leap_month: boolean;

  // Can Chi
  tu_tru: TuTru;

  // Giờ Hoàng Đạo
  gio_hoang_dao: GioCanChi[];

  // Tiết Khí
  tiet_khi: CurrentSolarTermInfo;

  // Phong Thuỷ
  phong_thuy: PhongThuyInfo;

  // Sự kiện / Ngày lễ
  events: CalendarEvent[];
}

export interface MonthDayBrief {
  solar_day: number;
  lunar_day: number;
  lunar_day_name: string;
  day_of_week: number;
  is_today: boolean;
  is_good_day: boolean;
  chi_so_ngay: number;
  is_holiday: boolean;
  events?: CalendarEvent[];
}

export interface MonthResponse {
  year: number;
  month: number;
  lunar_info: string;
  days: MonthDayBrief[];
}

export interface ConvertResult {
  solar: {
    day: number;
    month: number;
    year: number;
  };
  lunar: {
    day: number;
    month: number;
    year: number;
    leap_month: boolean;
  };
}

export interface GoodDayInfo {
  solar_day: number;
  solar_month: number;
  lunar_day: number;
  day_of_week: string;
  chi_so_ngay: number;
  danh_gia: string;
  day_can_chi: string;
  truc_ngay: string;
}

// ============================================
// API Request Params
// ============================================

export interface CalendarConvertParams {
  day: number;
  month: number;
  year: number;
  to_lunar?: boolean;
  leap_month?: boolean;
}

export interface CalendarGoodDaysParams {
  year: number;
  month: number;
}
