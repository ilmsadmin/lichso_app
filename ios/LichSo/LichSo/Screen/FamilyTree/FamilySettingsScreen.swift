import SwiftUI
import SwiftData
import CoreImage.CIFilterBuiltins
import UserNotifications

// ═══════════════════════════════════════════
// Family Settings Screen — Matches screen-family-settings.html
// Full CRUD for family settings, display, notifications,
// share/export, sync, danger zone.
// Uses REAL data from SwiftData via FamilyTreeViewModel
// ═══════════════════════════════════════════

private var PrimaryRed: Color { LSTheme.primary }
private var DeepRed: Color { LSTheme.deepRed }
private var GoldAccent: Color { LSTheme.gold }
private var SurfaceBg: Color { LSTheme.bg }
private var SurfaceContainer: Color { LSTheme.surfaceContainer }
private var SurfaceContainerHigh: Color { LSTheme.surfaceContainerHigh }
private var PrimaryContainer: Color { LSTheme.primaryContainer }
private var TextMain: Color { LSTheme.textPrimary }
private var TextSub: Color { LSTheme.textSecondary }
private var TextDim: Color { LSTheme.textTertiary }
private var OutlineVariant: Color { LSTheme.outlineVariant }

struct FamilySettingsScreen: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    @ObservedObject var viewModel: FamilyTreeViewModel

    @State private var showEditNameAlert = false
    @State private var showEditCrestAlert = false
    @State private var showEditHometownAlert = false
    @State private var showDeleteAllAlert = false
    @State private var showTreeModePicker = false
    @State private var showDaysBeforePicker = false
    @State private var showBackupResult = false
    @State private var backupResultMessage = ""
    @State private var showQRCode = false
    @State private var qrImage: UIImage?
    @State private var isExporting = false

    @State private var editText = ""

    // ── Share & Export ──

    private func presentShareSheet(items: [Any]) {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let root = windowScene.windows.first?.rootViewController else { return }
        let av = UIActivityViewController(activityItems: items, applicationActivities: nil)
        root.present(av, animated: true)
    }

    private func exportPDF() {
        isExporting = true
        let settings = viewModel.settings
        let familyName = settings?.familyName ?? "Gia phả"
        let crest = settings?.familyCrest ?? "GP"
        let hometown = settings?.hometown ?? ""
        let membersData = viewModel.members
        let memorialsData = viewModel.memorials

        DispatchQueue.global(qos: .userInitiated).async {
            let pageRect = CGRect(x: 0, y: 0, width: 595, height: 842) // A4
            let renderer = UIGraphicsPDFRenderer(bounds: pageRect)
            let data = renderer.pdfData { ctx in
                ctx.beginPage()
                var y: CGFloat = 40

                // Title
                let titleAttr: [NSAttributedString.Key: Any] = [
                    .font: UIFont.boldSystemFont(ofSize: 24),
                    .foregroundColor: UIColor(red: 0.72, green: 0.11, blue: 0.11, alpha: 1)
                ]
                let title = "📜 \(familyName)"
                title.draw(at: CGPoint(x: 40, y: y), withAttributes: titleAttr)
                y += 40

                // Meta
                let metaAttr: [NSAttributedString.Key: Any] = [
                    .font: UIFont.systemFont(ofSize: 12),
                    .foregroundColor: UIColor.darkGray
                ]
                let meta = "Biểu tượng: \(crest) | Quê quán: \(hometown.isEmpty ? "—" : hometown) | Xuất ngày: \(Self.todayString())"
                meta.draw(at: CGPoint(x: 40, y: y), withAttributes: metaAttr)
                y += 30

                // Stats
                let statAttr: [NSAttributedString.Key: Any] = [
                    .font: UIFont.systemFont(ofSize: 14, weight: .medium),
                    .foregroundColor: UIColor.black
                ]
                let gens = Set(membersData.map { $0.generation }).count
                "Thế hệ: \(gens) · Thành viên: \(membersData.count) · Ngày giỗ: \(memorialsData.count)".draw(at: CGPoint(x: 40, y: y), withAttributes: statAttr)
                y += 30

                // Separator
                UIColor.lightGray.setStroke()
                let line = UIBezierPath()
                line.move(to: CGPoint(x: 40, y: y))
                line.addLine(to: CGPoint(x: pageRect.width - 40, y: y))
                line.lineWidth = 0.5
                line.stroke()
                y += 20

                // Members by generation
                let headerAttr: [NSAttributedString.Key: Any] = [
                    .font: UIFont.boldSystemFont(ofSize: 16),
                    .foregroundColor: UIColor(red: 0.36, green: 0.15, blue: 0.07, alpha: 1)
                ]
                let bodyAttr: [NSAttributedString.Key: Any] = [
                    .font: UIFont.systemFont(ofSize: 12),
                    .foregroundColor: UIColor.darkGray
                ]

                let grouped = Dictionary(grouping: membersData) { $0.generation }
                for gen in grouped.keys.sorted() {
                    if y > pageRect.height - 60 {
                        ctx.beginPage()
                        y = 40
                    }
                    "Đời \(gen)".draw(at: CGPoint(x: 40, y: y), withAttributes: headerAttr)
                    y += 24
                    for m in (grouped[gen] ?? []).sorted(by: { $0.name < $1.name }) {
                        if y > pageRect.height - 40 {
                            ctx.beginPage()
                            y = 40
                        }
                        let years = [m.birthYear.map { "\($0)" }, m.deathYear.map { "†\($0)" }].compactMap { $0 }.joined(separator: " – ")
                        let memberLine = "  • \(m.name) — \(m.role)\(years.isEmpty ? "" : " (\(years))")"
                        memberLine.draw(at: CGPoint(x: 50, y: y), withAttributes: bodyAttr)
                        y += 18
                    }
                    y += 10
                }

                // Memorials
                if !memorialsData.isEmpty {
                    if y > pageRect.height - 80 {
                        ctx.beginPage()
                        y = 40
                    }
                    y += 10
                    "Ngày giỗ".draw(at: CGPoint(x: 40, y: y), withAttributes: headerAttr)
                    y += 24
                    for mem in memorialsData {
                        if y > pageRect.height - 40 {
                            ctx.beginPage()
                            y = 40
                        }
                        "  • \(mem.memberName) (\(mem.relation)) — \(mem.lunarDay)/\(mem.lunarMonth) âm lịch".draw(at: CGPoint(x: 50, y: y), withAttributes: bodyAttr)
                        y += 18
                    }
                }
            }

            DispatchQueue.main.async {
                isExporting = false
                let tmpURL = FileManager.default.temporaryDirectory.appendingPathComponent("\(familyName).pdf")
                try? data.write(to: tmpURL)
                presentShareSheet(items: [tmpURL])
            }
        }
    }

    private func exportImage() {
        isExporting = true
        let settings = viewModel.settings
        let familyName = settings?.familyName ?? "Gia phả"
        let membersData = viewModel.members
        let memorialsData = viewModel.memorials
        let gens = Set(membersData.map { $0.generation }).count

        DispatchQueue.global(qos: .userInitiated).async {
            let width: CGFloat = 800
            let height: CGFloat = 400 + CGFloat(membersData.count) * 22
            let renderer = UIGraphicsImageRenderer(size: CGSize(width: width, height: max(height, 500)))
            let image = renderer.image { ctx in
                // Background
                UIColor.white.setFill()
                ctx.fill(CGRect(x: 0, y: 0, width: width, height: max(height, 500)))

                var y: CGFloat = 30
                let titleAttr: [NSAttributedString.Key: Any] = [
                    .font: UIFont.boldSystemFont(ofSize: 28),
                    .foregroundColor: UIColor(red: 0.72, green: 0.11, blue: 0.11, alpha: 1)
                ]
                "📜 \(familyName)".draw(at: CGPoint(x: 30, y: y), withAttributes: titleAttr)
                y += 44

                let statAttr: [NSAttributedString.Key: Any] = [
                    .font: UIFont.systemFont(ofSize: 16, weight: .semibold),
                    .foregroundColor: UIColor.darkGray
                ]
                "\(gens) thế hệ · \(membersData.count) thành viên · \(memorialsData.count) ngày giỗ".draw(at: CGPoint(x: 30, y: y), withAttributes: statAttr)
                y += 36

                let headerAttr: [NSAttributedString.Key: Any] = [.font: UIFont.boldSystemFont(ofSize: 18), .foregroundColor: UIColor.brown]
                let bodyAttr: [NSAttributedString.Key: Any] = [.font: UIFont.systemFont(ofSize: 14), .foregroundColor: UIColor.darkGray]

                let grouped = Dictionary(grouping: membersData) { $0.generation }
                for gen in grouped.keys.sorted() {
                    "Đời \(gen)".draw(at: CGPoint(x: 30, y: y), withAttributes: headerAttr)
                    y += 26
                    for m in (grouped[gen] ?? []).sorted(by: { $0.name < $1.name }) {
                        "  • \(m.name) — \(m.role)".draw(at: CGPoint(x: 40, y: y), withAttributes: bodyAttr)
                        y += 20
                    }
                    y += 8
                }

                // Footer
                let footerAttr: [NSAttributedString.Key: Any] = [.font: UIFont.italicSystemFont(ofSize: 11), .foregroundColor: UIColor.lightGray]
                "Lịch Số — by Zenix Labs · \(Self.todayString())".draw(at: CGPoint(x: 30, y: y + 10), withAttributes: footerAttr)
            }

            DispatchQueue.main.async {
                isExporting = false
                presentShareSheet(items: [image])
            }
        }
    }

    private func shareFamilyLink() {
        let settings = viewModel.settings
        let familyName = settings?.familyName ?? "Gia phả"
        let gens = viewModel.totalGenerations
        let total = viewModel.totalMembers
        let memos = viewModel.totalMemorials
        let text = """
        📜 \(familyName)
        \(gens) thế hệ · \(total) thành viên · \(memos) ngày giỗ
        Được chia sẻ từ ứng dụng Lịch Số — by Zenix Labs
        https://apps.apple.com/app/id6740048518
        """
        presentShareSheet(items: [text])
    }

    private func generateQR() {
        let settings = viewModel.settings
        let data = "LICHSO_FAMILY|name:\(settings?.familyName ?? "")|gens:\(viewModel.totalGenerations)|members:\(viewModel.totalMembers)|hometown:\(settings?.hometown ?? "")|memorials:\(viewModel.totalMemorials)"
        let context = CIContext()
        let filter = CIFilter.qrCodeGenerator()
        filter.message = Data(data.utf8)
        filter.correctionLevel = "M"
        if let outputImage = filter.outputImage {
            let scaled = outputImage.transformed(by: CGAffineTransform(scaleX: 10, y: 10))
            if let cgImage = context.createCGImage(scaled, from: scaled.extent) {
                qrImage = UIImage(cgImage: cgImage)
                showQRCode = true
            }
        }
    }

    // ── iCloud Backup / Restore ──

    private func performBackup() {
        isExporting = true
        let membersData = viewModel.members
        let memorialsData = viewModel.memorials
        let settingsData = viewModel.settings

        DispatchQueue.global(qos: .userInitiated).async {
            var json: [String: Any] = [:]
            json["version"] = 1
            json["exportDate"] = ISO8601DateFormatter().string(from: Date())

            // Settings
            if let s = settingsData {
                json["settings"] = [
                    "familyName": s.familyName,
                    "familyCrest": s.familyCrest,
                    "hometown": s.hometown,
                    "treeDisplayMode": s.treeDisplayMode,
                    "treeTheme": s.treeTheme,
                    "showAvatar": s.showAvatar,
                    "showYears": s.showYears,
                    "remindMemorial": s.remindMemorial,
                    "remindBirthday": s.remindBirthday,
                    "remindDaysBefore": s.remindDaysBefore
                ]
            }

            // Members
            json["members"] = membersData.map { m -> [String: Any] in
                var d: [String: Any] = [
                    "id": m.id, "name": m.name, "role": m.role,
                    "gender": m.gender, "generation": m.generation,
                    "isSelf": m.isSelf, "isElder": m.isElder,
                    "emoji": m.emoji, "spouseIds": m.spouseIds,
                    "spouseOrder": m.spouseOrder, "parentIds": m.parentIds
                ]
                if let v = m.birthYear { d["birthYear"] = v }
                if let v = m.deathYear { d["deathYear"] = v }
                if let v = m.birthDateLunar { d["birthDateLunar"] = v }
                if let v = m.deathDateLunar { d["deathDateLunar"] = v }
                if let v = m.canChi { d["canChi"] = v }
                if let v = m.menh { d["menh"] = v }
                if let v = m.hometown { d["hometown"] = v }
                if let v = m.occupation { d["occupation"] = v }
                if let v = m.note { d["note"] = v }
                if let v = m.zodiacEmoji { d["zodiacEmoji"] = v }
                if let v = m.menhEmoji { d["menhEmoji"] = v }
                if let v = m.hanhEmoji { d["hanhEmoji"] = v }
                if let v = m.menhDetail { d["menhDetail"] = v }
                if let v = m.zodiacName { d["zodiacName"] = v }
                if let v = m.menhName { d["menhName"] = v }
                return d
            }

            // Memorials
            json["memorials"] = memorialsData.map { m -> [String: Any] in
                ["id": m.id, "memberId": m.memberId, "memberName": m.memberName,
                 "relation": m.relation, "lunarDay": m.lunarDay,
                 "lunarMonth": m.lunarMonth, "lunarLeap": m.lunarLeap,
                 "note": m.note ?? "", "remindBefore3Days": m.remindBefore3Days,
                 "remindBefore1Day": m.remindBefore1Day]
            }

            // Write to iCloud or local
            guard let jsonData = try? JSONSerialization.data(withJSONObject: json, options: [.prettyPrinted, .sortedKeys]) else {
                DispatchQueue.main.async {
                    isExporting = false
                    backupResultMessage = "Lỗi tạo dữ liệu sao lưu."
                    showBackupResult = true
                }
                return
            }

            // Save to iCloud Documents if available, else local Documents
            let fileManager = FileManager.default
            let fileName = "LichSo_GiaPha_Backup.json"
            var saveURL: URL

            if let iCloudURL = fileManager.url(forUbiquityContainerIdentifier: nil)?
                .appendingPathComponent("Documents") {
                try? fileManager.createDirectory(at: iCloudURL, withIntermediateDirectories: true)
                saveURL = iCloudURL.appendingPathComponent(fileName)
            } else {
                let localDir = fileManager.urls(for: .documentDirectory, in: .userDomainMask).first!
                saveURL = localDir.appendingPathComponent(fileName)
            }

            do {
                try jsonData.write(to: saveURL, options: .atomic)
                // Also save metadata to KVStore
                let store = NSUbiquitousKeyValueStore.default
                store.set(membersData.count, forKey: "family_backup_count")
                store.set(Date().timeIntervalSince1970, forKey: "family_backup_date")
                store.synchronize()

                DispatchQueue.main.async {
                    isExporting = false
                    backupResultMessage = "✅ Đã sao lưu \(membersData.count) thành viên, \(memorialsData.count) ngày giỗ.\nFile: \(saveURL.lastPathComponent)"
                    showBackupResult = true
                }
            } catch {
                DispatchQueue.main.async {
                    isExporting = false
                    backupResultMessage = "Lỗi ghi file: \(error.localizedDescription)"
                    showBackupResult = true
                }
            }
        }
    }

    private func performRestore() {
        isExporting = true

        DispatchQueue.global(qos: .userInitiated).async {
            let fileManager = FileManager.default
            let fileName = "LichSo_GiaPha_Backup.json"
            var readURL: URL?

            if let iCloudURL = fileManager.url(forUbiquityContainerIdentifier: nil)?
                .appendingPathComponent("Documents").appendingPathComponent(fileName),
               fileManager.fileExists(atPath: iCloudURL.path) {
                readURL = iCloudURL
            } else {
                let localDir = fileManager.urls(for: .documentDirectory, in: .userDomainMask).first!
                let localURL = localDir.appendingPathComponent(fileName)
                if fileManager.fileExists(atPath: localURL.path) {
                    readURL = localURL
                }
            }

            guard let url = readURL,
                  let data = try? Data(contentsOf: url),
                  let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] else {
                DispatchQueue.main.async {
                    isExporting = false
                    backupResultMessage = "Chưa có bản sao lưu nào.\nHãy sao lưu trước khi khôi phục."
                    showBackupResult = true
                }
                return
            }

            DispatchQueue.main.async {
                restoreFromJSON(json)
                isExporting = false
            }
        }
    }

    private func restoreFromJSON(_ json: [String: Any]) {
        let ctx = modelContext

        // Delete existing data
        try? ctx.delete(model: FamilyMemberEntity.self)
        try? ctx.delete(model: MemorialDayEntity.self)

        var memberCount = 0
        var memorialCount = 0

        // Restore settings
        if let sDict = json["settings"] as? [String: Any] {
            if let existing = try? ctx.fetch(FetchDescriptor<FamilySettingsEntity>()).first {
                existing.familyName = sDict["familyName"] as? String ?? existing.familyName
                existing.familyCrest = sDict["familyCrest"] as? String ?? existing.familyCrest
                existing.hometown = sDict["hometown"] as? String ?? existing.hometown
                existing.treeDisplayMode = sDict["treeDisplayMode"] as? String ?? existing.treeDisplayMode
                existing.showAvatar = sDict["showAvatar"] as? Bool ?? existing.showAvatar
                existing.showYears = sDict["showYears"] as? Bool ?? existing.showYears
                existing.remindMemorial = sDict["remindMemorial"] as? Bool ?? existing.remindMemorial
                existing.remindBirthday = sDict["remindBirthday"] as? Bool ?? existing.remindBirthday
                existing.remindDaysBefore = sDict["remindDaysBefore"] as? Int ?? existing.remindDaysBefore
            }
        }

        // Restore members
        if let membersArray = json["members"] as? [[String: Any]] {
            for d in membersArray {
                let m = FamilyMemberEntity(
                    id: d["id"] as? String ?? UUID().uuidString,
                    name: d["name"] as? String ?? "",
                    role: d["role"] as? String ?? "",
                    gender: d["gender"] as? String ?? "MALE",
                    generation: d["generation"] as? Int ?? 1,
                    birthYear: d["birthYear"] as? Int,
                    deathYear: d["deathYear"] as? Int,
                    birthDateLunar: d["birthDateLunar"] as? String,
                    deathDateLunar: d["deathDateLunar"] as? String,
                    canChi: d["canChi"] as? String,
                    menh: d["menh"] as? String,
                    zodiacEmoji: d["zodiacEmoji"] as? String,
                    menhEmoji: d["menhEmoji"] as? String,
                    hanhEmoji: d["hanhEmoji"] as? String,
                    menhDetail: d["menhDetail"] as? String,
                    zodiacName: d["zodiacName"] as? String,
                    menhName: d["menhName"] as? String,
                    hometown: d["hometown"] as? String,
                    occupation: d["occupation"] as? String,
                    isSelf: d["isSelf"] as? Bool ?? false,
                    isElder: d["isElder"] as? Bool ?? false,
                    emoji: d["emoji"] as? String ?? "👤",
                    spouseIds: d["spouseIds"] as? String ?? "",
                    spouseOrder: d["spouseOrder"] as? Int ?? 0,
                    parentIds: d["parentIds"] as? String ?? "",
                    note: d["note"] as? String
                )
                ctx.insert(m)
                memberCount += 1
            }
        }

        // Restore memorials
        if let memorialsArray = json["memorials"] as? [[String: Any]] {
            for d in memorialsArray {
                let m = MemorialDayEntity(
                    id: d["id"] as? String ?? UUID().uuidString,
                    memberId: d["memberId"] as? String ?? "",
                    memberName: d["memberName"] as? String ?? "",
                    relation: d["relation"] as? String ?? "",
                    lunarDay: d["lunarDay"] as? Int ?? 1,
                    lunarMonth: d["lunarMonth"] as? Int ?? 1,
                    lunarLeap: d["lunarLeap"] as? Int ?? 0,
                    note: (d["note"] as? String)?.isEmpty == true ? nil : d["note"] as? String,
                    remindBefore3Days: d["remindBefore3Days"] as? Bool ?? true,
                    remindBefore1Day: d["remindBefore1Day"] as? Bool ?? true
                )
                ctx.insert(m)
                memorialCount += 1
            }
        }

        try? ctx.save()
        viewModel.loadAll()

        backupResultMessage = "✅ Đã khôi phục \(memberCount) thành viên, \(memorialCount) ngày giỗ."
        showBackupResult = true
    }

    // ── Notifications ──

    private func scheduleMemorialNotifications() {
        let settings = viewModel.settings
        let daysBefore = settings?.remindDaysBefore ?? 3
        let center = UNUserNotificationCenter.current()

        // Remove old memorial notifications
        center.getPendingNotificationRequests { requests in
            let ids = requests.filter { $0.identifier.hasPrefix("memorial_") || $0.identifier.hasPrefix("birthday_") }.map(\.identifier)
            center.removePendingNotificationRequests(withIdentifiers: ids)
        }

        let cal = Calendar.current
        let today = Date()

        // Schedule memorial reminders
        if settings?.remindMemorial == true {
            for memorial in viewModel.memorials {
                let solar = viewModel.solarDateForMemorial(memorial)
                var comps = DateComponents()
                comps.day = solar.day
                comps.month = solar.month
                comps.year = solar.year
                guard let memorialDate = cal.date(from: comps),
                      let reminderDate = cal.date(byAdding: .day, value: -daysBefore, to: memorialDate),
                      reminderDate > today else { continue }

                let content = UNMutableNotificationContent()
                content.title = "🕯 Ngày giỗ \(memorial.memberName)"
                content.body = "Còn \(daysBefore) ngày nữa — \(memorial.lunarDay)/\(memorial.lunarMonth) ÂL (\(memorial.relation))"
                content.sound = .default
                content.categoryIdentifier = "memorial"

                var triggerComps = cal.dateComponents([.year, .month, .day], from: reminderDate)
                triggerComps.hour = 8
                let trigger = UNCalendarNotificationTrigger(dateMatching: triggerComps, repeats: false)
                let request = UNNotificationRequest(identifier: "memorial_\(memorial.id)", content: content, trigger: trigger)
                center.add(request)
            }
        }

        // Schedule birthday reminders
        if settings?.remindBirthday == true {
            for member in viewModel.members {
                guard let birthYear = member.birthYear else { continue }
                let currentYear = cal.component(.year, from: today)

                // If birthDateLunar is set, convert; otherwise use birthYear as solar estimate
                if let lunarStr = member.birthDateLunar, !lunarStr.isEmpty {
                    let parts = lunarStr.split(separator: "/").compactMap { Int($0) }
                    if parts.count >= 2 {
                        let solar = LunarCalendarUtil.convertLunar2Solar(lunarDay: parts[0], lunarMonth: parts[1], lunarYear: currentYear, lunarLeap: 0)
                        var comps = DateComponents()
                        comps.day = solar.0
                        comps.month = solar.1
                        comps.year = solar.2
                        guard let bDate = cal.date(from: comps),
                              let rDate = cal.date(byAdding: .day, value: -daysBefore, to: bDate),
                              rDate > today else { continue }
                        let age = currentYear - birthYear
                        let content = UNMutableNotificationContent()
                        content.title = "🎂 Sinh nhật \(member.name)"
                        content.body = "Còn \(daysBefore) ngày nữa — tròn \(age) tuổi"
                        content.sound = .default
                        var tc = cal.dateComponents([.year, .month, .day], from: rDate)
                        tc.hour = 8
                        let trigger = UNCalendarNotificationTrigger(dateMatching: tc, repeats: false)
                        let request = UNNotificationRequest(identifier: "birthday_\(member.id)", content: content, trigger: trigger)
                        center.add(request)
                    }
                }
            }
        }
    }

    // ── Helpers ──

    private static func todayString() -> String {
        let f = DateFormatter()
        f.dateFormat = "dd/MM/yyyy"
        return f.string(from: Date())
    }

    var body: some View {
        VStack(spacing: 0) {
            // ═══ HEADER ═══
            HStack(spacing: 12) {
                Button { dismiss() } label: {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.white)
                        .frame(width: 40, height: 40)
                        .background(Color.white.opacity(0.15))
                        .clipShape(Circle())
                }
                Text("Cài đặt gia phả")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.white)
                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.top, 12)
            .padding(.bottom, 16)
            .background(
                LinearGradient(
                    colors: [Color(red: 0.773, green: 0.157, blue: 0.157),
                             Color(red: 0.545, green: 0, blue: 0)],
                    startPoint: .topLeading, endPoint: .bottomTrailing
                )
            )

            ScrollView(.vertical, showsIndicators: false) {
                VStack(alignment: .leading, spacing: 0) {
                    // ═══ FAMILY BANNER ═══
                    FamilyBanner(settings: viewModel.settings)
                        .padding(.horizontal, 16)
                        .padding(.top, 16)
                        .padding(.bottom, 16)

                    // ═══ STAT ROW ═══
                    StatRow(
                        generations: viewModel.totalGenerations,
                        members: viewModel.totalMembers,
                        memorials: viewModel.totalMemorials
                    )
                    .padding(.horizontal, 16)
                    .padding(.bottom, 12)

                    // ═══ EDIT SECTION ═══
                    SectionLabel(icon: "square.and.pencil", text: "CHỈNH SỬA")
                        .padding(.horizontal, 16)

                    SettingsGroup {
                        SettingsRow(
                            icon: "person.text.rectangle.fill",
                            iconBg: Color(hex: "EFEBE9"),
                            iconFg: Color(hex: "5D4037"),
                            title: "Tên dòng họ",
                            desc: "Đổi tên hiển thị gia phả",
                            value: viewModel.settings?.familyName
                        ) {
                            editText = viewModel.settings?.familyName ?? ""
                            showEditNameAlert = true
                        }

                        Divider().padding(.leading, 64)

                        SettingsRow(
                            icon: "shield.fill",
                            iconBg: Color(hex: "FFF8E1"),
                            iconFg: Color(hex: "F57F17"),
                            title: "Biểu tượng dòng họ",
                            desc: "Chỉnh sửa ký hiệu Family Crest",
                            value: viewModel.settings?.familyCrest
                        ) {
                            editText = viewModel.settings?.familyCrest ?? ""
                            showEditCrestAlert = true
                        }

                        Divider().padding(.leading, 64)

                        SettingsRow(
                            icon: "mappin.and.ellipse",
                            iconBg: Color(hex: "F3E5F5"),
                            iconFg: Color(hex: "7B1FA2"),
                            title: "Quê quán gốc",
                            desc: "Quê gốc của dòng họ",
                            value: viewModel.settings?.hometown.isEmpty == false ? viewModel.settings?.hometown : "Chưa cài"
                        ) {
                            editText = viewModel.settings?.hometown ?? ""
                            showEditHometownAlert = true
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.bottom, 12)

                    // ═══ DISPLAY SECTION ═══
                    SectionLabel(icon: "eye", text: "HIỂN THỊ")
                        .padding(.horizontal, 16)

                    SettingsGroup {
                        SettingsRow(
                            icon: "arrow.triangle.branch",
                            iconBg: Color(hex: "E3F2FD"),
                            iconFg: Color(hex: "1565C0"),
                            title: "Kiểu hiển thị cây",
                            desc: "Dọc, ngang hoặc hình quạt",
                            value: treeDisplayModeLabel
                        ) {
                            showTreeModePicker = true
                        }

                        Divider().padding(.leading, 64)

                        SettingsToggle(
                            icon: "photo.fill",
                            iconBg: Color(hex: "E8F5E9"),
                            iconFg: Color(hex: "2E7D32"),
                            title: "Hiện ảnh đại diện",
                            desc: "Hiển thị ảnh trên cây gia phả",
                            isOn: Binding(
                                get: { viewModel.settings?.showAvatar ?? true },
                                set: { viewModel.settings?.showAvatar = $0; viewModel.updateSettings() }
                            )
                        )

                        Divider().padding(.leading, 64)

                        SettingsToggle(
                            icon: "calendar",
                            iconBg: Color(hex: "FFF8E1"),
                            iconFg: Color(hex: "F57F17"),
                            title: "Hiện năm sinh/mất",
                            desc: "Hiển thị năm trên node",
                            isOn: Binding(
                                get: { viewModel.settings?.showYears ?? true },
                                set: { viewModel.settings?.showYears = $0; viewModel.updateSettings() }
                            )
                        )
                    }
                    .padding(.horizontal, 16)
                    .padding(.bottom, 12)

                    // ═══ NOTIFICATIONS SECTION ═══
                    SectionLabel(icon: "bell.fill", text: "THÔNG BÁO")
                        .padding(.horizontal, 16)

                    SettingsGroup {
                        SettingsToggle(
                            icon: "flame.fill",
                            iconBg: Color(hex: "FFEBEE"),
                            iconFg: Color(hex: "C62828"),
                            title: "Nhắc ngày giỗ",
                            desc: "Nhắc nhở trước ngày giỗ",
                            isOn: Binding(
                                get: { viewModel.settings?.remindMemorial ?? true },
                                set: { val in
                                    viewModel.settings?.remindMemorial = val
                                    viewModel.updateSettings()
                                    scheduleMemorialNotifications()
                                }
                            )
                        )

                        Divider().padding(.leading, 64)

                        SettingsToggle(
                            icon: "gift.fill",
                            iconBg: Color(hex: "E3F2FD"),
                            iconFg: Color(hex: "1565C0"),
                            title: "Nhắc sinh nhật",
                            desc: "Nhắc sinh nhật thành viên",
                            isOn: Binding(
                                get: { viewModel.settings?.remindBirthday ?? true },
                                set: { val in
                                    viewModel.settings?.remindBirthday = val
                                    viewModel.updateSettings()
                                    scheduleMemorialNotifications()
                                }
                            )
                        )

                        Divider().padding(.leading, 64)

                        SettingsRow(
                            icon: "clock.fill",
                            iconBg: Color(hex: "FFF8E1"),
                            iconFg: Color(hex: "F57F17"),
                            title: "Thời gian nhắc trước",
                            desc: "Nhắc trước ngày giỗ bao lâu",
                            value: "\(viewModel.settings?.remindDaysBefore ?? 3) ngày"
                        ) {
                            showDaysBeforePicker = true
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.bottom, 12)

                    // ═══ SHARE & EXPORT ═══
                    SectionLabel(icon: "square.and.arrow.up", text: "CHIA SẺ & XUẤT")
                        .padding(.horizontal, 16)

                    ShareExportGrid(
                        onPDF: exportPDF,
                        onImage: exportImage,
                        onLink: shareFamilyLink,
                        onQR: generateQR
                    )
                        .padding(.horizontal, 16)
                        .padding(.bottom, 12)

                    // ═══ SYNC ═══
                    SectionLabel(icon: "arrow.triangle.2.circlepath", text: "ĐỒNG BỘ")
                        .padding(.horizontal, 16)

                    SettingsGroup {
                        SettingsRow(
                            icon: "icloud.and.arrow.up.fill",
                            iconBg: Color(hex: "E8F5E9"),
                            iconFg: Color(hex: "2E7D32"),
                            title: "Sao lưu lên iCloud",
                            desc: lastBackupDescription
                        ) {
                            performBackup()
                        }

                        Divider().padding(.leading, 64)

                        SettingsRow(
                            icon: "icloud.and.arrow.down.fill",
                            iconBg: Color(hex: "E3F2FD"),
                            iconFg: Color(hex: "1565C0"),
                            title: "Khôi phục từ iCloud",
                            desc: "Tải bản sao lưu trước đó"
                        ) {
                            performRestore()
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.bottom, 12)

                    // ═══ DANGER ZONE ═══
                    SectionLabel(icon: "exclamationmark.triangle.fill", text: "NGUY HIỂM")
                        .padding(.horizontal, 16)

                    Button { showDeleteAllAlert = true } label: {
                        HStack(spacing: 8) {
                            Image(systemName: "trash.fill")
                                .font(.system(size: 18))
                            Text("Xoá toàn bộ gia phả")
                                .font(.system(size: 14, weight: .semibold))
                        }
                        .foregroundColor(Color(hex: "C62828"))
                        .frame(maxWidth: .infinity)
                        .padding(14)
                        .background(Color(hex: "FFF5F5"))
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color(hex: "FFCDD2"), lineWidth: 1.5))
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 8)

                    Spacer().frame(height: 32)
                }
            }
        }
        .background(SurfaceBg)
        .navigationBarHidden(true)
        .onAppear {
            viewModel.setup(context: modelContext)
        }
        // ── ALERTS ──
        .alert("Tên dòng họ", isPresented: $showEditNameAlert) {
            TextField("Nhập tên", text: $editText)
            Button("Huỷ", role: .cancel) {}
            Button("Lưu") {
                guard !editText.trimmingCharacters(in: .whitespaces).isEmpty else { return }
                viewModel.settings?.familyName = editText.trimmingCharacters(in: .whitespaces)
                viewModel.updateSettings()
                #if DEBUG
                print("✅ Saved familyName: \(viewModel.settings?.familyName ?? "nil")")
                #endif
            }
        }
        .alert("Biểu tượng", isPresented: $showEditCrestAlert) {
            TextField("VD: Ng, Tr, Lê...", text: $editText)
            Button("Huỷ", role: .cancel) {}
            Button("Lưu") {
                guard !editText.trimmingCharacters(in: .whitespaces).isEmpty else { return }
                viewModel.settings?.familyCrest = editText.trimmingCharacters(in: .whitespaces)
                viewModel.updateSettings()
                #if DEBUG
                print("✅ Saved familyCrest: \(viewModel.settings?.familyCrest ?? "nil")")
                #endif
            }
        }
        .alert("Quê quán gốc", isPresented: $showEditHometownAlert) {
            TextField("VD: Hà Nam, Nghệ An...", text: $editText)
            Button("Huỷ", role: .cancel) {}
            Button("Lưu") {
                viewModel.settings?.hometown = editText.trimmingCharacters(in: .whitespaces)
                viewModel.updateSettings()
                #if DEBUG
                print("✅ Saved hometown: \(viewModel.settings?.hometown ?? "nil")")
                #endif
            }
        }
        .alert("Xoá toàn bộ gia phả", isPresented: $showDeleteAllAlert) {
            Button("Huỷ", role: .cancel) {}
            Button("Xoá", role: .destructive) {
                viewModel.deleteAllFamilyData()
            }
        } message: {
            Text("Hành động này sẽ xoá tất cả dữ liệu gia phả và không thể khôi phục. Bạn chắc chắn?")
        }
        .alert("Đồng bộ iCloud", isPresented: $showBackupResult) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(backupResultMessage)
        }
        .confirmationDialog("Kiểu hiển thị cây", isPresented: $showTreeModePicker) {
            Button("Dọc (Vertical)") {
                viewModel.settings?.treeDisplayMode = "vertical"
                viewModel.updateSettings()
            }
            Button("Ngang (Horizontal)") {
                viewModel.settings?.treeDisplayMode = "horizontal"
                viewModel.updateSettings()
            }
            Button("Hình quạt (Fan)") {
                viewModel.settings?.treeDisplayMode = "fan"
                viewModel.updateSettings()
            }
            Button("Huỷ", role: .cancel) {}
        }
        .confirmationDialog("Nhắc trước bao lâu", isPresented: $showDaysBeforePicker) {
            ForEach([1, 2, 3, 5, 7], id: \.self) { days in
                Button("\(days) ngày") {
                    viewModel.settings?.remindDaysBefore = days
                    viewModel.updateSettings()
                    scheduleMemorialNotifications()
                }
            }
            Button("Huỷ", role: .cancel) {}
        }
        .sheet(isPresented: $showQRCode) {
            QRCodeSheet(image: qrImage, familyName: viewModel.settings?.familyName ?? "Gia phả")
        }
        .overlay {
            if isExporting {
                Color.black.opacity(0.3)
                    .ignoresSafeArea()
                    .overlay(ProgressView("Đang xử lý...").tint(.white).foregroundColor(.white))
            }
        }
    }

    private var treeDisplayModeLabel: String {
        switch viewModel.settings?.treeDisplayMode {
        case "vertical": return "Dọc"
        case "horizontal": return "Ngang"
        case "fan": return "Hình quạt"
        default: return "Dọc"
        }
    }

    private var lastBackupDescription: String {
        let store = NSUbiquitousKeyValueStore.default
        if let ts = store.object(forKey: "family_backup_date") as? Double {
            let date = Date(timeIntervalSince1970: ts)
            let f = DateFormatter()
            f.dateFormat = "dd/MM/yyyy 'lúc' HH:mm"
            return "Lần cuối: \(f.string(from: date))"
        }
        return "Sao lưu dữ liệu gia phả"
    }
}

// ══════════════════════════════════════════
// SECTION LABEL
// ══════════════════════════════════════════

private struct SectionLabel: View {
    let icon: String
    let text: String

    var body: some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 14))
                .foregroundColor(PrimaryRed)
            Text(text)
                .font(.system(size: 12, weight: .bold))
                .foregroundColor(PrimaryRed)
                .tracking(0.5)
        }
        .padding(.top, 20)
        .padding(.bottom, 10)
    }
}

// ══════════════════════════════════════════
// FAMILY BANNER
// ══════════════════════════════════════════

private struct FamilyBanner: View {
    let settings: FamilySettingsEntity?

    var body: some View {
        ZStack(alignment: .topTrailing) {
            Circle()
                .fill(RadialGradient(colors: [Color(hex: "D4A017").opacity(0.12), .clear], center: .center, startRadius: 0, endRadius: 70))
                .frame(width: 140, height: 140)
                .offset(x: 20, y: -30)

            HStack(spacing: 14) {
                // Crest
                Circle()
                    .fill(LinearGradient(colors: [GoldAccent, Color(hex: "B8860B")], startPoint: .topLeading, endPoint: .bottomTrailing))
                    .frame(width: 56, height: 56)
                    .overlay(
                        Text(settings?.familyCrest ?? "GP")
                            .font(.system(size: 24, weight: .bold, design: .serif))
                            .foregroundColor(.white)
                    )
                    .overlay(Circle().stroke(.white.opacity(0.3), lineWidth: 2))

                VStack(alignment: .leading, spacing: 2) {
                    Text(settings?.familyName ?? "Gia phả của tôi")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.white)

                    let created = settings != nil ? formatDate(settings!.createdAt) : ""
                    if !created.isEmpty {
                        Text("Tạo ngày \(created)")
                            .font(.system(size: 12))
                            .foregroundColor(.white.opacity(0.6))
                    }
                }

                Spacer()
            }
            .padding(20)
        }
        .background(
            LinearGradient(
                colors: [Color(hex: "3E2723"), Color(hex: "4E342E"), Color(hex: "5D4037")],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .clipShape(RoundedRectangle(cornerRadius: 20))
    }

    private func formatDate(_ ts: Int64) -> String {
        let date = Date(timeIntervalSince1970: Double(ts) / 1000)
        let f = DateFormatter()
        f.dateFormat = "dd/MM/yyyy"
        return f.string(from: date)
    }
}

// ══════════════════════════════════════════
// STAT ROW
// ══════════════════════════════════════════

private struct StatRow: View {
    let generations: Int
    let members: Int
    let memorials: Int

    var body: some View {
        HStack(spacing: 8) {
            StatBox(value: generations, label: "Thế hệ")
            StatBox(value: members, label: "Thành viên")
            StatBox(value: memorials, label: "Ngày giỗ")
        }
    }
}

private struct StatBox: View {
    let value: Int
    let label: String

    var body: some View {
        VStack(spacing: 2) {
            Text("\(value)")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(PrimaryRed)
            Text(label)
                .font(.system(size: 10, weight: .medium))
                .foregroundColor(TextDim)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 14)
        .background(SurfaceContainer)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVariant, lineWidth: 1))
    }
}

// ══════════════════════════════════════════
// SETTINGS GROUP / ROW / TOGGLE
// ══════════════════════════════════════════

private struct SettingsGroup<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        VStack(spacing: 0) { content }
            .background(SurfaceContainer)
            .clipShape(RoundedRectangle(cornerRadius: 20))
            .overlay(RoundedRectangle(cornerRadius: 20).stroke(OutlineVariant, lineWidth: 1))
    }
}

private struct SettingsRow: View {
    let icon: String
    let iconBg: Color
    let iconFg: Color
    let title: String
    let desc: String
    var value: String? = nil
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                Image(systemName: icon)
                    .font(.system(size: 18))
                    .foregroundColor(iconFg)
                    .frame(width: 36, height: 36)
                    .background(iconBg)
                    .clipShape(RoundedRectangle(cornerRadius: 10))

                VStack(alignment: .leading, spacing: 1) {
                    Text(title)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(TextMain)
                    Text(desc)
                        .font(.system(size: 11))
                        .foregroundColor(TextDim)
                }

                Spacer()

                if let v = value {
                    Text(v)
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(TextDim)
                }

                Image(systemName: "chevron.right")
                    .font(.system(size: 14))
                    .foregroundColor(OutlineVariant)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
        }
        .buttonStyle(.plain)
    }
}

private struct SettingsToggle: View {
    let icon: String
    let iconBg: Color
    let iconFg: Color
    let title: String
    let desc: String
    @Binding var isOn: Bool

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 18))
                .foregroundColor(iconFg)
                .frame(width: 36, height: 36)
                .background(iconBg)
                .clipShape(RoundedRectangle(cornerRadius: 10))

            VStack(alignment: .leading, spacing: 1) {
                Text(title)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(TextMain)
                Text(desc)
                    .font(.system(size: 11))
                    .foregroundColor(TextDim)
            }

            Spacer()

            Toggle("", isOn: $isOn)
                .labelsHidden()
                .tint(Color(hex: "4CAF50"))
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
    }
}

// ══════════════════════════════════════════
// SHARE & EXPORT GRID
// ══════════════════════════════════════════

private struct ShareExportGrid: View {
    private let columns = [GridItem(.flexible(), spacing: 8), GridItem(.flexible(), spacing: 8)]
    let onPDF: () -> Void
    let onImage: () -> Void
    let onLink: () -> Void
    let onQR: () -> Void

    var body: some View {
        LazyVGrid(columns: columns, spacing: 8) {
            ShareButton(icon: "doc.richtext", iconBg: Color(hex: "FFEBEE"), iconFg: Color(hex: "C62828"), title: "Xuất PDF", desc: "File in ấn", action: onPDF)
            ShareButton(icon: "photo", iconBg: Color(hex: "E8F5E9"), iconFg: Color(hex: "2E7D32"), title: "Xuất Ảnh", desc: "PNG chất lượng cao", action: onImage)
            ShareButton(icon: "link", iconBg: Color(hex: "E3F2FD"), iconFg: Color(hex: "1565C0"), title: "Chia sẻ link", desc: "Gửi cho gia đình", action: onLink)
            ShareButton(icon: "qrcode", iconBg: Color(hex: "F3E5F5"), iconFg: Color(hex: "7B1FA2"), title: "Mã QR", desc: "Quét để xem", action: onQR)
        }
    }
}

private struct ShareButton: View {
    let icon: String
    let iconBg: Color
    let iconFg: Color
    let title: String
    let desc: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 10) {
                Image(systemName: icon)
                    .font(.system(size: 18))
                    .foregroundColor(iconFg)
                    .frame(width: 36, height: 36)
                    .background(iconBg)
                    .clipShape(RoundedRectangle(cornerRadius: 10))

                VStack(alignment: .leading, spacing: 0) {
                    Text(title)
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(TextMain)
                    Text(desc)
                        .font(.system(size: 10))
                        .foregroundColor(TextDim)
                }

                Spacer(minLength: 0)
            }
            .padding(14)
            .background(SurfaceContainer)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16).stroke(OutlineVariant, lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}

// ══════════════════════════════════════════
// QR CODE SHEET
// ══════════════════════════════════════════

private struct QRCodeSheet: View {
    let image: UIImage?
    let familyName: String
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                if let image {
                    Image(uiImage: image)
                        .interpolation(.none)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 260, height: 260)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .shadow(color: .black.opacity(0.1), radius: 8, y: 4)
                } else {
                    ProgressView()
                        .frame(width: 260, height: 260)
                }

                Text(familyName)
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(TextMain)

                Text("Quét mã QR để xem thông tin gia phả")
                    .font(.system(size: 13))
                    .foregroundColor(TextDim)

                if let image {
                    Button {
                        let av = UIActivityViewController(activityItems: [image], applicationActivities: nil)
                        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                           let root = windowScene.windows.first?.rootViewController {
                            root.present(av, animated: true)
                        }
                    } label: {
                        Label("Chia sẻ mã QR", systemImage: "square.and.arrow.up")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(14)
                            .background(PrimaryRed)
                            .clipShape(RoundedRectangle(cornerRadius: 14))
                    }
                    .padding(.horizontal, 40)
                }

                Spacer()
            }
            .padding(.top, 40)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Đóng") { dismiss() }
                }
            }
        }
    }
}
