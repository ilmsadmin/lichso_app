import Foundation
import CoreLocation

// ═══════════════════════════════════════════
// WeatherService — Fetches weather from Open-Meteo (free, no API key)
// Supports geocoding city name → coordinates
// ═══════════════════════════════════════════

// MARK: - Models

struct WeatherData {
    let cityName: String
    let temperature: Double         // °C
    let feelsLike: Double
    let humidity: Int               // %
    let uvIndex: Double
    let windSpeed: Double           // km/h
    let weatherCode: Int
    let hourlyForecast: [HourlyWeather]
    let dailyForecast: [DailyWeather]
    let sunrise: String             // "HH:mm"
    let sunset: String              // "HH:mm"
}

struct HourlyWeather: Identifiable {
    let id = UUID()
    let time: String                // "HH:mm"
    let temperature: Double
    let weatherCode: Int
    let precipitationProbability: Int
}

struct DailyWeather: Identifiable {
    let id = UUID()
    let date: Date
    let dayLabel: String            // "Thứ 2", "CN", etc.
    let tempMax: Double
    let tempMin: Double
    let weatherCode: Int
    let precipitationProbability: Int
}

// MARK: - WeatherCode helpers

extension WeatherData {
    var conditionText: String { WeatherService.conditionText(for: weatherCode) }
    var weatherEmoji: String { WeatherService.weatherEmoji(for: weatherCode) }
}
extension HourlyWeather {
    var weatherEmoji: String { WeatherService.weatherEmoji(for: weatherCode) }
}
extension DailyWeather {
    var weatherEmoji: String { WeatherService.weatherEmoji(for: weatherCode) }
}

// MARK: - City Coordinates (local lookup — no API needed)

struct CityCoord {
    let name: String
    let latitude: Double
    let longitude: Double
}

private let vietnameseCities: [CityCoord] = [
    // ── Thành phố trực thuộc Trung ương ──
    CityCoord(name: "Hà Nội",            latitude: 21.0285, longitude: 105.8542),
    CityCoord(name: "TP. Hồ Chí Minh",   latitude: 10.8231, longitude: 106.6297),
    CityCoord(name: "Đà Nẵng",           latitude: 16.0544, longitude: 108.2022),
    CityCoord(name: "Hải Phòng",         latitude: 20.8449, longitude: 106.6881),
    CityCoord(name: "Cần Thơ",           latitude: 10.0452, longitude: 105.7469),
    // ── Miền Bắc ──
    CityCoord(name: "Hà Giang",          latitude: 22.8026, longitude: 104.9784),
    CityCoord(name: "Cao Bằng",          latitude: 22.6657, longitude: 106.2522),
    CityCoord(name: "Bắc Kạn",           latitude: 22.1474, longitude: 105.8348),
    CityCoord(name: "Tuyên Quang",       latitude: 21.8236, longitude: 105.2180),
    CityCoord(name: "Lào Cai",           latitude: 22.4809, longitude: 103.9755),
    CityCoord(name: "Điện Biên",         latitude: 21.3860, longitude: 103.0230),
    CityCoord(name: "Lai Châu",          latitude: 22.3964, longitude: 103.4592),
    CityCoord(name: "Sơn La",            latitude: 21.3272, longitude: 103.9144),
    CityCoord(name: "Yên Bái",           latitude: 21.7051, longitude: 104.8753),
    CityCoord(name: "Hoà Bình",          latitude: 20.6133, longitude: 105.3387),
    CityCoord(name: "Thái Nguyên",       latitude: 21.5928, longitude: 105.8442),
    CityCoord(name: "Lạng Sơn",          latitude: 21.8537, longitude: 106.7615),
    CityCoord(name: "Quảng Ninh",        latitude: 20.9711, longitude: 107.0452),
    CityCoord(name: "Hạ Long",           latitude: 20.9711, longitude: 107.0452),
    CityCoord(name: "Bắc Giang",         latitude: 21.2731, longitude: 106.1946),
    CityCoord(name: "Phú Thọ",           latitude: 21.3989, longitude: 105.2281),
    CityCoord(name: "Vĩnh Phúc",         latitude: 21.3609, longitude: 105.5474),
    CityCoord(name: "Bắc Ninh",          latitude: 21.1861, longitude: 106.0763),
    CityCoord(name: "Hải Dương",         latitude: 20.9373, longitude: 106.3145),
    CityCoord(name: "Hưng Yên",          latitude: 20.6464, longitude: 106.0511),
    CityCoord(name: "Thái Bình",         latitude: 20.4463, longitude: 106.3366),
    CityCoord(name: "Hà Nam",            latitude: 20.5836, longitude: 105.9228),
    CityCoord(name: "Nam Định",          latitude: 20.4388, longitude: 106.1621),
    CityCoord(name: "Ninh Bình",         latitude: 20.2506, longitude: 105.9745),
    // ── Miền Trung ──
    CityCoord(name: "Thanh Hoá",         latitude: 19.8075, longitude: 105.7764),
    CityCoord(name: "Nghệ An",           latitude: 18.6796, longitude: 105.6813),
    CityCoord(name: "Vinh",              latitude: 18.6796, longitude: 105.6813),
    CityCoord(name: "Hà Tĩnh",           latitude: 18.3560, longitude: 105.8877),
    CityCoord(name: "Quảng Bình",        latitude: 17.4689, longitude: 106.5985),
    CityCoord(name: "Đồng Hới",          latitude: 17.4689, longitude: 106.5985),
    CityCoord(name: "Quảng Trị",         latitude: 16.7503, longitude: 107.1857),
    CityCoord(name: "Huế",               latitude: 16.4637, longitude: 107.5909),
    CityCoord(name: "Quảng Nam",         latitude: 15.8794, longitude: 108.3350),
    CityCoord(name: "Hội An",            latitude: 15.8801, longitude: 108.3380),
    CityCoord(name: "Quảng Ngãi",        latitude: 15.1213, longitude: 108.7923),
    CityCoord(name: "Bình Định",         latitude: 13.7830, longitude: 109.2197),
    CityCoord(name: "Quy Nhơn",          latitude: 13.7830, longitude: 109.2197),
    CityCoord(name: "Phú Yên",           latitude: 13.0882, longitude: 109.0929),
    CityCoord(name: "Tuy Hoà",           latitude: 13.0882, longitude: 109.0929),
    CityCoord(name: "Khánh Hoà",         latitude: 12.2388, longitude: 109.1967),
    CityCoord(name: "Nha Trang",         latitude: 12.2388, longitude: 109.1967),
    CityCoord(name: "Ninh Thuận",        latitude: 11.5645, longitude: 108.9885),
    CityCoord(name: "Phan Rang",         latitude: 11.5645, longitude: 108.9885),
    CityCoord(name: "Bình Thuận",        latitude: 10.9804, longitude: 108.2522),
    CityCoord(name: "Phan Thiết",        latitude: 10.9281, longitude: 108.1006),
    // ── Tây Nguyên ──
    CityCoord(name: "Kon Tum",           latitude: 14.3497, longitude: 108.0005),
    CityCoord(name: "Gia Lai",           latitude: 13.9833, longitude: 108.0000),
    CityCoord(name: "Pleiku",            latitude: 13.9833, longitude: 108.0000),
    CityCoord(name: "Đắk Lắk",           latitude: 12.6680, longitude: 108.0378),
    CityCoord(name: "Buôn Ma Thuột",     latitude: 12.6680, longitude: 108.0378),
    CityCoord(name: "Đắk Nông",          latitude: 12.0046, longitude: 107.6872),
    CityCoord(name: "Gia Nghĩa",         latitude: 12.0046, longitude: 107.6872),
    CityCoord(name: "Lâm Đồng",          latitude: 11.9404, longitude: 108.4583),
    CityCoord(name: "Đà Lạt",            latitude: 11.9404, longitude: 108.4583),
    CityCoord(name: "Bảo Lộc",           latitude: 11.5447, longitude: 107.8106),
    // ── Đông Nam Bộ ──
    CityCoord(name: "Bình Phước",        latitude: 11.7512, longitude: 106.9009),
    CityCoord(name: "Đồng Xoài",         latitude: 11.5355, longitude: 106.8828),
    CityCoord(name: "Tây Ninh",          latitude: 11.3352, longitude: 106.1098),
    CityCoord(name: "Bình Dương",        latitude: 11.1825, longitude: 106.6524),
    CityCoord(name: "Thủ Dầu Một",       latitude: 11.1825, longitude: 106.6524),
    CityCoord(name: "Đồng Nai",          latitude: 10.9450, longitude: 106.8249),
    CityCoord(name: "Biên Hoà",          latitude: 10.9450, longitude: 106.8249),
    CityCoord(name: "Bà Rịa - Vũng Tàu", latitude: 10.4114, longitude: 107.1362),
    CityCoord(name: "Vũng Tàu",          latitude: 10.4114, longitude: 107.1362),
    CityCoord(name: "Long An",           latitude: 10.6956, longitude: 106.2431),
    CityCoord(name: "Tân An",            latitude: 10.5253, longitude: 106.4064),
    // ── Đồng bằng Sông Cửu Long ──
    CityCoord(name: "Tiền Giang",        latitude: 10.4493, longitude: 106.3420),
    CityCoord(name: "Mỹ Tho",            latitude: 10.3600, longitude: 106.3600),
    CityCoord(name: "Bến Tre",           latitude: 10.2415, longitude: 106.3756),
    CityCoord(name: "Trà Vinh",          latitude:  9.9513, longitude: 106.3345),
    CityCoord(name: "Vĩnh Long",         latitude: 10.2397, longitude: 105.9571),
    CityCoord(name: "Đồng Tháp",         latitude: 10.4938, longitude: 105.6882),
    CityCoord(name: "Cao Lãnh",          latitude: 10.4590, longitude: 105.6328),
    CityCoord(name: "An Giang",          latitude: 10.5215, longitude: 105.1259),
    CityCoord(name: "Long Xuyên",        latitude: 10.3862, longitude: 105.4353),
    CityCoord(name: "Châu Đốc",          latitude: 10.7026, longitude: 105.1215),
    CityCoord(name: "Kiên Giang",        latitude: 10.0127, longitude: 105.0800),
    CityCoord(name: "Rạch Giá",          latitude: 10.0127, longitude: 105.0800),
    CityCoord(name: "Phú Quốc",          latitude: 10.2899, longitude: 103.9840),
    CityCoord(name: "Hậu Giang",         latitude:  9.7575, longitude: 105.6412),
    CityCoord(name: "Vị Thanh",          latitude:  9.7575, longitude: 105.4695),
    CityCoord(name: "Sóc Trăng",         latitude:  9.6031, longitude: 105.9739),
    CityCoord(name: "Bạc Liêu",          latitude:  9.2941, longitude: 105.7278),
    CityCoord(name: "Cà Mau",            latitude:  9.1769, longitude: 105.1501),
]

// MARK: - Service

@MainActor
final class WeatherService: ObservableObject {
    static let shared = WeatherService()

    @Published var weather: WeatherData?
    @Published var isLoading = false
    @Published var error: String?

    private var cachedCity: String?
    private var cacheDate: Date?

    /// URLSession with reasonable timeouts (15s connect, 20s resource)
    private let session: URLSession = {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 15
        config.timeoutIntervalForResource = 20
        return URLSession(configuration: config)
    }()

    func fetchWeather(for city: String, unit: String = "°C") async {
        let isCelsius = unit == "°C"

        // Skip refetch if same city and fresh (< 30 min) and no previous error
        if let cached = cachedCity, cached == city,
           let date = cacheDate, Date().timeIntervalSince(date) < 1800,
           weather != nil, error == nil {
            return
        }

        isLoading = true
        error = nil

        // Try up to 2 times (initial + 1 retry)
        var lastError: Error?
        for attempt in 1...2 {
            do {
                let (lat, lon) = try await resolveCoordinates(city: city)
                let data = try await fetchOpenMeteo(lat: lat, lon: lon, city: city, isCelsius: isCelsius)
                weather = data
                cachedCity = city
                cacheDate = Date()
                lastError = nil
                break
            } catch {
                lastError = error
                #if DEBUG
                print("❌ WeatherService attempt \(attempt) error: \(error)")
                #endif
                if attempt < 2 {
                    try? await Task.sleep(nanoseconds: 1_000_000_000) // wait 1s before retry
                }
            }
        }

        if let lastError {
            #if DEBUG
            print("❌ WeatherService failed after retries: \(lastError)")
            #endif
            self.error = "Không thể tải thời tiết"
            cachedCity = nil
            cacheDate = nil
        }

        isLoading = false
    }

    // MARK: - Coordinate resolution: local lookup first, then geocoding API fallback

    private func resolveCoordinates(city: String) async throws -> (Double, Double) {
        // 1) Try local lookup (instant, no network)
        if let local = vietnameseCities.first(where: { $0.name == city }) {
            return (local.latitude, local.longitude)
        }
        // 2) Fallback: Open-Meteo geocoding API
        return try await geocodeViaAPI(city: city)
    }

    private func geocodeViaAPI(city: String) async throws -> (Double, Double) {
        let encodedCity = city.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? city
        guard let url = URL(string: "https://geocoding-api.open-meteo.com/v1/search?name=\(encodedCity)&count=1&language=vi&format=json") else {
            throw URLError(.badURL)
        }
        let (data, _) = try await session.data(from: url)
        let json = try JSONSerialization.jsonObject(with: data) as? [String: Any]
        guard let results = json?["results"] as? [[String: Any]],
              let first = results.first,
              let lat = first["latitude"] as? Double,
              let lon = first["longitude"] as? Double else {
            throw URLError(.badServerResponse)
        }
        return (lat, lon)
    }

    // MARK: - Open-Meteo forecast

    private func fetchOpenMeteo(lat: Double, lon: Double, city: String, isCelsius: Bool) async throws -> WeatherData {
        let unit = isCelsius ? "celsius" : "fahrenheit"
        // Note: Open-Meteo v1 uses "weather_code" (new) but also still accepts "weathercode" (legacy).
        // We request both variants and pick whichever is present to stay compatible.
        let urlStr = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=\(lat)&longitude=\(lon)" +
            "&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m,uv_index" +
            "&hourly=temperature_2m,weather_code,precipitation_probability" +
            "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,sunrise,sunset" +
            "&temperature_unit=\(unit)&wind_speed_unit=kmh&timezone=auto&forecast_days=7"
        guard let url = URL(string: urlStr) else { throw URLError(.badURL) }
        let (data, response) = try await session.data(from: url)
        if let http = response as? HTTPURLResponse, http.statusCode != 200 {
            throw URLError(.badServerResponse)
        }
        let json = try JSONSerialization.jsonObject(with: data) as? [String: Any] ?? [:]

        // Current
        let current = json["current"] as? [String: Any] ?? [:]
        let temp        = current["temperature_2m"] as? Double ?? 0
        let feelsLike   = current["apparent_temperature"] as? Double ?? 0
        let humidity    = (current["relative_humidity_2m"] as? Int) ?? Int(current["relative_humidity_2m"] as? Double ?? 0)
        // Support both old "weathercode" and new "weather_code"
        let wCode       = (current["weather_code"] as? Int) ?? (current["weathercode"] as? Int) ?? 0
        let wind        = (current["wind_speed_10m"] as? Double) ?? (current["windspeed_10m"] as? Double) ?? 0
        let uv          = current["uv_index"] as? Double ?? 0

        // Hourly (next 24 hours)
        let hourly = json["hourly"] as? [String: Any] ?? [:]
        let hTimes  = hourly["time"] as? [String] ?? []
        let hTemps  = hourly["temperature_2m"] as? [Double] ?? []
        let hCodes  = (hourly["weather_code"] as? [Int]) ?? (hourly["weathercode"] as? [Int]) ?? []
        let hPrec   = hourly["precipitation_probability"] as? [Int] ?? []

        let nowStr = ISO8601DateFormatter().string(from: Date()).prefix(13) // "2026-04-15T09" (UTC)
        // Open-Meteo returns times in local timezone (timezone=auto), so also build a local prefix
        let localFormatter = DateFormatter()
        localFormatter.dateFormat = "yyyy-MM-dd'T'HH"
        localFormatter.timeZone = .current
        let localNowStr = localFormatter.string(from: Date()) // "2026-04-15T16" (local)
        let startIdx = hTimes.firstIndex(where: { $0.hasPrefix(localNowStr) })
            ?? hTimes.firstIndex(where: { $0.hasPrefix(String(nowStr)) })
            ?? 0
        let endIdx = min(startIdx + 24, hTimes.count)

        let hourlyForecast: [HourlyWeather] = (startIdx..<endIdx).map { i in
            let timeStr = hTimes[i]
            let hourLabel = String(timeStr.suffix(5)) // "HH:MM"
            return HourlyWeather(
                time: hourLabel,
                temperature: i < hTemps.count ? hTemps[i] : 0,
                weatherCode: i < hCodes.count ? hCodes[i] : 0,
                precipitationProbability: i < hPrec.count ? hPrec[i] : 0
            )
        }

        // Daily
        let daily = json["daily"] as? [String: Any] ?? [:]
        let dTimes   = daily["time"] as? [String] ?? []
        let dCodes   = (daily["weather_code"] as? [Int]) ?? (daily["weathercode"] as? [Int]) ?? []
        let dTempMax = daily["temperature_2m_max"] as? [Double] ?? []
        let dTempMin = daily["temperature_2m_min"] as? [Double] ?? []
        let dPrec    = daily["precipitation_probability_max"] as? [Int] ?? []
        let dRise    = daily["sunrise"] as? [String] ?? []
        let dSet     = daily["sunset"] as? [String] ?? []

        let cal = Calendar.current
        let viet = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"]
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"

        let dailyForecast: [DailyWeather] = dTimes.enumerated().map { i, dateStr in
            let date = dateFormatter.date(from: dateStr) ?? Date()
            let wd = cal.component(.weekday, from: date) - 1 // 0=Sun
            let isToday = cal.isDateInToday(date)
            let label = isToday ? "Hôm nay" : viet[wd]
            return DailyWeather(
                date: date,
                dayLabel: label,
                tempMax: i < dTempMax.count ? dTempMax[i] : 0,
                tempMin: i < dTempMin.count ? dTempMin[i] : 0,
                weatherCode: i < dCodes.count ? dCodes[i] : 0,
                precipitationProbability: i < dPrec.count ? dPrec[i] : 0
            )
        }

        // Sunrise/Sunset for today
        let sunriseStr = dRise.first.map { String($0.suffix(5)) } ?? "--:--"
        let sunsetStr  = dSet.first.map { String($0.suffix(5)) } ?? "--:--"

        return WeatherData(
            cityName: city,
            temperature: temp,
            feelsLike: feelsLike,
            humidity: humidity,
            uvIndex: uv,
            windSpeed: wind,
            weatherCode: wCode,
            hourlyForecast: hourlyForecast,
            dailyForecast: dailyForecast,
            sunrise: sunriseStr,
            sunset: sunsetStr
        )
    }

    // MARK: - WMO weather code helpers

    nonisolated static func conditionText(for code: Int) -> String {
        switch code {
        case 0:             return "Trời quang"
        case 1:             return "Ít mây"
        case 2:             return "Mây rải rác"
        case 3:             return "Nhiều mây"
        case 45, 48:        return "Sương mù"
        case 51, 53, 55:    return "Mưa phùn"
        case 61, 63, 65:    return "Mưa"
        case 71, 73, 75:    return "Tuyết"
        case 80, 81, 82:    return "Mưa rào"
        case 95:            return "Dông"
        case 96, 99:        return "Dông + mưa đá"
        default:            return "Không rõ"
        }
    }

    nonisolated static func weatherEmoji(for code: Int) -> String {
        switch code {
        case 0:             return "☀️"
        case 1:             return "🌤️"
        case 2:             return "⛅"
        case 3:             return "☁️"
        case 45, 48:        return "🌫️"
        case 51, 53, 55:    return "🌦️"
        case 61, 63, 65:    return "🌧️"
        case 71, 73, 75:    return "❄️"
        case 80, 81, 82:    return "🌦️"
        case 95:            return "⛈️"
        case 96, 99:        return "⛈️"
        default:            return "🌡️"
        }
    }
}
