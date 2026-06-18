package com.lichso.app

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import com.lichso.app.deeplink.CampaignRoutes
import com.lichso.app.deeplink.InstallReferrerManager
import com.lichso.app.ui.LichSoMainScreen
import com.lichso.app.ui.screen.onboarding.OnboardingScreen
import com.lichso.app.ui.screen.settings.SettingsKeys
import com.lichso.app.ui.screen.settings.safeSettingsData
import com.lichso.app.ui.screen.settings.settingsDataStore
import com.lichso.app.ui.theme.LichSoTheme
import com.lichso.app.update.InAppUpdateManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

private enum class AppScreen { ONBOARDING, MAIN }

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
        intent?.getStringExtra("url")
            ?.takeIf { it.startsWith("https://") && (it.contains("lichso.vn") || it.contains("zenix.vn")) }
            ?.let { url -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }

        // All screens navigable from a push notification or external deep link.
        val validRoutes = CampaignRoutes.valid

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

        // ── Campaign deep link (khớp quảng cáo ↔ màn hình) ──
        // Mỗi creative quảng cáo gắn 1 link để mở thẳng đúng tính năng nó hứa,
        // tránh user mới hụt hẫng vì rơi vào Home chung chung rồi gỡ app.
        // Hỗ trợ 2 dạng cho team marketing:
        //   lichso://chat            (host = đích, ngắn gọn nhất)
        //   lichso://open?screen=chat
        // Kèm alias thân thiện (ai/tuvi → chat, lich → home, dovui → quiz_home…).
        val campaignRoute = intent?.takeIf { it.action == android.content.Intent.ACTION_VIEW }
            ?.data
            ?.takeIf { it.scheme == "lichso" }
            ?.let { uri ->
                CampaignRoutes.resolve(uri.getQueryParameter("screen") ?: uri.host)
            }

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

            // Không còn màn splash: vào thẳng Onboarding (nếu chưa xong) hoặc Main.
            // currentScreen = null trong lúc đọc cờ onboarding từ DataStore (rất nhanh)
            // → chỉ hiển thị nền theme, tránh nháy sai màn.
            var currentScreen by remember { mutableStateOf<AppScreen?>(null) }
            // Route từ deferred deep link (Install Referrer) — tiêu thụ 1 lần khi vào Main.
            var deferredRoute by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(Unit) {
                val prefs = context.safeSettingsData.first()
                val done = prefs[SettingsKeys.ONBOARDING_COMPLETED] ?: false
                if (done) {
                    deferredRoute = InstallReferrerManager.consumePendingRoute(context)
                    currentScreen = AppScreen.MAIN
                } else {
                    currentScreen = AppScreen.ONBOARDING
                }
            }

            // ── Quyền thông báo (Android 13+) ──
            // KHÔNG xin ngay khi vào Home: user mới (đặc biệt từ ads) bị dialog
            // hệ thống chặn trước khi thấy giá trị → tỉ lệ từ chối + gỡ app cao.
            // Thay vào đó xin theo ngữ cảnh khi user bật "Nhắc nhở" lần đầu
            // (ItemEditScreen), và có sẵn card bật thủ công trong Cài đặt.

            LichSoTheme(darkTheme = darkMode, seasonalColors = seasonalColors) {
                when (currentScreen) {
                    // Đang đọc cờ onboarding — nền theme trống trong tích tắc.
                    null -> androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    )

                    AppScreen.ONBOARDING -> {
                        OnboardingScreen(
                            onFinish = {
                                coroutineScope.launch {
                                    // Mark onboarding as completed
                                    context.settingsDataStore.edit { prefs ->
                                        prefs[SettingsKeys.ONBOARDING_COMPLETED] = true
                                    }
                                    // Tiêu thụ deferred deep link (nếu referrer đã về kịp
                                    // trong lúc xem welcome) trước khi vào Main.
                                    deferredRoute = InstallReferrerManager.consumePendingRoute(context)
                                    currentScreen = AppScreen.MAIN
                                }
                            }
                        )
                    }

                    AppScreen.MAIN -> {
                        LichSoMainScreen(
                            modifier = Modifier.fillMaxSize(),
                            initialRoute = notificationRoute ?: widgetRoute ?: campaignRoute ?: deferredRoute ?: giftToken?.let { "streak_freeze" } ?: "home",
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
