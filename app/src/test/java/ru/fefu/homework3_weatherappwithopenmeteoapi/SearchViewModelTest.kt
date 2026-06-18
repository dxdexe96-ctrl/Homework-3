@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.fefu.homework3_weatherappwithopenmeteoapi

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.dto.CityResponse
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.entity.City
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.repository.WeatherRepository
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel.SearchUiState
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel.SearchViewModel

class SearchViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<WeatherRepository>()
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        every { repository.getFavourites() } returns flowOf(emptyList())
        coEvery { repository.saveCities(any()) } just runs
    }

    // Юнит-тест 1: Начальное состояние экрана
    @Test
    fun `initial state should be Idle`() {
        viewModel = SearchViewModel(repository)
        assertEquals(SearchUiState.Idle, viewModel.state.value)
    }

    // Юнит-тест 2 и Flow-тест 1: Полная последовательность эмиссий при успехе
    @Test
    fun `search success should emit Idle - Loading - Success`() = runTest {
        val cities = listOf(City(id = 1, name = "London", latitude = 20.0, longitude = 20.0))
        coEvery { repository.searchCities("Lon") } returns cities

        viewModel = SearchViewModel(repository)

        viewModel.state.test {
            assertEquals(SearchUiState.Idle, awaitItem()) // Эмиссия 1

            viewModel.onQueryChanged("Lon")
            testScheduler.advanceTimeBy(600)

            assertEquals(SearchUiState.Loading, awaitItem()) // Эмиссия 2
            val success = awaitItem() as SearchUiState.Success // Эмиссия 3
            assertEquals(1, success.items.size)
        }
    }

    // Юнит-тест 3 и Flow-тест 2: Полная последовательность эмиссий при ошибке
    @Test
    fun `repository error should emit Idle - Loading - Error`() = runTest {
        coEvery { repository.searchCities(any()) } throws Exception("Network Error")

        viewModel = SearchViewModel(repository)

        viewModel.state.test {
            assertEquals(SearchUiState.Idle, awaitItem())

            viewModel.onQueryChanged("Error")
            testScheduler.advanceTimeBy(600)

            assertEquals(SearchUiState.Loading, awaitItem())

            val error = awaitItem() as SearchUiState.Error
            assertEquals("Network Error", error.message)
        }
    }

    // Юнит-тест 4 и нетривиальный тест 1: Проверка, что retry действительно делает новую попытку
    @Test
    fun `retryClick should re-trigger search after network error`() = runTest {
        val cities = listOf(City(id = 1, name = "London", latitude = 20.0, longitude = 20.0))

        coEvery { repository.searchCities("Lon") } throws Exception("Network Error") andThen cities

        viewModel = SearchViewModel(repository)

        viewModel.state.test {
            assertEquals(SearchUiState.Idle, awaitItem())

            viewModel.onQueryChanged("Lon")
            testScheduler.advanceTimeBy(600)
            assertEquals(SearchUiState.Loading, awaitItem())
            assertTrue(awaitItem() is SearchUiState.Error)

            viewModel.retryClick()
            testScheduler.advanceTimeBy(600)

            assertEquals(SearchUiState.Loading, awaitItem())
            val success = awaitItem() as SearchUiState.Success
            assertEquals("London", success.items.first().city.name)
        }
    }

    // Юнит-тест 5 и нетривиальный тест 2: Пустой результат дает именно Empty, а не Success
    @Test
    fun `empty search result should emit Empty state`() = runTest {
        coEvery { repository.searchCities("Unknown") } returns emptyList()
        viewModel = SearchViewModel(repository)

        viewModel.state.test {
            assertEquals(SearchUiState.Idle, awaitItem())
            viewModel.onQueryChanged("Unknown")
            testScheduler.advanceTimeBy(600)

            assertEquals(SearchUiState.Loading, awaitItem())
            assertTrue(awaitItem() is SearchUiState.Empty)
        }
    }

    // Юнит-тест 6 (и FLOW-тест 3): Отмена устаревшего запроса
    @Test
    fun `rapid input should cancel previous search and only return last`() = runTest {
        coEvery { repository.searchCities("A") } coAnswers {
            delay(1000) // Медленный запрос
            listOf(City(id = 1, name = "Slow", latitude = 0.0, longitude = 0.0))
        }
        coEvery { repository.searchCities("AB") } returns listOf(
            City(id = 2, name = "Fast", latitude = 0.0, longitude = 0.0)
        )

        viewModel = SearchViewModel(repository)

        viewModel.state.test {
            assertEquals(SearchUiState.Idle, awaitItem())

            viewModel.onQueryChanged("A")
            testScheduler.advanceTimeBy(600)
            assertEquals(SearchUiState.Loading, awaitItem())

            viewModel.onQueryChanged("AB")
            testScheduler.advanceTimeBy(600)

            val result = awaitItem() as SearchUiState.Success
            assertEquals("Fast", result.items.first().city.name)
            expectNoEvents()
        }
    }

    // Юнит-тест 7
    @Test
    fun `model mapping toCity should correctly convert fields`() {
        val dto = CityResponse(
            id = 42,
            name = "Moscow",
            country = "Russia",
            admin1 = "Moscow Oblast",
            admin2 = null,
            latitude = 55.75,
            longitude = 37.61
        )

        val entity = dto.toCity()

        assertEquals(42, entity.id)
        assertEquals("Moscow", entity.name)
        assertEquals("Russia", entity.country)
    }

    // Юнит-тест 8
    @Test
    fun `items should correctly map isFavourite flag`() = runTest {
        val city = City(id = 1, name = "London", latitude = 20.0, longitude = 20.0)
        coEvery { repository.searchCities("Lon") } returns listOf(city)
        every { repository.getFavourites() } returns flowOf(listOf(city))

        viewModel = SearchViewModel(repository)

        viewModel.state.test {
            assertEquals(SearchUiState.Idle, awaitItem())

            viewModel.onQueryChanged("Lon")
            testScheduler.advanceTimeBy(600)

            assertEquals(SearchUiState.Loading, awaitItem())

            val success = awaitItem() as SearchUiState.Success
            assertTrue(success.items.first().isFavourite)
        }
    }
}