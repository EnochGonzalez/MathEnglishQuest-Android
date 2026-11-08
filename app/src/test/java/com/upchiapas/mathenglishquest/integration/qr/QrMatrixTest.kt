package com.upchiapas.mathenglishquest.integration.qr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QrMatrixTest {

    // Una cuadrícula de 3x3:  X . X
    //                         . X .
    //                         X . X
    private val matrix = QrMatrix(3, booleanArrayOf(true, false, true, false, true, false, true, false, true))

    @Test
    fun leeCadaModuloEnSuLugar() {
        assertTrue(matrix[0, 0]); assertFalse(matrix[1, 0]); assertTrue(matrix[2, 0])
        assertFalse(matrix[0, 1]); assertTrue(matrix[1, 1]); assertFalse(matrix[2, 1])
        assertTrue(matrix[0, 2]); assertFalse(matrix[1, 2]); assertTrue(matrix[2, 2])
    }

    @Test
    fun fueraDeLaCuadriculaSiempreEsClaro() {
        assertFalse(matrix[-1, 0]); assertFalse(matrix[0, -1])
        assertFalse(matrix[3, 0]); assertFalse(matrix[0, 3]); assertFalse(matrix[99, 99])
    }

    @Test
    fun elTamanoSeConserva() {
        assertEquals(3, matrix.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rechazaUnaListaDeModulosQueNoCuadra() {
        QrMatrix(3, BooleanArray(8))
    }
}
