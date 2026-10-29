package com.upchiapas.mathenglishquest.domain.dado

import com.upchiapas.mathenglishquest.data.model.Difficulty
import com.upchiapas.mathenglishquest.data.model.ExerciseStep
import com.upchiapas.mathenglishquest.data.model.GameMode
import com.upchiapas.mathenglishquest.data.model.Operation

/** Reglas del juego, sin nada de Android para poder probarlas fácil. */
object DadoRules {
    /** 3 corazones, contados en mitades (el comodín cuesta media vida). */
    const val MAX_HALF_HEARTS = 6
    const val RUSH_SECONDS = 6
    const val MAX_DIFFICULTY_INDEX = 3

    /** Rush usa una dificultad más baja para que dé tiempo de responder. */
    fun effectiveDifficulty(mode: GameMode, difficulty: Difficulty): Difficulty =
        if (mode == GameMode.RUSH) Difficulty.entries[maxOf(0, difficulty.ordinal - 1)] else difficulty

    /** Avisar del ajuste de Rush solo desde Difícil (índice 2). */
    fun needsRushWarning(mode: GameMode, difficulty: Difficulty): Boolean =
        mode == GameMode.RUSH && difficulty.ordinal >= 2

    /**
     * ¿Este ejercicio exige foto del procedimiento? (exerciseNeedsEvidence del prototipo)
     * Directo la pide desde Medio; Normal desde Difícil; Rush nunca. La dificultad que cuenta es la elegida,
     * no la efectiva. Que el usuario sea un estudiante con cuenta se revisa aparte.
     */
    fun needsEvidence(mode: GameMode, difficulty: Difficulty): Boolean {
        if (mode == GameMode.RUSH) return false
        val threshold = if (mode == GameMode.DIRECTO) Difficulty.MEDIO.ordinal else Difficulty.DIFICIL.ordinal
        return difficulty.ordinal >= threshold
    }

    /** Nivel máximo que se alcanza con esa racha: 3 → Medio, 6 → Difícil, 9 → Muy Difícil. */
    fun unlockedLevelForStreak(streak: Int): Int =
        Difficulty.entries.lastOrNull { streak >= it.unlockStreak }?.ordinal ?: 0

    /** Paso de una cadena (modos Normal y Rush): una resta que daría negativo se cambia por suma. */
    fun chainStep(accumulated: Double, operation: Operation, next: Int, allowNegatives: Boolean): ExerciseStep {
        var op = operation
        if (op == Operation.RESTA && !allowNegatives && accumulated - next < 0) op = Operation.SUMA
        val b = next.toDouble()
        return ExerciseStep(
            a = round2(accumulated),
            operation = op,
            b = b,
            result = round2(applyOperation(accumulated, op, b))
        )
    }
}
