package com.lichso.app.ui.screen.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lichso.app.R
import com.lichso.app.data.remote.Banner
import com.lichso.app.data.remote.WeatherState
import com.lichso.app.domain.HistoricalEventProvider
import com.lichso.app.domain.model.*
import com.lichso.app.ui.components.HeaderIconButton
import com.lichso.app.ui.components.LoginNudgeBanner
import com.lichso.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

// ── Banner mặc định hiển thị khi server chưa trả về dữ liệu ──
private val DEFAULT_BANNERS = listOf(
    Banner(
        id = "local_1",
        title = "Xem ngày tốt hôm nay",
        subtitle = "Gợi ý việc nên làm và giờ đẹp trong ngày",
        imageUrl = null,
        iconUrl = null,
        iconKey = "sun",
        ctaText = "Xem ngay",
        ctaType = "route",
        ctaRoute = "gooddays",
        bgColor = null,
        type = "feature",
    ),
    Banner(
        id = "local_2",
        title = "Khám phá kiến thức lịch sử",
        subtitle = "Bài viết hay về văn hóa và truyền thống Việt Nam",
        imageUrl = null,
        iconUrl = null,
        iconKey = "article",
        ctaText = "Đọc ngay",
        ctaType = "route",
        ctaRoute = "knowledge_feed",
        bgColor = null,
        type = "content",
    ),
    Banner(
        id = "local_3",
        title = "Đố vui lịch sử",
        subtitle = "Thử thách kiến thức với hàng trăm câu hỏi thú vị",
        imageUrl = null,
        iconUrl = null,
        iconKey = "quiz",
        ctaText = "Chơi ngay",
        ctaType = "route",
        ctaRoute = "quiz_home",
        bgColor = null,
        type = "quiz",
    ),
    Banner(
        id = "local_4",
        title = "Tử vi & Phong thuỷ AI",
        subtitle = "Giải đáp mọi thắc mắc về phong thuỷ, vận mệnh",
        imageUrl = null,
        iconUrl = null,
        iconKey = "ai",
        ctaText = "Hỏi AI",
        ctaType = "route",
        ctaRoute = "chat",
        bgColor = null,
        type = "ai",
    ),
)

@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onCountdownClick: () -> Unit = {},
    onFortuneCardShown: () -> Unit = {},
    onBannerAction: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val c = LichSoThemeColors.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    // ── Weather detail sheet ──
    var showWeatherSheet by remember { mutableStateOf(false) }

    // ── Page flip state ──
    val flipProgress = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var isAnimatingFlip by remember { mutableStateOf(false) }
    val flipCommitThreshold = 0.18f

    // Banners: ưu tiên server, fallback local
    val banners = remember(uiState.banners) {
        if (uiState.banners.isNotEmpty()) uiState.banners else DEFAULT_BANNERS
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (c.isDark) c.bg else Color(0xFFFAF5F0)) // cream background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            uiState.dayInfo?.let { info ->
                LaunchedEffect(uiState.selectedDate) {
                    if (uiState.selectedDate == LocalDate.now()) {
                        onFortuneCardShown()
                    }
                }

                // ═══ RED TOP SECTION (TopBar + Banner) ═══
                RedTopSection(
                    info = info,
                    weatherState = uiState.weatherState,
                    tempUnit = uiState.tempUnit,
                    banners = banners,
                    notificationUnreadCount = uiState.notificationUnreadCount,
                    avatarPath = uiState.avatarPath,
                    onMenuClick = onMenuClick,
                    onProfileClick = onProfileClick,
                    onNotificationClick = onNotificationClick,
                    onWeatherRefresh = { viewModel.refreshWeather() },
                    onWeatherClick = { showWeatherSheet = true },
                    onBannerAction = onBannerAction,
                )

                // ═══ MINI WEEK STRIP (white card, cream bg) ═══
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (c.isDark) c.bg else Color(0xFFFAF5F0))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = if (c.isDark) Color(0xFF2A1A1A) else Color.White,
                        shadowElevation = 4.dp,
                        tonalElevation = 0.dp,
                    ) {
                        MiniCalendarStrip(
                            selectedDate = uiState.selectedDate,
                            calendarDays = uiState.calendarDays,
                            weekStartSunday = uiState.weekStartSunday,
                            onDayClick = { day ->
                                viewModel.selectDay(day.solarDay, day.solarMonth, day.solarYear)
                            }
                        )
                    }
                }

                // ═══ PAGE FLIP CALENDAR AREA ═══
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clipToBounds()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragStart = {
                                    if (!isAnimatingFlip) isDragging = true
                                },
                                onDragEnd = {
                                    if (!isDragging) return@detectVerticalDragGestures
                                    isDragging = false
                                    val current = flipProgress.value

                                    if (kotlin.math.abs(current) >= flipCommitThreshold) {
                                        val goNext = current > 0
                                        isAnimatingFlip = true
                                        coroutineScope.launch {
                                            flipProgress.animateTo(
                                                if (goNext) 1f else -1f,
                                                tween(180, easing = FastOutSlowInEasing)
                                            )
                                            if (goNext) viewModel.nextDay() else viewModel.prevDay()
                                            flipProgress.snapTo(if (goNext) -0.25f else 0.25f)
                                            flipProgress.animateTo(0f, tween(200, easing = FastOutSlowInEasing))
                                            isAnimatingFlip = false
                                        }
                                    } else {
                                        isAnimatingFlip = true
                                        coroutineScope.launch {
                                            flipProgress.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 600f))
                                            isAnimatingFlip = false
                                        }
                                    }
                                },
                                onDragCancel = {
                                    isDragging = false
                                    coroutineScope.launch {
                                        flipProgress.animateTo(0f, spring(stiffness = 600f))
                                    }
                                },
                                onVerticalDrag = { change, dragAmount ->
                                    if (isDragging && !isAnimatingFlip) {
                                        change.consume()
                                        val delta = -dragAmount / 450f
                                        val newVal = (flipProgress.value + delta).coerceIn(-1f, 1f)
                                        coroutineScope.launch {
                                            flipProgress.snapTo(newVal)
                                        }
                                    }
                                }
                            )
                        }
                ) {
                    val progress = flipProgress.value
                    val absProgress = kotlin.math.abs(progress)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                cameraDistance = 16f * density
                                rotationX = progress * -80f
                                transformOrigin = if (progress >= 0)
                                    TransformOrigin(0.5f, 0f)
                                else
                                    TransformOrigin(0.5f, 1f)
                                alpha = (1f - absProgress * 0.9f).coerceIn(0f, 1f)
                            }
                    ) {
                        // ═══ NỘI DUNG CHÍNH ═══
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // ═══ BIG DATE DISPLAY ═══
                            BigDateSection(info = info)

                            Spacer(modifier = Modifier.height(6.dp))

                            // ═══ SWIPE HINT ═══
                            SwipeHint()

                            Spacer(modifier = Modifier.height(6.dp))

                            // ═══ QUOTE SECTION ═══
                            if (uiState.showQuote) {
                                QuoteSection(selectedDate = uiState.selectedDate)
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // ═══ SMART HINT BANNER ═══
                            com.lichso.app.notification.SmartReminderProvider
                                .contextualHintFor(info)?.let { hint ->
                                SmartHintBanner(text = hint)
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // ═══ EVENT CHIPS ═══
                            EventChips(
                                info = info,
                                showFestival = uiState.showFestival,
                                onHistoryClick = onHistoryClick
                            )

                            if (uiState.countdownEvents.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HomeCountdownCard(
                                    items = uiState.countdownEvents,
                                    onClick = onCountdownClick
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        } // end Column

        LoginNudgeBanner(
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    } // end Box

    // ═══ WEATHER DETAIL BOTTOM SHEET ═══
    if (showWeatherSheet) {
        val ws = uiState.weatherState
        if (ws is WeatherState.Success) {
            WeatherDetailSheet(
                weather = ws.weather,
                tempUnit = uiState.tempUnit,
                onDismiss = { showWeatherSheet = false },
                onRefresh = { viewModel.refreshWeather() },
                onCityChange = { cityName -> viewModel.changeCity(cityName) }
            )
        } else {
            showWeatherSheet = false
        }
    }
}

// ══════════════════════════════════════════
// RED TOP SECTION: TopBar + BannerCarousel
// ══════════════════════════════════════════

@Composable
private fun RedTopSection(
    info: DayInfo,
    weatherState: WeatherState,
    tempUnit: String,
    banners: List<Banner>,
    notificationUnreadCount: Int,
    avatarPath: String,
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onWeatherRefresh: () -> Unit,
    onWeatherClick: () -> Unit,
    onBannerAction: (String) -> Unit,
) {
    val c = LichSoThemeColors.current
    val redGradient = if (c.isDark) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF5D1212), Color(0xFF7F1D1D), Color(0xFF4A1010)),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFFC62828), Color(0xFFB71C1C), Color(0xFF8B0000)),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(redGradient)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 12.dp)
        ) {
            // ── Row 1: TopBar ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hamburger menu
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .clip(CircleShape)
                        .clickable { onMenuClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Weather chip
                WeatherChip(
                    weatherState = weatherState,
                    tempUnit = tempUnit,
                    onRefresh = onWeatherRefresh,
                    onClick = onWeatherClick
                )

                // Right: Bell + Avatar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Notification bell
                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                .clip(CircleShape)
                                .clickable { onNotificationClick() }
                        )
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "Thông báo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        if (notificationUnreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFFFF5252), CircleShape)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp)
                            )
                        }
                    }

                    // Avatar
                    ProfileAvatarButton(avatarPath = avatarPath, onClick = onProfileClick)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Banner Carousel ──
            BannerCarousel(
                banners = banners,
                onBannerAction = onBannerAction,
            )
        }
    }
}

// ══════════════════════════════════════════
// BANNER CAROUSEL
// ══════════════════════════════════════════

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun BannerCarousel(
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

private fun normalizeServerMediaUrl(rawUrl: String?): String? {
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

private fun bannerPresetIcon(
    iconKey: String?,
    bannerType: String?
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
        else -> when (bannerType) {
            "content" -> Icons.Filled.MenuBook
            "quiz" -> Icons.Filled.Psychology
            "ai" -> Icons.Filled.AutoAwesome
            "promo" -> Icons.Filled.LocalOffer
            else -> Icons.Filled.Today
        }
    }
}

@Composable
private fun BannerCard(
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
        when (banner.type) {
            "content" -> Pair(Color(0xFF1565C0), Color(0xFF0D47A1))
            "quiz"    -> Pair(Color(0xFF2E7D32), Color(0xFF1B5E20))
            "ai"      -> Pair(Color(0xFF4A148C), Color(0xFF311B92))
            else      -> Pair(Color(0xFFBF360C), Color(0xFF8D1A00))
        }
    }

    val normalizedImageUrl = remember(banner.imageUrl) { normalizeServerMediaUrl(banner.imageUrl) }
    val normalizedIconUrl = remember(banner.iconUrl) { normalizeServerMediaUrl(banner.iconUrl) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)                                   // ← bằng ½ cũ
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
                val icon = bannerPresetIcon(banner.iconKey, banner.type)
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
                )
                if (!banner.subtitle.isNullOrEmpty()) {
                    Text(
                        text = banner.subtitle,
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f),
                        ),
                        maxLines = 1,
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

// ══════════════════════════════════════════
// WEATHER CHIP
// ══════════════════════════════════════════

@Composable
private fun ProfileAvatarButton(
    avatarPath: String,
    onClick: () -> Unit,
) {
    val hasAvatar = avatarPath.isNotEmpty() && File(avatarPath).exists()
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                .clip(CircleShape)
                .clickable(onClick = onClick)
        )
        if (hasAvatar) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(File(avatarPath))
                    .memoryCacheKey(avatarPath)
                    .crossfade(true)
                    .build(),
                contentDescription = "Cá nhân",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
            )
        } else {
            Icon(
                Icons.Outlined.Person,
                contentDescription = "Cá nhân",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun WeatherChip(
    weatherState: WeatherState,
    tempUnit: String = "°C",
    onRefresh: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                when (weatherState) {
                    is WeatherState.Success -> onClick()
                    is WeatherState.Error -> onRefresh()
                    else -> {}
                }
            }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        when (weatherState) {
            is WeatherState.Loading -> {
                Icon(Icons.Filled.WbSunny, null, tint = Color(0xFFFFD54F).copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                Text("...", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f)))
            }
            is WeatherState.Success -> {
                val weather = weatherState.weather
                val displayTemp = if (tempUnit == "°F") (weather.temperature * 9 / 5 + 32).toInt() else weather.temperature.toInt()
                Text(weather.icon, style = TextStyle(fontSize = 16.sp))
                Text("${displayTemp}${tempUnit}", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                Text(weather.cityName, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White))
            }
            is WeatherState.Error -> {
                Icon(Icons.Filled.CloudOff, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                Text("Thử lại", style = TextStyle(fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f)))
            }
        }
    }
}

// ══════════════════════════════════════════
// MINI CALENDAR WEEK STRIP
// ══════════════════════════════════════════

@Composable
private fun MiniCalendarStrip(
    selectedDate: LocalDate,
    calendarDays: List<CalendarDay>,
    weekStartSunday: Boolean = false,
    onDayClick: (CalendarDay) -> Unit
) {
    val c = LichSoThemeColors.current
    val dayOfWeek = selectedDate.dayOfWeek.value
    val weekStart = if (weekStartSunday) {
        val offset = dayOfWeek % 7
        selectedDate.minusDays(offset.toLong())
    } else {
        selectedDate.minusDays((dayOfWeek - 1).toLong())
    }

    val weekDayLabels = if (weekStartSunday)
        listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
    else
        listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

    val goldColor   = if (c.isDark) Color(0xFFCCA84A) else Color(0xFFB8860B)
    val primaryRed  = if (c.isDark) Color(0xFFEF9A9A) else Color(0xFF9B1B1B)

    Column(modifier = Modifier.fillMaxWidth()) {

        // ── Header: ❖ 2026 • Tháng 5 ❖ ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("❖", style = TextStyle(fontSize = 10.sp, color = goldColor))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${selectedDate.year}",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryRed,
                )
            )
            Text(
                text = "  •  Tháng ${selectedDate.monthValue}",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryRed,
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("❖", style = TextStyle(fontSize = 10.sp, color = goldColor))
        }

        // ── Thin gold divider ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, goldColor.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )

        // ── Day columns ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
                .padding(top = 6.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            (0..6).forEach { i ->
                val date = weekStart.plusDays(i.toLong())
                val isToday = date == LocalDate.now()
                val isWeekend = if (weekStartSunday) i == 0 || i == 6 else i >= 5

                val lunarText = calendarDays.find {
                    it.solarDay == date.dayOfMonth &&
                    it.solarMonth == date.monthValue &&
                    it.solarYear == date.year
                }?.lunarDisplayText ?: ""

                val labelColor = when {
                    isToday   -> Color.White.copy(alpha = 0.85f)
                    isWeekend -> if (c.isDark) Color(0xFFEF9A9A) else Color(0xFFC62828)
                    else      -> c.textTertiary
                }
                val dateColor = when {
                    isToday   -> Color.White
                    isWeekend -> if (c.isDark) Color(0xFFEF9A9A) else Color(0xFFC62828)
                    else      -> c.textPrimary
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            if (isToday)
                                if (c.isDark) Color(0xFF7F1D1D) else Color(0xFF8B1A1A)
                            else Color.Transparent
                        )
                        .clickable {
                            calendarDays.find {
                                it.solarDay == date.dayOfMonth &&
                                it.solarMonth == date.monthValue &&
                                it.solarYear == date.year
                            }?.let { onDayClick(it) }
                        }
                        .padding(vertical = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Day-of-week label
                    Text(
                        weekDayLabels[i],
                        style = TextStyle(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = labelColor
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    // Solar date number
                    Text(
                        "${date.dayOfMonth}",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = dateColor
                        )
                    )
                    // Lunar date (small)
                    Text(
                        lunarText,
                        style = TextStyle(
                            fontSize = 7.sp,
                            color = if (isToday) Color.White.copy(alpha = 0.7f) else c.textSecondary
                        )
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// SWIPE HINT
// ══════════════════════════════════════════

@Composable
private fun SwipeHint() {
    val c = LichSoThemeColors.current
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(4000)
        visible = false
    }
    AnimatedVisibility(visible = visible, exit = fadeOut(tween(600))) {
        val infiniteTransition = rememberInfiniteTransition(label = "swipeHint")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.2f, targetValue = 0.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "hintAlpha"
        )
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 4f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "hintOffset"
        )
        Row(
            modifier = Modifier.graphicsLayer { this.alpha = alpha; translationY = offsetY },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Filled.SwipeVertical, null, tint = c.textTertiary, modifier = Modifier.size(16.dp))
            Text("Vuốt để lật lịch", style = TextStyle(fontSize = 10.sp, color = c.textTertiary))
        }
    }
}

// ══════════════════════════════════════════
// BIG DATE DISPLAY
// ══════════════════════════════════════════

@Composable
private fun BigDateSection(info: DayInfo) {
    val c = LichSoThemeColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Big solar date number
        Text(
            text = "%02d".format(info.solar.dd),
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 118.sp,
                color = if (c.isDark) Color(0xFFEF9A9A) else Color(0xFF9B1B1B),
                lineHeight = 118.sp,
                letterSpacing = (-2).sp
            )
        )

        // Gold decorative line
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(3.dp)
                .background(
                    Brush.horizontalGradient(listOf(Color.Transparent, c.gold, Color.Transparent)),
                    RoundedCornerShape(2.dp)
                )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Weekday label
        Text(
            text = info.dayOfWeek.uppercase(),
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = c.textPrimary,
                letterSpacing = 2.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Lunar date info card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = if (c.isDark) Color(0xFF2A1A1A) else Color.White,
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Moon icon
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F))),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_moon),
                        contentDescription = "Âm lịch",
                        tint = Color(0xFF1A237E),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Lunar date text
                Column(modifier = Modifier.weight(1f)) {
                    val lunarDayLabel = if (info.lunar.day <= 10) "Mùng ${info.lunar.day}" else "${info.lunar.day}"
                    Text(
                        "$lunarDayLabel tháng ${info.lunar.month}",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
                    )
                    Text(
                        "Năm ${info.yearCanChi}",
                        style = TextStyle(fontSize = 11.sp, color = c.textTertiary)
                    )
                }

                // Day can chi
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        "Ngày ${info.dayCanChi}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (c.isDark) Color(0xFFEF9A9A) else Color(0xFFC62828)
                        )
                    )
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = if (c.isDark) Color(0xFFEF9A9A) else Color(0xFFC62828),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day quality indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val isGood = !info.activities.isXauDay
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (isGood) c.goodGreen else c.badRed, CircleShape)
            )
            Text(
                text = if (isGood) "Ngày Hoàng Đạo ✦" else "Ngày Hắc Đạo",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isGood) c.goodGreen else c.badRed
                )
            )
        }
    }
}

// ══════════════════════════════════════════
// QUOTE SECTION
// ══════════════════════════════════════════

@Composable
private fun QuoteSection(selectedDate: LocalDate) {
    val c = LichSoThemeColors.current
    val (quoteText, quoteAuthor) = remember(selectedDate) {
        val dayOfYear = selectedDate.dayOfYear
        com.lichso.app.data.VietnameseQuotes.ofDay(dayOfYear)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "“ $quoteText ”",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                color = if (c.isDark) c.textSecondary else c.textTertiary,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "— $quoteAuthor",
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (c.isDark) c.textTertiary else c.outline
            )
        )
    }
}

// ══════════════════════════════════════════
// EVENT CHIPS
// ══════════════════════════════════════════

@Composable
private fun EventChips(info: DayInfo, showFestival: Boolean = true, onHistoryClick: () -> Unit = {}) {
    val c = LichSoThemeColors.current
    val holiday = info.solarHoliday ?: info.lunarHoliday
    val hasHistoryEvents = remember(info.solar.dd, info.solar.mm) {
        HistoricalEventProvider.getEvents(info.solar.dd, info.solar.mm).isNotEmpty()
    }
    val showHoliday = showFestival && holiday != null
    if (!showHoliday && !hasHistoryEvents) return

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        if (showHoliday) {
            EventChip(
                icon = Icons.Filled.Celebration,
                text = holiday.orEmpty(),
                bgColor = if (c.isDark) Color(0xFF3D2A10) else Color(0xFFFFF3E0),
                textColor = if (c.isDark) Color(0xFFE8A06A) else Color(0xFFE65100),
                borderColor = if (c.isDark) Color(0xFF5C3D1A) else Color(0xFFFFB74D)
            )
            if (hasHistoryEvents) Spacer(modifier = Modifier.width(8.dp))
        }
        if (hasHistoryEvents) {
            EventChip(
                icon = Icons.Filled.HistoryEdu,
                text = "Ngày này năm xưa",
                bgColor = if (c.isDark) Color(0xFF1A2E1A) else Color(0xFFE8F5E9),
                textColor = if (c.isDark) Color(0xFF81C784) else Color(0xFF2E7D32),
                borderColor = if (c.isDark) Color(0xFF2E4A2E) else Color(0xFF81C784),
                onClick = onHistoryClick
            )
        }
    }
}

@Composable
private fun EventChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    bgColor: Color,
    textColor: Color,
    borderColor: Color,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = textColor, modifier = Modifier.size(14.dp))
        Text(text, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = textColor))
    }
}

// ══════════════════════════════════════════
// SMART HINT BANNER
// ══════════════════════════════════════════

@Composable
private fun SmartHintBanner(text: String) {
    val c = LichSoThemeColors.current
    val bg = if (c.isDark) Color(0xFF2A2010) else Color(0xFFFFF8E1)
    val border = if (c.isDark) Color(0xFF5C4319) else Color(0xFFFFD54F)
    val tint = if (c.isDark) Color(0xFFFFCA28) else Color(0xFF8B5E00)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Filled.Lightbulb, null, tint = tint, modifier = Modifier.size(16.dp))
        Text(text, color = tint, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium))
    }
}

// ══════════════════════════════════════════
// HOME COUNTDOWN CARD
// ══════════════════════════════════════════

@Composable
private fun HomeCountdownCard(
    items: List<HomeCountdownItem>,
    onClick: () -> Unit,
) {
    val c = LichSoThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (c.isDark) Color(0xFF1B2A1B) else Color(0xFFE8F5E9))
            .border(1.dp, if (c.isDark) Color(0xFF2E4A2E) else Color(0xFF81C784), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Sắp tới",
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    color = if (c.isDark) Color(0xFF81C784) else Color(0xFF2E7D32))
            )
            Text(
                if (items.size > 2) "Xem tất cả (${items.size})" else "Xem tất cả",
                style = TextStyle(fontSize = 11.sp, color = c.textTertiary)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        items.take(2).forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    item.title,
                    style = TextStyle(fontSize = 12.sp, color = c.textPrimary),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                val txt = when {
                    item.daysLeft > 0 -> "${item.daysLeft} ngày"
                    item.daysLeft == 0L -> "Hôm nay"
                    else -> "Đã qua"
                }
                Text(
                    txt,
                    maxLines = 1,
                    style = TextStyle(
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = if (item.daysLeft > 0) Color(0xFF2E7D32) else Color(0xFFE65100)
                    )
                )
            }
        }
    }
}
