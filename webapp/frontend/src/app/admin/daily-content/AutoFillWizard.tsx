"use client";

import { useState, useMemo } from "react";
import {
  Wand2,
  CalendarRange,
  MessageSquareQuote,
  Landmark,
  Crown,
  Sparkles,
  Loader2,
  CheckCircle2,
  BarChart3,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Switch } from "@/components/ui/switch";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { useAutoFillDailyContent, useDailyContentStats } from "@/hooks/useV3";

const CONTENT_TYPES = [
  { id: "quote", label: "Danh ngôn", icon: MessageSquareQuote, color: "text-warm-amber" },
  { id: "event", label: "Sự kiện lịch sử", icon: Landmark, color: "text-blue-500" },
  { id: "famous_person", label: "Nhân vật nổi tiếng", icon: Crown, color: "text-purple-500" },
  { id: "folk_festival", label: "Lễ hội dân gian", icon: Sparkles, color: "text-pink-500" },
] as const;

interface AutoFillWizardProps {
  open: boolean;
  onClose: () => void;
}

export function AutoFillWizard({ open, onClose }: AutoFillWizardProps) {
  const now = new Date();
  const yearStr = now.getFullYear().toString();
  const [startDate, setStartDate] = useState(`${yearStr}-01-01`);
  const [endDate, setEndDate] = useState(`${yearStr}-12-31`);
  const [selectedTypes, setSelectedTypes] = useState<string[]>([
    "event",
    "famous_person",
    "folk_festival",
  ]);
  const [skipExisting, setSkipExisting] = useState(true);
  const [step, setStep] = useState<"config" | "confirm" | "result">("config");

  const autoFillMutation = useAutoFillDailyContent();
  const { data: statsData, isLoading: statsLoading } = useDailyContentStats(now.getFullYear());

  const stats = statsData?.data;

  const toggleType = (typeId: string) => {
    setSelectedTypes((prev) =>
      prev.includes(typeId) ? prev.filter((t) => t !== typeId) : [...prev, typeId]
    );
  };

  const dayCount = useMemo(() => {
    const start = new Date(startDate);
    const end = new Date(endDate);
    if (isNaN(start.getTime()) || isNaN(end.getTime())) return 0;
    return Math.max(0, Math.ceil((end.getTime() - start.getTime()) / 86400000) + 1);
  }, [startDate, endDate]);

  const handleAutoFill = () => {
    autoFillMutation.mutate(
      {
        start_date: startDate,
        end_date: endDate,
        content_types: selectedTypes,
        skip_existing: skipExisting,
      },
      {
        onSuccess: () => setStep("result"),
      }
    );
  };

  const handleClose = () => {
    setStep("config");
    onClose();
  };

  const result = autoFillMutation.data?.data;

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Wand2 className="text-warm-amber h-5 w-5" />
            Auto-fill Nội Dung Ngày
          </DialogTitle>
          <DialogDescription>
            Tự động gán nội dung có sẵn (sự kiện, nhân vật, lễ hội) vào lịch hằng ngày.
          </DialogDescription>
        </DialogHeader>

        {step === "config" && (
          <div className="space-y-6 pt-2">
            {/* Stats overview */}
            {stats && (
              <div className="bg-muted/50 space-y-2 rounded-lg p-4">
                <div className="flex items-center gap-2 text-sm font-medium">
                  <BarChart3 className="text-muted-foreground h-4 w-4" />
                  Thống kê hiện tại ({stats.coverage_summary.year})
                </div>
                <div className="grid grid-cols-3 gap-3 text-center">
                  <div>
                    <div className="text-foreground text-2xl font-bold">
                      {stats.coverage_summary.covered_days}
                    </div>
                    <div className="text-muted-foreground text-xs">Ngày có nội dung</div>
                  </div>
                  <div>
                    <div className="text-muted-foreground text-2xl font-bold">
                      {stats.coverage_summary.empty_days}
                    </div>
                    <div className="text-muted-foreground text-xs">Ngày trống</div>
                  </div>
                  <div>
                    <div className="text-warm-amber text-2xl font-bold">
                      {stats.coverage_summary.coverage_rate.toFixed(1)}%
                    </div>
                    <div className="text-muted-foreground text-xs">Tỷ lệ phủ</div>
                  </div>
                </div>
                {/* Coverage bar */}
                <div className="bg-muted h-2 w-full overflow-hidden rounded-full">
                  <div
                    className="bg-warm-amber h-full rounded-full transition-all"
                    style={{ width: `${Math.min(100, stats.coverage_summary.coverage_rate)}%` }}
                  />
                </div>
                {/* By type */}
                <div className="flex flex-wrap gap-2 text-xs">
                  {Object.entries(stats.by_type).map(([type, count]) => (
                    <span key={type} className="bg-background rounded border px-2 py-0.5">
                      {CONTENT_TYPES.find((t) => t.id === type)?.label ?? type}: {count}
                    </span>
                  ))}
                </div>
              </div>
            )}
            {statsLoading && (
              <div className="bg-muted/50 flex items-center justify-center rounded-lg p-4">
                <Loader2 className="text-muted-foreground h-5 w-5 animate-spin" />
              </div>
            )}

            {/* Date range */}
            <div className="space-y-2">
              <Label className="flex items-center gap-2">
                <CalendarRange className="h-4 w-4" />
                Khoảng thời gian
              </Label>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-muted-foreground text-xs">Từ ngày</label>
                  <Input
                    type="date"
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                  />
                </div>
                <div>
                  <label className="text-muted-foreground text-xs">Đến ngày</label>
                  <Input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
                </div>
              </div>
              <p className="text-muted-foreground text-xs">
                {dayCount > 0 ? `${dayCount} ngày` : "Chọn khoảng thời gian hợp lệ"}
              </p>
            </div>

            {/* Content types */}
            <div className="space-y-3">
              <Label>Loại nội dung tự động gán</Label>
              <div className="grid grid-cols-2 gap-2">
                {CONTENT_TYPES.map((type) => {
                  const Icon = type.icon;
                  const checked = selectedTypes.includes(type.id);
                  return (
                    <div
                      key={type.id}
                      role="button"
                      tabIndex={0}
                      onClick={() => toggleType(type.id)}
                      onKeyDown={(e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); toggleType(type.id); } }}
                      className={`flex cursor-pointer items-center gap-2 rounded-lg border p-3 text-left transition-all ${
                        checked
                          ? "border-warm-amber/50 bg-warm-amber/5"
                          : "border-border hover:border-muted-foreground/30"
                      }`}
                    >
                      <Checkbox checked={checked} className="pointer-events-none" />
                      <Icon className={`h-4 w-4 ${type.color}`} />
                      <span className="text-sm">{type.label}</span>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Skip existing */}
            <div className="flex items-center justify-between">
              <div>
                <Label htmlFor="skip-existing">Bỏ qua ngày đã có nội dung</Label>
                <p className="text-muted-foreground mt-0.5 text-xs">
                  Không tạo trùng lặp cho các ngày đã được gán nội dung
                </p>
              </div>
              <Switch id="skip-existing" checked={skipExisting} onCheckedChange={setSkipExisting} />
            </div>

            {/* Actions */}
            <div className="flex justify-end gap-2 pt-2">
              <Button variant="outline" onClick={handleClose}>
                Hủy
              </Button>
              <Button
                onClick={() => setStep("confirm")}
                disabled={selectedTypes.length === 0 || dayCount === 0}
              >
                Tiếp theo
              </Button>
            </div>
          </div>
        )}

        {step === "confirm" && (
          <div className="space-y-4 pt-2">
            <div className="space-y-3 rounded-lg border p-4">
              <h4 className="font-medium">Xác nhận tự động điền</h4>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Khoảng thời gian:</span>
                  <span>
                    {startDate} → {endDate} ({dayCount} ngày)
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Loại nội dung:</span>
                  <span>
                    {selectedTypes
                      .map((t) => CONTENT_TYPES.find((ct) => ct.id === t)?.label)
                      .join(", ")}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Bỏ qua ngày đã có:</span>
                  <span>{skipExisting ? "Có" : "Không"}</span>
                </div>
              </div>
            </div>

            <p className="text-muted-foreground text-sm">
              Hệ thống sẽ quét tất cả sự kiện, nhân vật, lễ hội có sẵn trong database và tự động gán
              vào lịch nội dung ngày tương ứng.
            </p>

            <div className="flex justify-end gap-2 pt-2">
              <Button variant="outline" onClick={() => setStep("config")}>
                Quay lại
              </Button>
              <Button onClick={handleAutoFill} disabled={autoFillMutation.isPending}>
                {autoFillMutation.isPending ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Đang xử lý...
                  </>
                ) : (
                  <>
                    <Wand2 className="mr-2 h-4 w-4" />
                    Bắt đầu Auto-fill
                  </>
                )}
              </Button>
            </div>
          </div>
        )}

        {step === "result" && result && (
          <div className="space-y-4 pt-2">
            <div className="flex flex-col items-center py-4 text-center">
              <CheckCircle2 className="mb-3 h-12 w-12 text-green-500" />
              <h4 className="text-lg font-medium">Hoàn thành!</h4>
              <p className="text-muted-foreground mt-1 text-sm">
                Đã tự động điền nội dung thành công.
              </p>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="rounded-lg border p-3 text-center">
                <div className="text-foreground text-2xl font-bold">{result.total_days}</div>
                <div className="text-muted-foreground text-xs">Tổng số ngày</div>
              </div>
              <div className="rounded-lg border p-3 text-center">
                <div className="text-2xl font-bold text-green-600">{result.filled_days}</div>
                <div className="text-muted-foreground text-xs">Ngày đã điền</div>
              </div>
              <div className="rounded-lg border p-3 text-center">
                <div className="text-muted-foreground text-2xl font-bold">
                  {result.skipped_days}
                </div>
                <div className="text-muted-foreground text-xs">Ngày bỏ qua</div>
              </div>
              <div className="rounded-lg border p-3 text-center">
                <div className="text-warm-amber text-2xl font-bold">{result.items_created}</div>
                <div className="text-muted-foreground text-xs">Nội dung tạo mới</div>
              </div>
            </div>

            <div className="flex justify-end pt-2">
              <Button onClick={handleClose}>Đóng</Button>
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
