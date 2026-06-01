package com.lichso.app.ui.screen.tools

import com.lichso.app.ui.theme.screenBackground
import android.graphics.Color as AndroidColor
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.lichso.app.data.remote.Banner
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.icons.PrayerIcons
import com.lichso.app.ui.theme.LichSoThemeColors

// ══════════════════════════════════════════════════════════
// TOOLS SCREEN — Grid of utilities (Tiện ích)
// ══════════════════════════════════════════════════════════

enum class ToolAction {
    AI_CHAT,
    FENG_SHUI_COMPASS,
    LO_BAN_RULER,
    BAT_TRACH,
    XONG_DAT,
    XUAT_HANH,
    SAO_HAN,
    PHI_TINH,
    FAMILY_TREE,
    HISTORY,
    GOOD_DAYS,
    ZODIAC_COMPAT,
    LUNAR_CONVERT,
    PRAYERS,
    BOOKMARKS,
    // v2 — Points Engine
    ORACLE_DRAW,
    POINTS_LEDGER,
    DAILY_STORE,
    ZODIAC_COLLECTION,
    // v2 Phase 3 — Doanh thu / Premium tools
    DATE_PICKER,
    // v2 Phase 4 — Delight
    STREAK_FREEZE,
    TIET_KHI,
    HOW_TO_EARN,
    // v2 Phase 5 — Date utilities
    DATE_MATH,
    COUNTDOWN,
    BIRTH_PLANNER,
    CYCLE_TRACKER,
    WORLD_CLOCK,
    WIDGET_MANAGER,
    // v3 — Quiz Engine
    QUIZ,
    // v3 Phase 2 — Content Feed
    KNOWLEDGE_FEED,
}

private data class ToolItem(
    val action: ToolAction,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val gradient: List<Color>,
    val badge: String? = null
)

private data class ToolSection(
    val locationKey: String,
    val title: String,
    val subtitle: String,
    val accent: Color,
    val items: List<ToolItem>
)

@Composable
fun ToolsScreen(
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onToolClick: (ToolAction) -> Unit = {},
    onBannerAction: (String) -> Unit = {},
    headerImageUrl: String? = null,
    viewModel: ToolsViewModel = hiltViewModel(),
) {
    val c = LichSoThemeColors.current
    val uiState by viewModel.uiState.collectAsState()

    // ── Nhóm 1: Lịch & Ngày tốt ──
    val calendarTools = listOf(
        ToolItem(
            ToolAction.GOOD_DAYS,
            "Ngày tốt / xấu",
            "Hoàng đạo · Hắc đạo",
            Icons.Filled.WbSunny,
            listOf(Color(0xFF2E7D32), Color(0xFF66BB6A))
        ),
        ToolItem(
            ToolAction.DATE_PICKER,
            "Chọn ngày đại sự",
            "Nhập trạch · Khai trương · Cưới hỏi",
            Icons.Filled.EventAvailable,
            listOf(Color(0xFF8B0000), Color(0xFFD4A017)),
            badge = "PRO"
        ),
        ToolItem(
            ToolAction.ZODIAC_COMPAT,
            "Xem tuổi hợp",
            "Tương sinh · Tương khắc",
            Icons.Filled.Favorite,
            listOf(Color(0xFFC2185B), Color(0xFFE91E63))
        ),
        ToolItem(
            ToolAction.LUNAR_CONVERT,
            "Đổi ngày Âm / Dương",
            "Chuyển đổi nhanh",
            Icons.Filled.SwapHoriz,
            listOf(Color(0xFF512DA8), Color(0xFF7E57C2))
        ),
        ToolItem(
            ToolAction.TIET_KHI,
            "24 Tiết Khí",
            "Vòng năm Á Đông · phong thuỷ",
            Icons.Filled.Spa,
            listOf(Color(0xFF388E3C), Color(0xFFFFB300))
        ),
        ToolItem(
            ToolAction.PRAYERS,
            "Văn khấn",
            "Bài cúng truyền thống",
            PrayerIcons.Filled,
            listOf(Color(0xFFBF360C), Color(0xFFE65100))
        ),
    )

    // ── Nhóm 2: Phong thủy & Nghi lễ ──
    val fengShuiTools = listOf(
        ToolItem(
            ToolAction.FENG_SHUI_COMPASS,
            "La bàn phong thủy",
            "Đo hướng, định cung, xem trạch",
            Icons.Filled.Explore,
            listOf(Color(0xFF1565C0), Color(0xFF26A69A)),
            badge = "NEW"
        ),
        ToolItem(
            ToolAction.LO_BAN_RULER,
            "Thước Lỗ Ban",
            "Đo kích thước cát hung",
            Icons.Filled.Straighten,
            listOf(Color(0xFF6D4C41), Color(0xFFBCAAA4)),
            badge = "NEW"
        ),
        ToolItem(
            ToolAction.BAT_TRACH,
            "Bát trạch",
            "Hướng tốt theo tuổi",
            Icons.Filled.Home,
            listOf(Color(0xFF8E24AA), Color(0xFFBA68C8)),
            badge = "NEW"
        ),
        ToolItem(
            ToolAction.XONG_DAT,
            "Xông đất",
            "Chọn người mở vận đầu năm",
            Icons.Filled.Home,
            listOf(Color(0xFFEF6C00), Color(0xFFFFB74D)),
            badge = "AI"
        ),
        ToolItem(
            ToolAction.XUAT_HANH,
            "Xuất hành",
            "Chọn hướng & giờ xuất phát",
            Icons.AutoMirrored.Filled.DirectionsWalk,
            listOf(Color(0xFF00897B), Color(0xFF4DB6AC)),
            badge = "AI"
        ),
        ToolItem(
            ToolAction.SAO_HAN,
            "Sao hạn năm",
            "Xem sao chiếu mệnh",
            Icons.Filled.Warning,
            listOf(Color(0xFFE53935), Color(0xFFFFA726)),
            badge = "AI"
        ),
        ToolItem(
            ToolAction.PHI_TINH,
            "Phi tinh cửu cung",
            "Luồng khí theo từng cung",
            Icons.Filled.GridView,
            listOf(Color(0xFF3949AB), Color(0xFF5C6BC0)),
            badge = "AI"
        ),
    )

    // ── Nhóm 3: Công cụ thực dụng ──
    val utilityTools = listOf(
        ToolItem(
            ToolAction.DATE_MATH,
            "Máy tính ngày",
            "Tuổi · Khoảng cách · Cộng/Trừ",
            Icons.Filled.Calculate,
            listOf(Color(0xFF1565C0), Color(0xFF42A5F5)),
            badge = "NEW"
        ),
        ToolItem(
            ToolAction.COUNTDOWN,
            "Đếm ngược sự kiện",
            "Hiển thị Home / Widget",
            Icons.Filled.HourglassBottom,
            listOf(Color(0xFF2E7D32), Color(0xFF66BB6A)),
            badge = "NEW"
        ),
        ToolItem(
            ToolAction.WORLD_CLOCK,
            "Múi giờ thế giới",
            "Đồng hồ & đổi múi giờ",
            Icons.Filled.Public,
            listOf(Color(0xFF1565C0), Color(0xFF5C6BC0)),
            badge = "NEW"
        ),
        ToolItem(
            ToolAction.FAMILY_TREE,
            "Cây gia phả",
            "Quản lý gia tộc",
            Icons.Filled.AccountTree,
            listOf(Color(0xFF1E88E5), Color(0xFF26A69A))
        ),
        ToolItem(
            ToolAction.HISTORY,
            "Ngày này năm xưa",
            "Sự kiện lịch sử",
            Icons.Filled.HistoryEdu,
            listOf(Color(0xFFE65100), Color(0xFFF9A825))
        ),
        ToolItem(
            ToolAction.BIRTH_PLANNER,
            "Ngày sinh đẹp",
            "Gợi ý quanh ngày dự sinh",
            Icons.Filled.ChildCare,
            listOf(Color(0xFFE65100), Color(0xFFFFB74D)),
            badge = "PRO"
        ),
        ToolItem(
            ToolAction.CYCLE_TRACKER,
            "Chu kỳ kinh nguyệt",
            "Kỳ tiếp theo · rụng trứng",
            Icons.Filled.Female,
            listOf(Color(0xFFC2185B), Color(0xFFF06292)),
            badge = "NEW"
        ),        ToolItem(
            ToolAction.WIDGET_MANAGER,
            "Quản lý Widget",
            "Thêm widget vào màn hình",
            Icons.Filled.Widgets,
            listOf(Color(0xFF6A1B9A), Color(0xFF9C27B0)),
            badge = null
        ),    )

    // ── Nhóm 4: Kho & Khám phá ──
    val collectionTools = listOf(
        ToolItem(
            ToolAction.QUIZ,
            "Quiz Kiến thức",
            "Thi đấu · Bảng xếp hạng",
            Icons.Filled.Quiz,
            listOf(Color(0xFF1565C0), Color(0xFF7B1FA2)),
            badge = "NEW"
        ),
        ToolItem(
            ToolAction.KNOWLEDGE_FEED,
            "Khám Phá",
            "Sự kiện · Nhân vật · Bài viết",
            Icons.Filled.Explore,
            listOf(Color(0xFF00838F), Color(0xFF26A69A)),
            badge = "NEW"
        ),
        ToolItem(
            ToolAction.AI_CHAT,
            "Tử vi AI",
            "Phân tích vận mệnh",
            Icons.Filled.AutoAwesome,
            listOf(Color(0xFFD32F2F), Color(0xFF7B1FA2)),
            badge = "AI"
        ),
        ToolItem(
            ToolAction.ORACLE_DRAW,
            "Rút quẻ đầu ngày",
            "Kinh Dịch · 1 lần/ngày",
            Icons.Filled.AutoAwesome,
            listOf(Color(0xFF8B0000), Color(0xFFD4A017)),
            badge = "v2"
        ),
        ToolItem(
            ToolAction.ZODIAC_COLLECTION,
            "Sưu tập 12 con giáp",
            "Rút thẻ ngày · Đổi điểm ⚡",
            Icons.Filled.Pets,
            listOf(Color(0xFF2E7D32), Color(0xFFD4A017)),
            badge = "NEW"
        ),
        ToolItem(
            ToolAction.BOOKMARKS,
            "Ngày đã lưu",
            "Đánh dấu quan trọng",
            Icons.Filled.Bookmarks,
            listOf(Color(0xFF00838F), Color(0xFF26C6DA))
        ),
        ToolItem(
            ToolAction.POINTS_LEDGER,
            "Nhật ký điểm",
            "Lịch sử công đức",
            Icons.AutoMirrored.Filled.MenuBook,
            listOf(Color(0xFF5D4037), Color(0xFF8D6E63))
        ),
        ToolItem(
            ToolAction.DAILY_STORE,
            "Kho mở khoá",
            "Tiêu ⚡ mở tính năng",
            Icons.Filled.Storefront,
            listOf(Color(0xFFD4A017), Color(0xFFF9A825))
        ),
        ToolItem(
            ToolAction.HOW_TO_EARN,
            "Hướng dẫn kiếm điểm",
            "+100☘️ khi xem lần đầu",
            Icons.AutoMirrored.Filled.MenuBook,
            listOf(Color(0xFFD4A017), Color(0xFF8B0000)),
            badge = "+100"
        ),
        ToolItem(
            ToolAction.STREAK_FREEZE,
            "Đóng băng streak",
            "Mua vé · tặng bạn bè",
            Icons.Filled.AcUnit,
            listOf(Color(0xFF1565C0), Color(0xFF26C6DA)),
            badge = "NEW"
        ),
    )

    val sections = listOf(
        ToolSection(
            locationKey = "tools_calendar",
            title = "Lịch & Ngày tốt",
            subtitle = "Chọn ngày · Đổi lịch · Tiết khí · Văn khấn",
            accent = Color(0xFF2E7D32),
            items = calendarTools
        ),
        ToolSection(
            locationKey = "tools_feng_shui",
            title = "Phong thủy & Nghi lễ",
            subtitle = "La bàn · Lỗ Ban · Bát trạch · Nghi lễ năm",
            accent = Color(0xFF1565C0),
            items = fengShuiTools
        ),
        ToolSection(
            locationKey = "tools_utility",
            title = "Công cụ thực dụng",
            subtitle = "Tính ngày · Đếm ngược · Đồng hồ · Gia phả",
            accent = Color(0xFF1565C0),
            items = utilityTools
        ),
        ToolSection(
            locationKey = "tools_collection",
            title = "Kho & Khám phá",
            subtitle = "AI · Rút quẻ · Sưu tập · Điểm thưởng",
            accent = Color(0xFF6A1B9A),
            items = collectionTools
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .screenBackground(c.bg)
    ) {
        AppTopBar(
            title = "Tiện ích",
            subtitle = "Khám phá công cụ hữu ích",
            onBackClick = onMenuClick,
            leadingIcon = Icons.Filled.Menu,
            headerImageUrl = headerImageUrl,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
        ) {
            sections.forEachIndexed { index, section ->
                val sectionBanner = remember(uiState.banners, section.locationKey) {
                    uiState.banners.firstOrNull { banner ->
                        banner.locations?.contains(section.locationKey) == true
                    }
                }
                if (sectionBanner != null) {
                    ToolsSectionBanner(
                        banner = sectionBanner,
                        onClick = {
                            val route = sectionBanner.ctaRoute
                            if (!route.isNullOrBlank()) onBannerAction(route)
                        }
                    )
                }

                ToolsSectionHeader(
                    title = section.title,
                    subtitle = section.subtitle,
                    accent = section.accent
                )

                val rows = section.items.chunked(3)
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { item ->
                                Box(modifier = Modifier.weight(1f)) {
                                    ToolCard(item = item, onClick = { onToolClick(item.action) })
                                }
                            }
                            repeat(3 - row.size) {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                if (index != sections.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Section 2: Ứng dụng khác từ Zenix Labs ──
            ToolsSectionHeader(
                title = "Ứng dụng từ Zenix Labs",
                subtitle = "Khám phá thêm sản phẩm chất lượng",
                accent = Color(0xFF1565C0),
                trailing = {
                    val context = LocalContext.current
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                val url = "https://play.google.com/store/apps/dev?id=5917415499542395567"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Tất cả",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1565C0)
                            )
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            )

            val zenixRows = zenixApps.chunked(3)
            Column(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                zenixRows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { app ->
                            Box(modifier = Modifier.weight(1f)) {
                                ZenixAppCard(app = app)
                            }
                        }
                        repeat(3 - row.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolsSectionBanner(
    banner: Banner,
    onClick: () -> Unit,
) {
    val cardColor = remember(banner.bgColor) {
        parseHexColorOrNull(banner.bgColor) ?: Color(0xFF8D1A00)
    }
    val imageUrl = remember(banner.imageUrl) { normalizeServerMediaUrl(banner.imageUrl) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardColor)
        )
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = banner.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.16f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = banner.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            if (!banner.subtitle.isNullOrBlank()) {
                Text(
                    text = banner.subtitle,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

private fun parseHexColorOrNull(hex: String?): Color? {
    val clean = hex?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return runCatching {
        Color(AndroidColor.parseColor(clean))
    }.getOrNull()
}

private fun normalizeServerMediaUrl(rawUrl: String?): String? {
    val value = rawUrl?.trim().orEmpty()
    if (value.isEmpty()) return null
    if (value.startsWith("http://") || value.startsWith("https://")) return value
    val cleaned = if (value.startsWith("/")) value.drop(1) else value
    return when {
        cleaned.startsWith("api/uploads/") -> "https://lichso.vn/$cleaned"
        cleaned.startsWith("uploads/") -> "https://lichso.vn/api/$cleaned"
        else -> "https://lichso.vn/$cleaned"
    }
}

@Composable
private fun ToolCard(item: ToolItem, onClick: () -> Unit) {
    val c = LichSoThemeColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        // Decorative gradient blob (top-right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(64.dp)
                .offset(x = 16.dp, y = (-16).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            item.gradient.last().copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
        ) {
            // Icon with gradient background
            Box {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = item.gradient,
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        item.icon,
                        contentDescription = item.title,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (item.badge != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-5).dp)
                            .background(c.gold, RoundedCornerShape(6.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            item.badge,
                            style = TextStyle(
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
                    }
                }
            }

            Text(
                item.title,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                    lineHeight = 13.sp,
                    textAlign = TextAlign.Center
                ),
                maxLines = 2
            )
        }
    }
}

// ══════════════════════════════════════════════════════════
// SECTION HEADER
// ══════════════════════════════════════════════════════════
@Composable
private fun ToolsSectionHeader(
    title: String,
    subtitle: String,
    accent: Color,
    trailing: @Composable (() -> Unit)? = null
) {
    val c = LichSoThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(22.dp)
                .background(accent, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary
                )
            )
            Text(
                subtitle,
                style = TextStyle(fontSize = 10.sp, color = c.textTertiary)
            )
        }
        if (trailing != null) trailing()
    }
}

// ══════════════════════════════════════════════════════════
// ZENIX LABS APPS
// ══════════════════════════════════════════════════════════
private data class ZenixApp(
    val name: String,
    val tagline: String,
    val packageName: String,
    val icon: @Composable () -> Unit,
    val gradient: List<Color>
)

private val zenixApps = listOf(
    ZenixApp(
        name = "Ovi: Quản Lý Chi Tiêu AI",
        tagline = "Quản lý chi tiêu",
        packageName = "com.oviapp.ovi",
        icon = { OviIcon() },
        gradient = listOf(Color(0xFF00897B), Color(0xFF26A69A))
    ),
    ZenixApp(
        name = "ViPOS - Quản lý bán hàng",
        tagline = "POS bán hàng",
        packageName = "com.minipos",
        icon = { ViPosIcon() },
        gradient = listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
    ),
    ZenixApp(
        name = "ID Photo – AI Passport Photo",
        tagline = "Ảnh thẻ AI",
        packageName = "com.idphoto_pro.app",
        icon = { IdPhotoIcon() },
        gradient = listOf(Color(0xFF6A1B9A), Color(0xFFAB47BC))
    ),
)

@Composable
private fun ZenixAppCard(app: ZenixApp) {
    val c = LichSoThemeColors.current
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(c.surfaceContainer, RoundedCornerShape(16.dp))
            .border(1.dp, c.outlineVariant, RoundedCornerShape(16.dp))
            .clickable {
                val uri = Uri.parse("https://play.google.com/store/apps/details?id=${app.packageName}")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.android.vending")
                }
                try {
                    context.startActivity(intent)
                } catch (_: Exception) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            }
    ) {
        // Decorative blob
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(64.dp)
                .offset(x = 16.dp, y = (-16).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            app.gradient.last().copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = app.gradient,
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                app.icon()
            }

            Text(
                app.name,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                    lineHeight = 13.sp,
                    textAlign = TextAlign.Center
                ),
                maxLines = 2
            )
        }
    }
}

// ══════════════════════════════════════════════════════════
// CUSTOM VECTOR ICONS for Zenix Labs apps
// All drawn via Canvas → crisp at any size, no raster assets.
// Designed to fit a 36dp gradient tile with white strokes/fills.
// ══════════════════════════════════════════════════════════

/** Ovi — minimal "$" coin with AI sparkle (chi tiêu AI) */
@Composable
private fun OviIcon() {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w * 0.42f
        val white = Color.White
        val whiteSoft = Color.White.copy(alpha = 0.55f)

        // Coin outer ring
        drawCircle(color = white, radius = r, center = Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.10f))

        // Dollar sign vertical bar
        drawLine(
            color = white,
            start = Offset(cx, cy - r * 0.62f),
            end = Offset(cx, cy + r * 0.62f),
            strokeWidth = w * 0.09f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        // S-curve top
        val seg = w * 0.09f
        drawLine(
            color = white,
            start = Offset(cx - r * 0.42f, cy - r * 0.18f),
            end = Offset(cx + r * 0.42f, cy - r * 0.18f),
            strokeWidth = seg,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        drawLine(
            color = white,
            start = Offset(cx - r * 0.42f, cy + r * 0.18f),
            end = Offset(cx + r * 0.42f, cy + r * 0.18f),
            strokeWidth = seg,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        // AI sparkle (top-right)
        val sx = cx + r * 0.95f
        val sy = cy - r * 0.95f
        val sr = w * 0.11f
        drawLine(color = whiteSoft, start = Offset(sx - sr, sy), end = Offset(sx + sr, sy), strokeWidth = w * 0.045f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color = whiteSoft, start = Offset(sx, sy - sr), end = Offset(sx, sy + sr), strokeWidth = w * 0.045f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    }
}

/** ViPOS — receipt with bars (POS / sales) */
@Composable
private fun ViPosIcon() {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val white = Color.White
        val whiteSoft = Color.White.copy(alpha = 0.45f)

        // Receipt rounded rect
        val left = w * 0.18f
        val right = w * 0.82f
        val top = h * 0.10f
        val bottom = h * 0.78f
        val corner = w * 0.10f

        drawRoundRect(
            color = white,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.08f)
        )

        // Zig-zag bottom edge
        val path = androidx.compose.ui.graphics.Path().apply {
            val zigY = bottom
            val zigStep = (right - left) / 6f
            moveTo(left, zigY)
            for (i in 1..6) {
                val x = left + zigStep * i
                val y = if (i % 2 == 1) zigY + h * 0.08f else zigY
                lineTo(x, y)
            }
        }
        drawPath(path, color = white, style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.08f, join = androidx.compose.ui.graphics.StrokeJoin.Round))

        // Bar chart inside (3 bars rising)
        val barW = w * 0.07f
        val baseY = h * 0.62f
        val barX1 = w * 0.32f
        val barX2 = w * 0.48f
        val barX3 = w * 0.64f
        drawLine(color = whiteSoft, start = Offset(barX1, baseY), end = Offset(barX1, baseY - h * 0.10f), strokeWidth = barW, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color = white, start = Offset(barX2, baseY), end = Offset(barX2, baseY - h * 0.20f), strokeWidth = barW, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color = white, start = Offset(barX3, baseY), end = Offset(barX3, baseY - h * 0.30f), strokeWidth = barW, cap = androidx.compose.ui.graphics.StrokeCap.Round)

        // Top header line
        drawLine(
            color = whiteSoft,
            start = Offset(left + w * 0.08f, top + h * 0.13f),
            end = Offset(right - w * 0.08f, top + h * 0.13f),
            strokeWidth = w * 0.05f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

/** ID Photo — passport-style portrait silhouette in a frame */
@Composable
private fun IdPhotoIcon() {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val white = Color.White
        val whiteSoft = Color.White.copy(alpha = 0.45f)

        // Outer photo frame
        val left = w * 0.14f
        val right = w * 0.86f
        val top = h * 0.10f
        val bottom = h * 0.90f
        val corner = w * 0.10f
        drawRoundRect(
            color = white,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.08f)
        )

        // Head (circle)
        val cx = w / 2f
        val headCy = h * 0.42f
        val headR = w * 0.13f
        drawCircle(color = white, radius = headR, center = Offset(cx, headCy))

        // Shoulders (arc-like rounded rect)
        val shoulderTop = h * 0.58f
        val shoulderBottom = h * 0.78f
        val shoulderW = w * 0.42f
        drawRoundRect(
            color = white,
            topLeft = Offset(cx - shoulderW / 2f, shoulderTop),
            size = androidx.compose.ui.geometry.Size(shoulderW, shoulderBottom - shoulderTop + corner),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.18f, w * 0.18f)
        )

        // Corner ticks (camera/AI accent in top-right)
        val tickLen = w * 0.10f
        val tickInset = w * 0.04f
        val tx = right + tickInset
        val ty = top - tickInset
        drawLine(color = whiteSoft, start = Offset(tx - tickLen, ty), end = Offset(tx, ty), strokeWidth = w * 0.05f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color = whiteSoft, start = Offset(tx, ty), end = Offset(tx, ty + tickLen), strokeWidth = w * 0.05f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    }
}
