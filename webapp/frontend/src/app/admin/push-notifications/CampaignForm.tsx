"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
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
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { useCreateCampaign, useUpdateCampaign } from "@/hooks/usePushNotifications";
import type { PushCampaign, CreateCampaignRequest } from "@/types/push-notification";
import { ROUTES } from "@/lib/constants";
import { Bell, Image, Link, Database } from "lucide-react";

interface CampaignFormProps {
  campaign?: PushCampaign;
}

export function CampaignForm({ campaign }: CampaignFormProps) {
  const router = useRouter();
  const isEdit = !!campaign;

  const [title, setTitle] = useState(campaign?.title ?? "");
  const [body, setBody] = useState(campaign?.body ?? "");
  const [imageURL, setImageURL] = useState(campaign?.image_url ?? "");
  const [clickAction, setClickAction] = useState(campaign?.click_action ?? "");
  const [targetType, setTargetType] = useState<"all" | "users">(campaign?.target_type ?? "all");
  const [targetUsers, setTargetUsers] = useState(campaign?.target_users ?? "");
  const [dataKey, setDataKey] = useState("");
  const [dataValue, setDataValue] = useState("");
  const [dataEntries, setDataEntries] = useState<Record<string, string>>(() => {
    if (!campaign?.data_payload || campaign.data_payload === "{}") return {};
    try {
      return JSON.parse(campaign.data_payload);
    } catch {
      return {};
    }
  });

  const createCampaign = useCreateCampaign();
  const updateCampaign = useUpdateCampaign();
  const isPending = createCampaign.isPending || updateCampaign.isPending;

  const addDataEntry = () => {
    if (!dataKey.trim()) return;
    setDataEntries((prev) => ({ ...prev, [dataKey.trim()]: dataValue.trim() }));
    setDataKey("");
    setDataValue("");
  };

  const removeDataEntry = (key: string) => {
    setDataEntries((prev) => {
      const next = { ...prev };
      delete next[key];
      return next;
    });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const payload: CreateCampaignRequest = {
      title,
      body,
      image_url: imageURL || undefined,
      click_action: clickAction || undefined,
      data_payload: Object.keys(dataEntries).length > 0 ? dataEntries : undefined,
      target_type: targetType,
      target_users:
        targetType === "users"
          ? targetUsers.split(",").map((s) => s.trim()).filter(Boolean)
          : undefined,
    };

    if (isEdit && campaign) {
      updateCampaign.mutate(
        { id: campaign.id, data: payload },
        { onSuccess: () => router.push(ROUTES.ADMIN_PUSH_NOTIFICATIONS) }
      );
    } else {
      createCampaign.mutate(payload, {
        onSuccess: () => router.push(ROUTES.ADMIN_PUSH_NOTIFICATIONS),
      });
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* Nội dung thông báo */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base flex items-center gap-2">
            <Bell className="w-4 h-4" />
            Nội dung thông báo
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="title">
              Tiêu đề <span className="text-destructive">*</span>
            </Label>
            <Input
              id="title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Tiêu đề thông báo"
              required
              maxLength={255}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="body">
              Nội dung <span className="text-destructive">*</span>
            </Label>
            <Textarea
              id="body"
              value={body}
              onChange={(e) => setBody(e.target.value)}
              placeholder="Nội dung thông báo hiển thị trên thiết bị..."
              required
              rows={4}
            />
          </div>
        </CardContent>
      </Card>

      {/* Hình ảnh & hành động */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base flex items-center gap-2">
            <Image className="w-4 h-4" />
            Hình ảnh & hành động
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="image_url">URL hình ảnh</Label>
            <Input
              id="image_url"
              type="url"
              value={imageURL}
              onChange={(e) => setImageURL(e.target.value)}
              placeholder="https://..."
            />
            <p className="text-xs text-muted-foreground">
              Hình ảnh hiển thị trong thông báo (tùy chọn)
            </p>
          </div>

          <div className="space-y-2">
            <Label htmlFor="click_action" className="flex items-center gap-1">
              <Link className="w-3 h-3" />
              Click action (deep link)
            </Label>
            <Input
              id="click_action"
              value={clickAction}
              onChange={(e) => setClickAction(e.target.value)}
              placeholder="OPEN_ARTICLE / lichso://article/123"
            />
            <p className="text-xs text-muted-foreground">
              Màn hình mở khi người dùng nhấn vào thông báo
            </p>
          </div>
        </CardContent>
      </Card>

      {/* Đối tượng gửi */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Đối tượng gửi</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label>Loại đối tượng</Label>
            <Select
              value={targetType}
              onValueChange={(v) => setTargetType(v as "all" | "users")}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Tất cả người dùng</SelectItem>
                <SelectItem value="users">Người dùng cụ thể</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {targetType === "users" && (
            <div className="space-y-2">
              <Label htmlFor="target_users">User IDs</Label>
              <Textarea
                id="target_users"
                value={targetUsers}
                onChange={(e) => setTargetUsers(e.target.value)}
                placeholder="uuid1, uuid2, uuid3 (cách nhau bằng dấu phẩy)"
                rows={3}
              />
            </div>
          )}
        </CardContent>
      </Card>

      {/* Data payload (extra key-value) */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base flex items-center gap-2">
            <Database className="w-4 h-4" />
            Data payload
            <span className="text-xs text-muted-foreground font-normal">(tùy chọn)</span>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <p className="text-sm text-muted-foreground">
            Dữ liệu bổ sung gửi kèm thông báo, dùng cho deep link phức tạp hoặc analytics.
          </p>

          {Object.entries(dataEntries).length > 0 && (
            <div className="space-y-2">
              {Object.entries(dataEntries).map(([k, v]) => (
                <div key={k} className="flex items-center gap-2 text-sm">
                  <code className="bg-muted px-2 py-1 rounded text-xs">{k}</code>
                  <span className="text-muted-foreground">:</span>
                  <code className="bg-muted px-2 py-1 rounded text-xs flex-1">{v}</code>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    className="h-6 px-2 text-destructive"
                    onClick={() => removeDataEntry(k)}
                  >
                    Xóa
                  </Button>
                </div>
              ))}
              <Separator />
            </div>
          )}

          <div className="flex gap-2">
            <Input
              value={dataKey}
              onChange={(e) => setDataKey(e.target.value)}
              placeholder="key"
              className="w-32"
            />
            <Input
              value={dataValue}
              onChange={(e) => setDataValue(e.target.value)}
              placeholder="value"
              className="flex-1"
              onKeyDown={(e) => e.key === "Enter" && (e.preventDefault(), addDataEntry())}
            />
            <Button type="button" variant="outline" onClick={addDataEntry}>
              Thêm
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Preview */}
      {(title || body) && (
        <Card className="border-dashed">
          <CardHeader>
            <CardTitle className="text-sm text-muted-foreground">Xem trước thông báo</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="bg-muted rounded-xl p-4 max-w-sm">
              <div className="flex items-start gap-3">
                <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center flex-shrink-0">
                  <Bell className="w-5 h-5 text-primary" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-semibold text-sm truncate">{title || "Tiêu đề"}</p>
                  <p className="text-xs text-muted-foreground line-clamp-2 mt-0.5">
                    {body || "Nội dung thông báo..."}
                  </p>
                </div>
              </div>
              {imageURL && (
                <div className="mt-3 rounded-lg overflow-hidden aspect-video bg-muted-foreground/10">
                  <img src={imageURL} alt="preview" className="w-full h-full object-cover" />
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      )}

      <div className="flex gap-3 justify-end">
        <Button
          type="button"
          variant="outline"
          onClick={() => router.push(ROUTES.ADMIN_PUSH_NOTIFICATIONS)}
        >
          Hủy
        </Button>
        <Button type="submit" disabled={isPending}>
          {isPending ? "Đang lưu..." : isEdit ? "Cập nhật" : "Tạo chiến dịch"}
        </Button>
      </div>
    </form>
  );
}
