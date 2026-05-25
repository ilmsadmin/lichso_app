"use client";

import { format } from "date-fns";
import { vi } from "date-fns/locale";
import { Eye, X, Calendar, Clock, User, Tag } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Separator } from "@/components/ui/separator";
import { ScrollArea } from "@/components/ui/scroll-area";
import { getImageUrl, fixContentImageUrls } from "@/lib/utils";
import { ArticleContent } from "@/components/articles/ArticleContent";

// ============================================
// Types
// ============================================

interface ArticlePreviewData {
  title: string;
  excerpt?: string;
  content: string;
  category_name?: string;
  tag_names?: string[];
  featured_image?: string;
  author_name?: string;
  meta_title?: string;
  meta_description?: string;
  status?: string;
  is_featured?: boolean;
}

interface ArticlePreviewProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  data: ArticlePreviewData;
}

// ============================================
// ArticlePreview Component
// ============================================

export function ArticlePreview({ open, onOpenChange, data }: ArticlePreviewProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] p-0 sm:max-w-3xl">
        <DialogHeader className="p-6 pb-0">
          <DialogTitle className="flex items-center gap-2">
            <Eye className="h-5 w-5" />
            Xem trước bài viết
          </DialogTitle>
        </DialogHeader>

        <ScrollArea className="max-h-[calc(90vh-80px)]">
          <div className="p-6 pt-4">
            {/* SEO Preview */}
            {(data.meta_title || data.meta_description) && (
              <div className="bg-muted/30 mb-6 rounded-lg border p-4">
                <p className="text-muted-foreground mb-2 text-xs font-medium">
                  Hiển thị trên Google:
                </p>
                <div className="space-y-1">
                  <p className="text-lg leading-tight text-blue-600 dark:text-blue-400">
                    {data.meta_title || data.title}
                  </p>
                  <p className="text-xs text-green-700 dark:text-green-400">
                    lichso.vn › bai-viet › ...
                  </p>
                  <p className="text-muted-foreground line-clamp-2 text-sm">
                    {data.meta_description || data.excerpt || ""}
                  </p>
                </div>
              </div>
            )}

            {/* Article Preview */}
            <article className="prose prose-sm sm:prose dark:prose-invert max-w-none">
              {/* Featured Image */}
              {data.featured_image && (
                <div className="not-prose mb-6 overflow-hidden rounded-lg">
                  {/* eslint-disable-next-line @next/next/no-img-element */}
                  <img
                    src={getImageUrl(data.featured_image)}
                    alt={data.title}
                    className="aspect-video w-full object-cover"
                  />
                </div>
              )}

              {/* Meta */}
              <div className="not-prose mb-4 flex flex-wrap items-center gap-3">
                {data.category_name && <Badge variant="secondary">{data.category_name}</Badge>}
                {data.status && (
                  <Badge
                    variant={
                      data.status === "published"
                        ? "default"
                        : data.status === "draft"
                          ? "outline"
                          : "secondary"
                    }
                  >
                    {data.status === "published"
                      ? "Đã xuất bản"
                      : data.status === "draft"
                        ? "Bản nháp"
                        : "Lưu trữ"}
                  </Badge>
                )}
                {data.is_featured && (
                  <Badge variant="default" className="bg-yellow-500">
                    ⭐ Nổi bật
                  </Badge>
                )}
              </div>

              {/* Title */}
              <h1 className="mb-2 text-2xl font-bold sm:text-3xl">
                {data.title || "Chưa có tiêu đề"}
              </h1>

              {/* Author & Date */}
              <div className="not-prose text-muted-foreground mb-4 flex items-center gap-4 text-sm">
                {data.author_name && (
                  <span className="flex items-center gap-1">
                    <User className="h-3.5 w-3.5" />
                    {data.author_name}
                  </span>
                )}
                <span className="flex items-center gap-1">
                  <Calendar className="h-3.5 w-3.5" />
                  {format(new Date(), "dd/MM/yyyy", { locale: vi })}
                </span>
              </div>

              {/* Excerpt */}
              {data.excerpt && (
                <p className="text-muted-foreground border-primary mb-6 border-l-4 pl-4 text-lg italic">
                  {data.excerpt}
                </p>
              )}

              <Separator className="my-4" />

              {/* Content */}
              <ArticleContent
                content={data.content || "<p>Chưa có nội dung.</p>"}
                className="mt-4"
              />

              {/* Tags */}
              {data.tag_names && data.tag_names.length > 0 && (
                <>
                  <Separator className="my-6" />
                  <div className="not-prose flex flex-wrap items-center gap-2">
                    <Tag className="text-muted-foreground h-4 w-4" />
                    {data.tag_names.map((tag) => (
                      <Badge key={tag} variant="outline">
                        {tag}
                      </Badge>
                    ))}
                  </div>
                </>
              )}
            </article>
          </div>
        </ScrollArea>
      </DialogContent>
    </Dialog>
  );
}
