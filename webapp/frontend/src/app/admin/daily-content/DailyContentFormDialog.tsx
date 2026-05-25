"use client";

import { useEffect, useState, useMemo } from "react";
import {
  Search,
  MessageSquareQuote,
  Landmark,
  Newspaper,
  Crown,
  Sparkles,
  Star,
  Check,
  X,
  Loader2,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Badge } from "@/components/ui/badge";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ScrollArea } from "@/components/ui/scroll-area";
import { useCreateDailyContent, useUpdateDailyContent, useDailyContentSchedule } from "@/hooks/useV3";
import { useQuotes } from "@/hooks/useQuotes";
import { useEvents } from "@/hooks/useEvents";
import { useArticles } from "@/hooks/useArticles";
import { useFamousPeople } from "@/hooks/useFamousPeople";
import { useFolkFestivals } from "@/hooks/useFolkFestivals";
import type { CreateDailyContentRequest, UpdateDailyContentRequest, DailyContentType, ScheduleMode } from "@/types/v3";

const CONTENT_TYPE_CONFIG = {
  quote: { label: "Danh ngôn", icon: MessageSquareQuote, color: "text-warm-amber", placeholder: "Tìm theo nội dung hoặc tác giả..." },
  event: { label: "Sự kiện lịch sử", icon: Landmark, color: "text-blue-500", placeholder: "Tìm theo tên sự kiện..." },
  article: { label: "Bài viết", icon: Newspaper, color: "text-emerald-500", placeholder: "Tìm theo tiêu đề bài viết..." },
  famous_person: { label: "Nhân vật nổi tiếng", icon: Crown, color: "text-purple-500", placeholder: "Tìm theo tên nhân vật..." },
  folk_festival: { label: "Lễ hội dân gian", icon: Sparkles, color: "text-pink-500", placeholder: "Tìm theo tên lễ hội..." },
  custom: { label: "Nội dung tùy chỉnh", icon: Star, color: "text-orange-500", placeholder: "" },
} as const;

interface ContentItem {
  id: string;
  title: string;
  subtitle?: string;
}

/** Hook to search content by type */
function useContentSearch(type: DailyContentType, search: string) {
  const enabled = type !== "custom" && search.length >= 1;
  const params = { search: search || undefined, limit: 10, page: 1 };

  const quotes = useQuotes(type === "quote" ? params : undefined);
  const events = useEvents(type === "event" ? params : undefined);
  const articles = useArticles(type === "article" ? params : undefined);
  const famousPeople = useFamousPeople(type === "famous_person" ? params : undefined);
  const festivals = useFolkFestivals(type === "folk_festival" ? params : undefined);

  return useMemo(() => {
    let items: ContentItem[] = [];
    let isLoading = false;

    switch (type) {
      case "quote":
        isLoading = quotes.isLoading;
        items = (quotes.data?.data ?? []).map((q) => ({
          id: q.id,
          title: q.quote.length > 80 ? q.quote.slice(0, 80) + "…" : q.quote,
          subtitle: `— ${q.author}`,
        }));
        break;
      case "event":
        isLoading = events.isLoading;
        items = (events.data?.data ?? []).map((e) => ({
          id: e.id,
          title: e.title,
          subtitle: e.event_date ? new Date(e.event_date).toLocaleDateString("vi-VN") : undefined,
        }));
        break;
      case "article":
        isLoading = articles.isLoading;
        items = (articles.data?.data ?? []).map((a) => ({
          id: a.id,
          title: a.title,
          subtitle: a.category?.name,
        }));
        break;
      case "famous_person":
        isLoading = famousPeople.isLoading;
        items = (famousPeople.data?.data ?? []).map((p) => ({
          id: p.id,
          title: p.name,
          subtitle: p.short_bio || p.nationality,
        }));
        break;
      case "folk_festival":
        isLoading = festivals.isLoading;
        items = (festivals.data?.data ?? []).map((f) => ({
          id: f.id,
          title: f.name,
          subtitle: f.festival_type,
        }));
        break;
    }

    return { items, isLoading, enabled };
  }, [type, quotes, events, articles, famousPeople, festivals]);
}

interface DailyContentFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  editingId?: string | null;
}

/** Truncate text to fit the dialog width */
function truncateText(text: string, max = 40): string {
  return text.length > max ? text.slice(0, max) + "…" : text;
}

/** Extract a display label from the `content` object returned by the API */
function getContentLabel(contentType: DailyContentType, content: unknown): string {
  if (!content || typeof content !== "object") return "";
  const c = content as Record<string, unknown>;
  switch (contentType) {
    case "quote":
      return truncateText((c.quote as string) ?? "");
    case "event":
      return truncateText((c.title as string) ?? "");
    case "article":
      return truncateText((c.title as string) ?? "");
    case "famous_person":
      return truncateText((c.name as string) ?? "");
    case "folk_festival":
      return truncateText((c.name as string) ?? "");
    default:
      return "";
  }
}

export function DailyContentFormDialog({
  open,
  onOpenChange,
  editingId,
}: DailyContentFormDialogProps) {
  const isEditing = !!editingId;
  const createMutation = useCreateDailyContent();
  const updateMutation = useUpdateDailyContent();
  const { data: existingData, isLoading: isLoadingExisting } = useDailyContentSchedule(
    editingId ?? null
  );

  const [populatedFor, setPopulatedFor] = useState<string | null>(null);
  const [contentType, setContentType] = useState<DailyContentType>("quote");
  const [contentId, setContentId] = useState("");
  const [contentSearch, setContentSearch] = useState("");
  const [selectedLabel, setSelectedLabel] = useState("");
  const [customTitle, setCustomTitle] = useState("");
  const [customContent, setCustomContent] = useState("");
  const [customImage, setCustomImage] = useState("");
  const [scheduleMode, setScheduleMode] = useState<ScheduleMode>("fixed_date");
  const [fixedDate, setFixedDate] = useState("");
  const [dayOfYear, setDayOfYear] = useState("");
  const [recurringMonth, setRecurringMonth] = useState("");
  const [recurringDay, setRecurringDay] = useState("");
  const [lunarMonth, setLunarMonth] = useState("");
  const [lunarDay, setLunarDay] = useState("");
  const [displayPriority, setDisplayPriority] = useState("0");
  const [displaySection, setDisplaySection] = useState("main");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  const { items, isLoading: searchLoading } = useContentSearch(contentType, contentSearch);

  // Populate form when editing an existing schedule
  useEffect(() => {
    if (!open || !editingId || !existingData?.data) return;
    // Skip if we already populated for this exact item
    if (populatedFor === editingId) return;
    const s = existingData.data;
    // Ensure the fetched data matches the editingId (not stale cache)
    if (s.id !== editingId) return;
    setContentType(s.content_type);
    setContentId(s.content_id ?? "");
    const label = getContentLabel(s.content_type, s.content);
    setSelectedLabel(label || (s.content_id ? `ID: ${s.content_id.slice(0, 12)}…` : ""));
    setCustomTitle(s.custom_title ?? "");
    setCustomContent(s.custom_content ?? "");
    setCustomImage(s.custom_image ?? "");
    setScheduleMode(s.schedule_mode);
    setFixedDate(s.fixed_date ?? "");
    setDayOfYear(s.day_of_year ? String(s.day_of_year) : "");
    setRecurringMonth(s.recurring_month ? String(s.recurring_month) : "");
    setRecurringDay(s.recurring_day ? String(s.recurring_day) : "");
    setLunarMonth(s.lunar_month ? String(s.lunar_month) : "");
    setLunarDay(s.lunar_day ? String(s.lunar_day) : "");
    setDisplayPriority(String(s.display_priority ?? 0));
    setDisplaySection(s.display_section ?? "main");
    setStartDate(s.start_date ?? "");
    setEndDate(s.end_date ?? "");
    setPopulatedFor(editingId);
  }, [open, editingId, existingData, populatedFor]);

  // Reset form when dialog closes
  useEffect(() => {
    if (!open) {
      setPopulatedFor(null);
      setContentType("quote");
      setContentId("");
      setContentSearch("");
      setSelectedLabel("");
      setCustomTitle("");
      setCustomContent("");
      setCustomImage("");
      setScheduleMode("fixed_date");
      setFixedDate("");
      setDayOfYear("");
      setRecurringMonth("");
      setRecurringDay("");
      setLunarMonth("");
      setLunarDay("");
      setDisplayPriority("0");
      setDisplaySection("main");
      setStartDate("");
      setEndDate("");
    }
  }, [open]);

  // Reset selection when content type changes (only for manual changes, not populate)
  const [typeChangedManually, setTypeChangedManually] = useState(false);
  useEffect(() => {
    if (typeChangedManually) {
      setContentId("");
      setContentSearch("");
      setSelectedLabel("");
      setTypeChangedManually(false);
    }
  }, [typeChangedManually]);

  const handleTypeChange = (v: DailyContentType) => {
    setContentType(v);
    setTypeChangedManually(true);
  };

  const handleSelectContent = (item: ContentItem) => {
    setContentId(item.id);
    setSelectedLabel(truncateText(item.title));
    setContentSearch("");
  };

  const handleClearSelection = () => {
    setContentId("");
    setSelectedLabel("");
    setContentSearch("");
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (isEditing && editingId) {
      // For update, explicitly include all fields so the backend can clear old values
      const updatePayload: UpdateDailyContentRequest = {
        content_type: contentType,
        schedule_mode: scheduleMode,
        display_priority: Number(displayPriority) || 0,
        display_section: displaySection,
        content_id: contentId || undefined,
        custom_title: customTitle || undefined,
        custom_content: customContent || undefined,
        custom_image: customImage || undefined,
        // Schedule mode fields — send values only for the active mode
        fixed_date: scheduleMode === "fixed_date" ? (fixedDate || undefined) : undefined,
        day_of_year: scheduleMode === "day_of_year" && dayOfYear ? Number(dayOfYear) : undefined,
        recurring_month: scheduleMode === "recurring_annual" && recurringMonth ? Number(recurringMonth) : undefined,
        recurring_day: scheduleMode === "recurring_annual" && recurringDay ? Number(recurringDay) : undefined,
        lunar_month: scheduleMode === "lunar_date" && lunarMonth ? Number(lunarMonth) : undefined,
        lunar_day: scheduleMode === "lunar_date" && lunarDay ? Number(lunarDay) : undefined,
        start_date: startDate || undefined,
        end_date: endDate || undefined,
      };

      updateMutation.mutate(
        { id: editingId, data: updatePayload },
        { onSuccess: () => onOpenChange(false) }
      );
    } else {
      const createPayload: CreateDailyContentRequest = {
        content_type: contentType,
        schedule_mode: scheduleMode,
        display_priority: Number(displayPriority) || 0,
        display_section: displaySection,
        ...(contentId && { content_id: contentId }),
        ...(customTitle && { custom_title: customTitle }),
        ...(customContent && { custom_content: customContent }),
        ...(customImage && { custom_image: customImage }),
        ...(fixedDate && { fixed_date: fixedDate }),
        ...(dayOfYear && { day_of_year: Number(dayOfYear) }),
        ...(recurringMonth && { recurring_month: Number(recurringMonth) }),
        ...(recurringDay && { recurring_day: Number(recurringDay) }),
        ...(lunarMonth && { lunar_month: Number(lunarMonth) }),
        ...(lunarDay && { lunar_day: Number(lunarDay) }),
        ...(startDate && { start_date: startDate }),
        ...(endDate && { end_date: endDate }),
      };

      createMutation.mutate(createPayload, {
        onSuccess: () => onOpenChange(false),
      });
    }
  };

  const isPending = createMutation.isPending || updateMutation.isPending;
  const typeConfig = CONTENT_TYPE_CONFIG[contentType] ?? CONTENT_TYPE_CONFIG.quote;
  const TypeIcon = typeConfig.icon;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[85vh] max-w-lg overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{isEditing ? "Cập nhật nội dung ngày" : "Thêm nội dung ngày"}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Loading state for edit mode */}
          {isEditing && populatedFor !== editingId && (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="text-muted-foreground h-6 w-6 animate-spin" />
            </div>
          )}

          {(!isEditing || populatedFor === editingId) && (<>
          {/* Content Type */}
          <div className="space-y-2">
            <Label>Loại nội dung *</Label>
            <Select
              value={contentType}
              onValueChange={(v) => handleTypeChange(v as DailyContentType)}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {Object.entries(CONTENT_TYPE_CONFIG).map(([key, cfg]) => {
                  const Icon = cfg.icon;
                  return (
                    <SelectItem key={key} value={key}>
                      <span className="flex items-center gap-2">
                        <Icon className={`h-4 w-4 ${cfg.color}`} />
                        {cfg.label}
                      </span>
                    </SelectItem>
                  );
                })}
              </SelectContent>
            </Select>
          </div>

          {/* Content Picker (for non-custom types) */}
          {contentType !== "custom" && (
            <div className="space-y-2">
              <Label>Chọn nội dung</Label>

              {/* Selected item display */}
              {contentId && selectedLabel ? (
                <div
                  className="flex items-center gap-2 rounded-lg border p-2.5"
                  style={{ borderColor: "var(--ls-border-warm)" }}
                >
                  <div className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-md bg-primary/10 ${typeConfig.color}`}>
                    <TypeIcon className="h-3.5 w-3.5" />
                  </div>
                  <p className="min-w-0 flex-1 truncate text-sm">{selectedLabel}</p>
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    className="h-6 w-6 shrink-0"
                    onClick={handleClearSelection}
                  >
                    <X className="h-3.5 w-3.5" />
                  </Button>
                </div>
              ) : (
                /* Search input */
                <div className="space-y-1">
                  <div className="relative">
                    <Search className="text-muted-foreground pointer-events-none absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
                    <Input
                      value={contentSearch}
                      onChange={(e) => setContentSearch(e.target.value)}
                      placeholder={typeConfig.placeholder}
                      className="pl-9"
                    />
                    {searchLoading && (
                      <Loader2 className="text-muted-foreground absolute top-1/2 right-3 h-4 w-4 -translate-y-1/2 animate-spin" />
                    )}
                  </div>

                  {/* Search results */}
                  {contentSearch.length >= 1 && (
                    <div className="rounded-lg border bg-white shadow-md dark:bg-zinc-900" style={{ borderColor: "var(--ls-border-warm)" }}>
                      <ScrollArea className="max-h-[200px]">
                        {searchLoading ? (
                          <div className="flex items-center justify-center py-6">
                            <Loader2 className="text-muted-foreground h-5 w-5 animate-spin" />
                          </div>
                        ) : items.length === 0 ? (
                          <div className="text-muted-foreground py-6 text-center text-sm">
                            Không tìm thấy kết quả
                          </div>
                        ) : (
                          <div className="p-1">
                            {items.map((item) => (
                              <button
                                key={item.id}
                                type="button"
                                onClick={() => handleSelectContent(item)}
                                className="flex w-full items-center gap-3 rounded-md bg-white px-3 py-2.5 text-left transition-colors hover:bg-zinc-50 dark:bg-zinc-900 dark:hover:bg-zinc-800"
                              >
                                <div className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-md bg-primary/10 ${typeConfig.color}`}>
                                  <TypeIcon className="h-3.5 w-3.5" />
                                </div>
                                <div className="min-w-0 flex-1">
                                  <p className="truncate text-sm font-medium">{item.title}</p>
                                  {item.subtitle && (
                                    <p className="text-muted-foreground truncate text-xs">{item.subtitle}</p>
                                  )}
                                </div>
                                <Check className="text-muted-foreground/0 h-4 w-4 shrink-0" />
                              </button>
                            ))}
                          </div>
                        )}
                      </ScrollArea>
                    </div>
                  )}
                </div>
              )}
            </div>
          )}

          {/* Custom content fields */}
          {contentType === "custom" && (
            <>
              <div className="space-y-2">
                <Label htmlFor="custom_title">Tiêu đề *</Label>
                <Input
                  id="custom_title"
                  value={customTitle}
                  onChange={(e) => setCustomTitle(e.target.value)}
                  placeholder="Tiêu đề nội dung tùy chỉnh"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="custom_content">Nội dung</Label>
                <Textarea
                  id="custom_content"
                  value={customContent}
                  onChange={(e) => setCustomContent(e.target.value)}
                  placeholder="Nội dung chi tiết..."
                  rows={3}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="custom_image">URL Ảnh</Label>
                <Input
                  id="custom_image"
                  value={customImage}
                  onChange={(e) => setCustomImage(e.target.value)}
                  placeholder="https://..."
                />
              </div>
            </>
          )}

          {/* Schedule Mode */}
          <div className="space-y-2">
            <Label>Chế độ lịch *</Label>
            <Select value={scheduleMode} onValueChange={(v) => setScheduleMode(v as ScheduleMode)}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="fixed_date">Ngày cố định</SelectItem>
                <SelectItem value="recurring_annual">Lặp hằng năm (Dương lịch)</SelectItem>
                <SelectItem value="day_of_year">Ngày thứ N trong năm</SelectItem>
                <SelectItem value="lunar_date">Ngày Âm lịch</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {/* Schedule fields based on mode */}
          {scheduleMode === "fixed_date" && (
            <div className="space-y-2">
              <Label htmlFor="fixed_date">Ngày</Label>
              <Input
                id="fixed_date"
                type="date"
                value={fixedDate}
                onChange={(e) => setFixedDate(e.target.value)}
              />
            </div>
          )}

          {scheduleMode === "recurring_annual" && (
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label htmlFor="recurring_month">Tháng</Label>
                <Input
                  id="recurring_month"
                  type="number"
                  min={1}
                  max={12}
                  value={recurringMonth}
                  onChange={(e) => setRecurringMonth(e.target.value)}
                  placeholder="1-12"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="recurring_day">Ngày</Label>
                <Input
                  id="recurring_day"
                  type="number"
                  min={1}
                  max={31}
                  value={recurringDay}
                  onChange={(e) => setRecurringDay(e.target.value)}
                  placeholder="1-31"
                />
              </div>
            </div>
          )}

          {scheduleMode === "day_of_year" && (
            <div className="space-y-2">
              <Label htmlFor="day_of_year">Ngày thứ</Label>
              <Input
                id="day_of_year"
                type="number"
                min={1}
                max={366}
                value={dayOfYear}
                onChange={(e) => setDayOfYear(e.target.value)}
                placeholder="1-366"
              />
            </div>
          )}

          {scheduleMode === "lunar_date" && (
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label htmlFor="lunar_month">Tháng Âm</Label>
                <Input
                  id="lunar_month"
                  type="number"
                  min={1}
                  max={12}
                  value={lunarMonth}
                  onChange={(e) => setLunarMonth(e.target.value)}
                  placeholder="1-12"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="lunar_day">Ngày Âm</Label>
                <Input
                  id="lunar_day"
                  type="number"
                  min={1}
                  max={30}
                  value={lunarDay}
                  onChange={(e) => setLunarDay(e.target.value)}
                  placeholder="1-30"
                />
              </div>
            </div>
          )}

          {/* Display settings */}
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-2">
              <Label htmlFor="display_priority">Ưu tiên</Label>
              <Input
                id="display_priority"
                type="number"
                min={0}
                value={displayPriority}
                onChange={(e) => setDisplayPriority(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="display_section">Section</Label>
              <Select value={displaySection} onValueChange={setDisplaySection}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="main">Main</SelectItem>
                  <SelectItem value="sidebar">Sidebar</SelectItem>
                  <SelectItem value="banner">Banner</SelectItem>
                  <SelectItem value="footer">Footer</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Date range */}
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-2">
              <Label htmlFor="start_date">Hiệu lực từ</Label>
              <Input
                id="start_date"
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="end_date">Đến ngày</Label>
              <Input
                id="end_date"
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
              />
            </div>
          </div>

          {/* Actions */}
          <div className="flex items-center justify-end gap-3 pt-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isPending}
            >
              Hủy
            </Button>
            <Button type="submit" disabled={isPending}>
              {isPending ? "Đang xử lý..." : isEditing ? "Cập nhật" : "Tạo mới"}
            </Button>
          </div>
          </>)}
        </form>
      </DialogContent>
    </Dialog>
  );
}
