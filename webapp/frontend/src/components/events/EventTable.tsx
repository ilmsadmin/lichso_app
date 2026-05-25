"use client";

import { useState } from "react";
import Link from "next/link";
import { MoreHorizontal, Pencil, Trash2, Eye, Calendar } from "lucide-react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { usePermission } from "@/hooks/usePermission";
import { formatDate, truncate, getImageUrl } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";
import type { EventSummary } from "@/types/event";

interface EventTableProps {
  events: EventSummary[];
  onDelete: (id: string) => void;
  isDeleting?: boolean;
}

const typeLabel: Record<string, string> = {
  historical_event: "Lịch sử",
  national_day: "Quốc lễ",
  world_day: "Quốc tế",
  anniversary: "Kỷ niệm",
  cultural: "Văn hóa",
  military: "Quân sự",
  other: "Khác",
};

const importanceVariant: Record<string, "default" | "secondary" | "destructive"> = {
  high: "destructive",
  medium: "default",
  low: "secondary",
};

const importanceLabel: Record<string, string> = {
  high: "Cao",
  medium: "Trung bình",
  low: "Thấp",
};

export function EventTable({ events, onDelete, isDeleting }: EventTableProps) {
  const { can } = usePermission();
  const [deleteEvent, setDeleteEvent] = useState<EventSummary | null>(null);

  return (
    <>
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-[350px]">Tiêu đề</TableHead>
              <TableHead>Loại</TableHead>
              <TableHead>Ngày sự kiện</TableHead>
              <TableHead>Mức độ</TableHead>
              <TableHead>Ngày tạo</TableHead>
              <TableHead className="w-[70px]" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {events.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="text-muted-foreground h-24 text-center">
                  Không tìm thấy sự kiện nào.
                </TableCell>
              </TableRow>
            ) : (
              events.map((event) => (
                <TableRow key={event.id}>
                  <TableCell>
                    <Link
                      href={`${ROUTES.ADMIN_EVENTS}/${event.id}`}
                      className="flex items-center gap-3 hover:opacity-80 transition-opacity"
                    >
                      <Avatar className="h-10 w-10 shrink-0 rounded-md">
                        {event.image_url && (
                          <AvatarImage src={getImageUrl(event.image_url)} alt={event.title} className="rounded-md object-cover" />
                        )}
                        <AvatarFallback className="bg-primary/10 text-primary rounded-md">
                          <Calendar className="h-4 w-4" />
                        </AvatarFallback>
                      </Avatar>
                      <div className="min-w-0">
                        <p className="truncate text-sm font-medium hover:text-primary transition-colors">
                          {truncate(event.title, 60)}
                        </p>
                        {event.country && (
                          <p className="text-muted-foreground text-xs">{event.country}</p>
                        )}
                      </div>
                    </Link>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline" className="text-xs">
                      {typeLabel[event.event_type] ?? event.event_type}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-muted-foreground text-sm">
                    {event.event_date}
                  </TableCell>
                  <TableCell>
                    <Badge variant={importanceVariant[event.importance] ?? "secondary"}>
                      {importanceLabel[event.importance] ?? event.importance}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-muted-foreground text-sm">
                    {formatDate(event.created_at)}
                  </TableCell>
                  <TableCell>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon" className="h-8 w-8">
                          <MoreHorizontal className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem asChild>
                          <Link href={`${ROUTES.ADMIN_EVENTS}/${event.id}`}>
                            <Eye className="mr-2 h-4 w-4" />
                            Xem chi tiết
                          </Link>
                        </DropdownMenuItem>
                        {can("content.update") && (
                          <DropdownMenuItem asChild>
                            <Link href={`${ROUTES.ADMIN_EVENTS}/${event.id}/edit`}>
                              <Pencil className="mr-2 h-4 w-4" />
                              Chỉnh sửa
                            </Link>
                          </DropdownMenuItem>
                        )}
                        {can("content.delete") && (
                          <>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                              onClick={() => setDeleteEvent(event)}
                              className="text-destructive focus:text-destructive"
                            >
                              <Trash2 className="mr-2 h-4 w-4" />
                              Xóa
                            </DropdownMenuItem>
                          </>
                        )}
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <ConfirmDialog
        open={!!deleteEvent}
        onOpenChange={(open) => !open && setDeleteEvent(null)}
        title="Xóa sự kiện"
        description={`Bạn có chắc chắn muốn xóa "${deleteEvent?.title}"? Hành động này không thể hoàn tác.`}
        confirmText="Xóa"
        variant="destructive"
        loading={isDeleting}
        onConfirm={() => {
          if (deleteEvent) {
            onDelete(deleteEvent.id);
            setDeleteEvent(null);
          }
        }}
      />
    </>
  );
}
