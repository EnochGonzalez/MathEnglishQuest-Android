package com.upchiapas.mathenglishquest.data.remote.model

import com.google.firebase.Timestamp
import com.upchiapas.mathenglishquest.data.remote.FirestoreSchema

/**
 * Documento de `evidencias/{id}` (el id lo genera Firestore).
 *
 * La foto **no** viaja dentro del documento: se sube a Firebase Storage y aquí se guarda
 * solo la ruta (`rutaStorage`), que es lo que descargan los demás celulares.
 */
data class EvidenciaDoc(
    val id: String = "",
    /** UID del estudiante dueño de la foto. */
    val uid: String = "",
    /** Código de su clase, copiado aquí para que el maestro consulte sin leer `usuarios`. */
    val claseId: String? = null,
    /** Ruta en Storage, por ejemplo `evidencias/{uid}/1730000000000.jpg`. */
    val rutaStorage: String = "",
    val fecha: Timestamp? = null,
    val dificultad: String = "",
    /** Operaciones separadas por "|", igual que en Room. */
    val categorias: String = "",
    val ejercicio: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        FirestoreSchema.Evidencia.UID to uid,
        FirestoreSchema.Evidencia.CLASE_ID to claseId,
        FirestoreSchema.Evidencia.RUTA_STORAGE to rutaStorage,
        FirestoreSchema.Evidencia.FECHA to (fecha ?: Timestamp.now()),
        FirestoreSchema.Evidencia.DIFICULTAD to dificultad,
        FirestoreSchema.Evidencia.CATEGORIAS to categorias,
        FirestoreSchema.Evidencia.EJERCICIO to ejercicio
    )

    companion object {
        fun fromMap(id: String, data: Map<String, Any?>): EvidenciaDoc = EvidenciaDoc(
            id = id,
            uid = data[FirestoreSchema.Evidencia.UID] as? String ?: "",
            claseId = data[FirestoreSchema.Evidencia.CLASE_ID] as? String,
            rutaStorage = data[FirestoreSchema.Evidencia.RUTA_STORAGE] as? String ?: "",
            fecha = data[FirestoreSchema.Evidencia.FECHA] as? Timestamp,
            dificultad = data[FirestoreSchema.Evidencia.DIFICULTAD] as? String ?: "",
            categorias = data[FirestoreSchema.Evidencia.CATEGORIAS] as? String ?: "",
            ejercicio = data[FirestoreSchema.Evidencia.EJERCICIO] as? String ?: ""
        )
    }
}
