import SwiftUI

// ═══════════════════════════════════════════
// KnowledgeFeedViewModel — màn "Khám Phá".
// Port từ Android `KnowledgeFeedViewModel.kt`: danh ngôn hôm nay, sự kiện "ngày này
// năm xưa", nhân vật sinh nhật, danh mục lọc, và bài viết phân trang (load more).
// ═══════════════════════════════════════════

@MainActor
final class KnowledgeFeedViewModel: ObservableObject {
    @Published var isLoading = true
    @Published var isRefreshing = false
    @Published var isLoadingMore = false
    @Published var events: [ContentEvent] = []
    @Published var famousPeople: [FamousPerson] = []
    @Published var categories: [ArticleCategory] = []
    @Published var articles: [Article] = []
    @Published var quote: Quote?
    @Published var selectedCategorySlug: String?
    @Published var selectedCategoryName: String?
    @Published var hasMoreArticles = true
    @Published var error: String?

    private let pageSize = 10
    private var nextArticlePage = 1
    private var didLoad = false

    private let service = QuizService.shared

    /// Tải lần đầu (gọi 1 lần khi xuất hiện).
    func loadIfNeeded() {
        guard !didLoad else { return }
        didLoad = true
        Task { await load(isRefresh: false) }
    }

    func refresh() {
        Task { await load(isRefresh: true) }
    }

    func load(isRefresh: Bool) async {
        nextArticlePage = 1
        isLoading = !isRefresh
        isRefreshing = isRefresh
        isLoadingMore = false
        error = nil
        hasMoreArticles = true

        let now = Date()
        let cal = Calendar.current
        let month = cal.component(.month, from: now)
        let day = cal.component(.day, from: now)
        let slug = selectedCategorySlug

        // Tải song song như Android (async let).
        async let eventsTask = try? service.fetchEventsByDate(month: month, day: day)
        async let peopleTask = try? service.fetchFamousPeople(month: month, day: day)
        async let categoriesTask = try? service.fetchCategories()
        async let articlesTask = try? service.fetchArticles(page: 1, limit: pageSize, category: slug)
        async let quoteTask = try? service.fetchTodayQuote()

        let loadedEvents = await eventsTask ?? []
        let loadedPeople = await peopleTask ?? []
        let loadedCategories = flatten(await categoriesTask ?? [])
        let loadedArticles = await articlesTask ?? []
        let loadedQuote = await quoteTask

        events = loadedEvents
        famousPeople = loadedPeople
        categories = loadedCategories
        articles = loadedArticles
        quote = loadedQuote
        selectedCategoryName = loadedCategories.first { $0.slug == slug }?.name
        hasMoreArticles = loadedArticles.count >= pageSize
        nextArticlePage = 2
        isLoading = false
        isRefreshing = false
    }

    func selectCategory(_ category: ArticleCategory?) {
        let slug = category?.slug
        if selectedCategorySlug == slug { return }
        selectedCategorySlug = slug
        selectedCategoryName = category?.name
        articles = []
        isLoadingMore = true
        hasMoreArticles = true
        nextArticlePage = 1
        Task {
            do {
                let list = try await service.fetchArticles(page: 1, limit: pageSize, category: slug)
                guard selectedCategorySlug == slug else { return }   // bỏ qua nếu đã đổi danh mục
                nextArticlePage = 2
                articles = list
                hasMoreArticles = list.count >= pageSize
                isLoadingMore = false
            } catch {
                guard selectedCategorySlug == slug else { return }
                isLoadingMore = false
                self.error = "Không thể tải bài viết. Vui lòng thử lại."
            }
        }
    }

    func loadMoreArticles() {
        guard !isLoading, !isLoadingMore, hasMoreArticles else { return }
        let slug = selectedCategorySlug
        let page = nextArticlePage
        isLoadingMore = true
        error = nil
        Task {
            do {
                let newArticles = try await service.fetchArticles(page: page, limit: pageSize, category: slug)
                guard selectedCategorySlug == slug else { return }
                let merged = (articles + newArticles).reduce(into: [Article]()) { acc, a in
                    if !acc.contains(where: { $0.id == a.id }) { acc.append(a) }
                }
                nextArticlePage = page + 1
                articles = merged
                hasMoreArticles = newArticles.count >= pageSize
                isLoadingMore = false
            } catch {
                guard selectedCategorySlug == slug else { return }
                isLoadingMore = false
                self.error = "Không thể tải thêm bài viết. Vui lòng thử lại."
            }
        }
    }

    /// Trải phẳng danh mục cha + con (như Android flattenCategories), loại trùng id.
    private func flatten(_ categories: [ArticleCategory]) -> [ArticleCategory] {
        var result: [ArticleCategory] = []
        var seen = Set<String>()
        func walk(_ list: [ArticleCategory]) {
            for c in list {
                if seen.insert(c.id).inserted { result.append(c) }
                if let children = c.children { walk(children) }
            }
        }
        walk(categories)
        return result
    }
}
