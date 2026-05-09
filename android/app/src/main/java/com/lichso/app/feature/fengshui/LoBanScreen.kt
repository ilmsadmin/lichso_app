package com.lichso.app.feature.fengshui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.theme.LichSoThemeColors

@Composable
fun LoBanScreen(
    onBackClick: () -> Unit,
    onAskAiClick: (String) -> Unit = {}
) {
    var selectedMode by remember { mutableStateOf(LoBanMode.DO_GIA_DUNG) }
    var lengthText by remember { mutableStateOf("81.0") }

    val lengthValue = lengthText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val result = evaluateLoBan(lengthValue, selectedMode)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(bottom = 24.dp)
    ) {
        AppTopBar(
            title = "Thước Lỗ Ban",
            subtitle = "Nhập số đo để xem cung cát lợi tương đối",
            onBackClick = onBackClick,
            actions = {
                OutlinedButton(onClick = {
                    onAskAiClick(
                        "Hãy giải thích thước Lỗ Ban và gợi ý kích thước cát lợi cho cửa chính, bếp, bàn thờ và giường ngủ."
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
                    text = "Chọn loại thước",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LoBanMode.entries.forEach { mode ->
                        if (mode == selectedMode) {
                            Button(onClick = { selectedMode = mode }) {
                                Text(mode.title)
                            }
                        } else {
                            OutlinedButton(onClick = { selectedMode = mode }) {
                                Text(mode.title)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LoBanRulerPreview(
                    mode = selectedMode,
                    result = result,
                    height = 320.dp
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
                    text = "Nhập số đo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = lengthText,
                    onValueChange = { next ->
                        val cleaned = next.filter { it.isDigit() || it == '.' || it == ',' }
                        if (cleaned.length <= 8) {
                            lengthText = cleaned
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Chiều dài (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Slider(
                    value = lengthValue.toFloat().coerceIn(0f, 200f),
                    onValueChange = { lengthText = String.format("%.1f", it) },
                    valueRange = 0f..200f
                )
                Text(
                    text = "Gợi ý: cửa chính, bàn thờ, giường ngủ thường cần kiểm tra nhiều lớp số đo khác nhau.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LichSoThemeColors.current.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (result.isAuspicious) {
                    LichSoThemeColors.current.surfaceContainer
                } else {
                    LichSoThemeColors.current.surfaceContainerHigh
                }
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (result.isAuspicious) "Kết quả cát lợi" else "Kết quả tham khảo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${String.format("%.1f", result.rawLengthCm)} cm • ${result.mode.title}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rơi vào cung ${result.zoneName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result.hint,
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
                    text = "Chu kỳ đang xét",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                InfoRow(label = "Chu kỳ", value = selectedMode.title)
                InfoRow(label = "Vị trí trong chu kỳ", value = "${String.format("%.1f", result.normalizedCm)} cm")
                InfoRow(label = "Mốc cung", value = "#${result.zoneIndex + 1} / 8")
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
                    text = "Mốc dùng nhanh",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Cửa chính: đo phủ bì theo khung thực tế.\n• Bàn thờ: ưu tiên số đo gọn, cân đối.\n• Giường ngủ: nên đo kích thước sử dụng thực tế, không chỉ phần khung.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LichSoThemeColors.current.textSecondary,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LoBanRulerPreview(
    mode: LoBanMode,
    result: LoBanResult,
    height: Dp
) {
    val colors = LichSoThemeColors.current
    val scrollState = rememberScrollState()
    val zoneNames = listOf(
        "Tài Lộc",
        "Bệnh Tật",
        "Ly Tán",
        "Nghĩa Đức",
        "Quan Lộc",
        "Kiếp Sát",
        "Họa Hại",
        "Bản Mệnh"
    )
    val goodZones = setOf(0, 3, 4, 7)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceContainer)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(10) { cycleIndex ->
                val highlightCurrentCycle = cycleIndex == 4
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    zoneNames.forEachIndexed { index, zoneName ->
                        val isGood = index in goodZones
                        val isCurrentZone = highlightCurrentCycle && index == result.zoneIndex
                        val containerColor = when {
                            isCurrentZone -> if (result.isAuspicious) {
                                colors.goodGreen.copy(alpha = 0.28f)
                            } else {
                                colors.badRed.copy(alpha = 0.22f)
                            }

                            isGood -> colors.goodGreen.copy(alpha = 0.14f)
                            else -> colors.badRed.copy(alpha = 0.09f)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(containerColor)
                                .border(
                                    width = if (isCurrentZone) 1.4.dp else 1.dp,
                                    color = if (isCurrentZone) colors.primary else colors.outlineVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                        ) {
                            Text(
                                text = zoneName,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCurrentZone) colors.textPrimary else colors.textSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(36.dp))
        }

        val progress = if (scrollState.maxValue == 0) 0f else {
            (scrollState.value.toFloat() / scrollState.maxValue.toFloat()).coerceIn(0f, 1f)
        }
        Box(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.CenterEnd)
                .fillMaxHeight()
                .width(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(colors.outlineVariant.copy(alpha = 0.25f))
        )
        Box(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.TopEnd)
                .padding(top = (height - 62.dp) * progress)
                .size(width = 10.dp, height = 62.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(colors.outline.copy(alpha = 0.7f))
        )

        Text(
            text = mode.subtitle,
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomStart)
                .padding(bottom = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = LichSoThemeColors.current.textSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
