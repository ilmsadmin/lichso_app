package com.lichso.app.ui.screen.familytree

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lichso.app.ui.theme.*
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.components.HeaderIconButton
import java.io.File

// ══════════════════════════════════════════════════════════════
// Gia Phả Screen — Family Tree
// Based on v2/screen-family-tree.html
// ══════════════════════════════════════════════════════════════

@Composable
fun FamilyTreeScreen(
    onBackClick: () -> Unit = {},
    onPrayersClick: (prayerId: Int?) -> Unit = {},
    viewModel: FamilyTreeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Main list screen ──
        FamilyTreeListScreen(
            uiState = uiState,
            viewModel = viewModel,
            onBackClick = onBackClick,
            onPrayersClick = onPrayersClick,
        )

        // ── Member detail overlay ──
        AnimatedVisibility(
            visible = uiState.showMemberDetail && uiState.selectedMemberId != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(200))
        ) {
            uiState.selectedMemberId?.let { memberId ->
                viewModel.getMember(memberId)?.let { member ->
                    MemberDetailScreen(
                        member = member,
                        viewModel = viewModel,
                        onBack = { viewModel.closeMemberDetail() },
                        onMemberClick = { viewModel.openMemberDetail(it) }
                    )
                }
            }
        }

        // ── Add member overlay ──
        AnimatedVisibility(
            visible = uiState.showAddMember,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(200))
        ) {
            AddMemberScreen(
                viewModel = viewModel,
                onBack = { viewModel.closeAddMember() }
            )
        }

        // ── Edit member overlay ──
        AnimatedVisibility(
            visible = uiState.showEditMember && uiState.editMemberId != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(200))
        ) {
            uiState.editMemberId?.let { memberId ->
                AddMemberScreen(
                    viewModel = viewModel,
                    editMemberId = memberId,
                    onBack = { viewModel.closeEditMember() }
                )
            }
        }

        // ── Family settings overlay ──
        AnimatedVisibility(
            visible = uiState.showFamilySettings,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(200))
        ) {
            FamilySettingsScreen(
                viewModel = viewModel,
                onBack = { viewModel.closeFamilySettings() }
            )
        }

        // ── Pick member overlay ──
        AnimatedVisibility(
            visible = uiState.showPickMember,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(200))
        ) {
            PickMemberScreen(
                viewModel = viewModel,
                onBack = { viewModel.closePickMember() },
                onMemberPicked = { viewModel.onMemberPicked(it) },
                excludeMemberId = uiState.pickMemberExcludeId,
            )
        }

        // ── Memorial detail overlay ──
        AnimatedVisibility(
            visible = uiState.showMemorialDetail && uiState.selectedMemorialId != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(200))
        ) {
            uiState.selectedMemorialId?.let { memorialId ->
                viewModel.memorials.find { it.id == memorialId }?.let { memorial ->
                    MemorialDetailScreen(
                        memorial = memorial,
                        viewModel = viewModel,
                        onBack = { viewModel.closeMemorialDetail() },
                        onMemberClick = {
                            viewModel.closeMemorialDetail()
                            viewModel.openMemberDetail(it)
                        },
                        onPrayersClick = onPrayersClick
                    )
                }
            }
        }

        // ── Memorial Note/Reminder edit overlay ──
        val showMemorialEdit = uiState.showMemorialNoteEdit || uiState.showMemorialReminderEdit
        AnimatedVisibility(
            visible = showMemorialEdit && uiState.memorialEditTarget != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(200))
        ) {
            uiState.memorialEditTarget?.let { memorial ->
                val editInitialType = if (uiState.showMemorialNoteEdit)
                    com.lichso.app.ui.screen.tasks.EditItemType.NOTE
                else
                    com.lichso.app.ui.screen.tasks.EditItemType.REMIND

                // Pre-fill for reminder: solar date parsed from memorial
                val prefilledReminder = if (uiState.showMemorialReminderEdit) {
                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    val triggerTime = try { sdf.parse(memorial.solarDate)?.time ?: System.currentTimeMillis() } catch (_: Exception) { System.currentTimeMillis() }
                    com.lichso.app.data.local.entity.ReminderEntity(
                        title = memorial.memberName,
                        subtitle = "${memorial.relation} · ${memorial.lunarDate}",
                        triggerTime = triggerTime,
                        repeatType = 5,      // Yearly
                        useLunar = true,
                        category = 4,        // Memorial (Ngày giỗ)
                        labels = "Ngày giỗ"
                    )
                } else null

                // Pre-fill for note
                val prefilledNote = if (uiState.showMemorialNoteEdit) {
                    com.lichso.app.data.local.entity.NoteEntity(
                        title = memorial.memberName,
                        content = "${memorial.relation} · ${memorial.lunarDate} (${memorial.solarDate})\n${memorial.countdown}",
                        colorIndex = 1, // warm color
                        labels = "Ngày giỗ"
                    )
                } else null

                key(memorial.id, editInitialType) {
                    com.lichso.app.ui.screen.tasks.NoteTaskEditScreen(
                        initialType = editInitialType,
                        prefillNote = prefilledNote,
                        prefillReminder = prefilledReminder,
                        onBackClick = { viewModel.closeMemorialEdit() },
                        onSaveNote = { note ->
                            viewModel.saveNoteForMemorial(note)
                            viewModel.closeMemorialEdit()
                        },
                        onSaveTask = { /* not used */ },
                        onSaveReminder = { reminder ->
                            viewModel.saveReminderForMemorial(reminder)
                            viewModel.closeMemorialEdit()
                        },
                        onDelete = { viewModel.closeMemorialEdit() }
                    )
                }
            }
        }

        // ── Member Reminder edit overlay (birthday / memorial from member list) ──
        AnimatedVisibility(
            visible = uiState.showMemberReminderEdit && uiState.memberReminderTarget != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(200))
        ) {
            uiState.memberReminderTarget?.let { member ->
                val isDeceased = member.deathYear != null

                val prefilledReminder = if (isDeceased) {
                    // ── Ngày giỗ: lunar death date, advance 3 days, yearly, lunar calendar ──
                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    val memorial = viewModel.getMemorialForMember(member.id)
                    val triggerTime = memorial?.solarDate?.let {
                        try { sdf.parse(it)?.time } catch (_: Exception) { null }
                    } ?: System.currentTimeMillis()

                    val lunarDateStr = member.deathDateLunar?.split("/")?.take(2)?.joinToString("/") ?: ""

                    com.lichso.app.data.local.entity.ReminderEntity(
                        title = "Ngày giỗ ${member.role} ${member.name}",
                        subtitle = "Giỗ ${member.role} · Đời ${member.generation}" +
                                if (lunarDateStr.isNotBlank()) " · $lunarDateStr Âm lịch" else "",
                        triggerTime = triggerTime,
                        repeatType = 5,       // Yearly
                        useLunar = true,
                        advanceDays = 3,      // Nhắc trước 3 ngày
                        category = 4,         // Memorial
                        labels = "Ngày giỗ",
                    )
                } else {
                    // ── Sinh nhật: solar birth date, advance 1 day, yearly, solar calendar ──
                    val birthDay = member.birthDateLunar?.split("/")?.getOrNull(0)?.toIntOrNull()
                    val birthMonth = member.birthDateLunar?.split("/")?.getOrNull(1)?.toIntOrNull()
                    val birthYearInt = member.birthYear

                    val triggerTime = if (birthDay != null && birthMonth != null && birthYearInt != null) {
                        val cal = java.util.Calendar.getInstance()
                        cal.set(java.util.Calendar.DAY_OF_MONTH, birthDay)
                        cal.set(java.util.Calendar.MONTH, birthMonth - 1)
                        cal.set(java.util.Calendar.YEAR, cal.get(java.util.Calendar.YEAR)) // this year
                        cal.set(java.util.Calendar.HOUR_OF_DAY, 7)
                        cal.set(java.util.Calendar.MINUTE, 0)
                        // If already passed this year, move to next year
                        if (cal.timeInMillis < System.currentTimeMillis()) {
                            cal.add(java.util.Calendar.YEAR, 1)
                        }
                        cal.timeInMillis
                    } else System.currentTimeMillis()

                    com.lichso.app.data.local.entity.ReminderEntity(
                        title = "Sinh nhật ${member.role} ${member.name}",
                        subtitle = "${member.role} · Đời ${member.generation}" +
                                if (member.birthYear != null) " · Sinh ${member.birthYear}" else "",
                        triggerTime = triggerTime,
                        repeatType = 5,       // Yearly
                        useLunar = false,
                        advanceDays = 1,      // Nhắc trước 1 ngày
                        category = 1,         // Birthday
                        labels = "Sinh nhật",
                    )
                }

                key(member.id, isDeceased) {
                    com.lichso.app.ui.screen.tasks.NoteTaskEditScreen(
                        initialType = com.lichso.app.ui.screen.tasks.EditItemType.REMIND,
                        prefillReminder = prefilledReminder,
                        onBackClick = { viewModel.closeMemberReminderEdit() },
                        onSaveNote = { /* not used */ },
                        onSaveTask = { /* not used */ },
                        onSaveReminder = { reminder ->
                            viewModel.saveMemberReminder(reminder)
                            viewModel.closeMemberReminderEdit()
                        },
                        onDelete = { viewModel.closeMemberReminderEdit() }
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// MAIN CONTENT (tabs)
// ══════════════════════════════════════════
@Composable
private fun FamilyTreeListScreen(
    uiState: FamilyTreeUiState,
    viewModel: FamilyTreeViewModel,
    onBackClick: () -> Unit,
    onPrayersClick: (prayerId: Int?) -> Unit = {},
) {
    val c = LichSoThemeColors.current
    val context = LocalContext.current

    // ── JSON backup export launcher ──
    var pendingExportJson by remember { mutableStateOf<String?>(null) }
    val jsonExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && pendingExportJson != null) {
            viewModel.writeExportToUri(uri, pendingExportJson!!)
        }
        pendingExportJson = null
    }

    // ── JSON restore import launcher ──
    val jsonImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.requestImport(uri)
        }
    }

    // ── PDF export launcher ──
    val pdfExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            viewModel.exportFamilyTreePdf(uri)
        }
    }

    // ── Image export launcher ──
    val imageExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
        if (uri != null) {
            viewModel.exportFamilyTreeImage(uri)
        }
    }

    // ── QR code dialog ──
    if (uiState.showQrDialog) {
        QrCodeDialog(
            qrBitmap = uiState.qrBitmap,
            familyName = uiState.familyName,
            onDismiss = { viewModel.hideQrDialog() }
        )
    }

    // ── Import confirm dialog ──
    if (uiState.showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelImport() },
            containerColor = c.bg,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            if (c.isDark) Color(0xFF1A2A3E) else Color(0xFFE3F2FD),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Download,
                        contentDescription = null,
                        tint = if (c.isDark) Color(0xFF64B5F6) else Color(0xFF1565C0),
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    "Nhập dữ liệu gia phả",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = c.textPrimary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    "Dữ liệu gia phả hiện tại sẽ được thay thế bằng dữ liệu từ file.\n\nBạn nên xuất bản sao lưu trước khi nhập. Tiếp tục?",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = c.textSecondary,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmImport() }) {
                    Text("Nhập", color = if (c.isDark) Color(0xFF64B5F6) else Color(0xFF1565C0), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelImport() }) {
                    Text("Hủy", color = c.outline)
                }
            }
        )
    }

    // Toast feedback for export
    LaunchedEffect(uiState.exportImportMessage) {
        uiState.exportImportMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearExportImportMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        // Header (brown gradient)
        FamilyTreeHeader(uiState, onBackClick, onSettingsClick = { viewModel.openFamilySettings() })

        // Tab bar
        TabBar(
            selectedTab = uiState.selectedTab,
            onTabSelect = { viewModel.selectTab(it) }
        )

        // Tab content
        Box(modifier = Modifier.weight(1f)) {
            when (uiState.selectedTab) {
                0 -> TreeTabContent(
                    viewModel = viewModel,
                    uiState = uiState,
                    onRestore = { jsonImportLauncher.launch(arrayOf("application/json", "*/*")) },
                )
                1 -> MemberListTabContent(viewModel, uiState)
                2 -> MemorialTabContent(viewModel, uiState, onPrayersClick)
            }

            // FABs (share actions + add person)
            if (uiState.selectedTab == 0) {
                var showShareActions by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Expandable share action mini-FABs
                    AnimatedVisibility(
                        visible = showShareActions,
                        enter = fadeIn(tween(200)) + expandVertically(expandFrom = Alignment.Bottom, animationSpec = tween(250)),
                        exit = fadeOut(tween(150)) + shrinkVertically(shrinkTowards = Alignment.Bottom, animationSpec = tween(200))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ShareMiniFab(
                                icon = Icons.Filled.PictureAsPdf,
                                label = "Xuất PDF",
                                containerColor = if (c.isDark) Color(0xFFD32F2F) else Color(0xFFC62828),
                                onClick = {
                                    showShareActions = false
                                    val fileName = "GiaPha_${uiState.familyName.replace(" ", "_")}.pdf"
                                    pdfExportLauncher.launch(fileName)
                                }
                            )
                            ShareMiniFab(
                                icon = Icons.Filled.Image,
                                label = "Xuất Ảnh",
                                containerColor = if (c.isDark) Color(0xFF43A047) else Color(0xFF2E7D32),
                                onClick = {
                                    showShareActions = false
                                    val fileName = "GiaPha_${uiState.familyName.replace(" ", "_")}.png"
                                    imageExportLauncher.launch(fileName)
                                }
                            )
                            ShareMiniFab(
                                icon = Icons.Filled.Share,
                                label = "Chia sẻ ảnh",
                                containerColor = if (c.isDark) Color(0xFF1E88E5) else Color(0xFF1565C0),
                                onClick = {
                                    showShareActions = false
                                    viewModel.shareFamilyLink(context)
                                }
                            )
                            ShareMiniFab(
                                icon = Icons.Filled.QrCode,
                                label = "Mã QR",
                                containerColor = if (c.isDark) Color(0xFF9C27B0) else Color(0xFF7B1FA2),
                                onClick = {
                                    showShareActions = false
                                    viewModel.showQrCode()
                                }
                            )
                            ShareMiniFab(
                                icon = Icons.Filled.BackupTable,
                                label = "Lưu dữ liệu",
                                containerColor = if (c.isDark) Color(0xFF00897B) else Color(0xFF00695C),
                                onClick = {
                                    showShareActions = false
                                    viewModel.exportFamilyData { json, fileName ->
                                        pendingExportJson = json
                                        jsonExportLauncher.launch(fileName)
                                    }
                                }
                            )
                        }
                    }

                    // Share toggle FAB
                    FloatingActionButton(
                        onClick = { showShareActions = !showShareActions },
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        containerColor = if (showShareActions)
                            (if (c.isDark) Color(0xFF4E342E) else Color(0xFF5D4037))
                        else
                            (if (c.isDark) Color(0xFF3E2723) else Color(0xFF4E342E)),
                        contentColor = if (c.isDark) Color(0xFFF0E8D0) else Color.White,
                    ) {
                        Icon(
                            if (showShareActions) Icons.Filled.Close else Icons.Filled.Share,
                            contentDescription = "Chia sẻ",
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Add member FAB (same size as share toggle)
                    FloatingActionButton(
                        onClick = { viewModel.openAddMember() },
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        containerColor = if (c.isDark) Color(0xFF4E342E) else Color(0xFF3E2723),
                        contentColor = if (c.isDark) Color(0xFFE8C84A) else Color(0xFFD4A017),
                    ) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = "Thêm", modifier = Modifier.size(22.dp))
                    }

                    // Restore from JSON FAB
                    FloatingActionButton(
                        onClick = { jsonImportLauncher.launch(arrayOf("application/json", "*/*")) },
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        containerColor = if (c.isDark) Color(0xFF2E3D2E) else Color(0xFF33691E),
                        contentColor = if (c.isDark) Color(0xFF81C784) else Color(0xFFA5D6A7),
                    ) {
                        Icon(Icons.Filled.RestorePage, contentDescription = "Restore", modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// HEADER — brown gradient
// ══════════════════════════════════════════
@Composable
private fun FamilyTreeHeader(uiState: FamilyTreeUiState, onBackClick: () -> Unit, onSettingsClick: () -> Unit = {}) {
    val c = LichSoThemeColors.current
    val colors = if (c.isDark)
        listOf(Color(0xFF2A1F1A), Color(0xFF362B22), Color(0xFF40332A))
    else
        listOf(Color(0xFF3E2723), Color(0xFF4E342E), Color(0xFF5D4037))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = colors,
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            HeaderIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Quay lại",
                onClick = onBackClick
            )

            // Center: Title & stats
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            ) {
                // Line 1: "Gia phả họ ..."
                Text(
                    "Gia phả ${uiState.familyName}",
                    style = TextStyle(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    maxLines = 1
                )
                // Line 2: "X thế hệ · Y thành viên"
                Text(
                    "${uiState.totalGenerations} thế hệ · ${uiState.totalMembers} thành viên",
                    style = TextStyle(fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                )
            }

            // Settings button
            HeaderIconButton(
                icon = Icons.Filled.MoreVert,
                contentDescription = "Cài đặt",
                onClick = onSettingsClick
            )
        }
    }
}

// ══════════════════════════════════════════
// SHARE MINI-FAB (used in expandable share actions)
// ══════════════════════════════════════════
@Composable
private fun ShareMiniFab(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    onClick: () -> Unit
) {
    val c = LichSoThemeColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Label chip
        Text(
            label,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (c.isDark) Color(0xFFF0E8D0) else Color.White,
            ),
            modifier = Modifier
                .background(
                    if (c.isDark) Color(0xFF3A3530) else Color(0xFF424242),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
        // Mini FAB
        SmallFloatingActionButton(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            containerColor = containerColor,
            contentColor = Color.White,
            modifier = Modifier.size(42.dp)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
        }
    }
}

// ══════════════════════════════════════════
// QR CODE DIALOG
// ══════════════════════════════════════════
@Composable
private fun QrCodeDialog(
    qrBitmap: Bitmap?,
    familyName: String,
    onDismiss: () -> Unit
) {
    val c = LichSoThemeColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.bg,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.QrCode,
                    contentDescription = null,
                    tint = if (c.isDark) Color(0xFFCE93D8) else Color(0xFF7B1FA2),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Mã QR Gia Phả",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
                    textAlign = TextAlign.Center
                )
                Text(
                    familyName,
                    style = TextStyle(fontSize = 13.sp, color = c.outline),
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (qrBitmap != null) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(c.surfaceContainer, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = if (c.isDark) Color(0xFFCE93D8) else Color(0xFF7B1FA2)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Quét mã QR để xem thông tin gia phả",
                    style = TextStyle(fontSize = 12.sp, color = c.outline, textAlign = TextAlign.Center)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = c.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

// ══════════════════════════════════════════
// TAB BAR
// ══════════════════════════════════════════
@Composable
private fun TabBar(selectedTab: Int, onTabSelect: (Int) -> Unit) {
    val c = LichSoThemeColors.current

    data class TabDef(val label: String, val icon: ImageVector)
    val tabs = listOf(
        TabDef("Cây phả", Icons.Filled.AccountTree),
        TabDef("Thành viên", Icons.Filled.Group),
        TabDef("Ngày giỗ", Icons.Filled.LocalFireDepartment),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.bg)
            .border(width = 0.dp, color = Color.Transparent) // for bottom border
            .drawBehind {
                drawLine(
                    color = c.outlineVariant,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        tabs.forEachIndexed { index, tab ->
            val isActive = index == selectedTab
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelect(index) }
                    .drawBehind {
                        if (isActive) {
                            drawLine(
                                color = if (c.isDark) Color(0xFFEF5350) else Color(0xFFB71C1C),
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                    .padding(vertical = 13.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    tab.icon,
                    contentDescription = tab.label,
                    tint = if (isActive) c.primary else c.outline,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    tab.label,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) c.primary else c.outline
                    )
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// TAB 1: TREE VIEW (Hierarchical)
// ══════════════════════════════════════════════════════════════
@Composable
private fun TreeTabContent(viewModel: FamilyTreeViewModel, uiState: FamilyTreeUiState, onRestore: () -> Unit) {
    val c = LichSoThemeColors.current
    val familyTree = remember(uiState.members) { viewModel.getFamilyTree() }
    val maxGen = remember(uiState.members) { uiState.members.maxOfOrNull { it.generation } ?: 0 }

    // Zoom & pan state
    var scale by remember { mutableFloatStateOf(0.85f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val minScale = 0.3f
    val maxScale = 3f

    if (uiState.members.isEmpty()) {
        // ── Empty state: Tour guide ──
        EmptyFamilyTreeGuide(
            onAddMember = { viewModel.openAddMember() },
            onRestore = onRestore,
        )
    } else {
        // ── Tree view with zoom/pan ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(minScale, maxScale)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
        ) {
        // Tree content — wraps to its intrinsic size, then scaled/translated
        Box(
            modifier = Modifier
                .wrapContentSize(unbounded = true, align = Alignment.TopCenter)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                    transformOrigin = TransformOrigin(0.5f, 0f)
                }
                .padding(horizontal = 16.dp, vertical = 20.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (familyTree.isNotEmpty()) {
                    // Render root family groups side by side
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        familyTree.forEach { rootGroup ->
                            FamilyGroupView(
                                group = rootGroup,
                                viewModel = viewModel,
                                maxGeneration = maxGen,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Zoom controls (bottom-left)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ZoomButton(Icons.Filled.Add) {
                scale = (scale * 1.25f).coerceIn(minScale, maxScale)
            }
            ZoomButton(Icons.Filled.Remove) {
                scale = (scale / 1.25f).coerceIn(minScale, maxScale)
            }
            ZoomButton(Icons.Filled.FitScreen) {
                scale = 0.85f
                offsetX = 0f
                offsetY = 0f
            }
        }
    }
    } // end else (has members)
}

// ══════════════════════════════════════════
// EMPTY STATE: TOUR GUIDE
// ══════════════════════════════════════════
@Composable
private fun EmptyFamilyTreeGuide(
    onAddMember: () -> Unit,
    onRestore: () -> Unit,
) {
    val c = LichSoThemeColors.current

    // Gentle entrance animation
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }

    AnimatedVisibility(
        visibleState = visible,
        enter = fadeIn(tween(600)) + slideInVertically(tween(500)) { it / 4 },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // ── Hero icon ──
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (c.isDark) listOf(Color(0xFF4A3728), Color(0xFF2C1E12))
                            else listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)),
                        ),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.AccountTree,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = if (c.isDark) Color(0xFFFFCC80) else Color(0xFF6D4C41),
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Title ──
            Text(
                "Cây Gia Phả",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary,
                    letterSpacing = 0.3.sp,
                ),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Bắt đầu xây dựng cây gia phả của gia đình bạn",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = c.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                ),
            )

            Spacer(modifier = Modifier.height(36.dp))

            // ── Steps guide ──
            val steps = listOf(
                Triple(Icons.Filled.PersonAdd, "Thêm thành viên đầu tiên", "Bắt đầu bằng cách thêm người lớn tuổi nhất trong gia đình (ông/bà, cụ…)"),
                Triple(Icons.Filled.FamilyRestroom, "Thêm quan hệ gia đình", "Thêm vợ/chồng, con cái để mở rộng cây gia phả"),
                Triple(Icons.Filled.Share, "Chia sẻ & lưu trữ", "Xuất gia phả dưới dạng ảnh, PDF hoặc lưu dữ liệu để lưu trữ an toàn"),
            )

            steps.forEachIndexed { index, (icon, title, desc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Step number badge
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (c.isDark) Color(0xFF3A2A1B) else Color(0xFFFFF8E1),
                                RoundedCornerShape(12.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = if (c.isDark) Color(0xFFFFCC80) else Color(0xFF5D4037),
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${index + 1}. $title",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = c.textPrimary,
                            ),
                        )
                        Text(
                            desc,
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = c.textSecondary,
                                lineHeight = 18.sp,
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Primary CTA: Add first member ──
            Button(
                onClick = onAddMember,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (c.isDark) Color(0xFF5D4037) else Color(0xFF6D4C41),
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    Icons.Filled.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Thêm thành viên đầu tiên",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Secondary CTA: Restore from backup ──
            OutlinedButton(
                onClick = onRestore,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    if (c.isDark) Color(0xFF5D4037) else Color(0xFF8D6E63),
                ),
            ) {
                Icon(
                    Icons.Filled.RestorePage,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (c.isDark) Color(0xFFBCAAA4) else Color(0xFF5D4037),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Khôi phục từ file backup",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (c.isDark) Color(0xFFBCAAA4) else Color(0xFF5D4037),
                    ),
                )
            }

            Spacer(modifier = Modifier.height(80.dp)) // room for FABs
        }
    }
}

@Composable
private fun GenerationLabel(label: String, c: LichSoColors) {
    Text(
        label,
        style = TextStyle(
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = c.primary,
            letterSpacing = 0.3.sp,
        ),
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

/**
 * Recursively render a family group: parents on top, connector line down,
 * then children side by side (each child may have their own family).
 *
 * For MultiSpouse parents, each wife-branch is rendered separately with
 * the couple header (husband + wife) and that wife's children below.
 */
@Composable
private fun FamilyGroupView(
    group: FamilyGroup,
    viewModel: FamilyTreeViewModel,
    maxGeneration: Int,
    showGenLabel: Boolean = true,
) {
    val c = LichSoThemeColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Show generation label only when requested (first occurrence)
        if (showGenLabel) {
            GenerationLabel(viewModel.getGenerationLabel(group.generation), c)
        }

        if (group.parents is TreeNode.MultiSpouse) {
            // ── Multi-spouse layout: show husband+all wives, then each wife's branch ──
            TreeNodeView(
                node = group.parents,
                viewModel = viewModel,
            )

            if (group.children.isNotEmpty()) {
                ConnectorDown(c)

                // Each child of a MultiSpouse parent is a wife-branch (Couple + that wife's children)
                // or unassigned children
                Row(
                    modifier = Modifier.drawBehind {
                        val y = 0f
                        drawLine(
                            color = c.outlineVariant,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 2.dp.toPx(),
                        )
                    }.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    group.children.forEach { childGroup ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            ConnectorDown(c, height = 8)

                            if (childGroup.wifeId != null && childGroup.children.isNotEmpty()) {
                                // Wife branch label
                                val wives = (group.parents as TreeNode.MultiSpouse).wives
                                val wifeIndex = wives.indexOfFirst { it.id == childGroup.wifeId }
                                val wife = wives.getOrNull(wifeIndex)
                                if (wife != null) {
                                    val label = getWifeLabel(wifeIndex)
                                    Text(
                                        "── $label ──",
                                        style = TextStyle(
                                            fontSize = 9.sp,
                                            color = c.outline,
                                            fontWeight = FontWeight.Medium,
                                        ),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }

                            // Render this wife's children normally
                            childGroup.children.forEach { grandchild ->
                                FamilyGroupView(
                                    group = grandchild,
                                    viewModel = viewModel,
                                    maxGeneration = maxGeneration,
                                    showGenLabel = false,
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // ── Standard single/couple layout ──
            TreeNodeView(
                node = group.parents,
                viewModel = viewModel,
            )

            // Children
            if (group.children.isNotEmpty()) {
                ConnectorDown(c)

                if (group.children.size == 1) {
                    FamilyGroupView(
                        group = group.children.first(),
                        viewModel = viewModel,
                        maxGeneration = maxGeneration,
                    )
                } else {
                    Row(
                        modifier = Modifier.drawBehind {
                            val y = 0f
                            drawLine(
                                color = c.outlineVariant,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 2.dp.toPx(),
                            )
                        }.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        group.children.forEach { childGroup ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                ConnectorDown(c, height = 8)
                                FamilyGroupView(
                                    group = childGroup,
                                    viewModel = viewModel,
                                    maxGeneration = maxGeneration,
                                )
                            }
                        }
                    }
                }
            } else if (group.generation == maxGeneration) {
                ConnectorDown(c)
                AddPersonNodeView(onClick = { viewModel.openAddMember() })
            }
        }
    }
}

@Composable
private fun TreeNodeView(
    node: TreeNode,
    viewModel: FamilyTreeViewModel,
) {
    when (node) {
        is TreeNode.Couple -> CoupleNodeView(
            node.person1, node.person2,
            onPersonClick = { viewModel.openMemberDetail(it) }
        )
        is TreeNode.MultiSpouse -> MultiSpouseNodeView(
            husband = node.husband,
            wives = node.wives,
            onPersonClick = { viewModel.openMemberDetail(it) }
        )
        is TreeNode.Single -> PersonNodeView(
            node.person,
            onClick = { viewModel.openMemberDetail(node.person.id) }
        )
        is TreeNode.AddPlaceholder -> AddPersonNodeView(
            onClick = { viewModel.openAddMember() }
        )
    }
}

@Composable
private fun ConnectorDown(c: LichSoColors, height: Int = 16) {
    Box(
        modifier = Modifier
            .width(2.dp)
            .height(height.dp)
            .background(c.outlineVariant)
    )
}

@Composable
private fun ZoomButton(icon: ImageVector, onClick: () -> Unit = {}) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(c.bg, RoundedCornerShape(12.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = c.textPrimary, modifier = Modifier.size(20.dp))
    }
}

// ── Person Node ──
@Composable
private fun PersonNodeView(member: FamilyMember, onClick: () -> Unit) {
    val c = LichSoThemeColors.current

    val borderColor = when {
        member.isSelf -> c.primary
        member.gender == Gender.MALE -> if (c.isDark) Color(0xFF1565C0) else Color(0xFF90CAF9)
        else -> if (c.isDark) Color(0xFFC2185B) else Color(0xFFF48FB1)
    }
    val borderWidth = if (member.isSelf) 2.dp else 1.5.dp
    val nodeAlpha = if (member.deathYear != null) 0.7f else 1f

    Column(
        modifier = Modifier
            .width(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(10.dp, 10.dp, 10.dp, 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(avatarBg(member), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val hasAvatar = !member.avatarPath.isNullOrEmpty() && File(member.avatarPath).exists()
            if (hasAvatar) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(member.avatarPath!!))
                        .crossfade(true)
                        .build(),
                    contentDescription = member.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Text(member.emoji, fontSize = 20.sp)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        // Name
        Text(
            member.name,
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = c.textPrimary.copy(alpha = nodeAlpha),
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            ),
            maxLines = 2
        )

        // Role
        Text(
            member.role,
            style = TextStyle(
                fontSize = 8.sp,
                fontWeight = if (member.isSelf) FontWeight.Bold else FontWeight.Medium,
                color = if (member.isSelf) c.primary else c.outline,
            )
        )

        // Year
        val yearText = when {
            member.deathYear != null -> "${member.birthYear} - ${member.deathYear}"
            member.birthYear != null -> "${member.birthYear}"
            else -> ""
        }
        if (yearText.isNotEmpty()) {
            Text(yearText, style = TextStyle(fontSize = 8.sp, color = c.outlineVariant))
        }
    }
}

@Composable
private fun avatarBg(member: FamilyMember): Color {
    val c = LichSoThemeColors.current
    return when {
        member.isElder -> if (c.isDark) Color(0xFF5D4037) else Color(0xFFFFE082) // golden
        member.gender == Gender.MALE -> if (c.isDark) Color(0xFF1A3A5C) else Color(0xFFE3F2FD)
        else -> if (c.isDark) Color(0xFF4A2030) else Color(0xFFFCE4EC)
    }
}

// ── Couple Node ──
@Composable
private fun CoupleNodeView(
    person1: FamilyMember,
    person2: FamilyMember,
    onPersonClick: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PersonNodeView(person1, onClick = { onPersonClick(person1.id) })
        Text("❤️", fontSize = 10.sp)
        PersonNodeView(person2, onClick = { onPersonClick(person2.id) })
    }
}

// ── Multi-Spouse Node: husband in center, wives around ──
@Composable
private fun MultiSpouseNodeView(
    husband: FamilyMember,
    wives: List<FamilyMember>,
    onPersonClick: (String) -> Unit
) {
    val c = LichSoThemeColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            wives.forEachIndexed { index, wife ->
                if (index == 0) {
                    // First wife on the left of husband
                    PersonNodeView(wife, onClick = { onPersonClick(wife.id) })
                    Text("❤️", fontSize = 10.sp)
                    PersonNodeView(husband, onClick = { onPersonClick(husband.id) })
                } else {
                    Text("❤️", fontSize = 10.sp)
                    PersonNodeView(wife, onClick = { onPersonClick(wife.id) })
                }
            }
        }
        // Wife order labels
        if (wives.size > 1) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                wives.forEachIndexed { index, wife ->
                    val label = getWifeLabel(index)
                    Text(
                        label,
                        style = TextStyle(
                            fontSize = 8.sp,
                            color = c.outline,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
            }
        }
    }
}

/** Helper: wife label by 0-based index in the sorted wives list */
private fun getWifeLabel(index: Int): String = when (index) {
    0 -> "Vợ cả"
    1 -> "Vợ hai"
    2 -> "Vợ ba"
    3 -> "Vợ tư"
    else -> "Vợ ${index + 1}"
}

// ── Add Person Placeholder ──
@Composable
private fun AddPersonNodeView(onClick: () -> Unit) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .width(90.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                2.dp, c.outlineVariant, RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(14.dp, 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.PersonAdd, null, tint = c.outline, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text("Thêm", style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Medium, color = c.outline))
    }
}

// ══════════════════════════════════════════════════════════════
// TAB 2: MEMBER LIST
// ══════════════════════════════════════════════════════════════
@Composable
private fun MemberListTabContent(viewModel: FamilyTreeViewModel, uiState: FamilyTreeUiState) {
    val c = LichSoThemeColors.current
    val grouped = remember(uiState.members) { viewModel.getMembersByGeneration() }
    val gens = grouped.keys.sorted()

    LazyColumn(
        contentPadding = PaddingValues(12.dp, 12.dp, 12.dp, 80.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        gens.forEach { gen ->
            val members = grouped[gen] ?: return@forEach

            // Generation title
            item(key = "gen_$gen") {
                GenerationTitle(gen, viewModel.getGenerationLabel(gen))
            }

            // Member cards
            items(members, key = { it.id }) { member ->
                MemberListCard(
                    member = member,
                    onClick = { viewModel.openMemberDetail(member.id) },
                    onReminderClick = { viewModel.openMemberReminderEdit(member) },
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun GenerationTitle(gen: Int, label: String) {
    val c = LichSoThemeColors.current
    val icon = when (gen) {
        1 -> Icons.Filled.Elderly
        2 -> Icons.Filled.Face6
        3 -> Icons.Filled.Face3
        else -> Icons.Filled.Person
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Icon(icon, null, tint = c.primary, modifier = Modifier.size(16.dp))
        Text(
            label.uppercase(),
            style = TextStyle(
                fontSize = 12.sp, fontWeight = FontWeight.Bold, color = c.primary,
                letterSpacing = 0.5.sp
            )
        )
    }
}

@Composable
private fun MemberListCard(member: FamilyMember, onClick: () -> Unit, onReminderClick: () -> Unit = {}) {
    val c = LichSoThemeColors.current
    val isDeceased = member.deathYear != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .then(
                if (member.isSelf) Modifier.border(2.dp, c.primary, RoundedCornerShape(16.dp))
                else Modifier.border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            )
            .clickable(onClick = onClick)
            .padding(12.dp, 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(avatarBg(member), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val hasAvatar = !member.avatarPath.isNullOrEmpty() && File(member.avatarPath).exists()
            if (hasAvatar) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(member.avatarPath!!))
                        .crossfade(true)
                        .build(),
                    contentDescription = member.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Text(member.emoji, fontSize = 22.sp)
            }
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                member.name,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
            )
            Text(
                buildString {
                    append(member.role)
                    member.canChi?.let { append(" · $it ${member.birthYear}") }
                        ?: member.birthYear?.let { append(" · $it") }
                },
                style = TextStyle(fontSize = 11.sp, color = c.textSecondary),
            )
            val dateText = when {
                isDeceased -> "${member.birthYear} — ${member.deathYear} (thọ ${member.deathYear!! - member.birthYear!!} tuổi)"
                member.birthYear != null -> "Sinh ${member.birthYear} · ${2026 - member.birthYear} tuổi"
                else -> ""
            }
            if (dateText.isNotEmpty()) {
                Text(dateText, style = TextStyle(fontSize = 10.sp, color = c.outline))
            }
        }

        // Tags + Reminder button
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (member.isSelf) {
                TagChip("Tôi", c.primaryContainer, c.primary)
            }
            if (isDeceased) {
                TagChip("Đã mất", if (c.isDark) Color(0xFF424242) else Color(0xFFF5F5F5), Color(0xFF757575))
            }
            // Reminder button
            val reminderLabel = if (isDeceased) "🕯️ Nhắc giỗ" else "🎂 Nhắc sinh nhật"
            val reminderBg = if (isDeceased) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
            val reminderTextColor = if (isDeceased)
                (if (c.isDark) Color(0xFFFFAB40) else Color(0xFFE65100))
            else
                (if (c.isDark) Color(0xFF81C784) else Color(0xFF2E7D32))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (c.isDark) reminderTextColor.copy(alpha = 0.15f) else reminderBg, RoundedCornerShape(8.dp))
                    .clickable(onClick = onReminderClick)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    reminderLabel,
                    style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = reminderTextColor)
                )
            }
        }
    }
}

@Composable
private fun TagChip(label: String, bg: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(label, style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = textColor))
    }
}

// ══════════════════════════════════════════════════════════════
// TAB 3: MEMORIAL (NGÀY GIỖ)
// ══════════════════════════════════════════════════════════════
@Composable
private fun MemorialTabContent(viewModel: FamilyTreeViewModel, uiState: FamilyTreeUiState, onPrayersClick: (prayerId: Int?) -> Unit = {}) {
    val c = LichSoThemeColors.current
    val memorials = uiState.memorials

    // Find deceased members without a memorial
    val deceasedWithoutMemorial = uiState.members.filter { member ->
        member.deathYear != null && memorials.none { it.memberId == member.id }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 80.dp)
    ) {
        if (memorials.isNotEmpty()) {
            // Section label
            item {
                Text(
                    "Ngày giỗ sắp tới".uppercase(),
                    style = TextStyle(
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = c.primary,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(memorials, key = { it.id }) { memorial ->
                MemorialCard(
                    memorial, c,
                    onClick = { viewModel.openMemorialDetail(memorial.id) },
                    onPrayersClick = { onPrayersClick(1) }, // 1 = "Văn khấn cúng giỗ"
                    onReminderClick = { viewModel.openMemorialReminderEdit(memorial) },
                    onNoteClick = { viewModel.openMemorialNoteEdit(memorial) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Suggest editing deceased members without lunar death date
        if (deceasedWithoutMemorial.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Thiếu ngày mất âm lịch".uppercase(),
                    style = TextStyle(
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = c.outline,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(deceasedWithoutMemorial, key = { "suggest_${it.id}" }) { member ->
                MissingLunarDateCard(member, c, onClick = { viewModel.openEditMember(member.id) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Empty state
        if (memorials.isEmpty() && deceasedWithoutMemorial.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🕯️", fontSize = 48.sp)
                    Text(
                        "Chưa có ngày giỗ nào",
                        style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary)
                    )
                    Text(
                        "Thêm thành viên đã mất với ngày mất (âm lịch) để tự động tạo ngày giỗ",
                        style = TextStyle(fontSize = 12.sp, color = c.outline, textAlign = TextAlign.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun MissingLunarDateCard(member: FamilyMember, c: LichSoColors, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (c.isDark) Color(0xFF3A2A1B) else Color(0xFFFFF3E0),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(member.emoji, fontSize = 20.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                member.name,
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
            )
            Text(
                "Bổ sung ngày mất âm lịch để tạo ngày giỗ",
                style = TextStyle(fontSize = 11.sp, color = c.outline)
            )
        }
        Icon(Icons.Filled.Edit, "Chỉnh sửa", tint = c.primary, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun MemorialCard(memorial: MemorialDay, c: LichSoColors, onClick: () -> Unit = {}, onPrayersClick: () -> Unit = {}, onReminderClick: () -> Unit = {}, onNoteClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(c.surfaceContainer, RoundedCornerShape(20.dp))
            .then(
                if (memorial.isUpcoming) Modifier.drawBehind {
                    drawLine(
                        color = if (c.isDark) Color(0xFFFFAB40) else Color(0xFFE65100),
                        start = Offset(0f, 16.dp.toPx()),
                        end = Offset(0f, size.height - 16.dp.toPx()),
                        strokeWidth = 3.dp.toPx()
                    )
                } else Modifier
            )
            .border(1.dp, c.outlineVariant, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(14.dp, 14.dp, 16.dp, 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    if (c.isDark) Color(0xFF3A2A1B) else Color(0xFFFFF3E0),
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("🕯️", fontSize = 24.sp)
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                memorial.memberName,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
            )
            Text(
                memorial.relation,
                style = TextStyle(fontSize = 11.sp, color = c.textSecondary)
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Filled.CalendarToday, null, tint = c.outline, modifier = Modifier.size(14.dp))
                Text(
                    "${memorial.lunarDate} — ${memorial.solarDate}",
                    style = TextStyle(fontSize = 12.sp, color = c.outline)
                )
            }

            // Countdown
            val countdownColor = if (memorial.isUpcoming) {
                if (c.isDark) Color(0xFFFFAB40) else Color(0xFFE65100)
            } else c.outline
            Text(
                "⏰ ${memorial.countdown}",
                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = countdownColor),
                modifier = Modifier.padding(top = 4.dp)
            )

            // Action chips
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MemorialActionChip("Văn khấn", Icons.Filled.MenuBook, isPrimary = true, c = c, onClick = onPrayersClick)
                MemorialActionChip("Nhắc nhở", Icons.Filled.Notifications, isPrimary = false, c = c, onClick = onReminderClick)
                MemorialActionChip("Ghi chú", Icons.Filled.EditNote, isPrimary = false, c = c, onClick = onNoteClick)
            }
        }
    }
}

@Composable
private fun MemorialActionChip(label: String, icon: ImageVector, isPrimary: Boolean, c: LichSoColors, onClick: () -> Unit = {}) {
    val bg = if (isPrimary) c.primary else Color.Transparent
    val textColor = if (isPrimary) Color.White else c.textSecondary
    val borderColor = if (isPrimary) c.primary else c.outlineVariant

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Icon(icon, null, tint = textColor, modifier = Modifier.size(13.dp))
        Text(label, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = textColor), maxLines = 1, softWrap = false)
    }
}
