package com.upchiapas.mathenglishquest.data.model

/** Progreso guardado del Dado, por usuario (equivale a saveDadoProgress del prototipo). */
data class DadoProgress(
    val halfHearts: Int,
    val streak: Int,
    val maxUnlockedLevel: Int,
    val currentLevel: Int,
    val mode: GameMode,
    val hadProgressedBeyondEasy: Boolean,
    val allowNegatives: Boolean
)
