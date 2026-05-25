import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

/**
 * Combine class names with Tailwind CSS merge support
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/**
 * Format a date to a readable string
 */
export function formatDate(date: string | Date, locale = "vi-VN"): string {
  return new Intl.DateTimeFormat(locale, {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(date));
}

/**
 * Truncate a string to a maximum length
 */
export function truncate(str: string, maxLength: number): string {
  if (str.length <= maxLength) return str;
  return str.slice(0, maxLength) + "...";
}

/**
 * Get initials from a full name (e.g., "John Doe" → "JD")
 */
export function getInitials(name: string): string {
  return name
    .split(" ")
    .map((part) => part.charAt(0))
    .join("")
    .toUpperCase()
    .slice(0, 2);
}

/**
 * Sleep for a specified number of milliseconds
 */
export function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/**
 * Capitalize the first letter of a string
 */
export function capitalize(str: string): string {
  return str.charAt(0).toUpperCase() + str.slice(1);
}

/**
 * Normalize an image/media URL so it always points to the correct backend.
 * The backend now returns host-independent relative paths like "/api/uploads/...".
 * This function prepends the backend origin when needed.
 */
export function getImageUrl(url?: string): string {
  if (!url) return "";

  const apiBase = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";
  const backendOrigin = apiBase.replace(/\/api\/?$/, ""); // e.g., http://localhost:8080

  // Already a full external URL that is NOT our backend — return as-is
  if (/^https?:\/\//.test(url)) {
    // Strip any old hardcoded host from upload URLs and rebuild with current host
    const uploadMatch = url.match(/https?:\/\/[^/]+(\/(?:api\/)?uploads\/.+)/);
    if (uploadMatch) {
      const path = uploadMatch[1].startsWith("/api/uploads/")
        ? uploadMatch[1]
        : "/api" + uploadMatch[1];
      return backendOrigin + path;
    }
    return url;
  }

  // Relative path: /api/uploads/...
  if (url.startsWith("/api/uploads/")) {
    return backendOrigin + url;
  }

  // Relative path without /api prefix: /uploads/...
  if (url.startsWith("/uploads/")) {
    return backendOrigin + "/api" + url;
  }

  return url;
}

/**
 * Fix all image/media URLs inside an HTML string so they point
 * to the correct backend (prepend host to relative paths, fix old absolute URLs).
 */
export function fixContentImageUrls(html: string): string {
  if (!html) return "";
  const apiBase = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";
  const backendOrigin = apiBase.replace(/\/api\/?$/, "");

  // Replace any absolute upload URL (with any host) → full URL with current host
  let fixed = html.replace(
    /https?:\/\/[^/]+(\/(?:api\/)?uploads\/)/g,
    backendOrigin + "/api/uploads/"
  );

  // Fix relative src="/uploads/..." → src="http://currenthost/api/uploads/..."
  fixed = fixed.replaceAll('src="/uploads/', `src="${backendOrigin}/api/uploads/`);

  // Fix relative src="/api/uploads/..." → src="http://currenthost/api/uploads/..."
  fixed = fixed.replaceAll('src="/api/uploads/', `src="${backendOrigin}/api/uploads/`);

  return fixed;
}

/**
 * Strip host from all upload URLs in HTML content, converting them to
 * host-independent relative paths. Use this before saving content to the backend
 * so the database never stores hardcoded hosts.
 *
 * e.g. src="http://localhost:8080/api/uploads/..." → src="/api/uploads/..."
 */
export function stripContentImageUrls(html: string): string {
  if (!html) return "";

  // Strip any http(s)://host(:port) prefix before /api/uploads/ or /uploads/
  return html.replace(/https?:\/\/[^/]+(\/(?:api\/)?uploads\/)/g, "/api/uploads/");
}
