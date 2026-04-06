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
import java.util.*

@Composable
fun NotificationScreen(
    onBackClick: () -> Unit = {},
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val c = LichSoThemeColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Group notifications by date
    val grouped = remember(state.notifications) {
        state.notifications.groupBy { notif ->
            viewModel.getDateLabel(notif.createdAt)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        // ═══ TOP BAR ═══
        TopBar(
            unreadCount = state.unreadCount,
            onBackClick = onBackClick,
            onMarkAllRead = { viewModel.markAllAsRead() }
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

                    // Notification cards
                    items(
                        items = notifications,
                        key = { it.id }
                    ) { notification ->
                        NotificationCard(
                            notification = notification,
                            timeLabel = viewModel.getTimeLabel(notification.createdAt),
                            onClick = {
                                if (!notification.isRead) {
                                    viewModel.markAsRead(notification)
                                }
                            },
                            onDismiss = { viewModel.deleteNotification(notification) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun TopBar(
    unreadCount: Int,
    onBackClick: () -> Unit,
    onMarkAllRead: () -> Unit
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
            Text(
                notification.title,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                    lineHeight = 18.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (notification.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    notification.description,
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
        else -> NotifTypeInfo(
            icon = Icons.Outlined.SystemUpdate,
            iconColor = Color(0xFF616161),
            bgColor = Color(0xFFF5F5F5)
        )
    }
}
