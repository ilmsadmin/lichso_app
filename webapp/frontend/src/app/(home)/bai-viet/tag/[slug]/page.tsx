import type { Metadata } from "next";
import TagArticlesClient from "./TagArticlesClient";

const BASE_URL = process.env.NEXT_PUBLIC_APP_URL || "https://lichso.vn";
const API_URL =
  process.env.API_INTERNAL_URL ||
  process.env.NEXT_PUBLIC_API_URL ||
  "http://localhost:8080/api";

async function fetchTag(slug: string) {
  try {
    const res = await fetch(`${API_URL}/tags/slug/${slug}`, {
      next: { revalidate: 600 },
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
  const tag = await fetchTag(slug);
  const name = tag?.name ?? slug;
  const description = `Các bài viết được gắn tag #${name} trên Lịch Số.`;

  return {
    title: `#${name} — Lịch Số`,
    description,
    keywords: [name.toLowerCase(), "tag bài viết", "lịch số"],
    alternates: {
      canonical: `${BASE_URL}/bai-viet/tag/${slug}`,
    },
    openGraph: {
      title: `#${name} — Lịch Số`,
      description,
      url: `${BASE_URL}/bai-viet/tag/${slug}`,
      siteName: "Lịch Số",
      locale: "vi_VN",
      type: "website",
    },
  };
}

export default async function TagArticlesPage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;
  return <TagArticlesClient slug={slug} />;
}
