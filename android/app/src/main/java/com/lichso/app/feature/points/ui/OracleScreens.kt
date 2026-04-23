package com.lichso.app.feature.points.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lichso.app.R
import com.lichso.app.feature.points.domain.ActionType
import com.lichso.app.feature.points.domain.Clock
import com.lichso.app.feature.points.domain.OracleDeck
import com.lichso.app.feature.points.domain.OracleQue
import com.lichso.app.ui.theme.LichSoThemeColors

/**
 * OracleDrawScreen — nghi lễ rút quẻ đầu ngày.
 * - Lắc ống xăm → animation rung
 * - Rút → award DRAW_KINH_DICH (1 lần/ngày) + nav tới OracleResultScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OracleDrawScreen(
    onBackClick: () -> Unit,
    onDrawn: () -> Unit,
    vm: PointsViewModel = hiltViewModel(),
    clock: Clock,
) {
    val c = LichSoThemeColors.current
    val todayUnlocks by vm.todayUnlocks.collectAsState()
    // Already drawn today? Use ActionLog / today's unlocks proxy: check via ledger action count.
    // Simpler: keep a local "drawn this session" flag triggered by award success.
    var isShaking by remember { mutableStateOf(false) }
    var hasDrawn by remember { mutableStateOf(false) }

    val shake = remember { Animatable(0f) }
    LaunchedEffect(isShaking) {
        if (isShaking) {
            repeat(5) {
                shake.animateTo(6f, tween(60))
                shake.animateTo(-6f, tween(60))
            }
            shake.animateTo(0f, tween(80))
            isShaking = false
        }
    }

    Scaffold(
        containerColor = c.bg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            if (c.isDark) listOf(Color(0xFF5D1212), Color(0xFF4A1010))
                            else listOf(c.primary, c.deepRed)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Rút quẻ đầu ngày",
                            style = TextStyle(
                                color = Color.White,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                            ),
                        )
                        Text(
                            "Nghi lễ truyền thống · 1 lần/ngày",
                            style = TextStyle(color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp),
                        )
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(c.gold.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(500f, 200f),
                        radius = 900f,
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(0.6f))

            // Jar (ống xăm)
            Box(
                modifier = Modifier
                    .size(240.dp, 300.dp)
                    .rotate(shake.value),
                contentAlignment = Alignment.BottomCenter,
            ) {
                // Sticks
                val stickData = listOf(
                    Triple(-60f, 150.dp, -10f),
                    Triple(-32f, 170.dp, -4f),
                    Triple(-4f,  180.dp, 0f),
                    Triple(24f,  175.dp, 4f),
                    Triple(52f,  160.dp, 9f),
                    Triple(78f,  140.dp, 14f),
                )
                stickData.forEach { (xOff, h, rot) ->
                    Box(
                        modifier = Modifier
                            .offset(x = xOff.dp, y = (-130).dp)
                            .rotate(rot)
                            .width(6.dp)
                            .height(h)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.verticalGradient(
                                    0f to c.gold,
                                    0.7f to Color(0xFFC6A300),
                                    0.7f to Color(0xFF8B0000),
                                    1f to c.deepRed,
                                )
                            )
                    )
                }
                // Pot
                Box(
                    modifier = Modifier
                        .size(200.dp, 150.dp)
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 100.dp, bottomEnd = 100.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF8B0000), Color(0xFFB71C1C))
                            )
                        )
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "\"Tâm tĩnh lặng, niệm điều cầu mong,\nlắc nhẹ 3 lần rồi rút một quẻ.\"",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    color = c.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                ),
                modifier = Modifier.padding(horizontal = 36.dp),
            )

            Spacer(Modifier.weight(1f))

            // CTA
            Button(
                onClick = {
                    if (!isShaking && !hasDrawn) {
                        isShaking = true
                        vm.award(ActionType.DRAW_KINH_DICH)
                        hasDrawn = true
                        onDrawn()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.primary),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_scroll),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Rút quẻ", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        "+15⚡ +5☯️",
                        style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    )
                }
            }

            Text(
                "Quẻ mỗi ngày được chọn bằng thuật toán bí truyền từ ngày âm – dương",
                style = TextStyle(fontSize = 11.sp, color = c.textTertiary, textAlign = TextAlign.Center),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
    }
}

/**
 * OracleResultScreen — hiển thị quẻ đã rút.
 * Dùng deterministic seed (epochDay) để đảm bảo quẻ cố định mỗi ngày.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OracleResultScreen(
    onBackClick: () -> Unit,
    onAskAi: (String) -> Unit,
    clock: Clock,
) {
    val c = LichSoThemeColors.current
    val que = remember(clock.todayEpochDay()) { OracleDeck.pickForDay(clock.todayEpochDay()) }

    Scaffold(
        containerColor = c.bg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            if (c.isDark) listOf(Color(0xFF5D1212), Color(0xFF4A1010))
                            else listOf(c.primary, c.deepRed)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Quẻ hôm nay",
                            style = TextStyle(
                                color = Color.White, fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold, fontSize = 18.sp,
                            )
                        )
                        Text(
                            "Deterministic · cố định cho ngày hôm nay",
                            style = TextStyle(color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp),
                        )
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OracleHeroCard(que = que)
            SectionCard(title = "Lời giải", body = que.interpretation)
            FourFortunesCard(que = que)
            SectionCard(
                title = "Gợi ý hành động",
                body = "• Hướng tốt: ${que.direction}\n• Giờ tốt: ${que.luckyHour}\n• ${que.suggestion}",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, c.primary),
                ) {
                    Text("Đóng", color = c.primary, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        val prompt = "Giải thêm giúp tôi quẻ \"${que.name}\" (${que.han}) — ${que.subtitle}. " +
                            "Thơ: \"${que.poem.replace("\n", " ")}\". Tôi muốn hiểu rõ ứng với bản thân hôm nay."
                        onAskAi(prompt)
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.primary),
                ) {
                    Text("Hỏi AI giải thêm", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OracleHeroCard(que: OracleQue) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    if (c.isDark) listOf(c.surface, c.surfaceContainer)
                    else listOf(Color(0xFFFFF8E1), Color(0xFFFFFBF5))
                )
            )
            .border(2.dp, c.gold, RoundedCornerShape(20.dp))
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(que.tier.colorHex))
                .padding(horizontal = 14.dp, vertical = 5.dp),
        ) {
            Text(
                que.tier.display,
                style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "Quẻ số ${que.index} · ${que.name}",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 26.sp, color = c.primary, fontWeight = FontWeight.Bold,
            ),
            textAlign = TextAlign.Center,
        )
        Text(
            que.han,
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 38.sp, color = c.gold,
                letterSpacing = 8.sp,
            ),
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            que.subtitle,
            style = TextStyle(fontSize = 11.sp, color = c.textTertiary),
            modifier = Modifier.padding(top = 2.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            que.poem,
            style = TextStyle(
                fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic,
                fontSize = 15.sp, color = c.textSecondary, lineHeight = 26.sp,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
private fun SectionCard(title: String, body: String) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surface)
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Text(
            title,
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 15.sp, fontWeight = FontWeight.Bold, color = c.primary,
            ),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            body,
            style = TextStyle(fontSize = 13.5.sp, color = c.textSecondary, lineHeight = 22.sp),
        )
    }
}

@Composable
private fun FourFortunesCard(que: OracleQue) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surface)
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Text(
            "Vận trong 4 phương diện",
            style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = c.primary),
        )
        Spacer(Modifier.height(10.dp))
        FortuneRow(label = "Công danh", value = que.career)
        FortuneRow(label = "Tài lộc", value = que.wealth)
        FortuneRow(label = "Tình duyên", value = que.love)
        FortuneRow(label = "Sức khỏe", value = que.health)
    }
}

@Composable
private fun FortuneRow(label: String, value: String) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            label,
            modifier = Modifier.width(80.dp),
            style = TextStyle(fontSize = 12.sp, color = c.textTertiary, fontWeight = FontWeight.SemiBold),
        )
        Text(
            value,
            style = TextStyle(fontSize = 13.sp, color = c.textPrimary, fontWeight = FontWeight.Medium, lineHeight = 19.sp),
        )
    }
}
