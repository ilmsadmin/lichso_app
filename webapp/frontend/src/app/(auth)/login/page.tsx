import { LoginForm } from "@/components/auth/LoginForm";
import { APP_NAME } from "@/lib/constants";

export const metadata = {
  title: `Đăng Nhập — ${APP_NAME}`,
};

export default function LoginPage() {
  return <LoginForm />;
}
