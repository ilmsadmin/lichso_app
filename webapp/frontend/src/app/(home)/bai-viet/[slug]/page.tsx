import type { Metadata } from "next";
import { getImageUrl } from "@/lib/utils";
import ArticleDetailClient from "./ArticleDetailClient";
import { BreadcrumbJsonLd } from "@/components/seo/BreadcrumbJsonLd";

// ============================================
// Trích xuất plain text từ HTML content
// ============================================
function extractTextFromHtml(html: string): string {
  // Xóa các thẻ HTML
  return html
    .replace(/<[^>]*>/g, " ")
    .replace(/&nbsp;/g, " ")
    .replace(/&amp;/g, "&")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/\s+/g, " ")
    .trim();
}

// Tạo description hấp dẫn từ các nguồn có sẵn
function buildDescription(article: {
  meta_description?: string;
  excerpt?: string;
  content?: string;
  title?: string;
}): string {
  const raw =
    article.meta_description?.trim() ||
    article.excerpt?.trim() ||
    (article.content ? extractTextFromHtml(article.content) : "") ||
    "";

  if (!raw) return `Khám phá bài viết "${article.title}" trên Lịch Số — kho tàng văn hóa, lịch sử và tri thức Việt Nam.`;

  // Cắt tối đa 160 ký tự, không cắt giữa từ
  if (raw.length <= 160) return raw;
  const truncated = raw.slice(0, 157);
  const lastSpace = truncated.lastIndexOf(" ");
  return (lastSpace > 100 ? truncated.slice(0, lastSpace) : truncated) + "...";
}

// ============================================
// Server-side fetch for metadata (OG tags)
// API_INTERNAL_URL: gọi trực tiếp qua Docker network (tránh vòng lặp nginx)
// NEXT_PUBLIC_API_URL: fallback khi chạy local dev
// ============================================
async function fetchArticleBySlug(slug: string) {
  const apiBase =
    process.env.API_INTERNAL_URL ||
    process.env.NEXT_PUBLIC_API_URL ||
    "http://localhost:8080/api";
  try {
    const res = await fetch(`${apiBase}/articles/slug/${slug}`, {
      next: { revalidate: 60 },
    });
    if (!res.ok) return null;
    const json = await res.json();
    return json?.data ?? null;
  } catch {
    return null;
  }
}

// ============================================
// Dynamic Metadata (OG tags per article)
// ============================================
export async function generateMetadata({
  params,
}: {
  params: Promise<{ slug: string }>;
}): Promise<Metadata> {
  const { slug } = await params;
  const article = await fetchArticleBySlug(slug);

  if (!article) {
    return {
      title: "Bài viết không tìm thấy — Lịch Số",
      description: "Bài viết bạn tìm kiếm không tồn tại hoặc đã bị xóa.",
    };
  }

  const title = article.meta_title || article.title;
  const description = buildDescription(article);
  const ogImage = getImageUrl(article.featured_image);
  const url = `${process.env.NEXT_PUBLIC_APP_URL || "https://lichso.vn"}/bai-viet/${slug}`;

  return {
    title: `${title} — Lịch Số`,
    description,
    alternates: { canonical: url },
    openGraph: {
      title,
      description,
      type: "article",
      locale: "vi_VN",
      url,
      siteName: "Lịch Số",
      ...(ogImage ? { images: [{ url: ogImage, width: 1200, height: 630, alt: title }] } : {}),
      ...(article.published_at ? { publishedTime: article.published_at } : {}),
      ...(article.author?.full_name ? { authors: [article.author.full_name] } : {}),
    },
    twitter: {
      card: "summary_large_image",
      title,
      description,
      ...(ogImage ? { images: [ogImage] } : {}),
    },
    other: {
      "fb:app_id": process.env.NEXT_PUBLIC_FACEBOOK_APP_ID || "",
    },
  };
}

// ============================================
// Page Component (Server Component) + JSON-LD
// ============================================
export default async function ArticleDetailPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params;
  const article = await fetchArticleBySlug(slug);

  const jsonLd = article
    ? {
        "@context": "https://schema.org",
        "@type": "Article",
        headline: article.meta_title || article.title,
        description: buildDescription(article),
        url: `${process.env.NEXT_PUBLIC_APP_URL || "https://lichso.vn"}/bai-viet/${slug}`,
        image: getImageUrl(article.featured_image) || undefined,
        datePublished: article.published_at || article.created_at,
        dateModified: article.updated_at || article.published_at,
        author: article.author?.full_name
          ? { "@type": "Person", name: article.author.full_name }
          : { "@type": "Organization", name: "Lịch Số" },
        publisher: {
          "@type": "Organization",
          name: "Lịch Số",
          url: process.env.NEXT_PUBLIC_APP_URL || "https://lichso.vn",
        },
        inLanguage: "vi",
      }
    : null;

  return (
    <>
      {jsonLd && (
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
        />
      )}
      <BreadcrumbJsonLd
        items={[
          { name: "Trang chủ", url: "/" },
          { name: "Bài viết", url: "/bai-viet" },
          { name: article?.title || slug, url: `/bai-viet/${slug}` },
        ]}
      />
      <ArticleDetailClient slug={slug} />
    </>
  );
}
