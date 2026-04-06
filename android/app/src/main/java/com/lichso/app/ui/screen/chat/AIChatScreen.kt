package com.lichso.app.ui.screen.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.data.local.entity.ChatMessageEntity
import com.lichso.app.ui.theme.*
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.components.HeaderIconButton
import com.lichso.app.ui.components.LichSoConfirmDialog

// ══════════════════════════════════════════════════════════
// AI Chat Screen — Material 3, matching screen-ai-chat.html
// ══════════════════════════════════════════════════════════

@Composable
fun AIChatScreen(
    onBackClick: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    val c = LichSoThemeColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()
    var showClearDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(state.messages.size, state.isTyping) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1 + if (state.isTyping) 1 else 0)
        }
    }

    // Clear chat confirmation dialog
    if (showClearDialog) {
        LichSoConfirmDialog(
            onDismiss = { showClearDialog = false },
            onConfirm = { viewModel.clearChat(); showClearDialog = false },
            title = "Xoá lịch sử chat?",
            message = "Tất cả tin nhắn sẽ bị xoá và không thể khôi phục.",
            icon = Icons.Filled.DeleteForever,
            iconTint = Color(0xFFD32F2F),
            iconBgColor = Color(0xFFFFEBEE),
            confirmText = "Xoá",
            confirmColor = Color(0xFFD32F2F),
        )
    }

    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .then(if (imeBottom > 0) Modifier.imePadding() else Modifier)
    ) {
        // ═══ AI HEADER — Red gradient with gold radial accent ═══
        AiChatHeader(
            onBackClick = onBackClick,
            onClearClick = { showClearDialog = true }
        )

        // ═══ SUGGESTION CHIPS ═══
        SuggestionChipsRow(viewModel = viewModel)

        // ═══ PROFILE COMPLETION BANNER ═══
        val profileSummary = state.profileSummary
        if (!profileSummary.isComplete) {
            ProfileCompletionBanner(
                missingFields = profileSummary.missingFields,
                onNavigateToProfile = onNavigateToProfile
            )
        }

        // ═══ CHAT MESSAGES ═══
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                ChatBubble(message = message, viewModel = viewModel)
            }
            if (state.isTyping) {
                item(key = "typing") { TypingIndicator() }
            }
        }

        // ═══ INPUT BAR ═══
        ChatInputBar(
            inputText = inputText,
            onInputChange = { inputText = it },
            isTyping = state.isTyping,
            focusRequester = focusRequester,
            onSend = {
                if (inputText.text.isNotBlank()) {
                    viewModel.sendMessage(inputText.text)
                    inputText = TextFieldValue("")
                }
            }
        )
    }
}

// ──────────────────────────────────────────────────────────
// AI HEADER
// ──────────────────────────────────────────────────────────

@Composable
private fun AiChatHeader(onBackClick: () -> Unit, onClearClick: () -> Unit) {
    val c = LichSoThemeColors.current

    AppTopBar(
        title = "Phong Thuỷ AI",
        subtitle = "Đang trực tuyến",
        onBackClick = onBackClick,
        actions = {
            // AI Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFD4A017),
                    modifier = Modifier.size(24.dp)
                )
            }
            HeaderIconButton(
                icon = Icons.Filled.DeleteOutline,
                contentDescription = "Xoá lịch sử",
                onClick = onClearClick
            )
        }
    )
}

// ──────────────────────────────────────────────────────────
// SUGGESTION CHIPS
// ──────────────────────────────────────────────────────────

@Composable
private fun SuggestionChipsRow(viewModel: ChatViewModel) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.bg)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp)
    ) {
        Text(
            "GỢI Ý HỎI",
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = c.outline,
                letterSpacing = 0.5.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            SuggestionChip(Icons.Filled.Stars, "Tử vi hôm nay") {
                viewModel.sendMessage("Xem tử vi hôm nay")
            }
            SuggestionChip(Icons.Filled.AutoAwesome, "Phân tích bát tự") {
                viewModel.sendMessage("Phân tích bát tự tứ trụ của tôi")
            }
            SuggestionChip(Icons.Filled.TrendingUp, "Vận hạn năm nay") {
                viewModel.sendMessage("Xem vận hạn năm nay của tôi")
            }
            SuggestionChip(Icons.Filled.Favorite, "Xem hợp tuổi") {
                viewModel.sendMessage("Xem hợp tuổi vợ chồng")
            }
            SuggestionChip(Icons.Filled.Palette, "Ngũ hành & mệnh") {
                viewModel.sendMessage("Phân tích ngũ hành và mệnh của tôi")
            }
            SuggestionChip(Icons.Filled.Pets, "Con giáp năm nay") {
                viewModel.sendMessage("Vận thế con giáp của tôi năm nay")
            }
            SuggestionChip(Icons.Filled.Home, "Phong thủy nhà") {
                viewModel.sendMessage("Tư vấn phong thủy nhà cửa theo tuổi")
            }
            SuggestionChip(Icons.Filled.Store, "Giờ khai trương") {
                viewModel.sendMessage("Giờ tốt khai trương hôm nay")
            }
            SuggestionChip(Icons.Filled.Favorite, "Ngày cưới tốt") {
                viewModel.sendMessage("Tìm ngày cưới tốt tháng này")
            }
            SuggestionChip(Icons.Filled.ChildCare, "Đặt tên con") {
                viewModel.sendMessage("Gợi ý tên đẹp cho con theo ngũ hành")
            }
        }
    }
}

@Composable
private fun SuggestionChip(icon: ImageVector, text: String, onClick: () -> Unit) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .background(c.surfaceContainerHigh, RoundedCornerShape(20.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = c.textPrimary, modifier = Modifier.size(14.dp))
        Text(
            text,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = c.textPrimary
            )
        )
    }
}

// ──────────────────────────────────────────────────────────
// PROFILE COMPLETION BANNER
// ──────────────────────────────────────────────────────────

@Composable
private fun ProfileCompletionBanner(
    missingFields: List<String>,
    onNavigateToProfile: () -> Unit
) {
    val c = LichSoThemeColors.current
    var dismissed by remember { mutableStateOf(false) }

    if (dismissed) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFF3E0), // warm amber-50
                        Color(0xFFFFE0B2)  // amber-100
                    )
                ),
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                Color(0xFFFFB74D).copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFFF9800).copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Hoàn tất hồ sơ cá nhân",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4E342E)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Để AI có thể phân tích tử vi, phong thủy chính xác theo tuổi & mệnh của bạn, hãy cập nhật thông tin cá nhân.",
                    style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF5D4037)
                    )
                )
                if (missingFields.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Còn thiếu: ${missingFields.joinToString(", ")}",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFBF360C)
                        )
                    )
                }
            }

            // Dismiss button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable { dismissed = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Đóng",
                    tint = Color(0xFF8D6E63),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigate to profile button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFFFF8F00),
                    RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onNavigateToProfile)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Edit,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Cập nhật hồ sơ ngay",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
        }
    }
}

// ──────────────────────────────────────────────────────────
// INPUT BAR
// ──────────────────────────────────────────────────────────

@Composable
private fun ChatInputBar(
    inputText: TextFieldValue,
    onInputChange: (TextFieldValue) -> Unit,
    isTyping: Boolean,
    focusRequester: FocusRequester,
    onSend: () -> Unit
) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.bg)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            placeholder = {
                Text(
                    "Hỏi AI về tử vi, ngày tốt...",
                    style = TextStyle(fontSize = 14.sp, color = c.outline)
                )
            },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = c.primary,
                unfocusedBorderColor = c.outlineVariant,
                focusedContainerColor = c.surfaceContainer,
                unfocusedContainerColor = c.surfaceContainer,
                cursorColor = c.primary
            ),
            textStyle = TextStyle(fontSize = 14.sp, color = c.textPrimary),
            singleLine = false,
            maxLines = 4
        )

        // Send button with enabled/disabled state
        val sendEnabled = !isTyping && inputText.text.isNotBlank()
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (sendEnabled) c.primary else c.primary.copy(alpha = 0.4f),
                    CircleShape
                )
                .clickable(enabled = sendEnabled, onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Gửi",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ──────────────────────────────────────────────────────────
// CHAT BUBBLE  — Rich content rendering
// ──────────────────────────────────────────────────────────

/**
 * Content blocks parsed from AI response text.
 */
private sealed class ContentBlock {
    data class TextBlock(val text: String) : ContentBlock()
    data class ResultCard(val rows: List<Pair<String, String>>) : ContentBlock()
    data class Header(val text: String) : ContentBlock()
    data class BulletList(val items: List<String>) : ContentBlock()
}

// ── Regex patterns ──

// Unicode icon characters used by local responses
private val LOCAL_ICONS = setOf(
    '◈', '☽', '◷', '✦', '⊕', '◎', '⊛', '❖', '⟡', '▪', '▫', '△', '◇', '·', '›'
)

/** Local-format key:value (icon + label + colon + value) */
private val localKvRegex = Regex("""^[◈☽◷✦⊕◎⊛❖⟡▪▫△◇·›]\s*(.+?)\s*[:：]\s*(.+)$""")

/**
 * Plain key:value — format yêu cầu từ system prompt.
 * Matches: "Label ngắn: Value gì đó"
 * Không match nếu bắt đầu bằng - (bullet) hoặc # (heading) hoặc | (table).
 */
private val plainKvRegex = Regex("""^(?!\s*[-#|*⚠️►▶])\s*(.{2,40}?)\s*[:：]\s*(.+)$""")

/**
 * Label-only line: "Nên làm:" or "Không nên:" — key with colon but no value.
 * These become Headers when followed by bullet items.
 */
private val labelOnlyRegex = Regex("""^(?!\s*[-#|*⚠️►▶])\s*(.{2,30}?)\s*[:：]\s*$""")

/** Words that ONLY appear at the start of normal sentences — not domain KV keys */
private val sentenceStartWords = setOf(
    "xin", "chào", "tôi", "bạn", "nhìn", "tuy", "vì", "vậy",
    "nói", "dựa", "khi", "nếu", "còn", "đây", "đó",
    "vui", "rất", "khá", "cũng", "và", "hoặc", "nhưng", "tức",
    "để", "cho", "về", "trên", "dưới", "trong", "ngoài",
    "however", "but", "note", "please", "the", "this", "that", "for"
)

/** Check if a plain kv match is really a key:value (not a regular sentence with colon) */
private fun isLikelyKvLine(key: String, value: String): Boolean {
    val keyLower = key.trim().lowercase()
    val keyWords = keyLower.split(Regex("\\s+"))
    // Key should be short (1-5 words)
    if (keyWords.size > 5) return false
    // Key should not start with common sentence-start words (NOT domain terms)
    if (keyWords.firstOrNull() in sentenceStartWords) return false
    // Key should not contain common sentence patterns
    if (keyLower.contains(" là ") || keyLower.contains(" có ") || keyLower.contains(" và ")) return false
    // Value should not be empty
    if (value.isBlank()) return false
    // Key that is all-lowercase long phrase → probably a sentence
    if (keyLower.length > 25 && keyLower == key.trim()) return false
    return true
}

/** Markdown table row: | col | col | ... */
private val mdTableRowRegex = Regex("""^\|(.+\|)+\s*$""")
/** Markdown table separator: |---|---| */
private val mdTableSepRegex = Regex("""^\|[\s:]*-+[\s:]*(\|[\s:]*-+[\s:]*)+\|?\s*$""")
/** Markdown heading ## */
private val mdHeadingRegex = Regex("""^#{1,4}\s+(.+)$""")
/** Standalone bold line: **Title** (used as section header by Gemini) */
private val mdBoldLineRegex = Regex("""^\*\*(.+?)\*\*\s*$""")
/** Markdown bullet: - item or * item or • item  (not ** bold) */
private val mdBulletRegex = Regex("""^[\s]*[-*•]\s+(.+)$""")

/** Regex to extract follow-up suggestions block: ~~~gợi ý ... ~~~ */
private val followUpBlockRegex = Regex("""~~~gợi ý\s*\n([\s\S]*?)~~~""")
/** Each suggestion line: 📌 Text */
private val followUpItemRegex = Regex("""📌\s*(.+)""")

/**
 * Extract follow-up suggestions from AI response and return (cleanedContent, suggestions).
 */
private fun extractFollowUpSuggestions(content: String): Pair<String, List<String>> {
    val match = followUpBlockRegex.find(content) ?: return content to emptyList()
    val suggestionsBlock = match.groupValues[1]
    val suggestions = followUpItemRegex.findAll(suggestionsBlock)
        .map { it.groupValues[1].trim() }
        .filter { it.isNotBlank() }
        .toList()
    val cleanedContent = content.replace(match.value, "").trimEnd()
    return cleanedContent to suggestions
}

private fun isLocalIconLine(line: String): Boolean {
    val trimmed = line.trimStart()
    return trimmed.isNotEmpty() && trimmed.first() in LOCAL_ICONS
}

/**
 * Parse raw AI response into structured content blocks.
 *
 * Priority order for each line:
 * 1. Empty → flush
 * 2. Markdown table separator → skip
 * 3. Markdown table row → ResultCard
 * 4. Markdown heading → Header
 * 5. Markdown bullet → BulletList
 * 6. Local icon key:value → ResultCard
 * 7. Local icon header/bullet → Header or BulletList
 * 8. Plain key:value (from new system prompt format) → ResultCard
 * 9. Separator → flush
 * 10. Regular text → TextBlock
 */
private fun parseContentBlocks(raw: String): List<ContentBlock> {
    val lines = raw.lines()
    val blocks = mutableListOf<ContentBlock>()
    val textBuffer = StringBuilder()
    val cardRows = mutableListOf<Pair<String, String>>()
    val bulletBuffer = mutableListOf<String>()

    fun flushText() {
        val t = textBuffer.toString().trim()
        if (t.isNotEmpty()) blocks.add(ContentBlock.TextBlock(t))
        textBuffer.clear()
    }

    fun flushCard() {
        if (cardRows.isNotEmpty()) {
            blocks.add(ContentBlock.ResultCard(cardRows.toList()))
            cardRows.clear()
        }
    }

    fun flushBullets() {
        if (bulletBuffer.isNotEmpty()) {
            blocks.add(ContentBlock.BulletList(bulletBuffer.toList()))
            bulletBuffer.clear()
        }
    }

    fun flushAll() { flushBullets(); flushCard(); flushText() }

    var i = 0
    while (i < lines.size) {
        val trimmed = lines[i].trim()

        // ── 1. Empty line ──
        if (trimmed.isEmpty()) {
            flushBullets()
            flushCard()
            if (textBuffer.isNotEmpty()) textBuffer.appendLine()
            i++; continue
        }

        // ── 2. Markdown table separator ──
        if (mdTableSepRegex.matches(trimmed)) { i++; continue }

        // ── 3. Markdown table row ──
        if (mdTableRowRegex.matches(trimmed)) {
            flushBullets(); flushText()
            val cells = trimmed.split("|").map { it.trim() }.filter { it.isNotEmpty() }
            if (cells.size == 2) {
                cardRows.add(stripMd(cells[0]) to stripMd(cells[1]))
            } else if (cells.size > 2) {
                flushCard()
                textBuffer.appendLine(cells.joinToString("  ·  ") { stripMd(it) })
            }
            i++; continue
        }

        // ── 4. Markdown heading (## Title) ──
        val headingMatch = mdHeadingRegex.matchEntire(trimmed)
        if (headingMatch != null) {
            flushAll()
            blocks.add(ContentBlock.Header(stripMd(headingMatch.groupValues[1])))
            i++; continue
        }

        // ── 4b. Standalone bold line (**Title**) → Header ──
        val boldLineMatch = mdBoldLineRegex.matchEntire(trimmed)
        if (boldLineMatch != null) {
            flushAll()
            blocks.add(ContentBlock.Header(boldLineMatch.groupValues[1].trim()))
            i++; continue
        }

        // ── 5. Markdown bullet (not **bold line) ──
        val bulletMatch = mdBulletRegex.matchEntire(trimmed)
        if (bulletMatch != null && !trimmed.trimStart().startsWith("**")) {
            flushCard(); flushText()
            bulletBuffer.add(stripMd(bulletMatch.groupValues[1]))
            i++; continue
        }

        // ── 6. Local icon key:value ──
        val localKvMatch = localKvRegex.matchEntire(trimmed)
        if (localKvMatch != null) {
            flushBullets(); flushText()
            cardRows.add(localKvMatch.groupValues[1].trim() to localKvMatch.groupValues[2].trim())
            i++; continue
        }

        // ── 7. Local icon line (header or bullet) ──
        if (isLocalIconLine(trimmed)) {
            val content = trimmed.dropWhile { it in LOCAL_ICONS || it.isWhitespace() }
            val isAllCapsHeader = content.length >= 4 && content.all { ch ->
                ch.isUpperCase() || ch.isWhitespace() || !ch.isLetter()
            }
            if (isAllCapsHeader) {
                flushAll()
                blocks.add(ContentBlock.Header(trimmed))
            } else {
                flushCard(); flushText()
                bulletBuffer.add(trimmed)
            }
            i++; continue
        }

        // ── 8. Label-only line "Nên làm:" (key + colon, no value) → Header ──
        val labelOnlyMatch = labelOnlyRegex.matchEntire(trimmed)
        if (labelOnlyMatch != null) {
            flushAll()
            blocks.add(ContentBlock.Header(labelOnlyMatch.groupValues[1].trim()))
            i++; continue
        }

        // ── 9. Plain key:value (new format from system prompt) ──
        val plainKvMatch = plainKvRegex.matchEntire(trimmed)
        if (plainKvMatch != null) {
            val key = stripMd(plainKvMatch.groupValues[1])
            val value = stripMd(plainKvMatch.groupValues[2])
            if (isLikelyKvLine(key, value)) {
                flushBullets(); flushText()
                cardRows.add(key to value)
                i++; continue
            }
        }

        // ── 10. Separator lines ──
        if (trimmed == "──" || trimmed == "─" || trimmed == "---" || trimmed == "***" || trimmed == "⚠️") {
            flushAll()
            i++; continue
        }

        // ── 11. Regular text ──
        flushBullets(); flushCard()
        if (textBuffer.isNotEmpty() && textBuffer.last() != '\n') textBuffer.appendLine()
        textBuffer.append(trimmed)
        i++
    }

    flushAll()
    return blocks
}

/** Strip markdown inline formatting: **bold**, *italic*, `code` */
private fun stripMd(text: String): String {
    return text
        .replace(Regex("""\*\*\*(.+?)\*\*\*"""), "$1")
        .replace(Regex("""\*\*(.+?)\*\*"""), "$1")
        .replace(Regex("""\*(.+?)\*"""), "$1")
        .replace(Regex("""`(.+?)`"""), "$1")
}

// ──────────────────────────────────────────────────────────
// CHAT BUBBLE
// ──────────────────────────────────────────────────────────

@Composable
private fun ChatBubble(message: ChatMessageEntity, viewModel: ChatViewModel) {
    val c = LichSoThemeColors.current
    val isUser = message.isUser

    // Extract follow-up suggestions for AI messages
    val (cleanContent, followUpSuggestions) = remember(message.content) {
        if (!isUser) extractFollowUpSuggestions(message.content)
        else message.content to emptyList()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        val bubbleShape = RoundedCornerShape(
            topStart = 20.dp, topEnd = 20.dp,
            bottomStart = if (isUser) 20.dp else 6.dp,
            bottomEnd = if (isUser) 6.dp else 20.dp
        )

        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .background(
                    if (isUser) c.primary else c.surfaceContainer,
                    bubbleShape
                )
                .padding(14.dp)
        ) {
            if (isUser) {
                // ── USER BUBBLE ──
                Text(
                    text = message.content,
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = Color.White
                    )
                )
            } else {
                // ── AI BUBBLE ── with rich formatting
                // Header: sparkle icon + "Phân tích AI"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = c.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "Phân tích AI",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = c.primary
                        )
                    )
                }

                // Parse content into rich blocks (use cleaned content without suggestion block)
                val blocks = remember(cleanContent) { parseContentBlocks(cleanContent) }

                blocks.forEach { block ->
                    when (block) {
                        is ContentBlock.Header -> {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = block.text,
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 22.sp,
                                    color = c.textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        is ContentBlock.TextBlock -> {
                            Text(
                                text = parseInlineMarkdown(block.text, c),
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    lineHeight = 21.sp,
                                    color = c.textPrimary
                                )
                            )
                        }

                        is ContentBlock.ResultCard -> {
                            ResultCardView(rows = block.rows)
                        }

                        is ContentBlock.BulletList -> {
                            BulletListView(items = block.items)
                        }
                    }
                }
            }

            // Timestamp
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = viewModel.formatTime(message.timestamp),
                style = TextStyle(
                    fontSize = 9.5.sp,
                    color = if (isUser) Color.White.copy(alpha = 0.6f) else c.outline
                ),
                modifier = Modifier.align(Alignment.End)
            )
        }

        // ── FOLLOW-UP SUGGESTIONS ── (below AI bubble)
        if (!isUser && followUpSuggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FollowUpSuggestionsRow(
                suggestions = followUpSuggestions,
                onSuggestionClick = { viewModel.sendMessage(it) }
            )
        }
    }
}

// ──────────────────────────────────────────────────────────
// FOLLOW-UP SUGGESTIONS — Clickable chips after AI response
// ──────────────────────────────────────────────────────────

@Composable
private fun FollowUpSuggestionsRow(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            "Hỏi tiếp:",
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = c.outline,
                letterSpacing = 0.3.sp
            )
        )
        suggestions.forEach { suggestion ->
            Row(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            listOf(
                                c.surfaceContainerHigh,
                                c.surfaceContainer
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSuggestionClick(suggestion) }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Filled.TouchApp,
                    contentDescription = null,
                    tint = c.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    suggestion,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = c.primary
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────
// RESULT CARD — Structured key-value rows
// ──────────────────────────────────────────────────────────

@Composable
private fun ResultCardView(rows: List<Pair<String, String>>) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(c.surfaceContainerHigh, RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        rows.forEachIndexed { index, (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = c.textSecondary
                    ),
                    modifier = Modifier.weight(0.4f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = value,
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = getValueColor(value, c)
                    ),
                    modifier = Modifier.weight(0.6f)
                )
            }
            if (index < rows.lastIndex) {
                HorizontalDivider(
                    color = c.outlineVariant,
                    thickness = 0.5.dp
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────
// BULLET LIST
// ──────────────────────────────────────────────────────────

@Composable
private fun BulletListView(items: List<String>) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "•",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = c.textSecondary
                    ),
                    modifier = Modifier.padding(top = 1.dp)
                )
                // If bullet item has "key: value" format, bold the key
                val colonIdx = item.indexOf(':')
                val bulletText = if (colonIdx in 1..35) {
                    val key = item.substring(0, colonIdx)
                    val value = item.substring(colonIdx + 1).trim()
                    // Only bold-key if key is short and value is non-empty
                    if (value.isNotEmpty() && key.split(" ").size <= 5) {
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(key)
                            }
                            append(": ")
                            val kwColor = getValueColor(value, c)
                            if (kwColor != c.textPrimary) {
                                withStyle(SpanStyle(color = kwColor)) { append(value) }
                            } else {
                                append(value)
                            }
                        }
                    } else {
                        parseInlineMarkdown(item, c)
                    }
                } else {
                    parseInlineMarkdown(item, c)
                }
                Text(
                    text = bulletText,
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = c.textPrimary
                    )
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────
// VALUE COLOR — green/red/primary for result card values
// ──────────────────────────────────────────────────────────

private fun getValueColor(value: String, c: LichSoColors): Color {
    val lower = value.lowercase()
    return when {
        lower.contains("hoàng đạo") || lower.contains("rất hợp") || lower.contains("rất tốt") ||
        lower.contains("phù hợp") || lower.contains("✓") || lower.contains("★★★") ||
        lower.contains("✅") || lower.contains("hoàn thành") ||
        lower.contains("tốt") && !lower.contains("không") -> c.goodGreen

        lower.contains("xấu") || lower.contains("không nên") || lower.contains("hắc đạo") ||
        lower.contains("phạm") || lower.contains("xung") || lower.contains("✗") ||
        lower.contains("tránh") || lower.contains("không phù hợp") -> c.badRed

        lower.contains("giờ") && (lower.contains("h-") || lower.contains("h)") || lower.contains(":")) -> c.primary

        else -> c.textPrimary
    }
}

// ──────────────────────────────────────────────────────────
// INLINE MARKDOWN PARSER → AnnotatedString
// Handles: **bold**, *italic*, ***bold italic***, `code`
// Also applies keyword-based coloring for Vietnamese terms.
// ──────────────────────────────────────────────────────────

/**
 * Inline token types for markdown parsing.
 */
private sealed class InlineToken {
    data class Plain(val text: String) : InlineToken()
    data class Bold(val text: String) : InlineToken()
    data class Italic(val text: String) : InlineToken()
    data class BoldItalic(val text: String) : InlineToken()
    data class Code(val text: String) : InlineToken()
}

/**
 * Parse inline markdown to AnnotatedString with proper styling.
 */
@Composable
private fun parseInlineMarkdown(text: String, c: LichSoColors): AnnotatedString {
    // Tokenize: find all **bold**, *italic*, ***both***, `code` spans
    val tokens = tokenizeInlineMarkdown(text)

    return buildAnnotatedString {
        for (token in tokens) {
            when (token) {
                is InlineToken.BoldItalic -> {
                    val style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = getKeywordColor(token.text, c) ?: Color.Unspecified
                    )
                    withStyle(style) { append(token.text) }
                }
                is InlineToken.Bold -> {
                    val style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = getKeywordColor(token.text, c) ?: Color.Unspecified
                    )
                    withStyle(style) { append(token.text) }
                }
                is InlineToken.Italic -> {
                    val style = SpanStyle(
                        fontStyle = FontStyle.Italic,
                        color = getKeywordColor(token.text, c) ?: Color.Unspecified
                    )
                    withStyle(style) { append(token.text) }
                }
                is InlineToken.Code -> {
                    withStyle(SpanStyle(
                        fontWeight = FontWeight.Medium,
                        background = c.surfaceContainerHigh,
                        color = c.primary
                    )) { append(token.text) }
                }
                is InlineToken.Plain -> {
                    val kwColor = getKeywordColor(token.text, c)
                    if (kwColor != null) {
                        withStyle(SpanStyle(color = kwColor, fontWeight = FontWeight.SemiBold)) {
                            append(token.text)
                        }
                    } else {
                        append(token.text)
                    }
                }
            }
        }
    }
}

/**
 * Tokenize text into inline markdown tokens.
 * Order matters: check *** before **, check ** before *.
 */
private fun tokenizeInlineMarkdown(text: String): List<InlineToken> {
    val tokens = mutableListOf<InlineToken>()
    // Combined regex: ***bold italic***, **bold**, *italic*, `code`
    val regex = Regex("""\*\*\*(.+?)\*\*\*|\*\*(.+?)\*\*|\*(.+?)\*|`(.+?)`""")

    var lastEnd = 0
    for (match in regex.findAll(text)) {
        if (match.range.first > lastEnd) {
            tokens.add(InlineToken.Plain(text.substring(lastEnd, match.range.first)))
        }
        when {
            match.groupValues[1].isNotEmpty() -> tokens.add(InlineToken.BoldItalic(match.groupValues[1]))
            match.groupValues[2].isNotEmpty() -> tokens.add(InlineToken.Bold(match.groupValues[2]))
            match.groupValues[3].isNotEmpty() -> tokens.add(InlineToken.Italic(match.groupValues[3]))
            match.groupValues[4].isNotEmpty() -> tokens.add(InlineToken.Code(match.groupValues[4]))
        }
        lastEnd = match.range.last + 1
    }
    if (lastEnd < text.length) {
        tokens.add(InlineToken.Plain(text.substring(lastEnd)))
    }
    return tokens
}

/**
 * Returns a highlight color for Vietnamese feng-shui keywords, or null if none match.
 */
private fun getKeywordColor(text: String, c: LichSoColors): Color? {
    val lower = text.lowercase()
    return when {
        lower.contains("rất tốt") || lower.contains("phù hợp") ||
        lower.contains("hoàng đạo") || lower.contains("đại cát") ||
        lower.contains("tốt lắm") -> c.goodGreen

        lower.contains("ngày xấu") || lower.contains("không phù hợp") ||
        lower.contains("hắc đạo") || lower.contains("đại hung") ||
        lower.contains("kiêng kỵ") -> c.badRed

        else -> null
    }
}

// ──────────────────────────────────────────────────────────
// TYPING INDICATOR — 3 bouncing dots
// ──────────────────────────────────────────────────────────

@Composable
private fun TypingIndicator() {
    val c = LichSoThemeColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    Row(
        modifier = Modifier
            .background(
                c.surfaceContainer,
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp)
            )
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) { index ->
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200)
                ),
                label = "dot$index"
            )
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200)
                ),
                label = "alpha$index"
            )
            Box(
                modifier = Modifier
                    .offset(y = offsetY.dp)
                    .size(8.dp)
                    .background(c.outline.copy(alpha = alpha), CircleShape)
            )
        }
    }
}
