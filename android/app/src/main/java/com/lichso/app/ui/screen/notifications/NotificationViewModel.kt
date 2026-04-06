package com.lichso.app.ui.screen.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.local.dao.NotificationDao
import com.lichso.app.data.local.entity.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class NotificationUiState(
    val notifications: List<NotificationEntity> = emptyList(),
    val unreadCount: Int = 0
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationDao: NotificationDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            notificationDao.getAllNotifications().collect { list ->
                _uiState.update { it.copy(notifications = list) }
            }
        }
        viewModelScope.launch {
            notificationDao.getUnreadCount().collect { count ->
                _uiState.update { it.copy(unreadCount = count) }
            }
        }
        // Seed sample notifications if empty
        viewModelScope.launch {
            notificationDao.getAllNotifications().first().let { list ->
                if (list.isEmpty()) seedSampleNotifications()
            }
        }
    }

    fun markAsRead(notification: NotificationEntity) {
        viewModelScope.launch {
            notificationDao.markAsRead(notification.id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationDao.markAllAsRead()
        }
    }

    fun deleteNotification(notification: NotificationEntity) {
        viewModelScope.launch {
            notificationDao.delete(notification)
        }
    }

    fun getDateLabel(timestamp: Long): String {
        val cal = Calendar.getInstance()
        val todayCal = Calendar.getInstance()
        cal.timeInMillis = timestamp

        return when {
            isSameDay(cal, todayCal) -> {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                "Hôm nay · ${sdf.format(Date(timestamp))}"
            }
            isSameDay(cal, todayCal.apply { add(Calendar.DAY_OF_YEAR, -1) }) -> {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                "Hôm qua · ${sdf.format(Date(timestamp))}"
            }
            else -> {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.format(Date(timestamp))
            }
        }
    }

    fun getTimeLabel(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private suspend fun seedSampleNotifications() {
        val now = System.currentTimeMillis()
        val hourMs = 3600_000L
        val dayMs = 86400_000L

        val samples = listOf(
            NotificationEntity(
                title = "Chào buổi sáng! Hôm nay Chủ Nhật, 05/04",
                description = "Mùng 8/3 Âm — Ngày Hoàng Đạo ✦ Tốt cho khai trương, xuất hành. Trời nắng 32°C tại Hà Nội.",
                type = "daily",
                isRead = false,
                createdAt = now - 2 * hourMs
            ),
            NotificationEntity(
                title = "🏯 Giỗ Tổ Hùng Vương sắp đến!",
                description = "Ngày 10/3 Âm lịch (10/04/2026) — còn 5 ngày nữa. Đây là ngày nghỉ lễ chính thức.",
                type = "holiday",
                isRead = false,
                createdAt = now - 2 * hourMs - 60_000
            ),
            NotificationEntity(
                title = "AI Tử Vi: Phân tích tuần mới",
                description = "Tuần 06-12/04 có 3 ngày Hoàng Đạo. Xem chi tiết để biết ngày nào phù hợp nhất cho tuổi Canh Thân.",
                type = "ai",
                isRead = false,
                createdAt = now - 3 * hourMs
            ),
            NotificationEntity(
                title = "Chào buổi sáng! Thứ Bảy, 04/04",
                description = "Mùng 7/3 Âm — Ngày Hoàng Đạo. Hướng xuất hành tốt: Đông Nam.",
                type = "daily",
                isRead = true,
                createdAt = now - dayMs - 2 * hourMs
            ),
            NotificationEntity(
                title = "Ngày tốt cho cưới hỏi tuần tới",
                description = "Ngày 08/04 (Quý Mùi) rất tốt cho cưới hỏi, khai trương. Xem chi tiết →",
                type = "good_day",
                isRead = true,
                createdAt = now - dayMs + 4 * hourMs
            ),
            NotificationEntity(
                title = "Nhắc nhở: Kỷ niệm ngày cưới",
                description = "Ngày mai 01/04 là ngày kỷ niệm ngày cưới. Đừng quên chuẩn bị nhé!",
                type = "reminder",
                isRead = true,
                createdAt = now - 2 * dayMs
            ),
            NotificationEntity(
                title = "Cập nhật ứng dụng v2.1.0",
                description = "Phiên bản mới với tính năng AI Tử Vi cải tiến và giao diện mới.",
                type = "system",
                isRead = true,
                createdAt = now - 2 * dayMs - 10 * hourMs
            ),
            NotificationEntity(
                title = "Chào buổi sáng! Thứ Sáu, 03/04",
                description = "Mùng 6/3 Âm — Ngày bình thường. Trời mưa nhỏ 28°C.",
                type = "daily",
                isRead = true,
                createdAt = now - 2 * dayMs - 2 * hourMs
            )
        )
        samples.forEach { notificationDao.insert(it) }
    }
}
