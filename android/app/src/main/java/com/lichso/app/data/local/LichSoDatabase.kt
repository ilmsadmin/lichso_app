package com.lichso.app.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lichso.app.data.local.dao.*
import com.lichso.app.data.local.entity.*

@Database(
    entities = [
        ItemEntity::class,
        ChatMessageEntity::class,
        BookmarkEntity::class,
        NotificationEntity::class,
        CountdownEventEntity::class,
        WorldClockCityEntity::class,
        CycleSettingsEntity::class,
        CycleLogEntity::class,
        FamilyMemberEntity::class,
        MemorialDayEntity::class,
        MemorialChecklistEntity::class,
        FamilySettingsEntity::class,
        MemberPhotoEntity::class,
        // v2 PointsEngine
        PointsLedgerEntity::class,
        ActionLogEntity::class,
        DailyUnlockEntity::class,
        PermanentUnlockEntity::class,
        StreakRecordEntity::class,
        // v3 Quiz sync queue
        QuizOfflineSessionEntity::class,
    ],
    version = 15,
    exportSchema = true
)
abstract class LichSoDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun notificationDao(): NotificationDao
    abstract fun countdownEventDao(): CountdownEventDao
    abstract fun worldClockCityDao(): WorldClockCityDao
    abstract fun cycleDao(): CycleDao
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun memorialDayDao(): MemorialDayDao
    abstract fun memorialChecklistDao(): MemorialChecklistDao
    abstract fun familySettingsDao(): FamilySettingsDao
    abstract fun memberPhotoDao(): MemberPhotoDao
    // v2 PointsEngine DAOs
    abstract fun pointsDao(): PointsDao
    abstract fun unlockDao(): UnlockDao
    abstract fun streakDao(): StreakDao
    abstract fun quizOfflineSessionDao(): QuizOfflineSessionDao

    companion object {
        private const val TAG = "LichSoDatabase"

        @Volatile private var INSTANCE: LichSoDatabase? = null

        /**
         * Migration-safe database builder.
         *
         * For FUTURE schema changes:
         * 1. Increment the version number above
         * 2. Add a new Migration(oldVersion, newVersion) below
         * 3. Add it to the .addMigrations() call
         *
         * Example:
         *   val MIGRATION_9_10 = Migration(9, 10) {
         *       it.execSQL("ALTER TABLE tasks ADD COLUMN category TEXT NOT NULL DEFAULT ''")
         *   }
         */

        // Placeholder: no-op migration to establish the pattern.
        // All existing users on version 9 will keep their data.

        /**
         * Migration 9→10: Multi-spouse support
         * - Add spouseIds column (comma-separated, replaces single spouseId)
         * - Add spouseOrder column (0=primary, 1=vợ cả, 2=vợ hai, ...)
         * - Migrate existing spouseId data into spouseIds
         * - Remove spouseId column (via table rebuild)
         */
        private val MIGRATION_9_10 = Migration(9, 10) { db ->
            Log.d(TAG, "Running MIGRATION_9_10: Multi-spouse support")
            // Add new columns
            db.execSQL("ALTER TABLE family_members ADD COLUMN spouseIds TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE family_members ADD COLUMN spouseOrder INTEGER NOT NULL DEFAULT 0")
            // Migrate existing spouseId → spouseIds
            db.execSQL("UPDATE family_members SET spouseIds = spouseId WHERE spouseId IS NOT NULL AND spouseId != ''")
            // Remove old spouseId column via table rebuild
            db.execSQL("""
                CREATE TABLE family_members_new (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    role TEXT NOT NULL,
                    gender TEXT NOT NULL,
                    generation INTEGER NOT NULL,
                    birthYear INTEGER,
                    deathYear INTEGER,
                    birthDateLunar TEXT,
                    deathDateLunar TEXT,
                    canChi TEXT,
                    menh TEXT,
                    zodiacEmoji TEXT,
                    menhEmoji TEXT,
                    hanhEmoji TEXT,
                    menhDetail TEXT,
                    zodiacName TEXT,
                    menhName TEXT,
                    hometown TEXT,
                    occupation TEXT,
                    isSelf INTEGER NOT NULL DEFAULT 0,
                    isElder INTEGER NOT NULL DEFAULT 0,
                    emoji TEXT NOT NULL DEFAULT '👤',
                    spouseIds TEXT NOT NULL DEFAULT '',
                    spouseOrder INTEGER NOT NULL DEFAULT 0,
                    parentIds TEXT NOT NULL DEFAULT '',
                    note TEXT,
                    avatarPath TEXT,
                    createdAt INTEGER NOT NULL DEFAULT 0,
                    updatedAt INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())
            db.execSQL("""
                INSERT INTO family_members_new 
                SELECT id, name, role, gender, generation, birthYear, deathYear,
                       birthDateLunar, deathDateLunar, canChi, menh, zodiacEmoji, menhEmoji,
                       hanhEmoji, menhDetail, zodiacName, menhName, hometown, occupation,
                       isSelf, isElder, emoji, spouseIds, spouseOrder, parentIds,
                       note, avatarPath, createdAt, updatedAt
                FROM family_members
            """.trimIndent())
            db.execSQL("DROP TABLE family_members")
            db.execSQL("ALTER TABLE family_members_new RENAME TO family_members")
            Log.d(TAG, "MIGRATION_9_10 complete")
        }

        /**
         * Migration 10→11: PointsEngine v2
         * Add 5 new tables for the points/unlock/streak domain.
         * See docs/POINTS_ENGINE_DATA_MODEL.md.
         */
        private val MIGRATION_10_11 = Migration(10, 11) { db ->
            Log.d(TAG, "Running MIGRATION_10_11: PointsEngine v2 tables")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS points_ledger (
                    id INTEGER NOT NULL PRIMARY KEY,
                    dailyPoints INTEGER NOT NULL DEFAULT 0,
                    spentDailyPoints INTEGER NOT NULL DEFAULT 0,
                    permanentPoints INTEGER NOT NULL DEFAULT 0,
                    lastResetEpochDay INTEGER NOT NULL DEFAULT 0,
                    updatedAt INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS action_log (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    actionType TEXT NOT NULL,
                    dailyPointsAwarded INTEGER NOT NULL,
                    permanentPointsAwarded INTEGER NOT NULL,
                    streakMultiplierApplied REAL NOT NULL,
                    epochDay INTEGER NOT NULL,
                    timestamp INTEGER NOT NULL DEFAULT 0,
                    metadata TEXT
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_action_log_actionType_epochDay ON action_log(actionType, epochDay)")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS daily_unlock (
                    unlockKey TEXT NOT NULL,
                    epochDay INTEGER NOT NULL,
                    cost INTEGER NOT NULL,
                    unlockedAt INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(unlockKey, epochDay)
                )
            """.trimIndent())
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS permanent_unlock (
                    unlockKey TEXT NOT NULL PRIMARY KEY,
                    rank TEXT NOT NULL,
                    unlockedAt INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS streak_record (
                    id INTEGER NOT NULL PRIMARY KEY,
                    currentStreak INTEGER NOT NULL DEFAULT 0,
                    longestStreak INTEGER NOT NULL DEFAULT 0,
                    lastCheckInEpochDay INTEGER NOT NULL DEFAULT 0,
                    freezeTokens INTEGER NOT NULL DEFAULT 0,
                    lastFreezeGrantedMonth INTEGER NOT NULL DEFAULT 0,
                    updatedAt INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())
            Log.d(TAG, "MIGRATION_10_11 complete")
        }

        /**
         * Migration 11→12: Countdown events table
         */
        private val MIGRATION_12_13 = Migration(12, 13) { db ->
            Log.d(TAG, "Running MIGRATION_12_13: world clock cities + cycle tracker tables")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS world_clock_cities (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    cityName TEXT NOT NULL,
                    timezone TEXT NOT NULL,
                    country TEXT NOT NULL DEFAULT '',
                    sortOrder INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS cycle_settings (
                    id INTEGER NOT NULL PRIMARY KEY,
                    cycleLength INTEGER NOT NULL DEFAULT 28,
                    periodLength INTEGER NOT NULL DEFAULT 5
                )
            """.trimIndent())
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS cycle_logs (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    startEpochDay INTEGER NOT NULL,
                    endEpochDay INTEGER NOT NULL DEFAULT -1,
                    notes TEXT NOT NULL DEFAULT '',
                    createdAt INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_cycle_logs_startEpochDay ON cycle_logs(startEpochDay)")
            Log.d(TAG, "MIGRATION_12_13 complete")
        }

        /**
         * Migration 13→14: quiz offline sync queue
         */
        private val MIGRATION_13_14 = Migration(13, 14) { db ->
            Log.d(TAG, "Running MIGRATION_13_14: quiz offline sync queue")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS quiz_offline_sessions (
                    clientSessionId TEXT NOT NULL PRIMARY KEY,
                    sessionType TEXT NOT NULL,
                    category TEXT,
                    questionIdsJson TEXT NOT NULL,
                    answersJson TEXT NOT NULL,
                    startedAtMs INTEGER NOT NULL,
                    finishedAtMs INTEGER NOT NULL,
                    createdAtMs INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())
            Log.d(TAG, "MIGRATION_13_14 complete")
        }

        /**
         * Migration 14→15: Hợp nhất tasks + notes + reminders → bảng "items".
         * Theo quyết định sản phẩm: bắt đầu mới (không cần giữ dữ liệu 3 bảng cũ),
         * nên ở đây drop 3 bảng cũ và tạo bảng items mới. Các bảng khác giữ nguyên.
         */
        private val MIGRATION_14_15 = Migration(14, 15) { db ->
            Log.d(TAG, "Running MIGRATION_14_15: unify tasks/notes/reminders → items")
            db.execSQL("DROP TABLE IF EXISTS tasks")
            db.execSQL("DROP TABLE IF EXISTS notes")
            db.execSQL("DROP TABLE IF EXISTS reminders")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS items (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL DEFAULT '',
                    tags TEXT NOT NULL DEFAULT '',
                    isTask INTEGER NOT NULL DEFAULT 0,
                    isDone INTEGER NOT NULL DEFAULT 0,
                    priority INTEGER NOT NULL DEFAULT 1,
                    dueDate INTEGER,
                    dueTime TEXT,
                    hasReminder INTEGER NOT NULL DEFAULT 0,
                    reminderAt INTEGER,
                    repeatType INTEGER NOT NULL DEFAULT 0,
                    useLunar INTEGER NOT NULL DEFAULT 0,
                    advanceDays INTEGER NOT NULL DEFAULT 0,
                    reminderEnabled INTEGER NOT NULL DEFAULT 1,
                    isPinned INTEGER NOT NULL DEFAULT 0,
                    colorIndex INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL DEFAULT 0,
                    updatedAt INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_items_dueDate ON items(dueDate)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_items_reminderAt ON items(reminderAt)")
            Log.d(TAG, "MIGRATION_14_15 complete")
        }

        private val MIGRATION_11_12 = Migration(11, 12) { db ->
            Log.d(TAG, "Running MIGRATION_11_12: countdown events")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS countdown_events (
                    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    targetEpochDay INTEGER NOT NULL,
                    note TEXT NOT NULL DEFAULT '',
                    showOnHome INTEGER NOT NULL DEFAULT 1,
                    showOnWidget INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_countdown_events_targetEpochDay ON countdown_events(targetEpochDay)")
            Log.d(TAG, "MIGRATION_11_12 complete")
        }

        fun getInstance(context: Context): LichSoDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LichSoDatabase::class.java,
                    "lichso.db"
                )
                    // Migrations
                    .addMigrations(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15)
                    .fallbackToDestructiveMigrationFrom(
                        // Only allow destructive migration from very old versions (pre-release)
                        // that we don't need to support. Current users on v9 are safe.
                        1, 2, 3, 4, 5, 6, 7, 8
                    )
                    // Safety net: prevent crash on unexpected version mismatch (e.g., downgrade)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
