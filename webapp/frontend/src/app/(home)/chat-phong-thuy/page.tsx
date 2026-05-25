import type { Metadata } from "next";
import { BackgroundLayer } from "@/components/lichso/BackgroundLayer";
import { AIChatWindow } from "@/components/ai/AIChatWindow";

export const metadata: Metadata = {
  title: "Chat Phong Thủy AI | LichSo.vn",
  description:
    "Tư vấn phong thủy, xem ngày tốt, giải đáp bát tự bằng trí tuệ nhân tạo. Chat trực tiếp với AI phong thủy.",
  keywords: ["chat phong thủy", "tư vấn phong thủy online", "hỏi đáp phong thủy ai"],
};

export default function ChatPhongThuayPage() {
  return (
    <>
      <BackgroundLayer />
      <div className="relative z-[1] mx-auto max-w-[1100px] px-4 pb-8 sm:px-7">
        {/* Header */}
        <div className="pt-8 pb-5">
          <h1 className="text-text-dark text-2xl font-[var(--font-lora)] font-semibold">
            💬 Chat Phong Thủy AI
          </h1>
          <p className="text-text-soft mt-1 text-sm">
            Hỏi đáp trực tiếp với AI về phong thủy, bát tự, xem ngày tốt, văn hóa phương Đông.
          </p>
        </div>

        <AIChatWindow />

        <p className="mt-4 text-center text-xs text-text-soft opacity-60">
          Kết quả tư vấn AI mang tính tham khảo. Không thay thế cho lời khuyên chuyên gia.
        </p>
      </div>
    </>
  );
}
