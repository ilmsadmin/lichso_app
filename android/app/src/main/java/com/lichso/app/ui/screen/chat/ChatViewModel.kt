package com.lichso.app.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.local.AiMemoryStore
import com.lichso.app.data.local.dao.ChatMessageDao
import com.lichso.app.data.local.entity.ChatMessageEntity
import com.lichso.app.data.remote.ChatMessage
import com.lichso.app.data.remote.OpenRouterApi
import com.lichso.app.domain.DayInfoProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessageEntity> = emptyList(),
    val isTyping: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatMessageDao: ChatMessageDao,
    private val dayInfoProvider: DayInfoProvider,
    private val openRouterApi: OpenRouterApi,
    private val aiMemoryStore: AiMemoryStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            chatMessageDao.getAllMessages().collect { messages ->
                _uiState.update { it.copy(messages = messages) }
                if (messages.isEmpty()) {
                    val greeting = buildGreetingMessage()
                    chatMessageDao.insert(
                        ChatMessageEntity(
                            content = greeting,
                            isUser = false
                        )
                    )
                }
            }
        }
    }

    private suspend fun buildGreetingMessage(): String {
        val userName = aiMemoryStore.getUserName()
        val aiName = aiMemoryStore.getAiName()
        val i = ChatIcons

        val selfName = aiName ?: "Lịch Số AI"

        return if (userName != null) {
            "Chào $userName! Tôi là $selfName — trợ lý phong thủy & lịch vạn niên của bạn.\n\n" +
                    "Rất vui được gặp lại! Bạn muốn hỏi gì hôm nay?"
        } else {
            "Xin chào! Tôi là $selfName — trợ lý phong thủy & lịch vạn niên thông minh.\n\n" +
                    "Tôi được hỗ trợ bởi AI tiên tiến, có thể giúp bạn:\n" +
                    "${i.ARROW} Xem ngày tốt/xấu cho công việc\n" +
                    "${i.ARROW} Tra cứu can chi, tiết khí, giờ hoàng đạo\n" +
                    "${i.ARROW} Gợi ý ngày cưới, khai trương, động thổ\n" +
                    "${i.ARROW} Giải đáp phong thủy chi tiết\n\n" +
                    "Bạn có thể cho tôi biết tên bạn, và tôi sẽ ghi nhớ nhé!"
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            chatMessageDao.insert(ChatMessageEntity(content = text.trim(), isUser = true))
            _uiState.update { it.copy(isTyping = true) }

            val contextInfo = buildTodayContext()
            val memoryContext = aiMemoryStore.getMemoryContext()

            val history = _uiState.value.messages.takeLast(10).map { msg ->
                ChatMessage(
                    role = if (msg.isUser) "user" else "assistant",
                    content = msg.content
                )
            }

            val result = openRouterApi.chat(
                userMessage = text.trim(),
                contextInfo = contextInfo,
                memoryContext = memoryContext,
                history = history
            )

            val response = result.getOrElse { error ->
                "${ChatIcons.WARNING} Không thể kết nối AI. Đang dùng trả lời cục bộ.\n\n" + generateLocalResponse(text.trim())
            }

            chatMessageDao.insert(ChatMessageEntity(content = response, isUser = false))
            _uiState.update { it.copy(isTyping = false) }

            // Parse and save memory from the conversation
            aiMemoryStore.parseAndSaveFromAiResponse(text.trim(), response)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatMessageDao.clearAll()
        }
    }

    fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    private fun buildTodayContext(): String {
        return try {
            val today = LocalDate.now()
            val info = dayInfoProvider.getDayInfo(today.dayOfMonth, today.monthValue, today.year)
            val sb = StringBuilder()
            sb.appendLine("Ngày: ${info.solar.dd}/${info.solar.mm}/${info.solar.yy} (${info.dayOfWeek})")
            sb.appendLine("Âm lịch: ${info.lunar.day}/${info.lunar.month} năm ${info.yearCanChi}")
            sb.appendLine("Can chi ngày: ${info.dayCanChi}")
            sb.appendLine("Can chi tháng: ${info.monthCanChi}")
            sb.appendLine("Can chi năm: ${info.yearCanChi}")
            sb.appendLine("Ngày xấu: ${info.activities.isXauDay}")
            if (info.activities.isNguyetKy) sb.appendLine("Nguyệt Kỵ: Có")
            if (info.activities.isTamNuong) sb.appendLine("Tam Nương: Có")
            sb.appendLine("Nên: ${info.activities.nenLam.joinToString(", ")}")
            sb.appendLine("Không nên: ${info.activities.khongNen.joinToString(", ")}")
            sb.appendLine("Giờ hoàng đạo: ${info.gioHoangDao.joinToString(", ") { "${it.name} (${it.time})" }}")
            sb.appendLine("Hướng Thần Tài: ${info.huong.thanTai}")
            sb.appendLine("Hướng Hỷ Thần: ${info.huong.hyThan}")
            sb.appendLine("Hướng Hắc Thần: ${info.huong.hungThan}")
            if (info.tietKhi.currentName != null) sb.appendLine("Tiết khí: ${info.tietKhi.currentName}")
            sb.appendLine("Pha trăng: ${info.moonPhase.name} (ngày ${info.moonPhase.age})")
            if (info.lunarHoliday != null) sb.appendLine("Ngày lễ âm: ${info.lunarHoliday}")
            if (info.solarHoliday != null) sb.appendLine("Ngày lễ dương: ${info.solarHoliday}")
            sb.toString().trim()
        } catch (e: Exception) {
            ""
        }
    }

    private fun generateLocalResponse(query: String): String {
        val today = LocalDate.now()
        val info = dayInfoProvider.getDayInfo(today.dayOfMonth, today.monthValue, today.year)
        val q = query.lowercase()
        val i = ChatIcons

        return when {
            q.contains("hôm nay") || q.contains("ngày tốt") || q.contains("hom nay") -> {
                val sb = StringBuilder()
                sb.appendLine("${i.CALENDAR} Hôm nay — ${info.solar.dd}/${info.solar.mm}/${info.solar.yy}")
                sb.appendLine("${i.LUNAR} Âm lịch: ${info.lunar.day}/${info.lunar.month} ${info.yearCanChi}")
                sb.appendLine("${i.CANCHI} Ngày: ${info.dayCanChi} · ${info.dayOfWeek}")
                sb.appendLine()
                if (info.activities.isXauDay) {
                    sb.appendLine("${i.WARNING} Hôm nay là ngày xấu.")
                }
                sb.appendLine("${i.CHECK} Nên: ${info.activities.nenLam.take(4).joinToString(", ")}")
                sb.appendLine("${i.CROSS} Không nên: ${info.activities.khongNen.take(4).joinToString(", ")}")
                sb.appendLine()
                sb.appendLine("${i.CLOCK} Giờ hoàng đạo: ${info.gioHoangDao.take(3).joinToString(", ") { "${it.name} (${it.time})" }}")
                sb.appendLine("${i.COMPASS} Hướng Thần Tài: ${info.huong.thanTai}")
                sb.toString().trim()
            }
            q.contains("giờ hoàng đạo") || q.contains("gio hoang dao") || q.contains("giờ tốt") -> {
                val sb = StringBuilder()
                sb.appendLine("${i.CLOCK} GIỜ HOÀNG ĐẠO — ${info.dayOfWeek} ${info.solar.dd}/${info.solar.mm}")
                sb.appendLine("${i.CANCHI} Ngày: ${info.dayCanChi}")
                sb.appendLine()
                info.gioHoangDao.forEach { sb.appendLine("   ${i.SPARKLE} ${it.name} (${it.time})") }
                sb.toString().trim()
            }
            q.contains("hướng") || q.contains("huong") || q.contains("xuất hành") -> {
                val sb = StringBuilder()
                sb.appendLine("${i.COMPASS} HƯỚNG TỐT — ${info.dayOfWeek} ${info.solar.dd}/${info.solar.mm}")
                sb.appendLine()
                sb.appendLine("${i.FORTUNE} Thần Tài: ${info.huong.thanTai}")
                sb.appendLine("${i.JOY} Hỷ Thần: ${info.huong.hyThan}")
                sb.appendLine("${i.WARNING} Tránh: ${info.huong.hungThan}")
                sb.toString().trim()
            }
            q.contains("can chi") -> {
                val sb = StringBuilder()
                sb.appendLine("${i.CANCHI} CAN CHI HÔM NAY")
                sb.appendLine("${i.STAR} Năm: ${info.yearCanChi}")
                sb.appendLine("${i.CALENDAR} Tháng: ${info.monthCanChi}")
                sb.appendLine("${i.INFO} Ngày: ${info.dayCanChi}")
                sb.toString().trim()
            }
            else -> {
                val sb = StringBuilder()
                sb.appendLine("${i.CALENDAR} ${info.solar.dd}/${info.solar.mm}/${info.solar.yy} · ${info.dayCanChi}")
                sb.appendLine("${i.LUNAR} Âm: ${info.lunar.day}/${info.lunar.month} ${info.yearCanChi}")
                sb.appendLine()
                sb.appendLine("Bạn có thể hỏi về: ngày tốt, giờ hoàng đạo, hướng xuất hành, can chi, cưới hỏi, khai trương...")
                sb.toString().trim()
            }
        }
    }
}
