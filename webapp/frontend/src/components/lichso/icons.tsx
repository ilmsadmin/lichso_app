/**
 * Custom SVG icon set for Lich So
 *
 * Design principles:
 * - Monoline stroke (1.5px) — clean, modern, airy
 * - 24x24 viewBox — standard sizing
 * - Uses `currentColor` — inherits text color for perfect theme sync
 * - Subtle filled accents (fillOpacity) for depth
 * - Rounded caps/joins — warm, approachable feel matching the app aesthetic
 */

interface IconProps {
  className?: string;
  style?: React.CSSProperties;
}

/** Calendar / Lich Thang — grid with a highlighted day */
export function IconCalendar({ className, style }: IconProps) {
  return (
    <svg
      className={className}
      style={style}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <rect x="3" y="4" width="18" height="18" rx="2.5" />
      <path d="M3 9.5h18" />
      <path d="M8 2v4" />
      <path d="M16 2v4" />
      <rect
        x="10"
        y="13"
        width="4"
        height="4"
        rx="1"
        fill="currentColor"
        fillOpacity="0.15"
        stroke="none"
      />
      <circle cx="12" cy="15" r="1" fill="currentColor" stroke="none" />
    </svg>
  );
}

/** Search / Tra Cuu — magnifying glass with subtle sparkle */
export function IconSearch({ className, style }: IconProps) {
  return (
    <svg
      className={className}
      style={style}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <circle cx="10.5" cy="10.5" r="6.5" />
      <path d="M21 21l-5-5" />
      <path d="M8 8l1.5 1.5" opacity="0.4" />
    </svg>
  );
}

/** Info / Chi Tiet — four-pointed star (matching the existing decorative motif) */
export function IconInfo({ className, style }: IconProps) {
  return (
    <svg
      className={className}
      style={style}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path
        d="M12 2l2.09 6.26L20.18 10l-4.91 3.34L16.36 20 12 16.27 7.64 20l1.09-6.66L3.82 10l6.09-1.74z"
        fill="currentColor"
        fillOpacity="0.08"
      />
      <path d="M12 2l2.09 6.26L20.18 10l-4.91 3.34L16.36 20 12 16.27 7.64 20l1.09-6.66L3.82 10l6.09-1.74z" />
    </svg>
  );
}

/** Good Days / Ngay Tot — leaf with a checkmark */
export function IconGoodDays({ className, style }: IconProps) {
  return (
    <svg
      className={className}
      style={style}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M6 21c0 0 1-8 10-12" />
      <path
        d="M17 4c0 0 2.5 1.5 3 5s-1 7-6 9c-5 2-9 0-9 0s1-4 5-6.5"
        fill="currentColor"
        fillOpacity="0.06"
      />
      <path d="M17 4c0 0 2.5 1.5 3 5s-1 7-6 9c-5 2-9 0-9 0s1-4 5-6.5" />
      <path d="M10.5 14l1.5 1.5 3-3.5" strokeWidth="1.8" />
    </svg>
  );
}

/** Activities / Nen & Ky — clipboard with check and cross */
export function IconActivities({ className, style }: IconProps) {
  return (
    <svg
      className={className}
      style={style}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2" />
      <rect x="9" y="2" width="6" height="4" rx="1.5" fill="currentColor" fillOpacity="0.1" />
      <rect x="9" y="2" width="6" height="4" rx="1.5" />
      <path d="M8.5 12.5l1.5 1.5 3-3" />
      <path d="M8.5 17.5l7 0" opacity="0.35" />
    </svg>
  );
}

/** Solar Terms / Tiet Khi — sun with seasonal arc */
export function IconSolarTerm({ className, style }: IconProps) {
  return (
    <svg
      className={className}
      style={style}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <circle cx="12" cy="12" r="3.5" fill="currentColor" fillOpacity="0.1" />
      <circle cx="12" cy="12" r="3.5" />
      <path d="M12 3v2.5" />
      <path d="M12 18.5V21" />
      <path d="M5.64 5.64l1.76 1.76" />
      <path d="M16.6 16.6l1.76 1.76" />
      <path d="M3 12h2.5" />
      <path d="M18.5 12H21" />
      <path d="M5.64 18.36l1.76-1.76" />
      <path d="M16.6 7.4l1.76-1.76" />
    </svg>
  );
}

/** Convert / Doi Lich — two-way arrows (yin-yang feel) */
export function IconConvert({ className, style }: IconProps) {
  return (
    <svg
      className={className}
      style={style}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M17 4l3 3-3 3" />
      <path d="M3 7h17" />
      <path d="M7 20l-3-3 3-3" />
      <path d="M21 17H4" />
      <circle cx="12" cy="12" r="1.5" fill="currentColor" fillOpacity="0.2" stroke="none" />
    </svg>
  );
}

/** Compass / Huong Tot — compass rose with pointer */
export function IconCompass({ className, style }: IconProps) {
  return (
    <svg
      className={className}
      style={style}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <circle cx="12" cy="12" r="9.5" />
      <polygon
        points="14.5 9.5 9.5 14.5 12 12"
        fill="currentColor"
        fillOpacity="0.25"
        stroke="none"
      />
      <polygon
        points="9.5 14.5 14.5 9.5 12 12"
        fill="currentColor"
        fillOpacity="0.08"
        stroke="none"
      />
      <path d="M16.24 7.76l-2.12 6.36-6.36 2.12 2.12-6.36z" />
      <circle cx="12" cy="12" r="1" fill="currentColor" stroke="none" />
    </svg>
  );
}

/** Bookmark — bookmark flag */
export function IconBookmark({ className, style }: IconProps) {
  return (
    <svg
      className={className}
      style={style}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path
        d="M19 21l-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16z"
        fill="currentColor"
        fillOpacity="0.08"
      />
      <path d="M19 21l-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16z" />
      <path d="M12 8v4" opacity="0.5" />
      <circle cx="12" cy="14" r="0.5" fill="currentColor" opacity="0.5" />
    </svg>
  );
}

/** Reminder / Bell — notification bell */
export function IconReminder({ className, style }: IconProps) {
  return (
    <svg
      className={className}
      style={style}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path
        d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"
        fill="currentColor"
        fillOpacity="0.06"
      />
      <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
      <path d="M13.73 21a2 2 0 0 1-3.46 0" />
      <circle cx="18" cy="4" r="2" fill="currentColor" fillOpacity="0.3" stroke="none" />
    </svg>
  );
}

/** Export / Download — download arrow */
export function IconExport({ className, style }: IconProps) {
  return (
    <svg
      className={className}
      style={style}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
      <polyline points="7 10 12 15 17 10" />
      <line x1="12" y1="15" x2="12" y2="3" />
      <rect
        x="5"
        y="19"
        width="14"
        height="2"
        rx="1"
        fill="currentColor"
        fillOpacity="0.08"
        stroke="none"
      />
    </svg>
  );
}
