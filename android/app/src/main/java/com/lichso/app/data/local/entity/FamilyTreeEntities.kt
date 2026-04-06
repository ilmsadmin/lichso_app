package com.lichso.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: String,
    val gender: String,              // "MALE" | "FEMALE"
    val generation: Int,
    val birthYear: Int? = null,
    val deathYear: Int? = null,
    val birthDateLunar: String? = null,
    val deathDateLunar: String? = null,
    val canChi: String? = null,
    val menh: String? = null,
    val zodiacEmoji: String? = null,
    val menhEmoji: String? = null,
    val hanhEmoji: String? = null,
    val menhDetail: String? = null,
    val zodiacName: String? = null,
    val menhName: String? = null,
    val hometown: String? = null,
    val occupation: String? = null,
    val isSelf: Boolean = false,
    val isElder: Boolean = false,
    val emoji: String = "👤",
    val spouseId: String? = null,
    val parentIds: String = "",      // comma-separated IDs
    val note: String? = null,
    val avatarPath: String? = null,  // internal file path for avatar image
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "memorial_days")
data class MemorialDayEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val memberName: String,
    val relation: String,
    val lunarDay: Int,              // ngày âm lịch (1-30)
    val lunarMonth: Int,            // tháng âm lịch (1-12)
    val lunarLeap: Int = 0,         // 1 nếu là tháng nhuận
    val note: String? = null,       // ghi chú ngày giỗ
    val remindBefore3Days: Boolean = true,
    val remindBefore1Day: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "memorial_checklist_items")
data class MemorialChecklistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memorialId: String,
    val text: String,
    val isDone: Boolean = false,
    val sortOrder: Int = 0,
)

@Entity(tableName = "family_settings")
data class FamilySettingsEntity(
    @PrimaryKey val id: Int = 1,     // singleton row
    val familyName: String = "Dòng họ Nguyễn",
    val familyCrest: String = "Ng",
    val hometown: String = "Hà Nam",
    val treeDisplayMode: String = "vertical",   // vertical, horizontal, fan
    val treeTheme: String = "classic",          // classic, modern
    val showAvatar: Boolean = true,
    val showYears: Boolean = true,
    val remindMemorial: Boolean = true,
    val remindBirthday: Boolean = true,
    val remindDaysBefore: Int = 3,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "member_photos")
data class MemberPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: String,
    val filePath: String,           // internal file path
    val caption: String? = null,    // optional caption
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)
