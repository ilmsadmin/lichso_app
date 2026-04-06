package com.lichso.app.domain.model

/**
 * Danh sách thành phố Việt Nam và tọa độ tương ứng
 * Dùng chung cho Settings, Weather, ...
 */
object CityCoordinates {

    data class CityInfo(
        val name: String,
        val latitude: Double,
        val longitude: Double
    )

    val cities = listOf(
        CityInfo("Hà Nội", 21.0285, 105.8542),
        CityInfo("TP. Hồ Chí Minh", 10.8231, 106.6297),
        CityInfo("Đà Nẵng", 16.0544, 108.2022),
        CityInfo("Hải Phòng", 20.8449, 106.6881),
        CityInfo("Cần Thơ", 10.0452, 105.7469),
        CityInfo("Huế", 16.4637, 107.5909),
        CityInfo("Nha Trang", 12.2388, 109.1967),
        CityInfo("Đà Lạt", 11.9404, 108.4583),
        CityInfo("Vũng Tàu", 10.4114, 107.1362),
        CityInfo("Quy Nhơn", 13.7830, 109.2197),
        CityInfo("Vinh", 18.6796, 105.6813),
        CityInfo("Buôn Ma Thuột", 12.6680, 108.0378),
        CityInfo("Thái Nguyên", 21.5928, 105.8442),
        CityInfo("Nam Định", 20.4388, 106.1621),
        CityInfo("Hạ Long", 20.9711, 107.0452),
    )

    /** Danh sách tên thành phố */
    val cityNames: List<String> = cities.map { it.name }

    /** Tìm tọa độ theo tên thành phố, fallback về Hà Nội */
    fun getCoordinates(cityName: String): CityInfo {
        return cities.find { it.name == cityName } ?: cities.first()
    }

    /** Chuyển thành LocationInfo */
    fun toLocationInfo(cityName: String): LocationInfo {
        val city = getCoordinates(cityName)
        return LocationInfo(
            latitude = city.latitude,
            longitude = city.longitude,
            cityName = city.name
        )
    }
}
