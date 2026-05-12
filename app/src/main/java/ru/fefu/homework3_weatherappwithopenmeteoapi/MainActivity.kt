package ru.fefu.homework3_weatherappwithopenmeteoapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.screen.DetailScreen
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.screen.FavouritesScreen
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.screen.SearchScreen
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel.WeatherViewModel

@AndroidEntryPoint
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
    val viewModel: WeatherViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = "search"){
        composable("search") {
            SearchScreen(
                searchQuery = viewModel.searchQuery.value,
                onQueryChange = viewModel::onQueryChange,
                onSearch = viewModel::searchCities,
                searchState = viewModel.searchState.value,
                onCityClick = { city ->
                    navController.navigate("detail/${city.id}")
                },
                onFavouriteClick = viewModel::toggleFavourite,
                onOpenFavourites = {
                    navController.navigate("favourites")
                }
            )
        }
        composable("favourites") {
            FavouritesScreen(
                favourites = viewModel.favourites.value,
                onBack = { navController.popBackStack() },
                onCityClick = { city ->
                    navController.navigate("detail/${city.id}")
                },
                onFavouriteClick = viewModel::toggleFavourite
            )
        }

        val detailArguments = listOf(navArgument("cityId") {
            type = NavType.IntType }
        )

        composable("detail/{cityId}", arguments = detailArguments) {
            LaunchedEffect(it.arguments!!.getInt("cityId")) {
                viewModel.loadWeather(it.arguments!!.getInt("cityId"))
            }
            DetailScreen(
                detailState = viewModel.detailState.value,
                onBack = { navController.popBackStack() },
                onFavouriteClick = { city -> viewModel.toggleFavourite(city) }
            )
        }
    }
}
