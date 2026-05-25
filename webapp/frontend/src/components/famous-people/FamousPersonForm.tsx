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
import { ArticleLinkPicker } from "@/components/shared/ArticleLinkPicker";
import type { FamousPerson } from "@/types/famousPerson";

const famousPersonFormSchema = z.object({
  name: z.string().min(1, "Tên không được để trống"),
  original_name: z.string().optional(),
  birth_date: z.string().optional(),
  death_date: z.string().optional(),
  nationality: z.string().optional(),
  occupation: z.string().optional(),
  category: z.string().min(1, "Vui lòng chọn danh mục"),
  short_bio: z.string().optional(),
  image_url: z.string().optional(),
  tags: z.string().optional(), // comma separated
  is_vietnamese: z.boolean(),
  article_id: z.string().optional(),
});

type FamousPersonFormData = z.infer<typeof famousPersonFormSchema>;

interface FamousPersonFormProps {
  person?: FamousPerson;
  onSubmit: (data: {
    name: string;
    original_name?: string;
    birth_date?: string;
    death_date?: string;
    nationality?: string;
    occupation?: string;
    category: string;
    short_bio?: string;
    image_url?: string;
    tags?: string[];
    is_vietnamese?: boolean;
    article_id?: string;
  }) => void;
  isSubmitting?: boolean;
  onCancel: () => void;
}

export function FamousPersonForm({
  person,
  onSubmit,
  isSubmitting,
  onCancel,
}: FamousPersonFormProps) {
  const isEditing = !!person;

  const form = useForm<FamousPersonFormData>({
    resolver: zodResolver(famousPersonFormSchema),
    defaultValues: {
      name: person?.name ?? "",
      original_name: person?.original_name ?? "",
      birth_date: person?.birth_date?.split("T")[0] ?? "",
      death_date: person?.death_date?.split("T")[0] ?? "",
      nationality: person?.nationality ?? "",
      occupation: person?.occupation ?? "",
      category: person?.category ?? "khac",
      short_bio: person?.short_bio ?? "",
      image_url: person?.image_url ?? "",
      tags: person?.tags?.join(", ") ?? "",
      is_vietnamese: person?.is_vietnamese ?? false,
      article_id: person?.article_id ?? "",
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
    if (person) {
      setValue("name", person.name);
      setValue("original_name", person.original_name || "");
      setValue("birth_date", person.birth_date?.split("T")[0] || "");
      setValue("death_date", person.death_date?.split("T")[0] || "");
      setValue("nationality", person.nationality || "");
      setValue("occupation", person.occupation || "");
      setValue("category", person.category || "khac");
      setValue("short_bio", person.short_bio || "");
      setValue("image_url", person.image_url || "");
      setValue("tags", person.tags?.join(", ") || "");
      setValue("is_vietnamese", person.is_vietnamese);
      setValue("article_id", person.article_id || "");
    }
  }, [person, setValue]);

  const handleFormSubmit = (data: FamousPersonFormData) => {
    const splitComma = (s?: string) =>
      s
        ? s
            .split(",")
            .map((t) => t.trim())
            .filter(Boolean)
        : undefined;
    onSubmit({
      name: data.name,
      original_name: data.original_name || undefined,
      birth_date: data.birth_date || undefined,
      death_date: data.death_date || undefined,
      nationality: data.nationality || undefined,
      occupation: data.occupation || undefined,
      category: data.category,
      short_bio: data.short_bio || undefined,
      image_url: data.image_url || undefined,
      tags: splitComma(data.tags),
      is_vietnamese: data.is_vietnamese,
      article_id: data.article_id || undefined,
    });
  };

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Thông tin nhân vật</CardTitle>
          <CardDescription>
            {isEditing ? "Cập nhật thông tin nhân vật nổi tiếng." : "Thêm nhân vật nổi tiếng mới."}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Tên nhân vật *</Label>
            <Input id="name" {...register("name")} placeholder="Hồ Chí Minh, Albert Einstein..." />
            {errors.name && <p className="text-destructive text-xs">{errors.name.message}</p>}
          </div>

          <div className="space-y-2">
            <Label htmlFor="original_name">Tên gốc (nếu có)</Label>
            <Input
              id="original_name"
              {...register("original_name")}
              placeholder="Nguyễn Sinh Cung, Albert Einstein..."
            />
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="birth_date">Ngày sinh</Label>
              <Input id="birth_date" type="date" {...register("birth_date")} />
            </div>
            <div className="space-y-2">
              <Label htmlFor="death_date">Ngày mất</Label>
              <Input id="death_date" type="date" {...register("death_date")} />
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="nationality">Quốc tịch</Label>
              <Input
                id="nationality"
                {...register("nationality")}
                placeholder="Việt Nam, Đức, Mỹ..."
              />
            </div>
            <div /> {/* Spacer for grid alignment */}
          </div>

          {/* Avatar Upload */}
          <ImagePickerV3
            value={watch("image_url")}
            onChange={(url: string) => setValue("image_url", url)}
            label="Ảnh đại diện"
            aspectRatio="square"
          />

          <div className="space-y-2">
            <Label htmlFor="short_bio">Tiểu sử ngắn</Label>
            <Textarea
              id="short_bio"
              {...register("short_bio")}
              placeholder="Mô tả ngắn gọn về nhân vật..."
              rows={3}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="occupation">Nghề nghiệp</Label>
            <Input
              id="occupation"
              {...register("occupation")}
              placeholder="Chính trị gia, Nhà khoa học, Nghệ sĩ..."
            />
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="category">Danh mục *</Label>
              <select
                id="category"
                {...register("category")}
                className="border-input bg-background ring-offset-background placeholder:text-muted-foreground focus-visible:ring-ring flex h-10 w-full rounded-md border px-3 py-2 text-sm file:border-0 file:bg-transparent file:text-sm file:font-medium focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:outline-none"
              >
                <option value="chinh_tri">Chính trị</option>
                <option value="khoa_hoc">Khoa học</option>
                <option value="nghe_thuat">Nghệ thuật</option>
                <option value="am_nhac">Âm nhạc</option>
                <option value="the_thao">Thể thao</option>
                <option value="van_hoc">Văn học</option>
                <option value="lich_su">Lịch sử</option>
                <option value="dien_anh">Điện ảnh</option>
                <option value="kinh_doanh">Kinh doanh</option>
                <option value="khac">Khác</option>
              </select>
              {errors.category && (
                <p className="text-destructive text-xs">{errors.category.message}</p>
              )}
            </div>
            <div className="space-y-2">
              <Label htmlFor="tags">Tags (phân cách bằng dấu phẩy)</Label>
              <Input
                id="tags"
                {...register("tags")}
                placeholder="lãnh tụ, nhà thơ, nhà khoa học..."
              />
            </div>
          </div>

          {/* Link to Article */}
          <ArticleLinkPicker
            value={watch("article_id") || null}
            onChange={(id) => setValue("article_id", id || "")}
            label="Liên kết bài viết chi tiết"
          />

          <div className="flex items-center gap-2">
            <Checkbox
              id="is_vietnamese"
              checked={watch("is_vietnamese")}
              onCheckedChange={(checked) => setValue("is_vietnamese", checked === true)}
            />
            <Label htmlFor="is_vietnamese" className="cursor-pointer">
              Nhân vật Việt Nam
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
          {isSubmitting ? "Đang xử lý..." : isEditing ? "Cập nhật" : "Tạo nhân vật"}
        </Button>
      </div>
    </form>
  );
}
