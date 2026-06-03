import SwiftUI

// ═══════════════════════════════════════════
// KnowledgeFeedScreen — màn "Khám Phá" (port giống Android KnowledgeFeedScreen.kt).
// Danh ngôn hôm nay · Ngày này năm xưa · Nhân vật sinh nhật · Danh mục · Bài viết
// (phân trang + kéo làm mới + cuộn vô hạn). Nguồn dữ liệu: server (không còn dùng
// HistoricalEventProvider cục bộ như bản iOS cũ).
// ═══════════════════════════════════════════

struct KnowledgeFeedScreen: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var vm = KnowledgeFeedViewModel()

    @State private var aiPrompt = ""
    @State private var showAi = false

    var body: some View {
        VStack(spacing: 0) {
            topBar

            if vm.isLoading {
                loadingSkeleton
            } else {
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: 0) {
                        if let quote = vm.quote {
                            QuoteCard(quote: quote) {
                                aiPrompt = "Giải thích sâu hơn về câu nói: \"\(quote.quote)\""
                                showAi = true
                            }
                        }

                        if !vm.events.isEmpty {
                            sectionHeader("Ngày này năm xưa")
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 12) {
                                    ForEach(vm.events) { EventCard(event: $0) }
                                }
                                .padding(.horizontal, 16)
                            }
                        }

                        if !vm.famousPeople.isEmpty {
                            sectionHeader("Nhân vật sinh nhật hôm nay")
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 12) {
                                    ForEach(vm.famousPeople) { PersonCard(person: $0) }
                                }
                                .padding(.horizontal, 16)
                            }
                        }

                        if !vm.categories.isEmpty {
                            sectionHeader("Danh mục bài viết")
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 12) {
                                    CategoryCard(name: "Tất cả", imageUrl: nil,
                                                 selected: vm.selectedCategorySlug == nil) {
                                        vm.selectCategory(nil)
                                    }
                                    ForEach(vm.categories) { category in
                                        CategoryCard(name: category.name, imageUrl: category.imageUrl,
                                                     selected: vm.selectedCategorySlug == category.slug) {
                                            vm.selectCategory(category)
                                        }
                                    }
                                }
                                .padding(.horizontal, 16)
                            }
                        }

                        articlesSection
                        emptyStateIfNeeded
                    }
                    .padding(.bottom, 24)
                }
                .refreshable { await vm.load(isRefresh: true) }
            }
        }
        .background(LSTheme.bg.ignoresSafeArea())
        .navigationBarHidden(true)
        .onAppear { vm.loadIfNeeded() }
        .fullScreenCover(isPresented: $showAi) {
            NavigationStack { AIChatScreen(initialMessage: aiPrompt) }
        }
    }

    // ── Top bar (đỏ gradient giống AppTopBar Android) ──
    private var topBar: some View {
        HStack(spacing: 12) {
            Button { dismiss() } label: {
                Image(systemName: "chevron.left")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(width: 40, height: 40)
            }
            VStack(alignment: .leading, spacing: 2) {
                Text("Khám Phá")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.white)
                Text("Kiến thức hôm nay")
                    .font(.system(size: 12))
                    .foregroundColor(.white.opacity(0.85))
            }
            Spacer()
            Button { vm.refresh() } label: {
                Image(systemName: "arrow.clockwise")
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(width: 40, height: 40)
            }
        }
        .padding(.horizontal, 8)
        .padding(.bottom, 10)
        .background(
            LinearGradient(
                colors: [Color(red: 0.773, green: 0.157, blue: 0.157),
                         Color(red: 0.545, green: 0, blue: 0)],
                startPoint: .topLeading, endPoint: .bottomTrailing
            )
            .ignoresSafeArea(edges: .top)
        )
    }

    private var loadingSkeleton: some View {
        VStack(spacing: 12) {
            ForEach(0..<6, id: \.self) { _ in
                RoundedRectangle(cornerRadius: 12)
                    .fill(LSTheme.surfaceContainer)
                    .frame(height: 84)
                    .padding(.horizontal, 16)
            }
            Spacer()
        }
        .padding(.top, 12)
        .redacted(reason: .placeholder)
    }

    @ViewBuilder private var articlesSection: some View {
        if !vm.articles.isEmpty {
            sectionHeader(vm.selectedCategoryName ?? "Bài viết mới nhất")
            ForEach(vm.articles) { article in
                NavigationLink(destination: ArticleDetailScreen(articleId: article.id)) {
                    ArticleCard(article: article)
                }
                .buttonStyle(.plain)
                .onAppear {
                    if article.id == vm.articles.last?.id { vm.loadMoreArticles() }
                }
            }
            if vm.isLoadingMore {
                centerSpinner.padding(.vertical, 16)
            } else if !vm.hasMoreArticles {
                Text("Bạn đã xem hết bài viết hiện có")
                    .font(.system(size: 12))
                    .foregroundColor(LSTheme.textTertiary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
            }
        } else if !vm.categories.isEmpty && vm.isLoadingMore {
            centerSpinner.padding(.vertical, 24)
        } else if !vm.categories.isEmpty && vm.selectedCategorySlug != nil {
            Text("Chưa có bài viết trong danh mục này")
                .font(.system(size: 13))
                .foregroundColor(LSTheme.textTertiary)
                .frame(maxWidth: .infinity)
                .padding(.horizontal, 16).padding(.vertical, 24)
        }
    }

    @ViewBuilder private var emptyStateIfNeeded: some View {
        if vm.events.isEmpty && vm.famousPeople.isEmpty && vm.articles.isEmpty && vm.quote == nil {
            Text("Không có dữ liệu hôm nay")
                .font(.system(size: 14))
                .foregroundColor(LSTheme.textTertiary)
                .frame(maxWidth: .infinity)
                .padding(.top, 64)
        }
    }

    private var centerSpinner: some View {
        ProgressView()
            .tint(LSTheme.primary)
            .frame(maxWidth: .infinity)
    }

    private func sectionHeader(_ title: String) -> some View {
        HStack(spacing: 8) {
            RoundedRectangle(cornerRadius: 2)
                .fill(LSTheme.primary)
                .frame(width: 3, height: 18)
            Text(title)
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(LSTheme.textPrimary)
        }
        .padding(.horizontal, 16).padding(.vertical, 8)
    }
}

// ══════════════════════════════════════════
// CARDS
// ══════════════════════════════════════════

private struct QuoteCard: View {
    let quote: Quote
    let onAskAi: () -> Void

    var body: some View {
        Button(action: onAskAi) {
            VStack(alignment: .leading, spacing: 8) {
                HStack(spacing: 6) {
                    Image(systemName: "quote.opening")
                        .font(.system(size: 16))
                        .foregroundColor(.white.opacity(0.7))
                    Text("Danh ngôn hôm nay")
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundColor(.white.opacity(0.7))
                }
                Text(quote.quote)
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(.white)
                    .lineSpacing(5)
                    .frame(maxWidth: .infinity, alignment: .leading)
                if let author = quote.author, !author.isEmpty {
                    Text("— \(author)")
                        .font(.system(size: 12))
                        .foregroundColor(.white.opacity(0.75))
                }
            }
            .padding(16)
            .background(
                LinearGradient(
                    colors: [Color(hex: "8B0000"), Color(hex: "D4A017")],
                    startPoint: .topLeading, endPoint: .bottomTrailing
                )
            )
            .clipShape(RoundedRectangle(cornerRadius: 16))
        }
        .buttonStyle(.plain)
        .padding(.horizontal, 16).padding(.vertical, 12)
    }
}

private struct EventCard: View {
    let event: ContentEvent
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            if let year = event.eventYear {
                Text("\(year)")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(LSTheme.primary)
                    .padding(.horizontal, 8).padding(.vertical, 3)
                    .background(LSTheme.primary.opacity(0.15))
                    .clipShape(RoundedRectangle(cornerRadius: 6))
            }
            Text(event.title)
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(LSTheme.textPrimary)
                .lineLimit(2)
            let desc = event.shortDescription ?? event.description
            if let desc, !desc.isEmpty {
                Text(desc)
                    .font(.system(size: 11))
                    .foregroundColor(LSTheme.textSecondary)
                    .lineLimit(3)
            }
        }
        .padding(12)
        .frame(width: 200, alignment: .leading)
        .background(LSTheme.surfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(LSTheme.outlineVariant, lineWidth: 1))
    }
}

private struct PersonCard: View {
    let person: FamousPerson
    private var initial: String { String(person.name.first ?? "?").uppercased() }
    var body: some View {
        VStack(spacing: 8) {
            ZStack {
                Circle()
                    .fill(LinearGradient(
                        colors: [Color(hex: "1565C0"), Color(hex: "7B1FA2")],
                        startPoint: .topLeading, endPoint: .bottomTrailing
                    ))
                    .frame(width: 48, height: 48)
                Text(initial)
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(.white)
            }
            Text(person.name)
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(LSTheme.textPrimary)
                .multilineTextAlignment(.center)
                .lineLimit(2)
            if let year = person.birthYear {
                Text("Sinh \(year)")
                    .font(.system(size: 11))
                    .foregroundColor(LSTheme.textTertiary)
            }
        }
        .padding(12)
        .frame(width: 140)
        .background(LSTheme.surfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(LSTheme.outlineVariant, lineWidth: 1))
    }
}

private struct CategoryCard: View {
    let name: String
    let imageUrl: String?
    let selected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            ZStack(alignment: .bottomLeading) {
                if let normalized = normalizeContentImageUrl(imageUrl), let url = URL(string: normalized) {
                    CachedAsyncImage(url: url) { image in
                        image.resizable().aspectRatio(contentMode: .fill)
                    } placeholder: {
                        LSTheme.surfaceContainer
                    }
                    .frame(width: 132, height: 92)
                    .clipped()
                    LinearGradient(colors: [.clear, .black.opacity(0.72)],
                                   startPoint: .top, endPoint: .bottom)
                } else {
                    LinearGradient(
                        colors: [LSTheme.primary.opacity(0.82), LSTheme.gold.opacity(0.72)],
                        startPoint: .topLeading, endPoint: .bottomTrailing
                    )
                    Image(systemName: "doc.text")
                        .font(.system(size: 26))
                        .foregroundColor(.white.opacity(0.36))
                        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topTrailing)
                        .padding(10)
                }
                Text(name)
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(.white)
                    .lineLimit(2)
                    .padding(12)
            }
            .frame(width: 132, height: 92)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(selected ? LSTheme.gold : LSTheme.outlineVariant,
                            lineWidth: selected ? 2 : 1)
            )
        }
        .buttonStyle(.plain)
    }
}

private struct ArticleCard: View {
    let article: Article
    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            if let normalized = normalizeContentImageUrl(article.featuredImage), let url = URL(string: normalized) {
                CachedAsyncImage(url: url) { image in
                    image.resizable().aspectRatio(contentMode: .fill)
                } placeholder: {
                    LSTheme.surfaceContainerHigh
                }
                .frame(width: 64, height: 64)
                .clipShape(RoundedRectangle(cornerRadius: 8))
            } else {
                ZStack {
                    LSTheme.surfaceContainerHigh
                    Image(systemName: "doc.text")
                        .font(.system(size: 26))
                        .foregroundColor(LSTheme.textTertiary)
                }
                .frame(width: 64, height: 64)
                .clipShape(RoundedRectangle(cornerRadius: 8))
            }
            VStack(alignment: .leading, spacing: 4) {
                Text(article.title)
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(LSTheme.textPrimary)
                    .lineLimit(2)
                if let cat = article.category?.name, !cat.isEmpty {
                    Text(cat)
                        .font(.system(size: 10, weight: .medium))
                        .foregroundColor(LSTheme.gold)
                        .padding(.horizontal, 6).padding(.vertical, 2)
                        .background(LSTheme.gold.opacity(0.15))
                        .clipShape(RoundedRectangle(cornerRadius: 4))
                }
                if let published = article.publishedAt, !published.isEmpty {
                    Text(String(published.prefix(10)))
                        .font(.system(size: 11))
                        .foregroundColor(LSTheme.textTertiary)
                }
            }
            Spacer(minLength: 0)
        }
        .padding(12)
        .background(LSTheme.surfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(LSTheme.outlineVariant, lineWidth: 1))
        .padding(.horizontal, 16).padding(.vertical, 6)
    }
}

// ── Chuẩn hoá URL ảnh nội dung (mirror Android normalizeContentImageUrl) ──
func normalizeContentImageUrl(_ url: String?) -> String? {
    guard let url, !url.isEmpty else { return nil }
    if url.hasPrefix("http://") || url.hasPrefix("https://") { return url }
    let cleaned = url.hasPrefix("/") ? String(url.dropFirst()) : url
    if cleaned.hasPrefix("api/uploads/") { return "https://lichso.vn/\(cleaned)" }
    if cleaned.hasPrefix("uploads/") { return "https://lichso.vn/api/\(cleaned)" }
    return "https://lichso.vn/\(cleaned)"
}
