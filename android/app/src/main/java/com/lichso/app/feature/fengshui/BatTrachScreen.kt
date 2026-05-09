package com.lichso.app.feature.fengshui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.theme.LichSoThemeColors
import com.lichso.app.util.CanChiCalculator
import com.lichso.app.util.LunarCalendarUtil
import java.time.LocalDate

@Composable
fun BatTrachScreen(
    onBackClick: () -> Unit,
    onAskAiClick: (String) -> Unit = {}
) {
    val today = remember { LocalDate.now() }
    var dayText by remember { mutableStateOf(today.dayOfMonth.toString()) }
    var monthText by remember { mutableStateOf(today.monthValue.toString()) }
    var yearText by remember { mutableStateOf(today.year.toString()) }
    var gender by remember { mutableStateOf("Nam") }

    val result = remember(dayText, monthText, yearText, gender) {
        val day = dayText.toIntOrNull()
        val month = monthText.toIntOrNull()
        val year = yearText.toIntOrNull()

        if (day == null || month == null || year == null || day !in 1..31 || month !in 1..12 || year !in 1900..2100) {
            null
        } else {
            val lunar = LunarCalendarUtil.convertSolar2Lunar(day, month, year)
            val yearCanChi = CanChiCalculator.getYearCanChi(lunar.lunarYear)
            val cungPhi = calculateCungPhi(lunar.lunarYear, gender)
            BatTrachResult(
                solarDay = day,
                solarMonth = month,
                solarYear = year,
                lunarYear = lunar.lunarYear,
                yearCanChi = yearCanChi,
                cungPhi = cungPhi
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(bottom = 24.dp)
    ) {
        AppTopBar(
            title = "Bát trạch",
            subtitle = "Xem cung phi và nhóm hướng tốt theo tuổi",
            onBackClick = onBackClick,
            actions = {
                OutlinedButton(onClick = {
                    onAskAiClick(
                        "Hãy phân tích bát trạch và gợi ý hướng tốt theo tuổi, mệnh và giới tính của tôi."
                    )
                }) {
                    Text("AI")
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = LichSoThemeColors.current.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Nhập ngày sinh",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dayText,
                        onValueChange = { dayText = it.filter { ch -> ch.isDigit() }.take(2) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Ngày") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = monthText,
                        onValueChange = { monthText = it.filter { ch -> ch.isDigit() }.take(2) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tháng") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = yearText,
                        onValueChange = { yearText = it.filter { ch -> ch.isDigit() }.take(4) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Năm") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Giới tính",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (gender == "Nam") {
                        Button(onClick = { gender = "Nam" }) { Text("Nam") }
                        OutlinedButton(onClick = { gender = "Nữ" }) { Text("Nữ") }
                        OutlinedButton(onClick = { gender = "Khác" }) { Text("Khác") }
                    } else if (gender == "Nữ") {
                        OutlinedButton(onClick = { gender = "Nam" }) { Text("Nam") }
                        Button(onClick = { gender = "Nữ" }) { Text("Nữ") }
                        OutlinedButton(onClick = { gender = "Khác" }) { Text("Khác") }
                    } else {
                        OutlinedButton(onClick = { gender = "Nam" }) { Text("Nam") }
                        OutlinedButton(onClick = { gender = "Nữ" }) { Text("Nữ") }
                        Button(onClick = { gender = "Khác" }) { Text("Khác") }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (result == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = LichSoThemeColors.current.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Chưa đủ dữ liệu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hãy nhập ngày, tháng, năm sinh hợp lệ để xem cung phi và nhóm hướng tốt.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LichSoThemeColors.current.textSecondary,
                        lineHeight = 22.sp
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = LichSoThemeColors.current.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Kết quả Bát trạch",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${result.yearCanChi} • Cung ${result.cungPhi.cungName}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${result.cungPhi.groupLabel} · ${result.cungPhi.genderLabel}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.cungPhi.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LichSoThemeColors.current.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = LichSoThemeColors.current.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Hướng tốt nên ưu tiên",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    result.cungPhi.favorableDirections.forEach { direction ->
                        DirectionBullet(text = direction)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = LichSoThemeColors.current.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Hướng nên cân nhắc tránh",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    result.cungPhi.unfavorableDirections.forEach { direction ->
                        DirectionBullet(text = direction)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = LichSoThemeColors.current.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Lưu ý",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Màn này dùng quy tắc cơ bản để lọc nhóm hướng. Nếu muốn tư vấn phong thủy nhà cụ thể, bạn có thể mở AI từ nút trên cùng để đi sâu hơn.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LichSoThemeColors.current.textSecondary,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private data class BatTrachResult(
    val solarDay: Int,
    val solarMonth: Int,
    val solarYear: Int,
    val lunarYear: Int,
    val yearCanChi: String,
    val cungPhi: CungPhiResult
)

@Composable
private fun DirectionBullet(text: String) {
    Text(
        text = "• $text",
        style = MaterialTheme.typography.bodyMedium,
        color = LichSoThemeColors.current.textSecondary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
