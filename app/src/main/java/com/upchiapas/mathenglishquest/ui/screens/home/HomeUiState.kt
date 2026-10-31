package com.upchiapas.mathenglishquest.ui.screens.home

import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.data.model.UserRole
import com.upchiapas.mathenglishquest.ui.util.UiText

data class HomeUiState(
    val userName: String = "Invitado",
    val role: UserRole = UserRole.INVITADO,
    val missionTitle: UiText = UiText.StringResource(R.string.home_mision_student_title),
    val missionSubtitle: UiText = UiText.StringResource(R.string.home_mision_student_subtitle)
)
