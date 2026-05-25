import { RegisterForm } from "@/components/auth/RegisterForm";
import { APP_NAME } from "@/lib/constants";

export const metadata = {
  title: `Đăng Ký — ${APP_NAME}`,
};

export default function RegisterPage() {
  return <RegisterForm />;
}
