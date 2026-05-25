import { V2Header } from "@/components/layouts/V2Header";
import { V2Footer } from "@/components/layouts/V2Footer";
import { AIFloatingButton } from "@/components/ai/AIFloatingButton";

export default function HomeLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-svh flex-col" style={{ background: "var(--v2-bg-primary)" }}>
      <V2Header />
      <main className="flex-1">{children}</main>
      <AIFloatingButton />
      <V2Footer />
    </div>
  );
}
