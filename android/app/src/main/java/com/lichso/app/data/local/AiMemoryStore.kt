package com.lichso.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.aiMemoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_memory")

@Singleton
class AiMemoryStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_AI_NAME = stringPreferencesKey("ai_name")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_BIRTHDATE = stringPreferencesKey("user_birthdate")
        private val KEY_USER_ZODIAC = stringPreferencesKey("user_zodiac")
        private val KEY_USER_HABITS = stringPreferencesKey("user_habits")
        private val KEY_USER_INTERESTS = stringPreferencesKey("user_interests")
        private val KEY_USER_NOTES = stringPreferencesKey("user_notes")
    }

    private val dataStore = context.aiMemoryDataStore

    suspend fun getUserName(): String? {
        return get(KEY_USER_NAME)
    }

    suspend fun getAiName(): String? {
        return get(KEY_AI_NAME)
    }

    suspend fun getMemoryContext(): String {
        val prefs = dataStore.data.first()
        val parts = mutableListOf<String>()

        prefs[KEY_AI_NAME]?.let { parts.add("Tên AI được đặt: $it") }
        prefs[KEY_USER_NAME]?.let { parts.add("Tên người dùng: $it") }
        prefs[KEY_USER_BIRTHDATE]?.let { parts.add("Ngày sinh người dùng: $it") }
        prefs[KEY_USER_ZODIAC]?.let { parts.add("Cung hoàng đạo/Con giáp: $it") }
        prefs[KEY_USER_HABITS]?.let { parts.add("Thói quen: $it") }
        prefs[KEY_USER_INTERESTS]?.let { parts.add("Sở thích: $it") }
        prefs[KEY_USER_NOTES]?.let { parts.add("Ghi chú khác: $it") }

        return if (parts.isNotEmpty()) {
            "Thông tin đã ghi nhớ về người dùng:\n" + parts.joinToString("\n")
        } else ""
    }

    suspend fun parseAndSaveFromAiResponse(userMessage: String, aiResponse: String) {
        val msgLower = userMessage.lowercase()

        // Detect user name
        val namePatterns = listOf(
            Regex("(?:tên|tao|mình|tôi|anh|em|chị) (?:là|tên là|tên) ([\\p{L}\\s]{2,20})", RegexOption.IGNORE_CASE),
            Regex("(?:gọi|xưng hô|kêu) (?:tôi|mình|tao|anh|em|chị) (?:là|bằng) ([\\p{L}\\s]{2,20})", RegexOption.IGNORE_CASE),
            Regex("(?:tên mình là|tôi tên|em tên|anh tên) ([\\p{L}\\s]{2,20})", RegexOption.IGNORE_CASE)
        )
        for (pattern in namePatterns) {
            pattern.find(userMessage)?.groupValues?.getOrNull(1)?.trim()?.let { name ->
                if (name.length in 2..20) save(KEY_USER_NAME, name)
            }
        }

        // Detect AI name change
        val aiNamePatterns = listOf(
            Regex("(?:đặt tên|gọi|đổi tên) (?:bạn|mày|AI|trợ lý) (?:là|thành|bằng) ([\\p{L}\\s]{2,20})", RegexOption.IGNORE_CASE),
            Regex("(?:từ giờ|bây giờ) (?:gọi|kêu) (?:bạn|mày) (?:là|bằng) ([\\p{L}\\s]{2,20})", RegexOption.IGNORE_CASE)
        )
        for (pattern in aiNamePatterns) {
            pattern.find(userMessage)?.groupValues?.getOrNull(1)?.trim()?.let { name ->
                if (name.length in 2..20) save(KEY_AI_NAME, name)
            }
        }

        // Detect birthdate
        val birthPatterns = listOf(
            Regex("(?:sinh|ngày sinh|birthday|born) (?:ngày |là )?([0-9]{1,2}[/\\-][0-9]{1,2}(?:[/\\-][0-9]{2,4})?)", RegexOption.IGNORE_CASE),
            Regex("(?:sinh ngày|ngày sinh của mình là) ([0-9]{1,2}[/\\-][0-9]{1,2}(?:[/\\-][0-9]{2,4})?)", RegexOption.IGNORE_CASE)
        )
        for (pattern in birthPatterns) {
            pattern.find(userMessage)?.groupValues?.getOrNull(1)?.trim()?.let { date ->
                save(KEY_USER_BIRTHDATE, date)
            }
        }

        // Detect hobbies/interests
        val hobbyPatterns = listOf(
            Regex("(?:thích|sở thích|đam mê|hay|yêu thích) (?:là )?([\\p{L}\\s,]{3,80})", RegexOption.IGNORE_CASE)
        )
        for (pattern in hobbyPatterns) {
            if (msgLower.contains("thích") || msgLower.contains("sở thích") || msgLower.contains("đam mê")) {
                pattern.find(userMessage)?.groupValues?.getOrNull(1)?.trim()?.let { interest ->
                    val existing = get(KEY_USER_INTERESTS) ?: ""
                    val updated = if (existing.isNotBlank()) "$existing; $interest" else interest
                    save(KEY_USER_INTERESTS, updated.take(200))
                }
            }
        }

        // Detect habits
        if (msgLower.contains("thói quen") || msgLower.contains("hay ") || msgLower.contains("thường ")) {
            val habitPatterns = listOf(
                Regex("(?:thói quen|thường|hay) (?:là )?([\\p{L}\\s,]{3,80})", RegexOption.IGNORE_CASE)
            )
            for (pattern in habitPatterns) {
                pattern.find(userMessage)?.groupValues?.getOrNull(1)?.trim()?.let { habit ->
                    val existing = get(KEY_USER_HABITS) ?: ""
                    val updated = if (existing.isNotBlank()) "$existing; $habit" else habit
                    save(KEY_USER_HABITS, updated.take(200))
                }
            }
        }
    }

    private suspend fun save(key: Preferences.Key<String>, value: String) {
        dataStore.edit { it[key] = value }
    }

    private suspend fun get(key: Preferences.Key<String>): String? {
        return dataStore.data.first()[key]
    }
}
