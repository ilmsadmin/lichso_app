package com.lichso.app.ui.screen.familytree

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lichso.app.data.local.FamilyTreeRepository
import com.lichso.app.ui.theme.*
import com.lichso.app.ui.components.AppTopBar
import java.io.File

// ══════════════════════════════════════════════════════════════
// Add Member Screen
// Based on v2/screen-add-member.html
// ══════════════════════════════════════════════════════════════

@Composable
fun AddMemberScreen(
    viewModel: FamilyTreeViewModel,
    onBack: () -> Unit,
    editMemberId: String? = null,
) {
    val c = LichSoThemeColors.current
    val isEdit = editMemberId != null
    val existingMember = editMemberId?.let { viewModel.getMember(it) }

    val uiState = viewModel.uiState.collectAsState().value
    val isFirstMember = !isEdit && uiState.totalMembers == 0

    // ── Local form state ──
    var name by remember { mutableStateOf(existingMember?.name ?: "") }
    var isMale by remember { mutableStateOf(existingMember?.gender != Gender.FEMALE) }
    var selectedRelation by remember { mutableStateOf(
        if (isFirstMember) "" else (existingMember?.role ?: "Con trai")
    ) }
    val familyHometown = uiState.familyHometown
    var hometown by remember { mutableStateOf(existingMember?.hometown ?: familyHometown) }
    var occupation by remember { mutableStateOf(existingMember?.occupation ?: "") }
    var noteText by remember { mutableStateOf(existingMember?.note ?: "") }
    var isDeceased by remember { mutableStateOf(existingMember?.deathYear != null) }
    var birthDay by remember { mutableStateOf(existingMember?.birthDateLunar?.split("/")?.getOrNull(0) ?: "") }
    var birthMonth by remember { mutableStateOf(existingMember?.birthDateLunar?.split("/")?.getOrNull(1) ?: "") }
    var birthYear by remember { mutableStateOf(existingMember?.birthYear?.toString() ?: "") }
    var deathDay by remember { mutableStateOf(existingMember?.deathDateLunar?.split("/")?.getOrNull(0) ?: "") }
    var deathMonth by remember { mutableStateOf(existingMember?.deathDateLunar?.split("/")?.getOrNull(1) ?: "") }
    var deathYear by remember { mutableStateOf(existingMember?.deathYear?.toString() ?: "") }
    var generation by remember { mutableIntStateOf(existingMember?.generation ?: 1) }
    var isElder by remember { mutableStateOf(existingMember?.isElder ?: false) }
    var isSelf by remember { mutableStateOf(existingMember?.isSelf ?: false) }
    var avatarPath by remember { mutableStateOf(existingMember?.avatarPath ?: "") }
    var avatarVersion by remember { mutableIntStateOf(0) } // force Coil to reload on change

    // ── Relationship members ──
    // For child relations: pick both father and mother
    var selectedFather by remember { mutableStateOf<FamilyMember?>(
        existingMember?.parentIds?.mapNotNull { viewModel.getMember(it) }
            ?.firstOrNull { it.gender == Gender.MALE }
    ) }
    var selectedMother by remember { mutableStateOf<FamilyMember?>(
        existingMember?.parentIds?.mapNotNull { viewModel.getMember(it) }
            ?.firstOrNull { it.gender == Gender.FEMALE }
    ) }
    // For spouse relations: pick one spouse
    var selectedSpouse by remember { mutableStateOf<FamilyMember?>(
        existingMember?.spouseIds?.firstOrNull()?.let { viewModel.getMember(it) }
    ) }

    // Member ID for avatar saving
    val memberId = remember { existingMember?.id ?: FamilyTreeRepository.generateId() }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.saveAvatarFromUri(it, memberId) { savedPath ->
                avatarPath = savedPath
                avatarVersion++  // force Coil cache invalidation
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .imePadding()
    ) {
        // ── Header ──
        AppTopBar(
            title = if (isEdit) "Sửa thành viên" else "Thêm thành viên",
            onBackClick = onBack,
            leadingIcon = Icons.Filled.Close
        )

        // ── Scrollable content ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ── Avatar upload ──
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                val hasAvatar = avatarPath.isNotEmpty() && File(avatarPath).exists()

                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    // Circle background + border (clipped separately)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(c.surfaceContainerHigh, CircleShape)
                            .border(width = 2.dp, color = if (hasAvatar) c.primary else c.outlineVariant, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasAvatar) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(File(avatarPath))
                                    .memoryCacheKey("${avatarPath}_$avatarVersion")
                                    .diskCacheKey("${avatarPath}_$avatarVersion")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Text(
                                if (isMale) "👨" else "👩",
                                fontSize = 40.sp
                            )
                        }
                    }
                    // Camera/Edit badge (outside the clipped circle)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(c.primary, CircleShape)
                            .border(2.dp, c.bg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (hasAvatar) Icons.Filled.Edit else Icons.Filled.PhotoCamera,
                            null, tint = Color.White, modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (hasAvatar) "Chạm để đổi ảnh" else "Chạm để thêm ảnh",
                    style = TextStyle(fontSize = 12.sp, color = c.outline),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                if (hasAvatar) {
                    Text(
                        "Xóa ảnh",
                        style = TextStyle(fontSize = 11.sp, color = Color(0xFFD32F2F)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                viewModel.removeAvatar(memberId)
                                avatarPath = ""
                            }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // ═══ THÔNG TIN CƠ BẢN ═══
            FormSectionTitle("Thông tin cơ bản", Icons.Filled.Person, c)

            FormLabel("Họ và tên", required = true, c = c)
            FormInput(value = name, onValueChange = { name = it }, placeholder = "Ví dụ: Nguyễn Văn Nam", c = c)
            Spacer(modifier = Modifier.height(14.dp))

            FormLabel("Giới tính", required = true, c = c)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                GenderButton(
                    emoji = "👨", label = "Nam",
                    isActive = isMale,
                    activeBg = Color(0xFFE3F2FD), activeBorder = Color(0xFF64B5F6), activeText = Color(0xFF1565C0),
                    modifier = Modifier.weight(1f), c = c,
                    onClick = { isMale = true }
                )
                GenderButton(
                    emoji = "👩", label = "Nữ",
                    isActive = !isMale,
                    activeBg = Color(0xFFFCE4EC), activeBorder = Color(0xFFF48FB1), activeText = Color(0xFFC2185B),
                    modifier = Modifier.weight(1f), c = c,
                    onClick = { isMale = false }
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            // ═══ QUAN HỆ ═══
            if (!isFirstMember) {
            FormSectionTitle("Quan hệ", Icons.Filled.Group, c)

            FormLabel("Loại quan hệ", required = true, c = c)
            val relations = listOf("Con trai", "Con gái", "Vợ/Chồng")
            RelationPicker(relations, selectedRelation, c) { relation ->
                selectedRelation = relation
                // Auto-adjust generation when relation changes
                if (relation.isNotBlank()) {
                    val isChild = relation in listOf("Con trai", "Con gái")
                    if (isChild) {
                        // Use father's generation if available, else mother's
                        val refGen = selectedFather?.generation ?: selectedMother?.generation
                        if (refGen != null) generation = refGen + 1
                    } else {
                        // Spouse: same generation
                        selectedSpouse?.let { generation = it.generation }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            val isChildRelation = selectedRelation in listOf("Con trai", "Con gái")

            if (isChildRelation) {
                // ── Pick Father ──
                FormLabel("Cha (bắt buộc)", required = true, c = c)
                ConnectCard(
                    member = selectedFather,
                    placeholder = "Chọn cha",
                    subPlaceholder = "Chạm để chọn cha",
                    c = c,
                    onClick = {
                        viewModel.openPickMember(excludeId = memberId) { picked ->
                            selectedFather = picked
                            // Auto-adjust generation: child = max(father, mother) + 1
                            val fatherGen = picked.generation
                            val motherGen = selectedMother?.generation ?: fatherGen
                            generation = maxOf(fatherGen, motherGen) + 1
                        }
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))

                // ── Pick Mother ──
                FormLabel("Mẹ (bắt buộc)", required = true, c = c)
                ConnectCard(
                    member = selectedMother,
                    placeholder = "Chọn mẹ",
                    subPlaceholder = "Chạm để chọn mẹ",
                    c = c,
                    onClick = {
                        viewModel.openPickMember(excludeId = memberId) { picked ->
                            selectedMother = picked
                            // Auto-adjust generation
                            val motherGen = picked.generation
                            val fatherGen = selectedFather?.generation ?: motherGen
                            generation = maxOf(fatherGen, motherGen) + 1
                        }
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))
            } else if (selectedRelation == "Vợ/Chồng") {
                // ── Pick Spouse ──
                FormLabel("Chồng/Vợ", required = true, c = c)
                ConnectCard(
                    member = selectedSpouse,
                    placeholder = "Chọn chồng/vợ",
                    subPlaceholder = "Chạm để chọn người liên kết",
                    c = c,
                    onClick = {
                        viewModel.openPickMember(excludeId = memberId) { picked ->
                            selectedSpouse = picked
                            generation = picked.generation
                        }
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            } // end if (!isFirstMember)

            // Generation
            FormLabel("Đời (thế hệ)", c = c)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(c.surfaceContainer, CircleShape)
                        .border(1.dp, c.outlineVariant, CircleShape)
                        .clickable { if (generation > 1) generation-- },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Remove, null, tint = c.textPrimary, modifier = Modifier.size(18.dp))
                }
                Text(
                    "Đời $generation",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(c.surfaceContainer, CircleShape)
                        .border(1.dp, c.outlineVariant, CircleShape)
                        .clickable { generation++ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Add, null, tint = c.textPrimary, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Flags
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FlagChip("Bậc trưởng lão", isElder, c) { isElder = it }
                FlagChip("Bản thân", isSelf, c) { isSelf = it }
            }
            Spacer(modifier = Modifier.height(14.dp))

            // ═══ NGÀY SINH ═══
            FormSectionTitle("Ngày sinh", Icons.Filled.Cake, c)

            FormLabel("Ngày sinh (Dương lịch)", c = c)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DateSelect("Ngày", birthDay, { birthDay = it }, Modifier.weight(1f), c)
                DateSelect("Tháng", birthMonth, { birthMonth = it }, Modifier.weight(1f), c)
                DateSelect("Năm", birthYear, { birthYear = it }, Modifier.weight(1f), c)
            }
            Spacer(modifier = Modifier.height(14.dp))

            CanChiResultCard(c, birthYear.toIntOrNull()?.let { viewModel.calculateCanChi(it) })

            // ── Deceased toggle ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.surfaceContainer, RoundedCornerShape(14.dp))
                    .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
                    .padding(14.dp, 14.dp, 16.dp, 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Filled.Church, null, tint = c.outline, modifier = Modifier.size(20.dp))
                    Text("Đã mất?", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary))
                }
                Switch(
                    checked = isDeceased,
                    onCheckedChange = { isDeceased = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = c.primary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = c.outlineVariant,
                    )
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            if (isDeceased) {
                FormLabel("Ngày mất (Âm lịch)", c = c)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DateSelect("Ngày", deathDay, { deathDay = it }, Modifier.weight(1f), c)
                    DateSelect("Tháng", deathMonth, { deathMonth = it }, Modifier.weight(1f), c)
                    DateSelect("Năm", deathYear, { deathYear = it }, Modifier.weight(1f), c)
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // ═══ THÔNG TIN BỔ SUNG ═══
            FormSectionTitle("Thông tin bổ sung", Icons.Filled.MoreHoriz, c)

            FormLabel("Quê quán", c = c)
            FormInput(value = hometown, onValueChange = { hometown = it }, placeholder = "Ví dụ: Hà Nam", c = c)
            Spacer(modifier = Modifier.height(14.dp))

            FormLabel("Nghề nghiệp", c = c)
            FormInput(value = occupation, onValueChange = { occupation = it }, placeholder = "Ví dụ: Giáo viên", c = c)
            Spacer(modifier = Modifier.height(14.dp))

            FormLabel("Ghi chú", c = c)
            FormTextArea(value = noteText, onValueChange = { noteText = it }, placeholder = "Ghi chú thêm về thành viên này...", c = c)

            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── Bottom bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.bg)
                .drawBehind {
                    drawLine(c.outlineVariant, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
                }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Cancel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.surfaceContainer, RoundedCornerShape(14.dp))
                    .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
                    .clickable(onClick = onBack)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Hủy", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.textSecondary))
            }

            // Save
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF4E342E), Color(0xFF3E2723))),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable {
                        if (name.isNotBlank()) {
                            // Use selectedRelation, or fall back to existing role / sensible default
                            val effectiveRelation = when {
                                selectedRelation.isNotBlank() -> selectedRelation
                                existingMember != null -> existingMember.role
                                else -> "Trưởng tộc" // first member
                            }
                            val emoji = when {
                                isElder && isMale -> "👴"
                                isElder && !isMale -> "👵"
                                isSelf -> "🧑"
                                isMale -> "👨"
                                else -> "👩"
                            }
                            val parentIds = when (effectiveRelation) {
                                "Con trai", "Con gái" -> {
                                    val ids = mutableListOf<String>()
                                    selectedFather?.let { ids.add(it.id) }
                                    selectedMother?.let { mother ->
                                        if (mother.id !in ids) ids.add(mother.id)
                                    }
                                    ids
                                }
                                else -> existingMember?.parentIds ?: emptyList()
                            }
                            // Multi-spouse support: build spouseIds list
                            val spouseIds = when (effectiveRelation) {
                                "Vợ/Chồng" -> selectedSpouse?.let { listOf(it.id) } ?: emptyList()
                                else -> existingMember?.spouseIds ?: emptyList()
                            }
                            // Determine spouse order for the new wife
                            val spouseOrder = when (effectiveRelation) {
                                "Vợ/Chồng" -> {
                                    // Count existing wives of the connected member
                                    val existingWifeCount = selectedSpouse?.spouseIds?.size ?: 0
                                    existingWifeCount + 1
                                }
                                else -> existingMember?.spouseOrder ?: 0
                            }
                            val birthYearInt = birthYear.toIntOrNull()
                            val deathYearInt = if (isDeceased) deathYear.toIntOrNull() else null
                            val birthLunar = if (birthDay.isNotBlank() && birthMonth.isNotBlank() && birthYear.isNotBlank())
                                "$birthDay/$birthMonth/$birthYear" else null
                            val deathLunar = if (isDeceased && deathDay.isNotBlank() && deathMonth.isNotBlank() && deathYear.isNotBlank())
                                "$deathDay/$deathMonth/$deathYear" else null

                            // Compute Can Chi fields from birth year
                            val canChiInfo = birthYearInt?.let { viewModel.calculateCanChi(it) }

                            val member = FamilyMember(
                                id = memberId,
                                name = name,
                                role = effectiveRelation,
                                gender = if (isMale) Gender.MALE else Gender.FEMALE,
                                generation = generation,
                                birthYear = birthYearInt,
                                deathYear = deathYearInt,
                                birthDateLunar = birthLunar,
                                deathDateLunar = deathLunar,
                                canChi = canChiInfo?.yearCanChi,
                                menh = canChiInfo?.menh,
                                zodiacEmoji = canChiInfo?.zodiacEmoji,
                                menhEmoji = canChiInfo?.menhEmoji,
                                hanhEmoji = canChiInfo?.hanhEmoji,
                                menhDetail = canChiInfo?.napAm,
                                zodiacName = canChiInfo?.zodiacName,
                                menhName = "Mệnh ${canChiInfo?.menh ?: ""}",
                                hometown = hometown.ifBlank { null },
                                occupation = occupation.ifBlank { null },
                                isSelf = isSelf,
                                isElder = isElder,
                                emoji = emoji,
                                spouseIds = spouseIds,
                                spouseOrder = spouseOrder,
                                parentIds = parentIds,
                                note = noteText.ifBlank { null },
                                avatarPath = avatarPath.ifBlank { null },
                            )
                            viewModel.saveMember(member)

                            // Memorial is auto-synced inside saveMember()

                            // If spouse relation, also update the connected member's spouseIds
                            if (effectiveRelation == "Vợ/Chồng" && selectedSpouse != null) {
                                val updatedSpouseIds = if (selectedSpouse!!.spouseIds.contains(member.id))
                                    selectedSpouse!!.spouseIds
                                else
                                    selectedSpouse!!.spouseIds + member.id
                                val updatedSpouse = selectedSpouse!!.copy(spouseIds = updatedSpouseIds)
                                viewModel.saveMember(updatedSpouse)
                            }
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.Check, null, tint = Color(0xFFD4A017), modifier = Modifier.size(18.dp))
                    Text(
                        if (isEdit) "Cập nhật" else "Lưu thành viên",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD4A017))
                    )
                }
            }
        }
    }
}

/**
 * Compute the generation of the new member based on the connected member's
 * generation and the chosen relationship.
 */
private fun computeGeneration(connectedGeneration: Int, relation: String): Int {
    return when (relation) {
        "Con trai", "Con gái" -> connectedGeneration + 1
        // Vợ/Chồng → same generation
        else                  -> connectedGeneration
    }
}

// ══════════════════════════════════════════
// FORM COMPONENTS
// ══════════════════════════════════════════

@Composable
private fun FormSectionTitle(title: String, icon: ImageVector, c: LichSoColors) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
    ) {
        Icon(icon, null, tint = c.primary, modifier = Modifier.size(16.dp))
        Text(
            title.uppercase(),
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = c.primary, letterSpacing = 0.5.sp)
        )
    }
}

@Composable
private fun FormLabel(label: String, required: Boolean = false, c: LichSoColors) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        Text(label, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary))
        if (required) {
            Text("*", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.primary))
        }
    }
}

@Composable
private fun FormInput(value: String, onValueChange: (String) -> Unit, placeholder: String, c: LichSoColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceContainer, RoundedCornerShape(14.dp))
            .border(1.5.dp, c.outlineVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        if (value.isEmpty()) {
            Text(placeholder, style = TextStyle(fontSize = 14.sp, color = c.outline))
        }
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 14.sp, color = c.textPrimary),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FormTextArea(value: String, onValueChange: (String) -> Unit, placeholder: String, c: LichSoColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .background(c.surfaceContainer, RoundedCornerShape(14.dp))
            .border(1.5.dp, c.outlineVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        if (value.isEmpty()) {
            Text(placeholder, style = TextStyle(fontSize = 14.sp, color = c.outline))
        }
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 14.sp, color = c.textPrimary),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun GenderButton(
    emoji: String, label: String, isActive: Boolean,
    activeBg: Color, activeBorder: Color, activeText: Color,
    modifier: Modifier, c: LichSoColors, onClick: () -> Unit
) {
    val bg = if (isActive) activeBg else c.surfaceContainer
    val border = if (isActive) activeBorder else c.outlineVariant
    val text = if (isActive) activeText else c.textSecondary

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg, RoundedCornerShape(14.dp))
            .border(1.5.dp, border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = text))
    }
}

@Composable
private fun FlagChip(label: String, isActive: Boolean, c: LichSoColors, onToggle: (Boolean) -> Unit) {
    val bg = if (isActive) c.primaryContainer else c.surfaceContainer
    val border = if (isActive) c.primary else c.outlineVariant
    val textColor = if (isActive) c.primary else c.textSecondary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg, RoundedCornerShape(12.dp))
            .border(1.5.dp, border, RoundedCornerShape(12.dp))
            .clickable { onToggle(!isActive) }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isActive) {
            Icon(Icons.Filled.Check, null, tint = textColor, modifier = Modifier.size(14.dp))
        }
        Text(label, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor))
    }
}

@Composable
private fun ConnectCard(
    member: FamilyMember?,
    c: LichSoColors,
    onClick: () -> Unit,
    placeholder: String = "Chọn thành viên",
    subPlaceholder: String = "Chạm để chọn người liên kết"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.surfaceContainer, RoundedCornerShape(14.dp))
            .border(1.5.dp, c.outlineVariant, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp, 12.dp, 14.dp, 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (member != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (member.gender == Gender.MALE) Color(0xFFE3F2FD) else Color(0xFFFCE4EC),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(member.emoji, fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(member.name, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary))
                Text("${member.role} · Đời ${member.generation}", style = TextStyle(fontSize = 11.sp, color = c.textSecondary))
            }
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(c.surfaceContainerHigh, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PersonSearch, null, tint = c.outline, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(placeholder, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary))
                Text(subPlaceholder, style = TextStyle(fontSize = 11.sp, color = c.outline))
            }
        }
        Icon(Icons.Filled.SwapHoriz, null, tint = c.primary, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun RelationPicker(
    relations: List<String>, selected: String,
    c: LichSoColors, onSelect: (String) -> Unit
) {
    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        relations.forEach { rel ->
            val isActive = rel == selected
            val bg = if (isActive) c.primary else c.surfaceContainer
            val textColor = if (isActive) Color.White else c.textSecondary
            val border = if (isActive) c.primary else c.outlineVariant

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg, RoundedCornerShape(12.dp))
                    .border(1.5.dp, border, RoundedCornerShape(12.dp))
                    .clickable {
                        // Allow deselecting by tapping the active item again
                        if (isActive) onSelect("") else onSelect(rel)
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(rel, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor))
            }
        }
    }
}

@Composable
private fun DateSelect(
    label: String, value: String, onValueChange: (String) -> Unit,
    modifier: Modifier, c: LichSoColors
) {
    Box(
        modifier = modifier
            .background(c.surfaceContainer, RoundedCornerShape(14.dp))
            .border(1.5.dp, c.outlineVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        if (value.isEmpty()) {
            Text(label, style = TextStyle(fontSize = 14.sp, color = c.outline))
        }
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 14.sp, color = c.textPrimary),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CanChiResultCard(c: LichSoColors, canChiInfo: FamilyTreeViewModel.CanChiInfo?) {
    if (canChiInfo == null) return

    val cardBg = if (c.isDark) Brush.linearGradient(listOf(Color(0xFF2E2510), Color(0xFF3A2E15)))
    else Brush.linearGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFFDE7)))
    val border = if (c.isDark) Color(0xFFFFE082).copy(alpha = 0.3f) else Color(0xFFFFE082)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardBg, RoundedCornerShape(16.dp))
            .border(1.5.dp, border, RoundedCornerShape(16.dp))
            .padding(14.dp, 14.dp, 14.dp, 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "✦ Tự động tính",
            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD4A017), letterSpacing = 0.5.sp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            "${canChiInfo.yearCanChi} · ${canChiInfo.napAm}",
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = c.textPrimary, fontFamily = FontFamily.Serif)
        )
        Text(
            "${canChiInfo.zodiacName} · ${canChiInfo.napAm}",
            style = TextStyle(fontSize = 11.sp, color = c.textSecondary),
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CanChiResultItem(canChiInfo.zodiacEmoji, "Con giáp", c)
            CanChiResultItem(canChiInfo.menhEmoji, "Mệnh ${canChiInfo.menh}", c)
            CanChiResultItem(canChiInfo.hanhEmoji, canChiInfo.hanhName, c)
        }
    }
    Spacer(modifier = Modifier.height(14.dp))
}

@Composable
private fun CanChiResultItem(emoji: String, text: String, c: LichSoColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(1.dp))
        Text(text, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary))
    }
}
