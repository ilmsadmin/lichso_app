"use client";

import { use } from "react";
import Link from "next/link";
import { ChevronLeft, Send, Clock, CheckCircle2, XCircle, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { usePushCampaign, useSendCampaign, usePushStats } from "@/hooks/usePushNotifications";
import { ROUTES } from "@/lib/constants";
import { CAMPAIGN_STATUS_LABELS, type CampaignStatus } from "@/types/push-notification";
import { format } from "date-fns";
import { vi } from "date-fns/locale";
import { CampaignForm } from "../CampaignForm";
import { PermissionGate } from "@/components/auth/PermissionGate";

const STATUS_ICON: Record<CampaignStatus, React.ReactNode> = {
  draft: <Clock className="w-4 h-4" />,
  scheduled: <Clock className="w-4 h-4" />,
  sending: <Loader2 className="w-4 h-4 animate-spin" />,
  sent: <CheckCircle2 className="w-4 h-4 text-green-600" />,
  failed: <XCircle className="w-4 h-4 text-destructive" />,
};

export default function CampaignDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const { data, isLoading } = usePushCampaign(id);
  const { data: statsData } = usePushStats();
  const sendCampaign = useSendCampaign();

  const campaign = data?.data;
  const activeDevices = statsData?.data?.active_devices ?? 0;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-20">
        <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (!campaign) {
    return (
      <div className="text-center py-20 text-muted-foreground">
        Không tìm thấy chiến dịch
      </div>
    );
  }

  const canEdit = campaign.status === "draft" || campaign.status === "scheduled";
  const canSend = campaign.status === "draft" || campaign.status === "scheduled";

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" asChild>
            <Link href={ROUTES.ADMIN_PUSH_NOTIFICATIONS}>
              <ChevronLeft className="w-5 h-5" />
            </Link>
          </Button>
          <div>
            <h1 className="text-2xl font-bold flex items-center gap-2">
              {campaign.title}
              <span className="inline-flex">{STATUS_ICON[campaign.status as CampaignStatus]}</span>
            </h1>
            <p className="text-muted-foreground text-sm">
              <Badge variant="outline" className="text-xs">
                {CAMPAIGN_STATUS_LABELS[campaign.status as CampaignStatus]}
              </Badge>
            </p>
          </div>
        </div>

        {canSend && (
          <PermissionGate permission="content.create">
            <AlertDialog>
              <AlertDialogTrigger asChild>
                <Button disabled={sendCampaign.isPending}>
                  <Send className="w-4 h-4 mr-2" />
                  Gửi ngay
                </Button>
              </AlertDialogTrigger>
              <AlertDialogContent>
                <AlertDialogHeader>
                  <AlertDialogTitle>Gửi thông báo?</AlertDialogTitle>
                  <AlertDialogDescription>
                    Thông báo &ldquo;{campaign.title}&rdquo; sẽ được gửi tới{" "}
                    {campaign.target_type === "all"
                      ? `tất cả ${activeDevices.toLocaleString()} thiết bị Android`
                      : "người dùng đã chọn"}
                    . Hành động này không thể hoàn tác.
                  </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                  <AlertDialogCancel>Hủy</AlertDialogCancel>
                  <AlertDialogAction onClick={() => sendCampaign.mutate(campaign.id)}>
                    Gửi ngay
                  </AlertDialogAction>
                </AlertDialogFooter>
              </AlertDialogContent>
            </AlertDialog>
          </PermissionGate>
        )}
      </div>

      {/* Delivery stats (only for sent campaigns) */}
      {campaign.status === "sent" && (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-xs text-muted-foreground">Đã gửi thành công</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold text-green-600">
                {campaign.sent_count.toLocaleString()}
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-xs text-muted-foreground">Thất bại</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold text-destructive">
                {campaign.fail_count.toLocaleString()}
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-xs text-muted-foreground">Tổng cộng</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold">
                {(campaign.sent_count + campaign.fail_count).toLocaleString()}
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-xs text-muted-foreground">Tỷ lệ thành công</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold">
                {campaign.sent_count + campaign.fail_count > 0
                  ? Math.round(
                      (campaign.sent_count /
                        (campaign.sent_count + campaign.fail_count)) *
                        100
                    )
                  : 0}
                %
              </p>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Campaign details */}
      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Chi tiết chiến dịch</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3 text-sm">
          <div className="flex justify-between">
            <span className="text-muted-foreground">Tiêu đề</span>
            <span className="font-medium">{campaign.title}</span>
          </div>
          <Separator />
          <div className="flex justify-between">
            <span className="text-muted-foreground">Nội dung</span>
            <span className="max-w-xs text-right">{campaign.body}</span>
          </div>
          <Separator />
          <div className="flex justify-between">
            <span className="text-muted-foreground">Đối tượng</span>
            <span>{campaign.target_type === "all" ? "Tất cả" : "Người dùng cụ thể"}</span>
          </div>
          {campaign.click_action && (
            <>
              <Separator />
              <div className="flex justify-between">
                <span className="text-muted-foreground">Click action</span>
                <code className="text-xs bg-muted px-2 py-1 rounded">{campaign.click_action}</code>
              </div>
            </>
          )}
          <Separator />
          <div className="flex justify-between">
            <span className="text-muted-foreground">Tạo lúc</span>
            <span>
              {format(new Date(campaign.created_at), "dd/MM/yyyy HH:mm", { locale: vi })}
            </span>
          </div>
          {campaign.sent_at && (
            <>
              <Separator />
              <div className="flex justify-between">
                <span className="text-muted-foreground">Gửi lúc</span>
                <span>
                  {format(new Date(campaign.sent_at), "dd/MM/yyyy HH:mm", { locale: vi })}
                </span>
              </div>
            </>
          )}
        </CardContent>
      </Card>

      {/* Edit form for editable campaigns */}
      {canEdit && (
        <div>
          <h2 className="text-lg font-semibold mb-4">Chỉnh sửa</h2>
          <CampaignForm campaign={campaign} />
        </div>
      )}
    </div>
  );
}
