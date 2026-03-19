package com.lichso.app.ui.screen.chat

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.data.local.entity.ChatMessageEntity
import com.lichso.app.ui.theme.*

@Composable
fun AIChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val c = LichSoThemeColors.current
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()
    var showClearDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Auto-scroll to bottom
    LaunchedEffect(state.messages.size, state.isTyping) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1 + if (state.isTyping) 1 else 0)
        }
    }

    // Clear chat confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearChat()
                    showClearDialog = false
                }) { Text("Xoá", color = c.red) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Huỷ", color = c.textSecondary) }
            },
            title = { Text("Xoá lịch sử chat?", color = c.textPrimary) },
            text = { Text("Tất cả tin nhắn sẽ bị xoá và không thể khôi phục.", color = c.textSecondary) },
            containerColor = c.bg2,
            shape = RoundedCornerShape(16.dp)
        )
    }

    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    val navBarPadding = 72.dp  // height of bottom nav bar

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .then(
                if (imeBottom > 0) {
                    // Keyboard is visible: use imePadding only, no bottom nav padding
                    Modifier.imePadding()
                } else {
                    // Keyboard hidden: add bottom nav padding
                    Modifier.padding(bottom = navBarPadding)
                }
            )
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Brush.linearGradient(listOf(c.gold, c.teal)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedRobotMini(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        "Lịch Số AI",
                        style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 18.sp, color = c.gold2)
                    )
                    Text(
                        "Trợ lý phong thuỷ thông minh",
                        style = TextStyle(fontSize = 10.5.sp, color = c.textTertiary)
                    )
                }
            }
            IconButton(
                onClick = { showClearDialog = true },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Xoá chat", tint = c.textSecondary, modifier = Modifier.size(18.dp))
            }
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                ChatBubble(message = message, viewModel = viewModel)
            }
            if (state.isTyping) {
                item { TypingIndicator() }
            }
        }

        // Quick topics — 2 rows: Phong thuỷ + Quản lý
        QuickTopicRow { topic ->
            inputText = TextFieldValue("")
            viewModel.sendMessage(topic)
        }
        QuickActionRow { prompt ->
            // Always fill input box for user to review/edit before sending
            inputText = TextFieldValue(
                text = prompt,
                selection = androidx.compose.ui.text.TextRange(prompt.length)
            )
            focusRequester.requestFocus()
        }

        // Input bar
        ChatInputBar(
            textFieldValue = inputText,
            onTextChange = { inputText = it },
            onSend = {
                if (inputText.text.isNotBlank()) {
                    viewModel.sendMessage(inputText.text)
                    inputText = TextFieldValue("")
                    focusRequester.requestFocus()
                }
            },
            isEnabled = !state.isTyping,
            focusRequester = focusRequester
        )
    }
}

@Composable
private fun ChatBubble(message: ChatMessageEntity, viewModel: ChatViewModel) {
    val c = LichSoThemeColors.current
    val isUser = message.isUser
    val bubbleBg = if (isUser) {
        if (c.isDark) Color(0xFF1A2A25) else Color(0xFFE4F5F0)
    } else c.bg2
    val bubbleBorder = if (isUser) {
        Color(0x334ABEAA)
    } else c.border

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(
                    bubbleBg,
                    RoundedCornerShape(
                        topStart = 14.dp, topEnd = 14.dp,
                        bottomStart = if (isUser) 14.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 14.dp
                    )
                )
                .border(
                    1.dp, bubbleBorder,
                    RoundedCornerShape(
                        topStart = 14.dp, topEnd = 14.dp,
                        bottomStart = if (isUser) 14.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 14.dp
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                StyledChatText(
                    text = message.content,
                    isUser = isUser
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = viewModel.formatTime(message.timestamp),
                    style = TextStyle(fontSize = 9.5.sp, color = c.textQuaternary),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

/**
 * Render nội dung chat với các symbol icon được tô màu đồng bộ.
 *
 * Các ký tự symbol trong ChatIcons sẽ được highlight bằng màu gold/teal
 * tạo cảm giác sang trọng, cổ điển phương Đông.
 */
@Composable
private fun StyledChatText(text: String, isUser: Boolean) {
    val c = LichSoThemeColors.current
    val baseColor = if (isUser) c.teal2 else c.textPrimary
    val accentColor = if (isUser) c.teal else c.gold2
    val dimColor = if (isUser) c.teal2.copy(alpha = 0.7f) else c.textSecondary

    // Symbols that should be highlighted with accent color
    val accentSymbols = setOf(
        ChatIcons.CALENDAR, ChatIcons.LUNAR, ChatIcons.CLOCK,
        ChatIcons.STAR, ChatIcons.CANCHI, ChatIcons.COMPASS,
        ChatIcons.FORTUNE, ChatIcons.JOY, ChatIcons.SPARKLE, ChatIcons.INFO
    )
    // Symbols for positive/check
    val checkSymbols = setOf(ChatIcons.CHECK)
    // Symbols for negative/warning
    val warnSymbols = setOf(ChatIcons.WARNING, ChatIcons.CROSS)

    val annotated = buildStyledText(
        text = text,
        baseColor = baseColor,
        accentColor = accentColor,
        checkColor = c.teal,
        warnColor = c.red2,
        dimColor = dimColor,
        accentSymbols = accentSymbols,
        checkSymbols = checkSymbols,
        warnSymbols = warnSymbols
    )

    Text(
        text = annotated,
        style = TextStyle(
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
    )
}

@Composable
private fun buildStyledText(
    text: String,
    baseColor: Color,
    accentColor: Color,
    checkColor: Color,
    warnColor: Color,
    dimColor: Color,
    accentSymbols: Set<String>,
    checkSymbols: Set<String>,
    warnSymbols: Set<String>
): AnnotatedString {
    return remember(text, baseColor, accentColor) {
        buildAnnotatedString {
            var i = 0
            while (i < text.length) {
                val ch = text[i].toString()
                when {
                    ch in accentSymbols -> {
                        withStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.SemiBold)) {
                            append(ch)
                        }
                    }
                    ch in checkSymbols -> {
                        withStyle(SpanStyle(color = checkColor, fontWeight = FontWeight.SemiBold)) {
                            append(ch)
                        }
                    }
                    ch in warnSymbols -> {
                        withStyle(SpanStyle(color = warnColor, fontWeight = FontWeight.SemiBold)) {
                            append(ch)
                        }
                    }
                    ch == ChatIcons.ARROW -> {
                        withStyle(SpanStyle(color = accentColor)) {
                            append(ch)
                        }
                    }
                    ch == ChatIcons.BULLET -> {
                        withStyle(SpanStyle(color = dimColor)) {
                            append(ch)
                        }
                    }
                    else -> {
                        withStyle(SpanStyle(color = baseColor)) {
                            append(ch)
                        }
                    }
                }
                i++
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .background(c.bg2, RoundedCornerShape(14.dp))
            .border(1.dp, c.border, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AnimatedRobotMini(color = c.teal, modifier = Modifier.size(16.dp))
        Text("Đang suy nghĩ...", style = TextStyle(fontSize = 12.sp, color = c.textTertiary, fontWeight = FontWeight.Medium))
        Text("•••", style = TextStyle(fontSize = 14.sp, color = c.gold, letterSpacing = 2.sp))
    }
}

@Composable
private fun QuickTopicRow(onTopicClick: (String) -> Unit) {
    val c = LichSoThemeColors.current

    data class QuickTopic(val icon: ImageVector, val text: String)

    val topics = listOf(
        QuickTopic(Icons.Outlined.CalendarToday, "Hôm nay ngày tốt không?"),
        QuickTopic(Icons.Outlined.AccessTime, "Giờ hoàng đạo"),
        QuickTopic(Icons.Outlined.Explore, "Hướng xuất hành"),
        QuickTopic(Icons.Outlined.Favorite, "Ngày cưới hỏi"),
        QuickTopic(Icons.Outlined.Store, "Khai trương"),
        QuickTopic(Icons.Outlined.AutoAwesome, "Can chi hôm nay"),
        QuickTopic(Icons.Outlined.WbSunny, "Tiết khí"),
        QuickTopic(Icons.Outlined.Home, "Động thổ xây nhà"),
        QuickTopic(Icons.Outlined.Assessment, "Thống kê"),
        QuickTopic(Icons.AutoMirrored.Outlined.EventNote, "Kế hoạch ngày"),
        QuickTopic(Icons.Outlined.Info, "Bạn giúp gì được?"),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        topics.forEach { topic ->
            Box(
                modifier = Modifier
                    .background(c.bg3, RoundedCornerShape(20.dp))
                    .border(1.dp, c.border, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onTopicClick(topic.text) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(topic.icon, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(13.dp))
                    Text(text = topic.text, style = TextStyle(fontSize = 11.sp, color = c.textSecondary), maxLines = 1)
                }
            }
        }
    }
}

/**
 * Row 2: Quick action prompts cho Task / Note / Reminder
 * Dùng Material Icons đồng bộ với QuickTopicRow
 */
@Composable
private fun QuickActionRow(onActionClick: (String) -> Unit) {
    val c = LichSoThemeColors.current

    data class QuickAction(val icon: ImageVector, val label: String, val prompt: String)

    val actions = listOf(
        QuickAction(Icons.AutoMirrored.Outlined.EventNote, "Kế hoạch ngày", "Tạo checklist kế hoạch ngày hôm nay gồm: kiểm tra email, họp team, hoàn thành task quan trọng, review, cập nhật tiến độ"),
        QuickAction(Icons.AutoMirrored.Outlined.NoteAdd, "Ghi chú nhanh", "Ghi chú: "),
        QuickAction(Icons.Outlined.Alarm, "Nhắc uống thuốc", "Nhắc tôi uống thuốc lúc 8h sáng hàng ngày"),
        QuickAction(Icons.Outlined.ShoppingCart, "Đi chợ", "Tạo checklist đi chợ gồm: Rau xanh, Thịt/cá, Trái cây, Gia vị, Đồ uống"),
        QuickAction(Icons.Outlined.FitnessCenter, "Lịch tập gym", "Tạo kế hoạch tập gym tuần này: Thứ 2 - Chest, Thứ 3 - Back, Thứ 4 - Nghỉ, Thứ 5 - Legs, Thứ 6 - Cardio"),
        QuickAction(Icons.Outlined.LightMode, "Routine sáng", "Tạo nhắc nhở buổi sáng hàng ngày: 6:00 Thức dậy, 6:15 Tập thể dục, 7:00 Ăn sáng, 7:30 Đọc sách"),
        QuickAction(Icons.AutoMirrored.Outlined.MenuBook, "Học tập", "Tạo kế hoạch học tập gồm: Đọc tài liệu, Làm bài tập, Ôn bài cũ, Ghi chú tóm tắt, Luyện đề"),
        QuickAction(Icons.Outlined.Flight, "Du lịch", "Tạo checklist chuẩn bị du lịch: Đặt vé, Đặt khách sạn, Chuẩn bị hành lý, Đổi tiền, Mua bảo hiểm"),
        QuickAction(Icons.Outlined.Cake, "Sinh nhật", "Tạo checklist sinh nhật: Đặt bánh, Mua đồ trang trí, Gửi lời mời, Chuẩn bị quà, Đặt nhà hàng"),
        QuickAction(Icons.Outlined.AccountBalance, "Ngân sách tháng", "Ghi chú ngân sách tháng này: Thu nhập, Chi phí cố định, Ăn uống, Di chuyển, Giải trí, Tiết kiệm"),
        QuickAction(Icons.Outlined.Assessment, "Thống kê", "Thống kê tổng hợp tất cả task, note, nhắc nhở"),
        QuickAction(Icons.Outlined.CheckCircle, "Xong task", "Đánh dấu xong task: "),
        QuickAction(Icons.Outlined.RemoveCircle, "Xoá task", "Xoá task: "),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        actions.forEach { action ->
            Box(
                modifier = Modifier
                    .background(
                        if (c.isDark) Color(0xFF17221E) else Color(0xFFF2F8F6),
                        RoundedCornerShape(20.dp)
                    )
                    .border(
                        1.dp,
                        if (c.isDark) Color(0xFF263832) else Color(0xFFD6EAE3),
                        RoundedCornerShape(20.dp)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onActionClick(action.prompt) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        action.icon,
                        contentDescription = null,
                        tint = c.teal2,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = action.label,
                        style = TextStyle(fontSize = 11.sp, color = c.teal2),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    textFieldValue: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    onSend: () -> Unit,
    isEnabled: Boolean,
    focusRequester: FocusRequester = FocusRequester()
) {
    val c = LichSoThemeColors.current
    val text = textFieldValue.text
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.bg)
            .padding(horizontal = 10.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .background(c.bg2, RoundedCornerShape(24.dp))
                .border(1.dp, c.border, RoundedCornerShape(24.dp))
        ) {
            TextField(
                value = textFieldValue,
                onValueChange = onTextChange,
                placeholder = {
                    Text("Hỏi về phong thuỷ, ngày tốt...", style = TextStyle(fontSize = 13.sp, color = c.textQuaternary))
                },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                enabled = isEnabled,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = c.textPrimary,
                    unfocusedTextColor = c.textPrimary,
                    cursorColor = c.gold
                ),
                textStyle = TextStyle(fontSize = 13.sp),
                singleLine = true
            )
        }

        // Send button
        IconButton(
            onClick = onSend,
            enabled = isEnabled && text.isNotBlank(),
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (text.isNotBlank() && isEnabled)
                        Brush.linearGradient(listOf(c.gold, c.teal))
                    else
                        Brush.linearGradient(listOf(c.surface, c.surface)),
                    CircleShape
                )
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Gửi",
                tint = if (text.isNotBlank() && isEnabled) {
                    if (c.isDark) Color(0xFF1A1500) else Color.White
                } else c.textQuaternary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/** Animated mini robot icon — same style as FAB robot, with blink + gentle tilt */
@Composable
private fun AnimatedRobotMini(
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "miniRobot")

    val headTilt by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "miniTilt"
    )

    val blinkPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
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
        label = "miniBlink"
    )

    val antennaGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "miniGlow"
    )

    val antennaBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "miniBounce"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val scale = minOf(w, h) / 24f
        val strokeW = 1.6f * scale

        rotate(degrees = headTilt, pivot = Offset(cx, cy)) {
            // Body
            drawRoundRect(
                color = color,
                topLeft = Offset(5f * scale, 11f * scale),
                size = Size(14f * scale, 10f * scale),
                cornerRadius = CornerRadius(2f * scale),
                style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Antenna stick + ball
            translate(top = antennaBounce * scale) {
                drawLine(
                    color = color,
                    start = Offset(12f * scale, 11f * scale),
                    end = Offset(12f * scale, 7f * scale),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                val ballR = 2f * scale
                // Glow
                drawCircle(
                    color = color.copy(alpha = antennaGlow * 0.3f),
                    radius = ballR * 1.5f,
                    center = Offset(12f * scale, 5f * scale)
                )
                drawCircle(
                    color = color,
                    radius = ballR,
                    center = Offset(12f * scale, 5f * scale),
                    style = Stroke(width = strokeW)
                )
                drawCircle(
                    color = color.copy(alpha = antennaGlow),
                    radius = ballR * 0.35f,
                    center = Offset(12f * scale, 5f * scale)
                )
            }

            // Eyes with blink
            val eyeH = 2f * scale * (1f - blinkPhase)
            val eyeW = 2.5f * scale
            val eyeY = 14f * scale - eyeH / 2f

            if (eyeH > 0.1f) {
                drawRoundRect(color = color, topLeft = Offset(7f * scale, eyeY), size = Size(eyeW, eyeH), cornerRadius = CornerRadius(0.5f * scale))
                drawRoundRect(color = color, topLeft = Offset(14.5f * scale, eyeY), size = Size(eyeW, eyeH), cornerRadius = CornerRadius(0.5f * scale))
            } else {
                drawLine(color = color, start = Offset(7f * scale, 14f * scale), end = Offset(9.5f * scale, 14f * scale), strokeWidth = strokeW * 0.8f, cap = StrokeCap.Round)
                drawLine(color = color, start = Offset(14.5f * scale, 14f * scale), end = Offset(17f * scale, 14f * scale), strokeWidth = strokeW * 0.8f, cap = StrokeCap.Round)
            }

            // Smile
            drawArc(
                color = color,
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(9.5f * scale, 16f * scale),
                size = Size(5f * scale, 2.5f * scale),
                style = Stroke(width = strokeW * 0.7f, cap = StrokeCap.Round)
            )

            // Ears
            drawLine(color = color.copy(alpha = 0.7f), start = Offset(5f * scale, 14f * scale), end = Offset(3f * scale, 13f * scale), strokeWidth = strokeW * 0.8f, cap = StrokeCap.Round)
            drawCircle(color = color.copy(alpha = 0.7f), radius = 0.7f * scale, center = Offset(2.5f * scale, 12.5f * scale))
            drawLine(color = color.copy(alpha = 0.7f), start = Offset(19f * scale, 14f * scale), end = Offset(21f * scale, 13f * scale), strokeWidth = strokeW * 0.8f, cap = StrokeCap.Round)
            drawCircle(color = color.copy(alpha = 0.7f), radius = 0.7f * scale, center = Offset(21.5f * scale, 12.5f * scale))
        }
    }
}
