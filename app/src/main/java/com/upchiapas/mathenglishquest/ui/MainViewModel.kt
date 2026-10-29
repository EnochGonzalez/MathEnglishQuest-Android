package com.upchiapas.mathenglishquest.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.mathenglishquest.data.model.UserRole
import com.upchiapas.mathenglishquest.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val role: UserRole = UserRole.INVITADO
)

/** Sabe quién tiene la sesión abierta para que la barra inferior muestre los íconos que le tocan. */
class MainViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    /** Se llama al entrar a un módulo: así el rol se actualiza después de iniciar o cerrar sesión. */
    fun refresh() {
        viewModelScope.launch {
            val role = authRepository.currentUser()?.role ?: UserRole.INVITADO
            _uiState.value = MainUiState(role = role)
        }
    }
}
