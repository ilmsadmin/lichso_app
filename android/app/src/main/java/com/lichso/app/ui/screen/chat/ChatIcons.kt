package com.lichso.app.ui.screen.chat

/**
 * Bộ ký hiệu đồng bộ cho toàn bộ ứng dụng Lịch Số.
 *
 * Dùng các ký tự Unicode nhẹ, thanh lịch, hiển thị đồng nhất
 * trên mọi thiết bị Android mà không phụ thuộc vendor emoji.
 *
 * Phong cách: tối giản, cổ điển phương Đông.
 */
object ChatIcons {
    // ── Lịch & Thời gian ──
    const val CALENDAR    = "◈"   // Ngày dương
    const val LUNAR       = "☽"   // Âm lịch / trăng
    const val CLOCK       = "◷"   // Giờ hoàng đạo
    const val STAR        = "✦"   // Giờ tốt, mục nổi bật

    // ── Can Chi & Phong thuỷ ──
    const val CANCHI      = "⊕"   // Can chi
    const val COMPASS     = "◎"   // Hướng / la bàn
    const val FORTUNE     = "⊛"   // Thần Tài
    const val JOY         = "❖"   // Hỷ Thần
    const val SPARKLE     = "⟡"   // Điểm sáng, giờ tốt list

    // ── Trạng thái ──
    const val CHECK       = "▪"   // Nên làm
    const val CROSS       = "▫"   // Không nên
    const val WARNING     = "△"   // Cảnh báo / ngày xấu / hung thần
    const val INFO        = "◇"   // Thông tin chung

    // ── Giao tiếp ──
    const val GREETING    = "─"   // Chào hỏi (dùng làm divider nhẹ)
    const val SECTION     = "──"  // Ngăn cách section

    // ── Bullet ──
    const val BULLET      = "·"   // Bullet point nhẹ
    const val ARROW       = "›"   // Mũi tên nhỏ
}
