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
import androidx.core.view.WindowInsetsControllerCompat

private val LichSoDarkColorScheme = darkColorScheme(
    primary = Color(0xFFB71C1C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFFD4A017),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF0B3),
    onSecondaryContainer = Color(0xFF3E2E00),
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
    primary = Color(0xFFB71C1C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFFC6A300),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF0B3),
    onSecondaryContainer = Color(0xFF3E2E00),
    tertiary = Color(0xFF006C4C),
    onTertiary = Color.White,
    error = LightColors.red,
    onError = Color.White,
    errorContainer = LightColors.red2,
    background = LightColors.bg,
    onBackground = LightColors.textPrimary,
    surface = LightColors.bg2,
    onSurface = LightColors.textPrimary,
    surfaceVariant = LightColors.bg3,
    onSurfaceVariant = LightColors.textSecondary,
    outline = Color(0xFF857371),
    outlineVariant = Color(0xFFD8C2BF),
)

@Composable
fun LichSoTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val materialScheme = if (darkTheme) LichSoDarkColorScheme else LichSoLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = colors.bg.toArgb()
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
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
