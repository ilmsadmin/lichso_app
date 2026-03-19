package com.lichso.app.ui.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════
// LỊCH SỐ — Design System Colors
// Dual Theme — Dark (Premium Gold) & Light
// ══════════════════════════════════════════

/**
 * Holds all custom colors used throughout the app.
 * Access via [LocalLichSoColors].
 */
@Stable
data class LichSoColors(
    val bg: Color,
    val bg2: Color,
    val bg3: Color,
    val bg4: Color,
    val surface: Color,
    val surface2: Color,
    val border: Color,
    val gold: Color,
    val gold2: Color,
    val goldDim: Color,
    val teal: Color,
    val teal2: Color,
    val tealDim: Color,
    val red: Color,
    val red2: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textQuaternary: Color,
    val noteGold: Color,
    val noteTeal: Color,
    val noteOrange: Color,
    val notePurple: Color,
    val noteGreen: Color,
    val noteRed: Color,
    val isDark: Boolean,
)

// ── Dark palette ──
val DarkColors = LichSoColors(
    bg = Color(0xFF0F0E0C),
    bg2 = Color(0xFF181610),
    bg3 = Color(0xFF211F1A),
    bg4 = Color(0xFF2A2720),
    surface = Color(0xFF2E2B23),
    surface2 = Color(0xFF363228),
    border = Color(0x1EFFDC64),
    gold = Color(0xFFE8C84A),
    gold2 = Color(0xFFF5D96E),
    goldDim = Color(0x2EE8C84A),
    teal = Color(0xFF4ABEAA),
    teal2 = Color(0xFF62D4C0),
    tealDim = Color(0x264ABEAA),
    red = Color(0xFFD94F3B),
    red2 = Color(0xFFE8614E),
    textPrimary = Color(0xFFF0E8D0),
    textSecondary = Color(0xFFB8AA88),
    textTertiary = Color(0xFF7A6E52),
    textQuaternary = Color(0xFF4A4435),
    noteGold = Color(0xFFE8C84A),
    noteTeal = Color(0xFF4ABEAA),
    noteOrange = Color(0xFFE8A06A),
    notePurple = Color(0xFFA084DC),
    noteGreen = Color(0xFF78C47A),
    noteRed = Color(0xFFE87070),
    isDark = true,
)

// ── Light palette ──
val LightColors = LichSoColors(
    bg = Color(0xFFF8F6F1),
    bg2 = Color(0xFFFFFFFF),
    bg3 = Color(0xFFF0EDE6),
    bg4 = Color(0xFFE8E4DB),
    surface = Color(0xFFEAE6DD),
    surface2 = Color(0xFFDDD8CD),
    border = Color(0x1A8B7A4A),
    gold = Color(0xFFC4A020),
    gold2 = Color(0xFFAA8A10),
    goldDim = Color(0x1EC4A020),
    teal = Color(0xFF2E9A88),
    teal2 = Color(0xFF1E8070),
    tealDim = Color(0x1A2E9A88),
    red = Color(0xFFC43D2B),
    red2 = Color(0xFFD04838),
    textPrimary = Color(0xFF1A1710),
    textSecondary = Color(0xFF5C5340),
    textTertiary = Color(0xFF8A7F68),
    textQuaternary = Color(0xFFB8AE98),
    noteGold = Color(0xFFC4A020),
    noteTeal = Color(0xFF2E9A88),
    noteOrange = Color(0xFFCC7B3A),
    notePurple = Color(0xFF7B5EB0),
    noteGreen = Color(0xFF4A9C4E),
    noteRed = Color(0xFFCC4040),
    isDark = false,
)

val LocalLichSoColors = staticCompositionLocalOf { DarkColors }

/**
 * Convenience accessor – use `LichSoThemeColors.current` anywhere in a @Composable.
 */
object LichSoThemeColors {
    val current: LichSoColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLichSoColors.current
}

// ══════════════════════════════════════════
// Legacy top-level vals for backward compat
// These reference the DARK palette directly.
// Prefer LichSoThemeColors.current in new code.
// ══════════════════════════════════════════
val Bg = DarkColors.bg
val Bg2 = DarkColors.bg2
val Bg3 = DarkColors.bg3
val Bg4 = DarkColors.bg4
val Surface = DarkColors.surface
val Surface2 = DarkColors.surface2
val Border = DarkColors.border
val Gold = DarkColors.gold
val Gold2 = DarkColors.gold2
val GoldDim = DarkColors.goldDim
val Teal = DarkColors.teal
val Teal2 = DarkColors.teal2
val TealDim = DarkColors.tealDim
val Red = DarkColors.red
val Red2 = DarkColors.red2
val TextPrimary = DarkColors.textPrimary
val TextSecondary = DarkColors.textSecondary
val TextTertiary = DarkColors.textTertiary
val TextQuaternary = DarkColors.textQuaternary
val NoteGold = DarkColors.noteGold
val NoteTeal = DarkColors.noteTeal
val NoteOrange = DarkColors.noteOrange
val NotePurple = DarkColors.notePurple
val NoteGreen = DarkColors.noteGreen
val NoteRed = DarkColors.noteRed
