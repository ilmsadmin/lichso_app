package com.lichso.app.ui.screen.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lichso.app.data.local.entity.BookmarkEntity
import com.lichso.app.ui.theme.*
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.components.HeaderIconButton
import com.lichso.app.ui.components.LichSoConfirmDialog
import java.io.File
import java.time.LocalDate

// ══════════════════════════════════════════════════════════
// Profile Screen — Full implementation
// ══════════════════════════════════════════════════════════

@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit = {},
    onFamilyTreeClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onBookmarksClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val c = LichSoThemeColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // ── Backup: hold JSON temporarily until user picks save location ──
    var pendingBackupJson by remember { mutableStateOf<String?>(null) }

    val backupExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && pendingBackupJson != null) {
            viewModel.writeBackupToUri(uri, pendingBackupJson!!)
        }
        pendingBackupJson = null
    }

    // ── Restore: pick a JSON file ──
    val restoreImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.requestRestore(uri)
        }
    }

    // Toast feedback
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeToast()
        }
    }

    // ═══ Sign Out Confirmation Dialog ═══
    if (state.showSignOutDialog) {
        SignOutConfirmDialog(
            onConfirm = { viewModel.signOut() },
            onDismiss = { viewModel.hideSignOutDialog() }
        )
    }

    // ═══ Add Bookmark Bottom Sheet ═══
    if (state.showAddSavedDayDialog) {
        AddBookmarkSheet(
            onAdd = { day, month, year, label, note ->
                viewModel.addBookmark(day, month, year, label, note)
            },
            onDismiss = { viewModel.hideAddSavedDay() }
        )
    }

    // ═══ Edit Profile Bottom Sheet ═══
    if (state.showEditProfileSheet) {
        EditProfileSheet(
            state = state,
            viewModel = viewModel,
            onDismiss = { viewModel.hideEditProfile() }
        )
    }

    // ═══ Restore Confirm Dialog ═══
    if (state.showRestoreConfirmDialog) {
        LichSoConfirmDialog(
            title = "Phục hồi dữ liệu?",
            message = "Toàn bộ dữ liệu hiện tại sẽ bị thay thế bởi bản sao lưu.\n\n${state.restoreSummary}",
            confirmText = "Phục hồi",
            dismissText = "Hủy",
            onConfirm = { viewModel.confirmRestore() },
            onDismiss = { viewModel.cancelRestore() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        // ═══ PROFILE HEADER (Red gradient) ═══
        AppTopBar(
            title = "Hồ sơ",
            onBackClick = onMenuClick,
            leadingIcon = Icons.Filled.Menu,
            actions = {
                HeaderIconButton(
                    icon = Icons.Outlined.Settings,
                    contentDescription = "Cài đặt",
                    onClick = onSettingsClick
                )
            },
            bottomContent = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Avatar
                    AvatarImage(
                        avatarPath = state.avatarPath,
                        size = 72,
                        borderColor = Color.White.copy(alpha = 0.3f),
                        placeholderTint = Color.White.copy(alpha = 0.8f),
                        bgColor = Color.White.copy(alpha = 0.15f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        state.displayName,
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    if (state.email.isNotEmpty()) {
                        Text(
                            state.email,
                            style = TextStyle(fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                        )
                    }

                    // Meta chips — show only when birth info is available
                    if (state.birthInfo.yearCanChi.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MetaChip(icon = Icons.Filled.Cake, text = "${state.birthInfo.yearCanChi} ${state.birthYear}")
                            MetaChip(icon = Icons.Filled.Stars, text = "Mệnh ${state.birthInfo.menh}")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Edit profile & Family tree buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.showEditProfile() }
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("Sửa hồ sơ", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
                        }

                        Row(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onFamilyTreeClick() }
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.AccountTree, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("Gia phả", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
                        }
                    }
                }
            }
        )

        // ═══ SCROLLABLE CONTENT ═══
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // ═══ STATS ROW ═══
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard("${state.allBookmarks.size}", "Ngày đã\nlưu", Modifier.weight(1f).fillMaxHeight())
                StatCard("${state.reminderCount}", "Nhắc nhở", Modifier.weight(1f).fillMaxHeight())
                StatCard("${state.noteCount}", "Ghi chú", Modifier.weight(1f).fillMaxHeight())
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ═══ BIRTH INFO CARDS (nếu đã nhập ngày sinh) ═══
            if (state.birthInfo.yearCanChi.isNotEmpty()) {
                BirthInfoSection(state = state)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ═══ BOOKMARKED DAYS PREVIEW ═══
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle(icon = Icons.Filled.Bookmark, text = "Ngày đã lưu")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (state.allBookmarks.isNotEmpty()) {
                        IconButton(onClick = onBookmarksClick, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.OpenInNew, contentDescription = "Xem tất cả", tint = c.outline, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = { viewModel.showAddSavedDay() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = "Thêm", tint = c.primary, modifier = Modifier.size(20.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            BookmarksPreviewRow(
                bookmarks = state.allBookmarks,
                onRemove = { viewModel.removeBookmark(it) },
                onViewAll = onBookmarksClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ═══ PERSONAL MENU ═══
            SectionTitle(icon = Icons.Filled.FolderOpen, text = "Cá nhân")
            Spacer(modifier = Modifier.height(10.dp))
            MenuGroup {
                MenuItem(
                    iconWrapColor = Color(0xFFFFEBEE),
                    iconColor = Color(0xFFC62828),
                    icon = Icons.Filled.Bookmark,
                    title = "Ngày đã lưu",
                    desc = "${state.allBookmarks.size} ngày quan trọng",
                    badge = if (state.allBookmarks.isNotEmpty()) "${state.allBookmarks.size}" else null,
                    badgeColor = c.primary,
                    onClick = onBookmarksClick
                )
                MenuDivider()
                MenuItem(
                    iconWrapColor = Color(0xFFFFF8E1),
                    iconColor = Color(0xFFF57F17),
                    icon = Icons.Filled.Notifications,
                    title = "Nhắc nhở",
                    desc = "${state.reminderCount} nhắc nhở đang hoạt động",
                    badge = if (state.reminderCount > 0) "${state.reminderCount}" else null,
                    badgeColor = c.gold,
                    onClick = onTasksClick
                )
                MenuDivider()
                MenuItem(
                    iconWrapColor = Color(0xFFE8F5E9),
                    iconColor = Color(0xFF2E7D32),
                    icon = Icons.Filled.EditNote,
                    title = "Ghi chú",
                    desc = "${state.noteCount} ghi chú theo ngày",
                    onClick = onTasksClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ═══ BACKUP & RESTORE ═══
            SectionTitle(icon = Icons.Filled.CloudSync, text = "Sao lưu & Phục hồi")
            Spacer(modifier = Modifier.height(10.dp))
            MenuGroup {
                MenuItem(
                    iconWrapColor = Color(0xFFE8F5E9),
                    iconColor = Color(0xFF2E7D32),
                    icon = Icons.Filled.Upload,
                    title = "Sao lưu dữ liệu",
                    desc = "Xuất toàn bộ dữ liệu ra file JSON",
                    onClick = {
                        viewModel.backupAllData { json, fileName ->
                            pendingBackupJson = json
                            backupExportLauncher.launch(fileName)
                        }
                    }
                )
                MenuDivider()
                MenuItem(
                    iconWrapColor = Color(0xFFE3F2FD),
                    iconColor = Color(0xFF1565C0),
                    icon = Icons.Filled.Download,
                    title = "Phục hồi dữ liệu",
                    desc = "Khôi phục từ file sao lưu JSON",
                    onClick = {
                        restoreImportLauncher.launch(arrayOf("application/json", "*/*"))
                    }
                )
            }

            // Loading indicator for backup/restore
            if (state.isBackingUp || state.isRestoring) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE3F2FD), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF1565C0)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        if (state.isBackingUp) "Đang sao lưu dữ liệu..." else "Đang phục hồi dữ liệu...",
                        style = TextStyle(fontSize = 13.sp, color = Color(0xFF1565C0))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ══════════════════════════════════════════
// BIRTH INFO SECTION (Can Chi, Mệnh, Con Giáp, Cung)
// ══════════════════════════════════════════

@Composable
private fun BirthInfoSection(state: ProfileUiState) {
    val c = LichSoThemeColors.current
    val info = state.birthInfo

    // Info hint card
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFFDE7))),
                RoundedCornerShape(16.dp)
            )
            .border(1.dp, Color(0xFFFFE082), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = c.gold, modifier = Modifier.size(20.dp))
        Text(
            "Dựa trên ngày sinh ${"%02d".format(state.birthDay)}/${"%02d".format(state.birthMonth)}/${state.birthYear}, hệ thống tự động tính:",
            style = TextStyle(fontSize = 12.sp, color = c.textSecondary, lineHeight = 18.sp)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Row 1: Tuổi, Mệnh, Con giáp
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CalcCard(label = "Tuổi", value = info.yearCanChi, modifier = Modifier.weight(1f))
        CalcCard(label = "Mệnh", value = info.menh, modifier = Modifier.weight(1f))
        CalcCard(label = "Con giáp", value = "${info.conGiapEmoji} ${info.conGiap}", modifier = Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Row 2: Cung, Ngũ Hành
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CalcCard(label = "Cung", value = info.cung, modifier = Modifier.weight(1f))
        CalcCard(label = "Ngũ hành", value = info.nguHanh, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CalcCard(label: String, value: String, modifier: Modifier = Modifier) {
    val c = LichSoThemeColors.current
    Column(
        modifier = modifier
            .background(c.surfaceContainer, RoundedCornerShape(14.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium, color = c.outline))
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            value,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.primary),
            textAlign = TextAlign.Center
        )
    }
}

// ══════════════════════════════════════════
// EDIT PROFILE BOTTOM SHEET
// ══════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileSheet(
    state: ProfileUiState,
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit
) {
    val c = LichSoThemeColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.saveAvatarFromUri(it) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.bg,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Sửa hồ sơ",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
                )
                Button(
                    onClick = { viewModel.saveProfile() },
                    colors = ButtonDefaults.buttonColors(containerColor = c.primary),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Lưu", style = TextStyle(fontWeight = FontWeight.SemiBold))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Avatar section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    ) {
                        AvatarImage(
                            avatarPath = state.avatarPath,
                            size = 88,
                            borderColor = c.outlineVariant,
                            placeholderTint = c.outline,
                            bgColor = c.bg3
                        )
                    }
                    // Camera badge (outside the clipped circle)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .background(c.primary, CircleShape)
                            .border(2.dp, c.bg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Đổi ảnh đại diện",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.primary),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                // Remove avatar option
                if (state.avatarPath.isNotEmpty()) {
                    Text(
                        "Xóa ảnh",
                        style = TextStyle(fontSize = 12.sp, color = c.badRed),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.removeAvatar() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Basic info fields ──
            FormField(
                label = "Họ và tên",
                icon = Icons.Filled.Person,
                value = state.editName,
                onValueChange = { viewModel.updateEditName(it) },
                placeholder = "Nhập họ tên"
            )

            FormField(
                label = "Email",
                icon = Icons.Filled.Email,
                value = state.editEmail,
                onValueChange = { viewModel.updateEditEmail(it) },
                placeholder = "Nhập email",
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Birth date section ──
            SectionTitle(icon = Icons.Filled.Cake, text = "Ngày sinh (Dương lịch)")
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FormField(
                    label = "Ngày",
                    value = state.editBirthDay,
                    onValueChange = { viewModel.updateEditBirthDay(it) },
                    placeholder = "DD",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                FormField(
                    label = "Tháng",
                    value = state.editBirthMonth,
                    onValueChange = { viewModel.updateEditBirthMonth(it) },
                    placeholder = "MM",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                FormField(
                    label = "Năm",
                    value = state.editBirthYear,
                    onValueChange = { viewModel.updateEditBirthYear(it) },
                    placeholder = "YYYY",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                "Dùng để tính Can Chi, Mệnh và phân tích phong thủy AI",
                style = TextStyle(fontSize = 11.sp, color = c.outline),
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Birth hour (optional) ──
            SectionTitle(icon = Icons.Filled.Schedule, text = "Giờ sinh (tùy chọn)")
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FormField(
                    label = "Giờ",
                    value = state.editBirthHour,
                    onValueChange = { viewModel.updateEditBirthHour(it) },
                    placeholder = "HH",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                FormField(
                    label = "Phút",
                    value = state.editBirthMinute,
                    onValueChange = { viewModel.updateEditBirthMinute(it) },
                    placeholder = "mm",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                "Giờ sinh giúp tính chính xác Tứ Trụ (Bát Tự)",
                style = TextStyle(fontSize = 11.sp, color = c.outline),
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Gender ──
            FormFieldLabel(icon = Icons.Filled.Wc, label = "Giới tính")
            Spacer(modifier = Modifier.height(6.dp))
            GenderSelector(
                selected = state.editGender,
                onSelect = { viewModel.updateEditGender(it) }
            )

            Text(
                "Ảnh hưởng đến tính Cung phong thủy",
                style = TextStyle(fontSize = 11.sp, color = c.outline),
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Save button at bottom ──
            Button(
                onClick = { viewModel.saveProfile() },
                colors = ButtonDefaults.buttonColors(containerColor = c.primary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Lưu hồ sơ",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GenderSelector(selected: String, onSelect: (String) -> Unit) {
    val c = LichSoThemeColors.current
    val options = listOf("Nam", "Nữ", "Khác")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected == option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) c.primaryContainer else c.surfaceContainer,
                        RoundedCornerShape(14.dp)
                    )
                    .border(
                        1.dp,
                        if (isSelected) c.primary else c.outlineVariant,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelect(option) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    option,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) c.primary else c.textPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    icon: ImageVector? = null,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    val c = LichSoThemeColors.current
    Column(modifier = modifier.padding(bottom = 12.dp)) {
        if (icon != null) {
            FormFieldLabel(icon = icon, label = label)
        } else {
            Text(
                label,
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.primary),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(placeholder, style = TextStyle(color = c.outline, fontSize = 15.sp))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = c.primary,
                unfocusedBorderColor = c.outlineVariant,
                focusedContainerColor = c.surfaceContainer,
                unfocusedContainerColor = c.surfaceContainer,
            ),
            textStyle = TextStyle(fontSize = 15.sp, color = c.textPrimary),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

@Composable
private fun FormFieldLabel(icon: ImageVector, label: String) {
    val c = LichSoThemeColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = c.primary, modifier = Modifier.size(14.dp))
        Text(label, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.primary))
    }
}

// ══════════════════════════════════════════
// SIGN OUT CONFIRM DIALOG
// ══════════════════════════════════════════

@Composable
private fun SignOutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    LichSoConfirmDialog(
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        title = "Đăng xuất",
        message = "Bạn có chắc muốn đăng xuất? Dữ liệu cục bộ vẫn được giữ nguyên.",
        icon = Icons.AutoMirrored.Filled.Logout,
        iconTint = Color(0xFFD32F2F),
        iconBgColor = Color(0xFFFFEBEE),
        confirmText = "Đăng xuất",
        confirmColor = Color(0xFFD32F2F),
    )
}

// ══════════════════════════════════════════
// BOOKMARKS PREVIEW ROW (real data from Room DB)
// ══════════════════════════════════════════

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookmarksPreviewRow(
    bookmarks: List<BookmarkEntity>,
    onRemove: (BookmarkEntity) -> Unit,
    onViewAll: () -> Unit
) {
    val c = LichSoThemeColors.current

    if (bookmarks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.surfaceContainer, RoundedCornerShape(14.dp))
                .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.BookmarkBorder, null, tint = c.outline, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(6.dp))
                Text(
                    "Chưa có ngày nào được lưu",
                    style = TextStyle(fontSize = 13.sp, color = c.outline)
                )
                Text(
                    "Nhấn + hoặc đánh dấu ngày trên lịch",
                    style = TextStyle(fontSize = 11.sp, color = c.outline.copy(alpha = 0.7f))
                )
            }
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bookmarks.size) { index ->
                val bm = bookmarks[index]
                Column(
                    modifier = Modifier
                        .width(78.dp)
                        .background(c.surfaceContainer, RoundedCornerShape(14.dp))
                        .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .combinedClickable(
                            onClick = { },
                            onLongClick = { onRemove(bm) }
                        )
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "%02d".format(bm.solarDay),
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.primary)
                    )
                    Text(
                        "Tháng ${bm.solarMonth}",
                        style = TextStyle(fontSize = 10.sp, color = c.textTertiary)
                    )
                    Text(
                        bm.label.ifEmpty { "${bm.solarDay}/${bm.solarMonth}" },
                        style = TextStyle(fontSize = 9.sp, color = c.outline),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            // "View All" card if more than 4
            if (bookmarks.size > 4) {
                item {
                    Column(
                        modifier = Modifier
                            .width(78.dp)
                            .background(c.primary.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                            .border(1.dp, c.primary.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onViewAll() }
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.MoreHoriz, null, tint = c.primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Xem tất cả",
                            style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = c.primary),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        Text(
            "Nhấn giữ để xóa",
            style = TextStyle(fontSize = 10.sp, color = c.outline),
            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
        )
    }
}

// ══════════════════════════════════════════
// ADD BOOKMARK BOTTOM SHEET (redesigned)
// ══════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBookmarkSheet(
    onAdd: (day: Int, month: Int, year: Int, label: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    val c = LichSoThemeColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val today = LocalDate.now()

    var dayText by remember { mutableStateOf("${today.dayOfMonth}") }
    var monthText by remember { mutableStateOf("${today.monthValue}") }
    var yearText by remember { mutableStateOf("${today.year}") }
    var labelText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var selectedQuickLabel by remember { mutableStateOf<String?>(null) }

    val quickLabels = listOf(
        "🎂" to "Sinh nhật",
        "💒" to "Ngày cưới",
        "🕯️" to "Giỗ chạp",
        "🎉" to "Ngày lễ",
        "🏢" to "Công việc",
        "✈️" to "Du lịch",
        "🏥" to "Khám bệnh",
        "⭐" to "Khác"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.bg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFFEBEE), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.BookmarkAdd, null, tint = c.primary, modifier = Modifier.size(22.dp))
                    }
                    Column {
                        Text("Lưu ngày quan trọng", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.textPrimary))
                        Text("Đánh dấu ngày để dễ tra cứu lại", style = TextStyle(fontSize = 12.sp, color = c.outline))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Quick label chips
            Text("Chọn nhanh loại ngày", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary))
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickLabels.size) { i ->
                    val (emoji, label) = quickLabels[i]
                    val isSelected = selectedQuickLabel == label
                    Row(
                        modifier = Modifier
                            .background(
                                if (isSelected) c.primary else c.surfaceContainer,
                                RoundedCornerShape(20.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) c.primary else c.outlineVariant,
                                RoundedCornerShape(20.dp)
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                selectedQuickLabel = if (isSelected) null else label
                                if (!isSelected) labelText = label
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(emoji, fontSize = 14.sp)
                        Text(
                            label,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else c.textSecondary
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Label field
            OutlinedTextField(
                value = labelText,
                onValueChange = {
                    labelText = it
                    selectedQuickLabel = null
                },
                label = { Text("Tên ngày") },
                placeholder = { Text("VD: Sinh nhật mẹ, Ngày cưới...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                leadingIcon = { Icon(Icons.Filled.Label, null, tint = c.outline, modifier = Modifier.size(20.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = c.primary,
                    unfocusedBorderColor = c.outlineVariant,
                )
            )

            Spacer(Modifier.height(12.dp))

            // Date fields
            Text("Ngày dương lịch", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dayText,
                    onValueChange = { dayText = it },
                    label = { Text("Ngày") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = c.primary,
                        unfocusedBorderColor = c.outlineVariant,
                    )
                )
                OutlinedTextField(
                    value = monthText,
                    onValueChange = { monthText = it },
                    label = { Text("Tháng") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = c.primary,
                        unfocusedBorderColor = c.outlineVariant,
                    )
                )
                OutlinedTextField(
                    value = yearText,
                    onValueChange = { yearText = it },
                    label = { Text("Năm") },
                    modifier = Modifier.weight(1.2f),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = c.primary,
                        unfocusedBorderColor = c.outlineVariant,
                    )
                )
            }

            // Quick date buttons
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val quickDates = listOf(
                    "Hôm nay" to today,
                    "Ngày mai" to today.plusDays(1),
                    "Tuần sau" to today.plusWeeks(1)
                )
                quickDates.forEach { (label, date) ->
                    Text(
                        label,
                        style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = c.primary),
                        modifier = Modifier
                            .background(c.primary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                dayText = "${date.dayOfMonth}"
                                monthText = "${date.monthValue}"
                                yearText = "${date.year}"
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Note field
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Ghi chú (tùy chọn)") },
                placeholder = { Text("Thêm ghi chú cho ngày này...") },
                modifier = Modifier.fillMaxWidth().height(90.dp),
                shape = RoundedCornerShape(14.dp),
                maxLines = 3,
                leadingIcon = { Icon(Icons.Filled.Notes, null, tint = c.outline, modifier = Modifier.size(20.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = c.primary,
                    unfocusedBorderColor = c.outlineVariant,
                )
            )

            Spacer(Modifier.height(20.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, c.outlineVariant)
                ) {
                    Text("Hủy", color = c.textSecondary)
                }

                // Add
                Button(
                    onClick = {
                        val d = dayText.toIntOrNull() ?: 0
                        val m = monthText.toIntOrNull() ?: 0
                        val y = yearText.toIntOrNull() ?: today.year
                        if (d in 1..31 && m in 1..12 && labelText.isNotBlank()) {
                            onAdd(d, m, y, labelText.trim(), noteText.trim())
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.primary),
                    enabled = labelText.isNotBlank() && (dayText.toIntOrNull() ?: 0) in 1..31 && (monthText.toIntOrNull() ?: 0) in 1..12
                ) {
                    Icon(Icons.Filled.BookmarkAdd, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Lưu ngày", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// AVATAR IMAGE (reusable, shows photo or placeholder)
// ══════════════════════════════════════════

@Composable
private fun AvatarImage(
    avatarPath: String,
    size: Int,
    borderColor: Color,
    placeholderTint: Color,
    bgColor: Color
) {
    val hasAvatar = avatarPath.isNotEmpty() && File(avatarPath).exists()

    Box(
        modifier = Modifier
            .size(size.dp)
            .border(3.dp, borderColor, CircleShape)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (hasAvatar) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(File(avatarPath))
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                tint = placeholderTint,
                modifier = Modifier.size((size / 2).dp)
            )
        }
    }
}

// ══════════════════════════════════════════
// REUSABLE COMPONENTS
// ══════════════════════════════════════════

@Composable
private fun MetaChip(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFFD4A017), modifier = Modifier.size(14.dp))
        Text(text, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.9f)))
    }
}

@Composable
private fun StatCard(num: String, label: String, modifier: Modifier = Modifier) {
    val c = LichSoThemeColors.current
    Column(
        modifier = modifier
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(num, style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = c.primary))
        Text(
            label,
            style = TextStyle(fontSize = 11.sp, color = c.textTertiary, lineHeight = 14.sp),
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun SectionTitle(icon: ImageVector, text: String) {
    val c = LichSoThemeColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = c.primary, modifier = Modifier.size(18.dp))
        Text(text, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.textPrimary))
    }
}



@Composable
private fun MenuGroup(content: @Composable ColumnScope.() -> Unit) {
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

@Composable
private fun MenuItem(
    iconWrapColor: Color,
    iconColor: Color,
    icon: ImageVector,
    title: String,
    desc: String? = null,
    badge: String? = null,
    badgeColor: Color = Color.Transparent,
    onClick: () -> Unit = {}
) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Icon wrapper
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconWrapColor, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary))
            if (desc != null) {
                Text(desc, style = TextStyle(fontSize = 11.sp, color = c.outline))
            }
        }

        // Badge
        if (badge != null) {
            Box(
                modifier = Modifier
                    .background(badgeColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(badge, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }
        }

        // Arrow
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = c.outlineVariant, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun MenuDivider() {
    val c = LichSoThemeColors.current
    HorizontalDivider(
        color = c.outlineVariant,
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 18.dp)
    )
}
