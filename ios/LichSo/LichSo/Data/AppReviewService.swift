import Foundation
import UIKit

private struct EmptyResponse: Codable {}

@MainActor
final class AppReviewService {
    static let shared = AppReviewService()

    private let baseURL = "https://api.lichso.vn/api"

    private init() {}

    private var token: String? {
        TokenStore.accessToken
    }

    private var hasValidToken: Bool {
        guard let token, !token.isEmpty else { return false }
        return true
    }

    func submitLowRatingFeedback(stars: Int, reviewText: String) async throws {
        try await submitReview(stars: stars, reviewText: reviewText, reviewFlow: "low_rating_feedback")
    }

    func submitHighRatingReview(stars: Int) async throws {
        try await submitReview(stars: stars, reviewText: "", reviewFlow: "high_rating_prompt")
    }

    private func submitReview(stars: Int, reviewText: String, reviewFlow: String) async throws {
        let body: [String: Any] = [
            "stars": stars,
            "review_text": reviewText,
            "review_flow": reviewFlow
        ]
        let bodyData = try JSONSerialization.data(withJSONObject: body)
        let request = makeRequest(path: "/app-reviews", method: "POST", body: bodyData)
        _ = try await execute(request) as EmptyResponse
    }

    private func makeRequest(path: String, method: String, body: Data?) -> URLRequest {
        var request = URLRequest(url: URL(string: "\(baseURL)\(path)")!)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        LichSoClientInfo.applyHeaders(to: &request)
        if let token, !token.isEmpty {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        request.httpBody = body
        return request
    }

    private func execute<T: Codable>(_ request: URLRequest) async throws -> T {
        let (data, httpResponse) = try await performRequestWithAuthRetry(request)
        guard (200...299).contains(httpResponse.statusCode) else {
            let body = String(data: data, encoding: .utf8) ?? ""
            throw NSError(
                domain: "AppReviewService",
                code: httpResponse.statusCode,
                userInfo: [NSLocalizedDescriptionKey: "Yêu cầu mạng thất bại (\(httpResponse.statusCode)): \(body)"]
            )
        }

        let decoder = JSONDecoder()
        let wrapper = try decoder.decode(ApiResponse<T>.self, from: data)
        if !wrapper.success, let msg = wrapper.message {
            throw NSError(domain: "AppReviewService", code: -2, userInfo: [NSLocalizedDescriptionKey: msg])
        }
        guard let resultData = wrapper.data else {
            throw NSError(domain: "AppReviewService", code: -3, userInfo: [NSLocalizedDescriptionKey: "Dữ liệu trả về trống"])
        }
        return resultData
    }

    private func performRequestWithAuthRetry(_ request: URLRequest) async throws -> (Data, HTTPURLResponse) {
        let (data, response) = try await URLSession.shared.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw NSError(domain: "AppReviewService", code: -1, userInfo: [NSLocalizedDescriptionKey: "Phản hồi máy chủ không hợp lệ"])
        }

        guard httpResponse.statusCode == 401, hasValidToken else {
            return (data, httpResponse)
        }

        await GoogleAuthService.shared.refreshTokenIfNeeded()
        guard let refreshedToken = token, !refreshedToken.isEmpty else {
            return (data, httpResponse)
        }

        var retriedRequest = request
        retriedRequest.setValue("Bearer \(refreshedToken)", forHTTPHeaderField: "Authorization")

        let (retryData, retryResponse) = try await URLSession.shared.data(for: retriedRequest)
        guard let retryHTTP = retryResponse as? HTTPURLResponse else {
            throw NSError(domain: "AppReviewService", code: -1, userInfo: [NSLocalizedDescriptionKey: "Phản hồi máy chủ không hợp lệ"])
        }
        return (retryData, retryHTTP)
    }
}
