"use client";

import { useState, use } from "react";
import Link from "next/link";
import { ArrowLeft, Coins, Trophy, Flame, Sparkles, Settings2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Textarea } from "@/components/ui/textarea";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { PermissionGate } from "@/components/auth/PermissionGate";
import {
  useUserPointsDetail,
  useUserDailyPoints,
  useAdjustUserPoints,
} from "@/hooks/usePointsAdmin";
import { ROUTES } from "@/lib/constants";

function fmt(n: number): string {
  return n.toLocaleString("vi-VN");
}

function fmtDateTime(s?: string): string {
  if (!s) return "—";
  return new Date(s).toLocaleString("vi-VN");
}

function StatCard({
  icon,
  label,
  value,
  hint,
}: {
  icon: React.ReactNode;
  label: string;
  value: string;
  hint?: string;
}) {
  return (
    <Card>
      <CardContent className="flex items-center gap-3 p-4">
        <div className="bg-muted flex h-10 w-10 items-center justify-center rounded-lg">{icon}</div>
        <div>
          <p className="text-muted-foreground text-xs">{label}</p>
          <p className="text-xl font-bold">{value}</p>
          {hint && <p className="text-muted-foreground text-xs">{hint}</p>}
        </div>
      </CardContent>
    </Card>
  );
}

export default function AdminUserPointsDetailPage({
  params,
}: {
  params: Promise<{ userId: string }>;
}) {
  const { userId } = use(params);

  const [days, setDays] = useState(30);
  const { data: detailRes, isLoading } = useUserPointsDetail(userId);
  const { data: dailyRes, isLoading: dailyLoading } = useUserDailyPoints(userId, days);
  const adjust = useAdjustUserPoints(userId);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [walletDelta, setWalletDelta] = useState("");
  const [resetQuiz, setResetQuiz] = useState(false);
  const [reason, setReason] = useState("");

  const detail = detailRes?.data;
  const daily = dailyRes?.data ?? [];

  const handleAdjust = () => {
    const delta = parseInt(walletDelta || "0", 10) || 0;
    if (delta === 0 && !resetQuiz) return;
    adjust.mutate(
      { wallet_delta: delta, reset_quiz_score: resetQuiz, reason: reason.trim() },
      {
        onSuccess: () => {
          setDialogOpen(false);
          setWalletDelta("");
          setResetQuiz(false);
          setReason("");
        },
      }
    );
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    );
  }

  if (!detail) {
    return (
      <div className="space-y-4">
        <Button variant="ghost" size="sm" asChild>
          <Link href={ROUTES.ADMIN_POINTS}>
            <ArrowLeft className="mr-2 h-4 w-4" /> Quay lại
          </Link>
        </Button>
        <p className="text-muted-foreground">Không tìm thấy người dùng.</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="icon" asChild>
            <Link href={ROUTES.ADMIN_POINTS}>
              <ArrowLeft className="h-4 w-4" />
            </Link>
          </Button>
          <div>
            <h1 className="text-2xl font-bold tracking-tight">
              {detail.display_name?.trim() || "(Không tên)"}
            </h1>
            <p className="text-muted-foreground text-sm">{detail.email}</p>
          </div>
        </div>

        <PermissionGate permission="users.update">
          <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
            <DialogTrigger asChild>
              <Button>
                <Settings2 className="mr-2 h-4 w-4" /> Điều chỉnh điểm
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Điều chỉnh điểm người dùng</DialogTitle>
                <DialogDescription>
                  Cộng/trừ số dư ví hoặc reset toàn bộ điểm quiz (dùng cho tài khoản cày điểm). Thao
                  tác được ghi log.
                </DialogDescription>
              </DialogHeader>

              <div className="space-y-4 py-2">
                <div className="space-y-2">
                  <Label htmlFor="wallet-delta">Thay đổi số dư ví (âm để trừ)</Label>
                  <Input
                    id="wallet-delta"
                    type="number"
                    placeholder="VD: -500 để trừ 500 điểm"
                    value={walletDelta}
                    onChange={(e) => setWalletDelta(e.target.value)}
                  />
                </div>

                <div className="flex items-center justify-between rounded-lg border p-3">
                  <div>
                    <p className="font-medium">Reset điểm quiz</p>
                    <p className="text-muted-foreground text-xs">
                      Đưa total/tuần/tháng/streak/XP về 0 và xoá điểm các phiên đã chơi.
                    </p>
                  </div>
                  <Switch checked={resetQuiz} onCheckedChange={setResetQuiz} />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="reason">Lý do</Label>
                  <Textarea
                    id="reason"
                    placeholder="VD: Phát hiện cày điểm qua offline sync"
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                  />
                </div>
              </div>

              <DialogFooter>
                <Button variant="outline" onClick={() => setDialogOpen(false)}>
                  Huỷ
                </Button>
                <Button
                  onClick={handleAdjust}
                  disabled={
                    adjust.isPending || (!resetQuiz && (parseInt(walletDelta || "0", 10) || 0) === 0)
                  }
                >
                  {adjust.isPending ? "Đang lưu..." : "Xác nhận"}
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </PermissionGate>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <StatCard
          icon={<Coins className="h-5 w-5 text-amber-500" />}
          label="Số dư ví"
          value={fmt(detail.wallet?.balance ?? 0)}
          hint={`Tích luỹ: ${fmt(detail.wallet?.lifetime_earned ?? 0)}`}
        />
        <StatCard
          icon={<Trophy className="h-5 w-5 text-yellow-500" />}
          label="Điểm quiz tổng"
          value={fmt(detail.quiz_score?.total_score ?? 0)}
          hint={`Tuần: ${fmt(detail.quiz_score?.week_score ?? 0)} · Tháng: ${fmt(detail.quiz_score?.month_score ?? 0)}`}
        />
        <StatCard
          icon={<Flame className="h-5 w-5 text-orange-500" />}
          label="Streak hiện tại"
          value={`${detail.quiz_score?.cur_streak ?? 0} ngày`}
          hint={`Cao nhất: ${detail.quiz_score?.best_streak ?? 0}`}
        />
        <StatCard
          icon={<Sparkles className="h-5 w-5 text-violet-500" />}
          label="XP"
          value={fmt(detail.quiz_score?.xp ?? 0)}
        />
      </div>

      {/* Daily history */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="text-lg">Lịch sử điểm hằng ngày</CardTitle>
          <Select value={String(days)} onValueChange={(v) => setDays(parseInt(v, 10))}>
            <SelectTrigger className="w-[130px]">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="7">7 ngày</SelectItem>
              <SelectItem value="30">30 ngày</SelectItem>
              <SelectItem value="90">90 ngày</SelectItem>
            </SelectContent>
          </Select>
        </CardHeader>
        <CardContent>
          {dailyLoading ? (
            <div className="flex items-center justify-center py-8">
              <div className="border-primary h-6 w-6 animate-spin rounded-full border-4 border-t-transparent" />
            </div>
          ) : (
            <div className="rounded-md border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Ngày</TableHead>
                    <TableHead className="text-right">Ví kiếm</TableHead>
                    <TableHead className="text-right">Ví tiêu</TableHead>
                    <TableHead className="text-right">Điểm quiz</TableHead>
                    <TableHead className="text-right">Lượt chơi</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {daily.map((d) => {
                    const suspicious = d.quiz_sessions >= 15;
                    return (
                      <TableRow key={d.date}>
                        <TableCell className="font-medium">{d.date}</TableCell>
                        <TableCell className="text-right text-emerald-600">
                          {d.app_points_earned > 0 ? `+${fmt(d.app_points_earned)}` : "—"}
                        </TableCell>
                        <TableCell className="text-right text-rose-600">
                          {d.app_points_spent > 0 ? `-${fmt(d.app_points_spent)}` : "—"}
                        </TableCell>
                        <TableCell className="text-right">{fmt(d.quiz_score)}</TableCell>
                        <TableCell className="text-right">
                          {d.quiz_sessions > 0 ? (
                            suspicious ? (
                              <Badge variant="destructive">{d.quiz_sessions}</Badge>
                            ) : (
                              d.quiz_sessions
                            )
                          ) : (
                            "—"
                          )}
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Two columns: transactions + sessions */}
      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Giao dịch ví gần đây</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            {detail.transactions.length === 0 ? (
              <p className="text-muted-foreground text-sm">Chưa có giao dịch.</p>
            ) : (
              detail.transactions.map((t) => (
                <div
                  key={t.id}
                  className="flex items-center justify-between border-b py-2 last:border-0"
                >
                  <div>
                    <p className="text-sm font-medium">{t.source}</p>
                    <p className="text-muted-foreground text-xs">{fmtDateTime(t.created_at)}</p>
                  </div>
                  <span
                    className={
                      t.direction === "earn"
                        ? "font-semibold text-emerald-600"
                        : "font-semibold text-rose-600"
                    }
                  >
                    {t.direction === "earn" ? "+" : "-"}
                    {fmt(t.amount)}
                  </span>
                </div>
              ))
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Phiên quiz gần đây</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            {detail.sessions.length === 0 ? (
              <p className="text-muted-foreground text-sm">Chưa có phiên chơi.</p>
            ) : (
              detail.sessions.map((sx) => (
                <div
                  key={sx.id}
                  className="flex items-center justify-between border-b py-2 last:border-0"
                >
                  <div>
                    <p className="text-sm font-medium">
                      {sx.session_type === "daily" ? "Hằng ngày" : "Chủ đề"}
                      <span className="text-muted-foreground"> · {sx.total} câu</span>
                    </p>
                    <p className="text-muted-foreground text-xs">{fmtDateTime(sx.finished_at)}</p>
                  </div>
                  <Badge variant={sx.score_v2 || sx.score ? "secondary" : "outline"}>
                    {fmt(sx.score_v2 || sx.score)} điểm
                  </Badge>
                </div>
              ))
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
