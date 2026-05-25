"use client";

import { use, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import {
  ArrowLeft,
  Calendar,
  Clock,
  ExternalLink,
  Eye,
  FileText,
  Globe,
  Pencil,
  Search,
  Star,
  Tag,
  Trash2,
  User,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { PermissionGate } from "@/components/auth/PermissionGate";
import { useArticle, useDeleteArticle, useUpdateArticle } from "@/hooks/useArticles";
import { formatDate, getImageUrl } from "@/lib/utils";
import { getCategoryIcon } from "@/lib/categoryIcons";
import { ROUTES } from "@/lib/constants";
import { ArticleContent } from "@/components/articles/ArticleContent";

/* ─── helpers ──────────────────────────────────────────────── */
const statusMap: Record<string, { label: string; color: string }> = {
  published: {
    label: "Đã xuất bản",
    color: "bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300",
  },
  draft: {
    label: "Nháp",
    color: "bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300",
  },
  archived: {
    label: "Lưu trữ",
    color: "bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300",
  },
};

function wordCount(html: string) {
  const text = html
    .replace(/<[^>]*>/g, " ")
    .replace(/\s+/g, " ")
    .trim();
  if (!text) return 0;
  // Vietnamese words are mostly single-syllable separated by spaces
  return text.split(/\s+/).length;
}

/* ═══════════════════════════════════════════════════════════ */

export default function ArticleDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const { data, isLoading } = useArticle(id);
  const deleteArticle = useDeleteArticle();
  const updateArticle = useUpdateArticle(id);
  const [showDelete, setShowDelete] = useState(false);

  const article = data?.data;

  const words = useMemo(
    () => (article?.content ? wordCount(article.content) : 0),
    [article?.content]
  );

  /* ─── loading state ─── */
  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-24">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    );
  }

  /* ─── not found ─── */
  if (!article) {
    return (
      <div className="flex flex-col items-center justify-center gap-4 py-24">
        <p className="text-muted-foreground">Không tìm thấy bài viết.</p>
        <Button variant="outline" onClick={() => router.push(ROUTES.ADMIN_ARTICLES)}>
          Quay lại danh sách
        </Button>
      </div>
    );
  }

  const status = statusMap[article.status] ?? statusMap.draft;

  const handleTogglePublish = () => {
    const newStatus = article.status === "published" ? "draft" : "published";
    updateArticle.mutate({ status: newStatus });
  };

  /* ─── render ─── */
  return (
    <div className="mx-auto max-w-6xl space-y-6">
      {/* ── Top bar ── */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <Button
            variant="ghost"
            size="icon"
            className="shrink-0"
            onClick={() => router.push(ROUTES.ADMIN_ARTICLES)}
          >
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div className="min-w-0">
            <h1 className="truncate text-2xl font-bold tracking-tight">Chi tiết bài viết</h1>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {article.status === "published" && article.slug && (
            <Button variant="outline" size="sm" asChild>
              <a href={`${ROUTES.ARTICLES}/${article.slug}`} target="_blank" rel="noopener noreferrer">
                <ExternalLink className="mr-2 h-4 w-4" />
                Xem trang công khai
              </a>
            </Button>
          )}
          <PermissionGate permission="content.update">
            <TooltipProvider>
              <Tooltip>
                <TooltipTrigger asChild>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={handleTogglePublish}
                    disabled={updateArticle.isPending}
                  >
                    {article.status === "published" ? (
                      <>
                        <FileText className="mr-2 h-4 w-4" />
                        Chuyển nháp
                      </>
                    ) : (
                      <>
                        <Globe className="mr-2 h-4 w-4" />
                        Xuất bản
                      </>
                    )}
                  </Button>
                </TooltipTrigger>
                <TooltipContent>
                  {article.status === "published"
                    ? "Chuyển bài viết về trạng thái nháp"
                    : "Xuất bản bài viết"}
                </TooltipContent>
              </Tooltip>
            </TooltipProvider>
          </PermissionGate>

          <PermissionGate permission="content.update">
            <Button variant="outline" size="sm" asChild>
              <Link href={`${ROUTES.ADMIN_ARTICLES}/${article.id}/edit`}>
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

      {/* ── Hero image ── */}
      {article.featured_image && (
        <div className="bg-muted relative aspect-[21/9] w-full overflow-hidden rounded-xl">
          <img
            src={getImageUrl(article.featured_image)}
            alt={article.title}
            className="h-full w-full object-cover"
          />
          {/* gradient overlay */}
          <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
          {/* title overlay */}
          <div className="absolute inset-x-0 bottom-0 p-6 md:p-8">
            <div className="mb-3 flex items-center gap-2">
              <span
                className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${status.color}`}
              >
                {status.label}
              </span>
              {article.is_featured && (
                <span className="inline-flex items-center gap-1 rounded-full bg-yellow-100 px-2.5 py-0.5 text-xs font-semibold text-yellow-800">
                  <Star className="h-3 w-3 fill-current" />
                  Nổi bật
                </span>
              )}
            </div>
            <h2 className="line-clamp-2 text-xl leading-tight font-bold text-white md:text-2xl lg:text-3xl">
              {article.title}
            </h2>
            {article.excerpt && (
              <p className="mt-2 line-clamp-2 max-w-3xl text-sm text-white/80">{article.excerpt}</p>
            )}
          </div>
        </div>
      )}

      {/* ── Title (when no hero image) ── */}
      {!article.featured_image && (
        <div className="space-y-2">
          <div className="flex items-center gap-2">
            <span
              className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${status.color}`}
            >
              {status.label}
            </span>
            {article.is_featured && (
              <span className="inline-flex items-center gap-1 rounded-full bg-yellow-100 px-2.5 py-0.5 text-xs font-semibold text-yellow-800">
                <Star className="h-3 w-3 fill-current" />
                Nổi bật
              </span>
            )}
          </div>
          <h2 className="text-2xl font-bold tracking-tight md:text-3xl">{article.title}</h2>
          {article.excerpt && <p className="text-muted-foreground">{article.excerpt}</p>}
        </div>
      )}

      {/* ── Stats bar ── */}
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        {[
          {
            icon: Eye,
            label: "Lượt xem",
            value: (article.view_count ?? 0).toLocaleString("vi-VN"),
          },
          {
            icon: Clock,
            label: "Thời gian đọc",
            value: `${article.reading_time ?? 0} phút`,
          },
          {
            icon: FileText,
            label: "Số từ",
            value: words.toLocaleString("vi-VN"),
          },
          {
            icon: Calendar,
            label: "Xuất bản",
            value: article.published_at ? formatDate(article.published_at) : "—",
          },
        ].map((stat) => (
          <Card key={stat.label} className="py-3">
            <CardContent className="flex items-center gap-3 px-4 py-0">
              <div className="bg-primary/10 text-primary flex h-9 w-9 shrink-0 items-center justify-center rounded-lg">
                <stat.icon className="h-4 w-4" />
              </div>
              <div className="min-w-0">
                <p className="text-muted-foreground truncate text-xs">{stat.label}</p>
                <p className="truncate text-sm font-semibold">{stat.value}</p>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* ── Two-column layout ── */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* ── Main content (2/3) ── */}
        <div className="space-y-6 lg:col-span-2">
          {/* Article body */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-base">Nội dung bài viết</CardTitle>
            </CardHeader>
            <CardContent>
              {article.content ? (
                <ArticleContent content={article.content} />
              ) : (
                <p className="text-muted-foreground text-sm italic">Chưa có nội dung.</p>
              )}
            </CardContent>
          </Card>
        </div>

        {/* ── Sidebar (1/3) ── */}
        <div className="space-y-5">
          {/* Author card */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="flex items-center gap-2 text-base">
                <User className="h-4 w-4" />
                Tác giả
              </CardTitle>
            </CardHeader>
            <CardContent>
              {article.author ? (
                <div className="flex items-center gap-3">
                  {article.author.avatar ? (
                    <img
                      src={getImageUrl(article.author.avatar)}
                      alt={article.author.full_name}
                      className="ring-primary/20 h-10 w-10 rounded-full object-cover ring-2"
                    />
                  ) : (
                    <div className="bg-primary/10 text-primary flex h-10 w-10 items-center justify-center rounded-full font-semibold">
                      {article.author.full_name?.charAt(0)?.toUpperCase() ?? "?"}
                    </div>
                  )}
                  <div className="min-w-0">
                    <p className="truncate text-sm font-medium">{article.author.full_name}</p>
                    <p className="text-muted-foreground text-xs">Tác giả</p>
                  </div>
                </div>
              ) : (
                <p className="text-muted-foreground text-sm italic">Không rõ tác giả</p>
              )}
            </CardContent>
          </Card>

          {/* Category */}
          {article.category && (
            <Card>
              <CardHeader className="pb-3">
                <CardTitle className="flex items-center gap-2 text-base">
                  <FileText className="h-4 w-4" />
                  Danh mục
                </CardTitle>
              </CardHeader>
              <CardContent>
                {(() => {
                  const CatIcon = getCategoryIcon(article.category.slug);
                  return (
                    <Badge variant="secondary" className="text-sm">
                      <CatIcon className="mr-1 h-3.5 w-3.5" />
                      {article.category.name}
                    </Badge>
                  );
                })()}
              </CardContent>
            </Card>
          )}

          {/* Tags */}
          {article.tags && article.tags.length > 0 && (
            <Card>
              <CardHeader className="pb-3">
                <CardTitle className="flex items-center gap-2 text-base">
                  <Tag className="h-4 w-4" />
                  Tags
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap gap-1.5">
                  {article.tags.map((tag) => (
                    <Badge key={typeof tag === "string" ? tag : tag.id} variant="outline">
                      {typeof tag === "string" ? tag : tag.name}
                    </Badge>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}

          {/* SEO / Meta card */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="flex items-center gap-2 text-base">
                <Search className="h-4 w-4" />
                SEO & Metadata
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm">
              <div>
                <p className="text-muted-foreground mb-0.5 text-xs font-medium">Slug</p>
                <p className="bg-muted rounded px-2 py-1 font-mono text-xs break-all">
                  {article.slug}
                </p>
              </div>
              {article.meta_title && (
                <div>
                  <p className="text-muted-foreground mb-0.5 text-xs font-medium">Meta Title</p>
                  <p className="text-sm">{article.meta_title}</p>
                </div>
              )}
              {article.meta_description && (
                <div>
                  <p className="text-muted-foreground mb-0.5 text-xs font-medium">
                    Meta Description
                  </p>
                  <p className="text-muted-foreground line-clamp-3 text-sm">
                    {article.meta_description}
                  </p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Timeline card */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="flex items-center gap-2 text-base">
                <Calendar className="h-4 w-4" />
                Thời gian
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm">
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">Ngày tạo</span>
                <span className="font-medium">{formatDate(article.created_at)}</span>
              </div>
              <Separator />
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">Cập nhật</span>
                <span className="font-medium">{formatDate(article.updated_at)}</span>
              </div>
              {article.published_at && (
                <>
                  <Separator />
                  <div className="flex items-center justify-between">
                    <span className="text-muted-foreground">Xuất bản</span>
                    <span className="font-medium">{formatDate(article.published_at)}</span>
                  </div>
                </>
              )}
            </CardContent>
          </Card>
        </div>
      </div>

      {/* ── Delete dialog ── */}
      <ConfirmDialog
        open={showDelete}
        onOpenChange={setShowDelete}
        title="Xóa bài viết"
        description={`Bạn có chắc chắn muốn xóa bài viết "${article.title}"? Hành động này không thể hoàn tác.`}
        confirmText="Xóa"
        variant="destructive"
        loading={deleteArticle.isPending}
        onConfirm={() => {
          deleteArticle.mutate(article.id, {
            onSuccess: () => router.push(ROUTES.ADMIN_ARTICLES),
          });
        }}
      />
    </div>
  );
}
