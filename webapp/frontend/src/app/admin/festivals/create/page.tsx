"use client";

import dynamic from "next/dynamic";
import { useRouter } from "next/navigation";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useCreateFolkFestival } from "@/hooks/useFolkFestivals";
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

export default function CreateFestivalPage() {
  const router = useRouter();
  const createFestival = useCreateFolkFestival();

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => router.push(ROUTES.ADMIN_FESTIVALS)}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Thêm lễ hội dân gian</h1>
          <p className="text-muted-foreground">Thêm lễ hội dân gian mới vào hệ thống.</p>
        </div>
      </div>

      <FolkFestivalForm
        isSubmitting={createFestival.isPending}
        onCancel={() => router.push(ROUTES.ADMIN_FESTIVALS)}
        onSubmit={(data) => {
          createFestival.mutate(data, {
            onSuccess: (response) => {
              if (response.success) {
                router.push(ROUTES.ADMIN_FESTIVALS);
              }
            },
          });
        }}
      />
    </div>
  );
}
