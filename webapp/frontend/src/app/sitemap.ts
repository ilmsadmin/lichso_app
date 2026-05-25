import type { MetadataRoute } from "next";

const BASE_URL = process.env.NEXT_PUBLIC_APP_URL || "https://lichso.vn";
const API_URL = process.env.NEXT_PUBLIC_API_URL || "https://lichso.vn/api";

// ============================================
// Helpers: fetch all slugs/ids from API
// ============================================
async function fetchAllSlugs(endpoint: string): Promise<{ slug: string; updated_at?: string }[]> {
  try {
    const res = await fetch(`${API_URL}${endpoint}`, {
      next: { revalidate: 3600 },
    });
    if (!res.ok) return [];
    const json = await res.json();
    // Support both { data: [...] } and { data: { items: [...] } }
    const items = Array.isArray(json?.data) ? json.data : (json?.data?.items ?? []);
    return items;
  } catch {
    return [];
  }
}

async function fetchAllIds(endpoint: string): Promise<{ id: string | number; updated_at?: string }[]> {
  try {
    const res = await fetch(`${API_URL}${endpoint}`, {
      next: { revalidate: 3600 },
    });
    if (!res.ok) return [];
    const json = await res.json();
    const items = Array.isArray(json?.data) ? json.data : (json?.data?.items ?? []);
    return items;
  } catch {
    return [];
  }
}

// ============================================
// Dynamic Sitemap
// ============================================
export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const now = new Date();

  // ---- Static pages ----
  const staticPages: MetadataRoute.Sitemap = [
    { url: BASE_URL,                                  lastModified: now, changeFrequency: "daily",   priority: 1.0 },
    { url: `${BASE_URL}/bai-viet`,                    lastModified: now, changeFrequency: "daily",   priority: 0.9 },
    { url: `${BASE_URL}/ngay-nay-trong-lich-su`,      lastModified: now, changeFrequency: "daily",   priority: 0.8 },
    { url: `${BASE_URL}/tra-cuu`,                     lastModified: now, changeFrequency: "daily",   priority: 0.8 },
    { url: `${BASE_URL}/ngay-tot`,                    lastModified: now, changeFrequency: "daily",   priority: 0.8 },
    { url: `${BASE_URL}/su-kien`,                     lastModified: now, changeFrequency: "weekly",  priority: 0.7 },
    { url: `${BASE_URL}/le-hoi`,                      lastModified: now, changeFrequency: "weekly",  priority: 0.7 },
    { url: `${BASE_URL}/nguoi-noi-tieng`,             lastModified: now, changeFrequency: "weekly",  priority: 0.7 },
    { url: `${BASE_URL}/cau-noi-noi-tieng`,           lastModified: now, changeFrequency: "weekly",  priority: 0.7 },
    { url: `${BASE_URL}/phong-thuy`,                  lastModified: now, changeFrequency: "monthly", priority: 0.6 },
    { url: `${BASE_URL}/tu-vi`,                       lastModified: now, changeFrequency: "monthly", priority: 0.6 },
    { url: `${BASE_URL}/gioi-thieu`,                  lastModified: now, changeFrequency: "monthly", priority: 0.4 },
    { url: `${BASE_URL}/contact`,                     lastModified: now, changeFrequency: "monthly", priority: 0.3 },
  ];

  // ---- Dynamic: Danh mục bài viết (/bai-viet/danh-muc/[slug]) ----
  const categories = await fetchAllSlugs("/categories?limit=200&page=1");
  const categoryPages: MetadataRoute.Sitemap = categories.map((c) => ({
    url: `${BASE_URL}/bai-viet/danh-muc/${c.slug}`,
    lastModified: c.updated_at ? new Date(c.updated_at) : now,
    changeFrequency: "weekly" as const,
    priority: 0.8,
  }));

  // ---- Dynamic: Tag bài viết (/bai-viet/tag/[slug]) ----
  const tags = await fetchAllSlugs("/tags?limit=500&page=1");
  const tagPages: MetadataRoute.Sitemap = tags.map((t) => ({
    url: `${BASE_URL}/bai-viet/tag/${t.slug}`,
    lastModified: t.updated_at ? new Date(t.updated_at) : now,
    changeFrequency: "weekly" as const,
    priority: 0.7,
  }));

  // ---- Dynamic: Bài viết (/bai-viet/[slug]) ----
  const articles = await fetchAllSlugs("/articles?limit=2000&page=1&status=published");
  const articlePages: MetadataRoute.Sitemap = articles.map((a) => ({
    url: `${BASE_URL}/bai-viet/${a.slug}`,
    lastModified: a.updated_at ? new Date(a.updated_at) : now,
    changeFrequency: "weekly" as const,
    priority: 0.8,
  }));

  // ---- Dynamic: Sự kiện (/su-kien/[slug]) ----
  const events = await fetchAllSlugs("/events?limit=2000&page=1");
  const eventPages: MetadataRoute.Sitemap = events.map((e) => ({
    url: `${BASE_URL}/su-kien/${e.slug}`,
    lastModified: e.updated_at ? new Date(e.updated_at) : now,
    changeFrequency: "weekly" as const,
    priority: 0.7,
  }));

  // ---- Dynamic: Lễ hội (/le-hoi/[slug]) ----
  const festivals = await fetchAllSlugs("/festivals?limit=2000&page=1");
  const festivalPages: MetadataRoute.Sitemap = festivals.map((f) => ({
    url: `${BASE_URL}/le-hoi/${f.slug}`,
    lastModified: f.updated_at ? new Date(f.updated_at) : now,
    changeFrequency: "weekly" as const,
    priority: 0.7,
  }));

  // ---- Dynamic: Người nổi tiếng (/nguoi-noi-tieng/[id]) ----
  const persons = await fetchAllIds("/famous-people?limit=2000&page=1");
  const personPages: MetadataRoute.Sitemap = persons.map((p) => ({
    url: `${BASE_URL}/nguoi-noi-tieng/${p.id}`,
    lastModified: p.updated_at ? new Date(p.updated_at) : now,
    changeFrequency: "monthly" as const,
    priority: 0.6,
  }));

  return [
    ...staticPages,
    ...categoryPages,
    ...tagPages,
    ...articlePages,
    ...eventPages,
    ...festivalPages,
    ...personPages,
  ];
}
