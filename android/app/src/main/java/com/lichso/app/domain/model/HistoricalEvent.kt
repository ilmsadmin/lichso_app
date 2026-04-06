package com.lichso.app.domain.model

/**
 * Sự kiện lịch sử "Ngày này năm xưa"
 */
data class HistoricalEvent(
    val year: Int,
    val title: String,
    val description: String,
    val category: HistoryCategory,
    val importance: EventImportance = EventImportance.MINOR,
    val hasImage: Boolean = false
)

enum class HistoryCategory(val label: String, val emoji: String) {
    VIETNAM("Lịch sử Việt Nam", "🇻🇳"),
    WORLD("Thế giới", "🌍"),
    CULTURE("Văn hóa", "🎨"),
    SCIENCE("Khoa học", "🔬")
}

enum class EventImportance {
    MAJOR,   // Sự kiện quan trọng — dot đỏ/xanh lớn
    MINOR    // Sự kiện phụ — dot nhỏ viền
}
