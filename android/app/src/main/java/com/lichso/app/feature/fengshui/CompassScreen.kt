package com.lichso.app.feature.fengshui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lichso.app.domain.DayInfoProvider
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.theme.LichSoThemeColors
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun CompassScreen(
    onBackClick: () -> Unit,
    onAskAiClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val headingState = remember { mutableFloatStateOf(0f) }
    val animatedHeading by animateFloatAsState(targetValue = headingState.floatValue, label = "compassHeading")
    val dayInfoProvider = remember { DayInfoProvider() }
    val today = remember { LocalDate.now() }
    val todayInfo = remember(today) {
        dayInfoProvider.getDayInfo(today.dayOfMonth, today.monthValue, today.year)
    }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (rotationSensor == null) {
            onDispose { }
        } else {
            val rotationMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    headingState.floatValue = ((azimuth + 360f) % 360f)
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    val headingLabel = compassDirectionForDegrees(animatedHeading)
    val roundedHeading = animatedHeading.roundToInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        AppTopBar(
            title = "La bàn phong thủy",
            subtitle = "Theo dõi hướng máy và gợi ý hướng tốt hôm nay",
            onBackClick = onBackClick,
            actions = {
                OutlinedButton(onClick = {
                    onAskAiClick(
                        "Hãy phân tích la bàn phong thủy, hướng nhà và cung phi theo tuổi của tôi."
                    )
                }) {
                    Text("AI")
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        CompassDial(
            heading = animatedHeading,
            headingLabel = headingLabel
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
                    text = "Hướng hiện tại",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$roundedHeading° · $headingLabel",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Màn này dùng cảm biến quay để cho bạn biết máy đang nghiêng về hướng nào. Khi quay máy, la bàn sẽ đổi theo thời gian thực.",
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
                    text = "Hướng tốt hôm nay",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                DirectionRow(label = "Thần Tài", value = todayInfo.huong.thanTai)
                DirectionRow(label = "Hỷ Thần", value = todayInfo.huong.hyThan)
                DirectionRow(label = "Nên tránh", value = todayInfo.huong.hungThan)
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
                    text = "Mẹo sử dụng",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Giữ máy phẳng để góc quay ổn định.\n• Ưu tiên kiểm tra ở nơi thoáng, tránh đứng sát kim loại lớn.\n• Dùng cùng với Bát trạch để đối chiếu hướng nhà hợp tuổi.",
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
private fun CompassDial(
    heading: Float,
    headingLabel: String
) {
    val c = LichSoThemeColors.current
    val needleRotation = -heading

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(290.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(c.surface),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(c.bg2),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .graphicsLayer { rotationZ = needleRotation },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "N",
                        modifier = Modifier.align(Alignment.TopCenter),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFE53935)
                    )
                    Text(
                        text = "E",
                        modifier = Modifier.align(Alignment.CenterEnd),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textSecondary
                    )
                    Text(
                        text = "S",
                        modifier = Modifier.align(Alignment.BottomCenter),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textSecondary
                    )
                    Text(
                        text = "W",
                        modifier = Modifier.align(Alignment.CenterStart),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textSecondary
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.Center)
                            .background(Color(0xFFE53935), MaterialTheme.shapes.extraLarge)
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = headingLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = c.primary
            )
            Text(
                text = "Đang đo hướng máy",
                fontSize = 12.sp,
                color = c.textSecondary
            )
        }
    }
}

@Composable
private fun DirectionRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = LichSoThemeColors.current.primary
        )
    }
}
