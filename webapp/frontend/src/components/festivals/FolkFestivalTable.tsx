"use client";

import { useState } from "react";
import Link from "next/link";
import { MoreHorizontal, Pencil, Trash2, Eye, Sparkles } from "lucide-react";
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
import { formatDate, getImageUrl } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";
import type { FolkFestivalSummary } from "@/types/festival";

interface FolkFestivalTableProps {
  festivals: FolkFestivalSummary[];
  onDelete: (id: string) => void;
  isDeleting?: boolean;
}

const typeLabel: Record<string, string> = {
  folk_festival: "Lễ hội dân gian",
  religion: "Tôn giáo",
  national_holiday: "Quốc lễ",
  seasonal: "Mùa vụ",
  other: "Khác",
};

export function FolkFestivalTable({ festivals, onDelete, isDeleting }: FolkFestivalTableProps) {
  const { can } = usePermission();
  const [deleteFestival, setDeleteFestival] = useState<FolkFestivalSummary | null>(null);

  return (
    <>
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-[300px]">Tên lễ hội</TableHead>
              <TableHead>Loại</TableHead>
              <TableHead>Ngày âm lịch</TableHead>
              <TableHead>Vùng miền</TableHead>
              <TableHead>Ngày tạo</TableHead>
              <TableHead className="w-[70px]" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {festivals.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="text-muted-foreground h-24 text-center">
                  Không tìm thấy lễ hội nào.
                </TableCell>
              </TableRow>
            ) : (
              festivals.map((festival) => (
                <TableRow key={festival.id}>
                  <TableCell>
                    <Link
                      href={`${ROUTES.ADMIN_FESTIVALS}/${festival.id}`}
                      className="flex items-center gap-3 hover:opacity-80 transition-opacity"
                    >
                      <Avatar className="h-10 w-10 shrink-0 rounded-md">
                        {festival.image_url && (
                          <AvatarImage src={getImageUrl(festival.image_url)} alt={festival.name} className="rounded-md object-cover" />
                        )}
                        <AvatarFallback className="bg-primary/10 text-primary rounded-md">
                          <Sparkles className="h-4 w-4" />
                        </AvatarFallback>
                      </Avatar>
                      <div className="min-w-0">
                        <p className="truncate text-sm font-medium hover:text-primary transition-colors">
                          {festival.name}
                        </p>
                        {festival.alternate_name && (
                          <p className="text-muted-foreground truncate text-xs">
                            {festival.alternate_name}
                          </p>
                        )}
                      </div>
                    </Link>
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline" className="text-xs">
                      {typeLabel[festival.festival_type] ?? festival.festival_type}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-muted-foreground text-sm">
                    {(festival.lunar_day ?? 0) > 0
                      ? `${festival.lunar_day}/${festival.lunar_month}`
                      : "—"}
                  </TableCell>
                  <TableCell>
                    <span className="text-sm">{festival.region || "—"}</span>
                  </TableCell>
                  <TableCell className="text-muted-foreground text-sm">
                    {formatDate(festival.created_at)}
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
                          <Link href={`${ROUTES.ADMIN_FESTIVALS}/${festival.id}`}>
                            <Eye className="mr-2 h-4 w-4" />
                            Xem chi tiết
                          </Link>
                        </DropdownMenuItem>
                        {can("content.update") && (
                          <DropdownMenuItem asChild>
                            <Link href={`${ROUTES.ADMIN_FESTIVALS}/${festival.id}/edit`}>
                              <Pencil className="mr-2 h-4 w-4" />
                              Chỉnh sửa
                            </Link>
                          </DropdownMenuItem>
                        )}
                        {can("content.delete") && (
                          <>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                              onClick={() => setDeleteFestival(festival)}
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
        open={!!deleteFestival}
        onOpenChange={(open) => !open && setDeleteFestival(null)}
        title="Xóa lễ hội"
        description={`Bạn có chắc chắn muốn xóa "${deleteFestival?.name}"? Hành động này không thể hoàn tác.`}
        confirmText="Xóa"
        variant="destructive"
        loading={isDeleting}
        onConfirm={() => {
          if (deleteFestival) {
            onDelete(deleteFestival.id);
            setDeleteFestival(null);
          }
        }}
      />
    </>
  );
}
