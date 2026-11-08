package com.upchiapas.mathenglishquest.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
import com.upchiapas.mathenglishquest.data.local.SessionManager
import com.upchiapas.mathenglishquest.data.local.dao.EvidenciaDao
import com.upchiapas.mathenglishquest.data.local.dao.UsuarioDao
import com.upchiapas.mathenglishquest.data.local.entity.Evidencia
import com.upchiapas.mathenglishquest.data.remote.FirestoreDataSource
import com.upchiapas.mathenglishquest.data.remote.StorageDataSource
import com.upchiapas.mathenglishquest.data.remote.model.EvidenciaDoc
import com.upchiapas.mathenglishquest.domain.dado.EvidenceSaver
import com.upchiapas.mathenglishquest.domain.portfolio.EvidenceEntry
import com.upchiapas.mathenglishquest.utils.EvidenceFiles
import com.upchiapas.mathenglishquest.utils.ImageCompressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Fotos del portafolio.
 *
 * Se comprimen, se suben a **Firebase Storage**, se registran en Firestore (`evidencias`) y
 * se guardan también en Room. Room es la copia que pinta la pantalla (y sirve sin internet);
 * las fotos de otros celulares se descargan a la carpeta privada la primera vez que se ven.
 */
class EvidenceRepository(
    context: Context,
    private val evidenciaDao: EvidenciaDao,
    private val usuarioDao: UsuarioDao,
    private val session: SessionManager,
    private val firestore: FirestoreDataSource,
    private val storage: StorageDataSource,
    private val mirror: UsuarioMirror,
    private val scope: CoroutineScope
) : EvidenceSaver {

    private val appContext = context.applicationContext

    /** Evidencias ya sincronizadas, para no abrir una escucha por cada consulta de la pantalla. */
    private val uidsSincronizados = ConcurrentHashMap.newKeySet<String>()

    override suspend fun save(
        source: String,
        difficultyName: String,
        categories: List<String>,
        exerciseText: String
    ): Boolean {
        val userId = session.userId()
        if (userId == null) {
            Log.e(TAG, "No hay una cuenta con sesión abierta: las evidencias solo se guardan para estudiantes con cuenta")
            return false
        }
        val usuario = usuarioDao.buscarPorId(userId)
        val uri = Uri.parse(source)

        val now = System.currentTimeMillis()
        val relativeName = "$EVIDENCE_DIR/u${userId}_$now.jpg"
        val file = File(appContext.filesDir, relativeName)

        if (!ImageCompressor.compressToFile(appContext, uri, file)) {
            Log.e(TAG, "La foto no se pudo comprimir ($source). Mira el mensaje anterior de ImageCompressor.")
            return false
        }

        // Sin UID todavía no hay nube (cuenta vieja de Room): se guarda solo en el teléfono.
        val uid = usuario?.firebaseUid
        var firebaseId: String? = null
        var storagePath: String? = null

        if (uid != null) {
            try {
                storagePath = storage.subirEvidencia(uid, file, "$now.jpg")
                firebaseId = firestore.guardarEvidencia(
                    EvidenciaDoc(
                        uid = uid,
                        claseId = usuario.claseId,
                        rutaStorage = storagePath,
                        fecha = Timestamp(now / 1000, 0),
                        dificultad = difficultyName,
                        categorias = categories.joinToString(SEPARATOR),
                        ejercicio = exerciseText
                    )
                )
                firestore.registrarEvidenciaEnPerfil(uid, now)
                mirror.registrarEvidenciaLocal(uid, now)
            } catch (e: Exception) {
                // La foto NO se pierde: queda guardada en el teléfono y se puede volver a subir.
                Log.w(TAG, "No se pudo subir la foto a Firebase: ${e.javaClass.simpleName}: ${e.message}")
                firebaseId = null
                storagePath = null
            }
        }

        return try {
            evidenciaDao.insertar(
                Evidencia(
                    usuarioId = userId,
                    rutaImagen = relativeName,
                    fecha = now,
                    dificultad = difficultyName,
                    categorias = categories.joinToString(SEPARATOR),
                    ejercicio = exerciseText,
                    firebaseId = firebaseId,
                    storagePath = storagePath
                )
            )
            EvidenceFiles.deleteTemp(appContext, uri)
            true
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo registrar la foto en la base de datos: ${e.javaClass.simpleName}: ${e.message}")
            file.delete()
            false
        }
    }

    /** Evidencias del usuario con sesión abierta, de la más reciente a la más antigua. */
    fun observeMine(): Flow<List<EvidenceEntry>> {
        val userId = session.userId() ?: return emptyFlow()
        return observeFor(userId)
    }

    /** Evidencias de un usuario concreto (el maestro las usa para ver el portafolio de sus estudiantes). */
    fun observeFor(userId: Int): Flow<List<EvidenceEntry>> {
        scope.launch { iniciarSincronizacion(userId) }
        return evidenciaDao.observarPorUsuario(userId).map { list -> list.map { it.toEntry() } }
    }
    fun fileFor(entry: EvidenceEntry): File = File(appContext.filesDir, entry.imageFileName)

    /**
     * Escucha las evidencias del estudiante en Firestore y las copia a Room, descargando las
     * fotos que falten. Así el maestro ve las fotos que el estudiante subió desde su celular.
     */
    private suspend fun iniciarSincronizacion(userId: Int) {
        val uid = usuarioDao.buscarPorId(userId)?.firebaseUid ?: return
        if (!uidsSincronizados.add(uid)) return

        scope.launch {
            firestore.observarEvidencias(uid)
                .catch { error ->
                    Log.w(TAG, "No se pudieron sincronizar las evidencias de $uid: ${error.message}")
                    uidsSincronizados.remove(uid)
                }
                .collect { docs ->
                    docs.forEach { doc -> guardarDesdeNube(doc) }
                }
        }
    }

    private suspend fun guardarDesdeNube(doc: EvidenciaDoc) {
        val usuario = usuarioDao.buscarPorFirebaseUid(doc.uid) ?: return
        val fecha = doc.fecha?.toDate()?.time ?: System.currentTimeMillis()
        val relativeName = "$EVIDENCE_DIR/u${usuario.id}_$fecha.jpg"

        val existente = evidenciaDao.buscarPorFirebaseId(doc.id)
        val fila = if (existente == null) {
            val nueva = Evidencia(
                usuarioId = usuario.id,
                rutaImagen = relativeName,
                fecha = fecha,
                dificultad = doc.dificultad,
                categorias = doc.categorias,
                ejercicio = doc.ejercicio,
                firebaseId = doc.id,
                storagePath = doc.rutaStorage
            )
            val id = evidenciaDao.insertar(nueva)
            nueva.copy(id = id.toInt())
        } else {
            existente
        }

        // La foto se descarga una sola vez; después se reutiliza la del teléfono.
        val archivo = File(appContext.filesDir, fila.rutaImagen)
        if (!archivo.exists() && doc.rutaStorage.isNotBlank()) {
            if (storage.descargar(doc.rutaStorage, archivo)) {
                // Tocar la fila hace que Room avise a la pantalla y la foto aparezca.
                evidenciaDao.actualizar(fila)
            }
        }
    }

    private fun Evidencia.toEntry() = EvidenceEntry(
        id = id,
        imageFileName = rutaImagen,
        dateMillis = fecha,
        difficulty = dificultad,
        categories = categorias.split(SEPARATOR).filter { it.isNotBlank() },
        exercise = ejercicio
    )

    private companion object {
        const val TAG = "EvidenceRepository"
        const val EVIDENCE_DIR = "evidencias"
        const val SEPARATOR = "|"
    }
}
