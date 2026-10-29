package com.upchiapas.mathenglishquest.domain.dado

import com.upchiapas.mathenglishquest.data.model.DadoProgress
import com.upchiapas.mathenglishquest.data.model.Difficulty
import com.upchiapas.mathenglishquest.data.model.GameMode

/**
 * Estado de las reglas del juego: vidas, racha, niveles desbloqueados, modo y dificultad.
 * Es inmutable y no depende de Android: cada acción devuelve un estado nuevo.
 * Las vidas se cuentan en mitades (6 = 3 corazones) porque el comodín cuesta media vida.
 */
data class DadoGameState(
    val halfHearts: Int = DadoRules.MAX_HALF_HEARTS,
    val streak: Int = 0,
    val maxUnlockedLevel: Int = 0,
    val difficulty: Difficulty = Difficulty.FACIL,
    val mode: GameMode = GameMode.DIRECTO,
    val allowNegatives: Boolean = false,
    val hadProgressedBeyondEasy: Boolean = false
) {
    val isGameOver: Boolean get() = halfHearts <= 0

    /** gainCorrect: suma a la racha y desbloquea niveles (3 → Medio, 6 → Difícil, 9 → Muy Difícil). */
    fun withCorrectAnswer(): DadoGameState {
        val newStreak = streak + 1
        return copy(
            streak = newStreak,
            maxUnlockedLevel = maxOf(maxUnlockedLevel, DadoRules.unlockedLevelForStreak(newStreak))
        )
    }

    /** loseFullHeart: pierde un corazón completo y la racha vuelve a 0 (el nivel desbloqueado se conserva). */
    fun withWrongAnswer(): DadoGameState =
        copy(halfHearts = maxOf(0, halfHearts - 2), streak = 0)

    /** loseHalfHeart: el comodín cuesta media vida y NO rompe la racha. */
    fun withWildcardUsed(): DadoGameState =
        copy(halfHearts = maxOf(0, halfHearts - 1))

    /** setLevel: devuelve null si ese nivel todavía está bloqueado. */
    fun withDifficulty(newDifficulty: Difficulty): DadoGameState? {
        if (newDifficulty.ordinal > maxUnlockedLevel) return null
        return copy(
            difficulty = newDifficulty,
            hadProgressedBeyondEasy = hadProgressedBeyondEasy || newDifficulty.ordinal > 0
        )
    }

    fun withMode(newMode: GameMode): DadoGameState = copy(mode = newMode)

    fun withNegatives(enabled: Boolean): DadoGameState = copy(allowNegatives = enabled)

    /** closeGameOver: vidas y racha se reinician; si había pasado de Fácil, se pierde ese progreso. */
    fun afterGameOver(): DadoGameState {
        val base = copy(halfHearts = DadoRules.MAX_HALF_HEARTS, streak = 0)
        return if (hadProgressedBeyondEasy) {
            base.copy(maxUnlockedLevel = 0, difficulty = Difficulty.FACIL, hadProgressedBeyondEasy = false)
        } else {
            base
        }
    }

    fun toProgress() = DadoProgress(
        halfHearts = halfHearts,
        streak = streak,
        maxUnlockedLevel = maxUnlockedLevel,
        currentLevel = difficulty.ordinal,
        mode = mode,
        hadProgressedBeyondEasy = hadProgressedBeyondEasy,
        allowNegatives = allowNegatives
    )

    companion object {
        fun fromProgress(p: DadoProgress) = DadoGameState(
            halfHearts = p.halfHearts.coerceIn(0, DadoRules.MAX_HALF_HEARTS),
            streak = maxOf(0, p.streak),
            maxUnlockedLevel = p.maxUnlockedLevel.coerceIn(0, DadoRules.MAX_DIFFICULTY_INDEX),
            difficulty = Difficulty.entries[p.currentLevel.coerceIn(0, DadoRules.MAX_DIFFICULTY_INDEX)],
            mode = p.mode,
            allowNegatives = p.allowNegatives,
            hadProgressedBeyondEasy = p.hadProgressedBeyondEasy
        )
    }
}
