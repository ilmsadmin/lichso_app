package com.lichso.app.ui

import com.lichso.app.ui.theme.screenBackground
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import com.lichso.app.feature.fengshui.BatTrachScreen
import com.lichso.app.feature.fengshui.CompassScreen
import com.lichso.app.feature.fengshui.LoBanScreen
import com.lichso.app.ui.screen.tools.ToolsScreen
import com.lichso.app.ui.screen.tools.ToolAction
import com.lichso.app.ui.screen.tools.WidgetManagerScreen
import com.lichso.app.R
import com.lichso.app.analytics.Analytics
import com.lichso.app.ui.icons.PrayerIcons
import com.lichso.app.ui.theme.*
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.lichso.app.util.SmartRatingManager
import com.lichso.app.ui.components.SmartRatingDialog
import com.lichso.app.update.InAppUpdateManager
import com.lichso.app.feature.points.ui.PointsViewModel
import com.lichso.app.feature.points.ui.PointsEvent
import com.lichso.app.feature.points.ui.RankUpDialog
import com.lichso.app.feature.points.ui.DailyOracleDialog
import com.lichso.app.feature.points.domain.ActionType
import com.lichso.app.feature.points.domain.Clock as PointsClock
import com.lichso.app.data.remote.Popup
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LichSoMainScreen(
    modifier: Modifier = Modifier,
    initialRoute: String = "home",
    giftToken: String? = null,
) {
    val c = LichSoThemeColors.current
    var currentRoute by remember { mutableStateOf(initialRoute) }
    var selectedArticleId by remember { mutableStateOf<String?>(null) }
    var articleDetailBackRoute by remember { mutableStateOf("knowledge_feed") }
    var prayerDetailShowing by remember { mutableStateOf(false) }
    var taskEditShowing by remember { mutableStateOf(false) }
    var initialPrayerId by remember { mutableStateOf<Int?>(null) }
    var initialAiMessage by remember { mutableStateOf<String?>(null) }
    var initialSearchTool by remember { mutableStateOf<String?>(null) }
    var quizCategory by remember { mutableStateOf<String?>(null) }
    val homeViewModel: HomeViewModel = hiltViewModel()
    val screenBgRepo = homeViewModel.screenBackgroundRepository
    val pointsViewModel: PointsViewModel = hiltViewModel()
    val pointsClock: PointsClock = remember { com.lichso.app.feature.points.domain.SystemClock() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    fun handleBannerAction(target: String) {
        val route = target.trim()
        if (route.isBlank()) return
        if (route.startsWith("http://") || route.startsWith("https://")) {
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(route)))
            }
            return
        }
        when (route) {
            "chat" -> {
                initialAiMessage = null
                currentRoute = "chat"
            }
            else -> currentRoute = route
        }
    }

    // Phase 4 — redeem streak-freeze gift từ deep link (1 lần duy nhất / token)
    LaunchedEffect(giftToken) {
        if (!giftToken.isNullOrBlank()) {
            pointsViewModel.redeemFreezeGift(giftToken)
        }
    }

    // ── v2 Points Engine: auto check-in + rank-up dialog ──
    LaunchedEffect(Unit) {
        pointsViewModel.checkInToday()
    }
    var rankUpEvent by remember { mutableStateOf<PointsEvent.RankUp?>(null) }
    LaunchedEffect(Unit) {
        pointsViewModel.events.collect { ev ->
            when (ev) {
                is PointsEvent.RankUp -> rankUpEvent = ev
                is PointsEvent.CheckedIn -> SmartRatingManager.recordHappyAction(context.applicationContext)
                else -> Unit
            }
        }
    }
    rankUpEvent?.let { ev ->
        RankUpDialog(
            rank = ev.rank,
            newUnlocks = ev.unlocks,
            onDismiss = { rankUpEvent = null },
        )
    }

    // ── Daily Oracle Popup: hiển thị 1 lần/ngày khi mở app ──
    // shouldShowDailyOraclePopup là true khi chưa show hôm nay → chốt vào local state
    // ngay lập tức để tránh dialog biến mất khi DataStore ghi xong và flow emit false.
    val shouldShowDailyOracle by pointsViewModel.shouldShowDailyOraclePopup.collectAsState()
    var dailyOracleVisible by remember { mutableStateOf(false) }
    LaunchedEffect(shouldShowDailyOracle) {
        if (shouldShowDailyOracle && !dailyOracleVisible) {
            dailyOracleVisible = true
            pointsViewModel.dismissDailyOraclePopup() // ghi ngày hôm nay vào DataStore
        }
    }
    if (dailyOracleVisible) {
        DailyOracleDialog(
            clock = pointsClock,
            onDismiss = { dailyOracleVisible = false },
            onAskAi = { prompt ->
                dailyOracleVisible = false
                initialAiMessage = prompt
                currentRoute = "chat"
            },
        )
    }

    // ── Analytics: log screen_view + custom screen event mỗi khi route đổi ──
    LaunchedEffect(currentRoute) {
        Analytics.logScreen(currentRoute)
    }

    // ── Phase 4+: trao điểm khi user mở các route quan trọng (cap theo dailyCap) ──
    LaunchedEffect(currentRoute) {
        val action = when (currentRoute) {
            "calendar" -> ActionType.VISIT_LUNAR_CALENDAR
            "prayers" -> ActionType.VISIT_VAN_KHAN
            "tools" -> ActionType.VISIT_TOOLS
            "chat" -> ActionType.VISIT_TU_VI
            "history" -> ActionType.VIEW_HISTORY_TODAY
            "ledger" -> ActionType.VIEW_LEDGER
            "profile" -> ActionType.VIEW_PROFILE
            "daily_store" -> ActionType.VIEW_DAILY_STORE
            "date_picker" -> ActionType.USE_DATE_PICKER
            "tiet_khi" -> ActionType.VIEW_TIET_KHI
            else -> null
        }
        if (action != null) pointsViewModel.award(action)
    }

    // ── Smart Rating Dialog ──
    val showRatingDialog by SmartRatingManager.shouldShow.collectAsState()
    SmartRatingDialog(
        visible = showRatingDialog,
        onDismiss = { SmartRatingManager.dismissNow() }
    )

    // ── In-App Update: hiển thị Snackbar "Khởi động lại" khi bản FLEXIBLE
    // đã tải xong ở nền. User bấm "Khởi động lại" → app tự restart và áp
    // dụng bản mới (không cần qua Play Store). ──
    val updateState by InAppUpdateManager.uiState.collectAsState()
    val updateSnackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(updateState) {
        if (updateState is InAppUpdateManager.UiState.ReadyToInstall) {
            val result = updateSnackbarHostState.showSnackbar(
                message = "Bản mới đã sẵn sàng — khởi động lại để cập nhật",
                actionLabel = "Khởi động lại",
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                InAppUpdateManager.completeFlexibleUpdate()
            }
        }
    }

    val hideBottomBar = currentRoute in listOf("chat", "familytree", "settings", "history", "notifications", "search", "bookmarks", "gooddays", "profile", "oracle_draw", "oracle_result", "ledger", "daily_store", "zodiac_collection", "date_picker", "streak_freeze", "points_tutorial", "tiet_khi", "widget_manager", "quiz_session", "quiz_result", "leaderboard", "knowledge_feed", "article_detail") || prayerDetailShowing || taskEditShowing

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

        Box(modifier = modifier.fillMaxSize().screenBackground(c.bg)) {
        // Content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (hideBottomBar) Modifier.navigationBarsPadding()
                    else Modifier.padding(bottom = bottomBarTotalHeight)
                )
        ) {
            // Screen-specific background image (fetched from server, cached by Coil)
            val screenBgUrl = rememberScreenBackgroundUrl(currentRoute, screenBgRepo)
            val normalizedBgUrl = normalizeServerMediaUrl(screenBgUrl)
            if (!normalizedBgUrl.isNullOrBlank()) {
                AsyncImage(
                    model = normalizedBgUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            // Header image — keyed by "<screen>_header" convention
            val headerBgUrl = normalizeServerMediaUrl(
                rememberScreenBackgroundUrl("${currentRoute}_header", screenBgRepo)
            )
            CompositionLocalProvider(LocalScreenBackgroundMode provides !normalizedBgUrl.isNullOrBlank()) {
                when (currentRoute) {
                    "home" -> HomeScreen(
                        onSettingsClick = { currentRoute = "settings" },
                        onMenuClick = toggleDrawer,
                        onProfileClick = { currentRoute = "profile" },
                        onHistoryClick = { currentRoute = "history" },
                        onNotificationClick = { currentRoute = "notifications" },
                        onCountdownClick = { currentRoute = "countdown" },
                        onFortuneCardShown = {
                            pointsViewModel.award(ActionType.VIEW_FORTUNE_CARD)
                        },
                        onBannerAction = { route -> handleBannerAction(route) },
                        hasServerBackground = !normalizedBgUrl.isNullOrBlank(),
                        headerImageUrl = headerBgUrl,
                    )
                    "calendar" -> CalendarScreen(
                        onGoodDaysClick = { currentRoute = "gooddays" },
                        onSearchClick = { currentRoute = "search" },
                        onMenuClick = toggleDrawer,
                        onEditVisibilityChanged = { taskEditShowing = it },
                        onAskAiClick = { day, month, year ->
                            initialAiMessage = "Phân tích chi tiết ngày $day/$month/$year"
                            currentRoute = "chat"
                        },
                        onArticleClick = { articleId ->
                            selectedArticleId = articleId
                            articleDetailBackRoute = "calendar"
                            currentRoute = "article_detail"
                        },
                        headerImageUrl = headerBgUrl,
                    )
                    "gooddays" -> GoodDaysScreen(
                        onBackClick = { currentRoute = "home" }
                    )
                    "prayers" -> PrayersScreen(
                        onBackClick = { currentRoute = "home" },
                        onMenuClick = toggleDrawer,
                        onDetailVisibilityChanged = { prayerDetailShowing = it },
                        initialPrayerId = initialPrayerId.also { initialPrayerId = null },
                        headerImageUrl = headerBgUrl,
                    )
                    "profile" -> ProfileScreen(
                        onSettingsClick = { currentRoute = "settings" },
                        onFamilyTreeClick = { currentRoute = "familytree" },
                        onBackClick = { currentRoute = "home" },
                        onMenuClick = toggleDrawer,
                        onTasksClick = { currentRoute = "tasks" },
                        onBookmarksClick = { currentRoute = "bookmarks" },
                        onLedgerClick = { currentRoute = "ledger" },
                    )
                    "tasks" -> TasksScreen3(
                        onBackClick = { currentRoute = "home" },
                        onMenuClick = toggleDrawer,
                        onEditVisibilityChanged = { taskEditShowing = it }
                    )
                    "tools" -> ToolsScreen(
                        onBackClick = { currentRoute = "home" },
                        onMenuClick = toggleDrawer,
                        onToolClick = { action ->
                            when (action) {
                                ToolAction.AI_CHAT -> {
                                    initialAiMessage = null
                                    currentRoute = "chat"
                                }
                                ToolAction.FENG_SHUI_COMPASS -> currentRoute = "feng_shui_compass"
                                ToolAction.LO_BAN_RULER -> currentRoute = "lo_ban"
                                ToolAction.BAT_TRACH -> currentRoute = "bat_trach"
                                ToolAction.XONG_DAT -> {
                                    initialAiMessage = "Hãy gợi ý người xông đất hợp tuổi, hợp mệnh và các lưu ý cần tránh cho năm nay."
                                    currentRoute = "chat"
                                }
                                ToolAction.XUAT_HANH -> {
                                    initialAiMessage = "Hãy gợi ý hướng và giờ xuất hành tốt, kèm giải thích ngắn gọn theo phong thuỷ."
                                    currentRoute = "chat"
                                }
                                ToolAction.SAO_HAN -> {
                                    initialAiMessage = "Hãy phân tích sao hạn năm nay và cho lời khuyên hoá giải đơn giản, dễ hiểu."
                                    currentRoute = "chat"
                                }
                                ToolAction.PHI_TINH -> {
                                    initialAiMessage = "Hãy giải thích phi tinh cửu cung và gợi ý cách đọc cơ bản cho người mới."
                                    currentRoute = "chat"
                                }
                                ToolAction.FAMILY_TREE -> currentRoute = "familytree"
                                ToolAction.HISTORY -> currentRoute = "history"
                                ToolAction.GOOD_DAYS -> currentRoute = "gooddays"
                                ToolAction.ZODIAC_COMPAT -> {
                                    initialSearchTool = "zodiac"
                                    currentRoute = "search"
                                    pointsViewModel.award(ActionType.USE_ZODIAC_COMPAT)
                                }
                                ToolAction.LUNAR_CONVERT -> {
                                    initialSearchTool = "lunar"
                                    currentRoute = "search"
                                    pointsViewModel.award(ActionType.USE_LUNAR_CONVERTER)
                                }
                                ToolAction.PRAYERS -> currentRoute = "prayers"
                                ToolAction.BOOKMARKS -> currentRoute = "bookmarks"
                                ToolAction.ORACLE_DRAW -> currentRoute = "oracle_draw"
                                ToolAction.DAILY_STORE -> currentRoute = "daily_store"
                                ToolAction.POINTS_LEDGER -> currentRoute = "ledger"
                                ToolAction.ZODIAC_COLLECTION -> currentRoute = "zodiac_collection"
                                ToolAction.DATE_PICKER -> currentRoute = "date_picker"
                                ToolAction.STREAK_FREEZE -> currentRoute = "streak_freeze"
                                ToolAction.TIET_KHI -> currentRoute = "tiet_khi"
                                ToolAction.HOW_TO_EARN -> currentRoute = "points_tutorial"
                                ToolAction.DATE_MATH -> currentRoute = "date_math"
                                ToolAction.COUNTDOWN -> currentRoute = "countdown"
                                ToolAction.BIRTH_PLANNER -> currentRoute = "birth_planner"
                                ToolAction.CYCLE_TRACKER -> currentRoute = "cycle_tracker"
                                ToolAction.WORLD_CLOCK -> currentRoute = "world_clock"
                                ToolAction.WIDGET_MANAGER -> currentRoute = "widget_manager"
                                ToolAction.QUIZ -> currentRoute = "quiz_home"
                                ToolAction.KNOWLEDGE_FEED -> currentRoute = "knowledge_feed"
                                else -> {}
                            }
                        },
                        headerImageUrl = headerBgUrl,
                    )
                    "feng_shui_compass" -> CompassScreen(
                        onBackClick = { currentRoute = "tools" },
                        onAskAiClick = { prompt ->
                            initialAiMessage = prompt
                            currentRoute = "chat"
                        }
                    )
                    "lo_ban" -> LoBanScreen(
                        onBackClick = { currentRoute = "tools" },
                        onAskAiClick = { prompt ->
                            initialAiMessage = prompt
                            currentRoute = "chat"
                        }
                    )
                    "bat_trach" -> BatTrachScreen(
                        onBackClick = { currentRoute = "tools" },
                        onAskAiClick = { prompt ->
                            initialAiMessage = prompt
                            currentRoute = "chat"
                        }
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
                        onGoodDaysClick = { currentRoute = "gooddays" },
                        initialTool = initialSearchTool.also { initialSearchTool = null }
                    )
                    "bookmarks" -> BookmarksScreen(
                        onBackClick = { currentRoute = "profile" },
                        onDateSelected = { year, month, day ->
                            homeViewModel.goToDate(year, month, day)
                            currentRoute = "calendar"
                        },
                        onAddBookmark = { currentRoute = "calendar" }
                    )
                    "oracle_draw" -> com.lichso.app.feature.points.ui.OracleDrawScreen(
                        onBackClick = { currentRoute = "tools" },
                        onDrawn = { currentRoute = "oracle_result" },
                        clock = pointsClock,
                    )
                    "oracle_result" -> com.lichso.app.feature.points.ui.OracleResultScreen(
                        onBackClick = { currentRoute = "tools" },
                        onAskAi = { prompt ->
                            initialAiMessage = prompt
                            currentRoute = "chat"
                        },
                        clock = pointsClock,
                    )
                    "ledger" -> com.lichso.app.feature.points.ui.LedgerScreen(
                        onBackClick = { currentRoute = "tools" },
                    )
                    "daily_store" -> com.lichso.app.feature.points.ui.DailyUnlockStoreScreen(
                        onBackClick = { currentRoute = "tools" },
                    )
                    "zodiac_collection" -> com.lichso.app.feature.points.ui.ZodiacCollectionScreen(
                        onBackClick = { currentRoute = "tools" },
                    )
                    "date_picker" -> com.lichso.app.feature.datepicker.DatePickerToolScreen(
                        onBackClick = { currentRoute = "tools" },
                    )
                    "streak_freeze" -> com.lichso.app.feature.points.ui.StreakFreezeScreen(
                        onBackClick = { currentRoute = "profile" },
                    )
                    "tiet_khi" -> com.lichso.app.feature.tietkhi.TietKhiScreen(
                        onBackClick = { currentRoute = "tools" },
                    )
                    "date_math" -> com.lichso.app.feature.datemath.DateMathScreen(
                        onBackClick = { currentRoute = "tools" },
                    )
                    "countdown" -> com.lichso.app.feature.countdown.CountdownScreen(
                        onBackClick = { currentRoute = "tools" },
                    )
                    "birth_planner" -> com.lichso.app.feature.birthplanner.BirthDatePlannerScreen(
                        onBackClick = { currentRoute = "tools" },
                    )
                    "cycle_tracker" -> com.lichso.app.feature.cycle.CycleTrackerScreen(
                        onBackClick = { currentRoute = "tools" },
                    )
                    "world_clock" -> com.lichso.app.feature.worldclock.WorldClockScreen(
                        onBackClick = { currentRoute = "tools" },
                    )
                    "points_tutorial" -> com.lichso.app.feature.points.ui.PointsTutorialScreen(
                        onBack = { currentRoute = "profile" },
                        onAction = { actionType ->
                            currentRoute = when (actionType.deeplink) {
                                "lichso://home" -> "home"
                                "lichso://calendar" -> "calendar"
                                "lichso://prayers" -> "prayers"
                                "lichso://tools" -> "tools"
                                "lichso://chat" -> "chat"
                                "lichso://oracle" -> "oracle_draw"
                                "lichso://history" -> "history"
                                "lichso://ledger" -> "ledger"
                                "lichso://profile" -> "profile"
                                "lichso://store" -> "daily_store"
                                "lichso://search" -> "search"
                                "lichso://date_picker" -> "date_picker"
                                "lichso://bookmarks" -> "bookmarks"
                                "lichso://tiet_khi" -> "tiet_khi"
                                "lichso://tasks" -> "home" // tasks integrated in home
                                else -> "home"
                            }
                        },
                    )
                    "widget_manager" -> WidgetManagerScreen(
                        onBackClick = { currentRoute = "tools" }
                    )
                    "quiz_home" -> com.lichso.app.feature.quiz.QuizHomeScreen(
                        onBackClick = { currentRoute = "home" },
                        onStartDaily = {
                            quizCategory = null
                            currentRoute = "quiz_session"
                        },
                        onStartTopic = { category ->
                            quizCategory = category
                            currentRoute = "quiz_session"
                        },
                        onLeaderboard = { currentRoute = "leaderboard" },
                    )
                    "quiz_session" -> com.lichso.app.feature.quiz.QuizSessionScreen(
                        onBackClick = { currentRoute = "quiz_home" },
                        onFinished = { currentRoute = "quiz_result" },
                        initialCategory = quizCategory,
                        onAskAi = { prompt ->
                            initialAiMessage = prompt
                            currentRoute = "chat"
                        },
                    )
                    "quiz_result" -> com.lichso.app.feature.quiz.QuizResultScreen(
                        onBackClick = { currentRoute = "quiz_home" },
                        onAskAi = { prompt ->
                            initialAiMessage = prompt
                            currentRoute = "chat"
                        },
                        onPlayAgain = { currentRoute = "quiz_session" },
                    )
                    "leaderboard" -> com.lichso.app.feature.quiz.LeaderboardScreen(
                        onBackClick = { currentRoute = "quiz_home" },
                    )
                    "knowledge_feed" -> com.lichso.app.feature.content.KnowledgeFeedScreen(
                        onBackClick = { currentRoute = "home" },
                        onAskAi = { prompt ->
                            initialAiMessage = prompt
                            currentRoute = "chat"
                        },
                        onArticleClick = { articleId ->
                            selectedArticleId = articleId
                            articleDetailBackRoute = "knowledge_feed"
                            currentRoute = "article_detail"
                        }
                    )
                    "article_detail" -> {
                        selectedArticleId?.let { articleId ->
                            com.lichso.app.feature.content.ArticleDetailScreen(
                                articleId = articleId,
                                onBackClick = { currentRoute = articleDetailBackRoute }
                            )
                        }
                    }
                    else -> HomeScreen(
                        onSettingsClick = { currentRoute = "settings" },
                        onMenuClick = toggleDrawer,
                        onProfileClick = { currentRoute = "profile" },
                        onHistoryClick = { currentRoute = "history" },
                        onNotificationClick = { currentRoute = "notifications" },
                        onBannerAction = { route -> handleBannerAction(route) },
                        hasServerBackground = !normalizedBgUrl.isNullOrBlank(),
                        headerImageUrl = headerBgUrl,
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

        // ── In-App Update Snackbar ──
        // Đặt phía trên bottom nav để không bị che. Indefinite — chỉ ẩn khi
        // user bấm "Khởi động lại" hoặc swipe-dismiss.
        SnackbarHost(
            hostState = updateSnackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = if (hideBottomBar) navBarBottom + 16.dp
                             else bottomBarTotalHeight + 8.dp
                )
                .padding(horizontal = 12.dp)
        )

        // ── Drag-and-Drop Popups Overlay ──
        val activePopups by homeViewModel.popups.collectAsState()
        val dismissedPopupIds = remember { mutableStateListOf<String>() }
        val currentPopup = activePopups.firstOrNull { it.id !in dismissedPopupIds }

        if (currentPopup != null) {
            var offsetX by remember(currentPopup.id) { mutableStateOf(0f) }
            var offsetY by remember(currentPopup.id) { mutableStateOf(0f) }
            var totalDragDistance by remember(currentPopup.id) { mutableStateOf(0f) }

            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .align(
                        when (currentPopup.position) {
                            "top_left" -> Alignment.TopStart
                            "top_right" -> Alignment.TopEnd
                            "bottom_left" -> Alignment.BottomStart
                            "bottom_right" -> Alignment.BottomEnd
                            "center_left" -> Alignment.CenterStart
                            "center_right" -> Alignment.CenterEnd
                            else -> Alignment.Center
                        }
                    )
                    .padding(
                        when (currentPopup.position) {
                            "top_left" -> PaddingValues(top = 96.dp, start = 16.dp)
                            "top_right" -> PaddingValues(top = 96.dp, end = 16.dp)
                            "bottom_left" -> PaddingValues(bottom = 96.dp, start = 16.dp)
                            "bottom_right" -> PaddingValues(bottom = 96.dp, end = 16.dp)
                            "center_left" -> PaddingValues(start = 16.dp)
                            "center_right" -> PaddingValues(end = 16.dp)
                            else -> PaddingValues(16.dp)
                        }
                    )
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            ) {
                // Popup Image (draggable and clickable)
                AsyncImage(
                    model = normalizeServerMediaUrl(currentPopup.imageUrl),
                    contentDescription = currentPopup.title,
                    modifier = Modifier
                        .widthIn(max = 90.dp)
                        .heightIn(max = 90.dp)
                        .pointerInput(currentPopup.id) {
                            awaitPointerEventScope {
                                while (true) {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    var pointerId = down.id
                                    var totalDragX = 0f
                                    var totalDragY = 0f
                                    var isDrag = false

                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val dragEvent = event.changes.firstOrNull { it.id == pointerId }
                                        if (dragEvent == null) {
                                            break
                                        }
                                        if (!dragEvent.pressed) {
                                            // Finger released
                                            if (!isDrag) {
                                                handleBannerAction(currentPopup.ctaRoute ?: "")
                                            }
                                            break
                                        }

                                        val positionChange = dragEvent.position - dragEvent.previousPosition
                                        totalDragX += positionChange.x
                                        totalDragY += positionChange.y

                                        if (!isDrag && (totalDragX * totalDragX + totalDragY * totalDragY > viewConfiguration.touchSlop * viewConfiguration.touchSlop)) {
                                            isDrag = true
                                        }

                                        if (isDrag) {
                                            dragEvent.consume()
                                            offsetX += positionChange.x
                                            offsetY += positionChange.y
                                        }
                                    }
                                }
                            }
                        }
                )

                // Close Button - placed inside the popup image layer
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = 8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .clickable { dismissedPopupIds.add(currentPopup.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Color.White,
                        modifier = Modifier.size(9.dp)
                    )
                }
            }
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
        DrawerMenuItem("prayers", "Văn Khấn", PrayerIcons.Outlined, PrayerIcons.Filled),
    )

    val exploreItems = listOf(
        DrawerMenuItem("history", "Ngày này năm xưa", Icons.Outlined.HistoryEdu, Icons.Filled.HistoryEdu),
        DrawerMenuItem("familytree", "Cây gia phả", Icons.Outlined.AccountTree, Icons.Filled.AccountTree),
        DrawerMenuItem("knowledge_feed", "Bài viết khám phá", Icons.Outlined.Article, Icons.Filled.Article),
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
        NavItem("quiz_home", "Đố Vui", Icons.Outlined.Quiz, Icons.Filled.Quiz),
        NavItem("tools", "Tiện ích", Icons.Outlined.Apps, Icons.Filled.Apps),
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
                .screenBackground(c.bg)
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
