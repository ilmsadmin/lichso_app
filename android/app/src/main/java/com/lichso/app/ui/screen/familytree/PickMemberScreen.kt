package com.lichso.app.ui.screen.familytree

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lichso.app.ui.theme.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ══════════════════════════════════════════════════════════════
// Pick Member Screen
// Based on v2/screen-pick-member.html
// ══════════════════════════════════════════════════════════════

@Composable
fun PickMemberScreen(
    viewModel: FamilyTreeViewModel,
    onBack: () -> Unit,
    onMemberPicked: (FamilyMember) -> Unit,
) {
    val c = LichSoThemeColors.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val members = uiState.members

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Tất cả") }
    var selectedMemberId by remember { mutableStateOf<String?>(null) }

    val filters = listOf("Tất cả") +
            (1..(uiState.totalGenerations)).map { "Đời $it" } +
            listOf("Nam", "Nữ")

    val filteredMembers = members.filter { member ->
        val matchesSearch = searchQuery.isBlank() || member.name.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when {
            selectedFilter == "Tất cả" -> true
            selectedFilter == "Nam" -> member.gender == Gender.MALE
            selectedFilter == "Nữ" -> member.gender == Gender.FEMALE
            selectedFilter.startsWith("Đời ") -> {
                val gen = selectedFilter.removePrefix("Đời ").toIntOrNull()
                gen != null && member.generation == gen
            }
            else -> true
        }
        matchesSearch && matchesFilter
    }

    val grouped = filteredMembers.groupBy { it.generation }
    val generations = grouped.keys.sorted()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .imePadding()
    ) {
        // ── Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
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
                Icon(Icons.Filled.Close, "Đóng", tint = c.textPrimary)
            }
            Column {
                Text(
                    "Chọn thành viên",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
                )
                Text(
                    "Chọn người liên kết trong gia phả",
                    style = TextStyle(fontSize = 12.sp, color = c.outline)
                )
            }
        }

        // ── Search bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(c.surfaceContainer, RoundedCornerShape(28.dp))
                .border(1.5.dp, c.outlineVariant, RoundedCornerShape(28.dp))
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Filled.Search, null, tint = c.outline, modifier = Modifier.size(20.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (searchQuery.isEmpty()) {
                    Text("Tìm theo tên...", style = TextStyle(fontSize = 14.sp, color = c.outline))
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = TextStyle(fontSize = 14.sp, color = c.textPrimary),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (searchQuery.isNotEmpty()) {
                Icon(
                    Icons.Filled.Close, "Xoá",
                    tint = c.outline,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { searchQuery = "" }
                )
            }
        }

        // ── Filter chips ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filters.forEach { filter ->
                val isActive = filter == selectedFilter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isActive) c.primary else c.bg,
                            RoundedCornerShape(16.dp)
                        )
                        .border(
                            1.5.dp,
                            if (isActive) c.primary else c.outlineVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        filter,
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isActive) Color.White else c.textSecondary
                        )
                    )
                }
            }
        }

        // ── Member list ──
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
        ) {
            generations.forEach { gen ->
                val genMembers = grouped[gen] ?: return@forEach

                item(key = "gen_label_$gen") {
                    Text(
                        viewModel.getGenerationLabel(gen).uppercase(),
                        style = TextStyle(
                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = c.primary, letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                    )
                }

                items(genMembers, key = { it.id }) { member ->
                    val isSelected = member.id == selectedMemberId

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) c.primaryContainer else Color.Transparent,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedMemberId = member.id }
                            .padding(12.dp, 12.dp, 14.dp, 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        val avatarBg = when {
                            member.isElder -> Brush.linearGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFE082)))
                            member.gender == Gender.MALE -> Brush.linearGradient(listOf(Color(0xFFE3F2FD), Color(0xFFE3F2FD)))
                            else -> Brush.linearGradient(listOf(Color(0xFFFCE4EC), Color(0xFFFCE4EC)))
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(avatarBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(member.emoji, fontSize = 22.sp)
                        }

                        // Info
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                member.name,
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
                            )
                            val yearStr = when {
                                member.deathYear != null -> "${member.birthYear}-${member.deathYear}"
                                member.birthYear != null -> "${member.birthYear}"
                                else -> ""
                            }
                            Text(
                                "${member.role} · $yearStr",
                                style = TextStyle(fontSize = 11.sp, color = c.textSecondary)
                            )
                        }

                        // Tags
                        if (member.deathYear != null) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (c.isDark) Color(0xFF424242) else Color(0xFFF5F5F5),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Đã mất", style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF757575)))
                            }
                        }
                        if (member.isSelf) {
                            Box(
                                modifier = Modifier
                                    .background(c.primaryContainer, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Tôi", style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = c.primary))
                            }
                        }

                        // Radio
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .border(
                                    2.dp,
                                    if (isSelected) c.primary else c.outlineVariant,
                                    CircleShape
                                )
                                .then(
                                    if (isSelected) Modifier.background(c.primary, CircleShape) else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // ── Bottom buttons ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(c.outlineVariant, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
                }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (selectedMemberId != null) c.primary else c.outlineVariant,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable(enabled = selectedMemberId != null) {
                        selectedMemberId?.let { id ->
                            members.find { it.id == id }?.let { onMemberPicked(it) }
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Text("Chọn", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }
    }
}
