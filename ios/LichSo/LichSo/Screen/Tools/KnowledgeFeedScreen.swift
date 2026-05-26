import SwiftUI

struct KnowledgeArticle: Identifiable {
    let id = UUID()
    let title: String
    let category: String
    let summary: String
    let readTime: String
    let accentColor: Color
}

struct KnowledgeFeedScreen: View {
    @Environment(\.dismiss) private var dismiss
    
    // Fetch today's events from the historical provider
    private var todayEvents: [HistoricalEvent] {
        let calendar = Calendar.current
        let today = Date()
        let dd = calendar.component(.day, from: today)
        let mm = calendar.component(.month, from: today)
        return HistoricalEventProvider.getEvents(day: dd, month: mm)
    }
    
    @State private var articles: [Article] = []
    @State private var isLoadingArticles = true
    
    var body: some View {
        ZStack {
            LSTheme.bg.ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Top bar
                HStack {
                    Button { dismiss() } label: {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundColor(LSTheme.textPrimary)
                            .frame(width: 44, height: 44)
                    }
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Khám Phá Kiến Thức")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("Sự kiện lịch sử & bài viết văn hóa")
                            .font(.system(size: 12))
                            .foregroundColor(LSTheme.textTertiary)
                    }
                    
                    Spacer()
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 8)
                .background(LSTheme.surfaceContainer)
                
                ScrollView {
                    VStack(spacing: 20) {
                        Spacer().frame(height: 10)
                        
                        // Today's History Section
                        VStack(alignment: .leading, spacing: 12) {
                            Text("SỰ KIỆN NỔI BẬT HÔM NAY")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(LSTheme.primary)
                                .padding(.horizontal, 16)
                            
                            if todayEvents.isEmpty {
                                Text("Không có sự kiện đặc biệt nào ghi nhận hôm nay.")
                                    .font(.system(size: 13))
                                    .foregroundColor(LSTheme.textSecondary)
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 10)
                            } else {
                                ForEach(todayEvents) { event in
                                    eventCard(event)
                                }
                            }
                        }
                        
                        // Recommended Articles Section
                        VStack(alignment: .leading, spacing: 12) {
                            Text("BÀI VIẾT ĐỌC NHIỀU")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(LSTheme.textSecondary)
                                .padding(.horizontal, 16)
                            
                            if isLoadingArticles {
                                ProgressView()
                                    .padding(.horizontal, 16)
                            } else if articles.isEmpty {
                                Text("Chưa có bài viết nào.")
                                    .font(.system(size: 13))
                                    .foregroundColor(LSTheme.textSecondary)
                                    .padding(.horizontal, 16)
                            } else {
                                ForEach(articles) { article in
                                    NavigationLink(destination: ArticleDetailScreen(articleId: article.id)) {
                                        articleCard(article)
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                        }
                    }
                    .padding(.bottom, 24)
                }
            }
        }
        .navigationBarHidden(true)
        .task {
            await loadArticles()
        }
    }
    
    private func loadArticles() async {
        isLoadingArticles = true
        do {
            let list = try await QuizService.shared.fetchArticles(page: 1, limit: 20)
            self.articles = list
            self.isLoadingArticles = false
        } catch {
            print("Error loading articles: \(error)")
            self.isLoadingArticles = false
        }
    }
    
    private func eventCard(_ event: HistoricalEvent) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text("Năm \(event.year)")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(LSTheme.primary)
                Spacer()
                Text(event.importance == .major ? "Đại sự" : "Sự kiện")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background(event.importance == .major ? LSTheme.primary : LSTheme.textSecondary)
                    .cornerRadius(8)
            }
            
            Text(event.title)
                .font(.system(size: 15, weight: .bold))
                .foregroundColor(LSTheme.textPrimary)
            
            Text(event.description)
                .font(.system(size: 13))
                .foregroundColor(LSTheme.textSecondary)
                .lineSpacing(4)
        }
        .padding(16)
        .background(LSTheme.surfaceContainer)
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(LSTheme.outlineVariant.opacity(0.3), lineWidth: 1)
        )
        .padding(.horizontal, 16)
    }
    
    private func articleCard(_ article: Article) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                if let category = article.category {
                    Text(category.name)
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(LSTheme.primary)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(LSTheme.primary.opacity(0.12))
                        .cornerRadius(6)
                }
                
                Spacer()
                
                if let readingTime = article.readingTime {
                    Text("\(readingTime) phút đọc")
                        .font(.system(size: 11))
                        .foregroundColor(LSTheme.textTertiary)
                }
            }
            
            Text(article.title)
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(LSTheme.textPrimary)
            
            if let summary = article.excerpt ?? article.content {
                Text(summary)
                    .font(.system(size: 13))
                    .foregroundColor(LSTheme.textSecondary)
                    .lineSpacing(4)
                    .lineLimit(3)
            }
        }
        .padding(16)
        .background(LSTheme.surfaceContainer)
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(LSTheme.outlineVariant.opacity(0.3), lineWidth: 1)
        )
        .padding(.horizontal, 16)
    }
}
