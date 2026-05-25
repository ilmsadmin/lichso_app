"use client";

import React, { useState, useRef, useEffect, useCallback } from "react";
import Image from "next/image";
import { cn, getImageUrl } from "@/lib/utils";
import type { MediaVariant, MediaFocalPoint } from "@/types/media";

// ============================================
// Variant width map matching backend DefaultVariants
// ============================================
const VARIANT_WIDTHS: Record<string, number> = {
  thumb_sm: 150,
  thumb_md: 300,
  thumb_lg: 600,
  medium: 800,
  large: 1200,
  og: 1200,
};

// ============================================
// BlurhashCanvas — tiny canvas that shows the blurhash
// ============================================
interface BlurhashPlaceholderProps {
  hash: string;
  width?: number;
  height?: number;
  className?: string;
}

/**
 * Decode a blurhash string into an array of RGBA pixels.
 * Lightweight decoder (no external lib) — adapted from the reference implementation.
 */
const BASE83_CHARS =
  "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz#$%*+,-.:;=?@[]^_{|}~";

function decode83(str: string): number {
  let value = 0;
  for (let i = 0; i < str.length; i++) {
    const c = str[i];
    const digit = BASE83_CHARS.indexOf(c);
    value = value * 83 + digit;
  }
  return value;
}

function sRGBToLinear(value: number): number {
  const v = value / 255;
  return v <= 0.04045 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
}

function linearToSRGB(value: number): number {
  const v = Math.max(0, Math.min(1, value));
  return v <= 0.0031308
    ? Math.round(v * 12.92 * 255 + 0.5)
    : Math.round((1.055 * Math.pow(v, 1 / 2.4) - 0.055) * 255 + 0.5);
}

function signPow(base: number, exp: number): number {
  return Math.sign(base) * Math.pow(Math.abs(base), exp);
}

function decodeDC(value: number): [number, number, number] {
  return [
    sRGBToLinear((value >> 16) & 255),
    sRGBToLinear((value >> 8) & 255),
    sRGBToLinear(value & 255),
  ];
}

function decodeAC(value: number, maximumValue: number): [number, number, number] {
  const quantR = Math.floor(value / (19 * 19));
  const quantG = Math.floor(value / 19) % 19;
  const quantB = value % 19;
  return [
    signPow((quantR - 9) / 9, 2.0) * maximumValue,
    signPow((quantG - 9) / 9, 2.0) * maximumValue,
    signPow((quantB - 9) / 9, 2.0) * maximumValue,
  ];
}

function decodeBlurhash(hash: string, width: number, height: number): Uint8ClampedArray | null {
  if (!hash || hash.length < 6) return null;

  try {
    const sizeFlag = decode83(hash[0]);
    const numY = Math.floor(sizeFlag / 9) + 1;
    const numX = (sizeFlag % 9) + 1;

    const quantisedMaximumValue = decode83(hash[1]);
    const maximumValue = (quantisedMaximumValue + 1) / 166;

    const colors: [number, number, number][] = new Array(numX * numY);
    colors[0] = decodeDC(decode83(hash.substring(2, 6)));

    for (let i = 1; i < numX * numY; i++) {
      colors[i] = decodeAC(decode83(hash.substring(4 + i * 2, 6 + i * 2)), maximumValue);
    }

    const pixels = new Uint8ClampedArray(width * height * 4);

    for (let y = 0; y < height; y++) {
      for (let x = 0; x < width; x++) {
        let r = 0,
          g = 0,
          b = 0;

        for (let j = 0; j < numY; j++) {
          for (let i = 0; i < numX; i++) {
            const basis =
              Math.cos((Math.PI * x * i) / width) * Math.cos((Math.PI * y * j) / height);
            const color = colors[i + j * numX];
            r += color[0] * basis;
            g += color[1] * basis;
            b += color[2] * basis;
          }
        }

        const idx = 4 * (y * width + x);
        pixels[idx] = linearToSRGB(r);
        pixels[idx + 1] = linearToSRGB(g);
        pixels[idx + 2] = linearToSRGB(b);
        pixels[idx + 3] = 255;
      }
    }

    return pixels;
  } catch {
    return null;
  }
}

/**
 * Render a blurhash as a tiny canvas element.
 */
function BlurhashCanvas({ hash, width = 32, height = 32, className }: BlurhashPlaceholderProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas || !hash) return;

    const pixels = decodeBlurhash(hash, width, height);
    if (!pixels) return;

    canvas.width = width;
    canvas.height = height;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    const imageData = ctx.createImageData(width, height);
    imageData.data.set(pixels);
    ctx.putImageData(imageData, 0, 0);
  }, [hash, width, height]);

  if (!hash) return null;

  return (
    <canvas
      ref={canvasRef}
      width={width}
      height={height}
      className={cn("absolute inset-0 h-full w-full", className)}
      style={{ filter: "blur(12px)", transform: "scale(1.2)" }}
    />
  );
}

// ============================================
// ResponsiveImage — Main exported component
// ============================================

interface ResponsiveImageProps {
  /** Original image URL (the base path from media file) */
  src: string;
  /** Alt text */
  alt: string;
  /** V3 media variants (optional — if provided, generates srcset) */
  variants?: MediaVariant[];
  /** BlurHash string for placeholder */
  blurHash?: string;
  /** Dominant color fallback (hex) */
  dominantColor?: string;
  /** Focal point for object-position */
  focalPoint?: MediaFocalPoint;
  /** Aspect ratio: "16/9", "4/3", "1/1", "auto" */
  aspectRatio?: string;
  /** Responsive sizes attribute for srcset */
  sizes?: string;
  /** Fill mode (true) or explicit width/height (false) */
  fill?: boolean;
  /** Width if not fill mode */
  width?: number;
  /** Height if not fill mode */
  height?: number;
  /** Priority loading (above-the-fold) */
  priority?: boolean;
  /** CSS class for the wrapper */
  className?: string;
  /** CSS class for the image element */
  imageClassName?: string;
  /** Show caption/credit below the image */
  caption?: string;
  /** Credit text */
  credit?: string;
  /** onClick handler */
  onClick?: () => void;
}

/**
 * ResponsiveImage — Smart image component for Phase 17 Responsive Image Delivery.
 *
 * Features:
 * - Generates <picture> with srcSet from V3 media variants
 * - BlurhashCanvas placeholder while loading
 * - Dominant color background fallback
 * - Focal point → object-position
 * - Lazy loading by default, priority for above-the-fold
 * - Graceful fallback to standard <Image> if no variants
 */
export function ResponsiveImage({
  src,
  alt,
  variants,
  blurHash,
  dominantColor,
  focalPoint,
  aspectRatio = "auto",
  sizes = "(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 33vw",
  fill = true,
  width,
  height,
  priority = false,
  className,
  imageClassName,
  caption,
  credit,
  onClick,
}: ResponsiveImageProps) {
  const [isLoaded, setIsLoaded] = useState(false);
  const [hasError, setHasError] = useState(false);

  // Compute the resolved image URL
  const resolvedSrc = getImageUrl(src);

  // Build srcset from V3 variants
  const srcSet = buildSrcSet(variants);

  // Compute WebP srcset (filter variants by mime_type)
  const webpSrcSet = buildSrcSet(variants?.filter((v) => v.mime_type === "image/webp"));

  // Focal point → object-position
  const objectPosition = focalPoint ? `${focalPoint.x}% ${focalPoint.y}%` : "center";

  // Aspect ratio style
  const aspectStyle = aspectRatio !== "auto" ? { aspectRatio } : undefined;

  const handleLoad = useCallback(() => {
    setIsLoaded(true);
  }, []);

  const handleError = useCallback(() => {
    setHasError(true);
    setIsLoaded(true);
  }, []);

  if (hasError) {
    return (
      <div
        className={cn(
          "bg-warm-cream/50 relative flex items-center justify-center overflow-hidden rounded-lg",
          className
        )}
        style={aspectStyle}
      >
        <span className="text-3xl opacity-30">🖼️</span>
      </div>
    );
  }

  return (
    <figure
      className={cn("relative", fill && "h-full w-full", className)}
      style={aspectStyle}
      onClick={onClick}
    >
      {/* BlurhashCanvas placeholder — visible until image loads */}
      {blurHash && !isLoaded && (
        <BlurhashCanvas
          hash={blurHash}
          width={32}
          height={32}
          className="z-[1] rounded-[inherit] object-cover"
        />
      )}

      {/* Dominant color background fallback */}
      {dominantColor && !blurHash && !isLoaded && (
        <div
          className="absolute inset-0 z-[1] rounded-[inherit]"
          style={{ backgroundColor: dominantColor }}
        />
      )}

      {/* The actual image — use <picture> with WebP source if we have variants */}
      {webpSrcSet || srcSet ? (
        <picture>
          {/* WebP sources */}
          {webpSrcSet && <source srcSet={webpSrcSet} sizes={sizes} type="image/webp" />}
          {/* Original format sources */}
          {srcSet && <source srcSet={srcSet} sizes={sizes} />}

          {/* Fallback <img> — Next.js Image can't be inside <picture>, so use raw <img> */}
          {fill ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img
              src={resolvedSrc}
              alt={alt}
              loading={priority ? "eager" : "lazy"}
              decoding="async"
              onLoad={handleLoad}
              onError={handleError}
              className={cn(
                "absolute inset-0 h-full w-full object-cover transition-opacity duration-500",
                isLoaded ? "opacity-100" : "opacity-0",
                imageClassName
              )}
              style={{ objectPosition }}
            />
          ) : (
            // eslint-disable-next-line @next/next/no-img-element
            <img
              src={resolvedSrc}
              alt={alt}
              width={width}
              height={height}
              loading={priority ? "eager" : "lazy"}
              decoding="async"
              onLoad={handleLoad}
              onError={handleError}
              className={cn(
                "transition-opacity duration-500",
                isLoaded ? "opacity-100" : "opacity-0",
                imageClassName
              )}
              style={{ objectPosition }}
            />
          )}
        </picture>
      ) : /* No variants — fall back to Next.js Image with optimization */
      fill ? (
        <Image
          src={resolvedSrc}
          alt={alt}
          fill
          sizes={sizes}
          priority={priority}
          unoptimized
          onLoad={handleLoad}
          onError={handleError}
          className={cn(
            "object-cover transition-opacity duration-500",
            isLoaded ? "opacity-100" : "opacity-0",
            imageClassName
          )}
          style={{ objectPosition }}
        />
      ) : (
        <Image
          src={resolvedSrc}
          alt={alt}
          width={width || 800}
          height={height || 600}
          sizes={sizes}
          priority={priority}
          unoptimized
          onLoad={handleLoad}
          onError={handleError}
          className={cn(
            "transition-opacity duration-500",
            isLoaded ? "opacity-100" : "opacity-0",
            imageClassName
          )}
          style={{ objectPosition }}
        />
      )}

      {/* Caption / Credit */}
      {(caption || credit) && (
        <figcaption className="text-text-muted-ls mt-2 text-center text-xs">
          {caption && <span>{caption}</span>}
          {caption && credit && <span> — </span>}
          {credit && <span className="italic">{credit}</span>}
        </figcaption>
      )}
    </figure>
  );
}

// ============================================
// Helper: build srcSet string from variants
// ============================================
function buildSrcSet(variants?: MediaVariant[]): string | undefined {
  if (!variants || variants.length === 0) return undefined;

  // Sort by width ascending and build srcset
  const sorted = [...variants].filter((v) => v.width > 0).sort((a, b) => a.width - b.width);

  if (sorted.length === 0) return undefined;

  return sorted.map((v) => `${getImageUrl(v.path)} ${v.width}w`).join(", ");
}

// ============================================
// Helper: get the best variant URL for a target width
// ============================================

/**
 * Given a list of variants and a target width, return the URL of the
 * smallest variant that is ≥ targetWidth. Falls back to the largest available.
 */
export function getVariantUrl(
  variants: MediaVariant[] | undefined,
  targetWidth: number,
  originalUrl: string
): string {
  if (!variants || variants.length === 0) return getImageUrl(originalUrl);

  const sorted = [...variants].filter((v) => v.width > 0).sort((a, b) => a.width - b.width);

  // Find the first variant that's >= targetWidth
  const match = sorted.find((v) => v.width >= targetWidth);
  if (match) return getImageUrl(match.path);

  // Fall back to the largest variant
  const largest = sorted[sorted.length - 1];
  if (largest) return getImageUrl(largest.path);

  return getImageUrl(originalUrl);
}

/**
 * Get a specific variant by name (e.g., "thumb_md", "large").
 */
export function getVariantByName(
  variants: MediaVariant[] | undefined,
  name: string,
  originalUrl: string
): string {
  if (!variants || variants.length === 0) return getImageUrl(originalUrl);
  const match = variants.find((v) => v.variant_name === name);
  return match ? getImageUrl(match.path) : getImageUrl(originalUrl);
}

// ============================================
// ResponsiveImageSkeleton — Loading skeleton
// ============================================
export function ResponsiveImageSkeleton({
  aspectRatio = "16/9",
  className,
}: {
  aspectRatio?: string;
  className?: string;
}) {
  return (
    <div
      className={cn("bg-warm-cream/50 animate-pulse rounded-lg", className)}
      style={{ aspectRatio }}
    />
  );
}
