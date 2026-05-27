import SwiftUI
import WebKit

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
        HTMLWebView(
            htmlContent: article.content ?? article.excerpt ?? "",
            featuredImage: article.featuredImage,
            title: article.title,
            categoryName: article.category?.name,
            readingTime: article.readingTime,
            publishedAt: article.publishedAt
        )
        .frame(maxWidth: .infinity, maxHeight: .infinity)
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

// HTML WebView Wrapper for SwiftUI
struct HTMLWebView: UIViewRepresentable {
    let htmlContent: String
    let featuredImage: String?
    let title: String
    let categoryName: String?
    let readingTime: Int?
    let publishedAt: String?

    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webView.backgroundColor = .clear
        webView.isOpaque = false
        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {
        let isDark = uiView.traitCollection.userInterfaceStyle == .dark
        let textColor = isDark ? "#F0E8D0" : "#1C1B1F"
        let subTextColor = isDark ? "#8A7E62" : "#857371"
        let primaryColor = isDark ? "#EF5350" : "#B71C1C"
        
        var headerHtml = ""
        if let imgUrlStr = featuredImage, let fullUrl = normalizeImageUrl(imgUrlStr) {
            headerHtml += "<img class='featured' src='\(fullUrl)' />"
        }
        
        headerHtml += "<h1>\(title)</h1>"
        
        headerHtml += "<div class='meta'>"
        if let cat = categoryName {
            headerHtml += "<span class='category'>\(cat)</span>"
        }
        if let read = readingTime {
            headerHtml += "<span class='read-time'>• \(read) phút đọc</span>"
        }
        if let pub = publishedAt {
            headerHtml += "<span class='date'>• Đăng ngày: \(pub.prefix(10))</span>"
        }
        headerHtml += "</div>"
        
        let styledHtml = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            font-size: 16px;
            line-height: 1.7;
            color: \(textColor);
            background-color: transparent;
            margin: 16px;
            padding: 0;
        }
        h1 {
            font-size: 24px;
            font-weight: bold;
            line-height: 1.3;
            margin-top: 16px;
            margin-bottom: 12px;
        }
        .meta {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
            font-size: 13px;
            color: \(subTextColor);
            margin-bottom: 20px;
            gap: 6px;
        }
        .category {
            font-weight: bold;
            color: \(primaryColor);
            background-color: \(primaryColor)1E;
            padding: 3px 8px;
            border-radius: 4px;
        }
        .featured {
            width: 100%;
            height: 200px;
            object-fit: cover;
            border-radius: 12px;
        }
        img:not(.featured) {
            max-width: 100%;
            height: auto;
            border-radius: 8px;
            margin: 12px 0;
        }
        hr {
            border: 0;
            border-top: 1px solid \(isDark ? "#5A4F42" : "#D8C2BF");
            margin: 20px 0;
        }
        p {
            margin-bottom: 16px;
        }
        </style>
        </head>
        <body>
        \(headerHtml)
        <hr>
        \(htmlContent)
        </body>
        </html>
        """
        
        let fixedHtml = fixContentImageUrls(styledHtml)
        uiView.loadHTMLString(fixedHtml, baseURL: URL(string: "https://lichso.vn"))
    }
    
    private func normalizeImageUrl(_ url: String) -> String? {
        if url.hasPrefix("http://") || url.hasPrefix("https://") {
            return url
        }
        let cleaned = url.hasPrefix("/") ? String(url.dropFirst()) : url
        if cleaned.hasPrefix("api/uploads/") {
            return "https://lichso.vn/" + cleaned
        } else if cleaned.hasPrefix("uploads/") {
            return "https://lichso.vn/api/" + cleaned
        }
        return "https://lichso.vn/" + cleaned
    }
    
    private func fixContentImageUrls(_ html: String) -> String {
        return html
            .replacingOccurrences(of: "src=\"/uploads/", with: "src=\"https://lichso.vn/api/uploads/")
            .replacingOccurrences(of: "src=\"/api/uploads/", with: "src=\"https://lichso.vn/api/uploads/")
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
