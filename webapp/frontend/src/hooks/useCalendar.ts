import { useQuery } from "@tanstack/react-query";
import {
  getToday,
  getDate,
  getMonth,
  convertDate,
  getGoodDays,
  getSolarTerms,
} from "@/services/calendarService";
import type { CalendarConvertParams, CalendarGoodDaysParams, DayResponse } from "@/types/calendar";

// ============================================
// Query Keys
// ============================================

export const calendarKeys = {
  all: ["calendar"] as const,
  today: () => [...calendarKeys.all, "today"] as const,
  date: (date: string) => [...calendarKeys.all, "date", date] as const,
  month: (year: number, month: number) => [...calendarKeys.all, "month", year, month] as const,
  convert: (params: CalendarConvertParams) => [...calendarKeys.all, "convert", params] as const,
  goodDays: (params: CalendarGoodDaysParams) => [...calendarKeys.all, "good-days", params] as const,
  solarTerms: (year: number) => [...calendarKeys.all, "solar-terms", year] as const,
};

// ============================================
// Hooks
// ============================================

/**
 * Hook to get today's full calendar info
 */
export function useCalendarToday(initialData?: DayResponse) {
  return useQuery({
    queryKey: calendarKeys.today(),
    queryFn: getToday,
    initialData,
    staleTime: 60 * 1000, // 1 minute
    refetchInterval: 5 * 60 * 1000, // Refetch every 5 minutes
  });
}

/**
 * Hook to get a specific date's full calendar info
 */
export function useCalendarDate(date: string, enabled = true) {
  return useQuery({
    queryKey: calendarKeys.date(date),
    queryFn: () => getDate(date),
    enabled: !!date && enabled,
    staleTime: 10 * 60 * 1000, // 10 minutes
  });
}

/**
 * Hook to get month calendar data
 */
export function useCalendarMonth(year: number, month: number) {
  return useQuery({
    queryKey: calendarKeys.month(year, month),
    queryFn: () => getMonth(year, month),
    staleTime: 10 * 60 * 1000,
  });
}

/**
 * Hook to convert dates
 */
export function useCalendarConvert(params: CalendarConvertParams, enabled = true) {
  return useQuery({
    queryKey: calendarKeys.convert(params),
    queryFn: () => convertDate(params),
    enabled,
    staleTime: Infinity, // Conversions are deterministic
  });
}

/**
 * Hook to get good days
 */
export function useGoodDays(params: CalendarGoodDaysParams) {
  return useQuery({
    queryKey: calendarKeys.goodDays(params),
    queryFn: () => getGoodDays(params),
    staleTime: 10 * 60 * 1000,
  });
}

/**
 * Hook to get solar terms
 */
export function useSolarTerms(year: number) {
  return useQuery({
    queryKey: calendarKeys.solarTerms(year),
    queryFn: () => getSolarTerms(year),
    staleTime: Infinity, // Solar terms don't change
  });
}
