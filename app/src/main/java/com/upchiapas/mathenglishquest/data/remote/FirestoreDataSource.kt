package com.upchiapas.mathenglishquest.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.upchiapas.mathenglishquest.data.model.UserRole
import com.upchiapas.mathenglishquest.data.remote.model.ClaseDoc
import com.upchiapas.mathenglishquest.data.remote.model.UsuarioDoc
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Lectura y escritura de las colecciones `usuarios` y `clases`.
 *
 * No conoce la interfaz ni Room: solo convierte entre documentos de Firestore y
 * [UsuarioDoc] / [ClaseDoc]. Los repositorios deciden qué hacer con eso.
 */
class FirestoreDataSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun usuarios() = db.collection(FirestoreSchema.COLECCION_USUARIOS)
    private fun clases() = db.collection(FirestoreSchema.COLECCION_CLASES)

    // ---------- Usuarios ----------

    suspend fun buscarUsuario(uid: String): UsuarioDoc? {
        val snap = usuarios().document(uid).get().awaitTask()
        return if (snap.exists()) UsuarioDoc.fromMap(uid, snap.data.orEmpty()) else null
    }

    /** Crea o reemplaza el perfil. El UID manda: es el id del documento. */
    suspend fun guardarUsuario(usuario: UsuarioDoc) {
        usuarios().document(usuario.uid).set(usuario.toMap()).awaitTask()
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
}
