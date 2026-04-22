import Foundation
import SwiftUI

// ═══════════════════════════════════════════
// SmartRatingManager — Singleton quản lý logic "xin đánh giá thông minh"
//
// Chiến lược:
// - Trigger sau khi user hoàn thành "happy action" (bookmark, lưu gia phả, v.v.)
// - Chỉ hỏi sau khi user thực hiện đủ MIN_ACTIONS_BEFORE_ASK happy actions
// - Không hỏi lại trong vòng MIN_DAYS_BETWEEN_ASKS ngày
// - Tối đa MAX_TIMES_TO_ASK lần, sau đó dừng hẳn
//
// Luồng:
// - Hài lòng → chọn sao → 4-5★ → StoreKit In-App Review
// - Không hài lòng / 1-3★ → gửi feedback qua email
// ═══════════════════════════════════════════

@MainActor
final class SmartRatingManager: ObservableObject {

    static let shared = SmartRatingManager()

    // ── UserDefaults keys ──
    private let kHappyActionCount = "smart_rating_happy_action_count"
    private let kLastAskedTime    = "smart_rating_last_asked_time"    // TimeInterval since 1970
    private let kTimesAsked       = "smart_rating_times_asked"
    private let kUserRated        = "smart_rating_user_rated"         // 0=not yet, 1=rated

    // ── Thresholds ──
    private let minActionsBeforeAsk = 3
    private let minDaysBetweenAsks  = 14
    private let maxTimesToAsk       = 3

    // ── Observable state ──
    @Published var shouldShow = false

    private init() {}

    // MARK: - Public API

    /// Gọi sau khi user hoàn thành "happy action" (bookmark, lưu thành viên, lưu văn khấn, v.v.)
    func recordHappyAction(weight: Int = 1) {
        let defaults = UserDefaults.standard
        let timesAsked = defaults.integer(forKey: kTimesAsked)
        let userRated  = defaults.integer(forKey: kUserRated)

        // Đã hỏi đủ lần hoặc user đã đánh giá → bỏ qua
        guard timesAsked < maxTimesToAsk, userRated != 1 else { return }

        let current = defaults.integer(forKey: kHappyActionCount)
        defaults.set(current + weight, forKey: kHappyActionCount)

        checkAndTrigger()
    }

    /// Kiểm tra điều kiện và trigger dialog nếu đủ
    func checkAndTrigger() {
        let defaults = UserDefaults.standard
        let actionCount   = defaults.integer(forKey: kHappyActionCount)
        let lastAskedTime = defaults.double(forKey: kLastAskedTime)
        let timesAsked    = defaults.integer(forKey: kTimesAsked)
        let userRated     = defaults.integer(forKey: kUserRated)

        // Đã đánh giá hoặc đã hỏi tối đa
        guard userRated != 1, timesAsked < maxTimesToAsk else { return }

        // Chưa đủ happy actions
        guard actionCount >= minActionsBeforeAsk else { return }

        // Quá gần lần hỏi trước
        let daysSinceLastAsked: Double
        if lastAskedTime == 0 {
            daysSinceLastAsked = .greatestFiniteMagnitude
        } else {
            daysSinceLastAsked = (Date().timeIntervalSince1970 - lastAskedTime) / 86400
        }
        guard daysSinceLastAsked >= Double(minDaysBetweenAsks) else { return }

        // Đủ điều kiện → show
        shouldShow = true
    }

    /// Trigger thủ công — dùng khi user bấm "Đánh giá ứng dụng" trong Settings/Sidebar
    func triggerManually() {
        shouldShow = true
    }

    /// Ghi nhận đã show dialog lần này (cooldown)
    func recordShown() {
        let defaults = UserDefaults.standard
        let current = defaults.integer(forKey: kTimesAsked)
        defaults.set(current + 1, forKey: kTimesAsked)
        defaults.set(Date().timeIntervalSince1970, forKey: kLastAskedTime)
        // Reset action count
        defaults.set(0, forKey: kHappyActionCount)
    }

    /// User chọn "Hài lòng" + 4-5★ → ghi nhận đã rated
    func recordRated() {
        UserDefaults.standard.set(1, forKey: kUserRated)
        shouldShow = false
    }

    /// Dismiss dialog — có thể hỏi lại sau
    func dismiss() {
        shouldShow = false
    }
}
