package com.lichso.app.update

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Quản lý In-App Update (Play Core) — cho phép user UPDATE NGAY trong app
 * mà không cần thoát ra Play Store.
 *
 * Có 2 chế độ Google hỗ trợ:
 *  - FLEXIBLE  : Update tải về nền, app vẫn chạy bình thường, sau khi tải
 *                xong sẽ hiện Snackbar "Khởi động lại" để hoàn tất.
 *                Dùng cho update không khẩn cấp (mặc định).
 *  - IMMEDIATE : Hiển thị màn full-screen blocking, user phải đợi tải +
 *                cài đặt rồi app tự khởi động lại. Chỉ dùng khi
 *                `updatePriority >= 4` (priority này được set ở Play
 *                Console khi roll-out).
 *
 * Lifecycle yêu cầu của Google:
 *  1. `register(activity)` trong `onCreate` để bind ActivityResultLauncher.
 *  2. `checkForUpdates(activity)` trong `onCreate` (sau register).
 *  3. `onResumeCheck(activity)` trong `onResume` để:
 *      - Resume IMMEDIATE update nếu user thoát ra giữa chừng.
 *      - Hiển thị banner "Khởi động lại" nếu FLEXIBLE đã DOWNLOADED.
 *  4. `unregister(activity)` trong `onDestroy` để gỡ listener tránh leak.
 *
 * Singleton (`object`) vì `MainActivity` là single-task & ta cần share
 * state với Compose layer (`LichSoMainScreen` collect [uiState] để hiện
 * Snackbar).
 */
object InAppUpdateManager {

    private const val TAG = "InAppUpdate"
    private const val PREFS = "in_app_update_prefs"
    private const val KEY_FLEXIBLE_DEFER_UNTIL = "flexible_defer_until_ms"

    /**
     * Cooldown khi user bấm "Để sau" với FLEXIBLE update.
     * 3 ngày — không quá lì để user thấy phiền, đủ lâu để tránh hỏi mỗi
     * lần mở app nhưng vẫn re-prompt nếu user bỏ qua.
     */
    private const val FLEXIBLE_DEFER_MS = 3L * 24 * 60 * 60 * 1000

    sealed class UiState {
        object Idle : UiState()
        /** Update đã tải xong (FLEXIBLE) — hiển thị Snackbar "Khởi động lại". */
        object ReadyToInstall : UiState()
        /** Đang tải FLEXIBLE update ở nền — có thể hiện progress nhỏ nếu muốn. */
        data class Downloading(val bytesDownloaded: Long, val totalBytes: Long) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var appUpdateManager: AppUpdateManager? = null
    private var updateLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

    private val installListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                _uiState.value = UiState.Downloading(
                    bytesDownloaded = state.bytesDownloaded(),
                    totalBytes = state.totalBytesToDownload()
                )
            }
            InstallStatus.DOWNLOADED -> {
                Log.i(TAG, "FLEXIBLE update downloaded — prompt user to restart")
                _uiState.value = UiState.ReadyToInstall
            }
            InstallStatus.INSTALLED -> {
                _uiState.value = UiState.Idle
            }
            InstallStatus.FAILED, InstallStatus.CANCELED -> {
                Log.w(TAG, "Install state=${state.installStatus()} errorCode=${state.installErrorCode()}")
                _uiState.value = UiState.Idle
            }
            else -> { /* PENDING / INSTALLING — không cần đổi UI */ }
        }
    }

    /**
     * Đăng ký ActivityResultLauncher trong [Activity.onCreate] (BẮT BUỘC
     * gọi trước `setContent`). Phải dùng [ActivityResultContracts.StartIntentSenderForResult]
     * vì Play Core dùng IntentSender chứ không phải Intent thường.
     */
    fun register(activity: androidx.activity.ComponentActivity) {
        if (appUpdateManager == null) {
            appUpdateManager = AppUpdateManagerFactory.create(activity.applicationContext)
        }
        appUpdateManager?.registerListener(installListener)

        updateLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result: ActivityResult ->
            // resultCode:
            //   RESULT_OK              = user accepted (FLEXIBLE: download bắt đầu, IMMEDIATE: đã update)
            //   RESULT_CANCELED        = user huỷ
            //   RESULT_IN_APP_UPDATE_FAILED (1) = lỗi
            when (result.resultCode) {
                Activity.RESULT_OK -> Log.d(TAG, "User accepted update flow")
                Activity.RESULT_CANCELED -> {
                    Log.d(TAG, "User cancelled update flow → defer 3 days")
                    deferFlexibleUpdate(activity)
                }
                else -> Log.w(TAG, "Update flow failed, resultCode=${result.resultCode}")
            }
        }
    }

    /**
     * Phải gọi trong [Activity.onDestroy] để gỡ listener — tránh memory
     * leak khi app process còn sống nhưng Activity bị destroy.
     */
    fun unregister() {
        appUpdateManager?.unregisterListener(installListener)
        updateLauncher = null
    }

    /**
     * Kiểm tra & khởi chạy update flow lần đầu (trong onCreate).
     *
     * Quy tắc chọn FLEXIBLE vs IMMEDIATE:
     *  - `updatePriority >= 4`  → IMMEDIATE (bắt buộc, dev set ở Play Console).
     *  - 1 <= priority <= 3     → FLEXIBLE (tải nền + prompt restart).
     *  - priority = 0           → FLEXIBLE, nhưng tôn trọng cooldown 3 ngày
     *                              nếu user vừa bấm "Để sau".
     */
    fun checkForUpdates(activity: Activity) {
        val manager = appUpdateManager ?: AppUpdateManagerFactory.create(activity.applicationContext).also {
            appUpdateManager = it
        }

        manager.appUpdateInfo
            .addOnSuccessListener { info ->
                val availability = info.updateAvailability()
                val priority = runCatching { info.updatePriority() }.getOrDefault(0)
                Log.d(TAG, "appUpdateInfo: availability=$availability, priority=$priority, " +
                        "flexibleAllowed=${info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)}, " +
                        "immediateAllowed=${info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)}")

                if (availability != UpdateAvailability.UPDATE_AVAILABLE) return@addOnSuccessListener

                // ── IMMEDIATE: ưu tiên cao, ép user update ──
                if (priority >= 4 && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    startUpdate(activity, info, AppUpdateType.IMMEDIATE)
                    return@addOnSuccessListener
                }

                // ── FLEXIBLE: tôn trọng cooldown ──
                if (info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    if (isFlexibleDeferred(activity)) {
                        Log.d(TAG, "FLEXIBLE update deferred — skipping prompt")
                        return@addOnSuccessListener
                    }
                    startUpdate(activity, info, AppUpdateType.FLEXIBLE)
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "appUpdateInfo failed: ${e.message}")
            }
    }

    /**
     * Phải gọi trong [Activity.onResume] (Google requirement):
     *  - Nếu user đã chấp nhận IMMEDIATE rồi thoát app → tiếp tục flow.
     *  - Nếu FLEXIBLE đã DOWNLOADED → cập nhật UI state để hiện Snackbar.
     */
    fun onResumeCheck(activity: Activity) {
        val manager = appUpdateManager ?: return
        manager.appUpdateInfo.addOnSuccessListener { info ->
            // FLEXIBLE: tải xong → bật Snackbar
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                _uiState.value = UiState.ReadyToInstall
            }
            // IMMEDIATE: nếu đang dở → resume
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS &&
                info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                startUpdate(activity, info, AppUpdateType.IMMEDIATE)
            }
        }
    }

    /**
     * Hoàn tất FLEXIBLE update: app sẽ restart và áp dụng bản mới.
     * Gọi từ Compose khi user bấm "Khởi động lại" trên Snackbar.
     */
    fun completeFlexibleUpdate() {
        appUpdateManager?.completeUpdate()
    }

    /** Public để Settings screen có thể "Kiểm tra cập nhật ngay" thủ công. */
    fun manualCheck(activity: Activity) {
        // Reset cooldown vì user chủ động bấm
        clearFlexibleDefer(activity)
        checkForUpdates(activity)
    }

    // ── internals ──

    private fun startUpdate(activity: Activity, info: AppUpdateInfo, @AppUpdateType type: Int) {
        val launcher = updateLauncher ?: run {
            Log.w(TAG, "Launcher not registered — call register() in onCreate")
            return
        }
        try {
            appUpdateManager?.startUpdateFlowForResult(
                info,
                launcher,
                AppUpdateOptions.newBuilder(type).build()
            )
        } catch (e: IntentSender.SendIntentException) {
            Log.e(TAG, "startUpdateFlowForResult failed", e)
        }
    }

    private fun deferFlexibleUpdate(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_FLEXIBLE_DEFER_UNTIL, System.currentTimeMillis() + FLEXIBLE_DEFER_MS)
            .apply()
    }

    private fun clearFlexibleDefer(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_FLEXIBLE_DEFER_UNTIL)
            .apply()
    }

    private fun isFlexibleDeferred(context: Context): Boolean {
        val until = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_FLEXIBLE_DEFER_UNTIL, 0L)
        return System.currentTimeMillis() < until
    }
}
