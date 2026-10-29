package com.upchiapas.mathenglishquest.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["correo"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombreCompleto: String,
    val correo: String,        // siempre en minúsculas
    val passwordHash: String,  // "algoritmo$iteraciones$sal$hash". Nunca se guarda la contraseña en texto plano
    val rol: String,           // "estudiante" o "maestro" (ver UserRole.dbValue)
    val claseId: String? = null // maestro: su código de clase; estudiante: el código al que se unió
)
