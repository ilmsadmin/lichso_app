package com.lichso.app.domain.model

/**
 * Thông tin thời tiết hiện tại
 */
data class WeatherInfo(
    val temperature: Double,        // Nhiệt độ (°C)
    val weatherCode: Int,           // WMO weather code
    val humidity: Int,              // Độ ẩm (%)
    val windSpeed: Double,          // Tốc độ gió (km/h)
    val cityName: String,           // Tên thành phố
    val description: String,        // Mô tả thời tiết
    val icon: String,               // Emoji icon
    val isDay: Boolean = true,      // Ban ngày hay ban đêm
    val feelsLike: Double? = null,  // Cảm giác nhiệt độ
    val uvIndex: Double? = null,    // Chỉ số UV
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Map WMO Weather Code → mô tả tiếng Việt + emoji
         */
        fun fromWeatherCode(code: Int, isDay: Boolean): Pair<String, String> {
            return when (code) {
                0 -> "Trời quang" to if (isDay) "☀️" else "🌙"
                1 -> "Quang đãng" to if (isDay) "🌤️" else "🌙"
                2 -> "Có mây" to "⛅"
                3 -> "Nhiều mây" to "☁️"
                45, 48 -> "Sương mù" to "🌫️"
                51, 53, 55 -> "Mưa phùn" to "🌦️"
                56, 57 -> "Mưa phùn đóng băng" to "🌧️"
                61, 63, 65 -> "Mưa" to "🌧️"
                66, 67 -> "Mưa đóng băng" to "🌧️"
                71, 73, 75 -> "Tuyết rơi" to "🌨️"
                77 -> "Mưa đá nhỏ" to "🌨️"
                80, 81, 82 -> "Mưa rào" to "🌧️"
                85, 86 -> "Tuyết rào" to "🌨️"
                95 -> "Dông" to "⛈️"
                96, 99 -> "Dông kèm mưa đá" to "⛈️"
                else -> "Không rõ" to "🌡️"
            }
        }
    }
}

/**
 * Vị trí địa lý
 */
data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val cityName: String
)
