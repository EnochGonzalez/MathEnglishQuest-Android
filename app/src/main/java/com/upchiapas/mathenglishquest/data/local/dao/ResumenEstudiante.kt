package com.upchiapas.mathenglishquest.data.local.dao

/** Fila del resumen de un estudiante: cuántas evidencias ha subido y cuándo fue la última. */
data class ResumenEstudiante(
    val id: Int,
    val nombre: String,
    val correo: String,
    val totalEvidencias: Int,
    /** null si todavía no ha subido ninguna. */
    val ultimaFecha: Long?
)
