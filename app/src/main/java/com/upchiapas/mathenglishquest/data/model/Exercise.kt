package com.upchiapas.mathenglishquest.data.model

/** Un ejercicio: términos y operaciones entre ellos. Siempre hay una operación menos que términos. */
data class Exercise(
    val terms: List<Int>,
    val operations: List<Operation>
)

/** Un paso del procedimiento: a (op) b = result. */
data class ExerciseStep(
    val a: Double,
    val operation: Operation,
    val b: Double,
    val result: Double
)

/** Una opción de respuesta. [value] sirve para comparar y [text] es lo que se muestra. */
data class AnswerOption(
    val value: Double,
    val text: String
)
