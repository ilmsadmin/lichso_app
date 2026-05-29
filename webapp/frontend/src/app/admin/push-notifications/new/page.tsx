"use client";

import Link from "next/link";
import { ChevronLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { ROUTES } from "@/lib/constants";
import { CampaignForm } from "../CampaignForm";

export default function NewCampaignPage() {
  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" asChild>
          <Link href={ROUTES.ADMIN_PUSH_NOTIFICATIONS}>
            <ChevronLeft className="w-5 h-5" />
          </Link>
        </Button>
        <div>
          <h1 className="text-2xl font-bold">Tạo thông báo mới</h1>
          <p className="text-muted-foreground text-sm">
            Soạn và gửi push notification tới người dùng Android
          </p>
        </div>
      </div>

      <CampaignForm />
    </div>
  );
}
