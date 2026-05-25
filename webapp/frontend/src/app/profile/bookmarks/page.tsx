"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { BookmarkIcon, Calendar, Trash2, StickyNote } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { toast } from "sonner";
import Link from "next/link";
import * as bookmarkService from "@/services/bookmarkService";
import type { Bookmark } from "@/types/bookmark";

const COLOR_STYLES: Record<string, string> = {
  amber: "bg-amber-100 text-amber-800 border-amber-200",
  jade: "bg-emerald-100 text-emerald-800 border-emerald-200",
  red: "bg-red-100 text-red-800 border-red-200",
  gold: "bg-yellow-100 text-yellow-800 border-yellow-200",
  blue: "bg-blue-100 text-blue-800 border-blue-200",
  purple: "bg-purple-100 text-purple-800 border-purple-200",
};

function formatDate(dateStr: string) {
  const [year, month, day] = dateStr.split("-");
  return `${day}/${month}/${year}`;
}

export default function BookmarksPage() {
  const queryClient = useQueryClient();
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const { data: bookmarks = [], isLoading, isError } = useQuery<Bookmark[]>({
    queryKey: ["bookmarks"],
    queryFn: () => bookmarkService.getBookmarks(),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => bookmarkService.deleteBookmark(id),
    onMutate: (id) => setDeletingId(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["bookmarks"] });
      toast.success("Đã xóa bookmark");
    },
    onError: () => toast.error("Xóa bookmark thất bại"),
    onSettled: () => setDeletingId(null),
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="flex items-center gap-2 text-3xl font-bold tracking-tight">
          <BookmarkIcon className="h-8 w-8" />
          Bookmarks
        </h1>
        <p className="text-muted-foreground mt-1">Các ngày bạn đã lưu để xem lại</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Danh sách bookmark</CardTitle>
          <CardDescription>
            {isLoading ? "Đang tải..." : `${bookmarks.length} bookmark`}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {/* Loading skeleton */}
          {isLoading && (
            <div className="space-y-3">
              {Array.from({ length: 4 }).map((_, i) => (
                <Skeleton key={i} className="h-16 w-full rounded-lg" />
              ))}
            </div>
          )}

          {/* Error */}
          {isError && (
            <div className="text-muted-foreground py-8 text-center">
              <p>Không thể tải danh sách bookmark. Vui lòng thử lại.</p>
            </div>
          )}

          {/* Empty */}
          {!isLoading && !isError && bookmarks.length === 0 && (
            <div className="text-muted-foreground flex items-center justify-center py-12">
              <div className="text-center">
                <BookmarkIcon className="mx-auto mb-4 h-12 w-12 opacity-50" />
                <p className="text-lg font-medium">Chưa có bookmark</p>
                <p className="mt-1 text-sm">Bookmark các ngày quan trọng để dễ dàng tra cứu sau</p>
                <Button asChild variant="outline" className="mt-4">
                  <Link href="/">Xem lịch hôm nay</Link>
                </Button>
              </div>
            </div>
          )}

          {/* Bookmark list */}
          {!isLoading && bookmarks.length > 0 && (
            <ul className="space-y-3">
              {bookmarks.map((bm) => (
                <li
                  key={bm.id}
                  className="flex items-start justify-between gap-3 rounded-lg border p-4 transition-colors hover:bg-muted/40"
                >
                  <div className="flex min-w-0 items-start gap-3">
                    <div
                      className={`mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-full border text-sm font-bold ${COLOR_STYLES[bm.color] ?? COLOR_STYLES.amber}`}
                    >
                      <Calendar className="h-4 w-4" />
                    </div>
                    <div className="min-w-0">
                      <div className="flex flex-wrap items-center gap-2">
                        <Link
                          href={`/?date=${bm.solar_date}`}
                          className="font-semibold hover:underline"
                        >
                          {bm.title || formatDate(bm.solar_date)}
                        </Link>
                        <Badge variant="outline" className="text-xs">
                          {formatDate(bm.solar_date)}
                        </Badge>
                        {bm.is_recurring && (
                          <Badge variant="secondary" className="text-xs">
                            Hàng năm
                          </Badge>
                        )}
                      </div>
                      {bm.note && (
                        <p className="text-muted-foreground mt-1 flex items-center gap-1 truncate text-sm">
                          <StickyNote className="h-3 w-3 shrink-0" />
                          {bm.note}
                        </p>
                      )}
                    </div>
                  </div>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="text-muted-foreground hover:text-destructive shrink-0"
                    disabled={deletingId === bm.id}
                    onClick={() => deleteMutation.mutate(bm.id)}
                    aria-label="Xóa bookmark"
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
