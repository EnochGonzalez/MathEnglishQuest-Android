package com.upchiapas.mathenglishquest.data.remote.model

import com.google.firebase.Timestamp
import com.upchiapas.mathenglishquest.data.remote.FirestoreSchema

/**
 * Documento de `progreso/{uid}`: el estado del módulo del Dado para que el estudiante
 * lo recupere al cambiar de celular. En el teléfono se sigue guardando una copia en
 * SharedPreferences para que funcione sin internet.
 */
data class ProgresoDoc(
    val halfHearts: Int = 6,
    val streak: Int = 0,
    val maxUnlockedLevel: Int = 0,
    val currentLevel: Int = 0,
    val mode: String = "DIRECTO",
    val hadProgressedBeyondEasy: Boolean = false,
    val allowNegatives: Boolean = false,
    val actualizadoEn: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        FirestoreSchema.Progreso.HALF_HEARTS to halfHearts.toLong(),
        FirestoreSchema.Progreso.STREAK to streak.toLong(),
        FirestoreSchema.Progreso.MAX_UNLOCKED to maxUnlockedLevel.toLong(),
        FirestoreSchema.Progreso.CURRENT_LEVEL to currentLevel.toLong(),
        FirestoreSchema.Progreso.MODE to mode,
        FirestoreSchema.Progreso.PROGRESSED to hadProgressedBeyondEasy,
        FirestoreSchema.Progreso.NEGATIVES to allowNegatives,
        FirestoreSchema.Progreso.ACTUALIZADO_EN to Timestamp.now()
    )

    companion object {
        fun fromMap(data: Map<String, Any?>): ProgresoDoc = ProgresoDoc(
            halfHearts = (data[FirestoreSchema.Progreso.HALF_HEARTS] as? Number)?.toInt() ?: 6,
            streak = (data[FirestoreSchema.Progreso.STREAK] as? Number)?.toInt() ?: 0,
            maxUnlockedLevel = (data[FirestoreSchema.Progreso.MAX_UNLOCKED] as? Number)?.toInt() ?: 0,
            currentLevel = (data[FirestoreSchema.Progreso.CURRENT_LEVEL] as? Number)?.toInt() ?: 0,
            mode = data[FirestoreSchema.Progreso.MODE] as? String ?: "DIRECTO",
            hadProgressedBeyondEasy = data[FirestoreSchema.Progreso.PROGRESSED] as? Boolean ?: false,
            allowNegatives = data[FirestoreSchema.Progreso.NEGATIVES] as? Boolean ?: false,
            actualizadoEn = data[FirestoreSchema.Progreso.ACTUALIZADO_EN] as? Timestamp
        )
    }
}
