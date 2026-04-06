package com.lichso.app.data.remote

import android.content.Context
import com.lichso.app.data.settings.AppSettingsRepository
import com.lichso.app.domain.model.CityCoordinates
import com.lichso.app.domain.model.WeatherInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

sealed class WeatherState {
    data object Loading : WeatherState()
    data class Success(val weather: WeatherInfo) : WeatherState()
    data class Error(val message: String) : WeatherState()
}

/**
 * Repository quản lý dữ liệu thời tiết với caching
 */
@Singleton
class WeatherRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val weatherApi: WeatherApi,
    private val appSettings: AppSettingsRepository
) {
    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    // Cache: giữ dữ liệu thời tiết 30 phút
    private var cachedWeather: WeatherInfo? = null
    private var lastFetchTime: Long = 0L
    private var lastCityName: String? = null
    private val cacheValidDuration = 30 * 60 * 1000L // 30 phút

    /**
     * Lấy thời tiết, ưu tiên cache nếu còn hạn.
     * Đọc vị trí từ Settings (DataStore), nếu thay đổi thành phố thì invalidate cache.
     */
    suspend fun fetchWeather(forceRefresh: Boolean = false) {
        // Đọc tên thành phố đã lưu trong Settings
        val savedCityName = appSettings.locationName.first()

        // Nếu thành phố thay đổi → invalidate cache
        if (savedCityName != lastCityName) {
            cachedWeather = null
            lastFetchTime = 0L
            lastCityName = savedCityName
        }

        // Kiểm tra cache
        if (!forceRefresh && cachedWeather != null &&
            System.currentTimeMillis() - lastFetchTime < cacheValidDuration
        ) {
            _weatherState.value = WeatherState.Success(cachedWeather!!)
            return
        }

        _weatherState.value = WeatherState.Loading

        // Sử dụng tọa độ từ CityCoordinates theo tên thành phố đã lưu
        val location = CityCoordinates.toLocationInfo(savedCityName)
        val result = weatherApi.getCurrentWeather(location)

        result.fold(
            onSuccess = { weather ->
                cachedWeather = weather
                lastFetchTime = System.currentTimeMillis()
                _weatherState.value = WeatherState.Success(weather)
            },
            onFailure = { error ->
                // Nếu có cache cũ thì vẫn dùng
                if (cachedWeather != null) {
                    _weatherState.value = WeatherState.Success(cachedWeather!!)
                } else {
                    _weatherState.value = WeatherState.Error(error.message ?: "Lỗi không xác định")
                }
            }
        )
    }

    /**
     * Lấy thời tiết cho một vị trí cụ thể (dùng từ WeatherDetailSheet khi đổi thành phố)
     */
    suspend fun fetchWeatherForCity(cityName: String) {
        _weatherState.value = WeatherState.Loading
        val location = CityCoordinates.toLocationInfo(cityName)
        val result = weatherApi.getCurrentWeather(location)

        result.fold(
            onSuccess = { weather ->
                cachedWeather = weather
                lastFetchTime = System.currentTimeMillis()
                lastCityName = cityName
                _weatherState.value = WeatherState.Success(weather)
            },
            onFailure = { error ->
                _weatherState.value = WeatherState.Error(error.message ?: "Lỗi")
            }
        )
    }
}
