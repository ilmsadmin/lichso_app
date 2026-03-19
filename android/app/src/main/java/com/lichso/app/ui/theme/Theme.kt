package com.lichso.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LichSoDarkColorScheme = darkColorScheme(
    primary = DarkColors.gold,
    onPrimary = DarkColors.bg,
    primaryContainer = DarkColors.goldDim,
    onPrimaryContainer = DarkColors.gold2,
    secondary = DarkColors.teal,
    onSecondary = DarkColors.bg,
    secondaryContainer = DarkColors.tealDim,
    onSecondaryContainer = DarkColors.teal2,
    error = DarkColors.red,
    onError = DarkColors.textPrimary,
    errorContainer = DarkColors.red2,
    background = DarkColors.bg,
    onBackground = DarkColors.textPrimary,
    surface = DarkColors.bg2,
    onSurface = DarkColors.textPrimary,
    surfaceVariant = DarkColors.bg3,
    onSurfaceVariant = DarkColors.textSecondary,
    outline = DarkColors.border,
    outlineVariant = DarkColors.textQuaternary,
)

private val LichSoLightColorScheme = lightColorScheme(
    primary = LightColors.gold,
    onPrimary = Color.White,
    primaryContainer = LightColors.goldDim,
    onPrimaryContainer = LightColors.gold2,
    secondary = LightColors.teal,
    onSecondary = Color.White,
    secondaryContainer = LightColors.tealDim,
    onSecondaryContainer = LightColors.teal2,
    error = LightColors.red,
    onError = Color.White,
    errorContainer = LightColors.red2,
    background = LightColors.bg,
    onBackground = LightColors.textPrimary,
    surface = LightColors.bg2,
    onSurface = LightColors.textPrimary,
    surfaceVariant = LightColors.bg3,
    onSurfaceVariant = LightColors.textSecondary,
    outline = LightColors.border,
    outlineVariant = LightColors.textQuaternary,
)

@Composable
fun LichSoTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val materialScheme = if (darkTheme) LichSoDarkColorScheme else LichSoLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.bg.toArgb()
            window.navigationBarColor = colors.bg2.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalLichSoColors provides colors) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = LichSoTypography,
            content = content
        )
    }
}
