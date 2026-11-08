package com.upchiapas.mathenglishquest.integration.qr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassQrPayloadTest {

    @Test
    fun encode_agregaElPrefijoYPoneMayusculas() {
        assertEquals("MEQ-CLASE:AB3C7D", ClassQrPayload.encode("ab3c7d"))
        assertEquals("MEQ-CLASE:AB3C7D", ClassQrPayload.encode("  AB3C7D "))
    }

    @Test
    fun decode_recuperaElCodigoQueSeCodifico() {
        listOf("AB3C7D", "ZZZZZZ", "234567", "KMNPQR", "AB3C7D2X").forEach {
            assertEquals(it, ClassQrPayload.decode(ClassQrPayload.encode(it)))
        }
    }

    @Test
    fun decode_aceptaElCodigoASecas() {
        assertEquals("AB3C7D", ClassQrPayload.decode("AB3C7D"))
    }

    @Test
    fun decode_toleraMinusculasEspaciosYSaltosDeLinea() {
        assertEquals("AB3C7D", ClassQrPayload.decode("meq-clase:ab3c7d"))
        assertEquals("AB3C7D", ClassQrPayload.decode("  MEQ-CLASE:AB3C7D \n"))
        assertEquals("AB3C7D", ClassQrPayload.decode("\tab3c7d\r\n"))
    }

    @Test
    fun decode_aceptaCodigosDe6a8Caracteres() {
        assertEquals("ABC234", ClassQrPayload.decode("ABC234"))
        assertEquals("ABC2345", ClassQrPayload.decode("ABC2345"))
        assertEquals("ABC23456", ClassQrPayload.decode("ABC23456"))
        assertNull(ClassQrPayload.decode("ABC23"))            // muy corto
        assertNull(ClassQrPayload.decode("ABC234567"))        // muy largo
    }

    @Test
    fun decode_rechazaLoQueNoEsUnaClase() {
        assertNull(ClassQrPayload.decode(null))
        assertNull(ClassQrPayload.decode(""))
        assertNull(ClassQrPayload.decode("   "))
        assertNull(ClassQrPayload.decode("hola mundo"))
        assertNull(ClassQrPayload.decode("https://example.com/promo"))
        assertNull(ClassQrPayload.decode("MEQ-CLASE:"))
        assertNull(ClassQrPayload.decode("OTRA-APP:AB3C7D"))
        assertNull(ClassQrPayload.decode("MEQ-CLASE:MEQ-CLASE:AB3C7D"))
    }

    @Test
    fun decode_rechazaLasLetrasYNumerosQueLaAppNuncaGenera() {
        // El registro del maestro no usa I, O, 0 ni 1 (se confunden al leerlos)
        listOf("ABCDEI", "ABCDEO", "ABCDE0", "ABCDE1", "MEQ-CLASE:ABC0O1").forEach {
            assertNull("«$it»", ClassQrPayload.decode(it))
        }
    }

    @Test
    fun decode_rechazaSimbolosYAcentos() {
        assertNull(ClassQrPayload.decode("ABC-34"))
        assertNull(ClassQrPayload.decode("ÁBC234"))
        assertNull(ClassQrPayload.decode("ABC 234"))
    }

    @Test
    fun alfabeto_eselMismoQueElDelRegistroDelMaestro() {
        // Si Leonardo cambia CODE_CHARS en AuthRepository, esta prueba avisa que el QR debe cambiar igual
        val teacherAlphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        teacherAlphabet.forEach { assertTrue("$it", ClassQrPayload.decode("AAAAA$it") != null) }
        "IO01".forEach { assertFalse("$it", ClassQrPayload.decode("AAAAA$it") != null) }
    }
}
