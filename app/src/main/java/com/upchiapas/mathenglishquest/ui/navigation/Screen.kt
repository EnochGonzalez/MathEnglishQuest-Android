package com.upchiapas.mathenglishquest.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object RegisterRole : Screen("register_role")
    object RegisterStudent : Screen("register_student")
    object RegisterTeacher : Screen("register_teacher")
    object TeacherCode : Screen("teacher_code/{code}") {
        const val ARG_CODE = "code"
        fun createRoute(code: String) = "teacher_code/$code"
    }
    object Home : Screen("home")
    object Dado : Screen("dado")
    object Numeros : Screen("numeros")
    object Frases : Screen("frases")
    object Portfolio : Screen("portfolio")
}
