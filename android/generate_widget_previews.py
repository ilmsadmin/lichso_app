#!/usr/bin/env python3
"""
Generate widget preview PNG images for Lịch Số app.
Creates both Light and Dark mode previews for all widgets.

Widgets:
  1. Calendar Day  (2×2) — Light + Dark
  2. AI Chat       (3×2) — Light + Dark
  3. Clock Glass   (4×2) — Transparent (chỉ 1 mode)
  4. Clock2        (4×2) — Light + Dark
  5. Calendar Month(4×4) — Light + Dark

Output: drawable-nodpi/ PNG files referenced by previewImage in widget info XML.
"""
import subprocess
import os
import sys

try:
    from PIL import Image, ImageDraw, ImageFont
except ImportError:
    print("Installing Pillow...")
    subprocess.check_call([sys.executable, "-m", "pip", "install", "Pillow"])
    from PIL import Image, ImageDraw, ImageFont

OUTPUT_DIR = "app/src/main/res/drawable-nodpi"
os.makedirs(OUTPUT_DIR, exist_ok=True)

# ── Shared color palettes ───────────────────────────────────────────
LIGHT = {
    "bg": (255, 251, 248),           # #FFFBF5
    "bg_outline": (216, 194, 191, 34),
    "text_primary": (28, 27, 31),     # #1C1B1F
    "text_secondary": (83, 67, 64),   # #534340
    "text_tertiary": (133, 115, 113), # #857371
    "text_muted": (160, 140, 138),    # #A08C8A
    "lunar": (212, 160, 23),          # #D4A017
    "red": (183, 28, 28),             # #B71C1C
    "green": (46, 125, 50),           # #2E7D32
    "divider": (216, 194, 191, 34),
    "bubble_bg": (243, 237, 235),     # #F3EDEB
    "input_bg": (243, 237, 235),
    "badge_bg": (0, 0, 0, 12),
}

DARK = {
    "bg": (127, 29, 29),             # #7F1D1D deep red
    "bg_outline": (255, 255, 255, 32),
    "text_primary": (255, 255, 255),
    "text_secondary": (255, 255, 255, 179),  # #B3FFFFFF
    "text_tertiary": (255, 255, 255, 128),
    "text_muted": (255, 255, 255, 102),
    "lunar": (255, 215, 0),          # #FFD700
    "red": (255, 255, 255),          # day number white in dark
    "green": (129, 199, 132),        # #81C784
    "divider": (255, 255, 255, 51),
    "bubble_bg": (255, 255, 255, 26),
    "input_bg": (255, 255, 255, 20),
    "badge_bg": (255, 255, 255, 32),
    "header_bg": (169, 50, 38),      # #A93226
}


def _load_fonts(sizes):
    """Load Helvetica fonts with fallback. sizes = dict of name→size."""
    fonts = {}
    for name, size in sizes.items():
        try:
            fonts[name] = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", size)
        except:
            fonts[name] = ImageFont.load_default()
    return fonts


# ═══════════════════════════════════════════════════════════════════
# 1. CALENDAR DAY WIDGET  (2×2)  — Light + Dark
# ═══════════════════════════════════════════════════════════════════

def create_calendar_preview(theme="light"):
    """2×2 Calendar day widget preview"""
    p = LIGHT if theme == "light" else DARK
    w, h = 400, 400
    img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    radius = 40
    draw.rounded_rectangle([(0, 0), (w-1, h-1)], radius=radius,
                           fill=p["bg"], outline=p["bg_outline"], width=1)

    f = _load_fonts({"dow": 24, "day": 96, "month": 22, "lunar": 22, "canchi": 20, "rating": 20})
    cx = w // 2

    day_num_color = (183, 28, 28) if theme == "light" else (255, 255, 255)

    draw.text((cx, 40), "Thứ Tư", fill=p["text_tertiary"], font=f["dow"], anchor="mt")
    draw.text((cx, 80), "08", fill=day_num_color, font=f["day"], anchor="mt")
    draw.text((cx, 190), "tháng 4 · 2026", fill=p["text_secondary"], font=f["month"], anchor="mt")
    # Divider
    draw.line([(cx-50, 225), (cx+50, 225)], fill=p["divider"], width=2)
    draw.text((cx, 240), "21 · tháng 2 Âm", fill=p["lunar"], font=f["lunar"], anchor="mt")
    draw.text((cx, 275), "Nhâm Tý · Bính Ngọ", fill=p["text_secondary"], font=f["canchi"], anchor="mt")
    draw.text((cx, 315), "✦ Ngày Hoàng Đạo", fill=p["green"], font=f["rating"], anchor="mt")

    suffix = "" if theme == "light" else "_dark"
    fname = f"widget_calendar_preview{suffix}.png"
    img.save(os.path.join(OUTPUT_DIR, fname))
    if theme == "dark":
        night_dir = OUTPUT_DIR.replace("drawable-nodpi", "drawable-night-nodpi")
        os.makedirs(night_dir, exist_ok=True)
        img.save(os.path.join(night_dir, "widget_calendar_preview.png"))
    print(f"✅ {fname}")


# ═══════════════════════════════════════════════════════════════════
# 2. AI CHAT WIDGET  (3×2)  — Light + Dark
# ═══════════════════════════════════════════════════════════════════

def create_ai_preview(theme="light"):
    """3×2 AI Chat widget preview"""
    p = LIGHT if theme == "light" else DARK
    w, h = 600, 400
    img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Card background
    draw.rounded_rectangle([(0, 0), (w-1, h-1)], radius=40,
                           fill=p["bg"], outline=p["bg_outline"], width=1)

    # Header (red in light, darker red in dark)
    header_bg = (183, 28, 28) if theme == "light" else (153, 27, 27)  # #991B1B
    draw.rounded_rectangle([(0, 0), (w-1, 80)], radius=40, fill=header_bg)
    draw.rectangle([(0, 40), (w-1, 80)], fill=header_bg)

    f = _load_fonts({"title": 24, "status": 16, "bubble": 20, "input": 20})

    # Avatar
    draw.ellipse([(20, 20), (60, 60)], fill=(255, 255, 255, 51))
    draw.text((40, 32), "✦", fill=(255, 213, 79), font=f["status"], anchor="mt")
    draw.text((72, 22), "Lịch Số AI", fill=(255, 255, 255), font=f["title"])
    draw.text((72, 50), "● Đang hoạt động", fill=(255, 255, 255, 153), font=f["status"])
    draw.text((w-40, 40), "8/4", fill=(255, 255, 255, 204), font=f["status"], anchor="mt")

    # AI bubble
    bubble_bg = p["bubble_bg"]
    bubble_text = p["text_primary"]
    draw.rounded_rectangle([(20, 100), (w-60, 160)], radius=20, fill=bubble_bg)
    draw.text((32, 110), "✦ Hôm nay Ngày Hoàng Đạo,", fill=bubble_text, font=f["bubble"])
    draw.text((32, 135), "hướng tài lộc Đông Nam", fill=bubble_text, font=f["bubble"])

    # User bubble
    user_bg = (183, 28, 28) if theme == "light" else (169, 50, 38)
    draw.rounded_rectangle([(w-220, 175), (w-20, 215)], radius=20, fill=user_bg)
    draw.text((w-210, 185), "Hỏi AI tử vi ›", fill=(255, 255, 255), font=f["bubble"])

    # Input bar
    draw.rounded_rectangle([(20, h-60), (w-80, h-20)], radius=16, fill=p["input_bg"])
    draw.text((36, h-50), "Hỏi gì đó...", fill=p["text_tertiary"], font=f["input"])
    send_bg = (183, 28, 28) if theme == "light" else (169, 50, 38)
    draw.rounded_rectangle([(w-60, h-60), (w-20, h-20)], radius=16, fill=send_bg)
    draw.text((w-40, h-48), "›", fill=(255, 255, 255), font=f["title"], anchor="mt")

    suffix = "" if theme == "light" else "_dark"
    fname = f"widget_ai_preview{suffix}.png"
    img.save(os.path.join(OUTPUT_DIR, fname))
    if theme == "dark":
        night_dir = OUTPUT_DIR.replace("drawable-nodpi", "drawable-night-nodpi")
        os.makedirs(night_dir, exist_ok=True)
        img.save(os.path.join(night_dir, "widget_ai_preview.png"))
    print(f"✅ {fname}")


# ═══════════════════════════════════════════════════════════════════
# 3. CLOCK GLASS WIDGET  (4×2)  — Transparent only (no dark/light)
# ═══════════════════════════════════════════════════════════════════

def create_clock_preview():
    """4×2 Clock & Weather widget preview — glassmorphism (transparent)"""
    w, h = 800, 400
    img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    draw.rounded_rectangle([(0, 0), (w-1, h-1)], radius=56,
                           fill=(0, 0, 0, 153), outline=(255, 255, 255, 48), width=2)

    f = _load_fonts({"time": 96, "date": 28, "lunar": 22, "temp": 52,
                     "desc": 22, "range": 16, "badge": 20, "icon": 64})

    draw.text((32, 40), "14:30", fill=(255, 255, 255), font=f["time"])
    draw.text((32, 155), "Thứ Tư, 08/04/2026", fill=(255, 255, 255, 230), font=f["date"])
    draw.text((32, 190), "☽ 21/02 Âm · Nhâm Tý", fill=(255, 215, 0, 230), font=f["lunar"])

    draw.line([(w//2, 40), (w//2, h-80)], fill=(255, 255, 255, 51), width=2)

    draw.text((w//2 + 60, 40), "⛅", fill=(255, 255, 255), font=f["icon"])
    draw.text((w//2 + 60, 130), "28°", fill=(255, 255, 255), font=f["temp"])
    draw.text((w//2 + 60, 195), "Có mây", fill=(255, 255, 255, 179), font=f["desc"])
    draw.text((w//2 + 60, 225), "💧 75%  💨 12 km/h", fill=(255, 255, 255, 128), font=f["range"])

    draw.rounded_rectangle([(24, h-60), (240, h-24)], radius=24, fill=(255, 255, 255, 32))
    draw.text((36, h-56), "✦ Ngày Hoàng Đạo", fill=(129, 199, 132), font=f["badge"])

    img.save(os.path.join(OUTPUT_DIR, "widget_clock_preview.png"))
    print("✅ widget_clock_preview.png")


# ═══════════════════════════════════════════════════════════════════
# 4. CLOCK2 WIDGET  (4×2)  — Light + Dark
# ═══════════════════════════════════════════════════════════════════

def create_clock2_preview(theme="light"):
    """4×2 Clock2 (Đồng Hồ Sáng/Tối) widget preview"""
    w, h = 800, 400
    img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    if theme == "light":
        bg_fill = (245, 245, 245)       # #F5F5F5
        bg_outline = (0, 0, 0, 18)
        time_color = (28, 27, 31)
        date_color = (83, 67, 64)
        lunar_color = (184, 134, 11)    # #B8860B
        divider_color = (0, 0, 0, 21)
        temp_color = (28, 27, 31)
        desc_color = (133, 115, 113)
        range_color = (160, 140, 138)
        badge_bg = (0, 0, 0, 12)
        badge_star = (46, 125, 50)
        badge_text = (83, 67, 64)
    else:
        bg_fill = (127, 29, 29)         # #7F1D1D
        bg_outline = (255, 255, 255, 32)
        time_color = (255, 255, 255)
        date_color = (255, 255, 255, 230)
        lunar_color = (184, 134, 11)
        divider_color = (255, 255, 255, 51)
        temp_color = (255, 255, 255)
        desc_color = (255, 255, 255, 179)
        range_color = (255, 255, 255, 128)
        badge_bg = (255, 255, 255, 32)
        badge_star = (129, 199, 132)
        badge_text = (255, 255, 255, 204)

    draw.rounded_rectangle([(0, 0), (w-1, h-1)], radius=56,
                           fill=bg_fill, outline=bg_outline, width=2)

    f = _load_fonts({"time": 96, "date": 28, "lunar": 22, "temp": 52,
                     "desc": 22, "range": 16, "badge": 20, "icon": 64})

    draw.text((32, 40), "14:30", fill=time_color, font=f["time"])
    draw.text((32, 155), "Thứ Tư, 08/04/2026", fill=date_color, font=f["date"])
    draw.text((32, 190), "☽ 21/02 Âm · Nhâm Tý", fill=lunar_color, font=f["lunar"])

    draw.line([(w//2, 40), (w//2, h-80)], fill=divider_color, width=2)

    draw.text((w//2 + 60, 40), "⛅", fill=time_color, font=f["icon"])
    draw.text((w//2 + 60, 130), "28°", fill=temp_color, font=f["temp"])
    draw.text((w//2 + 60, 195), "Có mây", fill=desc_color, font=f["desc"])
    draw.text((w//2 + 60, 225), "💧 75%  💨 12 km/h", fill=range_color, font=f["range"])

    draw.rounded_rectangle([(24, h-60), (240, h-24)], radius=24, fill=badge_bg)
    draw.text((36, h-56), "✦", fill=badge_star, font=f["badge"])
    draw.text((60, h-56), "Ngày Hoàng Đạo", fill=badge_text, font=f["badge"])

    suffix = "" if theme == "light" else "_dark"
    fname = f"widget_clock2_preview{suffix}.png"
    img.save(os.path.join(OUTPUT_DIR, fname))
    # Also save to night directory for dark mode
    if theme == "dark":
        night_dir = OUTPUT_DIR.replace("drawable-nodpi", "drawable-night-nodpi")
        os.makedirs(night_dir, exist_ok=True)
        img.save(os.path.join(night_dir, "widget_clock2_preview.png"))
    print(f"✅ {fname}")


# ═══════════════════════════════════════════════════════════════════
# 5. CALENDAR MONTH WIDGET  (4×4)  — Light + Dark
# ═══════════════════════════════════════════════════════════════════

def create_month_preview(theme="light"):
    """4×4 Month Calendar widget preview"""
    p = LIGHT if theme == "light" else DARK
    w, h = 800, 800
    img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Body background
    draw.rounded_rectangle([(0, 0), (w-1, h-1)], radius=40,
                           fill=p["bg"], outline=p["bg_outline"], width=1)

    # Header
    if theme == "light":
        header_bg = (183, 28, 28)
    else:
        header_bg = (169, 50, 38)  # #A93226 matching dark header
    draw.rounded_rectangle([(0, 0), (w-1, 100)], radius=40, fill=header_bg)
    draw.rectangle([(0, 50), (w-1, 100)], fill=header_bg)

    f = _load_fonts({"title": 32, "sub": 18, "dow": 18, "cell": 18, "lunar": 13, "badge": 20})

    draw.text((24, 20), "Tháng 4, 2026", fill=(255, 255, 255), font=f["title"])
    draw.text((24, 60), "Tháng 2 Âm lịch · Bính Ngọ", fill=(255, 215, 0, 230), font=f["sub"])

    # "Hôm nay" badge
    draw.rounded_rectangle([(w-150, 35), (w-20, 70)], radius=16, fill=(255, 255, 255, 32))
    draw.text((w-140, 40), "Hôm nay", fill=(255, 255, 255, 204), font=f["sub"])

    # Weekday header
    days_label = ["T2", "T3", "T4", "T5", "T6", "T7", "CN"]
    cell_w = (w - 40) // 7
    y_start = 120
    for i, d in enumerate(days_label):
        x = 20 + i * cell_w + cell_w // 2
        if theme == "light":
            color = (183, 28, 28) if i >= 5 else p["text_tertiary"]
        else:
            color = (255, 180, 180) if i >= 5 else p["text_tertiary"]
        draw.text((x, y_start), d, fill=color, font=f["dow"], anchor="mt")

    # Grid data (solar, lunar, is_current_month, is_today)
    # April 2026: ngày 8 = today (Thứ Tư)
    grid = [
        [(30,"12",False,False),(31,"13",False,False),(1,"14",True,False),(2,"15",True,False),(3,"16",True,False),(4,"17",True,False),(5,"18",True,False)],
        [(6,"19",True,False),(7,"20",True,False),(8,"21",True,True),(9,"22",True,False),(10,"23",True,False),(11,"24",True,False),(12,"25",True,False)],
        [(13,"26",True,False),(14,"27",True,False),(15,"28",True,False),(16,"29",True,False),(17,"1/3",True,False),(18,"2",True,False),(19,"3",True,False)],
        [(20,"4",True,False),(21,"5",True,False),(22,"6",True,False),(23,"7",True,False),(24,"8",True,False),(25,"9",True,False),(26,"10",True,False)],
        [(27,"11",True,False),(28,"12",True,False),(29,"13",True,False),(30,"14",True,False),(1,"15",False,False),(2,"16",False,False),(3,"17",False,False)],
    ]

    row_h = 110
    y_grid = 150

    for row_idx, row in enumerate(grid):
        y = y_grid + row_idx * row_h
        for col_idx, (solar, lunar, is_cur, is_today) in enumerate(row):
            x = 20 + col_idx * cell_w + cell_w // 2

            if is_today:
                today_bg = (183, 28, 28) if theme == "light" else (255, 255, 255)
                draw.rounded_rectangle([(x-22, y+2), (x+22, y+row_h-8)], radius=12, fill=today_bg)
                if theme == "light":
                    solar_color = (255, 255, 255)
                    lunar_color = (255, 255, 255, 179)
                else:
                    solar_color = (127, 29, 29)
                    lunar_color = (127, 29, 29, 179)
            elif not is_cur:
                if theme == "light":
                    solar_color = (153, 153, 153)
                    lunar_color = (153, 153, 153, 128)
                else:
                    solar_color = (255, 255, 255, 77)
                    lunar_color = (255, 255, 255, 51)
            elif col_idx >= 5:
                if theme == "light":
                    solar_color = (183, 28, 28)
                    lunar_color = (183, 28, 28, 128)
                else:
                    solar_color = (255, 180, 180)
                    lunar_color = (255, 180, 180, 128)
            else:
                solar_color = p["text_primary"]
                lunar_color = p["text_tertiary"]

            draw.text((x, y + 15), str(solar), fill=solar_color, font=f["cell"], anchor="mt")
            draw.text((x, y + 40), lunar, fill=lunar_color, font=f["lunar"], anchor="mt")

    suffix = "" if theme == "light" else "_dark"
    fname = f"widget_month_preview{suffix}.png"
    img.save(os.path.join(OUTPUT_DIR, fname))
    if theme == "dark":
        night_dir = OUTPUT_DIR.replace("drawable-nodpi", "drawable-night-nodpi")
        os.makedirs(night_dir, exist_ok=True)
        img.save(os.path.join(night_dir, "widget_month_preview.png"))
    print(f"✅ {fname}")


# ═══════════════════════════════════════════════════════════════════
#  MAIN
# ═══════════════════════════════════════════════════════════════════

if __name__ == "__main__":
    print("🖼  Generating widget preview images...\n")

    # 1. Calendar Day — light + dark
    create_calendar_preview("light")
    create_calendar_preview("dark")

    # 2. AI Chat — light + dark
    create_ai_preview("light")
    create_ai_preview("dark")

    # 3. Clock Glass — chỉ 1 mode (transparent)
    create_clock_preview()

    # 4. Clock2 Sáng/Tối — light + dark
    create_clock2_preview("light")
    create_clock2_preview("dark")

    # 5. Calendar Month — light + dark
    create_month_preview("light")
    create_month_preview("dark")

    print("\n🎉 All 9 preview images generated!")
    print(f"📂 Output: {OUTPUT_DIR}/")
