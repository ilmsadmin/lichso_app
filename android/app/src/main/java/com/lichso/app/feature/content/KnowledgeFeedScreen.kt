package com.lichso.app.feature.content

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.data.remote.*
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.theme.LichSoThemeColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun KnowledgeFeedScreen(
    onBackClick: () -> Unit = {},
    onAskAi: (String) -> Unit = {},
    viewModel: KnowledgeFeedViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        AppTopBar(
            title = "Khám Phá",
            subtitle = "Kiến thức hôm nay",
            onBackClick = onBackClick,
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Làm mới",
                        tint = Color.White,
                    )
                }
            }
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = c.red)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                uiState.quote?.let { quote ->
                    item {
                        QuoteCard(quote = quote, onAskAi = onAskAi)
                    }
                }

                if (uiState.events.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Ngày này năm xưa")
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(uiState.events) { event ->
                                EventCard(event = event)
                            }
                        }
                    }
                }

                if (uiState.famousPeople.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Nhân vật sinh nhật hôm nay")
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(uiState.famousPeople) { person ->
                                PersonCard(person = person)
                            }
                        }
                    }
                }

                if (uiState.articles.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Bài viết mới nhất")
                    }
                    items(uiState.articles) { article ->
                        ArticleCard(article = article)
                    }
                }

                if (uiState.events.isEmpty() && uiState.famousPeople.isEmpty() && uiState.articles.isEmpty() && uiState.quote == null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 64.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Không có dữ liệu hôm nay",
                                style = TextStyle(fontSize = 14.sp, color = c.textTertiary),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuoteCard(quote: Quote, onAskAi: (String) -> Unit) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF8B0000), Color(0xFFD4A017)),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
            .clickable { onAskAi("Giải thích sâu hơn về câu nói: \"${quote.quote}\"") }
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Filled.FormatQuote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    "Danh ngôn hôm nay",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.7f),
                    ),
                )
            }
            Text(
                quote.quote,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    lineHeight = 22.sp,
                ),
            )
            if (!quote.author.isNullOrBlank()) {
                Text(
                    "— ${quote.author}",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f),
                    ),
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(18.dp)
                .background(c.red, RoundedCornerShape(2.dp))
        )
        Text(
            title,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = c.textPrimary,
            ),
        )
    }
}

@Composable
private fun EventCard(event: ContentEvent) {
    val c = LichSoThemeColors.current
    Box(
        modifier = Modifier
            .width(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(c.surfaceContainer)
            .border(1.dp, c.outlineVariant, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (event.eventYear != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(c.red.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        "${event.eventYear}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = c.red,
                        ),
                    )
                }
            }
            Text(
                event.title,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                    lineHeight = 18.sp,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val desc = event.shortDescription ?: event.description
            if (!desc.isNullOrBlank()) {
                Text(
                    desc,
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = c.textSecondary,
                        lineHeight = 16.sp,
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PersonCard(person: FamousPerson) {
    val c = LichSoThemeColors.current
    val initial = person.name.firstOrNull()?.uppercase() ?: "?"
    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(c.surfaceContainer)
            .border(1.dp, c.outlineVariant, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1565C0), Color(0xFF7B1FA2)),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    initial,
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                )
            }
            Text(
                person.name,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                    lineHeight = 16.sp,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (person.birthYear != null) {
                Text(
                    "Sinh ${person.birthYear}",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = c.textTertiary,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ArticleCard(article: Article) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(c.surfaceContainer)
            .border(1.dp, c.outlineVariant, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(c.bg3),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Article,
                contentDescription = null,
                tint = c.textTertiary,
                modifier = Modifier.size(28.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                article.title,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                    lineHeight = 18.sp,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!article.category?.name.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(c.gold.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        article.category!!.name,
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = c.gold,
                        ),
                    )
                }
            }
            if (!article.publishedAt.isNullOrBlank()) {
                Text(
                    article.publishedAt.take(10),
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = c.textTertiary,
                    ),
                )
            }
        }
    }
}
