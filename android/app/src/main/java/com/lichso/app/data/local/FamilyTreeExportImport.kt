package com.lichso.app.data.local

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.lichso.app.data.local.entity.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Export / Import data model for Family Tree backup.
 *
 * The JSON structure includes:
 *  - metadata (app version, export date)
 *  - familySettings
 *  - members  (with inline avatar as base64)
 *  - memorials
 *  - checklistItems
 *  - memberPhotos (with inline photo data as base64, capped to keep file size reasonable)
 */
object FamilyTreeExportImport {

    private const val CURRENT_VERSION = 1
    private const val MAX_PHOTO_BYTES = 500_000 // ~500KB per photo to keep export manageable

    // ══════════════════════════════════════════
    // Export data model
    // ══════════════════════════════════════════

    data class ExportData(
        @SerializedName("version") val version: Int = CURRENT_VERSION,
        @SerializedName("exportDate") val exportDate: String = "",
        @SerializedName("appId") val appId: String = "com.lichso.app",
        @SerializedName("settings") val settings: ExportSettings? = null,
        @SerializedName("members") val members: List<ExportMember> = emptyList(),
        @SerializedName("memorials") val memorials: List<ExportMemorial> = emptyList(),
        @SerializedName("checklistItems") val checklistItems: List<ExportChecklistItem> = emptyList(),
        @SerializedName("memberPhotos") val memberPhotos: List<ExportMemberPhoto> = emptyList(),
    )

    data class ExportSettings(
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

    data class ExportMember(
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
        @SerializedName("spouseId") val spouseId: String? = null,       // legacy single-spouse (for backward compat)
        @SerializedName("spouseIds") val spouseIds: String = "",         // comma-separated spouse IDs
        @SerializedName("spouseOrder") val spouseOrder: Int = 0,
        @SerializedName("parentIds") val parentIds: String = "",
        @SerializedName("note") val note: String? = null,
        @SerializedName("avatarBase64") val avatarBase64: String? = null,
    )

    data class ExportMemorial(
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

    data class ExportChecklistItem(
        @SerializedName("memorialId") val memorialId: String,
        @SerializedName("text") val text: String,
        @SerializedName("isDone") val isDone: Boolean = false,
        @SerializedName("sortOrder") val sortOrder: Int = 0,
    )

    data class ExportMemberPhoto(
        @SerializedName("memberId") val memberId: String,
        @SerializedName("caption") val caption: String? = null,
        @SerializedName("sortOrder") val sortOrder: Int = 0,
        @SerializedName("photoBase64") val photoBase64: String? = null,
    )

    // ══════════════════════════════════════════
    // EXPORT
    // ══════════════════════════════════════════

    /**
     * Build the complete export JSON string from database entities.
     */
    fun buildExportJson(
        settings: FamilySettingsEntity?,
        members: List<FamilyMemberEntity>,
        memorials: List<MemorialDayEntity>,
        checklistItems: List<MemorialChecklistEntity>,
        photos: List<MemberPhotoEntity>,
    ): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val exportSettings = settings?.let {
            ExportSettings(
                familyName = it.familyName,
                familyCrest = it.familyCrest,
                hometown = it.hometown,
                treeDisplayMode = it.treeDisplayMode,
                treeTheme = it.treeTheme,
                showAvatar = it.showAvatar,
                showYears = it.showYears,
                remindMemorial = it.remindMemorial,
                remindBirthday = it.remindBirthday,
                remindDaysBefore = it.remindDaysBefore,
            )
        }

        val exportMembers = members.map { m ->
            val avatarBase64 = m.avatarPath?.let { path ->
                encodeFileToBase64(path, MAX_PHOTO_BYTES)
            }
            ExportMember(
                id = m.id, name = m.name, role = m.role, gender = m.gender,
                generation = m.generation, birthYear = m.birthYear, deathYear = m.deathYear,
                birthDateLunar = m.birthDateLunar, deathDateLunar = m.deathDateLunar,
                canChi = m.canChi, menh = m.menh,
                zodiacEmoji = m.zodiacEmoji, menhEmoji = m.menhEmoji, hanhEmoji = m.hanhEmoji,
                menhDetail = m.menhDetail, zodiacName = m.zodiacName, menhName = m.menhName,
                hometown = m.hometown, occupation = m.occupation,
                isSelf = m.isSelf, isElder = m.isElder, emoji = m.emoji,
                spouseIds = m.spouseIds, spouseOrder = m.spouseOrder, parentIds = m.parentIds,
                note = m.note, avatarBase64 = avatarBase64,
            )
        }

        val exportMemorials = memorials.map { m ->
            ExportMemorial(
                id = m.id, memberId = m.memberId, memberName = m.memberName,
                relation = m.relation, lunarDay = m.lunarDay, lunarMonth = m.lunarMonth,
                lunarLeap = m.lunarLeap, note = m.note,
                remindBefore3Days = m.remindBefore3Days, remindBefore1Day = m.remindBefore1Day,
            )
        }

        val exportChecklist = checklistItems.map { c ->
            ExportChecklistItem(
                memorialId = c.memorialId, text = c.text,
                isDone = c.isDone, sortOrder = c.sortOrder,
            )
        }

        val exportPhotos = photos.map { p ->
            val photoBase64 = encodeFileToBase64(p.filePath, MAX_PHOTO_BYTES)
            ExportMemberPhoto(
                memberId = p.memberId, caption = p.caption,
                sortOrder = p.sortOrder, photoBase64 = photoBase64,
            )
        }

        val exportData = ExportData(
            version = CURRENT_VERSION,
            exportDate = dateStr,
            settings = exportSettings,
            members = exportMembers,
            memorials = exportMemorials,
            checklistItems = exportChecklist,
            memberPhotos = exportPhotos,
        )

        return GsonBuilder().setPrettyPrinting().create().toJson(exportData)
    }

    /**
     * Write JSON to a Uri obtained from SAF (Storage Access Framework).
     */
    fun writeToUri(context: Context, uri: Uri, json: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(json.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Generate a suggested filename for the export.
     */
    fun generateFileName(familyName: String): String {
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val safeName = familyName.replace(Regex("[^\\p{L}\\p{N}_ ]"), "").trim().replace(" ", "_")
        return "lichso_giapha_${safeName}_$dateStr.json"
    }

    // ══════════════════════════════════════════
    // IMPORT
    // ══════════════════════════════════════════

    data class ImportResult(
        val success: Boolean,
        val membersCount: Int = 0,
        val memorialsCount: Int = 0,
        val photosCount: Int = 0,
        val errorMessage: String? = null,
    )

    /**
     * Parse JSON from a content URI.
     */
    fun readFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader(Charsets.UTF_8).readText()
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Parse JSON string into ExportData.
     * Returns null if the JSON is invalid or doesn't match our schema.
     */
    fun parseExportJson(json: String): ExportData? {
        return try {
            // Guard against excessively large imports (max 10MB)
            if (json.length > 10 * 1024 * 1024) return null

            val data = Gson().fromJson(json, ExportData::class.java)
            // Validate schema
            if (data == null) return null
            if (data.appId != "com.lichso.app") return null
            if (data.version < 1 || data.version > CURRENT_VERSION) return null
            // Sanity limits: prevent absurdly large imports
            if (data.members.size > 5000) return null
            if (data.memberPhotos.size > 10000) return null
            data
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Convert ExportData into Room entities ready for insertion.
     * Also restores avatar/photo files to internal storage.
     * Returns the entities and a result summary.
     */
    fun convertToEntities(
        context: Context,
        data: ExportData,
    ): ImportEntities {
        val now = System.currentTimeMillis()

        // Settings
        val settings = data.settings?.let {
            FamilySettingsEntity(
                familyName = it.familyName,
                familyCrest = it.familyCrest,
                hometown = it.hometown,
                treeDisplayMode = it.treeDisplayMode,
                treeTheme = it.treeTheme,
                showAvatar = it.showAvatar,
                showYears = it.showYears,
                remindMemorial = it.remindMemorial,
                remindBirthday = it.remindBirthday,
                remindDaysBefore = it.remindDaysBefore,
                createdAt = now, updatedAt = now,
            )
        }

        // Members (restore avatar images from base64)
        val members = data.members.map { m ->
            val avatarPath = m.avatarBase64?.let { b64 ->
                decodeBase64ToFile(context, b64, "avatars", "member_${m.id}_${now}.jpg")
            }
            FamilyMemberEntity(
                id = m.id, name = m.name, role = m.role, gender = m.gender,
                generation = m.generation, birthYear = m.birthYear, deathYear = m.deathYear,
                birthDateLunar = m.birthDateLunar, deathDateLunar = m.deathDateLunar,
                canChi = m.canChi, menh = m.menh,
                zodiacEmoji = m.zodiacEmoji, menhEmoji = m.menhEmoji, hanhEmoji = m.hanhEmoji,
                menhDetail = m.menhDetail, zodiacName = m.zodiacName, menhName = m.menhName,
                hometown = m.hometown, occupation = m.occupation,
                isSelf = m.isSelf, isElder = m.isElder, emoji = m.emoji,
                // Backward compatible: if spouseIds is empty, migrate from legacy spouseId
                spouseIds = if (m.spouseIds.isNotBlank()) m.spouseIds
                            else m.spouseId ?: "",
                spouseOrder = m.spouseOrder,
                parentIds = m.parentIds,
                note = m.note, avatarPath = avatarPath,
                createdAt = now, updatedAt = now,
            )
        }

        // Memorials
        val memorials = data.memorials.map { m ->
            MemorialDayEntity(
                id = m.id, memberId = m.memberId, memberName = m.memberName,
                relation = m.relation, lunarDay = m.lunarDay, lunarMonth = m.lunarMonth,
                lunarLeap = m.lunarLeap, note = m.note,
                remindBefore3Days = m.remindBefore3Days, remindBefore1Day = m.remindBefore1Day,
                createdAt = now, updatedAt = now,
            )
        }

        // Checklist items
        val checklistItems = data.checklistItems.map { c ->
            MemorialChecklistEntity(
                id = 0, // auto-generate
                memorialId = c.memorialId, text = c.text,
                isDone = c.isDone, sortOrder = c.sortOrder,
            )
        }

        // Member photos (restore photo files from base64)
        var restoredPhotosCount = 0
        val photos = data.memberPhotos.mapNotNull { p ->
            val photoPath = p.photoBase64?.let { b64 ->
                decodeBase64ToFile(
                    context, b64,
                    "member_photos/${p.memberId}",
                    "photo_${now}_${restoredPhotosCount}.jpg"
                )
            }
            if (photoPath != null) {
                restoredPhotosCount++
                MemberPhotoEntity(
                    id = 0, // auto-generate
                    memberId = p.memberId, filePath = photoPath,
                    caption = p.caption, sortOrder = p.sortOrder,
                    createdAt = now,
                )
            } else null
        }

        return ImportEntities(
            settings = settings,
            members = members,
            memorials = memorials,
            checklistItems = checklistItems,
            photos = photos,
        )
    }

    data class ImportEntities(
        val settings: FamilySettingsEntity?,
        val members: List<FamilyMemberEntity>,
        val memorials: List<MemorialDayEntity>,
        val checklistItems: List<MemorialChecklistEntity>,
        val photos: List<MemberPhotoEntity>,
    )

    /**
     * Raw data from database for building export JSON.
     */
    data class ExportRawData(
        val settings: FamilySettingsEntity?,
        val members: List<FamilyMemberEntity>,
        val memorials: List<MemorialDayEntity>,
        val checklistItems: List<MemorialChecklistEntity>,
        val photos: List<MemberPhotoEntity>,
    )

    // ══════════════════════════════════════════
    // Share Intent helper
    // ══════════════════════════════════════════

    /**
     * Create a share Intent for the exported JSON file.
     */
    fun createShareIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    // ══════════════════════════════════════════
    // File utility helpers
    // ══════════════════════════════════════════

    /**
     * Encode a file to base64 string. Returns null if file doesn't exist or is too large.
     */
    private fun encodeFileToBase64(filePath: String, maxBytes: Int): String? {
        return try {
            val file = File(filePath)
            if (!file.exists() || file.length() > maxBytes * 2) return null  // Skip very large files
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Decode a base64 string to a file in internal storage.
     * Returns the file path or null on failure.
     * Rejects data exceeding MAX_PHOTO_BYTES to prevent OOM.
     */
    private fun decodeBase64ToFile(
        context: Context,
        base64: String,
        subDir: String,
        fileName: String,
    ): String? {
        return try {
            // Guard: base64 string length ~1.37x the decoded size
            if (base64.length > MAX_PHOTO_BYTES * 2) return null

            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            if (bytes.size > MAX_PHOTO_BYTES) return null

            // Sanitize subDir and fileName to prevent path traversal
            val safeSubDir = subDir.replace("..", "").replace("/./", "/")
            val safeFileName = fileName.replace("..", "").replace("/", "_")

            val dir = File(context.filesDir, safeSubDir)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, safeFileName)
            file.writeBytes(bytes)
            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }
}
