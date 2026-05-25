import type { Metadata } from "next";
import { BackgroundLayer } from "@/components/lichso/BackgroundLayer";
import { TuViAIWizard } from "@/components/ai/TuViAIWizard";

export const metadata: Metadata = {
  title: "Tử Vi AI - Bát Tự & Phân Tích Sâu | LichSo.vn",
  description:
    "Xem tử vi bát tự chi tiết bằng AI. Phân tích tứ trụ, ngũ hành cân bằng, vận hạn, tình duyên theo ngày tháng năm giờ sinh.",
  keywords: ["tử vi ai", "bát tự", "tứ trụ", "ngũ hành", "xem tử vi online", "phong thủy ai"],
};

export default function TuViAIPage() {
  return (
    <>
      <BackgroundLayer />
      <div className="relative z-[1] px-4 pt-8 pb-20 sm:px-7">
        <TuViAIWizard />
        <p
          style={{
            textAlign: "center",
            fontSize: 11,
            color: "var(--ls-text-muted)",
            opacity: 0.6,
            marginTop: 20,
          }}
        >
          Kết quả tử vi AI mang tính tham khảo. Không thay thế cho lời khuyên chuyên gia phong thủy.
        </p>
      </div>
    </>
  );
}
