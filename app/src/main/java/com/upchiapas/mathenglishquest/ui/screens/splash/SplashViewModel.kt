package com.upchiapas.mathenglishquest.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.mathenglishquest.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Mientras suena la animación se revisa si ya había una sesión guardada.
            val hasSession = authRepository.hasValidSession()
            delay(SPLASH_DURATION_MS)
            _uiState.value = SplashUiState(
                destination = if (hasSession) SplashDestination.HOME else SplashDestination.LOGIN
            )
        }
    }

    private companion object {
        const val SPLASH_DURATION_MS = 3000L
    }
}
