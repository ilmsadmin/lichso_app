import SwiftUI

struct ArticleDetailScreen: View {
    let articleId: String
    
    @Environment(\.dismiss) private var dismiss
    @State private var article: Article? = nil
    @State private var isLoading = true
    @State private var errorMessage: String? = nil
    
    var body: some View {
        ZStack {
            LSTheme.bg.ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Top header bar
                headerBar
                
                if isLoading {
                    loadingView
                } else if let error = errorMessage {
                    errorView(error)
                } else if let article = article {
                    articleContent(article)
                }
            }
        }
        .navigationBarHidden(true)
        .task {
            await loadArticle()
        }
    }
    
    private var headerBar: some View {
        HStack {
            Button { dismiss() } label: {
                Image(systemName: "chevron.left")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(LSTheme.textPrimary)
                    .frame(width: 44, height: 44)
            }
            
            Spacer()
            
            Text("Chi Tiết Bài Viết")
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(LSTheme.textPrimary)
            
            Spacer()
            
            // Dummy spacer to balance back button
            Spacer().frame(width: 44)
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 8)
        .background(LSTheme.surfaceContainer)
        .border(width: 0.5, edges: [.bottom], color: LSTheme.outlineVariant.opacity(0.3))
    }
    
    private var loadingView: some View {
        Box(modifier: .fillMaxSize) {
            ProgressView()
                .tint(LSTheme.primary)
                .scaleEffect(1.2)
        }
    }
    
    private func errorView(_ message: String) -> some View {
        VStack(spacing: 16) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 40))
                .foregroundColor(LSTheme.primary.opacity(0.8))
            
            Text(message)
                .font(.system(size: 14))
                .foregroundColor(LSTheme.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            
            Button("Thử lại") {
                Task {
                    await loadArticle()
                }
            }
            .font(.system(size: 14, weight: .semibold))
            .foregroundColor(.white)
            .padding(.horizontal, 24)
            .padding(.vertical, 10)
            .background(LSTheme.primary)
            .cornerRadius(20)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
    
    private func articleContent(_ article: Article) -> some View {
        ScrollView(.vertical, showsIndicators: true) {
            VStack(alignment: .leading, spacing: 16) {
                // Featured Image
                if let imgUrlStr = article.featuredImage, let imgUrl = URL(string: imgUrlStr) {
                    AsyncImage(url: imgUrl) { phase in
                        switch phase {
                        case .success(let image):
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                                .frame(height: 200)
                                .clipped()
                                .cornerRadius(12)
                        case .failure:
                            // Fallback silent block
                            Color.clear.frame(height: 0)
                        case .empty:
                            ProgressView()
                                .frame(height: 200)
                                .frame(maxWidth: .infinity)
                                .background(LSTheme.surfaceContainer)
                                .cornerRadius(12)
                        @unknown default:
                            EmptyView()
                        }
                    }
                }
                
                // Metadata: Category & Read Time
                HStack(spacing: 8) {
                    if let category = article.category {
                        Text(category.name)
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(LSTheme.primary)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 4)
                            .background(LSTheme.primary.opacity(0.1))
                            .cornerRadius(6)
                    }
                    
                    if let readingTime = article.readingTime {
                        Text("•  \(readingTime) phút đọc")
                            .font(.system(size: 12))
                            .foregroundColor(LSTheme.textTertiary)
                    }
                    
                    Spacer()
                }
                
                // Title
                Text(article.title)
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(LSTheme.textPrimary)
                    .lineSpacing(4)
                
                // Date
                if let pubAt = article.publishedAt {
                    Text("Đăng ngày: \(pubAt.prefix(10))")
                        .font(.system(size: 12))
                        .foregroundColor(LSTheme.textTertiary)
                }
                
                Divider()
                    .background(LSTheme.outlineVariant.opacity(0.5))
                
                // Content
                Text(article.content ?? article.excerpt ?? "Không có nội dung bài viết.")
                    .font(.system(size: 15))
                    .foregroundColor(LSTheme.textSecondary)
                    .lineSpacing(8)
                    .multilineTextAlignment(.leading)
            }
            .padding(16)
        }
    }
    
    private func loadArticle() async {
        isLoading = true
        errorMessage = nil
        do {
            let fetched = try await QuizService.shared.fetchArticle(id: articleId)
            self.article = fetched
            self.isLoading = false
        } catch {
            self.errorMessage = "Không thể tải chi tiết bài viết: \(error.localizedDescription)"
            self.isLoading = false
        }
    }
}

// Helpers for View alignment
private struct Box<Content: View>: View {
    let modifier: FrameModifier
    let content: () -> Content
    
    enum FrameModifier {
        case fillMaxSize
    }
    
    var body: some View {
        Group {
            switch modifier {
            case .fillMaxSize:
                content().frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
    }
}

// Border utility matching WebApp styling
private extension View {
    func border(width: CGFloat, edges: [Edge], color: Color) -> some View {
        overlay(EdgeBorder(width: width, edges: edges).foregroundColor(color))
    }
}

private struct EdgeBorder: Shape {
    var width: CGFloat
    var edges: [Edge]

    func path(in rect: CGRect) -> Path {
        var path = Path()
        for edge in edges {
            var x: CGFloat {
                switch edge {
                case .top, .bottom, .leading: return rect.minX
                case .trailing: return rect.maxX - width
                }
            }

            var y: CGFloat {
                switch edge {
                case .top, .leading, .trailing: return rect.minY
                case .bottom: return rect.maxY - width
                }
            }

            var w: CGFloat {
                switch edge {
                case .top, .bottom: return rect.width
                case .leading, .trailing: return width
                }
            }

            var h: CGFloat {
                switch edge {
                case .top, .bottom: return width
                case .leading, .trailing: return rect.height
                }
            }

            path.addRect(CGRect(x: x, y: y, width: w, height: h))
        }
        return path
    }
}
