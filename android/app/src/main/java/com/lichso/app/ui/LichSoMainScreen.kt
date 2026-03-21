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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
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
        var containerWidth by remember { mutableIntStateOf(0) }
        var containerHeight by remember { mutableIntStateOf(0) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .then(if (currentRoute == "chat") Modifier else Modifier.padding(bottom = 80.dp))
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

        val density = LocalDensity.current
        val fabSize = with(density) { 56.dp.toPx() }
        val bottomNavHeight = with(density) { 80.dp.toPx() }
        val fabPaddingEnd = with(density) { 18.dp.toPx() }
        // Tăng padding từ 90.dp lên 110.dp để icon không bị menu lấp
        val fabPaddingBottom = with(density) { 110.dp.toPx() }
        
        var fabOffsetX by remember { mutableFloatStateOf(-1f) }
        var fabOffsetY by remember { mutableFloatStateOf(-1f) }

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
                    .padding(12.dp)
            )
        }

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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(c.bg2)
            .drawBehind {
                drawLine(
                    color = c.border,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1f
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
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
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                item.title,
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = tint
                                )
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .width(26.dp)
                                    .height(3.dp)
                                    .background(c.gold2, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                            )
                        }
                    }
                }
            }
        }
        // Fills the system navigation bar area below the nav bar content
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
        )
    }
}

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

@Composable
private fun rememberHomeIcon(tint: Color): VectorPainter {
    val icon = remember(tint) {
        buildIcon("Home") {
            addPath(
                pathData = PathData {
                    moveTo(3f, 12f); lineTo(12f, 3f); lineTo(21f, 12f)
                },
                stroke = SolidColor(tint),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
            addPath(
                pathData = PathData {
                    moveTo(5f, 10f); verticalLineTo(20f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6f, 21f); horizontalLineTo(18f)
                    arcTo(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 19f, 20f); verticalLineTo(10f)
                },
                stroke = SolidColor(tint),
                strokeLineWidth = 2.0f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }
    return rememberVectorPainter(image = icon)
}

@Composable
private fun rememberCalendarIcon(tint: Color): VectorPainter {
    val icon = remember(tint) {
        buildIcon("Calendar") {
            addPath(
                pathData = PathData {
                    moveTo(5f, 4f); horizontalLineTo(19f)
                    arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 21f, 6f); verticalLineTo(20f)
                    arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 19f, 22f); horizontalLineTo(5f)
                    arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3f, 20f); verticalLineTo(6f)
                    arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5f, 4f); close()
                },
                stroke = SolidColor(tint),
                strokeLineWidth = 2.0f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }
    return rememberVectorPainter(image = icon)
}

@Composable
private fun rememberTaskIcon(tint: Color): VectorPainter {
    val icon = remember(tint) {
        buildIcon("Task") {
            addPath(
                pathData = PathData {
                    moveTo(9f, 11f); lineTo(12f, 14f); lineTo(22f, 4f)
                },
                stroke = SolidColor(tint),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }
    return rememberVectorPainter(image = icon)
}

@Composable
private fun rememberDocumentIcon(tint: Color): VectorPainter {
    val icon = remember(tint) {
        buildIcon("Document") {
            addPath(
                pathData = PathData {
                    moveTo(14f, 2f); horizontalLineTo(6f)
                    arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4f, 4f); verticalLineTo(20f)
                    arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6f, 22f); horizontalLineTo(18f)
                    arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 20f, 20f); verticalLineTo(8f); close()
                },
                stroke = SolidColor(tint),
                strokeLineWidth = 2.0f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }
    return rememberVectorPainter(image = icon)
}

@Composable
private fun AnimatedRobotFab(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "robot")
    
    val headTilt by infiniteTransition.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "headTilt"
    )
    
    val blinkPhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3500
                0f at 0
                0f at 2800
                1f at 2950 using LinearEasing
                0f at 3100 using LinearEasing
                0f at 3500
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "blink"
    )

    val antennaBounce by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -1.5f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "antennaBounce"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val scale = minOf(w, h) / 24f
        val strokeW = 1.8f * scale
        val color = Color.White
        
        rotate(degrees = headTilt, pivot = Offset(w/2, h/2)) {
            // 1. Thân/Đầu Robot
            drawRoundRect(
                color = color,
                topLeft = Offset(5f * scale, 11f * scale),
                size = Size(14f * scale, 10f * scale),
                cornerRadius = CornerRadius(2.5f * scale),
                style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 2. Anten (có nhún)
            translate(top = antennaBounce * scale) {
                drawLine(
                    color = color,
                    start = Offset(12f * scale, 11f * scale),
                    end = Offset(12f * scale, 7f * scale),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = color,
                    radius = 2f * scale,
                    center = Offset(12f * scale, 5.5f * scale),
                    style = Stroke(width = strokeW)
                )
                drawCircle(
                    color = color.copy(alpha = 0.8f),
                    radius = 0.8f * scale,
                    center = Offset(12f * scale, 5.5f * scale)
                )
            }

            // 3. Mắt (có hiệu ứng chớp)
            val eyeH = 2.2f * scale * (1f - blinkPhase)
            val eyeW = 2.5f * scale
            val eyeY = 14.5f * scale - eyeH / 2f
            
            if (eyeH > 0.2f) {
                drawRoundRect(color = color, topLeft = Offset(7.5f * scale, eyeY), size = Size(eyeW, eyeH), cornerRadius = CornerRadius(0.6f * scale))
                drawRoundRect(color = color, topLeft = Offset(14f * scale, eyeY), size = Size(eyeW, eyeH), cornerRadius = CornerRadius(0.6f * scale))
            } else {
                drawLine(color = color, start = Offset(7.5f * scale, 14.5f * scale), end = Offset(10f * scale, 14.5f * scale), strokeWidth = strokeW * 0.8f, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(14f * scale, 14.5f * scale), end = Offset(16.5f * scale, 14.5f * scale), strokeWidth = strokeW * 0.8f, cap = StrokeCap.Round)
            }

            // 4. Miệng cười
            drawArc(
                color = color,
                startAngle = 10f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(10f * scale, 16.5f * scale),
                size = Size(4f * scale, 2.5f * scale),
                style = Stroke(width = strokeW * 0.8f, cap = StrokeCap.Round)
            )

            // 5. Tai robot
            drawLine(color = color.copy(alpha = 0.8f), start = Offset(5f * scale, 15f * scale), end = Offset(3f * scale, 14f * scale), strokeWidth = strokeW * 0.8f, cap = StrokeCap.Round)
            drawCircle(color = color.copy(alpha = 0.8f), radius = 0.8f * scale, center = Offset(2.5f * scale, 13.5f * scale))
            drawLine(color = color.copy(alpha = 0.8f), start = Offset(19f * scale, 15f * scale), end = Offset(21f * scale, 14f * scale), strokeWidth = strokeW * 0.8f, cap = StrokeCap.Round)
            drawCircle(color = color.copy(alpha = 0.8f), radius = 0.8f * scale, center = Offset(21.5f * scale, 13.5f * scale))
        }
    }
}
