"use client";

import { useState } from "react";
import Link from "next/link";
import { MoreHorizontal, Pencil, Trash2, Eye, Star } from "lucide-react";
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
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { usePermission } from "@/hooks/usePermission";
import { formatDate, truncate, getImageUrl } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";
import type { ArticleSummary } from "@/types/article";

interface ArticleTableProps {
  articles: ArticleSummary[];
  onDelete: (id: string) => void;
  isDeleting?: boolean;
}

const statusVariant: Record<string, "default" | "secondary" | "destructive"> = {
  published: "default",
  draft: "secondary",
  archived: "destructive",
};

const statusLabel: Record<string, string> = {
  published: "Đã xuất bản",
  draft: "Bản nháp",
  archived: "Lưu trữ",
};

export function ArticleTable({ articles, onDelete, isDeleting }: ArticleTableProps) {
  const { can } = usePermission();
  const [deleteArticle, setDeleteArticle] = useState<ArticleSummary | null>(null);

  return (
    <>
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-[350px]">Tiêu đề</TableHead>
              <TableHead>Danh mục</TableHead>
              <TableHead>Trạng thái</TableHead>
              <TableHead>Lượt xem</TableHead>
              <TableHead>Ngày tạo</TableHead>
              <TableHead className="w-[70px]" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {articles.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="text-muted-foreground h-24 text-center">
                  Không tìm thấy bài viết nào.
                </TableCell>
              </TableRow>
            ) : (
              articles.map((article) => (
                <TableRow key={article.id}>
                  <TableCell>
                    <div className="flex items-center gap-3">
                      {/* Thumbnail */}
                      {article.featured_image ? (
                        <Link
                          href={`${ROUTES.ADMIN_ARTICLES}/${article.id}`}
                          className="bg-muted relative h-14 w-20 shrink-0 overflow-hidden rounded-md border"
                        >
                          {/* eslint-disable-next-line @next/next/no-img-element */}
                          <img
                            src={getImageUrl(article.featured_image)}
                            alt={article.title}
                            className="h-full w-full object-cover"
                          />
                        </Link>
                      ) : (
                        <div className="bg-muted flex h-14 w-20 shrink-0 items-center justify-center rounded-md border">
                          <Eye className="text-muted-foreground/40 h-4 w-4" />
                        </div>
                      )}
                      <div className="min-w-0">
                        {article.is_featured && (
                          <Star className="mr-1 inline h-3.5 w-3.5 text-yellow-500" />
                        )}
                        <Link
                          href={`${ROUTES.ADMIN_ARTICLES}/${article.id}`}
                          className="text-sm font-medium hover:underline"
                        >
                          {truncate(article.title, 60)}
                        </Link>
                        {article.excerpt && (
                          <p className="text-muted-foreground mt-0.5 truncate text-xs">
                            {truncate(article.excerpt, 80)}
                          </p>
                        )}
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>
                    {article.category ? (
                      <Badge variant="outline" className="text-xs">
                        {article.category.name}
                      </Badge>
                    ) : (
                      <span className="text-muted-foreground text-xs">—</span>
                    )}
                  </TableCell>
                  <TableCell>
                    <Badge variant={statusVariant[article.status] ?? "secondary"}>
                      {statusLabel[article.status] ?? article.status}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-muted-foreground text-sm">
                    {article.view_count.toLocaleString()}
                  </TableCell>
                  <TableCell className="text-muted-foreground text-sm">
                    {formatDate(article.created_at)}
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
                          <Link href={`${ROUTES.ADMIN_ARTICLES}/${article.id}`}>
                            <Eye className="mr-2 h-4 w-4" />
                            Xem chi tiết
                          </Link>
                        </DropdownMenuItem>
                        {can("content.update") && (
                          <DropdownMenuItem asChild>
                            <Link href={`${ROUTES.ADMIN_ARTICLES}/${article.id}/edit`}>
                              <Pencil className="mr-2 h-4 w-4" />
                              Chỉnh sửa
                            </Link>
                          </DropdownMenuItem>
                        )}
                        {can("content.delete") && (
                          <>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                              onClick={() => setDeleteArticle(article)}
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

      {/* Delete confirmation */}
      <ConfirmDialog
        open={!!deleteArticle}
        onOpenChange={(open) => !open && setDeleteArticle(null)}
        title="Xóa bài viết"
        description={`Bạn có chắc chắn muốn xóa bài viết "${deleteArticle?.title}"? Hành động này không thể hoàn tác.`}
        confirmText="Xóa"
        variant="destructive"
        loading={isDeleting}
        onConfirm={() => {
          if (deleteArticle) {
            onDelete(deleteArticle.id);
            setDeleteArticle(null);
          }
        }}
      />
    </>
  );
}
