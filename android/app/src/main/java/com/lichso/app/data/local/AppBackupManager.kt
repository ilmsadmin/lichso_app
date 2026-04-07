package com.lichso.app.data.local

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.datastore.preferences.core.*
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.lichso.app.data.local.dao.*
import com.lichso.app.data.local.entity.*
import com.lichso.app.ui.screen.settings.settingsDataStore
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Full app backup / restore manager.
 *
 * Backs up ALL user data:
 *  - App settings (DataStore "lichso_settings")
 *  - AI memory (DataStore "ai_memory")
 *  - Tasks, Notes, Reminders, Bookmarks, Notifications, Chat messages (Room DB)
 *  - Family tree: members, memorials, checklist, settings, photos (Room DB)
 *  - Profile avatar (Base64)
 *  - Family member avatars & photos (Base64)
 */
object AppBackupManager {

    private const val CURRENT_VERSION = 1
    private const val MAX_PHOTO_BYTES = 500_000 // ~500KB per photo

    // ══════════════════════════════════════════
    // Data Models
    // ══════════════════════════════════════════

    data class AppBackupData(
        @SerializedName("version") val version: Int = CURRENT_VERSION,
        @SerializedName("exportDate") val exportDate: String = "",
        @SerializedName("appId") val appId: String = "com.lichso.app",
        @SerializedName("type") val type: String = "full_backup",

        // DataStore preferences
        @SerializedName("appSettings") val appSettings: Map<String, Any?> = emptyMap(),
        @SerializedName("aiMemory") val aiMemory: Map<String, String> = emptyMap(),

        // Profile avatar
        @SerializedName("profileAvatarBase64") val profileAvatarBase64: String? = null,

        // Room DB tables
        @SerializedName("tasks") val tasks: List<BackupTask> = emptyList(),
        @SerializedName("notes") val notes: List<BackupNote> = emptyList(),
        @SerializedName("reminders") val reminders: List<BackupReminder> = emptyList(),
        @SerializedName("bookmarks") val bookmarks: List<BackupBookmark> = emptyList(),
        @SerializedName("notifications") val notifications: List<BackupNotification> = emptyList(),
        @SerializedName("chatMessages") val chatMessages: List<BackupChatMessage> = emptyList(),

        // Family tree
        @SerializedName("familySettings") val familySettings: BackupFamilySettings? = null,
        @SerializedName("familyMembers") val familyMembers: List<BackupFamilyMember> = emptyList(),
        @SerializedName("memorialDays") val memorialDays: List<BackupMemorialDay> = emptyList(),
        @SerializedName("memorialChecklist") val memorialChecklist: List<BackupMemorialChecklist> = emptyList(),
        @SerializedName("memberPhotos") val memberPhotos: List<BackupMemberPhoto> = emptyList(),
    )

    // ── Backup data classes for each entity ──

    data class BackupTask(
        @SerializedName("title") val title: String,
        @SerializedName("description") val description: String = "",
        @SerializedName("dueDate") val dueDate: Long? = null,
        @SerializedName("dueTime") val dueTime: String? = null,
        @SerializedName("priority") val priority: Int = 1,
        @SerializedName("isDone") val isDone: Boolean = false,
        @SerializedName("labels") val labels: String = "",
        @SerializedName("hasReminder") val hasReminder: Boolean = false,
        @SerializedName("createdAt") val createdAt: Long = 0,
        @SerializedName("updatedAt") val updatedAt: Long = 0,
    )

    data class BackupNote(
        @SerializedName("title") val title: String,
        @SerializedName("content") val content: String = "",
        @SerializedName("colorIndex") val colorIndex: Int = 0,
        @SerializedName("isPinned") val isPinned: Boolean = false,
        @SerializedName("labels") val labels: String = "",
        @SerializedName("createdAt") val createdAt: Long = 0,
        @SerializedName("updatedAt") val updatedAt: Long = 0,
    )

    data class BackupReminder(
        @SerializedName("title") val title: String,
        @SerializedName("subtitle") val subtitle: String = "",
        @SerializedName("triggerTime") val triggerTime: Long = 0,
        @SerializedName("repeatType") val repeatType: Int = 0,
        @SerializedName("isEnabled") val isEnabled: Boolean = true,
        @SerializedName("useLunar") val useLunar: Boolean = false,
        @SerializedName("advanceDays") val advanceDays: Int = 0,
        @SerializedName("category") val category: Int = 0,
        @SerializedName("labels") val labels: String = "",
        @SerializedName("createdAt") val createdAt: Long = 0,
    )

    data class BackupBookmark(
        @SerializedName("solarDay") val solarDay: Int,
        @SerializedName("solarMonth") val solarMonth: Int,
        @SerializedName("solarYear") val solarYear: Int,
        @SerializedName("label") val label: String = "",
        @SerializedName("note") val note: String = "",
        @SerializedName("colorIndex") val colorIndex: Int = 0,
        @SerializedName("createdAt") val createdAt: Long = 0,
    )

    data class BackupNotification(
        @SerializedName("title") val title: String,
        @SerializedName("description") val description: String = "",
        @SerializedName("type") val type: String = "system",
        @SerializedName("isRead") val isRead: Boolean = false,
        @SerializedName("createdAt") val createdAt: Long = 0,
    )

    data class BackupChatMessage(
        @SerializedName("content") val content: String,
        @SerializedName("isUser") val isUser: Boolean,
        @SerializedName("timestamp") val timestamp: Long = 0,
    )

    data class BackupFamilySettings(
        @SerializedName("familyName") val familyName: String,
        @SerializedName("familyCrest") val familyCrest: String,
        @SerializedName("hometown") val hometown: String,
        @SerializedName("treeDisplayMode") val treeDisplayMode: String,
        @SerializedName("treeTheme") val treeTheme: String,
        @SerializedName("showAvatar") val showAvatar: Boolean,
        @SerializedName("showYears") val showYears: Boolean,
        @SerializedName("remindMemorial") val remindMemorial: Boolean,
        @SerializedName("remindBirthday") val remindBirthday: Boolean,
        @SerializedName("remindDaysBefore") val remindDaysBefore: Int,
    )

    data class BackupFamilyMember(
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String,
        @SerializedName("role") val role: String,
        @SerializedName("gender") val gender: String,
        @SerializedName("generation") val generation: Int,
        @SerializedName("birthYear") val birthYear: Int? = null,
        @SerializedName("deathYear") val deathYear: Int? = null,
        @SerializedName("birthDateLunar") val birthDateLunar: String? = null,
        @SerializedName("deathDateLunar") val deathDateLunar: String? = null,
        @SerializedName("canChi") val canChi: String? = null,
        @SerializedName("menh") val menh: String? = null,
        @SerializedName("zodiacEmoji") val zodiacEmoji: String? = null,
        @SerializedName("menhEmoji") val menhEmoji: String? = null,
        @SerializedName("hanhEmoji") val hanhEmoji: String? = null,
        @SerializedName("menhDetail") val menhDetail: String? = null,
        @SerializedName("zodiacName") val zodiacName: String? = null,
        @SerializedName("menhName") val menhName: String? = null,
        @SerializedName("hometown") val hometown: String? = null,
        @SerializedName("occupation") val occupation: String? = null,
        @SerializedName("isSelf") val isSelf: Boolean = false,
        @SerializedName("isElder") val isElder: Boolean = false,
        @SerializedName("emoji") val emoji: String = "👤",
        @SerializedName("spouseId") val spouseId: String? = null,       // legacy (backward compat)
        @SerializedName("spouseIds") val spouseIds: String = "",
        @SerializedName("spouseOrder") val spouseOrder: Int = 0,
        @SerializedName("parentIds") val parentIds: String = "",
        @SerializedName("note") val note: String? = null,
        @SerializedName("avatarBase64") val avatarBase64: String? = null,
    )

    data class BackupMemorialDay(
        @SerializedName("id") val id: String,
        @SerializedName("memberId") val memberId: String,
        @SerializedName("memberName") val memberName: String,
        @SerializedName("relation") val relation: String,
        @SerializedName("lunarDay") val lunarDay: Int,
        @SerializedName("lunarMonth") val lunarMonth: Int,
        @SerializedName("lunarLeap") val lunarLeap: Int = 0,
        @SerializedName("note") val note: String? = null,
        @SerializedName("remindBefore3Days") val remindBefore3Days: Boolean = true,
        @SerializedName("remindBefore1Day") val remindBefore1Day: Boolean = true,
    )

    data class BackupMemorialChecklist(
        @SerializedName("memorialId") val memorialId: String,
        @SerializedName("text") val text: String,
        @SerializedName("isDone") val isDone: Boolean = false,
        @SerializedName("sortOrder") val sortOrder: Int = 0,
    )

    data class BackupMemberPhoto(
        @SerializedName("memberId") val memberId: String,
        @SerializedName("caption") val caption: String? = null,
        @SerializedName("sortOrder") val sortOrder: Int = 0,
        @SerializedName("photoBase64") val photoBase64: String? = null,
    )

    // ══════════════════════════════════════════
    // EXPORT (BUILD JSON)
    // ══════════════════════════════════════════

    suspend fun buildBackupJson(
        context: Context,
        // Room DAOs
        taskDao: TaskDao,
        noteDao: NoteDao,
        reminderDao: ReminderDao,
        bookmarkDao: BookmarkDao,
        notificationDao: NotificationDao,
        chatMessageDao: ChatMessageDao,
        familyMemberDao: FamilyMemberDao,
        memorialDayDao: MemorialDayDao,
        memorialChecklistDao: MemorialChecklistDao,
        familySettingsDao: FamilySettingsDao,
        memberPhotoDao: MemberPhotoDao,
    ): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // ── Read DataStore preferences ──
        val appSettings = readAppSettings(context)
        val aiMemory = readAiMemory(context)

        // ── Profile avatar ──
        val profileAvatarBase64 = readProfileAvatar(context)

        // ── Room DB data ──
        val tasks = taskDao.getAllTasksOnce().map { t ->
            BackupTask(t.title, t.description, t.dueDate, t.dueTime, t.priority,
                t.isDone, t.labels, t.hasReminder, t.createdAt, t.updatedAt)
        }
        val notes = noteDao.getAllNotesOnce().map { n ->
            BackupNote(n.title, n.content, n.colorIndex, n.isPinned, n.labels, n.createdAt, n.updatedAt)
        }
        val reminders = reminderDao.getAllRemindersOnce().map { r ->
            BackupReminder(r.title, r.subtitle, r.triggerTime, r.repeatType,
                r.isEnabled, r.useLunar, r.advanceDays, r.category, r.labels, r.createdAt)
        }
        val bookmarks = bookmarkDao.getAllBookmarksOnce().map { b ->
            BackupBookmark(b.solarDay, b.solarMonth, b.solarYear, b.label, b.note, b.colorIndex, b.createdAt)
        }
        val notifications = notificationDao.getAllNotificationsOnce().map { n ->
            BackupNotification(n.title, n.description, n.type, n.isRead, n.createdAt)
        }
        val chatMessages = chatMessageDao.getAllMessagesOnce().map { m ->
            BackupChatMessage(m.content, m.isUser, m.timestamp)
        }

        // ── Family tree ──
        val familySettings = familySettingsDao.getSettingsOnce()?.let { s ->
            BackupFamilySettings(s.familyName, s.familyCrest, s.hometown,
                s.treeDisplayMode, s.treeTheme, s.showAvatar, s.showYears,
                s.remindMemorial, s.remindBirthday, s.remindDaysBefore)
        }
        val familyMembers = familyMemberDao.getAllMembersOnce().map { m ->
            val avatarBase64 = m.avatarPath?.let { encodeFileToBase64(it) }
            BackupFamilyMember(m.id, m.name, m.role, m.gender, m.generation,
                m.birthYear, m.deathYear, m.birthDateLunar, m.deathDateLunar,
                m.canChi, m.menh, m.zodiacEmoji, m.menhEmoji, m.hanhEmoji,
                m.menhDetail, m.zodiacName, m.menhName, m.hometown, m.occupation,
                m.isSelf, m.isElder, m.emoji,
                spouseIds = m.spouseIds, spouseOrder = m.spouseOrder,
                parentIds = m.parentIds, note = m.note, avatarBase64 = avatarBase64)
        }
        val memorialDays = memorialDayDao.getAllMemorialsOnce().map { m ->
            BackupMemorialDay(m.id, m.memberId, m.memberName, m.relation,
                m.lunarDay, m.lunarMonth, m.lunarLeap, m.note,
                m.remindBefore3Days, m.remindBefore1Day)
        }
        val memorialChecklist = memorialChecklistDao.getAllChecklistOnce().map { c ->
            BackupMemorialChecklist(c.memorialId, c.text, c.isDone, c.sortOrder)
        }
        val memberPhotos = memberPhotoDao.getAllPhotosOnce().map { p ->
            val photoBase64 = encodeFileToBase64(p.filePath)
            BackupMemberPhoto(p.memberId, p.caption, p.sortOrder, photoBase64)
        }

        val backupData = AppBackupData(
            version = CURRENT_VERSION,
            exportDate = dateStr,
            appSettings = appSettings,
            aiMemory = aiMemory,
            profileAvatarBase64 = profileAvatarBase64,
            tasks = tasks,
            notes = notes,
            reminders = reminders,
            bookmarks = bookmarks,
            notifications = notifications,
            chatMessages = chatMessages,
            familySettings = familySettings,
            familyMembers = familyMembers,
            memorialDays = memorialDays,
            memorialChecklist = memorialChecklist,
            memberPhotos = memberPhotos,
        )

        return GsonBuilder().setPrettyPrinting().create().toJson(backupData)
    }

    // ══════════════════════════════════════════
    // WRITE TO FILE
    // ══════════════════════════════════════════

    fun writeToUri(context: Context, uri: Uri, json: String) {
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(json.toByteArray(Charsets.UTF_8))
        }
    }

    fun generateFileName(): String {
        val date = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        return "lichso_backup_$date.json"
    }

    // ══════════════════════════════════════════
    // READ FROM FILE
    // ══════════════════════════════════════════

    fun readFromUri(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { input ->
            input.bufferedReader(Charsets.UTF_8).readText()
        } ?: throw IllegalStateException("Không thể đọc file")
    }

    fun parseBackupJson(json: String): AppBackupData {
        return GsonBuilder().create().fromJson(json, AppBackupData::class.java)
    }

    // ══════════════════════════════════════════
    // IMPORT (RESTORE)
    // ══════════════════════════════════════════

    suspend fun restoreFromBackup(
        context: Context,
        data: AppBackupData,
        // Room DAOs
        taskDao: TaskDao,
        noteDao: NoteDao,
        reminderDao: ReminderDao,
        bookmarkDao: BookmarkDao,
        notificationDao: NotificationDao,
        chatMessageDao: ChatMessageDao,
        familyMemberDao: FamilyMemberDao,
        memorialDayDao: MemorialDayDao,
        memorialChecklistDao: MemorialChecklistDao,
        familySettingsDao: FamilySettingsDao,
        memberPhotoDao: MemberPhotoDao,
    ) {
        // ── 1. Restore DataStore preferences ──
        restoreAppSettings(context, data.appSettings)
        restoreAiMemory(context, data.aiMemory)

        // ── 2. Restore profile avatar ──
        data.profileAvatarBase64?.let { base64 ->
            restoreProfileAvatar(context, base64)
        }

        // ── 3. Clear existing Room data ──
        taskDao.deleteAll()
        noteDao.deleteAll()
        reminderDao.deleteAll()
        bookmarkDao.deleteAll()
        notificationDao.deleteAll()
        chatMessageDao.clearAll()
        memberPhotoDao.deleteAll()
        memorialChecklistDao.deleteAll()
        memorialDayDao.deleteAll()
        familyMemberDao.deleteAll()

        // ── 4. Insert tasks ──
        data.tasks.forEach { t ->
            taskDao.insert(TaskEntity(
                title = t.title, description = t.description, dueDate = t.dueDate,
                dueTime = t.dueTime, priority = t.priority, isDone = t.isDone,
                labels = t.labels, hasReminder = t.hasReminder,
                createdAt = t.createdAt, updatedAt = t.updatedAt
            ))
        }

        // ── 5. Insert notes ──
        data.notes.forEach { n ->
            noteDao.insert(NoteEntity(
                title = n.title, content = n.content, colorIndex = n.colorIndex,
                isPinned = n.isPinned, labels = n.labels,
                createdAt = n.createdAt, updatedAt = n.updatedAt
            ))
        }

        // ── 6. Insert reminders ──
        data.reminders.forEach { r ->
            reminderDao.insert(ReminderEntity(
                title = r.title, subtitle = r.subtitle, triggerTime = r.triggerTime,
                repeatType = r.repeatType, isEnabled = r.isEnabled, useLunar = r.useLunar,
                advanceDays = r.advanceDays, category = r.category, labels = r.labels,
                createdAt = r.createdAt
            ))
        }

        // ── 7. Insert bookmarks ──
        data.bookmarks.forEach { b ->
            bookmarkDao.insert(BookmarkEntity(
                solarDay = b.solarDay, solarMonth = b.solarMonth, solarYear = b.solarYear,
                label = b.label, note = b.note, colorIndex = b.colorIndex,
                createdAt = b.createdAt
            ))
        }

        // ── 8. Insert notifications ──
        data.notifications.forEach { n ->
            notificationDao.insert(NotificationEntity(
                title = n.title, description = n.description, type = n.type,
                isRead = n.isRead, createdAt = n.createdAt
            ))
        }

        // ── 9. Insert chat messages ──
        data.chatMessages.forEach { m ->
            chatMessageDao.insert(ChatMessageEntity(
                content = m.content, isUser = m.isUser, timestamp = m.timestamp
            ))
        }

        // ── 10. Family settings ──
        data.familySettings?.let { s ->
            familySettingsDao.insert(FamilySettingsEntity(
                familyName = s.familyName, familyCrest = s.familyCrest,
                hometown = s.hometown, treeDisplayMode = s.treeDisplayMode,
                treeTheme = s.treeTheme, showAvatar = s.showAvatar,
                showYears = s.showYears, remindMemorial = s.remindMemorial,
                remindBirthday = s.remindBirthday, remindDaysBefore = s.remindDaysBefore
            ))
        }

        // ── 11. Family members (with avatar restoration) ──
        val photosDir = File(context.filesDir, "family_photos")
        if (!photosDir.exists()) photosDir.mkdirs()

        data.familyMembers.forEach { m ->
            val avatarPath = m.avatarBase64?.let { base64 ->
                decodeBase64ToFile(base64, File(photosDir, "avatar_${m.id}.jpg"))
            }
            familyMemberDao.insert(FamilyMemberEntity(
                id = m.id, name = m.name, role = m.role, gender = m.gender,
                generation = m.generation, birthYear = m.birthYear, deathYear = m.deathYear,
                birthDateLunar = m.birthDateLunar, deathDateLunar = m.deathDateLunar,
                canChi = m.canChi, menh = m.menh, zodiacEmoji = m.zodiacEmoji,
                menhEmoji = m.menhEmoji, hanhEmoji = m.hanhEmoji, menhDetail = m.menhDetail,
                zodiacName = m.zodiacName, menhName = m.menhName, hometown = m.hometown,
                occupation = m.occupation, isSelf = m.isSelf, isElder = m.isElder,
                emoji = m.emoji,
                spouseIds = if (m.spouseIds.isNotBlank()) m.spouseIds else m.spouseId ?: "",
                spouseOrder = m.spouseOrder,
                parentIds = m.parentIds,
                note = m.note, avatarPath = avatarPath
            ))
        }

        // ── 12. Memorial days ──
        data.memorialDays.forEach { m ->
            memorialDayDao.insert(MemorialDayEntity(
                id = m.id, memberId = m.memberId, memberName = m.memberName,
                relation = m.relation, lunarDay = m.lunarDay, lunarMonth = m.lunarMonth,
                lunarLeap = m.lunarLeap, note = m.note,
                remindBefore3Days = m.remindBefore3Days, remindBefore1Day = m.remindBefore1Day
            ))
        }

        // ── 13. Memorial checklist ──
        data.memorialChecklist.forEach { c ->
            memorialChecklistDao.insert(MemorialChecklistEntity(
                memorialId = c.memorialId, text = c.text,
                isDone = c.isDone, sortOrder = c.sortOrder
            ))
        }

        // ── 14. Member photos (with file restoration) ──
        data.memberPhotos.forEach { p ->
            val filePath = p.photoBase64?.let { base64 ->
                val photoFile = File(photosDir, "photo_${p.memberId}_${System.nanoTime()}.jpg")
                decodeBase64ToFile(base64, photoFile)
            }
            if (filePath != null) {
                memberPhotoDao.insert(MemberPhotoEntity(
                    memberId = p.memberId, filePath = filePath,
                    caption = p.caption, sortOrder = p.sortOrder
                ))
            }
        }
    }

    // ══════════════════════════════════════════
    // SUMMARY (for confirm dialog)
    // ══════════════════════════════════════════

    fun getBackupSummary(data: AppBackupData): String {
        val parts = mutableListOf<String>()
        parts.add("📅 Ngày sao lưu: ${data.exportDate}")
        if (data.tasks.isNotEmpty()) parts.add("✅ ${data.tasks.size} công việc")
        if (data.notes.isNotEmpty()) parts.add("📝 ${data.notes.size} ghi chú")
        if (data.reminders.isNotEmpty()) parts.add("🔔 ${data.reminders.size} nhắc nhở")
        if (data.bookmarks.isNotEmpty()) parts.add("🔖 ${data.bookmarks.size} ngày đã lưu")
        if (data.chatMessages.isNotEmpty()) parts.add("💬 ${data.chatMessages.size} tin nhắn AI")
        if (data.familyMembers.isNotEmpty()) parts.add("👨‍👩‍👧‍👦 ${data.familyMembers.size} thành viên gia phả")
        if (data.memorialDays.isNotEmpty()) parts.add("🕯️ ${data.memorialDays.size} ngày giỗ")
        if (data.appSettings.isNotEmpty()) parts.add("⚙️ Cài đặt ứng dụng")
        if (data.aiMemory.isNotEmpty()) parts.add("🧠 Bộ nhớ AI")
        if (data.profileAvatarBase64 != null) parts.add("🖼️ Ảnh đại diện")
        return parts.joinToString("\n")
    }

    // ══════════════════════════════════════════
    // INTERNAL HELPERS
    // ══════════════════════════════════════════

    private suspend fun readAppSettings(context: Context): Map<String, Any?> {
        val prefs = context.settingsDataStore.data.first()
        val map = mutableMapOf<String, Any?>()
        for (entry in prefs.asMap()) {
            val key = entry.key.name
            map[key] = entry.value
        }
        return map
    }

    private suspend fun readAiMemory(context: Context): Map<String, String> {
        val prefs = context.aiMemoryDataStore.data.first()
        val map = mutableMapOf<String, String>()
        for (entry in prefs.asMap()) {
            val value = entry.value
            if (value is String) {
                map[entry.key.name] = value
            }
        }
        return map
    }

    private fun readProfileAvatar(context: Context): String? {
        val avatarFile = File(context.filesDir, "avatars/profile_avatar.jpg")
        return if (avatarFile.exists()) {
            encodeFileToBase64(avatarFile.absolutePath)
        } else null
    }

    private suspend fun restoreAppSettings(context: Context, settings: Map<String, Any?>) {
        if (settings.isEmpty()) return
        context.settingsDataStore.edit { prefs ->
            prefs.clear()
            for ((key, value) in settings) {
                when (value) {
                    is Boolean -> prefs[booleanPreferencesKey(key)] = value
                    is String -> prefs[stringPreferencesKey(key)] = value
                    is Number -> {
                        // Gson deserializes numbers as Double, we need to handle int/long
                        if (value.toDouble() == value.toLong().toDouble()) {
                            prefs[intPreferencesKey(key)] = value.toInt()
                        } else {
                            // Store as string for safety
                            prefs[stringPreferencesKey(key)] = value.toString()
                        }
                    }
                }
            }
        }
    }

    private suspend fun restoreAiMemory(context: Context, memory: Map<String, String>) {
        if (memory.isEmpty()) return
        context.aiMemoryDataStore.edit { prefs ->
            prefs.clear()
            for ((key, value) in memory) {
                prefs[stringPreferencesKey(key)] = value
            }
        }
    }

    private fun restoreProfileAvatar(context: Context, base64: String) {
        try {
            // Guard against oversized data
            if (base64.length > MAX_PHOTO_BYTES * 2) return

            val avatarDir = File(context.filesDir, "avatars")
            if (!avatarDir.exists()) avatarDir.mkdirs()
            val destFile = File(avatarDir, "profile_avatar.jpg")
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            if (bytes.size > MAX_PHOTO_BYTES) return
            destFile.writeBytes(bytes)
        } catch (_: Exception) {
            // Silently skip if avatar restore fails
        }
    }

    private fun encodeFileToBase64(path: String): String? {
        return try {
            val file = File(path)
            if (!file.exists() || file.length() > MAX_PHOTO_BYTES) return null
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (_: Exception) {
            null
        }
    }

    private fun decodeBase64ToFile(base64: String, dest: File): String? {
        return try {
            // Guard against oversized data
            if (base64.length > MAX_PHOTO_BYTES * 2) return null

            val bytes = Base64.decode(base64, Base64.DEFAULT)
            if (bytes.size > MAX_PHOTO_BYTES) return null

            dest.parentFile?.let { if (!it.exists()) it.mkdirs() }
            dest.writeBytes(bytes)
            dest.absolutePath
        } catch (_: Exception) {
            null
        }
    }
}
