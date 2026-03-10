package ru.fefu.homework3_weatherappwithopenmeteoapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: WeatherViewModel = viewModel()

    NavHost(navController = navController, startDestination = "search")
    {
        composable("search")
        {
            SearchScreen(
                searchQuery = viewModel.searchQuery,
                onQueryChange = viewModel::onQueryChange,
                onSearch = viewModel::searchCities,
                searchState = viewModel.searchState,
                onCityClick = { city ->
                    viewModel.loadWeather(city)
                    navController.navigate("detail")
                },
                onFavouriteClick = viewModel::toggleFavourite
            )
        }
        composable("detail") {
            DetailScreen(
                detailState = viewModel.detailState,
                onBack = { navController.popBackStack() },
                onFavouriteClick = { city -> viewModel.toggleFavourite(city) }
            )
        }
    }
}