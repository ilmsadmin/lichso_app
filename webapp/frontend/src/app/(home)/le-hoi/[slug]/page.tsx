import type { Metadata } from "next";
import { getImageUrl } from "@/lib/utils";
import FestivalDetailClient from "./FestivalDetailClient";
import { BreadcrumbJsonLd } from "@/components/seo/BreadcrumbJsonLd";

const BASE_URL = process.env.NEXT_PUBLIC_APP_URL || "https://lichso.vn";
const API_URL =
  process.env.API_INTERNAL_URL ||
  process.env.NEXT_PUBLIC_API_URL ||
  "http://localhost:8080/api";

async function fetchFestivalBySlug(slug: string) {
  try {
    const res = await fetch(`${API_URL}/festivals/slug/${slug}`, {
      next: { revalidate: 3600 },
    });
    if (!res.ok) return null;
    const json = await res.json();
    return json?.data ?? null;
  } catch {
    return null;
  }
}

export async function generateMetadata({
  params,
}: {
  params: Promise<{ slug: string }>;
}): Promise<Metadata> {
  const { slug } = await params;
  const festival = await fetchFestivalBySlug(slug);

  if (!festival) {
    return {
      title: "Lễ hội không tìm thấy — Lịch Số",
      description: "Lễ hội bạn tìm kiếm không tồn tại hoặc đã bị xóa.",
    };
  }

  const title = festival.name;
  const description = festival.short_description || `Lễ hội truyền thống: ${festival.name}`;
  const ogImage = getImageUrl(festival.image_url);
  const url = `${BASE_URL}/le-hoi/${slug}`;

  return {
    title: `${title} — Lịch Số`,
    description,
    alternates: { canonical: url },
    openGraph: {
      title,
      description,
      url,
      type: "article",
      locale: "vi_VN",
      siteName: "Lịch Số",
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

export default async function FestivalDetailPage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;
  const festival = await fetchFestivalBySlug(slug);

  const jsonLd = festival
    ? {
        "@context": "https://schema.org",
        "@type": "Event",
        name: festival.name,
        description: festival.short_description || festival.description || "",
        url: `${BASE_URL}/le-hoi/${slug}`,
        inLanguage: "vi",
        location: {
          "@type": "Place",
          name: festival.location || "Việt Nam",
          address: { "@type": "PostalAddress", addressCountry: "VN" },
        },
        organizer: {
          "@type": "Organization",
          name: "Lịch Số",
          url: BASE_URL,
        },
        ...(festival.image_url
          ? { image: getImageUrl(festival.image_url) }
          : {}),
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
          { name: "Lễ hội", url: "/le-hoi" },
          { name: festival?.name || slug, url: `/le-hoi/${slug}` },
        ]}
      />
      <FestivalDetailClient slug={slug} />
    </>
  );
}
