"use client";

import dynamic from "next/dynamic";
import { useRouter } from "next/navigation";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useCreateEvent } from "@/hooks/useEvents";
import { ROUTES } from "@/lib/constants";

const EventForm = dynamic(
  () =>
    import("@/components/events/EventForm").then((mod) => ({
      default: mod.EventForm,
    })),
  {
    loading: () => (
      <div className="flex items-center justify-center py-12">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    ),
  }
);

export default function CreateEventPage() {
  const router = useRouter();
  const createEvent = useCreateEvent();

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => router.push(ROUTES.ADMIN_EVENTS)}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Tạo sự kiện</h1>
          <p className="text-muted-foreground">Thêm sự kiện mới vào hệ thống.</p>
        </div>
      </div>

      <EventForm
        isSubmitting={createEvent.isPending}
        onCancel={() => router.push(ROUTES.ADMIN_EVENTS)}
        onSubmit={(data) => {
          createEvent.mutate(
            { ...data, event_type: data.event_type || "historical_event" },
            {
              onSuccess: (response) => {
                if (response.success) {
                  router.push(ROUTES.ADMIN_EVENTS);
                }
              },
            }
          );
        }}
      />
    </div>
  );
}
