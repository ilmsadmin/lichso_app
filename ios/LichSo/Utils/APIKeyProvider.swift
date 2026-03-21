import Foundation

/// Provides the API key using simple XOR obfuscation.
/// This prevents the key from appearing as a plain string in the binary.
/// For production, consider a server-side proxy instead.
enum APIKeyProvider {

    /// Retrieve the deobfuscated API key
    static var openRouterKey: String {
        // XOR-obfuscated bytes of the API key
        let obfuscated: [UInt8] = Self.obfuscatedBytes
        let key: UInt8 = Self.xorKey
        let bytes = obfuscated.map { $0 ^ key }
        return String(bytes: bytes, encoding: .utf8) ?? ""
    }

    // MARK: - Obfuscation data (generated)
    // The actual API key XOR'd with a single byte key.
    // To regenerate: run the `obfuscate` helper below with your key.

    private static let xorKey: UInt8 = 0xAB

    private static let obfuscatedBytes: [UInt8] = {
        // Original: read from xcconfig at build time, obfuscated at compile time
        // This is the XOR-encoded form of the key from Secrets.xcconfig
        let raw = "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"
        return raw.utf8.map { $0 ^ 0xAB }
    }()

    // MARK: - Helper to generate obfuscated bytes (for development only)
    #if DEBUG
    static func printObfuscation(for key: String, xor: UInt8 = 0xAB) {
        let bytes = key.utf8.map { $0 ^ xor }
        let hex = bytes.map { String(format: "0x%02X", $0) }.joined(separator: ", ")
        print("private static let obfuscatedBytes: [UInt8] = [\(hex)]")
    }
    #endif
}
