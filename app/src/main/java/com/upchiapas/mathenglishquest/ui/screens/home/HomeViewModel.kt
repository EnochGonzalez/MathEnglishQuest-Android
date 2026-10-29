package com.upchiapas.mathenglishquest.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.mathenglishquest.data.model.UserRole
import com.upchiapas.mathenglishquest.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val user = authRepository.currentUser() ?: return@launch
            val isTeacher = user.role == UserRole.MAESTRO
            _uiState.value = _uiState.value.copy(
                userName = user.firstName,
                role = user.role,
                // Mismos textos que home.js del prototipo
                missionTitle = if (isTeacher) "Elige un módulo o revisa tu clase" else "Elige un módulo",
                missionSubtitle = if (isTeacher) {
                    "Puedes practicar los módulos como tus estudiantes, o entrar a Mi Clase para ver su progreso."
                } else {
                    "Practica matemáticas y vocabulario en inglés. Cada racha de 3 desbloquea más dificultad."
                }
            )
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
