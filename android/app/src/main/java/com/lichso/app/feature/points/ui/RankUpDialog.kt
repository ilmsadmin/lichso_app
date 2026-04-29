package com.lichso.app.feature.points.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lichso.app.R
import com.lichso.app.feature.points.domain.PermanentRank
import com.lichso.app.feature.points.domain.PermanentUnlockKey
import com.lichso.app.ui.theme.LichSoThemeColors

/**
 * Rank-up dialog shown when ☯️ crosses a PermanentRank threshold.
 */
@Composable
fun RankUpDialog(
    rank: PermanentRank,
    newUnlocks: List<PermanentUnlockKey>,
    onDismiss: () -> Unit,
) {
    val c = LichSoThemeColors.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + scaleIn(initialScale = 0.85f),
                exit = fadeOut() + scaleOut(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(0.88f)
                        .clip(RoundedCornerShape(28.dp))
                        .background(c.surface)
                        .border(2.dp, c.gold, RoundedCornerShape(28.dp))
                        .padding(24.dp)
                        .clickable(enabled = false) {}, // absorb click
                ) {
                    // Crown
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(listOf(c.gold, Color(0xFFA07812)))
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_crown),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(44.dp),
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "THĂNG BẬC",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp,
                            color = c.gold,
                        ),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        rank.displayName,
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = c.textPrimary,
                        ),
                    )
                    Spacer(Modifier.height(16.dp))

                    if (newUnlocks.isNotEmpty()) {
                        Text(
                            "Tính năng vừa mở khoá",
                            style = TextStyle(fontSize = 11.sp, color = c.textTertiary, letterSpacing = 1.sp),
                        )
                        Spacer(Modifier.height(8.dp))
                        newUnlocks.forEach { key ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check_circle),
                                    contentDescription = null,
                                    tint = c.goodGreen,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    key.label,
                                    style = TextStyle(fontSize = 13.sp, color = c.textPrimary),
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = c.primary),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text("Tiếp tục tu tập", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
