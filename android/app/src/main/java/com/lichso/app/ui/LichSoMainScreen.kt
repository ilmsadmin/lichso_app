package com.lichso.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlin.math.sin
import com.lichso.app.ui.screen.calendar.CalendarScreen
import com.lichso.app.ui.screen.chat.AIChatScreen
import com.lichso.app.ui.screen.home.HomeScreen
import com.lichso.app.ui.screen.settings.SettingsScreen
import com.lichso.app.ui.screen.tasks.TasksScreen2
import com.lichso.app.ui.screen.templates.TemplatesScreen
import com.lichso.app.ui.theme.*

@Composable
fun LichSoMainScreen(modifier: Modifier = Modifier) {
    val c = LichSoThemeColors.current
    var currentRoute by remember { mutableStateOf("home") }

    Box(modifier = modifier.fillMaxSize().background(c.bg)) {
        // Track container size for clamping FAB position
        var containerWidth by remember { mutableIntStateOf(0) }
        var containerHeight by remember { mutableIntStateOf(0) }

        // Content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .then(if (currentRoute == "chat") Modifier else Modifier.padding(bottom = 72.dp))
                .onGloballyPositioned { coords ->
                    containerWidth = coords.size.width
                    containerHeight = coords.size.height
                }
        ) {
            when (currentRoute) {
                "home" -> HomeScreen(onSettingsClick = { currentRoute = "settings" })
                "calendar" -> CalendarScreen()
                "tasks" -> TasksScreen2()
                "chat" -> AIChatScreen()
                "templates" -> TemplatesScreen()
                "settings" -> SettingsScreen()
                else -> HomeScreen(onSettingsClick = { currentRoute = "settings" })
            }
        }

        // FAB Robot — draggable AI button
        val density = LocalDensity.current
        val fabSize = with(density) { 56.dp.toPx() }
        val bottomNavHeight = with(density) { 72.dp.toPx() }
        val fabPaddingEnd = with(density) { 18.dp.toPx() }
        val fabPaddingBottom = with(density) { 90.dp.toPx() }
        // Initial position: bottom-end
        var fabOffsetX by remember { mutableFloatStateOf(-1f) }
        var fabOffsetY by remember { mutableFloatStateOf(-1f) }

        // Initialize position once container is measured
        LaunchedEffect(containerWidth, containerHeight) {
            if (containerWidth > 0 && containerHeight > 0 && fabOffsetX < 0f) {
                fabOffsetX = containerWidth - fabSize - fabPaddingEnd
                fabOffsetY = containerHeight + bottomNavHeight - fabSize - fabPaddingBottom
            }
        }

        if (fabOffsetX >= 0f) {
            AnimatedRobotFab(
                modifier = Modifier
                    .statusBarsPadding()
                    .offset { IntOffset(fabOffsetX.roundToInt(), fabOffsetY.roundToInt()) }
                    .size(56.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val maxX = containerWidth - fabSize
                            val maxY = containerHeight + bottomNavHeight - fabSize
                            fabOffsetX = (fabOffsetX + dragAmount.x).coerceIn(0f, maxX)
                            fabOffsetY = (fabOffsetY + dragAmount.y).coerceIn(0f, maxY)
                        }
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(c.teal, Color(0xFF237A6A))),
                        CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { currentRoute = "chat" }
                    .padding(12.dp) // padding giữa nền tròn 56dp và robot 32dp bên trong
            )
        }

        // Bottom Navigation
        BottomNavBar(
            currentRoute = currentRoute,
            onRouteSelected = { currentRoute = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun BottomNavBar(
    currentRoute: String,
    onRouteSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LichSoThemeColors.current

    data class NavItem(val route: String, val title: String)

    val items = listOf(
        NavItem("home", "Trang chủ"),
        NavItem("calendar", "Lịch"),
        NavItem("tasks", "Công việc"),
        NavItem("templates", "Template"),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(c.bg2)
            .drawBehind {
                drawLine(
                    color = c.border,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1f
                )
            }
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                val tint = if (isSelected) c.gold2 else c.textTertiary

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onRouteSelected(item.route) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        // Custom SVG-like icon
                        val iconPainter = when (item.route) {
                            "home" -> rememberHomeIcon(tint)
                            "calendar" -> rememberCalendarIcon(tint)
                            "tasks" -> rememberTaskIcon(tint)
                            "templates" -> rememberDocumentIcon(tint)
                            else -> rememberHomeIcon(tint)
                        }
                        Icon(
                            painter = iconPainter,
                            contentDescription = item.title,
                            tint = tint,
                            modifier = Modifier.size(22.dp).let {
                                if (isSelected) it.graphicsLayer { scaleX = 1.1f; scaleY = 1.1f } else it
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            item.title,
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = tint
                            )
                        )
                    }

                    // Gold underline indicator
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .width(22.dp)
                                .height(3.dp)
                                .background(c.gold2, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// Custom SVG-style icons matching HTML design
// Using ImageVector.Builder approach
// ══════════════════════════════════════════

private fun buildIcon(
    name: String,
    block: ImageVector.Builder.() -> Unit
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply(block).build()

/** Home icon — house with roof and chimney */
@Composable
private fun rememberHomeIcon(tint: Color): VectorPainter {
    val icon = remember(tint) {
        buildIcon("Home") {
            // Roof (triangle)
            addPath(
                pathData = PathData {
                    moveTo(3f, 12f)
                    lineTo(12f, 3f)
                    lineTo(21f, 12f)
                },
                stroke = SolidColor(tint),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
            // House body
            addPath(
                pathData = PathData {
                    moveTo(5f, 10f)
                    verticalLineTo(20f)
                    arcTo(1f, 1f, 0f, false, false, 6f, 21f)
                    horizontalLineTo(18f)
                    arcTo(1f, 1f, 0f, false, false, 19f, 20f)
                    verticalLineTo(10f)
                },
                stroke = SolidColor(tint),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
            // Door (arch)
            addPath(
                pathData = PathData {
                    moveTo(9f, 21f)
                    verticalLineTo(16f)
                    arcTo(3f, 3f, 0f, false, true, 15f, 16f)
                    verticalLineTo(21f)
                },
                stroke = SolidColor(tint),
                strokeLineWidth = 1.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
            // Door fill (subtle)
            addPath(
                pathData = PathData {
                    moveTo(9f, 21f)
                    verticalLineTo(16f)
                    arcTo(3f, 3f, 0f, false, true, 15f, 16f)
                    verticalLineTo(21f)
                    close()
                },
                fill = SolidColor(tint.copy(alpha = 0.15f)),
            )
            // Chimney
            addPath(
                pathData = PathData {
                    moveTo(16.5f, 4f)
                    verticalLineTo(8f)
                    horizontalLineTo(18.5f)
                    verticalLineTo(6f)
                },
                stroke = SolidColor(tint),
                strokeLineWidth = 1.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }
    return rememberVectorPainter(image = icon)
}

/** Calendar icon — rect body + date pegs + horizontal line + date mark */
@Composable
private fun rememberCalendarIcon(tint: Color): VectorPainter {
    val icon = remember(tint) {
        buildIcon("Calendar") {
            // Calendar body (rounded rect)
            addPath(
                pathData = PathData {
                    moveTo(5f, 4f)
                    horizontalLineTo(19f)
                    arcTo(2f, 2f, 0f, false, true, 21f, 6f)
                    verticalLineTo(20f)
                    arcTo(2f, 2f, 0f, false, true, 19f, 22f)
                    horizontalLineTo(5f)
                    arcTo(2f, 2f, 0f, false, true, 3f, 20f)
                    verticalLineTo(6f)
                    arcTo(2f, 2f, 0f, false, true, 5f, 4f)
                    close()
                },
                stroke = SolidColor(tint),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
            // Left peg
            addPath(
                pathData = PathData { moveTo(8f, 2f); lineTo(8f, 6f) },
                stroke = SolidColor(tint),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
            )
            // Right peg
            addPath(
                pathData = PathData { moveTo(16f, 2f); lineTo(16f, 6f) },
                stroke = SolidColor(tint),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
            )
            // Horizontal line
            addPath(
                pathData = PathData { moveTo(3f, 10f); lineTo(21f, 10f) },
                stroke = SolidColor(tint),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
            )
            // Date mark (small filled rect)
            addPath(
                pathData = PathData {
                    moveTo(8.5f, 14f); horizontalLineTo(10.5f)
                    arcTo(0.5f, 0.5f, 0f, false, true, 11f, 14.5f)
                    verticalLineTo(16.5f)
                    arcTo(0.5f, 0.5f, 0f, false, true, 10.5f, 17f)
                    horizontalLineTo(8.5f)
                    arcTo(0.5f, 0.5f, 0f, false, true, 8f, 16.5f)
                    verticalLineTo(14.5f)
                    arcTo(0.5f, 0.5f, 0f, false, true, 8.5f, 14f)
                    close()
                },
                fill = SolidColor(tint.copy(alpha = 0.4f)),
            )
        }
    }
    return rememberVectorPainter(image = icon)
}

/** Task / check icon — checkmark + box */
@Composable
private fun rememberTaskIcon(tint: Color): VectorPainter {
    val icon = remember(tint) {
        buildIcon("Task") {
            // Checkmark
            addPath(
                pathData = PathData {
                    moveTo(9f, 11f); lineTo(12f, 14f); lineTo(22f, 4f)
                },
                stroke = SolidColor(tint),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
            // Box
            addPath(
                pathData = PathData {
                    moveTo(21f, 12f)
                    verticalLineTo(19f)
                    arcTo(2f, 2f, 0f, false, true, 19f, 21f)
                    horizontalLineTo(5f)
                    arcTo(2f, 2f, 0f, false, true, 3f, 19f)
                    verticalLineTo(5f)
                    arcTo(2f, 2f, 0f, false, true, 5f, 3f)
                    horizontalLineTo(16f)
                },
                stroke = SolidColor(tint),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }
    return rememberVectorPainter(image = icon)
}

/** Animated Robot FAB — blinking eyes, wobbling head, bouncing antenna */
@Composable
private fun AnimatedRobotFab(modifier: Modifier = Modifier) {
    // --- Infinite transition for all animations ---
    val infiniteTransition = rememberInfiniteTransition(label = "robot")

    // Head tilt: gentle rotation -6° to +6°
    val headTilt by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "headTilt"
    )

    // Antenna bounce: slight Y offset
    val antennaBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "antennaBounce"
    )

    // Antenna ball glow pulse
    val antennaGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "antennaGlow"
    )

    // Eye blink: cycle 0→1→0, mostly open (0), blink at peak (1)
    val blinkPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3500
                0f at 0                      // eyes open
                0f at 2800                   // still open
                1f at 2950 using LinearEasing // snap shut
                0f at 3100 using LinearEasing // snap open
                0f at 3500                   // stay open
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "blink"
    )

    // Whole FAB gentle breathing scale
    val breathe by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    // Floating Y offset
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    Canvas(
        modifier = modifier
            .graphicsLayer {
                scaleX = breathe
                scaleY = breathe
                translationY = floatY
            }
    ) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Scale all drawing into the 24x24 viewport mapped to canvas
        val scale = minOf(w, h) / 24f
        val strokeW = 1.6f * scale
        val color = Color.White

        // Rotate head
        rotate(degrees = headTilt, pivot = Offset(cx, cy)) {
            // --- Body (rounded rect) ---
            val bodyLeft = 5f * scale
            val bodyTop = 11f * scale
            val bodyW = 14f * scale
            val bodyH = 10f * scale
            val bodyCorner = 2f * scale
            drawRoundRect(
                color = color,
                topLeft = Offset(bodyLeft, bodyTop),
                size = Size(bodyW, bodyH),
                cornerRadius = CornerRadius(bodyCorner),
                style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // --- Antenna stick ---
            translate(top = antennaBounce * scale) {
                drawLine(
                    color = color,
                    start = Offset(12f * scale, 11f * scale),
                    end = Offset(12f * scale, 7f * scale),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                // Antenna ball with glow
                val ballRadius = 2f * scale
                // Glow circle
                drawCircle(
                    color = Color(0xFF80FFE0).copy(alpha = antennaGlow * 0.4f),
                    radius = ballRadius * 1.7f,
                    center = Offset(12f * scale, 5f * scale)
                )
                // Main ball
                drawCircle(
                    color = color,
                    radius = ballRadius,
                    center = Offset(12f * scale, 5f * scale),
                    style = Stroke(width = strokeW)
                )
                // Inner bright dot
                drawCircle(
                    color = color.copy(alpha = antennaGlow),
                    radius = ballRadius * 0.4f,
                    center = Offset(12f * scale, 5f * scale)
                )
            }

            // --- Eyes (with blink) ---
            val eyeHeight = 2f * scale * (1f - blinkPhase) // shrink to 0 when blinking
            val eyeWidth = 2.5f * scale
            val eyeCorner = 0.5f * scale
            val eyeY = 14f * scale - eyeHeight / 2f

            // Left eye
            if (eyeHeight > 0.1f) {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(7f * scale, eyeY),
                    size = Size(eyeWidth, eyeHeight),
                    cornerRadius = CornerRadius(eyeCorner)
                )
            } else {
                // Closed eye — thin line
                drawLine(
                    color = color,
                    start = Offset(7f * scale, 14f * scale),
                    end = Offset(9.5f * scale, 14f * scale),
                    strokeWidth = strokeW * 0.8f,
                    cap = StrokeCap.Round
                )
            }

            // Right eye
            if (eyeHeight > 0.1f) {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(14.5f * scale, eyeY),
                    size = Size(eyeWidth, eyeHeight),
                    cornerRadius = CornerRadius(eyeCorner)
                )
            } else {
                drawLine(
                    color = color,
                    start = Offset(14.5f * scale, 14f * scale),
                    end = Offset(17f * scale, 14f * scale),
                    strokeWidth = strokeW * 0.8f,
                    cap = StrokeCap.Round
                )
            }

            // --- Smile (small arc) ---
            drawArc(
                color = color,
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(9.5f * scale, 16f * scale),
                size = Size(5f * scale, 2.5f * scale),
                style = Stroke(width = strokeW * 0.7f, cap = StrokeCap.Round)
            )

            // --- Ears / side antennas ---
            // Left ear
            drawLine(
                color = color.copy(alpha = 0.7f),
                start = Offset(5f * scale, 14f * scale),
                end = Offset(3f * scale, 13f * scale),
                strokeWidth = strokeW * 0.8f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = color.copy(alpha = 0.7f),
                radius = 0.7f * scale,
                center = Offset(2.5f * scale, 12.5f * scale)
            )
            // Right ear
            drawLine(
                color = color.copy(alpha = 0.7f),
                start = Offset(19f * scale, 14f * scale),
                end = Offset(21f * scale, 13f * scale),
                strokeWidth = strokeW * 0.8f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = color.copy(alpha = 0.7f),
                radius = 0.7f * scale,
                center = Offset(21.5f * scale, 12.5f * scale)
            )
        }
    }
}

/** Robot icon — body, antenna, eyes */
@Composable
private fun rememberRobotIcon(tint: Color): VectorPainter {
    val icon = remember(tint) {
        buildIcon("Robot") {
            // Body
            addPath(
                pathData = PathData {
                    moveTo(5f, 11f); horizontalLineTo(19f)
                    arcTo(2f, 2f, 0f, false, true, 21f, 13f)
                    verticalLineTo(19f)
                    arcTo(2f, 2f, 0f, false, true, 19f, 21f)
                    horizontalLineTo(5f)
                    arcTo(2f, 2f, 0f, false, true, 3f, 19f)
                    verticalLineTo(13f)
                    arcTo(2f, 2f, 0f, false, true, 5f, 11f)
                    close()
                },
                stroke = SolidColor(tint),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
            // Antenna stick
            addPath(
                pathData = PathData { moveTo(12f, 11f); lineTo(12f, 7f) },
                stroke = SolidColor(tint),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
            )
            // Antenna ball (circle cx=12 cy=5 r=2)
            addPath(
                pathData = PathData {
                    moveTo(14f, 5f)
                    arcTo(2f, 2f, 0f, true, true, 10f, 5f)
                    arcTo(2f, 2f, 0f, true, true, 14f, 5f)
                },
                stroke = SolidColor(tint),
                strokeLineWidth = 1.6f,
            )
            // Left eye
            addPath(
                pathData = PathData {
                    moveTo(7.5f, 13f); horizontalLineTo(9.5f)
                    arcTo(0.5f, 0.5f, 0f, false, true, 10f, 13.5f)
                    verticalLineTo(14.5f)
                    arcTo(0.5f, 0.5f, 0f, false, true, 9.5f, 15f)
                    horizontalLineTo(7.5f)
                    arcTo(0.5f, 0.5f, 0f, false, true, 7f, 14.5f)
                    verticalLineTo(13.5f)
                    arcTo(0.5f, 0.5f, 0f, false, true, 7.5f, 13f)
                    close()
                },
                fill = SolidColor(tint),
            )
            // Right eye
            addPath(
                pathData = PathData {
                    moveTo(14.5f, 13f); horizontalLineTo(16.5f)
                    arcTo(0.5f, 0.5f, 0f, false, true, 17f, 13.5f)
                    verticalLineTo(14.5f)
                    arcTo(0.5f, 0.5f, 0f, false, true, 16.5f, 15f)
                    horizontalLineTo(14.5f)
                    arcTo(0.5f, 0.5f, 0f, false, true, 14f, 14.5f)
                    verticalLineTo(13.5f)
                    arcTo(0.5f, 0.5f, 0f, false, true, 14.5f, 13f)
                    close()
                },
                fill = SolidColor(tint),
            )
        }
    }
    return rememberVectorPainter(image = icon)
}

/** Document / template icon — folded corner doc + lines */
@Composable
private fun rememberDocumentIcon(tint: Color): VectorPainter {
    val icon = remember(tint) {
        buildIcon("Document") {
            // Doc body
            addPath(
                pathData = PathData {
                    moveTo(14f, 2f); horizontalLineTo(6f)
                    arcTo(2f, 2f, 0f, false, false, 4f, 4f)
                    verticalLineTo(20f)
                    arcTo(2f, 2f, 0f, false, false, 6f, 22f)
                    horizontalLineTo(18f)
                    arcTo(2f, 2f, 0f, false, false, 20f, 20f)
                    verticalLineTo(8f)
                    close()
                },
                stroke = SolidColor(tint),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
            // Fold corner
            addPath(
                pathData = PathData {
                    moveTo(14f, 2f); verticalLineTo(8f); horizontalLineTo(20f)
                },
                stroke = SolidColor(tint),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
            // Line 1
            addPath(
                pathData = PathData { moveTo(16f, 13f); lineTo(8f, 13f) },
                stroke = SolidColor(tint),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
            )
            // Line 2
            addPath(
                pathData = PathData { moveTo(16f, 17f); lineTo(8f, 17f) },
                stroke = SolidColor(tint),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
            )
            // Short line
            addPath(
                pathData = PathData { moveTo(10f, 9f); lineTo(8f, 9f) },
                stroke = SolidColor(tint),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
            )
        }
    }
    return rememberVectorPainter(image = icon)
}
