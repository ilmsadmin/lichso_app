#!/usr/bin/env python3
"""
Generate widget preview PNG images from Android layout XML files.
Uses Android's layoutlib or simply creates colored preview images.
"""
import subprocess
import os
import sys

# We'll use Android's built-in screenshot capability via adb
# But since the device might not be connected, let's create simple preview images
# using PIL/Pillow

try:
    from PIL import Image, ImageDraw, ImageFont
except ImportError:
    print("Installing Pillow...")
    subprocess.check_call([sys.executable, "-m", "pip", "install", "Pillow"])
    from PIL import Image, ImageDraw, ImageFont

OUTPUT_DIR = "app/src/main/res/drawable-nodpi"
os.makedirs(OUTPUT_DIR, exist_ok=True)

def create_clock_preview():
    """4x2 Clock & Weather widget preview - dark glassmorphism style"""
    w, h = 800, 400
    img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Rounded rectangle background (glassmorphism dark)
    draw.rounded_rectangle([(0, 0), (w-1, h-1)], radius=56, fill=(0, 0, 0, 153), outline=(255, 255, 255, 48), width=2)
    
    # Left section
    try:
        font_time = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 96)
        font_date = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 28)
        font_lunar = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 22)
        font_weather_icon = ImageFont.truetype("/System/Library/Fonts/Apple Color Emoji.ttc", 64)
        font_temp = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 52)
        font_desc = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 22)
        font_range = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 16)
        font_badge = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 20)
    except:
        font_time = ImageFont.load_default()
        font_date = font_lunar = font_desc = font_range = font_badge = font_time
        font_weather_icon = font_temp = font_time
    
    # Time
    draw.text((32, 40), "14:30", fill=(255, 255, 255), font=font_time)
    # Solar date
    draw.text((32, 155), "Thứ Ba, 07/04/2026", fill=(255, 255, 255, 230), font=font_date)
    # Lunar
    draw.text((32, 190), "☽ 20/02 Âm · Bính Dần", fill=(255, 215, 0, 230), font=font_lunar)
    
    # Divider
    draw.line([(w//2, 40), (w//2, h-80)], fill=(255, 255, 255, 51), width=2)
    
    # Right section - weather
    draw.text((w//2 + 60, 40), "⛅", fill=(255, 255, 255), font=font_weather_icon)
    draw.text((w//2 + 60, 130), "28°", fill=(255, 255, 255), font=font_temp)
    draw.text((w//2 + 60, 195), "Có mây", fill=(255, 255, 255, 179), font=font_desc)
    draw.text((w//2 + 60, 225), "💧 75%  💨 12 km/h", fill=(255, 255, 255, 128), font=font_range)
    
    # Badge
    draw.rounded_rectangle([(24, h-60), (240, h-24)], radius=24, fill=(255, 255, 255, 32))
    draw.text((36, h-56), "✦ Ngày Hoàng Đạo", fill=(129, 199, 132), font=font_badge)
    
    img.save(os.path.join(OUTPUT_DIR, "widget_clock_preview.png"))
    print("✅ widget_clock_preview.png")


def create_calendar_preview():
    """2x2 Calendar day widget preview - light theme"""
    w, h = 400, 400
    img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # White rounded rect background
    draw.rounded_rectangle([(0, 0), (w-1, h-1)], radius=40, fill=(255, 251, 248), outline=(216, 194, 191, 34), width=1)
    
    try:
        font_dow = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 24)
        font_day = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 96)
        font_month = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 22)
        font_lunar = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 22)
        font_canchi = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 20)
        font_rating = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 20)
    except:
        font_dow = font_day = font_month = font_lunar = font_canchi = font_rating = ImageFont.load_default()
    
    cx = w // 2
    # Day of week
    draw.text((cx, 40), "Thứ Ba", fill=(133, 115, 113), font=font_dow, anchor="mt")
    # Large day number
    draw.text((cx, 80), "07", fill=(183, 28, 28), font=font_day, anchor="mt")
    # Month year
    draw.text((cx, 190), "tháng 4 · 2026", fill=(83, 67, 64), font=font_month, anchor="mt")
    # Divider
    draw.line([(cx-50, 225), (cx+50, 225)], fill=(216, 194, 191, 34), width=2)
    # Lunar
    draw.text((cx, 240), "20 · tháng 2 Âm", fill=(212, 160, 23), font=font_lunar, anchor="mt")
    # Can chi
    draw.text((cx, 275), "Bính Dần · Ất Tỵ", fill=(83, 67, 64), font=font_canchi, anchor="mt")
    # Rating
    draw.text((cx, 315), "✦ Ngày Hoàng Đạo", fill=(46, 125, 50), font=font_rating, anchor="mt")
    
    img.save(os.path.join(OUTPUT_DIR, "widget_calendar_preview.png"))
    print("✅ widget_calendar_preview.png")


def create_ai_preview():
    """3x2 AI Chat widget preview"""
    w, h = 600, 400
    img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Card background
    draw.rounded_rectangle([(0, 0), (w-1, h-1)], radius=40, fill=(255, 251, 248), outline=(216, 194, 191, 34), width=1)
    
    # Red header
    draw.rounded_rectangle([(0, 0), (w-1, 80)], radius=40, fill=(183, 28, 28))
    draw.rectangle([(0, 40), (w-1, 80)], fill=(183, 28, 28))
    
    try:
        font_title = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 24)
        font_status = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 16)
        font_bubble = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 20)
        font_input = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 20)
    except:
        font_title = font_status = font_bubble = font_input = ImageFont.load_default()
    
    # Header text
    draw.ellipse([(20, 20), (60, 60)], fill=(255, 255, 255, 51))
    draw.text((40, 32), "✦", fill=(255, 213, 79), font=font_status, anchor="mt")
    draw.text((72, 22), "Lịch Số AI", fill=(255, 255, 255), font=font_title)
    draw.text((72, 50), "● Đang hoạt động", fill=(255, 255, 255, 153), font=font_status)
    draw.text((w-40, 40), "7/4", fill=(255, 255, 255, 204), font=font_status, anchor="mt")
    
    # AI bubble
    draw.rounded_rectangle([(20, 100), (w-60, 160)], radius=20, fill=(243, 237, 235))
    draw.text((32, 110), "✦ Hôm nay Ngày Hoàng Đạo,", fill=(28, 27, 31), font=font_bubble)
    draw.text((32, 135), "hướng tài lộc Đông Nam", fill=(28, 27, 31), font=font_bubble)
    
    # User bubble
    draw.rounded_rectangle([(w-220, 175), (w-20, 215)], radius=20, fill=(183, 28, 28))
    draw.text((w-210, 185), "Hỏi AI tử vi ›", fill=(255, 255, 255), font=font_bubble)
    
    # Input bar
    draw.rounded_rectangle([(20, h-60), (w-80, h-20)], radius=16, fill=(243, 237, 235))
    draw.text((36, h-50), "Hỏi gì đó...", fill=(133, 115, 113), font=font_input)
    draw.rounded_rectangle([(w-60, h-60), (w-20, h-20)], radius=16, fill=(183, 28, 28))
    draw.text((w-40, h-48), "›", fill=(255, 255, 255), font=font_title, anchor="mt")
    
    img.save(os.path.join(OUTPUT_DIR, "widget_ai_preview.png"))
    print("✅ widget_ai_preview.png")


def create_month_preview():
    """4x4 Month Calendar widget preview"""
    w, h = 800, 800
    img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Background
    draw.rounded_rectangle([(0, 0), (w-1, h-1)], radius=40, fill=(255, 251, 248), outline=(216, 194, 191, 34), width=1)
    
    # Red header
    draw.rounded_rectangle([(0, 0), (w-1, 100)], radius=40, fill=(183, 28, 28))
    draw.rectangle([(0, 50), (w-1, 100)], fill=(183, 28, 28))
    
    try:
        font_title = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 32)
        font_sub = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 18)
        font_dow = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 18)
        font_cell = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 18)
        font_cell_lunar = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 13)
        font_badge = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 20)
    except:
        font_title = font_sub = font_dow = font_cell = font_cell_lunar = font_badge = ImageFont.load_default()
    
    # Header
    draw.text((24, 20), "Tháng 4, 2026", fill=(255, 255, 255), font=font_title)
    draw.text((24, 60), "Tháng 2 Âm lịch · Bính Ngọ", fill=(255, 215, 0, 230), font=font_sub)
    
    # "Hôm nay" badge
    draw.rounded_rectangle([(w-150, 35), (w-20, 70)], radius=16, fill=(255, 255, 255, 32))
    draw.text((w-140, 40), "Hôm nay", fill=(255, 255, 255, 204), font=font_sub)
    
    # Weekday header
    days = ["T2", "T3", "T4", "T5", "T6", "T7", "CN"]
    cell_w = (w - 40) // 7
    y_start = 120
    for i, d in enumerate(days):
        x = 20 + i * cell_w + cell_w // 2
        color = (183, 28, 28) if i >= 5 else (133, 115, 113)
        draw.text((x, y_start), d, fill=color, font=font_dow, anchor="mt")
    
    # Calendar grid data: (solar, lunar, is_current_month, is_today)
    grid = [
        [(30,"12",False,False),(31,"13",False,False),(1,"14",True,False),(2,"15",True,False),(3,"16",True,False),(4,"17",True,False),(5,"18",True,False)],
        [(6,"19",True,False),(7,"20",True,True),(8,"21",True,False),(9,"22",True,False),(10,"23",True,False),(11,"24",True,False),(12,"25",True,False)],
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
                # Today highlight circle
                draw.rounded_rectangle([(x-22, y+2), (x+22, y+row_h-8)], radius=12, fill=(183, 28, 28))
                solar_color = (255, 255, 255)
                lunar_color = (255, 255, 255, 179)
            elif not is_cur:
                solar_color = (153, 153, 153)
                lunar_color = (153, 153, 153, 128)
            elif col_idx >= 5:
                solar_color = (183, 28, 28)
                lunar_color = (183, 28, 28, 128)
            else:
                solar_color = (28, 27, 31)
                lunar_color = (133, 115, 113)
            
            draw.text((x, y + 15), str(solar), fill=solar_color, font=font_cell, anchor="mt")
            draw.text((x, y + 40), lunar, fill=lunar_color, font=font_cell_lunar, anchor="mt")
    
    img.save(os.path.join(OUTPUT_DIR, "widget_month_preview.png"))
    print("✅ widget_month_preview.png")


if __name__ == "__main__":
    create_clock_preview()
    create_calendar_preview()
    create_ai_preview()
    create_month_preview()
    print("\n🎉 All preview images generated!")
