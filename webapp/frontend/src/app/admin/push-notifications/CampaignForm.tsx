"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Search, Bell, X, CheckCircle2, Users, User } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Separator } from "@/components/ui/separator";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";
import { ClickActionSelect } from "./ClickActionSelect";
import {
  Dialog, DialogContent, DialogHeader, DialogTitle,
} from "@/components/ui/dialog";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useCreateCampaign, useUpdateCampaign, usePushTemplates, usePushGroups } from "@/hooks/usePushNotifications";
import { useUsers } from "@/hooks/useUsers";
import type { PushCampaign, CreateCampaignRequest, PushTemplate } from "@/types/push-notification";
import { ROUTES } from "@/lib/constants";
import { getInitials } from "@/lib/utils";

interface SelectedUser { id: string; name: string; email: string }

interface CampaignFormProps { campaign?: PushCampaign }

// ── Template picker dialog ─────────────────────────────────────────────────

function TemplatePickerDialog({
  open, onClose, onSelect,
}: { open: boolean; onClose: () => void; onSelect: (t: PushTemplate) => void }) {
  const { data } = usePushTemplates();
  const templates = data?.data ?? [];
  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent className="sm:max-w-lg max-h-[80vh] flex flex-col">
        <DialogHeader>
          <DialogTitle>Chọn mẫu thông báo</DialogTitle>
        </DialogHeader>
        <div className="flex-1 overflow-y-auto space-y-2">
          {templates.length === 0 ? (
            <p className="text-sm text-muted-foreground text-center py-6">
              Chưa có mẫu nào. Tạo mẫu tại trang Mẫu thông báo.
            </p>
          ) : (
            templates.map((t) => (
              <button key={t.id} onClick={() => { onSelect(t); onClose(); }}
                className="w-full text-left rounded-xl border p-3 hover:bg-muted/50 transition-colors">
                <p className="text-sm font-semibold">{t.name}</p>
                <p className="text-xs text-muted-foreground mt-0.5">{t.title}</p>
                <p className="text-xs text-muted-foreground line-clamp-1 mt-0.5">{t.body}</p>
              </button>
            ))
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}

// ── User search for specific target ───────────────────────────────────────

function UserSearchInput({
  selectedUsers, onAdd, onRemove,
}: {
  selectedUsers: SelectedUser[];
  onAdd: (u: SelectedUser) => void;
  onRemove: (id: string) => void;
}) {
  const [search, setSearch] = useState("");
  const { data } = useUsers({ search: search || undefined, limit: 6 });
  const results = (data?.data ?? []).filter((u) => !selectedUsers.find((s) => s.id === u.id));

  return (
    <div className="space-y-2">
      {/* Selected chips */}
      {selectedUsers.length > 0 && (
        <div className="flex flex-wrap gap-1.5">
          {selectedUsers.map((u) => (
            <div key={u.id}
              className="inline-flex items-center gap-1.5 rounded-full bg-primary/10 text-primary text-xs px-2.5 py-1">
              <Avatar className="h-4 w-4">
                <AvatarFallback className="text-[9px]">{getInitials(u.name || u.email)}</AvatarFallback>
              </Avatar>
              <span className="max-w-[120px] truncate">{u.name || u.email}</span>
              <button onClick={() => onRemove(u.id)} className="hover:opacity-70">
                <X className="w-3 h-3" />
              </button>
            </div>
          ))}
        </div>
      )}

      {/* Search input */}
      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-muted-foreground" />
        <Input className="pl-9 h-9 text-sm" placeholder="Tìm tên / email hoặc dán UUID..."
          value={search} onChange={(e) => setSearch(e.target.value)} />
      </div>

      {/* Dropdown results */}
      {search && (
        <div className="border rounded-lg divide-y max-h-44 overflow-y-auto shadow-sm">
          {results.length === 0 ? (
            <p className="text-xs text-muted-foreground p-3 text-center">Không tìm thấy người dùng</p>
          ) : (
            results.map((u) => (
              <button key={u.id} onClick={() => { onAdd({ id: u.id, name: u.full_name, email: u.email }); setSearch(""); }}
                className="w-full flex items-center gap-3 p-2.5 hover:bg-muted/50 text-left">
                <Avatar className="h-7 w-7 shrink-0">
                  <AvatarFallback className="text-xs">{getInitials(u.full_name || u.email)}</AvatarFallback>
                </Avatar>
                <div className="min-w-0">
                  <p className="text-sm font-medium truncate">{u.full_name || "—"}</p>
                  <p className="text-xs text-muted-foreground truncate">{u.email}</p>
                </div>
                <CheckCircle2 className="w-4 h-4 text-muted-foreground ml-auto shrink-0" />
              </button>
            ))
          )}
        </div>
      )}
    </div>
  );
}

// ── Main form ─────────────────────────────────────────────────────────────

export function CampaignForm({ campaign }: CampaignFormProps) {
  const router = useRouter();
  const isEdit = !!campaign;
  const { data: groupsData } = usePushGroups();
  const groups = groupsData?.data ?? [];

  const [templatePickerOpen, setTemplatePickerOpen] = useState(false);
  const [selectedTemplateId, setSelectedTemplateId] = useState<string | undefined>(
    campaign?.template_id ?? undefined
  );

  const [title, setTitle] = useState(campaign?.title ?? "");
  const [body, setBody] = useState(campaign?.body ?? "");
  const [imageURL, setImageURL] = useState(campaign?.image_url ?? "");
  const [clickAction, setClickAction] = useState(campaign?.click_action ?? "");
  const [targetType, setTargetType] = useState<"all" | "users" | "group">(
    campaign?.target_type ?? "all"
  );
  const [selectedUsers, setSelectedUsers] = useState<SelectedUser[]>([]);
  const [selectedGroupId, setSelectedGroupId] = useState<string>(
    campaign?.target_group_id ?? ""
  );

  const createCampaign = useCreateCampaign();
  const updateCampaign = useUpdateCampaign();
  const isPending = createCampaign.isPending || updateCampaign.isPending;

  const handleSelectTemplate = (t: PushTemplate) => {
    setSelectedTemplateId(t.id);
    setTitle(t.title);
    setBody(t.body);
    setImageURL(t.image_url ?? "");
    setClickAction(t.click_action ?? "");
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const payload: CreateCampaignRequest = {
      title,
      body,
      image_url: imageURL || undefined,
      click_action: clickAction || undefined,
      target_type: targetType,
      template_id: selectedTemplateId || undefined,
      ...(targetType === "users" && {
        target_users: selectedUsers.map((u) => u.id),
      }),
      ...(targetType === "group" && {
        target_group_id: selectedGroupId || undefined,
      }),
    };

    if (isEdit && campaign) {
      updateCampaign.mutate({ id: campaign.id, data: payload },
        { onSuccess: () => router.push(ROUTES.ADMIN_PUSH_NOTIFICATIONS) });
    } else {
      createCampaign.mutate(payload,
        { onSuccess: () => router.push(ROUTES.ADMIN_PUSH_NOTIFICATIONS) });
    }
  };

  return (
    <>
      <form onSubmit={handleSubmit} className="space-y-5">
        {/* ── Template picker ── */}
        <Card>
          <CardHeader className="pb-3">
            <div className="flex items-center justify-between">
              <CardTitle className="text-sm flex items-center gap-2">
                <Bell className="w-4 h-4" /> Nội dung thông báo
              </CardTitle>
              <Button type="button" variant="outline" size="sm" className="h-8 text-xs"
                onClick={() => setTemplatePickerOpen(true)}>
                {selectedTemplateId ? "Đổi mẫu" : "Dùng mẫu có sẵn"}
              </Button>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            {selectedTemplateId && (
              <div className="flex items-center gap-2 rounded-lg bg-primary/5 border border-primary/20 px-3 py-2">
                <CheckCircle2 className="w-3.5 h-3.5 text-primary shrink-0" />
                <span className="text-xs text-primary">Đang dùng mẫu</span>
                <button type="button" className="ml-auto text-muted-foreground hover:text-foreground"
                  onClick={() => setSelectedTemplateId(undefined)}>
                  <X className="w-3.5 h-3.5" />
                </button>
              </div>
            )}
            <div className="space-y-1.5">
              <Label>Tiêu đề <span className="text-destructive">*</span></Label>
              <Input value={title} onChange={(e) => setTitle(e.target.value)}
                placeholder="Tiêu đề thông báo" required maxLength={255} />
            </div>
            <div className="space-y-1.5">
              <Label>Nội dung <span className="text-destructive">*</span></Label>
              <Textarea value={body} onChange={(e) => setBody(e.target.value)}
                placeholder="Nội dung hiển thị trên thiết bị..." required rows={3} />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <Label>URL hình ảnh</Label>
                <Input value={imageURL} onChange={(e) => setImageURL(e.target.value)} placeholder="https://..." />
              </div>
              <div className="space-y-1.5">
                <Label>Hành động khi nhấn</Label>
                <ClickActionSelect value={clickAction} onChange={setClickAction} />
              </div>
            </div>
          </CardContent>
        </Card>

        {/* ── Target ── */}
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm">Đối tượng nhận</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-3 gap-2">
              {[
                { value: "all", label: "Tất cả", icon: <Bell className="w-4 h-4" />, desc: "Mọi thiết bị" },
                { value: "users", label: "Cụ thể", icon: <User className="w-4 h-4" />, desc: "Chọn người dùng" },
                { value: "group", label: "Nhóm", icon: <Users className="w-4 h-4" />, desc: "Nhóm đã tạo" },
              ].map((opt) => (
                <button key={opt.value} type="button"
                  onClick={() => setTargetType(opt.value as typeof targetType)}
                  className={`rounded-xl border p-3 text-center transition-all ${
                    targetType === opt.value
                      ? "border-primary bg-primary/5 text-primary"
                      : "hover:bg-muted/50 text-muted-foreground"
                  }`}>
                  <div className="flex justify-center mb-1">{opt.icon}</div>
                  <p className="text-xs font-semibold">{opt.label}</p>
                  <p className="text-[10px] mt-0.5">{opt.desc}</p>
                </button>
              ))}
            </div>

            {targetType === "users" && (
              <div className="space-y-2">
                <Separator />
                <p className="text-xs text-muted-foreground">Tìm và chọn người dùng:</p>
                <UserSearchInput
                  selectedUsers={selectedUsers}
                  onAdd={(u) => setSelectedUsers((prev) => [...prev, u])}
                  onRemove={(id) => setSelectedUsers((prev) => prev.filter((u) => u.id !== id))}
                />
              </div>
            )}

            {targetType === "group" && (
              <div className="space-y-2">
                <Separator />
                <Label>Chọn nhóm</Label>
                {groups.length === 0 ? (
                  <p className="text-xs text-muted-foreground">
                    Chưa có nhóm nào.{" "}
                    <a href={`${ROUTES.ADMIN_PUSH_NOTIFICATIONS}/groups`} className="underline">Tạo nhóm</a>
                  </p>
                ) : (
                  <Select value={selectedGroupId} onValueChange={setSelectedGroupId}>
                    <SelectTrigger>
                      <SelectValue placeholder="Chọn nhóm..." />
                    </SelectTrigger>
                    <SelectContent>
                      {groups.map((g) => (
                        <SelectItem key={g.id} value={g.id}>
                          {g.name}{" "}
                          <span className="text-muted-foreground text-xs">({g.member_count} thành viên)</span>
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
              </div>
            )}
          </CardContent>
        </Card>

        {/* ── Preview ── */}
        {(title || body) && (
          <Card className="border-dashed">
            <CardHeader className="pb-2">
              <CardTitle className="text-xs text-muted-foreground">Xem trước</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="bg-muted rounded-xl p-4 max-w-xs">
                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center shrink-0">
                    <Bell className="w-5 h-5 text-primary" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold text-sm">{title || "Tiêu đề"}</p>
                    <p className="text-xs text-muted-foreground line-clamp-2 mt-0.5">{body || "Nội dung..."}</p>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        )}

        <div className="flex gap-3 justify-end pt-2">
          <Button type="button" variant="outline"
            onClick={() => router.push(ROUTES.ADMIN_PUSH_NOTIFICATIONS)}>
            Hủy
          </Button>
          <Button type="submit" disabled={isPending || !title || !body}>
            {isPending ? "Đang lưu..." : isEdit ? "Cập nhật" : "Tạo chiến dịch"}
          </Button>
        </div>
      </form>

      <TemplatePickerDialog
        open={templatePickerOpen}
        onClose={() => setTemplatePickerOpen(false)}
        onSelect={handleSelectTemplate}
      />
    </>
  );
}
