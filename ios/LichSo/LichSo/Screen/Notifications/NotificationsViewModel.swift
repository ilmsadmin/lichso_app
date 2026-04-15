import SwiftUI
import SwiftData

// ═══════════════════════════════════════════
// NotificationsViewModel — SwiftData backed
// ═══════════════════════════════════════════

@MainActor
class NotificationsViewModel: ObservableObject {
    @Published var notifications: [NotificationEntity] = []
    private var modelContext: ModelContext?

    func setModelContext(_ context: ModelContext) {
        self.modelContext = context
        loadNotifications()
    }

    func loadNotifications() {
        guard let ctx = modelContext else { return }
        let descriptor = FetchDescriptor<NotificationEntity>(sortBy: [
            SortDescriptor(\.createdAt, order: .reverse)
        ])
        do { notifications = try ctx.fetch(descriptor) } catch {
            #if DEBUG
            print("Notif fetch error: \(error)")
            #endif
        }
    }

    // ── Date-grouped ──
    struct DateGroup: Identifiable {
        let id: String // "today" / "yesterday" / date string
        let label: String
        let items: [NotificationEntity]
    }

    var groupedNotifications: [DateGroup] {
        let cal = Calendar.current
        var buckets: [String: (label: String, items: [NotificationEntity], sort: Int)] = [:]
        let today = cal.startOfDay(for: Date())

        for n in notifications {
            let date = Date(timeIntervalSince1970: Double(n.createdAt) / 1000)
            let day = cal.startOfDay(for: date)
            let diff = cal.dateComponents([.day], from: day, to: today).day ?? 999

            let key: String
            let label: String
            let sort: Int
            if diff == 0 {
                key = "today"
                let f = DateFormatter(); f.locale = Locale(identifier: "vi_VN"); f.dateFormat = "dd/MM/yyyy"
                label = "Hôm nay · \(f.string(from: date))"
                sort = 0
            } else if diff == 1 {
                key = "yesterday"
                let f = DateFormatter(); f.locale = Locale(identifier: "vi_VN"); f.dateFormat = "dd/MM/yyyy"
                label = "Hôm qua · \(f.string(from: date))"
                sort = 1
            } else {
                let f = DateFormatter(); f.locale = Locale(identifier: "vi_VN"); f.dateFormat = "dd/MM/yyyy"
                let s = f.string(from: date)
                key = s
                label = s
                sort = diff
            }

            if buckets[key] == nil {
                buckets[key] = (label, [n], sort)
            } else {
                buckets[key]!.items.append(n)
            }
        }

        return buckets.sorted { $0.value.sort < $1.value.sort }
            .map { DateGroup(id: $0.key, label: $0.value.label, items: $0.value.items) }
    }

    var unreadCount: Int { notifications.filter { !$0.isRead }.count }

    // ── Actions ──
    func markAsRead(_ notification: NotificationEntity) {
        notification.isRead = true
        try? modelContext?.save()
        loadNotifications()
    }

    func markAllAsRead() {
        for n in notifications where !n.isRead { n.isRead = true }
        try? modelContext?.save()
        loadNotifications()
    }

    func deleteNotification(_ notification: NotificationEntity) {
        guard let ctx = modelContext else { return }
        ctx.delete(notification)
        try? ctx.save()
        loadNotifications()
    }

    // ── Type Metadata ──
    struct NotifTypeInfo {
        let icon: String
        let color: Color
        let bgColor: Color
    }

    static func typeInfo(for type: String) -> NotifTypeInfo {
        switch type {
        case "daily":    return NotifTypeInfo(icon: "sun.max.fill", color: Color(hex: "2E7D32"), bgColor: Color(hex: "E8F5E9"))
        case "holiday":  return NotifTypeInfo(icon: "party.popper.fill", color: Color(hex: "E65100"), bgColor: Color(hex: "FFF3E0"))
        case "ai":       return NotifTypeInfo(icon: "sparkles", color: Color(hex: "7B1FA2"), bgColor: Color(hex: "F3E5F5"))
        case "reminder": return NotifTypeInfo(icon: "bell.fill", color: Color(hex: "1565C0"), bgColor: Color(hex: "E3F2FD"))
        case "system":   return NotifTypeInfo(icon: "arrow.down.circle.fill", color: Color(hex: "616161"), bgColor: Color(hex: "F5F5F5"))
        case "good_day": return NotifTypeInfo(icon: "calendar.badge.checkmark", color: Color(hex: "F57F17"), bgColor: Color(hex: "FFF8E1"))
        default:         return NotifTypeInfo(icon: "bell.fill", color: Color(hex: "8A7E62"), bgColor: Color(hex: "F5F5F5"))
        }
    }

    static func formatTime(_ timestamp: Int64) -> String {
        let date = Date(timeIntervalSince1970: Double(timestamp) / 1000)
        let f = DateFormatter(); f.dateFormat = "HH:mm"
        return f.string(from: date)
    }

    // ── Generate real notifications from calendar data ──

    /// Tạo thông báo thật cho lần đầu mở app (dữ liệu thật từ lịch)
    func seedIfEmpty() {
        guard notifications.isEmpty, let ctx = modelContext else { return }
        NotificationService.shared.seedInitialNotifications(context: ctx)
        loadNotifications()
    }

    /// Tạo thông báo mới cho ngày hôm nay (gọi mỗi lần mở app)
    func generateTodayNotifications() {
        guard let ctx = modelContext else { return }
        NotificationService.shared.generateAllNotifications(context: ctx)
        loadNotifications()
    }
}
