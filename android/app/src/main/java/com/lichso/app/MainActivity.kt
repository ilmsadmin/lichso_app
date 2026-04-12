package com.lichso.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import com.lichso.app.ui.LichSoMainScreen
import com.lichso.app.ui.screen.onboarding.OnboardingScreen
import com.lichso.app.ui.screen.settings.SettingsKeys
import com.lichso.app.ui.screen.settings.settingsDataStore
import com.lichso.app.ui.screen.splash.SplashScreen
import com.lichso.app.ui.theme.LichSoTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private enum class AppScreen { SPLASH, ONBOARDING, MAIN }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Switch from splash theme (red background) to normal theme
        setTheme(R.style.Theme_LichSo)
        // Edge-to-edge with fully transparent system bars
        // Using SystemBarStyle to avoid deprecated setStatusBarColor/setNavigationBarColor
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )

        // Determine if launched from widget with a specific destination
        val widgetRoute = when (intent?.action) {
            "OPEN_AI_CHAT" -> "chat"
            else -> null
        }

        setContent {
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            val themeMode by context.settingsDataStore.data
                .map { it[SettingsKeys.THEME_MODE] ?: "system" }
                .collectAsState(initial = "system")

            val systemDark = isSystemInDarkTheme()
            val darkMode = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }

            // Track which screen to show
            var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }

            // Read onboarding state once on first composition
            var onboardingCompleted by remember { mutableStateOf<Boolean?>(null) }
            LaunchedEffect(Unit) {
                val prefs = context.settingsDataStore.data.first()
                onboardingCompleted = prefs[SettingsKeys.ONBOARDING_COMPLETED] ?: false
            }

            LichSoTheme(darkTheme = darkMode) {
                when (currentScreen) {
                    AppScreen.SPLASH -> {
                        SplashScreen(
                            onSplashFinished = {
                                currentScreen = when (onboardingCompleted) {
                                    true -> AppScreen.MAIN
                                    false -> AppScreen.ONBOARDING
                                    // null = still loading from DataStore, default to MAIN to avoid blank screen
                                    null -> AppScreen.MAIN
                                }
                            }
                        )
                    }

                    AppScreen.ONBOARDING -> {
                        OnboardingScreen(
                            onFinish = {
                                // Mark onboarding as completed
                                coroutineScope.launch {
                                    context.settingsDataStore.edit { prefs ->
                                        prefs[SettingsKeys.ONBOARDING_COMPLETED] = true
                                    }
                                }
                                currentScreen = AppScreen.MAIN
                            }
                        )
                    }

                    AppScreen.MAIN -> {
                        LichSoMainScreen(
                            modifier = Modifier.fillMaxSize(),
                            initialRoute = widgetRoute ?: "home"
                        )
                    }
                }
            }
        }
    }
}
