import type { Metadata } from "next";

const BASE_URL = process.env.NEXT_PUBLIC_APP_URL || "https://lichso.vn";

export const metadata: Metadata = {
  title: "Bài Viết — Lịch Số",
  description:
    "Khám phá các bài viết về lịch sử, văn hóa, phong thủy và truyền thống Việt Nam trên Lịch Số.",
  alternates: { canonical: `${BASE_URL}/bai-viet` },
  openGraph: {
    title: "Bài Viết — Lịch Số",
    description: "Khám phá các bài viết về lịch sử, văn hóa, phong thủy và truyền thống Việt Nam.",
    type: "website",
    locale: "vi_VN",
    siteName: "Lịch Số",
    url: `${BASE_URL}/bai-viet`,
  },
  other: {
    "fb:app_id": process.env.NEXT_PUBLIC_FACEBOOK_APP_ID || "",
  },
};

export default function ArticlesLayout({ children }: { children: React.ReactNode }) {
  return children;
}
