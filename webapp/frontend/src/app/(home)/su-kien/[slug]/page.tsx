import type { Metadata } from "next";
import { getImageUrl } from "@/lib/utils";
import EventDetailClient from "./EventDetailClient";

// ============================================
// Server-side fetch for metadata (OG tags)
// ============================================
async function fetchEventBySlug(slug: string) {
  const apiBase =
    process.env.API_INTERNAL_URL ||
    process.env.NEXT_PUBLIC_API_URL ||
    "http://localhost:8080/api";
  try {
    const res = await fetch(`${apiBase}/events/slug/${slug}`, {
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
// Dynamic Metadata (OG tags per event)
// ============================================
export async function generateMetadata({
  params,
}: {
  params: Promise<{ slug: string }>;
}): Promise<Metadata> {
  const { slug } = await params;
  const event = await fetchEventBySlug(slug);

  if (!event) {
    return {
      title: "Sự kiện không tìm thấy — Lịch Số",
      description: "Sự kiện bạn tìm kiếm không tồn tại hoặc đã bị xóa.",
    };
  }

  const title = event.title;
  const description = event.short_description || `Sự kiện lịch sử: ${event.title}`;
  const ogImage = getImageUrl(event.image_url);

  return {
    title: `${title} — Lịch Số`,
    description,
    openGraph: {
      title,
      description,
      type: "article",
      locale: "vi_VN",
      ...(ogImage ? { images: [{ url: ogImage, width: 1200, height: 630, alt: title }] } : {}),
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
// Page Component (Server Component)
// ============================================
export default async function EventDetailPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params;
  return <EventDetailClient slug={slug} />;
}
