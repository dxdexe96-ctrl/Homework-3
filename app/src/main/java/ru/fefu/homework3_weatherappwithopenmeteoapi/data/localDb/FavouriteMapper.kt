package ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb

import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.City

fun FavouriteEntity.toCity() = City(id, name, country, region1, region2, latitude, longitude)

fun City.toDbModel() =
    FavouriteEntity(id, name, country ?: "", admin1 ?: "", admin2 ?: "", latitude, longitude)