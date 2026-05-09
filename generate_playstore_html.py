import os

IMAGES = [
    "z7804174527177_e95c9543a02cc4a29ad67f5a0b7fe5c4.jpg",
    "z7804174527181_7db617336f98aeb9db7b8feef2bbfcf2.jpg",
    "z7804174527864_71bb1a1c1e16b56f7b0d29692a7f7e87.jpg",
    "z7804174535242_5b198e239b79f5102d14a5e649703e38.jpg",
    "z7804174540564_5be4c2884b8256c9e252fc8230b33817.jpg",
    "z7804174543114_6c748f90d8f2b0f82a1b7ed4c3b11836.jpg",
    "z7804174544254_2c64573ba696264097e23cf1897a7860.jpg",
    "z7804174552767_ebcbe8a0d86152b2682e254e971b77ca.jpg"
]

SCREENS = [
    {"file": "01_lich_van_nien.html", "title": "LỊCH VẠN NIÊN", "sub": "Chính Xác & Chi Tiết Nhất", "theme": "tet", "img": IMAGES[0]},
    {"file": "02_quan_ly.html", "title": "QUẢN LÝ CÔNG VIỆC", "sub": "Nhắc Nhở Theo Lịch Âm Dương", "theme": "gold", "img": IMAGES[1]},
    {"file": "03_tu_vi.html", "title": "TỬ VI & PHONG THUỶ", "sub": "Luận Giải Vận Mệnh Cùng AI", "theme": "mystic", "img": IMAGES[2]},
    {"file": "04_gia_pha.html", "title": "CÂY GIA PHẢ", "sub": "Gìn Giữ Truyền Thống Gia Đình", "theme": "nature", "img": IMAGES[3]},
    {"file": "05_van_khan.html", "title": "VĂN KHẤN CỔ TRUYỀN", "sub": "Đầy Đủ Cho Mọi Dịp Lễ Tết", "theme": "tet", "img": IMAGES[4]},
    {"file": "06_ngay_tot.html", "title": "XEM NGÀY TỐT XẤU", "sub": "Lựa Chọn Thời Điểm Vàng", "theme": "gold", "img": IMAGES[5]},
    {"file": "07_thoi_tiet.html", "title": "DỰ BÁO THỜI TIẾT", "sub": "Cập Nhật Nhanh Chóng Mỗi Ngày", "theme": "mystic", "img": IMAGES[6]},
    {"file": "08_giao_dien.html", "title": "ĐẬM CHẤT VIỆT NAM", "sub": "Thiết Kế Tinh Tế, Giao Diện Đẹp Mắt", "theme": "nature", "img": IMAGES[7]}
]

SVG_CLOUDS = """
<svg class="deco-clouds" viewBox="0 0 800 300" xmlns="http://www.w3.org/2000/svg">
  <path fill="rgba(255, 215, 0, 0.15)" d="M100 200 Q 150 150 200 200 T 300 200 T 400 200 T 500 200 T 600 200 T 700 200" stroke="rgba(255, 215, 0, 0.3)" stroke-width="8" stroke-linecap="round"/>
  <path fill="rgba(255, 255, 255, 0.1)" d="M50 250 Q 120 180 180 250 T 280 250 T 380 250 T 480 250 T 580 250 T 680 250 T 780 250" stroke="rgba(255, 255, 255, 0.2)" stroke-width="12" stroke-linecap="round"/>
</svg>
"""

SVG_DRUM = """
<svg class="deco-drum" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
  <circle cx="100" cy="100" r="90" fill="none" stroke="rgba(255, 215, 0, 0.2)" stroke-width="4"/>
  <circle cx="100" cy="100" r="70" fill="none" stroke="rgba(255, 215, 0, 0.15)" stroke-width="2" stroke-dasharray="5 5"/>
  <polygon points="100,20 115,85 180,100 115,115 100,180 85,115 20,100 85,85" fill="rgba(255, 215, 0, 0.2)"/>
  <circle cx="100" cy="100" r="30" fill="none" stroke="rgba(255, 215, 0, 0.3)" stroke-width="2"/>
</svg>
"""

SVG_LANTERN = """
<svg class="deco-lantern" viewBox="0 0 100 150" xmlns="http://www.w3.org/2000/svg">
  <rect x="48" y="0" width="4" height="20" fill="#FFD700"/>
  <ellipse cx="50" cy="50" rx="35" ry="45" fill="rgba(220, 20, 60, 0.8)" stroke="#FFD700" stroke-width="3"/>
  <path d="M50 5 Q 35 50 50 95" fill="none" stroke="#FFD700" stroke-width="1.5"/>
  <path d="M50 5 Q 65 50 50 95" fill="none" stroke="#FFD700" stroke-width="1.5"/>
  <rect x="40" y="95" width="20" height="10" fill="#FFD700"/>
  <path d="M45 105 L45 140 M50 105 L50 145 M55 105 L55 140" stroke="#FFD700" stroke-width="2"/>
</svg>
"""


HTML_TEMPLATE = """<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>{title}</title>
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@700;800&family=Be+Vietnam+Pro:wght@400;500;600&display=swap" rel="stylesheet">
    <style>
        * {{
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }}
        body {{
            width: 1586px;
            height: 1798px;
            overflow: hidden;
            font-family: 'Be Vietnam Pro', sans-serif;
            position: relative;
            display: flex;
            flex-direction: column;
            align-items: center;
        }}
        
        /* Themes */
        .theme-tet {{
            background: radial-gradient(circle at top right, #c62828, #8e0000 70%, #5c0000);
        }}
        .theme-gold {{
            background: radial-gradient(circle at top left, #b8860b, #8b6508 60%, #5c4305);
        }}
        .theme-mystic {{
            background: radial-gradient(circle at top, #28143d, #14081c 80%, #000000);
        }}
        .theme-nature {{
            background: radial-gradient(circle at bottom right, #1b4d3e, #0f2c23 75%, #05100d);
        }}

        /* Decorations */
        .deco-clouds {{
            position: absolute;
            bottom: -50px;
            left: 0;
            width: 200%;
            height: 400px;
            z-index: 1;
            animation: moveClouds 60s linear infinite;
        }}
        .deco-drum {{
            position: absolute;
            top: 5%;
            left: 5%;
            width: 600px;
            height: 600px;
            opacity: 0.4;
            transform: translate(-30%, -30%);
            z-index: 1;
        }}
        .deco-drum-2 {{
            position: absolute;
            bottom: 10%;
            right: 0%;
            width: 800px;
            height: 800px;
            opacity: 0.3;
            transform: translate(30%, 30%);
            z-index: 1;
        }}
        .deco-lantern {{
            position: absolute;
            top: -20px;
            width: 150px;
            z-index: 2;
            filter: drop-shadow(0 0 20px rgba(255, 215, 0, 0.4));
        }}
        .lantern-1 {{ right: 80px; height: 300px; }}
        .lantern-2 {{ right: 260px; height: 200px; top: -50px; }}
        .lantern-3 {{ left: 80px; height: 250px; }}

        /* Pattern overlay */
        .pattern-overlay {{
            position: absolute;
            inset: 0;
            background-image: radial-gradient(rgba(255, 215, 0, 0.05) 2px, transparent 2px);
            background-size: 40px 40px;
            z-index: 1;
        }}

        /* Content */
        .header {{
            position: relative;
            z-index: 10;
            text-align: center;
            margin-top: 140px;
            width: 90%;
            padding: 40px;
        }}
        
        .title {{
            font-family: 'Playfair Display', serif;
            font-size: 110px;
            font-weight: 800;
            color: #FFD700; /* Gold */
            text-transform: uppercase;
            text-shadow: 0 10px 30px rgba(0, 0, 0, 0.5), 0 2px 10px rgba(255, 215, 0, 0.3);
            letter-spacing: 4px;
            line-height: 1.2;
            margin-bottom: 25px;
        }}
        
        .subtitle {{
            font-size: 55px;
            font-weight: 500;
            color: #ffffff;
            text-shadow: 0 4px 15px rgba(0, 0, 0, 0.6);
            letter-spacing: 1px;
            background: linear-gradient(90deg, transparent, rgba(0,0,0,0.4), transparent);
            padding: 10px 40px;
            display: inline-block;
            border-radius: 50px;
        }}

        .phone-container {{
            position: relative;
            z-index: 10;
            margin-top: 80px;
            width: 860px;
            height: 1780px;
            background: #111;
            border-radius: 90px;
            padding: 24px;
            box-shadow: 
                0 60px 120px rgba(0, 0, 0, 0.8),
                0 0 0 4px #444,
                0 0 0 10px #222,
                0 0 0 14px rgba(255, 215, 0, 0.5),
                inset 0 0 20px rgba(255,255,255,0.2);
            display: flex;
            justify-content: center;
            align-items: flex-start;
            overflow: hidden;
            transform: translateY(0);
        }}
        
        .phone-notch {{
            position: absolute;
            top: 24px;
            left: 50%;
            transform: translateX(-50%);
            width: 260px;
            height: 45px;
            background: #111;
            border-bottom-left-radius: 24px;
            border-bottom-right-radius: 24px;
            z-index: 20;
        }}

        .screenshot {{
            width: 100%;
            height: 100%;
            object-fit: cover;
            border-radius: 70px;
            box-shadow: inset 0 0 10px rgba(0,0,0,0.5);
        }}
        
        .glow {{
            position: absolute;
            width: 1200px;
            height: 1200px;
            background: radial-gradient(circle, rgba(255,215,0,0.15) 0%, transparent 60%);
            top: 40%;
            left: 50%;
            transform: translate(-50%, -50%);
            z-index: 2;
            pointer-events: none;
        }}
    </style>
</head>
<body class="theme-{theme}">
    <div class="pattern-overlay"></div>
    {drum}
    {drum2}
    {clouds}
    {lantern1}
    {lantern2}
    {lantern3}
    <div class="glow"></div>
    
    <div class="header">
        <div class="title">{title}</div>
        <div class="subtitle">{sub}</div>
    </div>
    
    <div class="phone-container">
        <div class="phone-notch"></div>
        <img class="screenshot" src="../Ảnh Lịch Số/{img}" alt="Screenshot">
    </div>
</body>
</html>
"""

def generate():
    os.makedirs("playstore_screenshots_vi", exist_ok=True)
    
    for s in SCREENS:
        drum2 = SVG_DRUM.replace('deco-drum', 'deco-drum-2')
        lantern1 = SVG_LANTERN.replace('deco-lantern', 'deco-lantern lantern-1') if s['theme'] in ['tet', 'gold'] else ''
        lantern2 = SVG_LANTERN.replace('deco-lantern', 'deco-lantern lantern-2') if s['theme'] in ['tet', 'gold'] else ''
        lantern3 = SVG_LANTERN.replace('deco-lantern', 'deco-lantern lantern-3') if s['theme'] == 'tet' else ''
        
        html_content = HTML_TEMPLATE.format(
            title=s['title'],
            sub=s['sub'],
            theme=s['theme'],
            img=s['img'],
            drum=SVG_DRUM,
            drum2=drum2,
            clouds=SVG_CLOUDS,
            lantern1=lantern1,
            lantern2=lantern2,
            lantern3=lantern3
        )
        
        file_path = os.path.join("playstore_screenshots_vi", s['file'])
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(html_content)
        print(f"Generated {file_path}")

if __name__ == "__main__":
    generate()
