import api from "@/lib/api";
import type {
  DayResponse,
  MonthResponse,
  ConvertResult,
  GoodDayInfo,
  CalendarConvertParams,
  CalendarGoodDaysParams,
} from "@/types/calendar";
import type { SolarTermDate } from "@/types/calendar";

// ============================================
// Calendar API Service
// ============================================

/**
 * Get full info for today
 */
export async function getToday(): Promise<DayResponse> {
  const { data } = await api.get("/calendar/today");
  return data.data;
}

/**
 * Get full info for a specific date
 * @param date - format YYYY-MM-DD
 * @param hour - optional hour (0-23)
 */
export async function getDate(date: string, hour?: number): Promise<DayResponse> {
  const params = hour !== undefined ? { hour } : {};
  const { data } = await api.get(`/calendar/date/${date}`, { params });
  return data.data;
}

/**
 * Get month calendar data
 */
export async function getMonth(year: number, month: number): Promise<MonthResponse> {
  const { data } = await api.get(`/calendar/month/${year}/${month}`);
  return data.data;
}

/**
 * Convert between solar and lunar dates
 */
export async function convertDate(params: CalendarConvertParams): Promise<ConvertResult> {
  const { data } = await api.get("/calendar/convert", { params });
  return data.data;
}

/**
 * Get good days in a month
 */
export async function getGoodDays(params: CalendarGoodDaysParams): Promise<GoodDayInfo[]> {
  const { data } = await api.get("/calendar/good-days", { params });
  return data.data;
}

/**
 * Get 24 solar terms for a year
 */
export async function getSolarTerms(year: number): Promise<SolarTermDate[]> {
  const { data } = await api.get(`/calendar/solar-terms/${year}`);
  return data.data;
}

// ============================================
// Feng Shui API Service
// ============================================

/**
 * Get travel direction info for a date
 * @param date - format YYYY-MM-DD
 */
export async function getFengshuiDirection(date: string) {
  const { data } = await api.get(`/fengshui/direction/${date}`);
  return data.data;
}

/**
 * Get Hoang Dao hours for a date
 * @param date - format YYYY-MM-DD
 */
export async function getFengshuiHours(date: string) {
  const { data } = await api.get(`/fengshui/hours/${date}`);
  return data.data;
}

/**
 * Get recommended/discouraged activities for a date
 * @param date - format YYYY-MM-DD
 */
export async function getFengshuiActivities(date: string) {
  const { data } = await api.get(`/fengshui/activities/${date}`);
  return data.data;
}
