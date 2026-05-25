import type { Metadata } from "next";
import ArticlesClient from "./ArticlesClient";

const BASE_URL = process.env.NEXT_PUBLIC_APP_URL || "https://lichso.vn";

export const metadata: Metadata = {
  title: "Bài Viết — Lịch Số",
  description:
    "Khám phá các bài viết về lịch âm dương, phong thủy, tử vi, ngày tốt xấu và văn hóa truyền thống Việt Nam.",
  keywords: ["bài viết lịch số", "phong thủy", "tử vi", "văn hóa việt nam", "âm lịch"],
  alternates: {
    canonical: `${BASE_URL}/bai-viet`,
  },
  openGraph: {
    title: "Bài Viết — Lịch Số",
    description:
      "Khám phá các bài viết về lịch âm dương, phong thủy, tử vi, ngày tốt xấu và văn hóa truyền thống Việt Nam.",
    url: `${BASE_URL}/bai-viet`,
    siteName: "Lịch Số",
    locale: "vi_VN",
    type: "website",
  },
  other: {
    "fb:app_id": process.env.NEXT_PUBLIC_FACEBOOK_APP_ID || "",
  },
};

export default function ArticlesPage() {
  return <ArticlesClient />;
}
