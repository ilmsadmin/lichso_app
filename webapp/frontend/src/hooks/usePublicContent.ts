"use client";

import { useQuery } from "@tanstack/react-query";
import * as publicContent from "@/services/publicContentService";
import type { ArticleListParams, CategoryListParams, TagListParams } from "@/types/article";
import type { QuoteListParams } from "@/types/quote";
import type { FamousPersonListParams } from "@/types/famousPerson";
import type { EventListParams } from "@/types/event";
import type { FolkFestivalListParams } from "@/types/festival";

// ============================================
// Public Content Hooks
// ============================================

// --- Articles ---

const PUBLIC_ARTICLES_KEY = "public-articles";

export function usePublicArticles(params?: ArticleListParams) {
  return useQuery({
    queryKey: [PUBLIC_ARTICLES_KEY, params],
    queryFn: () => publicContent.getPublicArticles(params),
  });
}

export function usePublicArticleBySlug(slug: string) {
  return useQuery({
    queryKey: [PUBLIC_ARTICLES_KEY, "slug", slug],
    queryFn: () => publicContent.getPublicArticleBySlug(slug),
    enabled: !!slug,
  });
}

export function useRandomArticles(limit = 3, seed?: string) {
  return useQuery({
    queryKey: [PUBLIC_ARTICLES_KEY, "random", limit, seed ?? "default"],
    queryFn: () => publicContent.getRandomArticles(limit),
    staleTime: 24 * 60 * 60 * 1000, // 24h — stable for the whole day
  });
}

export function useSearchPublicArticles(query: string, params?: ArticleListParams) {
  return useQuery({
    queryKey: [PUBLIC_ARTICLES_KEY, "search", query, params],
    queryFn: () => publicContent.searchPublicArticles(query, params),
    enabled: query.length >= 2,
  });
}

export function usePublicCategories(params?: CategoryListParams) {
  return useQuery({
    queryKey: ["public-categories", params],
    queryFn: () => publicContent.getPublicCategories(params),
  });
}

export function usePublicCategoryBySlug(slug: string) {
  return useQuery({
    queryKey: ["public-categories", "slug", slug],
    queryFn: () => publicContent.getPublicCategoryBySlug(slug),
    enabled: !!slug,
  });
}

export function usePublicTags(params?: TagListParams) {
  return useQuery({
    queryKey: ["public-tags", params],
    queryFn: () => publicContent.getPublicTags(params),
  });
}

export function usePublicTagBySlug(slug: string) {
  return useQuery({
    queryKey: ["public-tags", "slug", slug],
    queryFn: () => publicContent.getPublicTagBySlug(slug),
    enabled: !!slug,
  });
}

// --- Quotes ---

const PUBLIC_QUOTES_KEY = "public-quotes";

export function useQuoteOfTheDay() {
  return useQuery({
    queryKey: [PUBLIC_QUOTES_KEY, "today"],
    queryFn: () => publicContent.getQuoteOfTheDay(),
    staleTime: 30 * 60 * 1000, // 30 minutes
  });
}

export function useRandomQuote() {
  return useQuery({
    queryKey: [PUBLIC_QUOTES_KEY, "random"],
    queryFn: () => publicContent.getRandomQuote(),
    staleTime: 0,
  });
}

export function usePublicQuotes(params?: QuoteListParams) {
  return useQuery({
    queryKey: [PUBLIC_QUOTES_KEY, params],
    queryFn: () => publicContent.getPublicQuotes(params),
  });
}

// --- Famous People ---

const PUBLIC_FAMOUS_PEOPLE_KEY = "public-famous-people";

export function usePublicFamousPeople(params?: FamousPersonListParams) {
  return useQuery({
    queryKey: [PUBLIC_FAMOUS_PEOPLE_KEY, params],
    queryFn: () => publicContent.getPublicFamousPeople(params),
  });
}

export function usePublicFamousPerson(id: string) {
  return useQuery({
    queryKey: [PUBLIC_FAMOUS_PEOPLE_KEY, id],
    queryFn: () => publicContent.getPublicFamousPerson(id),
    enabled: !!id,
  });
}

export function useFamousPeopleByBirthday(month: number, day: number) {
  return useQuery({
    queryKey: [PUBLIC_FAMOUS_PEOPLE_KEY, "birthday", month, day],
    queryFn: () => publicContent.getFamousPeopleByBirthday(month, day),
    enabled: month > 0 && day > 0,
  });
}

// --- Events ---

const PUBLIC_EVENTS_KEY = "public-events";

export function usePublicEvents(params?: EventListParams) {
  return useQuery({
    queryKey: [PUBLIC_EVENTS_KEY, params],
    queryFn: () => publicContent.getPublicEvents(params),
  });
}

export function usePublicEventBySlug(slug: string) {
  return useQuery({
    queryKey: [PUBLIC_EVENTS_KEY, "slug", slug],
    queryFn: () => publicContent.getPublicEventBySlug(slug),
    enabled: !!slug,
  });
}

export function useEventsByDate(month: number, day: number, lunarMonth?: number, lunarDay?: number) {
  return useQuery({
    queryKey: [PUBLIC_EVENTS_KEY, "date", month, day, lunarMonth, lunarDay],
    queryFn: () => publicContent.getEventsByDate(month, day, lunarMonth, lunarDay),
    enabled: month > 0 && day > 0,
  });
}

// --- Folk Festivals ---

const PUBLIC_FESTIVALS_KEY = "public-festivals";

export function usePublicFolkFestivals(params?: FolkFestivalListParams) {
  return useQuery({
    queryKey: [PUBLIC_FESTIVALS_KEY, params],
    queryFn: () => publicContent.getPublicFolkFestivals(params),
  });
}

export function usePublicFolkFestivalBySlug(slug: string) {
  return useQuery({
    queryKey: [PUBLIC_FESTIVALS_KEY, "slug", slug],
    queryFn: () => publicContent.getPublicFolkFestivalBySlug(slug),
    enabled: !!slug,
  });
}

export function useFestivalsByLunarDate(month: number, day: number) {
  return useQuery({
    queryKey: [PUBLIC_FESTIVALS_KEY, "lunar", month, day],
    queryFn: () => publicContent.getFestivalsByLunarDate(month, day),
    enabled: month > 0 && day > 0,
  });
}

export function useFestivalsBySolarDate(month: number, day: number) {
  return useQuery({
    queryKey: [PUBLIC_FESTIVALS_KEY, "solar", month, day],
    queryFn: () => publicContent.getFestivalsBySolarDate(month, day),
    enabled: month > 0 && day > 0,
  });
}
