"use client";

import { use } from "react";
import dynamic from "next/dynamic";
import { useRouter } from "next/navigation";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useFolkFestival, useUpdateFolkFestival } from "@/hooks/useFolkFestivals";
import { ROUTES } from "@/lib/constants";

const FolkFestivalForm = dynamic(
  () =>
    import("@/components/festivals/FolkFestivalForm").then((mod) => ({
      default: mod.FolkFestivalForm,
    })),
  {
    loading: () => (
      <div className="flex items-center justify-center py-12">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    ),
  }
);

export default function EditFestivalPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const { data: festivalData, isLoading } = useFolkFestival(id);
  const updateFestival = useUpdateFolkFestival(id);

  const festival = festivalData?.data;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => router.push(ROUTES.ADMIN_FESTIVALS)}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Chỉnh sửa lễ hội</h1>
          <p className="text-muted-foreground">Cập nhật thông tin lễ hội dân gian.</p>
        </div>
      </div>

      {festival && (
        <FolkFestivalForm
          festival={festival}
          isSubmitting={updateFestival.isPending}
          onCancel={() => router.push(ROUTES.ADMIN_FESTIVALS)}
          onSubmit={(data) => {
            updateFestival.mutate(data, {
              onSuccess: (response) => {
                if (response.success) {
                  router.push(ROUTES.ADMIN_FESTIVALS);
                }
              },
            });
          }}
        />
      )}
    </div>
  );
}
