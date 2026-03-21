#!/usr/bin/env python3
"""Generate iOS App Icon for Lịch Số AI — matching the provided design."""

from PIL import Image, ImageDraw, ImageFont, ImageFilter
import math
import os

SIZE = 1024
ICON_DIR = "ios/LichSo/Assets.xcassets/AppIcon.appiconset"

def draw_icon():
    """Draw the calendar icon matching the reference image."""
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # ── Background: very light blue-gray with subtle gradient feel ──
    # Outer background (rounded square will be masked by iOS)
    for y in range(SIZE):
        t = y / SIZE
        r = int(228 + t * 10)
        g = int(235 + t * 5)
        b = int(245 - t * 5)
        draw.line([(0, y), (SIZE, y)], fill=(r, g, b, 255))

    # Subtle decorative blob bottom-left
    for i in range(80, 0, -1):
        alpha = int(15 * (1 - i / 80))
        cx, cy = int(SIZE * 0.12), int(SIZE * 0.78)
        r = int(SIZE * 0.15 * i / 80)
        draw.ellipse([cx - r, cy - r, cx + r, cy + r],
                      fill=(180, 225, 240, alpha))

    # Subtle decorative arc top-right
    for i in range(60, 0, -1):
        alpha = int(10 * (1 - i / 60))
        cx, cy = int(SIZE * 0.92), int(SIZE * 0.15)
        r = int(SIZE * 0.12 * i / 60)
        draw.ellipse([cx - r, cy - r, cx + r, cy + r],
                      fill=(180, 230, 245, alpha))

    # ── Calendar card ──
    card_margin = int(SIZE * 0.10)
    card_top = int(SIZE * 0.12)
    card_bottom = int(SIZE * 0.82)
    card_left = card_margin
    card_right = SIZE - card_margin
    card_radius = int(SIZE * 0.06)

    # Card shadow
    shadow = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    sd = ImageDraw.Draw(shadow)
    sd.rounded_rectangle(
        [card_left + 4, card_top + 8, card_right + 4, card_bottom + 8],
        radius=card_radius, fill=(0, 0, 0, 30)
    )
    shadow = shadow.filter(ImageFilter.GaussianBlur(radius=15))
    img = Image.alpha_composite(img, shadow)
    draw = ImageDraw.Draw(img)

    # Card body (white)
    draw.rounded_rectangle(
        [card_left, card_top, card_right, card_bottom],
        radius=card_radius, fill=(255, 255, 255, 245)
    )

    # ── Header gradient (cyan to teal) ──
    header_bottom = card_top + int((card_bottom - card_top) * 0.28)

    # Create header with gradient
    header_h = header_bottom - card_top
    for y in range(header_h):
        t = y / header_h
        r = int(120 + t * 20)   # 120 → 140
        g = int(215 - t * 15)   # 215 → 200
        b = int(230 - t * 20)   # 230 → 210
        # Cyan-ish: rgb(100, 210, 230) → rgb(70, 195, 210)
        r = int(100 + t * 30)
        g = int(215 - t * 15)
        b = int(235 - t * 15)
        yy = card_top + y
        # Clip to rounded rect top
        x_start = card_left
        x_end = card_right
        if y < card_radius:
            # Simple clipping for top corners
            offset = card_radius - int(math.sqrt(max(0, card_radius**2 - (card_radius - y)**2)))
            x_start = card_left + offset
            x_end = card_right - offset
        draw.line([(x_start, yy), (x_end, yy)], fill=(r, g, b, 255))

    # ── Dotted line separator under header ──
    sep_y = header_bottom + int(SIZE * 0.01)
    dot_r = int(SIZE * 0.006)
    num_dots = 18
    dot_spacing = (card_right - card_left - int(SIZE * 0.08)) / (num_dots - 1)
    for i in range(num_dots):
        cx = card_left + int(SIZE * 0.04) + int(i * dot_spacing)
        draw.ellipse([cx - dot_r, sep_y - dot_r, cx + dot_r, sep_y + dot_r],
                      fill=(200, 225, 235, 180))

    # ── 3 Ring holes at top ──
    ring_y = card_top - int(SIZE * 0.005)
    ring_r = int(SIZE * 0.028)
    ring_positions = [0.32, 0.50, 0.68]
    for xf in ring_positions:
        cx = int(SIZE * xf)
        # Outer ring shadow
        draw.ellipse([cx - ring_r - 2, ring_y - ring_r - 2, cx + ring_r + 2, ring_y + ring_r + 2],
                      fill=(180, 200, 210, 60))
        # White ring
        draw.ellipse([cx - ring_r, ring_y - ring_r, cx + ring_r, ring_y + ring_r],
                      fill=(240, 245, 250, 255))
        # Inner hole
        inner_r = int(ring_r * 0.5)
        draw.ellipse([cx - inner_r, ring_y - inner_r, cx + inner_r, ring_y + inner_r],
                      fill=(210, 225, 235, 200))

    # ── Calendar date dots (grid) ──
    grid_top = header_bottom + int(SIZE * 0.06)
    grid_bottom = card_bottom - int(SIZE * 0.04)
    grid_left = card_left + int(SIZE * 0.06)
    grid_right = card_right - int(SIZE * 0.06)
    rows = 4
    cols = 5
    dot_grid_r = int(SIZE * 0.012)

    for row in range(rows):
        for col in range(cols):
            t_x = col / (cols - 1)
            t_y = row / (rows - 1)
            cx = grid_left + int(t_x * (grid_right - grid_left))
            cy = grid_top + int(t_y * (grid_bottom - grid_top))
            draw.ellipse([cx - dot_grid_r, cy - dot_grid_r, cx + dot_grid_r, cy + dot_grid_r],
                          fill=(210, 225, 235, 150))

    # ── Day "08" circle (highlighted date) ──
    day_cx = int(SIZE * 0.54)
    day_cy = grid_top + int(0.35 * (grid_bottom - grid_top))
    day_r = int(SIZE * 0.055)

    # Glow
    for i in range(20, 0, -1):
        alpha = int(8 * (1 - i / 20))
        r = day_r + i * 2
        draw.ellipse([day_cx - r, day_cy - r, day_cx + r, day_cy + r],
                      fill=(80, 210, 220, alpha))

    # Circle
    draw.ellipse([day_cx - day_r, day_cy - day_r, day_cx + day_r, day_cy + day_r],
                  fill=(80, 210, 220, 240))

    # Text "08"
    try:
        font_day = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", int(SIZE * 0.048))
    except:
        font_day = ImageFont.load_default()
    draw.text((day_cx, day_cy), "08", fill=(255, 255, 255, 255), font=font_day, anchor="mm")

    # ── Sun/Moon badge (top-right of card) ──
    badge_cx = card_right - int(SIZE * 0.02)
    badge_cy = card_top + int(SIZE * 0.02)
    badge_r = int(SIZE * 0.055)

    # White circle background
    draw.ellipse([badge_cx - badge_r, badge_cy - badge_r, badge_cx + badge_r, badge_cy + badge_r],
                  fill=(255, 255, 255, 240))
    # Border
    draw.ellipse([badge_cx - badge_r, badge_cy - badge_r, badge_cx + badge_r, badge_cy + badge_r],
                  outline=(220, 230, 240, 180), width=2)

    # Moon (dark arc) + Sun (gold)
    moon_r = int(badge_r * 0.6)
    # Gold sun part
    draw.ellipse([badge_cx - moon_r + 4, badge_cy - moon_r + 2, badge_cx + moon_r + 4, badge_cy + moon_r + 2],
                  fill=(245, 195, 70, 255))
    # Blue-gray moon overlay
    draw.ellipse([badge_cx - moon_r - 4, badge_cy - moon_r - 2, badge_cx + moon_r - 8, badge_cy + moon_r - 2],
                  fill=(130, 195, 215, 255))

    # ── AI/Network badge (bottom-right) ──
    ai_cx = card_right + int(SIZE * 0.01)
    ai_cy = card_bottom - int(SIZE * 0.01)
    ai_r = int(SIZE * 0.055)

    # White circle background
    draw.ellipse([ai_cx - ai_r, ai_cy - ai_r, ai_cx + ai_r, ai_cy + ai_r],
                  fill=(255, 255, 255, 245))
    draw.ellipse([ai_cx - ai_r, ai_cy - ai_r, ai_cx + ai_r, ai_cy + ai_r],
                  outline=(200, 225, 235, 200), width=2)

    # Network nodes
    node_r = int(SIZE * 0.008)
    center_r = int(SIZE * 0.012)

    # Center node (teal)
    draw.ellipse([ai_cx - center_r, ai_cy - center_r, ai_cx + center_r, ai_cy + center_r],
                  fill=(70, 205, 200, 255))

    # Surrounding nodes
    node_positions = []
    for angle in [0, 72, 144, 216, 288]:
        rad = math.radians(angle - 90)
        dist = int(ai_r * 0.6)
        nx = ai_cx + int(dist * math.cos(rad))
        ny = ai_cy + int(dist * math.sin(rad))
        node_positions.append((nx, ny))
        # Line from center to node
        draw.line([(ai_cx, ai_cy), (nx, ny)], fill=(130, 215, 210, 180), width=2)
        # Node dot
        draw.ellipse([nx - node_r, ny - node_r, nx + node_r, ny + node_r],
                      fill=(130, 215, 210, 220))

    return img


def generate_all_sizes(source_img):
    """Resize source to all required iOS icon sizes."""
    sizes = [20, 29, 40, 58, 60, 76, 80, 87, 120, 152, 167, 180, 1024]

    for s in sizes:
        resized = source_img.resize((s, s), Image.LANCZOS)
        # Convert to RGB for PNG (no alpha needed for app icon)
        if resized.mode == "RGBA":
            bg = Image.new("RGB", (s, s), (235, 240, 248))
            bg.paste(resized, mask=resized.split()[3])
            resized = bg
        output_path = os.path.join(ICON_DIR, f"AppIcon-{s}.png")
        resized.save(output_path, "PNG")
        print(f"  ✅ {output_path} ({s}x{s})")


if __name__ == "__main__":
    print("🎨 Generating Lịch Số AI app icon...")
    icon = draw_icon()

    # Save the 1024 source first
    print("\n📐 Resizing to all iOS sizes...")
    generate_all_sizes(icon)

    print("\n✅ Done! All icons generated.")
