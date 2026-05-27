import SwiftUI

struct iOSQuizCategory: Identifiable {
    let id: String
    let label: String
    let icon: String
    let colors: [Color]
}

struct QuizHomeScreen: View {
    @EnvironmentObject private var googleAuth: GoogleAuthService
    @State private var dailyQuestionCount: Int?
    @State private var categoryCounts: [String: Int] = [:]
    @State private var isLoadingCategoryCounts = true
    
    let categories = [
        iOSQuizCategory(id: "history_vn", label: "Lịch sử VN", icon: "book.fill", colors: [Color(hex: "8B0000"), Color(hex: "D32F2F")]),
        iOSQuizCategory(id: "history_world", label: "Thế giới", icon: "globe", colors: [Color(hex: "1565C0"), Color(hex: "42A5F5")]),
        iOSQuizCategory(id: "culture", label: "Văn hóa", icon: "character.book.closed.fill", colors: [Color(hex: "2E7D32"), Color(hex: "66BB6A")]),
        iOSQuizCategory(id: "geography", label: "Địa lý", icon: "map.fill", colors: [Color(hex: "E65100"), Color(hex: "FFFFB7")]),
        iOSQuizCategory(id: "general", label: "Tổng hợp", icon: "lightbulb.fill", colors: [Color(hex: "00695C"), Color(hex: "4DB6AC")])
    ]
    
    @State private var showSession = false
    @State private var selectedSessionType = "daily"
    @State private var selectedCategory: String? = nil
    @State private var showLeaderboard = false
    @State private var showLogin = false
    
    var body: some View {
        NavigationStack {
            ZStack {
                LSTheme.bg.ignoresSafeArea()
                
                VStack(spacing: 0) {
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Đố Vui")
                                .font(.system(size: 20, weight: .bold))
                                .foregroundColor(.white)
                            Text("Thi đấu và tích điểm thưởng")
                                .font(.system(size: 12))
                                .foregroundColor(.white.opacity(0.78))
                        }
                        
                        Spacer()
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 12)
                    .padding(.bottom, 12)
                    .background(
                        LinearGradient(
                            colors: [LSTheme.primary, LSTheme.deepRed],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    
                    ScrollView {
                        VStack(spacing: 16) {
                            // Daily Quiz Card
                            dailyQuizCard

                            if !googleAuth.isSignedIn {
                                quizLoginNudge
                            }
                            
                            rulesCard
                            
                            // Category list header
                            sectionHeader(title: "Chọn chủ đề", icon: "folder.fill")
                            
                            categoryGrid
                            
                            // Leaderboard header
                            sectionHeader(title: "Bảng xếp hạng", icon: "crown.fill")
                            
                            leaderboardButton
                        }
                        .padding(16)
                    }
                }
            }
            .fullScreenCover(isPresented: $showSession) {
                QuizSessionScreen(sessionType: selectedSessionType, category: selectedCategory)
            }
            .sheet(isPresented: $showLeaderboard) {
                LeaderboardScreen()
            }
            .fullScreenCover(isPresented: $showLogin) {
                LoginScreen().environmentObject(googleAuth)
            }
            .task {
                await loadOverview()
            }
        }
    }

    private func loadOverview() async {
        isLoadingCategoryCounts = true
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        let today = formatter.string(from: Date())
        async let daily = try? QuizService.shared.fetchDailyQuestions(date: today)
        let loadedDaily = await daily
        dailyQuestionCount = loadedDaily?.count

        var counts: [String: Int] = [:]
        for category in categories {
            let questions = try? await QuizService.shared.fetchQuestions(category: category.id, limit: 50)
            counts[category.id] = questions?.count ?? 0
        }
        categoryCounts = counts
        isLoadingCategoryCounts = false
    }
    
    private var dailyQuizCard: some View {
        Button(action: {
            selectedSessionType = "daily"
            selectedCategory = nil
            showSession = true
        }) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 6) {
                        Image(systemName: "calendar")
                            .font(.system(size: 18))
                            .foregroundColor(.white)
                        Text("Bộ đề hôm nay")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.white)
                    }
                    Text("\(dailyQuestionCount ?? 15) câu • Hôm nay")
                        .font(.system(size: 12))
                        .foregroundColor(.white.opacity(0.8))
                    
                    Spacer().frame(height: 4)
                    
                    HStack(spacing: 4) {
                        Image(systemName: "trophy.fill")
                            .font(.system(size: 12))
                            .foregroundColor(LSTheme.gold)
                        Text("Trả lời đúng để leo hạng")
                            .font(.system(size: 11))
                            .foregroundColor(.white.opacity(0.9))
                    }
                }
                
                Spacer()
                
                Image(systemName: "play.fill")
                    .font(.system(size: 20))
                    .foregroundColor(.white)
                    .frame(width: 44, height: 44)
                    .background(Color.white.opacity(0.2))
                    .cornerRadius(12)
            }
            .padding(16)
            .background(
                LinearGradient(
                    colors: [LSTheme.primary, LSTheme.deepRed],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            )
            .cornerRadius(20)
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    private var rulesCard: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(spacing: 6) {
                Image(systemName: "list.bullet.rectangle.portrait")
                    .font(.system(size: 14))
                    .foregroundColor(LSTheme.primary)
                Text("Luật chơi nhanh")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(LSTheme.textPrimary)
            }
            Text("Mỗi câu có 30 giây. Đúng được tính điểm leo bảng xếp hạng; điểm ngày dùng để mua trợ giúp khi bí.")
                .font(.system(size: 11))
                .foregroundColor(LSTheme.textSecondary)
                .lineSpacing(4)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(LSTheme.surfaceContainer)
        .cornerRadius(14)
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(LSTheme.outlineVariant.opacity(0.3), lineWidth: 1)
        )
    }
    
    private func sectionHeader(title: String, icon: String) -> some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 14))
                .foregroundColor(LSTheme.primary)
            Text(title)
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(LSTheme.textPrimary)
            Spacer()
        }
    }
    
    private var categoryGrid: some View {
        let columns = [
            GridItem(.flexible(), spacing: 10),
            GridItem(.flexible(), spacing: 10),
            GridItem(.flexible(), spacing: 10)
        ]
        
        return LazyVGrid(columns: columns, spacing: 10) {
            ForEach(categories) { cat in
                let count = categoryCounts[cat.id] ?? 0
                let isUnavailable = !isLoadingCategoryCounts && count == 0
                Button(action: {
                    guard !isUnavailable else { return }
                    selectedSessionType = "topic"
                    selectedCategory = cat.id
                    showSession = true
                }) {
                    VStack(spacing: 6) {
                        RoundedRectangle(cornerRadius: 10)
                            .fill(LinearGradient(colors: cat.colors, startPoint: .topLeading, endPoint: .bottomTrailing))
                            .frame(width: 40, height: 40)
                            .overlay(
                                Image(systemName: cat.icon)
                                    .font(.system(size: 18))
                                    .foregroundColor(.white)
                            )
                        
                        Text(cat.label)
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundColor(LSTheme.textPrimary)
                            .lineLimit(1)
                        
                        Text(
                            isLoadingCategoryCounts
                            ? "Đang tải"
                            : (isUnavailable ? "Chưa có quiz" : "\(count) câu")
                        )
                            .font(.system(size: 10))
                            .foregroundColor(LSTheme.textSecondary)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(LSTheme.surfaceContainer)
                    .cornerRadius(14)
                    .overlay(
                        RoundedRectangle(cornerRadius: 14)
                            .stroke(LSTheme.outlineVariant.opacity(0.3), lineWidth: 1)
                    )
                }
                .buttonStyle(PlainButtonStyle())
                .disabled(isUnavailable)
                .opacity(isUnavailable ? 0.45 : 1)
            }
        }
    }
    
    private var leaderboardButton: some View {
        Button(action: {
            showLeaderboard = true
        }) {
            HStack {
                HStack(spacing: 12) {
                    RoundedRectangle(cornerRadius: 12)
                        .fill(LinearGradient(colors: [Color(hex: "D4A017"), Color(hex: "EF6C00")], startPoint: .topLeading, endPoint: .bottomTrailing))
                        .frame(width: 44, height: 44)
                        .overlay(
                            Image(systemName: "crown.fill")
                                .font(.system(size: 20))
                                .foregroundColor(.white)
                        )
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Bảng xếp hạng")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(LSTheme.textPrimary)
                        Text("Top người chơi tuần này")
                            .font(.system(size: 12))
                            .foregroundColor(LSTheme.textSecondary)
                    }
                }
                
                Spacer()
                
                Image(systemName: "chevron.right")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(LSTheme.textTertiary)
            }
            .padding(14)
            .background(LSTheme.surfaceContainer)
            .cornerRadius(16)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(LSTheme.outlineVariant.opacity(0.3), lineWidth: 1)
            )
        }
        .buttonStyle(PlainButtonStyle())
    }

    private var quizLoginNudge: some View {
        HStack(spacing: 10) {
            Image(systemName: "person.crop.circle.badge.exclamationmark")
                .font(.system(size: 20))
                .foregroundColor(LSTheme.primary)
            VStack(alignment: .leading, spacing: 3) {
                Text("Đăng nhập để lưu điểm")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(LSTheme.textPrimary)
                Text("Khi chưa đăng nhập, điểm quiz chỉ lưu tạm trên thiết bị.")
                    .font(.system(size: 11))
                    .foregroundColor(LSTheme.textSecondary)
            }
            Spacer()
            Button("Đăng nhập") { showLogin = true }
                .font(.system(size: 12, weight: .bold))
                .padding(.horizontal, 10)
                .padding(.vertical, 7)
                .background(LSTheme.primary)
                .foregroundColor(.white)
                .cornerRadius(10)
        }
        .padding(12)
        .background(LSTheme.surfaceContainer)
        .cornerRadius(14)
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(LSTheme.outlineVariant.opacity(0.3), lineWidth: 1)
        )
    }
}
