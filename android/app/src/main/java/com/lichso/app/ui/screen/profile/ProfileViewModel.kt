package com.lichso.app.ui.screen.profile

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lichso.app.data.auth.AuthRepository
import com.lichso.app.data.auth.UserInfo
import com.lichso.app.data.local.AppBackupManager
import com.lichso.app.data.local.dao.*
import com.lichso.app.data.local.entity.BookmarkEntity
import com.lichso.app.ui.screen.settings.settingsDataStore
import com.lichso.app.util.CanChiCalculator
import com.lichso.app.util.LunarCalendarUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

// ── DataStore keys for profile ──
object ProfileKeys {
    val DISPLAY_NAME = stringPreferencesKey("profile_display_name")
    val EMAIL = stringPreferencesKey("profile_email")
    val AVATAR_PATH = stringPreferencesKey("profile_avatar_path") // internal file path
    val BIRTH_DAY = intPreferencesKey("profile_birth_day")
    val BIRTH_MONTH = intPreferencesKey("profile_birth_month")
    val BIRTH_YEAR = intPreferencesKey("profile_birth_year")
    val BIRTH_HOUR = intPreferencesKey("profile_birth_hour")
    val BIRTH_MINUTE = intPreferencesKey("profile_birth_minute")
    val GENDER = stringPreferencesKey("profile_gender") // "Nam", "Nữ", "Khác"
    val SAVED_DAYS_JSON = stringPreferencesKey("profile_saved_days")
}

data class SavedDay(
    val day: Int,
    val month: Int,
    val year: Int,
    val label: String,
    val isLunar: Boolean = false
)

data class BirthInfo(
    val yearCanChi: String = "",
    val menh: String = "",
    val nguHanh: String = "",
    val conGiap: String = "",
    val conGiapEmoji: String = "",
    val cung: String = ""
)

data class ProfileUiState(
    // Profile data
    val displayName: String = "Người dùng",
    val email: String = "",
    val avatarPath: String = "", // internal file path for avatar image
    val birthDay: Int = 0,
    val birthMonth: Int = 0,
    val birthYear: Int = 0,
    val birthHour: Int = -1,
    val birthMinute: Int = -1,
    val gender: String = "Nam",

    // Computed birth info
    val birthInfo: BirthInfo = BirthInfo(),

    // Auth user (from Firebase)
    val authUser: UserInfo? = null,

    // Stats
    val noteCount: Int = 0,
    val reminderCount: Int = 0,
    val savedDaysCount: Int = 0,
    val bookmarkCount: Int = 0,

    // Saved days
    val savedDays: List<SavedDay> = emptyList(),

    // Bookmarks from Room DB
    val allBookmarks: List<BookmarkEntity> = emptyList(),

    // Dialogs
    val showEditProfileSheet: Boolean = false,
    val showSignOutDialog: Boolean = false,
    val showAddSavedDayDialog: Boolean = false,

    // Edit form state
    val editName: String = "",
    val editEmail: String = "",
    val editBirthDay: String = "",
    val editBirthMonth: String = "",
    val editBirthYear: String = "",
    val editBirthHour: String = "",
    val editBirthMinute: String = "",
    val editGender: String = "Nam",

    // Backup / Restore
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val showRestoreConfirmDialog: Boolean = false,
    val pendingRestoreUri: Uri? = null,
    val restoreSummary: String = "",

    // Feedback
    val toastMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val noteDao: NoteDao,
    private val reminderDao: ReminderDao,
    private val bookmarkDao: BookmarkDao,
    private val taskDao: TaskDao,
    private val notificationDao: NotificationDao,
    private val chatMessageDao: ChatMessageDao,
    private val familyMemberDao: FamilyMemberDao,
    private val memorialDayDao: MemorialDayDao,
    private val memorialChecklistDao: MemorialChecklistDao,
    private val familySettingsDao: FamilySettingsDao,
    private val memberPhotoDao: MemberPhotoDao,
) : ViewModel() {

    private val dataStore = context.settingsDataStore

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Collect profile prefs
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                val name = prefs[ProfileKeys.DISPLAY_NAME] ?: "Người dùng"
                val email = prefs[ProfileKeys.EMAIL] ?: ""
                val avatarPath = prefs[ProfileKeys.AVATAR_PATH] ?: ""
                val bDay = prefs[ProfileKeys.BIRTH_DAY] ?: 0
                val bMonth = prefs[ProfileKeys.BIRTH_MONTH] ?: 0
                val bYear = prefs[ProfileKeys.BIRTH_YEAR] ?: 0
                val bHour = prefs[ProfileKeys.BIRTH_HOUR] ?: -1
                val bMin = prefs[ProfileKeys.BIRTH_MINUTE] ?: -1
                val gender = prefs[ProfileKeys.GENDER] ?: "Nam"
                val savedDaysJson = prefs[ProfileKeys.SAVED_DAYS_JSON] ?: ""

                val savedDays = parseSavedDays(savedDaysJson)
                val birthInfo = if (bYear > 0 && bMonth > 0 && bDay > 0) {
                    calculateBirthInfo(bDay, bMonth, bYear, gender)
                } else {
                    BirthInfo()
                }

                _uiState.update {
                    it.copy(
                        displayName = name,
                        email = email,
                        avatarPath = avatarPath,
                        birthDay = bDay,
                        birthMonth = bMonth,
                        birthYear = bYear,
                        birthHour = bHour,
                        birthMinute = bMin,
                        gender = gender,
                        birthInfo = birthInfo,
                        savedDays = savedDays,
                        savedDaysCount = savedDays.size
                    )
                }
            }
        }

        // Collect auth user
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { state ->
                    state.copy(
                        authUser = user,
                        // Sync Firebase display name / email if local is default
                        displayName = if (state.displayName == "Người dùng" && user != null)
                            user.displayName else state.displayName,
                        email = if (state.email.isEmpty() && user != null)
                            user.email else state.email
                    )
                }
            }
        }

        // Collect note count
        viewModelScope.launch {
            noteDao.getCount().collect { count ->
                _uiState.update { it.copy(noteCount = count) }
            }
        }

        // Collect active reminder count
        viewModelScope.launch {
            reminderDao.getActiveCount().collect { count ->
                _uiState.update { it.copy(reminderCount = count) }
            }
        }

        // Collect bookmark count
        viewModelScope.launch {
            bookmarkDao.getCount().collect { count ->
                _uiState.update { it.copy(bookmarkCount = count) }
            }
        }

        // Collect all bookmarks for the "Ngày đã lưu" preview
        viewModelScope.launch {
            bookmarkDao.getAllBookmarks().collect { bookmarks ->
                _uiState.update { it.copy(allBookmarks = bookmarks) }
            }
        }
    }

    // ═══ Calculate birth info (Can Chi, Menh, etc.) ═══

    private fun calculateBirthInfo(day: Int, month: Int, year: Int, gender: String): BirthInfo {
        return try {
            val lunar = LunarCalendarUtil.convertSolar2Lunar(day, month, year)
            val lunarYear = lunar.lunarYear

            val yearCanChi = CanChiCalculator.getYearCanChi(lunarYear)

            // Con giáp (Zodiac) based on Địa Chi of the year
            val chiIndex = (lunarYear + 8) % 12
            val conGiapNames = listOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")
            val conGiapEmojis = listOf("🐭", "🐮", "🐯", "🐱", "🐲", "🐍", "🐴", "🐐", "🐒", "🐔", "🐶", "🐷")
            val conGiap = conGiapNames[chiIndex]
            val conGiapEmoji = conGiapEmojis[chiIndex]

            // Ngũ hành nạp âm (60-year cycle Nap Am)
            val nguHanh = getNguHanhNapAm(lunarYear)

            // Mệnh from Ngũ hành nạp âm
            val menh = getMenhFromNguHanh(nguHanh)

            // Cung (based on birth year and gender)
            val cung = calculateCung(lunarYear, gender)

            BirthInfo(
                yearCanChi = yearCanChi,
                menh = menh,
                nguHanh = nguHanh,
                conGiap = conGiap,
                conGiapEmoji = conGiapEmoji,
                cung = cung
            )
        } catch (e: Exception) {
            BirthInfo()
        }
    }

    /**
     * Ngũ Hành Nạp Âm — 60-year cycle
     * Each pair of Thiên Can yields a specific element name
     */
    private fun getNguHanhNapAm(lunarYear: Int): String {
        val napAmList = listOf(
            "Hải Trung Kim", "Hải Trung Kim",     // Giáp Tý, Ất Sửu
            "Lư Trung Hỏa", "Lư Trung Hỏa",     // Bính Dần, Đinh Mão
            "Đại Lâm Mộc", "Đại Lâm Mộc",       // Mậu Thìn, Kỷ Tỵ
            "Lộ Bàng Thổ", "Lộ Bàng Thổ",        // Canh Ngọ, Tân Mùi
            "Kiếm Phong Kim", "Kiếm Phong Kim",   // Nhâm Thân, Quý Dậu
            "Sơn Đầu Hỏa", "Sơn Đầu Hỏa",       // Giáp Tuất, Ất Hợi
            "Giản Hạ Thủy", "Giản Hạ Thủy",       // Bính Tý, Đinh Sửu
            "Thành Đầu Thổ", "Thành Đầu Thổ",     // Mậu Dần, Kỷ Mão
            "Bạch Lạp Kim", "Bạch Lạp Kim",       // Canh Thìn, Tân Tỵ
            "Dương Liễu Mộc", "Dương Liễu Mộc",   // Nhâm Ngọ, Quý Mùi
            "Tuyền Trung Thủy", "Tuyền Trung Thủy", // Giáp Thân, Ất Dậu
            "Ốc Thượng Thổ", "Ốc Thượng Thổ",     // Bính Tuất, Đinh Hợi
            "Tích Lịch Hỏa", "Tích Lịch Hỏa",     // Mậu Tý, Kỷ Sửu
            "Tùng Bách Mộc", "Tùng Bách Mộc",     // Canh Dần, Tân Mão
            "Trường Lưu Thủy", "Trường Lưu Thủy", // Nhâm Thìn, Quý Tỵ
            "Sa Trung Kim", "Sa Trung Kim",         // Giáp Ngọ, Ất Mùi
            "Sơn Hạ Hỏa", "Sơn Hạ Hỏa",         // Bính Thân, Đinh Dậu
            "Bình Địa Mộc", "Bình Địa Mộc",       // Mậu Tuất, Kỷ Hợi
            "Bích Thượng Thổ", "Bích Thượng Thổ", // Canh Tý, Tân Sửu
            "Kim Bạch Kim", "Kim Bạch Kim",         // Nhâm Dần, Quý Mão
            "Phúc Đăng Hỏa", "Phúc Đăng Hỏa",     // Giáp Thìn, Ất Tỵ
            "Thiên Hà Thủy", "Thiên Hà Thủy",     // Bính Ngọ, Đinh Mùi
            "Đại Dịch Thổ", "Đại Dịch Thổ",       // Mậu Thân, Kỷ Dậu
            "Thoa Xuyến Kim", "Thoa Xuyến Kim",   // Canh Tuất, Tân Hợi
            "Tang Đố Mộc", "Tang Đố Mộc",         // Nhâm Tý, Quý Sửu
            "Đại Khê Thủy", "Đại Khê Thủy",       // Giáp Dần, Ất Mão
            "Sa Trung Thổ", "Sa Trung Thổ",       // Bính Thìn, Đinh Tỵ
            "Thiên Thượng Hỏa", "Thiên Thượng Hỏa", // Mậu Ngọ, Kỷ Mùi
            "Thạch Lựu Mộc", "Thạch Lựu Mộc",     // Canh Thân, Tân Dậu
            "Đại Hải Thủy", "Đại Hải Thủy"         // Nhâm Tuất, Quý Hợi
        )

        val index = ((lunarYear - 4) % 60 + 60) % 60
        return if (index in napAmList.indices) napAmList[index] else "Không rõ"
    }

    /**
     * Extract Mệnh (element) from Ngũ Hành Nạp Âm name
     */
    private fun getMenhFromNguHanh(nguHanh: String): String {
        return when {
            nguHanh.contains("Kim") -> "Kim"
            nguHanh.contains("Mộc") -> "Mộc"
            nguHanh.contains("Thủy") -> "Thủy"
            nguHanh.contains("Hỏa") -> "Hỏa"
            nguHanh.contains("Thổ") -> "Thổ"
            else -> "Không rõ"
        }
    }

    /**
     * Tính Cung (Bát Trạch) dựa trên năm sinh âm lịch và giới tính
     */
    private fun calculateCung(lunarYear: Int, gender: String): String {
        val cungNames = listOf("Khảm", "Ly", "Cấn", "Đoài", "Càn", "Khôn", "Tốn", "Chấn", "Trung Cung")

        val sum = digitSum(lunarYear)
        val isMale = gender != "Nữ"

        val cungIndex = if (isMale) {
            val v = (11 - sum % 9) % 9
            if (v == 0) 8 else v - 1 // Trung Cung for special case
        } else {
            val v = (sum + 4) % 9
            if (v == 0) 8 else v - 1
        }

        val cungGender = if (isMale) "Nam" else "Nữ"
        val cungName = if (cungIndex in cungNames.indices) cungNames[cungIndex] else "Khảm"
        return "$cungName ($cungGender)"
    }

    private fun digitSum(n: Int): Int {
        var s = 0
        var v = kotlin.math.abs(n)
        while (v > 0) {
            s += v % 10
            v /= 10
        }
        // Keep reducing until single digit
        while (s >= 10) {
            var ns = 0
            var sv = s
            while (sv > 0) {
                ns += sv % 10
                sv /= 10
            }
            s = ns
        }
        return s
    }

    // ═══ Saved days JSON helpers ═══

    private fun parseSavedDays(json: String): List<SavedDay> {
        if (json.isBlank()) return defaultSavedDays()
        return try {
            json.split("|").mapNotNull { entry ->
                val parts = entry.split(",")
                if (parts.size >= 4) {
                    SavedDay(
                        day = parts[0].toIntOrNull() ?: return@mapNotNull null,
                        month = parts[1].toIntOrNull() ?: return@mapNotNull null,
                        year = parts[2].toIntOrNull() ?: 0,
                        label = parts[3],
                        isLunar = parts.getOrNull(4)?.toBooleanStrictOrNull() ?: false
                    )
                } else null
            }
        } catch (e: Exception) {
            defaultSavedDays()
        }
    }

    private fun savedDaysToJson(days: List<SavedDay>): String {
        return days.joinToString("|") { "${it.day},${it.month},${it.year},${it.label},${it.isLunar}" }
    }

    private fun defaultSavedDays(): List<SavedDay> = listOf(
        SavedDay(10, 3, 0, "Giỗ Tổ", isLunar = true),
        SavedDay(30, 4, 0, "Giải phóng"),
        SavedDay(1, 5, 0, "Quốc tế LĐ"),
        SavedDay(2, 9, 0, "Quốc khánh"),
    )

    // ═══ UI Actions ═══

    fun showEditProfile() {
        val s = _uiState.value
        _uiState.update {
            it.copy(
                showEditProfileSheet = true,
                editName = s.displayName,
                editEmail = s.email,
                editBirthDay = if (s.birthDay > 0) s.birthDay.toString() else "",
                editBirthMonth = if (s.birthMonth > 0) s.birthMonth.toString() else "",
                editBirthYear = if (s.birthYear > 0) s.birthYear.toString() else "",
                editBirthHour = if (s.birthHour >= 0) s.birthHour.toString() else "",
                editBirthMinute = if (s.birthMinute >= 0) s.birthMinute.toString() else "",
                editGender = s.gender
            )
        }
    }

    fun hideEditProfile() {
        _uiState.update { it.copy(showEditProfileSheet = false) }
    }

    fun updateEditName(v: String) = _uiState.update { it.copy(editName = v) }
    fun updateEditEmail(v: String) = _uiState.update { it.copy(editEmail = v) }
    fun updateEditBirthDay(v: String) = _uiState.update { it.copy(editBirthDay = v) }
    fun updateEditBirthMonth(v: String) = _uiState.update { it.copy(editBirthMonth = v) }
    fun updateEditBirthYear(v: String) = _uiState.update { it.copy(editBirthYear = v) }
    fun updateEditBirthHour(v: String) = _uiState.update { it.copy(editBirthHour = v) }
    fun updateEditBirthMinute(v: String) = _uiState.update { it.copy(editBirthMinute = v) }
    fun updateEditGender(v: String) = _uiState.update { it.copy(editGender = v) }

    fun saveProfile() {
        val s = _uiState.value
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[ProfileKeys.DISPLAY_NAME] = s.editName.ifBlank { "Người dùng" }
                prefs[ProfileKeys.EMAIL] = s.editEmail
                prefs[ProfileKeys.BIRTH_DAY] = s.editBirthDay.toIntOrNull() ?: 0
                prefs[ProfileKeys.BIRTH_MONTH] = s.editBirthMonth.toIntOrNull() ?: 0
                prefs[ProfileKeys.BIRTH_YEAR] = s.editBirthYear.toIntOrNull() ?: 0
                prefs[ProfileKeys.BIRTH_HOUR] = s.editBirthHour.toIntOrNull() ?: -1
                prefs[ProfileKeys.BIRTH_MINUTE] = s.editBirthMinute.toIntOrNull() ?: -1
                prefs[ProfileKeys.GENDER] = s.editGender
            }
            _uiState.update {
                it.copy(
                    showEditProfileSheet = false,
                    toastMessage = "Đã lưu hồ sơ"
                )
            }
        }
    }

    // ═══ Saved Days Management ═══

    fun showAddSavedDay() = _uiState.update { it.copy(showAddSavedDayDialog = true) }
    fun hideAddSavedDay() = _uiState.update { it.copy(showAddSavedDayDialog = false) }

    fun addSavedDay(day: Int, month: Int, year: Int, label: String, isLunar: Boolean) {
        viewModelScope.launch {
            val current = _uiState.value.savedDays.toMutableList()
            current.add(SavedDay(day, month, year, label, isLunar))
            dataStore.edit { prefs ->
                prefs[ProfileKeys.SAVED_DAYS_JSON] = savedDaysToJson(current)
            }
            _uiState.update {
                it.copy(
                    showAddSavedDayDialog = false,
                    toastMessage = "Đã thêm ngày \"$label\""
                )
            }
        }
    }

    /**
     * Add a bookmark to Room DB (new flow)
     */
    fun addBookmark(day: Int, month: Int, year: Int, label: String, note: String = "") {
        viewModelScope.launch {
            bookmarkDao.insert(
                BookmarkEntity(
                    solarDay = day,
                    solarMonth = month,
                    solarYear = year,
                    label = label,
                    note = note
                )
            )
            _uiState.update {
                it.copy(
                    showAddSavedDayDialog = false,
                    toastMessage = "Đã lưu ngày \"$label\""
                )
            }
        }
    }

    fun removeBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            bookmarkDao.delete(bookmark)
            _uiState.update { it.copy(toastMessage = "Đã xóa \"${bookmark.label}\"") }
        }
    }

    fun removeSavedDay(index: Int) {
        viewModelScope.launch {
            val current = _uiState.value.savedDays.toMutableList()
            if (index in current.indices) {
                val removed = current.removeAt(index)
                dataStore.edit { prefs ->
                    prefs[ProfileKeys.SAVED_DAYS_JSON] = savedDaysToJson(current)
                }
                _uiState.update {
                    it.copy(toastMessage = "Đã xóa \"${removed.label}\"")
                }
            }
        }
    }

    // ═══ Avatar ═══

    /**
     * Copy ảnh từ gallery URI vào internal storage rồi lưu path vào DataStore.
     * Dùng internal storage để ảnh luôn truy cập được mà không cần permission lâu dài.
     */
    fun saveAvatarFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val savedPath = withContext(Dispatchers.IO) {
                    val avatarDir = File(context.filesDir, "avatars")
                    if (!avatarDir.exists()) avatarDir.mkdirs()

                    // Use timestamp in filename to bust Coil image cache
                    val ts = System.currentTimeMillis()
                    val destFile = File(avatarDir, "profile_avatar_$ts.jpg")

                    // Delete old avatar files
                    avatarDir.listFiles()?.filter {
                        it.name.startsWith("profile_avatar") && it != destFile
                    }?.forEach { it.delete() }

                    context.contentResolver.openInputStream(uri)?.use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    destFile.absolutePath
                }

                dataStore.edit { prefs ->
                    prefs[ProfileKeys.AVATAR_PATH] = savedPath
                }
                _uiState.update { it.copy(toastMessage = "Đã cập nhật ảnh đại diện") }
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "Không thể lưu ảnh: ${e.message}") }
            }
        }
    }

    fun removeAvatar() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val avatarDir = File(context.filesDir, "avatars")
                avatarDir.listFiles()?.filter {
                    it.name.startsWith("profile_avatar")
                }?.forEach { it.delete() }
            }
            dataStore.edit { prefs ->
                prefs.remove(ProfileKeys.AVATAR_PATH)
            }
            _uiState.update { it.copy(toastMessage = "Đã xóa ảnh đại diện") }
        }
    }

    // ═══ Sign out ═══

    fun showSignOutDialog() = _uiState.update { it.copy(showSignOutDialog = true) }
    fun hideSignOutDialog() = _uiState.update { it.copy(showSignOutDialog = false) }

    fun signOut() {
        authRepository.signOut()
        _uiState.update {
            it.copy(
                showSignOutDialog = false,
                toastMessage = "Đã đăng xuất"
            )
        }
    }

    fun isSignedIn(): Boolean = authRepository.isSignedIn()

    // ═══ Backup / Restore ═══

    /**
     * Start building backup JSON, then invoke callback with json + filename.
     * The callback should trigger SAF CreateDocument launcher.
     */
    fun backupAllData(onReady: (json: String, fileName: String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackingUp = true) }
            try {
                val json = withContext(Dispatchers.IO) {
                    AppBackupManager.buildBackupJson(
                        context = context,
                        taskDao = taskDao,
                        noteDao = noteDao,
                        reminderDao = reminderDao,
                        bookmarkDao = bookmarkDao,
                        notificationDao = notificationDao,
                        chatMessageDao = chatMessageDao,
                        familyMemberDao = familyMemberDao,
                        memorialDayDao = memorialDayDao,
                        memorialChecklistDao = memorialChecklistDao,
                        familySettingsDao = familySettingsDao,
                        memberPhotoDao = memberPhotoDao,
                    )
                }
                val fileName = AppBackupManager.generateFileName()
                _uiState.update { it.copy(isBackingUp = false) }
                onReady(json, fileName)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isBackingUp = false,
                        toastMessage = "Lỗi sao lưu: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Write backup JSON to SAF URI (called from CreateDocument launcher result).
     */
    fun writeBackupToUri(uri: Uri, json: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    AppBackupManager.writeToUri(context, uri, json)
                }
                _uiState.update { it.copy(toastMessage = "✅ Đã sao lưu thành công!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "Lỗi ghi file: ${e.message}") }
            }
        }
    }

    /**
     * User picked a backup JSON file to restore. Parse and show confirm dialog.
     */
    fun requestRestore(uri: Uri) {
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    AppBackupManager.readFromUri(context, uri)
                }
                val data = AppBackupManager.parseBackupJson(json)
                if (data.type != "full_backup" || data.appId != "com.lichso.app") {
                    _uiState.update { it.copy(toastMessage = "File không phải bản sao lưu Lịch Số") }
                    return@launch
                }
                val summary = AppBackupManager.getBackupSummary(data)
                _uiState.update {
                    it.copy(
                        showRestoreConfirmDialog = true,
                        pendingRestoreUri = uri,
                        restoreSummary = summary
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "Không thể đọc file: ${e.message}") }
            }
        }
    }

    /**
     * User confirmed restore → actually restore all data.
     */
    fun confirmRestore() {
        val uri = _uiState.value.pendingRestoreUri ?: return
        _uiState.update { it.copy(showRestoreConfirmDialog = false, isRestoring = true) }
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    AppBackupManager.readFromUri(context, uri)
                }
                val data = AppBackupManager.parseBackupJson(json)
                withContext(Dispatchers.IO) {
                    AppBackupManager.restoreFromBackup(
                        context = context, data = data,
                        taskDao = taskDao, noteDao = noteDao,
                        reminderDao = reminderDao, bookmarkDao = bookmarkDao,
                        notificationDao = notificationDao, chatMessageDao = chatMessageDao,
                        familyMemberDao = familyMemberDao, memorialDayDao = memorialDayDao,
                        memorialChecklistDao = memorialChecklistDao,
                        familySettingsDao = familySettingsDao, memberPhotoDao = memberPhotoDao,
                    )
                }
                _uiState.update {
                    it.copy(
                        isRestoring = false,
                        pendingRestoreUri = null,
                        toastMessage = "✅ Đã phục hồi dữ liệu thành công!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRestoring = false,
                        pendingRestoreUri = null,
                        toastMessage = "Lỗi phục hồi: ${e.message}"
                    )
                }
            }
        }
    }

    fun cancelRestore() {
        _uiState.update {
            it.copy(showRestoreConfirmDialog = false, pendingRestoreUri = null, restoreSummary = "")
        }
    }

    fun consumeToast() = _uiState.update { it.copy(toastMessage = null) }
}
