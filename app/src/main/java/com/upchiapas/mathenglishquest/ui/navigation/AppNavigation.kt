package com.upchiapas.mathenglishquest.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.upchiapas.mathenglishquest.data.model.UserRole
import com.upchiapas.mathenglishquest.ui.MainViewModel
import com.upchiapas.mathenglishquest.ui.components.BottomNavBar
import com.upchiapas.mathenglishquest.ui.components.BottomNavItem
import com.upchiapas.mathenglishquest.ui.rememberAppViewModelFactory
import com.upchiapas.mathenglishquest.ui.screens.auth.LoginScreen
import com.upchiapas.mathenglishquest.ui.screens.auth.RegisterRoleScreen
import com.upchiapas.mathenglishquest.ui.screens.auth.RegisterStudentScreen
import com.upchiapas.mathenglishquest.ui.screens.auth.RegisterTeacherScreen
import com.upchiapas.mathenglishquest.ui.screens.auth.TeacherCodeScreen
import com.upchiapas.mathenglishquest.ui.screens.clase.ClassScreen
import com.upchiapas.mathenglishquest.ui.screens.dado.DadoScreen
import com.upchiapas.mathenglishquest.ui.screens.frases.FrasesScreen
import com.upchiapas.mathenglishquest.ui.screens.home.HomeScreen
import com.upchiapas.mathenglishquest.ui.screens.numeros.NumerosScreen
import com.upchiapas.mathenglishquest.ui.screens.portfolio.PortfolioScreen
import com.upchiapas.mathenglishquest.ui.screens.portfolio.StudentPortfolioScreen
import com.upchiapas.mathenglishquest.ui.screens.splash.SplashDestination
import com.upchiapas.mathenglishquest.ui.screens.splash.SplashScreen
import com.upchiapas.mathenglishquest.ui.theme.AppTheme

/** La barra inferior solo aparece en los módulos, nunca en la pantalla de carga ni en el login. */
private val bottomBarRoutes = setOf(
    Screen.Home.route,
    Screen.Dado.route,
    Screen.Numeros.route,
    Screen.Frases.route,
    Screen.Portfolio.route,
    Screen.Clase.route,
    Screen.ClaseStudent.route
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Los estudiantes con cuenta tienen un quinto ícono: Portafolio
    val mainViewModel: MainViewModel = viewModel(factory = rememberAppViewModelFactory())
    val mainState by mainViewModel.uiState.collectAsState()
    LaunchedEffect(currentRoute) {
        if (currentRoute in bottomBarRoutes) mainViewModel.refresh()
    }
    // Estudiantes con cuenta: Portafolio. Maestros: Clase. Invitados: solo los 4 módulos.
    val navItems = remember(mainState.role) {
        when (mainState.role) {
            UserRole.ESTUDIANTE -> BottomNavItem.base + BottomNavItem.Portafolio
            UserRole.MAESTRO -> BottomNavItem.base + BottomNavItem.Clase
            UserRole.INVITADO -> BottomNavItem.base
        }
    }
    // Mientras el maestro mira el portafolio de un estudiante, el ícono resaltado sigue siendo "Clase"
    val highlightedRoute = if (currentRoute == Screen.ClaseStudent.route) Screen.Clase.route else currentRoute

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppTheme.colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                BottomNavBar(
                    currentRoute = highlightedRoute,
                    items = navItems,
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
            composable(Screen.Portfolio.route) { PortfolioScreen() }
            composable(Screen.Clase.route) {
                ClassScreen(onOpenStudent = { id -> navController.navigate(Screen.ClaseStudent.createRoute(id)) })
            }
            composable(
                route = Screen.ClaseStudent.route,
                arguments = listOf(navArgument(Screen.ClaseStudent.ARG_STUDENT_ID) { type = NavType.IntType })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getInt(Screen.ClaseStudent.ARG_STUDENT_ID) ?: -1
                StudentPortfolioScreen(studentId = studentId, onBack = { navController.popBackStack() })
            }
        }
    }
}

/** Entra a Inicio y borra del historial todas las pantallas de login/registro. */
private fun NavHostController.goHomeClearingAuth() {
    navigate(Screen.Home.route) {
        popUpTo(Screen.Login.route) { inclusive = true }
    }
}
