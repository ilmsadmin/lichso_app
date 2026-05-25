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
import { ImagePickerV3 } from "@/components/shared/ImagePickerV3";
import { Switch } from "@/components/ui/switch";
import type { HistoricalEvent } from "@/types/event";

const eventFormSchema = z.object({
  title: z.string().min(1, "Tiêu đề không được để trống"),
  event_date: z.string().optional(),
  event_day: z.union([z.number().min(1, "Ngày sự kiện là bắt buộc").max(31), z.nan()]),
  event_month: z.union([z.number().min(1, "Tháng sự kiện là bắt buộc").max(12), z.nan()]),
  event_year: z.union([z.number(), z.nan()]).optional(),
  is_lunar: z.boolean(),
  is_recurring: z.boolean(),
  event_type: z.string(),
  country: z.string().optional(),
  country_code: z.string().optional(),
  flag_emoji: z.string().optional(),
  short_description: z.string().optional(),
  image_url: z.string().optional(),
  importance: z.string(),
  tags: z.string().optional(), // comma separated
  article_id: z.string().optional(),
});

type EventFormData = z.infer<typeof eventFormSchema>;

interface EventFormProps {
  event?: HistoricalEvent;
  onSubmit: (data: {
    title: string;
    event_date?: string;
    event_day: number;
    event_month: number;
    event_year?: number;
    is_lunar?: boolean;
    is_recurring?: boolean;
    event_type?: string;
    country?: string;
    country_code?: string;
    flag_emoji?: string;
    short_description?: string;
    image_url?: string;
    importance?: string;
    tags?: string[];
    article_id?: string;
  }) => void;
  isSubmitting?: boolean;
  onCancel: () => void;
}

export function EventForm({ event, onSubmit, isSubmitting, onCancel }: EventFormProps) {
  const isEditing = !!event;

  const form = useForm<EventFormData>({
    resolver: zodResolver(eventFormSchema),
    defaultValues: {
      title: event?.title ?? "",
      event_date: event?.event_date ?? "",
      event_day: event?.event_day ?? 1,
      event_month: event?.event_month ?? 1,
      event_year: event?.event_year ?? undefined,
      is_lunar: event?.is_lunar ?? false,
      is_recurring: event?.is_recurring ?? false,
      event_type: event?.event_type ?? "historical_event",
      country: event?.country ?? "",
      country_code: event?.country_code ?? "",
      flag_emoji: event?.flag_emoji ?? "",
      short_description: event?.short_description ?? "",
      image_url: event?.image_url ?? "",
      importance: event?.importance ?? "medium",
      tags: event?.tags?.join(", ") ?? "",
      article_id: event?.article_id ?? "",
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
    if (event) {
      setValue("title", event.title);
      setValue("event_date", event.event_date || "");
      setValue("event_day", event.event_day);
      setValue("event_month", event.event_month);
      setValue("event_year", event.event_year ?? undefined);
      setValue("is_lunar", event.is_lunar ?? false);
      setValue("is_recurring", event.is_recurring);
      setValue("event_type", event.event_type);
      setValue("country", event.country || "");
      setValue("country_code", event.country_code || "");
      setValue("flag_emoji", event.flag_emoji || "");
      setValue("short_description", event.short_description || "");
      setValue("image_url", event.image_url || "");
      setValue("importance", event.importance);
      setValue("tags", event.tags?.join(", ") || "");
      setValue("article_id", event.article_id || "");
    }
  }, [event, setValue]);

  const handleFormSubmit = (data: EventFormData) => {
    const tagsArray = data.tags
      ? data.tags
          .split(",")
          .map((t) => t.trim())
          .filter(Boolean)
      : undefined;

    // Convert NaN to undefined for number fields
    const safeNum = (val: number | undefined) =>
      typeof val === "number" && !Number.isNaN(val) ? val : undefined;

    onSubmit({
      title: data.title,
      event_date: data.event_date || undefined,
      event_day: safeNum(data.event_day) ?? 1,
      event_month: safeNum(data.event_month) ?? 1,
      event_year: safeNum(data.event_year),
      is_lunar: data.is_lunar,
      is_recurring: data.is_recurring,
      event_type: data.event_type,
      country: data.country || undefined,
      country_code: data.country_code || undefined,
      flag_emoji: data.flag_emoji || undefined,
      short_description: data.short_description || undefined,
      image_url: data.image_url || undefined,
      importance: data.importance,
      tags: tagsArray,
      article_id: data.article_id || undefined,
    });
  };

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Thông tin sự kiện</CardTitle>
          <CardDescription>{isEditing ? "Cập nhật sự kiện." : "Thêm sự kiện mới."}</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="title">Tiêu đề *</Label>
            <Input id="title" {...register("title")} placeholder="Ngày Quốc khánh Việt Nam..." />
            {errors.title && <p className="text-destructive text-xs">{errors.title.message}</p>}
          </div>

          <div className="grid gap-4 md:grid-cols-3">
            <div className="space-y-2">
              <Label htmlFor="event_day">
                Ngày *{watch("is_lunar") ? " (Âm lịch)" : " (Dương lịch)"}
              </Label>
              <Input
                id="event_day"
                type="number"
                {...register("event_day", { valueAsNumber: true })}
                min={1}
                max={31}
              />
              {errors.event_day && (
                <p className="text-destructive text-xs">{errors.event_day.message}</p>
              )}
            </div>
            <div className="space-y-2">
              <Label htmlFor="event_month">
                Tháng *{watch("is_lunar") ? " (Âm lịch)" : " (Dương lịch)"}
              </Label>
              <Input
                id="event_month"
                type="number"
                {...register("event_month", { valueAsNumber: true })}
                min={1}
                max={12}
              />
              {errors.event_month && (
                <p className="text-destructive text-xs">{errors.event_month.message}</p>
              )}
            </div>
            <div className="space-y-2">
              <Label htmlFor="event_year">Năm (tuỳ chọn)</Label>
              <Input
                id="event_year"
                type="number"
                {...register("event_year", { valueAsNumber: true })}
                placeholder="VD: 1945"
              />
            </div>
          </div>

          {/* is_lunar toggle */}
          <div className="flex items-center gap-3 rounded-lg border px-4 py-3">
            <Switch
              id="is_lunar"
              checked={watch("is_lunar")}
              onCheckedChange={(v) => setValue("is_lunar", v)}
            />
            <div>
              <Label htmlFor="is_lunar" className="cursor-pointer font-medium">
                Sự kiện theo Âm lịch
              </Label>
              <p className="text-muted-foreground text-xs">
                {watch("is_lunar")
                  ? "Ngày/Tháng ở trên là Âm lịch — sẽ hiển thị đúng ngày âm mỗi năm."
                  : "Ngày/Tháng ở trên là Dương lịch (mặc định)."}
              </p>
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="event_date">Ngày sự kiện (YYYY-MM-DD)</Label>
            <Input
              id="event_date"
              {...register("event_date")}
              placeholder="1945-09-02 (tuỳ chọn)"
            />
          </div>

          <div className="grid gap-4 md:grid-cols-3">
            <div className="space-y-2">
              <Label>Loại sự kiện</Label>
              <Select value={watch("event_type")} onValueChange={(v) => setValue("event_type", v)}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="historical_event">Lịch sử</SelectItem>
                  <SelectItem value="national_day">Quốc lễ</SelectItem>
                  <SelectItem value="world_day">Ngày quốc tế</SelectItem>
                  <SelectItem value="anniversary">Kỷ niệm</SelectItem>
                  <SelectItem value="cultural">Văn hoá</SelectItem>
                  <SelectItem value="military">Quân sự</SelectItem>
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

            <div className="space-y-2">
              <Label htmlFor="country">Quốc gia</Label>
              <Input id="country" {...register("country")} placeholder="Việt Nam" />
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="country_code">Mã quốc gia</Label>
              <Input id="country_code" {...register("country_code")} placeholder="VN" />
            </div>
            <div className="space-y-2">
              <Label htmlFor="flag_emoji">Flag Emoji</Label>
              <Input id="flag_emoji" {...register("flag_emoji")} placeholder="🇻🇳" />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="short_description">Mô tả ngắn</Label>
            <Textarea
              id="short_description"
              {...register("short_description")}
              placeholder="Mô tả ngắn gọn về sự kiện..."
              rows={3}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="tags">Tags (phân cách bằng dấu phẩy)</Label>
            <Input id="tags" {...register("tags")} placeholder="lịch sử, quốc khánh, Việt Nam..." />
          </div>

          {/* Event Image */}
          <ImagePickerV3
            value={watch("image_url")}
            onChange={(url: string) => setValue("image_url", url)}
            label="Hình ảnh sự kiện"
            aspectRatio="video"
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
          {isSubmitting ? "Đang xử lý..." : isEditing ? "Cập nhật" : "Tạo sự kiện"}
        </Button>
      </div>
    </form>
  );
}
