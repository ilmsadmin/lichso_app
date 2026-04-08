package com.lichso.app.ui.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════
// LỊCH SỐ — Design System Colors v2
// Vietnamese Red & Gold — Material 3
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
    // v2 Material 3 additions
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val outline: Color,
    val outlineVariant: Color,
    val deepRed: Color,
    val goodGreen: Color,
    val badRed: Color,
    val neutralAmber: Color,
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
    red = Color(0xFFEF5350),
    red2 = Color(0xFFE57373),
    textPrimary = Color(0xFFF0E8D0),
    textSecondary = Color(0xFFB8AA88),
    textTertiary = Color(0xFF8A7E62),
    textQuaternary = Color(0xFF4A4435),
    noteGold = Color(0xFFE8C84A),
    noteTeal = Color(0xFF4ABEAA),
    noteOrange = Color(0xFFE8A06A),
    notePurple = Color(0xFFA084DC),
    noteGreen = Color(0xFF78C47A),
    noteRed = Color(0xFFE87070),
    isDark = true,
    primary = Color(0xFFEF5350),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3D1515),
    onPrimaryContainer = Color(0xFFFFDAD6),
    surfaceContainer = Color(0xFF2A2720),
    surfaceContainerHigh = Color(0xFF363228),
    outline = Color(0xFF9E9080),
    outlineVariant = Color(0xFF5A4F42),
    deepRed = Color(0xFFCF6679),
    goodGreen = Color(0xFF81C784),
    badRed = Color(0xFFEF5350),
    neutralAmber = Color(0xFFFFD54F),
)

// ── Light palette (Vietnamese Red & Gold — Material 3) ──
val LightColors = LichSoColors(
    bg = Color(0xFFFFFBF5),
    bg2 = Color(0xFFFFF8F0),
    bg3 = Color(0xFFF5DDD8),
    bg4 = Color(0xFFFFF0E8),
    surface = Color(0xFFFFF8F0),
    surface2 = Color(0xFFF5DDD8),
    border = Color(0x22D8C2BF),
    gold = Color(0xFFD4A017),
    gold2 = Color(0xFFC6A300),
    goldDim = Color(0x1ED4A017),
    teal = Color(0xFF006C4C),
    teal2 = Color(0xFF006C4C),
    tealDim = Color(0x1A006C4C),
    red = Color(0xFFB71C1C),
    red2 = Color(0xFFC62828),
    textPrimary = Color(0xFF1C1B1F),
    textSecondary = Color(0xFF534340),
    textTertiary = Color(0xFF857371),
    textQuaternary = Color(0xFFD8C2BF),
    noteGold = Color(0xFFD4A017),
    noteTeal = Color(0xFF006C4C),
    noteOrange = Color(0xFFE65100),
    notePurple = Color(0xFF7B1FA2),
    noteGreen = Color(0xFF2E7D32),
    noteRed = Color(0xFFC62828),
    isDark = false,
    primary = Color(0xFFB71C1C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    surfaceContainer = Color(0xFFFFF8F0),
    surfaceContainerHigh = Color(0xFFFFF0E8),
    outline = Color(0xFF857371),
    outlineVariant = Color(0xFFD8C2BF),
    deepRed = Color(0xFF8B0000),
    goodGreen = Color(0xFF2E7D32),
    badRed = Color(0xFFC62828),
    neutralAmber = Color(0xFFF57F17),
)

val LocalLichSoColors = staticCompositionLocalOf { LightColors }

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
// Now reference LIGHT palette as default.
// Prefer LichSoThemeColors.current in new code.
// ══════════════════════════════════════════
val Bg = LightColors.bg
val Bg2 = LightColors.bg2
val Bg3 = LightColors.bg3
val Bg4 = LightColors.bg4
val Surface = LightColors.surface
val Surface2 = LightColors.surface2
val Border = LightColors.border
val Gold = LightColors.gold
val Gold2 = LightColors.gold2
val GoldDim = LightColors.goldDim
val Teal = LightColors.teal
val Teal2 = LightColors.teal2
val TealDim = LightColors.tealDim
val Red = LightColors.red
val Red2 = LightColors.red2
val TextPrimary = LightColors.textPrimary
val TextSecondary = LightColors.textSecondary
val TextTertiary = LightColors.textTertiary
val TextQuaternary = LightColors.textQuaternary
val NoteGold = LightColors.noteGold
val NoteTeal = LightColors.noteTeal
val NoteOrange = LightColors.noteOrange
val NotePurple = LightColors.notePurple
val NoteGreen = LightColors.noteGreen
val NoteRed = LightColors.noteRed
