package com.upchiapas.mathenglishquest.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Foto del procedimiento de un ejercicio, guardada en el portafolio del estudiante. */
@Entity(
    tableName = "evidencias",
    indices = [Index(value = ["usuarioId"])]
)
data class Evidencia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    /** Archivo de la foto, relativo a la carpeta privada de la app (por ejemplo "evidencias/u1_1730000000000.jpg"). */
    val rutaImagen: String,
    val fecha: Long,
    val dificultad: String,
    /** Operaciones del ejercicio separadas por "|" (por ejemplo "Suma|Resta"). */
    val categorias: String
)
