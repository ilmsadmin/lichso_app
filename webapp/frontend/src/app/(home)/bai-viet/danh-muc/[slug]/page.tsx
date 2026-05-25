import type { Metadata } from "next";
import CategoryArticlesClient from "./CategoryArticlesClient";

const BASE_URL = process.env.NEXT_PUBLIC_APP_URL || "https://lichso.vn";
const API_URL =
  process.env.API_INTERNAL_URL ||
  process.env.NEXT_PUBLIC_API_URL ||
  "http://localhost:8080/api";

async function fetchCategory(slug: string) {
  try {
    const res = await fetch(`${API_URL}/categories/slug/${slug}`, {
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
  const category = await fetchCategory(slug);
  const name = category?.name ?? "Danh mục";
  const description =
    category?.description ||
    `Các bài viết trong danh mục ${name} trên Lịch Số.`;

  return {
    title: `${name} — Lịch Số`,
    description,
    keywords: [name.toLowerCase(), "bài viết lịch số", "lịch số"],
    alternates: {
      canonical: `${BASE_URL}/bai-viet/danh-muc/${slug}`,
    },
    openGraph: {
      title: `${name} — Lịch Số`,
      description,
      url: `${BASE_URL}/bai-viet/danh-muc/${slug}`,
      siteName: "Lịch Số",
      locale: "vi_VN",
      type: "website",
    },
  };
}

export default async function CategoryArticlesPage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;
  return <CategoryArticlesClient slug={slug} />;
}
