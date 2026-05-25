import { Suspense } from "react";
import { ResetPasswordForm } from "@/components/auth/ResetPasswordForm";
import { APP_NAME } from "@/lib/constants";

export const metadata = {
  title: `Đặt Lại Mật Khẩu — ${APP_NAME}`,
};

export default function ResetPasswordPage() {
  return (
    <Suspense
      fallback={
        <div className="flex items-center justify-center">
          <div className="border-warm-amber h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
        </div>
      }
    >
      <ResetPasswordForm />
    </Suspense>
  );
}
