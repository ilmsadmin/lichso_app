import Foundation
import AuthenticationServices
import UIKit
import CryptoKit

// ═══════════════════════════════════════════
// GoogleAuthService — Native OAuth2 via ASWebAuthenticationSession
// No external SDKs required.
// ═══════════════════════════════════════════

struct GoogleUserInfo: Codable {
    let id: String
    let email: String
    let name: String
    let picture: String?
}

struct GoogleTokenResponse: Codable {
    let access_token: String
    let id_token: String
    let token_type: String
    let expires_in: Int?
    let refresh_token: String?
}

struct BackendLoginResponse: Codable {
    let success: Bool
    let message: String?
    let access_token: String?
    let refresh_token: String?
    let user: BackendUser?
}

struct BackendLoginPayload: Codable {
    let access_token: String?
    let refresh_token: String?
    let user: BackendUser?
}

struct BackendAPIEnvelope<T: Codable>: Codable {
    let success: Bool
    let message: String?
    let data: T?
}

struct BackendUser: Codable {
    let id: String?
    let email: String?
    let name: String?
    let roles: [String]?
    let permissions: [String]?
}

@MainActor
class GoogleAuthService: NSObject, ObservableObject, ASWebAuthenticationPresentationContextProviding {

    static let shared = GoogleAuthService()

    @Published var isSignedIn: Bool = false
    @Published var displayName: String = ""
    @Published var email: String = ""
    @Published var photoURL: String = ""
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil

    private enum Keys {
        static let accessToken = "backend_access_token"
        static let refreshToken = "backend_refresh_token"
        static let displayName = "displayName"
        static let email = "profile_email"
        static let googlePhoto = "profile_google_photo_url"
        static let localAvatarPath = "profile_avatar_path"
    }

    // ─────────────────────────────────────────────────────────────────────────
    // QUAN TRỌNG: Phải tạo iOS OAuth Client ID riêng trong Google Cloud Console:
    //   console.cloud.google.com → APIs & Services → Credentials
    //   → Create Credentials → OAuth 2.0 Client ID
    //   → Application type: iOS
    //   → Bundle ID: com.lichso.app
    //   → Copy iOS Client ID (dạng xxxxxx.apps.googleusercontent.com)
    //
    // Web Client ID (hiện dùng cho Android) KHÔNG hỗ trợ custom URI scheme trên iOS.
    // ─────────────────────────────────────────────────────────────────────────

    // iOS OAuth2 Client ID — iOS type client, supports custom URI scheme
    private let iosClientId = "339910552400-n8hu9l5saitl758h8gtafje0aqu15nc8.apps.googleusercontent.com"

    // Redirect URI dùng reversed iOS client ID (không phải Web client ID)
    // Sau khi thay iosClientId, cập nhật 2 dòng dưới theo đúng iOS client ID
    private var redirectScheme: String {
        // Lấy phần trước ".apps.googleusercontent.com" rồi đảo ngược bằng cách giữ nguyên prefix
        // Format: com.googleusercontent.apps.<ios-client-id-prefix>
        let prefix = iosClientId.replacingOccurrences(of: ".apps.googleusercontent.com", with: "")
        return "com.googleusercontent.apps.\(prefix)"
    }
    private var redirectURI: String {
        "\(redirectScheme):/oauth2callback"
    }

    private let backendBaseURL = "https://api.lichso.vn/api"

    private var codeVerifier: String = ""

    private override init() {
        super.init()
        restoreSession()
    }

    // ── PKCE helpers ──
    private func generateCodeVerifier() -> String {
        var buffer = [UInt8](repeating: 0, count: 64)
        _ = SecRandomCopyBytes(kSecRandomDefault, buffer.count, &buffer)
        return Data(buffer).base64URLEncodedString()
    }

    private func codeChallenge(for verifier: String) -> String {
        let data = Data(verifier.utf8)
        let hashed = SHA256.hash(data: data)
        return Data(hashed).base64URLEncodedString()
    }

    // ── SIGN IN ──
    func signIn() async {
        isLoading = true
        errorMessage = nil

        codeVerifier = generateCodeVerifier()
        let challenge = codeChallenge(for: codeVerifier)
        let state = UUID().uuidString

        var components = URLComponents(string: "https://accounts.google.com/o/oauth2/v2/auth")!
        components.queryItems = [
            .init(name: "client_id",             value: iosClientId),   // iOS client ID — supports custom URI
            .init(name: "redirect_uri",           value: redirectURI),
            .init(name: "response_type",          value: "code"),
            .init(name: "scope",                  value: "openid email profile"),
            .init(name: "state",                  value: state),
            .init(name: "code_challenge",         value: challenge),
            .init(name: "code_challenge_method",  value: "S256"),
            .init(name: "access_type",            value: "offline"),
        ]
        guard let authURL = components.url else {
            errorMessage = "Không thể tạo URL đăng nhập."
            isLoading = false
            return
        }

        do {
            let callbackURL = try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<URL, Error>) in
                let session = ASWebAuthenticationSession(
                    url: authURL,
                    callbackURLScheme: redirectScheme
                ) { url, error in
                    if let error = error {
                        continuation.resume(throwing: error)
                    } else if let url = url {
                        continuation.resume(returning: url)
                    } else {
                        continuation.resume(throwing: NSError(domain: "GoogleAuth", code: -1,
                            userInfo: [NSLocalizedDescriptionKey: "Không nhận được callback URL"]))
                    }
                }
                session.presentationContextProvider = self
                session.prefersEphemeralWebBrowserSession = false
                session.start()
            }

            // Extract auth code from callback URL
            guard let components = URLComponents(url: callbackURL, resolvingAgainstBaseURL: false),
                  let code = components.queryItems?.first(where: { $0.name == "code" })?.value else {
                throw NSError(domain: "GoogleAuth", code: -2,
                              userInfo: [NSLocalizedDescriptionKey: "Không tìm thấy mã xác thực"])
            }

            // Exchange code → tokens
            let tokens = try await exchangeCodeForTokens(code: code)

            // Exchange id_token → backend JWT
            try await loginWithBackend(idToken: tokens.id_token)

        } catch ASWebAuthenticationSessionError.canceledLogin {
            // User cancelled — silently abort
            isLoading = false
            return
        } catch {
            errorMessage = ApiErrorUX.userMessage(
                from: error,
                fallback: "Đăng nhập thất bại. Vui lòng thử lại."
            )
            isLoading = false
        }
    }

    // ── Exchange code for tokens ──
    private func exchangeCodeForTokens(code: String) async throws -> GoogleTokenResponse {
        var request = URLRequest(url: URL(string: "https://oauth2.googleapis.com/token")!)
        request.httpMethod = "POST"
        request.setValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")
        // iOS clients with PKCE do NOT use client_secret
        let params = [
            "client_id":      iosClientId,
            "code":           code,
            "code_verifier":  codeVerifier,
            "redirect_uri":   redirectURI,
            "grant_type":     "authorization_code"
        ]
        request.httpBody = params.map { "\($0.key)=\($0.value.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? "")" }
                                  .joined(separator: "&")
                                  .data(using: .utf8)

        let (data, response) = try await URLSession.shared.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            let body = String(data: data, encoding: .utf8) ?? ""
            throw NSError(domain: "GoogleAuth", code: -3,
                          userInfo: [NSLocalizedDescriptionKey: "Token exchange thất bại: \(body)"])
        }
        return try JSONDecoder().decode(GoogleTokenResponse.self, from: data)
    }

    // ── Exchange id_token with backend ──
    private func loginWithBackend(idToken: String) async throws {
        var request = URLRequest(url: URL(string: "\(backendBaseURL)/auth/google")!)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        applyClientHeaders(to: &request)
        request.httpBody = try JSONSerialization.data(withJSONObject: ["id_token": idToken])

        let (data, response) = try await URLSession.shared.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            let body = String(data: data, encoding: .utf8) ?? ""
            throw NSError(domain: "GoogleAuth", code: -4,
                          userInfo: [NSLocalizedDescriptionKey: "Xác thực máy chủ thất bại: \(body)"])
        }

        let decoded = try decodeBackendLoginResponse(from: data)

        // Save tokens
        if let accessToken = decoded.access_token {
            UserDefaults.standard.set(accessToken, forKey: Keys.accessToken)
        }
        if let refreshToken = decoded.refresh_token {
            UserDefaults.standard.set(refreshToken, forKey: Keys.refreshToken)
        }

        // Fetch user info from Google
        let userInfo = try await fetchGoogleUserInfo(idToken: idToken)

        // Save profile locally
        UserDefaults.standard.set(userInfo.name, forKey: Keys.displayName)
        UserDefaults.standard.set(userInfo.email, forKey: Keys.email)
        if let pic = userInfo.picture {
            UserDefaults.standard.set(pic, forKey: Keys.googlePhoto)
        }
        // Prioritize Google avatar right after login.
        UserDefaults.standard.removeObject(forKey: Keys.localAvatarPath)

        // Update published state
        displayName = userInfo.name
        email       = userInfo.email
        photoURL    = userInfo.picture ?? ""
        isSignedIn  = true
        isLoading   = false
    }

    // ── Decode JWT claims from id_token ──
    private func fetchGoogleUserInfo(idToken: String) async throws -> GoogleUserInfo {
        // Decode JWT payload (no signature verification needed — backend already validated)
        let parts = idToken.split(separator: ".")
        guard parts.count >= 2 else {
            throw NSError(domain: "GoogleAuth", code: -5, userInfo: [NSLocalizedDescriptionKey: "id_token không hợp lệ"])
        }
        var base64 = String(parts[1])
        // Pad to multiple of 4
        let remainder = base64.count % 4
        if remainder != 0 { base64 += String(repeating: "=", count: 4 - remainder) }
        // Replace URL-safe chars
        base64 = base64.replacingOccurrences(of: "-", with: "+").replacingOccurrences(of: "_", with: "/")
        guard let payloadData = Data(base64Encoded: base64) else {
            throw NSError(domain: "GoogleAuth", code: -5, userInfo: [NSLocalizedDescriptionKey: "Không giải mã được token"])
        }
        let json = try JSONSerialization.jsonObject(with: payloadData) as? [String: Any] ?? [:]
        let name    = json["name"] as? String ?? (json["email"] as? String ?? "Người dùng")
        let emailStr = json["email"] as? String ?? ""
        let sub     = json["sub"] as? String ?? ""
        let picture = json["picture"] as? String
        return GoogleUserInfo(id: sub, email: emailStr, name: name, picture: picture)
    }

    // ── SIGN OUT ──
    func signOut() {
        // Revoke backend token if any
        if let token = UserDefaults.standard.string(forKey: Keys.accessToken) {
            Task {
                var req = URLRequest(url: URL(string: "\(backendBaseURL)/auth/logout")!)
                req.httpMethod = "POST"
                req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
                try? await URLSession.shared.data(for: req)
            }
        }
        UserDefaults.standard.removeObject(forKey: Keys.accessToken)
        UserDefaults.standard.removeObject(forKey: Keys.refreshToken)
        UserDefaults.standard.removeObject(forKey: Keys.googlePhoto)
        isSignedIn  = false
        displayName = ""
        email       = ""
        photoURL    = ""
    }

    // ── Restore session on launch ──
    private func restoreSession() {
        let accessToken = UserDefaults.standard.string(forKey: Keys.accessToken) ?? ""
        let refreshToken = UserDefaults.standard.string(forKey: Keys.refreshToken) ?? ""
        if !accessToken.isEmpty || !refreshToken.isEmpty {
            isSignedIn  = true
            displayName = UserDefaults.standard.string(forKey: Keys.displayName) ?? ""
            email       = UserDefaults.standard.string(forKey: Keys.email) ?? ""
            photoURL    = UserDefaults.standard.string(forKey: Keys.googlePhoto) ?? ""
        }
    }

    // ── Refresh backend token ──
    func refreshTokenIfNeeded() async {
        guard let refreshToken = UserDefaults.standard.string(forKey: Keys.refreshToken),
              !refreshToken.isEmpty else { return }
        var request = URLRequest(url: URL(string: "\(backendBaseURL)/auth/refresh")!)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        guard let body = try? JSONSerialization.data(withJSONObject: ["refresh_token": refreshToken]) else { return }
        request.httpBody = body
        guard let (data, _) = try? await URLSession.shared.data(for: request),
              let decoded = try? decodeBackendLoginResponse(from: data),
              let newToken = decoded.access_token else { return }
        UserDefaults.standard.set(newToken, forKey: Keys.accessToken)
        if let newRefresh = decoded.refresh_token {
            UserDefaults.standard.set(newRefresh, forKey: Keys.refreshToken)
        }
    }

    private func decodeBackendLoginResponse(from data: Data) throws -> BackendLoginResponse {
        let decoder = JSONDecoder()
        // Current backend format: { success, message, data: { access_token, ... } }
        if let wrapped = try? decoder.decode(BackendAPIEnvelope<BackendLoginPayload>.self, from: data),
           let payload = wrapped.data {
            return BackendLoginResponse(
                success: wrapped.success,
                message: wrapped.message,
                access_token: payload.access_token,
                refresh_token: payload.refresh_token,
                user: payload.user
            )
        }
        // Backward compatibility: { success, message, access_token, ... }
        return try decoder.decode(BackendLoginResponse.self, from: data)
    }

    private func applyClientHeaders(to request: inout URLRequest) {
        let appVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
        request.setValue("IOS", forHTTPHeaderField: "X-Client-Platform")
        request.setValue(appVersion, forHTTPHeaderField: "X-App-Version")
        request.setValue(UIDevice.current.model, forHTTPHeaderField: "X-Device-Name")
        request.setValue(UIDevice.current.systemVersion, forHTTPHeaderField: "X-OS-Version")
        if let deviceId = UIDevice.current.identifierForVendor?.uuidString {
            request.setValue(deviceId, forHTTPHeaderField: "X-Device-ID")
        }
    }

    // ── ASWebAuthenticationPresentationContextProviding ──
    nonisolated func presentationAnchor(for session: ASWebAuthenticationSession) -> ASPresentationAnchor {
        // ASWebAuthenticationSession always calls this on the main thread.
        // We use MainActor.assumeIsolated to safely access UIKit APIs.
        return MainActor.assumeIsolated {
            let scenes = UIApplication.shared.connectedScenes
            if let windowScene = scenes.compactMap({ $0 as? UIWindowScene })
                .first(where: { $0.activationState == .foregroundActive }),
               let window = windowScene.windows.first(where: { $0.isKeyWindow }) {
                return window
            }
            return ASPresentationAnchor()
        }
    }
}

// ── Data extension for PKCE Base64URL encoding ──
private extension Data {
    func base64URLEncodedString() -> String {
        base64EncodedString()
            .replacingOccurrences(of: "+", with: "-")
            .replacingOccurrences(of: "/", with: "_")
            .replacingOccurrences(of: "=", with: "")
    }
}
