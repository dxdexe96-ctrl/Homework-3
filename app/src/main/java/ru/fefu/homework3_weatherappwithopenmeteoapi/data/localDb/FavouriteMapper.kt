package ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb

import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.City

fun CityDbModel.toCity() =
    City(id, name, country, region1, region2, latitude, longitude)

fun FavouriteDbModel.toCity() =
    City(id, name, country, region1, region2, latitude, longitude)

fun City.toDbModel() =
    CityDbModel(
        id,
        name,
        country ?: "",
        admin1 ?: "",
        admin2 ?: "",
        latitude,
        longitude,
    )

fun City.toFavDbModel() =
    FavouriteDbModel(
        id,
        name,
        country ?: "",
        admin1 ?: "",
        admin2 ?: "",
        latitude,
        longitude,
    )