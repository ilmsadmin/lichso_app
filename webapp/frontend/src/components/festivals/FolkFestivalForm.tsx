"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { ArticleLinkPicker } from "@/components/shared/ArticleLinkPicker";
import { GalleryManagerV3 } from "@/components/shared/GalleryManagerV3";
import { ImagePickerV3 } from "@/components/shared/ImagePickerV3";
import type { FolkFestival } from "@/types/festival";

const festivalFormSchema = z.object({
  name: z.string().min(1, "Tên lễ hội không được để trống"),
  alternate_name: z.string().optional(),
  calendar_type: z.string(),
  festival_type: z.string(),
  lunar_month: z.union([z.number().min(0).max(12), z.nan()]).optional(),
  lunar_day: z.union([z.number().min(0).max(30), z.nan()]).optional(),
  solar_month: z.union([z.number().min(0).max(12), z.nan()]).optional(),
  solar_day: z.union([z.number().min(0).max(31), z.nan()]).optional(),
  duration_days: z.union([z.number().min(1), z.nan()]).optional(),
  region: z.string().optional(),
  country: z.string().optional(),
  short_description: z.string().optional(),
  traditions: z.string().optional(), // comma separated
  image_url: z.string().optional(),
  gallery_urls: z.array(z.string()),
  importance: z.string(),
  tags: z.string().optional(), // comma separated
  article_id: z.string().optional(),
});

type FestivalFormData = z.infer<typeof festivalFormSchema>;

interface FolkFestivalFormProps {
  festival?: FolkFestival;
  onSubmit: (data: {
    name: string;
    alternate_name?: string;
    calendar_type: string;
    festival_type: string;
    lunar_month?: number;
    lunar_day?: number;
    solar_month?: number;
    solar_day?: number;
    duration_days?: number;
    region?: string;
    country?: string;
    short_description?: string;
    traditions?: string[];
    image_url?: string;
    gallery_urls?: string[];
    importance?: string;
    tags?: string[];
    article_id?: string;
  }) => void;
  isSubmitting?: boolean;
  onCancel: () => void;
}

export function FolkFestivalForm({
  festival,
  onSubmit,
  isSubmitting,
  onCancel,
}: FolkFestivalFormProps) {
  const isEditing = !!festival;

  const form = useForm<FestivalFormData>({
    resolver: zodResolver(festivalFormSchema),
    defaultValues: {
      name: festival?.name ?? "",
      alternate_name: festival?.alternate_name ?? "",
      calendar_type: festival?.calendar_type ?? "lunar",
      festival_type: festival?.festival_type ?? "folk_festival",
      lunar_month: festival?.lunar_month ?? 0,
      lunar_day: festival?.lunar_day ?? 0,
      solar_month: festival?.solar_month ?? 0,
      solar_day: festival?.solar_day ?? 0,
      duration_days: festival?.duration_days ?? 1,
      region: festival?.region ?? "",
      country: festival?.country ?? "Việt Nam",
      short_description: festival?.short_description ?? "",
      traditions: festival?.traditions?.join(", ") ?? "",
      image_url: festival?.image_url ?? "",
      gallery_urls: festival?.gallery_urls ?? [],
      importance: festival?.importance ?? "medium",
      tags: festival?.tags?.join(", ") ?? "",
      article_id: festival?.article_id ?? "",
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
    if (festival) {
      setValue("name", festival.name);
      setValue("alternate_name", festival.alternate_name || "");
      setValue("calendar_type", festival.calendar_type);
      setValue("festival_type", festival.festival_type);
      setValue("lunar_month", festival.lunar_month ?? 0);
      setValue("lunar_day", festival.lunar_day ?? 0);
      setValue("solar_month", festival.solar_month ?? 0);
      setValue("solar_day", festival.solar_day ?? 0);
      setValue("duration_days", festival.duration_days ?? 1);
      setValue("region", festival.region || "");
      setValue("country", festival.country || "Việt Nam");
      setValue("short_description", festival.short_description || "");
      setValue("traditions", festival.traditions?.join(", ") || "");
      setValue("image_url", festival.image_url || "");
      setValue("gallery_urls", festival.gallery_urls ?? []);
      setValue("importance", festival.importance || "medium");
      setValue("tags", festival.tags?.join(", ") || "");
      setValue("article_id", festival.article_id || "");
    }
  }, [festival, setValue]);

  const splitComma = (s?: string) =>
    s
      ? s
          .split(",")
          .map((t) => t.trim())
          .filter(Boolean)
      : undefined;

  // Convert NaN and 0 to undefined for optional number fields
  const safeNum = (val: number | undefined) =>
    typeof val === "number" && !Number.isNaN(val) && val !== 0 ? val : undefined;

  const handleFormSubmit = (data: FestivalFormData) => {
    onSubmit({
      name: data.name,
      alternate_name: data.alternate_name || undefined,
      calendar_type: data.calendar_type,
      festival_type: data.festival_type,
      lunar_month: safeNum(data.lunar_month),
      lunar_day: safeNum(data.lunar_day),
      solar_month: safeNum(data.solar_month),
      solar_day: safeNum(data.solar_day),
      duration_days: safeNum(data.duration_days),
      region: data.region || undefined,
      country: data.country || undefined,
      short_description: data.short_description || undefined,
      traditions: splitComma(data.traditions),
      image_url: data.image_url || undefined,
      gallery_urls: data.gallery_urls.length > 0 ? data.gallery_urls : undefined,
      importance: data.importance,
      tags: splitComma(data.tags),
      article_id: data.article_id || undefined,
    });
  };

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Thông tin lễ hội</CardTitle>
          <CardDescription>
            {isEditing ? "Cập nhật thông tin lễ hội dân gian." : "Thêm lễ hội dân gian mới."}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="name">Tên lễ hội *</Label>
              <Input
                id="name"
                {...register("name")}
                placeholder="Tết Nguyên Đán, Lễ hội Đền Hùng..."
              />
              {errors.name && <p className="text-destructive text-xs">{errors.name.message}</p>}
            </div>
            <div className="space-y-2">
              <Label htmlFor="alternate_name">Tên khác</Label>
              <Input
                id="alternate_name"
                {...register("alternate_name")}
                placeholder="Tết Cả, Tết Ta..."
              />
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-3">
            <div className="space-y-2">
              <Label>Loại lễ hội</Label>
              <Select
                value={watch("festival_type")}
                onValueChange={(v) => setValue("festival_type", v)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="folk_festival">Lễ hội dân gian</SelectItem>
                  <SelectItem value="religion">Tôn giáo</SelectItem>
                  <SelectItem value="national_holiday">Quốc lễ</SelectItem>
                  <SelectItem value="seasonal">Mùa vụ</SelectItem>
                  <SelectItem value="other">Khác</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Loại lịch</Label>
              <Select
                value={watch("calendar_type")}
                onValueChange={(v) => setValue("calendar_type", v)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="lunar">Âm lịch</SelectItem>
                  <SelectItem value="solar">Dương lịch</SelectItem>
                  <SelectItem value="both">Cả hai</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Mức độ quan trọng</Label>
              <Select value={watch("importance")} onValueChange={(v) => setValue("importance", v)}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="high">Cao</SelectItem>
                  <SelectItem value="medium">Trung bình</SelectItem>
                  <SelectItem value="low">Thấp</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-4">
            <div className="space-y-2">
              <Label htmlFor="lunar_month">Tháng âm lịch</Label>
              <Input
                id="lunar_month"
                type="number"
                {...register("lunar_month", { valueAsNumber: true })}
                min={0}
                max={12}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="lunar_day">Ngày âm lịch</Label>
              <Input
                id="lunar_day"
                type="number"
                {...register("lunar_day", { valueAsNumber: true })}
                min={0}
                max={30}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="solar_month">Tháng dương lịch</Label>
              <Input
                id="solar_month"
                type="number"
                {...register("solar_month", { valueAsNumber: true })}
                min={0}
                max={12}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="solar_day">Ngày dương lịch</Label>
              <Input
                id="solar_day"
                type="number"
                {...register("solar_day", { valueAsNumber: true })}
                min={0}
                max={31}
              />
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-3">
            <div className="space-y-2">
              <Label htmlFor="duration_days">Số ngày diễn ra</Label>
              <Input
                id="duration_days"
                type="number"
                {...register("duration_days", { valueAsNumber: true })}
                min={1}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="region">Vùng miền</Label>
              <Input id="region" {...register("region")} placeholder="Bắc Bộ, Trung Bộ..." />
            </div>
            <div className="space-y-2">
              <Label htmlFor="country">Quốc gia</Label>
              <Input id="country" {...register("country")} placeholder="Việt Nam" />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="short_description">Mô tả ngắn</Label>
            <Textarea
              id="short_description"
              {...register("short_description")}
              placeholder="Mô tả ngắn gọn về lễ hội..."
              rows={3}
            />
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="traditions">Phong tục (phân cách bằng dấu phẩy)</Label>
              <Input
                id="traditions"
                {...register("traditions")}
                placeholder="gói bánh chưng, chúc tết..."
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="tags">Tags (phân cách bằng dấu phẩy)</Label>
              <Input id="tags" {...register("tags")} placeholder="tết, truyền thống, âm lịch..." />
            </div>
          </div>

          {/* Main Image */}
          <ImagePickerV3
            value={watch("image_url")}
            onChange={(url: string) => setValue("image_url", url)}
            label="Hình ảnh lễ hội"
            aspectRatio="video"
          />

          {/* Gallery Image Management */}
          <GalleryManagerV3
            images={watch("gallery_urls")}
            onChange={(imgs: string[]) => setValue("gallery_urls", imgs, { shouldDirty: true })}
            label="Thư viện ảnh lễ hội"
            maxImages={15}
          />

          {/* Link to Article */}
          <ArticleLinkPicker
            value={watch("article_id") || null}
            onChange={(id) => setValue("article_id", id || "")}
            label="Liên kết bài viết chi tiết"
          />
        </CardContent>
      </Card>

      <Separator />
      <div className="flex items-center justify-end gap-3">
        <Button type="button" variant="outline" onClick={onCancel} disabled={isSubmitting}>
          Hủy
        </Button>
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Đang xử lý..." : isEditing ? "Cập nhật" : "Tạo lễ hội"}
        </Button>
      </div>
    </form>
  );
}
