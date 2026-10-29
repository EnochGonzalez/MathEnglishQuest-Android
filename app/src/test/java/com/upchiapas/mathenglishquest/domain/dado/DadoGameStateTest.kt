package com.upchiapas.mathenglishquest.domain.dado

import com.upchiapas.mathenglishquest.data.model.Difficulty
import com.upchiapas.mathenglishquest.data.model.GameMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DadoGameStateTest {

    private fun DadoGameState.correct(times: Int): DadoGameState {
        var s = this
        repeat(times) { s = s.withCorrectAnswer() }
        return s
    }

    @Test
    fun inicio_tresCorazonesYSinRacha() {
        val s = DadoGameState()
        assertEquals(6, s.halfHearts)
        assertEquals(0, s.streak)
        assertEquals(Difficulty.FACIL, s.difficulty)
        assertEquals(GameMode.DIRECTO, s.mode)
        assertFalse(s.isGameOver)
    }

    @Test
    fun acierto_sumaRachaYDesbloqueaNiveles() {
        assertEquals(0, DadoGameState().correct(2).maxUnlockedLevel)
        assertEquals(1, DadoGameState().correct(3).maxUnlockedLevel)
        assertEquals(2, DadoGameState().correct(6).maxUnlockedLevel)
        assertEquals(3, DadoGameState().correct(9).maxUnlockedLevel)
        assertEquals(9, DadoGameState().correct(9).streak)
    }

    @Test
    fun error_quitaUnCorazonYRompeLaRachaPeroConservaNivel() {
        val s = DadoGameState().correct(3).withWrongAnswer()
        assertEquals(4, s.halfHearts)
        assertEquals(0, s.streak)
        assertEquals(1, s.maxUnlockedLevel)      // el nivel desbloqueado no se pierde por fallar
    }

    @Test
    fun tresErrores_sonGameOver() {
        var s = DadoGameState()
        s = s.withWrongAnswer(); assertFalse(s.isGameOver)
        s = s.withWrongAnswer(); assertFalse(s.isGameOver)
        s = s.withWrongAnswer(); assertTrue(s.isGameOver)
        assertEquals(0, s.halfHearts)
    }

    @Test
    fun comodin_cuestaMediaVidaYNoRompeLaRacha() {
        val s = DadoGameState().correct(4).withWildcardUsed()
        assertEquals(5, s.halfHearts)
        assertEquals(4, s.streak)
    }

    @Test
    fun comodin_conUnaMitadDeVidaEsGameOver() {
        val s = DadoGameState(halfHearts = 1).withWildcardUsed()
        assertEquals(0, s.halfHearts)
        assertTrue(s.isGameOver)
    }

    @Test
    fun vidasNuncaBajanDeCero() {
        val s = DadoGameState(halfHearts = 1).withWrongAnswer()
        assertEquals(0, s.halfHearts)
    }

    @Test
    fun nivel_bloqueadoNoSePuedeElegir() {
        assertEquals(null, DadoGameState().withDifficulty(Difficulty.MEDIO))
        assertEquals(null, DadoGameState().correct(5).withDifficulty(Difficulty.DIFICIL))
    }

    @Test
    fun nivel_desbloqueadoSePuedeElegirYMarcaProgreso() {
        val s = DadoGameState().correct(3).withDifficulty(Difficulty.MEDIO)!!
        assertEquals(Difficulty.MEDIO, s.difficulty)
        assertTrue(s.hadProgressedBeyondEasy)
    }

    @Test
    fun nivel_elegirFacilNoMarcaProgreso() {
        val s = DadoGameState().withDifficulty(Difficulty.FACIL)!!
        assertFalse(s.hadProgressedBeyondEasy)
    }

    @Test
    fun gameOver_siHabiaPasadoDeFacilVuelveAFacilYBloquea() {
        var s = DadoGameState().correct(3).withDifficulty(Difficulty.MEDIO)!!
        s = s.copy(halfHearts = 0)
        val after = s.afterGameOver()
        assertEquals(6, after.halfHearts)
        assertEquals(0, after.streak)
        assertEquals(0, after.maxUnlockedLevel)
        assertEquals(Difficulty.FACIL, after.difficulty)
        assertFalse(after.hadProgressedBeyondEasy)
    }

    @Test
    fun gameOver_siNuncaPasoDeFacilSoloReiniciaVidasYRacha() {
        // Desbloqueó Medio con la racha pero nunca lo eligió: el desbloqueo se conserva
        val s = DadoGameState().correct(3).copy(halfHearts = 0)
        val after = s.afterGameOver()
        assertEquals(6, after.halfHearts)
        assertEquals(0, after.streak)
        assertEquals(1, after.maxUnlockedLevel)
    }

    @Test
    fun gameOver_noCambiaElModoNiLosNegativos() {
        val s = DadoGameState(mode = GameMode.RUSH, allowNegatives = true, halfHearts = 0).afterGameOver()
        assertEquals(GameMode.RUSH, s.mode)
        assertTrue(s.allowNegatives)
    }

    @Test
    fun progreso_ida_y_vuelta() {
        val original = DadoGameState(
            halfHearts = 3, streak = 7, maxUnlockedLevel = 2, difficulty = Difficulty.MEDIO,
            mode = GameMode.NORMAL, allowNegatives = true, hadProgressedBeyondEasy = true
        )
        assertEquals(original, DadoGameState.fromProgress(original.toProgress()))
    }

    @Test
    fun progreso_valoresRarosSeCorrigen() {
        val s = DadoGameState.fromProgress(
            DadoGameState().toProgress().copy(halfHearts = 99, streak = -4, maxUnlockedLevel = 17, currentLevel = -2)
        )
        assertEquals(6, s.halfHearts)
        assertEquals(0, s.streak)
        assertEquals(3, s.maxUnlockedLevel)
        assertEquals(Difficulty.FACIL, s.difficulty)
    }
}
