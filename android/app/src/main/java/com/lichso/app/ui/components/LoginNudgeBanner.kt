package com.lichso.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lichso.app.data.auth.AuthNudgeViewModel
import com.lichso.app.ui.theme.LichSoThemeColors
import kotlinx.coroutines.delay

// Shows once per app process lifetime — resets on full restart.
private object LoginNudgeSession {
    var shown = false
}

@Composable
fun LoginNudgeBanner(
    modifier: Modifier = Modifier,
    viewModel: AuthNudgeViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val context = LocalContext.current
    val isGuest by viewModel.isGuestUser.collectAsState()
    val isSigningIn by viewModel.isSigningIn.collectAsState()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(isGuest) {
        if (!isGuest) {
            // Sign-in succeeded — slide banner away immediately
            visible = false
        } else if (!LoginNudgeSession.shown) {
            LoginNudgeSession.shown = true
            delay(3_000)
            visible = true
            delay(5_000)
            visible = false
        }
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(350)) +
                fadeIn(tween(350)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(280)) +
                fadeOut(tween(280)),
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .shadow(6.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(if (c.isDark) Color(0xFF2A1A1A) else Color.White),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = if (c.isDark) listOf(Color(0xFF7F1D1D), Color(0xFF4A1010))
                                else listOf(c.primary, Color(0xFFC62828)),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Đăng nhập để lưu tiến trình",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = c.textPrimary,
                        ),
                        maxLines = 1,
                    )
                    Text(
                        "Điểm thưởng & bảng xếp hạng",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = c.textSecondary,
                        ),
                        maxLines = 1,
                    )
                }

                // Sign-in button — shows spinner while in progress
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                colors = if (c.isDark) listOf(Color(0xFF7F1D1D), Color(0xFF4A1010))
                                else listOf(c.primary, Color(0xFFC62828)),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                            )
                        )
                        .then(
                            if (!isSigningIn) Modifier.clickable { viewModel.signInWithGoogle(context) }
                            else Modifier
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSigningIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            "Đăng nhập",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            ),
                        )
                    }
                }

                IconButton(
                    onClick = { visible = false },
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Đóng",
                        tint = c.textTertiary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}
