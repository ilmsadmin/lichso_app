import Foundation
import UIKit

/// Cung cấp các HTTP headers nhận diện client iOS cho backend logging.
/// Backend đọc X-Client-Platform để phân biệt IOS / ANDROID / WEB.
enum LichSoClientInfo {
    static let platform = "IOS"
    static let appVersion: String = {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
    }()
    static var deviceName: String { readableDeviceModel() }
    static var osVersion: String { UIDevice.current.systemVersion }
    static var deviceID: String? { UIDevice.current.identifierForVendor?.uuidString }

    static func applyHeaders(to request: inout URLRequest) {
        request.setValue(platform,    forHTTPHeaderField: "X-Client-Platform")
        request.setValue(appVersion,  forHTTPHeaderField: "X-App-Version")
        request.setValue(deviceName,  forHTTPHeaderField: "X-Device-Name")
        request.setValue(osVersion,   forHTTPHeaderField: "X-OS-Version")
        if let deviceID {
            request.setValue(deviceID, forHTTPHeaderField: "X-Device-ID")
        }
    }

    private static func readableDeviceModel() -> String {
        var systemInfo = utsname()
        uname(&systemInfo)

        let identifier = withUnsafePointer(to: &systemInfo.machine) { pointer in
            pointer.withMemoryRebound(to: CChar.self, capacity: Int(_SYS_NAMELEN)) {
                String(cString: $0)
            }
        }

        if identifier == "x86_64" || identifier == "arm64" {
            let simulatedIdentifier = ProcessInfo.processInfo.environment["SIMULATOR_MODEL_IDENTIFIER"] ?? ""
            let simulatedName = deviceModelMap[simulatedIdentifier] ?? simulatedIdentifier
            if !simulatedName.isEmpty {
                return "Simulator (\(simulatedName))"
            }
            return "Simulator"
        }

        if let modelName = deviceModelMap[identifier] {
            return modelName
        }

        let baseModel = UIDevice.current.model
        if identifier.isEmpty {
            return baseModel
        }
        return "\(baseModel) (\(identifier))"
    }
}

private let deviceModelMap: [String: String] = [
    "iPhone8,1": "iPhone 6s",
    "iPhone8,2": "iPhone 6s Plus",
    "iPhone8,4": "iPhone SE",
    "iPhone9,1": "iPhone 7",
    "iPhone9,2": "iPhone 7 Plus",
    "iPhone9,3": "iPhone 7",
    "iPhone9,4": "iPhone 7 Plus",
    "iPhone10,1": "iPhone 8",
    "iPhone10,2": "iPhone 8 Plus",
    "iPhone10,3": "iPhone X",
    "iPhone10,4": "iPhone 8",
    "iPhone10,5": "iPhone 8 Plus",
    "iPhone10,6": "iPhone X",
    "iPhone11,2": "iPhone XS",
    "iPhone11,4": "iPhone XS Max",
    "iPhone11,6": "iPhone XS Max",
    "iPhone11,8": "iPhone XR",
    "iPhone12,1": "iPhone 11",
    "iPhone12,3": "iPhone 11 Pro",
    "iPhone12,5": "iPhone 11 Pro Max",
    "iPhone12,8": "iPhone SE (2nd generation)",
    "iPhone13,1": "iPhone 12 mini",
    "iPhone13,2": "iPhone 12",
    "iPhone13,3": "iPhone 12 Pro",
    "iPhone13,4": "iPhone 12 Pro Max",
    "iPhone14,2": "iPhone 13 Pro",
    "iPhone14,3": "iPhone 13 Pro Max",
    "iPhone14,4": "iPhone 13 mini",
    "iPhone14,5": "iPhone 13",
    "iPhone14,6": "iPhone SE (3rd generation)",
    "iPhone14,7": "iPhone 14",
    "iPhone14,8": "iPhone 14 Plus",
    "iPhone15,2": "iPhone 14 Pro",
    "iPhone15,3": "iPhone 14 Pro Max",
    "iPhone15,4": "iPhone 15",
    "iPhone15,5": "iPhone 15 Plus",
    "iPhone16,1": "iPhone 15 Pro",
    "iPhone16,2": "iPhone 15 Pro Max",
    "iPad13,16": "iPad Air (5th generation)",
    "iPad13,17": "iPad Air (5th generation)",
    "iPad13,18": "iPad (10th generation)",
    "iPad13,19": "iPad (10th generation)",
    "iPad14,3": "iPad Pro 11-inch (4th generation)",
    "iPad14,4": "iPad Pro 11-inch (4th generation)",
    "iPad14,5": "iPad Pro 12.9-inch (6th generation)",
    "iPad14,6": "iPad Pro 12.9-inch (6th generation)"
]
