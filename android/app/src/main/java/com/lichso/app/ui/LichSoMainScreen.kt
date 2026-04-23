package com.lichso.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lichso.app.ui.screen.bookmarks.BookmarksScreen
import com.lichso.app.ui.screen.calendar.CalendarScreen
import com.lichso.app.ui.screen.chat.AIChatScreen
import com.lichso.app.ui.screen.familytree.FamilyTreeScreen
import com.lichso.app.ui.screen.gooddays.GoodDaysScreen
import com.lichso.app.ui.screen.history.ThisDayInHistoryScreen
import com.lichso.app.ui.screen.home.HomeScreen
import com.lichso.app.ui.screen.home.HomeViewModel
import com.lichso.app.ui.screen.notifications.NotificationScreen
import com.lichso.app.ui.screen.prayers.PrayersScreen
import com.lichso.app.ui.screen.profile.ProfileScreen
import com.lichso.app.ui.screen.search.SearchScreen
import com.lichso.app.ui.screen.settings.SettingsScreen
import com.lichso.app.ui.screen.tasks.TasksScreen3
import com.lichso.app.R
import com.lichso.app.analytics.Analytics
import com.lichso.app.ui.theme.*
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.lichso.app.util.ReviewHelper
import com.lichso.app.util.SmartRatingManager
import com.lichso.app.ui.components.SmartRatingDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LichSoMainScreen(modifier: Modifier = Modifier, initialRoute: String = "home") {
    val c = LichSoThemeColors.current
    var currentRoute by remember { mutableStateOf(initialRoute) }
    var prayerDetailShowing by remember { mutableStateOf(false) }
    var taskEditShowing by remember { mutableStateOf(false) }
    var initialPrayerId by remember { mutableStateOf<Int?>(null) }
    var initialAiMessage by remember { mutableStateOf<String?>(null) }
    val homeViewModel: HomeViewModel = hiltViewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // ── In-App Review: chỉ tăng open count để phục vụ logic SmartRating ──
    // KHÔNG tự gọi ReviewHelper.tryShowReview(activity) mỗi lần mở app, vì mỗi
    // lần launch In-App Review đều "đốt" quota của Google Play (quota rất hạn
    // chế: ước tính 1–2 lần/user/năm). Khi quota hết, các lần user chủ động
    // bấm 5 sao sẽ không hiển thị dialog → không có review nào thực sự được
    // gửi đi. Việc xin review chỉ thực hiện qua SmartRatingDialog (happy path)
    // hoặc manual từ Settings.
    LaunchedEffect(Unit) {
        ReviewHelper.incrementAppOpenCount(context)
    }

    // ── Analytics: log screen_view mỗi khi route đổi ──
    LaunchedEffect(currentRoute) {
        Analytics.logScreen(currentRoute, screenClass = "LichSoMainScreen")
    }

    // ── Smart Rating Dialog ──
    val showRatingDialog by SmartRatingManager.shouldShow.collectAsState()
    SmartRatingDialog(
        visible = showRatingDialog,
        onDismiss = { SmartRatingManager.dismiss() }
    )

    val hideBottomBar = currentRoute in listOf("chat", "familytree", "settings", "history", "notifications", "search", "bookmarks", "gooddays") || prayerDetailShowing || taskEditShowing

    val toggleDrawer: () -> Unit = {
        scope.launch {
            if (drawerState.isOpen) drawerState.close() else drawerState.open()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            DrawerMenuContent(
                currentRoute = currentRoute,
                onItemClick = { route ->
                    scope.launch { drawerState.close() }
                    currentRoute = route
                }
            )
        }
    ) {
        val navBarInsets = WindowInsets.navigationBars.asPaddingValues()
        val navBarBottom = navBarInsets.calculateBottomPadding()
        // Bottom bar height (72dp bar + 8dp raised offset) + actual navigation bar inset
        val bottomBarTotalHeight = 80.dp + navBarBottom

        Box(modifier = modifier.fillMaxSize().background(c.bg)) {
        // Content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (hideBottomBar) Modifier.navigationBarsPadding()
                    else Modifier.padding(bottom = bottomBarTotalHeight)
                )
        ) {
            when (currentRoute) {
                "home" -> HomeScreen(
                    onSettingsClick = { currentRoute = "settings" },
                    onMenuClick = toggleDrawer,
                    onProfileClick = { currentRoute = "profile" },
                    onHistoryClick = { currentRoute = "history" },
                    onNotificationClick = { currentRoute = "notifications" }
                )
                "calendar" -> CalendarScreen(
                    onGoodDaysClick = { currentRoute = "gooddays" },
                    onSearchClick = { currentRoute = "search" },
                    onMenuClick = toggleDrawer,
                    onEditVisibilityChanged = { taskEditShowing = it },
                    onAskAiClick = { day, month, year ->
                        initialAiMessage = "Phân tích chi tiết ngày $day/$month/$year"
                        currentRoute = "chat"
                    }
                )
                "gooddays" -> GoodDaysScreen(
                    onBackClick = { currentRoute = "home" }
                )
                "prayers" -> PrayersScreen(
                    onBackClick = { currentRoute = "home" },
                    onMenuClick = toggleDrawer,
                    onDetailVisibilityChanged = { prayerDetailShowing = it },
                    initialPrayerId = initialPrayerId.also { initialPrayerId = null }
                )
                "profile" -> ProfileScreen(
                    onSettingsClick = { currentRoute = "settings" },
                    onFamilyTreeClick = { currentRoute = "familytree" },
                    onBackClick = { currentRoute = "home" },
                    onMenuClick = toggleDrawer,
                    onTasksClick = { currentRoute = "tasks" },
                    onBookmarksClick = { currentRoute = "bookmarks" }
                )
                "tasks" -> TasksScreen3(
                    onBackClick = { currentRoute = "home" },
                    onMenuClick = toggleDrawer,
                    onEditVisibilityChanged = { taskEditShowing = it }
                )
                "notifications" -> NotificationScreen(
                    onBackClick = { currentRoute = "home" }
                )
                "familytree" -> FamilyTreeScreen(
                    onBackClick = { currentRoute = "profile" },
                    onPrayersClick = { prayerId ->
                        initialPrayerId = prayerId
                        currentRoute = "prayers"
                    }
                )
                "history" -> ThisDayInHistoryScreen(onBackClick = { currentRoute = "home" })
                "chat" -> AIChatScreen(
                    onBackClick = { currentRoute = "home" },
                    onNavigateToProfile = { currentRoute = "profile" },
                    initialMessage = initialAiMessage.also { initialAiMessage = null }
                )
                "settings" -> SettingsScreen(onBackClick = { currentRoute = "home" })
                "search" -> SearchScreen(
                    onBackClick = { currentRoute = "calendar" },
                    onDateSelected = { year, month, day ->
                        homeViewModel.goToDate(year, month, day)
                        currentRoute = "calendar"
                    },
                    onGoodDaysClick = { currentRoute = "gooddays" }
                )
                "bookmarks" -> BookmarksScreen(
                    onBackClick = { currentRoute = "profile" },
                    onDateSelected = { year, month, day ->
                        homeViewModel.goToDate(year, month, day)
                        currentRoute = "calendar"
                    },
                    onAddBookmark = { currentRoute = "calendar" }
                )
                else -> HomeScreen(
                    onSettingsClick = { currentRoute = "settings" },
                    onMenuClick = toggleDrawer,
                    onHistoryClick = { currentRoute = "history" },
                    onNotificationClick = { currentRoute = "notifications" }
                )
            }
        }

        // AI FAB — only show when bottom bar is visible
        if (!hideBottomBar) {
            val fabBottom = bottomBarTotalHeight + 8.dp
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = fabBottom)
            ) {
                // Circle background + icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (c.isDark)
                                Brush.linearGradient(listOf(Color(0xFF7F1D1D), Color(0xFF5D1212)))
                            else
                                Brush.linearGradient(listOf(c.primary, Color(0xFFC62828))),
                            CircleShape
                        )
                        .clickable { currentRoute = "chat" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = "AI Chat",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                // AI badge (outside the clipped circle)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .background(c.gold, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "AI",
                        style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    )
                }
            }
        }

        // Bottom Navigation Bar (Material 3 style)
        if (!hideBottomBar) {
            BottomNavBar(
                currentRoute = currentRoute,
                onRouteSelected = { currentRoute = it },
                onCenterClick = {
                    homeViewModel.goToToday()
                    currentRoute = "home"
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
    } // end ModalNavigationDrawer
}

// ══════════════════════════════════════════
// DRAWER MENU CONTENT (matching HTML design)
// ══════════════════════════════════════════

private data class DrawerMenuItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val iconFilled: ImageVector = icon
)

@Composable
private fun DrawerMenuContent(
    currentRoute: String,
    onItemClick: (String) -> Unit
) {
    val c = LichSoThemeColors.current

    val mainItems = listOf(
        DrawerMenuItem("home", "Trang chủ", Icons.Outlined.Today, Icons.Filled.Today),
        DrawerMenuItem("bookmarks", "Ngày đã lưu", Icons.Outlined.Bookmarks, Icons.Filled.Bookmarks),
    )

    val exploreItems = listOf(
        DrawerMenuItem("history", "Ngày này năm xưa", Icons.Outlined.HistoryEdu, Icons.Filled.HistoryEdu),
        DrawerMenuItem("familytree", "Cây gia phả", Icons.Outlined.AccountTree, Icons.Filled.AccountTree),
    )

    val bottomItems = listOf(
        DrawerMenuItem("settings", "Cài đặt", Icons.Outlined.Settings, Icons.Filled.Settings),
    )

    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
        drawerContainerColor = c.bg
    ) {
        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            // ── Drawer Header (Red Gradient) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = if (c.isDark) listOf(Color(0xFF5D1212), Color(0xFF4A1010))
                                     else listOf(c.primary, c.deepRed),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // App logo
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "Lịch Số",
                        modifier = Modifier
                            .size(58.dp)
                            .graphicsLayer {
                                scaleX = 1.5f
                                scaleY = 1.5f
                            }
                    )
                    Column {
                        Text(
                            "Lịch Số",
                            style = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        // Line 2: tagline
                        Text(
                            "Lịch vạn niên số 1 Việt Nam",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }

            // ── Drawer Items ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Main navigation items
                mainItems.forEach { item ->
                    DrawerNavItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { onItemClick(item.route) }
                    )
                }

                // Divider
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = c.outlineVariant
                )

                // Section title "Khám phá"
                Text(
                    "KHÁM PHÁ",
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textTertiary,
                        letterSpacing = 1.sp
                    )
                )

                // Explore items
                exploreItems.forEach { item ->
                    DrawerNavItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { onItemClick(item.route) }
                    )
                }

                // Divider
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = c.outlineVariant
                )

                // Bottom items
                bottomItems.forEach { item ->
                    DrawerNavItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { onItemClick(item.route) }
                    )
                }

                // Divider
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = c.outlineVariant
                )

                // Section title "Thông tin"
                Text(
                    "THÔNG TIN",
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textTertiary,
                        letterSpacing = 1.sp
                    )
                )

                // Info items
                val context = LocalContext.current

                DrawerActionItem(
                    icon = Icons.Outlined.StarRate,
                    title = "Đánh giá ứng dụng",
                    c = c
                ) {
                    SmartRatingManager.triggerManually()
                }

                DrawerActionItem(
                    icon = Icons.Outlined.Share,
                    title = "Chia sẻ ứng dụng",
                    c = c
                ) {
                    val shareUrl = "https://play.google.com/store/apps/details?id=com.lichso.app"
                    val shareText = "Lịch Số - Lịch vạn niên số 1 Việt Nam 🇻🇳\n\n" +
                        "Xem ngày âm lịch, ngày tốt xấu, văn khấn, cây gia phả và nhiều tính năng hữu ích khác. " +
                        "Tải ngay:\n$shareUrl"
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, null))
                }

                DrawerActionItem(
                    icon = Icons.Outlined.Policy,
                    title = "Chính sách bảo mật",
                    c = c
                ) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://apps.zenix.vn/privacy-policy"))
                    context.startActivity(intent)
                }
            }

            // ── Footer: "Lịch Số v... · Phát triển bởi Zenix Labs" ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 0.dp),
                    color = c.outlineVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_zenix_logo),
                    contentDescription = "Zenix Labs",
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Lịch Số v${getVersionName(LocalContext.current)}",
                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = c.textTertiary)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        "Phát triển bởi",
                        style = TextStyle(fontSize = 10.sp, color = c.textTertiary.copy(alpha = 0.7f))
                    )
                    Text(
                        "Zenix Labs",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF42A5F5),
                                    Color(0xFF26A69A),
                                    Color(0xFF009688)
                                )
                            )
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp).navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun DrawerNavItem(
    item: DrawerMenuItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val c = LichSoThemeColors.current

    val backgroundColor = if (isSelected) c.primaryContainer else Color.Transparent
    val contentColor = if (isSelected) c.onPrimaryContainer else c.textPrimary
    val iconToUse = if (isSelected) item.iconFilled else item.icon

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            iconToUse,
            contentDescription = item.title,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            item.title,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = contentColor
            )
        )
    }
}

@Composable
private fun DrawerActionItem(
    icon: ImageVector,
    title: String,
    c: LichSoColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = c.textSecondary,
            modifier = Modifier.size(22.dp)
        )
        Text(
            title,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = c.textSecondary
            )
        )
    }
}

private fun getVersionName(context: android.content.Context): String {
    return try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionName ?: "1.0"
    } catch (e: Exception) {
        "1.0"
    }
}

// ══════════════════════════════════════════
// BOTTOM NAV BAR
// ══════════════════════════════════════════

@Composable
private fun BottomNavBar(
    currentRoute: String,
    onRouteSelected: (String) -> Unit,
    onCenterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LichSoThemeColors.current

    data class NavItem(
        val route: String,
        val title: String,
        val icon: ImageVector,
        val iconFilled: ImageVector
    )

    // Side items (2 left, 2 right — center is the raised home/daily button)
    val leftItems = listOf(
        NavItem("calendar", "Lịch tháng", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
        NavItem("tasks", "Ghi chú", Icons.Outlined.EditNote, Icons.Filled.EditNote),
    )
    val rightItems = listOf(
        NavItem("prayers", "Văn Khấn", Icons.AutoMirrored.Outlined.MenuBook, Icons.AutoMirrored.Filled.MenuBook),
        NavItem("profile", "Cá nhân", Icons.Outlined.Person, Icons.Filled.Person),
    )

    val calendarDate = java.time.LocalDate.now().dayOfMonth.toString()
    val isHomeSelected = currentRoute == "home"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Background bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(c.bg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left items
                leftItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val tint = if (isSelected) c.primary else c.outline
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onRouteSelected(item.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .width(56.dp)
                                            .height(30.dp)
                                            .background(c.primaryContainer, RoundedCornerShape(15.dp))
                                    )
                                }
                                Icon(
                                    if (isSelected) item.iconFilled else item.icon,
                                    contentDescription = item.title,
                                    tint = tint,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                item.title,
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = tint
                                )
                            )
                        }
                    }
                }

                // Center spacer for the raised button
                Spacer(modifier = Modifier.weight(1.2f))

                // Right items
                rightItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val tint = if (isSelected) c.primary else c.outline
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onRouteSelected(item.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .width(56.dp)
                                            .height(30.dp)
                                            .background(c.primaryContainer, RoundedCornerShape(15.dp))
                                    )
                                }
                                Icon(
                                    if (isSelected) item.iconFilled else item.icon,
                                    contentDescription = item.title,
                                    tint = tint,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                item.title,
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = tint
                                )
                            )
                        }
                    }
                }
            }
            // System navigation bar padding
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            )
        }

        // ═══ RAISED CENTER HOME/DAILY BUTTON ═══
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        if (isHomeSelected)
                            Brush.linearGradient(
                                if (c.isDark) listOf(Color(0xFF7F1D1D), Color(0xFF5D1212))
                                else listOf(c.primary, Color(0xFFC62828))
                            )
                        else
                            Brush.linearGradient(
                                if (c.isDark) listOf(Color(0xFF5D1212).copy(alpha = 0.85f), Color(0xFF5D1212))
                                else listOf(c.primary.copy(alpha = 0.85f), c.primary)
                            ),
                        CircleShape
                    )
                    .border(
                        3.dp,
                        c.bg,
                        CircleShape
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onCenterClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    calendarDate,
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "Hôm nay",
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = if (isHomeSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isHomeSelected) c.primary else c.outline
                )
            )
        }
    }
}
