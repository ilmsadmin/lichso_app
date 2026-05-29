"use client";

import { useState } from "react";
import { Copy, Check, Smartphone, Globe, User as UserIcon, BarChart3, Star } from "lucide-react";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { useUserDetail } from "@/hooks/useUsers";
import { getInitials, formatDate } from "@/lib/utils";
import type { User } from "@/types/user";
import { formatDistanceToNow } from "date-fns";
import { vi } from "date-fns/locale";

interface UserDetailDrawerProps {
  user: User | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

function CopyButton({ value }: { value: string }) {
  const [copied, setCopied] = useState(false);
  const copy = () => {
    navigator.clipboard.writeText(value);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  };
  return (
    <button onClick={copy} className="text-muted-foreground hover:text-foreground transition-colors ml-1">
      {copied ? <Check className="w-3 h-3 text-green-500" /> : <Copy className="w-3 h-3" />}
    </button>
  );
}

function InfoRow({ label, value, mono = false }: { label: string; value: React.ReactNode; mono?: boolean }) {
  return (
    <div className="flex justify-between items-start gap-4 py-2">
      <span className="text-muted-foreground text-xs shrink-0 w-32">{label}</span>
      <span className={`text-xs text-right break-all ${mono ? "font-mono" : ""}`}>{value ?? "—"}</span>
    </div>
  );
}

function PlatformBadge({ platform }: { platform: string }) {
  const icon = platform === "android" ? "🤖" : platform === "ios" ? "🍎" : "🌐";
  return (
    <Badge variant="outline" className="text-xs gap-1">
      <span>{icon}</span>
      {platform}
    </Badge>
  );
}

export function UserDetailDrawer({ user, open, onOpenChange }: UserDetailDrawerProps) {
  const { data, isLoading } = useUserDetail(user?.id ?? "");
  const detail = data?.data;

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent side="right" className="w-full sm:max-w-lg overflow-y-auto">
        <SheetHeader className="pb-4">
          <SheetTitle className="sr-only">Chi tiết người dùng</SheetTitle>
          {user && (
            <div className="flex items-center gap-3">
              <Avatar className="h-14 w-14">
                <AvatarImage src={user.avatar} />
                <AvatarFallback className="text-lg bg-primary/10 text-primary">
                  {getInitials(user.full_name || user.email)}
                </AvatarFallback>
              </Avatar>
              <div className="min-w-0">
                <p className="font-semibold text-base truncate">{user.full_name || "Chưa đặt tên"}</p>
                <p className="text-sm text-muted-foreground truncate">{user.email}</p>
                <div className="flex items-center gap-1.5 mt-1 flex-wrap">
                  <Badge variant={user.is_active ? "default" : "secondary"} className="text-xs h-5">
                    {user.is_active ? "Active" : "Inactive"}
                  </Badge>
                  <Badge variant="outline" className="text-xs h-5">
                    {user.provider === "google" ? "🔵 Google" : "🔑 Local"}
                  </Badge>
                  {user.roles?.map((r) => (
                    <Badge
                      key={r.id}
                      variant={r.name === "super_admin" ? "destructive" : r.name === "admin" ? "default" : "secondary"}
                      className="text-xs h-5"
                    >
                      {r.display_name}
                    </Badge>
                  ))}
                </div>
              </div>
            </div>
          )}
        </SheetHeader>

        {isLoading ? (
          <div className="space-y-3 mt-4">
            {Array.from({ length: 8 }).map((_, i) => (
              <Skeleton key={i} className="h-6 w-full" />
            ))}
          </div>
        ) : detail ? (
          <div className="space-y-5 mt-2">

            {/* ── Thông tin cơ bản ── */}
            <section>
              <h3 className="text-xs font-semibold text-muted-foreground uppercase tracking-wide flex items-center gap-1.5 mb-1">
                <UserIcon className="w-3.5 h-3.5" /> Thông tin cơ bản
              </h3>
              <div className="divide-y divide-border/50">
                <div className="flex justify-between items-center py-2">
                  <span className="text-muted-foreground text-xs w-32">UUID</span>
                  <span className="font-mono text-xs text-right flex items-center gap-1 break-all">
                    {detail.id}
                    <CopyButton value={detail.id} />
                  </span>
                </div>
                <InfoRow label="Email" value={
                  <span className="flex items-center gap-1">
                    {detail.email}
                    <CopyButton value={detail.email} />
                  </span>
                } />
                <InfoRow label="Họ tên" value={detail.full_name || "—"} />
                <InfoRow label="Số điện thoại" value={detail.phone || "—"} mono />
                <InfoRow label="Provider" value={
                  <span>{detail.provider === "google" ? "🔵 Google" : "🔑 Local"}</span>
                } />
                {detail.provider !== "local" && detail.provider_id && (
                  <div className="flex justify-between items-start gap-4 py-2">
                    <span className="text-muted-foreground text-xs shrink-0 w-32">Provider ID</span>
                    <span className="font-mono text-xs text-right flex items-center gap-1 break-all">
                      {detail.provider_id}
                      <CopyButton value={detail.provider_id} />
                    </span>
                  </div>
                )}
                <InfoRow label="Trạng thái" value={
                  <Badge variant={detail.is_active ? "default" : "secondary"} className="text-xs">
                    {detail.is_active ? "Active" : "Inactive"}
                  </Badge>
                } />
                <InfoRow label="Đăng nhập cuối" value={
                  detail.last_login
                    ? formatDistanceToNow(new Date(detail.last_login), { addSuffix: true, locale: vi })
                    : "Chưa đăng nhập"
                } />
                <InfoRow label="Ngày tạo" value={formatDate(detail.created_at)} />
                <InfoRow label="Cập nhật" value={formatDate(detail.updated_at)} />
              </div>
            </section>

            <Separator />

            {/* ── Thống kê ── */}
            <section>
              <h3 className="text-xs font-semibold text-muted-foreground uppercase tracking-wide flex items-center gap-1.5 mb-2">
                <BarChart3 className="w-3.5 h-3.5" /> Thống kê hoạt động
              </h3>
              <div className="grid grid-cols-3 gap-2">
                {[
                  { label: "Bookmarks", value: detail.stats.bookmark_count, icon: "🔖" },
                  { label: "Ghi chú", value: detail.stats.note_count, icon: "📝" },
                  { label: "Thiết bị", value: detail.stats.device_count, icon: "📱" },
                  { label: "Streak", value: `${detail.stats.streak_days} ngày`, icon: "🔥" },
                  { label: "Điểm", value: detail.stats.points.toLocaleString(), icon: "⭐" },
                ].map((s) => (
                  <div key={s.label} className="bg-muted/50 rounded-lg p-2.5 text-center">
                    <p className="text-base">{s.icon}</p>
                    <p className="text-sm font-semibold mt-0.5">{s.value}</p>
                    <p className="text-xs text-muted-foreground">{s.label}</p>
                  </div>
                ))}
              </div>
            </section>

            <Separator />

            {/* ── Thiết bị ── */}
            <section>
              <h3 className="text-xs font-semibold text-muted-foreground uppercase tracking-wide flex items-center gap-1.5 mb-2">
                <Smartphone className="w-3.5 h-3.5" /> Thiết bị đã đăng ký ({detail.devices.length})
              </h3>
              {detail.devices.length === 0 ? (
                <p className="text-xs text-muted-foreground text-center py-4">Chưa có thiết bị nào</p>
              ) : (
                <div className="space-y-2">
                  {detail.devices.map((d, i) => (
                    <div key={i} className="border rounded-lg p-3 space-y-2">
                      <div className="flex items-center justify-between">
                        <PlatformBadge platform={d.platform} />
                        <span className="text-xs text-muted-foreground">
                          {formatDistanceToNow(new Date(d.last_seen), { addSuffix: true, locale: vi })}
                        </span>
                      </div>
                      <div className="grid grid-cols-2 gap-x-4 gap-y-1">
                        <div>
                          <p className="text-xs text-muted-foreground">Phiên bản app</p>
                          <p className="text-xs font-mono font-medium">{d.app_version || "—"}</p>
                        </div>
                        <div>
                          <p className="text-xs text-muted-foreground">Đăng ký lần đầu</p>
                          <p className="text-xs">{formatDate(d.created_at)}</p>
                        </div>
                        {d.device_id && (
                          <div className="col-span-2">
                            <p className="text-xs text-muted-foreground">Device ID</p>
                            <p className="text-xs font-mono truncate flex items-center gap-1">
                              {d.device_id}
                              <CopyButton value={d.device_id} />
                            </p>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </section>

          </div>
        ) : (
          <div className="text-center py-12 text-muted-foreground text-sm">Không tải được dữ liệu</div>
        )}
      </SheetContent>
    </Sheet>
  );
}
