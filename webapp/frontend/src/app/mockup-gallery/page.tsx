"use client";

import { useEffect, useState } from "react";

interface Asset {
  index: number;
  original: string;
  clean: string;
  width: number;
  height: number;
  aspect_ratio: number;
  avg_color: number[];
  ocr_text: string;
}

export default function MockupGallery() {
  const [assets, setAssets] = useState<Asset[]>([]);

  useEffect(() => {
    fetch("/assets-new/mapping.json")
      .then((res) => res.json())
      .then((data) => setAssets(data))
      .catch((err) => console.error("Error loading mapping:", err));
  }, []);

  return (
    <div className="min-h-screen bg-[#F9F5EE] p-8 text-[#1A0F0A]">
      <div className="mx-auto max-w-[1400px]">
        <header className="mb-8 border-b border-[#E8DFD2] pb-6 text-center">
          <h1 className="font-playfair text-3xl font-bold text-[#9B2335]">
            Lịch Số V2 — Thư Viện Assets Đã Tách Nền
          </h1>
          <p className="mt-2 text-sm text-[#5C4A3E]">
            Danh sách tất cả các ảnh PNG background-removed dùng làm icon và hình minh họa cho thiết kế mới.
          </p>
        </header>

        <div className="grid gap-6 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
          {assets.map((asset) => (
            <div
              key={asset.clean}
              className="flex flex-col rounded-xl border border-[#E8DFD2] bg-white p-4 shadow-sm transition-all hover:shadow-md"
            >
              <div className="flex h-48 items-center justify-center rounded-lg bg-[#F5F0E8] p-4">
                <img
                  src={`/assets-new/${asset.clean}`}
                  alt={asset.clean}
                  className="max-h-full max-w-full object-contain"
                />
              </div>
              <div className="mt-4 flex-1">
                <h3 className="text-sm font-semibold word-break break-all">
                  {asset.clean}
                </h3>
                <div className="mt-2 space-y-1 text-xs text-[#5C4A3E]">
                  <p>
                    <span className="font-medium text-[#9B2335]">File gốc:</span> {asset.original}
                  </p>
                  <p>
                    <span className="font-medium">Kích thước:</span> {asset.width}x{asset.height} (Ratio: {asset.aspect_ratio})
                  </p>
                  <p>
                    <span className="font-medium">Màu trung bình:</span> rgb({asset.avg_color.join(", ")})
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
