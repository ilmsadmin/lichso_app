package com.lichso.app.feature.content

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.lichso.app.data.remote.Article
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.theme.LichSoThemeColors

@Composable
fun ArticleDetailScreen(
    articleId: String,
    onBackClick: () -> Unit = {},
    viewModel: ArticleDetailViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(articleId) {
        viewModel.loadArticle(articleId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        AppTopBar(
            title = "Chi tiết bài viết",
            onBackClick = onBackClick,
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = c.red)
            }
        } else if (uiState.error != null) {
            ErrorView(message = uiState.error!!, onRetry = { viewModel.loadArticle(articleId) })
        } else if (uiState.article != null) {
            ArticleContent(article = uiState.article!!)
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = TextStyle(fontSize = 14.sp, color = c.textSecondary),
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = c.red)
        ) {
            Text("Thử lại", color = Color.White)
        }
    }
}

@Composable
private fun ArticleContent(article: Article) {
    val c = LichSoThemeColors.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Featured Image
        if (!article.featuredImage.isNullOrBlank()) {
            AsyncImage(
                model = article.featuredImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        // Category & Reading Time Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (article.category != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(c.red.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        article.category.name,
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = c.red,
                        ),
                    )
                }
            }

            if (article.readingTime != null) {
                Text(
                    text = "•  ${article.readingTime} phút đọc",
                    style = TextStyle(fontSize = 12.sp, color = c.textTertiary),
                )
            }
        }

        // Title
        Text(
            text = article.title,
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = c.textPrimary,
                lineHeight = 28.sp,
            ),
        )

        // Published Date
        if (!article.publishedAt.isNullOrBlank()) {
            Text(
                text = "Đăng ngày: ${article.publishedAt.take(10)}",
                style = TextStyle(fontSize = 12.sp, color = c.textTertiary),
            )
        }

        HorizontalDivider(thickness = 0.5.dp, color = c.outlineVariant.copy(alpha = 0.5f))

        // Content body
        Text(
            text = article.content ?: article.excerpt ?: "Không có nội dung.",
            style = TextStyle(
                fontSize = 15.sp,
                color = c.textSecondary,
                lineHeight = 24.sp,
            ),
        )
    }
}
