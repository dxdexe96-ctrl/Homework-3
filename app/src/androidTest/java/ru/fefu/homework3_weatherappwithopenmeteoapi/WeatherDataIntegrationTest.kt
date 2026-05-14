package ru.fefu.homework3_weatherappwithopenmeteoapi

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import io.mockk.mockk
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.dto.GeocodingResponse
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.dto.WeatherResponse
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb.AppDatabase
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.GeocodingApi
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.WeatherApi
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.City
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.CurrentWeather
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.repository.WeatherRepository
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.repository.WeatherRepositoryImpl

@RunWith(AndroidJUnit4::class)
class WeatherDataIntegrationTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: WeatherRepository

    private val fakeGeocodingApi = object : GeocodingApi {
        override suspend fun searchCity(name: String, count: Int, language: String) =
            GeocodingResponse(emptyList())
    }
    private val fakeWeatherApi = object : WeatherApi {
        override suspend fun getWeather(
            latitude: Double,
            longitude: Double,
            currentWeather: Boolean
        ) =
            WeatherResponse(
                CurrentWeather(
                    temperature = 20.0,
                    windspeed = 20.0,
                    weathercode = 2,
                    time = "now",
                )
            )
    }

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repository = WeatherRepositoryImpl(
            geocodingApi = fakeGeocodingApi,
            weatherApi = fakeWeatherApi,
            favouriteDao = db.favouriteDao()
        )
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun addFavourite_shouldCorrectlySaveCity() = runTest {
        val city = City(id = 1, name = "Moscow", latitude = 55.7, longitude = 37.6)

        repository.addFavourite(city)

        repository.getFavourites().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Moscow", list[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addFavourite_twice_shouldNotCreateDuplicateEntries() = runTest {
        val city = City(id = 7, name = "Moscow", latitude = 55.7, longitude = 37.6)

        repository.addFavourite(city)
        repository.addFavourite(city)

        repository.getFavourites().test {
            val list = awaitItem()
            assertEquals("В базе не должно быть дублей по ID", 1, list.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun removeFavourite_shouldEmitUpdatedListViaFlow() = runTest {
        val city = City(id = 3, name = "London", latitude = 51.5, longitude = -0.1)
        repository.addFavourite(city)

        repository.getFavourites().test {
            val firstList = awaitItem()
            assertEquals(1, firstList.size)

            repository.removeFavourite(city)

            val secondList = awaitItem()
            assertTrue("После удаления список должен стать пустым", secondList.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }
}