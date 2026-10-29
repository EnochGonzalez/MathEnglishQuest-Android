package com.upchiapas.mathenglishquest.domain.dado

import com.upchiapas.mathenglishquest.data.model.Difficulty
import com.upchiapas.mathenglishquest.data.model.Exercise
import com.upchiapas.mathenglishquest.data.model.Operation
import kotlin.random.Random

/** Genera ejercicios al azar según la dificultad (buildChain del prototipo). */
open class ExerciseGenerator(private val random: Random = Random.Default) {

    open fun generate(difficulty: Difficulty): Exercise {
        val terms = mutableListOf(randInt(difficulty.maxNumber))
        val operations = mutableListOf<Operation>()
        repeat(difficulty.termCount - 1) {
            val operation = difficulty.operations.random(random)
            val value = if (operation == Operation.DIVISION) DIVISORS.random(random) else randInt(difficulty.maxNumber)
            operations += operation
            terms += value
        }
        return Exercise(terms, operations)
    }

    /** Número entero entre 1 y [max], ambos incluidos. */
    private fun randInt(max: Int): Int = random.nextInt(1, max + 1)

    companion object {
        /** Divisores permitidos para que las divisiones sean cómodas (DIVISORS de state.js). */
        val DIVISORS = listOf(2, 4, 5, 10)
    }
}
