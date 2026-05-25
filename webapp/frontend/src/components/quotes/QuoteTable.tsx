"use client";

import { useState } from "react";
import Link from "next/link";
import { MoreHorizontal, Pencil, Trash2, Eye, Quote as QuoteIcon } from "lucide-react";
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
import { formatDate, truncate, getInitials, getImageUrl } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";
import type { Quote } from "@/types/quote";

interface QuoteTableProps {
  quotes: Quote[];
  onDelete: (id: string) => void;
  isDeleting?: boolean;
}

export function QuoteTable({ quotes, onDelete, isDeleting }: QuoteTableProps) {
  const { can } = usePermission();
  const [deleteQuote, setDeleteQuote] = useState<Quote | null>(null);

  return (
    <>
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-[450px]">Danh ngôn / Tác giả</TableHead>
              <TableHead>Ngày trong năm</TableHead>
              <TableHead>Trạng thái</TableHead>
              <TableHead>Ngày tạo</TableHead>
              <TableHead className="w-[70px]" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {quotes.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="text-muted-foreground h-24 text-center">
                  Không tìm thấy danh ngôn nào.
                </TableCell>
              </TableRow>
            ) : (
              quotes.map((quote) => (
                <TableRow key={quote.id}>
                  <TableCell>
                    <Link
                      href={`${ROUTES.ADMIN_QUOTES}/${quote.id}`}
                      className="flex items-center gap-3 hover:opacity-80 transition-opacity"
                    >
                      <Avatar className="h-10 w-10 shrink-0">
                        {quote.author_image_url && (
                          <AvatarImage src={getImageUrl(quote.author_image_url)} alt={quote.author} />
                        )}
                        <AvatarFallback className="bg-primary/10 text-primary">
                          <QuoteIcon className="h-4 w-4" />
                        </AvatarFallback>
                      </Avatar>
                      <div className="min-w-0">
                        <p className="text-sm italic leading-snug hover:text-primary transition-colors">
                          &ldquo;{truncate(quote.quote, 70)}&rdquo;
                        </p>
                        {quote.author && (
                          <p className="text-muted-foreground text-xs mt-0.5">— {quote.author}</p>
                        )}
                      </div>
                    </Link>
                  </TableCell>
                  <TableCell className="text-muted-foreground text-sm">
                    {quote.day_of_year && quote.day_of_year > 0 ? `Ngày ${quote.day_of_year}` : "—"}
                  </TableCell>
                  <TableCell>
                    <Badge
                      variant={quote.is_active ? "default" : "secondary"}
                      className={
                        quote.is_active
                          ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400"
                          : "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400"
                      }
                    >
                      {quote.is_active ? "Hoạt động" : "Ẩn"}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-muted-foreground text-sm">
                    {formatDate(quote.created_at)}
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
                          <Link href={`${ROUTES.ADMIN_QUOTES}/${quote.id}`}>
                            <Eye className="mr-2 h-4 w-4" />
                            Xem chi tiết
                          </Link>
                        </DropdownMenuItem>
                        {can("content.update") && (
                          <DropdownMenuItem asChild>
                            <Link href={`${ROUTES.ADMIN_QUOTES}/${quote.id}/edit`}>
                              <Pencil className="mr-2 h-4 w-4" />
                              Chỉnh sửa
                            </Link>
                          </DropdownMenuItem>
                        )}
                        {can("content.delete") && (
                          <>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                              onClick={() => setDeleteQuote(quote)}
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
        open={!!deleteQuote}
        onOpenChange={(open) => !open && setDeleteQuote(null)}
        title="Xóa danh ngôn"
        description={`Bạn có chắc chắn muốn xóa danh ngôn này? Hành động này không thể hoàn tác.`}
        confirmText="Xóa"
        variant="destructive"
        loading={isDeleting}
        onConfirm={() => {
          if (deleteQuote) {
            onDelete(deleteQuote.id);
            setDeleteQuote(null);
          }
        }}
      />
    </>
  );
}
