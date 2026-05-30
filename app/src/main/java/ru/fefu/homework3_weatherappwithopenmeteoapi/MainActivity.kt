package ru.fefu.homework3_weatherappwithopenmeteoapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.routes.DetailRoute
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.routes.FavouritesRoute
import ru.fefu.homework3_weatherappwithopenmeteoapi.ui.routes.SearchRoute

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
            SearchRoute(
                onCityClick = { cityItem ->
                    navController.navigate(Route.Details.createPath(cityItem.city.id))
                },
                onOpenFavourites = {
                    navController.navigate(Route.Favorites.path)
                }
            )
        }
        composable(Route.Favorites.path) {
            FavouritesRoute(
                onBack = { navController.popBackStack() },
                onCityClick = { cityItem ->
                    navController.navigate(Route.Details.createPath(cityItem.city.id))
                },
            )
        }
        composable(
            route = Route.Details.path,
            arguments = listOf(navArgument("cityId") { type = NavType.IntType })
        ) {
            DetailRoute(
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
