import Link from "next/link";

const GOOGLE_PLAY_URL =
  "https://play.google.com/store/apps/details?id=com.lichso.app";

interface AppStoreBadgesProps {
  className?: string;
  size?: "sm" | "md" | "lg";
  direction?: "row" | "col";
  showLabel?: boolean;
}

const sizeMap = {
  sm: { width: 140, height: 42 },
  md: { width: 168, height: 50 },
  lg: { width: 196, height: 58 },
};

function GooglePlayBadge({ w, h }: { w: number; h: number }) {
  return (
    <svg
      width={w}
      height={h}
      viewBox="0 0 180 54"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden
    >
      <rect
        x="0.5"
        y="0.5"
        width="179"
        height="53"
        rx="8.5"
        fill="#3D2E1A"
        stroke="rgba(196,120,58,0.45)"
      />
      <g transform="translate(12,10)">
        <path
          d="M3 4C3 2.6 4.5 1.8 5.7 2.6L23 15C24 15.7 24 17.3 23 18L5.7 30.4C4.5 31.2 3 30.4 3 29V4Z"
          fill="url(#gpGrad)"
        />
        <defs>
          <linearGradient
            id="gpGrad"
            x1="3"
            y1="2"
            x2="24"
            y2="31"
            gradientUnits="userSpaceOnUse"
          >
            <stop stopColor="#4A8B7F" />
            <stop offset="0.5" stopColor="#D4956A" />
            <stop offset="1" stopColor="#C4783A" />
          </linearGradient>
        </defs>
      </g>
      <text
        x="44"
        y="19.5"
        fill="#B8A090"
        fontFamily="system-ui, sans-serif"
        fontSize="8"
        letterSpacing="0.6"
      >
        TẢI VỀ TỪ
      </text>
      <text
        x="44"
        y="38"
        fill="#F5E8CC"
        fontFamily="Georgia, serif"
        fontSize="15.5"
        fontWeight="600"
        letterSpacing="0.3"
      >
        Google Play
      </text>
    </svg>
  );
}

function AppStoreBadgeSVG({ w, h }: { w: number; h: number }) {
  return (
    <svg
      width={w}
      height={h}
      viewBox="0 0 180 54"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden
    >
      <rect
        x="0.5"
        y="0.5"
        width="179"
        height="53"
        rx="8.5"
        fill="#3D2E1A"
        stroke="rgba(196,120,58,0.45)"
      />
      <g transform="translate(12,8)">
        <path
          d="M16.4 5.8c-.6.7-1.5 1.2-2.4 1.1.1-1 .4-2 1.1-2.7.6-.7 1.6-1.2 2.5-1.2 0 1-.4 2-.9 2.6l-.3.2Z"
          fill="#F5E8CC"
        />
        <path
          d="M16.6 7.2c-1.3-.1-2.5.8-3.1.8s-1.6-.7-2.7-.7c-1.4 0-2.7.8-3.4 2.1-1.5 2.5-.4 6.3 1 8.4.7 1 1.5 2.1 2.6 2.1s1.4-.7 2.7-.7 1.6.7 2.7.7 1.8-1 2.5-2.1c.5-.7.9-1.5 1.2-2.4-.8-.3-1.6-1.1-2-2-.5-1-.4-2.1.1-3 .4-.5.9-1 1.4-1.3-.6-.9-1.6-1.5-2.7-1.6l-.3-.3Z"
          fill="#F5E8CC"
        />
      </g>
      <text
        x="42"
        y="19.5"
        fill="#B8A090"
        fontFamily="system-ui, sans-serif"
        fontSize="8"
        letterSpacing="0.6"
      >
        SẮP RA MẮT TRÊN
      </text>
      <text
        x="42"
        y="38"
        fill="#F5E8CC"
        fontFamily="Georgia, serif"
        fontSize="15.5"
        fontWeight="600"
        letterSpacing="0.3"
      >
        App Store
      </text>
    </svg>
  );
}

export function AppStoreBadges({
  className = "",
  size = "md",
  direction = "row",
  showLabel = false,
}: AppStoreBadgesProps) {
  const { width, height } = sizeMap[size];

  return (
    <div className={className}>
      {showLabel && (
        <p className="text-text-soft mb-3 text-sm font-medium tracking-wide">
          📱 Tải ứng dụng Lịch Số
        </p>
      )}
      <div
        className={`flex gap-3 ${
          direction === "col"
            ? "flex-col items-start"
            : "flex-row flex-wrap items-center"
        }`}
      >
        <Link
          href={GOOGLE_PLAY_URL}
          target="_blank"
          rel="noopener noreferrer"
          className="inline-block rounded-[9px] transition-all duration-300 hover:-translate-y-0.5 hover:shadow-lg active:scale-[0.97]"
          aria-label="Tải ứng dụng Lịch Số trên Google Play"
        >
          <GooglePlayBadge w={width} h={height} />
        </Link>

        <div
          className="relative inline-block cursor-default opacity-70 transition-opacity duration-300 hover:opacity-80"
          title="Sắp ra mắt trên App Store"
        >
          <AppStoreBadgeSVG w={width} h={height} />
          <span
            className="absolute -top-2 -right-2 rounded-full px-1.5 py-0.5 text-[8px] font-bold tracking-wider text-white"
            style={{
              background:
                "linear-gradient(135deg, var(--warm-amber), var(--warm-gold))",
              boxShadow: "0 2px 8px rgba(196,120,58,0.35)",
            }}
          >
            SOON
          </span>
        </div>
      </div>
    </div>
  );
}
