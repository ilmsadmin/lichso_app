package com.lichso.app

import android.content.Intent
import android.graphics.Color
import android.net.Uri
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
import com.lichso.app.ui.screen.settings.safeSettingsData
import com.lichso.app.ui.screen.settings.settingsDataStore
import com.lichso.app.ui.screen.splash.SplashScreen
import com.lichso.app.ui.theme.LichSoTheme
import com.lichso.app.ui.theme.seasonalPaletteForMonth
import com.lichso.app.update.InAppUpdateManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

private enum class AppScreen { SPLASH, ONBOARDING, MAIN }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Switch from splash theme (red background) to normal theme
        setTheme(R.style.Theme_LichSo)
        // ── In-App Update ──
        // Phải register() TRƯỚC setContent vì ActivityResultLauncher
        // chỉ được phép register khi Activity ở STATE_INITIALIZED hoặc CREATED.
        InAppUpdateManager.register(this)
        InAppUpdateManager.checkForUpdates(this)
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

        // If launched from a background FCM notification that carries a URL (ad/promo),
        // open it in the browser. data["url"] is set by the backend for HTTP(S) click_actions.
        intent?.getStringExtra("url")?.takeIf { it.startsWith("http") }?.let { url ->
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        // All screens navigable from a push notification or external deep link.
        val validRoutes = setOf(
            "home", "calendar", "gooddays", "knowledge_feed", "quiz_home", "chat",
            "tools", "prayers", "history", "profile", "bookmarks", "tasks", "countdown",
            "familytree", "oracle_draw", "daily_store", "ledger", "zodiac_collection",
            "date_picker", "tiet_khi", "date_math", "birth_planner", "cycle_tracker",
            "world_clock", "widget_manager", "leaderboard", "notifications", "search",
            "settings", "bat_trach", "feng_shui_compass", "lo_ban",
        )

        // Determine if launched from a push notification or widget with a specific destination
        val notificationRoute = intent?.getStringExtra("navigate_to")?.let { raw ->
            val route = if (raw == "ai_chat") "chat" else raw
            route.takeIf { it in validRoutes }
        }
        val widgetRoute = when (intent?.action) {
            "OPEN_AI_CHAT" -> "chat"
            "OPEN_CALENDAR_QUICK" -> "calendar"
            else -> null
        }

        // Phase 4 — deep link gift: lichso://streak-gift?token=xxx
        val giftToken = intent?.takeIf { it.action == android.content.Intent.ACTION_VIEW }
            ?.data
            ?.takeIf { it.scheme == "lichso" && it.host == "streak-gift" }
            ?.getQueryParameter("token")

        setContent {
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            val themeModeFlow = remember(context) {
                context.safeSettingsData
                    .map { it[SettingsKeys.THEME_MODE] ?: "system" }
            }
            val themeMode by themeModeFlow.collectAsState(initial = "system")

            val systemDark = isSystemInDarkTheme()
            val darkMode = when (themeMode) {
                "dark" -> true
                "light" -> false
                "seasonal" -> false
                else -> systemDark
            }
            val seasonalColors = if (themeMode == "seasonal") {
                // Phase 4 — palette động theo 24 tiết khí (đổi mỗi ~15 ngày).
                com.lichso.app.ui.theme.solarTermPalette(LocalDate.now())
            } else null

            // Track which screen to show
            var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }

            // Read onboarding state once on first composition
            var onboardingCompleted by remember { mutableStateOf<Boolean?>(null) }
            LaunchedEffect(Unit) {
                val prefs = context.safeSettingsData.first()
                onboardingCompleted = prefs[SettingsKeys.ONBOARDING_COMPLETED] ?: false
            }

            LichSoTheme(darkTheme = darkMode, seasonalColors = seasonalColors) {
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
                            initialRoute = notificationRoute ?: widgetRoute ?: giftToken?.let { "streak_freeze" } ?: "home",
                            giftToken = giftToken,
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Google requirement: phải kiểm tra lại trong onResume để
        //  - Resume IMMEDIATE update nếu user thoát giữa chừng.
        //  - Cập nhật UI state nếu FLEXIBLE đã DOWNLOADED khi app ở background.
        InAppUpdateManager.onResumeCheck(this)
    }

    override fun onDestroy() {
        InAppUpdateManager.unregister()
        super.onDestroy()
    }
}
