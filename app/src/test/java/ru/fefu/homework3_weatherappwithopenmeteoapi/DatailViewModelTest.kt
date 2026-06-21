@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.fefu.homework3_weatherappwithopenmeteoapi

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.City
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.CurrentWeather
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.repository.WeatherRepository
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel.DetailUiState
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel.DetailViewModel

class DatailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<WeatherRepository>()
    private lateinit var viewModel: DetailViewModel

    @Test
    fun `retryClick should re-trigger weather fetch after error and transition to success`() =
        runTest {
            val cityId = 42
            val city = City(
                id = cityId,
                name = "Moscow",
                latitude = 55.75,
                longitude = 37.61,
                country = "Russia",
                admin1 = null,
                admin2 = null,
            )
            val fakeWeather = CurrentWeather(
                temperature = 25.0,
                windspeed = 5.0,
                weathercode = 1,
                time = "now"
            )

            coEvery { repository.getCityById(cityId) } returns city
            every { repository.existFavById(cityId) } returns flowOf(true)

            coEvery {
                repository.getWeather(city.latitude, city.longitude)
            } coAnswers {
                delay(100)
                throw Exception("Network Error")
            } coAndThen {
                delay(100)
                fakeWeather
            }

            val savedStateHandle = SavedStateHandle(mapOf("cityId" to cityId))

            viewModel = DetailViewModel(repository, savedStateHandle)

            viewModel.state.test {
                testScheduler.runCurrent()
                assertEquals(DetailUiState.Loading, awaitItem())

                testScheduler.advanceTimeBy(150)
                val errorState = awaitItem() as DetailUiState.Error
                assertEquals("Network Error", errorState.message)

                viewModel.retryClick()

                testScheduler.runCurrent()
                assertEquals(DetailUiState.Loading, awaitItem())

                testScheduler.advanceTimeBy(150)
                val successState = awaitItem() as DetailUiState.Success
                assertEquals(fakeWeather, successState.weather)
                assertEquals(cityId, successState.cityItem.city.id)
                assertTrue(successState.cityItem.isFavourite)

                cancelAndIgnoreRemainingEvents()
            }
        }
}