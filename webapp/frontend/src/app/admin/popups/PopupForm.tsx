"use client";

import { useState, useEffect, useRef } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";
import Link from "next/link";
import {
  ArrowLeft,
  Eye,
  Loader2,
  Upload,
  X,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
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
import { useCreatePopup, useUpdatePopup } from "@/hooks/usePopups";
import { ROUTES } from "@/lib/constants";
import { getImageUrl } from "@/lib/utils";
import { POPUP_POSITIONS } from "@/types/popup";
import type { Popup, CreatePopupRequest } from "@/types/popup";
import { MediaPickerDialog } from "@/components/shared/MediaPickerDialog";
import type { MediaFile } from "@/types/media";

interface PopupFormProps {
  popup?: Popup;
  isEdit?: boolean;
}

const APP_ROUTES = [
  { value: "home", label: "Trang chủ" },
  { value: "calendar", label: "Lịch tháng" },
  { value: "gooddays", label: "Ngày tốt/xấu" },
  { value: "knowledge_feed", label: "Bài viết khám phá" },
  { value: "quiz_home", label: "Đố vui" },
  { value: "chat", label: "AI Tử Vi" },
  { value: "tools", label: "Tiện ích" },
  { value: "prayers", label: "Văn khấn" },
  { value: "history", label: "Ngày này năm xưa" },
  { value: "profile", label: "Hồ sơ" },
  { value: "bookmarks", label: "Ngày đã lưu" },
  { value: "tasks", label: "Ghi chú" },
  { value: "countdown", label: "Đếm ngày" },
  { value: "oracle_draw", label: "Rút thẻ" },
  { value: "daily_store", label: "Cửa hàng ngày" },
  { value: "tiet_khi", label: "Tiết khí" },
] as const;

function inferCtaType(value?: string): "route" | "url" {
  return value?.startsWith("http://") || value?.startsWith("https://") ? "url" : "route";
}

export default function PopupForm({ popup, isEdit }: PopupFormProps) {
  const router = useRouter();
  const createPopup = useCreatePopup();
  const updatePopup = useUpdatePopup(popup?.id ?? "");

  const [isMediaPickerOpen, setIsMediaPickerOpen] = useState(false);

  const [form, setForm] = useState<CreatePopupRequest>(() =>
    popup
      ? {
          title: popup.title,
          image_url: popup.image_url,
          cta_type: popup.cta_type ?? inferCtaType(popup.cta_route),
          cta_route: popup.cta_route ?? "",
          position: popup.position,
          is_active: popup.is_active,
          start_date: popup.start_date ? popup.start_date.slice(0, 16) : "",
          end_date: popup.end_date ? popup.end_date.slice(0, 16) : "",
        }
      : {
          title: "",
          image_url: "",
          cta_type: "route",
          cta_route: "",
          position: "center_right",
          is_active: true,
          start_date: "",
          end_date: "",
        }
  );

  useEffect(() => {
    if (popup) {
      setForm({
        title: popup.title,
        image_url: popup.image_url,
        cta_type: popup.cta_type ?? inferCtaType(popup.cta_route),
        cta_route: popup.cta_route ?? "",
        position: popup.position,
        is_active: popup.is_active,
        start_date: popup.start_date ? popup.start_date.slice(0, 16) : "",
        end_date: popup.end_date ? popup.end_date.slice(0, 16) : "",
      });
    }
  }, [popup]);

  const handleMediaSelect = (file: MediaFile) => {
    setForm((current) => ({ ...current, image_url: file.url }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const payload: CreatePopupRequest = {
      ...form,
      start_date: form.start_date ? new Date(form.start_date).toISOString() : undefined,
      end_date: form.end_date ? new Date(form.end_date).toISOString() : undefined,
    };

    if (payload.cta_type === "url" && payload.cta_route && !/^https?:\/\//.test(payload.cta_route)) {
      payload.cta_route = `https://${payload.cta_route}`;
    }

    if (!payload.cta_route && !isEdit) {
      delete payload.cta_route;
      delete payload.cta_type;
    }

    try {
      if (isEdit) {
        const res = await updatePopup.mutateAsync(payload);
        if (res.success) router.push(ROUTES.ADMIN_POPUPS);
      } else {
        const res = await createPopup.mutateAsync(payload);
        if (res.success) router.push(ROUTES.ADMIN_POPUPS);
      }
    } catch {
      // Error handled by mutation hooks
    }
  };

  const positionClasses: Record<string, string> = {
    center_left: "top-1/2 left-4 -translate-y-1/2",
    center_right: "top-1/2 right-4 -translate-y-1/2",
    top_left: "top-4 left-4",
    top_right: "top-4 right-4",
    bottom_left: "bottom-4 left-4",
    bottom_right: "bottom-4 right-4",
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Link href={ROUTES.ADMIN_POPUPS}>
          <Button variant="ghost" size="sm">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Quay lại
          </Button>
        </Link>
        <div>
          <h1 className="text-2xl font-bold tracking-tight">
            {isEdit ? "Sửa popup" : "Tạo popup mới"}
          </h1>
          <p className="text-muted-foreground">
            {isEdit
              ? "Chỉnh sửa thông tin popup nổi hiện tại."
              : "Tạo popup nổi mới để hiển thị trên ứng dụng di động Lịch Số."}
          </p>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Form */}
        <div className="lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>Thông tin popup</CardTitle>
              <CardDescription>
                Nhập thông tin chi tiết của popup hiển thị đè (overlay) trên các màn hình.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-5">
                {/* Title */}
                <div className="space-y-2">
                  <Label htmlFor="title">Tên hiển thị / Tiêu đề *</Label>
                  <Input
                    id="title"
                    value={form.title}
                    onChange={(e) => setForm({ ...form, title: e.target.value })}
                    placeholder="VD: Popup Thử tài cùng Lịch Số"
                    required
                  />
                </div>

                {/* Position */}
                <div className="space-y-2">
                  <Label htmlFor="position">Vị trí mặc định trên màn hình</Label>
                  <Select
                    value={form.position}
                    onValueChange={(v) =>
                      setForm({ ...form, position: v as any })
                    }
                  >
                    <SelectTrigger id="position">
                      <SelectValue placeholder="Chọn vị trí ban đầu" />
                    </SelectTrigger>
                    <SelectContent>
                      {POPUP_POSITIONS.map((pos) => (
                        <SelectItem key={pos.value} value={pos.value}>
                          {pos.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <p className="text-muted-foreground text-xs">
                    Đây là vị trí popup xuất hiện lần đầu. Người dùng vẫn di chuyển (drag) được popup này bằng tay.
                  </p>
                </div>

                {/* Image upload */}
                <div className="space-y-2">
                  <Label>Hình ảnh popup *</Label>
                  <div className="rounded-xl border bg-muted/30 p-3 text-xs text-muted-foreground">
                    <p className="font-medium text-foreground">Gợi ý thiết kế popup nổi</p>
                    <ul className="mt-2 list-disc space-y-1 pl-4">
                      <li>Khuyên dùng hình ảnh có định dạng PNG/WebP trong suốt (transparent).</li>
                      <li>Kích thước khuyến nghị: khoảng 400x400 px đến 600x600 px (tỉ lệ 1:1 hoặc hình dáng tự do).</li>
                      <li>Tránh ảnh nền đục (trắng/đen) để ảnh hiển thị nổi bật và hoà hợp với màn hình app.</li>
                    </ul>
                  </div>
                  <div className="flex flex-wrap gap-2 mt-2">
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => setIsMediaPickerOpen(true)}
                    >
                      <Upload className="mr-2 h-4 w-4" />
                      Chọn ảnh popup từ Media
                    </Button>
                    {form.image_url && (
                      <Button
                        type="button"
                        variant="ghost"
                        onClick={() => setForm({ ...form, image_url: "" })}
                      >
                        <X className="mr-2 h-4 w-4" />
                        Xóa ảnh
                      </Button>
                    )}
                  </div>
                  {form.image_url && (
                    <div className="mt-2 overflow-hidden rounded-xl border max-w-[200px] bg-muted/10 p-2">
                      <Image
                        src={getImageUrl(form.image_url)}
                        alt="Popup image preview"
                        width={200}
                        height={200}
                        className="object-contain max-h-[200px] w-full"
                      />
                    </div>
                  )}
                </div>

                {/* CTA destinations */}
                <div className="grid gap-4 sm:grid-cols-2">
                  <div className="space-y-2">
                    <Label>Loại đích đến</Label>
                    <Select
                      value={form.cta_type ?? "route"}
                      onValueChange={(value) =>
                        setForm({ ...form, cta_type: value as "route" | "url", cta_route: "" })
                      }
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="route">Màn hình trong app</SelectItem>
                        <SelectItem value="url">Trang web (URL ngoài)</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="cta_route">
                      {form.cta_type === "url" ? "URL đích" : "Màn hình đích"}
                    </Label>
                    {form.cta_type === "url" ? (
                      <Input
                        id="cta_route"
                        type="url"
                        value={form.cta_route}
                        onChange={(e) => setForm({ ...form, cta_route: e.target.value })}
                        placeholder="https://lichso.vn/..."
                      />
                    ) : (
                      <Select
                        value={form.cta_route || undefined}
                        onValueChange={(value) => setForm({ ...form, cta_route: value })}
                      >
                        <SelectTrigger id="cta_route">
                          <SelectValue placeholder="Chọn màn hình" />
                        </SelectTrigger>
                        <SelectContent>
                          {APP_ROUTES.map((route) => (
                            <SelectItem key={route.value} value={route.value}>
                              {route.label} ({route.value})
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    )}
                  </div>
                </div>

                {/* Display dates & Active */}
                <div className="grid gap-4 sm:grid-cols-2">
                  <div className="space-y-2">
                    <Label htmlFor="start_date">Ngày bắt đầu (tùy chọn)</Label>
                    <Input
                      id="start_date"
                      type="datetime-local"
                      value={form.start_date}
                      onChange={(e) => setForm({ ...form, start_date: e.target.value })}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="end_date">Ngày kết thúc (tùy chọn)</Label>
                    <Input
                      id="end_date"
                      type="datetime-local"
                      value={form.end_date}
                      onChange={(e) => setForm({ ...form, end_date: e.target.value })}
                    />
                  </div>
                </div>

                <div className="flex items-center gap-3 pt-2">
                  <Switch
                    id="is_active"
                    checked={form.is_active}
                    onCheckedChange={(v) => setForm({ ...form, is_active: v })}
                  />
                  <Label htmlFor="is_active">Kích hoạt hiển thị</Label>
                </div>

                {/* Submit */}
                <div className="flex gap-3 pt-4">
                  <Button
                    type="submit"
                    disabled={createPopup.isPending || updatePopup.isPending || !form.image_url}
                  >
                    {createPopup.isPending || updatePopup.isPending
                      ? "Đang lưu..."
                      : isEdit
                        ? "Cập nhật"
                        : "Tạo popup"}
                  </Button>
                  <Link href={ROUTES.ADMIN_POPUPS}>
                    <Button variant="outline" type="button">
                      Hủy
                    </Button>
                  </Link>
                </div>
              </form>
            </CardContent>
          </Card>
        </div>

        {/* Live Preview */}
        <div className="lg:col-span-1">
          <Card className="sticky top-20">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Eye className="h-4 w-4" />
                Xem trước trên mobile
              </CardTitle>
              <CardDescription>
                Mô phỏng vị trí hiển thị popup nổi ban đầu.
              </CardDescription>
            </CardHeader>
            <CardContent>
              {/* Phone mockup */}
              <div className="mx-auto w-full max-w-[280px]">
                <div
                  className="relative h-[480px] rounded-2xl border-4 border-slate-700 bg-slate-900 p-2 overflow-hidden"
                  style={{
                    background: "linear-gradient(135deg, #1e1b4b, #31102f)",
                  }}
                >
                  {/* Status Bar */}
                  <div className="flex justify-between px-2 text-[9px] text-white/50 mb-4 select-none">
                    <span>13:07</span>
                    <span>5G LTE</span>
                  </div>

                  {/* App Layout Simulation */}
                  <div className="space-y-3 opacity-30 select-none">
                    <div className="h-6 w-1/2 bg-white/20 rounded" />
                    <div className="h-28 w-full bg-white/10 rounded-xl" />
                    <div className="grid grid-cols-4 gap-2">
                      <div className="h-10 bg-white/15 rounded-lg" />
                      <div className="h-10 bg-white/15 rounded-lg" />
                      <div className="h-10 bg-white/15 rounded-lg" />
                      <div className="h-10 bg-white/15 rounded-lg" />
                    </div>
                    <div className="h-28 w-full bg-white/10 rounded-xl" />
                  </div>

                  {/* Popup Floating Container */}
                  {form.image_url ? (
                    <div
                      className={`absolute w-32 h-32 flex flex-col items-center justify-center transition-all duration-300 ${
                        positionClasses[form.position || "center"]
                      }`}
                    >
                      {/* Image Box */}
                      <div className="relative w-24 h-24 bg-transparent border-2 border-dashed border-white/20 rounded-lg group select-none">
                        <Image
                          src={getImageUrl(form.image_url)}
                          alt=""
                          fill
                          sizes="96px"
                          className="object-contain animate-bounce"
                        />
                        {/* Close button simulator */}
                        <div className="absolute -top-1.5 -right-1.5 bg-red-600 rounded-full p-0.5 text-white shadow-lg border border-white">
                          <X className="h-2.5 w-2.5" />
                        </div>
                      </div>
                      {/* Label Position */}
                      <span className="text-[8px] bg-black/60 text-white px-1.5 py-0.5 rounded mt-1 font-mono">
                        {form.position}
                      </span>
                    </div>
                  ) : (
                    <div className="absolute inset-0 flex items-center justify-center">
                      <span className="text-white/40 text-xs">Vui lòng upload ảnh popup</span>
                    </div>
                  )}

                  {/* App Bottom Navigation Simulation */}
                  <div className="absolute bottom-0 inset-x-0 h-10 bg-slate-950/80 border-t border-white/5 flex items-center justify-around opacity-30 select-none">
                    <div className="h-3 w-3 bg-white/25 rounded-full" />
                    <div className="h-3 w-3 bg-white/25 rounded-full" />
                    <div className="h-5 w-5 bg-white/40 rounded-full" />
                    <div className="h-3 w-3 bg-white/25 rounded-full" />
                    <div className="h-3 w-3 bg-white/25 rounded-full" />
                  </div>
                </div>
              </div>

              {/* Info */}
              <div className="mt-4 space-y-2 text-xs">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">CTA Đích:</span>
                  <span className="font-mono">{form.cta_route || "Trang chủ (mặc định)"}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Kích hoạt:</span>
                  <span
                    className={`font-semibold ${form.is_active ? "text-green-500" : "text-red-500"}`}
                  >
                    {form.is_active ? "Có" : "Không"}
                  </span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      <MediaPickerDialog
        open={isMediaPickerOpen}
        onOpenChange={setIsMediaPickerOpen}
        onSelect={handleMediaSelect}
        title="Chọn ảnh popup"
        imagesOnly={true}
      />
    </div>
  );
}
