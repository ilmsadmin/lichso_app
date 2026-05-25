"use client";

import { useState, useCallback } from "react";
import dynamic from "next/dynamic";
import Link from "next/link";
import { Plus, Upload } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { SearchInput } from "@/components/shared/SearchInput";
import { Pagination } from "@/components/shared/Pagination";
import { PermissionGate } from "@/components/auth/PermissionGate";
import {
  BulkImportDialog,
  type CsvColumn,
  type ImportResult,
} from "@/components/shared/BulkImportDialog";
import { useEvents, useDeleteEvent, useCreateEvent } from "@/hooks/useEvents";
import { DEFAULT_PAGE_SIZE, ROUTES } from "@/lib/constants";

const EventTable = dynamic(
  () =>
    import("@/components/events/EventTable").then((mod) => ({
      default: mod.EventTable,
    })),
  {
    loading: () => (
      <div className="flex items-center justify-center py-12">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    ),
  }
);

export default function EventsPage() {
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(DEFAULT_PAGE_SIZE);
  const [search, setSearch] = useState("");
  const [eventType, setEventType] = useState<string>("all");
  const [bulkImportOpen, setBulkImportOpen] = useState(false);

  const { data, isLoading } = useEvents({
    page,
    limit,
    search: search || undefined,
    event_type: eventType !== "all" ? eventType : undefined,
  });

  const deleteEvent = useDeleteEvent();
  const createEvent = useCreateEvent();

  const events = data?.data ?? [];
  const meta = data?.meta;

  const handleSearch = useCallback((value: string) => {
    setSearch(value);
    setPage(1);
  }, []);

  const handleLimitChange = useCallback((newLimit: number) => {
    setLimit(newLimit);
    setPage(1);
  }, []);

  // Bulk import CSV columns
  const eventColumns: CsvColumn[] = [
    { key: "title", label: "Tiêu đề", required: true },
    { key: "event_date", label: "Ngày sự kiện", required: true },
    { key: "end_date", label: "Ngày kết thúc" },
    { key: "event_type", label: "Loại sự kiện" },
    { key: "description", label: "Mô tả" },
    { key: "importance", label: "Mức độ quan trọng" },
    { key: "country", label: "Quốc gia" },
    { key: "tags", label: "Tags" },
  ];

  const eventSampleData = [
    {
      title: "Ngày Quốc khánh Việt Nam",
      event_date: "09-02",
      end_date: "",
      event_type: "national_day",
      description: "Ngày tuyên bố Độc lập nước Việt Nam Dân chủ Cộng hòa",
      importance: "high",
      country: "Việt Nam",
      tags: "quốc khánh, lịch sử",
    },
    {
      title: "Ngày Quốc tế Phụ nữ",
      event_date: "03-08",
      end_date: "",
      event_type: "international_day",
      description: "Ngày Quốc tế Phụ nữ do Liên Hợp Quốc công nhận",
      importance: "medium",
      country: "",
      tags: "quốc tế, phụ nữ",
    },
  ];

  const handleBulkImport = async (records: Record<string, string>[]): Promise<ImportResult> => {
    let success = 0;
    let failed = 0;
    const errors: string[] = [];

    for (const record of records) {
      try {
        const tagsArray = record.tags
          ? record.tags
              .split(",")
              .map((t) => t.trim())
              .filter(Boolean)
          : undefined;

        const eventDate = record.event_date ? new Date(record.event_date) : new Date();
        await createEvent.mutateAsync({
          title: record.title,
          event_date: record.event_date,
          event_day: eventDate.getDate(),
          event_month: eventDate.getMonth() + 1,
          event_type:
            (record.event_type as import("@/types/event").EventType) || "historical_event",
          short_description: record.description || undefined,
          importance: (record.importance as import("@/types/event").EventImportance) || undefined,
          country: record.country || undefined,
          tags: tagsArray,
        });
        success++;
      } catch {
        failed++;
        errors.push(`"${record.title?.slice(0, 40)}..." - Lỗi khi tạo`);
      }
    }

    return { total: records.length, success, failed, errors };
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Sự kiện</h1>
          <p className="text-muted-foreground">Quản lý sự kiện lịch sử, kỷ niệm và ngày lễ.</p>
        </div>
        <PermissionGate permission="content.create">
          <div className="flex items-center gap-2">
            <Button size="sm" variant="outline" onClick={() => setBulkImportOpen(true)}>
              <Upload className="mr-2 h-4 w-4" />
              Import CSV
            </Button>
            <Button size="sm" asChild>
              <Link href={`${ROUTES.ADMIN_EVENTS}/create`}>
                <Plus className="mr-2 h-4 w-4" />
                Thêm sự kiện
              </Link>
            </Button>
          </div>
        </PermissionGate>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
        <SearchInput
          value={search}
          onChange={handleSearch}
          placeholder="Tìm kiếm sự kiện..."
          className="w-full sm:max-w-sm"
        />
        <Select
          value={eventType}
          onValueChange={(v) => {
            setEventType(v);
            setPage(1);
          }}
        >
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Loại sự kiện" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            <SelectItem value="historical">Lịch sử</SelectItem>
            <SelectItem value="national_day">Quốc lễ</SelectItem>
            <SelectItem value="international_day">Quốc tế</SelectItem>
            <SelectItem value="anniversary">Kỷ niệm</SelectItem>
            <SelectItem value="memorial">Tưởng niệm</SelectItem>
            <SelectItem value="other">Khác</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <div className="flex flex-col items-center gap-3">
            <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
            <p className="text-muted-foreground text-sm">Đang tải sự kiện...</p>
          </div>
        </div>
      ) : (
        <>
          <EventTable
            events={events}
            onDelete={(id) => deleteEvent.mutate(id)}
            isDeleting={deleteEvent.isPending}
          />

          {meta && (
            <Pagination
              page={meta.page}
              limit={meta.limit}
              total={meta.total}
              totalPages={meta.total_pages}
              onPageChange={setPage}
              onLimitChange={handleLimitChange}
            />
          )}
        </>
      )}

      {/* Bulk Import Dialog */}
      <BulkImportDialog
        open={bulkImportOpen}
        onOpenChange={setBulkImportOpen}
        title="Import sự kiện từ CSV"
        description="Upload file CSV chứa danh sách sự kiện để thêm hàng loạt."
        columns={eventColumns}
        sampleData={eventSampleData}
        onImport={handleBulkImport}
      />
    </div>
  );
}
