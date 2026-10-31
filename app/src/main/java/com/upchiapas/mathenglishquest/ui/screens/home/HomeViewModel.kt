package com.upchiapas.mathenglishquest.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.data.model.UserRole
import com.upchiapas.mathenglishquest.data.repository.AuthRepository
import com.upchiapas.mathenglishquest.ui.util.UiText
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
                missionTitle = if (isTeacher) UiText.StringResource(R.string.home_mision_teacher_title) else UiText.StringResource(R.string.home_mision_student_title),
                missionSubtitle = if (isTeacher) UiText.StringResource(R.string.home_mision_teacher_subtitle) else UiText.StringResource(R.string.home_mision_student_subtitle)
            )
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
