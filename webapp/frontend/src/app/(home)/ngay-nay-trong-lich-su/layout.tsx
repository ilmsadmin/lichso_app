import type { Metadata } from "next";

const BASE_URL = process.env.NEXT_PUBLIC_APP_URL || "https://lichso.vn";

export const metadata: Metadata = {
  title: "Ngày Này Trong Lịch Sử — Lịch Số",
  description:
    "Tìm hiểu các sự kiện lịch sử, sinh nhật danh nhân và lễ hội diễn ra trong ngày hôm nay.",
  alternates: { canonical: `${BASE_URL}/ngay-nay-trong-lich-su` },
  openGraph: {
    title: "Ngày Này Trong Lịch Sử — Lịch Số",
    description:
      "Tìm hiểu các sự kiện lịch sử, sinh nhật danh nhân và lễ hội diễn ra trong ngày hôm nay.",
    type: "website",
    locale: "vi_VN",
    siteName: "Lịch Số",
    url: `${BASE_URL}/ngay-nay-trong-lich-su`,
  },
  other: {
    "fb:app_id": process.env.NEXT_PUBLIC_FACEBOOK_APP_ID || "",
  },
};

export default function TodayInHistoryLayout({ children }: { children: React.ReactNode }) {
  return children;
}
