"use client";

import { useState } from "react";
import Link from "next/link";
import { MoreHorizontal, Pencil, Trash2, Eye } from "lucide-react";
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
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { usePermission } from "@/hooks/usePermission";
import { formatDate, getInitials, getImageUrl } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";
import type { FamousPersonSummary } from "@/types/famousPerson";

interface FamousPersonTableProps {
  people: FamousPersonSummary[];
  onDelete: (id: string) => void;
  isDeleting?: boolean;
}

export function FamousPersonTable({ people, onDelete, isDeleting }: FamousPersonTableProps) {
  const { can } = usePermission();
  const [deletePerson, setDeletePerson] = useState<FamousPersonSummary | null>(null);

  const formatBirthDate = (date: string | null) => {
    if (!date) return "—";
    try {
      return new Intl.DateTimeFormat("vi-VN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
      }).format(new Date(date));
    } catch {
      return date;
    }
  };

  return (
    <>
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-[300px]">Tên</TableHead>
              <TableHead>Quốc tịch</TableHead>
              <TableHead>Ngày sinh</TableHead>
              <TableHead>Danh mục</TableHead>
              <TableHead>Ngày tạo</TableHead>
              <TableHead className="w-[70px]" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {people.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="text-muted-foreground h-24 text-center">
                  Không tìm thấy nhân vật nào.
                </TableCell>
              </TableRow>
            ) : (
              people.map((person) => (
                <TableRow key={person.id}>
                  <TableCell>
                    <Link
                      href={`${ROUTES.ADMIN_FAMOUS_PEOPLE}/${person.id}`}
                      className="flex items-center gap-3 hover:opacity-80 transition-opacity"
                    >
                      <Avatar className="h-10 w-10 shrink-0">
                        {person.image_url && (
                          <AvatarImage src={getImageUrl(person.image_url)} alt={person.name} />
                        )}
                        <AvatarFallback className="bg-primary/10 text-primary text-xs">
                          {getInitials(person.name)}
                        </AvatarFallback>
                      </Avatar>
                      <div className="min-w-0">
                        <p className="truncate text-sm font-medium hover:text-primary transition-colors">
                          {person.name}
                        </p>
                        {person.short_bio && (
                          <p className="text-muted-foreground truncate text-xs">
                            {person.short_bio}
                          </p>
                        )}
                      </div>
                    </Link>
                  </TableCell>
                  <TableCell className="text-sm">
                    <div className="flex items-center gap-1">
                      {person.nationality || "—"}
                      {person.is_vietnamese && (
                        <Badge variant="outline" className="ml-1 text-xs">
                          🇻🇳
                        </Badge>
                      )}
                    </div>
                  </TableCell>
                  <TableCell className="text-muted-foreground text-sm">
                    {formatBirthDate(person.birth_date)}
                  </TableCell>
                  <TableCell>
                    <div className="flex flex-wrap gap-1">
                      {person.category && (
                        <Badge variant="secondary" className="text-xs">
                          {person.category}
                        </Badge>
                      )}
                    </div>
                  </TableCell>
                  <TableCell className="text-muted-foreground text-sm">
                    {formatDate(person.created_at)}
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
                          <Link href={`${ROUTES.ADMIN_FAMOUS_PEOPLE}/${person.id}`}>
                            <Eye className="mr-2 h-4 w-4" />
                            Xem chi tiết
                          </Link>
                        </DropdownMenuItem>
                        {can("content.update") && (
                          <DropdownMenuItem asChild>
                            <Link href={`${ROUTES.ADMIN_FAMOUS_PEOPLE}/${person.id}/edit`}>
                              <Pencil className="mr-2 h-4 w-4" />
                              Chỉnh sửa
                            </Link>
                          </DropdownMenuItem>
                        )}
                        {can("content.delete") && (
                          <>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                              onClick={() => setDeletePerson(person)}
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
        open={!!deletePerson}
        onOpenChange={(open) => !open && setDeletePerson(null)}
        title="Xóa nhân vật"
        description={`Bạn có chắc chắn muốn xóa "${deletePerson?.name}"? Hành động này không thể hoàn tác.`}
        confirmText="Xóa"
        variant="destructive"
        loading={isDeleting}
        onConfirm={() => {
          if (deletePerson) {
            onDelete(deletePerson.id);
            setDeletePerson(null);
          }
        }}
      />
    </>
  );
}
