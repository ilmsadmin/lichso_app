"use client";

import { usePathname } from "next/navigation";
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import { Fragment } from "react";

// Map route segments to display labels
const segmentLabels: Record<string, string> = {
  admin: "Admin",
  users: "Users",
  roles: "Roles",
  permissions: "Permissions",
  logs: "Activity Logs",
  settings: "Settings",
  profile: "Profile",
  create: "Tạo mới",
  edit: "Chỉnh sửa",
  articles: "Bài viết",
  categories: "Danh mục",
  tags: "Tags",
  quotes: "Danh ngôn",
  "famous-people": "Danh nhân",
  events: "Sự kiện",
  festivals: "Lễ hội",
  "app-reviews": "Đánh giá app",
  files: "Files",
  // Public content pages (Vietnamese slugs)
  "bai-viet": "Bài Viết",
  "cau-noi-noi-tieng": "Câu Nói Nổi Tiếng",
  "nguoi-noi-tieng": "Người Nổi Tiếng",
  "su-kien": "Sự Kiện",
  "le-hoi": "Lễ Hội",
  "ngay-nay-trong-lich-su": "Ngày Này Trong Lịch Sử",
};

export function BreadcrumbNav() {
  const pathname = usePathname();

  // Split path into segments and filter empty strings
  const segments = pathname.split("/").filter(Boolean);

  // Don't render breadcrumb if we're at root admin page
  if (segments.length <= 1) {
    return null;
  }

  // Build breadcrumb items
  const items = segments.map((segment, index) => {
    const href = "/" + segments.slice(0, index + 1).join("/");
    const isLast = index === segments.length - 1;

    // Check if segment looks like a UUID (skip labeling it)
    const isUUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(segment);

    const label = isUUID
      ? "Detail"
      : segmentLabels[segment] || segment.charAt(0).toUpperCase() + segment.slice(1);

    return { href, label, isLast };
  });

  return (
    <Breadcrumb>
      <BreadcrumbList>
        {items.map((item, index) => (
          <Fragment key={item.href}>
            {index > 0 && <BreadcrumbSeparator />}
            <BreadcrumbItem>
              {item.isLast ? (
                <BreadcrumbPage>{item.label}</BreadcrumbPage>
              ) : (
                <BreadcrumbLink href={item.href}>{item.label}</BreadcrumbLink>
              )}
            </BreadcrumbItem>
          </Fragment>
        ))}
      </BreadcrumbList>
    </Breadcrumb>
  );
}
