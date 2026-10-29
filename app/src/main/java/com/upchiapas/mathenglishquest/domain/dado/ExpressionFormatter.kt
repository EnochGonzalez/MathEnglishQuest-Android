package com.upchiapas.mathenglishquest.domain.dado

import com.upchiapas.mathenglishquest.data.model.Operation

/**
 * Texto de la ecuación para el modo Directo. Los productos y cocientes se agrupan entre paréntesis
 * para que se vea la jerarquía: "3 + (4 × 2)" (buildDisplayExpression del prototipo).
 */
object ExpressionFormatter {

    fun display(terms: List<Int>, operations: List<Operation>): String {
        // Los segmentos se separan en cada + o −
        val segments = mutableListOf<IntRange>()
        var segmentStart = 0
        for (index in operations.indices) {
            if (!operations[index].isMultiplicative) {
                segments += segmentStart..index
                segmentStart = index + 1
            }
        }
        segments += segmentStart..terms.lastIndex

        val parts = segments.map { range ->
            val text = buildString {
                append(terms[range.first])
                for (k in range.first until range.last) {
                    append(" ${operations[k].symbol} ${terms[k + 1]}")
                }
            }
            if (range.last > range.first && segments.size > 1) "($text)" else text
        }

        return buildString {
            append(parts.first())
            for (s in 1 until segments.size) {
                val boundary = operations[segments[s - 1].last]
                append(" ${boundary.symbol} ${parts[s]}")
            }
        }
    }

    /** Texto de una cadena de pasos (modos Normal y Rush), de izquierda a derecha y sin paréntesis: "5 + 3 - 2". */
    fun chain(terms: List<Int>, operations: List<Operation>): String = buildString {
        append(terms.first())
        for (k in operations.indices) append(" ${operations[k].symbol} ${terms[k + 1]}")
    }
}
