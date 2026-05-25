"use client";

import dynamic from "next/dynamic";
import { useRouter } from "next/navigation";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useCreateFamousPerson } from "@/hooks/useFamousPeople";
import { ROUTES } from "@/lib/constants";

const FamousPersonForm = dynamic(
  () =>
    import("@/components/famous-people/FamousPersonForm").then((mod) => ({
      default: mod.FamousPersonForm,
    })),
  {
    loading: () => (
      <div className="flex items-center justify-center py-12">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    ),
  }
);

export default function CreateFamousPersonPage() {
  const router = useRouter();
  const createPerson = useCreateFamousPerson();

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => router.push(ROUTES.ADMIN_FAMOUS_PEOPLE)}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Thêm nhân vật nổi tiếng</h1>
          <p className="text-muted-foreground">Thêm nhân vật nổi tiếng mới vào hệ thống.</p>
        </div>
      </div>

      <FamousPersonForm
        isSubmitting={createPerson.isPending}
        onCancel={() => router.push(ROUTES.ADMIN_FAMOUS_PEOPLE)}
        onSubmit={(data) => {
          createPerson.mutate(data, {
            onSuccess: (response) => {
              if (response.success) {
                router.push(ROUTES.ADMIN_FAMOUS_PEOPLE);
              }
            },
          });
        }}
      />
    </div>
  );
}
