package com.upchiapas.mathenglishquest.ui.screens.frases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.ui.util.UiText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class FrasesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FrasesUiState())
    val uiState: StateFlow<FrasesUiState> = _uiState.asStateFlow()

    private val englishWords = mapOf(
        0 to "Zero", 1 to "One", 2 to "Two", 3 to "Three", 4 to "Four",
        5 to "Five", 6 to "Six", 7 to "Seven", 8 to "Eight", 9 to "Nine",
        10 to "Ten", 11 to "Eleven", 12 to "Twelve", 13 to "Thirteen", 14 to "Fourteen",
        15 to "Fifteen", 16 to "Sixteen", 17 to "Seventeen", 18 to "Eighteen", 19 to "Nineteen", 20 to "Twenty"
    )

    init {
        generateNewQuestion()
    }

    fun getWord(num: Int): String = englishWords[num] ?: num.toString()

    fun selectMode(mode: FrasesMode) {
        val showModal = mode == FrasesMode.TODO_EN_INGLES
        _uiState.value = _uiState.value.copy(
            activeMode = mode,
            attemptCount = 1,
            showVoiceModal = showModal,
            showResultModal = false,
            selectedOption = null,
            correctOption = null,
            isAnswered = false,
            voiceInlineMessage = null
        )
        generateNewQuestion()
    }

    fun dismissVoiceModal() {
        _uiState.value = _uiState.value.copy(showVoiceModal = false)
    }

    fun dismissResultModal() {
        _uiState.value = _uiState.value.copy(
            showResultModal = false,
            attemptCount = 1,
            voiceInlineMessage = null
        )
        generateNewQuestion()
    }

    fun generateNewQuestion() {
        val isAdd = Random.nextBoolean()
        var n1 = Random.nextInt(1, 11)
        var n2 = Random.nextInt(1, 11)
        if (!isAdd && n1 < n2) {
            val temp = n1
            n1 = n2
            n2 = temp
        }
        val correctRes = if (isAdd) n1 + n2 else n1 - n2
        val correctWord = getWord(correctRes)

        val wrongOptions = mutableSetOf<String>()
        while (wrongOptions.size < 2) {
            val fakeRes = Random.nextInt(0, 21)
            if (fakeRes != correctRes) {
                wrongOptions.add(getWord(fakeRes))
            }
        }
        val allOptions = (wrongOptions.toList() + correctWord).shuffled()

        _uiState.value = _uiState.value.copy(
            currentQuestion = FrasesQuestion(n1, n2, isAdd, allOptions),
            attemptCount = 1,
            selectedOption = null,
            correctOption = null,
            isAnswered = false,
            voiceInlineMessage = null
        )
    }

    fun checkOptionAnswer(optionText: String) {
        if (_uiState.value.isAnswered) return
        val currentQ = _uiState.value.currentQuestion ?: return
        val correctWord = getWord(currentQ.result)
        val isCorrect = optionText == correctWord

        _uiState.value = _uiState.value.copy(
            selectedOption = optionText,
            correctOption = correctWord,
            isAnswered = true,
            speechToSpeak = if (isCorrect) UiText.StringResource(R.string.speech_correct, listOf(correctWord)) else UiText.StringResource(R.string.speech_try_again)
        )

        viewModelScope.launch {
            delay(1200)
            generateNewQuestion()
        }
    }

    /**
     * @param candidates interpretaciones del reconocedor de voz, la más probable primero.
     * La respuesta cuenta como correcta si CUALQUIERA de ellas contiene el número esperado.
     */
    fun handleVoiceResult(candidates: List<String>) {
        val currentQ = _uiState.value.currentQuestion ?: return
        // La que se muestra en "Escuché: ..." si la respuesta no fue correcta
        val spokenText = candidates.firstOrNull() ?: return
        val correctWord = getWord(currentQ.result)
        val isCorrect = candidates.any { isVoiceAnswerCorrect(it, currentQ.result, correctWord) }

        if (isCorrect) {
            _uiState.value = _uiState.value.copy(
                voiceInlineMessage = UiText.StringResource(R.string.frases_voice_correct_inline, listOf(correctWord)),
                speechToSpeak = UiText.StringResource(R.string.speech_correct, listOf(correctWord))
            )
            viewModelScope.launch {
                delay(1500)
                generateNewQuestion()
            }
        } else {
            val currentAttempts = _uiState.value.attemptCount
            if (currentAttempts < 3) {
                _uiState.value = _uiState.value.copy(
                    attemptCount = currentAttempts + 1,
                    voiceInlineMessage = UiText.StringResource(R.string.frases_voice_incorrect_inline, listOf(spokenText)),
                    speechToSpeak = UiText.StringResource(R.string.speech_try_again)
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    showResultModal = true,
                    resultModalAnswer = correctWord,
                    voiceInlineMessage = null,
                    speechToSpeak = UiText.StringResource(R.string.speech_answer_was, listOf(correctWord))
                )
            }
        }
    }

    private fun isVoiceAnswerCorrect(spokenText: String, targetNumber: Int, targetWord: String): Boolean {
        val tokens = spokenText.lowercase().split(Regex("[^a-z0-9]+")).filter { it.isNotBlank() }
        val accepted = setOf(targetWord.lowercase(), targetNumber.toString()) +
            soundAlikes[targetNumber].orEmpty()

        return tokens.any { token -> token in accepted }
    }

    /**
     * Palabras con las que el reconocedor suele confundir cada número cuando se dice solo
     * (sin "number" antes). Solo aplica a números de una palabra corta; del 11 en adelante
     * el reconocedor casi no falla.
     */
    private val soundAlikes: Map<Int, Set<String>> = mapOf(
        0 to setOf("cero", "hero"),
        1 to setOf("won", "wan", "1st"),
        2 to setOf("to", "too", "tu", "2nd"),
        3 to setOf("tree", "free", "3rd"),
        4 to setOf("for", "fore", "4th"),
        5 to setOf("fife", "hive", "5th"),
        6 to setOf("sex", "sics", "sick", "6th"),
        7 to setOf("7th"),
        8 to setOf("ate", "ait", "eighth", "8th", "hate", "aid", "eit"),
        9 to setOf("nein", "9th"),
        10 to setOf("tan", "then", "den", "10th")
    )

    fun speakAnswer() {
        val answer = _uiState.value.resultModalAnswer
        if (answer.isNotBlank()) {
            _uiState.value = _uiState.value.copy(speechToSpeak = UiText.DynamicString(answer))
        }
    }

    fun clearSpeech() {
        _uiState.value = _uiState.value.copy(speechToSpeak = null)
    }
}
