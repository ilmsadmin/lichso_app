import Foundation
import UIKit

/// Cung cấp các HTTP headers nhận diện client iOS cho backend logging.
/// Backend đọc X-Client-Platform để phân biệt IOS / ANDROID / WEB.
enum LichSoClientInfo {
    static let platform = "IOS"
    static let appVersion: String = {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
    }()
    static var deviceName: String { UIDevice.current.model }
    static var osVersion: String { UIDevice.current.systemVersion }

    static func applyHeaders(to request: inout URLRequest) {
        request.setValue(platform,    forHTTPHeaderField: "X-Client-Platform")
        request.setValue(appVersion,  forHTTPHeaderField: "X-App-Version")
        request.setValue(deviceName,  forHTTPHeaderField: "X-Device-Name")
        request.setValue(osVersion,   forHTTPHeaderField: "X-OS-Version")
    }
}
