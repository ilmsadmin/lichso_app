"""
Generate 3 App Store Preview Videos (1284 × 2778px, 30fps, 15-30s)
Uses existing screenshots with pan/zoom animations.
Format: iPhone 6.7" Display
"""

from PIL import Image, ImageDraw, ImageFont, ImageFilter
import os
import glob
import math
import struct
import zlib
import subprocess
import shutil

INPUT_DIR = "screenshots"
OUTPUT_DIR = "appstore_previews"
FRAME_SIZE = (886, 1920)  # App preview size for 6.7" (portrait)
FPS = 30
SECONDS_PER_SLIDE = 4
TRANSITION_FRAMES = int(FPS * 0.6)  # 0.6s transition

# Preview themes - 3 previews with different screenshot groups
# Files sorted: AI tử vi, Cài đặt, Cây gia phả, Chi tiết thành viên,
#   Danh mục, Lịch tháng, Lịch vạn niên, Nhắc nhở 2,
#   Nhắc nhở âm dương, Profile, Thành viên gia đình, Văn khấn
PREVIEWS = [
    {
        "name": "preview_1_calendar",
        "title": "Lịch Vạn Niên & Âm Lịch",
        "screenshots": [6, 5, 4, 11],
        "captions": [
            "Lịch Vạn Niên\nCan Chi & Giờ Hoàng Đạo",
            "Lịch Tháng\nÂm Dương Đầy Đủ",
            "Danh Mục\nTất Cả Tính Năng",
            "Văn Khấn\nĐầy Đủ Mọi Dịp",
        ],
        "gradient_top": (180, 0, 0),
        "gradient_bottom": (255, 80, 0),
        "accent": (255, 220, 180),
    },
    {
        "name": "preview_2_ai_family",
        "title": "AI & Gia Đình",
        "screenshots": [0, 2, 10, 3],
        "captions": [
            "Tử Vi Phong Thuỷ\nAI Tư Vấn Chính Xác",
            "Cây Gia Phả\nLưu Giữ Dòng Họ",
            "Thành Viên\nQuản Lý Gia Đình",
            "Chi Tiết Thành Viên\nNgày Sinh & Can Chi",
        ],
        "gradient_top": (139, 0, 255),
        "gradient_bottom": (255, 0, 110),
        "accent": (255, 200, 255),
    },
    {
        "name": "preview_3_reminders",
        "title": "Nhắc Nhở & Cá Nhân",
        "screenshots": [8, 7, 9, 1],
        "captions": [
            "Nhắc Nhở Âm Lịch\nGiỗ, Rằm, Mùng Một",
            "Nhắc Nhở\nKhông Bỏ Lỡ Sự Kiện",
            "Hồ Sơ Cá Nhân\nQuản Lý Tài Khoản",
            "Cài Đặt\nTuỳ Chỉnh Linh Hoạt",
        ],
        "gradient_top": (0, 130, 130),
        "gradient_bottom": (0, 210, 180),
        "accent": (180, 255, 240),
    },
]

GRADIENT_TOP = (15, 15, 35)
GRADIENT_BOTTOM = (5, 5, 15)
ACCENT_GOLD = (212, 175, 55)


def try_load_font(size):
    font_paths = [
        "/System/Library/Fonts/Supplemental/Arial Bold.ttf",
        "/System/Library/Fonts/Helvetica.ttc",
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


def draw_gradient(draw, width, height, top_color, bottom_color):
    for y in range(height):
        ratio = y / height
        r = int(top_color[0] + (bottom_color[0] - top_color[0]) * ratio)
        g = int(top_color[1] + (bottom_color[1] - top_color[1]) * ratio)
        b = int(top_color[2] + (bottom_color[2] - top_color[2]) * ratio)
        draw.line([(0, y), (width, y)], fill=(r, g, b))


def draw_rounded_rect(draw, xy, radius, fill):
    x0, y0, x1, y1 = xy
    draw.rectangle([x0 + radius, y0, x1 - radius, y1], fill=fill)
    draw.rectangle([x0, y0 + radius, x1, y1 - radius], fill=fill)
    draw.pieslice([x0, y0, x0 + 2*radius, y0 + 2*radius], 180, 270, fill=fill)
    draw.pieslice([x1 - 2*radius, y0, x1, y0 + 2*radius], 270, 360, fill=fill)
    draw.pieslice([x0, y1 - 2*radius, x0 + 2*radius, y1], 90, 180, fill=fill)
    draw.pieslice([x1 - 2*radius, y1 - 2*radius, x1, y1], 0, 90, fill=fill)


def create_slide_frame(screenshot, caption, frame_size, progress=1.0):
    """Create a single frame with phone mockup and caption."""
    target_w, target_h = frame_size
    canvas = Image.new("RGB", frame_size, (0, 0, 0))
    draw = ImageDraw.Draw(canvas)

    # Gradient background
    draw_gradient(draw, target_w, target_h, GRADIENT_TOP, GRADIENT_BOTTOM)

    # Caption text
    font_title = try_load_font(56)
    font_sub = try_load_font_regular(34)

    # Animate text: slide in from bottom with fade
    text_offset_y = int((1.0 - ease_out(progress)) * 60)

    for line_idx, line in enumerate(caption.split("\n")):
        bbox = draw.textbbox((0, 0), line, font=font_title)
        lw = bbox[2] - bbox[0]
        alpha_color = tuple(int(c * min(1.0, progress * 2)) for c in ACCENT_GOLD)
        draw.text(((target_w - lw) // 2, 100 + line_idx * 70 + text_offset_y),
                  line, fill=alpha_color, font=font_title)

    # Phone mockup
    phone_w = 520
    phone_h = 1120
    phone_x = (target_w - phone_w) // 2
    phone_y = target_h - phone_h - 140

    # Animate phone: subtle scale up
    scale_factor = 0.95 + 0.05 * ease_out(progress)
    scaled_phone_w = int(phone_w * scale_factor)
    scaled_phone_h = int(phone_h * scale_factor)
    phone_x = (target_w - scaled_phone_w) // 2
    phone_y = target_h - scaled_phone_h - 140

    # Draw phone bezel
    bezel = 6
    draw_rounded_rect(draw,
        (phone_x - bezel, phone_y - bezel,
         phone_x + scaled_phone_w + bezel, phone_y + scaled_phone_h + bezel),
        35, fill=(40, 40, 40))
    draw_rounded_rect(draw,
        (phone_x, phone_y, phone_x + scaled_phone_w, phone_y + scaled_phone_h),
        30, fill=(0, 0, 0))

    # Fit screenshot
    img_w, img_h = screenshot.size
    margin = 3
    sw = scaled_phone_w - margin * 2
    sh = scaled_phone_h - margin * 2
    sc = min(sw / img_w, sh / img_h)
    nw, nh = int(img_w * sc), int(img_h * sc)
    resized = screenshot.resize((nw, nh), Image.LANCZOS)

    mask = Image.new("L", (nw, nh), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, nw, nh], radius=27, fill=255)

    sx = phone_x + margin + (sw - nw) // 2
    sy = phone_y + margin + (sh - nh) // 2
    canvas.paste(resized, (sx, sy), mask)

    # Branding
    brand_font = try_load_font_regular(24)
    brand = "Lịch Số — Lịch Vạn Niên"
    bbox = draw.textbbox((0, 0), brand, font=brand_font)
    bw = bbox[2] - bbox[0]
    draw = ImageDraw.Draw(canvas)
    draw.text(((target_w - bw) // 2, target_h - 70), brand, fill=(100, 100, 100), font=brand_font)

    return canvas


def ease_out(t):
    """Ease out cubic."""
    return 1.0 - (1.0 - min(1.0, max(0.0, t))) ** 3


def blend_frames(frame_a, frame_b, alpha):
    """Cross-fade two frames."""
    return Image.blend(frame_a, frame_b, alpha)


def generate_preview(preview_config, all_screenshots, output_dir):
    """Generate one preview video as individual frames, then encode with ffmpeg."""
    name = preview_config["name"]
    indices = preview_config["screenshots"]
    captions = preview_config["captions"]

    # Clamp indices to available screenshots
    available = []
    for idx in indices:
        if idx < len(all_screenshots):
            available.append(idx)
    if not available:
        print(f"  Skipping {name}: no screenshots available")
        return

    screenshots = [all_screenshots[i] for i in available]
    used_captions = captions[:len(screenshots)]

    frames_dir = os.path.join(output_dir, f"{name}_frames")
    os.makedirs(frames_dir, exist_ok=True)

    total_slides = len(screenshots)
    frames_per_slide = FPS * SECONDS_PER_SLIDE
    total_frames = frames_per_slide * total_slides

    print(f"  Generating {total_frames} frames ({total_slides} slides × {SECONDS_PER_SLIDE}s)...")

    frame_num = 0
    for slide_idx in range(total_slides):
        screenshot = screenshots[slide_idx]
        caption = used_captions[slide_idx]

        for f in range(frames_per_slide):
            # Progress within slide for entrance animation
            entrance_progress = min(1.0, f / (FPS * 0.8))  # 0.8s entrance

            frame = create_slide_frame(screenshot, caption, FRAME_SIZE, entrance_progress)

            # Cross-fade transition to next slide
            if f >= frames_per_slide - TRANSITION_FRAMES and slide_idx < total_slides - 1:
                next_screenshot = screenshots[slide_idx + 1]
                next_caption = used_captions[slide_idx + 1]
                next_frame = create_slide_frame(next_screenshot, next_caption, FRAME_SIZE, 0.0)
                alpha = (f - (frames_per_slide - TRANSITION_FRAMES)) / TRANSITION_FRAMES
                frame = blend_frames(frame, next_frame, alpha)

            frame.save(os.path.join(frames_dir, f"frame_{frame_num:05d}.png"), "PNG")
            frame_num += 1

            if frame_num % 30 == 0:
                print(f"    Frame {frame_num}/{total_frames}")

    # Encode with ffmpeg
    output_path = os.path.join(output_dir, f"{name}.mp4")
    ffmpeg_cmd = [
        "ffmpeg", "-y",
        "-framerate", str(FPS),
        "-i", os.path.join(frames_dir, "frame_%05d.png"),
        "-c:v", "libx264",
        "-pix_fmt", "yuv420p",
        "-crf", "18",
        "-preset", "slow",
        "-movflags", "+faststart",
        output_path
    ]

    print(f"  Encoding video: {output_path}")
    try:
        subprocess.run(ffmpeg_cmd, check=True, capture_output=True, text=True)
        print(f"  ✅ {output_path} created successfully!")
    except FileNotFoundError:
        print("  ⚠️  ffmpeg not found. Frames saved, encode manually:")
        print(f"     ffmpeg -framerate {FPS} -i {frames_dir}/frame_%05d.png -c:v libx264 -pix_fmt yuv420p -crf 18 {output_path}")
        return
    except subprocess.CalledProcessError as e:
        print(f"  ❌ ffmpeg error: {e.stderr}")
        return

    # Cleanup frames
    shutil.rmtree(frames_dir)
    print(f"  Cleaned up temp frames.")


def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    files = sorted(glob.glob(os.path.join(INPUT_DIR, "*.jpg")))
    if not files:
        print("No screenshots found in", INPUT_DIR)
        return

    print(f"Found {len(files)} screenshots")
    all_screenshots = [Image.open(f).convert("RGBA") for f in files]

    for idx, preview in enumerate(PREVIEWS, 1):
        print(f"\n[{idx}/3] Generating {preview['name']}...")
        generate_preview(preview, all_screenshots, OUTPUT_DIR)

    print(f"\n🎬 Done! 3 app previews saved to {OUTPUT_DIR}/")
    print("Format: 886×1920 H.264, 30fps")
    print("Ready for App Store Connect upload.")


if __name__ == "__main__":
    main()
