package com.upchiapas.mathenglishquest.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.upchiapas.mathenglishquest.ui.components.BottomNavBar
import com.upchiapas.mathenglishquest.ui.screens.auth.LoginScreen
import com.upchiapas.mathenglishquest.ui.screens.auth.RegisterRoleScreen
import com.upchiapas.mathenglishquest.ui.screens.auth.RegisterStudentScreen
import com.upchiapas.mathenglishquest.ui.screens.auth.RegisterTeacherScreen
import com.upchiapas.mathenglishquest.ui.screens.auth.TeacherCodeScreen
import com.upchiapas.mathenglishquest.ui.screens.dado.DadoScreen
import com.upchiapas.mathenglishquest.ui.screens.frases.FrasesScreen
import com.upchiapas.mathenglishquest.ui.screens.home.HomeScreen
import com.upchiapas.mathenglishquest.ui.screens.numeros.NumerosScreen
import com.upchiapas.mathenglishquest.ui.screens.splash.SplashDestination
import com.upchiapas.mathenglishquest.ui.screens.splash.SplashScreen
import com.upchiapas.mathenglishquest.ui.theme.AppTheme

/** La barra inferior solo aparece en los módulos, nunca en la pantalla de carga ni en el login. */
private val bottomBarRoutes = setOf(
    Screen.Home.route,
    Screen.Dado.route,
    Screen.Numeros.route,
    Screen.Frases.route
)

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
            if (currentRoute in bottomBarRoutes) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { targetRoute ->
                        if (targetRoute != currentRoute) {
                            navController.navigate(targetRoute) {
                                // Sin saveState/restoreState: cada módulo arranca limpio al entrar (como las páginas del prototipo)
                                // y así un usuario nuevo nunca recibe el estado en memoria del anterior. El progreso
                                // importante (vidas, racha, nivel) se guarda aparte, por usuario.
                                popUpTo(Screen.Home.route)
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onFinished = { destination ->
                        val target = if (destination == SplashDestination.HOME) Screen.Home else Screen.Login
                        navController.navigate(target.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { navController.goHomeClearingAuth() },
                    onCreateAccount = { navController.navigate(Screen.RegisterRole.route) }
                )
            }
            composable(Screen.RegisterRole.route) {
                RegisterRoleScreen(
                    onStudent = { navController.navigate(Screen.RegisterStudent.route) },
                    onTeacher = { navController.navigate(Screen.RegisterTeacher.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.RegisterStudent.route) {
                RegisterStudentScreen(
                    onSuccess = { navController.goHomeClearingAuth() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.RegisterTeacher.route) {
                RegisterTeacherScreen(
                    onSuccess = { code ->
                        navController.navigate(Screen.TeacherCode.createRoute(code)) {
                            // Saca del historial el formulario y la elección de rol
                            popUpTo(Screen.RegisterRole.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.TeacherCode.route,
                arguments = listOf(navArgument(Screen.TeacherCode.ARG_CODE) { type = NavType.StringType })
            ) { backStackEntry ->
                val code = backStackEntry.arguments?.getString(Screen.TeacherCode.ARG_CODE).orEmpty()
                TeacherCodeScreen(
                    classCode = code,
                    onContinue = { navController.goHomeClearingAuth() }
                )
            }

            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Dado.route) { DadoScreen() }
            composable(Screen.Numeros.route) { NumerosScreen() }
            composable(Screen.Frases.route) { FrasesScreen() }
        }
    }
}

/** Entra a Inicio y borra del historial todas las pantallas de login/registro. */
private fun NavHostController.goHomeClearingAuth() {
    navigate(Screen.Home.route) {
        popUpTo(Screen.Login.route) { inclusive = true }
    }
}
