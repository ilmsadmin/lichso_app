"use client";

import { use } from "react";
import BannerForm from "../BannerForm";
import { useBanner } from "@/hooks/useBanners";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";
import { ROUTES } from "@/lib/constants";

interface EditBannerPageProps {
  params: Promise<{ id: string }>;
}

export default function EditBannerPage({ params }: EditBannerPageProps) {
  const { id } = use(params);
  const router = useRouter();
  const { data: bannerData, isLoading } = useBanner(id);

  const banner = bannerData?.data;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-24">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    );
  }

  if (!banner) {
    return (
      <div className="flex flex-col items-center justify-center gap-4 py-24">
        <p className="text-muted-foreground">Không tìm thấy banner.</p>
        <Button variant="outline" onClick={() => router.push(ROUTES.ADMIN_BANNERS)}>
          Quay lại danh sách
        </Button>
      </div>
    );
  }

  return <BannerForm banner={banner} isEdit={true} />;
}
