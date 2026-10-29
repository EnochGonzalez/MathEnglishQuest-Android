package com.upchiapas.mathenglishquest.domain.howto

import java.math.BigDecimal

/**
 * Las cuentas "a mano": suma en columna con acarreos, resta pidiendo prestado, multiplicación y división larga.
 * Trabajan con números como TEXTO de dígitos (con un punto si hay decimales, y ya alineados), igual que en papel.
 */
internal object ColumnMath {

    private const val DOT = "."

    /** "45" con longitud 5 -> ["", "", "", "4", "5"] */
    fun padCells(text: String, length: Int): List<String> =
        List((length - text.length).coerceAtLeast(0)) { "" } + text.map { it.toString() }

    // ---------- Suma en columna ----------

    class ColumnSum(
        val columns: Int,
        val carryRow: List<String>,
        val rows: List<List<String>>,
        val resultRow: List<String>
    )

    /** Suma varios números alineados a la derecha (columnSumData del prototipo, con soporte para el punto decimal). */
    fun columnSum(numbers: List<String>): ColumnSum {
        val length = numbers.maxOf { it.length } + 1
        val rows = numbers.map { padCells(it, length) }
        val carryRow = MutableList(length) { "" }
        val resultRow = MutableList(length) { "" }
        var carry = 0

        for (i in length - 1 downTo 0) {
            if (rows[0][i] == DOT) {          // la columna del punto solo se copia al resultado
                resultRow[i] = DOT
                continue
            }
            var sum = carry
            rows.forEach { sum += it[i].toIntOrNull() ?: 0 }
            resultRow[i] = (sum % 10).toString()
            carry = sum / 10
            if (carry > 0 && i > 0) {
                var target = i - 1
                if (rows[0][target] == DOT) target-- // el acarreo salta por encima del punto
                if (target >= 0) carryRow[target] = carry.toString()
            }
        }

        // Quita las columnas vacías de la izquierda
        var start = 0
        while (start < length - 1 && rows.all { it[start].isEmpty() } &&
            (resultRow[start].isEmpty() || resultRow[start] == "0")
        ) start++

        return ColumnSum(
            columns = length - start,
            carryRow = carryRow.drop(start),
            rows = rows.map { it.drop(start) },
            resultRow = resultRow.drop(start)
        )
    }

    fun additionBlocks(a: String, b: String): List<HowToBlock> {
        val sum = columnSum(listOf(a, b))
        return listOf(
            HowToBlock.Table(
                carryRow = sum.carryRow,
                rows = listOf(
                    TableRow("", sum.rows[0]),
                    TableRow("+", sum.rows[1], hasLine = true),
                    TableRow("", sum.resultRow, isResult = true)
                )
            )
        )
    }

    // ---------- Resta pidiendo prestado ----------

    class Subtraction(
        val borrowRow: List<String>,
        val rows: List<List<String>>,
        val resultRow: List<String>,
        val negative: Boolean
    )

    /** Si [b] es mayor que [a] se restan al revés y se marca el resultado como negativo (subtractionData). */
    fun subtraction(a: String, b: String): Subtraction {
        val negative = BigDecimal(b) > BigDecimal(a)
        val top = if (negative) b else a
        val bottom = if (negative) a else b

        val length = top.length
        val topCells = padCells(top, length)
        val bottomCells = padCells(bottom, length)
        val result = MutableList(length) { "" }
        val borrowRow = MutableList(length) { "" }
        var borrow = 0

        for (i in length - 1 downTo 0) {
            if (topCells[i] == DOT) {
                result[i] = DOT
                continue
            }
            var da = topCells[i].toIntOrNull() ?: 0
            val db = bottomCells[i].toIntOrNull() ?: 0
            da -= borrow
            if (da < db) {
                da += 10
                borrow = 1
                borrowRow[i] = da.toString()
            } else {
                borrow = 0
            }
            result[i] = (da - db).toString()
        }

        // Los ceros de la izquierda no se escriben (100 − 1 = 99, no 099), pero "0.75" conserva su 0
        var k = 0
        while (k < length - 1 && result[k] == "0" && result[k + 1] != DOT) {
            result[k] = ""
            k++
        }

        return Subtraction(borrowRow, listOf(topCells, bottomCells), result, negative)
    }

    fun subtractionBlocks(a: String, b: String): List<HowToBlock> {
        val s = subtraction(a, b)
        val blocks = mutableListOf<HowToBlock>(
            HowToBlock.Table(
                borrowRow = s.borrowRow,
                rows = listOf(
                    TableRow("", s.rows[0]),
                    TableRow("-", s.rows[1], hasLine = true),
                    TableRow("", s.resultRow, isResult = true)
                )
            )
        )
        if (s.negative) {
            blocks += HowToBlock.Text(
                "Como el segundo número es mayor, el resultado real es negativo: −${s.resultRow.joinToString("").trim()}",
                HowToTextStyle.NOTE
            )
        }
        return blocks
    }

    // ---------- Multiplicación ----------

    fun multiplicationBlocks(a: Long, b: Long): List<HowToBlock> {
        val bText = b.toString()
        return if (bText.length == 1) singleDigitMultiplication(a, b.toInt()) else multiDigitMultiplication(a, b)
    }

    /** Multiplicar por un solo dígito, con acarreos. */
    private fun singleDigitMultiplication(a: Long, b: Int): List<HowToBlock> {
        val aText = a.toString()
        val length = aText.length + 1
        val aCells = padCells(aText, length)
        val result = MutableList(length) { "" }
        val carryRow = MutableList(length) { "" }
        var carry = 0

        for (i in length - 1 downTo 0) {
            val da = aCells[i].toIntOrNull() ?: 0
            val product = da * b + carry
            result[i] = (product % 10).toString()
            carry = product / 10
            if (carry > 0 && i > 0) carryRow[i - 1] = carry.toString()
        }

        var start = 0
        while (start < length - 1 && aCells[start].isEmpty() && (result[start].isEmpty() || result[start] == "0")) start++

        return listOf(
            HowToBlock.Table(
                carryRow = carryRow.drop(start),
                rows = listOf(
                    TableRow("", aCells.drop(start)),
                    TableRow("×", padCells(b.toString(), length).drop(start), hasLine = true),
                    TableRow("", result.drop(start), isResult = true)
                )
            )
        )
    }

    /** Multiplicar por varios dígitos: un renglón por cada dígito de [b] y luego se suman. */
    private fun multiDigitMultiplication(a: Long, b: Long): List<HowToBlock> {
        val bText = b.toString()
        val partials = bText.reversed().mapIndexed { index, ch ->
            var p = a * (ch - '0')
            repeat(index) { p *= 10 }
            p
        }
        val sum = columnSum(partials.map { it.toString() })
        val width = partials.maxOf { it.toString().length } + 1

        val blocks = mutableListOf<HowToBlock>(
            HowToBlock.Text("$a × $b, un dígito de $b a la vez:", HowToTextStyle.LABEL),
            HowToBlock.Table(
                rows = listOf(
                    TableRow("", padCells(a.toString(), width)),
                    TableRow("×", padCells(bText, width), hasLine = true)
                )
            )
        )
        partials.forEach { p ->
            blocks += HowToBlock.Table(rows = listOf(TableRow("+", padCells(p.toString(), sum.columns))))
        }
        blocks += HowToBlock.Table(rows = listOf(TableRow("", sum.resultRow, hasLine = true, isResult = true)))
        return blocks
    }

    // ---------- División larga ("la casita") ----------

    /** Resultado de dividir: lo que se dibuja y los datos que necesita quien lo usa. */
    class DivisionRun(
        val blocks: List<HowToBlock>,
        /** "4.4", "25", "0.75" o "3.333…" */
        val quotientText: String,
        val isExact: Boolean,
        val truncated: Boolean
    )

    /** Máximo de decimales que se calculan en el cociente. */
    private const val MAX_DECIMALS = 3

    private class DrawnStep(
        val digitIndex: Int,
        /** Número parcial con el que se arma la fila previa (resto + cifra bajada); null en la primera resta. */
        val partialRowText: String?,
        val partialWidth: Int,
        val partialValue: Long,
        val quotientDigit: Long,
        val product: Long,
        val remainder: Long
    )

    /**
     * División larga como se escribe en el cuaderno. [dividendText] es un número sin signo, entero o con decimales
     * ("22", "4.4", "0.75"); [divisor] es un entero mayor que 0. Si sobra residuo se agrega el punto decimal en el
     * cociente y se baja un 0, hasta 3 decimales.
     */
    fun division(dividendText: String, divisor: Long): DivisionRun {
        val dotIndex = dividendText.indexOf('.')
        val hasDot = dotIndex >= 0
        val intPart = if (hasDot) dividendText.substring(0, dotIndex) else dividendText
        val decPart = if (hasDot) dividendText.substring(dotIndex + 1) else ""
        val intCount = intPart.length
        val realDigits = (intPart + decPart).map { it - '0' }

        // ---- 1) Simular la división, cifra por cifra ----
        val quotient = sortedMapOf<Int, Long>()     // cifra del cociente por índice de dígito
        val drawn = mutableListOf<DrawnStep>()
        val explanation = mutableListOf<String>()
        var current = 0L
        var started = false
        var partialText = ""                        // solo se lleva después de la primera resta
        var appended = 0
        var index = 0

        while (true) {
            val isReal = index < realDigits.size
            val brought: Int
            if (isReal) {
                brought = realDigits[index]
            } else {
                val decimalsSoFar = index - intCount
                if (current == 0L || decimalsSoFar >= MAX_DECIMALS) break
                brought = 0
                appended++
            }

            val drawnBefore = drawn.isNotEmpty()
            current = current * 10 + brought
            if (drawnBefore) partialText += brought.toString()

            // Frase de "bajar" (solo cuando ya hubo una resta y se baja una cifra)
            if (drawnBefore) {
                explanation += when {
                    !isReal && appended == 1 && !hasDot ->
                        "Como sobra residuo, ponemos el punto decimal en el cociente y bajamos un 0: queda $current."
                    !isReal && appended == 1 -> "Como todavía sobra residuo, bajamos un 0: queda $current."
                    !isReal -> "Bajamos otro 0: queda $current."
                    index == intCount && hasDot -> "Ponemos el punto decimal en el cociente y bajamos el $brought: queda $current."
                    else -> "Bajamos el $brought: queda $current."
                }
            }

            val q = current / divisor
            if (q > 0) started = true
            val inDecimals = index >= intCount
            if (started || inDecimals) quotient[index] = q

            if (q > 0) {
                val product = q * divisor
                val remainder = current - product
                val width = if (drawnBefore) partialText.length else current.toString().length
                drawn += DrawnStep(
                    digitIndex = index,
                    partialRowText = if (drawnBefore) partialText else null,
                    partialWidth = width,
                    partialValue = current,
                    quotientDigit = q,
                    product = product,
                    remainder = remainder
                )
                val times = if (q == 1L) "1 vez" else "$q veces"
                explanation += "En $current, el $divisor cabe $times ($q × $divisor = $product). " +
                    "Restamos: $current − $product = $remainder."
                current = remainder
                partialText = remainder.toString()
            } else if (started || inDecimals) {
                if (drawnBefore) {
                    explanation += "El $divisor no cabe en $current: ponemos 0 en el cociente."
                }
            }
            index++
        }

        val processed = index                      // cifras tratadas (reales + agregadas)
        val isExact = current == 0L
        val truncated = !isExact
        if (drawn.isNotEmpty()) {
            explanation += if (isExact) "Sobra 0, así que terminamos." else "Todavía sobra $current: lo dejamos aproximado."
        }

        // ---- 2) Armar la cuadrícula ----
        val columnIsDot = mutableListOf(false)       // columna 0: el signo "−"
        val digitCols = mutableListOf<Int>()
        repeat(intCount) { digitCols += columnIsDot.size; columnIsDot += false }
        var dotCol = -1
        if (hasDot || appended > 0) {
            dotCol = columnIsDot.size
            columnIsDot += true
        }
        repeat(processed - intCount) { digitCols += columnIsDot.size; columnIsDot += false }
        val columns = columnIsDot.size

        val dividendCells = MutableList(columns) { "" }
        realDigits.forEachIndexed { i, d -> dividendCells[digitCols[i]] = d.toString() }
        for (i in realDigits.size until processed) dividendCells[digitCols[i]] = "0"
        if (dotCol >= 0) dividendCells[dotCol] = "."
        val appendedFromCol = when {
            appended == 0 -> -1
            hasDot -> digitCols[realDigits.size]
            else -> dotCol
        }

        val quotientCells = MutableList(columns) { "" }
        quotient.forEach { (i, q) -> quotientCells[digitCols[i]] = q.toString() }
        if (dotCol >= 0) quotientCells[dotCol] = "."
        if (intCount > 0 && quotient.keys.none { it < intCount }) quotientCells[digitCols[intCount - 1]] = "0"
        if (truncated && quotient.isNotEmpty()) {
            val last = digitCols[quotient.lastKey()]
            quotientCells[last] = quotientCells[last] + "…"
        }

        /** Columnas de dígitos que ocupan las últimas [count] cifras que terminan en [endIndex]. */
        fun colsEndingAt(endIndex: Int, count: Int): List<Int> {
            val n = minOf(count, endIndex + 1)
            return (endIndex - n + 1..endIndex).map { digitCols[it] }
        }

        fun textRow(text: String, endIndex: Int): DivisionRow {
            val cells = MutableList(columns) { "" }
            val shown = text.takeLast(endIndex + 1)
            colsEndingAt(endIndex, shown.length).forEachIndexed { k, col -> cells[col] = shown[k].toString() }
            return DivisionRow(cells)
        }

        val rows = mutableListOf<DivisionRow>()
        drawn.forEach { step ->
            step.partialRowText?.let { rows += textRow(it, step.digitIndex) }

            val productText = step.product.toString()
            val cells = MutableList(columns) { "" }
            val productCols = colsEndingAt(step.digitIndex, productText.length)
            productCols.forEachIndexed { k, col -> cells[col] = productText[productText.length - productCols.size + k].toString() }
            // El "−" va en la cifra anterior más cercana (o en la columna del signo)
            val signCol = digitCols.lastOrNull { it < productCols.first() } ?: 0
            cells[signCol] = "−"
            val partialFirst = colsEndingAt(step.digitIndex, step.partialWidth).first()
            rows += DivisionRow(cells, underlineFromCol = minOf(signCol, partialFirst), underlineToCol = digitCols[step.digitIndex])
        }
        if (drawn.isNotEmpty()) rows += textRow(partialText, processed - 1)

        val casita = HowToBlock.DivisionCasita(
            divisor = divisor.toString(),
            columnIsDot = columnIsDot,
            quotient = quotientCells,
            dividend = dividendCells,
            appendedFromCol = appendedFromCol,
            rows = rows
        )
        val quotientText = quotientCells.joinToString("")
        val blocks = mutableListOf<HowToBlock>(casita)
        explanation.forEach { blocks += HowToBlock.Text(it, HowToTextStyle.STEP) }
        blocks += HowToBlock.Text(
            "Resultado: $quotientText${if (isExact) "" else " (aprox.)"}",
            HowToTextStyle.RESULT
        )
        return DivisionRun(blocks, quotientText, isExact, truncated)
    }
}
