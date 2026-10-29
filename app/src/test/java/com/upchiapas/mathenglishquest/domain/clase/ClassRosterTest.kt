package com.upchiapas.mathenglishquest.domain.clase

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ClassRosterTest {

    private val tz = TimeZone.getTimeZone("America/Mexico_City")
    private val es = Locale("es", "MX")

    private fun millis(year: Int, month: Int, day: Int, hour: Int = 12): Long =
        Calendar.getInstance(tz).apply { clear(); set(year, month - 1, day, hour, 0, 0) }.timeInMillis

    private fun s(id: Int, name: String, count: Int = 0, last: Long? = null, email: String = "e$id@mail.com") =
        StudentSummary(id, name, email, count, last)

    private val ana = s(1, "Ana López Pérez", 3, millis(2026, 10, 27))
    private val angel = s(2, "Ángel Gómez Ruiz", 0, null)
    private val beto = s(3, "Beto Díaz Mora", 7, millis(2026, 10, 28))
    private val zoe = s(4, "zoe Álvarez Cruz", 1, millis(2026, 10, 1))
    private val all = listOf(zoe, beto, angel, ana)

    // ---------- Buscar ----------

    @Test
    fun buscar_sinTextoDevuelveTodos() {
        assertEquals(all, ClassRoster.filter(all, ""))
        assertEquals(all, ClassRoster.filter(all, "   "))
    }

    @Test
    fun buscar_ignoraMayusculasYAcentos() {
        assertEquals(listOf(angel), ClassRoster.filter(all, "angel"))
        assertEquals(listOf(angel), ClassRoster.filter(all, "ÁNGEL"))
        assertEquals(listOf(ana), ClassRoster.filter(all, "lopez"))
    }

    @Test
    fun buscar_variasPalabrasDebenAparecerTodas() {
        assertEquals(listOf(ana), ClassRoster.filter(all, "ana perez"))
        assertEquals(emptyList<StudentSummary>(), ClassRoster.filter(all, "ana ruiz"))
    }

    @Test
    fun buscar_tambienPorCorreo() {
        assertEquals(listOf(beto), ClassRoster.filter(all, "e3@mail"))
    }

    // ---------- Ordenar ----------

    @Test
    fun ordenar_porNombreSinImportarAcentosNiMayusculas() {
        // Ana, Ángel, Beto, zoe (la "Á" de Ángel cuenta como A; "zoe" en minúscula va al final)
        assertEquals(listOf(1, 2, 3, 4), ClassRoster.sort(all, RosterOrder.NOMBRE).map { it.id })
    }

    @Test
    fun ordenar_porActividadMasRecientePrimeroYSinActividadAlFinal() {
        assertEquals(listOf(3, 1, 4, 2), ClassRoster.sort(all, RosterOrder.ACTIVIDAD).map { it.id })
    }

    @Test
    fun ordenar_empatesDeActividadSeDesempatanPorNombre() {
        val a = s(1, "Zulema Ruiz Soto")
        val b = s(2, "Abel Ruiz Soto")
        assertEquals(listOf(2, 1), ClassRoster.sort(listOf(a, b), RosterOrder.ACTIVIDAD).map { it.id })
    }

    // ---------- Texto de actividad ----------

    @Test
    fun actividad_sinEvidencias() {
        assertEquals("Todavía no sube evidencias", ClassRoster.activityText(angel, millis(2026, 10, 28), tz, es))
    }

    @Test
    fun actividad_singularYPlural() {
        val now = millis(2026, 10, 28)
        assertEquals("1 evidencia subida · última: 1 de octubre", ClassRoster.activityText(zoe, now, tz, es))
        assertEquals("7 evidencias subidas · última: Hoy", ClassRoster.activityText(beto, now, tz, es))
        assertEquals("3 evidencias subidas · última: Ayer", ClassRoster.activityText(ana, now, tz, es))
    }
}
