package com.upchiapas.mathenglishquest.ui.screens.dado

import com.upchiapas.mathenglishquest.data.model.AnswerOption
import com.upchiapas.mathenglishquest.data.model.Difficulty
import com.upchiapas.mathenglishquest.data.model.ExerciseStep
import com.upchiapas.mathenglishquest.data.model.GameMode
import com.upchiapas.mathenglishquest.domain.dado.DadoGameState

/** Texto pequeño debajo de la ecuación. La pantalla decide las palabras exactas. */
sealed interface DadoNote {
    data object None : DadoNote
    data object Hierarchy : DadoNote
    data class ChainStep(val current: Int, val total: Int) : DadoNote
    data class ChainDone(val finalResult: String) : DadoNote
}

/** Ventanas emergentes. */
sealed interface DadoDialog {
    data class RushWarning(val level: Difficulty, val effective: Difficulty) : DadoDialog
    data class GameOver(val hadProgressedBeyondEasy: Boolean) : DadoDialog
    data object HowTo : DadoDialog
}

data class DadoUiState(
    val game: DadoGameState = DadoGameState(),

    // Ejercicio en curso
    /** null = todavía no se ha tirado el dado ("Presiona Tirar Dado"). */
    val equation: String? = null,
    val note: DadoNote = DadoNote.None,
    val options: List<AnswerOption> = emptyList(),
    val selectedValue: Double? = null,
    /** Solo se llena después de responder, para marcar la correcta en verde. */
    val correctValue: Double? = null,
    val isAnswered: Boolean = false,
    val exercisePending: Boolean = false,
    val howToUnlocked: Boolean = false,
    val steps: List<ExerciseStep> = emptyList(),

    // Rush
    val showTimer: Boolean = false,
    val timerProgress: Float = 1f,

    val dialog: DadoDialog? = null
) {
    /** El dado solo se puede tirar si no hay un ejercicio activo. */
    val canRoll: Boolean get() = !exercisePending && !game.isGameOver

    /** El comodín cuesta media vida: necesita ejercicio activo y vidas. */
    val canUseWildcard: Boolean get() = exercisePending && game.halfHearts > 0

    /** "Cómo se resuelve" se habilita al responder (acierto o error) y nunca en Rush. */
    val canOpenHowTo: Boolean get() = game.mode != GameMode.RUSH && howToUnlocked
}
