package com.lichso.app.data.local.dao

import androidx.room.*
import com.lichso.app.data.local.entity.FamilyMemberEntity
import com.lichso.app.data.local.entity.FamilySettingsEntity
import com.lichso.app.data.local.entity.MemberPhotoEntity
import com.lichso.app.data.local.entity.MemorialChecklistEntity
import com.lichso.app.data.local.entity.MemorialDayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {

    @Query("SELECT * FROM family_members ORDER BY generation ASC, name ASC")
    fun getAllMembers(): Flow<List<FamilyMemberEntity>>

    @Query("SELECT * FROM family_members ORDER BY generation ASC, name ASC")
    suspend fun getAllMembersOnce(): List<FamilyMemberEntity>

    @Query("SELECT * FROM family_members WHERE id = :id")
    suspend fun getMemberById(id: String): FamilyMemberEntity?

    @Query("SELECT * FROM family_members WHERE generation = :generation ORDER BY name ASC")
    fun getMembersByGeneration(generation: Int): Flow<List<FamilyMemberEntity>>

    @Query("SELECT * FROM family_members WHERE name LIKE '%' || :query || '%' OR role LIKE '%' || :query || '%'")
    fun searchMembers(query: String): Flow<List<FamilyMemberEntity>>

    @Query("SELECT COUNT(*) FROM family_members")
    fun getMemberCount(): Flow<Int>

    @Query("SELECT MAX(generation) FROM family_members")
    fun getMaxGeneration(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: FamilyMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<FamilyMemberEntity>)

    @Update
    suspend fun update(member: FamilyMemberEntity)

    @Delete
    suspend fun delete(member: FamilyMemberEntity)

    @Query("DELETE FROM family_members WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM family_members")
    suspend fun deleteAll()
}

@Dao
interface MemorialDayDao {

    @Query("SELECT * FROM memorial_days ORDER BY lunarMonth ASC, lunarDay ASC")
    fun getAllMemorials(): Flow<List<MemorialDayEntity>>

    @Query("SELECT * FROM memorial_days ORDER BY lunarMonth ASC, lunarDay ASC")
    suspend fun getAllMemorialsOnce(): List<MemorialDayEntity>

    @Query("SELECT * FROM memorial_days WHERE id = :id")
    suspend fun getMemorialById(id: String): MemorialDayEntity?

    @Query("SELECT * FROM memorial_days WHERE memberId = :memberId")
    suspend fun getMemorialByMemberId(memberId: String): MemorialDayEntity?

    @Query("SELECT * FROM memorial_days WHERE memberId = :memberId")
    fun getMemorialByMemberIdFlow(memberId: String): Flow<MemorialDayEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memorial: MemorialDayEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memorials: List<MemorialDayEntity>)

    @Update
    suspend fun update(memorial: MemorialDayEntity)

    @Delete
    suspend fun delete(memorial: MemorialDayEntity)

    @Query("DELETE FROM memorial_days WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM memorial_days WHERE memberId = :memberId")
    suspend fun deleteByMemberId(memberId: String)

    @Query("DELETE FROM memorial_days")
    suspend fun deleteAll()
}

@Dao
interface MemorialChecklistDao {

    @Query("SELECT * FROM memorial_checklist_items WHERE memorialId = :memorialId ORDER BY sortOrder ASC")
    fun getChecklistForMemorial(memorialId: String): Flow<List<MemorialChecklistEntity>>

    @Query("SELECT * FROM memorial_checklist_items ORDER BY sortOrder ASC")
    suspend fun getAllChecklistOnce(): List<MemorialChecklistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MemorialChecklistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MemorialChecklistEntity>)

    @Update
    suspend fun update(item: MemorialChecklistEntity)

    @Query("UPDATE memorial_checklist_items SET isDone = :isDone WHERE id = :id")
    suspend fun toggleDone(id: Long, isDone: Boolean)

    @Delete
    suspend fun delete(item: MemorialChecklistEntity)

    @Query("DELETE FROM memorial_checklist_items WHERE memorialId = :memorialId")
    suspend fun deleteByMemorialId(memorialId: String)

    @Query("DELETE FROM memorial_checklist_items")
    suspend fun deleteAll()
}

@Dao
interface FamilySettingsDao {

    @Query("SELECT * FROM family_settings WHERE id = 1")
    fun getSettings(): Flow<FamilySettingsEntity?>

    @Query("SELECT * FROM family_settings WHERE id = 1")
    suspend fun getSettingsOnce(): FamilySettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: FamilySettingsEntity)

    @Update
    suspend fun update(settings: FamilySettingsEntity)
}

@Dao
interface MemberPhotoDao {

    @Query("SELECT * FROM member_photos WHERE memberId = :memberId ORDER BY sortOrder ASC, createdAt DESC")
    fun getPhotosForMember(memberId: String): Flow<List<MemberPhotoEntity>>

    @Query("SELECT * FROM member_photos ORDER BY memberId ASC, sortOrder ASC")
    suspend fun getAllPhotosOnce(): List<MemberPhotoEntity>

    @Query("SELECT * FROM member_photos WHERE id = :id")
    suspend fun getPhotoById(id: Long): MemberPhotoEntity?

    @Query("SELECT COUNT(*) FROM member_photos WHERE memberId = :memberId")
    suspend fun getPhotoCount(memberId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: MemberPhotoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<MemberPhotoEntity>)

    @Update
    suspend fun update(photo: MemberPhotoEntity)

    @Delete
    suspend fun delete(photo: MemberPhotoEntity)

    @Query("DELETE FROM member_photos WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM member_photos WHERE memberId = :memberId")
    suspend fun deleteByMemberId(memberId: String)

    @Query("DELETE FROM member_photos")
    suspend fun deleteAll()
}
