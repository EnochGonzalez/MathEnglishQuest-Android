package com.upchiapas.mathenglishquest.domain.portfolio

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/** Una evidencia del portafolio. [imageFileName] es relativo a la carpeta privada de la app. */
data class EvidenceEntry(
    val id: Int,
    val imageFileName: String,
    val dateMillis: Long,
    val difficulty: String,
    val categories: List<String>
)

/** Evidencias de un mismo día. [dayStartMillis] es la medianoche de ese día. */
data class DayGroup(
    val dayStartMillis: Long,
    val entries: List<EvidenceEntry>
)

object PortfolioGrouping {

    /** Agrupa por día conservando el orden (la más reciente primero), igual que renderPortfolioGallery. */
    fun groupByDay(entries: List<EvidenceEntry>, timeZone: TimeZone = TimeZone.getDefault()): List<DayGroup> {
        val groups = mutableListOf<DayGroup>()
        for (entry in entries) {
            val dayStart = startOfDay(entry.dateMillis, timeZone)
            val last = groups.lastOrNull()
            if (last != null && last.dayStartMillis == dayStart) {
                groups[groups.lastIndex] = last.copy(entries = last.entries + entry)
            } else {
                groups += DayGroup(dayStart, listOf(entry))
            }
        }
        return groups
    }

    /** "Hoy", "Ayer" o "28 de octubre" (con el año si no es el actual). */
    fun dayLabel(
        dayStartMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault(),
        locale: Locale = Locale("es", "MX")
    ): String {
        val today = startOfDay(nowMillis, timeZone)
        if (dayStartMillis == today) return "Hoy"
        if (dayStartMillis == startOfDay(today - 1, timeZone)) return "Ayer"

        val sameYear = yearOf(dayStartMillis, timeZone) == yearOf(nowMillis, timeZone)
        val pattern = if (sameYear) "d 'de' MMMM" else "d 'de' MMMM 'de' yyyy"
        return SimpleDateFormat(pattern, locale).apply { this.timeZone = timeZone }.format(dayStartMillis)
    }

    private fun startOfDay(millis: Long, timeZone: TimeZone): Long =
        Calendar.getInstance(timeZone).apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun yearOf(millis: Long, timeZone: TimeZone): Int =
        Calendar.getInstance(timeZone).apply { timeInMillis = millis }.get(Calendar.YEAR)
}
