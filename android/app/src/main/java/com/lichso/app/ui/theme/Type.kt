package com.lichso.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Since we can't bundle custom fonts without res files, use system defaults
// In production, add Be Vietnam Pro and Noto Serif as font resources
val BeVietnamPro = FontFamily.SansSerif
val NotoSerif = FontFamily.Serif

val LichSoTypography = Typography(
    // Page titles
    headlineLarge = TextStyle(
        fontFamily = NotoSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        color = Gold2,
        letterSpacing = 0.3.sp
    ),
    // Hero day number
    displayLarge = TextStyle(
        fontFamily = NotoSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 58.sp,
        color = Gold2,
        letterSpacing = (-2).sp,
        lineHeight = 58.sp
    ),
    // Stat numbers
    displayMedium = TextStyle(
        fontFamily = NotoSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 22.sp
    ),
    // Section labels
    labelSmall = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Bold,
        fontSize = 10.5.sp,
        color = TextTertiary,
        letterSpacing = 1.sp
    ),
    // Card headers
    labelMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 0.8.sp
    ),
    // Body text
    bodyMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        color = TextPrimary
    ),
    // Body secondary
    bodySmall = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = TextSecondary
    ),
    // Small text
    labelLarge = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        color = TextTertiary
    ),
    // Badge text
    titleSmall = TextStyle(
        fontFamily = NotoSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        color = Gold
    ),
    // Nav label
    titleMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        color = TextTertiary
    ),
    // Settings item name
    titleLarge = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        color = TextPrimary
    ),
)
