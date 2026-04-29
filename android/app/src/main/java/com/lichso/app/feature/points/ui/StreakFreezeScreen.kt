package com.lichso.app.feature.points.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lichso.app.feature.points.domain.FreezePurchaseResult
import com.lichso.app.feature.points.domain.FreezeRedeemResult
import com.lichso.app.ui.theme.LichSoThemeColors
import kotlin.random.Random

private const val FREEZE_COST: Long = 100L
private const val FREEZE_MAX_TOKENS = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakFreezeScreen(
    onBackClick: () -> Unit = {},
    pointsVm: PointsViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val ctx = LocalContext.current
    val balance by pointsVm.balance.collectAsState()
    val streak by pointsVm.streak.collectAsState()
    var feedback by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Đóng băng streak", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = c.surface,
                    titleContentColor = c.textPrimary,
                ),
            )
        },
        containerColor = c.bg,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // Hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF26C6DA))),
                    )
                    .padding(16.dp),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.AcUnit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "🛡️ Đóng băng streak",
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Mua thêm vé đóng băng — nếu lỡ quên check-in 1 ngày, streak vẫn được giữ nguyên.",
                        style = TextStyle(fontSize = 13.sp, color = Color(0xFFE0F7FA)),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Status row
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusChip(
                    icon = Icons.Filled.LocalFireDepartment,
                    label = "Streak hiện tại",
                    value = "${streak.current} ngày",
                    color = Color(0xFFE65100),
                )
                Spacer(Modifier.width(8.dp))
                StatusChip(
                    icon = Icons.Filled.AcUnit,
                    label = "Vé đóng băng",
                    value = "${streak.freezeTokens}/$FREEZE_MAX_TOKENS",
                    color = Color(0xFF1565C0),
                )
            }

            Spacer(Modifier.height(16.dp))

            // Cost card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.surface)
                    .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
                    .padding(16.dp),
            ) {
                Column {
                    Text("Mua thêm 1 vé", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.textPrimary))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Chi phí: $FREEZE_COST ☯ điểm vĩnh viễn",
                        style = TextStyle(fontSize = 13.sp, color = c.textSecondary),
                    )
                    Text(
                        "Số dư của bạn: ${balance.permanent} ☯",
                        style = TextStyle(fontSize = 12.sp, color = c.textTertiary),
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            pointsVm.purchaseFreezeToken(FREEZE_COST) { r ->
                                feedback = when (r) {
                                    FreezePurchaseResult.Success -> "✅ Đã thêm 1 vé đóng băng"
                                    is FreezePurchaseResult.InsufficientPoints -> "❌ Thiếu ${r.needed} ☯"
                                    FreezePurchaseResult.MaxTokens -> "❌ Đã đạt tối đa $FREEZE_MAX_TOKENS vé"
                                }
                            }
                        },
                        enabled = balance.permanent >= FREEZE_COST && streak.freezeTokens < FREEZE_MAX_TOKENS,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Icon(Icons.Filled.AcUnit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Mua vé · $FREEZE_COST ☯", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Share gift card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.surface)
                    .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
                    .padding(16.dp),
            ) {
                Column {
                    Text("🎁 Tặng vé cho bạn bè", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.textPrimary))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Tạo link mời — bạn bè bấm link sẽ nhận 1 vé đóng băng miễn phí (mỗi link chỉ dùng 1 lần).",
                        style = TextStyle(fontSize = 12.sp, color = c.textSecondary),
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            val token = randomToken()
                            val link = "lichso://streak-gift?token=$token"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "🛡️ Mình tặng bạn 1 vé đóng băng streak Lịch Số!\n" +
                                            "Bấm vào link để nhận: $link\n\n" +
                                            "Tải app Lịch Số trên CH Play để dùng nhé.",
                                )
                            }
                            ctx.startActivity(Intent.createChooser(intent, "Chia sẻ vé tặng"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Icon(Icons.Filled.CardGiftcard, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Tạo link tặng & chia sẻ", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            feedback?.let { msg ->
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(c.surfaceContainer)
                        .padding(12.dp),
                ) {
                    Text(msg, style = TextStyle(fontSize = 13.sp, color = c.textPrimary))
                }
            }
        }
    }
}

@Composable
private fun RowScope.StatusChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(12.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(label, style = TextStyle(fontSize = 11.sp, color = c.textSecondary))
            }
            Spacer(Modifier.height(4.dp))
            Text(value, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = c.textPrimary))
        }
    }
}

private fun randomToken(): String {
    val chars = ('a'..'z') + ('0'..'9')
    return (1..16).map { chars[Random.nextInt(chars.size)] }.joinToString("")
}
