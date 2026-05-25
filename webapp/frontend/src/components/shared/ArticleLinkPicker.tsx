"use client";

import { useState, useEffect } from "react";
import { Search, LinkIcon, X, FileText } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Badge } from "@/components/ui/badge";
import { useArticle, useArticles } from "@/hooks/useArticles";
import { cn } from "@/lib/utils";
import type { ArticleSummary } from "@/types/article";

// ============================================
// ArticleLinkPicker Props
// ============================================

interface ArticleLinkPickerProps {
  value?: string | null;
  onChange: (articleId: string | null) => void;
  label?: string;
  className?: string;
}

// ============================================
// ArticleLinkPicker Component
// ============================================

export function ArticleLinkPicker({
  value,
  onChange,
  label = "Liên kết bài viết",
  className,
}: ArticleLinkPickerProps) {
  const [dialogOpen, setDialogOpen] = useState(false);
  const [search, setSearch] = useState("");
  const [selectedArticle, setSelectedArticle] = useState<ArticleSummary | null>(null);

  const { data: articlesData, isLoading } = useArticles({
    page: 1,
    limit: 20,
    search: search || undefined,
    status: "published",
  });

  const articles = articlesData?.data ?? [];

  // Load linked article info by ID when value is set
  const { data: linkedArticleData } = useArticle(value || "");

  useEffect(() => {
    if (value && linkedArticleData?.data) {
      const article = linkedArticleData.data;
      setSelectedArticle({
        id: article.id,
        title: article.title,
        slug: article.slug,
        excerpt: article.excerpt || "",
        category_id: article.category_id || "",
        author_id: article.author_id || "",
        status: article.status,
        featured_image: article.featured_image || "",
        view_count: article.view_count || 0,
        reading_time: article.reading_time || 0,
        is_featured: article.is_featured || false,
        published_at: article.published_at || null,
        created_at: article.created_at,
        category: article.category,
      });
    } else if (!value) {
      setSelectedArticle(null);
    }
  }, [value, linkedArticleData]);

  const handleSelect = (article: ArticleSummary) => {
    onChange(article.id);
    setSelectedArticle(article);
    setDialogOpen(false);
    setSearch("");
  };

  const handleRemove = () => {
    onChange(null);
    setSelectedArticle(null);
  };

  return (
    <div className={cn("space-y-2", className)}>
      <Label>{label}</Label>

      {value && selectedArticle ? (
        <div className="flex items-center gap-3 rounded-lg border p-3">
          <FileText className="text-primary h-5 w-5 shrink-0" />
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-medium">{selectedArticle.title}</p>
            <p className="text-muted-foreground text-xs">
              {selectedArticle.status === "published"
                ? "Đã xuất bản"
                : selectedArticle.status === "draft"
                  ? "Bản nháp"
                  : "Lưu trữ"}
            </p>
          </div>
          <Button
            type="button"
            variant="ghost"
            size="icon"
            className="h-8 w-8 shrink-0"
            onClick={handleRemove}
          >
            <X className="h-4 w-4" />
          </Button>
        </div>
      ) : (
        <Button
          type="button"
          variant="outline"
          className="text-muted-foreground w-full justify-start"
          onClick={() => setDialogOpen(true)}
        >
          <LinkIcon className="mr-2 h-4 w-4" />
          Chọn bài viết liên kết...
        </Button>
      )}

      {/* Article Search Dialog */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="sm:max-w-lg">
          <DialogHeader>
            <DialogTitle>Chọn bài viết liên kết</DialogTitle>
          </DialogHeader>

          <div className="space-y-4">
            <div className="relative">
              <Search className="text-muted-foreground absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
              <Input
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Tìm kiếm bài viết..."
                className="pl-9"
              />
            </div>

            <div className="max-h-[300px] space-y-1 overflow-y-auto">
              {isLoading ? (
                <div className="flex items-center justify-center py-8">
                  <div className="border-primary h-6 w-6 animate-spin rounded-full border-2 border-t-transparent" />
                </div>
              ) : articles.length === 0 ? (
                <div className="text-muted-foreground py-8 text-center text-sm">
                  Không tìm thấy bài viết nào
                </div>
              ) : (
                articles.map((article) => (
                  <button
                    key={article.id}
                    type="button"
                    className={cn(
                      "hover:bg-accent flex w-full items-center gap-3 rounded-lg p-3 text-left transition-colors",
                      value === article.id && "bg-accent"
                    )}
                    onClick={() => handleSelect(article)}
                  >
                    <FileText className="text-muted-foreground h-4 w-4 shrink-0" />
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-medium">{article.title}</p>
                      <div className="mt-0.5 flex items-center gap-2">
                        <Badge variant="outline" className="px-1.5 py-0 text-[10px]">
                          {article.status === "published"
                            ? "Đã xuất bản"
                            : article.status === "draft"
                              ? "Bản nháp"
                              : "Lưu trữ"}
                        </Badge>
                        {article.category && (
                          <span className="text-muted-foreground text-xs">
                            {article.category.name}
                          </span>
                        )}
                      </div>
                    </div>
                  </button>
                ))
              )}
            </div>

            {value && (
              <Button type="button" variant="ghost" className="w-full" onClick={handleRemove}>
                <X className="mr-2 h-4 w-4" />
                Bỏ liên kết
              </Button>
            )}
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
