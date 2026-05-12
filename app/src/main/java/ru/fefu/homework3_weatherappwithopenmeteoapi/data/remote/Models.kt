package ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote

data class City(
    val id: Int,
    val name: String,
    val country: String? = "",
    val admin1: String? = "",
    val latitude: Double,
    val longitude: Double
)

data class GeocodingResponse(
    val results: List<City>? = null
)

data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val weathercode: Int,
    val time: String
)

data class WeatherResponse(
    val current_weather: CurrentWeather
)


fun weatherDescription(code: Int): String = when (code) {
    0 -> "Ясно"
    in 1..3 -> "Переменная облачность"
    in 45..48 -> "Туман"
    in 51..55 -> "Морось"
    in 61..67 -> "Дождь"
    in 71..77 -> "Снег"
    in 80..82 -> "Ливень"
    in 95..99 -> "Гроза"
    else -> "Неизвестно"
}