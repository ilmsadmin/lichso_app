"use client";

import dynamic from "next/dynamic";
import { useRouter } from "next/navigation";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useCreateQuote } from "@/hooks/useQuotes";
import { ROUTES } from "@/lib/constants";

const QuoteForm = dynamic(
  () =>
    import("@/components/quotes/QuoteForm").then((mod) => ({
      default: mod.QuoteForm,
    })),
  {
    loading: () => (
      <div className="flex items-center justify-center py-12">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    ),
  }
);

export default function CreateQuotePage() {
  const router = useRouter();
  const createQuote = useCreateQuote();

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => router.push(ROUTES.ADMIN_QUOTES)}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Tạo danh ngôn</h1>
          <p className="text-muted-foreground">Thêm danh ngôn mới vào hệ thống.</p>
        </div>
      </div>

      <QuoteForm
        isSubmitting={createQuote.isPending}
        onCancel={() => router.push(ROUTES.ADMIN_QUOTES)}
        onSubmit={(data) => {
          createQuote.mutate(data, {
            onSuccess: (response) => {
              if (response.success) {
                router.push(ROUTES.ADMIN_QUOTES);
              }
            },
          });
        }}
      />
    </div>
  );
}
