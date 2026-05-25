import api from "@/lib/api";
import type { ApiResponse, PaginatedResponse } from "@/types/api";
import type {
  Article,
  ArticleSummary,
  ArticleCategory,
  ArticleTag,
  ArticleListParams,
  CategoryListParams,
  TagListParams,
} from "@/types/article";
import type { Quote, QuoteListParams } from "@/types/quote";
import type {
  FamousPerson,
  FamousPersonSummary,
  FamousPersonListParams,
} from "@/types/famousPerson";
import type { HistoricalEvent, EventSummary, EventListParams } from "@/types/event";
import type { FolkFestival, FolkFestivalSummary, FolkFestivalListParams } from "@/types/festival";

// ============================================
// Public Content API Service
// Uses public routes (no authentication required)
// ============================================

// --- Articles ---

export async function getPublicArticles(
  params?: ArticleListParams
): Promise<PaginatedResponse<ArticleSummary>> {
  const response = await api.get<PaginatedResponse<ArticleSummary>>("/articles", {
    params: { ...params, status: "published" },
  });
  return response.data;
}

export async function getPublicArticleBySlug(slug: string): Promise<ApiResponse<Article>> {
  const response = await api.get<ApiResponse<Article>>(`/articles/slug/${slug}`);
  return response.data;
}

export async function searchPublicArticles(
  query: string,
  params?: ArticleListParams
): Promise<PaginatedResponse<ArticleSummary>> {
  const response = await api.get<PaginatedResponse<ArticleSummary>>("/articles/search", {
    params: { ...params, search: query },
  });
  return response.data;
}

export async function getRandomArticles(limit = 5): Promise<ApiResponse<ArticleSummary[]>> {
  const response = await api.get<ApiResponse<ArticleSummary[]>>("/articles/random", {
    params: { limit },
  });
  return response.data;
}

// --- Categories ---

export async function getPublicCategories(
  params?: CategoryListParams
): Promise<PaginatedResponse<ArticleCategory>> {
  const response = await api.get<PaginatedResponse<ArticleCategory>>("/categories", {
    params: { ...params, is_active: true },
  });
  return response.data;
}

export async function getPublicCategoryBySlug(slug: string): Promise<ApiResponse<ArticleCategory>> {
  const response = await api.get<ApiResponse<ArticleCategory>>(`/categories/slug/${slug}`);
  return response.data;
}

// --- Tags ---

export async function getPublicTags(
  params?: TagListParams
): Promise<PaginatedResponse<ArticleTag>> {
  const response = await api.get<PaginatedResponse<ArticleTag>>("/tags", {
    params,
  });
  return response.data;
}

export async function getPublicTagBySlug(slug: string): Promise<ApiResponse<ArticleTag>> {
  const response = await api.get<ApiResponse<ArticleTag>>(`/tags/slug/${slug}`);
  return response.data;
}

// --- Quotes ---

export async function getQuoteOfTheDay(): Promise<ApiResponse<Quote>> {
  const response = await api.get<ApiResponse<Quote>>("/quotes/today");
  return response.data;
}

export async function getRandomQuote(): Promise<ApiResponse<Quote>> {
  const response = await api.get<ApiResponse<Quote>>("/quotes/random");
  return response.data;
}

export async function getPublicQuotes(params?: QuoteListParams): Promise<PaginatedResponse<Quote>> {
  const response = await api.get<PaginatedResponse<Quote>>("/quotes", {
    params: { ...params, is_active: true },
  });
  return response.data;
}

// --- Famous People ---

export async function getPublicFamousPeople(
  params?: FamousPersonListParams
): Promise<PaginatedResponse<FamousPersonSummary>> {
  const response = await api.get<PaginatedResponse<FamousPersonSummary>>("/famous-people", {
    params,
  });
  return response.data;
}

export async function getPublicFamousPerson(id: string): Promise<ApiResponse<FamousPerson>> {
  const response = await api.get<ApiResponse<FamousPerson>>(`/famous-people/${id}`);
  return response.data;
}

export async function getFamousPeopleByBirthday(
  month: number,
  day: number
): Promise<ApiResponse<FamousPerson[]>> {
  const response = await api.get<ApiResponse<FamousPerson[]>>(
    `/famous-people/birthday/${month}/${day}`
  );
  return response.data;
}

// --- Events ---

export async function getPublicEvents(
  params?: EventListParams
): Promise<PaginatedResponse<EventSummary>> {
  const response = await api.get<PaginatedResponse<EventSummary>>("/events", {
    params,
  });
  return response.data;
}

export async function getPublicEventBySlug(slug: string): Promise<ApiResponse<HistoricalEvent>> {
  const response = await api.get<ApiResponse<HistoricalEvent>>(`/events/slug/${slug}`);
  return response.data;
}

export async function getEventsByDate(
  month: number,
  day: number,
  lunarMonth?: number,
  lunarDay?: number
): Promise<ApiResponse<HistoricalEvent[]>> {
  const response = await api.get<ApiResponse<HistoricalEvent[]>>(`/events/date/${month}/${day}`, {
    params: {
      ...(lunarMonth !== undefined && { lunar_month: lunarMonth }),
      ...(lunarDay !== undefined && { lunar_day: lunarDay }),
    },
  });
  return response.data;
}

// --- Folk Festivals ---

export async function getPublicFolkFestivals(
  params?: FolkFestivalListParams
): Promise<PaginatedResponse<FolkFestivalSummary>> {
  const response = await api.get<PaginatedResponse<FolkFestivalSummary>>("/festivals", { params });
  return response.data;
}

export async function getPublicFolkFestivalBySlug(
  slug: string
): Promise<ApiResponse<FolkFestival>> {
  const response = await api.get<ApiResponse<FolkFestival>>(`/festivals/slug/${slug}`);
  return response.data;
}

export async function getFestivalsByLunarDate(
  month: number,
  day: number
): Promise<ApiResponse<FolkFestival[]>> {
  const response = await api.get<ApiResponse<FolkFestival[]>>(`/festivals/lunar/${month}/${day}`);
  return response.data;
}

export async function getFestivalsBySolarDate(
  month: number,
  day: number
): Promise<ApiResponse<FolkFestival[]>> {
  const response = await api.get<ApiResponse<FolkFestival[]>>(`/festivals/solar/${month}/${day}`);
  return response.data;
}
