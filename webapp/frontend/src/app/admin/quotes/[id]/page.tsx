"use client";

import { use } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, Pencil, Trash2, Quote as QuoteIcon, Globe, User, Calendar, Hash, Eye, ExternalLink } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { PermissionGate } from "@/components/auth/PermissionGate";
import { useQuote, useDeleteQuote } from "@/hooks/useQuotes";
import { formatDate, getInitials, getImageUrl } from "@/lib/utils";
import { ROUTES } from "@/lib/constants";
import { useState } from "react";

export default function QuoteDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const { data, isLoading } = useQuote(id);
  const deleteQuote = useDeleteQuote();
  const [showDelete, setShowDelete] = useState(false);

  const quote = data?.data;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-24">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    );
  }

  if (!quote) {
    return (
      <div className="flex flex-col items-center justify-center gap-4 py-24">
        <p className="text-muted-foreground">Không tìm thấy danh ngôn.</p>
        <Button variant="outline" onClick={() => router.push(ROUTES.ADMIN_QUOTES)}>
          Quay lại danh sách
        </Button>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => router.push(ROUTES.ADMIN_QUOTES)}>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div>
            <h1 className="text-2xl font-bold tracking-tight">Chi tiết danh ngôn</h1>
            <p className="text-muted-foreground text-sm">Xem và quản lý thông tin danh ngôn</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" asChild>
            <a href={ROUTES.QUOTES} target="_blank" rel="noopener noreferrer">
              <ExternalLink className="mr-2 h-4 w-4" />
              Xem trang công khai
            </a>
          </Button>
          <PermissionGate permission="content.update">
            <Button variant="outline" size="sm" asChild>
              <Link href={`${ROUTES.ADMIN_QUOTES}/${quote.id}/edit`}>
                <Pencil className="mr-2 h-4 w-4" />
                Chỉnh sửa
              </Link>
            </Button>
          </PermissionGate>
          <PermissionGate permission="content.delete">
            <Button
              variant="outline"
              size="sm"
              className="text-destructive hover:text-destructive"
              onClick={() => setShowDelete(true)}
            >
              <Trash2 className="mr-2 h-4 w-4" />
              Xóa
            </Button>
          </PermissionGate>
        </div>
      </div>

      {/* Hero Quote Card */}
      <Card className="overflow-hidden">
        <div className="bg-gradient-to-br from-primary/5 via-primary/10 to-primary/5 p-8">
          <div className="flex items-start gap-6">
            <Avatar className="h-20 w-20 shrink-0 ring-4 ring-background shadow-lg">
              {quote.author_image_url && (
                <AvatarImage src={getImageUrl(quote.author_image_url)} alt={quote.author} />
              )}
              <AvatarFallback className="bg-primary text-primary-foreground text-xl">
                {quote.author ? getInitials(quote.author) : <QuoteIcon className="h-8 w-8" />}
              </AvatarFallback>
            </Avatar>
            <div className="flex-1 min-w-0">
              <blockquote className="text-xl font-medium italic leading-relaxed">
                &ldquo;{quote.quote}&rdquo;
              </blockquote>
              {quote.original_quote && (
                <p className="text-muted-foreground mt-2 text-sm italic">
                  &ldquo;{quote.original_quote}&rdquo;
                </p>
              )}
              <div className="mt-4 flex items-center gap-3">
                <p className="font-semibold">{quote.author || "Khuyết danh"}</p>
                {quote.author_bio && (
                  <span className="text-muted-foreground text-sm">— {quote.author_bio}</span>
                )}
              </div>
              <div className="mt-2 flex items-center gap-2">
                <Badge variant={quote.is_active ? "default" : "secondary"}>
                  {quote.is_active ? "Hoạt động" : "Ẩn"}
                </Badge>
                {quote.day_of_year && quote.day_of_year > 0 && (
                  <Badge variant="outline">Ngày {quote.day_of_year}</Badge>
                )}
              </div>
            </div>
          </div>
        </div>
      </Card>

      <div className="grid gap-6 md:grid-cols-2">
        {/* Author Info Card */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <User className="h-4 w-4 text-primary" />
              Thông tin tác giả
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Tên tác giả</p>
                <p className="text-sm mt-1 font-medium">{quote.author || "—"}</p>
              </div>
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Tiểu sử</p>
                <p className="text-sm mt-1">{quote.author_bio || "—"}</p>
              </div>
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Quốc tịch</p>
                <p className="text-sm mt-1">{quote.author_nationality || "—"}</p>
              </div>
              {(quote.author_birth_year || quote.author_death_year) && (
                <div>
                  <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Năm sinh – mất</p>
                  <p className="text-sm mt-1">
                    {quote.author_birth_year || "?"} – {quote.author_death_year || "?"}
                  </p>
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Metadata Card */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <Globe className="h-4 w-4 text-primary" />
              Thông tin chi tiết
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Ngôn ngữ gốc</p>
                <p className="text-sm mt-1">{quote.original_language || "—"}</p>
              </div>
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Ngày trong năm</p>
                <p className="text-sm mt-1">
                  {quote.day_of_year && quote.day_of_year > 0 ? `Ngày ${quote.day_of_year}` : "—"}
                </p>
              </div>
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Trạng thái</p>
                <Badge variant={quote.is_active ? "default" : "secondary"} className="mt-1">
                  {quote.is_active ? "Hoạt động" : "Ẩn"}
                </Badge>
              </div>
              <div>
                <p className="text-muted-foreground text-xs font-medium uppercase tracking-wider">Ngày tạo</p>
                <p className="text-sm mt-1">{formatDate(quote.created_at)}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Tags Card */}
      {quote.tags && quote.tags.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <Hash className="h-4 w-4 text-primary" />
              Tags
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-2">
              {quote.tags.map((tag) => (
                <Badge key={tag} variant="outline" className="px-3 py-1">
                  {tag}
                </Badge>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Delete Dialog */}
      <ConfirmDialog
        open={showDelete}
        onOpenChange={setShowDelete}
        title="Xóa danh ngôn"
        description="Bạn có chắc chắn muốn xóa danh ngôn này? Hành động này không thể hoàn tác."
        confirmText="Xóa"
        variant="destructive"
        loading={deleteQuote.isPending}
        onConfirm={() => {
          deleteQuote.mutate(quote.id, {
            onSuccess: () => router.push(ROUTES.ADMIN_QUOTES),
          });
        }}
      />
    </div>
  );
}
