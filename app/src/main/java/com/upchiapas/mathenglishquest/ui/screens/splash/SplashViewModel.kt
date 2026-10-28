package com.upchiapas.mathenglishquest.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        // Aquí después se revisará la sesión guardada (login o inicio) antes de terminar.
        viewModelScope.launch {
            delay(SPLASH_DURATION_MS)
            _uiState.value = SplashUiState(isFinished = true)
        }
    }

    private companion object {
        const val SPLASH_DURATION_MS = 3000L
    }
}
