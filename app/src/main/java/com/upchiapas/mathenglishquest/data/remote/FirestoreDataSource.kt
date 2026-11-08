package com.upchiapas.mathenglishquest.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.upchiapas.mathenglishquest.data.model.UserRole
import com.upchiapas.mathenglishquest.data.remote.model.ClaseDoc
import com.upchiapas.mathenglishquest.data.remote.model.EvidenciaDoc
import com.upchiapas.mathenglishquest.data.remote.model.ProgresoDoc
import com.upchiapas.mathenglishquest.data.remote.model.UsuarioDoc
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Lectura y escritura de las colecciones de Firestore (`usuarios`, `clases`, `evidencias`
 * y `progreso`).
 *
 * No conoce la interfaz ni Room: solo convierte entre documentos de Firestore y los
 * modelos `*Doc`. Los repositorios deciden qué hacer con eso.
 */
class FirestoreDataSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun usuarios() = db.collection(FirestoreSchema.COLECCION_USUARIOS)
    private fun clases() = db.collection(FirestoreSchema.COLECCION_CLASES)
    private fun evidencias() = db.collection(FirestoreSchema.COLECCION_EVIDENCIAS)
    private fun progreso() = db.collection(FirestoreSchema.COLECCION_PROGRESO)

    // ---------- Usuarios ----------

    suspend fun buscarUsuario(uid: String): UsuarioDoc? {
        val snap = usuarios().document(uid).get().awaitTask()
        return if (snap.exists()) UsuarioDoc.fromMap(uid, snap.data.orEmpty()) else null
    }

    /** Crea o reemplaza el perfil. El UID manda: es el id del documento. */
    suspend fun guardarUsuario(usuario: UsuarioDoc) {
        usuarios().document(usuario.uid).set(usuario.toMap()).awaitTask()
    }

    /**
     * Actualiza solo los campos que manda la app sin borrar el resto (por ejemplo, el
     * contador de evidencias que mantiene `registrarEvidenciaEnPerfil`).
     */
    suspend fun actualizarUsuario(uid: String, campos: Map<String, Any?>) {
        usuarios().document(uid).update(campos).awaitTask()
    }

    /** Estudiantes de una clase, en tiempo real. Se ordena en memoria para no exigir índices compuestos. */
    fun observarEstudiantes(claseId: String): Flow<List<UsuarioDoc>> = callbackFlow {
        val registro = usuarios()
            .whereEqualTo(FirestoreSchema.Usuario.CLASE_ID, claseId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val estudiantes = snapshot?.documents
                    ?.mapNotNull { doc -> doc.data?.let { UsuarioDoc.fromMap(doc.id, it) } }
                    ?.filter { it.rol == UserRole.ESTUDIANTE.dbValue }
                    ?.sortedBy { it.nombreCompleto.lowercase() }
                    .orEmpty()
                trySend(estudiantes)
            }
        awaitClose { registro.remove() }
    }

    // ---------- Clases ----------

    /** El id del documento es el código, así que una simple lectura dice si existe. */
    suspend fun buscarClase(codigo: String): ClaseDoc? {
        val snap = clases().document(codigo).get().awaitTask()
        return if (snap.exists()) ClaseDoc.fromMap(codigo, snap.data.orEmpty()) else null
    }

    suspend fun crearClase(clase: ClaseDoc) {
        clases().document(clase.codigo).set(clase.toMap()).awaitTask()
    }

    /** Se usa para deshacer un registro de maestro que falló a medias. */
    suspend fun eliminarClase(codigo: String) {
        clases().document(codigo).delete().awaitTask()
    }

    // ---------- Evidencias ----------

    /** Guarda la ficha de una foto y devuelve el id del documento. */
    suspend fun guardarEvidencia(evidencia: EvidenciaDoc): String =
        evidencias().add(evidencia.toMap()).awaitTask().id

    /** Evidencias de un estudiante, en tiempo real (la más reciente primero). */
    fun observarEvidencias(uid: String): Flow<List<EvidenciaDoc>> = callbackFlow {
        val registro = evidencias()
            .whereEqualTo(FirestoreSchema.Evidencia.UID, uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val docs = snapshot?.documents
                    ?.mapNotNull { doc -> doc.data?.let { EvidenciaDoc.fromMap(doc.id, it) } }
                    ?.sortedByDescending { it.fecha?.seconds ?: 0L }
                    .orEmpty()
                trySend(docs)
            }
        awaitClose { registro.remove() }
    }

    /**
     * Suma una evidencia al contador del perfil, para que el maestro vea el total en su
     * panel sin descargar todas las fotos. `increment` se resuelve en el servidor, así que
     * dos celulares no se pisan.
     */
    suspend fun registrarEvidenciaEnPerfil(uid: String, fechaMillis: Long) {
        usuarios().document(uid).update(
            mapOf(
                FirestoreSchema.Usuario.TOTAL_EVIDENCIAS to FieldValue.increment(1),
                FirestoreSchema.Usuario.ULTIMA_EVIDENCIA to Timestamp(fechaMillis / 1000, 0),
                FirestoreSchema.Usuario.ACTUALIZADO_EN to Timestamp.now()
            )
        ).awaitTask()
    }

    // ---------- Progreso del Dado ----------

    suspend fun buscarProgreso(uid: String): ProgresoDoc? {
        val snap = progreso().document(uid).get().awaitTask()
        return if (snap.exists()) ProgresoDoc.fromMap(snap.data.orEmpty()) else null
    }

    suspend fun guardarProgreso(uid: String, doc: ProgresoDoc) {
        progreso().document(uid).set(doc.toMap()).awaitTask()
    }
}
