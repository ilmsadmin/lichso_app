package com.lichso.app.ui.screen.settings

import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.ui.theme.*

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val c = LichSoThemeColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Toast
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeToast()
        }
    }

    // ─── Dialogs ───
    if (state.showPrivacyPolicyDialog) {
        PrivacyPolicyDialog(onDismiss = { viewModel.dismissPrivacyPolicy() })
    }

    // ─── Content ───

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text("Cài đặt", style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 22.sp, color = c.gold2))
        }

        // ═══ General Settings ═══
        SettingsGroup("Cài đặt chung") {
            SettingsToggle("Thông báo nhắc nhở", Icons.Outlined.Notifications, state.notifyEnabled) { viewModel.setNotifyEnabled(it) }
            SettingsDivider()
            SettingsToggle("Hiển thị lịch âm", Icons.Outlined.CalendarMonth, state.lunarBadgeEnabled) { viewModel.setLunarBadge(it) }
            SettingsDivider()
            SettingsToggle("Thông báo giờ đại cát", Icons.Outlined.AccessTime, state.gioDaiCatEnabled) { viewModel.setGioDaiCat(it) }
            SettingsDivider()
            SettingsToggle("Chế độ tối", Icons.Outlined.DarkMode, state.darkModeEnabled) { viewModel.setDarkMode(it) }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ═══ About ═══
        SettingsGroup("Thông tin") {
            SettingsArrow("Hướng dẫn sử dụng", Icons.AutoMirrored.Outlined.HelpOutline, null) { viewModel.openHelp() }
            SettingsDivider()
            SettingsArrow("Đánh giá ứng dụng", Icons.Outlined.Star, null) { viewModel.rateApp() }
            SettingsDivider()
            SettingsArrow("Chia sẻ ứng dụng", Icons.Outlined.Share, null) { viewModel.shareApp() }
            SettingsDivider()
            SettingsArrow("Chính sách bảo mật", Icons.Outlined.Lock, null) { viewModel.openPrivacyPolicy() }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Version
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Lịch Số — Lịch Vạn Niên", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.textTertiary))
            Spacer(modifier = Modifier.height(3.dp))
            Text("Phiên bản 2.0.1 · Build 2025.03", style = TextStyle(fontSize = 11.sp, color = c.textQuaternary))
            Spacer(modifier = Modifier.height(3.dp))
            Text("Made with ♥ by Lịch Số Team", style = TextStyle(fontSize = 10.sp, color = c.textQuaternary))
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}

// ═══════════════════════════════════════
// Reusable Setting Components
// ═══════════════════════════════════════

@Composable
private fun SettingsGroup(label: String, content: @Composable ColumnScope.() -> Unit) {
    val c = LichSoThemeColors.current
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = label.uppercase(),
            style = TextStyle(fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = c.textTertiary, letterSpacing = 1.sp),
            modifier = Modifier.padding(bottom = 7.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.bg2, RoundedCornerShape(12.dp))
                .border(1.dp, c.border, RoundedCornerShape(12.dp))
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsToggle(title: String, icon: ImageVector, checked: Boolean, onToggle: (Boolean) -> Unit) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(18.dp))
        Text(title, style = TextStyle(fontSize = 13.5.sp, color = c.textPrimary), modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = c.teal,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = c.surface2
            ),
            modifier = Modifier.height(22.dp)
        )
    }
}

@Composable
private fun SettingsArrow(title: String, icon: ImageVector, value: String?, onClick: () -> Unit) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(18.dp))
        Text(title, style = TextStyle(fontSize = 13.5.sp, color = c.textPrimary), modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, style = TextStyle(fontSize = 12.sp, color = c.textTertiary))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = c.textQuaternary, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SettingsDivider() {
    val c = LichSoThemeColors.current
    HorizontalDivider(color = c.border, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 14.dp))
}

// ═══════════════════════════════════════
// Privacy Policy Dialog (WebView)
// ═══════════════════════════════════════

@Composable
private fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    val c = LichSoThemeColors.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .background(c.bg2, RoundedCornerShape(16.dp))
        ) {
            // Title bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Chính sách bảo mật",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = c.gold2
                    )
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = c.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            HorizontalDivider(color = c.border, thickness = 0.5.dp)

            // WebView
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = false
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        loadUrl("file:///android_asset/privacy_policy.html")
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 4.dp)
            )
        }
    }
}
