import Link from "next/link";
import Image from "next/image";
import { ROUTES } from "@/lib/constants";
import "./V2Footer.css";

const CURRENT_YEAR = 2026;

const footerLinks = {
  features: [
    { label: "Lịch Hôm Nay", href: ROUTES.HOME },
    { label: "Tra cứu ngày", href: "/tra-cuu" },
    { label: "Ngày tốt xấu", href: "/ngay-tot" },
    { label: "Phong Thuỷ", href: "/phong-thuy" },
    { label: "Tử Vi AI", href: "/tu-vi-ai" },
  ],
  discover: [
    { label: "Lịch sử Việt Nam", href: ROUTES.TODAY_IN_HISTORY },
    { label: "Văn hoá dân gian", href: ROUTES.ARTICLES },
    { label: "Nhân vật lịch sử", href: ROUTES.FAMOUS_PEOPLE },
    { label: "Lễ hội truyền thống", href: ROUTES.FESTIVALS },
    { label: "Châm ngôn nổi tiếng", href: ROUTES.QUOTES },
  ],
  support: [
    { label: "Giới thiệu", href: "/gioi-thieu" },
    { label: "Liên hệ", href: "/contact" },
    { label: "Chính sách", href: "/privacy" },
    { label: "Góp ý", href: "/contact" },
  ],
};

export function V2Footer() {
  return (
    <footer
      className="mt-12"
      style={{
        background: "var(--v2-bg-card)",
        borderTop: "1px solid var(--v2-border-primary)",
      }}
    >
      <div className="mx-auto max-w-[1400px] px-4 pt-10 pb-5 sm:px-8">
        {/* Grid */}
        <div
          className="grid gap-8 pb-7 sm:grid-cols-2 lg:grid-cols-4"
          style={{ borderBottom: "1px solid var(--v2-border-light)" }}
        >
          {/* Brand */}
          <div className="sm:col-span-2 lg:col-span-1">
            <div className="mb-3 flex items-center gap-2.5">
              <Image
                src="/logo-v2.png"
                alt="Lịch Số"
                width={36}
                height={36}
                className="rounded-md"
              />
              <span
                className="font-playfair text-lg font-bold"
                style={{ color: "var(--v2-text-accent)" }}
              >
                Lịch Số
              </span>
            </div>
            <p
              className="mb-4 text-[13px] leading-relaxed"
              style={{ color: "var(--v2-text-muted)" }}
            >
              Lịch Vạn Niên Số 1 Việt Nam — Nơi truyền thống gặp gỡ công nghệ.<br />
              Tra cứu lịch âm dương, ngày tốt, phong thuỷ và khám phá văn hoá dân tộc Việt.
            </p>
            {/* Social */}
            <div className="flex gap-1.5">
              {[
                { icon: "fab fa-facebook-f", href: "#" },
                { icon: "fab fa-youtube", href: "#" },
                { icon: "fab fa-tiktok", href: "#" },
                { icon: "fas fa-envelope", href: "mailto:info@lichso.vn" },
              ].map((s) => (
                <a
                  key={s.icon}
                  href={s.href}
                  className="v2-footer-social"
                >
                  <i className={s.icon} />
                </a>
              ))}
            </div>
          </div>

          {/* Features */}
          <div>
            <h4
              className="mb-3.5 text-[13px] font-bold uppercase tracking-wider"
              style={{ color: "var(--v2-text-primary)" }}
            >
              Tính năng
            </h4>
            {footerLinks.features.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                className="v2-footer-link"
              >
                {link.label}
              </Link>
            ))}
          </div>

          {/* Discover */}
          <div>
            <h4
              className="mb-3.5 text-[13px] font-bold uppercase tracking-wider"
              style={{ color: "var(--v2-text-primary)" }}
            >
              Khám phá
            </h4>
            {footerLinks.discover.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                className="v2-footer-link"
              >
                {link.label}
              </Link>
            ))}
          </div>

          {/* Support */}
          <div>
            <h4
              className="mb-3.5 text-[13px] font-bold uppercase tracking-wider"
              style={{ color: "var(--v2-text-primary)" }}
            >
              Hỗ trợ
            </h4>
            {footerLinks.support.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                className="v2-footer-link"
              >
                {link.label}
              </Link>
            ))}
          </div>
        </div>

        {/* Bottom */}
        <div className="flex flex-col items-center justify-between gap-2 pt-5 sm:flex-row">
          <div className="text-[12px]" style={{ color: "var(--v2-text-muted)" }}>
            <span>© {CURRENT_YEAR} Lịch Số by <strong style={{ color: "var(--v2-text-secondary)" }}>Zenix Labs</strong> — Lịch Vạn Niên Việt Nam.</span>
          </div>
          <span className="text-[12px]" style={{ color: "var(--v2-text-muted)" }}>
            Thiết kế với ❤️ cho người Việt
          </span>
        </div>
      </div>
    </footer>
  );
}
