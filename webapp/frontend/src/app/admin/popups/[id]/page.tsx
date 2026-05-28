"use client";

import { use } from "react";
import PopupForm from "../PopupForm";
import { usePopup } from "@/hooks/usePopups";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";
import { ROUTES } from "@/lib/constants";

interface EditPopupPageProps {
  params: Promise<{ id: string }>;
}

export default function EditPopupPage({ params }: EditPopupPageProps) {
  const { id } = use(params);
  const router = useRouter();
  const { data: popupData, isLoading } = usePopup(id);

  const popup = popupData?.data;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-24">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    );
  }

  if (!popup) {
    return (
      <div className="flex flex-col items-center justify-center gap-4 py-24">
        <p className="text-muted-foreground">Không tìm thấy popup.</p>
        <Button variant="outline" onClick={() => router.push(ROUTES.ADMIN_POPUPS)}>
          Quay lại danh sách
        </Button>
      </div>
    );
  }

  return <PopupForm popup={popup} isEdit={true} />;
}
