import Foundation
import Security

// ═══════════════════════════════════════════
// TokenStore — secure storage for auth tokens via the iOS Keychain.
//
// Trước đây access_token / refresh_token được lưu trong UserDefaults (plaintext,
// nằm trong backup không mã hoá). Chuyển sang Keychain với thuộc tính
// kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly để token không rời khỏi máy.
//
// Tự động di trú (lazy migration) token cũ từ UserDefaults sang Keychain ở lần
// đọc đầu tiên rồi xoá khỏi UserDefaults.
// ═══════════════════════════════════════════

enum TokenStore {

    private enum Account {
        static let accessToken = "backend_access_token"
        static let refreshToken = "backend_refresh_token"
    }

    private static let service = "com.lichso.app.auth"

    // ── Public API ──
    static var accessToken: String? {
        get { read(Account.accessToken) }
        set { write(newValue, for: Account.accessToken) }
    }

    static var refreshToken: String? {
        get { read(Account.refreshToken) }
        set { write(newValue, for: Account.refreshToken) }
    }

    static func clear() {
        delete(Account.accessToken)
        delete(Account.refreshToken)
    }

    // ── Keychain primitives ──
    private static func baseQuery(_ account: String) -> [String: Any] {
        [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account
        ]
    }

    private static func read(_ account: String) -> String? {
        var query = baseQuery(account)
        query[kSecReturnData as String] = true
        query[kSecMatchLimit as String] = kSecMatchLimitOne

        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)
        if status == errSecSuccess,
           let data = result as? Data,
           let value = String(data: data, encoding: .utf8),
           !value.isEmpty {
            return value
        }
        // Lazy migration: token cũ còn nằm trong UserDefaults?
        if let legacy = UserDefaults.standard.string(forKey: account), !legacy.isEmpty {
            write(legacy, for: account)
            UserDefaults.standard.removeObject(forKey: account)
            return legacy
        }
        return nil
    }

    private static func write(_ value: String?, for account: String) {
        guard let value, !value.isEmpty else {
            delete(account)
            return
        }
        let data = Data(value.utf8)
        let query = baseQuery(account)
        let attributes: [String: Any] = [
            kSecValueData as String: data,
            kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
        ]
        let status = SecItemUpdate(query as CFDictionary, attributes as CFDictionary)
        if status == errSecItemNotFound {
            var insert = query
            insert[kSecValueData as String] = data
            insert[kSecAttrAccessible as String] = kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
            SecItemAdd(insert as CFDictionary, nil)
        }
        // Dọn bản UserDefaults cũ nếu còn sót
        UserDefaults.standard.removeObject(forKey: account)
    }

    private static func delete(_ account: String) {
        SecItemDelete(baseQuery(account) as CFDictionary)
        UserDefaults.standard.removeObject(forKey: account)
    }
}
