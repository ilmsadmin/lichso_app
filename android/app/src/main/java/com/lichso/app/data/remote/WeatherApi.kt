package com.lichso.app.data.remote

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lichso.app.domain.model.LocationInfo
import com.lichso.app.domain.model.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API client cho Open-Meteo (miễn phí, không cần API key)
 * https://open-meteo.com/
 */
@Singleton
class WeatherApi @Inject constructor() {

    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Lấy thời tiết hiện tại từ Open-Meteo API
     */
    suspend fun getCurrentWeather(location: LocationInfo): Result<WeatherInfo> =
        withContext(Dispatchers.IO) {
            try {
                val url = buildString {
                    append("https://api.open-meteo.com/v1/forecast?")
                    append("latitude=${location.latitude}")
                    append("&longitude=${location.longitude}")
                    append("&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code,wind_speed_10m,uv_index")
                    append("&timezone=Asia/Ho_Chi_Minh")
                }

                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (!response.isSuccessful || body.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("API error: ${response.code}"))
                }

                val weatherResponse = gson.fromJson(body, OpenMeteoResponse::class.java)
                val current = weatherResponse.current
                    ?: return@withContext Result.failure(Exception("No current weather data"))

                val isDay = (current.isDay ?: 1) == 1
                val (description, icon) = WeatherInfo.fromWeatherCode(current.weatherCode ?: 0, isDay)

                val weatherInfo = WeatherInfo(
                    temperature = current.temperature ?: 0.0,
                    weatherCode = current.weatherCode ?: 0,
                    humidity = current.humidity ?: 0,
                    windSpeed = current.windSpeed ?: 0.0,
                    cityName = location.cityName,
                    description = description,
                    icon = icon,
                    isDay = isDay,
                    feelsLike = current.apparentTemperature,
                    uvIndex = current.uvIndex
                )

                Result.success(weatherInfo)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Geocoding: tìm tên thành phố từ tọa độ (dùng Open-Meteo geocoding API)
     */
    suspend fun reverseGeocode(lat: Double, lon: Double): String =
        withContext(Dispatchers.IO) {
            try {
                val url = "https://geocoding-api.open-meteo.com/v1/search?name=&latitude=$lat&longitude=$lon&count=1&language=vi"
                // Fallback: xác định thành phố gần nhất từ danh sách VN
                findNearestVietnameseCity(lat, lon)
            } catch (e: Exception) {
                "Việt Nam"
            }
        }

    /**
     * Tìm thành phố Việt Nam gần nhất từ tọa độ
     */
    private fun findNearestVietnameseCity(lat: Double, lon: Double): String {
        val cities = listOf(
            Triple("Hà Nội", 21.0285, 105.8542),
            Triple("TP.HCM", 10.8231, 106.6297),
            Triple("Đà Nẵng", 16.0544, 108.2022),
            Triple("Hải Phòng", 20.8449, 106.6881),
            Triple("Cần Thơ", 10.0452, 105.7469),
            Triple("Huế", 16.4637, 107.5909),
            Triple("Nha Trang", 12.2388, 109.1967),
            Triple("Đà Lạt", 11.9404, 108.4583),
            Triple("Vũng Tàu", 10.4114, 107.1362),
            Triple("Quy Nhơn", 13.7830, 109.2197),
            Triple("Vinh", 18.6796, 105.6813),
            Triple("Thanh Hóa", 19.8067, 105.7852),
            Triple("Thái Nguyên", 21.5928, 105.8442),
            Triple("Nam Định", 20.4388, 106.1621),
            Triple("Buôn Ma Thuột", 12.6680, 108.0378),
            Triple("Biên Hòa", 10.9574, 106.8426),
            Triple("Hạ Long", 20.9711, 107.0452),
            Triple("Pleiku", 13.9833, 108.0),
            Triple("Rạch Giá", 10.0125, 105.0809),
            Triple("Long Xuyên", 10.3861, 105.4350),
            Triple("Phan Thiết", 10.9289, 108.1002),
            Triple("Sapa", 22.3363, 103.8438),
            Triple("Phú Quốc", 10.2270, 103.9667),
        )

        return cities.minByOrNull { (_, cityLat, cityLon) ->
            val dLat = lat - cityLat
            val dLon = lon - cityLon
            dLat * dLat + dLon * dLon
        }?.first ?: "Việt Nam"
    }
}

// ── Open-Meteo JSON Response Models ──

data class OpenMeteoResponse(
    val current: OpenMeteoCurrent?
)

data class OpenMeteoCurrent(
    @SerializedName("temperature_2m")
    val temperature: Double?,
    @SerializedName("relative_humidity_2m")
    val humidity: Int?,
    @SerializedName("apparent_temperature")
    val apparentTemperature: Double?,
    @SerializedName("is_day")
    val isDay: Int?,
    @SerializedName("weather_code")
    val weatherCode: Int?,
    @SerializedName("wind_speed_10m")
    val windSpeed: Double?,
    @SerializedName("uv_index")
    val uvIndex: Double?
)
