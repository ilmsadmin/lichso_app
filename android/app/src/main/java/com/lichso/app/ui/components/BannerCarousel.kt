package com.lichso.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lichso.app.data.remote.Banner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ══════════════════════════════════════════
// BANNER CAROUSEL
// ══════════════════════════════════════════

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun BannerCarousel(
    banners: List<Banner>,
    onBannerAction: (String) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { banners.size })
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll mỗi 4 giây
    LaunchedEffect(pagerState.pageCount) {
        if (pagerState.pageCount <= 1) return@LaunchedEffect
        while (true) {
            delay(4000)
            val next = (pagerState.currentPage + 1) % pagerState.pageCount
            pagerState.animateScrollToPage(next)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 12.dp,
        ) { page ->
            BannerCard(
                banner = banners[page],
                onCtaClick = { route -> if (route != null) onBannerAction(route) }
            )
        }

        if (banners.size > 1) {
            Spacer(modifier = Modifier.height(7.dp))

            // Pagination dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(banners.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 16.dp else 5.dp,
                        animationSpec = tween(300),
                        label = "dotWidth"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .height(5.dp)
                            .width(width)
                            .background(
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// BANNER CARD
// ══════════════════════════════════════════

fun normalizeServerMediaUrl(rawUrl: String?): String? {
    val value = rawUrl?.trim().orEmpty()
    if (value.isBlank()) return null
    if (value.startsWith("http://") || value.startsWith("https://")) return value

    val cleaned = value.removePrefix("/")
    return when {
        cleaned.startsWith("api/uploads/") -> "https://lichso.vn/$cleaned"
        cleaned.startsWith("uploads/") -> "https://lichso.vn/api/$cleaned"
        else -> "https://lichso.vn/$cleaned"
    }
}

fun bannerPresetIcon(
    iconKey: String?
): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconKey) {
        "calendar" -> Icons.Filled.CalendarMonth
        "article" -> Icons.Filled.MenuBook
        "quiz" -> Icons.Filled.Psychology
        "ai" -> Icons.Filled.AutoAwesome
        "gift" -> Icons.Filled.Redeem
        "star" -> Icons.Filled.Star
        "sun" -> Icons.Filled.WbSunny
        "moon" -> Icons.Filled.NightsStay
        "clock" -> Icons.Filled.Schedule
        "compass" -> Icons.Filled.Explore
        "scroll" -> Icons.Filled.Article
        "bell" -> Icons.Filled.Notifications
        "trophy" -> Icons.Filled.EmojiEvents
        "users" -> Icons.Filled.Groups
        "heart" -> Icons.Filled.Favorite
        "home" -> Icons.Filled.Home
        "shop" -> Icons.Filled.ShoppingBag
        "wallet" -> Icons.Filled.AccountBalanceWallet
        "map" -> Icons.Filled.Place
        "shield" -> Icons.Filled.VerifiedUser
        else -> Icons.Filled.Today
    }
}

@Composable
fun BannerCard(
    banner: Banner,
    onCtaClick: (String?) -> Unit,
) {
    val parsedColor = remember(banner.bgColor) {
        val colorStr = banner.bgColor
        if (colorStr.isNullOrBlank()) null
        else {
            val formatted = if (colorStr.startsWith("#")) colorStr else "#$colorStr"
            try {
                Color(android.graphics.Color.parseColor(formatted))
            } catch (_: Exception) {
                null
            }
        }
    }

    // Màu gradient theo type, hoặc dùng bgColor nếu có
    val (gradStart, gradEnd) = if (parsedColor != null) {
        Pair(parsedColor, parsedColor)
    } else {
        Pair(Color(0xFF1565C0), Color(0xFF0D47A1)) // Default color
    }

    val normalizedImageUrl = remember(banner.imageUrl) { normalizeServerMediaUrl(banner.imageUrl) }
    val normalizedIconUrl = remember(banner.iconUrl) { normalizeServerMediaUrl(banner.iconUrl) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(gradStart, gradEnd),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .clickable { onCtaClick(banner.ctaRoute) }
    ) {
        // Ảnh nền từ server (nếu có)
        if (!normalizedImageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(normalizedImageUrl).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                alpha = 0.9f,
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.48f),
                                Color.Black.copy(alpha = 0.28f),
                                Color.Black.copy(alpha = 0.12f),
                            )
                        )
                    )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon nhỏ bên trái
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        Color.White.copy(alpha = 0.15f),
                        RoundedCornerShape(10.dp)
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                val icon = bannerPresetIcon(banner.iconKey)
                if (!normalizedIconUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(normalizedIconUrl).crossfade(true).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(28.dp),
                    )
                } else {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title + subtitle (1 dòng mỗi cái)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = banner.title,
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!banner.subtitle.isNullOrEmpty()) {
                    Text(
                        text = banner.subtitle,
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f),
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // CTA pill nhỏ gọn
            if (!banner.ctaText.isNullOrEmpty()) {
                Row(
                    modifier = Modifier
                        .background(Color(0xFFF5E6C8), RoundedCornerShape(20.dp))
                        .clickable { onCtaClick(banner.ctaRoute) }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        banner.ctaText,
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF8B2500),
                        )
                    )
                    Icon(
                        Icons.Filled.ChevronRight, null,
                        tint = Color(0xFF8B2500),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
