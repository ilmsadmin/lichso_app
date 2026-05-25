"use client";

import { use } from "react";
import dynamic from "next/dynamic";
import { useRouter } from "next/navigation";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useEvent, useUpdateEvent } from "@/hooks/useEvents";
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

export default function EditEventPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const { data: eventData, isLoading } = useEvent(id);
  const updateEvent = useUpdateEvent(id);

  const event = eventData?.data;

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
        <Button variant="ghost" size="icon" onClick={() => router.push(ROUTES.ADMIN_EVENTS)}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Chỉnh sửa sự kiện</h1>
          <p className="text-muted-foreground">Cập nhật thông tin sự kiện.</p>
        </div>
      </div>

      {event && (
        <EventForm
          event={event}
          isSubmitting={updateEvent.isPending}
          onCancel={() => router.push(ROUTES.ADMIN_EVENTS)}
          onSubmit={(data) => {
            updateEvent.mutate(data, {
              onSuccess: (response) => {
                if (response.success) {
                  router.push(ROUTES.ADMIN_EVENTS);
                }
              },
            });
          }}
        />
      )}
    </div>
  );
}
