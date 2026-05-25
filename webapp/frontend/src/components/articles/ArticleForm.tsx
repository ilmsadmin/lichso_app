"use client";

import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Eye, AlertCircle } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { RichTextEditor } from "@/components/shared/RichTextEditor";
import { ImagePickerV3 } from "@/components/shared/ImagePickerV3";
import { ArticlePreview } from "@/components/articles/ArticlePreview";
import { ArticleRelationPanel } from "@/components/articles/ArticleRelationPanel";
import { MediaContentLinker } from "@/components/shared/MediaContentLinker";
import type { Article, ArticleCategory, ArticleTag, ArticleStatus } from "@/types/article";

// ============================================
// Schema
// ============================================

const articleFormSchema = z.object({
  title: z.string().min(1, "Tiêu đề không được để trống"),
  excerpt: z.string().optional(),
  content: z.string().refine(
    (val) => {
      // Strip HTML tags and whitespace to check if there's actual content
      const stripped = val.replace(/<[^>]*>/g, "").trim();
      return stripped.length > 0;
    },
    { message: "Nội dung không được để trống" }
  ),
  category_id: z.string().optional(),
  status: z.enum(["draft", "published", "archived"]),
  featured_image: z.string().optional(),
  tag_ids: z.array(z.string()),
  meta_title: z.string().optional(),
  meta_description: z.string().optional(),
  og_image: z.string().optional(),
  is_featured: z.boolean(),
  published_at: z.string().optional(),
});

type ArticleFormData = z.infer<typeof articleFormSchema>;

// ============================================
// Component
// ============================================

interface ArticleFormProps {
  article?: Article;
  categories?: ArticleCategory[];
  tags?: ArticleTag[];
  onSubmit: (data: ArticleFormData) => void;
  isSubmitting?: boolean;
  onCancel: () => void;
}

export function ArticleForm({
  article,
  categories = [],
  tags = [],
  onSubmit,
  isSubmitting,
  onCancel,
}: ArticleFormProps) {
  const isEditing = !!article;
  const [previewOpen, setPreviewOpen] = useState(false);

  const form = useForm<ArticleFormData>({
    resolver: zodResolver(articleFormSchema),
    defaultValues: {
      title: article?.title ?? "",
      excerpt: article?.excerpt ?? "",
      content: article?.content ?? "",
      category_id: article?.category_id ?? "",
      status: (article?.status as ArticleStatus) ?? "draft",
      featured_image: article?.featured_image ?? "",
      tag_ids: article?.tags?.map((t) => t.id) ?? [],
      meta_title: article?.meta_title ?? "",
      meta_description: article?.meta_description ?? "",
      og_image: (article as unknown as Record<string, string>)?.og_image ?? "",
      is_featured: article?.is_featured ?? false,
      published_at: article?.published_at?.split("T")[0] ?? "",
    },
  });

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = form;

  const selectedTags = watch("tag_ids") ?? [];
  const watchedStatus = watch("status");

  // Track form-level validation errors to display a summary
  const [validationErrors, setValidationErrors] = useState<string[]>([]);

  useEffect(() => {
    if (article) {
      setValue("title", article.title);
      setValue("excerpt", article.excerpt || "");
      setValue("content", article.content);
      setValue("category_id", article.category_id);
      setValue("status", article.status);
      setValue("featured_image", article.featured_image || "");
      setValue("tag_ids", article.tags?.map((t) => t.id) ?? []);
      setValue("meta_title", article.meta_title || "");
      setValue("meta_description", article.meta_description || "");
      setValue("og_image", (article as unknown as Record<string, string>)?.og_image || "");
      setValue("is_featured", article.is_featured);
      setValue("published_at", article.published_at?.split("T")[0] || "");
    }
  }, [article, setValue]);

  const toggleTag = (tagId: string) => {
    const current = selectedTags;
    const updated = current.includes(tagId)
      ? current.filter((id) => id !== tagId)
      : [...current, tagId];
    setValue("tag_ids", updated, { shouldDirty: true });
  };

  // Build preview data
  const getPreviewData = () => ({
    title: watch("title"),
    excerpt: watch("excerpt"),
    content: watch("content"),
    category_name: categories.find((c) => c.id === watch("category_id"))?.name,
    tag_names: tags.filter((t) => selectedTags.includes(t.id)).map((t) => t.name),
    featured_image: watch("featured_image"),
    author_name: article?.author?.full_name,
    meta_title: watch("meta_title"),
    meta_description: watch("meta_description"),
    status: watch("status"),
    is_featured: watch("is_featured"),
  });

  // Handle form submission with error feedback
  const onFormSubmit = handleSubmit(
    (data) => {
      setValidationErrors([]);
      onSubmit(data);
    },
    (fieldErrors) => {
      const messages: string[] = [];
      Object.entries(fieldErrors).forEach(([, error]) => {
        if (error?.message) {
          messages.push(error.message);
        }
      });
      setValidationErrors(messages);
      toast.error("Vui lòng kiểm tra lại các trường bắt buộc");
    }
  );

  return (
    <>
      <form onSubmit={onFormSubmit} className="space-y-6">
        {/* Validation Error Summary */}
        {validationErrors.length > 0 && (
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>
              <ul className="list-disc pl-4">
                {validationErrors.map((msg, i) => (
                  <li key={i}>{msg}</li>
                ))}
              </ul>
            </AlertDescription>
          </Alert>
        )}

        {/* ── Two-column layout ── */}
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
          {/* ══════ LEFT COLUMN: Content editing (2/3) ══════ */}
          <div className="space-y-6 lg:col-span-2">
            {/* Basic Info */}
            <Card>
              <CardHeader>
                <CardTitle>Thông tin bài viết</CardTitle>
                <CardDescription>
                  {isEditing ? "Cập nhật thông tin bài viết." : "Tạo bài viết mới."}
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="title">Tiêu đề *</Label>
                  <Input id="title" {...register("title")} placeholder="Nhập tiêu đề bài viết..." />
                  {errors.title && <p className="text-destructive text-xs">{errors.title.message}</p>}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="excerpt">Tóm tắt</Label>
                  <Textarea
                    id="excerpt"
                    {...register("excerpt")}
                    placeholder="Mô tả ngắn gọn về bài viết..."
                    rows={3}
                  />
                </div>

                {/* Rich Text Editor */}
                <div className="space-y-2">
                  <Label>Nội dung *</Label>
                  <RichTextEditor
                    content={watch("content")}
                    onChange={(html) =>
                      setValue("content", html, { shouldDirty: true, shouldValidate: true })
                    }
                    placeholder="Bắt đầu viết nội dung bài viết..."
                  />
                  {errors.content && (
                    <p className="text-destructive text-xs">{errors.content.message}</p>
                  )}
                </div>
              </CardContent>
            </Card>

            {/* Related Articles (only when editing) */}
            {isEditing && article?.id && <ArticleRelationPanel articleId={article.id} />}
          </div>

          {/* ══════ RIGHT COLUMN: Options sidebar (1/3) ══════ */}
          <div className="space-y-6">
            {/* Publish / Status */}
            <Card>
              <CardHeader className="pb-3">
                <CardTitle className="text-base">Xuất bản</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label>Trạng thái</Label>
                  <Select
                    value={watch("status")}
                    onValueChange={(v) => setValue("status", v as ArticleStatus)}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="draft">Bản nháp</SelectItem>
                      <SelectItem value="published">Xuất bản</SelectItem>
                      <SelectItem value="archived">Lưu trữ</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                {/* Schedule Publishing */}
                {watchedStatus === "published" && (
                  <div className="space-y-2">
                    <Label htmlFor="published_at">Hẹn giờ xuất bản</Label>
                    <Input id="published_at" type="datetime-local" {...register("published_at")} />
                    <p className="text-muted-foreground text-xs">
                      Để trống = xuất bản ngay.
                    </p>
                  </div>
                )}

                <div className="flex items-center gap-2">
                  <Checkbox
                    id="is_featured"
                    checked={watch("is_featured")}
                    onCheckedChange={(checked) => setValue("is_featured", checked === true)}
                  />
                  <Label htmlFor="is_featured" className="cursor-pointer">
                    Bài viết nổi bật
                  </Label>
                </div>

                <Separator />

                {/* Action buttons */}
                <div className="flex flex-col gap-2">
                  <Button type="submit" className="w-full" disabled={isSubmitting}>
                    {isSubmitting ? "Đang xử lý..." : isEditing ? "Cập nhật" : "Tạo bài viết"}
                  </Button>
                  <div className="grid grid-cols-2 gap-2">
                    <Button type="button" variant="outline" size="sm" onClick={() => setPreviewOpen(true)}>
                      <Eye className="mr-2 h-4 w-4" />
                      Xem trước
                    </Button>
                    <Button type="button" variant="ghost" size="sm" onClick={onCancel} disabled={isSubmitting}>
                      Hủy
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Category */}
            <Card>
              <CardHeader className="pb-3">
                <CardTitle className="text-base">Danh mục</CardTitle>
              </CardHeader>
              <CardContent>
                <Select
                  value={watch("category_id")}
                  onValueChange={(v) => setValue("category_id", v)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Chọn danh mục" />
                  </SelectTrigger>
                  <SelectContent>
                    {categories.map((cat) => (
                      <SelectItem key={cat.id} value={cat.id}>
                        {cat.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {errors.category_id && (
                  <p className="text-destructive mt-1 text-xs">{errors.category_id.message}</p>
                )}
              </CardContent>
            </Card>

            {/* Tags */}
            {tags.length > 0 && (
              <Card>
                <CardHeader className="pb-3">
                  <CardTitle className="text-base">Tags</CardTitle>
                  <CardDescription>Gán tags cho bài viết để dễ dàng phân loại.</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="flex flex-wrap gap-2">
                    {tags.map((tag) => (
                      <label
                        key={tag.id}
                        className="hover:bg-accent flex cursor-pointer items-center gap-1.5 rounded-md border px-2.5 py-1.5 text-sm transition-colors"
                      >
                        <Checkbox
                          checked={selectedTags.includes(tag.id)}
                          onCheckedChange={() => toggleTag(tag.id)}
                        />
                        <span>{tag.name}</span>
                      </label>
                    ))}
                  </div>
                </CardContent>
              </Card>
            )}

            {/* Featured Image */}
            <Card>
              <CardHeader className="pb-3">
                <CardTitle className="text-base">Ảnh đại diện</CardTitle>
              </CardHeader>
              <CardContent>
                <ImagePickerV3
                  value={watch("featured_image")}
                  onChange={(url: string) => setValue("featured_image", url)}
                  aspectRatio="video"
                />
              </CardContent>
            </Card>

            {/* SEO */}
            <Card>
              <CardHeader className="pb-3">
                <CardTitle className="text-base">SEO</CardTitle>
                <CardDescription>Tối ưu hóa cho công cụ tìm kiếm.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="meta_title">Meta Title</Label>
                  <Input
                    id="meta_title"
                    {...register("meta_title")}
                    placeholder="Tiêu đề hiển thị trên trang tìm kiếm"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="meta_description">Meta Description</Label>
                  <Textarea
                    id="meta_description"
                    {...register("meta_description")}
                    placeholder="Mô tả hiển thị trên trang tìm kiếm"
                    rows={3}
                  />
                </div>
                <ImagePickerV3
                  value={watch("og_image")}
                  onChange={(url: string) => setValue("og_image", url)}
                  label="OG Image (ảnh chia sẻ mạng xã hội)"
                  aspectRatio="og"
                />
              </CardContent>
            </Card>

            {/* Media Attachments (only when editing) */}
            {isEditing && article?.id && (
              <MediaContentLinker
                entityType="article"
                entityId={article.id}
                label="Media đính kèm"
                description="Quản lý media liên kết với bài viết này (ảnh minh họa, tài liệu đính kèm...)"
              />
            )}
          </div>
        </div>
      </form>

      {/* Preview Dialog */}
      <ArticlePreview open={previewOpen} onOpenChange={setPreviewOpen} data={getPreviewData()} />
    </>
  );
}
