"use client";

import { useState, useEffect, useRef } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";
import Link from "next/link";
import {
  ArrowLeft,
  Bell,
  BookOpen,
  Brain,
  CalendarDays,
  Clock3,
  Compass,
  Eye,
  Gift,
  Heart,
  Home,
  Loader2,
  MapPin,
  Moon,
  ScrollText,
  ShieldCheck,
  ShoppingBag,
  Sparkles,
  Star,
  Sun,
  Trophy,
  Upload,
  Users,
  Wallet,
  X,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";
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
import { getImageUrl } from "@/lib/utils";
import { uploadFile } from "@/services/mediaService";
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
  { value: "familytree", label: "Cây gia phả" },
  { value: "oracle_draw", label: "Rút thẻ" },
  { value: "daily_store", label: "Cửa hàng ngày" },
  { value: "ledger", label: "Lịch sử điểm" },
  { value: "zodiac_collection", label: "Bộ sưu tập con giáp" },
  { value: "date_picker", label: "Chọn ngày đẹp" },
  { value: "tiet_khi", label: "Tiết khí" },
  { value: "date_math", label: "Tính ngày" },
  { value: "birth_planner", label: "Kế hoạch sinh" },
  { value: "cycle_tracker", label: "Theo dõi chu kỳ" },
  { value: "world_clock", label: "Giờ thế giới" },
  { value: "widget_manager", label: "Quản lý widget" },
  { value: "leaderboard", label: "Bảng xếp hạng" },
] as const;

const BANNER_ICON_PRESETS: Array<{ key: string; label: string; Icon: LucideIcon }> = [
  { key: "calendar", label: "Lịch ngày", Icon: CalendarDays },
  { key: "article", label: "Bài viết", Icon: BookOpen },
  { key: "quiz", label: "Đố vui", Icon: Brain },
  { key: "ai", label: "AI", Icon: Sparkles },
  { key: "gift", label: "Quà tặng", Icon: Gift },
  { key: "star", label: "Nổi bật", Icon: Star },
  { key: "sun", label: "Ngày tốt", Icon: Sun },
  { key: "moon", label: "Âm lịch", Icon: Moon },
  { key: "clock", label: "Giờ đẹp", Icon: Clock3 },
  { key: "compass", label: "Phong thuỷ", Icon: Compass },
  { key: "scroll", label: "Văn khấn", Icon: ScrollText },
  { key: "bell", label: "Nhắc nhở", Icon: Bell },
  { key: "trophy", label: "Xếp hạng", Icon: Trophy },
  { key: "users", label: "Gia đình", Icon: Users },
  { key: "heart", label: "Sức khỏe", Icon: Heart },
  { key: "home", label: "Nhà cửa", Icon: Home },
  { key: "shop", label: "Cửa hàng", Icon: ShoppingBag },
  { key: "wallet", label: "Điểm thưởng", Icon: Wallet },
  { key: "map", label: "Vị trí", Icon: MapPin },
  { key: "shield", label: "Bảo hộ", Icon: ShieldCheck },
];

function inferCtaType(value?: string): "route" | "url" {
  return value?.startsWith("http://") || value?.startsWith("https://") ? "url" : "route";
}

export default function BannerForm({ banner, isEdit }: BannerFormProps) {
  const router = useRouter();
  const createBanner = useCreateBanner();
  const updateBanner = useUpdateBanner(banner?.id ?? "");
  const imageInputRef = useRef<HTMLInputElement>(null);
  const iconInputRef = useRef<HTMLInputElement>(null);
  const [isUploadingImage, setIsUploadingImage] = useState(false);
  const [isUploadingIcon, setIsUploadingIcon] = useState(false);

  const [form, setForm] = useState<CreateBannerRequest>(() =>
    banner
      ? {
          title: banner.title,
          subtitle: banner.subtitle ?? "",
          image_url: banner.image_url ?? "",
          icon_url: banner.icon_url ?? "",
          icon_key: banner.icon_key ?? "",
          cta_text: banner.cta_text ?? "",
          cta_type: banner.cta_type ?? inferCtaType(banner.cta_route),
          cta_route: banner.cta_route ?? "",
          bg_color: banner.bg_color ?? "",
          type: banner.type,
          is_active: banner.is_active,
          sort_order: banner.sort_order,
          start_date: banner.start_date ? banner.start_date.slice(0, 16) : "",
          end_date: banner.end_date ? banner.end_date.slice(0, 16) : "",
        }
      : {
          title: "",
          subtitle: "",
          image_url: "",
          icon_url: "",
          icon_key: "",
          cta_text: "",
          cta_type: "route",
          cta_route: "",
          bg_color: "",
          type: "feature",
          is_active: true,
          sort_order: 0,
          start_date: "",
          end_date: "",
        }
  );

  useEffect(() => {
    if (banner) {
      setForm({
        title: banner.title,
        subtitle: banner.subtitle ?? "",
        image_url: banner.image_url ?? "",
        icon_url: banner.icon_url ?? "",
        icon_key: banner.icon_key ?? "",
        cta_text: banner.cta_text ?? "",
        cta_type: banner.cta_type ?? inferCtaType(banner.cta_route),
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

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setIsUploadingImage(true);
    try {
      const res = await uploadFile(file, "banners");
      const uploadedUrl = res.data?.url;
      if (res.success && uploadedUrl) {
        setForm((current) => ({ ...current, image_url: uploadedUrl }));
      }
    } finally {
      setIsUploadingImage(false);
      if (imageInputRef.current) imageInputRef.current.value = "";
    }
  };

  const handleIconUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setIsUploadingIcon(true);
    try {
      const res = await uploadFile(file, "banners/icons");
      const uploadedUrl = res.data?.url;
      if (res.success && uploadedUrl) {
        setForm((current) => ({ ...current, icon_url: uploadedUrl, icon_key: "" }));
      }
    } finally {
      setIsUploadingIcon(false);
      if (iconInputRef.current) iconInputRef.current.value = "";
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const payload: CreateBannerRequest = {
      ...form,
      start_date: form.start_date ? new Date(form.start_date).toISOString() : undefined,
      end_date: form.end_date ? new Date(form.end_date).toISOString() : undefined,
    };

    if (payload.cta_type === "url" && payload.cta_route && !/^https?:\/\//.test(payload.cta_route)) {
      payload.cta_route = `https://${payload.cta_route}`;
    }

    // Clean empty strings on create. On edit, keep empty strings so fields can be cleared.
    if (!isEdit && !payload.subtitle) delete payload.subtitle;
    if (!isEdit && !payload.image_url) delete payload.image_url;
    if (!isEdit && !payload.icon_url) delete payload.icon_url;
    if (!isEdit && !payload.icon_key) delete payload.icon_key;
    if (!isEdit && !payload.cta_text) delete payload.cta_text;
    if (!payload.cta_route && !isEdit) {
      delete payload.cta_route;
      delete payload.cta_type;
    }
    if (!isEdit && !payload.bg_color) delete payload.bg_color;

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
  const selectedIconPreset = BANNER_ICON_PRESETS.find((preset) => preset.key === form.icon_key);
  const PreviewIcon = selectedIconPreset?.Icon;

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
                    <Label>Loại đích</Label>
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
                        <SelectItem value="route">Route trong app</SelectItem>
                        <SelectItem value="url">URL ngoài</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="cta_route">
                    {form.cta_type === "url" ? "URL đích" : "Route trong app"}
                  </Label>
                  {form.cta_type === "url" ? (
                    <Input
                      id="cta_route"
                      type="url"
                      value={form.cta_route}
                      onChange={(e) => setForm({ ...form, cta_route: e.target.value })}
                      placeholder="https://lichso.vn/bai-viet/..."
                    />
                  ) : (
                    <Select
                      value={form.cta_route || undefined}
                      onValueChange={(value) => setForm({ ...form, cta_route: value })}
                    >
                      <SelectTrigger id="cta_route">
                        <SelectValue placeholder="Chọn màn hình trong app" />
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
                  <p className="text-muted-foreground text-xs">
                    {form.cta_type === "url"
                      ? "Khi người dùng bấm banner trên Android, URL sẽ mở bằng trình duyệt."
                      : "Route được chọn sẽ điều hướng trực tiếp trong app Android."}
                  </p>
                </div>

                {/* Icon upload */}
                <div className="space-y-2">
                  <Label>Icon banner (tùy chọn)</Label>
                  <div className="rounded-xl border bg-muted/30 p-3 text-xs text-muted-foreground">
                    <p className="font-medium text-foreground">Chọn icon có sẵn hoặc upload icon riêng</p>
                    <ul className="mt-2 list-disc space-y-1 pl-4">
                      <li>Bộ icon có sẵn đã được tối ưu để hiển thị đồng bộ trong app Android.</li>
                      <li>Kích thước khuyến nghị: 256 x 256 px hoặc 512 x 512 px, tỉ lệ vuông 1:1.</li>
                      <li>Ưu tiên PNG/WebP nền trong suốt để icon nổi tốt trên mọi màu banner.</li>
                      <li>Nên dùng biểu tượng đơn giản, ít chi tiết; tránh chữ nhỏ vì icon trên app khá nhỏ.</li>
                      <li>Chừa khoảng trống an toàn quanh icon khoảng 20% để không bị sát viền.</li>
                      <li>Icon upload sẽ ưu tiên hơn icon có sẵn. Nếu không chọn gì, app dùng icon mặc định theo loại banner.</li>
                    </ul>
                  </div>
                  <div className="grid grid-cols-2 gap-2 sm:grid-cols-4 lg:grid-cols-5">
                    {BANNER_ICON_PRESETS.map(({ key, label, Icon }) => {
                      const isSelected = form.icon_key === key && !form.icon_url;
                      return (
                        <button
                          key={key}
                          type="button"
                          onClick={() => setForm({ ...form, icon_key: key, icon_url: "" })}
                          className={`flex min-h-20 flex-col items-center justify-center gap-2 rounded-xl border p-3 text-center transition ${
                            isSelected
                              ? "border-primary bg-primary/10 text-primary"
                              : "border-border bg-background hover:border-primary/40 hover:bg-muted/50"
                          }`}
                        >
                          <Icon className="h-6 w-6" />
                          <span className="text-xs font-medium">{label}</span>
                        </button>
                      );
                    })}
                  </div>
                  <input
                    ref={iconInputRef}
                    type="file"
                    accept="image/*"
                    className="hidden"
                    onChange={handleIconUpload}
                  />
                  <div className="flex flex-wrap gap-2">
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => iconInputRef.current?.click()}
                      disabled={isUploadingIcon}
                    >
                      {isUploadingIcon ? (
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      ) : (
                        <Upload className="mr-2 h-4 w-4" />
                      )}
                      {isUploadingIcon ? "Đang upload..." : "Upload icon"}
                    </Button>
                    {(form.icon_url || form.icon_key) && (
                      <Button
                        type="button"
                        variant="ghost"
                        onClick={() => setForm({ ...form, icon_url: "", icon_key: "" })}
                      >
                        <X className="mr-2 h-4 w-4" />
                        Xóa icon
                      </Button>
                    )}
                  </div>
                  {selectedIconPreset && PreviewIcon && !form.icon_url && (
                    <div className="flex items-center gap-3 rounded-xl border p-3">
                      <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-muted text-primary">
                        <PreviewIcon className="h-8 w-8" />
                      </div>
                      <p className="text-muted-foreground text-xs">
                        Đang dùng icon có sẵn: {selectedIconPreset.label}. App Android sẽ hiển thị icon này trên banner.
                      </p>
                    </div>
                  )}
                  {form.icon_url && (
                    <div className="flex items-center gap-3 rounded-xl border p-3">
                      <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-muted">
                        <Image
                          src={getImageUrl(form.icon_url)}
                          alt="Icon preview"
                          width={48}
                          height={48}
                          className="h-12 w-12 object-contain"
                        />
                      </div>
                      <p className="text-muted-foreground text-xs">
                        Icon sẽ được app Android ưu tiên hiển thị thay cho icon mặc định.
                      </p>
                    </div>
                  )}
                  <p className="text-muted-foreground text-xs">
                    Icon sẽ được upload vào thư mục media `banners/icons`.
                  </p>
                </div>

                {/* Image upload */}
                <div className="space-y-2">
                  <Label>Hình ảnh banner (tùy chọn)</Label>
                  <div className="rounded-xl border bg-muted/30 p-3 text-xs text-muted-foreground">
                    <p className="font-medium text-foreground">Gợi ý ảnh banner đẹp trên app</p>
                    <ul className="mt-2 list-disc space-y-1 pl-4">
                      <li>Kích thước khuyến nghị: 1200 x 420 px, tỉ lệ khoảng 20:7.</li>
                      <li>Vùng nội dung quan trọng nên nằm giữa ảnh, tránh sát mép vì app sẽ crop theo màn hình.</li>
                      <li>Ưu tiên WebP, JPG hoặc PNG; dung lượng nên dưới 1 MB để tải nhanh trên mobile.</li>
                      <li>Không nên đặt quá nhiều chữ trong ảnh; hãy dùng Tiêu đề, Mô tả và CTA của banner để hiển thị text.</li>
                      <li>Chọn ảnh có nền đủ tương phản với chữ trắng hoặc phủ màu nền banner để nội dung dễ đọc.</li>
                    </ul>
                  </div>
                  <input
                    ref={imageInputRef}
                    type="file"
                    accept="image/*"
                    className="hidden"
                    onChange={handleImageUpload}
                  />
                  <div className="flex flex-wrap gap-2">
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => imageInputRef.current?.click()}
                      disabled={isUploadingImage}
                    >
                      {isUploadingImage ? (
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      ) : (
                        <Upload className="mr-2 h-4 w-4" />
                      )}
                      {isUploadingImage ? "Đang upload..." : "Upload banner"}
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
                    <div className="overflow-hidden rounded-xl border">
                      <Image
                        src={getImageUrl(form.image_url)}
                        alt="Banner preview"
                        width={800}
                        height={320}
                        className="h-40 w-full object-cover"
                      />
                    </div>
                  )}
                  <p className="text-muted-foreground text-xs">
                    Ảnh sẽ được upload vào thư mục media `banners`.
                  </p>
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
                    className="relative flex items-center gap-3 overflow-hidden rounded-xl p-3"
                    style={{
                      background: `linear-gradient(135deg, ${form.bg_color || typeColors.start}, ${typeColors.end})`,
                      minHeight: "60px",
                    }}
                  >
                    {form.image_url && (
                      <Image
                        src={getImageUrl(form.image_url)}
                        alt=""
                        fill
                        sizes="280px"
                        className="object-cover opacity-80"
                      />
                    )}
                    {form.image_url && (
                      <div className="absolute inset-0 bg-gradient-to-r from-black/45 via-black/25 to-black/10" />
                    )}
                    {/* Icon placeholder */}
                    <div
                      className="relative flex h-9 w-9 shrink-0 items-center justify-center rounded-lg"
                      style={{ backgroundColor: "rgba(255,255,255,0.15)" }}
                    >
                      {form.icon_url ? (
                        <Image
                          src={getImageUrl(form.icon_url)}
                          alt=""
                          width={28}
                          height={28}
                          className="h-7 w-7 object-contain"
                        />
                      ) : PreviewIcon ? (
                        <PreviewIcon className="h-6 w-6 text-white" />
                      ) : (
                        <span className="text-[10px] font-bold uppercase text-white">
                          {form.type === "content"
                            ? "BV"
                            : form.type === "quiz"
                              ? "QZ"
                              : form.type === "ai"
                                ? "AI"
                                : form.type === "promo"
                                  ? "KM"
                                  : "LS"}
                        </span>
                      )}
                    </div>

                    {/* Text */}
                    <div className="relative min-w-0 flex-1">
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
                      <div className="relative shrink-0 rounded-full bg-amber-100 px-2.5 py-1">
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
                  <span className="text-muted-foreground">
                    {form.cta_type === "url" ? "URL:" : "Route:"}
                  </span>
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
