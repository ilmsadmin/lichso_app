import { ImageResponse } from "next/og";

export const runtime = "edge";
export const alt = "Lịch Số — Lịch Vạn Niên Việt Nam";
export const size = { width: 1200, height: 630 };
export const contentType = "image/png";

export default function OgImage() {
  return new ImageResponse(
    (
      <div
        style={{
          width: "100%",
          height: "100%",
          background: "linear-gradient(135deg, #1a0f00 0%, #2d1a00 50%, #1a0f00 100%)",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          fontFamily: "serif",
          position: "relative",
        }}
      >
        {/* Decorative border */}
        <div
          style={{
            position: "absolute",
            inset: "24px",
            border: "1.5px solid rgba(196,120,58,0.4)",
            borderRadius: "16px",
          }}
        />

        {/* Top label */}
        <div
          style={{
            fontSize: 18,
            color: "rgba(196,120,58,0.7)",
            letterSpacing: "0.3em",
            textTransform: "uppercase",
            marginBottom: "20px",
          }}
        >
          ✦ lichso.vn ✦
        </div>

        {/* Main title */}
        <div
          style={{
            fontSize: 72,
            fontWeight: "bold",
            color: "#F5DEB3",
            marginBottom: "16px",
            textShadow: "0 4px 24px rgba(196,120,58,0.5)",
          }}
        >
          Lịch Số
        </div>

        {/* Subtitle */}
        <div
          style={{
            fontSize: 28,
            color: "rgba(196,120,58,0.9)",
            marginBottom: "32px",
          }}
        >
          Lịch Vạn Niên Việt Nam Hiện Đại
        </div>

        {/* Divider */}
        <div
          style={{
            width: "200px",
            height: "1px",
            background: "rgba(196,120,58,0.4)",
            marginBottom: "28px",
          }}
        />

        {/* Features */}
        <div
          style={{
            display: "flex",
            gap: "32px",
            fontSize: 20,
            color: "rgba(245,222,179,0.7)",
          }}
        >
          <span>☯ Âm Lịch</span>
          <span>✦ Ngày Tốt</span>
          <span>🧭 Phong Thuỷ</span>
          <span>⭐ Tử Vi</span>
        </div>
      </div>
    ),
    { ...size }
  );
}
