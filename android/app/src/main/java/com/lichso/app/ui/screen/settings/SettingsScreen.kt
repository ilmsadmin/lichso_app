package com.lichso.app.ui.screen.settings

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.lichso.app.R
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.ui.theme.*
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.util.ReviewHelper

// ══════════════════════════════════════════════════════════
// Settings Screen — Material 3, matching screen-settings.html
// ══════════════════════════════════════════════════════════

// Icon wrap color presets (bg + foreground) — dark-mode aware
private data class IconWrapColor(val bg: Color, val fg: Color)

// Light defaults — these are overridden at callsite via settingsIconColors()
private val iconRed = IconWrapColor(Color(0xFFFFEBEE), Color(0xFFC62828))
private val iconAmber = IconWrapColor(Color(0xFFFFF8E1), Color(0xFFF57F17))
private val iconGreen = IconWrapColor(Color(0xFFE8F5E9), Color(0xFF2E7D32))
private val iconBlue = IconWrapColor(Color(0xFFE3F2FD), Color(0xFF1565C0))
private val iconPurple = IconWrapColor(Color(0xFFF3E5F5), Color(0xFF7B1FA2))
private val iconTeal = IconWrapColor(Color(0xFFE0F2F1), Color(0xFF00695C))
private val iconGrey = IconWrapColor(Color(0xFFF5F5F5), Color(0xFF616161))
private val iconOrange = IconWrapColor(Color(0xFFFFF3E0), Color(0xFFE65100))

// Dark variants
private val iconRedDark = IconWrapColor(Color(0xFF3A1B1B), Color(0xFFEF5350))
private val iconAmberDark = IconWrapColor(Color(0xFF3A3010), Color(0xFFFFD54F))
private val iconGreenDark = IconWrapColor(Color(0xFF1B3A2F), Color(0xFF81C784))
private val iconBlueDark = IconWrapColor(Color(0xFF1B2A3A), Color(0xFF64B5F6))
private val iconPurpleDark = IconWrapColor(Color(0xFF2A1B3A), Color(0xFFCE93D8))
private val iconTealDark = IconWrapColor(Color(0xFF1B3A35), Color(0xFF4DB6AC))
private val iconGreyDark = IconWrapColor(Color(0xFF2A2A2A), Color(0xFF9E9E9E))
private val iconOrangeDark = IconWrapColor(Color(0xFF3A2A1B), Color(0xFFE8A06A))

// Helper to pick icon color based on dark mode
@Composable
private fun themedIcon(light: IconWrapColor, dark: IconWrapColor): IconWrapColor {
    return if (LichSoThemeColors.current.isDark) dark else light
}

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val c = LichSoThemeColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Dark-mode-aware icon colors
    val iconRed = themedIcon(iconRed, iconRedDark)
    val iconAmber = themedIcon(iconAmber, iconAmberDark)
    val iconGreen = themedIcon(iconGreen, iconGreenDark)
    val iconBlue = themedIcon(iconBlue, iconBlueDark)
    val iconPurple = themedIcon(iconPurple, iconPurpleDark)
    val iconTeal = themedIcon(iconTeal, iconTealDark)
    val iconGrey = themedIcon(iconGrey, iconGreyDark)
    val iconOrange = themedIcon(iconOrange, iconOrangeDark)

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

    if (state.showWeekStartDialog) {
        SelectionDialog(
            title = "Ngày bắt đầu tuần",
            options = listOf("Thứ Hai", "Chủ Nhật"),
            selected = state.weekStart,
            onSelect = { viewModel.setWeekStart(it) },
            onDismiss = { viewModel.hideWeekStartDialog() }
        )
    }

    if (state.showThemeModeDialog) {
        val themeModeOptions = listOf("Theo hệ thống", "Sáng", "Tối")
        val currentLabel = when (state.themeMode) {
            "light" -> "Sáng"
            "dark" -> "Tối"
            else -> "Theo hệ thống"
        }
        SelectionDialog(
            title = "Giao diện",
            options = themeModeOptions,
            selected = currentLabel,
            onSelect = { label ->
                val mode = when (label) {
                    "Sáng" -> "light"
                    "Tối" -> "dark"
                    else -> "system"
                }
                viewModel.setThemeMode(mode)
            },
            onDismiss = { viewModel.hideThemeModeDialog() }
        )
    }

    if (state.showTempUnitDialog) {
        SelectionDialog(
            title = "Đơn vị nhiệt độ",
            options = listOf("°C", "°F"),
            selected = state.tempUnit,
            onSelect = { viewModel.setTempUnit(it) },
            onDismiss = { viewModel.hideTempUnitDialog() }
        )
    }

    if (state.showLocationDialog) {
        LocationSelectionDialog(
            currentLocation = state.locationName,
            onSelect = { viewModel.setLocationName(it) },
            onDismiss = { viewModel.hideLocationDialog() }
        )
    }

    if (state.showTimePickerDialog) {
        ReminderTimePickerDialog(
            currentHour = state.reminderHour,
            currentMinute = state.reminderMinute,
            onConfirm = { h, m -> viewModel.setReminderTime(h, m) },
            onDismiss = { viewModel.hideTimePickerDialog() }
        )
    }

    if (state.showClearCacheDialog) {
        ClearCacheConfirmDialog(
            cacheSize = state.cacheSize,
            onConfirm = { viewModel.clearCache() },
            onDismiss = { viewModel.hideClearCacheDialog() }
        )
    }

    // ─── Content ───
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        // ═══ TOP BAR ═══
        SettingsTopBar(onBackClick = onBackClick)

        // ═══ SCROLLABLE CONTENT ═══
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // ── CHUNG ──
            SectionTitle("Chung")
            SettingsGroup {
                SettingsArrowItem(
                    icon = Icons.Filled.CalendarToday, iconColor = iconRed,
                    title = "Ngày bắt đầu tuần", desc = "Ngày đầu tiên của tuần",
                    value = state.weekStart
                ) { viewModel.showWeekStartDialog() }
                SettingsItemDivider()
                SettingsArrowItem(
                    icon = Icons.Filled.Palette, iconColor = iconPurple,
                    title = "Giao diện", desc = "Sáng / Tối / Theo hệ thống",
                    value = when (state.themeMode) {
                        "light" -> "Sáng"
                        "dark" -> "Tối"
                        else -> "Hệ thống"
                    }
                ) { viewModel.showThemeModeDialog() }
            }

            // ── HIỂN THỊ ──
            SectionTitle("Hiển thị")
            SettingsGroup {
                SettingsToggleItem(
                    icon = Icons.Filled.DarkMode, iconColor = iconTeal,
                    title = "Hiển thị âm lịch", desc = "Hiện ngày âm trên lịch tháng",
                    checked = state.lunarBadgeEnabled
                ) { viewModel.setLunarBadge(it) }
                SettingsItemDivider()
                SettingsToggleItem(
                    icon = Icons.Filled.EventAvailable, iconColor = iconGreen,
                    title = "Ngày hoàng đạo", desc = "Đánh dấu ngày tốt/xấu trên lịch",
                    checked = state.gioDaiCatEnabled
                ) { viewModel.setGioDaiCat(it) }
                SettingsItemDivider()
                SettingsToggleItem(
                    icon = Icons.Filled.Celebration, iconColor = iconOrange,
                    title = "Ngày lễ / sự kiện", desc = "Hiển thị ngày lễ Việt Nam & quốc tế",
                    checked = state.festivalEnabled
                ) { viewModel.setFestivalEnabled(it) }
                SettingsItemDivider()
                SettingsToggleItem(
                    icon = Icons.Filled.FormatQuote, iconColor = iconBlue,
                    title = "Câu danh ngôn", desc = "Hiện câu nói hay mỗi ngày",
                    checked = state.quoteEnabled
                ) { viewModel.setQuoteEnabled(it) }
            }

            // ── THÔNG BÁO ──
            SectionTitle("Thông báo")
            SettingsGroup {
                SettingsToggleItem(
                    icon = Icons.Filled.Notifications, iconColor = iconAmber,
                    title = "Nhắc nhở hàng ngày", desc = "Thông báo thông tin ngày mới",
                    checked = state.notifyEnabled
                ) { viewModel.setNotifyEnabled(it) }
                SettingsItemDivider()
                SettingsArrowItem(
                    icon = Icons.Filled.Schedule, iconColor = iconRed,
                    title = "Giờ nhắc nhở", desc = "Thời gian nhận thông báo",
                    value = String.format("%02d:%02d", state.reminderHour, state.reminderMinute)
                ) { viewModel.showTimePickerDialog() }
                SettingsItemDivider()
                SettingsToggleItem(
                    icon = Icons.Filled.Celebration, iconColor = iconGreen,
                    title = "Nhắc ngày lễ", desc = "Báo trước 1 ngày trước lễ",
                    checked = state.festivalReminderEnabled
                ) { viewModel.setFestivalReminder(it) }
            }

            // ── VỊ TRÍ & THỜI TIẾT ──
            SectionTitle("Vị trí & Thời tiết")
            SettingsGroup {
                SettingsArrowItem(
                    icon = Icons.Filled.LocationOn, iconColor = iconBlue,
                    title = "Vị trí", desc = "Dùng cho thời tiết & giờ mặt trời",
                    value = state.locationName
                ) { viewModel.showLocationDialog() }
                SettingsItemDivider()
                SettingsArrowItem(
                    icon = Icons.Filled.Thermostat, iconColor = iconTeal,
                    title = "Đơn vị nhiệt độ", desc = "Celsius hoặc Fahrenheit",
                    value = state.tempUnit
                ) { viewModel.showTempUnitDialog() }
            }

            SectionTitle("Dữ liệu")
            SettingsGroup {
                SettingsArrowItem(
                    icon = Icons.Filled.DeleteSweep, iconColor = iconRed,
                    title = "Xoá bộ nhớ đệm", desc = "Giải phóng dung lượng",
                    value = state.cacheSize
                ) { viewModel.showClearCacheDialog() }
            }

            // ── HỖ TRỢ ──
            SectionTitle("Hỗ trợ")
            SettingsGroup {
                SettingsArrowItem(
                    icon = Icons.Filled.Star, iconColor = IconWrapColor(Color(0xFFFFF8E1), Color(0xFFF9A825)),
                    title = "Đánh giá ứng dụng", desc = "Đánh giá 5 sao trên Google Play"
                ) {
                    val activity = context as? android.app.Activity
                    if (activity != null) {
                        ReviewHelper.launchReviewFlow(activity)
                    }
                }
                SettingsItemDivider()
                SettingsArrowItem(
                    icon = Icons.Filled.Share, iconColor = iconBlue,
                    title = "Chia sẻ ứng dụng", desc = "Giới thiệu cho bạn bè & người thân"
                ) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Lịch Số - Lịch Vạn Niên & Phong Thủy")
                        putExtra(Intent.EXTRA_TEXT, "Lịch Số - Ứng dụng lịch vạn niên, phong thủy, gia phả số miễn phí!\n\nhttps://play.google.com/store/apps/details?id=com.lichso.app")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ ứng dụng"))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ──────────────────────────────────────────────────────────
// TOP BAR
// ──────────────────────────────────────────────────────────

@Composable
private fun SettingsTopBar(onBackClick: () -> Unit) {
    AppTopBar(
        title = "Cài đặt",
        onBackClick = onBackClick
    )
}

// ──────────────────────────────────────────────────────────
// SECTION TITLE
// ──────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    val c = LichSoThemeColors.current
    Text(
        text = text.uppercase(),
        style = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = c.primary,
            letterSpacing = 0.8.sp
        ),
        modifier = Modifier.padding(start = 4.dp, top = 20.dp, bottom = 10.dp)
    )
}

// ──────────────────────────────────────────────────────────
// SETTINGS GROUP CARD
// ──────────────────────────────────────────────────────────

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceContainer, RoundedCornerShape(20.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(20.dp))
    ) {
        content()
    }
}

// ──────────────────────────────────────────────────────────
// SETTING ITEM — Toggle
// ──────────────────────────────────────────────────────────

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    iconColor: IconWrapColor,
    title: String,
    desc: String? = null,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!checked) }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Colored icon wrap
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.bg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor.fg, modifier = Modifier.size(20.dp))
        }
        // Title + Desc
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
            )
            if (desc != null) {
                Text(
                    desc,
                    style = TextStyle(fontSize = 11.sp, color = c.outline),
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }
        // Toggle
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = c.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = c.outlineVariant
            )
        )
    }
}

// ──────────────────────────────────────────────────────────
// SETTING ITEM — Arrow (navigation / selection)
// ──────────────────────────────────────────────────────────

@Composable
private fun SettingsArrowItem(
    icon: ImageVector,
    iconColor: IconWrapColor,
    title: String,
    desc: String? = null,
    value: String? = null,
    onClick: () -> Unit = {}
) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Colored icon wrap
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.bg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor.fg, modifier = Modifier.size(20.dp))
        }
        // Title + Desc
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
            )
            if (desc != null) {
                Text(
                    desc,
                    style = TextStyle(fontSize = 11.sp, color = c.outline),
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }
        // Value
        if (value != null) {
            Text(
                value,
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = c.primary)
            )
        }
        // Arrow
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = c.outlineVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ──────────────────────────────────────────────────────────
// DIVIDER
// ──────────────────────────────────────────────────────────

@Composable
private fun SettingsItemDivider() {
    val c = LichSoThemeColors.current
    HorizontalDivider(
        color = c.outlineVariant,
        thickness = 0.5.dp,
        modifier = Modifier.padding(start = 68.dp, end = 18.dp)
    )
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
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
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

// ═══════════════════════════════════════
// Zenix Labs Banner
// ═══════════════════════════════════════

private val ZenixDark = Color(0xFF1A1A2E)
private val ZenixAccent = Color(0xFF6C63FF)

@Composable
private fun ZenixLabsBanner(context: android.content.Context) {
    val c = LichSoThemeColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceContainer, RoundedCornerShape(20.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(20.dp))
    ) {
        // ── Header ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(ZenixDark, Color(0xFF16213E))),
                    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_zenix_logo),
                    contentDescription = "Zenix Labs",
                    modifier = Modifier.size(42.dp)
                )
                Column {
                    Text(
                        "Zenix Labs",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF42A5F5),
                                    Color(0xFF26A69A),
                                    Color(0xFF009688)
                                )
                            )
                        )
                    )
                    Text(
                        "Simple software. Powerful results.",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                    )
                }
            }
        }

        // ── Description ──
        Text(
            text = "Phần mềm hiện đại, tối giản trong trải nghiệm nhưng mạnh mẽ trong khả năng.",
            style = TextStyle(
                fontSize = 12.sp,
                color = c.textSecondary,
                lineHeight = 18.sp
            ),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
        )

        ZenixDivider()

        // ── Ecosystem items ──
        ZenixEcoItem(
            icon = Icons.Filled.Psychology,
            title = "Zenix AI",
            desc = "AI tích hợp vào sản phẩm thực tế",
            iconBg = Color(0xFF7C4DFF),
            iconTint = Color.White
        )
        ZenixDivider()
        ZenixEcoItem(
            icon = Icons.Filled.Build,
            title = "Zenix Tools",
            desc = "Công cụ mạnh mẽ cho công việc hàng ngày",
            iconBg = Color(0xFF00897B),
            iconTint = Color.White
        )
        ZenixDivider()
        ZenixEcoItem(
            icon = Icons.Filled.Cloud,
            title = "Zenix Cloud",
            desc = "Hạ tầng cloud cho ứng dụng hiện đại",
            iconBg = Color(0xFF1E88E5),
            iconTint = Color.White
        )
        ZenixDivider()
        ZenixEcoItem(
            icon = Icons.Filled.PhoneAndroid,
            title = "Zenix Mobile",
            desc = "Ứng dụng di động nhanh, gọn, dễ dùng",
            iconBg = Color(0xFF43A047),
            iconTint = Color.White
        )
        ZenixDivider()
        ZenixEcoItem(
            icon = Icons.Filled.Code,
            title = "Zenix Dev",
            desc = "Framework & công cụ cho lập trình viên",
            iconBg = Color(0xFFE65100),
            iconTint = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Action Buttons ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Google Play
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(c.primary, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/dev?id=5917415499542395567")
                        )
                        context.startActivity(intent)
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "Google Play",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                }
            }

            // Website
            Box(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, c.outlineVariant, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://zenix.vn"))
                        context.startActivity(intent)
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Filled.Language,
                        contentDescription = null,
                        tint = c.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "zenix.vn",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = c.textSecondary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun ZenixEcoItem(
    icon: ImageVector,
    title: String,
    desc: String,
    iconBg: Color,
    iconTint: Color
) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconBg.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconBg, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
            )
            Text(
                desc,
                style = TextStyle(fontSize = 11.sp, color = c.outline),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ZenixDivider() {
    val c = LichSoThemeColors.current
    HorizontalDivider(
        color = c.outlineVariant,
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 18.dp)
    )
}

// ═══════════════════════════════════════
// About Dialog
// ═══════════════════════════════════════

@Composable
private fun AboutZenixDialog(
    versionName: String,
    onDismiss: () -> Unit
) {
    val c = LichSoThemeColors.current
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(c.bg, RoundedCornerShape(24.dp))
                .border(1.dp, c.outlineVariant, RoundedCornerShape(24.dp)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Header gradient ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            if (c.isDark) listOf(Color(0xFF5D1212), Color(0xFF7F1D1D), Color(0xFF4A1010))
                            else listOf(Color(0xFFB71C1C), Color(0xFFD32F2F), Color(0xFF8B0000))
                        )
                    )
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Lịch Số",
                        style = TextStyle(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Lịch Vạn Niên · Cây Gia Phả · AI Tử Vi",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Info rows ──
            AboutInfoRow(icon = Icons.Outlined.Info, label = "Phiên bản", value = "v$versionName")
            AboutInfoRow(icon = Icons.Outlined.Business, label = "Nhà phát triển", value = "Zenix Labs Co., Ltd")
            AboutInfoRow(icon = Icons.Outlined.Language, label = "Website", value = "zenix.vn")
            AboutInfoRow(icon = Icons.Outlined.Email, label = "Email", value = "hello@zenix.vn")
            AboutInfoRow(icon = Icons.Outlined.CalendarToday, label = "Thành lập", value = "2026")

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = c.outlineVariant,
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Philosophy ──
            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.FormatQuote,
                    contentDescription = null,
                    tint = c.textTertiary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Simple software. Powerful results.",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = c.textTertiary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Action buttons ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://zenix.vn"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, c.outlineVariant)
                ) {
                    Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Website", fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/dev?id=5917415499542395567")
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.primary)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Google Play", fontSize = 13.sp, maxLines = 1)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onDismiss) {
                Text("Đóng", color = c.textTertiary)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AboutInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(16.dp))
        Text(
            label,
            style = TextStyle(fontSize = 13.sp, color = c.textTertiary),
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
        )
    }
}

// ═══════════════════════════════════════
// Selection Dialog (Language, Week Start, Temp Unit)
// ═══════════════════════════════════════

@Composable
private fun SelectionDialog(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val c = LichSoThemeColors.current
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(c.bg2, RoundedCornerShape(20.dp))
                .border(1.dp, c.outlineVariant, RoundedCornerShape(20.dp))
        ) {
            // Title
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary
                ),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
            )
            HorizontalDivider(color = c.outlineVariant, thickness = 0.5.dp)

            // Options
            options.forEach { option ->
                val isSelected = option == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(option) }
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = option,
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) c.primary else c.textPrimary
                        )
                    )
                    if (isSelected) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = c.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Cancel button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Huỷ", color = c.textTertiary)
            }
        }
    }
}

// ═══════════════════════════════════════
// Location Selection Dialog
// ═══════════════════════════════════════

@Composable
private fun LocationSelectionDialog(
    currentLocation: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val c = LichSoThemeColors.current
    val cities = com.lichso.app.domain.model.CityCoordinates.cityNames

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(c.bg2, RoundedCornerShape(20.dp))
                .border(1.dp, c.outlineVariant, RoundedCornerShape(20.dp))
        ) {
            // Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = c.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Chọn thành phố",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = c.textPrimary
                    )
                )
            }
            HorizontalDivider(color = c.outlineVariant, thickness = 0.5.dp)

            // Scrollable city list
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                cities.forEach { city ->
                    val isSelected = city == currentLocation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(city) }
                            .padding(horizontal = 24.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                if (isSelected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSelected) c.primary else c.outlineVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = city,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) c.primary else c.textPrimary
                                )
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = c.outlineVariant, thickness = 0.5.dp)

            // Bottom actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Huỷ", color = c.textTertiary)
                }
            }
        }
    }
}

// ═══════════════════════════════════════
// Reminder Time Picker Dialog
// ═══════════════════════════════════════

@Composable
private fun ReminderTimePickerDialog(
    currentHour: Int,
    currentMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val c = LichSoThemeColors.current
    var selectedHour by remember { mutableIntStateOf(currentHour) }
    var selectedMinute by remember { mutableIntStateOf(currentMinute) }

    // Predefined quick options
    val quickTimes = listOf(
        Triple(6, 0, "06:00"),
        Triple(7, 0, "07:00"),
        Triple(8, 0, "08:00"),
        Triple(12, 0, "12:00"),
        Triple(18, 0, "18:00"),
        Triple(20, 0, "20:00"),
    )

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(c.bg2, RoundedCornerShape(20.dp))
                .border(1.dp, c.outlineVariant, RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Giờ nhắc nhở",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary
                )
            )
            Text(
                text = "Chọn thời gian nhận thông báo mỗi ngày",
                style = TextStyle(fontSize = 12.sp, color = c.textTertiary),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Current time display
            Text(
                text = String.format("%02d:%02d", selectedHour, selectedMinute),
                style = TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.primary
                ),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Quick time options
            Text(
                text = "Chọn nhanh",
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.textTertiary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Quick time chips - 3 per row
            for (rowIndex in quickTimes.indices step 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in rowIndex until minOf(rowIndex + 3, quickTimes.size)) {
                        val (h, m, label) = quickTimes[i]
                        val isSelected = selectedHour == h && selectedMinute == m
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) c.primary.copy(alpha = 0.15f)
                                    else c.surfaceContainer
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) c.primary else c.outlineVariant,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedHour = h
                                    selectedMinute = m
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) c.primary else c.textPrimary
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hour & Minute adjusters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Giờ", style = TextStyle(fontSize = 11.sp, color = c.textTertiary))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { selectedHour = if (selectedHour > 0) selectedHour - 1 else 23 },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Giảm giờ", tint = c.textSecondary)
                        }
                        Text(
                            String.format("%02d", selectedHour),
                            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
                        )
                        IconButton(
                            onClick = { selectedHour = if (selectedHour < 23) selectedHour + 1 else 0 },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Tăng giờ", tint = c.textSecondary)
                        }
                    }
                }

                Text(":", style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = c.textTertiary))

                // Minute
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Phút", style = TextStyle(fontSize = 11.sp, color = c.textTertiary))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { selectedMinute = if (selectedMinute > 0) selectedMinute - 5 else 55 },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Giảm phút", tint = c.textSecondary)
                        }
                        Text(
                            String.format("%02d", selectedMinute),
                            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
                        )
                        IconButton(
                            onClick = { selectedMinute = if (selectedMinute < 55) selectedMinute + 5 else 0 },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Tăng phút", tint = c.textSecondary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Huỷ", color = c.textTertiary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onConfirm(selectedHour, selectedMinute) },
                    colors = ButtonDefaults.buttonColors(containerColor = c.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Xác nhận")
                }
            }
        }
    }
}

// ═══════════════════════════════════════
// Clear Cache Confirmation Dialog
// ═══════════════════════════════════════

@Composable
private fun ClearCacheConfirmDialog(
    cacheSize: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val c = LichSoThemeColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.bg2,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Icon(
                Icons.Filled.DeleteSweep,
                contentDescription = null,
                tint = Color(0xFFE53935),
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "Xoá bộ nhớ đệm",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary
                )
            )
        },
        text = {
            Text(
                "Bạn có chắc muốn xoá $cacheSize dữ liệu cache? Ứng dụng có thể tải lại chậm hơn một chút.",
                style = TextStyle(fontSize = 14.sp, color = c.textSecondary)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Xoá cache")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Huỷ", color = c.textTertiary)
            }
        }
    )
}
