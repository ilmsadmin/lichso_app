package com.lichso.app.widget

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lichso.app.domain.model.CityCoordinates
import com.lichso.app.domain.model.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

private const val TAG = "WidgetWeather"
private const val PREFS_NAME = "widget_weather_cache"
private const val KEY_TEMP = "temp"
private const val KEY_ICON = "icon"
private const val KEY_DESC = "desc"
private const val KEY_CODE = "code"
private const val KEY_CITY = "city"
private const val KEY_HUMIDITY = "humidity"
private const val KEY_WIND = "wind"
private const val KEY_TEMP_MIN = "temp_min"
private const val KEY_TEMP_MAX = "temp_max"
private const val KEY_TIMESTAMP = "timestamp"
private const val CACHE_DURATION = 30 * 60 * 1000L // 30 phút

/**
 * Widget-friendly weather helper (no Hilt).
 * Fetches weather from Open-Meteo and caches to SharedPreferences.
 */
object WidgetWeatherHelper {

    data class CachedWeather(
        val temperature: Double,
        val icon: String,
        val description: String,
        val humidity: Int,
        val windSpeed: Double,
        val tempMin: Double,
        val tempMax: Double,
        val cityName: String,
        val timestamp: Long
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    /**
     * Get cached weather from SharedPreferences. Returns null if no cache or expired.
     */
    fun getCachedWeather(context: Context): CachedWeather? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val ts = prefs.getLong(KEY_TIMESTAMP, 0L)
        if (ts == 0L) return null

        // Still return even if expired — caller decides
        return CachedWeather(
            temperature = prefs.getFloat(KEY_TEMP, 0f).toDouble(),
            icon = prefs.getString(KEY_ICON, "🌡️") ?: "🌡️",
            description = prefs.getString(KEY_DESC, "") ?: "",
            humidity = prefs.getInt(KEY_HUMIDITY, 0),
            windSpeed = prefs.getFloat(KEY_WIND, 0f).toDouble(),
            tempMin = prefs.getFloat(KEY_TEMP_MIN, 0f).toDouble(),
            tempMax = prefs.getFloat(KEY_TEMP_MAX, 0f).toDouble(),
            cityName = prefs.getString(KEY_CITY, "") ?: "",
            timestamp = ts
        )
    }

    /**
     * Check if cached weather is still fresh (< 30 min) and has all required fields.
     */
    fun isCacheFresh(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val ts = prefs.getLong(KEY_TIMESTAMP, 0L)
        if (ts <= 0 || System.currentTimeMillis() - ts >= CACHE_DURATION) return false
        // Also stale if temp min/max are missing (old cache format)
        val hasMinMax = prefs.getFloat(KEY_TEMP_MIN, 0f) != 0f || prefs.getFloat(KEY_TEMP_MAX, 0f) != 0f
        return hasMinMax
    }

    /**
     * Read the city name from app settings DataStore.
     * Since DataStore is complex to read synchronously, we use a fallback approach:
     * read from our own cache first, then fallback to "Hà Nội".
     */
    private fun getCityName(context: Context): String {
        // Try to read from our widget cache first
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cached = prefs.getString(KEY_CITY, null)
        if (!cached.isNullOrBlank()) return cached

        // Try reading from app's settings DataStore via preferences file
        // DataStore filename: "settings" -> file "settings.preferences_pb"
        // This is complex, so just use default
        return "Hà Nội"
    }

    /**
     * Fetch weather from Open-Meteo API and cache result.
     * Call from a CoroutineWorker or background thread.
     */
    suspend fun fetchAndCacheWeather(context: Context, cityName: String? = null): CachedWeather? =
        withContext(Dispatchers.IO) {
            try {
                val city = cityName ?: getCityName(context)
                val location = CityCoordinates.toLocationInfo(city)

                val url = buildString {
                    append("https://api.open-meteo.com/v1/forecast?")
                    append("latitude=${location.latitude}")
                    append("&longitude=${location.longitude}")
                    append("&current=temperature_2m,relative_humidity_2m,is_day,weather_code,wind_speed_10m")
                    append("&daily=temperature_2m_max,temperature_2m_min")
                    append("&timezone=Asia/Ho_Chi_Minh")
                    append("&forecast_days=1")
                }

                val request = Request.Builder().url(url).get().build()
                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (!response.isSuccessful || body.isNullOrBlank()) {
                    Log.w(TAG, "HTTP ${response.code}")
                    return@withContext getCachedWeather(context)
                }

                val meteo = gson.fromJson(body, OpenMeteoWidgetResponse::class.java)
                val current = meteo.current ?: return@withContext getCachedWeather(context)

                val isDay = (current.isDay ?: 1) == 1
                val (desc, icon) = WeatherInfo.fromWeatherCode(current.weatherCode ?: 0, isDay)
                val temp = current.temperature ?: 0.0
                val humidity = current.humidity ?: 0
                val wind = current.windSpeed ?: 0.0

                // Daily min/max (first element = today)
                val daily = meteo.daily
                val tempMin = daily?.temperatureMin?.firstOrNull() ?: (temp - 3.0)
                val tempMax = daily?.temperatureMax?.firstOrNull() ?: (temp + 3.0)

                val cached = CachedWeather(
                    temperature = temp,
                    icon = icon,
                    description = desc,
                    humidity = humidity,
                    windSpeed = wind,
                    tempMin = tempMin,
                    tempMax = tempMax,
                    cityName = city,
                    timestamp = System.currentTimeMillis()
                )

                // Save to SharedPreferences
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putFloat(KEY_TEMP, temp.toFloat())
                    .putString(KEY_ICON, icon)
                    .putString(KEY_DESC, desc)
                    .putInt(KEY_CODE, current.weatherCode ?: 0)
                    .putInt(KEY_HUMIDITY, humidity)
                    .putFloat(KEY_WIND, wind.toFloat())
                    .putFloat(KEY_TEMP_MIN, tempMin.toFloat())
                    .putFloat(KEY_TEMP_MAX, tempMax.toFloat())
                    .putString(KEY_CITY, city)
                    .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                    .apply()

                Log.d(TAG, "Weather cached: $temp°C $desc ($icon) for $city")
                cached
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching weather", e)
                getCachedWeather(context)
            }
        }

    /**
     * Save city name for widget usage (called from main app when user changes city).
     */
    fun saveCityName(context: Context, cityName: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CITY, cityName)
            .apply()
    }
}

// ── JSON model for Open-Meteo (widget-only, minimal) ──

data class OpenMeteoWidgetResponse(
    val current: OpenMeteoWidgetCurrent?,
    val daily: OpenMeteoWidgetDaily?
)

data class OpenMeteoWidgetCurrent(
    @SerializedName("temperature_2m")
    val temperature: Double?,
    @SerializedName("relative_humidity_2m")
    val humidity: Int?,
    @SerializedName("is_day")
    val isDay: Int?,
    @SerializedName("weather_code")
    val weatherCode: Int?,
    @SerializedName("wind_speed_10m")
    val windSpeed: Double?
)

data class OpenMeteoWidgetDaily(
    @SerializedName("temperature_2m_max")
    val temperatureMax: List<Double>?,
    @SerializedName("temperature_2m_min")
    val temperatureMin: List<Double>?
)
