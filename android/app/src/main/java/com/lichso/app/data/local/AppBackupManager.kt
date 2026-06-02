package com.lichso.app.data.local

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.datastore.preferences.core.*
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.lichso.app.data.local.dao.*
import com.lichso.app.data.local.entity.*
import com.lichso.app.ui.screen.settings.SettingsKeys
import com.lichso.app.ui.screen.settings.safeSettingsData
import com.lichso.app.ui.screen.settings.settingsDataStore
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Full app backup / restore manager — v2.
 *
 * Backs up ALL user data:
 *  - App settings (DataStore "lichso_settings")
 *  - AI memory (DataStore "ai_memory")
 *  - Tasks, Notes, Reminders, Bookmarks, Notifications, Chat messages (Room)
 *  - Family tree: members, memorials, checklist, settings, photos (Room)
 *  - Points Engine: ledger, action_log, daily_unlock, permanent_unlock, streak (Room)
 *  - Countdown events, World clock cities, Cycle tracker (Room)
 *  - Profile avatar (Base64)
 */
object AppBackupManager {

    private const val CURRENT_VERSION = 2
    private const val MAX_PHOTO_BYTES = 500_000

    // Keys known to be Long in DataStore — needed to restore correct type
    private val LONG_PREF_KEYS = setOf(
        SettingsKeys.DAILY_ORACLE_EPOCH_DAY.name,
    )

    // ══════════════════════════════════════════
    // Top-level backup envelope
    // ══════════════════════════════════════════

    data class AppBackupData(
        @SerializedName("version") val version: Int = CURRENT_VERSION,
        @SerializedName("exportDate") val exportDate: String = "",
        @SerializedName("appId") val appId: String = "com.lichso.app",
        @SerializedName("type") val type: String = "full_backup",

        // DataStore
        @SerializedName("appSettings") val appSettings: Map<String, Any?> = emptyMap(),
        @SerializedName("aiMemory") val aiMemory: Map<String, String> = emptyMap(),

        // Profile
        @SerializedName("profileAvatarBase64") val profileAvatarBase64: String? = null,

        // Core user data — bảng hợp nhất "items" (thay tasks/notes/reminders).
        // 3 trường cũ giữ lại để đọc backup phiên bản trước (chuyển thành items khi restore).
        @SerializedName("items") val items: List<BackupItem> = emptyList(),
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

        // Points Engine v2
        @SerializedName("pointsLedger") val pointsLedger: BackupPointsLedger? = null,
        @SerializedName("actionLogs") val actionLogs: List<BackupActionLog> = emptyList(),
        @SerializedName("dailyUnlocks") val dailyUnlocks: List<BackupDailyUnlock> = emptyList(),
        @SerializedName("permanentUnlocks") val permanentUnlocks: List<BackupPermanentUnlock> = emptyList(),
        @SerializedName("streakRecord") val streakRecord: BackupStreakRecord? = null,

        // V2 features
        @SerializedName("countdownEvents") val countdownEvents: List<BackupCountdownEvent> = emptyList(),
        @SerializedName("worldClockCities") val worldClockCities: List<BackupWorldClockCity> = emptyList(),
        @SerializedName("cycleSettings") val cycleSettings: BackupCycleSettings? = null,
        @SerializedName("cycleLogs") val cycleLogs: List<BackupCycleLog> = emptyList(),
    )

    // ── Unified item backup ───────────────────────────────────────────────────

    data class BackupItem(
        @SerializedName("title") val title: String,
        @SerializedName("description") val description: String = "",
        @SerializedName("tags") val tags: String = "",
        @SerializedName("isTask") val isTask: Boolean = false,
        @SerializedName("isDone") val isDone: Boolean = false,
        @SerializedName("priority") val priority: Int = 1,
        @SerializedName("dueDate") val dueDate: Long? = null,
        @SerializedName("dueTime") val dueTime: String? = null,
        @SerializedName("hasReminder") val hasReminder: Boolean = false,
        @SerializedName("reminderAt") val reminderAt: Long? = null,
        @SerializedName("repeatType") val repeatType: Int = 0,
        @SerializedName("useLunar") val useLunar: Boolean = false,
        @SerializedName("advanceDays") val advanceDays: Int = 0,
        @SerializedName("reminderEnabled") val reminderEnabled: Boolean = true,
        @SerializedName("isPinned") val isPinned: Boolean = false,
        @SerializedName("colorIndex") val colorIndex: Int = 0,
        @SerializedName("createdAt") val createdAt: Long = 0,
        @SerializedName("updatedAt") val updatedAt: Long = 0,
    )

    // ── Legacy backup entities (đọc backup cũ) ────────────────────────────────

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
        @SerializedName("spouseId") val spouseId: String? = null,
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

    // ── Points Engine v2 backup entities ────────────────────────────────────

    data class BackupPointsLedger(
        @SerializedName("dailyPoints") val dailyPoints: Int,
        @SerializedName("spentDailyPoints") val spentDailyPoints: Int,
        @SerializedName("permanentPoints") val permanentPoints: Long,
        @SerializedName("lastResetEpochDay") val lastResetEpochDay: Long,
        @SerializedName("updatedAt") val updatedAt: Long,
    )

    data class BackupActionLog(
        @SerializedName("actionType") val actionType: String,
        @SerializedName("dailyPointsAwarded") val dailyPointsAwarded: Int,
        @SerializedName("permanentPointsAwarded") val permanentPointsAwarded: Int,
        @SerializedName("streakMultiplierApplied") val streakMultiplierApplied: Float,
        @SerializedName("epochDay") val epochDay: Long,
        @SerializedName("timestamp") val timestamp: Long,
        @SerializedName("metadata") val metadata: String? = null,
    )

    data class BackupDailyUnlock(
        @SerializedName("unlockKey") val unlockKey: String,
        @SerializedName("epochDay") val epochDay: Long,
        @SerializedName("cost") val cost: Int,
        @SerializedName("unlockedAt") val unlockedAt: Long,
    )

    data class BackupPermanentUnlock(
        @SerializedName("unlockKey") val unlockKey: String,
        @SerializedName("rank") val rank: String,
        @SerializedName("unlockedAt") val unlockedAt: Long,
    )

    data class BackupStreakRecord(
        @SerializedName("currentStreak") val currentStreak: Int,
        @SerializedName("longestStreak") val longestStreak: Int,
        @SerializedName("lastCheckInEpochDay") val lastCheckInEpochDay: Long,
        @SerializedName("freezeTokens") val freezeTokens: Int,
        @SerializedName("lastFreezeGrantedMonth") val lastFreezeGrantedMonth: Int,
        @SerializedName("updatedAt") val updatedAt: Long,
    )

    // ── V2 feature backup entities ───────────────────────────────────────────

    data class BackupCountdownEvent(
        @SerializedName("title") val title: String,
        @SerializedName("targetEpochDay") val targetEpochDay: Long,
        @SerializedName("note") val note: String = "",
        @SerializedName("showOnHome") val showOnHome: Boolean = true,
        @SerializedName("showOnWidget") val showOnWidget: Boolean = true,
        @SerializedName("createdAt") val createdAt: Long = 0,
    )

    data class BackupWorldClockCity(
        @SerializedName("cityName") val cityName: String,
        @SerializedName("timezone") val timezone: String,
        @SerializedName("country") val country: String = "",
        @SerializedName("sortOrder") val sortOrder: Int = 0,
    )

    data class BackupCycleSettings(
        @SerializedName("cycleLength") val cycleLength: Int,
        @SerializedName("periodLength") val periodLength: Int,
    )

    data class BackupCycleLog(
        @SerializedName("startEpochDay") val startEpochDay: Long,
        @SerializedName("endEpochDay") val endEpochDay: Long,
        @SerializedName("notes") val notes: String = "",
        @SerializedName("createdAt") val createdAt: Long = 0,
    )

    // ══════════════════════════════════════════
    // EXPORT (BUILD JSON)
    // ══════════════════════════════════════════

    suspend fun buildBackupJson(
        context: Context,
        // Core DAOs
        itemDao: ItemDao,
        bookmarkDao: BookmarkDao,
        notificationDao: NotificationDao,
        chatMessageDao: ChatMessageDao,
        // Family tree DAOs
        familyMemberDao: FamilyMemberDao,
        memorialDayDao: MemorialDayDao,
        memorialChecklistDao: MemorialChecklistDao,
        familySettingsDao: FamilySettingsDao,
        memberPhotoDao: MemberPhotoDao,
        // Points Engine DAOs
        pointsDao: PointsDao,
        unlockDao: UnlockDao,
        streakDao: StreakDao,
        // V2 feature DAOs
        countdownEventDao: CountdownEventDao,
        worldClockCityDao: WorldClockCityDao,
        cycleDao: CycleDao,
    ): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // ── DataStore ──
        val appSettings = readAppSettings(context)
        val aiMemory = readAiMemory(context)

        // ── Profile avatar ──
        val profileAvatarBase64 = readProfileAvatar(context)

        // ── Core Room data ──
        val items = itemDao.getAllItemsOnce().map { i ->
            BackupItem(
                title = i.title, description = i.description, tags = i.tags,
                isTask = i.isTask, isDone = i.isDone, priority = i.priority,
                dueDate = i.dueDate, dueTime = i.dueTime,
                hasReminder = i.hasReminder, reminderAt = i.reminderAt, repeatType = i.repeatType,
                useLunar = i.useLunar, advanceDays = i.advanceDays, reminderEnabled = i.reminderEnabled,
                isPinned = i.isPinned, colorIndex = i.colorIndex,
                createdAt = i.createdAt, updatedAt = i.updatedAt,
            )
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

        // ── Points Engine v2 ──
        val ledger = pointsDao.getLedger()?.let { l ->
            BackupPointsLedger(l.dailyPoints, l.spentDailyPoints, l.permanentPoints,
                l.lastResetEpochDay, l.updatedAt)
        }
        val actionLogs = pointsDao.getAllActionLogsOnce().map { a ->
            BackupActionLog(a.actionType, a.dailyPointsAwarded, a.permanentPointsAwarded,
                a.streakMultiplierApplied, a.epochDay, a.timestamp, a.metadata)
        }
        val dailyUnlocks = unlockDao.getAllDailyUnlocksOnce().map { u ->
            BackupDailyUnlock(u.unlockKey, u.epochDay, u.cost, u.unlockedAt)
        }
        val permanentUnlocks = unlockDao.getAllPermanent().map { u ->
            BackupPermanentUnlock(u.unlockKey, u.rank, u.unlockedAt)
        }
        val streak = streakDao.getStreak()?.let { s ->
            BackupStreakRecord(s.currentStreak, s.longestStreak, s.lastCheckInEpochDay,
                s.freezeTokens, s.lastFreezeGrantedMonth, s.updatedAt)
        }

        // ── V2 features ──
        val countdownEvents = countdownEventDao.getAllOnce().map { e ->
            BackupCountdownEvent(e.title, e.targetEpochDay, e.note, e.showOnHome, e.showOnWidget, e.createdAt)
        }
        val worldClockCities = worldClockCityDao.getAllOnce().map { c ->
            BackupWorldClockCity(c.cityName, c.timezone, c.country, c.sortOrder)
        }
        val cycleSettings = cycleDao.getSettingsOnce()?.let { s ->
            BackupCycleSettings(s.cycleLength, s.periodLength)
        }
        val cycleLogs = cycleDao.getAllLogsOnce().map { l ->
            BackupCycleLog(l.startEpochDay, l.endEpochDay, l.notes, l.createdAt)
        }

        val backupData = AppBackupData(
            version = CURRENT_VERSION,
            exportDate = dateStr,
            appSettings = appSettings,
            aiMemory = aiMemory,
            profileAvatarBase64 = profileAvatarBase64,
            items = items,
            bookmarks = bookmarks, notifications = notifications, chatMessages = chatMessages,
            familySettings = familySettings, familyMembers = familyMembers,
            memorialDays = memorialDays, memorialChecklist = memorialChecklist, memberPhotos = memberPhotos,
            pointsLedger = ledger, actionLogs = actionLogs,
            dailyUnlocks = dailyUnlocks, permanentUnlocks = permanentUnlocks, streakRecord = streak,
            countdownEvents = countdownEvents, worldClockCities = worldClockCities,
            cycleSettings = cycleSettings, cycleLogs = cycleLogs,
        )

        return GsonBuilder().setPrettyPrinting().create().toJson(backupData)
    }

    // ══════════════════════════════════════════
    // WRITE / READ FILE
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

    fun readFromUri(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { input ->
            input.bufferedReader(Charsets.UTF_8).readText()
        } ?: throw IllegalStateException("Không thể đọc file")
    }

    fun parseBackupJson(json: String): AppBackupData {
        if (json.length > 50 * 1024 * 1024) {
            throw IllegalStateException("File sao lưu quá lớn (>50MB)")
        }
        val data = GsonBuilder().create().fromJson(json, AppBackupData::class.java)
            ?: throw IllegalStateException("Không thể đọc dữ liệu sao lưu")

        if (data.appId != "com.lichso.app") {
            throw IllegalStateException("File không phải bản sao lưu Lịch Số")
        }
        if (data.familyMembers.size > 5000 || data.chatMessages.size > 50000 ||
            data.memberPhotos.size > 10000) {
            throw IllegalStateException("Dữ liệu sao lưu vượt quá giới hạn cho phép")
        }
        return data
    }

    // ══════════════════════════════════════════
    // RESTORE
    // ══════════════════════════════════════════

    suspend fun restoreFromBackup(
        context: Context,
        data: AppBackupData,
        // Core DAOs
        itemDao: ItemDao,
        bookmarkDao: BookmarkDao,
        notificationDao: NotificationDao,
        chatMessageDao: ChatMessageDao,
        // Family tree DAOs
        familyMemberDao: FamilyMemberDao,
        memorialDayDao: MemorialDayDao,
        memorialChecklistDao: MemorialChecklistDao,
        familySettingsDao: FamilySettingsDao,
        memberPhotoDao: MemberPhotoDao,
        // Points Engine DAOs
        pointsDao: PointsDao,
        unlockDao: UnlockDao,
        streakDao: StreakDao,
        // V2 feature DAOs
        countdownEventDao: CountdownEventDao,
        worldClockCityDao: WorldClockCityDao,
        cycleDao: CycleDao,
    ) {
        // 1. DataStore
        restoreAppSettings(context, data.appSettings)
        restoreAiMemory(context, data.aiMemory)

        // 2. Profile avatar
        data.profileAvatarBase64?.let { restoreProfileAvatar(context, it) }

        // 3. Clear all Room tables
        itemDao.deleteAll()
        bookmarkDao.deleteAll()
        notificationDao.deleteAll()
        chatMessageDao.clearAll()
        memberPhotoDao.deleteAll()
        memorialChecklistDao.deleteAll()
        memorialDayDao.deleteAll()
        familyMemberDao.deleteAll()
        clearPointsData(pointsDao, unlockDao, streakDao)
        clearV2Data(countdownEventDao, worldClockCityDao, cycleDao)

        // 4. Core data — items hợp nhất
        data.items.forEach { i ->
            itemDao.insert(ItemEntity(
                title = i.title, description = i.description, tags = i.tags,
                isTask = i.isTask, isDone = i.isDone, priority = i.priority,
                dueDate = i.dueDate, dueTime = i.dueTime,
                hasReminder = i.hasReminder, reminderAt = i.reminderAt, repeatType = i.repeatType,
                useLunar = i.useLunar, advanceDays = i.advanceDays, reminderEnabled = i.reminderEnabled,
                isPinned = i.isPinned, colorIndex = i.colorIndex,
                createdAt = i.createdAt, updatedAt = i.updatedAt,
            ))
        }
        // Backward-compat: chuyển dữ liệu backup cũ (tasks/notes/reminders) thành items
        data.tasks.forEach { t ->
            itemDao.insert(ItemEntity(title = t.title, description = t.description, tags = t.labels,
                isTask = true, isDone = t.isDone, priority = t.priority, dueDate = t.dueDate, dueTime = t.dueTime,
                hasReminder = t.hasReminder, createdAt = t.createdAt, updatedAt = t.updatedAt))
        }
        data.notes.forEach { n ->
            itemDao.insert(ItemEntity(title = n.title, description = n.content, tags = n.labels,
                colorIndex = n.colorIndex, isPinned = n.isPinned, createdAt = n.createdAt, updatedAt = n.updatedAt))
        }
        data.reminders.forEach { r ->
            itemDao.insert(ItemEntity(title = r.title, description = r.subtitle, tags = r.labels,
                hasReminder = true, reminderAt = r.triggerTime, repeatType = r.repeatType,
                reminderEnabled = r.isEnabled, useLunar = r.useLunar, advanceDays = r.advanceDays,
                createdAt = r.createdAt))
        }
        data.bookmarks.forEach { b ->
            bookmarkDao.insert(BookmarkEntity(solarDay = b.solarDay, solarMonth = b.solarMonth,
                solarYear = b.solarYear, label = b.label, note = b.note, colorIndex = b.colorIndex, createdAt = b.createdAt))
        }
        data.notifications.forEach { n ->
            notificationDao.insert(NotificationEntity(title = n.title, description = n.description,
                type = n.type, isRead = n.isRead, createdAt = n.createdAt))
        }
        data.chatMessages.forEach { m ->
            chatMessageDao.insert(ChatMessageEntity(content = m.content, isUser = m.isUser, timestamp = m.timestamp))
        }

        // 5. Family tree
        data.familySettings?.let { s ->
            familySettingsDao.insert(FamilySettingsEntity(familyName = s.familyName, familyCrest = s.familyCrest,
                hometown = s.hometown, treeDisplayMode = s.treeDisplayMode, treeTheme = s.treeTheme,
                showAvatar = s.showAvatar, showYears = s.showYears, remindMemorial = s.remindMemorial,
                remindBirthday = s.remindBirthday, remindDaysBefore = s.remindDaysBefore))
        }
        val photosDir = File(context.filesDir, "family_photos").also { if (!it.exists()) it.mkdirs() }
        data.familyMembers.forEach { m ->
            val avatarPath = m.avatarBase64?.let { base64 ->
                decodeBase64ToFile(base64, File(photosDir, "avatar_${m.id}.jpg"))
            }
            familyMemberDao.insert(FamilyMemberEntity(id = m.id, name = m.name, role = m.role,
                gender = m.gender, generation = m.generation, birthYear = m.birthYear, deathYear = m.deathYear,
                birthDateLunar = m.birthDateLunar, deathDateLunar = m.deathDateLunar,
                canChi = m.canChi, menh = m.menh, zodiacEmoji = m.zodiacEmoji, menhEmoji = m.menhEmoji,
                hanhEmoji = m.hanhEmoji, menhDetail = m.menhDetail, zodiacName = m.zodiacName,
                menhName = m.menhName, hometown = m.hometown, occupation = m.occupation,
                isSelf = m.isSelf, isElder = m.isElder, emoji = m.emoji,
                spouseIds = if (m.spouseIds.isNotBlank()) m.spouseIds else m.spouseId ?: "",
                spouseOrder = m.spouseOrder, parentIds = m.parentIds, note = m.note, avatarPath = avatarPath))
        }
        data.memorialDays.forEach { m ->
            memorialDayDao.insert(MemorialDayEntity(id = m.id, memberId = m.memberId,
                memberName = m.memberName, relation = m.relation, lunarDay = m.lunarDay,
                lunarMonth = m.lunarMonth, lunarLeap = m.lunarLeap, note = m.note,
                remindBefore3Days = m.remindBefore3Days, remindBefore1Day = m.remindBefore1Day))
        }
        data.memorialChecklist.forEach { c ->
            memorialChecklistDao.insert(MemorialChecklistEntity(memorialId = c.memorialId,
                text = c.text, isDone = c.isDone, sortOrder = c.sortOrder))
        }
        data.memberPhotos.forEach { p ->
            val filePath = p.photoBase64?.let { base64 ->
                decodeBase64ToFile(base64, File(photosDir, "photo_${p.memberId}_${System.nanoTime()}.jpg"))
            }
            if (filePath != null) {
                memberPhotoDao.insert(MemberPhotoEntity(memberId = p.memberId, filePath = filePath,
                    caption = p.caption, sortOrder = p.sortOrder))
            }
        }

        // 6. Points Engine v2
        data.pointsLedger?.let { l ->
            pointsDao.upsertLedger(PointsLedgerEntity(id = 1, dailyPoints = l.dailyPoints,
                spentDailyPoints = l.spentDailyPoints, permanentPoints = l.permanentPoints,
                lastResetEpochDay = l.lastResetEpochDay, updatedAt = l.updatedAt))
        }
        data.actionLogs.forEach { a ->
            pointsDao.logAction(ActionLogEntity(actionType = a.actionType,
                dailyPointsAwarded = a.dailyPointsAwarded, permanentPointsAwarded = a.permanentPointsAwarded,
                streakMultiplierApplied = a.streakMultiplierApplied, epochDay = a.epochDay,
                timestamp = a.timestamp, metadata = a.metadata))
        }
        data.dailyUnlocks.forEach { u ->
            unlockDao.insertDailyUnlock(DailyUnlockEntity(unlockKey = u.unlockKey,
                epochDay = u.epochDay, cost = u.cost, unlockedAt = u.unlockedAt))
        }
        data.permanentUnlocks.forEach { u ->
            unlockDao.insertPermanentUnlock(PermanentUnlockEntity(unlockKey = u.unlockKey,
                rank = u.rank, unlockedAt = u.unlockedAt))
        }
        data.streakRecord?.let { s ->
            streakDao.upsertStreak(StreakRecordEntity(id = 1, currentStreak = s.currentStreak,
                longestStreak = s.longestStreak, lastCheckInEpochDay = s.lastCheckInEpochDay,
                freezeTokens = s.freezeTokens, lastFreezeGrantedMonth = s.lastFreezeGrantedMonth,
                updatedAt = s.updatedAt))
        }

        // 7. V2 features
        data.countdownEvents.forEach { e ->
            countdownEventDao.insert(CountdownEventEntity(title = e.title, targetEpochDay = e.targetEpochDay,
                note = e.note, showOnHome = e.showOnHome, showOnWidget = e.showOnWidget, createdAt = e.createdAt))
        }
        data.worldClockCities.forEach { c ->
            worldClockCityDao.insert(WorldClockCityEntity(cityName = c.cityName, timezone = c.timezone,
                country = c.country, sortOrder = c.sortOrder))
        }
        data.cycleSettings?.let { s ->
            cycleDao.saveSettings(CycleSettingsEntity(id = 1, cycleLength = s.cycleLength, periodLength = s.periodLength))
        }
        data.cycleLogs.forEach { l ->
            cycleDao.insertLog(CycleLogEntity(startEpochDay = l.startEpochDay, endEpochDay = l.endEpochDay,
                notes = l.notes, createdAt = l.createdAt))
        }
    }

    // ══════════════════════════════════════════
    // SUMMARY
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
        data.pointsLedger?.let { parts.add("⚡ ${it.dailyPoints}đ / ☯ ${it.permanentPoints}đ tích lũy") }
        data.streakRecord?.let { if (it.currentStreak > 0) parts.add("🔥 Chuỗi ${it.currentStreak} ngày") }
        if (data.permanentUnlocks.isNotEmpty()) parts.add("🔓 ${data.permanentUnlocks.size} tính năng đã mở khóa")
        if (data.countdownEvents.isNotEmpty()) parts.add("⏳ ${data.countdownEvents.size} sự kiện đếm ngược")
        if (data.worldClockCities.isNotEmpty()) parts.add("🌍 ${data.worldClockCities.size} thành phố đồng hồ")
        if (data.cycleLogs.isNotEmpty()) parts.add("🗓️ ${data.cycleLogs.size} chu kỳ đã lưu")
        if (data.appSettings.isNotEmpty()) parts.add("⚙️ Cài đặt ứng dụng")
        if (data.aiMemory.isNotEmpty()) parts.add("🧠 Bộ nhớ AI")
        if (data.profileAvatarBase64 != null) parts.add("🖼️ Ảnh đại diện")
        return parts.joinToString("\n")
    }

    // ══════════════════════════════════════════
    // INTERNAL HELPERS
    // ══════════════════════════════════════════

    private suspend fun readAppSettings(context: Context): Map<String, Any?> {
        val prefs = context.safeSettingsData.first()
        val map = mutableMapOf<String, Any?>()
        for (entry in prefs.asMap()) {
            map[entry.key.name] = entry.value
        }
        return map
    }

    private suspend fun readAiMemory(context: Context): Map<String, String> {
        val prefs = context.aiMemoryDataStore.data.first()
        val map = mutableMapOf<String, String>()
        for (entry in prefs.asMap()) {
            val value = entry.value
            if (value is String) map[entry.key.name] = value
        }
        return map
    }

    private fun readProfileAvatar(context: Context): String? {
        val avatarFile = File(context.filesDir, "avatars/profile_avatar.jpg")
        return if (avatarFile.exists()) encodeFileToBase64(avatarFile.absolutePath) else null
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
                        val longVal = value.toLong()
                        when {
                            // Known Long keys — must be restored as Long or they crash on read
                            key in LONG_PREF_KEYS ->
                                prefs[longPreferencesKey(key)] = longVal
                            // Values too large for Int must be Long
                            longVal > Int.MAX_VALUE || longVal < Int.MIN_VALUE ->
                                prefs[longPreferencesKey(key)] = longVal
                            // Regular integer-like numbers
                            value.toDouble() == longVal.toDouble() ->
                                prefs[intPreferencesKey(key)] = value.toInt()
                            // Float/double
                            else ->
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
            if (base64.length > MAX_PHOTO_BYTES * 2) return
            val avatarDir = File(context.filesDir, "avatars").also { if (!it.exists()) it.mkdirs() }
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            if (bytes.size > MAX_PHOTO_BYTES) return
            File(avatarDir, "profile_avatar.jpg").writeBytes(bytes)
        } catch (_: Exception) {}
    }

    private suspend fun clearPointsData(pointsDao: PointsDao, unlockDao: UnlockDao, streakDao: StreakDao) {
        pointsDao.clearAll()
        unlockDao.clearAll()
        streakDao.clearAll()
    }

    private suspend fun clearV2Data(
        countdownEventDao: CountdownEventDao,
        worldClockCityDao: WorldClockCityDao,
        cycleDao: CycleDao,
    ) {
        countdownEventDao.deleteAll()
        worldClockCityDao.deleteAll()
        cycleDao.deleteAll()
    }

    private fun encodeFileToBase64(path: String): String? {
        return try {
            val file = File(path)
            if (!file.exists() || file.length() > MAX_PHOTO_BYTES) return null
            Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
        } catch (_: Exception) { null }
    }

    private fun decodeBase64ToFile(base64: String, dest: File): String? {
        return try {
            if (base64.length > MAX_PHOTO_BYTES * 2) return null
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            if (bytes.size > MAX_PHOTO_BYTES) return null
            dest.parentFile?.let { if (!it.exists()) it.mkdirs() }
            dest.writeBytes(bytes)
            dest.absolutePath
        } catch (_: Exception) { null }
    }
}
