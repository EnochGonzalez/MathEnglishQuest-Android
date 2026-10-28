package com.upchiapas.mathenglishquest.ui.screens.home

import com.upchiapas.mathenglishquest.data.model.UserRole

data class HomeUiState(
    val userName: String = "Invitado",
    val role: UserRole = UserRole.INVITADO,
    val missionTitle: String = "Elige un módulo",
    val missionSubtitle: String = "Practica matemáticas y vocabulario en inglés. Cada racha de 3 desbloquea más dificultad."
)
