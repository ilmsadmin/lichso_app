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

    fun deleteAllNotifications() {
        viewModelScope.launch {
            notificationDao.deleteAll()
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
}
