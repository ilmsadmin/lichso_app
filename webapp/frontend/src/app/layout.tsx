import type { Metadata, Viewport } from "next";
import Script from "next/script";
import { Be_Vietnam_Pro, Lora, Playfair_Display } from "next/font/google";
import { Providers } from "@/components/shared/Providers";
import { ServiceWorkerRegistrar } from "@/components/pwa/ServiceWorkerRegistrar";
import { PWAInstallPrompt } from "@/components/pwa/PWAInstallPrompt";
import { OfflineIndicator } from "@/components/pwa/OfflineIndicator";
import "./globals.css";

// Only load the 3 fonts actually used in the UI — removed Geist and Noto_Serif_SC
// (Noto SC is CJK → very heavy; Geist was only for latin mono fallback)
const beVietnamPro = Be_Vietnam_Pro({
  variable: "--font-be-vietnam",
  subsets: ["latin", "vietnamese"],
  weight: ["400", "500", "600"],
  display: "swap",
  preload: true,
});

const lora = Lora({
  variable: "--font-lora",
  subsets: ["latin", "vietnamese"],
  weight: ["400", "600"],
  style: ["normal", "italic"],
  display: "swap",
  preload: true,
});

const playfairDisplay = Playfair_Display({
  variable: "--font-playfair",
  subsets: ["latin", "vietnamese"],
  weight: ["400", "700"],
  style: ["normal"],
  display: "swap",
  preload: true,
});

export const viewport: Viewport = {
  themeColor: "#C4783A",
  width: "device-width",
  initialScale: 1,
  maximumScale: 1,
  viewportFit: "cover",
};

export const metadata: Metadata = {
  metadataBase: new URL(process.env.NEXT_PUBLIC_APP_URL || "https://lichso.vn"),
  title: {
    default: "Lịch Số — Lịch Vạn Niên Việt Nam",
    template: "%s | Lịch Số",
  },
  description:
    "Tra cứu lịch âm dương, ngày tốt xấu, giờ hoàng đạo, phong thủy, tiết khí — Lịch Vạn Niên Việt Nam hiện đại.",
  manifest: "/manifest.json",
  // Uncomment and fill in after verifying Google Search Console:
  // verification: { google: "YOUR_GOOGLE_VERIFICATION_CODE" },
  icons: {
    icon: [
      { url: "/favicon-16x16.png", type: "image/png", sizes: "16x16" },
      { url: "/favicon-32x32.png", type: "image/png", sizes: "32x32" },
      { url: "/favicon-48x48.png", type: "image/png", sizes: "48x48" },
    ],
    apple: [
      { url: "/apple-touch-icon.png", sizes: "180x180" },
    ],
  },
  appleWebApp: {
    capable: true,
    statusBarStyle: "default",
    title: "Lịch Số",
  },
  openGraph: {
    title: "Lịch Số — Lịch Vạn Niên Việt Nam",
    description: "Tra cứu lịch âm dương, ngày tốt xấu, giờ hoàng đạo, phong thủy, tiết khí.",
    images: [{ url: "/og-image.png", width: 1200, height: 630, alt: "Lịch Số — Lịch Vạn Niên" }],
    locale: "vi_VN",
    type: "website",
    siteName: "Lịch Số",
  },
  other: {
    "fb:app_id": process.env.NEXT_PUBLIC_FACEBOOK_APP_ID || "",
  },
  twitter: {
    card: "summary_large_image",
    title: "Lịch Số — Lịch Vạn Niên Việt Nam",
    description: "Tra cứu lịch âm dương, ngày tốt xấu, giờ hoàng đạo, phong thủy, tiết khí.",
    images: ["/og-image.png"],
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi" suppressHydrationWarning>
      <head>
        {/* Preconnect to Google Fonts — eliminates render-blocking latency */}
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        {/* Font Awesome for V2 icons */}
        <link
          rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"
          crossOrigin="anonymous"
        />
        {/* DNS prefetch for analytics */}
        <link rel="dns-prefetch" href="https://www.googletagmanager.com" />
        <link rel="dns-prefetch" href="https://www.google-analytics.com" />
      </head>
      <body
        className={`${beVietnamPro.variable} ${lora.variable} ${playfairDisplay.variable} antialiased`}
      >
        {/* Google tag (gtag.js) - GA4
            lazyOnload = tải sau khi trang idle xong, không chặn LCP/FCP
            Trước dùng afterInteractive → vẫn tải trong critical path */}
        <Script
          src="https://www.googletagmanager.com/gtag/js?id=G-JCMRKQM82D"
          strategy="lazyOnload"
        />
        <Script id="ga4-gtag" strategy="lazyOnload">
          {`
            window.dataLayer = window.dataLayer || [];
            function gtag(){dataLayer.push(arguments);}
            gtag('js', new Date());
            gtag('config', 'G-JCMRKQM82D', { send_page_view: false });
            // Gửi pageview sau khi page load xong
            window.addEventListener('load', function() {
              gtag('event', 'page_view');
            });
          `}
        </Script>

        <Providers>
          {children}
          <ServiceWorkerRegistrar />
          <PWAInstallPrompt />
          <OfflineIndicator />
        </Providers>
      </body>
    </html>
  );
}
