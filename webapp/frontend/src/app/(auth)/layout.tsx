import Link from "next/link";
import { APP_NAME, ROUTES } from "@/lib/constants";
import { BackgroundLayer } from "@/components/lichso/BackgroundLayer";
import { LogoSeal } from "@/components/lichso/LogoSeal";

export const metadata = {
  title: `Đăng Nhập — ${APP_NAME}`,
};

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="relative flex min-h-screen flex-col items-center justify-center px-4 py-12">
      {/* Shared background from the main site */}
      <BackgroundLayer />

      {/* Logo / Brand */}
      <Link
        href={ROUTES.HOME}
        className="group relative z-10 mb-8 flex flex-col items-center gap-3"
      >
        <LogoSeal className="h-16 w-16" />
        <div className="text-center">
          <h2 className="text-text-dark text-2xl font-[var(--font-lora)] font-semibold tracking-wide">
            {APP_NAME}
          </h2>
          <p className="text-text-soft mt-0.5 text-[11px] tracking-[2px] uppercase">
            Lịch Vạn Niên Việt Nam
          </p>
        </div>
      </Link>

      {/* Auth form content */}
      <div className="relative z-10 flex w-full justify-center">{children}</div>

      {/* Footer link */}
      <div className="relative z-10 mt-8">
        <Link
          href={ROUTES.HOME}
          className="text-text-soft hover:text-warm-amber text-[13px] transition-colors"
        >
          ← Quay về trang chủ
        </Link>
      </div>
    </div>
  );
}
