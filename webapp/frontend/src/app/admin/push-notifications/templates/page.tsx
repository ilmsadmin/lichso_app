"use client";

import { useState } from "react";
import { Plus, Pencil, Trash2, Bell } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter,
} from "@/components/ui/dialog";
import {
  AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent,
  AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { usePushTemplates, useCreateTemplate, useUpdateTemplate, useDeleteTemplate } from "@/hooks/usePushNotifications";
import type { PushTemplate, CreateTemplateRequest } from "@/types/push-notification";

function TemplateDialog({
  open, onClose, template,
}: { open: boolean; onClose: () => void; template?: PushTemplate }) {
  const isEdit = !!template;
  const [form, setForm] = useState<CreateTemplateRequest>({
    name: template?.name ?? "",
    title: template?.title ?? "",
    body: template?.body ?? "",
    image_url: template?.image_url ?? "",
    click_action: template?.click_action ?? "",
  });

  const create = useCreateTemplate();
  const update = useUpdateTemplate();
  const isPending = create.isPending || update.isPending;

  const handleSave = () => {
    if (!form.name || !form.title || !form.body) return;
    if (isEdit && template) {
      update.mutate({ id: template.id, data: form }, { onSuccess: onClose });
    } else {
      create.mutate(form, { onSuccess: onClose });
    }
  };

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>{isEdit ? "Sửa mẫu" : "Tạo mẫu thông báo"}</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label>Tên mẫu <span className="text-destructive">*</span></Label>
            <Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })}
              placeholder="VD: Thông báo cập nhật tính năng" />
          </div>
          <div className="space-y-1.5">
            <Label>Tiêu đề <span className="text-destructive">*</span></Label>
            <Input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })}
              placeholder="Tiêu đề thông báo" maxLength={255} />
          </div>
          <div className="space-y-1.5">
            <Label>Nội dung <span className="text-destructive">*</span></Label>
            <Textarea value={form.body} onChange={(e) => setForm({ ...form, body: e.target.value })}
              placeholder="Nội dung hiển thị trên thiết bị..." rows={3} />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <Label>URL hình ảnh</Label>
              <Input value={form.image_url} onChange={(e) => setForm({ ...form, image_url: e.target.value })}
                placeholder="https://..." />
            </div>
            <div className="space-y-1.5">
              <Label>Click action</Label>
              <Input value={form.click_action} onChange={(e) => setForm({ ...form, click_action: e.target.value })}
                placeholder="lichso://..." />
            </div>
          </div>

          {/* Preview */}
          {(form.title || form.body) && (
            <div className="rounded-xl bg-muted/60 border p-3">
              <p className="text-[10px] text-muted-foreground mb-2 uppercase tracking-wide">Xem trước</p>
              <div className="flex gap-2.5">
                <div className="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center shrink-0">
                  <Bell className="w-4 h-4 text-primary" />
                </div>
                <div>
                  <p className="text-sm font-semibold leading-tight">{form.title || "Tiêu đề"}</p>
                  <p className="text-xs text-muted-foreground mt-0.5 line-clamp-2">{form.body || "Nội dung..."}</p>
                </div>
              </div>
            </div>
          )}
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Hủy</Button>
          <Button onClick={handleSave} disabled={isPending || !form.name || !form.title || !form.body}>
            {isPending ? "Đang lưu..." : isEdit ? "Cập nhật" : "Tạo mẫu"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function TemplatesPage() {
  const { data, isLoading } = usePushTemplates();
  const deleteTemplate = useDeleteTemplate();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editTemplate, setEditTemplate] = useState<PushTemplate | undefined>();

  const templates = data?.data ?? [];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Mẫu thông báo</h1>
          <p className="text-muted-foreground text-sm mt-1">Tạo sẵn các mẫu để dùng nhiều lần khi gửi push notification</p>
        </div>
        <Button onClick={() => { setEditTemplate(undefined); setDialogOpen(true); }}>
          <Plus className="w-4 h-4 mr-2" /> Tạo mẫu mới
        </Button>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="h-40 rounded-xl bg-muted animate-pulse" />
          ))}
        </div>
      ) : templates.length === 0 ? (
        <div className="text-center py-16 text-muted-foreground">
          <Bell className="w-10 h-10 mx-auto mb-3 opacity-30" />
          <p>Chưa có mẫu nào. Tạo mẫu đầu tiên!</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {templates.map((t) => (
            <Card key={t.id} className="group hover:shadow-md transition-shadow">
              <CardHeader className="pb-2">
                <div className="flex items-start justify-between gap-2">
                  <CardTitle className="text-sm font-semibold line-clamp-1">{t.name}</CardTitle>
                  <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity shrink-0">
                    <Button variant="ghost" size="icon" className="h-7 w-7"
                      onClick={() => { setEditTemplate(t); setDialogOpen(true); }}>
                      <Pencil className="w-3.5 h-3.5" />
                    </Button>
                    <AlertDialog>
                      <AlertDialogTrigger asChild>
                        <Button variant="ghost" size="icon" className="h-7 w-7 text-destructive">
                          <Trash2 className="w-3.5 h-3.5" />
                        </Button>
                      </AlertDialogTrigger>
                      <AlertDialogContent>
                        <AlertDialogHeader>
                          <AlertDialogTitle>Xóa mẫu?</AlertDialogTitle>
                          <AlertDialogDescription>Mẫu &ldquo;{t.name}&rdquo; sẽ bị xóa vĩnh viễn.</AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter>
                          <AlertDialogCancel>Hủy</AlertDialogCancel>
                          <AlertDialogAction className="bg-destructive text-destructive-foreground"
                            onClick={() => deleteTemplate.mutate(t.id)}>Xóa</AlertDialogAction>
                        </AlertDialogFooter>
                      </AlertDialogContent>
                    </AlertDialog>
                  </div>
                </div>
              </CardHeader>
              <CardContent className="space-y-2">
                <div className="rounded-lg bg-muted/50 p-2.5">
                  <p className="text-xs font-medium leading-tight">{t.title}</p>
                  <p className="text-xs text-muted-foreground mt-1 line-clamp-2">{t.body}</p>
                </div>
                {t.click_action && (
                  <p className="text-[11px] text-muted-foreground font-mono truncate">{t.click_action}</p>
                )}
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <TemplateDialog
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        template={editTemplate}
      />
    </div>
  );
}
