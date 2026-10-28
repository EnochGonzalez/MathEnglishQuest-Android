package com.upchiapas.mathenglishquest.ui.screens.frases

enum class FrasesMode(val title: String) {
    SOLO_LETRAS("Solo letras"),
    NUMEROS_Y_LETRAS("Números y letras"),
    TODO_EN_INGLES("Todo en inglés")
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
    val voiceInlineMessage: String? = null,
    val speechToSpeak: String? = null
)
