import type { Metadata } from "next";
import V2HomeClient from "./V2HomeClient";
import type { DayResponse } from "@/types/calendar";

const BASE_URL = process.env.NEXT_PUBLIC_APP_URL || "https://lichso.vn";
const API_URL =
  process.env.API_INTERNAL_URL ||
  process.env.NEXT_PUBLIC_API_URL ||
  "http://localhost:8080/api";

// Server-side fetch today's calendar data — eliminates client waterfall (LCP fix)
async function fetchTodayServer(): Promise<DayResponse | null> {
  try {
    const res = await fetch(`${API_URL}/calendar/today`, {
      next: { revalidate: 300 }, // Cache 5 min on server
    });
    if (!res.ok) return null;
    const json = await res.json();
    return json?.data ?? null;
  } catch {
    return null;
  }
}

export const metadata: Metadata = {
  title: "Lịch Số — Lịch Vạn Niên Việt Nam Hiện Đại",
  description:
    "Tra cứu lịch âm dương, ngày tốt xấu, giờ hoàng đạo, phong thủy, tiết khí — Lịch Vạn Niên Việt Nam chính xác, hiện đại.",
  keywords: [
    "lịch vạn niên",
    "lịch âm dương",
    "ngày tốt xấu",
    "giờ hoàng đạo",
    "phong thủy",
    "tiết khí",
    "lịch số",
    "âm lịch 2026",
  ],
  alternates: {
    canonical: BASE_URL,
  },
  openGraph: {
    title: "Lịch Số — Lịch Vạn Niên Việt Nam",
    description: "Tra cứu lịch âm dương, ngày tốt xấu, giờ hoàng đạo, phong thủy, tiết khí.",
    url: BASE_URL,
    siteName: "Lịch Số",
    images: [{ url: `${BASE_URL}/og-image.png`, width: 1200, height: 630, alt: "Lịch Số — Lịch Vạn Niên" }],
    locale: "vi_VN",
    type: "website",
  },
  twitter: {
    card: "summary_large_image",
    title: "Lịch Số — Lịch Vạn Niên Việt Nam",
    description: "Tra cứu lịch âm dương, ngày tốt xấu, giờ hoàng đạo, phong thủy, tiết khí.",
    images: [`${BASE_URL}/og-image.png`],
  },
  other: {
    "fb:app_id": process.env.NEXT_PUBLIC_FACEBOOK_APP_ID || "",
  },
};

export default async function HomePage() {
  // Prefetch on server → eliminates client-side API waterfall → fixes LCP 7.9s
  const initialTodayData = await fetchTodayServer();

  return (
    <>
      {/* JSON-LD: WebSite + SearchAction schema */}
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{
          __html: JSON.stringify({
            "@context": "https://schema.org",
            "@type": "WebSite",
            name: "Lịch Số",
            alternateName: "Lịch Vạn Niên Việt Nam",
            url: BASE_URL,
            description:
              "Tra cứu lịch âm dương, ngày tốt xấu, giờ hoàng đạo, phong thủy, tiết khí.",
            inLanguage: "vi",
            potentialAction: {
              "@type": "SearchAction",
              target: {
                "@type": "EntryPoint",
                urlTemplate: `${BASE_URL}/tra-cuu?q={search_term_string}`,
              },
              "query-input": "required name=search_term_string",
            },
          }),
        }}
      />
      <V2HomeClient initialTodayData={initialTodayData} />
    </>
  );
}
