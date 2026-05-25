"use client";

import { fixContentImageUrls } from "@/lib/utils";

interface ArticleContentProps {
  content: string;
  className?: string;
}

/**
 * Renders article HTML content (from TipTap) with beautiful, consistent styling.
 * Used in both the public article page and the admin preview.
 */
export function ArticleContent({ content, className = "" }: ArticleContentProps) {
  return (
    <div
      className={`article-content ${className}`}
      dangerouslySetInnerHTML={{ __html: fixContentImageUrls(content) }}
    />
  );
}
