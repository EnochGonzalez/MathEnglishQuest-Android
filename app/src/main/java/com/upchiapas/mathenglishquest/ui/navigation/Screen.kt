package com.upchiapas.mathenglishquest.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Dado : Screen("dado")
    object Numeros : Screen("numeros")
    object Frases : Screen("frases")
}
