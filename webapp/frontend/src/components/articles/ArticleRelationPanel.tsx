"use client";

import { useState } from "react";
import { Plus, Trash2, Link2, ArrowRightLeft, Sparkles } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { ArticleLinkPicker } from "@/components/shared/ArticleLinkPicker";
import {
  useArticleRelations,
  useCreateArticleRelation,
  useDeleteArticleRelation,
} from "@/hooks/useV3";
import type { ArticleRelationType } from "@/types/v3";

// ============================================
// Constants
// ============================================

const RELATION_TYPE_LABELS: Record<ArticleRelationType, string> = {
  related: "Liên quan",
  series: "Chuỗi bài",
  reference: "Tham khảo",
  translation: "Bản dịch",
};

const RELATION_TYPE_COLORS: Record<ArticleRelationType, string> = {
  related: "bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300",
  series: "bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-300",
  reference: "bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-300",
  translation: "bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300",
};

// ============================================
// Component
// ============================================

interface ArticleRelationPanelProps {
  articleId: string;
}

export function ArticleRelationPanel({ articleId }: ArticleRelationPanelProps) {
  const [selectedTargetId, setSelectedTargetId] = useState<string | null>(null);
  const [relationType, setRelationType] = useState<ArticleRelationType>("related");
  const [deleteId, setDeleteId] = useState<string | null>(null);

  const { data: relationsData, isLoading } = useArticleRelations(articleId);
  const createRelation = useCreateArticleRelation(articleId);
  const deleteRelation = useDeleteArticleRelation(articleId);

  const relations = relationsData?.data ?? [];

  const handleAdd = () => {
    if (!selectedTargetId) return;
    createRelation.mutate(
      {
        target_article_id: selectedTargetId,
        relation_type: relationType,
        is_bidirectional: relationType === "related",
      },
      {
        onSuccess: () => {
          setSelectedTargetId(null);
          setRelationType("related");
        },
      }
    );
  };

  const handleDelete = () => {
    if (!deleteId) return;
    deleteRelation.mutate(deleteId, {
      onSuccess: () => setDeleteId(null),
    });
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Link2 className="h-5 w-5" />
          Bài viết liên quan
        </CardTitle>
        <CardDescription>Thêm các bài viết liên quan, chuỗi bài hoặc tham khảo.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Add relation form */}
        <div className="flex flex-col gap-3 rounded-lg border border-dashed p-4">
          <div className="text-muted-foreground flex items-center gap-2 text-sm font-medium">
            <Plus className="h-4 w-4" />
            Thêm liên kết
          </div>
          <ArticleLinkPicker
            value={selectedTargetId}
            onChange={setSelectedTargetId}
            label="Chọn bài viết"
          />
          <div className="flex items-center gap-3">
            <Select
              value={relationType}
              onValueChange={(v) => setRelationType(v as ArticleRelationType)}
            >
              <SelectTrigger className="w-[180px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="related">Liên quan</SelectItem>
                <SelectItem value="series">Chuỗi bài</SelectItem>
                <SelectItem value="reference">Tham khảo</SelectItem>
                <SelectItem value="translation">Bản dịch</SelectItem>
              </SelectContent>
            </Select>
            <Button
              type="button"
              size="sm"
              onClick={handleAdd}
              disabled={!selectedTargetId || createRelation.isPending}
            >
              {createRelation.isPending ? (
                <div className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
              ) : (
                <Plus className="mr-1 h-4 w-4" />
              )}
              Thêm
            </Button>
          </div>
        </div>

        {/* Relations list */}
        {isLoading ? (
          <div className="space-y-2">
            {Array.from({ length: 3 }).map((_, i) => (
              <Skeleton key={i} className="h-14 w-full rounded-lg" />
            ))}
          </div>
        ) : relations.length === 0 ? (
          <div className="text-muted-foreground flex flex-col items-center justify-center py-8 text-center">
            <Sparkles className="mb-2 h-8 w-8 opacity-40" />
            <p className="text-sm">Chưa có bài viết liên quan</p>
            <p className="mt-1 text-xs">Hệ thống sẽ tự gợi ý dựa trên danh mục và tags</p>
          </div>
        ) : (
          <div className="space-y-2">
            {relations.map((rel) => (
              <div
                key={rel.id}
                className="hover:bg-muted/50 flex items-center gap-3 rounded-lg border p-3 transition-colors"
              >
                {rel.is_bidirectional && (
                  <ArrowRightLeft className="text-muted-foreground h-4 w-4 shrink-0" />
                )}
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-medium">
                    {rel.target_article?.title ?? "Bài viết không xác định"}
                  </p>
                  <div className="mt-1 flex items-center gap-2">
                    <Badge
                      variant="secondary"
                      className={`px-1.5 py-0 text-[10px] ${RELATION_TYPE_COLORS[rel.relation_type]}`}
                    >
                      {RELATION_TYPE_LABELS[rel.relation_type]}
                    </Badge>
                    {rel.target_article?.category && (
                      <span className="text-muted-foreground text-[11px]">
                        {rel.target_article.category.name}
                      </span>
                    )}
                  </div>
                </div>
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  className="text-destructive/60 hover:text-destructive h-8 w-8 shrink-0"
                  onClick={() => setDeleteId(rel.id)}
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            ))}
          </div>
        )}

        {/* Delete confirmation */}
        <ConfirmDialog
          open={!!deleteId}
          onOpenChange={(open) => !open && setDeleteId(null)}
          title="Xóa liên kết"
          description="Bạn có chắc muốn xóa liên kết bài viết này?"
          confirmText="Xóa"
          variant="destructive"
          onConfirm={handleDelete}
          loading={deleteRelation.isPending}
        />
      </CardContent>
    </Card>
  );
}
