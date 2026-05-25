"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Checkbox } from "@/components/ui/checkbox";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { ImagePickerV3 } from "@/components/shared/ImagePickerV3";
import type { Quote } from "@/types/quote";

const quoteFormSchema = z.object({
  quote: z.string().min(1, "Nội dung danh ngôn không được để trống"),
  original_quote: z.string().optional(),
  original_language: z.string().optional(),
  author: z.string().min(1, "Tên tác giả không được để trống"),
  author_bio: z.string().optional(),
  author_birth_year: z.union([z.number(), z.nan()]).optional(),
  author_death_year: z.union([z.number(), z.nan()]).optional(),
  author_nationality: z.string().optional(),
  author_image_url: z.string().optional(),
  day_of_year: z.union([z.number().min(0).max(366), z.nan()]).optional(),
  tags: z.string().optional(), // comma separated
  is_active: z.boolean(),
});

type QuoteFormData = z.infer<typeof quoteFormSchema>;

interface QuoteFormProps {
  quote?: Quote;
  onSubmit: (data: {
    quote: string;
    original_quote?: string;
    original_language?: string;
    author: string;
    author_bio?: string;
    author_birth_year?: number;
    author_death_year?: number;
    author_nationality?: string;
    author_image_url?: string;
    day_of_year?: number;
    tags?: string[];
    is_active?: boolean;
  }) => void;
  isSubmitting?: boolean;
  onCancel: () => void;
}

export function QuoteForm({ quote, onSubmit, isSubmitting, onCancel }: QuoteFormProps) {
  const isEditing = !!quote;

  const form = useForm<QuoteFormData>({
    resolver: zodResolver(quoteFormSchema),
    defaultValues: {
      quote: quote?.quote ?? "",
      original_quote: quote?.original_quote ?? "",
      original_language: quote?.original_language ?? "vi",
      author: quote?.author ?? "",
      author_bio: quote?.author_bio ?? "",
      author_birth_year: quote?.author_birth_year ?? undefined,
      author_death_year: quote?.author_death_year ?? undefined,
      author_nationality: quote?.author_nationality ?? "",
      author_image_url: quote?.author_image_url ?? "",
      day_of_year: quote?.day_of_year ?? 0,
      tags: quote?.tags?.join(", ") ?? "",
      is_active: quote?.is_active ?? true,
    },
  });

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = form;

  useEffect(() => {
    if (quote) {
      setValue("quote", quote.quote);
      setValue("original_quote", quote.original_quote || "");
      setValue("original_language", quote.original_language || "vi");
      setValue("author", quote.author);
      setValue("author_bio", quote.author_bio || "");
      setValue("author_birth_year", quote.author_birth_year ?? undefined);
      setValue("author_death_year", quote.author_death_year ?? undefined);
      setValue("author_nationality", quote.author_nationality || "");
      setValue("author_image_url", quote.author_image_url || "");
      setValue("day_of_year", quote.day_of_year ?? 0);
      setValue("tags", quote.tags?.join(", ") || "");
      setValue("is_active", quote.is_active);
    }
  }, [quote, setValue]);

  const handleFormSubmit = (data: QuoteFormData) => {
    const tagsArray = data.tags
      ? data.tags
          .split(",")
          .map((t) => t.trim())
          .filter(Boolean)
      : undefined;

    // Convert NaN to undefined for number fields
    const birthYear =
      typeof data.author_birth_year === "number" && !Number.isNaN(data.author_birth_year)
        ? data.author_birth_year
        : undefined;
    const deathYear =
      typeof data.author_death_year === "number" && !Number.isNaN(data.author_death_year)
        ? data.author_death_year
        : undefined;
    const dayOfYear =
      typeof data.day_of_year === "number" && !Number.isNaN(data.day_of_year)
        ? data.day_of_year
        : undefined;

    onSubmit({
      quote: data.quote,
      original_quote: data.original_quote || undefined,
      original_language: data.original_language || undefined,
      author: data.author,
      author_bio: data.author_bio || undefined,
      author_birth_year: birthYear,
      author_death_year: deathYear,
      author_nationality: data.author_nationality || undefined,
      author_image_url: data.author_image_url || undefined,
      day_of_year: dayOfYear,
      tags: tagsArray,
      is_active: data.is_active,
    });
  };

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Thông tin danh ngôn</CardTitle>
          <CardDescription>
            {isEditing ? "Cập nhật danh ngôn." : "Thêm danh ngôn mới."}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="quote">Nội dung danh ngôn (Tiếng Việt) *</Label>
            <Textarea
              id="quote"
              {...register("quote")}
              placeholder="Nhập nội dung danh ngôn..."
              rows={4}
            />
            {errors.quote && <p className="text-destructive text-xs">{errors.quote.message}</p>}
          </div>

          <div className="space-y-2">
            <Label htmlFor="original_quote">Nội dung gốc (nếu dịch)</Label>
            <Textarea
              id="original_quote"
              {...register("original_quote")}
              placeholder="Original quote in another language..."
              rows={3}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="original_language">Ngôn ngữ gốc</Label>
            <Input
              id="original_language"
              {...register("original_language")}
              placeholder="vi, en, fr..."
            />
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="author">Tác giả *</Label>
              <Input
                id="author"
                {...register("author")}
                placeholder="Nguyễn Du, Albert Einstein..."
              />
              {errors.author && <p className="text-destructive text-xs">{errors.author.message}</p>}
            </div>
            <div className="space-y-2">
              <Label htmlFor="author_nationality">Quốc tịch tác giả</Label>
              <Input
                id="author_nationality"
                {...register("author_nationality")}
                placeholder="Việt Nam, Đức, Mỹ..."
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="author_bio">Tiểu sử tác giả</Label>
            <Textarea
              id="author_bio"
              {...register("author_bio")}
              placeholder="Nhà thơ, nhà vật lý..."
              rows={2}
            />
          </div>

          <div className="grid gap-4 md:grid-cols-3">
            <div className="space-y-2">
              <Label htmlFor="author_birth_year">Năm sinh</Label>
              <Input
                id="author_birth_year"
                type="number"
                {...register("author_birth_year", { valueAsNumber: true })}
                placeholder="1765"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="author_death_year">Năm mất</Label>
              <Input
                id="author_death_year"
                type="number"
                {...register("author_death_year", { valueAsNumber: true })}
                placeholder="1820"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="day_of_year">Ngày trong năm (1-366)</Label>
              <Input
                id="day_of_year"
                type="number"
                {...register("day_of_year", { valueAsNumber: true })}
                placeholder="0 = không chỉ định"
                min={0}
                max={366}
              />
            </div>
          </div>

          {/* Author Image */}
          <ImagePickerV3
            value={watch("author_image_url")}
            onChange={(url: string) => setValue("author_image_url", url)}
            label="Ảnh tác giả"
            aspectRatio="square"
          />

          <div className="space-y-2">
            <Label htmlFor="tags">Tags (phân cách bằng dấu phẩy)</Label>
            <Input
              id="tags"
              {...register("tags")}
              placeholder="triết học, cuộc sống, tình yêu..."
            />
          </div>

          <div className="flex items-center gap-2">
            <Checkbox
              id="is_active"
              checked={watch("is_active")}
              onCheckedChange={(checked) => setValue("is_active", checked === true)}
            />
            <Label htmlFor="is_active" className="cursor-pointer">
              Hiển thị danh ngôn
            </Label>
          </div>
        </CardContent>
      </Card>

      <Separator />
      <div className="flex items-center justify-end gap-3">
        <Button type="button" variant="outline" onClick={onCancel} disabled={isSubmitting}>
          Hủy
        </Button>
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Đang xử lý..." : isEditing ? "Cập nhật" : "Tạo danh ngôn"}
        </Button>
      </div>
    </form>
  );
}
