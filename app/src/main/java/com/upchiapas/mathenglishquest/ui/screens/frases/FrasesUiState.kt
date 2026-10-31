package com.upchiapas.mathenglishquest.ui.screens.frases

import androidx.annotation.StringRes
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.ui.util.UiText

enum class FrasesMode(@StringRes val titleRes: Int) {
    SOLO_LETRAS(R.string.frases_mode_sololetras),
    NUMEROS_Y_LETRAS(R.string.frases_mode_numerosyletras),
    TODO_EN_INGLES(R.string.frases_mode_todoeningles)
}

data class FrasesQuestion(
    val num1: Int,
    val num2: Int,
    val isAddition: Boolean,
    val options: List<String>
) {
    val result: Int = if (isAddition) num1 + num2 else num1 - num2
}

data class FrasesUiState(
    val activeMode: FrasesMode = FrasesMode.SOLO_LETRAS,
    val currentQuestion: FrasesQuestion? = null,
    val attemptCount: Int = 1,
    val showVoiceModal: Boolean = false,
    val showResultModal: Boolean = false,
    val resultModalAnswer: String = "",
    val selectedOption: String? = null,
    val correctOption: String? = null,
    val isAnswered: Boolean = false,
    val voiceInlineMessage: UiText? = null,
    val speechToSpeak: UiText? = null
)
