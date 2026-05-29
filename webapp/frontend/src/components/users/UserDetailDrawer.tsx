"use client";

import { useState } from "react";
import { Copy, Check, Smartphone, BarChart3, User as UserIcon } from "lucide-react";
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
    <button
      onClick={copy}
      className="text-muted-foreground hover:text-foreground transition-colors ml-1 shrink-0"
    >
      {copied ? <Check className="w-3 h-3 text-green-500" /> : <Copy className="w-3 h-3" />}
    </button>
  );
}

function InfoRow({
  label,
  value,
  mono = false,
}: {
  label: string;
  value: React.ReactNode;
  mono?: boolean;
}) {
  return (
    <div className="flex justify-between items-start gap-3 py-2.5">
      <span className="text-muted-foreground text-xs shrink-0 w-28">{label}</span>
      <span className={`text-xs text-right break-all ${mono ? "font-mono" : ""}`}>{value ?? "—"}</span>
    </div>
  );
}

export function UserDetailDrawer({ user, open, onOpenChange }: UserDetailDrawerProps) {
  const { data, isLoading } = useUserDetail(user?.id ?? "");
  const detail = data?.data;

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent side="right" className="w-full sm:max-w-md flex flex-col p-0 overflow-hidden">
        {/* ── Header cố định ── */}
        <div className="px-6 pt-6 pb-4 border-b bg-background shrink-0">
          <SheetTitle className="sr-only">Chi tiết người dùng</SheetTitle>
          {user && (
            <div className="flex items-center gap-4">
              <Avatar className="h-14 w-14 shrink-0">
                <AvatarImage src={user.avatar} />
                <AvatarFallback className="text-lg bg-primary/10 text-primary font-semibold">
                  {getInitials(user.full_name || user.email)}
                </AvatarFallback>
              </Avatar>
              <div className="min-w-0 flex-1">
                <p className="font-semibold text-base truncate leading-tight">
                  {user.full_name || "Chưa đặt tên"}
                </p>
                <p className="text-sm text-muted-foreground truncate mt-0.5">{user.email}</p>
                <div className="flex items-center gap-1.5 mt-2 flex-wrap">
                  <Badge
                    variant={user.is_active ? "default" : "secondary"}
                    className="text-xs h-5 px-2"
                  >
                    {user.is_active ? "Active" : "Inactive"}
                  </Badge>
                  <Badge variant="outline" className="text-xs h-5 px-2">
                    {user.provider === "google" ? "🔵 Google" : "🔑 Local"}
                  </Badge>
                  {user.roles?.map((r) => (
                    <Badge
                      key={r.id}
                      variant={
                        r.name === "super_admin"
                          ? "destructive"
                          : r.name === "admin"
                          ? "default"
                          : "secondary"
                      }
                      className="text-xs h-5 px-2"
                    >
                      {r.display_name}
                    </Badge>
                  ))}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* ── Scrollable body ── */}
        <div className="flex-1 overflow-y-auto">
          {isLoading ? (
            <div className="px-6 py-6 space-y-3">
              {Array.from({ length: 10 }).map((_, i) => (
                <Skeleton key={i} className="h-5 w-full" />
              ))}
            </div>
          ) : detail ? (
            <div className="px-6 py-5 space-y-6">

              {/* ── Thông tin cơ bản ── */}
              <section>
                <SectionTitle icon={<UserIcon className="w-3.5 h-3.5" />} title="Thông tin cơ bản" />
                <div className="mt-2 rounded-xl border divide-y divide-border/60 overflow-hidden">

                  <div className="flex justify-between items-center px-4 py-2.5">
                    <span className="text-muted-foreground text-xs w-28 shrink-0">UUID</span>
                    <span className="font-mono text-[11px] text-right flex items-center gap-1 break-all text-muted-foreground">
                      {detail.id}
                      <CopyButton value={detail.id} />
                    </span>
                  </div>

                  <div className="px-4 divide-y divide-border/40">
                    <InfoRow
                      label="Email"
                      value={
                        <span className="flex items-center gap-1 justify-end">
                          {detail.email}
                          <CopyButton value={detail.email} />
                        </span>
                      }
                    />
                    <InfoRow label="Họ tên" value={detail.full_name || "—"} />
                    <InfoRow
                      label="Số điện thoại"
                      value={detail.phone || <span className="text-muted-foreground/60">Chưa cập nhật</span>}
                      mono
                    />
                    <InfoRow
                      label="Provider"
                      value={
                        <span>{detail.provider === "google" ? "🔵 Google" : "🔑 Local"}</span>
                      }
                    />
                    {detail.provider !== "local" && detail.provider_id && (
                      <div className="flex justify-between items-start gap-3 py-2.5">
                        <span className="text-muted-foreground text-xs shrink-0 w-28">Provider ID</span>
                        <span className="font-mono text-[11px] text-right flex items-center gap-1 break-all text-muted-foreground">
                          {detail.provider_id}
                          <CopyButton value={detail.provider_id} />
                        </span>
                      </div>
                    )}
                    <InfoRow
                      label="Trạng thái"
                      value={
                        <Badge
                          variant={detail.is_active ? "default" : "secondary"}
                          className="text-xs"
                        >
                          {detail.is_active ? "Active" : "Inactive"}
                        </Badge>
                      }
                    />
                    <InfoRow
                      label="Đăng nhập cuối"
                      value={
                        detail.last_login
                          ? formatDistanceToNow(new Date(detail.last_login), {
                              addSuffix: true,
                              locale: vi,
                            })
                          : "Chưa đăng nhập"
                      }
                    />
                    <InfoRow label="Ngày tạo" value={formatDate(detail.created_at)} />
                    <InfoRow label="Cập nhật" value={formatDate(detail.updated_at)} />
                  </div>
                </div>
              </section>

              {/* ── Thống kê hoạt động ── */}
              <section>
                <SectionTitle icon={<BarChart3 className="w-3.5 h-3.5" />} title="Thống kê hoạt động" />
                <div className="mt-2 grid grid-cols-3 gap-2">
                  {[
                    { label: "Bookmarks", value: detail.stats.bookmark_count, icon: "🔖" },
                    { label: "Ghi chú", value: detail.stats.note_count, icon: "📝" },
                    { label: "Thiết bị", value: detail.stats.device_count, icon: "📱" },
                    { label: "Streak", value: `${detail.stats.streak_days}`, unit: "ngày", icon: "🔥" },
                    { label: "Điểm", value: detail.stats.points.toLocaleString(), icon: "⭐" },
                  ].map((s) => (
                    <div
                      key={s.label}
                      className="bg-muted/40 rounded-xl p-3 text-center border border-border/40"
                    >
                      <p className="text-xl leading-none">{s.icon}</p>
                      <p className="text-sm font-bold mt-1.5 leading-none">
                        {s.value}
                        {"unit" in s && s.unit && (
                          <span className="text-[10px] font-normal text-muted-foreground ml-0.5">
                            {s.unit}
                          </span>
                        )}
                      </p>
                      <p className="text-[11px] text-muted-foreground mt-1">{s.label}</p>
                    </div>
                  ))}
                </div>
              </section>

              {/* ── Thiết bị đã đăng ký ── */}
              <section>
                <SectionTitle
                  icon={<Smartphone className="w-3.5 h-3.5" />}
                  title={`Thiết bị đã đăng ký (${detail.devices.length})`}
                />
                <div className="mt-2 space-y-2">
                  {detail.devices.length === 0 ? (
                    <div className="rounded-xl border border-dashed p-6 text-center">
                      <p className="text-xs text-muted-foreground">Chưa có thiết bị nào</p>
                      <p className="text-[11px] text-muted-foreground/60 mt-1">
                        Mở app để đăng ký thiết bị
                      </p>
                    </div>
                  ) : (
                    detail.devices.map((d, i) => (
                      <div key={i} className="rounded-xl border bg-card p-4 space-y-3">
                        {/* Header device */}
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <span className="text-lg">
                              {d.platform === "android" ? "🤖" : d.platform === "ios" ? "🍎" : "🌐"}
                            </span>
                            <div>
                              <p className="text-sm font-medium leading-tight">
                                {d.device_name || `${d.platform} device`}
                              </p>
                              <p className="text-[11px] text-muted-foreground capitalize">
                                {d.platform} · v{d.app_version || "—"}
                              </p>
                            </div>
                          </div>
                          <span className="text-[11px] text-muted-foreground">
                            {formatDistanceToNow(new Date(d.last_seen), {
                              addSuffix: true,
                              locale: vi,
                            })}
                          </span>
                        </div>

                        {/* Device details */}
                        <div className="grid grid-cols-2 gap-x-4 gap-y-2 pt-2 border-t border-border/40">
                          <div>
                            <p className="text-[10px] text-muted-foreground uppercase tracking-wide">
                              Đăng ký lần đầu
                            </p>
                            <p className="text-xs mt-0.5">{formatDate(d.created_at)}</p>
                          </div>
                          <div>
                            <p className="text-[10px] text-muted-foreground uppercase tracking-wide">
                              Hoạt động cuối
                            </p>
                            <p className="text-xs mt-0.5">{formatDate(d.last_seen)}</p>
                          </div>
                          {d.device_id && (
                            <div className="col-span-2">
                              <p className="text-[10px] text-muted-foreground uppercase tracking-wide">
                                Device ID
                              </p>
                              <p className="text-[11px] font-mono mt-0.5 flex items-center gap-1 text-muted-foreground">
                                <span className="truncate">{d.device_id}</span>
                                <CopyButton value={d.device_id} />
                              </p>
                            </div>
                          )}
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </section>

              {/* bottom padding */}
              <div className="h-4" />
            </div>
          ) : (
            <div className="px-6 py-12 text-center text-sm text-muted-foreground">
              Không tải được dữ liệu
            </div>
          )}
        </div>
      </SheetContent>
    </Sheet>
  );
}

function SectionTitle({ icon, title }: { icon: React.ReactNode; title: string }) {
  return (
    <h3 className="text-xs font-semibold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
      {icon}
      {title}
    </h3>
  );
}
