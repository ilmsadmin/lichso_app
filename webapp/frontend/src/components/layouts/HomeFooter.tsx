import Link from "next/link";
import { ROUTES } from "@/lib/constants";
import { AppStoreBadges } from "@/components/shared/AppStoreBadges";

const footerLinks = {
  tienIch: [
    { label: "Lịch Tháng", href: "/" },
    { label: "Tra Cứu Ngày", href: "/tra-cuu" },
    { label: "Ngày Tốt", href: "/ngay-tot" },
    { label: "Đổi Lịch Âm / Dương", href: "/doi-lich" },
  ],
  phongThuy: [
    { label: "Phong Thủy", href: "/phong-thuy" },
    { label: "Giờ Hoàng Đạo", href: "/gio-hoang-dao" },
    { label: "Hướng Xuất Hành", href: "/huong-xuat-hanh" },
    { label: "Tiết Khí", href: "/tiet-khi" },
  ],
  khac: [
    { label: "Giới Thiệu", href: "/gioi-thieu" },
    { label: "Liên Hệ", href: "/lien-he" },
    { label: "Chính Sách Bảo Mật", href: "/privacy" },
  ],
};

export function HomeFooter() {
  return (
    <footer
      style={{ borderTop: "1px solid var(--ls-border-warm)", background: "var(--ls-card-bg)" }}
    >
      <div className="mx-auto max-w-[1180px] px-4 py-10 sm:px-6 lg:px-7">
        <div className="grid gap-8 md:grid-cols-2 lg:grid-cols-4">
          {/* Brand */}
          <div className="lg:col-span-1">
            <Link href={ROUTES.HOME} className="flex items-center gap-2">
              <span className="text-text-dark text-lg font-[var(--font-lora)] font-semibold tracking-wide">
                Lịch Số
              </span>
            </Link>
            <p className="text-text-soft mt-3 text-sm leading-relaxed">
              Lịch Vạn Niên Việt Nam hiện đại — tra cứu âm dương, ngày tốt, giờ hoàng đạo, phong
              thủy, tiết khí.
            </p>
            <AppStoreBadges className="mt-4" size="sm" direction="col" />
          </div>

          {/* Tiện ích */}
          <div>
            <h3 className="text-text-dark text-sm font-semibold">Tiện Ích</h3>
            <ul className="mt-3 space-y-2">
              {footerLinks.tienIch.map((link) => (
                <li key={link.href}>
                  <Link
                    href={link.href}
                    className="text-text-soft hover:text-warm-amber text-sm transition-colors"
                  >
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          {/* Phong thuỷ */}
          <div>
            <h3 className="text-text-dark text-sm font-semibold">Phong Thủy</h3>
            <ul className="mt-3 space-y-2">
              {footerLinks.phongThuy.map((link) => (
                <li key={link.href}>
                  <Link
                    href={link.href}
                    className="text-text-soft hover:text-warm-amber text-sm transition-colors"
                  >
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          {/* Khác */}
          <div>
            <h3 className="text-text-dark text-sm font-semibold">Khác</h3>
            <ul className="mt-3 space-y-2">
              {footerLinks.khac.map((link) => (
                <li key={link.href}>
                  <Link
                    href={link.href}
                    className="text-text-soft hover:text-warm-amber text-sm transition-colors"
                  >
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        </div>

        {/* Bottom */}
        <div
          className="mt-10 flex flex-col items-center justify-between gap-4 pt-6 sm:flex-row"
          style={{ borderTop: "1px solid var(--ls-border-soft)" }}
        >
          <p className="text-text-muted-ls text-xs">
            &copy; {new Date().getFullYear()} Lịch Số — Lịch Vạn Niên Việt Nam
          </p>
          <p className="text-text-muted-ls text-xs">Xây dựng bằng Next.js, Go Fiber & ❤️</p>
        </div>
      </div>
    </footer>
  );
}
