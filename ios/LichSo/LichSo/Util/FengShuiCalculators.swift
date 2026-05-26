import Foundation

enum LoBanMode: String, CaseIterable, Identifiable {
    case doGiaDung = "doGiaDung"
    case banTho = "banTho"
    case giuongTu = "giuongTu"
    
    var id: String { rawValue }
    
    var title: String {
        switch self {
        case .doGiaDung: return "Thước 52.2 cm"
        case .banTho: return "Thước 42.9 cm"
        case .giuongTu: return "Thước 38.8 cm"
        }
    }
    
    var cycleCm: Double {
        switch self {
        case .doGiaDung: return 52.2
        case .banTho: return 42.9
        case .giuongTu: return 38.8
        }
    }
    
    var subtitle: String {
        switch self {
        case .doGiaDung: return "Cửa chính, cổng, cửa phòng"
        case .banTho: return "Bàn thờ, đồ nội thất"
        case .giuongTu: return "Giường, tủ, bàn nhỏ"
        }
    }
}

struct LoBanResult {
    let mode: LoBanMode
    let rawLengthCm: Double
    let normalizedCm: Double
    let zoneIndex: Int
    let zoneName: String
    let isAuspicious: Bool
    let hint: String
}

struct CungPhiResult {
    let cungName: String
    let genderLabel: String
    let groupLabel: String
    let favorableDirections: [String]
    let unfavorableDirections: [String]
    let note: String
}

enum FengShuiCalculators {
    
    static func compassDirectionForDegrees(_ degrees: Double) -> String {
        let normalized = ((degrees.truncatingRemainder(dividingBy: 360.0)) + 360.0).truncatingRemainder(dividingBy: 360.0)
        switch normalized {
        case 337.5..<360.0, 0.0..<22.5:
            return "Bắc"
        case 22.5..<67.5:
            return "Đông Bắc"
        case 67.5..<112.5:
            return "Đông"
        case 112.5..<157.5:
            return "Đông Nam"
        case 157.5..<202.5:
            return "Nam"
        case 202.5..<247.5:
            return "Tây Nam"
        case 247.5..<292.5:
            return "Tây"
        default:
            return "Tây Bắc"
        }
    }
    
    static func evaluateLoBan(lengthCm: Double, mode: LoBanMode) -> LoBanResult {
        let sanitizedLength = max(0.0, lengthCm)
        let cycle = mode.cycleCm
        let normalized = cycle <= 0.0 ? 0.0 : ((sanitizedLength.truncatingRemainder(dividingBy: cycle)) + cycle).truncatingRemainder(dividingBy: cycle)
        let zoneSize = cycle / 8.0
        let zoneIndex = zoneSize > 0.0 ? min(Int(normalized / zoneSize), 7) : 0
        
        let zoneNames = [
            "Tài Lộc",
            "Bệnh Tật",
            "Ly Tán",
            "Nghĩa Đức",
            "Quan Lộc",
            "Kiếp Sát",
            "Họa Hại",
            "Bản Mệnh"
        ]
        
        let zoneName = zoneIndex < zoneNames.count ? zoneNames[zoneIndex] : zoneNames[0]
        let isAuspicious = [0, 3, 4, 7].contains(zoneIndex)
        
        let hint: String
        switch zoneIndex {
        case 0: hint = "Thiên về tài khí, dễ dùng cho kích thước cát lợi."
        case 1: hint = "Nghiêng về bệnh tật, nên cân nhắc chỉnh lại."
        case 2: hint = "Dễ sinh ly tán, phù hợp kiểm tra lại số đo."
        case 3: hint = "Nghiêng về nghĩa khí, thường được xem là khá ổn."
        case 4: hint = "Thiên về quan lộc, hợp với kích thước cần sự bền vững."
        case 5: hint = "Kiếp sát, nên hạn chế nếu đang chọn số đo chính."
        case 6: hint = "Họa hại, nên xem lại nếu đang thiết kế chính xác."
        default: hint = "Bản mệnh, thường là mốc cân bằng trong chu kỳ thước."
        }
        
        return LoBanResult(
            mode: mode,
            rawLengthCm: sanitizedLength,
            normalizedCm: normalized,
            zoneIndex: zoneIndex,
            zoneName: zoneName,
            isAuspicious: isAuspicious,
            hint: hint
        )
    }
    
    static func calculateCungPhi(lunarYear: Int, gender: String) -> CungPhiResult {
        let cungNames = ["Khảm", "Ly", "Cấn", "Đoài", "Càn", "Khôn", "Tốn", "Chấn", "Trung Cung"]
        let sum = digitSum(lunarYear)
        let isMale = gender != "Nữ"
        
        let cungIndex: Int
        if isMale {
            let value = (11 - sum % 9) % 9
            cungIndex = value == 0 ? 8 : value - 1
        } else {
            let value = (sum + 4) % 9
            cungIndex = value == 0 ? 8 : value - 1
        }
        
        let cungName = cungIndex < cungNames.count ? cungNames[cungIndex] : "Khảm"
        let genderLabel = isMale ? "Nam" : "Nữ"
        let isEastGroup = ["Khảm", "Ly", "Chấn", "Tốn"].contains(cungName)
        let groupLabel = cungName == "Trung Cung" ? "Chưa xác định" : (isEastGroup ? "Đông tứ mệnh" : "Tây tứ mệnh")
        
        let favorableDirections = isEastGroup ? ["Bắc", "Đông", "Nam", "Đông Nam"] : ["Tây", "Tây Bắc", "Tây Nam", "Đông Bắc"]
        let unfavorableDirections = isEastGroup ? ["Tây", "Tây Bắc", "Tây Nam", "Đông Bắc"] : ["Bắc", "Đông", "Nam", "Đông Nam"]
        
        let note = cungName == "Trung Cung" ?
            "Số đo này rơi vào trường hợp đặc biệt, nên xem lại thông tin sinh để phân tích kỹ hơn." :
            "Màn này dùng quy tắc Bát trạch cơ bản: ưu tiên một trong các hướng thuộc nhóm của bạn."
            
        return CungPhiResult(
            cungName: cungName,
            genderLabel: genderLabel,
            groupLabel: groupLabel,
            favorableDirections: favorableDirections,
            unfavorableDirections: unfavorableDirections,
            note: note
        )
    }
    
    private static func digitSum(_ value: Int) -> Int {
        var sum = 0
        var number = abs(value)
        while number > 0 {
            sum += number % 10
            number /= 10
        }
        while sum >= 10 {
            var reduced = 0
            var current = sum
            while current > 0 {
                reduced += current % 10
                current /= 10
            }
            sum = reduced
        }
        return sum
    }
}
