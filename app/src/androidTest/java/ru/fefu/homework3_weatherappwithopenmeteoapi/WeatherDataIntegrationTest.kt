package ru.fefu.homework3_weatherappwithopenmeteoapi

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.dto.CurrentWeatherResponse
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.dto.GeocodingResponse
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.dto.WeatherResponse
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb.AppDatabase
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.GeocodingApi
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.WeatherApi
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.repository.WeatherRepositoryImpl
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.City
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.repository.WeatherRepository
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel.SearchUiState
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel.SearchViewModel

class WeatherDataIntegrationTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: WeatherRepository

    private val fakeWeatherApi = object : WeatherApi {
        override suspend fun getWeather(
            latitude: Double,
            longitude: Double,
            currentWeather: Boolean
        ) =
            WeatherResponse(
                CurrentWeatherResponse(
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
    }

    @After
    fun closeDb() = db.close()

    // Интеграционный тест 1: data-слой
    @Test
    fun addFavourite_shouldCorrectlySaveCity() = runTest {
        val fakeGeocodingApi = object : GeocodingApi {
            override suspend fun searchCity(name: String, count: Int, language: String) =
                GeocodingResponse(emptyList())
        }
        repository = WeatherRepositoryImpl(
            fakeGeocodingApi,
            fakeWeatherApi,
            db.citiesDao(),
            db.favouriteDao()
        )

        val city = City(id = 1, name = "Moscow", latitude = 55.7, longitude = 37.6)
        repository.addFavourite(city)

        repository.getFavourites().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Moscow", list[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Интеграционный тест 2: контракт поведения
    @Test
    fun addFavourite_twice_shouldNotCreateDuplicateEntries() = runTest {
        val fakeGeocodingApi = object : GeocodingApi {
            override suspend fun searchCity(name: String, count: Int, language: String) =
                GeocodingResponse(emptyList())
        }
        repository = WeatherRepositoryImpl(
            fakeGeocodingApi,
            fakeWeatherApi,
            db.citiesDao(),
            db.favouriteDao()
        )

        val city = City(id = 7, name = "Moscow", latitude = 55.7, longitude = 37.6)
        repository.addFavourite(city)
        repository.addFavourite(city)

        repository.getFavourites().test {
            val list = awaitItem()
            assertEquals("В базе не должно быть дублей по ID", 1, list.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Интеграционный тест 3
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun integration_error_then_retry_shouldTransitionToSuccess() = runTest {
        var shouldFail = true

        val dynamicGeocodingApi = object : GeocodingApi {
            override suspend fun searchCity(
                name: String,
                count: Int,
                language: String
            ): GeocodingResponse {
                if (shouldFail) throw Exception("Network Error")
                return GeocodingResponse(
                    listOf(
                        ru.fefu.homework3_weatherappwithopenmeteoapi.data.dto.CityResponse(
                            id = 1, name = "London", latitude = 20.0, longitude = 20.0
                        )
                    )
                )
            }
        }

        val realRepository = WeatherRepositoryImpl(
            geocodingApi = dynamicGeocodingApi,
            weatherApi = fakeWeatherApi,
            favouriteDao = db.favouriteDao(),
            citiesDao = db.citiesDao()
        )

        val viewModel = SearchViewModel(realRepository)

        viewModel.state.test {
            assertEquals(SearchUiState.Idle, awaitItem())

            viewModel.onQueryChanged("Lon")
            testScheduler.advanceTimeBy(600)
            assertEquals(SearchUiState.Loading, awaitItem())
            val errorState = awaitItem() as SearchUiState.Error
            assertEquals("Network Error", errorState.message)

            shouldFail = false
            viewModel.retryClick()
            testScheduler.advanceTimeBy(600)

            assertEquals(SearchUiState.Loading, awaitItem())
            val successState = awaitItem() as SearchUiState.Success
            assertEquals("London", successState.items.first().city.name)

            cancelAndIgnoreRemainingEvents()
        }
    }
}