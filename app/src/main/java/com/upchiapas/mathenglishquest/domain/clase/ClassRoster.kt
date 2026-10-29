package com.upchiapas.mathenglishquest.domain.clase

import com.upchiapas.mathenglishquest.domain.portfolio.PortfolioGrouping
import java.text.Normalizer
import java.util.Locale
import java.util.TimeZone

/** Un estudiante vinculado a la clase, con su resumen de actividad. */
data class StudentSummary(
    val id: Int,
    val name: String,
    val email: String,
    val evidenceCount: Int,
    /** Momento de su última evidencia; null si todavía no ha subido ninguna. */
    val lastActivityMillis: Long?
)

enum class RosterOrder(val label: String) {
    NOMBRE("A-Z"),
    ACTIVIDAD("Más reciente")
}

/** Buscar, ordenar y describir la lista de estudiantes de una clase. Sin nada de Android. */
object ClassRoster {

    /** Ignora mayúsculas y acentos; todas las palabras escritas deben aparecer en el nombre o el correo. */
    fun filter(students: List<StudentSummary>, query: String): List<StudentSummary> {
        val words = normalize(query).split(' ').filter { it.isNotEmpty() }
        if (words.isEmpty()) return students
        return students.filter { student ->
            val haystack = normalize("${student.name} ${student.email}")
            words.all { haystack.contains(it) }
        }
    }

    fun sort(students: List<StudentSummary>, order: RosterOrder): List<StudentSummary> = when (order) {
        RosterOrder.NOMBRE -> students.sortedBy { normalize(it.name) }
        // Más reciente primero; quienes nunca han subido nada van al final, y entre empates se usa el nombre
        RosterOrder.ACTIVIDAD -> students.sortedWith(
            compareByDescending<StudentSummary> { it.lastActivityMillis ?: Long.MIN_VALUE }
                .thenBy { normalize(it.name) }
        )
    }

    /** "3 evidencias subidas · última: Hoy" o "Todavía no sube evidencias". */
    fun activityText(
        student: StudentSummary,
        nowMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault(),
        locale: Locale = Locale("es", "MX")
    ): String {
        val count = student.evidenceCount
        val last = student.lastActivityMillis
        if (count == 0 || last == null) return "Todavía no sube evidencias"
        val plural = if (count == 1) "evidencia subida" else "evidencias subidas"
        return "$count $plural · última: ${PortfolioGrouping.dayLabelOf(last, nowMillis, timeZone, locale)}"
    }

    private fun normalize(text: String): String =
        Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase(Locale.ROOT)
}
