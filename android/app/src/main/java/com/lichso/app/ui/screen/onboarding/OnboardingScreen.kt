package com.lichso.app.ui.screen.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import com.lichso.app.ui.screen.profile.ProfileKeys
import com.lichso.app.ui.screen.settings.settingsDataStore
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

// ══════════════════════════════════════════
// Colors
// ══════════════════════════════════════════
private val PrimaryRed = Color(0xFFB71C1C)
private val DeepRed = Color(0xFF8B0000)
private val GoldAccent = Color(0xFFD4A017)
private val GoldLight = Color(0xFFE8C84A)
private val SurfaceBg = Color(0xFFFFFBF5)
private val TextMain = Color(0xFF1C1B1F)
private val TextSub = Color(0xFF534340)
private val TextDim = Color(0xFF857371)
private val Outline = Color(0xFFD8C2BF)

// ══════════════════════════════════════════
// Onboarding page data
// ══════════════════════════════════════════
private data class OnboardingPage(
    val icon: ImageVector,
    val iconBg: List<Color>,
    val accentColor: Color,
    val title: String,
    val subtitle: String,
    val description: String,
    val featureChips: List<Pair<ImageVector, String>>
)

private val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Filled.CalendarMonth,
        iconBg = listOf(PrimaryRed, Color(0xFFD32F2F)),
        accentColor = PrimaryRed,
        title = "Lịch Vạn Niên",
        subtitle = "Âm lịch · Dương lịch · Can Chi",
        description = "Xem lịch âm dương chính xác, ngày giờ hoàng đạo, tiết khí và thông tin can chi đầy đủ cho mỗi ngày.",
        featureChips = listOf(
            Icons.Filled.Today to "Lịch âm dương",
            Icons.Filled.DarkMode to "Tuần trăng",
            Icons.Filled.Schedule to "Giờ hoàng đạo",
            Icons.Filled.AutoAwesome to "Can chi ngày"
        )
    ),
    OnboardingPage(
        icon = Icons.Filled.EventAvailable,
        iconBg = listOf(Color(0xFF2E7D32), Color(0xFF43A047)),
        accentColor = Color(0xFF2E7D32),
        title = "Ngày Tốt · Ngày Xấu",
        subtitle = "Chọn ngày theo phong thủy",
        description = "Tra cứu ngày tốt xấu cho mọi việc: cưới hỏi, khai trương, xây nhà, xuất hành... theo lịch vạn niên truyền thống.",
        featureChips = listOf(
            Icons.Filled.Favorite to "Cưới hỏi",
            Icons.Filled.Store to "Khai trương",
            Icons.Filled.Home to "Xây nhà",
            Icons.Filled.FlightTakeoff to "Xuất hành"
        )
    ),
    OnboardingPage(
        icon = Icons.AutoMirrored.Filled.MenuBook,
        iconBg = listOf(GoldAccent, GoldLight),
        accentColor = GoldAccent,
        title = "Văn Khấn · Cúng Lễ",
        subtitle = "Trọn bộ văn khấn truyền thống",
        description = "Hơn 100 bài văn khấn cho mọi dịp: cúng gia tiên, khai trương, động thổ, cầu an, giải hạn...",
        featureChips = listOf(
            Icons.Filled.TempleHindu to "Cúng gia tiên",
            Icons.Filled.Celebration to "Ngày lễ tết",
            Icons.Filled.VolunteerActivism to "Cầu an",
            Icons.Filled.Bookmark to "Lưu & chia sẻ"
        )
    ),
    OnboardingPage(
        icon = Icons.Filled.AccountTree,
        iconBg = listOf(Color(0xFF6A1B9A), Color(0xFFAB47BC)),
        accentColor = Color(0xFF7B1FA2),
        title = "Gia Phả Gia Đình",
        subtitle = "Lưu giữ cội nguồn · Kết nối thế hệ",
        description = "Xây dựng cây gia phả trực quan, lưu giữ thông tin từng thành viên, ngày giỗ và kỷ niệm quan trọng của dòng họ.",
        featureChips = listOf(
            Icons.Filled.FamilyRestroom to "Cây gia phả",
            Icons.Filled.Person to "Hồ sơ thành viên",
            Icons.Filled.Cake to "Ngày giỗ · Sinh nhật",
            Icons.Filled.Share to "Chia sẻ gia phả"
        )
    ),
    OnboardingPage(
        icon = Icons.Filled.AutoAwesome,
        iconBg = listOf(Color(0xFF1565C0), Color(0xFF42A5F5)),
        accentColor = Color(0xFF1565C0),
        title = "Trợ Lý AI Thông Minh",
        subtitle = "Hỏi đáp phong thủy bằng AI",
        description = "Hỏi bất cứ điều gì về phong thủy, ngày giờ tốt, văn khấn... AI sẽ trả lời chính xác và nhanh chóng.",
        featureChips = listOf(
            Icons.AutoMirrored.Filled.Chat to "Hỏi đáp AI",
            Icons.Filled.HistoryEdu to "Ngày này năm xưa",
            Icons.Filled.Notifications to "Nhắc nhở thông minh",
            Icons.Filled.TipsAndUpdates to "Gợi ý thông minh"
        )
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val permissionsPageIndex = onboardingPages.size       // page 5
    val profilePageIndex = onboardingPages.size + 1       // page 6
    val totalPages = onboardingPages.size + 2             // +1 permissions +1 profile
    val pagerState = rememberPagerState(pageCount = { totalPages })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == totalPages - 1
    val isProfilePage = pagerState.currentPage == profilePageIndex
    val isPermissionsPage = pagerState.currentPage == permissionsPageIndex
    val context = LocalContext.current

    // ── Profile form state ──
    var inputName by remember { mutableStateOf("") }
    var inputBirthDay by remember { mutableStateOf("") }
    var inputBirthMonth by remember { mutableStateOf("") }
    var inputBirthYear by remember { mutableStateOf("") }
    var inputBirthHour by remember { mutableStateOf("") }
    var inputBirthMinute by remember { mutableStateOf("") }
    var inputGender by remember { mutableStateOf("Nam") }
    var nameError by remember { mutableStateOf(false) }
    var yearError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
            .imePadding()
    ) {
        // ── Pager ──
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            if (pageIndex < onboardingPages.size) {
                val page = onboardingPages[pageIndex]
                val pageOffset = (
                    (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                ).absoluteValue
                OnboardingPageContent(page = page, pageOffset = pageOffset)
            } else if (pageIndex == permissionsPageIndex) {
                // ── Permissions Page ──
                PermissionsSetupPage(
                    onSkip = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(profilePageIndex)
                        }
                    },
                    onNext = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(profilePageIndex)
                        }
                    },
                    totalPages = totalPages,
                    currentPage = permissionsPageIndex
                )
            } else {
                // ── Profile Setup Page ──
                ProfileSetupPage(
                    inputName = inputName,
                    onNameChange = { inputName = it; nameError = false },
                    inputBirthDay = inputBirthDay,
                    onBirthDayChange = { inputBirthDay = it },
                    inputBirthMonth = inputBirthMonth,
                    onBirthMonthChange = { inputBirthMonth = it },
                    inputBirthYear = inputBirthYear,
                    onBirthYearChange = { inputBirthYear = it; yearError = false },
                    inputBirthHour = inputBirthHour,
                    onBirthHourChange = { inputBirthHour = it },
                    inputBirthMinute = inputBirthMinute,
                    onBirthMinuteChange = { inputBirthMinute = it },
                    inputGender = inputGender,
                    onGenderChange = { inputGender = it },
                    nameError = nameError,
                    yearError = yearError,
                    onSkip = { onFinish() },
                    onFinish = {
                        val nameTrimmed = inputName.trim()
                        val yearInt = inputBirthYear.toIntOrNull() ?: 0
                        if (nameTrimmed.isNotBlank() && yearInt > 0) {
                            if (yearInt < 1900 || yearInt > 2100) {
                                yearError = true
                                return@ProfileSetupPage
                            }
                        }
                        coroutineScope.launch {
                            context.settingsDataStore.edit { prefs ->
                                if (nameTrimmed.isNotBlank()) {
                                    prefs[ProfileKeys.DISPLAY_NAME] = nameTrimmed
                                }
                                if (yearInt > 0) {
                                    prefs[ProfileKeys.BIRTH_YEAR] = yearInt
                                }
                                prefs[ProfileKeys.BIRTH_DAY] = inputBirthDay.toIntOrNull() ?: 0
                                prefs[ProfileKeys.BIRTH_MONTH] = inputBirthMonth.toIntOrNull() ?: 0
                                prefs[ProfileKeys.BIRTH_HOUR] = inputBirthHour.toIntOrNull() ?: -1
                                prefs[ProfileKeys.BIRTH_MINUTE] = inputBirthMinute.toIntOrNull() ?: -1
                                prefs[ProfileKeys.GENDER] = inputGender
                            }
                            onFinish()
                        }
                    },
                    totalPages = totalPages,
                    currentPage = profilePageIndex
                )
            }
        }

        // ── Bottom controls (hidden on profile & permissions pages — buttons are inline there) ──
        if (!isProfilePage && !isPermissionsPage) Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalPages) { index ->
                    val isActive = pagerState.currentPage == index
                    val dotWidth by animateDpAsState(
                        targetValue = if (isActive) 28.dp else 8.dp,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
                        label = "dotWidth"
                    )
                    val accentColor = when {
                        index < onboardingPages.size -> onboardingPages[index].accentColor
                        index == permissionsPageIndex -> Color(0xFF1565C0)
                        else -> PrimaryRed
                    }
                    val dotColor by animateColorAsState(
                        targetValue = if (isActive) accentColor else Outline,
                        animationSpec = tween(300),
                        label = "dotColor"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(dotWidth)
                            .clip(RoundedCornerShape(4.dp))
                            .background(dotColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip button
                if (!isProfilePage && !isPermissionsPage) {
                    Text(
                        text = "Bỏ qua",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDim
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                // Skip to permissions page
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(permissionsPageIndex)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                } else if (isPermissionsPage) {
                    // On permissions page — allow skip to profile
                    Text(
                        text = "Để sau",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDim
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(profilePageIndex)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                } else {
                    // Skip profile — go straight to main app
                    Text(
                        text = "Bỏ qua",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDim
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onFinish() }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }

                // Next / Bắt đầu button
                val buttonColor = when {
                    isProfilePage -> PrimaryRed
                    isPermissionsPage -> Color(0xFF1565C0)
                    else -> onboardingPages[pagerState.currentPage.coerceAtMost(onboardingPages.lastIndex)].accentColor
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            if (isProfilePage) {
                                Brush.linearGradient(listOf(PrimaryRed, DeepRed))
                            } else if (isPermissionsPage) {
                                Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF1976D2)))
                            } else {
                                Brush.linearGradient(listOf(buttonColor, buttonColor.copy(alpha = 0.85f)))
                            }
                        )
                        .clickable {
                            if (isProfilePage) {
                                // Validate if user entered something
                                val nameTrimmed = inputName.trim()
                                val yearInt = inputBirthYear.toIntOrNull() ?: 0
                                if (nameTrimmed.isNotBlank() && yearInt > 0) {
                                    // Has data — validate year range
                                    if (yearInt < 1900 || yearInt > 2100) {
                                        yearError = true
                                        return@clickable
                                    }
                                }
                                // Save profile to DataStore (even partial data)
                                coroutineScope.launch {
                                    context.settingsDataStore.edit { prefs ->
                                        if (nameTrimmed.isNotBlank()) {
                                            prefs[ProfileKeys.DISPLAY_NAME] = nameTrimmed
                                        }
                                        if (yearInt > 0) {
                                            prefs[ProfileKeys.BIRTH_YEAR] = yearInt
                                        }
                                        prefs[ProfileKeys.BIRTH_DAY] = inputBirthDay.toIntOrNull() ?: 0
                                        prefs[ProfileKeys.BIRTH_MONTH] = inputBirthMonth.toIntOrNull() ?: 0
                                        prefs[ProfileKeys.BIRTH_HOUR] = inputBirthHour.toIntOrNull() ?: -1
                                        prefs[ProfileKeys.BIRTH_MINUTE] = inputBirthMinute.toIntOrNull() ?: -1
                                        prefs[ProfileKeys.GENDER] = inputGender
                                    }
                                    onFinish()
                                }
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        }
                        .padding(
                            horizontal = if (isProfilePage) 32.dp else 24.dp,
                            vertical = 14.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when {
                                isProfilePage -> "Bắt đầu sử dụng"
                                isPermissionsPage -> "Tiếp tục"
                                else -> "Tiếp theo"
                            },
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Icon(
                            if (isProfilePage) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

// ══════════════════════════════════════════
// SINGLE PAGE CONTENT
// ══════════════════════════════════════════
@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    pageOffset: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 32.dp)
            .padding(top = 60.dp, bottom = 160.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // ── Big Icon with animated background ──
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .graphicsLayer {
                    // Parallax: icon moves slightly
                    translationX = pageOffset * -80f
                    alpha = 1f - (pageOffset * 0.5f).coerceAtMost(1f)
                    scaleX = 1f - (pageOffset * 0.15f)
                    scaleY = 1f - (pageOffset * 0.15f)
                }
        ) {
            // Decorative outer ring
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .alpha(0.12f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(page.accentColor, Color.Transparent)
                        ),
                        CircleShape
                    )
            )

            // Icon circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(page.iconBg)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    page.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // ── Title ──
        Text(
            text = page.title,
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.graphicsLayer {
                translationX = pageOffset * -40f
                alpha = 1f - (pageOffset * 0.6f).coerceAtMost(1f)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Subtitle ──
        Text(
            text = page.subtitle,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = page.accentColor,
                textAlign = TextAlign.Center,
                letterSpacing = 0.3.sp
            ),
            modifier = Modifier.graphicsLayer {
                translationX = pageOffset * -30f
                alpha = 1f - (pageOffset * 0.7f).coerceAtMost(1f)
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // ── Gold decorative line ──
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, GoldAccent, Color.Transparent)
                    ),
                    RoundedCornerShape(1.dp)
                )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Description ──
        Text(
            text = page.description,
            style = TextStyle(
                fontSize = 15.sp,
                color = TextSub,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            ),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .graphicsLayer {
                    translationX = pageOffset * -20f
                    alpha = 1f - (pageOffset * 0.8f).coerceAtMost(1f)
                }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ── Feature Chips ──
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.graphicsLayer {
                translationY = pageOffset * 40f
                alpha = 1f - (pageOffset * 0.9f).coerceAtMost(1f)
            }
        ) {
            // Row 1
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                page.featureChips.take(2).forEach { (icon, label) ->
                    FeatureChip(icon = icon, label = label, accentColor = page.accentColor)
                }
            }
            // Row 2
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                page.featureChips.drop(2).forEach { (icon, label) ->
                    FeatureChip(icon = icon, label = label, accentColor = page.accentColor)
                }
            }
        }
    }
}

@Composable
private fun FeatureChip(
    icon: ImageVector,
    label: String,
    accentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .background(
                accentColor.copy(alpha = 0.08f),
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                accentColor.copy(alpha = 0.15f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            label,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = accentColor
            )
        )
    }
}

// ══════════════════════════════════════════
// PROFILE SETUP PAGE — Final onboarding step
// ══════════════════════════════════════════

@Composable
private fun ProfileSetupPage(
    inputName: String,
    onNameChange: (String) -> Unit,
    inputBirthDay: String,
    onBirthDayChange: (String) -> Unit,
    inputBirthMonth: String,
    onBirthMonthChange: (String) -> Unit,
    inputBirthYear: String,
    onBirthYearChange: (String) -> Unit,
    inputBirthHour: String,
    onBirthHourChange: (String) -> Unit,
    inputBirthMinute: String,
    onBirthMinuteChange: (String) -> Unit,
    inputGender: String,
    onGenderChange: (String) -> Unit,
    nameError: Boolean,
    yearError: Boolean,
    onSkip: () -> Unit,
    onFinish: () -> Unit,
    totalPages: Int,
    currentPage: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp)
            .padding(top = 48.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Icon ──
        Box(
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .alpha(0.12f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PrimaryRed, Color.Transparent)
                        ),
                        CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(PrimaryRed, Color(0xFFD32F2F)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Thông tin cá nhân",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            "Giúp AI phân tích phong thủy chính xác theo tuổi & mệnh",
            style = TextStyle(
                fontSize = 13.sp,
                color = TextSub,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Gold line
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, GoldAccent, Color.Transparent)
                    ),
                    RoundedCornerShape(1.dp)
                )
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ═══ FORM FIELDS ═══

        // ── Tên (required) ──
        OnboardingFormField(
            value = inputName,
            onValueChange = onNameChange,
            label = "Tên hiển thị *",
            placeholder = "Nhập tên của bạn",
            icon = Icons.Filled.Person,
            isError = nameError,
            errorText = if (nameError) "Vui lòng nhập tên" else null
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Năm sinh (required) + Giới tính ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OnboardingFormField(
                value = inputBirthYear,
                onValueChange = { if (it.length <= 4) onBirthYearChange(it) },
                label = "Năm sinh *",
                placeholder = "VD: 1995",
                icon = Icons.Filled.CalendarMonth,
                keyboardType = KeyboardType.Number,
                isError = yearError,
                errorText = if (yearError) "Năm không hợp lệ" else null,
                modifier = Modifier.weight(1f)
            )
            // Gender selector
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Giới tính",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSub
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Nam", "Nữ", "Khác").forEach { g ->
                        val isSelected = inputGender == g
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) PrimaryRed.copy(alpha = 0.12f)
                                    else Color.Transparent
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) PrimaryRed else Outline,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onGenderChange(g) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                g,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PrimaryRed else TextSub
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Optional: ngày, tháng sinh ──
        Text(
            "Thông tin thêm (không bắt buộc)",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextDim
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OnboardingFormField(
                value = inputBirthDay,
                onValueChange = { if (it.length <= 2) onBirthDayChange(it) },
                label = "Ngày sinh",
                placeholder = "DD",
                icon = null,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
            OnboardingFormField(
                value = inputBirthMonth,
                onValueChange = { if (it.length <= 2) onBirthMonthChange(it) },
                label = "Tháng sinh",
                placeholder = "MM",
                icon = null,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Optional: giờ sinh ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OnboardingFormField(
                value = inputBirthHour,
                onValueChange = { if (it.length <= 2) onBirthHourChange(it) },
                label = "Giờ sinh",
                placeholder = "HH",
                icon = Icons.Filled.Schedule,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
            OnboardingFormField(
                value = inputBirthMinute,
                onValueChange = { if (it.length <= 2) onBirthMinuteChange(it) },
                label = "Phút",
                placeholder = "MM",
                icon = null,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Privacy Notice ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF81C784).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = null,
                tint = Color(0xFF388E3C),
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    "Bảo mật tuyệt đối",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Chúng tôi không thu thập bất kỳ thông tin cá nhân nào. " +
                            "Tất cả dữ liệu chỉ được lưu trữ trên điện thoại của bạn, " +
                            "phục vụ cho AI tử vi & phong thủy tư vấn chính xác hơn.",
                    style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF33691E)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── Hint ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GoldAccent.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .border(1.dp, GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Filled.TipsAndUpdates,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(18.dp)
            )
            Text(
                "Bạn có thể bổ sung thêm thông tin sau trong mục Hồ sơ. " +
                        "Ngày giờ sinh giúp AI phân tích can chi, mệnh, cung... chính xác hơn.",
                style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = TextSub
                )
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Inline page indicator dots ──
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            repeat(totalPages) { index ->
                val isActive = currentPage == index
                val dotWidth = if (isActive) 28.dp else 8.dp
                val accentColor = when {
                    index < onboardingPages.size -> onboardingPages[index].accentColor
                    index == onboardingPages.size -> Color(0xFF1565C0)
                    else -> PrimaryRed
                }
                val dotColor = if (isActive) accentColor else Outline
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(dotWidth)
                        .clip(RoundedCornerShape(4.dp))
                        .background(dotColor)
                )
            }
        }

        // ── Inline action buttons ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bỏ qua",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDim
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSkip() }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(listOf(PrimaryRed, DeepRed))
                    )
                    .clickable { onFinish() }
                    .padding(horizontal = 32.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Bắt đầu sử dụng",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

// ══════════════════════════════════════════
// PERMISSIONS SETUP PAGE
// ══════════════════════════════════════════

@Composable
private fun PermissionsSetupPage(
    onSkip: () -> Unit,
    onNext: () -> Unit,
    totalPages: Int,
    currentPage: Int
) {
    val context = LocalContext.current

    // ── Notification permission state ──
    var notificationGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    // ── Photo library permission state ──
    var photoGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+: partial access via READ_MEDIA_VISUAL_USER_SELECTED or full via READ_MEDIA_IMAGES
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                // Android 12 and below
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        )
    }

    // Notification permission launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationGranted = isGranted
    }

    // Photo permission launcher (handles multiple permissions)
    val photoPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        photoGranted = permissions.values.any { it }
    }

    // Helper to recheck permissions
    fun recheckPermissions() {
        notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true

        photoGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    // Recheck when returning from settings
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                recheckPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val allGranted = notificationGranted && photoGranted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp)
            .padding(top = 48.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Icon ──
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .alpha(0.12f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF1565C0), Color.Transparent)
                        ),
                        CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF42A5F5)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (allGranted) Icons.Filled.VerifiedUser else Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Cấp quyền ứng dụng",
            style = TextStyle(
                fontFamily = FontFamily.Serif,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            "Để nhắc nhở, thông báo và quản lý hình ảnh gia phả hoạt động tốt, ứng dụng cần một số quyền truy cập",
            style = TextStyle(
                fontSize = 13.sp,
                color = TextSub,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Gold line
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, GoldAccent, Color.Transparent)
                    ),
                    RoundedCornerShape(1.dp)
                )
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ═══ PERMISSION ITEMS ═══

        // 1. Notification Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionItemCard(
                icon = Icons.Filled.Notifications,
                iconColor = Color(0xFFE65100),
                title = "Thông báo",
                description = "Nhận nhắc nhở tờ lịch hàng ngày, ngày lễ âm lịch, giờ hoàng đạo và nhắc nhở cá nhân đúng giờ.",
                isGranted = notificationGranted,
                onRequest = {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2. Photo Library Permission
        PermissionItemCard(
            icon = Icons.Filled.PhotoLibrary,
            iconColor = Color(0xFF7B1FA2),
            title = "Bộ sưu tập ảnh",
            description = "Cho phép thêm hình ảnh kỷ niệm vào gia phả, đặt ảnh đại diện thành viên và ảnh hồ sơ cá nhân.",
            isGranted = photoGranted,
            onRequest = {
                val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
                } else {
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                photoPermissionLauncher.launch(permissions)
            }
        )
        Spacer(modifier = Modifier.height(12.dp))

        Spacer(modifier = Modifier.height(8.dp))

        // ── Status summary ──
        if (allGranted) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF81C784).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF388E3C),
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        "Tất cả quyền đã được cấp!",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    )
                    Text(
                        "Nhắc nhở và thông báo sẽ hoạt động chính xác.",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color(0xFF33691E)
                        )
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFFFCC80).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        "Một số quyền chưa được cấp",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Bạn vẫn có thể tiếp tục, nhưng nhắc nhở có thể không hoạt động. " +
                                "Bạn có thể cấp quyền sau trong phần Cài đặt.",
                        style = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFFBF360C)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Why we need these ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GoldAccent.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .border(1.dp, GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Filled.TipsAndUpdates,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(18.dp)
            )
            Text(
                "Các quyền này giúp ứng dụng nhắc nhở bạn về ngày giỗ, lễ tết, " +
                        "giờ hoàng đạo, nhắc nhở cá nhân và cho phép thêm ảnh kỷ niệm vào gia phả. " +
                        "Dữ liệu của bạn luôn được lưu trữ trên thiết bị.",
                style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = TextSub
                )
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Inline page indicator dots ──
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            repeat(totalPages) { index ->
                val isActive = currentPage == index
                val dotWidth = if (isActive) 28.dp else 8.dp
                val accentColor = when {
                    index < onboardingPages.size -> onboardingPages[index].accentColor
                    index == onboardingPages.size -> Color(0xFF1565C0)
                    else -> PrimaryRed
                }
                val dotColor = if (isActive) accentColor else Outline
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(dotWidth)
                        .clip(RoundedCornerShape(4.dp))
                        .background(dotColor)
                )
            }
        }

        // ── Inline action buttons ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Để sau",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDim
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSkip() }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF1976D2)))
                    )
                    .clickable { onNext() }
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tiếp tục",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

// ══════════════════════════════════════════
// PERMISSION ITEM CARD
// ══════════════════════════════════════════

@Composable
private fun PermissionItemCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isGranted) Color(0xFFF1F8E9) else Color.White,
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                if (isGranted) Color(0xFFA5D6A7) else Outline,
                RoundedCornerShape(16.dp)
            )
            .clickable(enabled = !isGranted) { onRequest() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (isGranted) Color(0xFFC8E6C9)
                    else iconColor.copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isGranted) Icons.Filled.Check else icon,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF388E3C) else iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        // Text
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    title,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGranted) Color(0xFF2E7D32) else TextMain
                    )
                )
                if (isGranted) {
                    Text(
                        "✓ Đã cấp",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF388E3C)
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                description,
                style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = if (isGranted) Color(0xFF33691E) else TextSub
                )
            )
            if (!isGranted) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.1f))
                        .border(1.dp, iconColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .clickable { onRequest() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Cấp quyền",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = iconColor
                        )
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════
// REUSABLE FORM FIELD for Onboarding
// ══════════════════════════════════════════

@Composable
private fun OnboardingFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorText: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            label,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isError) Color(0xFFB71C1C) else TextSub
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    style = TextStyle(fontSize = 14.sp, color = TextDim)
                )
            },
            leadingIcon = icon?.let {
                {
                    Icon(
                        it,
                        contentDescription = null,
                        tint = if (isError) Color(0xFFB71C1C) else TextDim,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            isError = isError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryRed,
                unfocusedBorderColor = Outline,
                errorBorderColor = Color(0xFFB71C1C),
                cursorColor = PrimaryRed,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                errorContainerColor = Color(0xFFFFF0F0)
            ),
            shape = RoundedCornerShape(14.dp),
            textStyle = TextStyle(fontSize = 14.sp, color = TextMain),
            modifier = Modifier.fillMaxWidth()
        )
        if (errorText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                errorText,
                style = TextStyle(
                    fontSize = 11.sp,
                    color = Color(0xFFB71C1C)
                )
            )
        }
    }
}
