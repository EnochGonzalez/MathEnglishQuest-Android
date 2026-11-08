package com.upchiapas.mathenglishquest.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.mathenglishquest.data.repository.AuthError
import com.upchiapas.mathenglishquest.data.repository.AuthRepository
import com.upchiapas.mathenglishquest.data.repository.AuthResult
import com.upchiapas.mathenglishquest.integration.qr.ClassQrPayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Qué pasó con el último escaneo de QR, para avisárselo a la persona. */
enum class QrMessage {
    /** Se leyó el QR de una clase y el código ya está escrito. */
    SCANNED,
    /** El QR no era de una clase de la app. */
    INVALID,
    /** No se pudo abrir el lector de QR. */
    FAILED
}

data class RegisterStudentUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val classCode: String = "",
    val isLoading: Boolean = false,
    val error: AuthError? = null,
    val isSuccess: Boolean = false,
    val qrMessage: QrMessage? = null
)

class RegisterStudentViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterStudentUiState())
    val uiState: StateFlow<RegisterStudentUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, error = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }
    fun onClassCodeChange(value: String) =
        _uiState.update { it.copy(classCode = value.uppercase(), error = null, qrMessage = null) }

    /** El lector devolvió un texto: si es el QR de una clase, se llena el código; si no, se avisa. */
    fun onQrScanned(rawValue: String?) {
        val code = ClassQrPayload.decode(rawValue)
        _uiState.update {
            if (code != null) it.copy(classCode = code, error = null, qrMessage = QrMessage.SCANNED)
            else it.copy(qrMessage = QrMessage.INVALID)
        }
    }

    fun onQrScanFailed() = _uiState.update { it.copy(qrMessage = QrMessage.FAILED) }

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
