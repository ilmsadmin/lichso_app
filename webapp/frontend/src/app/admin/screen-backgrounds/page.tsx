"use client";

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { Trash2, Image as ImageIcon, LayoutTemplate, Wallpaper } from "lucide-react";
import {
  getScreenBackgrounds,
  setScreenBackgroundUrl,
  deleteScreenBackground,
} from "@/services/adminService";
import type { ScreenBackgroundResponse } from "@/types/settings";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import Image from "next/image";
import { MediaPickerDialog } from "@/components/shared/MediaPickerDialog";
import type { MediaFile } from "@/types/media";

/** Keys ending with "_header" → header images; everything else → backgrounds */
function isHeaderKey(screenKey: string) {
  return screenKey.endsWith("_header");
}

export default function ScreenBackgroundsPage() {
  const queryClient = useQueryClient();
  const [uploadingKey, setUploadingKey] = useState<string | null>(null);

  // Media picker state
  const [isMediaPickerOpen, setIsMediaPickerOpen] = useState(false);
  const [activeScreenKey, setActiveScreenKey] = useState<string | null>(null);

  // Fetch all entries
  const { data, isLoading, error } = useQuery({
    queryKey: ["screenBackgrounds"],
    queryFn: getScreenBackgrounds,
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: deleteScreenBackground,
    onSuccess: () => {
      toast.success("Đã xóa ảnh");
      queryClient.invalidateQueries({ queryKey: ["screenBackgrounds"] });
    },
    onError: (err: any) => {
      toast.error(err.message || "Không thể xóa ảnh");
    },
  });

  const all: ScreenBackgroundResponse[] = data?.data || [];
  const backgrounds = all.filter((bg) => !isHeaderKey(bg.screen_key));
  const headers = all.filter((bg) => isHeaderKey(bg.screen_key));

  const handleMediaSelect = async (file: MediaFile) => {
    if (!activeScreenKey) return;
    try {
      setUploadingKey(activeScreenKey);
      await setScreenBackgroundUrl(activeScreenKey, file.url);
      toast.success("Cập nhật thành công");
      queryClient.invalidateQueries({ queryKey: ["screenBackgrounds"] });
    } catch (err: any) {
      toast.error(err.message || "Cập nhật thất bại");
    } finally {
      setUploadingKey(null);
      setActiveScreenKey(null);
    }
  };

  const openMediaPicker = (screenKey: string) => {
    setActiveScreenKey(screenKey);
    setIsMediaPickerOpen(true);
  };

  const handleDelete = (screenKey: string) => {
    if (confirm("Bạn có chắc chắn muốn xóa ảnh này? App sẽ trở về mặc định.")) {
      deleteMutation.mutate(screenKey);
    }
  };

  if (error) {
    return (
      <div className="p-6">
        <div className="text-red-500">Đã xảy ra lỗi khi tải danh sách</div>
      </div>
    );
  }

  const SkeletonGrid = () => (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      {Array.from({ length: 4 }).map((_, i) => (
        <Card key={i}>
          <CardHeader><Skeleton className="h-6 w-3/4" /></CardHeader>
          <CardContent><Skeleton className="h-48 w-full rounded-md" /></CardContent>
        </Card>
      ))}
    </div>
  );

  const BgCard = ({ bg }: { bg: ScreenBackgroundResponse }) => (
    <Card className="flex flex-col">
      <CardHeader className="pb-3">
        <CardTitle className="text-lg">{bg.screen_name}</CardTitle>
        <p className="text-sm text-muted-foreground font-mono">{bg.screen_key}</p>
      </CardHeader>
      <CardContent className="flex-1 pb-3">
        <div
          className={`relative bg-muted rounded-md overflow-hidden flex items-center justify-center border border-border ${
            isHeaderKey(bg.screen_key) ? "aspect-[16/6]" : "aspect-[9/16]"
          }`}
        >
          {bg.image_url ? (
            <Image
              src={bg.image_url}
              alt={`Ảnh cho ${bg.screen_name}`}
              fill
              className="object-cover"
              unoptimized
            />
          ) : (
            <div className="flex flex-col items-center text-muted-foreground">
              <ImageIcon className="h-10 w-10 mb-2 opacity-50" />
              <span className="text-sm">Mặc định</span>
            </div>
          )}
          {uploadingKey === bg.screen_key && (
            <div className="absolute inset-0 bg-background/80 flex items-center justify-center z-10">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
            </div>
          )}
        </div>
      </CardContent>
      <CardFooter className="flex justify-between gap-2 pt-0">
        <Button
          variant="outline"
          className="flex-1"
          onClick={() => openMediaPicker(bg.screen_key)}
          disabled={uploadingKey === bg.screen_key}
        >
          <ImageIcon className="h-4 w-4 mr-2" />
          {bg.image_url ? "Thay đổi" : "Chọn ảnh"}
        </Button>
        {bg.image_url && (
          <Button
            variant="destructive"
            size="icon"
            onClick={() => handleDelete(bg.screen_key)}
            disabled={deleteMutation.isPending}
          >
            <Trash2 className="h-4 w-4" />
          </Button>
        )}
      </CardFooter>
    </Card>
  );

  return (
    <div className="p-6 space-y-10">
      {/* ── Page header ── */}
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Ảnh Nền &amp; Header</h1>
        <p className="text-muted-foreground mt-2">
          Quản lý ảnh nền toàn màn hình và ảnh header cho từng màn hình trên ứng dụng di động.
        </p>
      </div>

      {/* ══ SECTION 1: Header images ══ */}
      <section>
        <div className="flex items-center gap-2 mb-4">
          <LayoutTemplate className="h-5 w-5 text-primary" />
          <h2 className="text-lg font-semibold">Ảnh Header</h2>
          <span className="text-xs text-muted-foreground ml-1">
            (phần đầu màn hình — tỉ lệ ngang)
          </span>
        </div>
        {isLoading ? (
          <SkeletonGrid />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {headers.map((bg) => (
              <BgCard key={bg.screen_key} bg={bg} />
            ))}
          </div>
        )}
      </section>

      {/* ══ SECTION 2: Background images ══ */}
      <section>
        <div className="flex items-center gap-2 mb-4">
          <Wallpaper className="h-5 w-5 text-primary" />
          <h2 className="text-lg font-semibold">Ảnh Nền Toàn Màn Hình</h2>
          <span className="text-xs text-muted-foreground ml-1">
            (phủ cả màn hình — tỉ lệ dọc)
          </span>
        </div>
        {isLoading ? (
          <SkeletonGrid />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {backgrounds.map((bg) => (
              <BgCard key={bg.screen_key} bg={bg} />
            ))}
          </div>
        )}
      </section>

      <MediaPickerDialog
        open={isMediaPickerOpen}
        onOpenChange={setIsMediaPickerOpen}
        onSelect={handleMediaSelect}
        title={
          activeScreenKey && isHeaderKey(activeScreenKey)
            ? "Chọn ảnh header"
            : "Chọn ảnh nền màn hình"
        }
        imagesOnly={true}
      />
    </div>
  );
}
