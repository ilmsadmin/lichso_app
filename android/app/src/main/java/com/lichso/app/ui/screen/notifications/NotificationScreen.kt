package com.lichso.app.ui.screen.notifications

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.lichso.app.ui.components.AppTopBar
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lichso.app.data.local.entity.NotificationEntity
import com.lichso.app.ui.theme.*

@Composable
fun NotificationScreen(
    onBackClick: () -> Unit = {},
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val c = LichSoThemeColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedNotification by remember { mutableStateOf<NotificationEntity?>(null) }

    // Group notifications by date
    val grouped = remember(state.notifications) {
        state.notifications.groupBy { notif ->
            viewModel.getDateLabel(notif.createdAt)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        // ═══ TOP BAR ═══
        TopBar(
            unreadCount = state.unreadCount,
            totalCount = state.notifications.size,
            onBackClick = onBackClick,
            onMarkAllRead = { viewModel.markAllAsRead() },
            onDeleteAll = { viewModel.deleteAllNotifications() }
        )

        // ═══ NOTIFICATION LIST ═══
        if (state.notifications.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.NotificationsOff,
                        contentDescription = null,
                        tint = c.textQuaternary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Chưa có thông báo nào",
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.textSecondary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Thông báo mới sẽ xuất hiện tại đây",
                        style = TextStyle(fontSize = 13.sp, color = c.textTertiary)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                grouped.forEach { (dateLabel, notifications) ->
                    // Date group header
                    item(key = "header_$dateLabel") {
                        Text(
                            text = dateLabel,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = c.primary,
                                letterSpacing = 0.5.sp
                            ),
                            modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 10.dp)
                        )
                    }

                    // Notification cards with swipe-to-dismiss
                    items(
                        items = notifications,
                        key = { it.id }
                    ) { notification ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteNotification(notification)
                                    true
                                } else false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 8.dp)
                                        .background(Color(0xFFE53935), RoundedCornerShape(16.dp))
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Xoá",
                                        tint = Color.White
                                    )
                                }
                            },
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = true
                        ) {
                            NotificationCard(
                                notification = notification,
                                timeLabel = viewModel.getTimeLabel(notification.createdAt),
                                onClick = {
                                    if (!notification.isRead) {
                                        viewModel.markAsRead(notification)
                                    }
                                    selectedNotification = notification
                                },
                                onDismiss = { viewModel.deleteNotification(notification) }
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }

    // ═══ NOTIFICATION DETAIL OVERLAY ═══
    AnimatedVisibility(
        visible = selectedNotification != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 4 })
    ) {
        selectedNotification?.let { notification ->
            NotificationDetailOverlay(
                notification = notification,
                dateLabel = viewModel.getDateLabel(notification.createdAt),
                timeLabel = viewModel.getTimeLabel(notification.createdAt),
                onDismiss = { selectedNotification = null },
                onDelete = {
                    viewModel.deleteNotification(notification)
                    selectedNotification = null
                }
            )
        }
    }
    } // end Box
} // end NotificationScreen

@Composable
private fun TopBar(
    unreadCount: Int,
    totalCount: Int,
    onBackClick: () -> Unit,
    onMarkAllRead: () -> Unit,
    onDeleteAll: () -> Unit
) {
    val c = LichSoThemeColors.current

    AppTopBar(
        title = "Thông báo",
        subtitle = if (unreadCount > 0) "$unreadCount chưa đọc" else null,
        onBackClick = onBackClick,
        actions = {
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onMarkAllRead() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        "Đọc hết",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                }
            }
            if (totalCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onDeleteAll() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        "Xoá hết",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    )
}

@Composable
private fun NotificationCard(
    notification: NotificationEntity,
    timeLabel: String,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val c = LichSoThemeColors.current
    val isUnread = !notification.isRead
    val typeInfo = getNotificationTypeInfo(notification.type)
    val previewText = remember(notification.description, notification.type) {
        buildNotificationPreview(notification)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(
                if (isUnread) c.primaryContainer else c.surfaceContainer,
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                if (isUnread) c.primary.copy(alpha = 0.2f) else c.outlineVariant,
                RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(typeInfo.bgColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                typeInfo.icon,
                contentDescription = null,
                tint = typeInfo.iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        // Content
        Column(modifier = Modifier.weight(1f)) {
            if (isUnread) {
                Text(
                    "Mới",
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = c.primary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                notification.title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary,
                    lineHeight = 18.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (previewText.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    previewText,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = c.textSecondary,
                        lineHeight = 17.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                timeLabel,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = c.textTertiary
                )
            )
        }

        // Unread dot
        if (isUnread) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(8.dp)
                    .background(c.primary, CircleShape)
            )
        }
    }
}

private data class NotifTypeInfo(
    val icon: ImageVector,
    val iconColor: Color,
    val bgColor: Color
)

private data class MorningSummary(
    val weather: String = "",
    val advice: String = "",
    val date: String = "",
    val canChi: String = "",
    val goodHours: String = "",
    val shouldDo: String = "",
    val shouldAvoid: String = "",
)

private fun parseMorningSummary(description: String): MorningSummary {
    val lines = description.lines().map { it.trim() }.filter { it.isNotBlank() }
    return MorningSummary(
        weather = lines.firstOrNull().orEmpty(),
        advice = lines.getOrNull(1).orEmpty(),
        date = lines.firstOrNull { it.startsWith("Ngày:") }?.substringAfter(":")?.trim().orEmpty(),
        canChi = lines.firstOrNull { it.startsWith("Can Chi:") }?.substringAfter(":")?.trim().orEmpty(),
        goodHours = lines.firstOrNull { it.startsWith("Giờ hoàng đạo:") }?.substringAfter(":")?.trim().orEmpty(),
        shouldDo = lines.firstOrNull { it.startsWith("Nên:") }?.substringAfter(":")?.trim().orEmpty(),
        shouldAvoid = lines.firstOrNull { it.startsWith("Tránh:") }?.substringAfter(":")?.trim().orEmpty(),
    )
}

private fun buildNotificationPreview(notification: NotificationEntity): String {
    if (notification.type != "daily") return notification.description
    val summary = parseMorningSummary(notification.description)
    val rating = summary.canChi.substringAfter("-", "").trim()
    val goodHourCount = summary.goodHours.split(",").count { it.trim().isNotBlank() }
    val firstDo = summary.shouldDo.split(",").firstOrNull()?.trim().orEmpty()

    return buildString {
        if (summary.weather.isNotBlank()) append(summary.weather)
        if (rating.isNotBlank()) {
            if (isNotBlank()) append(". ")
            append("Ngày $rating")
        }
        if (goodHourCount > 0) {
            if (isNotBlank()) append(" - ")
            append("$goodHourCount giờ đẹp")
        }
        if (firstDo.isNotBlank()) {
            if (isNotBlank()) append(". ")
            append("Hợp: $firstDo")
        }
    }.ifBlank { notification.description }
}

private fun getNotificationTypeInfo(type: String): NotifTypeInfo {
    return when (type) {
        "daily" -> NotifTypeInfo(
            icon = Icons.Outlined.WbSunny,
            iconColor = Color(0xFF2E7D32),
            bgColor = Color(0xFFE8F5E9)
        )
        "holiday" -> NotifTypeInfo(
            icon = Icons.Outlined.Celebration,
            iconColor = Color(0xFFE65100),
            bgColor = Color(0xFFFFF3E0)
        )
        "ai" -> NotifTypeInfo(
            icon = Icons.Outlined.AutoAwesome,
            iconColor = Color(0xFF7B1FA2),
            bgColor = Color(0xFFF3E5F5)
        )
        "reminder" -> NotifTypeInfo(
            icon = Icons.Outlined.Notifications,
            iconColor = Color(0xFF1565C0),
            bgColor = Color(0xFFE3F2FD)
        )
        "good_day" -> NotifTypeInfo(
            icon = Icons.Outlined.EventAvailable,
            iconColor = Color(0xFFF57F17),
            bgColor = Color(0xFFFFF8E1)
        )
        "weather" -> NotifTypeInfo(
            icon = Icons.Outlined.WbSunny,
            iconColor = Color(0xFF0277BD),
            bgColor = Color(0xFFE1F5FE)
        )
        else -> NotifTypeInfo(
            icon = Icons.Outlined.SystemUpdate,
            iconColor = Color(0xFF616161),
            bgColor = Color(0xFFF5F5F5)
        )
    }
}

// ══════════════════════════════════════════
// NOTIFICATION DETAIL OVERLAY
// ══════════════════════════════════════════

@Composable
private fun NotificationDetailOverlay(
    notification: NotificationEntity,
    dateLabel: String,
    timeLabel: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val c = LichSoThemeColors.current
    val typeInfo = getNotificationTypeInfo(notification.type)
    val typeLabel = getNotificationTypeLabel(notification.type)

    // Scrim + content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .heightIn(max = 720.dp)
                .verticalScroll(rememberScrollState())
                .background(c.bg, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { /* consume clicks */ }
                .navigationBarsPadding()
                .padding(top = 12.dp, bottom = 24.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .background(c.outlineVariant, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Header: Icon + Type label + Close
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(typeInfo.bgColor, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            typeInfo.icon,
                            contentDescription = null,
                            tint = typeInfo.iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            typeLabel,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = typeInfo.iconColor,
                                letterSpacing = 0.3.sp
                            )
                        )
                        Text(
                            "$dateLabel · $timeLabel",
                            style = TextStyle(fontSize = 11.sp, color = c.textTertiary)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(c.surfaceContainer, CircleShape)
                        .clip(CircleShape)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Đóng",
                        tint = c.textTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider
            HorizontalDivider(
                color = c.outlineVariant,
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (notification.type == "daily") {
                MorningSummaryDetail(
                    title = notification.title,
                    summary = parseMorningSummary(notification.description),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            } else {
                // Title
                Text(
                    notification.title,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = c.textPrimary,
                        lineHeight = 26.sp
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                // Description
                if (notification.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        notification.description,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = c.textSecondary,
                            lineHeight = 22.sp
                        ),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Delete button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(c.surfaceContainer, RoundedCornerShape(14.dp))
                        .border(1.dp, c.outlineVariant, RoundedCornerShape(14.dp))
                        .clickable(onClick = onDelete)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Outlined.Delete, null, tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                        Text(
                            "Xoá",
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                        )
                    }
                }

                // Close button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(c.primary, RoundedCornerShape(14.dp))
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Đóng",
                        style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }
            }
        }
    }
}

@Composable
private fun MorningSummaryDetail(
    title: String,
    summary: MorningSummary,
    modifier: Modifier = Modifier
) {
    val c = LichSoThemeColors.current
    val rating = summary.canChi.substringAfter("-", "").trim()
    val canChiName = summary.canChi.substringBefore("-").trim()

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFB71C1C), Color(0xFFE65100), Color(0xFFFFB74D))
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Text(
                    title,
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        lineHeight = 28.sp
                    )
                )
                if (summary.weather.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        summary.weather,
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.95f),
                            lineHeight = 21.sp
                        )
                    )
                }
                if (summary.advice.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        summary.advice,
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.86f),
                            lineHeight = 19.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (summary.date.isNotBlank()) {
                HighlightChip(
                    label = "Ngày",
                    value = summary.date,
                    color = Color(0xFF8B1A1A),
                    modifier = Modifier.weight(1f)
                )
            }
            if (rating.isNotBlank()) {
                HighlightChip(
                    label = "Đánh giá",
                    value = rating,
                    color = if (rating.contains("Xấu", ignoreCase = true)) Color(0xFFD84315) else Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (canChiName.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            HighlightChip(
                label = "Can Chi",
                value = canChiName,
                color = Color(0xFF6D4C41),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (summary.goodHours.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            InsightBlock(
                icon = Icons.Outlined.Schedule,
                title = "Giờ đẹp để tranh thủ",
                body = summary.goodHours,
                tint = Color(0xFFB8860B)
            )
        }

        if (summary.shouldDo.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            InsightBlock(
                icon = Icons.Outlined.CheckCircle,
                title = "Nên làm hôm nay",
                body = summary.shouldDo,
                tint = Color(0xFF2E7D32)
            )
        }

        if (summary.shouldAvoid.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            InsightBlock(
                icon = Icons.Outlined.WarningAmber,
                title = "Nên tránh",
                body = summary.shouldAvoid,
                tint = Color(0xFFC62828)
            )
        }
    }
}

@Composable
private fun HighlightChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.24f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text(
            label,
            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            value,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = LichSoThemeColors.current.textPrimary,
                lineHeight = 18.sp
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InsightBlock(
    icon: ImageVector,
    title: String,
    body: String,
    tint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(tint.copy(alpha = 0.08f))
            .border(1.dp, tint.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(tint.copy(alpha = 0.14f), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(19.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tint)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                body,
                style = TextStyle(
                    fontSize = 13.sp,
                    color = LichSoThemeColors.current.textSecondary,
                    lineHeight = 19.sp
                )
            )
        }
    }
}

private fun getNotificationTypeLabel(type: String): String = when (type) {
    "daily" -> "Thông báo hàng ngày"
    "holiday" -> "Ngày lễ / Sự kiện"
    "ai" -> "Trợ lý AI"
    "reminder" -> "Nhắc nhở"
    "good_day" -> "Ngày tốt / Giờ hoàng đạo"
    "weather" -> "Thời tiết buổi sáng"
    else -> "Thông báo hệ thống"
}
