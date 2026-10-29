package com.upchiapas.mathenglishquest.ui.screens.splash

enum class SplashDestination { LOGIN, HOME }

data class SplashUiState(
    /** null mientras carga; al terminar indica a qué pantalla ir. */
    val destination: SplashDestination? = null
)
