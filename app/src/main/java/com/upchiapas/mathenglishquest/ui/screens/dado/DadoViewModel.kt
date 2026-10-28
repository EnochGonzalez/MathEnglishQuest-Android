package com.upchiapas.mathenglishquest.ui.screens.dado

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class DadoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DadoUiState())
    val uiState: StateFlow<DadoUiState> = _uiState.asStateFlow()

    fun selectDifficulty(difficulty: DadoDifficulty) {
        _uiState.value = _uiState.value.copy(selectedDifficulty = difficulty)
    }

    fun rollDice() {
        val currentAngle = _uiState.value.rotationAngle
        val newDice1 = Random.nextInt(1, 7)
        val newDice2 = Random.nextInt(1, 7)
        val newStreak = _uiState.value.streakCount + 1

        _uiState.value = _uiState.value.copy(
            dice1 = newDice1,
            dice2 = newDice2,
            streakCount = newStreak,
            rotationAngle = currentAngle + 360f
        )
    }
}
