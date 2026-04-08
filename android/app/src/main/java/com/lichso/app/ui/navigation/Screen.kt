package com.lichso.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val iconOutlined: ImageVector
) {
    data object Home : Screen("home", "Lịch", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    data object Tasks : Screen("tasks", "Công việc", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle)
    data object Chat : Screen("chat", "AI Chat", Icons.Filled.SmartToy, Icons.Outlined.SmartToy)
    data object Templates : Screen("templates", "Mẫu", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    data object Settings : Screen("settings", "Cài đặt", Icons.Filled.Settings, Icons.Outlined.Settings)

    companion object {
        val bottomNavItems = listOf(Home, Tasks, Chat, Templates)
    }
}
