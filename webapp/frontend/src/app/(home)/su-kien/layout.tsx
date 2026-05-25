import type { Metadata } from "next";

const BASE_URL = process.env.NEXT_PUBLIC_APP_URL || "https://lichso.vn";

export const metadata: Metadata = {
  title: "Sự Kiện Lịch Sử — Lịch Số",
  description: "Khám phá các sự kiện lịch sử quan trọng, ngày lễ quốc gia và quốc tế trên Lịch Số.",
  alternates: { canonical: `${BASE_URL}/su-kien` },
  openGraph: {
    title: "Sự Kiện Lịch Sử — Lịch Số",
    description: "Khám phá các sự kiện lịch sử quan trọng, ngày lễ quốc gia và quốc tế.",
    type: "website",
    locale: "vi_VN",
    siteName: "Lịch Số",
    url: `${BASE_URL}/su-kien`,
  },
  other: {
    "fb:app_id": process.env.NEXT_PUBLIC_FACEBOOK_APP_ID || "",
  },
};

export default function EventsLayout({ children }: { children: React.ReactNode }) {
  return children;
}
