package com.lichso.app.ui.screen.familytree

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.lichso.app.data.local.entity.MemorialChecklistEntity
import com.lichso.app.ui.components.HeaderIconButton
import com.lichso.app.ui.components.LichSoConfirmDialog
import com.lichso.app.ui.components.LichSoDialog
import com.lichso.app.ui.theme.*
import java.io.File

// ══════════════════════════════════════════════════════════════
// Memorial Detail Screen
// Based on v2/screen-memorial-detail.html
// ══════════════════════════════════════════════════════════════

@Composable
fun MemorialDetailScreen(
    memorial: MemorialDay,
    viewModel: FamilyTreeViewModel,
    onBack: () -> Unit,
    onMemberClick: (String) -> Unit = {},
    onPrayersClick: (prayerId: Int?) -> Unit = {},
) {
    val c = LichSoThemeColors.current
    val member = viewModel.getMember(memorial.memberId)
    val checklist by viewModel.getChecklistFlow(memorial.id).collectAsStateWithLifecycle(initialValue = emptyList())

    var showAddChecklistDialog by remember { mutableStateOf(false) }
    var newChecklistText by remember { mutableStateOf("") }
    var showEditDateDialog by remember { mutableStateOf(false) }
    var showEditNoteDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editNoteText by remember(memorial.note) { mutableStateOf(memorial.note ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .imePadding()
    ) {
        // ── Hero ──
        MemorialHero(memorial, member, onBack, onEdit = { showEditDateDialog = true }, onDelete = { showDeleteDialog = true })

        // ── Scrollable content ──
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
        ) {
            // Date cards
            item {
                DateCards(memorial, c)
            }

            // Person info
            if (member != null) {
                item {
                    MemorialSectionTitle("Người được thờ cúng", Icons.Filled.Person, c)
                    PersonInfoCard(member, c, onClick = { onMemberClick(member.id) })
                }
            }

            // Offerings
            item {
                MemorialSectionTitle("Lễ vật cần chuẩn bị", Icons.Filled.Restaurant, c)
                OfferingGrid(c)
            }

            // Checklist
            item {
                MemorialSectionTitle("Chuẩn bị", Icons.Filled.Checklist, c)
            }

            items(checklist, key = { it.id }) { item ->
                ChecklistItem(
                    item = item,
                    c = c,
                    onToggle = { viewModel.toggleChecklistItem(item.id, !item.isDone) },
                    onDelete = { viewModel.deleteChecklistItem(item) }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            item {
                // Add checklist button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(2.dp, c.outlineVariant, RoundedCornerShape(14.dp))
                        .clickable { showAddChecklistDialog = true }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.Add, null, tint = c.outline, modifier = Modifier.size(18.dp))
                        Text(
                            "Thêm mục chuẩn bị",
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.outline)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Prayer link
            item {
                MemorialSectionTitle("Văn khấn", Icons.Filled.MenuBook, c)
                PrayerLinkCard(c, onClick = { onPrayersClick(1) }) // 1 = "Văn khấn cúng giỗ"
            }

            // Reminder
            item {
                MemorialSectionTitle("Nhắc nhở", Icons.Filled.Notifications, c)
                ReminderCard(
                    title = "Nhắc trước 3 ngày",
                    desc = memorial.remind3DaysDateStr,
                    checked = memorial.remindBefore3Days, c = c,
                    onCheckedChange = { viewModel.updateMemorialRemind3Days(memorial.id, it) }
                )
                Spacer(modifier = Modifier.height(6.dp))
                ReminderCard(
                    title = "Nhắc trước 1 ngày (Tiên thường)",
                    desc = memorial.remind1DayDateStr,
                    checked = memorial.remindBefore1Day, c = c,
                    onCheckedChange = { viewModel.updateMemorialRemind1Day(memorial.id, it) }
                )
            }

            // Notes
            item {
                MemorialSectionTitle("Ghi chú ngày giỗ", Icons.Filled.EditNote, c)
                if (memorial.note != null) {
                    MemorialNoteCard(
                        text = memorial.note,
                        dateInfo = "Cập nhật gần đây",
                        c = c,
                        onEditClick = { showEditNoteDialog = true }
                    )
                } else {
                    // Add note button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(2.dp, c.outlineVariant, RoundedCornerShape(14.dp))
                            .clickable { showEditNoteDialog = true }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Add, null, tint = c.outline, modifier = Modifier.size(18.dp))
                            Text(
                                "Thêm ghi chú",
                                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.outline)
                            )
                        }
                    }
                }
            }

            // Quote
            item {
                Spacer(modifier = Modifier.height(12.dp))
                QuoteCard(c)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // ── Add checklist dialog ──
    if (showAddChecklistDialog) {
        LichSoDialog(
            onDismiss = { showAddChecklistDialog = false; newChecklistText = "" },
            title = "Thêm mục chuẩn bị",
            icon = Icons.Filled.PlaylistAdd,
            iconTint = Color(0xFF2E7D32),
            iconBgColor = Color(0xFFE8F5E9),
            confirmText = "Thêm",
            confirmColor = Color(0xFF2E7D32),
            confirmEnabled = newChecklistText.isNotBlank(),
            onConfirm = {
                if (newChecklistText.isNotBlank()) {
                    viewModel.addChecklistItem(memorial.id, newChecklistText)
                    newChecklistText = ""
                    showAddChecklistDialog = false
                }
            },
        ) {
            OutlinedTextField(
                value = newChecklistText,
                onValueChange = { newChecklistText = it },
                label = { Text("Mục chuẩn bị", fontSize = 12.sp) },
                placeholder = { Text("Ví dụ: Mua hoa cúc, Đặt gà luộc...", fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 15.sp)
            )
        }
    }

    // ── Edit Note Dialog ──
    if (showEditNoteDialog) {
        LichSoDialog(
            onDismiss = { showEditNoteDialog = false; editNoteText = memorial.note ?: "" },
            title = "Ghi chú ngày giỗ",
            icon = Icons.Filled.EditNote,
            iconTint = Color(0xFFE65100),
            iconBgColor = Color(0xFFFFF3E0),
            confirmText = "Lưu",
            confirmColor = Color(0xFFE65100),
            onConfirm = {
                viewModel.updateMemorialNote(memorial.id, editNoteText.ifBlank { null })
                showEditNoteDialog = false
            },
        ) {
            OutlinedTextField(
                value = editNoteText,
                onValueChange = { editNoteText = it },
                label = { Text("Ghi chú", fontSize = 12.sp) },
                placeholder = { Text("Nhập ghi chú cho ngày giỗ...", fontSize = 13.sp) },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 14.sp)
            )
        }
    }

    // ── Edit Date Dialog ──
    if (showEditDateDialog) {
        EditMemorialDateDialog(
            currentLunarDay = memorial.lunarDay,
            currentLunarMonth = memorial.lunarMonth,
            onDismiss = { showEditDateDialog = false },
            onSave = { day, month ->
                viewModel.updateMemorialDate(memorial.id, day, month)
                showEditDateDialog = false
            }
        )
    }

    // ── Delete Confirmation Dialog ──
    if (showDeleteDialog) {
        LichSoConfirmDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteMemorial(memorial.id)
            },
            title = "Xóa ngày giỗ",
            message = "Bạn có chắc muốn xóa ngày giỗ này? Ngày mất âm lịch của thành viên cũng sẽ bị xóa. Bạn có thể thêm lại bằng cách cập nhật ngày mất khi chỉnh sửa thành viên.",
            icon = Icons.Filled.DeleteForever,
            confirmText = "Xóa",
        )
    }
}

// ══════════════════════════════════════════
// HERO
// ══════════════════════════════════════════
@Composable
private fun MemorialHero(memorial: MemorialDay, member: FamilyMember?, onBack: () -> Unit, onEdit: () -> Unit = {}, onDelete: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(Color(0xFFBF360C), Color(0xFFE65100), Color(0xFFFF6D00)))
            )
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Nav row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", onClick = onBack)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HeaderIconButton(Icons.Filled.Edit, "Sửa", onClick = onEdit)
                    HeaderIconButton(Icons.Filled.Delete, "Xóa", onClick = onDelete)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Candle emoji
            Text("🕯️", fontSize = 52.sp)

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                memorial.memberName,
                style = TextStyle(
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White,
                    textAlign = TextAlign.Center, lineHeight = 26.sp
                )
            )
            Text(
                memorial.relation,
                style = TextStyle(fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f)),
                modifier = Modifier.padding(top = 4.dp)
            )

            // Countdown badge
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.Schedule, null, tint = Color(0xFFFFCC80), modifier = Modifier.size(18.dp))
                if (memorial.countdownDays == 0L) {
                    Text(
                        "Hôm nay",
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFCC80))
                    )
                } else {
                    Text("Còn", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
                    Text(
                        "${memorial.countdownDays}",
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFCC80))
                    )
                    Text("ngày nữa", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// DATE CARDS
// ══════════════════════════════════════════
@Composable
private fun DateCards(memorial: MemorialDay, c: LichSoColors) {
    // Parse solarDate to get day of week
    val solarDateParts = memorial.solarDate.split("/")
    val solarYear = if (solarDateParts.size >= 3) solarDateParts[2] else ""
    val dayOfWeekStr = try {
        val date = java.time.LocalDate.of(
            solarDateParts[2].toInt(),
            solarDateParts[1].toInt(),
            solarDateParts[0].toInt()
        )
        when (date.dayOfWeek.value) {
            1 -> "Thứ Hai"
            2 -> "Thứ Ba"
            3 -> "Thứ Tư"
            4 -> "Thứ Năm"
            5 -> "Thứ Sáu"
            6 -> "Thứ Bảy"
            7 -> "Chủ Nhật"
            else -> ""
        }
    } catch (_: Exception) { "" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Lunar
        Column(
            modifier = Modifier
                .weight(1f)
                .background(
                    Brush.linearGradient(listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2))),
                    RoundedCornerShape(16.dp)
                )
                .border(1.dp, Color(0xFFFFCC80), RoundedCornerShape(16.dp))
                .padding(14.dp, 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "ÂM LỊCH",
                style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp, color = Color(0xFFE65100))
            )
            Text(
                memorial.lunarDate.replace(" Âm lịch", ""),
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBF360C)),
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                "Năm ${memorial.yearCanChi} $solarYear",
                style = TextStyle(fontSize = 10.sp, color = Color(0xFFE65100)),
                modifier = Modifier.padding(top = 1.dp)
            )
        }
        // Solar
        Column(
            modifier = Modifier
                .weight(1f)
                .background(c.surfaceContainer, RoundedCornerShape(16.dp))
                .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
                .padding(14.dp, 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "DƯƠNG LỊCH",
                style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp, color = c.outline)
            )
            Text(
                memorial.solarDate.take(5),
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                "$dayOfWeekStr, $solarYear",
                style = TextStyle(fontSize = 10.sp, color = c.outline),
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}

// ══════════════════════════════════════════
// PERSON INFO CARD
// ══════════════════════════════════════════
@Composable
private fun PersonInfoCard(member: FamilyMember, c: LichSoColors, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(c.surfaceContainer, RoundedCornerShape(18.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp, 14.dp, 16.dp, 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFE082))),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            val avatarFile = member.avatarPath?.let { File(it) }
            if (avatarFile != null && avatarFile.exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatarFile)
                        .crossfade(true)
                        .build(),
                    contentDescription = member.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Text(member.emoji, fontSize = 28.sp)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                member.name,
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
            )
            Text(
                "${member.role} · ${member.canChi ?: ""} ${member.birthYear ?: ""} · Mệnh ${member.menh ?: ""}",
                style = TextStyle(fontSize = 12.sp, color = c.textSecondary),
                modifier = Modifier.padding(top = 2.dp)
            )
            if (member.deathYear != null && member.birthYear != null) {
                Text(
                    "${member.birthYear} — ${member.deathYear} (thọ ${member.deathYear - member.birthYear} tuổi)",
                    style = TextStyle(fontSize = 11.sp, color = c.outline),
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }
        Icon(Icons.Filled.ChevronRight, null, tint = c.outlineVariant, modifier = Modifier.size(22.dp))
    }
    Spacer(modifier = Modifier.height(12.dp))
}

// ══════════════════════════════════════════
// OFFERING GRID
// ══════════════════════════════════════════
@Composable
private fun OfferingGrid(c: LichSoColors) {
    val offerings = listOf(
        "🪔" to "Hương", "🌸" to "Hoa", "🍵" to "Nước / Trà", "🍊" to "Quả",
        "🍚" to "Cơm", "🍗" to "Gà", "🍶" to "Rượu", "🪙" to "Vàng mã"
    )

    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 4
    ) {
        offerings.forEach { (emoji, name) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(c.surfaceContainer, RoundedCornerShape(14.dp))
                    .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
                    .padding(10.dp, 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(3.dp))
                Text(name, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium, color = c.textSecondary))
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}

// ══════════════════════════════════════════
// CHECKLIST ITEM
// ══════════════════════════════════════════
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChecklistItem(item: MemorialChecklistEntity, c: LichSoColors, onToggle: () -> Unit, onDelete: () -> Unit = {}) {
    var showDeleteOption by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.surfaceContainer, RoundedCornerShape(14.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
            .combinedClickable(onClick = onToggle, onLongClick = { showDeleteOption = !showDeleteOption })
            .padding(13.dp, 13.dp, 14.dp, 13.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .then(
                    if (item.isDone) Modifier.background(Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                    else Modifier.border(2.dp, c.outlineVariant, RoundedCornerShape(8.dp))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (item.isDone) {
                Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        Text(
            item.text,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (item.isDone) c.outline else c.textPrimary,
                textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
            ),
            modifier = Modifier.weight(1f)
        )
        if (showDeleteOption) {
            Icon(
                Icons.Filled.Close, "Xóa",
                tint = Color(0xFFEF5350),
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onDelete)
            )
        }
    }
}

// ══════════════════════════════════════════
// PRAYER LINK
// ══════════════════════════════════════════
@Composable
private fun PrayerLinkCard(c: LichSoColors, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(listOf(c.primary, Color(0xFFC62828))),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("📖", fontSize = 32.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Văn khấn cúng giỗ",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            )
            Text(
                "Bài khấn cúng giỗ Ông Bà truyền thống",
                style = TextStyle(fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f)),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Icon(Icons.Filled.ChevronRight, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(22.dp))
    }
    Spacer(modifier = Modifier.height(12.dp))
}

// ══════════════════════════════════════════
// REMINDER CARD
// ══════════════════════════════════════════
@Composable
private fun ReminderCard(title: String, desc: String, checked: Boolean, c: LichSoColors, onCheckedChange: (Boolean) -> Unit = {}) {
    var isChecked by remember { mutableStateOf(checked) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .padding(14.dp, 14.dp, 16.dp, 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.NotificationsActive, null, tint = Color(0xFF1565C0), modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary))
            Text(desc, style = TextStyle(fontSize = 11.sp, color = c.textSecondary), modifier = Modifier.padding(top = 1.dp))
        }
        Switch(
            checked = isChecked,
            onCheckedChange = { isChecked = it; onCheckedChange(it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4CAF50),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = c.outlineVariant,
            )
        )
    }
}

// ══════════════════════════════════════════
// NOTE CARD
// ══════════════════════════════════════════
@Composable
private fun MemorialNoteCard(text: String, dateInfo: String, c: LichSoColors, onEditClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .padding(14.dp, 14.dp, 16.dp, 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(dateInfo, style = TextStyle(fontSize = 10.sp, color = c.outline))
            Icon(
                Icons.Filled.Edit, null, tint = c.primary,
                modifier = Modifier.size(18.dp).clickable { onEditClick() }
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text, style = TextStyle(fontSize = 13.sp, color = c.textPrimary, lineHeight = 20.sp))
    }
    Spacer(modifier = Modifier.height(12.dp))
}

// ══════════════════════════════════════════
// QUOTE CARD
// ══════════════════════════════════════════
@Composable
private fun QuoteCard(c: LichSoColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFFDE7))),
                RoundedCornerShape(16.dp)
            )
            .border(1.dp, Color(0xFFFFE082), RoundedCornerShape(16.dp))
            .padding(16.dp, 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "❝ Cây có cội, nước có nguồn. Kính nhớ ông bà, tổ tiên là đạo làm người ❞",
            style = TextStyle(
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = c.textSecondary,
                fontFamily = FontFamily.Serif,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
            )
        )
    }
}

// ══════════════════════════════════════════
// SECTION TITLE
// ══════════════════════════════════════════
@Composable
private fun MemorialSectionTitle(title: String, icon: ImageVector, c: LichSoColors) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 18.dp, bottom = 10.dp, start = 4.dp)
    ) {
        Icon(icon, null, tint = c.primary, modifier = Modifier.size(18.dp))
        Text(
            title,
            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
        )
    }
}

// ══════════════════════════════════════════
// EDIT MEMORIAL DATE DIALOG
// ══════════════════════════════════════════
@Composable
private fun EditMemorialDateDialog(
    currentLunarDay: Int,
    currentLunarMonth: Int,
    onDismiss: () -> Unit,
    onSave: (day: Int, month: Int) -> Unit,
) {
    var dayText by remember { mutableStateOf(currentLunarDay.toString()) }
    var monthText by remember { mutableStateOf(currentLunarMonth.toString()) }

    val validDay = dayText.toIntOrNull()?.let { it in 1..30 } == true
    val validMonth = monthText.toIntOrNull()?.let { it in 1..12 } == true

    LichSoDialog(
        onDismiss = onDismiss,
        title = "Sửa ngày giỗ (Âm lịch)",
        icon = Icons.Filled.EditCalendar,
        iconTint = Color(0xFFE65100),
        iconBgColor = Color(0xFFFFF3E0),
        confirmText = "Lưu",
        confirmColor = Color(0xFFE65100),
        confirmEnabled = validDay && validMonth,
        onConfirm = {
            val day = dayText.toIntOrNull()
            val month = monthText.toIntOrNull()
            if (day != null && month != null && day in 1..30 && month in 1..12) {
                onSave(day, month)
            }
        },
    ) {
        Text(
            "Nhập ngày và tháng âm lịch",
            style = TextStyle(fontSize = 13.sp, color = Color.Gray)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = dayText,
                onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) dayText = it },
                label = { Text("Ngày", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(fontSize = 15.sp)
            )
            OutlinedTextField(
                value = monthText,
                onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) monthText = it },
                label = { Text("Tháng", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(fontSize = 15.sp)
            )
        }
    }
}
