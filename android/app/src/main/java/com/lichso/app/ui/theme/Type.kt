package com.lichso.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lichso.app.R

// ── Font thương hiệu ──
// Be Vietnam Pro được bundle trong res/font (4 weight tĩnh) — tối ưu cho tiếng Việt,
// dấu thanh cân đối, đọc tốt ở cỡ nhỏ. Đặt làm font mặc định toàn app qua
// LocalTextStyle trong LichSoTheme nên cả những Text dùng TextStyle inline
// (không khai báo fontFamily) cũng kế thừa font này.
val BeVietnamPro = FontFamily(
    Font(R.font.be_vietnam_pro_regular, FontWeight.Normal),
    Font(R.font.be_vietnam_pro_medium, FontWeight.Medium),
    Font(R.font.be_vietnam_pro_semibold, FontWeight.SemiBold),
    Font(R.font.be_vietnam_pro_bold, FontWeight.Bold),
)

// Serif hệ thống cho vài tiêu đề/điểm nhấn (số ngày, headline lịch).
val NotoSerif = FontFamily.Serif

// ── Type scale ──
// LƯU Ý: KHÔNG nướng `color` vào Typography — màu chữ phải lấy theo theme
// (onSurface / LocalContentColor) để hoạt động đúng ở cả light & dark mode.
// (Bug cũ: nướng màu sáng vào đây làm chữ sai màu khi dùng MaterialTheme.typography ở dark.)
// Cỡ chữ nền được nâng lên so với bản cũ để dễ đọc cho nhóm người dùng lớn tuổi.
val LichSoTypography = Typography(
    // Page titles
    headlineLarge = TextStyle(
        fontFamily = NotoSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        letterSpacing = 0.3.sp
    ),
    // Hero day number
    displayLarge = TextStyle(
        fontFamily = NotoSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 58.sp,
        letterSpacing = (-2).sp,
        lineHeight = 58.sp
    ),
    // Stat numbers
    displayMedium = TextStyle(
        fontFamily = NotoSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 26.sp
    ),
    // Section labels
    labelSmall = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Bold,
        fontSize = 11.5.sp,
        letterSpacing = 0.8.sp
    ),
    // Card headers
    labelMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 0.6.sp
    ),
    // Body text
    bodyMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    // Body secondary
    bodySmall = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    // Small text
    labelLarge = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp
    ),
    // Badge text
    titleSmall = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp
    ),
    // Nav label
    titleMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp
    ),
    // Settings item name
    titleLarge = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    ),
)
