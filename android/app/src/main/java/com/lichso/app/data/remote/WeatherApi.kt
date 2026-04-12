package com.lichso.app.data.remote

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.lichso.app.domain.model.LocationInfo
import com.lichso.app.domain.model.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "WeatherApi"

/**
 * API client cho thời tiết.
 * Ưu tiên Open-Meteo (miễn phí, không cần API key).
 * Fallback sang wttr.in nếu Open-Meteo gặp lỗi.
 */
@Singleton
class WeatherApi @Inject constructor(
    private val client: OkHttpClient
) {

    private val gson = Gson()

    /**
     * Lấy thời tiết hiện tại — thử Open-Meteo trước, fallback sang wttr.in
     */
    suspend fun getCurrentWeather(location: LocationInfo): Result<WeatherInfo> =
        withContext(Dispatchers.IO) {
            // Thử Open-Meteo trước
            val openMeteoResult = fetchFromOpenMeteo(location)
            if (openMeteoResult.isSuccess) {
                return@withContext openMeteoResult
            }
            Log.w(TAG, "Open-Meteo failed: ${openMeteoResult.exceptionOrNull()?.message}, trying wttr.in fallback")

            // Fallback: wttr.in
            val wttrResult = fetchFromWttrIn(location)
            if (wttrResult.isSuccess) {
                return@withContext wttrResult
            }
            Log.e(TAG, "wttr.in also failed: ${wttrResult.exceptionOrNull()?.message}")

            // Cả hai đều fail
            Result.failure(
                Exception("Không thể lấy thời tiết. Open-Meteo: ${openMeteoResult.exceptionOrNull()?.message}, wttr.in: ${wttrResult.exceptionOrNull()?.message}")
            )
        }

    // ── Open-Meteo ──

    private fun fetchFromOpenMeteo(location: LocationInfo): Result<WeatherInfo> {
        return try {
            // Validate coordinates to prevent URL injection
            val lat = location.latitude.coerceIn(-90.0, 90.0)
            val lon = location.longitude.coerceIn(-180.0, 180.0)

            val url = buildString {
                append("https://api.open-meteo.com/v1/forecast?")
                append("latitude=$lat")
                append("&longitude=$lon")
                append("&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code,wind_speed_10m,uv_index")
                append("&timezone=Asia/Ho_Chi_Minh")
            }

            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            response.use { resp ->
                val body = resp.body?.string()

                if (!resp.isSuccessful || body.isNullOrBlank()) {
                    return Result.failure(Exception("HTTP ${resp.code}"))
                }

                val weatherResponse = gson.fromJson(body, OpenMeteoResponse::class.java)
                val current = weatherResponse.current
                    ?: return Result.failure(Exception("No current data"))

                val isDay = (current.isDay ?: 1) == 1
                val (description, icon) = WeatherInfo.fromWeatherCode(current.weatherCode ?: 0, isDay)

                Result.success(
                    WeatherInfo(
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
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── wttr.in fallback ──

    private fun fetchFromWttrIn(location: LocationInfo): Result<WeatherInfo> {
        return try {
            val url = "https://wttr.in/${location.latitude},${location.longitude}?format=j1"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "LichSo-App/1.0")
                .get()
                .build()

            val response = client.newCall(request).execute()
            response.use { resp ->
                val body = resp.body?.string()

                if (!resp.isSuccessful || body.isNullOrBlank()) {
                    return Result.failure(Exception("wttr.in HTTP ${resp.code}"))
                }

                val root = JsonParser.parseString(body).asJsonObject
                val currentArray = root.getAsJsonArray("current_condition")
                if (currentArray == null || currentArray.size() == 0) {
                    return Result.failure(Exception("wttr.in: no current_condition"))
                }
                val current = currentArray[0].asJsonObject

                val tempC = current.get("temp_C")?.asDouble ?: 0.0
                val humidity = current.get("humidity")?.asInt ?: 0
                val windSpeedKmph = current.get("windspeedKmph")?.asDouble ?: 0.0
                val feelsLike = current.get("FeelsLikeC")?.asDouble
                val uvIndex = current.get("uvIndex")?.asDouble
                val wttrWeatherCode = current.get("weatherCode")?.asInt ?: 0

                // wttr.in dùng WWO weather codes, map sang WMO
                val wmoCode = mapWwoToWmo(wttrWeatherCode)
                val isDay = isDaytime()
                val (description, icon) = WeatherInfo.fromWeatherCode(wmoCode, isDay)

                Result.success(
                    WeatherInfo(
                        temperature = tempC,
                        weatherCode = wmoCode,
                        humidity = humidity,
                        windSpeed = windSpeedKmph,
                        cityName = location.cityName,
                        description = description,
                        icon = icon,
                        isDay = isDay,
                        feelsLike = feelsLike,
                        uvIndex = uvIndex
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Map WWO (World Weather Online) weather code → WMO code
     */
    private fun mapWwoToWmo(wwoCode: Int): Int {
        return when (wwoCode) {
            113 -> 0        // Clear/Sunny
            116 -> 2        // Partly cloudy
            119 -> 3        // Cloudy
            122 -> 3        // Overcast
            143 -> 45       // Mist
            176 -> 80       // Patchy rain nearby
            179 -> 71       // Patchy snow nearby
            182 -> 66       // Patchy sleet nearby
            185 -> 56       // Patchy freezing drizzle
            200 -> 95       // Thundery outbreaks nearby
            227 -> 77       // Blowing snow
            230 -> 75       // Blizzard
            248, 260 -> 45  // Fog / Freezing fog
            263, 266 -> 51  // Patchy light drizzle / Light drizzle
            281, 284 -> 56  // Freezing drizzle
            293, 296 -> 61  // Patchy light rain / Light rain
            299, 302 -> 63  // Moderate rain (at times)
            305, 308 -> 65  // Heavy rain (at times)
            311, 314 -> 66  // Light/Moderate freezing rain
            317 -> 67       // Heavy freezing rain
            320 -> 73       // Light sleet
            323, 326 -> 71  // Patchy light snow / Light snow
            329, 332 -> 73  // Patchy moderate / Moderate snow
            335, 338 -> 75  // Patchy heavy / Heavy snow
            350 -> 77       // Ice pellets
            353 -> 80       // Light rain shower
            356 -> 81       // Moderate/heavy rain shower
            359 -> 82       // Torrential rain shower
            362, 365 -> 85  // Light/Moderate sleet showers
            368, 371 -> 85  // Light/Moderate snow showers
            374, 377 -> 77  // Light/Moderate ice pellet showers
            386 -> 95       // Patchy light rain with thunder
            389 -> 99       // Moderate or heavy rain with thunder
            392 -> 95       // Patchy light snow with thunder
            395 -> 99       // Moderate or heavy snow with thunder
            else -> 0
        }
    }

    /**
     * Kiểm tra có phải ban ngày không (6h-18h theo timezone Vietnam)
     */
    private fun isDaytime(): Boolean {
        val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"))
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        return hour in 6..17
    }

    /**
     * Geocoding: tìm tên thành phố từ tọa độ
     */
    suspend fun reverseGeocode(lat: Double, lon: Double): String =
        withContext(Dispatchers.IO) {
            try {
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
