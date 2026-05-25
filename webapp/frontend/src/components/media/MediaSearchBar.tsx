"use client";

import { useState, useCallback } from "react";
import {
  Search,
  X,
  SlidersHorizontal,
  Image,
  Film,
  Music,
  FileText,
  Star,
  Tag,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible";
import { cn } from "@/lib/utils";
import type { MediaListParams } from "@/types/media";

interface MediaSearchBarProps {
  params: MediaListParams;
  onParamsChange: (params: MediaListParams) => void;
  className?: string;
}

const MEDIA_TYPES = [
  { value: "", label: "Tất cả loại", icon: null },
  { value: "image", label: "Hình ảnh", icon: Image },
  { value: "video", label: "Video", icon: Film },
  { value: "audio", label: "Âm thanh", icon: Music },
  { value: "document", label: "Tài liệu", icon: FileText },
];

const SORT_OPTIONS = [
  { value: "created_at:desc", label: "Mới nhất" },
  { value: "created_at:asc", label: "Cũ nhất" },
  { value: "size:desc", label: "Lớn nhất" },
  { value: "size:asc", label: "Nhỏ nhất" },
  { value: "name:asc", label: "Tên A–Z" },
  { value: "name:desc", label: "Tên Z–A" },
];

export function MediaSearchBar({ params, onParamsChange, className }: MediaSearchBarProps) {
  const [searchInput, setSearchInput] = useState(params.search || "");
  const [expanded, setExpanded] = useState(false);
  const [tagInput, setTagInput] = useState(params.tag || "");

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    onParamsChange({ ...params, search: searchInput || undefined, page: 1 });
  };

  const handleClearSearch = () => {
    setSearchInput("");
    onParamsChange({ ...params, search: undefined, page: 1 });
  };

  const handleTypeChange = (value: string) => {
    onParamsChange({
      ...params,
      media_type: value || undefined,
      page: 1,
    });
  };

  const handleSortChange = (value: string) => {
    const [sort_by, sort_order] = value.split(":");
    onParamsChange({
      ...params,
      sort_by,
      sort_order: sort_order as "asc" | "desc",
    });
  };

  const handleFavoriteToggle = () => {
    onParamsChange({
      ...params,
      favorite: params.favorite ? undefined : true,
      page: 1,
    });
  };

  const handleTagSearch = () => {
    onParamsChange({
      ...params,
      tag: tagInput.trim() || undefined,
      page: 1,
    });
  };

  const handleClearAll = () => {
    setSearchInput("");
    setTagInput("");
    onParamsChange({
      page: 1,
      limit: params.limit,
    });
  };

  const activeFilters: string[] = [
    params.media_type
      ? `Loại: ${MEDIA_TYPES.find((t) => t.value === params.media_type)?.label}`
      : "",
    params.favorite ? "Yêu thích" : "",
    params.tag ? `Tag: ${params.tag}` : "",
    params.search ? `Tìm: ${params.search}` : "",
  ].filter(Boolean);

  const currentSort = `${params.sort_by || "created_at"}:${params.sort_order || "desc"}`;

  return (
    <div className={cn("space-y-2", className)}>
      <div className="flex items-center gap-2">
        {/* Main Search */}
        <form onSubmit={handleSearch} className="relative max-w-md flex-1">
          <Search className="text-muted-foreground absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
          <Input
            type="search"
            placeholder="Tìm kiếm file..."
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            className="h-9 pr-8 pl-9"
          />
          {searchInput && (
            <button
              type="button"
              className="text-muted-foreground hover:text-foreground absolute top-1/2 right-3 -translate-y-1/2"
              onClick={handleClearSearch}
            >
              <X className="h-3.5 w-3.5" />
            </button>
          )}
        </form>

        {/* Media Type quick filter */}
        <Select value={params.media_type || ""} onValueChange={handleTypeChange}>
          <SelectTrigger className="h-9 w-[140px]">
            <SelectValue placeholder="Tất cả loại" />
          </SelectTrigger>
          <SelectContent>
            {MEDIA_TYPES.map(({ value, label, icon: Icon }) => (
              <SelectItem key={value || "__all"} value={value || "all"}>
                <span className="flex items-center gap-2">
                  {Icon && <Icon className="h-3.5 w-3.5" />}
                  {label}
                </span>
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        {/* Sort */}
        <Select value={currentSort} onValueChange={handleSortChange}>
          <SelectTrigger className="h-9 w-[130px]">
            <SelectValue placeholder="Sắp xếp" />
          </SelectTrigger>
          <SelectContent>
            {SORT_OPTIONS.map(({ value, label }) => (
              <SelectItem key={value} value={value}>
                {label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        {/* Favorite toggle */}
        <Button
          variant={params.favorite ? "default" : "outline"}
          size="icon"
          className="h-9 w-9 shrink-0"
          onClick={handleFavoriteToggle}
          title="Yêu thích"
        >
          <Star className={cn("h-4 w-4", params.favorite && "fill-current")} />
        </Button>

        {/* Advanced filters toggle */}
        <Collapsible open={expanded} onOpenChange={setExpanded}>
          <CollapsibleTrigger asChild>
            <Button
              variant={expanded ? "secondary" : "outline"}
              size="icon"
              className="h-9 w-9 shrink-0"
              title="Bộ lọc nâng cao"
            >
              <SlidersHorizontal className="h-4 w-4" />
            </Button>
          </CollapsibleTrigger>
        </Collapsible>
      </div>

      {/* Advanced Filters Panel */}
      <Collapsible open={expanded} onOpenChange={setExpanded}>
        <CollapsibleContent>
          <div className="bg-muted/20 flex items-center gap-3 rounded-lg border p-3">
            {/* Tag filter */}
            <div className="flex items-center gap-2">
              <Tag className="text-muted-foreground h-4 w-4" />
              <Input
                value={tagInput}
                onChange={(e) => setTagInput(e.target.value)}
                placeholder="Lọc theo tag..."
                className="h-8 w-40"
                onKeyDown={(e) => e.key === "Enter" && handleTagSearch()}
              />
              <Button variant="outline" size="sm" className="h-8" onClick={handleTagSearch}>
                Lọc
              </Button>
            </div>
          </div>
        </CollapsibleContent>
      </Collapsible>

      {/* Active filters display */}
      {activeFilters.length > 0 && (
        <div className="flex flex-wrap items-center gap-2">
          <span className="text-muted-foreground text-xs">Đang lọc:</span>
          {activeFilters.map((filter) => (
            <Badge key={filter} variant="secondary" className="text-xs">
              {filter}
            </Badge>
          ))}
          <Button variant="ghost" size="sm" className="h-6 text-xs" onClick={handleClearAll}>
            <X className="mr-1 h-3 w-3" />
            Xóa bộ lọc
          </Button>
        </div>
      )}
    </div>
  );
}
