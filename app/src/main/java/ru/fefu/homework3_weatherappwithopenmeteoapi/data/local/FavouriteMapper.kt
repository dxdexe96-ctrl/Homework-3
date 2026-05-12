package ru.fefu.homework3_weatherappwithopenmeteoapi.data.local

import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.City

fun FavouriteEntity.toCity() = City(id, name, country, region, latitude, longitude)

fun City.toEntity() = FavouriteEntity(id, name, country ?: "", admin1 ?: "", latitude, longitude)