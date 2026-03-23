// MainActivity.kt
package com.example.panicelevators

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.panicelevators.navigation.AppNavGraph
import com.example.panicelevators.ui.favorites.FavoritesViewModelFactory
import com.example.panicelevators.ui.home.HomeViewModelFactory
import com.example.panicelevators.ui.theme.PanicElevatorsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = applicationContext as PanicElevatorsApplication
        val favoritesViewModelFactory = FavoritesViewModelFactory(app.favoritesRepository)
        val homeViewModelFactory = HomeViewModelFactory(app.recentRepository)

        installSplashScreen()

        setContent {
            // ✅ rememberSaveable mantiene el estado al rotar la pantalla
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }

            PanicElevatorsTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController,
                    favoritesViewModelFactory = favoritesViewModelFactory,
                    homeViewModelFactory = homeViewModelFactory,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}