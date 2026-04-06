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
    iconTint: Color = Color(0xFFB71C1C),
    iconBgColor: Color = Color(0xFFFFEBEE),
    confirmText: String = "Xác nhận",
    dismissText: String = "Hủy",
    confirmColor: Color = Color(0xFFB71C1C),
    onConfirm: () -> Unit,
    confirmEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = LichSoThemeColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
        icon = icon?.let {
            {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(iconBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(it, null, tint = iconTint, modifier = Modifier.size(24.dp))
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
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor),
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
    iconTint: Color = Color(0xFFC62828),
    iconBgColor: Color = Color(0xFFFFEBEE),
    confirmText: String = "Xác nhận",
    dismissText: String = "Hủy",
    confirmColor: Color = Color(0xFFC62828),
) {
    val c = LichSoThemeColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
        icon = icon?.let {
            {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(iconBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(it, null, tint = iconTint, modifier = Modifier.size(24.dp))
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
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor),
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
