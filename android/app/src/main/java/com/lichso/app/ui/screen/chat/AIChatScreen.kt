package com.lichso.app.ui.screen.chat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.data.local.entity.ChatMessageEntity
import com.lichso.app.ui.theme.*

@Composable
fun AIChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val c = LichSoThemeColors.current
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var showClearDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Speech-to-text launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                inputText = spokenText
            }
        }
    }

    // Permission launcher for RECORD_AUDIO
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchSpeechRecognizer(speechLauncher, context)
        } else {
            Toast.makeText(context, "Cần cấp quyền micro để dùng giọng nói", Toast.LENGTH_SHORT).show()
        }
    }

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
                    Icon(Icons.Outlined.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
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

        // Quick topics
        QuickTopicRow { topic ->
            inputText = ""
            viewModel.sendMessage(topic)
        }

        // Input bar with voice button
        ChatInputBar(
            text = inputText,
            onTextChange = { inputText = it },
            onSend = {
                if (inputText.isNotBlank()) {
                    viewModel.sendMessage(inputText)
                    inputText = ""
                    focusRequester.requestFocus()
                }
            },
            onVoice = {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                    launchSpeechRecognizer(speechLauncher, context)
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            isEnabled = !state.isTyping,
            focusRequester = focusRequester
        )
    }
}

private fun launchSpeechRecognizer(
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>,
    context: android.content.Context
) {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
        Toast.makeText(context, "Thiết bị không hỗ trợ nhận dạng giọng nói", Toast.LENGTH_SHORT).show()
        return
    }
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "vi-VN")
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói nội dung bạn muốn hỏi...")
    }
    launcher.launch(intent)
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
        Icon(Icons.Outlined.SmartToy, contentDescription = null, tint = c.teal, modifier = Modifier.size(16.dp))
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
        QuickTopic(Icons.Outlined.Home, "Động thổ xây nhà")
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

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onVoice: () -> Unit,
    isEnabled: Boolean,
    focusRequester: FocusRequester = FocusRequester()
) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.bg)
            .padding(horizontal = 10.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Voice button
        IconButton(
            onClick = onVoice,
            enabled = isEnabled,
            modifier = Modifier
                .size(40.dp)
                .background(c.bg3, CircleShape)
                .border(1.dp, c.border, CircleShape)
        ) {
            Icon(
                Icons.Outlined.Mic,
                contentDescription = "Giọng nói",
                tint = if (isEnabled) c.teal2 else c.textQuaternary,
                modifier = Modifier.size(18.dp)
            )
        }

        // Text field
        Box(
            modifier = Modifier
                .weight(1f)
                .background(c.bg2, RoundedCornerShape(24.dp))
                .border(1.dp, c.border, RoundedCornerShape(24.dp))
        ) {
            TextField(
                value = text,
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
                Icons.Filled.Send,
                contentDescription = "Gửi",
                tint = if (text.isNotBlank() && isEnabled) {
                    if (c.isDark) Color(0xFF1A1500) else Color.White
                } else c.textQuaternary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
