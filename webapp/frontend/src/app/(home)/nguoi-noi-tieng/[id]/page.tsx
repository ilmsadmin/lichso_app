import type { Metadata } from "next";
import { getImageUrl } from "@/lib/utils";
import FamousPersonClient from "./FamousPersonClient";

const BASE_URL = process.env.NEXT_PUBLIC_APP_URL || "https://lichso.vn";
const API_URL =
  process.env.API_INTERNAL_URL ||
  process.env.NEXT_PUBLIC_API_URL ||
  "http://localhost:8080/api";

async function fetchFamousPersonById(id: string) {
  try {
    const res = await fetch(`${API_URL}/famous-people/${id}`, {
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
  params: Promise<{ id: string }>;
}): Promise<Metadata> {
  const { id } = await params;
  const person = await fetchFamousPersonById(id);

  if (!person) {
    return {
      title: "Nhân vật không tìm thấy — Lịch Số",
      description: "Nhân vật bạn tìm kiếm không tồn tại hoặc đã bị xóa.",
    };
  }

  const title = person.full_name || person.name;
  const description =
    person.biography_summary ||
    person.short_bio ||
    `Thông tin về ${title} — nhân vật lịch sử Việt Nam.`;
  const ogImage = getImageUrl(person.avatar_url || person.image_url);
  const url = `${BASE_URL}/nguoi-noi-tieng/${id}`;

  return {
    title: `${title} — Lịch Số`,
    description,
    alternates: { canonical: url },
    openGraph: {
      title,
      description,
      url,
      type: "profile",
      locale: "vi_VN",
      siteName: "Lịch Số",
      ...(ogImage ? { images: [{ url: ogImage, width: 800, height: 800, alt: title }] } : {}),
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

export default async function FamousPersonDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;

  // Fetch server-side for JSON-LD
  const person = await fetchFamousPersonById(id);

  const jsonLd = person
    ? {
        "@context": "https://schema.org",
        "@type": "Person",
        name: person.full_name || person.name,
        description: person.biography_summary || person.short_bio || "",
        url: `${BASE_URL}/nguoi-noi-tieng/${id}`,
        ...(person.birth_date ? { birthDate: person.birth_date } : {}),
        ...(person.death_date ? { deathDate: person.death_date } : {}),
        ...(person.nationality ? { nationality: person.nationality } : {}),
        ...(person.avatar_url ? { image: getImageUrl(person.avatar_url) } : {}),
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
      <FamousPersonClient id={id} />
    </>
  );
}
