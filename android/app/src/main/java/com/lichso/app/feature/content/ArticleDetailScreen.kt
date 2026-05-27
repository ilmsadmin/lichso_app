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
import androidx.compose.runtime.remember
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

    val normalizedFeaturedImage = remember(article.featuredImage) {
        val img = article.featuredImage
        if (img.isNullOrBlank()) null
        else if (img.startsWith("http://") || img.startsWith("https://")) img
        else {
            val cleaned = if (img.startsWith("/")) img.substring(1) else img
            if (cleaned.startsWith("api/uploads/")) "https://lichso.vn/$cleaned"
            else if (cleaned.startsWith("uploads/")) "https://lichso.vn/api/$cleaned"
            else "https://lichso.vn/$cleaned"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Featured Image
        if (!normalizedFeaturedImage.isNullOrBlank()) {
            AsyncImage(
                model = normalizedFeaturedImage,
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
        val normalizedFeaturedImage = remember(article.featuredImage) {
            val img = article.featuredImage
            if (img.isNullOrBlank()) null
            else if (img.startsWith("http://") || img.startsWith("https://")) img
            else {
                val cleaned = if (img.startsWith("/")) img.substring(1) else img
                if (cleaned.startsWith("api/uploads/")) "https://lichso.vn/$cleaned"
                else if (cleaned.startsWith("uploads/")) "https://lichso.vn/api/$cleaned"
                else "https://lichso.vn/$cleaned"
            }
        }

        val fullHtml = remember(article.content, article.excerpt, normalizedFeaturedImage, c.isDark) {
            val rawContent = article.content ?: article.excerpt ?: "Không có nội dung."
            val fixedContent = rawContent
                .replace("src=\"/uploads/", "src=\"https://lichso.vn/api/uploads/")
                .replace("src=\"/api/uploads/", "src=\"https://lichso.vn/api/uploads/")
            
            val textColor = if (c.isDark) "#F0E8D0" else "#1C1B1F"
            val linkColor = if (c.isDark) "#EF5350" else "#B71C1C"

            """
            <!DOCTYPE html>
            <html>
            <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
            body {
                font-family: -apple-system, BlinkMacSystemFont, sans-serif;
                font-size: 16px;
                line-height: 1.7;
                color: $textColor;
                background-color: transparent;
                margin: 0;
                padding: 0;
            }
            a {
                color: $linkColor;
                text-decoration: none;
            }
            img {
                max-width: 100%;
                height: auto;
                border-radius: 8px;
                margin: 12px 0;
            }
            hr {
                border: 0;
                border-top: 1px solid ${if (c.isDark) "#5A4F42" else "#D8C2BF"};
                margin: 20px 0;
            }
            p {
                margin-bottom: 16px;
            }
            </style>
            </head>
            <body>
            $fixedContent
            </body>
            </html>
            """.trimIndent()
        }

        ArticleHtmlView(html = fullHtml, modifier = Modifier.fillMaxWidth().weight(1f))
    }
}

@Composable
private fun ArticleHtmlView(html: String, modifier: Modifier = Modifier) {
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { context ->
            android.webkit.WebView(context).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                settings.javaScriptEnabled = false
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://lichso.vn", html, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}
