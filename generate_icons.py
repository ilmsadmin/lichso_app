#!/usr/bin/env python3
"""
Generate Android app icons and splash logo from the Lịc        square_icon = create_square_with_padding(logo, size,    play_store = create_square_with_padding(logo, 512, padding_ratio=0.0)padding_ratio=0.0) Số logo.
"""
from PIL import Image, ImageDraw
import os
import sys

LOGO_PATH = "/Volumes/DATA/Projects/lichso/v2/lich-so-v2.png"
RES_DIR = "/Volumes/DATA/Projects/lichso/android/app/src/main/res"

# Background color: white — the logo already has its own red/gold colors
BG_COLOR = (255, 255, 255, 255)  # #FFFFFF - white background

def create_square_with_padding(img, target_size, padding_ratio=0.05):
    """Create a square image with the logo centered, with optional padding."""
    max_dim = max(img.width, img.height)
    square = Image.new('RGBA', (max_dim, max_dim), BG_COLOR)
    offset_x = (max_dim - img.width) // 2
    offset_y = (max_dim - img.height) // 2
    square.paste(img, (offset_x, offset_y), img if img.mode == 'RGBA' else None)
    
    if padding_ratio > 0:
        padded_size = int(max_dim / (1 - 2 * padding_ratio))
        padded = Image.new('RGBA', (padded_size, padded_size), BG_COLOR)
        pad_offset = (padded_size - max_dim) // 2
        padded.paste(square, (pad_offset, pad_offset), square)
        square = padded
    
    return square.resize((target_size, target_size), Image.LANCZOS)


def create_round_icon(img, size):
    """Create a round icon version."""
    mask = Image.new('L', (size, size), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse([0, 0, size - 1, size - 1], fill=255)
    
    result = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    result.paste(img, (0, 0), mask)
    return result


def create_adaptive_icon_foreground(img, target_size):
    """
    Adaptive icons: 108dp canvas, 72dp safe zone (18dp padding each side).
    Logo fits in the inner ~62% of the canvas.
    """
    canvas = Image.new('RGBA', (target_size, target_size), (0, 0, 0, 0))
    logo_size = int(target_size * 0.98)
    
    max_dim = max(img.width, img.height)
    square = Image.new('RGBA', (max_dim, max_dim), (0, 0, 0, 0))
    offset_x = (max_dim - img.width) // 2
    offset_y = (max_dim - img.height) // 2
    square.paste(img, (offset_x, offset_y), img if img.mode == 'RGBA' else None)
    
    logo_resized = square.resize((logo_size, logo_size), Image.LANCZOS)
    paste_offset = (target_size - logo_size) // 2
    canvas.paste(logo_resized, (paste_offset, paste_offset), logo_resized)
    
    return canvas


def main():
    print(f"Loading logo from: {LOGO_PATH}")
    logo = Image.open(LOGO_PATH).convert('RGBA')
    print(f"Original size: {logo.width}x{logo.height}")
    
    # 1. Standard launcher icons
    icon_sizes = {
        'mipmap-mdpi': 48,
        'mipmap-hdpi': 72,
        'mipmap-xhdpi': 96,
        'mipmap-xxhdpi': 144,
        'mipmap-xxxhdpi': 192,
    }
    
    for folder, size in icon_sizes.items():
        output_dir = os.path.join(RES_DIR, folder)
        os.makedirs(output_dir, exist_ok=True)
        
        square_icon = create_square_with_padding(logo, size, padding_ratio=0.02)
        square_path = os.path.join(output_dir, 'ic_launcher.png')
        square_icon.convert('RGB').save(square_path, 'PNG', optimize=True)
        print(f"  ✓ {folder}/ic_launcher.png ({size}x{size})")
        
        round_icon = create_round_icon(square_icon, size)
        round_path = os.path.join(output_dir, 'ic_launcher_round.png')
        round_icon.save(round_path, 'PNG', optimize=True)
        print(f"  ✓ {folder}/ic_launcher_round.png ({size}x{size})")
    
    # 2. Adaptive icon foreground (density-specific PNGs)
    adaptive_fg_sizes = {
        'drawable-mdpi': 108,
        'drawable-hdpi': 162,
        'drawable-xhdpi': 216,
        'drawable-xxhdpi': 324,
        'drawable-xxxhdpi': 432,
    }
    
    for folder, size in adaptive_fg_sizes.items():
        output_dir = os.path.join(RES_DIR, folder)
        os.makedirs(output_dir, exist_ok=True)
        
        fg = create_adaptive_icon_foreground(logo, size)
        fg_path = os.path.join(output_dir, 'ic_launcher_foreground.png')
        fg.save(fg_path, 'PNG', optimize=True)
        print(f"  ✓ {folder}/ic_launcher_foreground.png ({size}x{size})")
    
    # 3. Splash screen logo (various densities)
    splash_sizes = {
        'drawable-mdpi': 200,
        'drawable-hdpi': 300,
        'drawable-xhdpi': 400,
        'drawable-xxhdpi': 600,
        'drawable-xxxhdpi': 800,
    }
    
    for folder, size in splash_sizes.items():
        output_dir = os.path.join(RES_DIR, folder)
        os.makedirs(output_dir, exist_ok=True)
        
        ratio = logo.width / logo.height
        if ratio > 1:
            w, h = size, int(size / ratio)
        else:
            w, h = int(size * ratio), size
        
        splash = logo.resize((w, h), Image.LANCZOS)
        splash_path = os.path.join(output_dir, 'splash_logo.png')
        splash.save(splash_path, 'PNG', optimize=True)
        print(f"  ✓ {folder}/splash_logo.png ({w}x{h})")
    
    # General drawable splash logo
    ratio = logo.width / logo.height
    splash_general = logo.resize((600, int(600 / ratio)), Image.LANCZOS)
    splash_general_path = os.path.join(RES_DIR, 'drawable', 'splash_logo.png')
    splash_general.save(splash_general_path, 'PNG', optimize=True)
    print(f"  ✓ drawable/splash_logo.png")
    
    # 4. Play Store icon (512x512)
    play_store = create_square_with_padding(logo, 512, padding_ratio=0.02)
    play_store_path = "/Volumes/DATA/Projects/lichso/android/app/play_store_icon.png"
    play_store.convert('RGB').save(play_store_path, 'PNG', optimize=True)
    print(f"  ✓ play_store_icon.png (512x512)")
    
    print("\n✅ All icons generated successfully!")


if __name__ == '__main__':
    main()
