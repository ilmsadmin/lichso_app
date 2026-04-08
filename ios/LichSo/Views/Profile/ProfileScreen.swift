import SwiftUI

struct ProfileScreen: View {
    @Environment(\.lichSoColors) private var c
    @EnvironmentObject private var settings: AppSettings
    var onSettingsClick: () -> Void = {}
    var onFamilyTreeClick: () -> Void = {}
    var onBackClick: () -> Void = {}
    var onTasksClick: () -> Void = {}
    var onBookmarksClick: () -> Void = {}

    var body: some View {
        VStack(spacing: 0) {
            // Top bar
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(c.textPrimary)
                }
                Spacer()
                Text("CÁ NHÂN")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(c.textPrimary)
                    .tracking(1)
                Spacer()
                Button(action: onSettingsClick) {
                    Image(systemName: "gearshape")
                        .font(.system(size: 18))
                        .foregroundColor(c.textSecondary)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)

            ScrollView {
                VStack(spacing: 16) {
                    // Profile header
                    VStack(spacing: 12) {
                        ZStack {
                            Circle()
                                .fill(c.primaryContainer)
                                .frame(width: 80, height: 80)
                            Image(systemName: "person.fill")
                                .font(.system(size: 36))
                                .foregroundColor(c.primary)
                        }

                        Text(settings.userName.isEmpty ? "Người dùng" : settings.userName)
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(c.textPrimary)

                        if !settings.userGender.isEmpty {
                            Text(settings.userGender)
                                .font(.system(size: 13))
                                .foregroundColor(c.textTertiary)
                        }

                        if !settings.userBirthDate.isEmpty {
                            HStack(spacing: 4) {
                                Image(systemName: "birthday.cake")
                                    .font(.system(size: 12))
                                Text(settings.userBirthDate)
                                    .font(.system(size: 13))
                            }
                            .foregroundColor(c.textSecondary)
                        }
                    }
                    .padding(.vertical, 16)

                    // Quick actions
                    VStack(spacing: 8) {
                        profileButton(icon: "person.3.sequence.fill", title: "Cây gia phả", subtitle: "Quản lý gia phả gia đình", color: c.teal, action: onFamilyTreeClick)
                        profileButton(icon: "checkmark.circle.fill", title: "Công việc & Ghi chú", subtitle: "Quản lý công việc hàng ngày", color: c.gold, action: onTasksClick)
                        profileButton(icon: "bookmark.fill", title: "Ngày đã lưu", subtitle: "Các ngày đã đánh dấu", color: c.noteOrange, action: onBookmarksClick)
                    }
                    .padding(.horizontal, 16)

                    // Edit profile
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Thông tin cá nhân")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(c.textPrimary)

                        VStack(spacing: 10) {
                            editField("Họ tên", value: $settings.userName)
                            editField("Giới tính", value: $settings.userGender)
                            editField("Ngày sinh", value: $settings.userBirthDate)
                            editField("Giờ sinh", value: $settings.userBirthHour)
                        }
                    }
                    .padding(16)
                    .background(
                        RoundedRectangle(cornerRadius: 16)
                            .fill(c.surface)
                    )
                    .padding(.horizontal, 16)
                }
            }
        }
        .background(c.bg)
    }

    @ViewBuilder
    private func profileButton(icon: String, title: String, subtitle: String, color: Color, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: 14) {
                Image(systemName: icon)
                    .font(.system(size: 22))
                    .foregroundColor(color)
                    .frame(width: 40)
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(c.textPrimary)
                    Text(subtitle)
                        .font(.system(size: 12))
                        .foregroundColor(c.textTertiary)
                }
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.system(size: 13))
                    .foregroundColor(c.textQuaternary)
            }
            .padding(14)
            .background(
                RoundedRectangle(cornerRadius: 14)
                    .fill(c.surface)
            )
        }
    }

    @ViewBuilder
    private func editField(_ title: String, value: Binding<String>) -> some View {
        HStack {
            Text(title)
                .font(.system(size: 13))
                .foregroundColor(c.textTertiary)
                .frame(width: 80, alignment: .leading)
            TextField(title, text: value)
                .font(.system(size: 14))
                .foregroundColor(c.textPrimary)
        }
    }
}
