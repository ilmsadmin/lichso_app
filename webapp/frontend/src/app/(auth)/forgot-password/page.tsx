import { ForgotPasswordForm } from "@/components/auth/ForgotPasswordForm";
import { APP_NAME } from "@/lib/constants";

export const metadata = {
  title: `Quên Mật Khẩu — ${APP_NAME}`,
};

export default function ForgotPasswordPage() {
  return <ForgotPasswordForm />;
}
