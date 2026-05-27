"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, Eye } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Switch } from "@/components/ui/switch";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { useCreateBanner, useUpdateBanner } from "@/hooks/useBanners";
import { ROUTES } from "@/lib/constants";
import { BANNER_TYPES } from "@/types/banner";
import type { Banner, CreateBannerRequest } from "@/types/banner";

interface BannerFormProps {
  banner?: Banner;
  isEdit?: boolean;
}

// Banner type color mapping (matches Android app)
const TYPE_COLORS: Record<string, { start: string; end: string }> = {
  feature: { start: "#BF360C", end: "#8D1A00" },
  content: { start: "#1565C0", end: "#0D47A1" },
  quiz: { start: "#2E7D32", end: "#1B5E20" },
  ai: { start: "#4A148C", end: "#311B92" },
  promo: { start: "#E65100", end: "#BF360C" },
  custom: { start: "#37474F", end: "#263238" },
};

export default function BannerForm({ banner, isEdit }: BannerFormProps) {
  const router = useRouter();
  const createBanner = useCreateBanner();
  const updateBanner = useUpdateBanner(banner?.id ?? "");

  const [form, setForm] = useState<CreateBannerRequest>({
    title: "",
    subtitle: "",
    image_url: "",
    cta_text: "",
    cta_route: "",
    bg_color: "",
    type: "feature",
    is_active: true,
    sort_order: 0,
    start_date: "",
    end_date: "",
  });

  useEffect(() => {
    if (banner) {
      setForm({
        title: banner.title,
        subtitle: banner.subtitle ?? "",
        image_url: banner.image_url ?? "",
        cta_text: banner.cta_text ?? "",
        cta_route: banner.cta_route ?? "",
        bg_color: banner.bg_color ?? "",
        type: banner.type,
        is_active: banner.is_active,
        sort_order: banner.sort_order,
        start_date: banner.start_date ? banner.start_date.slice(0, 16) : "",
        end_date: banner.end_date ? banner.end_date.slice(0, 16) : "",
      });
    }
  }, [banner]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const payload: CreateBannerRequest = {
      ...form,
      start_date: form.start_date ? new Date(form.start_date).toISOString() : undefined,
      end_date: form.end_date ? new Date(form.end_date).toISOString() : undefined,
    };

    // Clean empty strings
    if (!payload.subtitle) delete payload.subtitle;
    if (!payload.image_url) delete payload.image_url;
    if (!payload.cta_text) delete payload.cta_text;
    if (!payload.cta_route) delete payload.cta_route;
    if (!payload.bg_color) delete payload.bg_color;

    try {
      if (isEdit) {
        const res = await updateBanner.mutateAsync(payload);
        if (res.success) router.push(ROUTES.ADMIN_BANNERS);
      } else {
        const res = await createBanner.mutateAsync(payload);
        if (res.success) router.push(ROUTES.ADMIN_BANNERS);
      }
    } catch {
      // Error handled by mutation hooks
    }
  };

  const typeColors = TYPE_COLORS[form.type || "feature"] || TYPE_COLORS.feature;

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Link href={ROUTES.ADMIN_BANNERS}>
          <Button variant="ghost" size="sm">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Quay lại
          </Button>
        </Link>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">
            {isEdit ? "Sửa banner" : "Tạo banner mới"}
          </h1>
          <p className="text-muted-foreground">
            {isEdit
              ? "Chỉnh sửa thông tin banner hiện tại."
              : "Tạo banner mới để hiển thị trên ứng dụng."}
          </p>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Form */}
        <div className="lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>Thông tin banner</CardTitle>
              <CardDescription>
                Nhập thông tin banner. Banner sẽ hiển thị trên trang chủ ứng dụng di động.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-5">
                {/* Title */}
                <div className="space-y-2">
                  <Label htmlFor="title">Tiêu đề *</Label>
                  <Input
                    id="title"
                    value={form.title}
                    onChange={(e) => setForm({ ...form, title: e.target.value })}
                    placeholder="VD: Khám phá kiến thức lịch sử"
                    required
                  />
                </div>

                {/* Subtitle */}
                <div className="space-y-2">
                  <Label htmlFor="subtitle">Mô tả</Label>
                  <Textarea
                    id="subtitle"
                    value={form.subtitle}
                    onChange={(e) => setForm({ ...form, subtitle: e.target.value })}
                    placeholder="VD: Bài viết hay về văn hóa và truyền thống Việt Nam"
                    rows={2}
                  />
                </div>

                {/* Type & Color */}
                <div className="grid gap-4 sm:grid-cols-2">
                  <div className="space-y-2">
                    <Label>Loại banner</Label>
                    <Select
                      value={form.type}
                      onValueChange={(v) => setForm({ ...form, type: v })}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {BANNER_TYPES.map((t) => (
                          <SelectItem key={t.value} value={t.value}>
                            <span className="flex items-center gap-2">
                              <span
                                className="inline-block h-3 w-3 rounded-full"
                                style={{ backgroundColor: t.color }}
                              />
                              {t.label}
                            </span>
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="bg_color">Màu nền (hex, tùy chọn)</Label>
                    <div className="flex gap-2">
                      <Input
                        id="bg_color"
                        value={form.bg_color}
                        onChange={(e) => setForm({ ...form, bg_color: e.target.value })}
                        placeholder="#1565C0"
                        className="flex-1"
                      />
                      <input
                        type="color"
                        value={form.bg_color || typeColors.start}
                        onChange={(e) => setForm({ ...form, bg_color: e.target.value })}
                        className="h-10 w-10 cursor-pointer rounded border p-0.5"
                      />
                    </div>
                  </div>
                </div>

                {/* CTA */}
                <div className="grid gap-4 sm:grid-cols-2">
                  <div className="space-y-2">
                    <Label htmlFor="cta_text">Text nút CTA</Label>
                    <Input
                      id="cta_text"
                      value={form.cta_text}
                      onChange={(e) => setForm({ ...form, cta_text: e.target.value })}
                      placeholder="VD: Đọc ngay"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="cta_route">Route / URL đích</Label>
                    <Input
                      id="cta_route"
                      value={form.cta_route}
                      onChange={(e) => setForm({ ...form, cta_route: e.target.value })}
                      placeholder="VD: knowledge_feed hoặc https://..."
                    />
                    <p className="text-muted-foreground text-xs">
                      Nhập route trong app (gooddays, quiz_home, chat, knowledge_feed...) hoặc URL đầy đủ.
                    </p>
                  </div>
                </div>

                {/* Image URL */}
                <div className="space-y-2">
                  <Label htmlFor="image_url">URL hình ảnh (tùy chọn)</Label>
                  <Input
                    id="image_url"
                    value={form.image_url}
                    onChange={(e) => setForm({ ...form, image_url: e.target.value })}
                    placeholder="https://example.com/banner.jpg"
                  />
                </div>

                {/* Sort & Active */}
                <div className="grid gap-4 sm:grid-cols-2">
                  <div className="space-y-2">
                    <Label htmlFor="sort_order">Thứ tự hiển thị</Label>
                    <Input
                      id="sort_order"
                      type="number"
                      value={form.sort_order}
                      onChange={(e) =>
                        setForm({ ...form, sort_order: parseInt(e.target.value) || 0 })
                      }
                    />
                    <p className="text-muted-foreground text-xs">
                      Số nhỏ hơn sẽ hiển thị trước.
                    </p>
                  </div>
                  <div className="flex items-center gap-3 pt-6">
                    <Switch
                      id="is_active"
                      checked={form.is_active}
                      onCheckedChange={(v) => setForm({ ...form, is_active: v })}
                    />
                    <Label htmlFor="is_active">Kích hoạt</Label>
                  </div>
                </div>

                {/* Date range */}
                <div className="grid gap-4 sm:grid-cols-2">
                  <div className="space-y-2">
                    <Label htmlFor="start_date">Ngày bắt đầu (tùy chọn)</Label>
                    <Input
                      id="start_date"
                      type="datetime-local"
                      value={form.start_date}
                      onChange={(e) => setForm({ ...form, start_date: e.target.value })}
                    />
                    <p className="text-muted-foreground text-xs">
                      Để trống = hiển thị ngay lập tức.
                    </p>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="end_date">Ngày kết thúc (tùy chọn)</Label>
                    <Input
                      id="end_date"
                      type="datetime-local"
                      value={form.end_date}
                      onChange={(e) => setForm({ ...form, end_date: e.target.value })}
                    />
                    <p className="text-muted-foreground text-xs">
                      Để trống = hiển thị vô thời hạn.
                    </p>
                  </div>
                </div>

                {/* Submit */}
                <div className="flex gap-3 pt-4">
                  <Button
                    type="submit"
                    disabled={createBanner.isPending || updateBanner.isPending}
                  >
                    {createBanner.isPending || updateBanner.isPending
                      ? "Đang lưu..."
                      : isEdit
                        ? "Cập nhật"
                        : "Tạo banner"}
                  </Button>
                  <Link href={ROUTES.ADMIN_BANNERS}>
                    <Button variant="outline" type="button">
                      Hủy
                    </Button>
                  </Link>
                </div>
              </form>
            </CardContent>
          </Card>
        </div>

        {/* Live preview */}
        <div className="lg:col-span-1">
          <Card className="sticky top-20">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Eye className="h-4 w-4" />
                Xem trước
              </CardTitle>
              <CardDescription>
                Preview banner trên ứng dụng di động.
              </CardDescription>
            </CardHeader>
            <CardContent>
              {/* Phone mockup */}
              <div className="mx-auto w-full max-w-[280px]">
                <div
                  className="rounded-2xl p-3"
                  style={{
                    background: "linear-gradient(135deg, #C62828, #8B0000)",
                  }}
                >
                  {/* Mini preview banner */}
                  <div
                    className="flex items-center gap-3 rounded-xl p-3"
                    style={{
                      background: `linear-gradient(135deg, ${form.bg_color || typeColors.start}, ${typeColors.end})`,
                      minHeight: "60px",
                    }}
                  >
                    {/* Icon placeholder */}
                    <div
                      className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg"
                      style={{ backgroundColor: "rgba(255,255,255,0.15)" }}
                    >
                      <span className="text-xs text-white">
                        {form.type === "content"
                          ? "📄"
                          : form.type === "quiz"
                            ? "🧠"
                            : form.type === "ai"
                              ? "✨"
                              : form.type === "promo"
                                ? "🎁"
                                : "📅"}
                      </span>
                    </div>

                    {/* Text */}
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-xs font-bold text-white">
                        {form.title || "Tiêu đề banner"}
                      </p>
                      {form.subtitle && (
                        <p className="truncate text-[10px] text-white/80">
                          {form.subtitle}
                        </p>
                      )}
                    </div>

                    {/* CTA button */}
                    {form.cta_text && (
                      <div className="shrink-0 rounded-full bg-amber-100 px-2.5 py-1">
                        <span className="whitespace-nowrap text-[9px] font-semibold text-amber-900">
                          {form.cta_text} ›
                        </span>
                      </div>
                    )}
                  </div>

                  {/* Dots indicator */}
                  <div className="mt-2 flex justify-center gap-1">
                    <div className="h-1 w-3 rounded-full bg-white" />
                    <div className="h-1 w-1 rounded-full bg-white/40" />
                    <div className="h-1 w-1 rounded-full bg-white/40" />
                  </div>
                </div>
              </div>

              {/* Info */}
              <div className="mt-4 space-y-2 text-xs">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Loại:</span>
                  <span className="font-medium">
                    {BANNER_TYPES.find((t) => t.value === form.type)?.label || form.type}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Route:</span>
                  <span className="font-mono text-xs">{form.cta_route || "—"}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Trạng thái:</span>
                  <span
                    className={`font-medium ${form.is_active ? "text-green-600" : "text-red-500"}`}
                  >
                    {form.is_active ? "Kích hoạt" : "Ẩn"}
                  </span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
