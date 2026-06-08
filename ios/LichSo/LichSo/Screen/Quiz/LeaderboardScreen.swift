import SwiftUI

struct LeaderboardScreen: View {
    @Environment(\.dismiss) private var dismiss
    
    @State private var selectedPeriod = "weekly"
    @State private var entries: [LeaderboardEntry] = []
    @State private var myRank: MyRankResponse? = nil
    @State private var isLoading = false
    @State private var errorMsg: String? = nil
    
    var body: some View {
        NavigationStack {
            ZStack {
                LSTheme.bg.ignoresSafeArea()
                
                VStack(spacing: 0) {
                    HStack {
                        Button(action: { dismiss() }) {
                            Image(systemName: "chevron.left")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(.white)
                                .frame(width: 36, height: 36)
                                .background(Color.white.opacity(0.15))
                                .clipShape(Circle())
                        }
                        
                        Spacer()
                        
                        Text("Bảng xếp hạng")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.white)
                        
                        Spacer()
                        
                        Spacer().frame(width: 36)
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
                    
                    // Period tabs
                    HStack(spacing: 8) {
                        tabButton(label: "Tuần này", period: "weekly")
                        tabButton(label: "Tháng này", period: "monthly")
                        tabButton(label: "Toàn thời gian", period: "alltime")
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    
                    if isLoading {
                        Spacer()
                        ProgressView()
                        Spacer()
                    } else if let error = errorMsg {
                        Spacer()
                        Text(error)
                            .foregroundColor(LSTheme.textSecondary)
                        Spacer()
                    } else {
                        // My rank banner
                        if let rank = myRank, rank.rank > 0 {
                            myRankBanner(rank: rank)
                                .padding(.horizontal, 16)
                                .padding(.bottom, 10)
                        }
                        
                        List {
                            ForEach(entries) { entry in
                                leaderboardRow(entry: entry)
                                    .listRowBackground(Color.clear)
                                    .listRowInsets(EdgeInsets(top: 4, leading: 16, bottom: 4, trailing: 16))
                                    .listRowSeparator(.hidden)
                            }
                        }
                        .listStyle(PlainListStyle())
                        .background(Color.clear)
                    }
                }
            }
            .onAppear(perform: loadData)
            .onChange(of: selectedPeriod) { _, _ in
                loadData()
            }
        }
    }
    
    private func tabButton(label: String, period: String) -> some View {
        let isSelected = selectedPeriod == period
        return Button(action: { selectedPeriod = period }) {
            Text(label)
                .font(.system(size: 12, weight: isSelected ? .bold : .semibold))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
                .background(isSelected ? LSTheme.primary : LSTheme.surfaceContainer)
                .foregroundColor(isSelected ? .white : LSTheme.textSecondary)
                .cornerRadius(20)
                .overlay(
                    RoundedRectangle(cornerRadius: 20)
                        .stroke(isSelected ? LSTheme.primary : LSTheme.outlineVariant.opacity(0.3), lineWidth: 1)
                )
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    private func loadData() {
        isLoading = true
        errorMsg = nil
        Task {
            do {
                self.entries = try await QuizService.shared.fetchLeaderboard(period: selectedPeriod)
                self.myRank = try? await QuizService.shared.getMyRank(period: selectedPeriod)
                self.isLoading = false
            } catch {
                self.errorMsg = ApiErrorUX.userMessage(
                    from: error,
                    fallback: "Không thể tải bảng xếp hạng lúc này. Vui lòng thử lại."
                )
                self.isLoading = false
            }
        }
    }
    
    private func myRankBanner(rank: MyRankResponse) -> some View {
        HStack {
            HStack(spacing: 8) {
                Image(systemName: "person.circle.fill")
                    .foregroundColor(LSTheme.primary)
                Text("Hạng của bạn: #\(rank.rank)")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(LSTheme.textPrimary)
            }
            Spacer()
            Text("\(score(for: rank)) điểm")
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(LSTheme.primary)
        }
        .padding(12)
        .background(LSTheme.primary.opacity(0.12))
        .cornerRadius(14)
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(LSTheme.primary.opacity(0.3), lineWidth: 1)
        )
    }
    
    private func leaderboardRow(entry: LeaderboardEntry) -> some View {
        let rank = entry.rank ?? 0
        let rankColor: Color
        if rank == 1 { rankColor = Color(hex: "D4A017") }
        else if rank == 2 { rankColor = Color(hex: "C0C0C0") }
        else if rank == 3 { rankColor = Color(hex: "CD7F32") }
        else { rankColor = LSTheme.textTertiary }
        
        return HStack(spacing: 12) {
            // Rank badge
            ZStack {
                Circle()
                    .fill(rankColor.opacity(0.15))
                    .frame(width: 36, height: 36)
                if rank <= 3 {
                    Image(systemName: "crown.fill")
                        .font(.system(size: 16))
                        .foregroundColor(rankColor)
                } else {
                    Text("#\(rank)")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(rankColor)
                }
            }
            
            // Avatar placeholder
            ZStack {
                Circle()
                    .fill(LSTheme.primary.opacity(0.12))
                    .frame(width: 40, height: 40)
                Text(entry.displayName.prefix(1).uppercased())
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(LSTheme.primary)
            }
            
            VStack(alignment: .leading, spacing: 2) {
                Text(entry.displayName)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(LSTheme.textPrimary)
                Text(scoreCaption(for: entry))
                    .font(.system(size: 11))
                    .foregroundColor(LSTheme.textSecondary)
            }
            
            Spacer()
            
            // Total score
            Text("\(score(for: entry))")
                .font(.system(size: 13, weight: .bold))
                .foregroundColor(rankColor)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(rankColor.opacity(0.12))
                .cornerRadius(8)
        }
        .padding(12)
        .background(LSTheme.surfaceContainer)
        .cornerRadius(14)
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(LSTheme.outlineVariant.opacity(0.3), lineWidth: 1)
        )
    }

    private func score(for rank: MyRankResponse) -> Int {
        switch selectedPeriod {
        case "monthly": rank.monthScore
        case "alltime": rank.totalScore
        default: rank.weekScore
        }
    }

    private func score(for entry: LeaderboardEntry) -> Int {
        switch selectedPeriod {
        case "monthly": entry.monthScore
        case "alltime": entry.totalScore
        default: entry.weekScore
        }
    }

    private func scoreCaption(for entry: LeaderboardEntry) -> String {
        switch selectedPeriod {
        case "monthly": "\(entry.monthScore) điểm tháng"
        case "alltime": "\(entry.totalScore) điểm tổng"
        default: "\(entry.weekScore) điểm tuần"
        }
    }
}
