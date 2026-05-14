package ru.fefu.homework3_weatherappwithopenmeteoapi.data.dto

import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.CurrentWeather

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