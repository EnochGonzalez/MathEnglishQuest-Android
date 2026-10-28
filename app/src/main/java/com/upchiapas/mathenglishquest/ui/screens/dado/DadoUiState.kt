package com.upchiapas.mathenglishquest.ui.screens.dado

enum class DadoDifficulty(val title: String) {
    FACIL("Fácil"),
    MEDIO("Medio"),
    DIFICIEL("Difícil")
}

data class DadoUiState(
    val selectedDifficulty: DadoDifficulty = DadoDifficulty.FACIL,
    val dice1: Int = 1,
    val dice2: Int = 1,
    val streakCount: Int = 0,
    val rotationAngle: Float = 0f
) {
    val result: Int = if (selectedDifficulty == DadoDifficulty.FACIL) dice1 else dice1 + dice2
}
