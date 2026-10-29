package com.upchiapas.mathenglishquest.domain.portfolio

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class PortfolioGroupingTest {

    private val tz = TimeZone.getTimeZone("America/Mexico_City")
    private val es = Locale("es", "MX")

    private fun millis(year: Int, month: Int, day: Int, hour: Int = 12, minute: Int = 0): Long =
        Calendar.getInstance(tz).apply {
            clear()
            set(year, month - 1, day, hour, minute, 0)
        }.timeInMillis

    private fun entry(id: Int, at: Long) = EvidenceEntry(id, "evidencias/u1_$id.jpg", at, "Medio", listOf("Suma"))

    @Test
    fun agrupa_porDiaConservandoElOrden() {
        val entries = listOf(
            entry(1, millis(2026, 10, 28, 15)),
            entry(2, millis(2026, 10, 28, 9)),
            entry(3, millis(2026, 10, 27, 20)),
            entry(4, millis(2026, 10, 20, 8))
        )
        val groups = PortfolioGrouping.groupByDay(entries, tz)
        assertEquals(3, groups.size)
        assertEquals(listOf(1, 2), groups[0].entries.map { it.id })
        assertEquals(listOf(3), groups[1].entries.map { it.id })
        assertEquals(listOf(4), groups[2].entries.map { it.id })
    }

    @Test
    fun agrupa_listaVaciaNoDaGrupos() {
        assertEquals(0, PortfolioGrouping.groupByDay(emptyList(), tz).size)
    }

    @Test
    fun agrupa_cercaDeMedianocheSeparaLosDias() {
        val entries = listOf(
            entry(1, millis(2026, 10, 28, 0, 5)),
            entry(2, millis(2026, 10, 27, 23, 55))
        )
        assertEquals(2, PortfolioGrouping.groupByDay(entries, tz).size)
    }

    @Test
    fun etiqueta_hoyYAyer() {
        val now = millis(2026, 10, 28, 14)
        val today = PortfolioGrouping.groupByDay(listOf(entry(1, now)), tz)[0].dayStartMillis
        val yesterday = PortfolioGrouping.groupByDay(listOf(entry(2, millis(2026, 10, 27, 23, 30))), tz)[0].dayStartMillis
        assertEquals("Hoy", PortfolioGrouping.dayLabel(today, now, tz, es))
        assertEquals("Ayer", PortfolioGrouping.dayLabel(yesterday, now, tz, es))
    }

    @Test
    fun etiqueta_fechaDelMismoAnoSinAno() {
        val now = millis(2026, 10, 28, 14)
        val day = PortfolioGrouping.groupByDay(listOf(entry(1, millis(2026, 10, 5))), tz)[0].dayStartMillis
        assertEquals("5 de octubre", PortfolioGrouping.dayLabel(day, now, tz, es))
    }

    @Test
    fun etiqueta_otroAnoLlevaAno() {
        val now = millis(2026, 1, 10, 14)
        val day = PortfolioGrouping.groupByDay(listOf(entry(1, millis(2025, 12, 31))), tz)[0].dayStartMillis
        assertEquals("31 de diciembre de 2025", PortfolioGrouping.dayLabel(day, now, tz, es))
    }

    @Test
    fun etiqueta_ayerCruzandoElFinDeMes() {
        val now = millis(2026, 11, 1, 10)
        val day = PortfolioGrouping.groupByDay(listOf(entry(1, millis(2026, 10, 31, 22))), tz)[0].dayStartMillis
        assertEquals("Ayer", PortfolioGrouping.dayLabel(day, now, tz, es))
    }
}
