# 📐 PointsEngine – Data Model & Use Cases

> **Module**: `points` (core domain cho Lịch Số v2.0)
> **Platform**: Android first (Kotlin + Room + Hilt + Coroutines/Flow), mirror sang iOS (SwiftData) sau
> **Package**: `com.lichso.app.feature.points`

---

## 🎯 Mục tiêu module

1. Lưu trữ **⚡ Điểm ngày** (reset 00:00) và **☯️ Điểm vĩnh viễn** (cumulative)
2. Ghi nhận mọi **hành động** (action) có tính điểm, kèm daily cap
3. Quản lý **Streak** (chuỗi ngày check-in liên tiếp) + freeze
4. Quản lý **Daily Unlocks** (tiêu điểm ngày mở tính năng hôm nay)
5. Quản lý **Permanent Unlocks** (đạt mốc tích luỹ → unlock vĩnh viễn)
6. Expose **Flow reactive** để UI auto-update thanh tiến trình

---

## 1. Kiến trúc tổng thể

```
┌──────────────────────────────────────────────────────────┐
│                       UI (Compose)                       │
│  DailyUnlockCard │ StreakBadge │ PointsProgressBar        │
└────────────┬─────────────────────────────────┬───────────┘
             │ observe Flow                    │ action
             ▼                                 ▼
┌──────────────────────────────────────────────────────────┐
│               PointsEngine (Use cases)                   │
│  AwardPoints │ SpendDailyPoints │ CheckUnlock │ Streak   │
└────────────┬─────────────────────────────────┬───────────┘
             │                                 │
             ▼                                 ▼
┌──────────────────────────────────────────────────────────┐
│              Repository (single source)                  │
│    PointsRepository (Room + DataStore + Clock)           │
└────────────┬─────────────────────────────────┬───────────┘
             │                                 │
             ▼                                 ▼
┌────────────────────────┐      ┌──────────────────────────┐
│  Room Database         │      │  DataStore (Preferences) │
│  - PointsLedger        │      │  - lastResetDate         │
│  - ActionLog           │      │  - freezeTokens          │
│  - DailyUnlock         │      │  - firstInstallDate      │
│  - PermanentUnlock     │      │                          │
│  - StreakRecord        │      │                          │
└────────────────────────┘      └──────────────────────────┘
```

---

## 2. Enums & Value Objects

### 2.1 `ActionType` – tất cả hành động cộng điểm

```kotlin
enum class ActionType(
    val dailyPoints: Int,
    val permanentPoints: Int,
    val dailyCap: Int,           // -1 = unlimited
    val category: ActionCategory
) {
    // Daily engagement
    DAILY_CHECK_IN       (10, 5,   1,  ActionCategory.ENGAGEMENT),
    VIEW_FORTUNE_CARD    (5,  2,   1,  ActionCategory.ENGAGEMENT),
    DRAW_KINH_DICH       (15, 5,   1,  ActionCategory.ENGAGEMENT),

    // Screen visits
    VISIT_LUNAR_CALENDAR (3,  1,   3,  ActionCategory.NAVIGATION),
    VISIT_VAN_KHAN       (5,  2,   3,  ActionCategory.NAVIGATION),
    VISIT_TU_VI          (4,  2,   3,  ActionCategory.NAVIGATION),

    // Deep engagement
    READ_VAN_KHAN_FULL   (20, 10,  5,  ActionCategory.DEEP),
    CHAT_AI_MESSAGE      (2,  1,   10, ActionCategory.DEEP),
    CREATE_REMINDER      (5,  3,   5,  ActionCategory.DEEP),
    COMPLETE_REMINDER    (10, 5,  -1,  ActionCategory.DEEP),

    // Viral / Share
    SHARE_TO_SOCIAL      (30, 20,  3,  ActionCategory.VIRAL),
    INVITE_FRIEND_SENT   (0,  200, -1, ActionCategory.VIRAL),
    INVITE_FRIEND_INSTALLED(0, 500, -1, ActionCategory.VIRAL),
    RATE_APP_5_STAR      (0,  1000, 1, ActionCategory.VIRAL),

    // Rewarded ads
    WATCH_REWARDED_AD    (20, 0,   5,  ActionCategory.AD),

    // Location / Spiritual
    CHECKIN_TEMPLE       (0,  50,  3,  ActionCategory.LOCATION),

    // Streak milestones (awarded automatically)
    STREAK_7_DAYS        (0,  300,  1, ActionCategory.MILESTONE),
    STREAK_30_DAYS       (0,  1500, 1, ActionCategory.MILESTONE),
    STREAK_100_DAYS      (0,  10_000, 1, ActionCategory.MILESTONE),
    STREAK_365_DAYS      (0,  50_000, 1, ActionCategory.MILESTONE),
}

enum class ActionCategory {
    ENGAGEMENT, NAVIGATION, DEEP, VIRAL, AD, LOCATION, MILESTONE
}
```

### 2.2 `StreakTier` – nhân hệ số

```kotlin
enum class StreakTier(
    val minDays: Int,
    val dailyMultiplier: Float,
    val permanentMultiplier: Float,
    val displayName: String
) {
    BEGINNER (0,   1.0f, 1.0f, "Tân thủ"),
    WEEK     (7,   1.5f, 1.2f, "Tu tập"),
    MONTH    (30,  2.0f, 1.5f, "Kiên tâm"),
    CENTURY  (100, 3.0f, 2.0f, "Đại định"),
    YEAR     (365, 5.0f, 3.0f, "Thiên mệnh");

    companion object {
        fun fromStreak(days: Int): StreakTier =
            entries.last { days >= it.minDays }
    }
}
```

### 2.3 `DailyUnlockKey` – tính năng mở theo ngày

```kotlin
enum class DailyUnlockKey(val cost: Int, val label: String) {
    DETAILED_HOROSCOPE     (20, "Tử vi chi tiết hôm nay"),
    VAN_KHAN_FULL          (30, "Văn khấn đầy đủ"),
    AI_MASTER_10_MSG       (40, "AI Thầy Số (10 tin)"),
    SEASONAL_THEME         (25, "Theme hôm nay"),
    LUCKY_HOURS_FULL       (15, "Giờ hoàng đạo chi tiết"),
    WEEK_FORTUNE_CHART     (50, "Biểu đồ vận hạn tuần"),
    LA_SO_TU_VI            (60, "Lá số tử vi hôm nay"),
    DAILY_ZODIAC_CARD      (20, "Mở thẻ 12 con giáp"),
    CHOOSE_DAY_TOOL        (150, "Chọn ngày tốt (1 lần)"),
}
```

### 2.4 `PermanentRank` – cấp bậc tích luỹ

```kotlin
enum class PermanentRank(
    val threshold: Long,
    val displayName: String,
    val unlocks: List<PermanentUnlockKey>
) {
    NOVICE    (0,       "Vô danh",   emptyList()),
    SO_CO     (500,     "Sơ cơ",     listOf(REMOVE_ADS, THEME_TRANG_RAM)),
    TU_TAP    (2_000,   "Tu tập",    listOf(AI_20_MSG_PER_DAY, WATERMARKED_EXPORT)),
    THONG_THAO(5_000,   "Thông thạo",listOf(LUCKY_HOURS_PERMANENT, PREMIUM_THEMES)),
    DAO_SI    (15_000,  "Đạo sĩ",    listOf(AR_FENG_SHUI_WEEKLY, CHOOSE_DAY_TOOL_PERM)),
    CHAN_NHAN (40_000,  "Chân nhân", listOf(AI_UNLIMITED, BABY_NAME_TOOL, PDF_EXPORT)),
    THIEN_SU  (100_000, "Thiên sư",  listOf(ALL_PREMIUM, BADGE_CROWN, GOLD_FRAME));

    companion object {
        fun fromTotal(total: Long): PermanentRank =
            entries.last { total >= it.threshold }

        fun nextOf(current: PermanentRank): PermanentRank? =
            entries.getOrNull(current.ordinal + 1)
    }
}

enum class PermanentUnlockKey {
    REMOVE_ADS, THEME_TRANG_RAM,
    AI_20_MSG_PER_DAY, WATERMARKED_EXPORT,
    LUCKY_HOURS_PERMANENT, PREMIUM_THEMES,
    AR_FENG_SHUI_WEEKLY, CHOOSE_DAY_TOOL_PERM,
    AI_UNLIMITED, BABY_NAME_TOOL, PDF_EXPORT,
    ALL_PREMIUM, BADGE_CROWN, GOLD_FRAME,
}
```

---

## 3. Room Entities

### 3.1 `PointsLedgerEntity` – balance hiện tại (1 row duy nhất, id=1)

```kotlin
@Entity(tableName = "points_ledger")
data class PointsLedgerEntity(
    @PrimaryKey val id: Int = 1,         // singleton
    val dailyPoints: Int,                // ⚡ reset 00:00
    val spentDailyPoints: Int,           // ⚡ đã tiêu hôm nay (cho analytics)
    val permanentPoints: Long,           // ☯️ tích luỹ
    val lastResetEpochDay: Long,         // ngày reset gần nhất (LocalDate.toEpochDay)
    val updatedAt: Long = System.currentTimeMillis()
)
```

### 3.2 `ActionLogEntity` – lịch sử mỗi action (phục vụ cap + analytics)

```kotlin
@Entity(
    tableName = "action_log",
    indices = [Index(value = ["actionType", "epochDay"])]
)
data class ActionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionType: String,              // ActionType.name
    val dailyPointsAwarded: Int,
    val permanentPointsAwarded: Int,
    val streakMultiplierApplied: Float,
    val epochDay: Long,                  // để count cap/ngày
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: String? = null         // JSON (screen name, share target...)
)
```

### 3.3 `DailyUnlockEntity` – tính năng đã unlock hôm nay

```kotlin
@Entity(
    tableName = "daily_unlock",
    primaryKeys = ["unlockKey", "epochDay"]
)
data class DailyUnlockEntity(
    val unlockKey: String,               // DailyUnlockKey.name
    val epochDay: Long,
    val cost: Int,
    val unlockedAt: Long = System.currentTimeMillis()
)
```

### 3.4 `PermanentUnlockEntity` – đã unlock vĩnh viễn

```kotlin
@Entity(tableName = "permanent_unlock")
data class PermanentUnlockEntity(
    @PrimaryKey val unlockKey: String,   // PermanentUnlockKey.name
    val rank: String,                    // PermanentRank.name khi đạt
    val unlockedAt: Long = System.currentTimeMillis()
)
```

### 3.5 `StreakRecordEntity` – streak hiện tại

```kotlin
@Entity(tableName = "streak_record")
data class StreakRecordEntity(
    @PrimaryKey val id: Int = 1,         // singleton
    val currentStreak: Int,
    val longestStreak: Int,
    val lastCheckInEpochDay: Long,
    val freezeTokens: Int,               // số lần freeze còn
    val lastFreezeGrantedMonth: Int,     // YYYYMM, để grant free freeze/tháng
    val updatedAt: Long = System.currentTimeMillis()
)
```

---

## 4. DAOs

### 4.1 `PointsDao`

```kotlin
@Dao
interface PointsDao {
    @Query("SELECT * FROM points_ledger WHERE id = 1")
    fun observeLedger(): Flow<PointsLedgerEntity?>

    @Query("SELECT * FROM points_ledger WHERE id = 1")
    suspend fun getLedger(): PointsLedgerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLedger(entity: PointsLedgerEntity)

    @Insert
    suspend fun logAction(log: ActionLogEntity)

    @Query("""
        SELECT COUNT(*) FROM action_log
        WHERE actionType = :type AND epochDay = :day
    """)
    suspend fun countActionToday(type: String, day: Long): Int

    @Query("DELETE FROM action_log WHERE epochDay < :beforeDay")
    suspend fun pruneOldLogs(beforeDay: Long)   // giữ 90 ngày
}
```

### 4.2 `UnlockDao`

```kotlin
@Dao
interface UnlockDao {
    // Daily
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDailyUnlock(entity: DailyUnlockEntity): Long

    @Query("""
        SELECT EXISTS(SELECT 1 FROM daily_unlock
        WHERE unlockKey = :key AND epochDay = :day)
    """)
    fun observeDailyUnlocked(key: String, day: Long): Flow<Boolean>

    @Query("SELECT * FROM daily_unlock WHERE epochDay = :day")
    fun observeTodayUnlocks(day: Long): Flow<List<DailyUnlockEntity>>

    // Permanent
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermanentUnlock(entity: PermanentUnlockEntity)

    @Query("SELECT * FROM permanent_unlock")
    fun observeAllPermanent(): Flow<List<PermanentUnlockEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM permanent_unlock WHERE unlockKey = :key)")
    fun observePermanentUnlocked(key: String): Flow<Boolean>
}
```

### 4.3 `StreakDao`

```kotlin
@Dao
interface StreakDao {
    @Query("SELECT * FROM streak_record WHERE id = 1")
    fun observeStreak(): Flow<StreakRecordEntity?>

    @Query("SELECT * FROM streak_record WHERE id = 1")
    suspend fun getStreak(): StreakRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStreak(entity: StreakRecordEntity)
}
```

### 4.4 Database

```kotlin
@Database(
    entities = [
        PointsLedgerEntity::class,
        ActionLogEntity::class,
        DailyUnlockEntity::class,
        PermanentUnlockEntity::class,
        StreakRecordEntity::class,
    ],
    version = 1,
    exportSchema = true
)
abstract class PointsDatabase : RoomDatabase() {
    abstract fun pointsDao(): PointsDao
    abstract fun unlockDao(): UnlockDao
    abstract fun streakDao(): StreakDao
}
```

> 💡 Có thể merge vào `LichSoDatabase` hiện có, chỉ cần thêm migration.

---

## 5. Domain models (expose lên UI)

```kotlin
data class PointsBalance(
    val daily: Int,
    val permanent: Long,
    val rank: PermanentRank,
    val nextRank: PermanentRank?,
    val progressToNextRank: Float           // 0f..1f
)

data class StreakState(
    val current: Int,
    val longest: Int,
    val tier: StreakTier,
    val freezeTokens: Int,
    val missedToday: Boolean,               // true nếu chưa check-in hôm nay
    val daysToNextTier: Int?
)

data class DailyUnlockState(
    val key: DailyUnlockKey,
    val isUnlocked: Boolean,
    val cost: Int,
    val currentDaily: Int,
    val canAfford: Boolean,
    val shortestPath: List<ActionSuggestion> // gợi ý action để đủ điểm
)

data class ActionSuggestion(
    val action: ActionType,
    val potentialGain: Int,
    val label: String,                       // "Rút quẻ hôm nay"
    val deeplink: String                     // "lichso://draw"
)

sealed interface AwardResult {
    data class Success(
        val dailyAwarded: Int,
        val permanentAwarded: Long,
        val multiplier: Float,
        val newRankUnlocked: PermanentRank?
    ) : AwardResult
    data object CappedForToday : AwardResult
    data object AlreadyRewarded : AwardResult
}

sealed interface SpendResult {
    data object Success : SpendResult
    data class InsufficientPoints(val needed: Int, val have: Int) : SpendResult
    data object AlreadyUnlocked : SpendResult
}
```

---

## 6. Use Cases (Clean Architecture)

### 6.1 `AwardPointsUseCase`

```kotlin
class AwardPointsUseCase @Inject constructor(
    private val repo: PointsRepository,
    private val streakRepo: StreakRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(
        action: ActionType,
        metadata: Map<String, String> = emptyMap()
    ): AwardResult {
        val today = clock.todayEpochDay()

        // 1. Check cap
        if (action.dailyCap != -1) {
            val count = repo.countActionToday(action, today)
            if (count >= action.dailyCap) return AwardResult.CappedForToday
        }

        // 2. Reset daily points nếu sang ngày mới
        repo.resetDailyIfNeeded(today)

        // 3. Tính multiplier từ streak
        val streak = streakRepo.getCurrent()
        val tier = StreakTier.fromStreak(streak.currentStreak)
        val dailyGain = (action.dailyPoints * tier.dailyMultiplier).toInt()
        val permGain = (action.permanentPoints * tier.permanentMultiplier).toLong()

        // 4. Update ledger
        val prevRank = repo.currentRank()
        repo.addPoints(dailyGain, permGain, today)
        val newRank = repo.currentRank()

        // 5. Log action
        repo.logAction(action, dailyGain, permGain, tier.dailyMultiplier, today, metadata)

        // 6. Grant permanent unlocks nếu vừa lên rank
        val rankedUp = newRank.ordinal > prevRank.ordinal
        if (rankedUp) repo.grantUnlocksForRank(newRank)

        return AwardResult.Success(
            dailyAwarded = dailyGain,
            permanentAwarded = permGain,
            multiplier = tier.dailyMultiplier,
            newRankUnlocked = if (rankedUp) newRank else null
        )
    }
}
```

### 6.2 `SpendDailyPointsUseCase`

```kotlin
class SpendDailyPointsUseCase @Inject constructor(
    private val repo: PointsRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(key: DailyUnlockKey): SpendResult {
        val today = clock.todayEpochDay()
        repo.resetDailyIfNeeded(today)

        if (repo.isDailyUnlocked(key, today)) return SpendResult.AlreadyUnlocked

        val balance = repo.getLedger().dailyPoints
        if (balance < key.cost) {
            return SpendResult.InsufficientPoints(key.cost, balance)
        }

        repo.spendDaily(key.cost)
        repo.insertDailyUnlock(key, today)
        return SpendResult.Success
    }
}
```

### 6.3 `CheckInStreakUseCase`

```kotlin
class CheckInStreakUseCase @Inject constructor(
    private val streakRepo: StreakRepository,
    private val award: AwardPointsUseCase,
    private val clock: Clock
) {
    suspend operator fun invoke(): StreakCheckInResult {
        val today = clock.todayEpochDay()
        val state = streakRepo.getCurrent()

        if (state.lastCheckInEpochDay == today) {
            return StreakCheckInResult.AlreadyCheckedIn
        }

        val diff = today - state.lastCheckInEpochDay
        val newStreak = when {
            state.lastCheckInEpochDay == 0L -> 1          // first ever
            diff == 1L -> state.currentStreak + 1         // continuing
            diff == 2L && state.freezeTokens > 0 -> {
                streakRepo.consumeFreeze()
                state.currentStreak + 1                   // saved by freeze
            }
            else -> 1                                     // broken, restart
        }

        streakRepo.update(
            currentStreak = newStreak,
            longestStreak = max(newStreak, state.longestStreak),
            lastCheckInEpochDay = today
        )

        // Grant free freeze mỗi tháng
        streakRepo.grantMonthlyFreezeIfDue(today)

        // Award check-in action
        award(ActionType.DAILY_CHECK_IN)

        // Milestone bonuses
        when (newStreak) {
            7   -> award(ActionType.STREAK_7_DAYS)
            30  -> award(ActionType.STREAK_30_DAYS)
            100 -> award(ActionType.STREAK_100_DAYS)
            365 -> award(ActionType.STREAK_365_DAYS)
        }

        return StreakCheckInResult.Success(newStreak, diff == 2L)
    }
}

sealed interface StreakCheckInResult {
    data class Success(val newStreak: Int, val freezeUsed: Boolean) : StreakCheckInResult
    data object AlreadyCheckedIn : StreakCheckInResult
}
```

### 6.4 `ObserveDailyUnlockStateUseCase`

```kotlin
class ObserveDailyUnlockStateUseCase @Inject constructor(
    private val repo: PointsRepository,
    private val suggester: UnlockSuggester,
    private val clock: Clock
) {
    operator fun invoke(key: DailyUnlockKey): Flow<DailyUnlockState> {
        val today = clock.todayEpochDay()
        return combine(
            repo.observeLedger(),
            repo.observeDailyUnlocked(key, today)
        ) { ledger, unlocked ->
            val daily = ledger?.dailyPoints ?: 0
            DailyUnlockState(
                key = key,
                isUnlocked = unlocked,
                cost = key.cost,
                currentDaily = daily,
                canAfford = daily >= key.cost,
                shortestPath = if (!unlocked && daily < key.cost)
                    suggester.suggest(needed = key.cost - daily, today = today)
                else emptyList()
            )
        }
    }
}
```

### 6.5 `UnlockSuggester`

```kotlin
class UnlockSuggester @Inject constructor(
    private val repo: PointsRepository,
    private val streakRepo: StreakRepository
) {
    suspend fun suggest(needed: Int, today: Long): List<ActionSuggestion> {
        val tier = StreakTier.fromStreak(streakRepo.getCurrent().currentStreak)
        val candidates = ActionType.entries
            .filter { it.dailyPoints > 0 && it.category != ActionCategory.MILESTONE }
            .map { action ->
                val done = repo.countActionToday(action, today)
                val remaining = if (action.dailyCap == -1) Int.MAX_VALUE
                                else (action.dailyCap - done).coerceAtLeast(0)
                action to remaining
            }
            .filter { (_, remaining) -> remaining > 0 }

        // Greedy: chọn action có dailyPoints cao nhất / còn slot
        val result = mutableListOf<ActionSuggestion>()
        var remainingNeeded = needed
        for ((action, remaining) in candidates.sortedByDescending { it.first.dailyPoints }) {
            if (remainingNeeded <= 0) break
            val gain = (action.dailyPoints * tier.dailyMultiplier).toInt()
            val times = min(remaining, ceil(remainingNeeded.toDouble() / gain).toInt())
            result += ActionSuggestion(
                action = action,
                potentialGain = gain * times,
                label = action.label(),
                deeplink = action.deeplink()
            )
            remainingNeeded -= gain * times
        }
        return result.take(3)   // top 3 gợi ý
    }
}
```

### 6.6 `ObservePointsBalanceUseCase`

```kotlin
class ObservePointsBalanceUseCase @Inject constructor(
    private val repo: PointsRepository
) {
    operator fun invoke(): Flow<PointsBalance> =
        repo.observeLedger().map { ledger ->
            val total = ledger?.permanentPoints ?: 0L
            val rank = PermanentRank.fromTotal(total)
            val next = PermanentRank.nextOf(rank)
            val progress = next?.let {
                ((total - rank.threshold).toFloat() /
                 (it.threshold - rank.threshold).toFloat()).coerceIn(0f, 1f)
            } ?: 1f
            PointsBalance(
                daily = ledger?.dailyPoints ?: 0,
                permanent = total,
                rank = rank,
                nextRank = next,
                progressToNextRank = progress
            )
        }
}
```

---

## 7. Repository layer

```kotlin
interface PointsRepository {
    fun observeLedger(): Flow<PointsLedgerEntity?>
    fun observeDailyUnlocked(key: DailyUnlockKey, day: Long): Flow<Boolean>
    fun observePermanentUnlocks(): Flow<Set<PermanentUnlockKey>>

    suspend fun getLedger(): PointsLedgerEntity
    suspend fun resetDailyIfNeeded(today: Long)
    suspend fun addPoints(daily: Int, permanent: Long, today: Long)
    suspend fun spendDaily(amount: Int)
    suspend fun countActionToday(action: ActionType, today: Long): Int
    suspend fun logAction(
        action: ActionType, daily: Int, perm: Long,
        mult: Float, today: Long, metadata: Map<String, String>
    )
    suspend fun isDailyUnlocked(key: DailyUnlockKey, today: Long): Boolean
    suspend fun insertDailyUnlock(key: DailyUnlockKey, today: Long)
    suspend fun currentRank(): PermanentRank
    suspend fun grantUnlocksForRank(rank: PermanentRank)
}
```

**Implementation notes**:
- Tất cả mutations wrap trong `withTransaction { }` để đảm bảo atomic
- `resetDailyIfNeeded` so sánh `lastResetEpochDay` với `today`, nếu khác thì set `dailyPoints = 0`
- `PointsRepository` **là singleton** (Hilt `@Singleton`), tránh race condition
- Dùng `Mutex` cho các critical section (award + spend)

---

## 8. Dependency Injection (Hilt)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object PointsModule {

    @Provides @Singleton
    fun providePointsDatabase(@ApplicationContext ctx: Context): PointsDatabase =
        Room.databaseBuilder(ctx, PointsDatabase::class.java, "points.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun providePointsDao(db: PointsDatabase) = db.pointsDao()
    @Provides fun provideUnlockDao(db: PointsDatabase) = db.unlockDao()
    @Provides fun provideStreakDao(db: PointsDatabase) = db.streakDao()

    @Provides @Singleton
    fun provideClock(): Clock = SystemClock()

    @Binds @Singleton
    abstract fun bindPointsRepository(
        impl: PointsRepositoryImpl
    ): PointsRepository
}
```

---

## 9. Usage trong UI (Compose)

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val checkIn: CheckInStreakUseCase,
    private val awardPoints: AwardPointsUseCase,
    observeBalance: ObservePointsBalanceUseCase,
    observeStreak: ObserveStreakUseCase,
) : ViewModel() {

    val balance: StateFlow<PointsBalance?> = observeBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val streak: StateFlow<StreakState?> = observeStreak()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch { checkIn() }      // auto check-in on app open
    }

    fun onFortuneCardViewed() = viewModelScope.launch {
        awardPoints(ActionType.VIEW_FORTUNE_CARD)
    }
}
```

---

## 10. Testing checklist

- [ ] `AwardPointsUseCase`: cap enforcement, multiplier calc, rank-up detection
- [ ] `SpendDailyPointsUseCase`: insufficient, already unlocked, happy path
- [ ] `CheckInStreakUseCase`: continuing, broken, freeze consumed, monthly freeze grant
- [ ] `UnlockSuggester`: greedy optimality, cap-awareness
- [ ] Daily reset at 00:00 (timezone-aware — dùng `LocalDate.now(ZoneId.systemDefault())`)
- [ ] Migration from v1 database (no points data)
- [ ] Concurrent award calls (Mutex)
- [ ] Action log pruning (giữ 90 ngày)

---

## 11. Analytics events

Mỗi award / spend / unlock → emit Firebase event:

```kotlin
Analytics.log("points_awarded", bundleOf(
    "action" to action.name,
    "daily" to daily,
    "permanent" to perm,
    "multiplier" to mult,
    "streak" to streak
))

Analytics.log("daily_unlock_purchased", bundleOf(
    "key" to key.name,
    "cost" to key.cost,
    "balance_after" to balance
))

Analytics.log("permanent_rank_up", bundleOf(
    "from" to prevRank.name,
    "to" to newRank.name,
    "days_to_reach" to daysFromInstall
))
```

---

## 12. Sample flows

### Flow 1: Mở app buổi sáng
```
App open
  → HomeVM.init → checkIn() → streak+1, +10⚡ +5☯️
  → Show Fortune Card → onFortuneCardViewed() → +5⚡ +2☯️
  → User tap "Rút quẻ" → award(DRAW_KINH_DICH) → +15⚡ +5☯️
  Total: 30⚡ accumulated
```

### Flow 2: Unlock AI Thầy Số (cần 40⚡)
```
Balance: 30⚡ → not enough
UI shows suggestion: "Đọc văn khấn (+20⚡)"
User reads → award(READ_VAN_KHAN_FULL) → +20⚡
Balance: 50⚡ ✅
User taps unlock AI → spend(AI_MASTER_10_MSG) → -40⚡
Balance: 10⚡, AI unlocked for today
```

### Flow 3: Rank up
```
Permanent: 498 ☯️
User shares → award(SHARE_TO_SOCIAL) → +20☯️
Permanent: 518 ☯️ → crosses 500
→ Rank: NOVICE → SO_CO
→ grantUnlocksForRank(SO_CO) → REMOVE_ADS + THEME_TRANG_RAM
→ UI shows 🎉 dialog "Chúc mừng! Bạn đã đạt Sơ cơ"
```
