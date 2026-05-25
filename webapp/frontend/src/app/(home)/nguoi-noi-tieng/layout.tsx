import type { Metadata } from "next";

const BASE_URL = process.env.NEXT_PUBLIC_APP_URL || "https://lichso.vn";

export const metadata: Metadata = {
  title: "Người Nổi Tiếng — Lịch Số",
  description:
    "Tìm hiểu về các danh nhân, nhân vật lịch sử nổi tiếng Việt Nam và thế giới trên Lịch Số.",
  alternates: { canonical: `${BASE_URL}/nguoi-noi-tieng` },
  openGraph: {
    title: "Người Nổi Tiếng — Lịch Số",
    description: "Tìm hiểu về các danh nhân, nhân vật lịch sử nổi tiếng Việt Nam và thế giới.",
    type: "website",
    locale: "vi_VN",
    siteName: "Lịch Số",
    url: `${BASE_URL}/nguoi-noi-tieng`,
  },
  other: {
    "fb:app_id": process.env.NEXT_PUBLIC_FACEBOOK_APP_ID || "",
  },
};

export default function FamousPeopleLayout({ children }: { children: React.ReactNode }) {
  return children;
}
