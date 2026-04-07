package com.lichso.app.data.local

import com.lichso.app.data.local.dao.*
import com.lichso.app.data.local.entity.*
import com.lichso.app.ui.screen.familytree.*
import com.lichso.app.util.CanChiCalculator
import com.lichso.app.util.LunarCalendarUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyTreeRepository @Inject constructor(
    private val memberDao: FamilyMemberDao,
    private val memorialDao: MemorialDayDao,
    private val checklistDao: MemorialChecklistDao,
    private val settingsDao: FamilySettingsDao,
    private val photoDao: MemberPhotoDao,
) {
    // ══════════════════════════════════════════
    // MEMBERS
    // ══════════════════════════════════════════

    fun getAllMembersFlow(): Flow<List<FamilyMember>> =
        memberDao.getAllMembers().map { list -> list.map { it.toDomain() } }

    suspend fun getMemberById(id: String): FamilyMember? =
        memberDao.getMemberById(id)?.toDomain()

    fun getMemberCountFlow(): Flow<Int> = memberDao.getMemberCount()

    fun getMaxGenerationFlow(): Flow<Int?> = memberDao.getMaxGeneration()

    fun searchMembersFlow(query: String): Flow<List<FamilyMember>> =
        memberDao.searchMembers(query).map { list -> list.map { it.toDomain() } }

    suspend fun addMember(member: FamilyMember) {
        memberDao.insert(member.toEntity())
    }

    suspend fun updateMember(member: FamilyMember) {
        memberDao.update(member.toEntity().copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteMember(id: String) {
        memberDao.deleteById(id)
        // Also remove memorial for this member
        val memorial = memorialDao.getMemorialByMemberId(id)
        memorial?.let {
            checklistDao.deleteByMemorialId(it.id)
            memorialDao.deleteById(it.id)
        }
    }

    suspend fun deleteAllMembers() {
        memberDao.deleteAll()
        memorialDao.deleteAll()
    }

    // ══════════════════════════════════════════
    // MEMORIAL DAYS
    // ══════════════════════════════════════════

    fun getAllMemorialsFlow(): Flow<List<MemorialDay>> =
        memorialDao.getAllMemorials().map { list ->
            list.map { it.toDomain() }.sortedBy { it.countdownDays }
        }

    suspend fun getMemorialById(id: String): MemorialDay? =
        memorialDao.getMemorialById(id)?.toDomain()

    suspend fun getMemorialByMemberId(memberId: String): MemorialDay? =
        memorialDao.getMemorialByMemberId(memberId)?.toDomain()

    fun getMemorialByMemberIdFlow(memberId: String): Flow<MemorialDay?> =
        memorialDao.getMemorialByMemberIdFlow(memberId).map { it?.toDomain() }

    suspend fun addMemorial(memorial: MemorialDay) {
        memorialDao.insert(memorial.toEntity())
    }

    suspend fun updateMemorial(memorial: MemorialDay) {
        memorialDao.update(memorial.toEntity().copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteMemorial(id: String) {
        checklistDao.deleteByMemorialId(id)
        memorialDao.deleteById(id)
    }

    suspend fun deleteMemorialByMemberId(memberId: String) {
        val existing = memorialDao.getMemorialByMemberId(memberId)
        if (existing != null) {
            checklistDao.deleteByMemorialId(existing.id)
        }
        memorialDao.deleteByMemberId(memberId)
    }

    // ══════════════════════════════════════════
    // MEMORIAL CHECKLIST
    // ══════════════════════════════════════════

    fun getChecklistFlow(memorialId: String): Flow<List<MemorialChecklistEntity>> =
        checklistDao.getChecklistForMemorial(memorialId)

    suspend fun addChecklistItem(memorialId: String, text: String, sortOrder: Int = 0): Long =
        checklistDao.insert(
            MemorialChecklistEntity(
                memorialId = memorialId,
                text = text,
                sortOrder = sortOrder
            )
        )

    suspend fun toggleChecklistItem(id: Long, isDone: Boolean) =
        checklistDao.toggleDone(id, isDone)

    suspend fun deleteChecklistItem(item: MemorialChecklistEntity) =
        checklistDao.delete(item)

    // ══════════════════════════════════════════
    // SETTINGS
    // ══════════════════════════════════════════

    fun getSettingsFlow(): Flow<FamilySettingsEntity?> = settingsDao.getSettings()

    suspend fun getSettings(): FamilySettingsEntity =
        settingsDao.getSettingsOnce() ?: FamilySettingsEntity().also { settingsDao.insert(it) }

    suspend fun updateSettings(settings: FamilySettingsEntity) {
        settingsDao.insert(settings.copy(updatedAt = System.currentTimeMillis()))
    }

    // ══════════════════════════════════════════
    // MEMBER PHOTOS
    // ══════════════════════════════════════════

    fun getPhotosForMemberFlow(memberId: String): Flow<List<MemberPhotoEntity>> =
        photoDao.getPhotosForMember(memberId)

    suspend fun addPhoto(memberId: String, filePath: String, caption: String? = null): Long {
        val count = photoDao.getPhotoCount(memberId)
        return photoDao.insert(
            MemberPhotoEntity(
                memberId = memberId,
                filePath = filePath,
                caption = caption,
                sortOrder = count,
            )
        )
    }

    suspend fun deletePhoto(photoId: Long) {
        photoDao.deleteById(photoId)
    }

    suspend fun deletePhotosByMember(memberId: String) {
        photoDao.deleteByMemberId(memberId)
    }

    // ══════════════════════════════════════════
    // SEED DATA (initial mock data)
    // ══════════════════════════════════════════

    suspend fun seedIfEmpty() {
        // Only insert default settings if no settings exist yet
        val settings = settingsDao.getSettingsOnce()
        if (settings == null) {
            settingsDao.insert(FamilySettingsEntity())
        }
        // No sample data — start with a clean family tree
    }

    // ══════════════════════════════════════════
    // EXPORT / IMPORT
    // ══════════════════════════════════════════

    /**
     * Get all raw entities for export.
     */
    suspend fun getAllDataForExport(): FamilyTreeExportImport.ExportRawData {
        return FamilyTreeExportImport.ExportRawData(
            settings = settingsDao.getSettingsOnce(),
            members = memberDao.getAllMembersOnce(),
            memorials = memorialDao.getAllMemorialsOnce(),
            checklistItems = checklistDao.getAllChecklistOnce(),
            photos = photoDao.getAllPhotosOnce(),
        )
    }

    /**
     * Import all entities, replacing existing data.
     * Clears current family data first, then inserts new data.
     */
    suspend fun importAllData(entities: FamilyTreeExportImport.ImportEntities) {
        // Clear existing data
        photoDao.deleteAll()
        checklistDao.deleteAll()
        memorialDao.deleteAll()
        memberDao.deleteAll()

        // Import settings
        entities.settings?.let { settingsDao.insert(it) }

        // Import members
        if (entities.members.isNotEmpty()) {
            memberDao.insertAll(entities.members)
        }

        // Import memorials
        if (entities.memorials.isNotEmpty()) {
            memorialDao.insertAll(entities.memorials)
        }

        // Import checklist items
        if (entities.checklistItems.isNotEmpty()) {
            checklistDao.insertAll(entities.checklistItems)
        }

        // Import photos
        if (entities.photos.isNotEmpty()) {
            photoDao.insertAll(entities.photos)
        }
    }

    /**
     * Delete all photos files from internal storage.
     */
    suspend fun deleteAllPhotoFiles(context: android.content.Context) {
        val avatarDir = File(context.filesDir, "avatars")
        if (avatarDir.exists()) avatarDir.deleteRecursively()

        val photosDir = File(context.filesDir, "member_photos")
        if (photosDir.exists()) photosDir.deleteRecursively()
    }

    // ══════════════════════════════════════════
    // MAPPERS
    // ══════════════════════════════════════════

    private fun FamilyMemberEntity.toDomain(): FamilyMember = FamilyMember(
        id = id,
        name = name,
        role = role,
        gender = if (gender == "MALE") Gender.MALE else Gender.FEMALE,
        generation = generation,
        birthYear = birthYear,
        deathYear = deathYear,
        birthDateLunar = birthDateLunar,
        deathDateLunar = deathDateLunar,
        canChi = canChi,
        menh = menh,
        zodiacEmoji = zodiacEmoji,
        menhEmoji = menhEmoji,
        hanhEmoji = hanhEmoji,
        menhDetail = menhDetail,
        zodiacName = zodiacName,
        menhName = menhName,
        hometown = hometown,
        occupation = occupation,
        isSelf = isSelf,
        isElder = isElder,
        emoji = emoji,
        spouseIds = if (spouseIds.isBlank()) emptyList() else spouseIds.split(",").filter { it.isNotBlank() },
        spouseOrder = spouseOrder,
        parentIds = if (parentIds.isBlank()) emptyList() else parentIds.split(",").filter { it.isNotBlank() },
        note = note,
        avatarPath = avatarPath,
    )

    private fun FamilyMember.toEntity(): FamilyMemberEntity = FamilyMemberEntity(
        id = id,
        name = name,
        role = role,
        gender = gender.name,
        generation = generation,
        birthYear = birthYear,
        deathYear = deathYear,
        birthDateLunar = birthDateLunar,
        deathDateLunar = deathDateLunar,
        canChi = canChi,
        menh = menh,
        zodiacEmoji = zodiacEmoji,
        menhEmoji = menhEmoji,
        hanhEmoji = hanhEmoji,
        menhDetail = menhDetail,
        zodiacName = zodiacName,
        menhName = menhName,
        hometown = hometown,
        occupation = occupation,
        isSelf = isSelf,
        isElder = isElder,
        emoji = emoji,
        spouseIds = spouseIds.joinToString(","),
        spouseOrder = spouseOrder,
        parentIds = parentIds.joinToString(","),
        note = note,
        avatarPath = avatarPath,
    )

    private fun MemorialDayEntity.toDomain(): MemorialDay {
        val today = LocalDate.now()
        val currentYear = today.year

        // Tính ngày dương lịch của ngày giỗ trong năm nay
        val (solarDay, solarMonth, solarYear) = LunarCalendarUtil.convertLunar2Solar(
            lunarDay, lunarMonth, currentYear, lunarLeap, LunarCalendarUtil.TZ
        )
        var solarDate = LocalDate.of(solarYear, solarMonth, solarDay)

        // Nếu ngày giỗ năm nay đã qua, tính cho năm sau
        if (solarDate.isBefore(today)) {
            val (sd2, sm2, sy2) = LunarCalendarUtil.convertLunar2Solar(
                lunarDay, lunarMonth, currentYear + 1, lunarLeap, LunarCalendarUtil.TZ
            )
            solarDate = LocalDate.of(sy2, sm2, sd2)
        }

        val daysUntil = ChronoUnit.DAYS.between(today, solarDate)
        val countdown = when {
            daysUntil == 0L -> "Hôm nay"
            daysUntil == 1L -> "Còn 1 ngày nữa"
            daysUntil < 30 -> "Còn $daysUntil ngày nữa"
            daysUntil < 60 -> "Còn khoảng 1 tháng nữa"
            else -> "Còn ${daysUntil / 30} tháng nữa"
        }

        val isUpcoming = daysUntil in 0..30

        val lunarDateStr = "${lunarDay}/${lunarMonth} Âm lịch"
        val solarDateStr = solarDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

        // Can Chi cho năm âm lịch
        val lunarResult = LunarCalendarUtil.convertSolar2Lunar(solarDate.dayOfMonth, solarDate.monthValue, solarDate.year)
        val yearCanChi = CanChiCalculator.getYearCanChi(lunarResult.lunarYear)

        // Tính ngày nhắc nhở
        val remind3Date = solarDate.minusDays(3)
        val remind1Date = solarDate.minusDays(1)
        val remind3DaysDateStr = remind3Date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " lúc 08:00"
        val remind1DayDateStr = remind1Date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " lúc 07:00"

        // Tính thứ trong tuần cho ngày dương lịch
        val dayOfWeekStr = when (solarDate.dayOfWeek.value) {
            1 -> "Thứ Hai"
            2 -> "Thứ Ba"
            3 -> "Thứ Tư"
            4 -> "Thứ Năm"
            5 -> "Thứ Sáu"
            6 -> "Thứ Bảy"
            7 -> "Chủ Nhật"
            else -> ""
        }

        return MemorialDay(
            id = id,
            memberId = memberId,
            memberName = memberName,
            relation = relation,
            lunarDay = lunarDay,
            lunarMonth = lunarMonth,
            lunarLeap = lunarLeap,
            lunarDate = lunarDateStr,
            solarDate = solarDateStr,
            countdown = countdown,
            countdownDays = daysUntil,
            isUpcoming = isUpcoming,
            yearCanChi = yearCanChi,
            note = note,
            remindBefore3Days = remindBefore3Days,
            remindBefore1Day = remindBefore1Day,
            remind3DaysDateStr = remind3DaysDateStr,
            remind1DayDateStr = remind1DayDateStr,
        )
    }

    private fun MemorialDay.toEntity(): MemorialDayEntity = MemorialDayEntity(
        id = id,
        memberId = memberId,
        memberName = memberName,
        relation = relation,
        lunarDay = lunarDay,
        lunarMonth = lunarMonth,
        lunarLeap = lunarLeap,
        note = note,
        remindBefore3Days = remindBefore3Days,
        remindBefore1Day = remindBefore1Day,
    )

    companion object {
        fun generateId(): String = UUID.randomUUID().toString().take(8)
    }
}
