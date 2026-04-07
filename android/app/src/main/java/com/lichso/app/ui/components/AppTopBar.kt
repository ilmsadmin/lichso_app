package com.lichso.app.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lichso.app.ui.theme.LichSoThemeColors

// ══════════════════════════════════════════════════════════
// Shared App Top Bar — Unified header for all screens
// Pattern from HomeScreen's RedHeader
// ══════════════════════════════════════════════════════════

/**
 * A unified top bar with gradient background.
 *
 * @param title Main title text (shown in white, bold)
 * @param subtitle Optional subtitle text (shown in white with 65% alpha)
 * @param onBackClick If provided, shows a back arrow button on the left
 * @param leadingIcon Custom leading icon (overrides back arrow). Used with onBackClick for the click handler.
 * @param gradientColors Custom gradient colors. Defaults to the app's red gradient.
 * @param actions Composable slot for right-side action buttons
 * @param bottomContent Optional composable content below the title row
 */
@Composable
fun AppTopBar(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null,
    gradientColors: List<Color>? = null,
    actions: @Composable RowScope.() -> Unit = {},
    bottomContent: @Composable (ColumnScope.() -> Unit)? = null
) {
    val c = LichSoThemeColors.current
    val colors = gradientColors ?: listOf(c.primary, Color(0xFFD32F2F), c.deepRed)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = colors,
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column {
            // Top row: back button + title + actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (onBackClick != null) {
                        HeaderIconButton(
                            icon = leadingIcon ?: Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (leadingIcon != null) title else "Quay lại",
                            onClick = onBackClick
                        )
                    }
                    Column {
                        Text(
                            title,
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        if (subtitle != null) {
                            Text(
                                subtitle,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions
                )
            }
            // Optional bottom content
            if (bottomContent != null) {
                Spacer(modifier = Modifier.height(12.dp))
                bottomContent()
            }
        }
    }
}

/**
 * Standard 40dp circle icon button used in headers.
 * White icon on semi-transparent white background.
 */
@Composable
fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
) {
    Box(
        modifier = modifier
            .size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        // Clickable circle background (clipped separately so badge is not clipped)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.White.copy(alpha = 0.12f), CircleShape)
                .clip(CircleShape)
                .clickable(onClick = onClick)
        )
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(if (badgeCount > 9) 18.dp else 16.dp)
                    .background(Color(0xFFE53935), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (badgeCount > 99) "99+" else "$badgeCount",
                    style = TextStyle(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}
