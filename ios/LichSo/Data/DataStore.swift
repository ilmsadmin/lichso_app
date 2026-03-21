import Foundation

/// Simple JSON-file-based persistence for app data.
/// Stores data in the app's Documents directory.
enum DataStore {

    // MARK: - File URLs

    private static var documentsURL: URL {
        FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
    }

    private static func fileURL(for key: String) -> URL {
        documentsURL.appendingPathComponent("\(key).json")
    }

    // MARK: - Save

    static func save<T: Encodable>(_ data: T, forKey key: String) {
        do {
            let encoded = try JSONEncoder().encode(data)
            try encoded.write(to: fileURL(for: key), options: .atomic)
        } catch {
            print("⚠️ DataStore save error (\(key)): \(error.localizedDescription)")
        }
    }

    // MARK: - Load

    static func load<T: Decodable>(_ type: T.Type, forKey key: String) -> T? {
        let url = fileURL(for: key)
        guard FileManager.default.fileExists(atPath: url.path) else { return nil }
        do {
            let data = try Data(contentsOf: url)
            return try JSONDecoder().decode(type, from: data)
        } catch {
            print("⚠️ DataStore load error (\(key)): \(error.localizedDescription)")
            return nil
        }
    }

    // MARK: - Delete

    static func delete(forKey key: String) {
        let url = fileURL(for: key)
        try? FileManager.default.removeItem(at: url)
    }

    // MARK: - Keys
    static let tasksKey = "user_tasks"
    static let notesKey = "user_notes"
    static let remindersKey = "user_reminders"
    static let chatMessagesKey = "chat_messages"
}
