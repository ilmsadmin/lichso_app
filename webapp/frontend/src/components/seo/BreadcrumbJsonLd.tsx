/**
 * Reusable BreadcrumbJsonLd component for Schema.org BreadcrumbList
 * Usage: <BreadcrumbJsonLd items={[{ name: "Trang chủ", url: "/" }, { name: "Bài viết", url: "/bai-viet" }, { name: "Tiêu đề bài", url: "/bai-viet/slug" }]} />
 */

const BASE_URL = process.env.NEXT_PUBLIC_APP_URL || "https://lichso.vn";

interface BreadcrumbItem {
  name: string;
  url: string;
}

interface BreadcrumbJsonLdProps {
  items: BreadcrumbItem[];
}

export function BreadcrumbJsonLd({ items }: BreadcrumbJsonLdProps) {
  const jsonLd = {
    "@context": "https://schema.org",
    "@type": "BreadcrumbList",
    itemListElement: items.map((item, index) => ({
      "@type": "ListItem",
      position: index + 1,
      name: item.name,
      item: item.url.startsWith("http") ? item.url : `${BASE_URL}${item.url}`,
    })),
  };

  return (
    <script
      type="application/ld+json"
      dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
    />
  );
}
