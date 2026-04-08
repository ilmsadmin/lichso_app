import SwiftUI
import SwiftData

struct BookmarksScreen: View {
    @Environment(\.lichSoColors) private var c
    @Environment(\.modelContext) private var modelContext
    @State private var bookmarks: [BookmarkEntity] = []
    var onBackClick: () -> Void = {}
    var onDateSelected: (Int, Int, Int) -> Void = { _, _, _ in }

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(c.textPrimary)
                }
                Spacer()
                Text("NGÀY ĐÃ LƯU")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(c.textPrimary)
                    .tracking(1)
                Spacer()
                Color.clear.frame(width: 24)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)

            ScrollView {
                if bookmarks.isEmpty {
                    VStack(spacing: 12) {
                        Image(systemName: "bookmark")
                            .font(.system(size: 48))
                            .foregroundColor(c.textQuaternary)
                            .padding(.top, 80)
                        Text("Chưa có ngày nào được lưu")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(c.textSecondary)
                        Text("Mở lịch tháng và nhấn giữ một ngày để đánh dấu")
                            .font(.system(size: 13))
                            .foregroundColor(c.textTertiary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 40)
                    }
                    .frame(maxWidth: .infinity)
                }
                LazyVStack(spacing: 8) {
                    ForEach(bookmarks, id: \.id) { bookmark in
                        Button(action: {
                            onDateSelected(bookmark.solarYear, bookmark.solarMonth, bookmark.solarDay)
                        }) {
                            HStack(spacing: 12) {
                                VStack(spacing: 2) {
                                    Text("\(bookmark.solarDay)")
                                        .font(.system(size: 20, weight: .bold))
                                        .foregroundColor(c.primary)
                                    Text("\(bookmark.solarMonth)/\(bookmark.solarYear)")
                                        .font(.system(size: 11))
                                        .foregroundColor(c.textTertiary)
                                }
                                .frame(width: 56)
                                VStack(alignment: .leading, spacing: 3) {
                                    Text(bookmark.note.isEmpty ? "Ngày \(bookmark.solarDay)/\(bookmark.solarMonth)/\(bookmark.solarYear)" : bookmark.note)
                                        .font(.system(size: 14, weight: .medium))
                                        .foregroundColor(c.textPrimary)
                                    Text(formatDate(bookmark.createdAt))
                                        .font(.system(size: 11))
                                        .foregroundColor(c.textTertiary)
                                }
                                Spacer()
                                Button(action: {
                                    deleteBookmark(bookmark)
                                }) {
                                    Image(systemName: "trash")
                                        .font(.system(size: 14))
                                        .foregroundColor(c.textTertiary)
                                }
                            }
                            .padding(12)
                            .background(
                                RoundedRectangle(cornerRadius: 12)
                                    .fill(c.surface)
                            )
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
            }
        }
        .background(c.bg)
        .onAppear { loadBookmarks() }
    }

    private func loadBookmarks() {
        let descriptor = FetchDescriptor<BookmarkEntity>(
            sortBy: [SortDescriptor(\.createdAt, order: .reverse)]
        )
        bookmarks = (try? modelContext.fetch(descriptor)) ?? []
    }

    private func deleteBookmark(_ bookmark: BookmarkEntity) {
        modelContext.delete(bookmark)
        try? modelContext.save()
        loadBookmarks()
    }

    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy HH:mm"
        return formatter.string(from: date)
    }
}
