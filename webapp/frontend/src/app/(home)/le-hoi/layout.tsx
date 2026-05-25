import type { Metadata } from "next";

const BASE_URL = process.env.NEXT_PUBLIC_APP_URL || "https://lichso.vn";

export const metadata: Metadata = {
  title: "Lễ Hội Dân Gian — Lịch Số",
  description:
    "Khám phá các lễ hội dân gian, phong tục truyền thống Việt Nam qua từng mùa trên Lịch Số.",
  alternates: { canonical: `${BASE_URL}/le-hoi` },
  openGraph: {
    title: "Lễ Hội Dân Gian — Lịch Số",
    description: "Khám phá các lễ hội dân gian, phong tục truyền thống Việt Nam qua từng mùa.",
    type: "website",
    locale: "vi_VN",
    siteName: "Lịch Số",
    url: `${BASE_URL}/le-hoi`,
  },
  other: {
    "fb:app_id": process.env.NEXT_PUBLIC_FACEBOOK_APP_ID || "",
  },
};

export default function FestivalsLayout({ children }: { children: React.ReactNode }) {
  return children;
}
