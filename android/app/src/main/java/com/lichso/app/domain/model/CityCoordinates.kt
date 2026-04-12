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
        // ── Thành phố trực thuộc Trung ương ──
        CityInfo("Hà Nội", 21.0285, 105.8542),
        CityInfo("TP. Hồ Chí Minh", 10.8231, 106.6297),
        CityInfo("Đà Nẵng", 16.0544, 108.2022),
        CityInfo("Hải Phòng", 20.8449, 106.6881),
        CityInfo("Cần Thơ", 10.0452, 105.7469),

        // ── Miền Bắc ──
        CityInfo("Hà Giang", 22.8026, 104.9784),
        CityInfo("Cao Bằng", 22.6657, 106.2522),
        CityInfo("Bắc Kạn", 22.1474, 105.8348),
        CityInfo("Tuyên Quang", 21.8236, 105.2180),
        CityInfo("Lào Cai", 22.4809, 103.9755),
        CityInfo("Điện Biên", 21.3860, 103.0230),
        CityInfo("Lai Châu", 22.3964, 103.4592),
        CityInfo("Sơn La", 21.3272, 103.9144),
        CityInfo("Yên Bái", 21.7051, 104.8753),
        CityInfo("Hoà Bình", 20.6133, 105.3387),
        CityInfo("Thái Nguyên", 21.5928, 105.8442),
        CityInfo("Lạng Sơn", 21.8537, 106.7615),
        CityInfo("Quảng Ninh", 20.9711, 107.0452),
        CityInfo("Hạ Long", 20.9711, 107.0452),
        CityInfo("Bắc Giang", 21.2731, 106.1946),
        CityInfo("Phú Thọ", 21.3989, 105.2281),
        CityInfo("Vĩnh Phúc", 21.3609, 105.5474),
        CityInfo("Bắc Ninh", 21.1861, 106.0763),
        CityInfo("Hải Dương", 20.9373, 106.3145),
        CityInfo("Hưng Yên", 20.6464, 106.0511),
        CityInfo("Thái Bình", 20.4463, 106.3366),
        CityInfo("Hà Nam", 20.5836, 105.9228),
        CityInfo("Nam Định", 20.4388, 106.1621),
        CityInfo("Ninh Bình", 20.2506, 105.9745),

        // ── Miền Trung ──
        CityInfo("Thanh Hoá", 19.8075, 105.7764),
        CityInfo("Nghệ An", 18.6796, 105.6813),
        CityInfo("Vinh", 18.6796, 105.6813),
        CityInfo("Hà Tĩnh", 18.3560, 105.8877),
        CityInfo("Quảng Bình", 17.4689, 106.5985),
        CityInfo("Đồng Hới", 17.4689, 106.5985),
        CityInfo("Quảng Trị", 16.7503, 107.1857),
        CityInfo("Huế", 16.4637, 107.5909),
        CityInfo("Quảng Nam", 15.8794, 108.3350),
        CityInfo("Hội An", 15.8801, 108.3380),
        CityInfo("Quảng Ngãi", 15.1213, 108.7923),
        CityInfo("Bình Định", 13.7830, 109.2197),
        CityInfo("Quy Nhơn", 13.7830, 109.2197),
        CityInfo("Phú Yên", 13.0882, 109.0929),
        CityInfo("Tuy Hoà", 13.0882, 109.0929),
        CityInfo("Khánh Hoà", 12.2388, 109.1967),
        CityInfo("Nha Trang", 12.2388, 109.1967),
        CityInfo("Ninh Thuận", 11.5645, 108.9885),
        CityInfo("Phan Rang", 11.5645, 108.9885),
        CityInfo("Bình Thuận", 10.9804, 108.2522),
        CityInfo("Phan Thiết", 10.9281, 108.1006),

        // ── Tây Nguyên ──
        CityInfo("Kon Tum", 14.3497, 108.0005),
        CityInfo("Gia Lai", 13.9833, 108.0000),
        CityInfo("Pleiku", 13.9833, 108.0000),
        CityInfo("Đắk Lắk", 12.6680, 108.0378),
        CityInfo("Buôn Ma Thuột", 12.6680, 108.0378),
        CityInfo("Đắk Nông", 12.0046, 107.6872),
        CityInfo("Gia Nghĩa", 12.0046, 107.6872),
        CityInfo("Lâm Đồng", 11.9404, 108.4583),
        CityInfo("Đà Lạt", 11.9404, 108.4583),
        CityInfo("Bảo Lộc", 11.5447, 107.8106),

        // ── Đông Nam Bộ ──
        CityInfo("Bình Phước", 11.7512, 106.9009),
        CityInfo("Đồng Xoài", 11.5355, 106.8828),
        CityInfo("Tây Ninh", 11.3352, 106.1098),
        CityInfo("Bình Dương", 11.1825, 106.6524),
        CityInfo("Thủ Dầu Một", 11.1825, 106.6524),
        CityInfo("Đồng Nai", 10.9450, 106.8249),
        CityInfo("Biên Hoà", 10.9450, 106.8249),
        CityInfo("Bà Rịa - Vũng Tàu", 10.4114, 107.1362),
        CityInfo("Vũng Tàu", 10.4114, 107.1362),
        CityInfo("Long An", 10.6956, 106.2431),
        CityInfo("Tân An", 10.5253, 106.4064),

        // ── Đồng bằng Sông Cửu Long ──
        CityInfo("Tiền Giang", 10.4493, 106.3420),
        CityInfo("Mỹ Tho", 10.3600, 106.3600),
        CityInfo("Bến Tre", 10.2415, 106.3756),
        CityInfo("Trà Vinh", 9.9513, 106.3345),
        CityInfo("Vĩnh Long", 10.2397, 105.9571),
        CityInfo("Đồng Tháp", 10.4938, 105.6882),
        CityInfo("Cao Lãnh", 10.4590, 105.6328),
        CityInfo("An Giang", 10.5215, 105.1259),
        CityInfo("Long Xuyên", 10.3862, 105.4353),
        CityInfo("Châu Đốc", 10.7026, 105.1215),
        CityInfo("Kiên Giang", 10.0127, 105.0800),
        CityInfo("Rạch Giá", 10.0127, 105.0800),
        CityInfo("Phú Quốc", 10.2899, 103.9840),
        CityInfo("Hậu Giang", 9.7575, 105.6412),
        CityInfo("Vị Thanh", 9.7575, 105.4695),
        CityInfo("Sóc Trăng", 9.6031, 105.9739),
        CityInfo("Bạc Liêu", 9.2941, 105.7278),
        CityInfo("Cà Mau", 9.1769, 105.1501),
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
