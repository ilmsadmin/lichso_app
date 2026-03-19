package com.lichso.app.ui.screen.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.ui.theme.*

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val c = LichSoThemeColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Language picker dialog
    if (state.showLanguageDialog) {
        PickerDialog(
            title = "Ngôn ngữ",
            options = listOf("Tiếng Việt", "English", "中文", "日本語"),
            selected = state.language,
            onSelect = { viewModel.setLanguage(it) },
            onDismiss = { viewModel.hideLanguageDialog() }
        )
    }

    // Calendar style dialog
    if (state.showCalendarStyleDialog) {
        PickerDialog(
            title = "Kiểu lịch",
            options = listOf("Lưới tháng", "Danh sách tuần", "Danh sách ngày"),
            selected = state.calendarStyle,
            onSelect = { viewModel.setCalendarStyle(it) },
            onDismiss = { viewModel.hideCalendarStyleDialog() }
        )
    }

    // Week start dialog
    if (state.showWeekStartDialog) {
        PickerDialog(
            title = "Ngày bắt đầu tuần",
            options = listOf("Thứ Hai", "Chủ Nhật"),
            selected = state.weekStart,
            onSelect = { viewModel.setWeekStart(it) },
            onDismiss = { viewModel.hideWeekStartDialog() }
        )
    }

    // Clear cache dialog
    if (state.showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideClearCacheDialog() },
            confirmButton = {
                TextButton(onClick = { viewModel.clearCache() }) { Text("Xoá", color = c.red) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideClearCacheDialog() }) { Text("Huỷ", color = c.textSecondary) }
            },
            title = { Text("Xoá cache?", color = c.textPrimary) },
            text = { Text("Dữ liệu cache (${state.cacheSize}) sẽ bị xoá. Ứng dụng sẽ tải lại dữ liệu khi cần.", color = c.textSecondary) },
            containerColor = c.bg2,
            shape = RoundedCornerShape(16.dp)
        )
    }

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

        // Profile Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .background(c.bg2, RoundedCornerShape(12.dp))
                .border(1.dp, c.border, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Brush.linearGradient(listOf(c.gold, c.teal)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("L", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, color = if (c.isDark) Color(0xFF1A1500) else Color.White))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Lịch Số Premium", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = c.textPrimary))
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = c.teal2, modifier = Modifier.size(12.dp))
                    Text("Phiên bản Pro · Trọn đời", style = TextStyle(fontSize = 11.sp, color = c.teal2))
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = c.textQuaternary, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.height(18.dp))

        // General Settings
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

        // Display Settings
        SettingsGroup("Hiển thị") {
            SettingsArrow("Ngôn ngữ", Icons.Outlined.Language, state.language) { viewModel.showLanguageDialog() }
            SettingsDivider()
            SettingsArrow("Kiểu lịch", Icons.Outlined.GridView, state.calendarStyle) { viewModel.showCalendarStyleDialog() }
            SettingsDivider()
            SettingsArrow("Ngày bắt đầu tuần", Icons.Outlined.DateRange, state.weekStart) { viewModel.showWeekStartDialog() }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Data & Sync
        SettingsGroup("Dữ liệu & Đồng bộ") {
            SettingsArrow("Sao lưu dữ liệu", Icons.Outlined.CloudUpload, null) { /* TODO */ }
            SettingsDivider()
            SettingsArrow("Khôi phục dữ liệu", Icons.Outlined.CloudDownload, null) { /* TODO */ }
            SettingsDivider()
            SettingsArrow("Xoá cache", Icons.Outlined.DeleteOutline, state.cacheSize) { viewModel.showClearCacheDialog() }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // About
        SettingsGroup("Thông tin") {
            SettingsArrow("Hướng dẫn sử dụng", Icons.Outlined.HelpOutline, null) { }
            SettingsDivider()
            SettingsArrow("Đánh giá ứng dụng", Icons.Outlined.Star, null) { }
            SettingsDivider()
            SettingsArrow("Chia sẻ ứng dụng", Icons.Outlined.Share, null) { }
            SettingsDivider()
            SettingsArrow("Chính sách bảo mật", Icons.Outlined.Lock, null) { }
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

@Composable
private fun PickerDialog(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val c = LichSoThemeColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng", color = c.textSecondary) }
        },
        title = { Text(title, color = c.textPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                options.forEach { option ->
                    val isSelected = option == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(option) }
                            .background(if (isSelected) c.goldDim else Color.Transparent, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            option,
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = if (isSelected) c.gold2 else c.textPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                        if (isSelected) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = c.gold, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
        containerColor = c.bg2,
        shape = RoundedCornerShape(16.dp)
    )
}

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
