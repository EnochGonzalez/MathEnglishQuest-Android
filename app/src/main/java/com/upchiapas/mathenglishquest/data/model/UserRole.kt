package com.upchiapas.mathenglishquest.data.model

/**
 * Rol del usuario. [label] es el texto de la insignia del header; [dbValue] es lo que se guarda en Room.
 */
enum class UserRole(val label: String, val dbValue: String) {
    ESTUDIANTE("Estudiante", "estudiante"),
    MAESTRO("Maestro(a)", "maestro"),
    INVITADO("Invitado", "invitado");

    companion object {
        fun fromDb(value: String): UserRole =
            entries.firstOrNull { it.dbValue == value } ?: ESTUDIANTE
    }
}
