package com.lichso.app.data.local.dao

import androidx.room.*
import com.lichso.app.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY solarYear DESC, solarMonth DESC, solarDay DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT COUNT(*) FROM bookmarks")
    fun getCount(): Flow<Int>

    @Query("SELECT * FROM bookmarks WHERE solarDay = :day AND solarMonth = :month AND solarYear = :year LIMIT 1")
    fun getBookmarkForDate(day: Int, month: Int, year: Int): Flow<BookmarkEntity?>

    @Query("SELECT * FROM bookmarks WHERE solarDay = :day AND solarMonth = :month AND solarYear = :year LIMIT 1")
    suspend fun getBookmarkForDateSync(day: Int, month: Int, year: Int): BookmarkEntity?

    @Query("SELECT * FROM bookmarks WHERE solarMonth = :month AND solarYear = :year")
    fun getBookmarksForMonth(month: Int, year: Int): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity): Long

    @Update
    suspend fun update(bookmark: BookmarkEntity)

    @Delete
    suspend fun delete(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE solarDay = :day AND solarMonth = :month AND solarYear = :year")
    suspend fun deleteByDate(day: Int, month: Int, year: Int)

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    suspend fun getAllBookmarksOnce(): List<BookmarkEntity>

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAll()
}
