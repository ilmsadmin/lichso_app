import { Metadata } from "next";
import {
  Landmark,
  Target,
  CalendarDays,
  Compass,
  Leaf,
  ClipboardList,
  MoonStar,
  Wand2,
  BookOpen,
  Smartphone,
  type LucideIcon,
} from "lucide-react";
import { BackgroundLayer } from "@/components/lichso/BackgroundLayer";
import { AppStoreBadges } from "@/components/shared/AppStoreBadges";

export const metadata: Metadata = {
  title: "Giới Thiệu — Lịch Số",
  description:
    "Lịch Số — Ứng dụng Lịch Vạn Niên Việt Nam hiện đại, tra cứu âm dương, phong thuỷ, tiết khí.",
};

const features = [
  {
    icon: CalendarDays,
    iconBg: "rgba(196,120,58,0.12)",
    iconColor: "var(--warm-amber)",
    title: "Lịch Âm Dương chính xác",
    desc: "Thuật toán chuyển đổi Âm-Dương dựa trên công trình nghiên cứu thiên văn của Hồ Ngọc Đức, đảm bảo độ chính xác cao cho giai đoạn 1900–2100.",
  },
  {
    icon: Compass,
    iconBg: "rgba(99,102,241,0.12)",
    iconColor: "#6366f1",
    title: "Phong Thuỷ & Giờ Hoàng Đạo",
    desc: "Tính toán 12 Trực ngày, 28 Sao chiếu mệnh, giờ Hoàng Đạo / Hắc Đạo, hướng xuất hành Tài thần, Hỷ thần.",
  },
  {
    icon: Leaf,
    iconBg: "rgba(16,185,129,0.12)",
    iconColor: "#10b981",
    title: "24 Tiết Khí",
    desc: "Xác định 24 tiết khí theo thuật toán thiên văn (tính toán kinh độ Mặt Trời), hiển thị tiến trình và tiết khí tiếp theo.",
  },
  {
    icon: ClipboardList,
    iconBg: "rgba(59,130,246,0.12)",
    iconColor: "#3b82f6",
    title: "Việc Nên / Không Nên",
    desc: "Gợi ý các hoạt động phù hợp hoặc cần tránh trong ngày, dựa trên Trực, Sao, Can Chi, giúp bạn lên kế hoạch hợp lý.",
  },
  {
    icon: MoonStar,
    iconBg: "rgba(139,92,246,0.12)",
    iconColor: "#8b5cf6",
    title: "Pha Trăng & Can Chi",
    desc: "Hiển thị pha trăng chính xác và Tứ Trụ Can Chi (Năm, Tháng, Ngày, Giờ) kèm Ngũ Hành, Âm Dương.",
  },
  {
    icon: Wand2,
    iconBg: "rgba(168,85,247,0.12)",
    iconColor: "#a855f7",
    title: "Tử Vi giản lược",
    desc: "Tra cứu nhanh thông tin con giáp, ngũ hành nạp âm, tính cách, tam hợp, tương xung theo năm sinh.",
  },
];

const techStack = [
  { label: "Frontend", value: "Next.js 16 · React 19 · TypeScript · Tailwind CSS 4" },
  { label: "Backend", value: "Go 1.24 · Fiber v2 · GORM · Clean Architecture" },
  { label: "Database", value: "PostgreSQL 16 · MongoDB 7 · Redis 7" },
  { label: "Calendar", value: "Hồ Ngọc Đức Algorithm · Astronomical Solar Terms" },
];

export default function GioiThieuPage() {
  return (
    <>
      <BackgroundLayer />
      <div className="relative z-[1] mx-auto max-w-[1180px] px-4 pb-16 sm:px-7">
        {/* Hero */}
        <div className="pt-12 pb-10 text-center">
          <div
            className="mb-5 inline-flex h-16 w-16 items-center justify-center rounded-2xl"
            style={{
              background: "linear-gradient(135deg, var(--warm-amber), var(--warm-gold))",
              boxShadow: "0 6px 24px rgba(196,120,58,0.3)",
            }}
          >
            <Landmark className="h-8 w-8 text-white" />
          </div>
          <h1 className="text-text-dark mb-3 text-4xl font-[var(--font-lora)] font-semibold">
            Lịch Số
          </h1>
          <p className="text-text-soft mx-auto max-w-2xl text-lg leading-relaxed">
            Ứng dụng Lịch Vạn Niên Việt Nam hiện đại — kết hợp giữa thuật toán thiên văn chính xác
            và phong thuỷ truyền thống, phục vụ nhu cầu tra cứu ngày Âm Dương, ngày tốt xấu, tiết
            khí của người Việt.
          </p>
        </div>

        {/* Mission */}
        <div
          className="mb-8 rounded-2xl p-8 backdrop-blur-[14px]"
          style={{
            background: "var(--ls-card-bg-strong)",
            border: "1px solid var(--ls-border-warm)",
            boxShadow: "0 8px 40px var(--ls-shadow-warm)",
          }}
        >
          <div className="flex items-start gap-4">
            <div
              className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl"
              style={{ background: "rgba(196,120,58,0.12)" }}
            >
              <Target className="text-warm-amber h-5 w-5" />
            </div>
            <div>
              <h2 className="text-text-dark mb-3 text-xl font-[var(--font-lora)] font-semibold">
                Sứ mệnh
              </h2>
              <p className="text-text-mid text-sm leading-relaxed">
                Lịch Số ra đời với mong muốn mang đến một công cụ tra cứu lịch Vạn Niên hiện đại,
                chính xác và dễ sử dụng, giúp người Việt kết nối với văn hoá truyền thống trong thời
                đại số. Thay vì phải tra cứu từ nhiều nguồn khác nhau, Lịch Số tập trung tất cả
                thông tin Âm lịch, Can Chi, phong thuỷ, tiết khí vào một giao diện duy nhất — đẹp,
                nhanh, và miễn phí.
              </p>
            </div>
          </div>
        </div>

        {/* Features */}
        <h2 className="text-text-dark mb-5 flex items-center gap-2 text-xl font-[var(--font-lora)]">
          <span
            className="h-5 w-1 rounded-sm"
            style={{
              background: "linear-gradient(to bottom, var(--warm-amber), var(--warm-gold))",
            }}
          />
          Tính năng nổi bật
        </h2>
        <div className="mb-10 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {features.map((f) => (
            <div
              key={f.title}
              className="rounded-2xl p-5 backdrop-blur-[14px] transition-all hover:translate-y-[-2px]"
              style={{
                background: "var(--ls-card-bg)",
                border: "1px solid var(--ls-border-soft)",
              }}
            >
              <div
                className="mb-3 flex h-10 w-10 items-center justify-center rounded-xl"
                style={{ background: f.iconBg }}
              >
                <f.icon className="h-5 w-5" style={{ color: f.iconColor }} />
              </div>
              <h3 className="text-text-dark mb-2 font-[var(--font-lora)] font-medium">{f.title}</h3>
              <p className="text-text-soft text-xs leading-relaxed">{f.desc}</p>
            </div>
          ))}
        </div>

        {/* Tech stack */}
        <h2 className="text-text-dark mb-5 flex items-center gap-2 text-xl font-[var(--font-lora)]">
          <span
            className="h-5 w-1 rounded-sm"
            style={{
              background: "linear-gradient(to bottom, var(--warm-amber), var(--warm-gold))",
            }}
          />
          Công nghệ sử dụng
        </h2>
        <div
          className="mb-10 rounded-2xl p-6 backdrop-blur-[14px]"
          style={{
            background: "var(--ls-card-bg)",
            border: "1px solid var(--ls-border-soft)",
          }}
        >
          <div className="space-y-3">
            {techStack.map((t) => (
              <div
                key={t.label}
                className="flex flex-col gap-1 sm:flex-row sm:items-center sm:gap-4"
              >
                <span className="text-text-soft min-w-[100px] text-xs font-semibold tracking-[2px] uppercase">
                  {t.label}
                </span>
                <span className="text-text-mid text-sm">{t.value}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Download App */}
        <div
          className="mb-8 rounded-2xl p-8 backdrop-blur-[14px]"
          style={{
            background: "var(--ls-card-bg-strong)",
            border: "1px solid var(--ls-border-warm)",
            boxShadow: "0 8px 40px var(--ls-shadow-warm)",
          }}
        >
          <div className="flex flex-col items-center gap-4 sm:flex-row sm:items-start sm:gap-6">
            <div
              className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl"
              style={{
                background: "linear-gradient(135deg, var(--warm-amber), var(--warm-gold))",
                boxShadow: "0 4px 16px rgba(196,120,58,0.25)",
              }}
            >
              <Smartphone className="h-6 w-6 text-white" />
            </div>
            <div className="text-center sm:text-left">
              <h2 className="text-text-dark mb-2 text-xl font-[var(--font-lora)] font-semibold">
                Tải ứng dụng Lịch Số
              </h2>
              <p className="text-text-mid mb-4 text-sm leading-relaxed">
                Tra cứu lịch âm dương, ngày tốt xấu, giờ hoàng đạo mọi lúc mọi nơi ngay trên điện
                thoại. Ứng dụng hoàn toàn miễn phí.
              </p>
              <AppStoreBadges size="md" />
            </div>
          </div>
        </div>

        {/* References */}
        <div
          className="rounded-2xl p-6 backdrop-blur-[14px]"
          style={{
            background: "var(--ls-card-bg)",
            border: "1px solid var(--ls-border-soft)",
          }}
        >
          <h3 className="text-text-dark mb-3 flex items-center gap-2 text-base font-[var(--font-lora)]">
            <BookOpen className="text-warm-amber h-4 w-4" /> Tham khảo
          </h3>
          <ul className="text-text-mid space-y-2 text-sm">
            <li>
              • Thuật toán Âm lịch Việt Nam —{" "}
              <a
                href="https://www.informatik.uni-leipzig.de/~duc/amlich/"
                target="_blank"
                rel="noopener noreferrer"
                className="text-warm-amber hover:underline"
              >
                Ho Ngoc Duc
              </a>
            </li>
            <li>
              • 24 Tiết Khí —{" "}
              <a
                href="https://vi.wikipedia.org/wiki/Ti%E1%BA%BFt_kh%C3%AD"
                target="_blank"
                rel="noopener noreferrer"
                className="text-warm-amber hover:underline"
              >
                Wikipedia
              </a>
            </li>
            <li>• Zplus Base Code — Nền tảng boilerplate Go Fiber + Next.js</li>
          </ul>
        </div>
      </div>
    </>
  );
}
