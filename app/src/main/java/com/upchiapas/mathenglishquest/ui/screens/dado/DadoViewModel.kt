package com.upchiapas.mathenglishquest.ui.screens.dado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.mathenglishquest.data.model.Difficulty
import com.upchiapas.mathenglishquest.data.model.Exercise
import com.upchiapas.mathenglishquest.data.model.ExerciseStep
import com.upchiapas.mathenglishquest.data.model.GameMode
import com.upchiapas.mathenglishquest.data.model.Operation
import com.upchiapas.mathenglishquest.domain.dado.AnswerOptionsGenerator
import com.upchiapas.mathenglishquest.domain.dado.DadoGameState
import com.upchiapas.mathenglishquest.domain.dado.DadoProgressStore
import com.upchiapas.mathenglishquest.domain.dado.DadoRules
import com.upchiapas.mathenglishquest.domain.dado.ExerciseGenerator
import com.upchiapas.mathenglishquest.domain.dado.EvidenceSaver
import com.upchiapas.mathenglishquest.domain.dado.ExpressionFormatter
import com.upchiapas.mathenglishquest.domain.dado.HierarchyEvaluator
import com.upchiapas.mathenglishquest.domain.dado.NumberFormatter
import com.upchiapas.mathenglishquest.domain.howto.HowToBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Cerebro del módulo del Dado. Las reglas (vidas, racha, niveles) viven en [DadoGameState];
 * aquí solo se coordinan el ejercicio en curso, las esperas y el temporizador de Rush.
 */
class DadoViewModel(
    private val userKeyProvider: suspend () -> String,
    private val progressStore: DadoProgressStore,
    private val exerciseGenerator: ExerciseGenerator = ExerciseGenerator(),
    private val optionsGenerator: AnswerOptionsGenerator = AnswerOptionsGenerator(),
    /** true solo para estudiantes con cuenta (no invitados ni maestros). */
    private val evidenceEligibleProvider: suspend () -> Boolean = { false },
    private val evidenceSaver: EvidenceSaver? = null,
    private val nextDelayMs: Long = NEXT_DELAY_MS,
    private val chainDoneDelayMs: Long = CHAIN_DONE_DELAY_MS,
    private val rushTotalMs: Long = DadoRules.RUSH_SECONDS * 1000L
) : ViewModel() {

    private val _uiState = MutableStateFlow(DadoUiState())
    val uiState: StateFlow<DadoUiState> = _uiState.asStateFlow()

    /** Cadena de pasos de los modos Normal y Rush. */
    private class ChainState(
        val terms: List<Int>,
        val operations: List<Operation>,
        var index: Int,
        var accumulated: Double
    )

    private var userKey: String? = null
    private var evidenceEligible = false
    private var currentCategories: List<String> = emptyList()
    private var currentExerciseText: String = ""
    /** Sube cada vez que el ejercicio se reinicia: así una foto que termina tarde no desbloquea otro ejercicio. */
    private var exerciseToken = 0
    private var correctValue: Double = 0.0
    private var answerHandler: ((Boolean) -> Unit)? = null
    private var chain: ChainState? = null
    private var flowJob: Job? = null     // espera entre ejercicios
    private var timerJob: Job? = null    // cuenta regresiva de Rush

    init {
        viewModelScope.launch {
            val key = userKeyProvider()
            userKey = key
            evidenceEligible = evidenceEligibleProvider()
            progressStore.load(key)?.let { progress ->
                val game = DadoGameState.fromProgress(progress)
                _uiState.update { it.copy(game = game) }
                // Si la app se cerró en plena pantalla de "¡Perdiste!", se vuelve a mostrar
                if (game.isGameOver) showGameOver()
            }
        }
    }

    // ---------- Acciones de la pantalla ----------

    fun onModeSelected(mode: GameMode) {
        cancelJobs()
        _uiState.update { it.copy(game = it.game.withMode(mode)).clearedExercise() }
        save()
        showRushWarningIfNeeded()
    }

    /** Cambiar de dificultad NO genera ejercicio nuevo (sería un comodín gratis): aplica en la próxima tirada. */
    fun onDifficultySelected(difficulty: Difficulty) {
        val newGame = _uiState.value.game.withDifficulty(difficulty) ?: return
        _uiState.update { it.copy(game = newGame) }
        save()
        showRushWarningIfNeeded()
    }

    fun onNegativesChanged(enabled: Boolean) {
        _uiState.update { it.copy(game = it.game.withNegatives(enabled)) }
        save()
    }

    fun onRollDice() {
        if (!_uiState.value.canRoll) return
        startExercise()
    }

    /** Comodín: cambia el ejercicio por media vida, sin romper la racha. */
    fun onWildcard() {
        if (!_uiState.value.canUseWildcard) return
        cancelJobs()
        _uiState.update { it.copy(game = it.game.withWildcardUsed()) }
        save()
        if (_uiState.value.game.isGameOver) showGameOver() else startExercise()
    }

    fun onAnswerSelected(value: Double) {
        val state = _uiState.value
        if (!state.exercisePending || state.isAnswered || state.options.isEmpty() || state.awaitingEvidence) return
        timerJob?.cancel()
        _uiState.update {
            it.copy(isAnswered = true, selectedValue = value, correctValue = correctValue, howToUnlocked = true)
        }
        answerHandler?.invoke(value == correctValue)
    }

    /** El estudiante tomó o eligió la foto de su procedimiento: se guarda y recién entonces aparecen las opciones. */
    fun onEvidencePicked(source: String) {
        val saver = evidenceSaver ?: return
        val state = _uiState.value
        if (!state.exercisePending || !state.awaitingEvidence || state.evidenceUploading) return

        val token = exerciseToken
        val difficultyName = state.game.difficulty.displayName
        val categories = currentCategories
        val exerciseText = currentExerciseText
        _uiState.update { it.copy(evidenceUploading = true, evidenceError = false) }
        viewModelScope.launch {
            val saved = saver.save(source, difficultyName, categories, exerciseText)
            if (token != exerciseToken) return@launch   // el ejercicio cambió mientras se guardaba
            _uiState.update {
                if (saved) it.copy(evidenceUploading = false, awaitingEvidence = false)
                else it.copy(evidenceUploading = false, evidenceError = true)
            }
        }
    }

    fun onHowToClick() {
        if (_uiState.value.canOpenHowTo) {
            _uiState.update { it.copy(dialog = DadoDialog.HowTo(HowToBuilder.solutions(it.steps))) }
        }
    }

    fun onDialogDismissed() {
        _uiState.update { it.copy(dialog = null) }
    }

    fun onGameOverDismissed() {
        cancelJobs()
        _uiState.update { it.copy(game = it.game.afterGameOver()).clearedExercise().copy(dialog = null) }
        save()
    }

    // ---------- Ejercicios ----------

    private fun startExercise() {
        cancelJobs()
        val game = _uiState.value.game
        val difficulty = DadoRules.effectiveDifficulty(game.mode, game.difficulty)
        val exercise = exerciseGenerator.generate(difficulty)

        // La foto se pide una sola vez por tirada: en Normal/Rush los pasos siguientes ya no la piden
        val needsEvidence = evidenceEligible && DadoRules.needsEvidence(game.mode, game.difficulty)
        currentCategories = exercise.operations.map { it.category }.distinct()
        currentExerciseText = ExpressionFormatter.chain(exercise.terms, exercise.operations)
        _uiState.update {
            it.clearedExercise().copy(exercisePending = true, awaitingEvidence = needsEvidence)
        }
        if (game.mode == GameMode.DIRECTO) {
            showDirecto(exercise, game.allowNegatives)
        } else {
            chain = ChainState(exercise.terms, exercise.operations, index = 0, accumulated = exercise.terms.first().toDouble())
            showChainStep()
        }
    }

    /** Modo Directo: se muestra la expresión completa y se resuelve respetando la jerarquía. */
    private fun showDirecto(exercise: Exercise, allowNegatives: Boolean) {
        val evaluation = HierarchyEvaluator.evaluate(exercise.terms, exercise.operations, allowNegatives)
        currentCategories = evaluation.operations.map { it.category }.distinct()
        val expression = ExpressionFormatter.display(exercise.terms, evaluation.operations)
        currentExerciseText = expression
        val equation = "$expression ="
        presentExercise(equation, DadoNote.Hierarchy, evaluation.result, evaluation.steps) { correct ->
            if (correct) {
                registerCorrect()
                scheduleNext(nextDelayMs) { startExercise() }
            } else {
                registerWrong()
            }
        }
    }

    /** Modos Normal y Rush: un paso a la vez, arrastrando el resultado anterior. */
    private fun showChainStep() {
        val c = chain ?: return
        val game = _uiState.value.game

        if (c.index >= c.operations.size) {
            _uiState.update {
                it.copy(
                    equation = null,
                    note = DadoNote.ChainDone(NumberFormatter.format(c.accumulated)),
                    options = emptyList(),
                    isAnswered = false,
                    showTimer = false
                )
            }
            scheduleNext(chainDoneDelayMs) { startExercise() }
            return
        }

        val step = DadoRules.chainStep(c.accumulated, c.operations[c.index], c.terms[c.index + 1], game.allowNegatives)
        val equation = "${NumberFormatter.format(step.a)} ${step.operation.symbol} ${NumberFormatter.format(step.b)} ="
        presentExercise(equation, DadoNote.ChainStep(c.index + 1, c.operations.size), step.result, listOf(step)) { correct ->
            if (correct) {
                registerCorrect()
                c.accumulated = step.result
                c.index++
                scheduleNext(nextDelayMs) { showChainStep() }
            } else {
                registerWrong()
            }
        }
        if (game.mode == GameMode.RUSH) startTimer()
    }

    private fun presentExercise(
        equation: String,
        note: DadoNote,
        correct: Double,
        steps: List<ExerciseStep>,
        handler: (Boolean) -> Unit
    ) {
        correctValue = correct
        answerHandler = handler
        _uiState.update {
            it.copy(
                equation = equation,
                note = note,
                options = optionsGenerator.generate(correct, it.game.allowNegatives),
                steps = steps,
                isAnswered = false,
                selectedValue = null,
                correctValue = null,
                howToUnlocked = false
            )
        }
    }

    // ---------- Resultado de cada respuesta ----------

    private fun registerCorrect() {
        _uiState.update { it.copy(game = it.game.withCorrectAnswer()) }
        save()
    }

    /** Pierde un corazón y se espera a que el estudiante vuelva a tirar el dado. */
    private fun registerWrong() {
        _uiState.update { it.copy(game = it.game.withWrongAnswer(), exercisePending = false) }
        save()
        if (_uiState.value.game.isGameOver) showGameOver()
    }

    private fun showGameOver() {
        cancelJobs()
        _uiState.update {
            it.copy(exercisePending = false, dialog = DadoDialog.GameOver(it.game.hadProgressedBeyondEasy))
        }
    }

    private fun showRushWarningIfNeeded() {
        val game = _uiState.value.game
        if (DadoRules.needsRushWarning(game.mode, game.difficulty)) {
            val effective = DadoRules.effectiveDifficulty(game.mode, game.difficulty)
            _uiState.update { it.copy(dialog = DadoDialog.RushWarning(game.difficulty, effective)) }
        }
    }

    // ---------- Esperas y temporizador ----------

    private fun scheduleNext(delayMs: Long, action: () -> Unit) {
        flowJob?.cancel()
        flowJob = viewModelScope.launch {
            delay(delayMs)
            action()
        }
    }

    /** Rush: cuenta regresiva por cada paso. Si se acaba, cuenta como error. */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remaining = rushTotalMs
            _uiState.update { it.copy(showTimer = true, timerProgress = 1f) }
            while (remaining > 0) {
                delay(TIMER_TICK_MS)
                remaining -= TIMER_TICK_MS
                _uiState.update { it.copy(timerProgress = (remaining.toFloat() / rushTotalMs).coerceIn(0f, 1f)) }
            }
            onTimeExpired()
        }
    }

    private fun onTimeExpired() {
        val state = _uiState.value
        if (state.isAnswered || !state.exercisePending) return
        // Se marca como respondido (sin elegir nada) para que ya no se puedan tocar las opciones
        _uiState.update { it.copy(isAnswered = true, selectedValue = null, correctValue = correctValue) }
        registerWrong()
    }

    private fun cancelJobs() {
        exerciseToken++
        flowJob?.cancel()
        timerJob?.cancel()
    }

    // ---------- Utilidades ----------

    private fun save() {
        val key = userKey ?: return
        progressStore.save(key, _uiState.value.game.toProgress())
    }

    /** Deja la pantalla en "Presiona Tirar Dado" (no toca las reglas ni las ventanas). */
    private fun DadoUiState.clearedExercise() = copy(
        equation = null,
        note = DadoNote.None,
        options = emptyList(),
        selectedValue = null,
        correctValue = null,
        isAnswered = false,
        exercisePending = false,
        howToUnlocked = false,
        steps = emptyList(),
        awaitingEvidence = false,
        evidenceUploading = false,
        evidenceError = false,
        showTimer = false,
        timerProgress = 1f
    )

    private companion object {
        const val NEXT_DELAY_MS = 500L
        const val CHAIN_DONE_DELAY_MS = 1200L
        const val TIMER_TICK_MS = 100L
    }
}
