import SwiftUI

// ═══════════════════════════════════════════
// WeatherDetailSheet — Bottom sheet showing detailed weather info
// Opens when tapping the weather chip in HomeScreen header
// ═══════════════════════════════════════════

private var PrimaryRed: Color   { LSTheme.primary }
private var DeepRed: Color      { LSTheme.deepRed }
private var GoldAccent: Color   { LSTheme.gold }
private var SurfaceBg: Color    { LSTheme.bg }
private var SurfaceCard: Color  { LSTheme.surfaceContainer }
private var SurfaceHigh: Color  { LSTheme.surfaceContainerHigh }
private var TextMain: Color     { LSTheme.textPrimary }
private var TextSub: Color      { LSTheme.textSecondary }
private var TextDim: Color      { LSTheme.textTertiary }
private var OutlineVar: Color   { LSTheme.outlineVariant }

struct WeatherDetailSheet: View {
    let city: String
    let unit: String

    @ObservedObject private var service = WeatherService.shared
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ZStack {
                SurfaceBg.ignoresSafeArea()

                if service.isLoading && service.weather == nil {
                    loadingView
                } else if let weather = service.weather {
                    ScrollView(.vertical, showsIndicators: false) {
                        VStack(spacing: 16) {
                            // ── Current Weather Card ──
                            currentWeatherCard(weather)

                            // ── Hourly Forecast ──
                            if !weather.hourlyForecast.isEmpty {
                                hourlyCard(weather)
                            }

                            // ── Detail Grid ──
                            detailGrid(weather)

                            // ── 7-day Forecast ──
                            if !weather.dailyForecast.isEmpty {
                                dailyForecastCard(weather)
                            }

                            Spacer().frame(height: 20)
                        }
                        .padding(.horizontal, 16)
                        .padding(.top, 8)
                    }
                } else if service.error != nil {
                    errorView
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .principal) {
                    HStack(spacing: 6) {
                        Image(systemName: "cloud.sun.fill")
                            .foregroundColor(GoldAccent)
                        Text("Thời tiết")
                            .font(.system(size: 17, weight: .semibold))
                            .foregroundColor(TextMain)
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button { dismiss() } label: {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 22))
                            .foregroundStyle(TextDim)
                    }
                }
            }
        }
        .task {
            await service.fetchWeather(for: city, unit: unit)
        }
    }

    // MARK: - Current Weather Card

    private func currentWeatherCard(_ w: WeatherData) -> some View {
        VStack(spacing: 0) {
            // Gradient header
            VStack(spacing: 8) {
                Text(w.weatherEmoji)
                    .font(.system(size: 64))

                Text("\(Int(w.temperature.rounded()))\(unit)")
                    .font(.system(size: 56, weight: .thin))
                    .foregroundColor(.white)

                Text(w.conditionText)
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(.white.opacity(0.9))

                Text(w.cityName)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.white.opacity(0.7))

                Text("Cảm giác như \(Int(w.feelsLike.rounded()))\(unit)")
                    .font(.system(size: 13))
                    .foregroundColor(.white.opacity(0.6))
                    .padding(.top, 2)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 28)
            .background(
                LinearGradient(
                    colors: [Color(hex: "1565C0"), Color(hex: "1976D2"), Color(hex: "42A5F5")],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            )
            .clipShape(RoundedRectangle(cornerRadius: 20))
        }
    }

    // MARK: - Hourly Forecast

    private func hourlyCard(_ w: WeatherData) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader(icon: "clock", title: "Dự báo theo giờ")

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 4) {
                    ForEach(w.hourlyForecast.prefix(24)) { hour in
                        VStack(spacing: 6) {
                            Text(hour.time)
                                .font(.system(size: 11, weight: .medium))
                                .foregroundColor(TextDim)

                            Text(hour.weatherEmoji)
                                .font(.system(size: 20))

                            Text("\(Int(hour.temperature.rounded()))°")
                                .font(.system(size: 14, weight: .semibold))
                                .foregroundColor(TextMain)

                            if hour.precipitationProbability > 0 {
                                HStack(spacing: 2) {
                                    Image(systemName: "drop.fill")
                                        .font(.system(size: 8))
                                        .foregroundColor(Color(hex: "42A5F5"))
                                    Text("\(hour.precipitationProbability)%")
                                        .font(.system(size: 9))
                                        .foregroundColor(Color(hex: "42A5F5"))
                                }
                            } else {
                                Spacer().frame(height: 14)
                            }
                        }
                        .frame(width: 52)
                        .padding(.vertical, 10)
                        .background(SurfaceHigh)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                }
                .padding(.horizontal, 2)
            }
        }
        .padding(16)
        .background(SurfaceCard)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    // MARK: - Detail Grid

    private func detailGrid(_ w: WeatherData) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader(icon: "info.circle", title: "Chi tiết")

            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                detailCell(icon: "humidity.fill", iconColor: Color(hex: "42A5F5"),
                           title: "Độ ẩm", value: "\(w.humidity)%")
                detailCell(icon: "wind", iconColor: Color(hex: "78909C"),
                           title: "Gió", value: "\(Int(w.windSpeed)) km/h")
                detailCell(icon: "sun.max.fill", iconColor: GoldAccent,
                           title: "UV Index", value: uvLabel(w.uvIndex))
                detailCell(icon: "sunrise.fill", iconColor: Color(hex: "FF8F00"),
                           title: "Mặt trời mọc", value: w.sunrise)
                detailCell(icon: "sunset.fill", iconColor: Color(hex: "E65100"),
                           title: "Mặt trời lặn", value: w.sunset)
                detailCell(icon: "thermometer.medium", iconColor: Color(hex: "EF5350"),
                           title: "Cảm giác", value: "\(Int(w.feelsLike.rounded()))\(unit)")
            }
        }
        .padding(16)
        .background(SurfaceCard)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    // MARK: - 7-Day Forecast

    private func dailyForecastCard(_ w: WeatherData) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader(icon: "calendar", title: "Dự báo 7 ngày")

            VStack(spacing: 0) {
                ForEach(Array(w.dailyForecast.enumerated()), id: \.offset) { idx, day in
                    HStack(spacing: 12) {
                        Text(day.dayLabel)
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(TextMain)
                            .frame(width: 72, alignment: .leading)

                        Text(day.weatherEmoji)
                            .font(.system(size: 20))

                        if day.precipitationProbability > 0 {
                            HStack(spacing: 2) {
                                Image(systemName: "drop.fill")
                                    .font(.system(size: 10))
                                    .foregroundColor(Color(hex: "42A5F5"))
                                Text("\(day.precipitationProbability)%")
                                    .font(.system(size: 11))
                                    .foregroundColor(Color(hex: "42A5F5"))
                            }
                        }

                        Spacer()

                        HStack(spacing: 6) {
                            Text("\(Int(day.tempMin.rounded()))°")
                                .font(.system(size: 13))
                                .foregroundColor(TextDim)

                            // Temperature bar
                            GeometryReader { geo in
                                ZStack(alignment: .leading) {
                                    Capsule().fill(OutlineVar).frame(height: 4)
                                    Capsule()
                                        .fill(tempBarGradient(day))
                                        .frame(width: geo.size.width, height: 4)
                                }
                            }
                            .frame(width: 60, height: 4)

                            Text("\(Int(day.tempMax.rounded()))°")
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundColor(TextMain)
                        }
                    }
                    .padding(.vertical, 10)

                    if idx < w.dailyForecast.count - 1 {
                        Divider().background(OutlineVar)
                    }
                }
            }
        }
        .padding(16)
        .background(SurfaceCard)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    // MARK: - Loading / Error

    private var loadingView: some View {
        VStack(spacing: 16) {
            ProgressView()
                .scaleEffect(1.4)
                .tint(PrimaryRed)
            Text("Đang tải thời tiết...")
                .font(.system(size: 14))
                .foregroundColor(TextDim)
        }
    }

    private var errorView: some View {
        VStack(spacing: 16) {
            Image(systemName: "cloud.slash")
                .font(.system(size: 48))
                .foregroundColor(TextDim)
            Text("Không thể tải thời tiết")
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(TextMain)
            Text("Kiểm tra kết nối mạng và thử lại")
                .font(.system(size: 13))
                .foregroundColor(TextDim)
            Button {
                Task { await service.fetchWeather(for: city, unit: unit) }
            } label: {
                Text("Thử lại")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 10)
                    .background(PrimaryRed)
                    .clipShape(Capsule())
            }
        }
    }

    // MARK: - Subviews

    private func sectionHeader(icon: String, title: String) -> some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 13, weight: .semibold))
                .foregroundColor(TextDim)
            Text(title.uppercased())
                .font(.system(size: 11, weight: .semibold))
                .foregroundColor(TextDim)
                .tracking(0.5)
        }
    }

    private func detailCell(icon: String, iconColor: Color, title: String, value: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.system(size: 14))
                    .foregroundColor(iconColor)
                Text(title)
                    .font(.system(size: 12))
                    .foregroundColor(TextDim)
            }
            Text(value)
                .font(.system(size: 20, weight: .semibold))
                .foregroundColor(TextMain)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(SurfaceHigh)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private func uvLabel(_ uv: Double) -> String {
        switch uv {
        case ..<3:  return "\(Int(uv)) Thấp"
        case ..<6:  return "\(Int(uv)) TB"
        case ..<8:  return "\(Int(uv)) Cao"
        case ..<11: return "\(Int(uv)) Rất cao"
        default:    return "\(Int(uv)) Cực cao"
        }
    }

    private func tempBarGradient(_ day: DailyWeather) -> LinearGradient {
        LinearGradient(
            colors: [Color(hex: "42A5F5"), GoldAccent, Color(hex: "EF5350")],
            startPoint: .leading,
            endPoint: .trailing
        )
    }
}
