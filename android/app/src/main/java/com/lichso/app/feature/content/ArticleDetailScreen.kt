package com.lichso.app.feature.content

import com.lichso.app.ui.theme.screenBackground
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.data.remote.Article
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.theme.LichSoThemeColors

@Composable
fun ArticleDetailScreen(
    articleId: String,
    onBackClick: () -> Unit = {},
    onArticleClick: (String) -> Unit = {},
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
            .screenBackground(c.bg)
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
        } else if (!uiState.error.isNullOrBlank()) {
            ErrorView(message = uiState.error.orEmpty(), onRetry = { viewModel.loadArticle(articleId) })
        } else if (uiState.article != null) {
            ArticleContent(
                article = uiState.article!!,
                relatedArticles = uiState.relatedArticles,
                onArticleClick = onArticleClick,
            )
        } else {
            ErrorView(
                message = "Không có dữ liệu bài viết. Vui lòng thử lại.",
                onRetry = { viewModel.loadArticle(articleId) }
            )
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
        Icon(
            imageVector = Icons.Filled.SignalWifiOff,
            contentDescription = null,
            tint = c.textSecondary.copy(alpha = 0.5f),
            modifier = Modifier
                .size(56.dp)
                .padding(bottom = 4.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = TextStyle(
                fontSize = 15.sp,
                color = c.textSecondary,
                fontWeight = FontWeight.Medium,
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp),
        )
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = c.red)
        ) {
            Text("Thử lại", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ArticleContent(
    article: Article,
    relatedArticles: List<Article>,
    onArticleClick: (String) -> Unit,
) {
    val c = LichSoThemeColors.current

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

    val fullHtml = remember(article, normalizedFeaturedImage, relatedArticles, c.isDark) {
        val rawContent = article.content ?: article.excerpt ?: "Không có nội dung."
        val fixedContent = rawContent
            .replace("src=\"/uploads/", "src=\"https://lichso.vn/api/uploads/")
            .replace("src=\"/api/uploads/", "src=\"https://lichso.vn/api/uploads/")

        val textColor = if (c.isDark) "#F0E8D0" else "#1C1B1F"
        val linkColor = if (c.isDark) "#EF5350" else "#B71C1C"
        val subColor = if (c.isDark) "#B8AA88" else "#857371"
        val dividerColor = if (c.isDark) "#5A4F42" else "#D8C2BF"
        val categoryBg = if (c.isDark) "rgba(239,83,80,0.22)" else "rgba(183,28,28,0.12)"
        val categoryText = if (c.isDark) "#FF8A80" else "#B71C1C"
        val cardBg = if (c.isDark) "#2A1A1A" else "#FFFFFF"
        val cardBorder = if (c.isDark) "#4B3936" else "#E6D8D4"

        val coverHtml = if (!normalizedFeaturedImage.isNullOrBlank()) {
            "<img class=\"cover\" src=\"$normalizedFeaturedImage\" alt=\"cover\" />"
        } else ""
        val categoryHtml = article.category?.name?.takeIf { it.isNotBlank() }?.let {
            "<span class=\"chip\">$it</span>"
        } ?: ""
        val readingHtml = article.readingTime?.let { "<span class=\"meta-item\">• $it phút đọc</span>" } ?: ""
        val dateHtml = article.publishedAt?.takeIf { it.isNotBlank() }?.let { "<div class=\"date\">Đăng ngày: ${it.take(10)}</div>" } ?: ""
        val relatedHtml = buildRelatedArticlesHtml(relatedArticles)

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
            padding: 16px;
        }
        a { color: $linkColor; text-decoration: none; }
        img { max-width: 100%; height: auto; border-radius: 8px; margin: 12px 0; }
        .cover { width: 100%; max-height: 260px; object-fit: cover; border-radius: 12px; margin: 0 0 16px 0; }
        .meta { display: flex; gap: 8px; align-items: center; margin-bottom: 10px; flex-wrap: wrap; }
        .chip { display: inline-block; background: $categoryBg; color: $categoryText; padding: 4px 10px; border-radius: 6px; font-size: 11px; font-weight: 700; }
        .meta-item { color: $subColor; font-size: 12px; }
        h1 { font-size: 24px; line-height: 1.35; margin: 0 0 10px 0; color: $textColor; }
        .date { color: $subColor; font-size: 12px; margin-bottom: 14px; }
        hr { border: 0; border-top: 1px solid $dividerColor; margin: 18px 0; }
        p { margin-bottom: 16px; }
        .related { margin-top: 28px; padding-top: 18px; border-top: 1px solid $dividerColor; }
        .related-title { font-size: 18px; font-weight: 800; margin: 0 0 12px 0; color: $textColor; }
        .related-card {
            display: block;
            background: $cardBg;
            border: 1px solid $cardBorder;
            border-radius: 14px;
            padding: 12px;
            margin: 10px 0;
            color: $textColor;
            text-decoration: none;
        }
        .related-card-title { font-size: 15px; font-weight: 700; line-height: 1.4; margin-bottom: 6px; }
        .related-card-meta { color: $subColor; font-size: 12px; }
        </style>
        </head>
        <body>
        $coverHtml
        <div class="meta">$categoryHtml$readingHtml</div>
        <h1>${article.title}</h1>
        $dateHtml
        <hr />
        $fixedContent
        $relatedHtml
        </body>
        </html>
        """.trimIndent()
    }

    ArticleHtmlView(
        html = fullHtml,
        onArticleClick = onArticleClick,
        modifier = Modifier
            .fillMaxSize(),
    )
}

@Composable
private fun ArticleHtmlView(
    html: String,
    onArticleClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { context ->
            android.webkit.WebView(context).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                settings.javaScriptEnabled = false
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.domStorageEnabled = false
                settings.javaScriptCanOpenWindowsAutomatically = false
                settings.loadsImagesAutomatically = true
                settings.builtInZoomControls = false
                settings.displayZoomControls = false
                settings.setSupportZoom(false)
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
                settings.allowFileAccessFromFileURLs = false
                settings.allowUniversalAccessFromFileURLs = false
                webViewClient = object : android.webkit.WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: android.webkit.WebView?,
                        request: android.webkit.WebResourceRequest?,
                    ): Boolean {
                        val url = request?.url?.toString().orEmpty()
                        val prefix = "lichso://article/"
                        return if (url.startsWith(prefix)) {
                            onArticleClick(url.removePrefix(prefix))
                            true
                        } else {
                            if (url.startsWith("http://") || url.startsWith("https://")) {
                                runCatching {
                                    context.startActivity(
                                        android.content.Intent(
                                            android.content.Intent.ACTION_VIEW,
                                            android.net.Uri.parse(url),
                                        )
                                    )
                                }
                            }
                            true
                        }
                    }
                }
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://lichso.vn", html, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}

private fun buildRelatedArticlesHtml(articles: List<Article>): String {
    if (articles.isEmpty()) return ""
    val cards = articles.joinToString(separator = "\n") { article ->
        val category = article.category?.name?.takeIf { it.isNotBlank() } ?: "Bài viết"
        val date = article.publishedAt?.takeIf { it.isNotBlank() }?.take(10)
        val meta = listOfNotNull(category, date).joinToString(" • ")
        """
        <a class="related-card" href="lichso://article/${article.id}">
            <div class="related-card-title">${escapeHtml(article.title)}</div>
            <div class="related-card-meta">${escapeHtml(meta)}</div>
        </a>
        """.trimIndent()
    }
    return """
    <section class="related">
        <h2 class="related-title">Bài viết liên quan</h2>
        $cards
    </section>
    """.trimIndent()
}

private fun escapeHtml(value: String): String =
    value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
