package com.lichso.app.ui.screen.familytree

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.ui.components.LichSoConfirmDialog
import com.lichso.app.ui.components.LichSoDialog
import com.lichso.app.ui.theme.*

// ══════════════════════════════════════════════════════════════
// Family Settings Screen
// Based on v2/screen-family-settings.html
// ══════════════════════════════════════════════════════════════

@Composable
fun FamilySettingsScreen(
    viewModel: FamilyTreeViewModel,
    onBack: () -> Unit,
) {
    val c = LichSoThemeColors.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val localContext = LocalContext.current

    // Editable state
    var editingField by remember { mutableStateOf<String?>(null) }
    var editValue by remember { mutableStateOf("") }

    // Toggles
    var remindMemorial by remember { mutableStateOf(true) }
    var remindBirthday by remember { mutableStateOf(true) }
    var showAvatar by remember { mutableStateOf(true) }
    var showYears by remember { mutableStateOf(true) }

    var showDeleteDialog by remember { mutableStateOf(false) }

    // ── Export: hold JSON temporarily until user picks save location ──
    var pendingExportJson by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && pendingExportJson != null) {
            viewModel.writeExportToUri(uri, pendingExportJson!!)
        }
        pendingExportJson = null
    }

    // ── Import: pick a JSON file ──
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.requestImport(uri)
        }
    }

    // Show toast / snackbar for export/import result
    LaunchedEffect(uiState.exportImportMessage) {
        uiState.exportImportMessage?.let { msg ->
            Toast.makeText(localContext, msg, Toast.LENGTH_LONG).show()
            viewModel.clearExportImportMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .imePadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .drawBehind {
                    drawLine(c.outlineVariant, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", tint = c.textPrimary)
            }
            Text(
                "Cài đặt gia phả",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
                modifier = Modifier.weight(1f)
            )
        }

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // ── Family Banner ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            if (c.isDark) listOf(Color(0xFF2A1F1A), Color(0xFF362B22), Color(0xFF40332A))
                            else listOf(Color(0xFF3E2723), Color(0xFF4E342E), Color(0xFF5D4037))
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Crest
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    if (c.isDark) listOf(Color(0xFFB8860B), Color(0xFF9A7200))
                                    else listOf(Color(0xFFD4A017), Color(0xFFB8860B))
                                ),
                                CircleShape
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            uiState.familyCrest,
                            style = TextStyle(
                                fontSize = 24.sp, fontWeight = FontWeight.Bold,
                                color = Color.White, fontFamily = FontFamily.Serif
                            )
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            uiState.familyName,
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Text(
                            "Tạo ngày 15/01/2024",
                            style = TextStyle(fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                        )
                    }
                }
            }

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsStatCard("${uiState.totalGenerations}", "Thế hệ", c.primary, Modifier.weight(1f), c)
                SettingsStatCard("${uiState.totalMembers}", "Thành viên", c.primary, Modifier.weight(1f), c)
                SettingsStatCard("${uiState.memorials.size}", "Ngày giỗ", c.primary, Modifier.weight(1f), c)
            }
            Spacer(modifier = Modifier.height(12.dp))

            // ═══ CHỈNH SỬA ═══
            SettingsSectionTitle("Chỉnh sửa", Icons.Filled.Edit, c)
            SettingsGroup(c) {
                SettingsItem(
                    icon = Icons.Filled.Badge,
                    iconBg = if (c.isDark) Color(0xFF3E3530) else Color(0xFFEFEBE9),
                    iconTint = if (c.isDark) Color(0xFFBCAAA4) else Color(0xFF5D4037),
                    title = "Tên dòng họ", desc = "Đổi tên hiển thị gia phả",
                    value = uiState.familyName.split(" ").lastOrNull() ?: "",
                    c = c, showDivider = true,
                    onClick = {
                        editingField = "familyName"
                        editValue = uiState.familyName
                    }
                )
                SettingsItem(
                    icon = Icons.Filled.Shield,
                    iconBg = if (c.isDark) Color(0xFF3A3520) else Color(0xFFFFF8E1),
                    iconTint = if (c.isDark) Color(0xFFFFD54F) else Color(0xFFF57F17),
                    title = "Biểu tượng dòng họ", desc = "Chỉnh sửa ký hiệu trên Family Crest",
                    value = uiState.familyCrest,
                    c = c, showDivider = true,
                    onClick = {
                        editingField = "familyCrest"
                        editValue = uiState.familyCrest
                    }
                )
                SettingsItem(
                    icon = Icons.Filled.LocationOn,
                    iconBg = if (c.isDark) Color(0xFF2D1F3D) else Color(0xFFF3E5F5),
                    iconTint = if (c.isDark) Color(0xFFCE93D8) else Color(0xFF7B1FA2),
                    title = "Quê quán gốc", desc = "Quê gốc của dòng họ",
                    value = uiState.familyHometown,
                    c = c, showDivider = false,
                    onClick = {
                        editingField = "hometown"
                        editValue = uiState.familyHometown
                    }
                )
            }

            // ═══ HIỂN THỊ ═══
            SettingsSectionTitle("Hiển thị", Icons.Filled.Visibility, c)
            SettingsGroup(c) {
                SettingsItem(
                    icon = Icons.Filled.AccountTree,
                    iconBg = if (c.isDark) Color(0xFF1A2A3E) else Color(0xFFE3F2FD),
                    iconTint = if (c.isDark) Color(0xFF64B5F6) else Color(0xFF1565C0),
                    title = "Kiểu hiển thị cây", desc = "Dọc, ngang hoặc hình quạt",
                    value = "Dọc",
                    c = c, showDivider = true, onClick = {}
                )
                SettingsToggleItem(
                    icon = Icons.Filled.Photo,
                    iconBg = if (c.isDark) Color(0xFF1A3020) else Color(0xFFE8F5E9),
                    iconTint = if (c.isDark) Color(0xFF81C784) else Color(0xFF2E7D32),
                    title = "Hiện ảnh đại diện", desc = "Hiển thị ảnh trên cây gia phả",
                    checked = showAvatar, c = c, showDivider = true,
                    onCheckedChange = {
                        showAvatar = it
                        viewModel.updateFamilySettings(showAvatar = it)
                    }
                )
                SettingsToggleItem(
                    icon = Icons.Filled.CalendarMonth,
                    iconBg = if (c.isDark) Color(0xFF3A3520) else Color(0xFFFFF8E1),
                    iconTint = if (c.isDark) Color(0xFFFFD54F) else Color(0xFFF57F17),
                    title = "Hiện năm sinh/mất", desc = "Hiển thị năm trên node",
                    checked = showYears, c = c, showDivider = false,
                    onCheckedChange = {
                        showYears = it
                        viewModel.updateFamilySettings(showYears = it)
                    }
                )
            }

            // ═══ THÔNG BÁO ═══
            SettingsSectionTitle("Thông báo", Icons.Filled.Notifications, c)
            SettingsGroup(c) {
                SettingsToggleItem(
                    icon = Icons.Filled.LocalFireDepartment,
                    iconBg = if (c.isDark) Color(0xFF3A1A1A) else Color(0xFFFFEBEE),
                    iconTint = if (c.isDark) Color(0xFFEF5350) else Color(0xFFC62828),
                    title = "Nhắc ngày giỗ", desc = "Nhắc nhở trước ngày giỗ",
                    checked = remindMemorial, c = c, showDivider = true,
                    onCheckedChange = {
                        remindMemorial = it
                        viewModel.updateFamilySettings(remindMemorial = it)
                    }
                )
                SettingsToggleItem(
                    icon = Icons.Filled.Cake,
                    iconBg = if (c.isDark) Color(0xFF1A2A3E) else Color(0xFFE3F2FD),
                    iconTint = if (c.isDark) Color(0xFF64B5F6) else Color(0xFF1565C0),
                    title = "Nhắc sinh nhật", desc = "Nhắc sinh nhật thành viên",
                    checked = remindBirthday, c = c, showDivider = true,
                    onCheckedChange = {
                        remindBirthday = it
                        viewModel.updateFamilySettings(remindBirthday = it)
                    }
                )
                SettingsItem(
                    icon = Icons.Filled.Schedule,
                    iconBg = if (c.isDark) Color(0xFF3A3520) else Color(0xFFFFF8E1),
                    iconTint = if (c.isDark) Color(0xFFFFD54F) else Color(0xFFF57F17),
                    title = "Thời gian nhắc trước", desc = "Nhắc trước ngày giỗ bao lâu",
                    value = "3 ngày",
                    c = c, showDivider = false, onClick = {}
                )
            }

            // ═══ ĐỒNG BỘ ═══
            SettingsSectionTitle("Sao lưu & Khôi phục", Icons.Filled.Sync, c)
            SettingsGroup(c) {
                SettingsItem(
                    icon = Icons.Filled.Upload,
                    iconBg = if (c.isDark) Color(0xFF1A3020) else Color(0xFFE8F5E9),
                    iconTint = if (c.isDark) Color(0xFF81C784) else Color(0xFF2E7D32),
                    title = "Xuất dữ liệu gia phả", desc = "Lưu file JSON để chia sẻ, backup",
                    c = c, showDivider = true,
                    onClick = {
                        viewModel.exportFamilyData { json, fileName ->
                            pendingExportJson = json
                            exportLauncher.launch(fileName)
                        }
                    }
                )
                SettingsItem(
                    icon = Icons.Filled.Download,
                    iconBg = if (c.isDark) Color(0xFF1A2A3E) else Color(0xFFE3F2FD),
                    iconTint = if (c.isDark) Color(0xFF64B5F6) else Color(0xFF1565C0),
                    title = "Nhập dữ liệu gia phả", desc = "Khôi phục từ file JSON đã xuất",
                    c = c, showDivider = true,
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    }
                )
                SettingsItem(
                    icon = Icons.Filled.CloudUpload,
                    iconBg = if (c.isDark) Color(0xFF2A2A2A) else Color(0xFFF5F5F5),
                    iconTint = if (c.isDark) Color(0xFF9E9E9E) else Color(0xFF616161),
                    title = "Sao lưu lên Cloud", desc = "Đang phát triển...",
                    c = c, showDivider = false, onClick = {}
                )
            }

            // Loading indicator for export/import
            if (uiState.isExporting || uiState.isImporting) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (c.isDark) Color(0xFF1A2A3E) else Color(0xFFE3F2FD),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = if (c.isDark) Color(0xFF64B5F6) else Color(0xFF1565C0)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        if (uiState.isExporting) "Đang xuất dữ liệu..." else "Đang nhập dữ liệu...",
                        style = TextStyle(fontSize = 13.sp, color = if (c.isDark) Color(0xFF64B5F6) else Color(0xFF1565C0))
                    )
                }
            }

            // ═══ NGUY HIỂM ═══
            SettingsSectionTitle("Nguy hiểm", Icons.Filled.Warning, c)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        1.5.dp,
                        if (c.isDark) Color(0xFF5A2020) else Color(0xFFFFCDD2),
                        RoundedCornerShape(14.dp)
                    )
                    .background(
                        if (c.isDark) Color(0xFF3A1A1A) else Color(0xFFFFF5F5),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { showDeleteDialog = true }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.DeleteForever, null, tint = if (c.isDark) Color(0xFFEF5350) else Color(0xFFC62828), modifier = Modifier.size(20.dp))
                    Text(
                        "Xoá toàn bộ gia phả",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (c.isDark) Color(0xFFEF5350) else Color(0xFFC62828))
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // ── Edit Dialog ──
    if (editingField != null) {
        val dialogTitle = when (editingField) {
            "familyName" -> "Tên dòng họ"
            "familyCrest" -> "Biểu tượng dòng họ"
            "hometown" -> "Quê quán gốc"
            else -> "Chỉnh sửa"
        }
        LichSoDialog(
            onDismiss = { editingField = null },
            title = dialogTitle,
            icon = Icons.Filled.Edit,
            iconTint = if (c.isDark) Color(0xFF64B5F6) else Color(0xFF1565C0),
            iconBgColor = if (c.isDark) Color(0xFF1A2A3E) else Color(0xFFE3F2FD),
            confirmText = "Lưu",
            confirmColor = if (c.isDark) Color(0xFF64B5F6) else Color(0xFF1565C0),
            onConfirm = {
                when (editingField) {
                    "familyName" -> viewModel.updateFamilySettings(familyName = editValue)
                    "familyCrest" -> viewModel.updateFamilySettings(familyCrest = editValue)
                    "hometown" -> viewModel.updateFamilySettings(hometown = editValue)
                }
                editingField = null
            },
        ) {
            OutlinedTextField(
                value = editValue,
                onValueChange = { editValue = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 15.sp)
            )
        }
    }

    // ── Delete confirmation dialog ──
    if (showDeleteDialog) {
        LichSoConfirmDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deleteAllFamilyData()
                showDeleteDialog = false
                onBack()
            },
            title = "Xác nhận xoá",
            message = "Bạn có chắc chắn muốn xoá toàn bộ dữ liệu gia phả? Hành động này không thể hoàn tác.",
            icon = Icons.Filled.DeleteForever,
            iconTint = if (c.isDark) Color(0xFFEF5350) else Color(0xFFC62828),
            iconBgColor = if (c.isDark) Color(0xFF3A1A1A) else Color(0xFFFFEBEE),
            confirmText = "Xoá",
            confirmColor = if (c.isDark) Color(0xFFEF5350) else Color(0xFFC62828),
        )
    }

    // ── Import confirmation dialog ──
    if (uiState.showImportConfirmDialog) {
        LichSoConfirmDialog(
            onDismiss = { viewModel.cancelImport() },
            onConfirm = { viewModel.confirmImport() },
            title = "Nhập dữ liệu gia phả",
            message = "Dữ liệu gia phả hiện tại sẽ được thay thế bằng dữ liệu từ file.\n\nBạn nên xuất bản sao lưu trước khi nhập. Tiếp tục?",
            icon = Icons.Filled.Download,
            iconTint = if (c.isDark) Color(0xFF64B5F6) else Color(0xFF1565C0),
            iconBgColor = if (c.isDark) Color(0xFF1A2A3E) else Color(0xFFE3F2FD),
            confirmText = "Nhập",
            confirmColor = if (c.isDark) Color(0xFF64B5F6) else Color(0xFF1565C0),
        )
    }
}

// ══════════════════════════════════════════
// Settings components
// ══════════════════════════════════════════

@Composable
private fun SettingsStatCard(value: String, label: String, valueColor: Color, modifier: Modifier, c: LichSoColors) {
    Column(
        modifier = modifier
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .padding(14.dp, 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = valueColor))
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium, color = c.outline))
    }
}

@Composable
private fun SettingsSectionTitle(title: String, icon: ImageVector, c: LichSoColors) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 20.dp, bottom = 10.dp, start = 4.dp)
    ) {
        Icon(icon, null, tint = c.primary, modifier = Modifier.size(16.dp))
        Text(
            title.uppercase(),
            style = TextStyle(
                fontSize = 12.sp, fontWeight = FontWeight.Bold,
                color = c.primary, letterSpacing = 0.5.sp
            )
        )
    }
}

@Composable
private fun SettingsGroup(c: LichSoColors, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceContainer, RoundedCornerShape(20.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(20.dp)),
        content = content
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector, iconBg: Color, iconTint: Color,
    title: String, desc: String,
    value: String? = null,
    c: LichSoColors, showDivider: Boolean, onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (showDivider) Modifier.drawBehind {
                    drawLine(c.outlineVariant, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                } else Modifier
            )
            .clickable(onClick = onClick)
            .padding(14.dp, 14.dp, 16.dp, 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary))
            Text(desc, style = TextStyle(fontSize = 11.sp, color = c.outline))
        }
        if (value != null) {
            Text(value, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = c.outline))
        }
        Icon(Icons.Filled.ChevronRight, null, tint = c.outlineVariant, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector, iconBg: Color, iconTint: Color,
    title: String, desc: String,
    checked: Boolean, c: LichSoColors, showDivider: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (showDivider) Modifier.drawBehind {
                    drawLine(c.outlineVariant, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                } else Modifier
            )
            .padding(14.dp, 14.dp, 16.dp, 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary))
            Text(desc, style = TextStyle(fontSize = 11.sp, color = c.outline))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4CAF50),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = c.outlineVariant,
            )
        )
    }
}
