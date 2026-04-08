import Foundation

// MARK: - Weather API (Open-Meteo)

actor WeatherAPI {
    static let shared = WeatherAPI()

    private struct OpenMeteoResponse: Decodable {
        let current: CurrentWeather?

        struct CurrentWeather: Decodable {
            let temperature_2m: Double?
            let relative_humidity_2m: Int?
            let apparent_temperature: Double?
            let is_day: Int?
            let weather_code: Int?
            let wind_speed_10m: Double?
            let uv_index: Double?
        }
    }

    func getCurrentWeather(location: LocationInfo) async throws -> WeatherInfo {
        let urlString = "https://api.open-meteo.com/v1/forecast?"
            + "latitude=\(location.latitude)"
            + "&longitude=\(location.longitude)"
            + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code,wind_speed_10m,uv_index"
            + "&timezone=Asia/Ho_Chi_Minh"

        guard let url = URL(string: urlString) else {
            throw URLError(.badURL)
        }

        let (data, response) = try await URLSession.shared.data(from: url)

        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }

        let decoded = try JSONDecoder().decode(OpenMeteoResponse.self, from: data)
        guard let current = decoded.current else {
            throw URLError(.cannotParseResponse)
        }

        let isDay = (current.is_day ?? 1) == 1
        let (description, icon) = WeatherInfo.fromWeatherCode(current.weather_code ?? 0, isDay: isDay)

        return WeatherInfo(
            temperature: current.temperature_2m ?? 0,
            weatherCode: current.weather_code ?? 0,
            humidity: current.relative_humidity_2m ?? 0,
            windSpeed: current.wind_speed_10m ?? 0,
            cityName: location.cityName,
            description: description,
            icon: icon,
            isDay: isDay,
            feelsLike: current.apparent_temperature,
            uvIndex: current.uv_index,
            timestamp: Date()
        )
    }
}
