package com.lichso.app.feature.birthplanner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.components.LichSoDatePickerDialog
import com.lichso.app.ui.theme.LichSoThemeColors
import com.lichso.app.util.LunarCalendarUtil
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DF: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private data class BirthCandidate(
    val date: LocalDate,
    val score: Int,
    val reason: String,
)

@Composable
fun BirthDatePlannerScreen(onBackClick: () -> Unit = {}) {
    val c = LichSoThemeColors.current
    var dueDate by remember { mutableStateOf(LocalDate.now().plusMonths(2)) }
    var rangeDays by remember { mutableStateOf(15) }
    var showDueDatePicker by remember { mutableStateOf(false) }

    val candidates = remember(dueDate, rangeDays) {
        (-rangeDays..rangeDays).map { offset ->
            val date = dueDate.plusDays(offset.toLong())
            val lunar = LunarCalendarUtil.convertSolar2Lunar(date.dayOfMonth, date.monthValue, date.year)
            val scoreBase = 60
            val score = scoreBase +
                (if (lunar.lunarDay == 1 || lunar.lunarDay == 15) 8 else 0) +
                (if (lunar.lunarDay in 6..10 || lunar.lunarDay in 16..20) 10 else 0) -
                (if (lunar.lunarDay in listOf(5, 14, 23)) 12 else 0)
            val reason = when {
                lunar.lunarDay in 6..10 || lunar.lunarDay in 16..20 -> "Ngày cân bằng, dễ xếp vào nhóm tốt"
                lunar.lunarDay in listOf(5, 14, 23) -> "Nên cân nhắc thêm giờ sinh phù hợp"
                else -> "Điểm trung tính, cần xem thêm giờ tốt"
            }
            BirthCandidate(date = date, score = score.coerceIn(0, 100), reason = reason)
        }.sortedByDescending { it.score }.take(10)
    }

    Column(modifier = Modifier.fillMaxSize().background(c.bg)) {
        AppTopBar(
            title = "Ngày sinh đẹp",
            subtitle = "Gợi ý ngày quanh dự sinh",
            onBackClick = onBackClick,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF3E0), RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFFFFCC80), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Color(0xFFE65100))
                            Text("Ngày dự sinh: ${dueDate.format(DF)}", style = TextStyle(fontSize = 14.sp, color = c.textPrimary, fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                "Chọn",
                                style = TextStyle(fontSize = 13.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold),
                                modifier = Modifier.clickable { showDueDatePicker = true }
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(7, 15, 30).forEach { d ->
                                val selected = d == rangeDays
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (selected) Color(0xFFE65100) else Color.White,
                                            RoundedCornerShape(18.dp)
                                        )
                                        .border(1.dp, Color(0xFFFFCC80), RoundedCornerShape(18.dp))
                                        .clickable { rangeDays = d }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        "±$d ngày",
                                        style = TextStyle(fontSize = 12.sp, color = if (selected) Color.White else Color(0xFFE65100), fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            items(candidates) { item ->
                BirthCandidateRow(item)
            }

            item {
                Text(
                    "Gợi ý này chỉ mang tính tham khảo truyền thống; cần ưu tiên an toàn mẹ và bé theo chỉ định bác sĩ.",
                    style = TextStyle(fontSize = 11.sp, color = c.textTertiary)
                )
            }
        }
    }

    if (showDueDatePicker) {
        LichSoDatePickerDialog(
            initialDate = dueDate,
            onDismiss = { showDueDatePicker = false },
            onDateSelected = {
                dueDate = it
                showDueDatePicker = false
            },
        )
    }
}

@Composable
private fun BirthCandidateRow(item: BirthCandidate) {
    val c = LichSoThemeColors.current
    val color = when {
        item.score >= 80 -> Color(0xFF2E7D32)
        item.score >= 65 -> Color(0xFFE65100)
        else -> Color(0xFF757575)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceContainer, RoundedCornerShape(14.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.date.format(DF), style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.textPrimary))
            Text(item.reason, style = TextStyle(fontSize = 12.sp, color = c.textSecondary))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Text("${item.score}", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color))
        }
    }
}
