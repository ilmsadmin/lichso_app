"use client";

import { useEffect, useMemo, useState } from "react";
import { Search, MessageSquareMore, Smartphone, Star } from "lucide-react";
import { useAppReviews, useUpdateAppReview } from "@/hooks/useAppReviews";
import type { AppReview, AppReviewPlatform, AppReviewStatus } from "@/types/app-review";
import { DEFAULT_PAGE_SIZE } from "@/lib/constants";
import { Pagination } from "@/components/shared/Pagination";
import { SearchInput } from "@/components/shared/SearchInput";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

const statusLabels: Record<AppReviewStatus, string> = {
  new: "Mới",
  reviewed: "Đã xem",
  resolved: "Đã xử lý",
};

const flowLabels = {
  low_rating_feedback: "1-3 sao",
  high_rating_prompt: "4-5 sao",
} as const;

const sourceLabels: Record<string, string> = {
  in_app_review: "In-App Review",
  play_store_fallback: "Play Store (fallback)",
  play_store_manual: "Play Store (thủ công)",
};

function renderStars(stars: number) {
  return "★".repeat(stars) + "☆".repeat(5 - stars);
}

export default function AppReviewsPage() {
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(DEFAULT_PAGE_SIZE);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState<"all" | AppReviewStatus>("all");
  const [platform, setPlatform] = useState<"all" | AppReviewPlatform>("all");
  const [stars, setStars] = useState<string>("all");
  const [selectedReview, setSelectedReview] = useState<AppReview | null>(null);
  const [draftStatus, setDraftStatus] = useState<AppReviewStatus>("new");
  const [draftNote, setDraftNote] = useState("");

  const { data, isLoading } = useAppReviews({
    page,
    limit,
    search: search || undefined,
    status: status !== "all" ? status : undefined,
    platform: platform !== "all" ? platform : undefined,
    stars: stars !== "all" ? Number(stars) : undefined,
  });

  const updateReview = useUpdateAppReview(selectedReview?.id ?? "");

  useEffect(() => {
    if (!selectedReview) return;
    setDraftStatus(selectedReview.status);
    setDraftNote(selectedReview.admin_note ?? "");
  }, [selectedReview]);

  const reviews = data?.data ?? [];
  const meta = data?.meta;

  const reviewSummary = useMemo(() => {
    const total = reviews.length;
    const lowRatings = reviews.filter((item) => item.stars <= 3).length;
    const highRatings = reviews.filter((item) => item.stars >= 4).length;
    return { total, lowRatings, highRatings };
  }, [reviews]);

  const formatDate = (value: string) =>
    new Date(value).toLocaleString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });

  const handleSave = async () => {
    if (!selectedReview) return;
    const response = await updateReview.mutateAsync({
      status: draftStatus,
      admin_note: draftNote,
    });
    if (response.success && response.data) {
      setSelectedReview(response.data);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Đánh giá ứng dụng</h1>
          <p className="text-muted-foreground">
            Theo dõi toàn bộ đánh giá 1-5 sao được gửi từ app Android và iOS lên máy chủ.
          </p>
        </div>

        <div className="grid grid-cols-3 gap-3 lg:min-w-[360px]">
          <div className="rounded-xl border p-3">
            <div className="text-muted-foreground text-xs">Trang hiện tại</div>
            <div className="mt-1 text-xl font-semibold">{reviewSummary.total}</div>
          </div>
          <div className="rounded-xl border p-3">
            <div className="text-muted-foreground text-xs">1-3 sao</div>
            <div className="mt-1 text-xl font-semibold text-amber-600">
              {reviewSummary.lowRatings}
            </div>
          </div>
          <div className="rounded-xl border p-3">
            <div className="text-muted-foreground text-xs">4-5 sao</div>
            <div className="mt-1 text-xl font-semibold text-emerald-600">
              {reviewSummary.highRatings}
            </div>
          </div>
        </div>
      </div>

      <div className="flex flex-col gap-3 xl:flex-row xl:items-center">
        <SearchInput
          value={search}
          onChange={(value) => {
            setSearch(value);
            setPage(1);
          }}
          placeholder="Tìm theo nội dung, email, thiết bị, phiên bản..."
          className="w-full xl:max-w-md"
        />
        <Select
          value={status}
          onValueChange={(value: "all" | AppReviewStatus) => {
            setStatus(value);
            setPage(1);
          }}
        >
          <SelectTrigger className="w-full xl:w-[180px]">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả trạng thái</SelectItem>
            <SelectItem value="new">Mới</SelectItem>
            <SelectItem value="reviewed">Đã xem</SelectItem>
            <SelectItem value="resolved">Đã xử lý</SelectItem>
          </SelectContent>
        </Select>
        <Select
          value={platform}
          onValueChange={(value: "all" | AppReviewPlatform) => {
            setPlatform(value);
            setPage(1);
          }}
        >
          <SelectTrigger className="w-full xl:w-[160px]">
            <SelectValue placeholder="Nền tảng" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả nền tảng</SelectItem>
            <SelectItem value="android">Android</SelectItem>
            <SelectItem value="ios">iOS</SelectItem>
          </SelectContent>
        </Select>
        <Select
          value={stars}
          onValueChange={(value) => {
            setStars(value);
            setPage(1);
          }}
        >
          <SelectTrigger className="w-full xl:w-[140px]">
            <SelectValue placeholder="Số sao" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả sao</SelectItem>
            <SelectItem value="1">1 sao</SelectItem>
            <SelectItem value="2">2 sao</SelectItem>
            <SelectItem value="3">3 sao</SelectItem>
            <SelectItem value="4">4 sao</SelectItem>
            <SelectItem value="5">5 sao</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <div className="flex flex-col items-center gap-3">
            <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
            <p className="text-muted-foreground text-sm">Đang tải đánh giá ứng dụng...</p>
          </div>
        </div>
      ) : reviews.length === 0 ? (
        <div className="rounded-2xl border border-dashed p-12 text-center">
          <Search className="text-muted-foreground mx-auto mb-3 h-8 w-8" />
          <h2 className="text-lg font-semibold">Chưa có đánh giá nào khớp bộ lọc</h2>
          <p className="text-muted-foreground mt-2 text-sm">
            Khi người dùng đánh giá app trên Android hoặc iOS, dữ liệu sẽ xuất hiện ở đây.
          </p>
        </div>
      ) : (
        <>
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Người dùng / Thiết bị</TableHead>
                  <TableHead>Nền tảng</TableHead>
                  <TableHead>Số sao</TableHead>
                  <TableHead>Luồng</TableHead>
                  <TableHead>Nội dung</TableHead>
                  <TableHead>Trạng thái</TableHead>
                  <TableHead>Thời gian</TableHead>
                  <TableHead className="text-right">Thao tác</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {reviews.map((review) => (
                  <TableRow key={review.id}>
                    <TableCell className="max-w-[260px]">
                      <div className="space-y-1">
                        <div className="font-medium">
                          {review.user?.full_name || review.user?.email || "Khách ẩn danh"}
                        </div>
                        <div className="text-muted-foreground text-xs">
                          {review.user?.email ||
                            review.device_name ||
                            review.device_id ||
                            "Không có định danh"}
                        </div>
                        <div className="text-muted-foreground text-xs">
                          {review.app_version ? `App ${review.app_version}` : "Chưa có version"}
                          {review.os_version ? ` • OS ${review.os_version}` : ""}
                        </div>
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge variant="secondary" className="uppercase">
                        {review.platform}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <div className="font-medium text-amber-600">{renderStars(review.stars)}</div>
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant={
                          review.review_flow === "high_rating_prompt" ? "default" : "outline"
                        }
                      >
                        {flowLabels[review.review_flow]}
                      </Badge>
                    </TableCell>
                    <TableCell className="max-w-[320px]">
                      <p className="line-clamp-2 text-sm">
                        {review.review_text?.trim() || "Không có nội dung văn bản"}
                      </p>
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant={
                          review.status === "resolved"
                            ? "default"
                            : review.status === "reviewed"
                              ? "secondary"
                              : "outline"
                        }
                      >
                        {statusLabels[review.status]}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-sm">{formatDate(review.created_at)}</TableCell>
                    <TableCell className="text-right">
                      <Button variant="outline" size="sm" onClick={() => setSelectedReview(review)}>
                        <MessageSquareMore className="mr-2 h-4 w-4" />
                        Xem
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>

          {meta && (
            <Pagination
              page={meta.page}
              limit={meta.limit}
              total={meta.total}
              totalPages={meta.total_pages}
              onPageChange={setPage}
              onLimitChange={(newLimit) => {
                setLimit(newLimit);
                setPage(1);
              }}
            />
          )}
        </>
      )}

      <Dialog open={!!selectedReview} onOpenChange={(open) => !open && setSelectedReview(null)}>
        <DialogContent className="sm:max-w-2xl">
          <DialogHeader>
            <DialogTitle>Chi tiết đánh giá ứng dụng</DialogTitle>
            <DialogDescription>
              Xem phản hồi gốc từ app và cập nhật trạng thái xử lý nội bộ.
            </DialogDescription>
          </DialogHeader>

          {selectedReview && (
            <div className="space-y-5">
              <div className="grid gap-3 md:grid-cols-2">
                <div className="rounded-xl border p-4">
                  <div className="text-muted-foreground text-xs">Người gửi</div>
                  <div className="mt-1 font-medium">
                    {selectedReview.user?.full_name ||
                      selectedReview.user?.email ||
                      "Khách ẩn danh"}
                  </div>
                  <div className="text-muted-foreground mt-1 text-sm">
                    {selectedReview.user?.email ||
                      selectedReview.device_name ||
                      selectedReview.device_id}
                  </div>
                </div>
                <div className="rounded-xl border p-4">
                  <div className="text-muted-foreground text-xs">Thiết bị</div>
                  <div className="mt-1 flex items-center gap-2 font-medium uppercase">
                    <Smartphone className="h-4 w-4" />
                    {selectedReview.platform}
                  </div>
                  <div className="text-muted-foreground mt-1 text-sm">
                    {selectedReview.device_name || "Không rõ thiết bị"}
                    {selectedReview.os_version ? ` • OS ${selectedReview.os_version}` : ""}
                    {selectedReview.app_version ? ` • App ${selectedReview.app_version}` : ""}
                  </div>
                </div>
              </div>

              <div className="rounded-xl border p-4">
                <div className="flex flex-wrap items-center gap-3">
                  <Badge variant="secondary">
                    <Star className="mr-1 h-3.5 w-3.5" />
                    {selectedReview.stars}/5 sao
                  </Badge>
                  <Badge variant="outline">{flowLabels[selectedReview.review_flow]}</Badge>
                  {selectedReview.review_source && (
                    <Badge
                      variant={selectedReview.review_source === "in_app_review" ? "default" : "secondary"}
                    >
                      {sourceLabels[selectedReview.review_source] || selectedReview.review_source}
                    </Badge>
                  )}
                  <span className="text-muted-foreground text-sm">
                    Gửi lúc {formatDate(selectedReview.created_at)}
                  </span>
                </div>
                <div className="mt-4 text-sm leading-6 whitespace-pre-wrap">
                  {selectedReview.review_text?.trim() || "Người dùng không nhập thêm nội dung."}
                </div>
              </div>

              <div className="grid gap-4">
                <div>
                  <label className="mb-2 block text-sm font-medium">Trạng thái xử lý</label>
                  <Select
                    value={draftStatus}
                    onValueChange={(value: AppReviewStatus) => setDraftStatus(value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Chọn trạng thái" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="new">Mới</SelectItem>
                      <SelectItem value="reviewed">Đã xem</SelectItem>
                      <SelectItem value="resolved">Đã xử lý</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <label className="mb-2 block text-sm font-medium">Ghi chú nội bộ</label>
                  <Textarea
                    value={draftNote}
                    onChange={(event) => setDraftNote(event.target.value)}
                    placeholder="Ví dụ: đã chuyển bug cho team mobile, đã liên hệ người dùng..."
                    rows={5}
                  />
                </div>
              </div>

              <div className="flex justify-end gap-3">
                <Button variant="outline" onClick={() => setSelectedReview(null)}>
                  Đóng
                </Button>
                <Button onClick={handleSave} disabled={updateReview.isPending}>
                  {updateReview.isPending ? "Đang lưu..." : "Lưu cập nhật"}
                </Button>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
