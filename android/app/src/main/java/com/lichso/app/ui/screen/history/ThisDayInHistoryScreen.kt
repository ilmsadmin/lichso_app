package com.lichso.app.ui.screen.history

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.domain.model.*
import com.lichso.app.ui.theme.*
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.components.HeaderIconButton

// ══════════════════════════════════════════
// Màu header gradient xanh lá (từ mock HTML)
// ══════════════════════════════════════════
private val HeaderGreen1 = Color(0xFF1B5E20)
private val HeaderGreen2 = Color(0xFF2E7D32)
private val HeaderGreen3 = Color(0xFF388E3C)

// ══════════════════════════════════════════
// Màu category badge
// ══════════════════════════════════════════
private val VnBg = Color(0xFFFFEBEE)
private val VnText = Color(0xFFC62828)
private val WorldBg = Color(0xFFE3F2FD)
private val WorldText = Color(0xFF1565C0)
private val CultureBg = Color(0xFFFFF8E1)
private val CultureText = Color(0xFFF57F17)
private val ScienceBg = Color(0xFFE8F5E9)
private val ScienceText = Color(0xFF2E7D32)

// ══════════════════════════════════════════
// Màu timeline dot
// ══════════════════════════════════════════
private val DotWorld = Color(0xFF1565C0)
private val DotCulture = Color(0xFFD4A017)

@Composable
fun ThisDayInHistoryScreen(
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    viewModel: ThisDayInHistoryViewModel = hiltViewModel()
) {
    val c = LichSoThemeColors.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        // ═══ GREEN GRADIENT HEADER ═══
        HistoryHeader(
            dayDisplay = uiState.dayDisplay,
            monthDisplay = uiState.monthDisplay,
            onBackClick = onBackClick,
            onShareClick = {
                // Build share text from events
                val sb = StringBuilder()
                sb.appendLine("📅 Ngày này năm xưa — ${uiState.fullDateDisplay}")
                sb.appendLine()
                uiState.events.forEachIndexed { index, event ->
                    val yearsAgo = viewModel.yearsAgo(event.year)
                    sb.appendLine("${index + 1}. [${event.year}] ${event.title} ($yearsAgo năm trước)")
                    if (event.description.isNotBlank()) {
                        sb.appendLine("   ${event.description}")
                    }
                }
                sb.appendLine()
                sb.appendLine("— Lịch Sổ App")

                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Ngày này năm xưa — ${uiState.fullDateDisplay}")
                    putExtra(android.content.Intent.EXTRA_TEXT, sb.toString())
                }
                context.startActivity(android.content.Intent.createChooser(shareIntent, "Chia sẻ"))
            }
        )

        // ═══ DATE PICKER ROW ═══
        DatePickerRow(
            dateDisplay = uiState.fullDateDisplay,
            onPrevDay = { viewModel.prevDay() },
            onNextDay = { viewModel.nextDay() }
        )

        // ═══ TIMELINE CONTENT ═══
        if (uiState.events.isEmpty()) {
            EmptyState()
        } else {
            TimelineContent(
                events = uiState.events,
                yearsAgo = { viewModel.yearsAgo(it) }
            )
        }
    }
}

// ══════════════════════════════════════════
// HEADER (green gradient — matching HTML mock)
// ══════════════════════════════════════════
@Composable
private fun HistoryHeader(
    dayDisplay: String,
    monthDisplay: String,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit
) {
    AppTopBar(
        title = "Ngày này năm xưa",
        onBackClick = onBackClick,
        gradientColors = listOf(HeaderGreen1, HeaderGreen2, HeaderGreen3),
        actions = {
            HeaderIconButton(
                icon = Icons.Filled.Share,
                contentDescription = "Chia sẻ",
                onClick = onShareClick
            )
        },
        bottomContent = {
            // Big date display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    dayDisplay,
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 32.sp
                    )
                )
                Text(
                    monthDisplay,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
            }
        }
    )
}

// ══════════════════════════════════════════
// DATE PICKER ROW
// ══════════════════════════════════════════
@Composable
private fun DatePickerRow(
    dateDisplay: String,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit
) {
    val c = LichSoThemeColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Bottom border
                drawLine(
                    color = c.outlineVariant,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1f
                )
            }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DateArrowButton(
            icon = Icons.Filled.ChevronLeft,
            onClick = onPrevDay
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            dateDisplay,
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = c.textPrimary
            ),
            modifier = Modifier.widthIn(min = 160.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.width(16.dp))

        DateArrowButton(
            icon = Icons.Filled.ChevronRight,
            onClick = onNextDay
        )
    }
}

@Composable
private fun DateArrowButton(icon: ImageVector, onClick: () -> Unit) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .size(36.dp)
            .border(1.dp, c.outlineVariant, CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = c.textPrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ══════════════════════════════════════════
// TIMELINE CONTENT
// ══════════════════════════════════════════
@Composable
private fun TimelineContent(
    events: List<HistoricalEvent>,
    yearsAgo: (Int) -> Int
) {
    val c = LichSoThemeColors.current
    val timelineLineColor = c.outlineVariant

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        itemsIndexed(events) { index, event ->
            TimelineItem(
                event = event,
                yearsAgoValue = yearsAgo(event.year),
                isLast = index == events.lastIndex,
                lineColor = timelineLineColor
            )
        }
    }
}

@Composable
private fun TimelineItem(
    event: HistoricalEvent,
    yearsAgoValue: Int,
    isLast: Boolean,
    lineColor: Color
) {
    val c = LichSoThemeColors.current

    Row(modifier = Modifier.fillMaxWidth()) {
        // ── Timeline column (dot + line) ──
        Box(
            modifier = Modifier.width(34.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Vertical line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(lineColor)
                        .align(Alignment.TopCenter)
                )
            }

            // Dot
            TimelineDot(
                category = event.category,
                importance = event.importance
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // ── Card ──
        TimelineCard(
            event = event,
            yearsAgoValue = yearsAgoValue,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 20.dp)
        )
    }
}

@Composable
private fun TimelineDot(
    category: HistoryCategory,
    importance: EventImportance
) {
    val c = LichSoThemeColors.current

    val dotColor = when {
        importance == EventImportance.MAJOR && category == HistoryCategory.WORLD -> DotWorld
        importance == EventImportance.MAJOR && category == HistoryCategory.CULTURE -> DotCulture
        importance == EventImportance.MAJOR -> c.primary
        else -> Color.Transparent // minor → border only
    }

    val dotIcon = when {
        importance == EventImportance.MAJOR && category == HistoryCategory.VIETNAM -> Icons.Filled.Star
        importance == EventImportance.MAJOR && category == HistoryCategory.WORLD -> Icons.Filled.Public
        importance == EventImportance.MAJOR && category == HistoryCategory.CULTURE -> Icons.Filled.Palette
        importance == EventImportance.MAJOR && category == HistoryCategory.SCIENCE -> Icons.Filled.Science
        else -> Icons.Filled.Circle
    }

    Box(
        modifier = Modifier
            .size(22.dp)
            .then(
                if (importance == EventImportance.MINOR) {
                    Modifier
                        .background(c.bg, CircleShape)
                        .border(2.dp, c.outlineVariant, CircleShape)
                } else {
                    Modifier.background(dotColor, CircleShape)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            dotIcon,
            contentDescription = null,
            tint = if (importance == EventImportance.MAJOR) Color.White else c.outline,
            modifier = Modifier.size(if (importance == EventImportance.MAJOR) 12.dp else 10.dp)
        )
    }
}

@Composable
private fun TimelineCard(
    event: HistoricalEvent,
    yearsAgoValue: Int,
    modifier: Modifier = Modifier
) {
    val c = LichSoThemeColors.current

    Column(
        modifier = modifier
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { }
            .padding(14.dp)
    ) {
        // Year + "X năm trước"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "${event.year}",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.primary
                )
            )
            Text(
                "· $yearsAgoValue năm trước",
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.outline
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Title
        Text(
            event.title,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = c.textPrimary,
                lineHeight = 20.sp
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Description
        Text(
            event.description,
            style = TextStyle(
                fontSize = 12.sp,
                color = c.textSecondary,
                lineHeight = 18.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category badge
        CategoryBadge(category = event.category)

        // Image placeholder (for major events with images)
        if (event.hasImage) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFF5F5F5), Color(0xFFE0E0E0))
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Image,
                    contentDescription = null,
                    tint = c.outlineVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryBadge(category: HistoryCategory) {
    val (bgColor, textColor) = when (category) {
        HistoryCategory.VIETNAM -> VnBg to VnText
        HistoryCategory.WORLD -> WorldBg to WorldText
        HistoryCategory.CULTURE -> CultureBg to CultureText
        HistoryCategory.SCIENCE -> ScienceBg to ScienceText
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            category.emoji,
            style = TextStyle(fontSize = 10.sp)
        )
        Text(
            category.label,
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        )
    }
}

// ══════════════════════════════════════════
// EMPTY STATE
// ══════════════════════════════════════════
@Composable
private fun EmptyState() {
    val c = LichSoThemeColors.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(40.dp)
        ) {
            Icon(
                Icons.Filled.HistoryEdu,
                contentDescription = null,
                tint = c.outlineVariant,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Chưa có dữ liệu sự kiện\ncho ngày này",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = c.outline,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )
            )
        }
    }
}
