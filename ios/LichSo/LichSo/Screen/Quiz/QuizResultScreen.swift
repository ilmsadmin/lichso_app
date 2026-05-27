import SwiftUI
import UIKit

struct QuizResultScreen: View {
    let result: SessionResult?
    let answers: [SubmitAnswerResult?]
    let questions: [QuizQuestion]
    let onReviewWrong: ([QuizQuestion], [Int64: String], [Int64: String]) -> Void
    let onClose: () -> Void
    let onPlayAgain: () -> Void

    private var correctCount: Int {
        answers.compactMap { $0 }.filter(\.isCorrect).count
    }

    private var total: Int {
        questions.count
    }

    private var wrongQuestions: [QuizQuestion] {
        answers.enumerated().compactMap { index, answer in
            guard answer?.isCorrect == false else { return nil }
            return questions[safe: index]
        }
    }

    private var wrongCorrectAnswers: [Int64: String] {
        answers.enumerated().reduce(into: [:]) { partial, item in
            let (index, answer) = item
            guard let question = questions[safe: index],
                  let answer,
                  answer.isCorrect == false,
                  !answer.correct.isEmpty
            else { return }
            partial[question.id] = answer.correct
        }
    }

    private var wrongExplanations: [Int64: String] {
        answers.enumerated().reduce(into: [:]) { partial, item in
            let (index, answer) = item
            guard let question = questions[safe: index],
                  let answer,
                  answer.isCorrect == false
            else { return }
            if let explanation = answer.explanation?.trimmingCharacters(in: .whitespacesAndNewlines), !explanation.isEmpty {
                partial[question.id] = explanation
            } else if let explanation = question.explanation?.trimmingCharacters(in: .whitespacesAndNewlines), !explanation.isEmpty {
                partial[question.id] = explanation
            }
        }
    }

    var body: some View {
        ZStack {
            LSTheme.bg.ignoresSafeArea()
            ScrollView {
                VStack(spacing: 18) {
                    resultHero
                    pointsPanel
                    actionButtons
                    answerReviewList
                }
                .padding(18)
            }
        }
    }

    private var resultHero: some View {
        VStack(spacing: 12) {
            Text("Kết quả")
                .font(.system(size: 18, weight: .bold, design: .rounded))
                .foregroundStyle(.white.opacity(0.82))
            Text("\(correctCount) / \(max(total, 1))")
                .font(.system(size: 58, weight: .heavy, design: .rounded))
                .foregroundStyle(.white)
            Text(result?.sessionTitle.isEmpty == false ? result!.sessionTitle : resultTitle)
                .font(.system(size: 22, weight: .heavy, design: .rounded))
                .foregroundStyle(LSTheme.gold)
            if !wrongQuestions.isEmpty {
                Label("\(wrongQuestions.count) câu sai có thể ôn lại", systemImage: "arrow.uturn.backward.circle.fill")
                    .font(.subheadline.weight(.bold))
                    .foregroundStyle(.white.opacity(0.9))
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .background(.white.opacity(0.14), in: Capsule())
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 30)
        .background(
            LinearGradient(
                colors: [LSTheme.primary, LSTheme.deepRed],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            ),
            in: RoundedRectangle(cornerRadius: 28)
        )
    }

    private var pointsPanel: some View {
        VStack(spacing: 12) {
            HStack(spacing: 12) {
                pointChip(label: "\(result?.scoreV2 ?? correctCount * 10) điểm", icon: "star.fill", color: LSTheme.primary)
                pointChip(label: "+\(result?.appPointsEarned ?? result?.pointsEarned ?? correctCount) App Points", icon: "sparkles", color: LSTheme.gold)
            }
            HStack(spacing: 12) {
                pointChip(label: "+\(result?.xpEarned ?? correctCount * 10) XP", icon: "bolt.fill", color: LSTheme.teal)
                pointChip(label: result?.rank ?? 0 > 0 ? "Hạng \(result?.rank ?? 0)" : "Bảng xếp hạng", icon: "trophy.fill", color: LSTheme.goodGreen)
            }
        }
    }

    private var actionButtons: some View {
        VStack(spacing: 12) {
            if !wrongQuestions.isEmpty {
                Button {
                    onReviewWrong(wrongQuestions, wrongCorrectAnswers, wrongExplanations)
                } label: {
                    Label("Ôn lại \(wrongQuestions.count) câu sai", systemImage: "arrow.counterclockwise.circle.fill")
                        .font(.headline)
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 15)
                        .background(LSTheme.neutralAmberFallback, in: RoundedRectangle(cornerRadius: 18))
                }
            }

            HStack(spacing: 12) {
                Button(action: onPlayAgain) {
                    Label("Chơi lại", systemImage: "play.circle.fill")
                        .font(.headline)
                        .foregroundStyle(LSTheme.primary)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 15)
                        .background(LSTheme.primaryContainer.opacity(0.5), in: RoundedRectangle(cornerRadius: 18))
                }
                Button(action: onClose) {
                    Text("Đóng")
                        .font(.headline)
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 15)
                        .background(LSTheme.primary, in: RoundedRectangle(cornerRadius: 18))
                }
            }

            Button(action: shareResult) {
                Label("Chia sẻ kết quả", systemImage: "square.and.arrow.up")
                    .font(.subheadline.weight(.bold))
                    .foregroundStyle(LSTheme.textPrimary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(LSTheme.surfaceContainer, in: RoundedRectangle(cornerRadius: 18))
                    .overlay(
                        RoundedRectangle(cornerRadius: 18)
                            .stroke(LSTheme.outlineVariant.opacity(0.4), lineWidth: 1)
                    )
            }
        }
    }

    private var answerReviewList: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Tổng kết câu hỏi")
                .font(.system(size: 22, weight: .heavy, design: .rounded))
                .foregroundStyle(LSTheme.textPrimary)
                .padding(.top, 4)
            ForEach(Array(questions.enumerated()), id: \.element.id) { index, question in
                ResultAnswerRow(index: index, question: question, answer: answers[safe: index] ?? nil)
            }
        }
    }

    private var resultTitle: String {
        let ratio = total == 0 ? 0 : Double(correctCount) / Double(total)
        if ratio >= 0.8 { return "Xuất sắc!" }
        if ratio >= 0.5 { return "Tốt lắm!" }
        return "Cùng ôn lại nhé!"
    }

    private func pointChip(label: String, icon: String, color: Color) -> some View {
        Label(label, systemImage: icon)
            .font(.subheadline.weight(.bold))
            .foregroundStyle(color)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 12)
            .background(color.opacity(0.12), in: RoundedRectangle(cornerRadius: 18))
            .overlay(
                RoundedRectangle(cornerRadius: 18)
                    .stroke(color.opacity(0.25), lineWidth: 1)
            )
    }

    private func shareResult() {
        let text = """
        KẾT QUẢ ĐỐ VUI LỊCH SỐ
        Số câu đúng: \(correctCount)/\(total)
        Điểm thành tích: \(result?.scoreV2 ?? correctCount * 10)
        XP nhận được: +\(result?.xpEarned ?? correctCount * 10)
        """
        let activityVC = UIActivityViewController(activityItems: [text], applicationActivities: nil)
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootVC = windowScene.windows.first?.rootViewController {
            rootVC.present(activityVC, animated: true)
        }
    }
}

private struct ResultAnswerRow: View {
    let index: Int
    let question: QuizQuestion
    let answer: SubmitAnswerResult?

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 10) {
                Text("\(index + 1)")
                    .font(.headline)
                    .foregroundStyle(.white)
                    .frame(width: 34, height: 34)
                    .background((answer?.isCorrect == true ? LSTheme.goodGreen : LSTheme.badRed), in: Circle())
                Text(question.content)
                    .font(.subheadline.weight(.bold))
                    .foregroundStyle(LSTheme.textPrimary)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            HStack {
                Text("Bạn chọn: \(displayChoice(answer?.chosen ?? "-"))")
                    .foregroundStyle(LSTheme.textSecondary)
                Spacer()
                Text(answer?.isCorrect == true ? "Đúng" : "Sai")
                    .font(.caption.weight(.bold))
                    .foregroundStyle(answer?.isCorrect == true ? LSTheme.goodGreen : LSTheme.badRed)
            }
            .font(.caption)
        }
        .padding(14)
        .background(LSTheme.surface.opacity(0.9), in: RoundedRectangle(cornerRadius: 18))
        .overlay(
            RoundedRectangle(cornerRadius: 18)
                .stroke(LSTheme.outlineVariant.opacity(0.45), lineWidth: 1)
        )
    }
}

private func displayChoice(_ value: String) -> String {
    let key = value.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
    return ["a", "b", "c", "d"].contains(key) ? key.uppercased() : value
}

private extension LSTheme {
    static var neutralAmberFallback: Color { Color(hex: "F57F17") }
}

private extension Array {
    subscript(safe index: Int) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}
