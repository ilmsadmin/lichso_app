import SwiftUI
import SwiftData

struct NotificationScreen: View {
    @Environment(\.lichSoColors) private var c
    @Environment(\.modelContext) private var modelContext
    @State private var notifications: [NotificationEntity] = []
    var onBackClick: () -> Void = {}

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(c.textPrimary)
                }
                Spacer()
                Text("THÔNG BÁO")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(c.textPrimary)
                    .tracking(1)
                Spacer()
                if !notifications.isEmpty {
                    Button("Đọc hết") {
                        markAllRead()
                    }
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(c.primary)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)

            ScrollView {
                if notifications.isEmpty {
                    VStack(spacing: 12) {
                        Image(systemName: "bell.slash")
                            .font(.system(size: 48))
                            .foregroundColor(c.textQuaternary)
                            .padding(.top, 80)
                        Text("Không có thông báo")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(c.textSecondary)
                    }
                    .frame(maxWidth: .infinity)
                }
                LazyVStack(spacing: 8) {
                    ForEach(notifications, id: \.id) { notification in
                        NotificationRow(notification: notification, c: c)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
            }
        }
        .background(c.bg)
        .onAppear { loadNotifications() }
    }

    private func loadNotifications() {
        let descriptor = FetchDescriptor<NotificationEntity>(
            sortBy: [SortDescriptor(\NotificationEntity.createdAt, order: .reverse)]
        )
        notifications = (try? modelContext.fetch(descriptor)) ?? []
    }

    private func markAllRead() {
        for n in notifications {
            n.isRead = true
        }
        try? modelContext.save()
        loadNotifications()
    }
}

struct NotificationRow: View {
    let notification: NotificationEntity
    let c: LichSoColors

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "bell.fill")
                .font(.system(size: 18))
                .foregroundColor(notification.isRead ? c.textTertiary : c.primary)
            VStack(alignment: .leading, spacing: 3) {
                Text(notification.title)
                    .font(.system(size: 14, weight: notification.isRead ? .medium : .semibold))
                    .foregroundColor(notification.isRead ? c.textSecondary : c.textPrimary)
                Text(notification.notificationDescription)
                    .font(.system(size: 12))
                    .foregroundColor(c.textTertiary)
                    .lineLimit(2)
                Text(formatDate(notification.createdAt))
                    .font(.system(size: 10))
                    .foregroundColor(c.textQuaternary)
            }
            Spacer()
            if !notification.isRead {
                Circle()
                    .fill(c.primary)
                    .frame(width: 8, height: 8)
            }
        }
        .padding(12)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(notification.isRead ? c.bg : c.surface)
        )
    }

    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy HH:mm"
        return formatter.string(from: date)
    }
}
