package com.upchiapas.mathenglishquest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombreCompleto: String,
    val rol: String, // "alumno" o "profesor"
    val claseId: String? = null // código de clase; null si es invitado
)