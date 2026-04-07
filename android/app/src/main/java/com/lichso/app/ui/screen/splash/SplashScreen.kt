package com.lichso.app.ui.screen.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lichso.app.BuildConfig
import com.lichso.app.R
import kotlinx.coroutines.delay

// ══════════════════════════════════════════
// Palette — Traditional Red & Gold
// ══════════════════════════════════════════
private val BgDeepRed = Color(0xFF6D0B0B)
private val BgRed = Color(0xFF9B1B1B)
private val BgCrimson = Color(0xFFC62828)

private val Gold50 = Color(0xFFFFF8E1)   // near-white gold highlight
private val Gold200 = Color(0xFFE6C361)
private val Gold400 = Color(0xFFD4A017)
private val Gold600 = Color(0xFFB8860B)
private val Gold800 = Color(0xFF8B6914)

private val ZenixStart = Color(0xFF64FFDA)
private val ZenixMid = Color(0xFF448AFF)
private val ZenixEnd = Color(0xFFB388FF)

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    /* ─── animation flags ─── */
    var started by remember { mutableStateOf(false) }

    // 1) Logo — spring bounce
    val logoScale by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = spring(dampingRatio = .5f, stiffness = 280f),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(500), label = "logoAlpha"
    )

    // 2) Title — delayed fade
    val titleAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(500, delayMillis = 350), label = "titleAlpha"
    )
    val titleSlide by animateFloatAsState(
        targetValue = if (started) 0f else 24f,
        animationSpec = tween(500, delayMillis = 350, easing = FastOutSlowInEasing),
        label = "titleSlide"
    )

    // 3) Subtitle — later
    val subAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(400, delayMillis = 650), label = "subAlpha"
    )
    val subSlide by animateFloatAsState(
        targetValue = if (started) 0f else 20f,
        animationSpec = tween(400, delayMillis = 650, easing = FastOutSlowInEasing),
        label = "subSlide"
    )

    // 4) Bottom — latest
    val bottomAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(500, delayMillis = 900), label = "bottomAlpha"
    )

    // ∞ Slow-spinning gold ring
    val inf = rememberInfiniteTransition(label = "inf")
    val ringRotation by inf.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20_000, easing = LinearEasing)),
        label = "ringRot"
    )
    val glowPulse by inf.animateFloat(
        initialValue = .06f, targetValue = .14f,
        animationSpec = infiniteRepeatable(
            tween(3000, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "glow"
    )

    LaunchedEffect(Unit) {
        started = true
        delay(2400)
        onSplashFinished()
    }

    // ══════════════════════════════════════════
    //  UI
    // ══════════════════════════════════════════
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BgDeepRed, BgRed, BgCrimson),
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        /* ── ambient glow top-right ── */
        Box(
            Modifier
                .size(340.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-80).dp)
                .alpha(glowPulse)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Gold400, Color.Transparent),
                        radius = 340f
                    ),
                    CircleShape
                )
        )
        /* ── ambient glow bottom-left ── */
        Box(
            Modifier
                .size(280.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-70).dp, y = 100.dp)
                .alpha(glowPulse * .6f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Gold200, Color.Transparent),
                        radius = 280f
                    ),
                    CircleShape
                )
        )

        /* ═══ center content ═══ */
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (-28).dp)
        ) {
            /* ── logo + ring ── */
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(210.dp)
            ) {
                // Rotating thin gold arc ring
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .rotate(ringRotation)
                        .alpha(.35f)
                ) {
                    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color.Transparent, Gold200, Gold50, Color.Transparent)
                        ),
                        startAngle = 0f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = stroke
                    )
                }

                // Soft radial glow behind logo
                Box(
                    Modifier
                        .size(180.dp)
                        .alpha(.12f)
                        .background(
                            Brush.radialGradient(listOf(Gold200, Color.Transparent)),
                            CircleShape
                        )
                )

                // Logo
                Image(
                    painter = painterResource(R.drawable.splash_logo),
                    contentDescription = "Lịch Số",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(160.dp)
                        .scale(logoScale)
                        .alpha(logoAlpha)
                )
            }

            /* ── app name ── */
            Text(
                text = "Lịch Số",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    brush = Brush.linearGradient(
                        listOf(Gold600, Gold200, Gold50, Gold200, Gold600),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, 0f)
                    ),
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier
                    .graphicsLayer {
                        alpha = titleAlpha
                        translationY = titleSlide
                    }
            )

            Spacer(Modifier.height(10.dp))

            /* ── thin divider ── */
            Box(
                Modifier
                    .width(56.dp)
                    .height(2.dp)
                    .alpha(titleAlpha)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Gold400, Color.Transparent)
                        ),
                        RoundedCornerShape(1.dp)
                    )
            )

            Spacer(Modifier.height(14.dp))

            /* ── tagline ── */
            Text(
                text = "Lịch Vạn Niên  ·  Gia Phả  ·  AI Tử Vi",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = .55f),
                    letterSpacing = .8.sp,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.graphicsLayer {
                    alpha = subAlpha
                    translationY = subSlide
                }
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                style = TextStyle(
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = .25f),
                ),
                modifier = Modifier.graphicsLayer {
                    alpha = subAlpha
                    translationY = subSlide
                }
            )
        }

        /* ═══ bottom branding ═══ */
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 52.dp)
                .alpha(bottomAlpha)
        ) {
            // Small decorative dot
            Box(
                Modifier
                    .size(3.dp)
                    .background(Gold400.copy(alpha = .4f), CircleShape)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            brush = Brush.linearGradient(
                                listOf(Gold600, Gold200, Gold50, Gold200, Gold600),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, 0f)
                            ),
                        )
                    ) { append("Phát triển bởi  ") }
                    withStyle(
                        SpanStyle(
                            brush = Brush.linearGradient(
                                listOf(ZenixStart, ZenixMid, ZenixEnd),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, 0f)
                            ),
                            fontWeight = FontWeight.SemiBold,
                        )
                    ) { append("Zenix Labs") }
                },
                style = TextStyle(
                    fontSize = 12.sp,
                    letterSpacing = .5.sp,
                ),
            )
        }
    }
}
