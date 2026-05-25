"use client";

import { use } from "react";
import dynamic from "next/dynamic";
import { useRouter } from "next/navigation";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useFamousPerson, useUpdateFamousPerson } from "@/hooks/useFamousPeople";
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

export default function EditFamousPersonPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const { data: personData, isLoading } = useFamousPerson(id);
  const updatePerson = useUpdateFamousPerson(id);

  const person = personData?.data;

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
        <Button variant="ghost" size="icon" onClick={() => router.push(ROUTES.ADMIN_FAMOUS_PEOPLE)}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Chỉnh sửa nhân vật</h1>
          <p className="text-muted-foreground">Cập nhật thông tin nhân vật nổi tiếng.</p>
        </div>
      </div>

      {person && (
        <FamousPersonForm
          person={person}
          isSubmitting={updatePerson.isPending}
          onCancel={() => router.push(ROUTES.ADMIN_FAMOUS_PEOPLE)}
          onSubmit={(data) => {
            updatePerson.mutate(data, {
              onSuccess: (response) => {
                if (response.success) {
                  router.push(ROUTES.ADMIN_FAMOUS_PEOPLE);
                }
              },
            });
          }}
        />
      )}
    </div>
  );
}
