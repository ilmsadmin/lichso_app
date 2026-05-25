import type { Metadata } from "next";

const BASE_URL = process.env.NEXT_PUBLIC_APP_URL || "https://lichso.vn";

export const metadata: Metadata = {
  title: "Câu Nói Nổi Tiếng — Lịch Số",
  description:
    "Tổng hợp những câu nói nổi tiếng, danh ngôn truyền cảm hứng từ các danh nhân Việt Nam và thế giới.",
  alternates: { canonical: `${BASE_URL}/cau-noi-noi-tieng` },
  openGraph: {
    title: "Câu Nói Nổi Tiếng — Lịch Số",
    description:
      "Tổng hợp những câu nói nổi tiếng, danh ngôn truyền cảm hứng từ các danh nhân Việt Nam và thế giới.",
    type: "website",
    locale: "vi_VN",
    siteName: "Lịch Số",
    url: `${BASE_URL}/cau-noi-noi-tieng`,
  },
  other: {
    "fb:app_id": process.env.NEXT_PUBLIC_FACEBOOK_APP_ID || "",
  },
};

export default function QuotesLayout({ children }: { children: React.ReactNode }) {
  return children;
}
