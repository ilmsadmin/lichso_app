import {
  Landmark,
  Palette,
  Compass,
  Sparkles,
  Crown,
  Moon,
  Star,
  Newspaper,
  Hash,
  Eye,
  Layers3,
  type LucideIcon,
} from "lucide-react";

/**
 * Map category slug → Lucide icon component.
 * Keeps all category icons in one place so they stay
 * consistent across public pages, admin, and filters.
 */
export const CATEGORY_ICON_MAP: Record<string, LucideIcon> = {
  "lich-su": Landmark,
  "van-hoa": Palette,
  "phong-thuy": Compass,
  "le-hoi": Sparkles,
  "nhan-vat-lich-su": Crown,
  "am-lich": Moon,
  "tu-vi": Star,
  "tin-tuc": Newspaper,
  "than-so-hoc": Hash,
  "tuong-so": Eye,
};

/** Fallback icon when a category slug is not in the map */
export const DEFAULT_CATEGORY_ICON: LucideIcon = Layers3;

/**
 * Get the Lucide icon component for a category.
 * Falls back to `Layers3` if slug is unknown.
 */
export function getCategoryIcon(slug?: string | null): LucideIcon {
  if (!slug) return DEFAULT_CATEGORY_ICON;
  return CATEGORY_ICON_MAP[slug] ?? DEFAULT_CATEGORY_ICON;
}
