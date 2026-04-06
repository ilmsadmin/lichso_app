package com.lichso.app.ui.screen.familytree

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lichso.app.ui.theme.*
import com.lichso.app.ui.components.HeaderIconButton
import com.lichso.app.ui.components.LichSoConfirmDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ══════════════════════════════════════════════════════════════
// Member Detail Screen
// Based on v2/screen-member-detail.html
// ══════════════════════════════════════════════════════════════

@Composable
fun MemberDetailScreen(
    member: FamilyMember,
    viewModel: FamilyTreeViewModel,
    onBack: () -> Unit,
    onMemberClick: (String) -> Unit = {},
) {
    val c = LichSoThemeColors.current
    val isDeceased = member.deathYear != null
    val relationships = viewModel.getRelationshipsFor(member.id)
    val memorial = viewModel.getMemorialForMember(member.id)
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Photos
    val photos by viewModel.getPhotosFlow(member.id).collectAsStateWithLifecycle(initialValue = emptyList())
    var viewingPhoto by remember { mutableStateOf<MemberPhoto?>(null) }

    // Multi-photo picker
    val multiPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addMemberPhotos(uris, member.id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        // ── Hero ──
        MemberHero(member, isDeceased, onBack, onEdit = { viewModel.openEditMember(member.id) }, onDelete = { showDeleteDialog = true })

        // ── Scrollable content ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 0.dp)
        ) {
            // Stats row
            StatsRow(member, c)

            // Can Chi card
            if (member.canChi != null) {
                CanChiCard(member, c)
            }

            // Memorial card — auto-derived from death date
            if (isDeceased) {
                SectionTitle("Ngày giỗ", Icons.Filled.LocalFireDepartment, c)
                if (memorial != null) {
                    MemorialMiniCard(memorial, c, onClick = { viewModel.openMemorialDetail(memorial.id) })
                } else {
                    // Deceased but no lunar death date → hint to edit
                    MissingDeathDateHint(c)
                }
            }

            // Personal info
            SectionTitle("Thông tin cá nhân", Icons.Filled.Person, c)
            PersonalInfoGroup(member, c)

            // Relationships
            if (relationships.isNotEmpty()) {
                SectionTitle("Quan hệ gia đình", Icons.Filled.Group, c)
                relationships.forEach { rel ->
                    RelationshipCard(rel, c, onClick = { onMemberClick(rel.memberId) })
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Photos
            SectionTitle("Hình ảnh kỷ niệm", Icons.Filled.PhotoLibrary, c)
            PhotoGrid(
                photos = photos,
                c = c,
                onAddClick = { multiPhotoPickerLauncher.launch("image/*") },
                onPhotoClick = { viewingPhoto = it },
            )

            // Notes
            if (member.note != null) {
                SectionTitle("Ghi chú", Icons.Filled.EditNote, c)
                NoteCard(member.note, "Ghi bởi Nguyễn Văn An · 15/01/2024", c)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── Bottom action bar ──
        ActionBar(c, onEdit = { viewModel.openEditMember(member.id) }, onDelete = { showDeleteDialog = true })
    }

    // ── Delete confirmation dialog ──
    if (showDeleteDialog) {
        LichSoConfirmDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deleteMember(member.id)
                showDeleteDialog = false
            },
            title = "Xóa thành viên",
            message = "Bạn có chắc muốn xóa \"${member.name}\" khỏi gia phả? Hành động này không thể hoàn tác.",
            icon = Icons.Filled.PersonRemove,
            confirmText = "Xóa",
        )
    }

    // ── Photo viewer dialog ──
    viewingPhoto?.let { photo ->
        PhotoViewerDialog(
            photo = photo,
            c = c,
            onDismiss = { viewingPhoto = null },
            onDelete = {
                viewModel.deleteMemberPhoto(photo)
                viewingPhoto = null
            }
        )
    }
}

// ══════════════════════════════════════════
// HERO HEADER
// ══════════════════════════════════════════
@Composable
private fun MemberHero(member: FamilyMember, isDeceased: Boolean, onBack: () -> Unit, onEdit: () -> Unit = {}, onDelete: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF3E2723), Color(0xFF4E342E), Color(0xFF5D4037))
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
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

            Spacer(modifier = Modifier.height(20.dp))

            // Avatar
            Box(contentAlignment = Alignment.Center) {
                val avatarBg = when {
                    member.isElder -> Brush.linearGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFE082)))
                    member.gender == Gender.MALE -> Brush.linearGradient(listOf(Color(0xFFBBDEFB), Color(0xFF90CAF9)))
                    else -> Brush.linearGradient(listOf(Color(0xFFF8BBD0), Color(0xFFF48FB1)))
                }
                val hasAvatar = !member.avatarPath.isNullOrEmpty() && File(member.avatarPath).exists()
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(avatarBg, CircleShape)
                        .border(3.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
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
                        Text(member.emoji, fontSize = 44.sp)
                    }
                }
                // Deceased badge
                if (isDeceased) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF5D4037))
                            .border(2.dp, Color(0xFF3E2723), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✝", style = TextStyle(fontSize = 12.sp, color = Color.White))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name & role
            Text(
                member.name,
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            )
            Text(
                member.role,
                style = TextStyle(fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
            )

            // Chips
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                HeroChip("🏛️ Đời ${member.generation}", Color(0xFFD4A017).copy(alpha = 0.2f), Color(0xFFD4A017))
                if (isDeceased) {
                    HeroChip("✝ Đã mất · ${member.deathYear}", Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.7f))
                }
                if (member.isSelf) {
                    HeroChip("⭐ Bản thân", Color(0xFFB71C1C).copy(alpha = 0.3f), Color(0xFFFFCDD2))
                }
            }
        }
    }
}

@Composable
private fun HeroChip(label: String, bg: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(label, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = textColor))
    }
}

// ══════════════════════════════════════════
// STATS ROW
// ══════════════════════════════════════════
@Composable
private fun StatsRow(member: FamilyMember, c: LichSoColors) {
    val isDeceased = member.deathYear != null
    val age = if (isDeceased && member.birthYear != null && member.deathYear != null)
        member.deathYear - member.birthYear
    else if (member.birthYear != null)
        2026 - member.birthYear
    else null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (age != null) {
            StatCard(
                value = "$age",
                label = if (isDeceased) "Tuổi thọ" else "Tuổi",
                valueColor = c.primary,
                modifier = Modifier.weight(1f), c = c
            )
        }
        member.canChi?.let {
            StatCard(value = it, label = "Năm sinh", valueColor = Color(0xFFD4A017), modifier = Modifier.weight(1f), c = c)
        }
        member.menh?.let {
            StatCard(value = it, label = "Mệnh", valueColor = c.goodGreen, modifier = Modifier.weight(1f), c = c)
        }
    }
}

@Composable
private fun StatCard(
    value: String, label: String, valueColor: Color,
    modifier: Modifier = Modifier, c: LichSoColors
) {
    Column(
        modifier = modifier
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .padding(14.dp, 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor))
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium, color = c.outline))
    }
}

// ══════════════════════════════════════════
// CAN CHI CARD
// ══════════════════════════════════════════
@Composable
private fun CanChiCard(member: FamilyMember, c: LichSoColors) {
    val cardBg = if (c.isDark) Brush.linearGradient(listOf(Color(0xFF2E2510), Color(0xFF3A2E15)))
    else Brush.linearGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFFDE7)))
    val border = if (c.isDark) Color(0xFFFFE082).copy(alpha = 0.3f) else Color(0xFFFFE082)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardBg, RoundedCornerShape(20.dp))
            .border(1.5.dp, border, RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "✦ Thông tin âm lịch",
            style = TextStyle(
                fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = Color(0xFFD4A017), letterSpacing = 0.5.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))

        val mainText = buildString {
            append(member.canChi ?: "")
            member.menhDetail?.let { append(" · $it") }
        }
        Text(
            mainText,
            style = TextStyle(
                fontSize = 20.sp, fontWeight = FontWeight.Bold,
                color = c.textPrimary, fontFamily = FontFamily.Serif
            )
        )

        member.menhName?.let {
            Text(
                "${member.zodiacName} · $it",
                style = TextStyle(fontSize = 12.sp, color = c.textSecondary, lineHeight = 17.sp),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Emoji row
        if (member.zodiacEmoji != null || member.menhEmoji != null || member.hanhEmoji != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                member.zodiacEmoji?.let { CanChiItem(it, "Con giáp", c) }
                member.menhEmoji?.let { CanChiItem(it, "Mệnh ${member.menh ?: ""}", c) }
                member.hanhEmoji?.let { CanChiItem(it, "Hành ${member.menh ?: ""}", c) }
            }
        }
    }
}

@Composable
private fun CanChiItem(emoji: String, text: String, c: LichSoColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary))
    }
}

// ══════════════════════════════════════════
// MISSING DEATH DATE HINT
// ══════════════════════════════════════════
@Composable
private fun MissingDeathDateHint(c: LichSoColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("🕯️", fontSize = 28.sp)
        Text(
            "Chưa có ngày giỗ",
            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.outline)
        )
        Text(
            "Bổ sung ngày mất (âm lịch) khi chỉnh sửa thành viên để tự động tạo ngày giỗ",
            style = TextStyle(fontSize = 11.sp, color = c.outlineVariant, textAlign = TextAlign.Center)
        )
    }
}

// ══════════════════════════════════════════
// MEMORIAL MINI CARD
// ══════════════════════════════════════════
@Composable
private fun MemorialMiniCard(memorial: MemorialDay, c: LichSoColors, onClick: () -> Unit = {}) {
    val bg = if (c.isDark) Brush.linearGradient(listOf(Color(0xFF3A2A1B), Color(0xFF2E2510)))
    else Brush.linearGradient(listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)))
    val border = if (c.isDark) Color(0xFFFFCC80).copy(alpha = 0.3f) else Color(0xFFFFCC80)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(16.dp))
            .border(1.5.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp, 14.dp, 16.dp, 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🕯️", fontSize = 28.sp)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Giỗ ${memorial.relation.split("·").firstOrNull()?.trim() ?: ""}",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
            )
            Text(
                "${memorial.lunarDate} — ${memorial.solarDate}",
                style = TextStyle(fontSize = 12.sp, color = c.textSecondary),
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                "⏰ ${memorial.countdown}",
                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBF360C)),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFFE65100), modifier = Modifier.size(22.dp))
    }
}

// ══════════════════════════════════════════
// SECTION TITLE
// ══════════════════════════════════════════
@Composable
private fun SectionTitle(title: String, icon: ImageVector, c: LichSoColors) {
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
// PERSONAL INFO GROUP
// ══════════════════════════════════════════
@Composable
private fun PersonalInfoGroup(member: FamilyMember, c: LichSoColors) {
    data class InfoRow(val icon: ImageVector, val iconBg: Color, val iconTint: Color, val label: String, val value: String)

    val rows = buildList {
        add(InfoRow(Icons.Filled.Badge, Color(0xFFEFEBE9), Color(0xFF5D4037), "Họ và tên", member.name))
        add(InfoRow(
            Icons.Filled.Male, Color(0xFFE3F2FD), Color(0xFF1565C0),
            "Giới tính", if (member.gender == Gender.MALE) "Nam" else "Nữ"
        ))
        member.birthDateLunar?.let {
            add(InfoRow(Icons.Filled.Cake, Color(0xFFE8F5E9), Color(0xFF2E7D32), "Ngày sinh", "$it (Âm lịch)"))
        }
        member.deathDateLunar?.let {
            add(InfoRow(Icons.Filled.EventBusy, Color(0xFFFFEBEE), Color(0xFFC62828), "Ngày mất", "$it (Âm lịch)"))
        }
        member.hometown?.let {
            add(InfoRow(Icons.Filled.LocationOn, Color(0xFFF3E5F5), Color(0xFF7B1FA2), "Quê quán", it))
        }
        member.occupation?.let {
            add(InfoRow(Icons.Filled.Work, Color(0xFFFFF8E1), Color(0xFFF57F17), "Nghề nghiệp", it))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceContainer, RoundedCornerShape(20.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(20.dp))
    ) {
        rows.forEachIndexed { index, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (index < rows.lastIndex) Modifier.drawBehind {
                            drawLine(
                                color = c.outlineVariant,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        } else Modifier
                    )
                    .padding(13.dp, 13.dp, 16.dp, 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(row.iconBg, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(row.icon, null, tint = row.iconTint, modifier = Modifier.size(18.dp))
                }
                Text(row.label, style = TextStyle(fontSize = 12.sp, color = c.outline), modifier = Modifier.weight(1f))
                Text(
                    row.value,
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

// ══════════════════════════════════════════
// RELATIONSHIP CARD
// ══════════════════════════════════════════
@Composable
private fun RelationshipCard(rel: Relationship, c: LichSoColors, onClick: () -> Unit) {
    val avatarBg = when {
        rel.isElder -> Brush.linearGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFE082)))
        rel.gender == Gender.MALE -> Brush.linearGradient(listOf(Color(0xFFE3F2FD), Color(0xFFE3F2FD)))
        else -> Brush.linearGradient(listOf(Color(0xFFFCE4EC), Color(0xFFFCE4EC)))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp, 12.dp, 14.dp, 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(avatarBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(rel.emoji, fontSize = 20.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(rel.name, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary))
            Text(rel.role, style = TextStyle(fontSize = 11.sp, color = c.textSecondary))
        }

        Icon(Icons.Filled.ChevronRight, null, tint = c.outlineVariant, modifier = Modifier.size(20.dp))
    }
}

// ══════════════════════════════════════════
// PHOTO GRID
// ══════════════════════════════════════════
@Composable
private fun PhotoGrid(
    photos: List<MemberPhoto>,
    c: LichSoColors,
    onAddClick: () -> Unit,
    onPhotoClick: (MemberPhoto) -> Unit,
) {
    if (photos.isEmpty()) {
        // Empty state
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, c.outlineVariant, RoundedCornerShape(16.dp))
                .clickable(onClick = onAddClick)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Filled.AddPhotoAlternate, null, tint = c.outline, modifier = Modifier.size(32.dp))
            Text(
                "Thêm hình ảnh kỷ niệm",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.outline)
            )
            Text(
                "Chạm để chọn ảnh từ thư viện",
                style = TextStyle(fontSize = 11.sp, color = c.outlineVariant)
            )
        }
    } else {
        // Grid rows (3 columns)
        val rows = (photos + listOf(null)).chunked(3) // null = add button
        rows.forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                rowItems.forEach { photo ->
                    if (photo != null) {
                        PhotoThumbnail(
                            photo = photo,
                            c = c,
                            modifier = Modifier.weight(1f),
                            onClick = { onPhotoClick(photo) }
                        )
                    } else {
                        // Add photo button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, c.outlineVariant, RoundedCornerShape(12.dp))
                                .clickable(onClick = onAddClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.AddPhotoAlternate, null, tint = c.outline, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                // Fill remaining slots in last row
                val remaining = 3 - rowItems.size
                repeat(remaining) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PhotoThumbnail(
    photo: MemberPhoto,
    c: LichSoColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val file = File(photo.filePath)
    val exists = file.exists()

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(c.surfaceContainer, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (exists) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(file)
                    .size(300, 300)
                    .crossfade(true)
                    .build(),
                contentDescription = photo.caption ?: "Ảnh kỷ niệm",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
            )
        } else {
            Icon(Icons.Filled.BrokenImage, null, tint = c.outline, modifier = Modifier.size(24.dp))
        }
    }
}

// ══════════════════════════════════════════
// PHOTO VIEWER DIALOG
// ══════════════════════════════════════════
@Composable
private fun PhotoViewerDialog(
    photo: MemberPhoto,
    c: LichSoColors,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val dateStr = remember(photo.createdAt) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(photo.createdAt))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .clickable(onClick = onDismiss)
        ) {
            // Photo
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(File(photo.filePath))
                    .crossfade(true)
                    .build(),
                contentDescription = photo.caption ?: "Ảnh kỷ niệm",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )

            // Top bar: close + delete
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, "Đóng", tint = Color.White, modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Filled.Delete, "Xóa", tint = Color(0xFFEF5350), modifier = Modifier.size(24.dp))
                }
            }

            // Bottom: date info
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    "📅 Thêm lúc $dateStr",
                    style = TextStyle(fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                )
            }
        }
    }

    // Delete confirmation
    if (showDeleteConfirm) {
        LichSoConfirmDialog(
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
            title = "Xóa ảnh",
            message = "Bạn có chắc muốn xóa ảnh này? Hành động không thể hoàn tác.",
            icon = Icons.Filled.DeleteForever,
            iconTint = Color(0xFFD32F2F),
            iconBgColor = Color(0xFFFFEBEE),
            confirmText = "Xóa",
            confirmColor = Color(0xFFD32F2F),
        )
    }
}

// ══════════════════════════════════════════
// NOTE CARD
// ══════════════════════════════════════════
@Composable
private fun NoteCard(text: String, dateInfo: String, c: LichSoColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .padding(14.dp, 14.dp, 16.dp, 14.dp)
    ) {
        Text(dateInfo, style = TextStyle(fontSize = 10.sp, color = c.outline))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text, style = TextStyle(fontSize = 13.sp, color = c.textPrimary, lineHeight = 20.sp))
    }
}

// ══════════════════════════════════════════
// BOTTOM ACTION BAR
// ══════════════════════════════════════════
@Composable
private fun ActionBar(c: LichSoColors, onEdit: () -> Unit = {}, onDelete: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.bg)
            .drawBehind {
                drawLine(
                    color = c.outlineVariant,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Share button
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(c.surfaceContainer, RoundedCornerShape(14.dp))
                .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
                .clickable { }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.Share, null, tint = c.textPrimary, modifier = Modifier.size(18.dp))
                Text("Chia sẻ", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.textPrimary))
            }
        }

        // Edit button
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(listOf(Color(0xFF4E342E), Color(0xFF3E2723))),
                    RoundedCornerShape(14.dp)
                )
                .clickable(onClick = onEdit)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.Edit, null, tint = Color(0xFFD4A017), modifier = Modifier.size(18.dp))
                Text("Chỉnh sửa", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD4A017)))
            }
        }
    }
}
