"""
Generate App Store screenshots (1284 × 2778px) with vibrant marketing design.
- Eye-catching gradient backgrounds per screenshot
- iPhone device frame mockup with glow effects
- Vietnamese marketing captions matched to actual screen content
Compatible with iPhone 6.7" (iPhone 14 Pro Max, 15 Pro Max, etc.)
"""

from PIL import Image, ImageDraw, ImageFilter, ImageFont
import os
import glob
import math

INPUT_DIR = "screenshots"
OUTPUT_DIR = "appstore_screenshots"
TARGET_SIZE = (1284, 2778)

# Captions matched to actual screenshot filenames (sorted alphabetically)
# Files: AI tử vi, Cài đặt, Cây gia phả, Chi tiết thành viên,
#        Danh mục, Lịch tháng, Lịch vạn niên, Nhắc nhở 2,
#        Nhắc nhở theo lịch âm dương, Profile, Thành viên gia đình, Văn khấn
SCREENSHOTS_CONFIG = [
    {
        "title": "Tử Vi & Phong Thuỷ",
        "subtitle": "AI tư vấn tử vi, phong thuỷ\nchính xác theo ngày sinh",
        "gradient": [(139, 0, 255), (255, 0, 110)],      # Purple → Pink
        "accent": (255, 200, 255),
    },
    {
        "title": "Tuỳ Chỉnh Linh Hoạt",
        "subtitle": "Cá nhân hoá mọi thiết lập\ntheo phong cách của bạn",
        "gradient": [(30, 30, 60), (60, 60, 100)],        # Dark elegant
        "accent": (180, 200, 255),
    },
    {
        "title": "Cây Gia Phả",
        "subtitle": "Lưu giữ & quản lý\ndòng họ gia đình",
        "gradient": [(0, 100, 0), (0, 200, 100)],         # Green forest
        "accent": (200, 255, 200),
    },
    {
        "title": "Thông Tin Thành Viên",
        "subtitle": "Chi tiết ngày sinh, tuổi\ncan chi từng người",
        "gradient": [(0, 50, 100), (0, 150, 200)],        # Ocean blue
        "accent": (180, 230, 255),
    },
    {
        "title": "Danh Mục Đầy Đủ",
        "subtitle": "Tất cả tính năng\ntrong tầm tay bạn",
        "gradient": [(200, 80, 0), (255, 160, 0)],        # Orange fire
        "accent": (255, 230, 180),
    },
    {
        "title": "Lịch Tháng",
        "subtitle": "Tổng quan lịch âm dương\nmỗi tháng trong năm",
        "gradient": [(0, 80, 180), (0, 180, 255)],        # Sky blue
        "accent": (200, 230, 255),
    },
    {
        "title": "Lịch Vạn Niên",
        "subtitle": "Tra cứu can chi, giờ hoàng đạo\nhướng xuất hành mỗi ngày",
        "gradient": [(180, 0, 0), (255, 80, 0)],          # Red → Orange
        "accent": (255, 220, 180),
    },
    {
        "title": "Nhắc Nhở Thông Minh",
        "subtitle": "Không bỏ lỡ sự kiện\nvới nhắc nhở tự động",
        "gradient": [(0, 130, 130), (0, 210, 180)],       # Teal
        "accent": (180, 255, 240),
    },
    {
        "title": "Nhắc Nhở Âm Lịch",
        "subtitle": "Giỗ, lễ, rằm, mùng một\nnhắc theo lịch âm dương",
        "gradient": [(80, 0, 150), (180, 0, 200)],        # Deep purple
        "accent": (230, 200, 255),
    },
    {
        "title": "Hồ Sơ Cá Nhân",
        "subtitle": "Quản lý thông tin\ntài khoản dễ dàng",
        "gradient": [(0, 60, 120), (30, 120, 180)],       # Navy
        "accent": (180, 210, 255),
    },
    {
        "title": "Gia Đình",
        "subtitle": "Quản lý thành viên\nvà mối quan hệ",
        "gradient": [(150, 50, 0), (220, 120, 0)],        # Warm brown
        "accent": (255, 220, 180),
    },
    {
        "title": "Văn Khấn",
        "subtitle": "Bộ sưu tập văn khấn\nđầy đủ cho mọi dịp",
        "gradient": [(160, 0, 50), (220, 50, 80)],        # Crimson
        "accent": (255, 200, 210),
    },
]

os.makedirs(OUTPUT_DIR, exist_ok=True)

files = sorted(glob.glob(os.path.join(INPUT_DIR, "*.jpg")))[:10]


def draw_gradient_3color(draw, width, height, top_color, bottom_color):
    """Draw vibrant vertical gradient with a bright middle."""
    mid_color = tuple(min(255, int((t + b) / 2 + 40)) for t, b in zip(top_color, bottom_color))
    for y in range(height):
        ratio = y / height
        if ratio < 0.5:
            r2 = ratio * 2
            r = int(top_color[0] + (mid_color[0] - top_color[0]) * r2)
            g = int(top_color[1] + (mid_color[1] - top_color[1]) * r2)
            b = int(top_color[2] + (mid_color[2] - top_color[2]) * r2)
        else:
            r2 = (ratio - 0.5) * 2
            r = int(mid_color[0] + (bottom_color[0] - mid_color[0]) * r2)
            g = int(mid_color[1] + (bottom_color[1] - mid_color[1]) * r2)
            b = int(mid_color[2] + (bottom_color[2] - mid_color[2]) * r2)
        draw.line([(0, y), (width, y)], fill=(r, g, b))


def draw_rounded_rect(draw, xy, radius, fill):
    x0, y0, x1, y1 = xy
    draw.rectangle([x0 + radius, y0, x1 - radius, y1], fill=fill)
    draw.rectangle([x0, y0 + radius, x1, y1 - radius], fill=fill)
    draw.pieslice([x0, y0, x0 + 2*radius, y0 + 2*radius], 180, 270, fill=fill)
    draw.pieslice([x1 - 2*radius, y0, x1, y0 + 2*radius], 270, 360, fill=fill)
    draw.pieslice([x0, y1 - 2*radius, x0 + 2*radius, y1], 90, 180, fill=fill)
    draw.pieslice([x1 - 2*radius, y1 - 2*radius, x1, y1], 0, 90, fill=fill)


def draw_glow_circle(canvas, cx, cy, radius, color, opacity=60):
    """Draw a soft glow circle for decoration."""
    glow = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow)
    for r in range(radius, 0, -2):
        alpha = int(opacity * (r / radius) ** 0.5)
        glow_draw.ellipse(
            [cx - r, cy - r, cx + r, cy + r],
            fill=(*color, alpha)
        )
    return Image.alpha_composite(canvas.convert("RGBA"), glow)


def add_phone_frame(canvas, screenshot, x, y, phone_w, phone_h, corner_radius=45, glow_color=(255, 255, 255)):
    """Add phone frame with glow effect."""
    # Glow behind phone
    canvas_rgba = canvas.convert("RGBA")
    glow = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow)
    for s in range(40, 0, -1):
        alpha = int(40 * (s / 40))
        draw_rounded_rect(glow_draw,
            (x - s, y - s + 10, x + phone_w + s, y + phone_h + s + 10),
            corner_radius + s, fill=(*glow_color[:3], alpha))
    canvas_rgba = Image.alpha_composite(canvas_rgba, glow)
    canvas = canvas_rgba.convert("RGB")

    draw = ImageDraw.Draw(canvas)

    # Phone bezel
    bezel = 10
    draw_rounded_rect(draw,
        (x - bezel, y - bezel, x + phone_w + bezel, y + phone_h + bezel),
        corner_radius + bezel, fill=(25, 25, 25))
    # Inner bezel highlight
    draw_rounded_rect(draw,
        (x - 2, y - 2, x + phone_w + 2, y + phone_h + 2),
        corner_radius + 2, fill=(60, 60, 60))
    # Screen
    draw_rounded_rect(draw,
        (x, y, x + phone_w, y + phone_h),
        corner_radius, fill=(0, 0, 0))

    # Resize screenshot to fit
    margin = 4
    sw = phone_w - margin * 2
    sh = phone_h - margin * 2
    img_w, img_h = screenshot.size
    scale = min(sw / img_w, sh / img_h)
    nw, nh = int(img_w * scale), int(img_h * scale)
    resized = screenshot.resize((nw, nh), Image.LANCZOS)

    mask = Image.new("L", (nw, nh), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, nw, nh], radius=corner_radius - 4, fill=255)

    sx = x + margin + (sw - nw) // 2
    sy = y + margin + (sh - nh) // 2
    canvas.paste(resized, (sx, sy), mask)

    return canvas


def try_load_font(size):
    font_paths = [
        "/System/Library/Fonts/Supplemental/Arial Bold.ttf",
        "/System/Library/Fonts/Helvetica.ttc",
        "/System/Library/Fonts/SFNSDisplay.ttf",
        "/System/Library/Fonts/SFNS.ttf",
    ]
    for fp in font_paths:
        try:
            return ImageFont.truetype(fp, size)
        except (OSError, IOError):
            continue
    return ImageFont.load_default()


def try_load_font_regular(size):
    font_paths = [
        "/System/Library/Fonts/Supplemental/Arial.ttf",
        "/System/Library/Fonts/Helvetica.ttc",
        "/System/Library/Fonts/SFNS.ttf",
    ]
    for fp in font_paths:
        try:
            return ImageFont.truetype(fp, size)
        except (OSError, IOError):
            continue
    return ImageFont.load_default()


def draw_text_with_shadow(draw, pos, text, font, fill, shadow_color=(0, 0, 0), shadow_offset=3):
    x, y = pos
    draw.text((x + shadow_offset, y + shadow_offset), text, fill=shadow_color, font=font)
    draw.text((x, y), text, fill=fill, font=font)


for i, filepath in enumerate(files, 1):
    img = Image.open(filepath).convert("RGBA")
    config = SCREENSHOTS_CONFIG[i - 1] if i <= len(SCREENSHOTS_CONFIG) else SCREENSHOTS_CONFIG[0]

    canvas = Image.new("RGB", TARGET_SIZE, (0, 0, 0))
    draw = ImageDraw.Draw(canvas)
    target_w, target_h = TARGET_SIZE

    # 1) Vibrant gradient background
    grad_top, grad_bottom = config["gradient"]
    draw_gradient_3color(draw, target_w, target_h, grad_top, grad_bottom)

    # 2) Decorative glow circles
    canvas = draw_glow_circle(canvas, target_w // 4, 400, 300, grad_top, 50)
    canvas = draw_glow_circle(canvas, target_w * 3 // 4, target_h - 500, 350, grad_bottom, 40)
    canvas = draw_glow_circle(canvas, target_w // 2, target_h // 2, 500, config["accent"][:3], 20)

    draw = ImageDraw.Draw(canvas)

    # 3) Title with shadow
    font_title = try_load_font(78)
    font_sub = try_load_font_regular(46)

    title = config["title"]
    bbox = draw.textbbox((0, 0), title, font=font_title)
    tw = bbox[2] - bbox[0]
    draw_text_with_shadow(draw, ((target_w - tw) // 2, 130), title,
                          font_title, fill=(255, 255, 255), shadow_offset=4)

    # Subtitle
    for line_idx, line in enumerate(config["subtitle"].split("\n")):
        bbox = draw.textbbox((0, 0), line, font=font_sub)
        lw = bbox[2] - bbox[0]
        draw_text_with_shadow(draw, ((target_w - lw) // 2, 250 + line_idx * 62), line,
                              font_sub, fill=config["accent"], shadow_offset=2)

    # 4) Phone mockup
    phone_w = 730
    phone_h = 1580
    phone_x = (target_w - phone_w) // 2
    phone_y = target_h - phone_h - 200

    canvas = add_phone_frame(canvas, img, phone_x, phone_y, phone_w, phone_h,
                             corner_radius=45, glow_color=config["accent"])

    # 5) Bottom branding bar
    draw_final = ImageDraw.Draw(canvas)
    brand_font = try_load_font(34)
    brand_text = "LỊCH SỐ — Lịch Vạn Niên"
    bbox = draw_final.textbbox((0, 0), brand_text, font=brand_font)
    bw = bbox[2] - bbox[0]
    # Semi-transparent bar
    bar_y = target_h - 130
    draw_final.rectangle([(0, bar_y), (target_w, target_h)], fill=(*grad_bottom, ))
    draw_final.text(((target_w - bw) // 2, bar_y + 35), brand_text,
                    fill=(255, 255, 255, 200), font=brand_font)

    # Small tagline
    tag_font = try_load_font_regular(26)
    tag = "Ứng dụng lịch #1 cho người Việt"
    bbox = draw_final.textbbox((0, 0), tag, font=tag_font)
    tw2 = bbox[2] - bbox[0]
    draw_final.text(((target_w - tw2) // 2, bar_y + 80), tag,
                    fill=config["accent"], font=tag_font)

    output_path = os.path.join(OUTPUT_DIR, f"screenshot_{i:02d}.png")
    canvas.save(output_path, "PNG")
    print(f"[{i}/{len(files)}] {os.path.basename(filepath)} -> {output_path}")

print(f"\nDone! {len(files)} App Store screenshots saved to {OUTPUT_DIR}/")
