package com.upchiapas.mathenglishquest.data.model

/** Rol que se muestra en la insignia del header (en el prototipo: Estudiante / Maestro(a) / Invitado). */
enum class UserRole(val label: String) {
    ESTUDIANTE("Estudiante"),
    MAESTRO("Maestro(a)"),
    INVITADO("Invitado")
}
