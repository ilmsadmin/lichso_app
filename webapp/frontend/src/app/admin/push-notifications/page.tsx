"use client";

import { useState, useCallback } from "react";
import { Plus, Send, Trash2, Eye, Smartphone } from "lucide-react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Pagination } from "@/components/shared/Pagination";
import { PermissionGate } from "@/components/auth/PermissionGate";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
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
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { usePushCampaigns, usePushStats, useDeleteCampaign, useSendCampaign } from "@/hooks/usePushNotifications";
import { DEFAULT_PAGE_SIZE, ROUTES } from "@/lib/constants";
import { CAMPAIGN_STATUS_LABELS, type CampaignStatus } from "@/types/push-notification";
import { formatDistanceToNow } from "date-fns";
import { vi } from "date-fns/locale";

const STATUS_VARIANT: Record<CampaignStatus, "default" | "secondary" | "destructive" | "outline"> = {
  draft: "secondary",
  scheduled: "outline",
  sending: "default",
  sent: "default",
  failed: "destructive",
};

export default function PushNotificationsPage() {
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(DEFAULT_PAGE_SIZE);

  const { data, isLoading } = usePushCampaigns({ page, limit });
  const { data: statsData } = usePushStats();
  const deleteCampaign = useDeleteCampaign();
  const sendCampaign = useSendCampaign();

  const campaigns = data?.data ?? [];
  const meta = data?.meta;
  const activeDevices = statsData?.data?.active_devices ?? 0;

  const handleSend = useCallback((id: string) => {
    sendCampaign.mutate(id);
  }, [sendCampaign]);

  const handleDelete = useCallback((id: string) => {
    deleteCampaign.mutate(id);
  }, [deleteCampaign]);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Push Notifications</h1>
          <p className="text-muted-foreground text-sm mt-1">
            Quản lý thông báo đẩy tới người dùng Android
          </p>
        </div>
        <PermissionGate permission="content.create">
          <Button asChild>
            <Link href={`${ROUTES.ADMIN_PUSH_NOTIFICATIONS}/new`}>
              <Plus className="w-4 h-4 mr-2" />
              Tạo thông báo
            </Link>
          </Button>
        </PermissionGate>
      </div>

      {/* Stats card */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground flex items-center gap-2">
              <Smartphone className="w-4 h-4" />
              Thiết bị đang hoạt động
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{activeDevices.toLocaleString()}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Tổng chiến dịch
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">{meta?.total ?? 0}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Đã gửi
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-3xl font-bold">
              {campaigns.filter((c) => c.status === "sent").length}
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Campaigns table */}
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Tiêu đề</TableHead>
              <TableHead>Trạng thái</TableHead>
              <TableHead>Đối tượng</TableHead>
              <TableHead className="text-right">Đã gửi</TableHead>
              <TableHead>Thời gian</TableHead>
              <TableHead className="text-right">Thao tác</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center py-8 text-muted-foreground">
                  Đang tải...
                </TableCell>
              </TableRow>
            ) : campaigns.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center py-8 text-muted-foreground">
                  Chưa có chiến dịch nào
                </TableCell>
              </TableRow>
            ) : (
              campaigns.map((campaign) => (
                <TableRow key={campaign.id}>
                  <TableCell>
                    <div>
                      <p className="font-medium">{campaign.title}</p>
                      <p className="text-sm text-muted-foreground line-clamp-1">{campaign.body}</p>
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant={STATUS_VARIANT[campaign.status as CampaignStatus]}>
                      {CAMPAIGN_STATUS_LABELS[campaign.status as CampaignStatus]}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <span className="text-sm">
                      {campaign.target_type === "all" ? "Tất cả" : "Người dùng cụ thể"}
                    </span>
                  </TableCell>
                  <TableCell className="text-right">
                    {campaign.status === "sent" ? (
                      <span className="text-sm">
                        {campaign.sent_count.toLocaleString()}
                        {campaign.fail_count > 0 && (
                          <span className="text-destructive ml-1">
                            (-{campaign.fail_count})
                          </span>
                        )}
                      </span>
                    ) : (
                      <span className="text-muted-foreground">—</span>
                    )}
                  </TableCell>
                  <TableCell>
                    <span className="text-sm text-muted-foreground">
                      {formatDistanceToNow(new Date(campaign.created_at), {
                        addSuffix: true,
                        locale: vi,
                      })}
                    </span>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center justify-end gap-1">
                      <PermissionGate permission="content.read">
                        <Button variant="ghost" size="icon" asChild>
                          <Link href={`${ROUTES.ADMIN_PUSH_NOTIFICATIONS}/${campaign.id}`}>
                            <Eye className="w-4 h-4" />
                          </Link>
                        </Button>
                      </PermissionGate>

                      {(campaign.status === "draft" || campaign.status === "scheduled") && (
                        <PermissionGate permission="content.create">
                          <AlertDialog>
                            <AlertDialogTrigger asChild>
                              <Button
                                variant="ghost"
                                size="icon"
                                className="text-blue-600 hover:text-blue-700"
                                disabled={sendCampaign.isPending}
                              >
                                <Send className="w-4 h-4" />
                              </Button>
                            </AlertDialogTrigger>
                            <AlertDialogContent>
                              <AlertDialogHeader>
                                <AlertDialogTitle>Gửi thông báo?</AlertDialogTitle>
                                <AlertDialogDescription>
                                  Thông báo &ldquo;{campaign.title}&rdquo; sẽ được gửi tới{" "}
                                  {campaign.target_type === "all"
                                    ? `tất cả ${activeDevices.toLocaleString()} thiết bị`
                                    : "người dùng đã chọn"}
                                  . Hành động này không thể hoàn tác.
                                </AlertDialogDescription>
                              </AlertDialogHeader>
                              <AlertDialogFooter>
                                <AlertDialogCancel>Hủy</AlertDialogCancel>
                                <AlertDialogAction onClick={() => handleSend(campaign.id)}>
                                  Gửi ngay
                                </AlertDialogAction>
                              </AlertDialogFooter>
                            </AlertDialogContent>
                          </AlertDialog>
                        </PermissionGate>
                      )}

                      {campaign.status !== "sending" && (
                        <PermissionGate permission="content.delete">
                          <AlertDialog>
                            <AlertDialogTrigger asChild>
                              <Button
                                variant="ghost"
                                size="icon"
                                className="text-destructive hover:text-destructive"
                              >
                                <Trash2 className="w-4 h-4" />
                              </Button>
                            </AlertDialogTrigger>
                            <AlertDialogContent>
                              <AlertDialogHeader>
                                <AlertDialogTitle>Xóa chiến dịch?</AlertDialogTitle>
                                <AlertDialogDescription>
                                  Chiến dịch &ldquo;{campaign.title}&rdquo; sẽ bị xóa vĩnh viễn.
                                </AlertDialogDescription>
                              </AlertDialogHeader>
                              <AlertDialogFooter>
                                <AlertDialogCancel>Hủy</AlertDialogCancel>
                                <AlertDialogAction
                                  className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                                  onClick={() => handleDelete(campaign.id)}
                                >
                                  Xóa
                                </AlertDialogAction>
                              </AlertDialogFooter>
                            </AlertDialogContent>
                          </AlertDialog>
                        </PermissionGate>
                      )}
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {meta && meta.total_pages > 1 && (
        <Pagination
          page={page}
          limit={limit}
          total={meta.total}
          totalPages={meta.total_pages}
          onPageChange={setPage}
          onLimitChange={(newLimit) => { setLimit(newLimit); setPage(1); }}
        />
      )}
    </div>
  );
}
