package com.upchiapas.mathenglishquest.domain.howto

import com.upchiapas.mathenglishquest.data.model.ExerciseStep
import com.upchiapas.mathenglishquest.data.model.Operation
import com.upchiapas.mathenglishquest.domain.dado.NumberFormatter
import com.upchiapas.mathenglishquest.domain.dado.applyOperation
import com.upchiapas.mathenglishquest.domain.dado.round2
import java.math.BigDecimal
import kotlin.math.max

/** Resultado de la calculadora "¿Quieres resolver otra operación?". */
sealed interface CustomSolveResult {
    data class Success(val solution: HowToSolution) : CustomSolveResult
    data class Failure(val message: String) : CustomSolveResult
}

/**
 * Convierte un paso (a, operación, b) en su procedimiento paso a paso.
 * El prototipo solo manejaba enteros positivos; aquí también se explican los decimales (resultado de una división)
 * y los números negativos (cuando el modo Negativos está activado).
 */
object HowToBuilder {

    fun solution(index: Int, step: ExerciseStep): HowToSolution = HowToSolution(
        title = "Paso ${index + 1}: ${step.operation.category} — ${format(step.a)} ${step.operation.symbol} ${format(step.b)}",
        blocks = blocks(step.a, step.operation, step.b)
    )

    fun solutions(steps: List<ExerciseStep>): List<HowToSolution> = steps.mapIndexed(::solution)

    fun blocks(a: Double, operation: Operation, b: Double): List<HowToBlock> {
        val x = toDecimal(a)
        val y = toDecimal(b)
        return when (operation) {
            Operation.SUMA -> add(x, y)
            Operation.RESTA -> subtract(x, y)
            Operation.MULTIPLICACION -> multiply(x, y, a, b)
            Operation.DIVISION -> divide(x, y, a, b)
        }
    }

    // ---------- Calculadora personalizada ----------

    private val CUSTOM_PATTERN = Regex("^(\\d+)([+\\-*/])(\\d+)$")

    /** Acepta "100/30", "45+27", "6x7", "8÷2"... (solo enteros positivos, como el prototipo). */
    fun solveCustom(input: String): CustomSolveResult {
        val cleaned = input.replace(Regex("[×xX]"), "*").replace("÷", "/").replace(Regex("\\s+"), "")
        val match = CUSTOM_PATTERN.matchEntire(cleaned)
            ?: return CustomSolveResult.Failure("Escribe una operación simple, ej: 100/30 o 45+27")

        val a = match.groupValues[1].toLongOrNull()
        val b = match.groupValues[3].toLongOrNull()
        if (a == null || b == null || a > MAX_CUSTOM || b > MAX_CUSTOM) {
            return CustomSolveResult.Failure("Usa números de hasta 9 dígitos")
        }
        val operation = when (match.groupValues[2]) {
            "+" -> Operation.SUMA
            "-" -> Operation.RESTA
            "*" -> Operation.MULTIPLICACION
            else -> Operation.DIVISION
        }
        if (operation == Operation.DIVISION && b == 0L) {
            return CustomSolveResult.Failure("No se puede dividir entre 0")
        }

        val title = "${operation.category} — $a ${operation.symbol} $b"
        return CustomSolveResult.Success(HowToSolution(title, blocks(a.toDouble(), operation, b.toDouble())))
    }

    private const val MAX_CUSTOM = 999_999_999L

    // ---------- Suma ----------

    private fun add(x: BigDecimal, y: BigDecimal): List<HowToBlock> = when {
        x.signum() >= 0 && y.signum() >= 0 -> additionBlocks(x, y)
        x.signum() < 0 && y.signum() < 0 ->
            listOf(note("Los dos números son negativos: se suman sus valores y el resultado lleva signo negativo.")) +
                additionBlocks(x.abs(), y.abs()) +
                note("Resultado: −${plain(x.abs().add(y.abs()))}")
        x.signum() < 0 ->
            listOf(note("Como ${plain(x)} es negativo, esta suma se convierte en una resta: ${plain(y)} − ${plain(x.abs())}.")) +
                subtractionBlocks(y, x.abs())
        else ->
            listOf(note("Sumar ${plain(y)} equivale a restar su valor: ${plain(x)} − ${plain(y.abs())}.")) +
                subtractionBlocks(x, y.abs())
    }

    // ---------- Resta ----------

    private fun subtract(x: BigDecimal, y: BigDecimal): List<HowToBlock> = when {
        x.signum() >= 0 && y.signum() >= 0 -> subtractionBlocks(x, y)
        x.signum() < 0 && y.signum() >= 0 ->
            listOf(note("Restarle un número a un negativo: se suman los valores y el resultado es negativo.")) +
                additionBlocks(x.abs(), y) +
                note("Resultado: −${plain(x.abs().add(y))}")
        x.signum() >= 0 ->
            listOf(note("Restar un número negativo equivale a sumar: ${plain(x)} + ${plain(y.abs())}.")) +
                additionBlocks(x, y.abs())
        else ->
            listOf(note("Con dos negativos: ${plain(x)} − (${plain(y)}) = ${plain(y.abs())} − ${plain(x.abs())}.")) +
                subtractionBlocks(y.abs(), x.abs())
    }

    // ---------- Multiplicación ----------

    private fun multiply(x: BigDecimal, y: BigDecimal, a: Double, b: Double): List<HowToBlock> {
        val negative = (x.signum() < 0) != (y.signum() < 0) && x.signum() != 0 && y.signum() != 0
        val ax = x.abs()
        val ay = y.abs()
        val decimals = ax.scale() + ay.scale()
        val blocks = mutableListOf<HowToBlock>()

        if (x.signum() < 0 || y.signum() < 0) {
            blocks += note("Un negativo por un positivo da negativo; dos negativos dan positivo. Multiplicamos los valores sin signo.")
        }
        val left = ax.movePointRight(ax.scale()).toLong()
        val right = ay.movePointRight(ay.scale()).toLong()
        if (decimals > 0) {
            blocks += note("Quitamos los puntos decimales y multiplicamos como números enteros: $left × $right.")
        }
        blocks += ColumnMath.multiplicationBlocks(left, right)
        if (decimals > 0) {
            blocks += note("Entre los dos números hay $decimals decimal${if (decimals == 1) "" else "es"}, así que el resultado también: ${plain(ax.multiply(ay))}")
        }
        if (negative) blocks += note("Resultado con signo: ${format(round2(applyOperation(a, Operation.MULTIPLICACION, b)))}")
        return blocks
    }

    // ---------- División ----------

    private fun divide(x: BigDecimal, y: BigDecimal, a: Double, b: Double): List<HowToBlock> {
        if (y.signum() == 0) return listOf(note("No se puede dividir entre 0"))
        val negative = (x.signum() < 0) != (y.signum() < 0) && x.signum() != 0
        val ax = x.abs()
        val ay = y.abs()
        val blocks = mutableListOf<HowToBlock>()

        if (x.signum() < 0 || y.signum() < 0) {
            blocks += note("Un negativo entre un positivo da negativo; dos negativos dan positivo. Dividimos los valores sin signo.")
        }

        // Con divisor entero (lo normal) se divide tal cual, conservando el punto decimal del dividendo.
        // Solo si el divisor tuviera decimales se mueve el punto igual en los dos números.
        val moved = ay.scale()
        val dividendText: String
        val divisor: Long
        if (moved == 0) {
            dividendText = plain(ax)
            divisor = ay.toLong()
        } else {
            val shiftedDividend = ax.movePointRight(moved)
            dividendText = plain(shiftedDividend)
            divisor = ay.movePointRight(moved).toLong()
            blocks += note("Movemos el punto decimal igual en los dos números: $dividendText ÷ $divisor.")
        }

        val run = ColumnMath.division(dividendText, divisor)
        blocks += run.blocks

        val result = round2(applyOperation(a, Operation.DIVISION, b))
        val decimalsShown = run.quotientText.substringAfter('.', "").trimEnd('…').length
        if (decimalsShown > 2 || run.truncated) {
            blocks += note("En el ejercicio el resultado se redondea a 2 decimales: ${format(result)}")
        }
        if (negative) blocks += note("Resultado con signo: ${format(result)}")
        return blocks
    }

    // ---------- Piezas comunes ----------

    private fun additionBlocks(x: BigDecimal, y: BigDecimal): List<HowToBlock> {
        val (a, b) = aligned(x, y)
        val blocks = mutableListOf<HowToBlock>()
        if (a.contains('.')) blocks += note("Alineamos los puntos decimales.")
        blocks += ColumnMath.additionBlocks(a, b)
        return blocks
    }

    private fun subtractionBlocks(x: BigDecimal, y: BigDecimal): List<HowToBlock> {
        val (a, b) = aligned(x, y)
        val blocks = mutableListOf<HowToBlock>()
        if (a.contains('.')) blocks += note("Alineamos los puntos decimales.")
        blocks += ColumnMath.subtractionBlocks(a, b)
        return blocks
    }

    /** Dos números positivos con los mismos decimales: 0.75 y 2 -> "0.75" y "2.00". */
    private fun aligned(x: BigDecimal, y: BigDecimal): Pair<String, String> {
        val scale = max(x.scale(), y.scale())
        return x.setScale(scale).toPlainString() to y.setScale(scale).toPlainString()
    }

    /** Número redondeado a 2 decimales y sin ceros sobrantes (escala 0 si es entero). */
    private fun toDecimal(value: Double): BigDecimal {
        val d = BigDecimal.valueOf(round2(value)).stripTrailingZeros()
        return if (d.scale() < 0) d.setScale(0) else d
    }

    private fun plain(value: BigDecimal): String = value.stripTrailingZeros().let {
        if (it.scale() < 0) it.setScale(0).toPlainString() else it.toPlainString()
    }

    private fun format(value: Double): String = NumberFormatter.format(value)

    private fun note(text: String) = HowToBlock.Text(text, HowToTextStyle.NOTE)
}
