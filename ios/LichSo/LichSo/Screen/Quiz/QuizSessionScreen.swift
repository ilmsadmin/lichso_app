import SwiftUI

struct QuizSessionScreen: View {
    let sessionType: String
    let category: String?
    
    @Environment(\.dismiss) private var dismiss
    
    @State private var isLoading = true
    @State private var errorMessage: String? = nil
    @State private var questions: [QuizQuestion] = []
    @State private var currentIndex = 0
    @State private var currentSessionId: String = ""
    @State private var score = 0
    @State private var timeRemaining = 30
    @State private var timer: Timer? = nil
    
    @State private var chosenOption: String? = nil
    @State private var isAnswered = false
    @State private var lastResult: SubmitAnswerResult? = nil
    @State private var showResultScreen = false
    @State private var finalResult: SessionResult? = nil
    
    @State private var dailyPoints = 100 // Local points for assist demo
    @State private var hintOpened = false
    @State private var fiftyFiftyUsed = false
    @State private var extraTimeUsed = false
    @State private var hiddenOptions: Set<String> = []
    
    var body: some View {
        NavigationStack {
            ZStack {
                LSTheme.bg.ignoresSafeArea()
                
                if isLoading {
                    VStack {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: LSTheme.primary))
                        Text("Đang tải câu hỏi...")
                            .foregroundColor(LSTheme.textSecondary)
                            .padding(.top, 8)
                    }
                } else if let error = errorMessage {
                    VStack(spacing: 16) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .font(.system(size: 48))
                            .foregroundColor(.orange)
                        Text(error)
                            .foregroundColor(LSTheme.textPrimary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 24)
                        Button("Thử lại") {
                            loadQuiz()
                        }
                        .padding(.horizontal, 24)
                        .padding(.vertical, 10)
                        .background(LSTheme.primary)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                    }
                } else if !questions.isEmpty && currentIndex < questions.count {
                    let currentQuestion = questions[currentIndex]
                    
                    VStack(spacing: 0) {
                        // Top bar
                        HStack {
                            Button(action: { dismiss() }) {
                                Image(systemName: "xmark")
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundColor(LSTheme.textPrimary)
                            }
                            
                            Spacer()
                            
                            Text("Đố Vui")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(LSTheme.textPrimary)
                            
                            Spacer()
                            
                            Spacer().frame(width: 24)
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)
                        
                        ScrollView {
                            VStack(spacing: 16) {
                                // Progress & Timer
                                HStack {
                                    VStack(alignment: .leading, spacing: 6) {
                                        Text("Câu hỏi \(currentIndex + 1) / \(questions.count)")
                                            .font(.system(size: 13, weight: .semibold))
                                            .foregroundColor(LSTheme.textSecondary)
                                        
                                        ProgressView(value: Double(currentIndex + 1), total: Double(questions.count))
                                            .tint(LSTheme.primary)
                                    }
                                    
                                    Spacer()
                                    
                                    // Timer Circle
                                    ZStack {
                                        Circle()
                                            .stroke(timerColor.opacity(0.2), lineWidth: 3)
                                            .frame(width: 48, height: 48)
                                        Circle()
                                            .trim(from: 0.0, to: CGFloat(timeRemaining) / 30.0)
                                            .stroke(timerColor, lineWidth: 3)
                                            .frame(width: 48, height: 48)
                                            .rotationEffect(Angle(degrees: -90))
                                        Text("\(timeRemaining)")
                                            .font(.system(size: 14, weight: .bold))
                                            .foregroundColor(timerColor)
                                    }
                                }
                                .padding(.horizontal, 16)
                                
                                // Assist Panel
                                assistPanel
                                
                                // Question card
                                VStack(alignment: .leading, spacing: 8) {
                                    Text(currentQuestion.category.uppercased())
                                        .font(.system(size: 11, weight: .bold))
                                        .foregroundColor(LSTheme.primary)
                                    Text(currentQuestion.content)
                                        .font(.system(size: 16, weight: .semibold))
                                        .foregroundColor(LSTheme.textPrimary)
                                        .lineSpacing(4)
                                }
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(18)
                                .background(LSTheme.surfaceContainer)
                                .cornerRadius(16)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 16)
                                        .stroke(LSTheme.outlineVariant.opacity(0.3), lineWidth: 1)
                                )
                                .padding(.horizontal, 16)
                                
                                if hintOpened, let hint = currentQuestion.hint {
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text("💡 Gợi ý:")
                                            .font(.system(size: 12, weight: .bold))
                                            .foregroundColor(LSTheme.gold)
                                        Text(hint)
                                            .font(.system(size: 12))
                                            .foregroundColor(LSTheme.textSecondary)
                                    }
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .padding(12)
                                    .background(LSTheme.surfaceContainerHigh)
                                    .cornerRadius(12)
                                    .padding(.horizontal, 16)
                                }
                                
                                // Options
                                VStack(spacing: 10) {
                                    optionButton(label: "A", text: currentQuestion.optionA)
                                    optionButton(label: "B", text: currentQuestion.optionB)
                                    optionButton(label: "C", text: currentQuestion.optionC)
                                    optionButton(label: "D", text: currentQuestion.optionD)
                                }
                                .padding(.horizontal, 16)
                                
                                if isAnswered, let result = lastResult {
                                    explanationCard(result: result, q: currentQuestion)
                                        .padding(.horizontal, 16)
                                }
                                
                                if isAnswered {
                                    Button(action: nextQuestion) {
                                        HStack {
                                            Text(currentIndex + 1 >= questions.count ? "Xem kết quả" : "Tiếp theo")
                                                .font(.system(size: 15, weight: .semibold))
                                            Image(systemName: "arrow.right")
                                        }
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 14)
                                        .background(LSTheme.primary)
                                        .foregroundColor(.white)
                                        .cornerRadius(12)
                                    }
                                    .padding(.horizontal, 16)
                                }
                            }
                            .padding(.vertical, 8)
                        }
                    }
                }
            }
            .onAppear(perform: loadQuiz)
            .onDisappear(perform: stopTimer)
            .fullScreenCover(isPresented: $showResultScreen) {
                QuizResultScreen(result: finalResult, total: questions.count, correct: score) {
                    dismiss()
                }
            }
        }
    }
    
    private var timerColor: Color {
        if timeRemaining > 15 { return .green }
        if timeRemaining > 7 { return .orange }
        return .red
    }
    
    private func startTimer() {
        stopTimer()
        timeRemaining = 30
        timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { _ in
            if timeRemaining > 0 {
                timeRemaining -= 1
            } else {
                // Time's up -> auto submit wrong answer
                stopTimer()
                submitAnswer(option: "")
            }
        }
    }
    
    private func stopTimer() {
        timer?.invalidate()
        timer = nil
    }
    
    private func loadQuiz() {
        isLoading = true
        errorMessage = nil
        Task {
            do {
                let (session, list) = try await QuizService.shared.startSession(sessionType: sessionType, category: category)
                self.questions = list
                self.currentSessionId = session.id
                self.isLoading = false
                startTimer()
            } catch {
                self.errorMessage = error.localizedDescription
                self.isLoading = false
            }
        }
    }
    
    private func submitAnswer(option: String) {
        stopTimer()
        chosenOption = option
        isAnswered = true
        
        let correctOption = (questions[currentIndex].correct ?? "a").uppercased()
        let isCorrect = option.uppercased() == correctOption
        if isCorrect {
            score += 1
        }
        
        // Build mock / temp SubmitAnswerResult
        let explanation = questions[currentIndex].explanation ?? "Đáp án đúng là \(correctOption)"
        lastResult = SubmitAnswerResult(
            questionId: questions[currentIndex].id,
            chosen: option,
            correct: correctOption,
            isCorrect: isCorrect,
            explanation: explanation,
            articleId: nil,
            pointsEarned: isCorrect ? 10 : 0,
            timeMs: (30 - timeRemaining) * 1000
        )
        
        // Send answer to server if token exists
        if !currentSessionId.isEmpty {
            Task {
                _ = try? await QuizService.shared.submitAnswer(
                    sessionId: currentSessionId,
                    questionId: questions[currentIndex].id,
                    chosen: option,
                    timeMs: (30 - timeRemaining) * 1000
                )
            }
        }
    }
    
    private func nextQuestion() {
        if currentIndex + 1 < questions.count {
            currentIndex += 1
            chosenOption = nil
            isAnswered = false
            lastResult = nil
            hintOpened = false
            hiddenOptions.removeAll()
            startTimer()
        } else {
            // Quiz finished
            finishQuiz()
        }
    }
    
    private func finishQuiz() {
        isLoading = true
        if !currentSessionId.isEmpty {
            Task {
                do {
                    let res = try await QuizService.shared.finishSession(sessionId: currentSessionId)
                    self.finalResult = res
                    self.isLoading = false
                    self.showResultScreen = true
                } catch {
                    self.isLoading = false
                    self.showResultScreen = true
                }
            }
        } else {
            // Guest completion
            self.finalResult = SessionResult(
                sessionId: "",
                score: score,
                scoreV2: score * 10,
                total: questions.count,
                pointsEarned: score * 10,
                bonusPoints: 0,
                newWeekScore: score * 10,
                rank: 0,
                appPointsEarned: score * 10,
                xpEarned: score * 10,
                sessionTitle: "Đố Vui Khách",
                unlockedMasteryTitles: [],
                unlockedBadges: []
            )
            self.isLoading = false
            self.showResultScreen = true
        }
    }
    
    private var assistPanel: some View {
        VStack(spacing: 8) {
            HStack {
                HStack(spacing: 6) {
                    Image(systemName: "sparkles")
                        .foregroundColor(LSTheme.gold)
                    Text("Trợ giúp")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(LSTheme.textPrimary)
                }
                Spacer()
                Text("\(dailyPoints) điểm")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(LSTheme.gold)
            }
            
            HStack(spacing: 8) {
                // 50:50
                Button(action: useFiftyFifty) {
                    VStack {
                        Image(systemName: "eye.slash.fill")
                        Text("50 / 50")
                            .font(.system(size: 10, weight: .semibold))
                        Text("20đ")
                            .font(.system(size: 9))
                            .foregroundColor(LSTheme.textTertiary)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                    .background(fiftyFiftyUsed ? Color.gray.opacity(0.2) : LSTheme.primary.opacity(0.1))
                    .foregroundColor(fiftyFiftyUsed ? .gray : LSTheme.primary)
                    .cornerRadius(10)
                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(fiftyFiftyUsed ? Color.clear : LSTheme.primary.opacity(0.3)))
                }
                .disabled(fiftyFiftyUsed || isAnswered)
                
                // Gợi ý
                Button(action: useHint) {
                    VStack {
                        Image(systemName: "lightbulb.fill")
                        Text("Gợi ý")
                            .font(.system(size: 10, weight: .semibold))
                        Text("15đ")
                            .font(.system(size: 9))
                            .foregroundColor(LSTheme.textTertiary)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                    .background(hintOpened ? Color.gray.opacity(0.2) : LSTheme.primary.opacity(0.1))
                    .foregroundColor(hintOpened ? .gray : LSTheme.primary)
                    .cornerRadius(10)
                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(hintOpened ? Color.clear : LSTheme.primary.opacity(0.3)))
                }
                .disabled(hintOpened || isAnswered)
                
                // Thêm giờ
                Button(action: useExtraTime) {
                    VStack {
                        Image(systemName: "timer")
                        Text("Thêm giờ")
                            .font(.system(size: 10, weight: .semibold))
                        Text("10đ")
                            .font(.system(size: 9))
                            .foregroundColor(LSTheme.textTertiary)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                    .background(extraTimeUsed ? Color.gray.opacity(0.2) : LSTheme.primary.opacity(0.1))
                    .foregroundColor(extraTimeUsed ? .gray : LSTheme.primary)
                    .cornerRadius(10)
                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(extraTimeUsed ? Color.clear : LSTheme.primary.opacity(0.3)))
                }
                .disabled(extraTimeUsed || isAnswered)
            }
        }
        .padding(12)
        .background(LSTheme.surfaceContainer)
        .cornerRadius(14)
        .padding(.horizontal, 16)
    }
    
    private func useFiftyFifty() {
        guard dailyPoints >= 20 else { return }
        dailyPoints -= 20
        fiftyFiftyUsed = true
        
        let correctOption = questions[currentIndex].correctAnswer ?? "A"
        let wrongOptions = ["A", "B", "C", "D"].filter { $0.uppercased() != correctOption.uppercased() }
        let shuffledWrong = wrongOptions.shuffled()
        hiddenOptions.insert(shuffledWrong[0])
        hiddenOptions.insert(shuffledWrong[1])
    }
    
    private func useHint() {
        guard dailyPoints >= 15 else { return }
        dailyPoints -= 15
        hintOpened = true
    }
    
    private func useExtraTime() {
        guard dailyPoints >= 10 else { return }
        dailyPoints -= 10
        extraTimeUsed = true
        timeRemaining = min(timeRemaining + 15, 30)
    }
    
    private func optionButton(label: String, text: String) -> some View {
        let isHidden = hiddenOptions.contains(label)
        
        let isSelected = chosenOption == label
        let correctOption = (questions[currentIndex].correct ?? "a").uppercased()
        let isCorrect = correctOption == label.uppercased()
        
        var bgColor = LSTheme.surfaceContainer
        var strokeColor = LSTheme.outlineVariant.opacity(0.4)
        var textColor = LSTheme.textPrimary
        
        if isAnswered {
            if isCorrect {
                bgColor = Color.green.opacity(0.15)
                strokeColor = .green
                textColor = .green
            } else if isSelected {
                bgColor = Color.red.opacity(0.15)
                strokeColor = .red
                textColor = .red
            }
        } else if isSelected {
            bgColor = LSTheme.primary.opacity(0.15)
            strokeColor = LSTheme.primary
        }
        
        return Button(action: {
            if !isAnswered && !isHidden {
                submitAnswer(option: label)
            }
        }) {
            HStack(spacing: 12) {
                ZStack {
                    Circle()
                        .fill(strokeColor.opacity(0.15))
                        .frame(width: 32, height: 32)
                    Text(label)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(strokeColor)
                }
                
                Text(isHidden ? "Đã loại bởi 50/50" : text)
                    .font(.system(size: 14))
                    .foregroundColor(isHidden ? LSTheme.textTertiary : textColor)
                    .multilineTextAlignment(.leading)
                
                Spacer()
                
                if isAnswered {
                    if isCorrect {
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundColor(.green)
                    } else if isSelected {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.red)
                    }
                }
            }
            .padding(14)
            .background(bgColor)
            .cornerRadius(14)
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(strokeColor, lineWidth: 1.5)
            )
        }
        .disabled(isAnswered || isHidden)
        .opacity(isHidden ? 0.5 : 1.0)
    }
    
    private func explanationCard(result: SubmitAnswerResult, q: QuizQuestion) -> some View {
        let accentColor = result.isCorrect ? Color.green : Color.red
        return VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 6) {
                Image(systemName: result.isCorrect ? "checkmark.circle.fill" : "xmark.circle.fill")
                    .foregroundColor(accentColor)
                Text(result.isCorrect ? "Chính xác! (+10đ)" : "Chưa chính xác (Đáp án: \(result.correct))")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(accentColor)
            }
            
            if let explanation = result.explanation {
                Text(explanation)
                    .font(.system(size: 13))
                    .foregroundColor(LSTheme.textPrimary)
                    .lineSpacing(4)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(accentColor.opacity(0.08))
        .cornerRadius(14)
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(accentColor.opacity(0.3), lineWidth: 1)
        )
    }
}
