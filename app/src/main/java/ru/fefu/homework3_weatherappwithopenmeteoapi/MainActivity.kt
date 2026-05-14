package ru.fefu.homework3_weatherappwithopenmeteoapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.screen.DetailScreen
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.screen.FavouritesScreen
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.screen.SearchScreen
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.viewmodel.DetailViewModel

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

    NavHost(navController = navController, startDestination = "search") {
        composable(Route.Search.path) {
            SearchScreen(
                onCityClick = { city ->
                    navController.navigate(Route.Details.createPath(city.id))
                },
                onOpenFavourites = {
                    navController.navigate(Route.Favorites.path)
                }
            )
        }
        composable(Route.Favorites.path) {
            FavouritesScreen(
                onBack = { navController.popBackStack() },
                onCityClick = { city ->
                    navController.navigate(Route.Details.createPath(city.id))
                },
            )
        }
        composable(
            route = Route.Details.path,
            arguments = listOf(navArgument("cityId") { type = NavType.IntType })
        ) { backStackEntry ->
            val cityId = backStackEntry.arguments?.getInt("cityId") ?: 0
            DetailScreen(
                vm = hiltViewModel<DetailViewModel, DetailViewModel.Factory> { it.create(cityId) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

sealed class Route(val path: String) {
    object Favorites : Route("favourites")
    object Search : Route("search")
    object Details : Route("detail/{cityId}") {
        fun createPath(cityId: Int): String = "detail/$cityId"
    }
}
