package com.upchiapas.mathenglishquest.data.model

/**
 * Niveles de dificultad (LEVELS de state.js).
 * [unlockStreak] es la racha de aciertos que desbloquea el nivel.
 */
enum class Difficulty(
    val displayName: String,
    val shortName: String,
    val maxNumber: Int,
    val operations: List<Operation>,
    val termCount: Int,
    val unlockStreak: Int
) {
    FACIL("Fácil", "Fácil", 10, listOf(Operation.SUMA, Operation.RESTA), 2, 0),
    MEDIO("Medio", "Medio", 20, listOf(Operation.SUMA, Operation.RESTA, Operation.MULTIPLICACION), 3, 3),
    DIFICIL(
        "Difícil", "Difícil", 50,
        listOf(Operation.SUMA, Operation.RESTA, Operation.MULTIPLICACION, Operation.DIVISION), 4, 6
    ),
    MUY_DIFICIL(
        "Muy Difícil", "Muy Dif.", 100,
        listOf(Operation.SUMA, Operation.RESTA, Operation.MULTIPLICACION, Operation.DIVISION), 5, 9
    )
}
