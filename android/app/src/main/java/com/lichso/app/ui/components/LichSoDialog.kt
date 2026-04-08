package com.lichso.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lichso.app.ui.theme.LichSoColors
import com.lichso.app.ui.theme.LichSoThemeColors

/**
 * Styled AlertDialog matching the LichSo design system.
 *
 * Usage patterns:
 *  - Normal:    confirmColor = c.primary   (red)
 *  - Danger:    confirmColor = Color(0xFFC62828), isDanger = true
 *  - Positive:  confirmColor = Color(0xFF2E7D32)
 */
@Composable
fun LichSoDialog(
    onDismiss: () -> Unit,
    title: String,
    icon: ImageVector? = null,
    iconTint: Color = Color.Unspecified,
    iconBgColor: Color = Color.Unspecified,
    confirmText: String = "Xác nhận",
    dismissText: String = "Hủy",
    confirmColor: Color = Color.Unspecified,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = LichSoThemeColors.current
    val resolvedIconTint = if (iconTint == Color.Unspecified) c.primary else iconTint
    val resolvedIconBg = if (iconBgColor == Color.Unspecified) {
        if (c.isDark) c.primaryContainer else Color(0xFFFFEBEE)
    } else iconBgColor
    val resolvedConfirmColor = if (confirmColor == Color.Unspecified) {
        if (c.isDark) Color(0xFF7F1D1D) else Color(0xFFB71C1C)
    } else confirmColor

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
        icon = icon?.let {
            {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(resolvedIconBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(it, null, tint = resolvedIconTint, modifier = Modifier.size(24.dp))
                }
            }
        },
        title = {
            Text(
                title,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = c.textPrimary
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = confirmEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = resolvedConfirmColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(confirmText, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(dismissText, color = c.textSecondary)
            }
        }
    )
}

/**
 * Simple confirmation dialog (e.g. delete, sign out).
 */
@Composable
fun LichSoConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    icon: ImageVector? = null,
    iconTint: Color = Color.Unspecified,
    iconBgColor: Color = Color.Unspecified,
    confirmText: String = "Xác nhận",
    dismissText: String = "Hủy",
    confirmColor: Color = Color.Unspecified,
) {
    val c = LichSoThemeColors.current
    val resolvedIconTint2 = if (iconTint == Color.Unspecified) c.primary else iconTint
    val resolvedIconBg2 = if (iconBgColor == Color.Unspecified) {
        if (c.isDark) c.primaryContainer else Color(0xFFFFEBEE)
    } else iconBgColor
    val resolvedConfirmColor2 = if (confirmColor == Color.Unspecified) {
        if (c.isDark) Color(0xFF7F1D1D) else Color(0xFFC62828)
    } else confirmColor

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
        icon = icon?.let {
            {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(resolvedIconBg2, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(it, null, tint = resolvedIconTint2, modifier = Modifier.size(24.dp))
                }
            }
        },
        title = {
            Text(
                title,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = c.textPrimary
                )
            )
        },
        text = {
            Text(
                message,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = c.textSecondary,
                    lineHeight = 20.sp,
                )
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = resolvedConfirmColor2),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(confirmText, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(dismissText, color = c.textSecondary)
            }
        }
    )
}
