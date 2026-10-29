package com.upchiapas.mathenglishquest.domain.dado

import com.upchiapas.mathenglishquest.data.model.ExerciseStep
import com.upchiapas.mathenglishquest.data.model.Operation

data class HierarchyResult(
    val result: Double,
    val steps: List<ExerciseStep>,
    /** Operaciones finales: una resta que daría negativo se cambia por suma cuando no se permiten negativos. */
    val operations: List<Operation>
)

fun applyOperation(a: Double, operation: Operation, b: Double): Double = when (operation) {
    Operation.SUMA -> a + b
    Operation.RESTA -> a - b
    Operation.MULTIPLICACION -> a * b
    Operation.DIVISION -> a / b
}

/** Resuelve respetando la jerarquía: primero × y ÷ de izquierda a derecha, luego + y − (evaluateHierarchy). */
object HierarchyEvaluator {

    private class Entry(var operation: Operation, val index: Int)

    fun evaluate(terms: List<Int>, operations: List<Operation>, allowNegatives: Boolean): HierarchyResult {
        val values = terms.map { it.toDouble() }.toMutableList()
        val entries = operations.mapIndexed { index, op -> Entry(op, index) }.toMutableList()
        val adjusted = operations.toMutableList()
        val steps = mutableListOf<ExerciseStep>()

        // 1) Multiplicaciones y divisiones
        var i = 0
        while (i < entries.size) {
            val entry = entries[i]
            if (entry.operation.isMultiplicative) {
                val a = values[i]
                val b = values[i + 1]
                val result = round2(applyOperation(a, entry.operation, b))
                steps += ExerciseStep(a, entry.operation, b, result)
                values.removeAt(i + 1)
                values[i] = result
                entries.removeAt(i)
            } else {
                i++
            }
        }

        // 2) Sumas y restas
        for (j in entries.indices) {
            val entry = entries[j]
            if (entry.operation == Operation.RESTA && !allowNegatives && values[j] - values[j + 1] < 0) {
                entry.operation = Operation.SUMA
                adjusted[entry.index] = Operation.SUMA
            }
            val a = values[j]
            val b = values[j + 1]
            val result = round2(applyOperation(a, entry.operation, b))
            steps += ExerciseStep(a, entry.operation, b, result)
            values[j + 1] = result
        }

        return HierarchyResult(result = values.last(), steps = steps, operations = adjusted)
    }
}
