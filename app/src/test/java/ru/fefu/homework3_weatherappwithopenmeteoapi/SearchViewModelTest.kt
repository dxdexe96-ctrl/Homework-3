@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.fefu.homework3_weatherappwithopenmeteoapi

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
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
    }

    @Test
    fun `initial state should be Idle`() {
        viewModel = SearchViewModel(repository)
        assertEquals(SearchUiState.Idle, viewModel.state.value)
    }

    @Test
    fun `search success should emit Loading then Success`() = runTest {
        val cities = listOf(City(id = 1, name = "London"))
        coEvery { repository.searchCities("Lon") } returns flowOf(cities)

        viewModel = SearchViewModel(repository)

        viewModel.state.test {
            assertEquals(SearchUiState.Idle, awaitItem())
            viewModel.onQueryChanged("Lon")

            assertEquals(SearchUiState.Loading, awaitItem())
            val success = awaitItem() as SearchUiState.Success
            assertEquals(1, success.items.size)
        }
    }

    @Test
    fun `empty search result should emit Empty state`() = runTest {
        coEvery { repository.searchCities("Unknown") } returns flowOf(emptyList())
        viewModel = SearchViewModel(repository)

        viewModel.state.test {
            assertEquals(SearchUiState.Idle, awaitItem())
            viewModel.onQueryChanged("Unknown")
            testScheduler.advanceTimeBy(600)
            assertEquals(SearchUiState.Loading, awaitItem())

            val item = awaitItem()
            assertTrue("Ожидался Empty, но пришел $item", item is SearchUiState.Empty)
        }
    }

    @Test
    fun `repository error should emit Error state`() = runTest {
        coEvery { repository.searchCities(any()) } returns flow { throw Exception("Network Error") }

        viewModel = SearchViewModel(repository)
        viewModel.onQueryChanged("Error")

        viewModel.state.test {
            skipItems(2)
            val error = awaitItem() as SearchUiState.Error
            assertEquals("Network Error", error.message)
        }
    }

    @Test
    fun `rapid input should cancel previous search and only return last`() = runTest {
        val slowFlow = flow { delay(1000); emit(listOf(City(name = "Slow"))) }
        val fastFlow = flowOf(listOf(City(name = "Fast")))

        coEvery { repository.searchCities("A") } returns slowFlow
        coEvery { repository.searchCities("AB") } returns fastFlow

        viewModel = SearchViewModel(repository)

        viewModel.state.test {
            awaitItem()
            viewModel.onQueryChanged("A")
            testScheduler.advanceTimeBy(200)

            viewModel.onQueryChanged("AB")
            testScheduler.advanceTimeBy(600)

            assertEquals(SearchUiState.Loading, awaitItem())
            val result = awaitItem() as SearchUiState.Success

            assertEquals("Fast", result.items.first().city.name)
            expectNoEvents()
        }
    }

    @Test
    fun `items should correctly map isFavourite flag`() = runTest {
        val city = City(id = 1, name = "London")
        coEvery { repository.searchCities("Lon") } returns flowOf(listOf(city))
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