"use client";

import { useState, useEffect } from "react";
import { Input } from "@/components/ui/input";
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from "@/components/ui/select";

const APP_SCREENS = [
  { value: "home",              label: "Trang chủ" },
  { value: "calendar",          label: "Lịch tháng" },
  { value: "gooddays",          label: "Ngày tốt/xấu" },
  { value: "knowledge_feed",    label: "Bài viết khám phá" },
  { value: "quiz_home",         label: "Đố vui" },
  { value: "chat",              label: "AI Tử Vi" },
  { value: "tools",             label: "Tiện ích" },
  { value: "prayers",           label: "Văn khấn" },
  { value: "history",           label: "Ngày này năm xưa" },
  { value: "profile",           label: "Hồ sơ" },
  { value: "bookmarks",         label: "Ngày đã lưu" },
  { value: "tasks",             label: "Ghi chú" },
  { value: "countdown",         label: "Đếm ngày" },
  { value: "familytree",        label: "Cây gia phả" },
  { value: "oracle_draw",       label: "Rút thẻ" },
  { value: "daily_store",       label: "Cửa hàng ngày" },
  { value: "ledger",            label: "Lịch sử điểm" },
  { value: "zodiac_collection", label: "Bộ sưu tập con giáp" },
  { value: "date_picker",       label: "Chọn ngày đẹp" },
  { value: "tiet_khi",          label: "Tiết khí" },
  { value: "date_math",         label: "Tính ngày" },
  { value: "birth_planner",     label: "Kế hoạch sinh" },
  { value: "cycle_tracker",     label: "Theo dõi chu kỳ" },
  { value: "world_clock",       label: "Giờ thế giới" },
  { value: "widget_manager",    label: "Quản lý widget" },
  { value: "leaderboard",       label: "Bảng xếp hạng" },
  { value: "notifications",     label: "Thông báo" },
] as const;

const SCREEN_VALUES = new Set(APP_SCREENS.map((s) => s.value));
const CUSTOM_URL = "__url__";

function isAppScreen(value: string) {
  return SCREEN_VALUES.has(value as never);
}

interface Props {
  value: string;
  onChange: (v: string) => void;
}

export function ClickActionSelect({ value, onChange }: Props) {
  const initialSelect = isAppScreen(value) ? value : value ? CUSTOM_URL : "";
  const [selectValue, setSelectValue] = useState(initialSelect);
  const [urlInput, setUrlInput] = useState(isAppScreen(value) || !value ? "" : value);

  useEffect(() => {
    if (isAppScreen(value)) {
      setSelectValue(value);
      setUrlInput("");
    } else if (value) {
      setSelectValue(CUSTOM_URL);
      setUrlInput(value);
    } else {
      setSelectValue("");
      setUrlInput("");
    }
  }, [value]);

  const handleSelectChange = (v: string) => {
    setSelectValue(v);
    if (v === CUSTOM_URL) {
      onChange(urlInput);
    } else {
      setUrlInput("");
      onChange(v);
    }
  };

  const handleUrlChange = (v: string) => {
    setUrlInput(v);
    onChange(v);
  };

  return (
    <div className="space-y-2">
      <Select value={selectValue} onValueChange={handleSelectChange}>
        <SelectTrigger>
          <SelectValue placeholder="Chọn màn hình..." />
        </SelectTrigger>
        <SelectContent>
          {APP_SCREENS.map((s) => (
            <SelectItem key={s.value} value={s.value}>
              {s.label}
              <span className="ml-1 text-muted-foreground text-xs">({s.value})</span>
            </SelectItem>
          ))}
          <SelectItem value={CUSTOM_URL}>🔗 URL ngoài (quảng cáo...)</SelectItem>
        </SelectContent>
      </Select>

      {selectValue === CUSTOM_URL && (
        <Input
          value={urlInput}
          onChange={(e) => handleUrlChange(e.target.value)}
          placeholder="https://..."
          className="text-sm"
        />
      )}
    </div>
  );
}
