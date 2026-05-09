package com.lichso.app.ui.screen.tools

import android.appwidget.AppWidgetManager
import android.os.Build
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.theme.LichSoThemeColors
import com.lichso.app.widget.WidgetPinHelper
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@Composable
fun WidgetManagerScreen(
    onBackClick: () -> Unit = {},
) {
    val c = LichSoThemeColors.current
    val context = LocalContext.current

    val pinSupported = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported
        } else false
    }
    var showGuideDialog by remember { mutableStateOf(false) }

    if (showGuideDialog) {
        WidgetGuideDialog(onDismiss = { showGuideDialog = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        // ═══ TOP BAR ═══
        AppTopBar(
            title = "Quản lý Widget",
            subtitle = "Màn hình chính & màn hình khoá (nếu hỗ trợ)",
            onBackClick = onBackClick
        )

        // ═══ CONTENT ═══
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp)
        ) {
            item {
                WidgetHeroCard(
                    pinSupported = pinSupported,
                    totalWidgets = WidgetPinHelper.allWidgets.size,
                    onPinPrimary = {
                        val first = WidgetPinHelper.allWidgets.firstOrNull()
                        if (first != null && pinSupported) {
                            val success = WidgetPinHelper.requestPin(context, first)
                            if (!success) showGuideDialog = true
                        } else {
                            showGuideDialog = true
                        }
                    },
                    onShowGuide = { showGuideDialog = true }
                )
            }

            item {
                LockScreenWidgetCard(onShowGuide = { showGuideDialog = true })
            }

            item {
                Text(
                    "Danh sách widget",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.textPrimary),
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
            }

            items(WidgetPinHelper.allWidgets) { widget ->
                val iconColors = listOf(
                    Color(0xFF2E7D32), Color(0xFF1565C0), Color(0xFFEF6C00),
                    Color(0xFF8E24AA), Color(0xFF6D4C41)
                )
                val bgColors = listOf(
                    Color(0xFFE8F5E9), Color(0xFFE3F2FD), Color(0xFFFFF3E0),
                    Color(0xFFF3E5F5), Color(0xFFFBE9E7)
                )
                val index = WidgetPinHelper.allWidgets.indexOf(widget)
                val iconColor = iconColors[index % iconColors.size]
                val bgColor = bgColors[index % bgColors.size]

                WidgetItemCard(
                    widget = widget,
                    iconColor = iconColor,
                    bgColor = bgColor,
                    pinSupported = pinSupported,
                    onPin = {
                        if (pinSupported) {
                            val success = WidgetPinHelper.requestPin(context, widget)
                            if (!success) showGuideDialog = true
                        } else {
                            showGuideDialog = true
                        }
                    }
                )
            }

            item {
                WidgetInfoStrip(pinSupported = pinSupported)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LockScreenWidgetCard(onShowGuide: () -> Unit) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceContainer.copy(alpha = 0.75f), RoundedCornerShape(18.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(c.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = c.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Màn hình khoá",
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
                )
                Text(
                    "Widget đã bật keyguard có thể xuất hiện trên lock screen nếu launcher/OEM hỗ trợ.",
                    style = TextStyle(fontSize = 12.sp, color = c.textSecondary)
                )
            }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LockFeatureChip(text = "Canh giờ")
            LockFeatureChip(text = "Đồng hồ")
            LockFeatureChip(text = "Lịch tháng")
            LockFeatureChip(text = "Đếm ngược")
        }

        Text(
            "Không phải mọi launcher đều cho ghim widget thẳng vào màn hình khoá. Nếu máy không hỗ trợ, hãy mở hướng dẫn thủ công.",
            style = TextStyle(fontSize = 12.sp, color = c.textTertiary)
        )

        OutlinedButton(
            onClick = onShowGuide,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, c.primary.copy(alpha = 0.45f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = c.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Xem cách bật", style = TextStyle(fontWeight = FontWeight.SemiBold))
        }
    }
}

@Composable
private fun LockFeatureChip(text: String) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .background(c.primary.copy(alpha = 0.08f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text,
            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
        )
    }
}

@Composable
private fun WidgetHeroCard(
    pinSupported: Boolean,
    totalWidgets: Int,
    onPinPrimary: () -> Unit,
    onShowGuide: () -> Unit,
) {
    val c = LichSoThemeColors.current
    val grad = if (c.isDark) {
        listOf(Color(0xFF3A0E0E), Color(0xFF5C1616), Color(0xFF2E0B0B))
    } else {
        listOf(c.primary, Color(0xFFD32F2F), c.deepRed)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(grad, start = Offset.Zero, end = Offset(700f, 500f)), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Widgets,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Widget Màn Hình Chính",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                )
                Text(
                    "$totalWidgets mẫu sẵn sàng · Đồng hồ · Lịch · AI · Đếm ngược",
                    style = TextStyle(fontSize = 12.sp, color = Color.White.copy(alpha = 0.78f))
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip(
                icon = Icons.Filled.CheckCircle,
                text = if (pinSupported) "Pin nhanh: Có" else "Pin nhanh: Không",
                tint = Color.White,
                modifier = Modifier.weight(1f)
            )
            StatusChip(
                icon = Icons.Filled.Dashboard,
                text = "$totalWidgets widget",
                tint = Color.White,
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onPinPrimary,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = c.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Thêm nhanh", style = TextStyle(fontWeight = FontWeight.SemiBold))
            }
            OutlinedButton(
                onClick = onShowGuide,
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Hướng dẫn", style = TextStyle(fontWeight = FontWeight.SemiBold))
            }
        }
    }
}

@Composable
private fun WidgetItemCard(
    widget: WidgetPinHelper.WidgetInfo,
    iconColor: Color,
    bgColor: Color,
    pinSupported: Boolean,
    onPin: () -> Unit,
) {
    val c = LichSoThemeColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(16.dp))
            .border(1.dp, iconColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Widgets,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                widget.label,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
            )
            Text(
                widget.description,
                style = TextStyle(fontSize = 12.sp, color = c.textSecondary),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (pinSupported) iconColor else c.surfaceContainerHigh)
                .clickable(onClick = onPin)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    if (pinSupported) Icons.Filled.Add else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    if (pinSupported) "Thêm" else "Xem",
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                )
            }
        }
    }
}

@Composable
private fun WidgetInfoStrip(pinSupported: Boolean) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(c.surfaceContainer.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            "Gợi ý sử dụng",
            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
        )
        Text(
            if (pinSupported) {
                "Bạn có thể bấm Thêm để launcher hiện hộp thoại ghim widget ngay."
            } else {
                "Máy chưa hỗ trợ pin tự động, hãy bấm Xem để mở hướng dẫn thêm thủ công."
            },
            style = TextStyle(fontSize = 12.sp, color = c.textSecondary)
        )
    }
}

@Composable
private fun StatusChip(
    icon: ImageVector,
    text: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Text(
            text,
            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = tint)
        )
    }
}

@Composable
private fun WidgetGuideDialog(onDismiss: () -> Unit) {
    val c = LichSoThemeColors.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .background(c.surfaceContainer, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Filled.Widgets, contentDescription = null, tint = c.primary, modifier = Modifier.size(24.dp))
                Text(
                    "Cách thêm Widget",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
                )
            }

            val steps = listOf(
                "Chạm và giữ vào vùng trống trên màn hình chính.",
                "Chọn mục Widget hoặc Tiện ích.",
                "Tìm ứng dụng Lịch Số trong danh sách.",
                "Giữ widget muốn dùng, kéo tới vị trí mong muốn rồi thả.",
                "Hoàn tất, widget sẽ hiển thị trên màn hình chính."
            )
            steps.forEachIndexed { i, step ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(c.primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${i + 1}",
                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = c.primary)
                        )
                    }
                    Text(
                        step,
                        style = TextStyle(fontSize = 13.sp, color = c.textSecondary),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Lưu ý: tên mục Widget có thể khác nhau giữa các hãng launcher.",
                style = TextStyle(fontSize = 12.sp, color = c.textTertiary),
                modifier = Modifier
                    .background(c.primary.copy(alpha = 0.07f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.primary)
            ) {
                Text("Đã hiểu", style = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp))
            }
        }
    }
}
