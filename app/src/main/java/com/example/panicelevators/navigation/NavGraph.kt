// navigation/NavGraph.kt
package com.example.panicelevators.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.panicelevators.PanicElevatorsApplication
import com.example.panicelevators.ui.about.AboutScreen
import com.example.panicelevators.ui.detail.DetailScreen
import com.example.panicelevators.ui.favorites.FavoritesScreen
import com.example.panicelevators.ui.favorites.FavoritesViewModelFactory
import com.example.panicelevators.ui.home.HomeScreen
import com.example.panicelevators.ui.home.HomeViewModelFactory
import com.example.panicelevators.ui.search.SearchScreen
import kotlinx.coroutines.launch

// ✅ Regex que define un código de error válido:
//    letras, números, espacios y guiones — máximo 20 caracteres
private val VALID_ERROR_CODE = Regex("^[a-zA-Z0-9\\s\\-]{1,20}$")

private fun String.isValidErrorCode(): Boolean =
    this.isNotBlank() && VALID_ERROR_CODE.matches(this)

@Composable
fun AppNavGraph(
    navController: NavHostController,
    favoritesViewModelFactory: FavoritesViewModelFactory,
    homeViewModelFactory: HomeViewModelFactory,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val app = navController.context.applicationContext as PanicElevatorsApplication
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        enterTransition = { fadeIn(animationSpec = tween(200)) },
        exitTransition = { fadeOut(animationSpec = tween(200)) },
        popEnterTransition = { fadeIn(animationSpec = tween(200)) },
        popExitTransition = { fadeOut(animationSpec = tween(200)) }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                homeViewModelFactory = homeViewModelFactory,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                navController = navController,
                onErrorClick = { errorCode ->
                    // ✅ Validar antes de registrar y navegar
                    if (errorCode.isValidErrorCode()) {
                        scope.launch {
                            app.recentRepository.addRecent(errorCode)
                        }
                        navController.navigate(Screen.Detail.createRoute(errorCode))
                    }
                }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(navController)
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                navController = navController,
                favoritesViewModelFactory = favoritesViewModelFactory
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("code") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // ✅ Segunda línea de defensa — valida el código al extraerlo
            //    de los argumentos de navegación
            val code = backStackEntry.arguments
                ?.getString("code")
                ?.takeIf { it.isValidErrorCode() }
                ?: ""

            DetailScreen(
                errorCode = code,
                navController = navController
            )
        }
    }
}