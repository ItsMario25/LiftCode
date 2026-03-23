// navigation/Screen.kt
package com.example.panicelevators.navigation

// navigation/Screen.kt
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Favorites : Screen("favorites")  // ← Agregar
    object About : Screen("about")
    object Detail : Screen("detail/{code}") {
        fun createRoute(code: String): String = "detail/$code"
    }
}