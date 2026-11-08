package com.upchiapas.mathenglishquest.data.remote.model

import com.google.firebase.Timestamp
import com.upchiapas.mathenglishquest.data.remote.FirestoreSchema

/**
 * Documento de `clases/{codigo}`.
 *
 * El id del documento **es** el código de clase (6 caracteres), por eso no se puede
 * repetir: Firestore ya no permite crear dos documentos con el mismo id. Eso reemplaza
 * la búsqueda `buscarMaestroPorCodigo` que hoy hace Room.
 */
data class ClaseDoc(
    val codigo: String = "",
    val maestroUid: String = "",
    val maestroNombre: String = "",
    val creadoEn: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        FirestoreSchema.Clase.CODIGO to codigo,
        FirestoreSchema.Clase.MAESTRO_UID to maestroUid,
        FirestoreSchema.Clase.MAESTRO_NOMBRE to maestroNombre,
        FirestoreSchema.Clase.CREADO_EN to (creadoEn ?: Timestamp.now())
    )

    companion object {
        fun fromMap(codigo: String, data: Map<String, Any?>): ClaseDoc = ClaseDoc(
            codigo = data[FirestoreSchema.Clase.CODIGO] as? String ?: codigo,
            maestroUid = data[FirestoreSchema.Clase.MAESTRO_UID] as? String ?: "",
            maestroNombre = data[FirestoreSchema.Clase.MAESTRO_NOMBRE] as? String ?: "",
            creadoEn = data[FirestoreSchema.Clase.CREADO_EN] as? Timestamp
        )
    }
}
