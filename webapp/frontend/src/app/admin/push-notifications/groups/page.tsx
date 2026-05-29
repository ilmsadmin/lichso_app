"use client";

import { useState } from "react";
import { Plus, Users, Pencil, Trash2, UserMinus, Search } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter,
} from "@/components/ui/dialog";
import {
  AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent,
  AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import {
  usePushGroups, useCreateGroup, useUpdateGroup, useDeleteGroup,
  useGroupMembers, useAddGroupMember, useRemoveGroupMember,
} from "@/hooks/usePushNotifications";
import { useUsers } from "@/hooks/useUsers";
import { getInitials } from "@/lib/utils";
import type { UserGroup } from "@/types/push-notification";

// ── Group detail / member management ──────────────────────────────────────

function GroupMembersDialog({ group, open, onClose }: { group: UserGroup; open: boolean; onClose: () => void }) {
  const [search, setSearch] = useState("");
  const { data: membersData, isLoading: loadingMembers } = useGroupMembers(open ? group.id : "");
  const { data: usersData } = useUsers({ search: search || undefined, limit: 8 });
  const addMember = useAddGroupMember();
  const removeMember = useRemoveGroupMember();

  const members = membersData?.data ?? [];
  const memberIds = new Set(members.map((m) => m.user_id));
  const searchResults = (usersData?.data ?? []).filter((u) => !memberIds.has(u.id));

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent className="sm:max-w-lg max-h-[85vh] flex flex-col">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Users className="w-4 h-4" />
            {group.name}
            <Badge variant="secondary" className="text-xs">{group.member_count} thành viên</Badge>
          </DialogTitle>
        </DialogHeader>

        <div className="flex-1 overflow-y-auto space-y-4">
          {/* Add member search */}
          <div className="space-y-2">
            <Label className="text-xs text-muted-foreground uppercase tracking-wide">Thêm thành viên</Label>
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-muted-foreground" />
              <Input
                className="pl-9 h-9 text-sm"
                placeholder="Tìm theo tên hoặc nhập UUID..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            {search && (
              <div className="border rounded-lg divide-y max-h-48 overflow-y-auto">
                {searchResults.length === 0 ? (
                  <p className="text-xs text-muted-foreground p-3 text-center">Không tìm thấy</p>
                ) : (
                  searchResults.map((u) => (
                    <div key={u.id} className="flex items-center gap-3 p-2.5 hover:bg-muted/50">
                      <Avatar className="h-7 w-7">
                        <AvatarFallback className="text-xs">{getInitials(u.full_name || u.email)}</AvatarFallback>
                      </Avatar>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium truncate">{u.full_name || u.email}</p>
                        <p className="text-xs text-muted-foreground truncate">{u.email}</p>
                      </div>
                      <Button size="sm" variant="outline" className="h-7 text-xs shrink-0"
                        disabled={addMember.isPending}
                        onClick={() => addMember.mutate({ groupId: group.id, userId: u.id })}>
                        Thêm
                      </Button>
                    </div>
                  ))
                )}
              </div>
            )}
          </div>

          {/* Member list */}
          <div className="space-y-2">
            <Label className="text-xs text-muted-foreground uppercase tracking-wide">
              Danh sách ({members.length})
            </Label>
            {loadingMembers ? (
              <div className="space-y-2">
                {Array.from({ length: 3 }).map((_, i) => (
                  <div key={i} className="h-12 rounded-lg bg-muted animate-pulse" />
                ))}
              </div>
            ) : members.length === 0 ? (
              <p className="text-xs text-muted-foreground text-center py-4">Nhóm chưa có thành viên</p>
            ) : (
              <div className="border rounded-lg divide-y">
                {members.map((m) => (
                  <div key={m.user_id} className="flex items-center gap-3 p-2.5">
                    <Avatar className="h-7 w-7">
                      <AvatarFallback className="text-xs">{getInitials(m.full_name || m.email)}</AvatarFallback>
                    </Avatar>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium truncate">{m.full_name || m.email}</p>
                      <p className="text-xs text-muted-foreground truncate">{m.email}</p>
                    </div>
                    <Button variant="ghost" size="icon" className="h-7 w-7 text-destructive shrink-0"
                      disabled={removeMember.isPending}
                      onClick={() => removeMember.mutate({ groupId: group.id, userId: m.user_id })}>
                      <UserMinus className="w-3.5 h-3.5" />
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Đóng</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

// ── Group form dialog ─────────────────────────────────────────────────────

function GroupFormDialog({ open, onClose, group }: { open: boolean; onClose: () => void; group?: UserGroup }) {
  const isEdit = !!group;
  const [name, setName] = useState(group?.name ?? "");
  const [description, setDescription] = useState(group?.description ?? "");
  const create = useCreateGroup();
  const update = useUpdateGroup();
  const isPending = create.isPending || update.isPending;

  const handleSave = () => {
    if (!name.trim()) return;
    const data = { name: name.trim(), description };
    if (isEdit && group) {
      update.mutate({ id: group.id, data }, { onSuccess: onClose });
    } else {
      create.mutate(data, { onSuccess: onClose });
    }
  };

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent className="sm:max-w-sm">
        <DialogHeader>
          <DialogTitle>{isEdit ? "Sửa nhóm" : "Tạo nhóm mới"}</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label>Tên nhóm <span className="text-destructive">*</span></Label>
            <Input value={name} onChange={(e) => setName(e.target.value)} placeholder="VD: Người dùng VIP" />
          </div>
          <div className="space-y-1.5">
            <Label>Mô tả</Label>
            <Input value={description} onChange={(e) => setDescription(e.target.value)}
              placeholder="Mô tả ngắn về nhóm này" />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Hủy</Button>
          <Button onClick={handleSave} disabled={isPending || !name.trim()}>
            {isPending ? "Đang lưu..." : isEdit ? "Cập nhật" : "Tạo nhóm"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

// ── Main page ─────────────────────────────────────────────────────────────

export default function GroupsPage() {
  const { data, isLoading } = usePushGroups();
  const deleteGroup = useDeleteGroup();
  const [formOpen, setFormOpen] = useState(false);
  const [editGroup, setEditGroup] = useState<UserGroup | undefined>();
  const [managingGroup, setManagingGroup] = useState<UserGroup | null>(null);

  const groups = data?.data ?? [];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Nhóm người dùng</h1>
          <p className="text-muted-foreground text-sm mt-1">
            Phân nhóm người dùng để gửi push notification có mục tiêu
          </p>
        </div>
        <Button onClick={() => { setEditGroup(undefined); setFormOpen(true); }}>
          <Plus className="w-4 h-4 mr-2" /> Tạo nhóm
        </Button>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 3 }).map((_, i) => <div key={i} className="h-28 rounded-xl bg-muted animate-pulse" />)}
        </div>
      ) : groups.length === 0 ? (
        <div className="text-center py-16 text-muted-foreground">
          <Users className="w-10 h-10 mx-auto mb-3 opacity-30" />
          <p>Chưa có nhóm nào. Tạo nhóm đầu tiên!</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {groups.map((g) => (
            <div key={g.id}
              className="group rounded-xl border bg-card p-4 hover:shadow-md transition-shadow cursor-pointer"
              onClick={() => setManagingGroup(g)}>
              <div className="flex items-start justify-between gap-2">
                <div className="flex items-center gap-2.5">
                  <div className="w-9 h-9 rounded-full bg-primary/10 flex items-center justify-center shrink-0">
                    <Users className="w-4 h-4 text-primary" />
                  </div>
                  <div>
                    <p className="font-semibold text-sm">{g.name}</p>
                    <p className="text-xs text-muted-foreground">
                      {g.member_count} thành viên
                    </p>
                  </div>
                </div>
                <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity"
                  onClick={(e) => e.stopPropagation()}>
                  <Button variant="ghost" size="icon" className="h-7 w-7"
                    onClick={() => { setEditGroup(g); setFormOpen(true); }}>
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
                        <AlertDialogTitle>Xóa nhóm?</AlertDialogTitle>
                        <AlertDialogDescription>
                          Nhóm &ldquo;{g.name}&rdquo; sẽ bị xóa vĩnh viễn. Các thành viên không bị ảnh hưởng.
                        </AlertDialogDescription>
                      </AlertDialogHeader>
                      <AlertDialogFooter>
                        <AlertDialogCancel>Hủy</AlertDialogCancel>
                        <AlertDialogAction className="bg-destructive text-destructive-foreground"
                          onClick={() => deleteGroup.mutate(g.id)}>Xóa</AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>
                </div>
              </div>
              {g.description && (
                <p className="text-xs text-muted-foreground mt-2 line-clamp-2">{g.description}</p>
              )}
            </div>
          ))}
        </div>
      )}

      <GroupFormDialog open={formOpen} onClose={() => setFormOpen(false)} group={editGroup} />

      {managingGroup && (
        <GroupMembersDialog
          group={managingGroup}
          open={!!managingGroup}
          onClose={() => setManagingGroup(null)}
        />
      )}
    </div>
  );
}
