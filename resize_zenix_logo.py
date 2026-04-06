from PIL import Image
import os

source = "/Volumes/DATA/Projects/lichso/v2/Zenix-Logo.png"
base = "/Volumes/DATA/Projects/lichso/android/app/src/main/res"

img = Image.open(source).convert("RGBA")
print(f"Original size: {img.size}")

sizes = {
    "drawable-mdpi": 48,
    "drawable-hdpi": 72,
    "drawable-xhdpi": 96,
    "drawable-xxhdpi": 144,
    "drawable-xxxhdpi": 192,
    "drawable": 96,
}

for folder, size in sizes.items():
    img_copy = img.copy()
    img_copy.thumbnail((size, size), Image.LANCZOS)
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    x = (size - img_copy.width) // 2
    y = (size - img_copy.height) // 2
    canvas.paste(img_copy, (x, y))
    out_path = os.path.join(base, folder, "ic_zenix_logo.png")
    canvas.save(out_path, "PNG")
    print(f"Saved {folder}: {size}x{size} (logo: {img_copy.width}x{img_copy.height})")

print("Done!")
