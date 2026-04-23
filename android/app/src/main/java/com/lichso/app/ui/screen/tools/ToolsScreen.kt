package com.lichso.app.ui.screen.tools

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lichso.app.ui.components.AppTopBar
import com.lichso.app.ui.theme.LichSoThemeColors

// ══════════════════════════════════════════════════════════
// TOOLS SCREEN — Grid of utilities (Tiện ích)
// ══════════════════════════════════════════════════════════

enum class ToolAction {
    AI_CHAT,
    FAMILY_TREE,
    HISTORY,
    GOOD_DAYS,
    ZODIAC_COMPAT,
    LUNAR_CONVERT,
    PRAYERS,
    BOOKMARKS
}

private data class ToolItem(
    val action: ToolAction,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val gradient: List<Color>,
    val badge: String? = null
)

@Composable
fun ToolsScreen(
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onToolClick: (ToolAction) -> Unit = {}
) {
    val c = LichSoThemeColors.current

    val tools = listOf(
        ToolItem(
            ToolAction.AI_CHAT,
            "Tử vi AI",
            "Phân tích vận mệnh",
            Icons.Filled.AutoAwesome,
            listOf(Color(0xFFD32F2F), Color(0xFF7B1FA2)),
            badge = "AI"
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
            ToolAction.GOOD_DAYS,
            "Ngày tốt / xấu",
            "Hoàng đạo · Hắc đạo",
            Icons.Filled.WbSunny,
            listOf(Color(0xFF2E7D32), Color(0xFF66BB6A))
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
            ToolAction.PRAYERS,
            "Văn khấn",
            "Bài cúng truyền thống",
            Icons.Filled.LocalFireDepartment,
            listOf(Color(0xFFBF360C), Color(0xFFE65100))
        ),
        ToolItem(
            ToolAction.BOOKMARKS,
            "Ngày đã lưu",
            "Đánh dấu quan trọng",
            Icons.Filled.Bookmarks,
            listOf(Color(0xFF00838F), Color(0xFF26C6DA))
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        AppTopBar(
            title = "Tiện ích",
            subtitle = "Khám phá công cụ hữu ích",
            onBackClick = onMenuClick,
            leadingIcon = Icons.Filled.Menu
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            // ── Section 1: Tiện ích trong app ──
            ToolsSectionHeader(
                title = "Công cụ trong ứng dụng",
                subtitle = "Tiện ích tích hợp sẵn",
                accent = Color(0xFFD32F2F)
            )

            // Render grid as plain rows (3 per row) so it fits inside scroll
            val rows = tools.chunked(3)
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
                        // Fill empty slots so last row keeps card width consistent
                        repeat(3 - row.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
