package com.upchiapas.mathenglishquest.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.upchiapas.mathenglishquest.ui.components.BottomNavBar
import com.upchiapas.mathenglishquest.ui.screens.dado.DadoScreen
import com.upchiapas.mathenglishquest.ui.screens.frases.FrasesScreen
import com.upchiapas.mathenglishquest.ui.screens.home.HomeScreen
import com.upchiapas.mathenglishquest.ui.screens.numeros.NumerosScreen
import com.upchiapas.mathenglishquest.ui.theme.AppTheme

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppTheme.colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { targetRoute ->
                    if (targetRoute != currentRoute) {
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.Home.route) {
                                saveState = (targetRoute != Screen.Home.route)
                            }
                            launchSingleTop = true
                            restoreState = (targetRoute != Screen.Home.route)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Dado.route) { DadoScreen() }
            composable(Screen.Numeros.route) { NumerosScreen() }
            composable(Screen.Frases.route) { FrasesScreen() }
        }
    }
}
