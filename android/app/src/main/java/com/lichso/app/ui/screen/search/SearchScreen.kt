package com.lichso.app.ui.screen.search

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.ui.theme.LichSoThemeColors
import com.lichso.app.ui.components.AppTopBar

// ══════════════════════════════════════════════════════════
// SEARCH SCREEN — Matching screen-search.html mock
// ══════════════════════════════════════════════════════════

@Composable
fun SearchScreen(
    onBackClick: () -> Unit = {},
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit = { _, _, _ -> },
    onGoodDaysClick: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val c = LichSoThemeColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Lunar converter state
    var showLunarConverter by remember { mutableStateOf(false) }
    var lunarDayInput by remember { mutableStateOf("") }
    var lunarMonthInput by remember { mutableStateOf("") }
    var conversionResult by remember { mutableStateOf<LunarConversion?>(null) }

    // Auto-focus search bar
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .imePadding()
    ) {
        // ═══ TOP BAR ═══
        AppTopBar(
            title = "Tìm kiếm",
            onBackClick = onBackClick
        )

        // ═══ SEARCH BAR ═══
        SearchBar(
            query = state.query,
            onQueryChange = { viewModel.onQueryChange(it) },
            onClear = { viewModel.clearQuery() },
            onSearch = {
                viewModel.saveSearchToRecent(state.query)
                keyboardController?.hide()
            },
            focusRequester = focusRequester
        )

        // ═══ QUICK ACTIONS ═══
        AnimatedVisibility(
            visible = state.query.isBlank(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            QuickActionsSection(
                onConvertClick = { showLunarConverter = !showLunarConverter },
                onGoodDaysClick = onGoodDaysClick,
                onGoToDateClick = {
                    viewModel.onQueryChange("")
                    focusRequester.requestFocus()
                }
            )
        }

        // ═══ CONTENT AREA ═══
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // ── Lunar Converter Card ──
            AnimatedVisibility(
                visible = showLunarConverter,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LunarConverterCard(
                    lunarDay = lunarDayInput,
                    lunarMonth = lunarMonthInput,
                    onLunarDayChange = { lunarDayInput = it },
                    onLunarMonthChange = { lunarMonthInput = it },
                    result = conversionResult,
                    onConvert = {
                        val d = lunarDayInput.toIntOrNull() ?: 0
                        val m = lunarMonthInput.toIntOrNull() ?: 0
                        conversionResult = viewModel.convertLunarToSolar(d, m)
                    }
                )
            }

            // ── Search Results ──
            if (state.results.isNotEmpty()) {
                SectionLabel("Kết quả tìm kiếm")
                Spacer(modifier = Modifier.height(4.dp))
                state.results.forEach { result ->
                    SearchResultCard(
                        result = result,
                        query = state.query,
                        onClick = {
                            viewModel.saveSearchToRecent(state.query)
                            if (result.solarDay > 0 && result.solarMonth > 0 && result.solarYear > 0) {
                                onDateSelected(result.solarYear, result.solarMonth, result.solarDay)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Inline Lunar conversion if available
                state.lunarConversion?.let { conv ->
                    LunarConversionResultCard(conv)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // ── Recent Searches (only when no query) ──
            if (state.query.isBlank() && state.recentSearches.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel("Tìm gần đây")
                    if (state.recentSearches.size > 2) {
                        TextButton(
                            onClick = { viewModel.clearAllRecent() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                "Xoá tất cả",
                                style = TextStyle(fontSize = 12.sp, color = c.textTertiary)
                            )
                        }
                    }
                }
                state.recentSearches.forEach { query ->
                    RecentSearchItem(
                        text = query,
                        onClick = {
                            viewModel.onQueryChange(query)
                        },
                        onRemove = { viewModel.removeRecentSearch(query) }
                    )
                }
            }

            // ── Empty State ──
            if (state.query.isNotBlank() && state.results.isEmpty()) {
                Spacer(modifier = Modifier.height(40.dp))
                EmptySearchState(query = state.query)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ══════════════════════════════════════════
// SEARCH BAR
// ══════════════════════════════════════════

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit,
    focusRequester: FocusRequester
) {
    val c = LichSoThemeColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search field
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    "Tìm ngày, sự kiện, ngày lễ...",
                    style = TextStyle(fontSize = 15.sp, color = c.outline)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = c.primary,
                    modifier = Modifier.size(22.dp)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(c.surfaceContainerHigh, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Xoá",
                                tint = c.textTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = c.primary,
                unfocusedBorderColor = c.outlineVariant,
                focusedContainerColor = c.surfaceContainerHigh,
                unfocusedContainerColor = c.surfaceContainerHigh,
                cursorColor = c.primary
            ),
            textStyle = TextStyle(fontSize = 15.sp, color = c.textPrimary)
        )
    }
}

// ══════════════════════════════════════════
// QUICK ACTIONS
// ══════════════════════════════════════════

@Composable
private fun QuickActionsSection(
    onConvertClick: () -> Unit,
    onGoodDaysClick: () -> Unit,
    onGoToDateClick: () -> Unit
) {
    val c = LichSoThemeColors.current

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            "TRA CỨU NHANH",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = c.outline,
                letterSpacing = 0.5.sp
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionItem(
                icon = Icons.Filled.SwapHoriz,
                label = "Đổi\nÂm → Dương",
                color = Color(0xFFC62828),
                modifier = Modifier.weight(1f),
                onClick = onConvertClick
            )
            QuickActionItem(
                icon = Icons.Filled.EventAvailable,
                label = "Ngày tốt\ntuần này",
                color = Color(0xFFF57F17),
                modifier = Modifier.weight(1f),
                onClick = onGoodDaysClick
            )
            QuickActionItem(
                icon = Icons.Filled.AutoAwesome,
                label = "Tuổi\nhợp",
                color = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f),
                onClick = { /* TODO */ }
            )
            QuickActionItem(
                icon = Icons.Filled.CalendarMonth,
                label = "Đi đến\nngày",
                color = Color(0xFF1565C0),
                modifier = Modifier.weight(1f),
                onClick = onGoToDateClick
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val c = LichSoThemeColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Text(
            label,
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = c.textTertiary,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
        )
    }
}

// ══════════════════════════════════════════
// SEARCH RESULT CARD
// ══════════════════════════════════════════

@Composable
private fun SearchResultCard(
    result: SearchResult,
    query: String,
    onClick: () -> Unit
) {
    val c = LichSoThemeColors.current

    val (iconBg, iconColor, icon) = when (result.type) {
        SearchResultType.HOLIDAY -> Triple(
            Color(0xFFFFF3E0), Color(0xFFE65100), Icons.Filled.Celebration
        )
        SearchResultType.DATE -> Triple(
            c.primaryContainer, c.primary, Icons.Filled.CalendarToday
        )
        SearchResultType.LUNAR -> Triple(
            Color(0xFFFFF8E1), Color(0xFFF57F17), Icons.Filled.DarkMode
        )
        SearchResultType.ZODIAC -> Triple(
            Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Filled.AutoAwesome
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconBg, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            // Title with highlight
            Text(
                text = buildHighlightedText(result.title, query, c.primary, c.primaryContainer),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                result.description,
                style = TextStyle(fontSize = 11.sp, color = c.textTertiary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Arrow
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = c.outlineVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun buildHighlightedText(
    text: String,
    highlight: String,
    highlightColor: Color,
    highlightBg: Color
) = buildAnnotatedString {
    if (highlight.isBlank()) {
        append(text)
        return@buildAnnotatedString
    }
    val lowText = text.lowercase()
    val lowHighlight = highlight.lowercase()
    var start = 0
    var idx = lowText.indexOf(lowHighlight)
    while (idx >= 0) {
        append(text.substring(start, idx))
        withStyle(SpanStyle(color = highlightColor, background = highlightBg)) {
            append(text.substring(idx, idx + highlight.length))
        }
        start = idx + highlight.length
        idx = lowText.indexOf(lowHighlight, start)
    }
    append(text.substring(start))
}

// ══════════════════════════════════════════
// LUNAR CONVERTER CARD
// ══════════════════════════════════════════

@Composable
private fun LunarConverterCard(
    lunarDay: String,
    lunarMonth: String,
    onLunarDayChange: (String) -> Unit,
    onLunarMonthChange: (String) -> Unit,
    result: LunarConversion?,
    onConvert: () -> Unit
) {
    val c = LichSoThemeColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .background(
                Brush.linearGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFFDE7))),
                RoundedCornerShape(20.dp)
            )
            .border(1.dp, Color(0xFFFFE082), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Filled.SwapHoriz,
                contentDescription = null,
                tint = Color(0xFFD4A017),
                modifier = Modifier.size(18.dp)
            )
            Text(
                "Chuyển đổi Âm ↔ Dương",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4A017)
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Day input
            OutlinedTextField(
                value = lunarDay,
                onValueChange = { if (it.length <= 2) onLunarDayChange(it.filter { ch -> ch.isDigit() }) },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Ngày", style = TextStyle(fontSize = 14.sp, color = c.outline, textAlign = TextAlign.Center))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFE082),
                    unfocusedBorderColor = Color(0xFFFFE082),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = Color(0xFFD4A017)
                ),
                textStyle = TextStyle(fontSize = 14.sp, color = c.textPrimary, textAlign = TextAlign.Center)
            )

            Text("/", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD4A017)))

            // Month input
            OutlinedTextField(
                value = lunarMonth,
                onValueChange = { if (it.length <= 2) onLunarMonthChange(it.filter { ch -> ch.isDigit() }) },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Tháng", style = TextStyle(fontSize = 14.sp, color = c.outline, textAlign = TextAlign.Center))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onConvert() }),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFE082),
                    unfocusedBorderColor = Color(0xFFFFE082),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = Color(0xFFD4A017)
                ),
                textStyle = TextStyle(fontSize = 14.sp, color = c.textPrimary, textAlign = TextAlign.Center)
            )

            // Convert button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFD4A017), CircleShape)
                    .clip(CircleShape)
                    .clickable(onClick = onConvert),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.ArrowForward,
                    contentDescription = "Chuyển đổi",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Result
        AnimatedVisibility(visible = result != null) {
            result?.let { conv ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HorizontalDivider(color = Color(0xFFFFE082).copy(alpha = 0.5f), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        buildAnnotatedString {
                            append("${conv.lunarDay}/${conv.lunarMonth} Âm → ")
                            withStyle(SpanStyle(color = Color(0xFFB71C1C), fontWeight = FontWeight.Bold)) {
                                append(String.format("%02d/%02d/%d", conv.solarDay, conv.solarMonth, conv.solarYear))
                            }
                            append(" Dương lịch")
                        },
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1B1F))
                    )
                    Text(
                        conv.dayOfWeek,
                        style = TextStyle(fontSize = 12.sp, color = Color(0xFF534340))
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// LUNAR CONVERSION RESULT (inline in results)
// ══════════════════════════════════════════

@Composable
private fun LunarConversionResultCard(conv: LunarConversion) {
    val c = LichSoThemeColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFFDE7))),
                RoundedCornerShape(20.dp)
            )
            .border(1.dp, Color(0xFFFFE082), RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Filled.SwapHoriz, null, tint = Color(0xFFD4A017), modifier = Modifier.size(18.dp))
            Text(
                "Chuyển đổi Âm ↔ Dương",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD4A017))
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            buildAnnotatedString {
                append("${conv.lunarDay}/${conv.lunarMonth} Âm → ")
                withStyle(SpanStyle(color = c.primary, fontWeight = FontWeight.Bold)) {
                    append(String.format("%02d/%02d/%d", conv.solarDay, conv.solarMonth, conv.solarYear))
                }
                append(" Dương lịch")
            },
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.textPrimary)
        )
        Text(
            conv.dayOfWeek,
            style = TextStyle(fontSize = 12.sp, color = c.textTertiary)
        )
    }
}

// ══════════════════════════════════════════
// RECENT SEARCH ITEM
// ══════════════════════════════════════════

@Composable
private fun RecentSearchItem(
    text: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val c = LichSoThemeColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Filled.History,
            contentDescription = null,
            tint = c.outline,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text,
            style = TextStyle(fontSize = 14.sp, color = c.textTertiary),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Xoá",
                tint = c.outlineVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ══════════════════════════════════════════
// SECTION LABEL
// ══════════════════════════════════════════

@Composable
private fun SectionLabel(text: String) {
    val c = LichSoThemeColors.current
    Text(
        text = text.uppercase(),
        style = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = c.primary,
            letterSpacing = 0.5.sp
        ),
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

// ══════════════════════════════════════════
// EMPTY STATE
// ══════════════════════════════════════════

@Composable
private fun EmptySearchState(query: String) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.SearchOff,
            contentDescription = null,
            tint = c.outlineVariant,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Không tìm thấy kết quả",
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = c.textSecondary
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Thử tìm \"Giỗ Tổ\", \"10/3\", \"ngày tốt\"...",
            style = TextStyle(fontSize = 13.sp, color = c.textTertiary),
            textAlign = TextAlign.Center
        )
    }
}
