package com.upchiapas.mathenglishquest.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.upchiapas.mathenglishquest.ui.screens.dado.DadoScreen
import com.upchiapas.mathenglishquest.ui.screens.frases.FrasesScreen
import com.upchiapas.mathenglishquest.ui.screens.home.HomeScreen
import com.upchiapas.mathenglishquest.ui.screens.numeros.NumerosScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Dado.route) { DadoScreen() }
        composable(Screen.Numeros.route) { NumerosScreen() }
        composable(Screen.Frases.route) { FrasesScreen() }
    }
}