import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactCompiler: true,

  // Standalone output for Docker production builds
  output: "standalone",

  // Enable gzip/brotli compression
  compress: true,

  // Experimental features for performance
  experimental: {
    // Optimise CSS — inline critical CSS, defer non-critical
    optimizeCss: true,
    // Only transpile packages that actually need it — reduces JS bundle size
    optimizePackageImports: [
      "lucide-react",
      "@radix-ui/react-dialog",
      "@radix-ui/react-dropdown-menu",
      "@radix-ui/react-select",
      "@radix-ui/react-tabs",
      "@radix-ui/react-tooltip",
      "date-fns",
    ],
    // Track which components affect Core Web Vitals
    webVitalsAttribution: ["CLS", "LCP", "FCP", "FID", "TTFB", "INP"],
  },

  // Image optimization
  images: {
    remotePatterns: [
      {
        protocol: "http",
        hostname: "localhost",
      },
      {
        protocol: "https",
        hostname: "**",
      },
    ],
    formats: ["image/avif", "image/webp"],
    // Match V3 variant widths: thumb_sm(150), thumb_md(300), thumb_lg(600), medium(800), large(1200)
    deviceSizes: [640, 750, 828, 1080, 1200, 1920],
    imageSizes: [150, 300, 600, 800],
    minimumCacheTTL: 3600,
  },

  // Headers for security and performance
  async headers() {
    return [
      {
        source: "/(.*)",
        headers: [
          {
            key: "X-DNS-Prefetch-Control",
            value: "on",
          },
          // Tell crawlers to index all pages (except admin/api handled by robots.ts)
          {
            key: "X-Robots-Tag",
            value: "index, follow",
          },
        ],
      },
      {
        // Block indexing for admin and API
        source: "/(admin|api)/(.*)",
        headers: [
          {
            key: "X-Robots-Tag",
            value: "noindex, nofollow",
          },
        ],
      },
      {
        // Service worker — no cache to always get latest
        source: "/sw.js",
        headers: [
          {
            key: "Cache-Control",
            value: "no-cache, no-store, must-revalidate",
          },
          {
            key: "Service-Worker-Allowed",
            value: "/",
          },
        ],
      },
      {
        // Manifest
        source: "/manifest.json",
        headers: [
          {
            key: "Cache-Control",
            value: "public, max-age=3600",
          },
          {
            key: "Content-Type",
            value: "application/manifest+json",
          },
        ],
      },
      {
        // Cache static assets aggressively
        source: "/static/(.*)",
        headers: [
          {
            key: "Cache-Control",
            value: "public, max-age=31536000, immutable",
          },
        ],
      },
      {
        // Cache Next.js built chunks — immutable (content-hashed filenames)
        source: "/_next/static/(.*)",
        headers: [
          {
            key: "Cache-Control",
            value: "public, max-age=31536000, immutable",
          },
        ],
      },
    ];
  },

  // Turbopack configuration (Next.js 16 default bundler)
  turbopack: {},
};

export default nextConfig;
