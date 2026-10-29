package com.upchiapas.mathenglishquest.domain.howto

import com.upchiapas.mathenglishquest.data.model.ExerciseStep
import com.upchiapas.mathenglishquest.data.model.Operation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HowToTest {

    private fun tables(blocks: List<HowToBlock>) = blocks.filterIsInstance<HowToBlock.Table>()
    private fun texts(blocks: List<HowToBlock>) = blocks.filterIsInstance<HowToBlock.Text>().map { it.text }
    private fun cells(row: TableRow) = row.cells.joinToString("")

    // ---------- Suma en columna ----------

    @Test
    fun suma_conAcarreo() {
        val s = ColumnMath.columnSum(listOf("45", "27"))
        assertEquals(2, s.columns)
        assertEquals(listOf("7", "2"), s.resultRow)
        assertEquals(listOf("1", ""), s.carryRow)      // el 1 de acarreo va sobre las decenas
    }

    @Test
    fun suma_acarreoEnCadena() {
        val s = ColumnMath.columnSum(listOf("99", "1"))
        assertEquals(listOf("1", "0", "0"), s.resultRow)
        assertEquals(listOf("1", "1", ""), s.carryRow)
    }

    @Test
    fun suma_sinAcarreoNoDejaColumnasDeMas() {
        val s = ColumnMath.columnSum(listOf("12", "5"))
        assertEquals(2, s.columns)
        assertEquals(listOf("1", "7"), s.resultRow)
        assertTrue(s.carryRow.all { it.isEmpty() })
    }

    @Test
    fun suma_variosNumeros() {
        val s = ColumnMath.columnSum(listOf("48", "360"))
        assertEquals(listOf("4", "0", "8"), s.resultRow)
    }

    @Test
    fun suma_conDecimales() {
        val s = ColumnMath.columnSum(listOf("0.75", "2.00"))
        assertEquals(listOf("2", ".", "7", "5"), s.resultRow)
    }

    @Test
    fun suma_elAcarreoSaltaElPuntoDecimal() {
        val s = ColumnMath.columnSum(listOf("0.95", "0.10"))
        assertEquals(listOf("1", ".", "0", "5"), s.resultRow)
        // el acarreo de las centésimas llega a las unidades, no se queda en la columna del punto
        assertEquals("1", s.carryRow[0])
    }

    // ---------- Resta ----------

    @Test
    fun resta_pidiendoPrestado() {
        val s = ColumnMath.subtraction("52", "27")
        assertEquals(listOf("2", "5"), s.resultRow)
        assertEquals(listOf("", "12"), s.borrowRow)
        assertFalse(s.negative)
    }

    @Test
    fun resta_prestadoEnCadena() {
        val s = ColumnMath.subtraction("100", "1")
        assertEquals(listOf("", "9", "9"), s.resultRow)       // sin cero a la izquierda
        assertEquals(listOf("", "9", "10"), s.borrowRow)
    }

    @Test
    fun resta_siElSegundoEsMayorSeInvierteYSeAvisa() {
        val blocks = ColumnMath.subtractionBlocks("3", "8")
        assertEquals("5", cells(tables(blocks)[0].rows.last()))
        assertTrue(texts(blocks).any { it.contains("negativo: −5") })
    }

    @Test
    fun resta_conDecimalesConservaElCeroAntesDelPunto() {
        val s = ColumnMath.subtraction("1.00", "0.25")
        assertEquals(listOf("0", ".", "7", "5"), s.resultRow)
    }

    // ---------- Multiplicación ----------

    @Test
    fun multiplicacion_porUnDigitoConAcarreo() {
        val t = tables(ColumnMath.multiplicationBlocks(23, 4))[0]
        assertEquals(listOf("1", ""), t.carryRow)
        assertEquals("92", cells(t.rows.last()))
        assertEquals("4", cells(t.rows[1]))
        assertEquals("×", t.rows[1].op)
    }

    @Test
    fun multiplicacion_porVariosDigitos() {
        val blocks = ColumnMath.multiplicationBlocks(12, 34)
        assertEquals("12 × 34, un dígito de 34 a la vez:", (blocks[0] as HowToBlock.Text).text)
        val t = tables(blocks)
        assertEquals(4, t.size)                                   // encabezado + 2 renglones + suma
        assertEquals("48", cells(t[1].rows[0]).trim())            // 12 × 4
        assertEquals("360", cells(t[2].rows[0]).trim())           // 12 × 30
        assertEquals("408", cells(t.last().rows[0]))
    }

    // ---------- División larga (la casita) ----------

    private fun rowText(row: DivisionRow) = row.cells.joinToString("")
    private fun casita(blocks: List<HowToBlock>) = blocks.first { it is HowToBlock.DivisionCasita } as HowToBlock.DivisionCasita
    private fun quotient(block: HowToBlock.DivisionCasita) = block.quotient.joinToString("")
    private fun steps(blocks: List<HowToBlock>) = blocks.filterIsInstance<HowToBlock.Text>().filter { it.style == HowToTextStyle.STEP }.map { it.text }

    @Test
    fun division_exacta() {
        val run = ColumnMath.division("100", 4)
        assertEquals("25", run.quotientText)
        assertTrue(run.isExact)
        assertEquals(2, casita(run.blocks).rows.count { it.underlineFromCol >= 0 })   // dos restas
    }

    @Test
    fun division_conDecimalesExactos() {
        val run = ColumnMath.division("3", 4)
        assertEquals("0.75", run.quotientText)
        assertFalse(run.truncated)
    }

    @Test
    fun division_queNoTermina() {
        val run = ColumnMath.division("100", 30)
        assertEquals("3.333…", run.quotientText)
        assertTrue(run.truncated)
        assertFalse(run.isExact)
    }

    @Test
    fun division_dividendoMasChicoQueElDivisor() {
        assertEquals("0.075", ColumnMath.division("75", 1000).quotientText)
    }

    @Test
    fun division_cerosEnElCociente() {
        assertEquals("201", ColumnMath.division("1005", 5).quotientText)
        assertEquals("0.008", ColumnMath.division("0.04", 5).quotientText)
        assertEquals("0", ColumnMath.division("0", 5).quotientText)
    }

    /** El ejemplo del usuario: 22 ÷ 5 = 4.4, escrito como en el cuaderno. */
    @Test
    fun casita_22entre5_comoEnElCuaderno() {
        val c = casita(ColumnMath.division("22", 5).blocks)
        assertEquals("5", c.divisor)
        assertEquals("4.4", quotient(c))
        assertEquals("22.0", c.dividend.joinToString(""))              // el 0 agregado es el que se baja
        // −20 / 20 / −20 / 0
        assertEquals(listOf("−20", "20", "−20", "0"), c.rows.map { rowText(it) })
        assertEquals(listOf(true, false, true, false), c.rows.map { it.underlineFromCol >= 0 })
    }

    /** El segundo ejemplo del usuario: 4.4 ÷ 2 = 2.2, conservando el punto del dividendo. */
    @Test
    fun casita_4punto4entre2_conservaElPunto() {
        val c = casita(ColumnMath.division("4.4", 2).blocks)
        assertEquals("2.2", quotient(c))
        assertEquals("4.4", c.dividend.joinToString(""))               // sin ceros agregados
        assertEquals(-1, c.appendedFromCol)
        assertEquals(listOf("−4", "04", "−4", "0"), c.rows.map { rowText(it) })   // el "0 4" que baja
    }

    @Test
    fun casita_losCerosAgregadosSeMarcan() {
        val c = casita(ColumnMath.division("22", 5).blocks)
        assertEquals("0", c.dividend[c.appendedFromCol + 1])           // la columna siguiente al punto es el 0 agregado
        assertTrue(c.columnIsDot[c.appendedFromCol])                   // y la marca empieza en el punto decimal
    }

    @Test
    fun casita_todasLasFilasMidenLoMismoQueLaCuadricula() {
        listOf("22" to 5L, "4.4" to 2L, "100" to 30L, "1005" to 5L, "0.04" to 5L, "12.5" to 5L).forEach { (d, v) ->
            val c = casita(ColumnMath.division(d, v).blocks)
            val n = c.columnIsDot.size
            assertEquals(n, c.quotient.size); assertEquals(n, c.dividend.size)
            c.rows.forEach {
                assertEquals(n, it.cells.size)
                // en las columnas del punto decimal nunca va una cifra ni un signo
                it.cells.forEachIndexed { col, cell -> if (c.columnIsDot[col]) assertEquals("", cell) }
            }
        }
    }

    @Test
    fun casita_explicaCadaPasoConPalabras() {
        val texts = steps(ColumnMath.division("22", 5).blocks)
        assertEquals("En 22, el 5 cabe 4 veces (4 × 5 = 20). Restamos: 22 − 20 = 2.", texts[0])
        assertTrue(texts[1].contains("ponemos el punto decimal en el cociente y bajamos un 0: queda 20"))
        assertEquals("En 20, el 5 cabe 4 veces (4 × 5 = 20). Restamos: 20 − 20 = 0.", texts[2])
        assertEquals("Sobra 0, así que terminamos.", texts[3])
    }

    @Test
    fun casita_cabeUnaVezSeDiceEnSingular() {
        val texts = steps(ColumnMath.division("7", 5).blocks)
        assertTrue(texts[0].contains("cabe 1 vez (1 × 5 = 5)"))
    }

    @Test
    fun casita_resultadoAproximadoLoDice() {
        val last = ColumnMath.division("100", 30).blocks.last() as HowToBlock.Text
        assertEquals("Resultado: 3.333… (aprox.)", last.text)
        assertEquals(HowToTextStyle.RESULT, last.style)
    }

    /**
     * Comprobación matemática del dibujo: cociente × divisor + residuo final (con su posición decimal) = dividendo.
     */
    @Test
    fun casita_elDibujoEsMatematicamenteCorrecto() {
        val cases = listOf("22" to 5L, "4.4" to 2L, "100" to 30L, "3" to 4L, "0.75" to 4L, "1005" to 5L, "0.04" to 5L, "12.5" to 5L, "999" to 7L, "0.5" to 8L)
        cases.forEach { (d, v) ->
            val run = ColumnMath.division(d, v)
            val c = casita(run.blocks)
            val q = java.math.BigDecimal(quotient(c).trimEnd('…'))
            val decimals = q.scale()
            val remainder = java.math.BigDecimal(rowText(c.rows.last()).ifEmpty { "0" })
            val rebuilt = q.multiply(java.math.BigDecimal(v)).add(remainder.movePointLeft(decimals))
            assertEquals("$d ÷ $v", 0L, rebuilt.compareTo(java.math.BigDecimal(d)).toLong())
        }
    }

    // ---------- Pasos completos ----------

    @Test
    fun paso_titulo() {
        val step = ExerciseStep(3.0, Operation.SUMA, 4.0, 7.0)
        assertEquals("Paso 1: Suma — 3 + 4", HowToBuilder.solution(0, step).title)
        val div = ExerciseStep(100.0, Operation.DIVISION, 4.0, 25.0)
        assertEquals("Paso 3: División — 100 ÷ 4", HowToBuilder.solution(2, div).title)
    }

    @Test
    fun paso_sumaDeEnterosNoLlevaNotas() {
        val blocks = HowToBuilder.blocks(45.0, Operation.SUMA, 27.0)
        assertEquals(1, blocks.size)
        assertEquals("72", cells(tables(blocks)[0].rows.last()))
    }

    @Test
    fun paso_sumaConDecimalesAlineaElPunto() {
        val blocks = HowToBuilder.blocks(0.75, Operation.SUMA, 2.0)
        assertTrue(texts(blocks).any { it.contains("Alineamos los puntos decimales") })
        assertEquals("2.75", cells(tables(blocks)[0].rows.last()))
    }

    @Test
    fun paso_sumaConUnNegativoSeConvierteEnResta() {
        // −5 + 3 = −2
        val blocks = HowToBuilder.blocks(-5.0, Operation.SUMA, 3.0)
        assertTrue(texts(blocks).first().contains("se convierte en una resta"))
        assertTrue(texts(blocks).any { it.contains("negativo: −2") })
    }

    @Test
    fun paso_sumaConUnNegativoQueDaPositivo() {
        // −3 + 8 = 5
        val blocks = HowToBuilder.blocks(-3.0, Operation.SUMA, 8.0)
        assertEquals("5", cells(tables(blocks)[0].rows.last()))
        assertFalse(texts(blocks).any { it.contains("negativo: −") })
    }

    @Test
    fun paso_restaAUnNegativoSumaLosValores() {
        // −5 − 3 = −8
        val blocks = HowToBuilder.blocks(-5.0, Operation.RESTA, 3.0)
        assertEquals("8", cells(tables(blocks)[0].rows.last()))
        assertTrue(texts(blocks).any { it.contains("Resultado: −8") })
    }

    @Test
    fun paso_restaNormalDaLaTabla() {
        val blocks = HowToBuilder.blocks(52.0, Operation.RESTA, 27.0)
        assertEquals("25", cells(tables(blocks)[0].rows.last()))
    }

    @Test
    fun paso_multiplicacionConDecimalesQuitaElPunto() {
        // 0.75 × 4 = 3
        val blocks = HowToBuilder.blocks(0.75, Operation.MULTIPLICACION, 4.0)
        assertTrue(texts(blocks).any { it.contains("75 × 4") })
        assertEquals("300", cells(tables(blocks)[0].rows.last()))
        assertTrue(texts(blocks).any { it.contains("2 decimales") })
    }

    @Test
    fun paso_multiplicacionConNegativoLlevaSigno() {
        val blocks = HowToBuilder.blocks(-6.0, Operation.MULTIPLICACION, 7.0)
        assertEquals("42", cells(tables(blocks)[0].rows.last()))
        assertTrue(texts(blocks).any { it.contains("Resultado con signo: -42") || it.contains("Resultado con signo: −42") })
    }

    @Test
    fun paso_divisionConDecimalesConservaElPuntoDelDividendo() {
        // 2.5 ÷ 2 = 1.25: se divide 2.5 tal cual, sin convertirlo en 25 ÷ 20
        val blocks = HowToBuilder.blocks(2.5, Operation.DIVISION, 2.0)
        assertEquals("1.25", quotient(casita(blocks)))
        assertFalse(texts(blocks).any { it.contains("Movemos el punto") })
    }

    @Test
    fun paso_divisionConDivisorConDecimalesSiMueveElPunto() {
        // 3 ÷ 0.5 = 6  ->  30 ÷ 5
        val blocks = HowToBuilder.blocks(3.0, Operation.DIVISION, 0.5)
        assertTrue(texts(blocks).any { it.contains("30 ÷ 5") })
        assertEquals("6", quotient(casita(blocks)))
    }

    @Test
    fun paso_divisionQueSeRedondea() {
        // 0.75 ÷ 4 = 0.1875 -> el ejercicio lo redondea a 0.19
        val blocks = HowToBuilder.blocks(0.75, Operation.DIVISION, 4.0)
        assertTrue(texts(blocks).any { it.contains("se redondea a 2 decimales: 0.19") })
    }

    @Test
    fun paso_divisionAproximada() {
        val blocks = HowToBuilder.blocks(100.0, Operation.DIVISION, 30.0)
        assertEquals("3.333…", quotient(casita(blocks)))
        assertTrue(texts(blocks).any { it.contains("se redondea a 2 decimales: 3.33") })
    }

    /** El ejercicio del usuario: (22 ÷ 5 ÷ 2) + 41 */
    @Test
    fun ejercicioDelUsuario_22entre5entre2mas41() {
        val a = casita(HowToBuilder.blocks(22.0, Operation.DIVISION, 5.0))
        assertEquals("4.4", quotient(a))
        val b = casita(HowToBuilder.blocks(4.4, Operation.DIVISION, 2.0))
        assertEquals("2.2", quotient(b))
        assertEquals("4.4", b.dividend.joinToString(""))
        val sum = HowToBuilder.blocks(2.2, Operation.SUMA, 41.0)
        assertTrue(texts(sum).any { it.contains("Alineamos los puntos decimales") })
        assertEquals("43.2", cells(tables(sum)[0].rows.last()))
    }

    @Test
    fun paso_divisionEntreCeroNoSeCae() {
        val blocks = HowToBuilder.blocks(5.0, Operation.DIVISION, 0.0)
        assertTrue(texts(blocks).first().contains("No se puede dividir entre 0"))
    }

    @Test
    fun paso_numerosGrandesNoRevientan() {
        // Cadenas de multiplicaciones del nivel Muy Difícil pueden llegar a cientos de millones
        val blocks = HowToBuilder.blocks(100_000_000.0, Operation.MULTIPLICACION, 100.0)
        assertEquals("10000000000", cells(tables(blocks).last().rows.last()))
    }

    // ---------- Calculadora personalizada ----------

    @Test
    fun calculadora_aceptaVariasFormas() {
        listOf("100/30", " 45 + 27 ", "6x7", "6X7", "6×7", "8÷2", "9-4").forEach {
            assertTrue("debería aceptar «$it»", HowToBuilder.solveCustom(it) is CustomSolveResult.Success)
        }
    }

    @Test
    fun calculadora_tituloYBloques() {
        val r = HowToBuilder.solveCustom("100/30") as CustomSolveResult.Success
        assertEquals("División — 100 ÷ 30", r.solution.title)
        assertEquals("3.333…", quotient(casita(r.solution.blocks)))
    }

    @Test
    fun calculadora_rechazaLoQueNoEntiende() {
        listOf("", "abc", "5+", "+5", "1.5+2", "3++4", "2+3+4").forEach {
            val r = HowToBuilder.solveCustom(it)
            assertTrue("debería rechazar «$it»", r is CustomSolveResult.Failure)
        }
        assertEquals("Escribe una operación simple, ej: 100/30 o 45+27", (HowToBuilder.solveCustom("abc") as CustomSolveResult.Failure).message)
    }

    @Test
    fun calculadora_noDivideEntreCero() {
        val r = HowToBuilder.solveCustom("5/0") as CustomSolveResult.Failure
        assertEquals("No se puede dividir entre 0", r.message)
    }

    @Test
    fun calculadora_numerosDemasiadoLargos() {
        assertTrue(HowToBuilder.solveCustom("1234567890123*2") is CustomSolveResult.Failure)
    }
}
