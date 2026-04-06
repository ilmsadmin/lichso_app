package com.lichso.app.ui.screen.home

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lichso.app.domain.model.CityCoordinates
import com.lichso.app.domain.model.WeatherInfo
import com.lichso.app.ui.theme.LichSoThemeColors

/**
 * Bottom sheet hiển thị chi tiết thời tiết
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDetailSheet(
    weather: WeatherInfo,
    tempUnit: String = "°C",
    onDismiss: () -> Unit,
    onRefresh: () -> Unit = {},
    onCityChange: (String) -> Unit = {}
) {
    val c = LichSoThemeColors.current

    // State cho dropdown chọn thành phố
    var showCityDropdown by remember { mutableStateOf(false) }

    // Helper: chuyển đổi °C → °F nếu cần
    fun formatTemp(celsius: Double): String {
        val value = if (tempUnit == "°F") (celsius * 9 / 5 + 32).toInt() else celsius.toInt()
        return "${value}${tempUnit}"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = c.bg,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(c.outlineVariant, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Header: Icon + Nhiệt độ ──
            Text(
                text = weather.icon,
                style = TextStyle(fontSize = 64.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatTemp(weather.temperature),
                style = TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary
                )
            )

            Text(
                text = weather.description,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textSecondary
                )
            )

            // ── Chọn thành phố ──
            Spacer(modifier = Modifier.height(6.dp))
            Box {
                Row(
                    modifier = Modifier
                        .background(c.surfaceContainer, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { showCityDropdown = true }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = c.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = weather.cityName,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = c.textPrimary
                        )
                    )
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = c.textTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showCityDropdown,
                    onDismissRequest = { showCityDropdown = false },
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .background(c.bg2)
                ) {
                    CityCoordinates.cityNames.forEach { city ->
                        val isSelected = city == weather.cityName
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        if (isSelected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) c.primary else c.outlineVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = city,
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) c.primary else c.textPrimary
                                        )
                                    )
                                }
                            },
                            onClick = {
                                showCityDropdown = false
                                if (city != weather.cityName) {
                                    onCityChange(city)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Chi tiết thời tiết ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetailItem(
                    icon = Icons.Filled.Thermostat,
                    label = "Cảm giác",
                    value = formatTemp(weather.feelsLike ?: weather.temperature),
                    color = Color(0xFFEF5350)
                )
                WeatherDetailItem(
                    icon = Icons.Filled.WaterDrop,
                    label = "Độ ẩm",
                    value = "${weather.humidity}%",
                    color = Color(0xFF42A5F5)
                )
                WeatherDetailItem(
                    icon = Icons.Filled.Air,
                    label = "Gió",
                    value = "${weather.windSpeed.toInt()} km/h",
                    color = Color(0xFF66BB6A)
                )
                WeatherDetailItem(
                    icon = Icons.Filled.WbSunny,
                    label = "UV",
                    value = weather.uvIndex?.let { "%.1f".format(it) } ?: "N/A",
                    color = Color(0xFFFFB74D)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Gợi ý phong thuỷ theo thời tiết ──
            WeatherFengShuiTip(weather)

            Spacer(modifier = Modifier.height(16.dp))

            // ── Nút làm mới ──
            OutlinedButton(
                onClick = onRefresh,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = c.primary),
                border = BorderStroke(1.dp, c.primary.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cập nhật thời tiết")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Nguồn: Open-Meteo • Cập nhật mỗi 30 phút",
                style = TextStyle(fontSize = 11.sp, color = c.textTertiary),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WeatherDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    val c = LichSoThemeColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(color.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = c.textPrimary
            )
        )
        Text(
            text = label,
            style = TextStyle(
                fontSize = 11.sp,
                color = c.textTertiary
            )
        )
    }
}

/**
 * Gợi ý phong thuỷ dựa trên thời tiết
 */
@Composable
private fun WeatherFengShuiTip(weather: WeatherInfo) {
    val c = LichSoThemeColors.current

    val (tip, tipIcon) = when {
        weather.weatherCode in listOf(0, 1) && weather.isDay ->
            "Trời quang đãng — thích hợp xuất hành, khai trương, làm lễ ngoài trời ☀️" to "✦"
        weather.weatherCode in listOf(2, 3) ->
            "Trời có mây — tốt cho làm việc văn phòng, ký kết hợp đồng ☁️" to "✓"
        weather.weatherCode in 45..48 ->
            "Sương mù — nên thận trọng khi di chuyển, tốt cho thiền định 🧘" to "⚡"
        weather.weatherCode in 51..57 ->
            "Mưa phùn — hạn chế xuất hành, tốt cho việc nhà, đọc sách 📖" to "~"
        weather.weatherCode in 61..67 || weather.weatherCode in 80..82 ->
            "Mưa — nên ở nhà, tốt cho cúng bái trong nhà, sắp xếp bàn thờ 🙏" to "✗"
        weather.weatherCode in 95..99 ->
            "Dông bão — tránh ra ngoài, hãy ở nhà cầu an, giữ bình an 🏠" to "⚠"
        weather.temperature >= 38 ->
            "Nắng nóng — hạn chế hoạt động ngoài trời, uống đủ nước 💧" to "🔥"
        weather.temperature <= 10 ->
            "Trời lạnh — giữ ấm, nên thắp hương cầu may mắn 🕯️" to "❄️"
        else ->
            "Thời tiết ổn định — phù hợp cho các hoạt động thường ngày 🌿" to "✓"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFFF8E1).copy(alpha = 0.6f),
                        Color(0xFFFFF3E0).copy(alpha = 0.6f)
                    )
                ),
                RoundedCornerShape(14.dp)
            )
            .border(1.dp, Color(0xFFFFE082).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🔮", style = TextStyle(fontSize = 16.sp))
                Text(
                    "Gợi ý phong thuỷ theo thời tiết",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF795548)
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tip,
                style = TextStyle(
                    fontSize = 13.sp,
                    color = Color(0xFF5D4037),
                    lineHeight = 20.sp
                )
            )
        }
    }
}
