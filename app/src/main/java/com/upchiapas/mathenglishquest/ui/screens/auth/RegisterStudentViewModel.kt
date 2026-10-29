package com.upchiapas.mathenglishquest.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.mathenglishquest.data.repository.AuthError
import com.upchiapas.mathenglishquest.data.repository.AuthRepository
import com.upchiapas.mathenglishquest.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterStudentUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val classCode: String = "",
    val isLoading: Boolean = false,
    val error: AuthError? = null,
    val isSuccess: Boolean = false
)

class RegisterStudentViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterStudentUiState())
    val uiState: StateFlow<RegisterStudentUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, error = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }
    fun onClassCodeChange(value: String) = _uiState.update { it.copy(classCode = value.uppercase(), error = null) }

    fun register() {
        val s = _uiState.value
        if (s.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.registerStudent(s.name, s.email, s.password, s.classCode)) {
                is AuthResult.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                is AuthResult.Failure -> _uiState.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }
}
