import SwiftUI

private let quizQuestionTimeSeconds = 30

private enum QuizAssistType: String, CaseIterable, Hashable {
    case fiftyFifty
    case hint
    case extraTime

    var cost: Int {
        switch self {
        case .fiftyFifty: 12
        case .hint: 8
        case .extraTime: 10
        }
    }

    var label: String {
        switch self {
        case .fiftyFifty: "50/50"
        case .hint: "Gợi ý"
        case .extraTime: "+15 giây"
        }
    }

    var icon: String {
        switch self {
        case .fiftyFifty: "line.3.horizontal.decrease"
        case .hint: "lightbulb.fill"
        case .extraTime: "timer"
        }
    }

    var spendSource: String {
        switch self {
        case .fiftyFifty: "quiz_assist_fifty_fifty"
        case .hint: "quiz_assist_hint"
        case .extraTime: "quiz_assist_extra_time"
        }
    }
}

private struct QuizQuestionState {
    var questions: [QuizQuestion]
    var currentIndex: Int
    var sessionId: String?
    var answers: [SubmitAnswerResult?]
    var timeRemaining: Int
    var lastResult: SubmitAnswerResult?
    var showingResult: Bool
    var hiddenOptions: [Int64: Set<String>]
    var hints: [Int64: String]
    var usedAssists: [Int64: Set<QuizAssistType>]
    var dailyPoints: Int
    var assistMessage: String?
    var practiceMode: Bool
    var modeNotice: String?
    var reviewMode: Bool
    var reviewCorrectAnswers: [Int64: String]
    var reviewExplanations: [Int64: String]
}

private enum QuizPlayState {
    case idle
    case loading
    case question(QuizQuestionState)
    case finished(SessionResult?, [SubmitAnswerResult?], [QuizQuestion])
    case error(String)
}

@MainActor
private final class QuizPlayViewModel: ObservableObject {
    @Published var state: QuizPlayState = .idle

    private var sessionType = "daily"
    private var category: String?
    private var startedAtMs = Int64(Date().timeIntervalSince1970 * 1000)
    private var timerTask: Task<Void, Never>?
    private var autoAdvanceTask: Task<Void, Never>?
    private var submittingQuestionIDs = Set<Int64>()
    private let replayTrackPrefix = "ranked_replay_"
    private let topicReplayCooldownSeconds: TimeInterval = 24 * 60 * 60

    deinit {
        timerTask?.cancel()
        autoAdvanceTask?.cancel()
        submittingQuestionIDs.removeAll()
    }

    func load(sessionType: String, category: String?) {
        self.sessionType = sessionType
        self.category = category
        self.startedAtMs = Int64(Date().timeIntervalSince1970 * 1000)
        state = .loading
        timerTask?.cancel()
        autoAdvanceTask?.cancel()

        Task {
            do {
                let (session, loadedQuestions) = try await QuizService.shared.startSession(sessionType: sessionType, category: category)
                let rawQuestions = sessionType == "daily" ? Array(loadedQuestions.prefix(15)) : loadedQuestions
                let isAuthenticatedSession = !(session.id.isEmpty)
                if isAuthenticatedSession && isReplayBlocked(sessionType: sessionType, category: category, questionIDs: rawQuestions.map(\.id)) {
                    try await loadPracticeReplay(sessionType: sessionType, category: category)
                    return
                }
                let questions = await enrichQuestionsForLocalFallback(rawQuestions, sessionType: sessionType, category: category)
                guard !questions.isEmpty else {
                    state = .error("Chưa có câu hỏi cho mục này.")
                    return
                }
                let points = (try? await QuizService.shared.fetchPointWallet().balance) ?? 0
                state = .question(
                    QuizQuestionState(
                        questions: questions,
                        currentIndex: 0,
                        sessionId: session.id.isEmpty ? nil : session.id,
                        answers: Array(repeating: nil, count: questions.count),
                        timeRemaining: quizQuestionTimeSeconds,
                        lastResult: nil,
                        showingResult: false,
                        hiddenOptions: [:],
                        hints: [:],
                        usedAssists: [:],
                        dailyPoints: points,
                        assistMessage: nil,
                        practiceMode: false,
                        modeNotice: nil,
                        reviewMode: false,
                        reviewCorrectAnswers: [:],
                        reviewExplanations: [:]
                    )
                )
                startTimer()
            } catch {
                debugLog("QuizSession start error: \(error)")
                state = .error("Không thể tải câu hỏi. Lỗi: \(error.localizedDescription)")
            }
        }
    }

    func loadWrongQuestionsSession(
        questions: [QuizQuestion],
        correctAnswers: [Int64: String],
        explanations: [Int64: String]
    ) {
        guard !questions.isEmpty else { return }
        sessionType = "review"
        category = nil
        startedAtMs = Int64(Date().timeIntervalSince1970 * 1000)
        timerTask?.cancel()
        autoAdvanceTask?.cancel()
        state = .question(
            QuizQuestionState(
                questions: questions,
                currentIndex: 0,
                sessionId: nil,
                answers: Array(repeating: nil, count: questions.count),
                timeRemaining: quizQuestionTimeSeconds,
                lastResult: nil,
                showingResult: false,
                hiddenOptions: [:],
                hints: [:],
                usedAssists: [:],
                dailyPoints: 0,
                assistMessage: nil,
                practiceMode: true,
                modeNotice: "Chế độ ôn lại - không tính bảng xếp hạng.",
                reviewMode: true,
                reviewCorrectAnswers: correctAnswers,
                reviewExplanations: explanations
            )
        )
        startTimer()
    }

    func submitAnswer(_ chosen: String) {
        guard case .question(var current) = state,
              let question = current.questions[safe: current.currentIndex],
              !current.showingResult,
              !submittingQuestionIDs.contains(question.id),
              isQuestionUnanswered(current)
        else { return }

        timerTask?.cancel()
        submittingQuestionIDs.insert(question.id)
        let selected = normalizeChoice(chosen)
        let timeMs = (quizQuestionTimeSeconds - current.timeRemaining).clamped(to: 0...quizQuestionTimeSeconds) * 1000

        if let sessionId = current.sessionId {
            current.showingResult = true
            current.assistMessage = nil
            state = .question(current)

            Task {
                do {
                    let result = try await QuizService.shared.submitAnswer(
                        sessionId: sessionId,
                        questionId: question.id,
                        chosen: selected,
                        timeMs: timeMs
                    )
                    submittingQuestionIDs.remove(question.id)
                    updateWithAnswer(result)
                    scheduleAutoAdvance(from: current.currentIndex)
                } catch {
                    submittingQuestionIDs.remove(question.id)
                    if isAlreadyAnsweredError(error) {
                        updateQuestionState { state in
                            state.assistMessage = "Câu này đã được máy chủ ghi nhận, chuyển sang câu tiếp theo."
                            state.showingResult = false
                        }
                        nextQuestion()
                        return
                    }

                    updateQuestionState { state in
                        state.showingResult = false
                        state.assistMessage = ApiErrorUX.userMessage(
                            from: error,
                            fallback: "Không thể gửi đáp án lúc này. Vui lòng chọn lại."
                        )
                    }
                    startTimer()
                }
            }
        } else {
            let result = buildLocalResult(question: question, chosen: selected, timeMs: timeMs, state: current)
            submittingQuestionIDs.remove(question.id)
            updateWithAnswer(result)
            scheduleAutoAdvance(from: current.currentIndex)
        }
    }

    func useAssist(_ type: QuizAssistType) {
        guard case .question(let current) = state,
              let question = current.questions[safe: current.currentIndex],
              !current.showingResult
        else { return }

        if current.usedAssists[question.id, default: []].contains(type) {
            setAssistMessage("\(type.label) đã dùng cho câu này")
            return
        }
        if type == .hint, question.hint?.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty != false {
            setAssistMessage("Câu này chưa có gợi ý từ người soạn")
            return
        }
        let correct = correctOptionKey(question, override: current.reviewCorrectAnswers[question.id])
        if type == .fiftyFifty, correct.isEmpty, current.sessionId == nil {
            setAssistMessage("Câu này chưa đủ dữ liệu để dùng trợ giúp")
            return
        }

        if current.sessionId != nil, hasBackendToken {
            Task {
                do {
                    let wallet = try await QuizService.shared.spendPoints(
                        amount: type.cost,
                        source: type.spendSource,
                        sourceId: current.sessionId,
                        idempotencyKey: "quiz_assist_\(type.rawValue)_\(question.id)_\(current.sessionId ?? "guest")",
                        metadata: [
                            "session_id": current.sessionId ?? "",
                            "question_id": question.id
                        ]
                    )
                    applyAssist(type, question: question, correct: correct, newBalance: wallet.balance)
                } catch {
                    setAssistMessage(
                        ApiErrorUX.userMessage(
                            from: error,
                            fallback: "Không thể thực hiện giao dịch điểm lúc này. Vui lòng thử lại."
                        )
                    )
                }
            }
        } else {
            if current.dailyPoints < type.cost {
                setAssistMessage("Cần thêm \(type.cost - current.dailyPoints) điểm ngày để dùng \(type.label)")
                return
            }
            applyAssist(type, question: question, correct: correct, newBalance: current.dailyPoints - type.cost)
        }
    }

    func nextQuestion() {
        autoAdvanceTask?.cancel()
        submittingQuestionIDs.removeAll()
        guard case .question(var current) = state else { return }
        let nextIndex = current.currentIndex + 1
        if nextIndex >= current.questions.count {
            finishQuiz()
            return
        }
        current.currentIndex = nextIndex
        current.timeRemaining = quizQuestionTimeSeconds
        current.lastResult = nil
        current.showingResult = false
        current.assistMessage = nil
        state = .question(current)
        startTimer()
    }

    func retry() {
        load(sessionType: sessionType, category: category)
    }

    func stop() {
        timerTask?.cancel()
        autoAdvanceTask?.cancel()
        submittingQuestionIDs.removeAll()
    }

    private var hasBackendToken: Bool {
        !(TokenStore.accessToken ?? "").isEmpty
    }

    private func startTimer() {
        timerTask?.cancel()
        timerTask = Task { [weak self] in
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 1_000_000_000)
                guard !Task.isCancelled else { return }
                await MainActor.run {
                    self?.tick()
                }
            }
        }
    }

    private func tick() {
        guard case .question(var current) = state, !current.showingResult else { return }
        if current.timeRemaining <= 0 {
            submitAnswer("")
            return
        }
        current.timeRemaining -= 1
        state = .question(current)
    }

    private func updateWithAnswer(_ result: SubmitAnswerResult) {
        if result.isCorrect { Haptics.success() } else { Haptics.error() }
        updateQuestionState { current in
            if current.answers.indices.contains(current.currentIndex) {
                current.answers[current.currentIndex] = result
            }
            current.lastResult = result
            current.showingResult = true
            current.assistMessage = nil
        }
    }

    private func finishQuiz() {
        timerTask?.cancel()
        autoAdvanceTask?.cancel()
        guard case .question(let current) = state else { return }

        if let sessionId = current.sessionId {
            if current.practiceMode {
                state = .finished(makeLocalSessionResult(from: current), current.answers, current.questions)
                return
            }
            Task {
                do {
                    let result = try await QuizService.shared.finishSession(sessionId: sessionId)
                    markReplayCredited(sessionType: sessionType, category: category, questionIDs: current.questions.map(\.id))
                    state = .finished(result, current.answers, current.questions)
                } catch {
                    state = .finished(makeLocalSessionResult(from: current), current.answers, current.questions)
                }
            }
        } else {
            state = .finished(makeLocalSessionResult(from: current), current.answers, current.questions)
        }
    }

    private func makeLocalSessionResult(from state: QuizQuestionState) -> SessionResult {
        let correctCount = state.answers.compactMap { $0 }.filter(\.isCorrect).count
        let earned = state.answers.compactMap { $0 }.reduce(0) { $0 + $1.pointsEarned }
        return SessionResult(
            sessionId: "local-\(startedAtMs)",
            score: correctCount,
            scoreV2: correctCount * 10,
            total: state.questions.count,
            pointsEarned: earned,
            bonusPoints: 0,
            newWeekScore: 0,
            rank: 0,
            appPointsEarned: earned,
            xpEarned: correctCount * 10,
            sessionTitle: state.reviewMode ? "Ôn lại câu sai" : "Đố vui",
            unlockedMasteryTitles: [],
            unlockedBadges: []
        )
    }

    private func scheduleAutoAdvance(from index: Int) {
        autoAdvanceTask?.cancel()
        autoAdvanceTask = Task { [weak self] in
            try? await Task.sleep(nanoseconds: 3_500_000_000)
            await MainActor.run {
                guard case .question(let latest) = self?.state,
                      latest.currentIndex == index,
                      latest.showingResult
                else { return }
                self?.nextQuestion()
            }
        }
    }

    private func buildLocalResult(
        question: QuizQuestion,
        chosen: String,
        timeMs: Int,
        state: QuizQuestionState
    ) -> SubmitAnswerResult {
        let correct = correctOptionKey(question, override: state.reviewCorrectAnswers[question.id])
        let explanation = state.reviewExplanations[question.id] ?? question.explanation
        let isCorrect = !correct.isEmpty && chosen == correct
        return SubmitAnswerResult(
            questionId: question.id,
            chosen: chosen,
            correct: correct,
            isCorrect: isCorrect,
            explanation: explanation,
            articleId: question.articleId,
            pointsEarned: isCorrect ? 1 : 0,
            timeMs: timeMs
        )
    }

    private func fallbackLocalResult(
        question: QuizQuestion,
        chosen: String,
        timeMs: Int,
        state: QuizQuestionState
    ) -> SubmitAnswerResult? {
        let result = buildLocalResult(question: question, chosen: chosen, timeMs: timeMs, state: state)
        return result.correct.isEmpty ? nil : result
    }

    private func enrichQuestionsForLocalFallback(
        _ questions: [QuizQuestion],
        sessionType: String,
        category: String?
    ) async -> [QuizQuestion] {
        let publicQuestions: [QuizQuestion]?
        if sessionType == "daily" {
            let formatter = DateFormatter()
            formatter.dateFormat = "yyyy-MM-dd"
            publicQuestions = try? await QuizService.shared.fetchDailyQuestions(date: formatter.string(from: Date()))
        } else {
            publicQuestions = try? await QuizService.shared.fetchQuestions(category: category, limit: max(questions.count, 10))
        }

        guard let publicQuestions else { return questions }
        let byId = Dictionary(uniqueKeysWithValues: publicQuestions.map { ($0.id, $0) })
        let byContent = publicQuestions.reduce(into: [String: QuizQuestion]()) { result, question in
            result[normalizeQuestionText(question.content)] = question
        }
        return questions.map { question in
            guard let publicQuestion = byId[question.id] ?? byContent[normalizeQuestionText(question.content)] else {
                return question
            }
            return mergeQuestion(question, with: publicQuestion)
        }
    }

    private func recoverAndGradeLocally(
        question: QuizQuestion,
        chosen: String,
        timeMs: Int,
        expectedIndex: Int
    ) async -> Bool {
        guard let resolvedQuestion = await resolveQuestionForLocalFallback(question) else {
            return false
        }
        guard case .question(var latest) = state,
              latest.currentIndex == expectedIndex,
              latest.questions[safe: expectedIndex]?.id == question.id
        else { return false }

        latest.questions[expectedIndex] = resolvedQuestion
        latest.showingResult = false
        latest.assistMessage = nil
        state = .question(latest)

        guard let result = fallbackLocalResult(
            question: resolvedQuestion,
            chosen: chosen,
            timeMs: timeMs,
            state: latest
        ) else { return false }

        updateWithAnswer(result)
        scheduleAutoAdvance(from: expectedIndex)
        return true
    }

    private func resolveQuestionForLocalFallback(_ question: QuizQuestion) async -> QuizQuestion? {
        let publicQuestions: [QuizQuestion]?
        if sessionType == "daily" {
            let formatter = DateFormatter()
            formatter.dateFormat = "yyyy-MM-dd"
            publicQuestions = try? await QuizService.shared.fetchDailyQuestions(date: formatter.string(from: Date()))
        } else {
            publicQuestions = try? await QuizService.shared.fetchQuestions(category: category, limit: 60)
        }

        guard let publicQuestions else { return nil }
        let byId = Dictionary(uniqueKeysWithValues: publicQuestions.map { ($0.id, $0) })
        let byContent = publicQuestions.reduce(into: [String: QuizQuestion]()) { result, item in
            result[normalizeQuestionText(item.content)] = item
        }
        guard let publicQuestion = byId[question.id] ?? byContent[normalizeQuestionText(question.content)] else {
            return nil
        }
        let merged = mergeQuestion(question, with: publicQuestion)
        return correctOptionKey(merged, override: nil).isEmpty ? nil : merged
    }

    private func mergeQuestion(_ question: QuizQuestion, with publicQuestion: QuizQuestion) -> QuizQuestion {
        QuizQuestion(
            id: question.id,
            content: question.content,
            optionA: question.optionA,
            optionB: question.optionB,
            optionC: question.optionC,
            optionD: question.optionD,
            category: question.category,
            difficulty: question.difficulty,
            articleId: question.articleId ?? publicQuestion.articleId,
            correct: nonEmpty(question.correct) ?? nonEmpty(publicQuestion.correct),
            correctAnswer: nonEmpty(question.correctAnswer) ?? nonEmpty(publicQuestion.correctAnswer),
            hint: nonEmpty(question.hint) ?? nonEmpty(publicQuestion.hint),
            explanation: nonEmpty(question.explanation) ?? nonEmpty(publicQuestion.explanation)
        )
    }

    private func normalizeQuestionText(_ value: String) -> String {
        value.trimmingCharacters(in: .whitespacesAndNewlines)
            .replacingOccurrences(of: "\\s+", with: " ", options: .regularExpression)
            .lowercased()
    }

    private func nonEmpty(_ value: String?) -> String? {
        guard let value = value?.trimmingCharacters(in: .whitespacesAndNewlines), !value.isEmpty else {
            return nil
        }
        return value
    }

    private func applyAssist(_ type: QuizAssistType, question: QuizQuestion, correct: String, newBalance: Int) {
        updateQuestionState { current in
            current.usedAssists[question.id, default: []].insert(type)
            current.dailyPoints = max(0, newBalance)
            switch type {
            case .fiftyFifty:
                current.hiddenOptions[question.id] = buildFiftyFiftyHiddenOptions(question: question, correct: correct)
                current.assistMessage = "Đã loại 2 đáp án sai"
            case .hint:
                current.hints[question.id] = question.hint?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
                current.assistMessage = "Đã mở gợi ý"
            case .extraTime:
                current.timeRemaining = min(current.timeRemaining + 15, quizQuestionTimeSeconds + 15)
                current.assistMessage = "Đã cộng thêm 15 giây"
            }
        }
    }

    private func setAssistMessage(_ message: String) {
        updateQuestionState { $0.assistMessage = message }
    }

    private func isQuestionUnanswered(_ state: QuizQuestionState) -> Bool {
        guard state.answers.indices.contains(state.currentIndex) else { return true }
        return state.answers[state.currentIndex] == nil
    }

    private func isAlreadyAnsweredError(_ error: Error) -> Bool {
        error.localizedDescription.localizedCaseInsensitiveContains("already answered")
    }

    private func friendlyNetworkMessage(_ error: Error) -> String {
        if error.localizedDescription.localizedCaseInsensitiveContains("network") {
            return "Kết nối mạng không ổn định. Vui lòng chọn lại."
        }
        return "Kết nối điểm số chưa ổn định, app vẫn tiếp tục chấm trên thiết bị."
    }

    private func updateQuestionState(_ transform: (inout QuizQuestionState) -> Void) {
        guard case .question(var current) = state else { return }
        transform(&current)
        state = .question(current)
    }

    private func loadPracticeReplay(sessionType: String, category: String?) async throws {
        let (_, loadedQuestions) = try await QuizService.shared.startSession(
            sessionType: sessionType,
            category: category,
            forceGuest: true
        )
        let rawQuestions = sessionType == "daily" ? Array(loadedQuestions.prefix(15)) : loadedQuestions
        let questions = await enrichQuestionsForLocalFallback(rawQuestions, sessionType: sessionType, category: category)
        let points = (try? await QuizService.shared.fetchPointWallet().balance) ?? 0
        state = .question(
            QuizQuestionState(
                questions: questions,
                currentIndex: 0,
                sessionId: nil,
                answers: Array(repeating: nil, count: questions.count),
                timeRemaining: quizQuestionTimeSeconds,
                lastResult: nil,
                showingResult: false,
                hiddenOptions: [:],
                hints: [:],
                usedAssists: [:],
                dailyPoints: points,
                assistMessage: nil,
                practiceMode: true,
                modeNotice: "Bạn đang chơi lại cùng bộ câu hỏi. Lượt này chỉ để luyện tập, không tính điểm BXH.",
                reviewMode: false,
                reviewCorrectAnswers: [:],
                reviewExplanations: [:]
            )
        )
        startTimer()
    }

    private func isReplayBlocked(sessionType: String, category: String?, questionIDs: [Int64]) -> Bool {
        guard !questionIDs.isEmpty else { return false }
        let key = replayKey(sessionType: sessionType, category: category, questionIDs: questionIDs)
        let lastCreditedAt = UserDefaults.standard.double(forKey: key)
        guard lastCreditedAt > 0 else { return false }

        let lastDate = Date(timeIntervalSince1970: lastCreditedAt)
        if sessionType == "daily" {
            return Calendar.current.isDate(lastDate, inSameDayAs: Date())
        }
        return Date().timeIntervalSince(lastDate) < topicReplayCooldownSeconds
    }

    private func markReplayCredited(sessionType: String, category: String?, questionIDs: [Int64]) {
        guard !questionIDs.isEmpty else { return }
        let key = replayKey(sessionType: sessionType, category: category, questionIDs: questionIDs)
        UserDefaults.standard.set(Date().timeIntervalSince1970, forKey: key)
    }

    private func replayKey(sessionType: String, category: String?, questionIDs: [Int64]) -> String {
        let normalized = Array(Set(questionIDs)).sorted().map(String.init).joined(separator: "-")
        return "\(replayTrackPrefix)\(sessionType):\(category ?? ""):\(normalized)"
    }
}

struct QuizSessionScreen: View {
    let sessionType: String
    let category: String?
    var onClose: (() -> Void)? = nil

    @Environment(\.dismiss) private var dismiss
    @StateObject private var viewModel = QuizPlayViewModel()

    var body: some View {
        ZStack {
            LSTheme.bg.ignoresSafeArea()
            switch viewModel.state {
            case .idle, .loading:
                loadingView
            case .error(let message):
                errorView(message)
            case .question(let state):
                quizContent(state)
            case .finished(let result, let answers, let questions):
                QuizResultScreen(
                    result: result,
                    answers: answers,
                    questions: questions,
                    onReviewWrong: { wrongQuestions, correctAnswers, explanations in
                        viewModel.loadWrongQuestionsSession(
                            questions: wrongQuestions,
                            correctAnswers: correctAnswers,
                            explanations: explanations
                        )
                    },
                    onClose: {
                        closeScreen()
                    },
                    onPlayAgain: {
                        viewModel.load(sessionType: sessionType, category: category)
                    }
                )
            }
        }
        .task {
            if case .idle = viewModel.state {
                viewModel.load(sessionType: sessionType, category: category)
            }
        }
        .onDisappear {
            viewModel.stop()
        }
    }

    private var loadingView: some View {
        VStack(spacing: 16) {
            ProgressView()
                .tint(LSTheme.primary)
            Text("Đang tải câu hỏi...")
                .font(.headline)
                .foregroundStyle(LSTheme.textSecondary)
        }
    }

    private func errorView(_ message: String) -> some View {
        VStack(spacing: 18) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 58, weight: .bold))
                .foregroundStyle(Color(hex: "F57F17"))
            Text(message)
                .font(.headline)
                .multilineTextAlignment(.center)
                .foregroundStyle(LSTheme.textPrimary)
            Button("Thử lại") {
                viewModel.retry()
            }
            .font(.headline)
            .foregroundStyle(.white)
            .padding(.horizontal, 28)
            .padding(.vertical, 14)
            .background(LSTheme.primary, in: RoundedRectangle(cornerRadius: 16))
        }
        .padding(28)
    }

    private func quizContent(_ state: QuizQuestionState) -> some View {
        GeometryReader { proxy in
            let contentWidth = max(0, proxy.size.width - 40)
            VStack(spacing: 0) {
                QuizTopHeader(title: state.reviewMode ? "Ôn Lại" : "Đố Vui") {
                    closeScreen()
                }
                .frame(width: proxy.size.width)

                ScrollView {
                    VStack(spacing: 10) {
                        progressRow(state)
                        if let modeNotice = state.modeNotice {
                            Text(modeNotice)
                                .font(.footnote.weight(.semibold))
                                .foregroundStyle(LSTheme.textSecondary)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 10)
                                .background(
                                    RoundedRectangle(cornerRadius: 12)
                                        .fill(LSTheme.surfaceContainer)
                                )
                                .overlay(
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(LSTheme.border, lineWidth: 1)
                                )
                        }
                        AssistPanel(state: state) { type in
                            viewModel.useAssist(type)
                        }
                        if let message = state.assistMessage {
                            Text(message)
                                .font(.subheadline.weight(.semibold))
                                .foregroundStyle(LSTheme.primary)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(.horizontal, 4)
                        }
                        if let question = state.questions[safe: state.currentIndex] {
                            QuestionCard(question: question)
                            HintCard(text: state.hints[question.id])
                            answerList(state: state, question: question)
                            if let result = state.lastResult, state.showingResult {
                                ExplanationCard(question: question, result: result, reviewMode: state.reviewMode)
                                Button {
                                    viewModel.nextQuestion()
                                } label: {
                                    Text(state.currentIndex + 1 >= state.questions.count ? "Xem kết quả" : "Câu tiếp theo")
                                        .font(.headline)
                                        .foregroundStyle(.white)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 14)
                                        .background(LSTheme.primary, in: RoundedRectangle(cornerRadius: 14))
                                }
                            }
                        }
                    }
                    .frame(width: contentWidth)
                    .padding(.vertical, 12)
                }
            }
        }
    }

    private func progressRow(_ state: QuizQuestionState) -> some View {
        HStack(alignment: .center, spacing: 12) {
            VStack(alignment: .leading, spacing: 10) {
                Text("\(state.currentIndex + 1) / \(state.questions.count)")
                    .font(.system(size: 16, weight: .bold, design: .rounded))
                    .foregroundStyle(LSTheme.textPrimary)
                ProgressDots(total: state.questions.count, current: state.currentIndex, answers: state.answers)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            Spacer()
            TimerCircle(seconds: state.timeRemaining)
                .padding(.trailing, 2)
        }
        .frame(maxWidth: .infinity)
    }

    private func answerList(state: QuizQuestionState, question: QuizQuestion) -> some View {
        let options = [
            ("a", question.optionA),
            ("b", question.optionB),
            ("c", question.optionC),
            ("d", question.optionD)
        ]
        let hidden = state.hiddenOptions[question.id, default: []]
        let revealAnswer = state.reviewMode || state.lastResult?.isCorrect == true
        let correct = revealAnswer ? normalizeChoice(state.lastResult?.correct) : ""

        return VStack(spacing: 10) {
            ForEach(options, id: \.0) { key, text in
                if !hidden.contains(key) {
                    AnswerOptionButton(
                        key: key,
                        text: text,
                        selected: normalizeChoice(state.lastResult?.chosen) == key,
                        correct: correct == key,
                        wrong: state.showingResult && normalizeChoice(state.lastResult?.chosen) == key && state.lastResult?.isCorrect == false,
                        disabled: state.showingResult
                    ) {
                        viewModel.submitAnswer(key)
                    }
                }
            }
        }
        .frame(maxWidth: .infinity)
    }

    private func closeScreen() {
        viewModel.stop()
        if let onClose {
            onClose()
        } else {
            dismiss()
        }
    }
}

private struct QuizTopHeader: View {
    let title: String
    let onBack: () -> Void

    var body: some View {
        HStack(spacing: 8) {
            Button(action: onBack) {
                Image(systemName: "chevron.left")
                .accessibilityLabel("Quay lại")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundStyle(.white)
                    .frame(width: 36, height: 36)
                    .background(Color.white.opacity(0.15))
                    .clipShape(Circle())
            }
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(.white)
                Text("Trả lời câu hỏi và tích điểm")
                    .font(.system(size: 12))
                    .foregroundStyle(.white.opacity(0.78))
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
        .frame(maxWidth: .infinity)
    }
}

private struct ProgressDots: View {
    let total: Int
    let current: Int
    let answers: [SubmitAnswerResult?]

    var body: some View {
        HStack(spacing: 4) {
            ForEach(0..<total, id: \.self) { index in
                if index > 0 && index % 5 == 0 {
                    Rectangle()
                        .fill(LSTheme.outlineVariant.opacity(0.75))
                        .frame(width: 2, height: 18)
                        .padding(.horizontal, 4)
                }
                Circle()
                    .fill(color(for: index))
                    .frame(width: 8, height: 8)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private func color(for index: Int) -> Color {
        if index == current { return LSTheme.primary }
        if let answer = answers[safe: index] ?? nil {
            return answer.isCorrect ? LSTheme.goodGreen : LSTheme.badRed
        }
        return LSTheme.outlineVariant.opacity(0.45)
    }
}

private struct TimerCircle: View {
    let seconds: Int

    var body: some View {
        ZStack {
            Circle()
                .stroke(LSTheme.goodGreen.opacity(0.18), lineWidth: 4)
            Circle()
                .trim(from: 0, to: CGFloat(max(seconds, 0)) / CGFloat(quizQuestionTimeSeconds + 15))
                .stroke(timerColor, style: StrokeStyle(lineWidth: 4, lineCap: .round))
                .rotationEffect(.degrees(-90))
            Text("\(max(0, seconds))")
                .font(.system(size: 16, weight: .bold, design: .rounded))
                .foregroundStyle(timerColor)
        }
        .frame(width: 48, height: 48)
    }

    private var timerColor: Color {
        seconds <= 10 ? LSTheme.badRed : LSTheme.goodGreen
    }
}

private struct AssistPanel: View {
    let state: QuizQuestionState
    let onTap: (QuizAssistType) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Label("Trợ giúp", systemImage: "sparkles")
                    .font(.system(size: 15, weight: .bold, design: .rounded))
                    .foregroundStyle(LSTheme.textPrimary)
                    .labelStyle(.titleAndIcon)
                Spacer()
                Text("\(state.dailyPoints) điểm")
                    .font(.system(size: 14, weight: .bold, design: .rounded))
                    .foregroundStyle(LSTheme.gold)
            }
            HStack(spacing: 6) {
                ForEach(QuizAssistType.allCases, id: \.self) { type in
                    Button {
                        onTap(type)
                    } label: {
                        VStack(spacing: 4) {
                            Image(systemName: type.icon)
                                .font(.system(size: 15, weight: .bold))
                            Text(type.label)
                                .font(.system(size: 12, weight: .bold, design: .rounded))
                            Text("\(type.cost) điểm")
                                .font(.system(size: 11))
                                .foregroundStyle(LSTheme.textSecondary)
                        }
                        .foregroundStyle(LSTheme.primary)
                        .frame(maxWidth: .infinity)
                        .frame(height: 60)
                        .background(LSTheme.primaryContainer.opacity(0.35), in: RoundedRectangle(cornerRadius: 12))
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(LSTheme.primary.opacity(0.28), lineWidth: 1)
                        )
                    }
                    .disabled(state.showingResult)
                    .opacity(state.showingResult ? 0.55 : 1)
                }
            }
        }
        .padding(12)
        .frame(maxWidth: .infinity)
        .background(LSTheme.surface.opacity(0.9), in: RoundedRectangle(cornerRadius: 14))
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(LSTheme.outlineVariant.opacity(0.45), lineWidth: 1)
        )
    }
}

private struct QuestionCard: View {
    let question: QuizQuestion

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 8) {
                Text(categoryLabel(question.category))
                    .font(.system(size: 13, weight: .bold))
                    .foregroundStyle(LSTheme.primary)
                Text("•")
                    .foregroundStyle(LSTheme.textTertiary)
                Text(difficultyLabel(question.difficulty))
                    .font(.system(size: 13))
                    .foregroundStyle(LSTheme.textTertiary)
            }
            Text(question.content)
                .font(.system(size: 16, weight: .bold, design: .rounded))
                .lineSpacing(4)
                .foregroundStyle(LSTheme.textPrimary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(LSTheme.surface.opacity(0.88), in: RoundedRectangle(cornerRadius: 14))
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(LSTheme.outlineVariant.opacity(0.45), lineWidth: 1)
        )
    }
}

private struct HintCard: View {
    let text: String?

    var body: some View {
        if let text, !text.isEmpty {
            HStack(alignment: .top, spacing: 12) {
                Image(systemName: "lightbulb.fill")
                    .foregroundStyle(LSTheme.gold)
                Text(text)
                    .font(.subheadline)
                    .foregroundStyle(LSTheme.textSecondary)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(14)
            .background(LSTheme.goldDim, in: RoundedRectangle(cornerRadius: 14))
        }
    }
}

private struct AnswerOptionButton: View {
    let key: String
    let text: String
    let selected: Bool
    let correct: Bool
    let wrong: Bool
    let disabled: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 14) {
                Text(key.uppercased())
                    .font(.system(size: 15, weight: .bold, design: .rounded))
                    .foregroundStyle(circleForeground)
                    .frame(width: 34, height: 34)
                    .background(circleBackground, in: Circle())
                Text(text)
                    .font(.system(size: 15, weight: .semibold, design: .rounded))
                    .foregroundStyle(LSTheme.textPrimary)
                    .multilineTextAlignment(.leading)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 12)
            .background(cardBackground, in: RoundedRectangle(cornerRadius: 14))
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(borderColor, lineWidth: 1)
            )
            .frame(maxWidth: .infinity)
        }
        .disabled(disabled)
    }

    private var cardBackground: Color {
        if correct { return LSTheme.goodGreen.opacity(0.14) }
        if wrong { return LSTheme.badRed.opacity(0.12) }
        if selected { return LSTheme.primaryContainer.opacity(0.36) }
        return LSTheme.surface.opacity(0.82)
    }

    private var borderColor: Color {
        if correct { return LSTheme.goodGreen }
        if wrong { return LSTheme.badRed }
        if selected { return LSTheme.primary.opacity(0.5) }
        return LSTheme.outlineVariant.opacity(0.88)
    }

    private var circleBackground: Color {
        if correct { return LSTheme.goodGreen }
        if wrong { return LSTheme.badRed }
        if selected { return LSTheme.primary }
        return LSTheme.surfaceContainerHigh.opacity(0.72)
    }

    private var circleForeground: Color {
        correct || wrong || selected ? .white : LSTheme.textTertiary.opacity(0.55)
    }
}

private struct ExplanationCard: View {
    let question: QuizQuestion
    let result: SubmitAnswerResult
    let reviewMode: Bool

    var body: some View {
        let reveal = reviewMode || result.isCorrect
        VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 10) {
                Image(systemName: result.isCorrect ? "checkmark.circle.fill" : "xmark.circle.fill")
                    .foregroundStyle(result.isCorrect ? LSTheme.goodGreen : LSTheme.badRed)
                Text(result.isCorrect ? "Chính xác" : "Chưa đúng")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundStyle(result.isCorrect ? LSTheme.goodGreen : LSTheme.badRed)
            }
            if reveal {
                Text("Đáp án đúng: \(displayChoice(result.correct))")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundStyle(LSTheme.textPrimary)
                if let explanation = result.explanation?.trimmingCharacters(in: .whitespacesAndNewlines), !explanation.isEmpty {
                    Text(explanation)
                        .font(.system(size: 13))
                        .lineSpacing(4)
                        .foregroundStyle(LSTheme.textSecondary)
                }
            } else {
                Text("Đáp án và lời giải sẽ mở ở phần ôn lại câu sai.")
                    .font(.system(size: 13))
                    .foregroundStyle(LSTheme.textSecondary)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background((result.isCorrect ? LSTheme.goodGreen : LSTheme.badRed).opacity(0.09), in: RoundedRectangle(cornerRadius: 14))
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke((result.isCorrect ? LSTheme.goodGreen : LSTheme.badRed).opacity(0.28), lineWidth: 1)
        )
    }
}

private func normalizeChoice(_ value: String?) -> String {
    value?.trimmingCharacters(in: .whitespacesAndNewlines).lowercased() ?? ""
}

private func correctOptionKey(_ question: QuizQuestion, override: String? = nil) -> String {
    let normalizedOverride = normalizeChoice(override)
    if ["a", "b", "c", "d"].contains(normalizedOverride) {
        return normalizedOverride
    }
    let normalizedCorrect = normalizeChoice(question.correct)
    if ["a", "b", "c", "d"].contains(normalizedCorrect) {
        return normalizedCorrect
    }
    let normalizedCorrectAnswer = normalizeChoice(question.correctAnswer)
    if ["a", "b", "c", "d"].contains(normalizedCorrectAnswer) {
        return normalizedCorrectAnswer
    }
    let answerText = normalizeChoice(question.correctAnswer ?? question.correct)
    let options = [question.optionA, question.optionB, question.optionC, question.optionD].map(normalizeChoice)
    if let index = options.firstIndex(of: answerText) {
        return ["a", "b", "c", "d"][index]
    }
    return ""
}

private func buildFiftyFiftyHiddenOptions(question: QuizQuestion, correct: String) -> Set<String> {
    let seed = Int(question.id) + question.content.unicodeScalars.reduce(0) { ($0 * 31 + Int($1.value)) & 0x7fffffff }
    return Set(["a", "b", "c", "d"]
        .filter { $0 != correct }
        .sorted { ((seed + Int($0.unicodeScalars.first?.value ?? 0)) % 97) < ((seed + Int($1.unicodeScalars.first?.value ?? 0)) % 97) }
        .prefix(2))
}

private func categoryLabel(_ value: String) -> String {
    switch value {
    case "history_vn": "Lịch sử Việt Nam"
    case "history_world": "Lịch sử thế giới"
    case "culture": "Văn hóa"
    case "festival": "Lễ hội"
    case "figure": "Nhân vật"
    default: value.replacingOccurrences(of: "_", with: " ").capitalized
    }
}

private func difficultyLabel(_ value: String) -> String {
    switch value {
    case "easy": "Dễ"
    case "medium": "Trung bình"
    case "hard": "Khó"
    default: value.capitalized
    }
}

private func displayChoice(_ value: String) -> String {
    let key = normalizeChoice(value)
    return ["a", "b", "c", "d"].contains(key) ? key.uppercased() : value
}

private extension Array {
    subscript(safe index: Int) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}

private extension Comparable {
    func clamped(to limits: ClosedRange<Self>) -> Self {
        min(max(self, limits.lowerBound), limits.upperBound)
    }
}
