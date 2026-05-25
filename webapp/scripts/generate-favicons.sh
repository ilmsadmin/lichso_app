#!/bin/bash
# Generate favicon and icon variants from logo.png
# Usage: ./scripts/generate-favicons.sh
# Requires: sips (macOS built-in) and iconutil or ImageMagick

set -e

LOGO="frontend/public/logo.png"
OUT="frontend/public"

if [ ! -f "$LOGO" ]; then
  echo "❌ Logo not found at $LOGO"
  echo "   Please save your logo image as $LOGO first."
  exit 1
fi

echo "🎨 Generating favicon variants from $LOGO..."

# 1. favicon-96.png (96x96)
sips -z 96 96 "$LOGO" --out "$OUT/favicon-96.png" > /dev/null 2>&1
echo "  ✅ favicon-96.png (96×96)"

# 2. favicon-48.png (48x48)
sips -z 48 48 "$LOGO" --out "$OUT/favicon-48.png" > /dev/null 2>&1
echo "  ✅ favicon-48.png (48×48)"

# 3. favicon-32.png (32x32) — for .ico conversion
sips -z 32 32 "$LOGO" --out "$OUT/favicon-32.png" > /dev/null 2>&1
echo "  ✅ favicon-32.png (32×32)"

# 4. apple-touch-icon.png (180x180)
sips -z 180 180 "$LOGO" --out "$OUT/apple-touch-icon.png" > /dev/null 2>&1
echo "  ✅ apple-touch-icon.png (180×180)"

# 5. favicon.ico — try using ImageMagick if available, else copy 32px png
if command -v convert &> /dev/null; then
  convert "$OUT/favicon-32.png" "$OUT/favicon.ico"
  echo "  ✅ favicon.ico (via ImageMagick)"
else
  # fallback: just copy 32px png as favicon.ico (browsers accept PNG in .ico)
  cp "$OUT/favicon-32.png" "$OUT/favicon.ico"
  echo "  ✅ favicon.ico (copied from 32px PNG — install ImageMagick for proper .ico)"
fi

# Cleanup intermediate 32px if desired
# rm "$OUT/favicon-32.png"

echo ""
echo "🎉 Done! Favicon files generated in $OUT/"
echo "   Files: favicon.ico, favicon-48.png, favicon-96.png, apple-touch-icon.png, logo.png"
