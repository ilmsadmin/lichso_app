"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { 
  CloudSun, 
  Lightbulb, 
  Quote, 
  Bell, 
  ArrowRight, 
  ChevronLeft, 
  ChevronRight, 
  CheckCircle2, 
  XCircle, 
  MinusCircle,
  Menu,
  Facebook,
  Youtube,
  Apple,
  Play
} from "lucide-react";

export default function MockupPage() {
  const [currentDate, setCurrentDate] = useState({
    solarDay: 18,
    solarMonth: 5,
    solarYear: 2026,
    dayOfWeek: "Thứ Bảy",
    lunarDay: 11,
    lunarMonth: "Tháng Tư",
    lunarYear: "Bính Ngọ",
    canChi: "Mậu Thân",
    tietKhi: "Tiểu Mãn",
    menhNgay: "Đại Trạch Thổ"
  });

  return (
    <div style={{ background: "var(--v2-bg-primary)" }} className="min-h-screen relative text-[var(--v2-text-primary)] font-body antialiased overflow-x-hidden">
      
      {/* Decorative Traditional Paper Overlay */}
      <div 
        className="pointer-events-none fixed inset-0 opacity-[0.03] z-[999]"
        style={{
          backgroundImage: `url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E")`
        }}
      />

      {/* Floating Cloud Background Ornaments */}
      <div className="pointer-events-none absolute left-[3%] top-[12%] opacity-15 hidden lg:block animate-pulse duration-10000">
        <img src="/assets-new/chatgpt_image_12_09_07_30_thg_5_2026_2_s_a.png" className="max-w-[180px]" alt="Ornament Cloud" />
      </div>
      <div className="pointer-events-none absolute right-[3%] top-[45%] opacity-15 hidden lg:block animate-pulse duration-10000" style={{ animationDelay: "2s" }}>
        <img src="/assets-new/chatgpt_image_12_09_07_30_thg_5_2026_3_s_a.png" className="max-w-[200px]" alt="Ornament Cloud" />
      </div>

      {/* ════════════════ HEADER / NAV ════════════════ */}
      <header className="sticky top-0 z-50 w-full border-b border-[var(--v2-border-primary)] bg-[#1A0F0A]/95 backdrop-blur-md">
        <div className="mx-auto flex max-w-[1400px] items-center justify-between px-6 py-4 sm:px-8 md:px-12">
          {/* Logo */}
          <Link href="#" className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-[var(--v2-bg-accent)]">
              <span className="text-xl text-white font-bold">L</span>
            </div>
            <span className="font-playfair text-2xl font-bold text-white tracking-wide">Lịch Số</span>
          </Link>

          {/* Nav Links */}
          <nav className="hidden items-center gap-8 md:flex">
            <Link href="#" className="text-sm font-semibold text-white hover:text-[var(--v2-bg-gold)] transition-colors relative after:absolute after:bottom-[-4px] after:left-0 after:h-[2px] after:w-full after:bg-[var(--v2-bg-gold)] after:scale-x-100 after:transition-transform">
              Trang chủ
            </Link>
            <Link href="#" className="text-sm font-medium text-white/70 hover:text-[var(--v2-bg-gold)] transition-colors">
              Lịch ngày
            </Link>
            <Link href="#" className="text-sm font-medium text-white/70 hover:text-[var(--v2-bg-gold)] transition-colors">
              Lịch tháng
            </Link>
            <Link href="#" className="text-sm font-medium text-white/70 hover:text-[var(--v2-bg-gold)] transition-colors">
              Quiz
            </Link>
            <Link href="#" className="text-sm font-medium text-white/70 hover:text-[var(--v2-bg-gold)] transition-colors">
              Tiện ích
            </Link>
            <Link href="#" className="text-sm font-medium text-white/70 hover:text-[var(--v2-bg-gold)] transition-colors">
              Tải app
            </Link>
          </nav>

          {/* CTA Button */}
          <Link 
            href="#lich-ngay" 
            className="hidden items-center gap-2 rounded-full px-6 py-2.5 text-sm font-bold text-[#1A0F0A] shadow-lg transition-all hover:scale-105 md:flex"
            style={{
              background: "linear-gradient(135deg, #E6C57A 0%, #C8A84E 50%, #B2923E 100%)",
            }}
          >
            <span>Mở Lịch Ngay</span>
            <ArrowRight className="h-3.5 w-3.5" />
          </Link>

          {/* Mobile Menu Icon */}
          <button className="md:hidden text-white hover:text-[var(--v2-bg-gold)]">
            <Menu className="h-6 w-6" />
          </button>
        </div>
      </header>

      {/* ════════════════ HERO SECTION ════════════════ */}
      <section className="relative px-6 py-12 sm:px-8 md:px-12 lg:py-20">
        <div className="mx-auto grid max-w-[1400px] gap-12 lg:grid-cols-[1.1fr_1fr]">
          
          {/* Left Column */}
          <div className="flex flex-col justify-center space-y-8 relative z-10">
            {/* Badge */}
            <div 
              className="self-start rounded-full border px-4 py-1.5 text-xs font-bold tracking-widest uppercase"
              style={{
                borderColor: "rgba(200, 168, 78, 0.3)",
                background: "var(--v2-bg-gold-soft)",
                color: "var(--v2-text-gold)",
              }}
            >
              ◆ Chuẩn Lịch Việt
            </div>

            {/* Heading & Subtext */}
            <div className="space-y-5">
              <h1 className="font-playfair text-4xl font-bold leading-[1.15] text-[var(--v2-text-primary)] sm:text-5xl md:text-6xl">
                Tra cứu lịch Việt <br />
                <span className="text-[var(--v2-bg-accent)] font-semibold">mỗi ngày</span>
              </h1>
              <p className="font-lora text-lg leading-relaxed text-[var(--v2-text-secondary)] max-w-xl">
                Khám phá âm lịch, tiết khí, ngày cát hung, giờ hoàng đạo và các phong tục tập quán, tín ngưỡng thờ tự dân gian Việt Nam chuẩn xác và trọn vẹn nhất.
              </p>
            </div>

            {/* Feature Mini Columns */}
            <div className="grid grid-cols-3 gap-4 pt-6 border-t border-[var(--v2-border-primary)]">
              <div className="flex flex-col items-center text-center p-3 rounded-xl hover:bg-[var(--v2-bg-gold-soft)] transition-colors duration-300">
                <img src="/assets-new/chatgpt_image_11_55_46_30_thg_5_2026_6_s_a.png" className="w-12 h-12 object-contain mb-3" alt="Chuẩn xác" />
                <h3 className="text-xs font-bold text-[var(--v2-text-primary)]">Chuẩn xác</h3>
                <p className="text-[10px] text-[var(--v2-text-muted)] mt-1">Dữ liệu tin cậy</p>
              </div>
              <div className="flex flex-col items-center text-center p-3 rounded-xl hover:bg-[var(--v2-bg-gold-soft)] transition-colors duration-300">
                <img src="/assets-new/chatgpt_image_11_55_46_30_thg_5_2026_7_s_a.png" className="w-12 h-12 object-contain mb-3" alt="Thân thiện" />
                <h3 className="text-xs font-bold text-[var(--v2-text-primary)]">Thân thiện</h3>
                <p className="text-[10px] text-[var(--v2-text-muted)] mt-1">Dễ xem, dễ dùng</p>
              </div>
              <div className="flex flex-col items-center text-center p-3 rounded-xl hover:bg-[var(--v2-bg-gold-soft)] transition-colors duration-300">
                <img src="/assets-new/chatgpt_image_11_55_47_30_thg_5_2026_8_s_a.png" className="w-12 h-12 object-contain mb-3" alt="Bản sắc" />
                <h3 className="text-xs font-bold text-[var(--v2-text-primary)]">Đậm bản sắc</h3>
                <p className="text-[10px] text-[var(--v2-text-muted)] mt-1">Văn hóa Việt</p>
              </div>
            </div>
          </div>

          {/* Right Column (Widget with Pagoda Illustration behind it) */}
          <div id="lich-ngay" className="relative flex items-center justify-center min-h-[480px]">
            {/* Pagoda Asset (Asset 26 - vertical) */}
            <div className="absolute -right-16 -top-24 z-0 h-[112%] w-[112%] opacity-90 pointer-events-none">
              <img src="/assets-new/chatgpt_image_12_20_25_30_thg_5_2026_s_a.png" className="w-full h-full object-contain" alt="Traditional Pagoda Artwork" />
            </div>

            {/* Daily Calendar Card Widget */}
            <div className="relative z-10 w-full max-w-[440px] overflow-hidden rounded-[28px] border-4 border-[var(--v2-border-gold)] bg-white shadow-2xl transition-all hover:shadow-[0_0_40px_rgba(200,168,78,0.25)] duration-500">
              {/* Header */}
              <div className="bg-[var(--v2-bg-accent)] px-6 py-4 text-center border-b-2 border-[var(--v2-border-gold)]">
                <span className="font-playfair text-sm font-semibold tracking-wider text-white uppercase">Lịch Ngày Hiện Tại</span>
              </div>

              {/* Main Content */}
              <div className="p-6 sm:p-8">
                <div className="grid grid-cols-2 gap-4 divide-x divide-[var(--v2-border-primary)] text-center">
                  
                  {/* Solar Calendar */}
                  <div className="flex flex-col items-center justify-center">
                    <span className="text-[10px] font-bold tracking-wider text-[var(--v2-text-muted)] uppercase">DƯƠNG LỊCH</span>
                    <span className="font-playfair text-7xl font-bold text-[var(--v2-bg-accent)] my-3">{currentDate.solarDay}</span>
                    <span className="text-sm font-semibold text-[var(--v2-text-primary)]">{currentDate.dayOfWeek}</span>
                    <span className="text-[11px] text-[var(--v2-text-muted)] mt-1">{currentDate.solarDay}/{currentDate.solarMonth}/{currentDate.solarYear} (GMT+7)</span>
                  </div>

                  {/* Lunar Calendar */}
                  <div className="flex flex-col items-center justify-center">
                    <span className="text-[10px] font-bold tracking-wider text-[var(--v2-text-muted)] uppercase">ÂM LỊCH</span>
                    <span className="font-playfair text-7xl font-bold text-[#5C4A3E] my-3">{currentDate.lunarDay}</span>
                    <span className="text-sm font-semibold text-[#5C4A3E]">{currentDate.lunarMonth}</span>
                    <span className="text-[11px] text-[var(--v2-text-muted)] mt-1">{currentDate.lunarYear} - Năm {currentDate.lunarYear}</span>
                  </div>
                </div>

                {/* Astrology Details Row */}
                <div className="mt-8 grid grid-cols-3 gap-3 border-t border-b border-[var(--v2-border-primary)] py-4 text-center">
                  <div className="bg-[var(--v2-bg-hover)]/60 rounded-xl p-2.5">
                    <p className="text-[9px] font-semibold text-[var(--v2-text-muted)] uppercase">CAN CHI NGÀY</p>
                    <p className="text-xs font-bold text-[var(--v2-text-primary)] mt-1">{currentDate.canChi}</p>
                  </div>
                  <div className="bg-[var(--v2-bg-hover)]/60 rounded-xl p-2.5">
                    <p className="text-[9px] font-semibold text-[var(--v2-text-muted)] uppercase">TIẾT KHÍ</p>
                    <p className="text-xs font-bold text-[var(--v2-text-primary)] mt-1">{currentDate.tietKhi}</p>
                  </div>
                  <div className="bg-[var(--v2-bg-hover)]/60 rounded-xl p-2.5">
                    <p className="text-[9px] font-semibold text-[var(--v2-text-muted)] uppercase">MỆNH NGÀY</p>
                    <p className="text-xs font-bold text-[var(--v2-text-primary)] mt-1">{currentDate.menhNgay}</p>
                  </div>
                </div>

                {/* Hours & Day Quality */}
                <div className="mt-6 space-y-4">
                  <div>
                    <h4 className="text-[10px] font-bold tracking-wider text-[var(--v2-text-muted)] uppercase mb-2">◆ Giờ Hoàng Đạo</h4>
                    <div className="flex flex-wrap gap-1.5">
                      {["Tý (23-01)", "Sửu (01-03)", "Thìn (07-09)", "Tỵ (09-11)", "Mùi (13-15)", "Tuất (19-21)"].map((time) => (
                        <span key={time} className="rounded bg-emerald-50 px-2 py-1 text-[11px] font-semibold text-emerald-800 border border-emerald-100">
                          {time}
                        </span>
                      ))}
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-1.5">
                      <h4 className="text-[10px] font-bold tracking-wider text-emerald-700 uppercase">✓ NGÀY TỐT</h4>
                      <ul className="text-[11px] text-[var(--v2-text-secondary)] space-y-1">
                        <li className="flex items-center gap-1.5"><CheckCircle2 className="h-3 w-3 text-emerald-600" /> Hoàng đạo</li>
                        <li className="flex items-center gap-1.5"><CheckCircle2 className="h-3 w-3 text-emerald-600" /> Thiên quý</li>
                        <li className="flex items-center gap-1.5"><CheckCircle2 className="h-3 w-3 text-emerald-600" /> Kim đường</li>
                      </ul>
                    </div>
                    <div className="space-y-1.5">
                      <h4 className="text-[10px] font-bold tracking-wider text-rose-700 uppercase">✗ NGÀY XẤU</h4>
                      <ul className="text-[11px] text-[var(--v2-text-secondary)] space-y-1">
                        <li className="flex items-center gap-1.5"><XCircle className="h-3 w-3 text-rose-600" /> Hắc đạo</li>
                        <li className="flex items-center gap-1.5"><XCircle className="h-3 w-3 text-rose-600" /> Thiên hình</li>
                        <li className="flex items-center gap-1.5"><XCircle className="h-3 w-3 text-rose-600" /> Kiếp sát</li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ════════════════ DAILY INSIGHTS ROW ════════════════ */}
      <section className="mx-auto max-w-[1400px] px-6 py-6 sm:px-8 md:px-12">
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {/* Card 1: Weather */}
          <div className="flex flex-col justify-between rounded-2xl border border-[var(--v2-border-primary)] bg-white p-5 shadow-sm hover:border-[var(--v2-border-gold)]/50 transition-all duration-300">
            <div className="flex items-center justify-between">
              <span className="text-[10px] font-bold tracking-wider text-[var(--v2-text-muted)] uppercase">THỜI TIẾT HÔM NAY</span>
              <CloudSun className="h-5 w-5 text-[var(--v2-border-gold)]" />
            </div>
            <div className="my-4 flex items-center justify-between">
              <div>
                <p className="text-sm font-semibold text-[var(--v2-text-primary)]">Hà Nội</p>
                <p className="text-[11px] text-[var(--v2-text-muted)] mt-0.5">Nhiều mây</p>
              </div>
              <div className="text-right">
                <p className="text-2xl font-bold text-[var(--v2-bg-accent)]">28°C</p>
                <p className="text-[10px] text-[var(--v2-text-muted)]">26° - 32°</p>
              </div>
            </div>
            <div className="border-t border-[var(--v2-border-primary)]/60 pt-3 text-[11px] text-[var(--v2-text-secondary)] flex justify-between">
              <span>Độ ẩm 74%</span>
              <span>Gió nhẹ</span>
            </div>
          </div>

          {/* Card 2: Suggestion */}
          <div className="flex flex-col justify-between rounded-2xl border border-[var(--v2-border-primary)] bg-white p-5 shadow-sm hover:border-[var(--v2-border-gold)]/50 transition-all duration-300">
            <div className="flex items-center justify-between">
              <span className="text-[10px] font-bold tracking-wider text-[var(--v2-text-muted)] uppercase">GỢI Ý HÔM NAY</span>
              <Lightbulb className="h-5 w-5 text-[var(--v2-border-gold)]" />
            </div>
            <p className="my-4 font-serif text-xs leading-relaxed text-[var(--v2-text-secondary)]">
              "Ngày cát lợi, thích hợp cho việc cầu tài lộc, khai trương cửa hàng, ký kết hợp đồng thương mại và xuất hành hướng Tây Nam."
            </p>
            <div className="border-t border-[var(--v2-border-primary)]/60 pt-3 text-[11px] text-[var(--v2-text-muted)]">
              <span>Cát tinh: Thiên Hỷ, Nguyệt Tài</span>
            </div>
          </div>

          {/* Card 3: Quote */}
          <div className="flex flex-col justify-between rounded-2xl border border-[var(--v2-border-primary)] bg-white p-5 shadow-sm hover:border-[var(--v2-border-gold)]/50 transition-all duration-300">
            <div className="flex items-center justify-between">
              <span className="text-[10px] font-bold tracking-wider text-[var(--v2-text-muted)] uppercase">CÂU DANH NGÔN</span>
              <Quote className="h-5 w-5 text-[var(--v2-border-gold)]" />
            </div>
            <p className="my-4 font-serif text-xs italic leading-relaxed text-[var(--v2-text-secondary)]">
              "Hiểu quá khứ để trân trọng hiện tại, hiểu hiện tại để kiến tạo một tương lai rạng ngời."
            </p>
            <div className="border-t border-[var(--v2-border-primary)]/60 pt-3 text-[11px] text-[var(--v2-text-muted)] text-right">
              <span>— Khuyết danh</span>
            </div>
          </div>

          {/* Card 4: Reminder */}
          <div className="flex flex-col justify-between rounded-2xl border border-[var(--v2-border-primary)] bg-white p-5 shadow-sm hover:border-[var(--v2-border-gold)]/50 transition-all duration-300">
            <div className="flex items-center justify-between">
              <span className="text-[10px] font-bold tracking-wider text-[var(--v2-text-muted)] uppercase">NHẮC NHỞ</span>
              <Bell className="h-5 w-5 text-[var(--v2-bg-accent)]" />
            </div>
            <div className="my-4">
              <p className="text-xs font-bold text-[var(--v2-bg-accent)]">Mai là Ngày Rằm (15/04 ÂL)</p>
              <p className="text-[11px] text-[var(--v2-text-secondary)] mt-1 leading-normal">
                Chuẩn bị sớ lễ vật dâng hương Phật, cầu gia đạo bình an, làm việc thiện tích đức.
              </p>
            </div>
            <div className="border-t border-[var(--v2-border-primary)]/60 pt-3 text-[11px] text-[var(--v2-text-secondary)] flex justify-between">
              <span>Đại lễ Phật Đản</span>
              <Link href="#" className="text-[var(--v2-bg-accent)] font-semibold hover:underline">
                Xem lễ văn
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* ════════════════ MONTHLY CALENDAR SECTION ════════════════ */}
      <section className="mx-auto max-w-[1400px] px-6 py-12 sm:px-8 md:px-12">
        <div className="grid gap-8 lg:grid-cols-[1.1fr_1fr]">
          
          {/* Monthly Calendar Grid */}
          <div className="rounded-2xl border border-[var(--v2-border-primary)] bg-white p-6 shadow-sm">
            {/* Calendar header */}
            <div className="flex items-center justify-between mb-6">
              <button className="h-9 w-9 flex items-center justify-center rounded-full border border-[var(--v2-border-primary)] hover:bg-[var(--v2-bg-hover)] text-[var(--v2-text-secondary)] transition-colors">
                <ChevronLeft className="h-4 w-4" />
              </button>
              <h2 className="font-playfair text-xl font-bold text-[var(--v2-text-primary)]">
                LỊCH THÁNG 5/2026
              </h2>
              <button className="h-9 w-9 flex items-center justify-center rounded-full border border-[var(--v2-border-primary)] hover:bg-[var(--v2-bg-hover)] text-[var(--v2-text-secondary)] transition-colors">
                <ChevronRight className="h-4 w-4" />
              </button>
            </div>

            {/* Grid Table */}
            <div className="overflow-x-auto">
              <table className="w-full min-w-[500px]">
                <thead>
                  <tr className="border-b border-[var(--v2-border-primary)]/60 text-center">
                    {["T2", "T3", "T4", "T5", "T6", "T7"].map((d) => (
                      <th key={d} className="py-3 text-xs font-bold text-[var(--v2-text-secondary)]">{d}</th>
                    ))}
                    <th className="py-3 text-xs font-bold text-[var(--v2-bg-accent)]">CN</th>
                  </tr>
                </thead>
                <tbody>
                  {/* Calendar mockup rows */}
                  <tr className="text-center">
                    <td className="p-3 opacity-30 text-sm font-semibold">27 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">11/03</span></td>
                    <td className="p-3 opacity-30 text-sm font-semibold">28 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">12</span></td>
                    <td className="p-3 opacity-30 text-sm font-semibold">29 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">13</span></td>
                    <td className="p-3 opacity-30 text-sm font-semibold">30 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">14</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">1 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">15</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">2 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">16</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold text-[var(--v2-bg-accent)]">3 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">17</span></td>
                  </tr>
                  <tr className="text-center">
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">4 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">18</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">5 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">19</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">6 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">20</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">7 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">21</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">8 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">22</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">9 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">23</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold text-[var(--v2-bg-accent)]">10 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">24</span></td>
                  </tr>
                  <tr className="text-center">
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">11 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">25</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">12 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">26</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">13 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">27</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">14 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">28</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">15 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">29</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold font-medium">16 <span className="block text-[9px] font-normal text-[var(--v2-text-gold)] font-bold mt-0.5">01/04</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold text-[var(--v2-bg-accent)]">17 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">02</span></td>
                  </tr>
                  {/* Today Row */}
                  <tr className="text-center">
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">18 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">03</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">19 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">04</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">20 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">05</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">21 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">06</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">22 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">07</span></td>
                    {/* Highlighted Today */}
                    <td className="p-3">
                      <div className="mx-auto flex h-11 w-11 flex-col items-center justify-center rounded-full bg-[var(--v2-bg-accent)] text-white shadow-md border-2 border-[var(--v2-border-gold)]">
                        <div className="text-xs font-bold leading-none">23</div>
                        <div className="text-[8px] leading-none mt-0.5 text-[var(--v2-border-gold)] font-bold">08/04</div>
                      </div>
                    </td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold text-[var(--v2-bg-accent)]">24 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">09</span></td>
                  </tr>
                  <tr className="text-center">
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">25 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">10</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">26 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">11</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">27 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">12</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">28 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">13</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold">29 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">14</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold font-medium">30 <span className="block text-[9px] font-normal text-[var(--v2-text-gold)] font-bold mt-0.5">15/04</span></td>
                    <td className="p-3 hover:bg-[var(--v2-bg-hover)]/60 rounded-lg cursor-pointer text-sm font-semibold text-[var(--v2-bg-accent)]">31 <span className="block text-[9px] font-normal text-[var(--v2-text-muted)] mt-0.5">16</span></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          {/* Monthly Overview details */}
          <div className="rounded-2xl border border-[var(--v2-border-primary)] bg-white p-6 shadow-sm flex flex-col justify-between">
            <div>
              <h3 className="font-playfair text-lg font-bold text-[var(--v2-text-primary)] border-b border-[var(--v2-border-primary)]/60 pb-3 uppercase">
                TổNg Quan Tháng
              </h3>

              {/* Day stats */}
              <div className="grid grid-cols-3 gap-3 my-6 text-center">
                <div className="rounded-xl bg-emerald-50 border border-emerald-100 p-3">
                  <span className="text-xs font-semibold text-emerald-800 flex items-center justify-center gap-1">
                    <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600" /> Ngày Tốt
                  </span>
                  <p className="text-xl font-bold text-emerald-950 mt-1">12 ngày</p>
                </div>
                <div className="rounded-xl bg-rose-50 border border-rose-100 p-3">
                  <span className="text-xs font-semibold text-rose-800 flex items-center justify-center gap-1">
                    <XCircle className="h-3.5 w-3.5 text-rose-600" /> Ngày Xấu
                  </span>
                  <p className="text-xl font-bold text-rose-950 mt-1">7 ngày</p>
                </div>
                <div className="rounded-xl bg-amber-50 border border-amber-100 p-3">
                  <span className="text-xs font-semibold text-amber-800 flex items-center justify-center gap-1">
                    <MinusCircle className="h-3.5 w-3.5 text-amber-600" /> Bình thường
                  </span>
                  <p className="text-xl font-bold text-amber-950 mt-1">12 ngày</p>
                </div>
              </div>

              {/* Lists */}
              <div className="space-y-4">
                <div>
                  <h4 className="text-[10px] font-bold tracking-wider text-[var(--v2-text-muted)] uppercase mb-2">◆ Tiết Khí Trong Tháng</h4>
                  <ul className="text-xs text-[var(--v2-text-secondary)] space-y-1.5 pl-3 border-l-2 border-[var(--v2-border-gold)]/40">
                    <li className="flex justify-between"><span>Lập Hạ:</span> <span className="font-semibold text-[var(--v2-text-primary)]">05/05</span></li>
                    <li className="flex justify-between"><span>Tiểu Mãn:</span> <span className="font-semibold text-[var(--v2-text-primary)]">20/05</span></li>
                  </ul>
                </div>

                <div>
                  <h4 className="text-[10px] font-bold tracking-wider text-[var(--v2-text-muted)] uppercase mb-2">◆ Lễ & Sự Kiện Nổi Bật</h4>
                  <ul className="text-xs text-[var(--v2-text-secondary)] space-y-1.5 pl-3 border-l-2 border-[var(--v2-bg-accent)]/40">
                    <li className="flex justify-between"><span>01/05: Quốc tế Lao động</span> <span className="text-[10px] bg-[var(--v2-bg-gold-soft)] text-[var(--v2-text-gold)] font-semibold px-2 py-0.5 rounded-full">Sự kiện chính</span></li>
                    <li className="flex justify-between"><span>12/05: Ngày của Mẹ</span> <span className="text-[10px] bg-[var(--v2-bg-gold-soft)] text-[var(--v2-text-gold)] font-semibold px-2 py-0.5 rounded-full">Gia đình</span></li>
                    <li className="flex justify-between"><span>19/05: Phật Đản</span> <span className="text-[10px] bg-rose-50 text-[var(--v2-bg-accent)] font-semibold px-2 py-0.5 rounded-full">Đại lễ</span></li>
                  </ul>
                </div>
              </div>
            </div>

            {/* CTA Link */}
            <Link 
              href="#" 
              className="text-[#1A0F0A] text-center rounded-full py-3 text-xs font-bold shadow hover:scale-[1.02] transition-transform mt-6 block"
              style={{
                background: "linear-gradient(135deg, #E6C57A 0%, #C8A84E 50%, #B2923E 100%)",
              }}
            >
              <span>Xem Chi Tiết Tháng</span>
              <ArrowRight className="h-3 w-3 inline-block ml-1.5" />
            </Link>
          </div>
        </div>
      </section>

      {/* ════════════════ QUIZ SECTION ════════════════ */}
      <section className="mx-auto max-w-[1400px] px-6 py-6 sm:px-8 md:px-12">
        <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-[#5C1520] via-[#851C2A] to-[#5C1520] p-8 text-white shadow-xl md:p-12 border-2 border-[var(--v2-border-gold)]">
          
          {/* Banner background graphic (Asset 21 - aspect 3) */}
          <div className="pointer-events-none absolute inset-0 z-0 opacity-20">
            <img src="/assets-new/chatgpt_image_12_09_08_30_thg_5_2026_4_s_a.png" className="w-full h-full object-cover" alt="Red Pattern Background" />
          </div>

          <div className="relative z-10 grid gap-8 lg:grid-cols-[1fr_1.8fr_1.7fr] items-center">
            {/* Left illustration */}
            <div className="flex justify-center lg:justify-start">
              <img src="/assets-new/chatgpt_image_12_20_15_30_thg_5_2026_s_a.png" className="w-36 h-36 object-contain" alt="Treasure Chest Quiz illustration" />
            </div>

            {/* Middle Info */}
            <div className="text-center lg:text-left space-y-4">
              <span className="inline-block rounded bg-white/10 px-3 py-1 text-[10px] font-bold uppercase tracking-wider text-[var(--v2-text-gold)]">
                THỬ TÀI CÙNG LỊCH SỐ
              </span>
              <h2 className="font-playfair text-3xl font-bold leading-tight md:text-4xl text-[var(--v2-text-gold)]">
                Quiz Lịch Sử & Văn Hóa Việt
              </h2>
              <p className="text-xs text-white/80 leading-relaxed max-w-xl">
                Khám phá chặng đường lịch sử đầy tự hào, phong tục tập quán xưa nay và cuộc đời của các vĩ nhân danh tướng nước Việt thông qua chuỗi câu đố thú vị mỗi ngày.
              </p>
              <div className="flex items-center justify-center lg:justify-start gap-3 mt-4">
                <div className="flex -space-x-1.5">
                  {["A", "B", "C"].map((avatar) => (
                    <span key={avatar} className="inline-block h-6 w-6 rounded-full border border-[var(--v2-bg-accent)] bg-white/20 text-[9px] flex items-center justify-center font-bold text-white">
                      {avatar}
                    </span>
                  ))}
                </div>
                <span className="text-[11px] text-white/60 font-semibold"><span className="text-[var(--v2-text-gold)] font-bold">12,458</span> người đang chơi</span>
              </div>
            </div>

            {/* Right: Leaderboard */}
            <div className="bg-black/25 backdrop-blur-sm rounded-2xl p-5 border border-white/10 space-y-4">
              <h4 className="text-[10px] font-bold tracking-wider text-[var(--v2-text-gold)] uppercase border-b border-white/10 pb-2">HÀNG XẾP HẠNG</h4>
              <ul className="text-[11px] space-y-2.5">
                <li className="flex items-center justify-between"><span className="flex items-center gap-2"><span className="text-[var(--v2-text-gold)] font-bold">1.</span> Kim Long</span> <span className="font-bold text-white/95">12,850đ</span></li>
                <li className="flex items-center justify-between"><span className="flex items-center gap-2"><span className="text-slate-300 font-bold">2.</span> Ngọc Mai</span> <span className="font-bold text-white/95">9,620đ</span></li>
                <li className="flex items-center justify-between"><span className="flex items-center gap-2"><span className="text-amber-600 font-bold">3.</span> Hữu Phúc</span> <span className="font-bold text-white/95">8,430đ</span></li>
              </ul>
              <Link 
                href="#" 
                className="text-[#1A0F0A] text-center rounded-full py-2.5 text-xs font-bold shadow w-full block transition-transform hover:scale-102 mt-4"
                style={{
                  background: "linear-gradient(135deg, #E6C57A 0%, #C8A84E 50%, #B2923E 100%)",
                }}
              >
                Bắt đầu thử thách
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* ════════════════ UTILITIES GRID (KHO TIỆN ÍCH) ════════════════ */}
      <section className="mx-auto max-w-[1400px] px-6 py-12 sm:px-8 md:px-12">
        <div className="text-center space-y-3 mb-10">
          <span className="text-xs font-bold tracking-widest text-[var(--v2-bg-accent)] uppercase">TIỆN ÍCH TOÀN DIỆN</span>
          <h2 className="font-playfair text-3xl font-bold text-[var(--v2-text-primary)]">KHO TIỆN ÍCH</h2>
          <div className="h-1 w-20 bg-[var(--v2-border-gold)] mx-auto rounded"></div>
        </div>

        <div className="grid gap-6 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
          
          {[
            { title: "Xem ngày tốt", desc: "Chọn ngày tốt theo việc cần làm", asset: "chatgpt_image_10_36_29_30_thg_5_2026_1_s_a.png" },
            { title: "Văn khấn", desc: "Tổng hợp văn khấn đầy đủ, chuẩn tâm linh", asset: "chatgpt_image_10_36_29_30_thg_5_2026_2_s_a.png" },
            { title: "Rút quẻ", desc: "Kinh dịch - 64 quẻ giải đáp vận mệnh", asset: "chatgpt_image_10_36_29_30_thg_5_2026_3_s_a.png" },
            { title: "Tử vi AI", desc: "Luận giải tử vi cá nhân bằng AI", asset: "chatgpt_image_10_36_29_30_thg_5_2026_4_s_a.png" },
            { title: "Nhắc ngày lễ", desc: "Ghi nhớ ngày giỗ chạp, lễ sự kiện quan trọng", asset: "chatgpt_image_10_36_29_30_thg_5_2026_5_s_a.png" },
            { title: "Phong tục dân gian", desc: "Khám phá phong tục tín ngưỡng xưa và nay", asset: "chatgpt_image_10_36_29_30_thg_5_2026_6_s_a.png" },
            { title: "Đổi ngày âm dương", desc: "Chuyển đổi lịch âm dương nhanh chóng", asset: "chatgpt_image_10_36_29_30_thg_5_2026_7_s_a.png" },
            { title: "Sự kiện truyền thống", desc: "Sự kiện, lễ hội hội làng khắp cả nước", asset: "chatgpt_image_10_36_29_30_thg_5_2026_8_s_a.png" },
          ].map((item) => (
            <div 
              key={item.title} 
              className="group rounded-2xl border border-[var(--v2-border-primary)] bg-white p-5 shadow-sm hover:border-[var(--v2-border-gold)] hover:shadow-md transition-all duration-300 flex items-center gap-4 cursor-pointer"
            >
              <img src={`/assets-new/${item.asset}`} className="w-16 h-16 object-contain group-hover:scale-105 transition-transform" alt={item.title} />
              <div>
                <h3 className="text-sm font-bold text-[var(--v2-text-primary)]">{item.title}</h3>
                <p className="text-[11px] text-[var(--v2-text-muted)] mt-1">{item.desc}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ════════════════ WHY CHOOSE US (VÌ SAO CHỌN LỊCH SỐ?) ════════════════ */}
      <section className="mx-auto max-w-[1400px] px-6 py-12 sm:px-8 md:px-12 bg-[var(--v2-bg-gold-soft)]/40 rounded-[32px] my-12 border border-[var(--v2-border-primary)]/50">
        <div className="text-center space-y-3 mb-12">
          <span className="text-xs font-bold tracking-widest text-[var(--v2-bg-accent)] uppercase font-semibold">TẠI SAO LÀ CHÚNG TÔI?</span>
          <h2 className="font-playfair text-3xl font-bold text-[var(--v2-text-primary)]">VÌ SAO CHỌN LỊCH SỐ?</h2>
          <div className="h-1 w-20 bg-[var(--v2-border-gold)] mx-auto rounded"></div>
        </div>

        <div className="grid gap-8 md:grid-cols-2 lg:grid-cols-4">
          {[
            { title: "Chuẩn lịch Việt", desc: "Dữ liệu lịch vạn niên và tâm linh chính xác, được tư vấn kiểm chứng bởi chuyên gia đầu ngành.", asset: "chatgpt_image_10_36_29_30_thg_5_2026_9_s_a.png" },
            { title: "Giao diện hiện đại", desc: "Thiết kế tinh tế kết hợp yếu tố truyền thống và hơi thở hiện đại, mang lại trải nghiệm xem lịch mượt mà nhất.", asset: "chatgpt_image_10_36_29_30_thg_5_2026_10_s_a.png" },
            { title: "Nội dung phong phú", desc: "Thư viện tổng hợp thông tin, kiến thức văn hóa, phong tục, danh nhân khổng lồ được biên soạn kỹ lưỡng.", asset: "chatgpt_image_11_55_44_30_thg_5_2026_2_s_a_1_.png" },
            { title: "Nhiều tiện ích", desc: "Sở hữu hệ thống tính năng thông minh phong phú hỗ trợ đời sống tâm linh hàng ngày của người Việt.", asset: "chatgpt_image_11_55_45_30_thg_5_2026_5_s_a.png" },
          ].map((item) => (
            <div key={item.title} className="flex flex-col items-center text-center space-y-4">
              <div className="h-28 w-28 flex items-center justify-center bg-white rounded-full border border-[var(--v2-border-primary)]/80 shadow-sm hover:shadow-md transition-shadow">
                <img src={`/assets-new/${item.asset}`} className="w-20 h-20 object-contain" alt={item.title} />
              </div>
              <h3 className="font-serif text-lg font-bold text-[var(--v2-text-primary)]">{item.title}</h3>
              <p className="text-xs leading-relaxed text-[var(--v2-text-secondary)] max-w-[220px]">
                {item.desc}
              </p>
            </div>
          ))}
        </div>
      </section>

      {/* ════════════════ FOOTER ════════════════ */}
      <footer className="bg-[#1A0F0A] text-white border-t-2 border-[var(--v2-border-gold)] pt-16 pb-8 px-6 sm:px-8 md:px-12">
        <div className="mx-auto max-w-[1400px] grid gap-12 sm:grid-cols-2 lg:grid-cols-5 border-b border-white/10 pb-12">
          
          {/* Logo & About */}
          <div className="space-y-4 lg:col-span-2">
            <Link href="#" className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-[var(--v2-bg-accent)]">
                <span className="text-xl text-white font-bold">L</span>
              </div>
              <span className="font-playfair text-2xl font-bold tracking-wide text-white">Lịch Số</span>
            </Link>
            <p className="text-xs text-white/60 leading-relaxed max-w-sm">
              Ứng dụng và website tra cứu lịch Việt chuẩn xác, tiện ích và đậm đà bản sắc văn hóa dân tộc Việt Nam. Giữ gìn hồn Việt trong kỷ nguyên số.
            </p>
            <div className="flex gap-4 pt-2">
              <Link href="#" className="h-8 w-8 rounded-full bg-white/5 hover:bg-[var(--v2-bg-accent)] flex items-center justify-center text-xs transition-colors">
                <Facebook className="h-4 w-4" />
              </Link>
              <Link href="#" className="h-8 w-8 rounded-full bg-white/5 hover:bg-[var(--v2-bg-accent)] flex items-center justify-center text-xs transition-colors">
                <Youtube className="h-4 w-4" />
              </Link>
            </div>
          </div>

          {/* Links 1 */}
          <div className="space-y-4">
            <h4 className="text-xs font-bold text-[var(--v2-border-gold)] tracking-widest uppercase">SẢN PHẨM</h4>
            <ul className="text-xs text-white/70 space-y-2.5">
              <li><Link href="#" className="hover:text-[var(--v2-border-gold)] transition-colors">Lịch ngày</Link></li>
              <li><Link href="#" className="hover:text-[var(--v2-border-gold)] transition-colors">Lịch tháng</Link></li>
              <li><Link href="#" className="hover:text-[var(--v2-border-gold)] transition-colors">Quiz văn hóa</Link></li>
              <li><Link href="#" className="hover:text-[var(--v2-border-gold)] transition-colors">Kho tiện ích</Link></li>
              <li><Link href="#" className="hover:text-[var(--v2-border-gold)] transition-colors">Tải ứng dụng</Link></li>
            </ul>
          </div>

          {/* Links 2 */}
          <div className="space-y-4">
            <h4 className="text-xs font-bold text-[var(--v2-border-gold)] tracking-widest uppercase">HỖ TRỢ</h4>
            <ul className="text-xs text-white/70 space-y-2.5">
              <li><Link href="#" className="hover:text-[var(--v2-border-gold)] transition-colors">Hướng dẫn sử dụng</Link></li>
              <li><Link href="#" className="hover:text-[var(--v2-border-gold)] transition-colors">Câu hỏi thường gặp</Link></li>
              <li><Link href="#" className="hover:text-[var(--v2-border-gold)] transition-colors">Liên hệ hỗ trợ</Link></li>
              <li><Link href="#" className="hover:text-[var(--v2-border-gold)] transition-colors">Góp ý ứng dụng</Link></li>
            </ul>
          </div>

          {/* Download & QR */}
          <div className="space-y-4">
            <h4 className="text-xs font-bold text-[var(--v2-border-gold)] tracking-widest uppercase font-semibold">TẢI APP LỊCH SỐ</h4>
            <div className="flex flex-col gap-2">
              {/* App store button */}
              <Link href="#" className="flex items-center gap-2 bg-black border border-white/10 rounded-lg p-2 hover:border-[var(--v2-border-gold)]/50 transition-colors">
                <Apple className="h-5 w-5 text-white" />
                <div className="text-left leading-none">
                  <span className="text-[9px] text-white/50 block">Tải về trên</span>
                  <span className="text-[11px] font-bold text-white">App Store</span>
                </div>
              </Link>
              {/* Play Store button */}
              <Link href="#" className="flex items-center gap-2 bg-black border border-white/10 rounded-lg p-2 hover:border-[var(--v2-border-gold)]/50 transition-colors">
                <Play className="h-4 w-4 text-white fill-white" />
                <div className="text-left leading-none">
                  <span className="text-[9px] text-white/50 block">Tải về trên</span>
                  <span className="text-[11px] font-bold text-white">Google Play</span>
                </div>
              </Link>
            </div>
            {/* QR Mockup */}
            <div className="mt-4 flex items-center gap-3">
              <div className="h-16 w-16 bg-white rounded p-1 flex items-center justify-center text-black font-bold text-xs">
                QR
              </div>
              <span className="text-[10px] text-white/50 leading-tight">
                Quét mã QR để <br />tải app nhanh nhất
              </span>
            </div>
          </div>
        </div>

        {/* Footer bottom */}
        <div className="mx-auto max-w-[1400px] pt-8 flex flex-col md:flex-row items-center justify-between gap-4 text-xs text-white/40">
          <span>© 2026 LichSo.vn. Tất cả quyền được bảo lưu.</span>
          <div className="flex gap-6">
            <Link href="#" className="hover:text-white transition-colors">Điều khoản dịch vụ</Link>
            <Link href="#" className="hover:text-white transition-colors">Chính bảo mật</Link>
          </div>
        </div>
      </footer>
    </div>
  );
}
