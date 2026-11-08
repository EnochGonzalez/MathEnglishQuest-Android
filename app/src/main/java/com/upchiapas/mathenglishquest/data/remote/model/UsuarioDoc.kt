package com.upchiapas.mathenglishquest.data.remote.model

import com.google.firebase.Timestamp
import com.upchiapas.mathenglishquest.data.remote.FirestoreSchema

/**
 * Documento de `usuarios/{uid}`.
 *
 * El id del documento es el UID que asigna Firebase Auth, así que nunca se guarda
 * la contraseña: de eso se encarga Auth.
 */
data class UsuarioDoc(
    val uid: String = "",
    val nombreCompleto: String = "",
    val correo: String = "",
    /** "estudiante" o "maestro" (ver [com.upchiapas.mathenglishquest.data.model.UserRole]). */
    val rol: String = "",
    /** Maestro: el código de su clase. Estudiante: el código al que se unió. */
    val claseId: String? = null,
    val creadoEn: Timestamp? = null,
    val actualizadoEn: Timestamp? = null
) {
    /** Formato exacto del documento en Firestore. */
    fun toMap(): Map<String, Any?> = mapOf(
        FirestoreSchema.Usuario.NOMBRE to nombreCompleto,
        FirestoreSchema.Usuario.CORREO to correo,
        FirestoreSchema.Usuario.ROL to rol,
        FirestoreSchema.Usuario.CLASE_ID to claseId,
        FirestoreSchema.Usuario.CREADO_EN to (creadoEn ?: Timestamp.now()),
        FirestoreSchema.Usuario.ACTUALIZADO_EN to Timestamp.now()
    )

    companion object {
        /** Reconstruye el documento a partir de los datos crudos de Firestore. */
        fun fromMap(uid: String, data: Map<String, Any?>): UsuarioDoc = UsuarioDoc(
            uid = uid,
            nombreCompleto = data[FirestoreSchema.Usuario.NOMBRE] as? String ?: "",
            correo = data[FirestoreSchema.Usuario.CORREO] as? String ?: "",
            rol = data[FirestoreSchema.Usuario.ROL] as? String ?: "",
            claseId = data[FirestoreSchema.Usuario.CLASE_ID] as? String,
            creadoEn = data[FirestoreSchema.Usuario.CREADO_EN] as? Timestamp,
            actualizadoEn = data[FirestoreSchema.Usuario.ACTUALIZADO_EN] as? Timestamp
        )
    }
}
