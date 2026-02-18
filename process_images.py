#!/usr/bin/env python3
"""
BikeCheck — foto-verwerking voor Android
=========================================
Verwerkt de vier fietsfoto's naar het juiste formaat voor de kaartweergave.

Vereisten: pip install Pillow

Gebruik:
  python process_images.py

Daarna: kopieer de bestanden uit 'android_ready\' naar:
  app\src\main\res\drawable\
en verwijder de gelijknamige .xml bestanden (bike_bg_*.xml)
"""

try:
    from PIL import Image, ImageFilter, ImageOps
except ImportError:
    print("Pillow niet gevonden. Installeer met:  pip install Pillow")
    raise SystemExit(1)

import os

# ─── Invoer-paden (pas aan indien nodig) ──────────────────────────────────────
INPUT_DIR = r"C:\Users\JFR\OneDrive - DGMR\050 notebook LM\BikeCheckApp\images"

IMAGES = {
    "bike_ultimate.jpg": "bike_bg_ultimate.jpg",   # Donker, carbon race fiets
    "bike_Atalaya.jpg":  "bike_bg_atalaya.jpg",    # Zilver/titanium gravel fiets
    "bike_Neuron.jpg":   "bike_bg_neuron.jpg",     # Crème/wit MTB fully
    "bike_exceed.jpg":   "bike_bg_exceed.jpg",     # Donker MTB hardtail
}

# ─── Output-instellingen ──────────────────────────────────────────────────────
# 5:3 verhouding — past goed in de 150dp-hoge kaarten bij elk schermformaat
OUTPUT_WIDTH  = 900
OUTPUT_HEIGHT = 540
JPEG_QUALITY  = 88

OUTPUT_DIR = os.path.join(INPUT_DIR, "android_ready")

# ─── Verwerking ───────────────────────────────────────────────────────────────
def center_crop(img: Image.Image, target_w: int, target_h: int) -> Image.Image:
    """Snijd vanuit het midden bij naar de gewenste verhouding."""
    w, h = img.size
    target_ratio = target_w / target_h

    if (w / h) > target_ratio:
        # Te breed → crop links/rechts
        new_w = int(h * target_ratio)
        left = (w - new_w) // 2
        img = img.crop((left, 0, left + new_w, h))
    else:
        # Te hoog → crop boven/onder (behoud bovenkant iets meer dan midden)
        new_h = int(w / target_ratio)
        top = max(0, (h - new_h) // 2 - h // 12)   # licht naar boven verschoven
        img = img.crop((0, top, w, top + new_h))

    return img


def process_image(src_path: str, dst_path: str) -> None:
    img = Image.open(src_path).convert("RGB")
    img = ImageOps.exif_transpose(img)              # correcte rotatie (EXIF)
    img = center_crop(img, OUTPUT_WIDTH, OUTPUT_HEIGHT)
    img = img.resize((OUTPUT_WIDTH, OUTPUT_HEIGHT), Image.LANCZOS)
    img = img.filter(ImageFilter.UnsharpMask(radius=0.7, percent=110, threshold=3))
    img.save(dst_path, "JPEG", quality=JPEG_QUALITY, optimize=True, subsampling=0)


# ─── Hoofd-loop ───────────────────────────────────────────────────────────────
os.makedirs(OUTPUT_DIR, exist_ok=True)

print()
print("BikeCheck foto-verwerking")
print("=" * 40)
print(f"Output-map: {OUTPUT_DIR}")
print()

success = 0
for src_name, dst_name in IMAGES.items():
    src = os.path.join(INPUT_DIR, src_name)
    dst = os.path.join(OUTPUT_DIR, dst_name)

    if not os.path.exists(src):
        print(f"  ✗  Niet gevonden: {src_name}")
        continue

    try:
        process_image(src, dst)
        size_kb = os.path.getsize(dst) // 1024
        print(f"  ✓  {dst_name}   ({OUTPUT_WIDTH}×{OUTPUT_HEIGHT}px, {size_kb} KB)")
        success += 1
    except Exception as e:
        print(f"  ✗  {src_name}: {e}")

print()
print(f"{success}/{len(IMAGES)} foto's verwerkt.")
print()
print("Volgende stappen:")
print("  1. Kopieer alle bestanden uit 'android_ready\' naar:")
print(r"       app\src\main\res\drawable\")
print("  2. Verwijder de oude gradient-placeholders met dezelfde naam:")
print("       bike_bg_ultimate.xml  bike_bg_atalaya.xml")
print("       bike_bg_neuron.xml    bike_bg_exceed.xml")
print("  3. Rebuild het project in Android Studio.")
print()
