package com.lichso.app.data.remote

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

// ── DataStore extension for screen backgrounds cache ──
private val Context.screenBgDataStore by preferencesDataStore(name = "screen_backgrounds_cache")

private val KEY_BG_JSON = stringPreferencesKey("screen_backgrounds_json")

/**
 * Repository quản lý ảnh nền cho từng màn hình:
 * - Fetch danh sách từ server khi có mạng.
 * - Cache JSON mapping vào DataStore để dùng khi offline.
 * - Expose [backgrounds] StateFlow<Map<screenKey, imageUrl>> cho UI layer.
 */
@Singleton
class ScreenBackgroundRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: LichSoApi,
) {
    private val gson = Gson()

    // In-memory state: screenKey → imageUrl
    private val _backgrounds = MutableStateFlow<Map<String, String>>(emptyMap())
    val backgrounds: StateFlow<Map<String, String>> = _backgrounds.asStateFlow()

    /**
     * Returns a Flow of the cached backgrounds map directly from DataStore.
     * Useful to restore state before the first network fetch.
     */
    val cachedBackgrounds: Flow<Map<String, String>> =
        context.screenBgDataStore.data.map { prefs ->
            val json = prefs[KEY_BG_JSON] ?: return@map emptyMap()
            parseJson(json)
        }

    /**
     * Fetches screen backgrounds from the server.
     * On success, updates the in-memory state and persists to DataStore.
     * On failure, silently falls back to cached data already in [backgrounds].
     */
    suspend fun fetchBackgrounds() {
        api.getScreenBackgrounds()
            .onSuccess { list ->
                val map = list
                    .filter { !it.imageUrl.isNullOrBlank() && it.isActive }
                    .associate { it.screenKey to it.imageUrl!! }

                _backgrounds.update { map }

                // Persist to DataStore
                val json = gson.toJson(map)
                context.screenBgDataStore.edit { prefs ->
                    prefs[KEY_BG_JSON] = json
                }
            }
            // On network failure: keep existing in-memory state (already loaded from cache at startup)
    }

    /**
     * Loads the cached backgrounds from DataStore into memory.
     * Should be called once at app startup before the first network request.
     */
    suspend fun loadFromCache() {
        val p = context.screenBgDataStore.data.first()
        val json = p[KEY_BG_JSON] ?: return
        val map = parseJson(json)
        if (map.isNotEmpty()) {
            _backgrounds.update { map }
        }
    }

    /**
     * Returns the image URL for a given screen key, or null if not set.
     */
    fun getImageUrl(screenKey: String): String? = _backgrounds.value[screenKey]

    private fun parseJson(json: String): Map<String, String> {
        return runCatching {
            val type = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson<Map<String, String>>(json, type) ?: emptyMap()
        }.getOrDefault(emptyMap())
    }
}
