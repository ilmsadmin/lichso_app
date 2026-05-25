"use client";

import { useEffect, useState } from "react";

export function BackgroundLayer() {
  // Defer decorative SVG until after main content paints — reduces TBT & LCP
  const [showDecorations, setShowDecorations] = useState(false);
  useEffect(() => {
    if (typeof requestIdleCallback !== "undefined") {
      const id = requestIdleCallback(() => setShowDecorations(true), { timeout: 2000 });
      return () => cancelIdleCallback(id);
    } else {
      const id = setTimeout(() => setShowDecorations(true), 200);
      return () => clearTimeout(id);
    }
  }, []);

  return (
    <>
      {/* Main scene background — painted immediately (needed for FCP) */}
      <div
        className="pointer-events-none fixed inset-0 z-0"
        style={{ background: "var(--ls-scene-bg)" }}
      />

      {/* Subtle gold/jade accent glows — lightweight */}
      <div
        className="pointer-events-none fixed inset-0 z-0"
        style={{
          backgroundImage: `
            radial-gradient(ellipse 50% 40% at 15% 80%, var(--ls-accent-glow-1) 0%, transparent 70%),
            radial-gradient(ellipse 40% 50% at 85% 15%, var(--ls-accent-glow-2) 0%, transparent 70%)
          `,
        }}
      />

      {/* Decorative SVG + grain — deferred until idle so it does not block LCP */}
      {showDecorations && (
        <>
          {/* Paper grain texture */}
          <div
            className="pointer-events-none fixed inset-0 z-0"
            style={
              {
                opacity: "var(--ls-grain-opacity)" as unknown as number,
                backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='200' height='200' filter='url(%23n)' opacity='0.025'/%3E%3C/svg%3E")`,
              } as React.CSSProperties
            }
          />

          {/* SVG decorations */}
          <svg
            className="pointer-events-none fixed inset-0 z-0 overflow-hidden"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 1440 820"
            preserveAspectRatio="xMidYMid slice"
            aria-hidden="true"
          >
            {/* Lotus left */}
            <g opacity="0.5" stroke="var(--ls-svg-lotus-stroke)" strokeWidth="1.2" fill="none">
              <ellipse cx="180" cy="640" rx="38" ry="55" transform="rotate(-10 180 640)" />
              <ellipse cx="148" cy="648" rx="32" ry="48" transform="rotate(-35 148 648)" />
              <ellipse cx="212" cy="648" rx="32" ry="48" transform="rotate(35 212 648)" />
              <ellipse cx="120" cy="660" rx="25" ry="38" transform="rotate(-60 120 660)" />
              <ellipse cx="240" cy="660" rx="25" ry="38" transform="rotate(60 240 660)" />
              <line x1="180" y1="695" x2="180" y2="800" />
              <ellipse cx="156" cy="714" rx="20" ry="32" transform="rotate(-25 156 714)" />
              <ellipse cx="204" cy="714" rx="20" ry="32" transform="rotate(25 204 714)" />
              <ellipse cx="90" cy="760" rx="20" ry="30" transform="rotate(-5 90 760)" />
              <ellipse cx="74" cy="766" rx="15" ry="22" transform="rotate(-30 74 766)" />
              <ellipse cx="106" cy="766" rx="15" ry="22" transform="rotate(30 106 766)" />
              <line x1="90" y1="790" x2="90" y2="820" />
            </g>

            {/* Frame lines left */}
            <g opacity="0.18" stroke="var(--ls-svg-frame-stroke)" strokeWidth="1" fill="none">
              <rect x="55" y="120" width="220" height="400" rx="2" />
              <rect x="65" y="130" width="200" height="380" rx="2" />
            </g>

            {/* Large lantern circles right */}
            <circle cx="1340" cy="120" r="260" fill="none" stroke="var(--ls-svg-lantern-stroke)" strokeWidth="1.5" />
            <circle cx="1340" cy="120" r="220" fill="none" stroke="var(--ls-svg-lantern-stroke-inner)" strokeWidth="1" />

            {/* Grid lines (lantern lattice) */}
            <g opacity="0.12" stroke="var(--ls-svg-grid-stroke)" strokeWidth="0.8" fill="none">
              {[1100, 1150, 1200, 1250, 1300, 1350, 1400].map((x) => (
                <line key={`v-${x}`} x1={x} y1="0" x2={x} y2="350" />
              ))}
              {[0, 50, 100, 150, 200, 250, 300].map((y) => (
                <line key={`h-${y}`} x1="1080" y1={y} x2="1440" y2={y} />
              ))}
            </g>

            {/* Clouds right */}
            <g opacity="0.35" fill="var(--ls-svg-cloud-fill)" stroke="none">
              <ellipse cx="1260" cy="400" rx="90" ry="38" />
              <ellipse cx="1320" cy="390" rx="70" ry="32" />
              <ellipse cx="1200" cy="412" rx="60" ry="28" />
              <ellipse cx="1350" cy="420" rx="50" ry="24" />
              <ellipse cx="1160" cy="460" rx="80" ry="34" />
              <ellipse cx="1230" cy="450" rx="65" ry="28" />
              <ellipse cx="1300" cy="460" rx="55" ry="26" />
              <ellipse cx="1370" cy="480" rx="60" ry="28" />
            </g>

            {/* Lotus top right */}
            <g opacity="0.35" stroke="var(--ls-svg-lotus-stroke)" strokeWidth="1.1" fill="none">
              <ellipse cx="1350" cy="280" rx="22" ry="32" transform="rotate(10 1350 280)" />
              <ellipse cx="1330" cy="288" rx="18" ry="26" transform="rotate(-18 1330 288)" />
              <ellipse cx="1370" cy="288" rx="18" ry="26" transform="rotate(18 1370 288)" />
              <line x1="1350" y1="312" x2="1350" y2="360" />
            </g>
          </svg>
        </>
      )}
    </>
  );
}
