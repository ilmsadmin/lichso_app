package com.lichso.app.ui.screen.templates

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.ui.theme.*

@Composable
fun TemplatesScreen(viewModel: TemplatesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredTemplates = viewModel.getFilteredTemplates()

    if (state.showDetail && state.detailResult != null) {
        TemplateDetailScreen(
            result = state.detailResult!!,
            viewModel = viewModel,
            onBack = { viewModel.closeDetail() }
        )
    } else {
        TemplateListScreen(
            selectedTab = state.selectedTab,
            templates = filteredTemplates,
            onTabSelected = { viewModel.selectTab(it) },
            onTemplateClick = { viewModel.openTemplateDetail(it) }
        )
    }
}

@Composable
private fun TemplateListScreen(
    selectedTab: TemplateTab,
    templates: List<TemplateItem>,
    onTabSelected: (TemplateTab) -> Unit,
    onTemplateClick: (TemplateItem) -> Unit
) {
    val c = LichSoThemeColors.current
    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mẫu tra cứu", style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 22.sp, color = c.gold2))
            }

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TabChip("Tất cả", selectedTab == TemplateTab.ALL) { onTabSelected(TemplateTab.ALL) }
                TabChip("Lễ nghi", selectedTab == TemplateTab.CEREMONY) { onTabSelected(TemplateTab.CEREMONY) }
                TabChip("Sự kiện", selectedTab == TemplateTab.EVENTS) { onTabSelected(TemplateTab.EVENTS) }
                TabChip("Phong thủy", selectedTab == TemplateTab.FENG_SHUI) { onTabSelected(TemplateTab.FENG_SHUI) }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Template Cards
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                templates.forEach { template ->
                    TemplateCard(template = template, onClick = { onTemplateClick(template) })
                }
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
private fun TemplateDetailScreen(
    result: TemplateDetailResult,
    viewModel: TemplatesViewModel,
    onBack: () -> Unit
) {
    val c = LichSoThemeColors.current
    var goodDays by remember { mutableStateOf<List<GoodDayResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = c.textSecondary, modifier = Modifier.size(18.dp))
            }
            Icon(
                imageVector = templateIconFor(result.template.iconName),
                contentDescription = null,
                tint = c.gold2,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = result.template.title,
                style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 18.sp, color = c.gold2),
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Today's analysis card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.bg2, RoundedCornerShape(14.dp))
                        .border(1.dp, c.border, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.Assessment, contentDescription = null, tint = c.gold2, modifier = Modifier.size(18.dp))
                        Text("Phân tích hôm nay", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.gold2, letterSpacing = 0.5.sp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = result.summary, style = TextStyle(fontSize = 13.sp, color = c.textPrimary, lineHeight = 20.sp))
                }
            }

            // Find good days button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isSearching) c.surface else c.goldDim,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (isSearching) c.border else c.gold.copy(alpha = 0.38f),
                            RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !isSearching) {
                            isSearching = true
                            goodDays = viewModel.findGoodDays(result.template)
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (isSearching) Icons.Filled.CheckCircle else Icons.Outlined.Search,
                            contentDescription = null,
                            tint = if (isSearching) c.teal2 else c.gold2,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isSearching) "Đã tìm ${goodDays.size} ngày tốt trong 30 ngày tới" else "Tìm ngày tốt trong 30 ngày tới",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSearching) c.teal2 else c.gold2
                            )
                        )
                    }
                }
            }

            // Good days results
            if (goodDays.isNotEmpty()) {
                item {
                    Text(
                        text = "NGÀY TỐT SẮP TỚI",
                        style = TextStyle(fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = c.textTertiary, letterSpacing = 1.sp),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(goodDays) { day -> GoodDayCard(day = day) }
            }

            // Empty state
            if (isSearching && goodDays.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.SearchOff, contentDescription = null, tint = c.textQuaternary, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Không tìm thấy ngày tốt\ntrong 30 ngày tới", style = TextStyle(fontSize = 13.sp, color = c.textTertiary), textAlign = TextAlign.Center)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun GoodDayCard(day: GoodDayResult) {
    val c = LichSoThemeColors.current
    val dayNum = day.date.dayOfMonth

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.bg2, RoundedCornerShape(12.dp))
            .border(1.dp, c.border, RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Date badge
        Column(
            modifier = Modifier
                .size(50.dp)
                .background(c.goldDim, RoundedCornerShape(10.dp))
                .border(1.dp, c.gold.copy(alpha = 0.28f), RoundedCornerShape(10.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("$dayNum", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.gold2))
            Text("T${day.date.monthValue}", style = TextStyle(fontSize = 10.sp, color = c.gold))
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(day.dayOfWeek, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary))
                if (day.daysFromNow == 0) {
                    Box(
                        modifier = Modifier
                            .background(c.tealDim, RoundedCornerShape(20.dp))
                            .border(1.dp, c.teal.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text("Hôm nay", style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = c.teal2))
                    }
                } else {
                    Text("· còn ${day.daysFromNow} ngày", style = TextStyle(fontSize = 11.sp, color = c.textTertiary))
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(12.dp))
                Text(day.dayCanChi, style = TextStyle(fontSize = 11.5.sp, color = c.textSecondary))
                Icon(Icons.Outlined.NightsStay, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(12.dp))
                Text(day.lunarStr, style = TextStyle(fontSize = 11.5.sp, color = c.textSecondary))
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(11.dp))
                Text(day.gioHoangDao, style = TextStyle(fontSize = 10.5.sp, color = c.textTertiary, lineHeight = 15.sp))
            }
        }

        // Check mark
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = c.teal2, modifier = Modifier.size(18.dp).padding(top = 2.dp))
    }
}

@Composable
private fun TabChip(label: String, isActive: Boolean, onClick: () -> Unit) {
    val c = LichSoThemeColors.current
    val bg = if (isActive) c.goldDim else c.bg3
    val borderColor = if (isActive) c.gold.copy(alpha = 0.38f) else c.border
    val textColor = if (isActive) c.gold2 else c.textTertiary

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(label, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor))
    }
}

@Composable
private fun TemplateCard(template: TemplateItem, onClick: () -> Unit) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.bg2, RoundedCornerShape(12.dp))
            .border(1.dp, c.border, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(c.bg3, RoundedCornerShape(10.dp))
                .border(1.dp, c.border, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = templateIconFor(template.iconName),
                contentDescription = null,
                tint = c.gold2,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(template.title, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary))
            Spacer(modifier = Modifier.height(3.dp))
            Text(template.description, style = TextStyle(fontSize = 11.5.sp, color = c.textTertiary, lineHeight = 16.sp), maxLines = 2)
            Spacer(modifier = Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                template.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(c.bg3, RoundedCornerShape(20.dp))
                            .border(1.dp, c.border, RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(tag, style = TextStyle(fontSize = 10.sp, color = c.textQuaternary))
                    }
                }
            }
        }

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = c.textQuaternary, modifier = Modifier.size(18.dp).padding(top = 2.dp))
    }
}

@Composable
private fun templateIconFor(iconName: String): ImageVector = when (iconName) {
    "favorite" -> Icons.Outlined.Favorite
    "home" -> Icons.Outlined.Home
    "store" -> Icons.Outlined.Store
    "directions_car" -> Icons.Outlined.DirectionsCar
    "flight" -> Icons.Outlined.Flight
    "school" -> Icons.Outlined.School
    "child_care" -> Icons.Outlined.ChildCare
    "temple_buddhist" -> Icons.Outlined.AccountBalance
    else -> Icons.Outlined.Article
}
