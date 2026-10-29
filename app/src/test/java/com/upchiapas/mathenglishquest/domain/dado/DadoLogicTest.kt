package com.upchiapas.mathenglishquest.domain.dado

import com.upchiapas.mathenglishquest.data.model.Difficulty
import com.upchiapas.mathenglishquest.data.model.GameMode
import com.upchiapas.mathenglishquest.data.model.Operation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DadoLogicTest {

    private val S = Operation.SUMA
    private val R = Operation.RESTA
    private val M = Operation.MULTIPLICACION
    private val D = Operation.DIVISION

    // ---------- Jerarquía de operaciones ----------

    @Test
    fun jerarquia_multiplicaPrimero() {
        // 3 + 4 × 2 = 11
        val r = HierarchyEvaluator.evaluate(listOf(3, 4, 2), listOf(S, M), allowNegatives = false)
        assertEquals(11.0, r.result, 0.0001)
        assertEquals(2, r.steps.size)
        assertEquals(M, r.steps[0].operation)   // el producto va primero
        assertEquals(8.0, r.steps[0].result, 0.0001)
        assertEquals(S, r.steps[1].operation)
    }

    @Test
    fun jerarquia_restaNegativaSeCambiaPorSuma() {
        // 10 - 4 × 3: 10 - 12 sería negativo, así que pasa a 10 + 12 = 22
        val r = HierarchyEvaluator.evaluate(listOf(10, 4, 3), listOf(R, M), allowNegatives = false)
        assertEquals(22.0, r.result, 0.0001)
        assertEquals(listOf(S, M), r.operations)
    }

    @Test
    fun jerarquia_conNegativosPermitidos() {
        val r = HierarchyEvaluator.evaluate(listOf(10, 4, 3), listOf(R, M), allowNegatives = true)
        assertEquals(-2.0, r.result, 0.0001)
        assertEquals(listOf(R, M), r.operations)
    }

    @Test
    fun jerarquia_divisionConDecimales() {
        val r = HierarchyEvaluator.evaluate(listOf(3, 4), listOf(D), allowNegatives = false)
        assertEquals(0.75, r.result, 0.0001)
    }

    @Test
    fun jerarquia_izquierdaADerecha() {
        // 8 ÷ 4 × 3 = (8 ÷ 4) × 3 = 6
        val r = HierarchyEvaluator.evaluate(listOf(8, 4, 3), listOf(D, M), allowNegatives = false)
        assertEquals(6.0, r.result, 0.0001)
        assertEquals(D, r.steps[0].operation)
    }

    @Test
    fun jerarquia_variosProductos() {
        // 2 × 3 + 4 × 5 = 26
        val r = HierarchyEvaluator.evaluate(listOf(2, 3, 4, 5), listOf(M, S, M), allowNegatives = false)
        assertEquals(26.0, r.result, 0.0001)
        assertEquals(3, r.steps.size)
    }

    @Test
    fun jerarquia_noModificaLaListaOriginal() {
        val ops = listOf(R, M)
        HierarchyEvaluator.evaluate(listOf(10, 4, 3), ops, allowNegatives = false)
        assertEquals(listOf(R, M), ops)
    }

    // ---------- Texto de la ecuación ----------

    @Test
    fun formato_agrupaProductosConParentesis() {
        assertEquals("3 + (4 × 2)", ExpressionFormatter.display(listOf(3, 4, 2), listOf(S, M)))
        assertEquals("(2 × 3) + (4 × 5)", ExpressionFormatter.display(listOf(2, 3, 4, 5), listOf(M, S, M)))
    }

    @Test
    fun formato_sinParentesisSiEsUnSoloBloque() {
        assertEquals("2 × 3 × 4", ExpressionFormatter.display(listOf(2, 3, 4), listOf(M, M)))
        assertEquals("5 + 3", ExpressionFormatter.display(listOf(5, 3), listOf(S)))
        assertEquals("9 - 4 + 2", ExpressionFormatter.display(listOf(9, 4, 2), listOf(R, S)))
    }

    @Test
    fun formato_usaLosSimbolosDeMostrar() {
        assertEquals("12 ÷ 4", ExpressionFormatter.display(listOf(12, 4), listOf(D)))
    }

    // ---------- Cadena (modos Normal y Rush) ----------

    @Test
    fun cadena_restaNegativaPasaASuma() {
        val step = DadoRules.chainStep(3.0, R, 8, allowNegatives = false)
        assertEquals(S, step.operation)
        assertEquals(11.0, step.result, 0.0001)
    }

    @Test
    fun cadena_restaNormalSeRespeta() {
        val step = DadoRules.chainStep(9.0, R, 4, allowNegatives = false)
        assertEquals(R, step.operation)
        assertEquals(5.0, step.result, 0.0001)
    }

    @Test
    fun cadena_negativosPermitidos() {
        val step = DadoRules.chainStep(3.0, R, 8, allowNegatives = true)
        assertEquals(-5.0, step.result, 0.0001)
    }

    // ---------- Opciones de respuesta ----------

    @Test
    fun opciones_tresDistintasConLaCorrecta() {
        repeat(200) { seed ->
            val options = AnswerOptionsGenerator(Random(seed)).generate(7.0, allowNegatives = false)
            assertEquals(3, options.size)
            assertEquals(3, options.map { it.value }.toSet().size)
            assertTrue(options.any { it.value == 7.0 })
        }
    }

    @Test
    fun opciones_sinNegativosNuncaSaleNegativo() {
        repeat(200) { seed ->
            val options = AnswerOptionsGenerator(Random(seed)).generate(1.0, allowNegatives = false)
            assertTrue(options.all { it.value >= 0 })
        }
    }

    @Test
    fun opciones_conDecimalesUsanSaltosChicos() {
        repeat(100) { seed ->
            val options = AnswerOptionsGenerator(Random(seed)).generate(0.75, allowNegatives = false)
            assertEquals(3, options.map { it.value }.toSet().size)
            assertTrue(options.all { Math.abs(it.value - 0.75) <= 1.25 + 0.0001 || it.value >= 0 })
        }
    }

    @Test
    fun opciones_correctaConCeroSinNegativosNoSeAtora() {
        repeat(100) { seed ->
            val options = AnswerOptionsGenerator(Random(seed)).generate(0.0, allowNegatives = false)
            assertEquals(3, options.size)
        }
    }

    // ---------- Generador ----------

    @Test
    fun generador_respetaCadaDificultad() {
        Difficulty.entries.forEach { difficulty ->
            val generator = ExerciseGenerator(Random(difficulty.ordinal))
            repeat(300) {
                val e = generator.generate(difficulty)
                assertEquals(difficulty.termCount, e.terms.size)
                assertEquals(difficulty.termCount - 1, e.operations.size)
                assertTrue(e.terms.first() in 1..difficulty.maxNumber)
                e.operations.forEachIndexed { index, op ->
                    assertTrue(op in difficulty.operations)
                    val operand = e.terms[index + 1]
                    if (op == Operation.DIVISION) {
                        assertTrue(operand in ExerciseGenerator.DIVISORS)
                    } else {
                        assertTrue(operand in 1..difficulty.maxNumber)
                    }
                }
            }
        }
    }

    // ---------- Reglas ----------

    @Test
    fun reglas_desbloqueoPorRacha() {
        assertEquals(0, DadoRules.unlockedLevelForStreak(0))
        assertEquals(0, DadoRules.unlockedLevelForStreak(2))
        assertEquals(1, DadoRules.unlockedLevelForStreak(3))
        assertEquals(1, DadoRules.unlockedLevelForStreak(5))
        assertEquals(2, DadoRules.unlockedLevelForStreak(6))
        assertEquals(3, DadoRules.unlockedLevelForStreak(9))
        assertEquals(3, DadoRules.unlockedLevelForStreak(40))
    }

    @Test
    fun reglas_rushUsaUnNivelMasBajo() {
        assertEquals(Difficulty.FACIL, DadoRules.effectiveDifficulty(GameMode.RUSH, Difficulty.FACIL))
        assertEquals(Difficulty.FACIL, DadoRules.effectiveDifficulty(GameMode.RUSH, Difficulty.MEDIO))
        assertEquals(Difficulty.MEDIO, DadoRules.effectiveDifficulty(GameMode.RUSH, Difficulty.DIFICIL))
        assertEquals(Difficulty.DIFICIL, DadoRules.effectiveDifficulty(GameMode.RUSH, Difficulty.MUY_DIFICIL))
        assertEquals(Difficulty.DIFICIL, DadoRules.effectiveDifficulty(GameMode.DIRECTO, Difficulty.DIFICIL))
        assertEquals(Difficulty.MUY_DIFICIL, DadoRules.effectiveDifficulty(GameMode.NORMAL, Difficulty.MUY_DIFICIL))
    }

    @Test
    fun reglas_avisoDeRushSoloDesdeDificil() {
        assertFalse(DadoRules.needsRushWarning(GameMode.RUSH, Difficulty.FACIL))
        assertFalse(DadoRules.needsRushWarning(GameMode.RUSH, Difficulty.MEDIO))
        assertTrue(DadoRules.needsRushWarning(GameMode.RUSH, Difficulty.DIFICIL))
        assertTrue(DadoRules.needsRushWarning(GameMode.RUSH, Difficulty.MUY_DIFICIL))
        assertFalse(DadoRules.needsRushWarning(GameMode.DIRECTO, Difficulty.MUY_DIFICIL))
    }

    // ---------- Formato de números ----------

    @Test
    fun numeros_seMuestranSinCerosSobrantes() {
        assertEquals("5", NumberFormatter.format(5.0))
        assertEquals("0.75", NumberFormatter.format(0.75))
        assertEquals("2.5", NumberFormatter.format(2.5))
        assertEquals("-3", NumberFormatter.format(-3.0))
        assertEquals("0", NumberFormatter.format(0.0))
        assertEquals("3.33", NumberFormatter.format(10.0 / 3.0))
    }

    // ---------- Foto de evidencia ----------

    @Test
    fun evidencia_directoDesdeMedio() {
        assertFalse(DadoRules.needsEvidence(GameMode.DIRECTO, Difficulty.FACIL))
        assertTrue(DadoRules.needsEvidence(GameMode.DIRECTO, Difficulty.MEDIO))
        assertTrue(DadoRules.needsEvidence(GameMode.DIRECTO, Difficulty.DIFICIL))
        assertTrue(DadoRules.needsEvidence(GameMode.DIRECTO, Difficulty.MUY_DIFICIL))
    }

    @Test
    fun evidencia_normalDesdeDificil() {
        assertFalse(DadoRules.needsEvidence(GameMode.NORMAL, Difficulty.FACIL))
        assertFalse(DadoRules.needsEvidence(GameMode.NORMAL, Difficulty.MEDIO))
        assertTrue(DadoRules.needsEvidence(GameMode.NORMAL, Difficulty.DIFICIL))
        assertTrue(DadoRules.needsEvidence(GameMode.NORMAL, Difficulty.MUY_DIFICIL))
    }

    @Test
    fun evidencia_rushNunca() {
        Difficulty.entries.forEach { assertFalse(DadoRules.needsEvidence(GameMode.RUSH, it)) }
    }
}
