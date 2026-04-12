package com.lichso.app.ui.screen.prayers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.ui.theme.*
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.components.HeaderIconButton
import java.util.Locale

// ══════════════════════════════════════════════════════════════
// Màn hình Văn Khấn — based on v2/screen-prayers.html mock
// ══════════════════════════════════════════════════════════════

@Composable
fun PrayersScreen(
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onDetailVisibilityChanged: (Boolean) -> Unit = {},
    initialPrayerId: Int? = null,
    viewModel: PrayersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Auto-open prayer detail if initialPrayerId is provided
    LaunchedEffect(initialPrayerId) {
        if (initialPrayerId != null) {
            viewModel.openPrayerById(initialPrayerId)
        }
    }

    // Notify parent when detail visibility changes
    LaunchedEffect(uiState.showDetail) {
        onDetailVisibilityChanged(uiState.showDetail)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── List view ──
        PrayerListScreen(
            uiState = uiState,
            viewModel = viewModel,
            onBackClick = onBackClick,
            onMenuClick = onMenuClick
        )

        // ── Detail overlay (slide up) ──
        AnimatedVisibility(
            visible = uiState.showDetail && uiState.selectedPrayer != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(200))
        ) {
            uiState.selectedPrayer?.let { prayer ->
                PrayerDetailScreen(
                    prayer = prayer,
                    onBack = { viewModel.closeDetail() }
                )
            }
        }
    }
}

// ══════════════════════════════════════════
// PRAYER LIST SCREEN
// ══════════════════════════════════════════
@Composable
private fun PrayerListScreen(
    uiState: PrayersUiState,
    viewModel: PrayersViewModel,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit = {}
) {
    val c = LichSoThemeColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .imePadding()
    ) {
        // ── Header ──
        PrayerHeader(onMenuClick = onMenuClick)

        // ── Search Bar ──
        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = { viewModel.updateSearchQuery(it) }
        )

        // ── Category Chips ──
        CategoryChips(
            categories = viewModel.categories,
            selectedId = uiState.selectedCategoryId,
            onSelect = { viewModel.selectCategory(it) }
        )

        // ── Content ──
        val grouped = viewModel.getGroupedPrayers()
        val showFeatured = uiState.selectedCategoryId == "all" && uiState.searchQuery.isBlank()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
        ) {
            // Featured card
            if (showFeatured) {
                item {
                    FeaturedCard(
                        prayer = viewModel.featuredPrayer,
                        onClick = { viewModel.openPrayerDetail(viewModel.featuredPrayer) }
                    )
                }
            }

            // Grouped sections
            val sectionOrder = listOf("gio", "ram", "tet", "nhap", "khai", "xe", "chua", "gia", "dat")
            val displayedSections = mutableSetOf<String>() // avoid duplicate section headers

            sectionOrder.forEach { catId ->
                val prayers = grouped[catId] ?: return@forEach
                val (_, sectionTitle) = viewModel.getCategoryLabel(catId)

                // Section header (group by section title to merge nhap/khai/xe)
                if (sectionTitle !in displayedSections) {
                    displayedSections.add(sectionTitle)
                    item {
                        SectionHeader(title = sectionTitle, catId = catId)
                    }
                }

                items(prayers, key = { it.id }) { prayer ->
                    PrayerCard(
                        prayer = prayer,
                        onClick = { viewModel.openPrayerDetail(prayer) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Empty state
            if (grouped.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.SearchOff, contentDescription = null, tint = c.outline, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Không tìm thấy bài văn khấn",
                            style = TextStyle(fontSize = 14.sp, color = c.textTertiary)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(96.dp)) }
        }
    }
}

// ══════════════════════════════════════════
// HEADER
// ══════════════════════════════════════════
@Composable
private fun PrayerHeader(onMenuClick: () -> Unit) {
    AppTopBar(
        title = "Văn Khấn",
        onBackClick = onMenuClick,
        leadingIcon = Icons.Filled.Menu
    )
}

// ══════════════════════════════════════════
// SEARCH BAR
// ══════════════════════════════════════════
@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    val c = LichSoThemeColors.current

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .background(c.surfaceContainer, RoundedCornerShape(28.dp))
            .border(1.5.dp, c.outlineVariant, RoundedCornerShape(28.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = c.outline, modifier = Modifier.size(20.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    "Tìm bài văn khấn...",
                    style = TextStyle(fontSize = 14.sp, color = c.outline)
                )
            }
            androidx.compose.foundation.text.BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(fontSize = 14.sp, color = c.textPrimary),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Xoá",
                tint = c.outline,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onQueryChange("") }
            )
        }
    }
}

// ══════════════════════════════════════════
// CATEGORY CHIPS
// ══════════════════════════════════════════
@Composable
private fun CategoryChips(
    categories: List<PrayerCategory>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    val c = LichSoThemeColors.current

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        items(categories, key = { it.id }) { cat ->
            val isActive = cat.id == selectedId
            val bg = if (isActive) c.primary else c.bg
            val textColor = if (isActive) Color.White else c.textSecondary
            val borderColor = if (isActive) c.primary else c.outlineVariant

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg, RoundedCornerShape(20.dp))
                    .border(1.5.dp, borderColor, RoundedCornerShape(20.dp))
                    .clickable { onSelect(cat.id) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Icon(
                    imageVector = PrayerIcons.fromEmoji(cat.emoji),
                    contentDescription = null,
                    tint = if (isActive) Color.White else c.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    cat.label,
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                )
            }
        }
    }
}

// ══════════════════════════════════════════
// FEATURED CARD
// ══════════════════════════════════════════
@Composable
private fun FeaturedCard(prayer: PrayerItem, onClick: () -> Unit) {
    val c = LichSoThemeColors.current

    val cardBg = if (c.isDark) {
        Brush.linearGradient(listOf(Color(0xFF2E2510), Color(0xFF3A2E15)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3)))
    }
    val borderColor = if (c.isDark) Color(0xFFFFE082).copy(alpha = 0.3f) else Color(0xFFFFE082)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg, RoundedCornerShape(20.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFD4A017), Color(0xFFB8860B))),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PrayerIcons.fromEmoji(prayer.emoji, prayer.emojiStyle),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Hay dùng nhất",
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100),
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    prayer.name,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = c.textPrimary,
                        lineHeight = 22.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    prayer.description,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = c.textSecondary,
                        lineHeight = 17.sp
                    )
                )
            }
        }
    }
}

// ══════════════════════════════════════════
// SECTION HEADER
// ══════════════════════════════════════════
@Composable
private fun SectionHeader(title: String, catId: String) {
    val c = LichSoThemeColors.current
    val icon = when {
        catId == "gio" -> Icons.Filled.LocalFireDepartment
        catId == "ram" -> Icons.Filled.Nightlight
        catId == "tet" -> Icons.Filled.Celebration
        catId in listOf("nhap", "khai", "xe") -> Icons.Filled.Home
        catId == "chua" -> Icons.Filled.AccountBalance
        else -> Icons.Filled.Article
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 14.dp, bottom = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = c.primary, modifier = Modifier.size(18.dp))
        Text(
            title,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
        )
    }
}

// ══════════════════════════════════════════
// PRAYER CARD
// ══════════════════════════════════════════
@Composable
private fun PrayerCard(prayer: PrayerItem, onClick: () -> Unit) {
    val c = LichSoThemeColors.current

    val emojiBg = when (prayer.emojiStyle) {
        "gio" -> if (c.isDark) Color(0xFF3A2A1B) else Color(0xFFFFF3E0)
        "ram" -> if (c.isDark) Color(0xFF2A1B3A) else Color(0xFFF3E5F5)
        "tet" -> if (c.isDark) Color(0xFF3A1B1B) else Color(0xFFFFEBEE)
        "khai" -> if (c.isDark) Color(0xFF1B3A2F) else Color(0xFFE8F5E9)
        "nhap" -> if (c.isDark) Color(0xFF1B2A3A) else Color(0xFFE3F2FD)
        "cong" -> if (c.isDark) Color(0xFF2E2510) else Color(0xFFFFF8E1)
        "xe" -> if (c.isDark) Color(0xFF2A2A2A) else Color(0xFFECEFF1)
        "chua" -> if (c.isDark) Color(0xFF3A1B2A) else Color(0xFFFCE4EC)
        "than" -> if (c.isDark) Color(0xFF2A2A2A) else Color(0xFFFAFAFA)
        else -> c.surfaceContainer
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .then(
                if (prayer.isPopular) Modifier.border(
                    width = 1.dp,
                    color = c.primary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                else Modifier.border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Emoji icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(emojiBg, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            val iconTint = when (prayer.emojiStyle) {
                "gio" -> if (c.isDark) Color(0xFFFFAB40) else Color(0xFFE65100)
                "ram" -> if (c.isDark) Color(0xFFCE93D8) else Color(0xFF7B1FA2)
                "tet" -> if (c.isDark) Color(0xFFEF9A9A) else Color(0xFFC62828)
                "khai" -> if (c.isDark) Color(0xFFA5D6A7) else Color(0xFF2E7D32)
                "nhap" -> if (c.isDark) Color(0xFF90CAF9) else Color(0xFF1565C0)
                "cong" -> if (c.isDark) Color(0xFFFFE082) else Color(0xFFE65100)
                "xe" -> if (c.isDark) Color(0xFFB0BEC5) else Color(0xFF37474F)
                "chua" -> if (c.isDark) Color(0xFFF48FB1) else Color(0xFFC2185B)
                "than" -> if (c.isDark) Color(0xFFBDBDBD) else Color(0xFF424242)
                else -> c.textSecondary
            }
            Icon(
                imageVector = PrayerIcons.fromEmoji(prayer.emoji, prayer.emojiStyle),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                prayer.name,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                    lineHeight = 19.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                prayer.description,
                style = TextStyle(
                    fontSize = 11.sp,
                    color = c.textSecondary,
                    lineHeight = 15.sp
                ),
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Tags
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                prayer.tags.forEach { tag ->
                    val tagBg = when (tag.type) {
                        PrayerTagType.HOT -> if (c.isDark) Color(0xFF3A1B1B) else Color(0xFFFFEBEE)
                        PrayerTagType.NEW -> if (c.isDark) Color(0xFF1B3A2F) else Color(0xFFE8F5E9)
                        PrayerTagType.NORMAL -> if (c.isDark) c.surface else c.bg3
                    }
                    val tagColor = when (tag.type) {
                        PrayerTagType.HOT -> c.badRed
                        PrayerTagType.NEW -> c.goodGreen
                        PrayerTagType.NORMAL -> c.textSecondary
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .background(tagBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        // Hot/New indicator dot instead of emoji
                        if (tag.type == PrayerTagType.HOT) {
                            Icon(
                                Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = tagColor,
                                modifier = Modifier.size(10.dp)
                            )
                        } else if (tag.type == PrayerTagType.NEW) {
                            Icon(
                                Icons.Filled.FiberNew,
                                contentDescription = null,
                                tint = tagColor,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                        Text(
                            tag.label,
                            style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = tagColor)
                        )
                    }
                }
            }
        }

        // Arrow
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = c.outlineVariant,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
    }
}

// ══════════════════════════════════════════════════════════════
// PRAYER DETAIL SCREEN (full-screen overlay)
// ══════════════════════════════════════════════════════════════
@Composable
private fun PrayerDetailScreen(
    prayer: PrayerItem,
    onBack: () -> Unit
) {
    val c = LichSoThemeColors.current
    val context = LocalContext.current

    // ── TTS engine ──
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }
    var ttsInitialized by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Set Vietnamese voice inside callback where engine is ready
                val e = engine ?: return@TextToSpeech
                val viLocale = Locale("vi", "VN")
                val result = e.setLanguage(viLocale)

                // If simple locale didn't work, search available voices
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val viVoice = e.voices?.firstOrNull { voice ->
                        voice.locale.language == "vi" && !voice.isNetworkConnectionRequired
                    } ?: e.voices?.firstOrNull { voice ->
                        voice.locale.language == "vi"
                    }
                    if (viVoice != null) {
                        e.voice = viVoice
                    }
                }
                ttsInitialized = true
            }
        }
        tts = engine
        onDispose {
            engine?.stop()
            engine?.shutdown()
        }
    }

    // Track speaking state
    LaunchedEffect(tts) {
        tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { isSpeaking = true }
            override fun onDone(utteranceId: String?) { isSpeaking = false }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { isSpeaking = false }
        })
    }

    // Clean prayer text (strip emphasis markers for TTS & clipboard)
    val plainText = remember(prayer.content) {
        prayer.content
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("\\*(.*?)\\*"), "$1")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        // ── Detail Header ──
        DetailHeader(
            prayer = prayer,
            onBack = {
                tts?.stop()
                onBack()
            },
            onShare = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_SUBJECT, prayer.name)
                    putExtra(Intent.EXTRA_TEXT, "${prayer.name}\n\n$plainText")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Chia sẻ văn khấn"))
            }
        )

        // ── Detail Content ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Prayer title
            Text(
                prayer.name,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary,
                    lineHeight = 28.sp
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Prayer text
            PrayerTextBlock(content = prayer.content)

            // Note card
            if (prayer.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                NoteCard(note = prayer.note)
            }
        }

        // ── Bottom Bar ──
        DetailBottomBar(
            isSpeaking = isSpeaking,
            onCopy = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(prayer.name, "${prayer.name}\n\n$plainText")
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Đã sao chép văn khấn", Toast.LENGTH_SHORT).show()
            },
            onReadAloud = {
                val engine = tts
                if (engine == null || !ttsInitialized) {
                    Toast.makeText(context, "Đang khởi tạo, vui lòng thử lại", Toast.LENGTH_SHORT).show()
                    return@DetailBottomBar
                }
                if (isSpeaking) {
                    engine.stop()
                    isSpeaking = false
                } else {
                    // Ensure Vietnamese voice is set before speaking
                    val viLocale = Locale("vi", "VN")
                    val langResult = engine.setLanguage(viLocale)
                    if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        // Fallback: find Vietnamese voice from available voices
                        val viVoice = engine.voices?.firstOrNull { voice ->
                            voice.locale.language == "vi" && !voice.isNetworkConnectionRequired
                        } ?: engine.voices?.firstOrNull { voice ->
                            voice.locale.language == "vi"
                        }
                        if (viVoice != null) {
                            engine.voice = viVoice
                        } else {
                            Toast.makeText(context, "Không tìm thấy giọng tiếng Việt. Vui lòng cài đặt trong Google TTS.", Toast.LENGTH_LONG).show()
                            return@DetailBottomBar
                        }
                    }
                    engine.setSpeechRate(0.85f)
                    engine.setPitch(1.0f)

                    // Split into chunks (TTS has a ~4000 char limit per utterance)
                    val chunks = plainText.chunked(3500)
                    chunks.forEachIndexed { index, chunk ->
                        val params = android.os.Bundle()
                        engine.speak(
                            chunk,
                            if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
                            params,
                            "prayer_chunk_$index"
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun DetailHeader(prayer: PrayerItem, onBack: () -> Unit, onShare: () -> Unit = {}) {
    AppTopBar(
        title = "Văn Khấn",
        onBackClick = onBack,
        actions = {
            HeaderIconButton(
                icon = Icons.Filled.Share,
                contentDescription = "Chia sẻ",
                onClick = onShare
            )
        }
    )
}

@Composable
private fun PrayerTextBlock(content: String) {
    val c = LichSoThemeColors.current

    // Parse content: lines with …(...)… become emphasized
    val annotated = buildAnnotatedString {
        val lines = content.split("\n")
        lines.forEachIndexed { index, line ->
            // Detect emphasis patterns: text between …(  )… or (3 lần) etc.
            val emphasisRegex = Regex("…\\([^)]*\\)…|\\([^)]*lần[^)]*\\)|\\([^)]*vái[^)]*\\)")
            var lastEnd = 0
            emphasisRegex.findAll(line).forEach { match ->
                // Normal text before match
                withStyle(SpanStyle(color = c.textPrimary)) {
                    append(line.substring(lastEnd, match.range.first))
                }
                // Emphasized text
                withStyle(SpanStyle(color = c.primary, fontWeight = FontWeight.Bold)) {
                    append(match.value)
                }
                lastEnd = match.range.last + 1
            }
            // Remaining normal text
            if (lastEnd < line.length) {
                withStyle(SpanStyle(color = c.textPrimary)) {
                    append(line.substring(lastEnd))
                }
            }
            if (index < lines.lastIndex) append("\n")
        }
    }

    Text(
        text = annotated,
        style = TextStyle(
            fontFamily = FontFamily.Serif,
            fontSize = 15.sp,
            lineHeight = 28.sp
        )
    )
}

@Composable
private fun NoteCard(note: String) {
    val c = LichSoThemeColors.current
    val bg = if (c.isDark) Color(0xFF2E2510) else Color(0xFFFFF8E1)
    val border = if (c.isDark) Color(0xFFFFE082).copy(alpha = 0.2f) else Color(0xFFFFE082)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(16.dp))
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Icon(
                Icons.Filled.Lightbulb,
                contentDescription = null,
                tint = Color(0xFFE65100),
                modifier = Modifier.size(16.dp)
            )
            Text(
                "Lưu ý",
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
            )
        }
        Text(
            note,
            style = TextStyle(
                fontSize = 12.sp,
                color = c.textSecondary,
                lineHeight = 18.sp
            )
        )
    }
}

@Composable
private fun DetailBottomBar(
    isSpeaking: Boolean = false,
    onCopy: () -> Unit = {},
    onReadAloud: () -> Unit = {}
) {
    val c = LichSoThemeColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.bg)
            .border(
                width = 1.dp,
                color = c.outlineVariant,
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
            )
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Copy button
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(c.surfaceContainer, RoundedCornerShape(14.dp))
                .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
                .clickable { onCopy() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.ContentCopy, null, tint = c.textPrimary, modifier = Modifier.size(18.dp))
                Text(
                    "Sao chép",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
                )
            }
        }

        // Read aloud button
        val ttsBackground = if (isSpeaking) c.badRed else c.primary
        val ttsIcon = if (isSpeaking) Icons.Filled.Stop else Icons.Filled.RecordVoiceOver
        val ttsLabel = if (isSpeaking) "Dừng đọc" else "Đọc to"

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(ttsBackground, RoundedCornerShape(14.dp))
                .clickable { onReadAloud() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(ttsIcon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Text(
                    ttsLabel,
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                )
            }
        }
    }
}
