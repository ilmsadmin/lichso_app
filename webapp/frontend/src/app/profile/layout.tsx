"use client";

import { AuthGuard } from "@/components/auth/AuthGuard";
import { ProfileHeader } from "@/components/layouts/ProfileHeader";

export default function ProfileLayout({ children }: { children: React.ReactNode }) {
  return (
    <AuthGuard>
      <div className="bg-background flex min-h-screen flex-col">
        <ProfileHeader />
        <main className="flex-1">
          <div className="mx-auto max-w-4xl px-4 py-6 sm:px-6 lg:px-8">{children}</div>
        </main>
      </div>
    </AuthGuard>
  );
}
