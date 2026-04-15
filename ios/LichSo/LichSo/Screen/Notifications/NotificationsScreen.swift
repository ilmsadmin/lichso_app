import SwiftUI
import SwiftData

// ═══════════════════════════════════════════
// Notifications Screen
// Matches screen-notifications.html design
// ═══════════════════════════════════════════

private var PrimaryRed: Color { LSTheme.primary }
private var PrimaryContainer: Color { LSTheme.primaryContainer }
private var SurfaceBg: Color { LSTheme.bg }
private var SurfaceContainer: Color { LSTheme.surfaceContainer }
private var TextMain: Color { LSTheme.textPrimary }
private var TextSub: Color { LSTheme.textSecondary }
private var TextDim: Color { LSTheme.textTertiary }
private var OutlineVariant: Color { LSTheme.outlineVariant }

struct NotificationsScreen: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    @StateObject private var vm = NotificationsViewModel()

    var body: some View {
        VStack(spacing: 0) {
            // ═══ TOP BAR ═══
            HStack(spacing: 12) {
                Button { dismiss() } label: {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(.white)
                        .frame(width: 40, height: 40)
                        .background(Color.white.opacity(0.15))
                        .clipShape(Circle())
                }

                Text("Thông báo")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(.white)

                Spacer()

                if vm.unreadCount > 0 {
                    Button { vm.markAllAsRead() } label: {
                        Text("Đọc hết")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(.white)
                            .padding(.horizontal, 14)
                            .padding(.vertical, 6)
                            .background(Color.white.opacity(0.2))
                            .clipShape(Capsule())
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 8)
            .padding(.bottom, 16)
            .background(
                LinearGradient(
                    colors: [Color(red: 0.773, green: 0.157, blue: 0.157),
                             Color(red: 0.545, green: 0, blue: 0)],
                    startPoint: .topLeading, endPoint: .bottomTrailing
                )
                .ignoresSafeArea(edges: .top)
            )

            // ═══ CONTENT ═══
            if vm.notifications.isEmpty {
                Spacer()
                VStack(spacing: 12) {
                    Image(systemName: "bell.slash")
                        .font(.system(size: 44))
                        .foregroundColor(TextDim.opacity(0.4))
                    Text("Chưa có thông báo nào")
                        .font(.system(size: 14))
                        .foregroundColor(TextDim)
                }
                Spacer()
            } else {
                ScrollView(.vertical, showsIndicators: false) {
                    LazyVStack(spacing: 0) {
                        ForEach(vm.groupedNotifications) { group in
                            // Date group header
                            HStack {
                                Text(group.label)
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(PrimaryRed)
                                    .textCase(.uppercase)
                                    .tracking(0.5)
                                Spacer()
                            }
                            .padding(.horizontal, 4)
                            .padding(.top, 16)
                            .padding(.bottom, 10)

                            // Cards
                            ForEach(group.items, id: \.id) { notif in
                                NotifCard(notif: notif, onTap: { vm.markAsRead(notif) }, onDelete: { vm.deleteNotification(notif) })
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.bottom, 40)
                }
            }
        }
        .background(SurfaceBg)
        .navigationBarHidden(true)
        .onAppear {
            vm.setModelContext(modelContext)
            vm.seedIfEmpty()
            vm.generateTodayNotifications()
        }
    }
}

// ══════════════════════════════════════════
// NOTIFICATION CARD
// ══════════════════════════════════════════

private struct NotifCard: View {
    let notif: NotificationEntity
    let onTap: () -> Void
    let onDelete: () -> Void
    @State private var isExpanded = false

    private var info: NotificationsViewModel.NotifTypeInfo {
        NotificationsViewModel.typeInfo(for: notif.type)
    }

    private var hasMultipleLines: Bool {
        notif.notificationDescription.contains("\n")
    }

    var body: some View {
        Button {
            if hasMultipleLines {
                withAnimation(.easeInOut(duration: 0.2)) { isExpanded.toggle() }
            }
            onTap()
        } label: {
            HStack(alignment: .top, spacing: 12) {
                // Type icon
                Image(systemName: info.icon)
                    .font(.system(size: 20))
                    .foregroundColor(info.color)
                    .frame(width: 40, height: 40)
                    .background(info.bgColor)
                    .clipShape(RoundedRectangle(cornerRadius: 12))

                // Content
                VStack(alignment: .leading, spacing: 4) {
                    Text(notif.title)
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(TextMain)
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)

                    if !notif.notificationDescription.isEmpty {
                        Text(notif.notificationDescription)
                            .font(.system(size: 12))
                            .foregroundColor(TextSub)
                            .lineLimit(isExpanded ? nil : 3)
                            .multilineTextAlignment(.leading)
                            .fixedSize(horizontal: false, vertical: isExpanded)
                    }

                    HStack(spacing: 6) {
                        Text(NotificationsViewModel.formatTime(notif.createdAt))
                            .font(.system(size: 10))
                            .foregroundColor(TextDim)

                        if hasMultipleLines {
                            Image(systemName: isExpanded ? "chevron.up" : "chevron.down")
                                .font(.system(size: 8, weight: .bold))
                                .foregroundColor(TextDim)
                        }
                    }
                    .padding(.top, 2)
                }

                Spacer(minLength: 0)

                // Unread dot
                if !notif.isRead {
                    Circle()
                        .fill(PrimaryRed)
                        .frame(width: 8, height: 8)
                        .padding(.top, 4)
                }
            }
            .padding(14)
            .background(notif.isRead ? SurfaceContainer : PrimaryContainer)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(notif.isRead ? OutlineVariant : PrimaryRed.opacity(0.2), lineWidth: 1)
            )
        }
        .buttonStyle(.plain)
        .padding(.bottom, 8)
        .contextMenu {
            if !notif.isRead {
                Button { onTap() } label: { Label("Đánh dấu đã đọc", systemImage: "checkmark.circle") }
            }
            Button(role: .destructive) { onDelete() } label: { Label("Xóa", systemImage: "trash") }
        }
    }
}
