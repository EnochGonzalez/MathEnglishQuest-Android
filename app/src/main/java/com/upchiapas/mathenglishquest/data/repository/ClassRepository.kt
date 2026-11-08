package com.upchiapas.mathenglishquest.data.repository

import android.util.Log
import com.upchiapas.mathenglishquest.data.local.SessionManager
import com.upchiapas.mathenglishquest.data.local.dao.UsuarioDao
import com.upchiapas.mathenglishquest.data.model.UserRole
import com.upchiapas.mathenglishquest.data.remote.FirestoreDataSource
import com.upchiapas.mathenglishquest.domain.clase.ClassDataSource
import com.upchiapas.mathenglishquest.domain.clase.StudentSummary
import com.upchiapas.mathenglishquest.domain.portfolio.EvidenceEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * "Mi Clase": los estudiantes llegan de **Firestore** (para que el maestro los vea aunque
 * estén en otro celular) y se copian a Room; el roster se arma con esa copia local, que
 * además trae el conteo de evidencias del teléfono.
 *
 * La primera vez que se abre la pantalla se escucha la clase en tiempo real: si un estudiante
 * se registra, aparece sin necesidad de recargar.
 */
class ClassRepository(
    private val usuarioDao: UsuarioDao,
    private val session: SessionManager,
    private val evidenceRepository: EvidenceRepository,
    private val firestore: FirestoreDataSource,
    private val mirror: UsuarioMirror,
    private val scope: CoroutineScope
) : ClassDataSource {

    /** Clases que ya tienen una escucha activa, para no abrir varias por la misma clase. */
    private val clasesSincronizadas = ConcurrentHashMap.newKeySet<String>()

    override suspend fun myClassCode(): String? {
        val id = session.userId() ?: return null
        val user = usuarioDao.buscarPorId(id) ?: return null
        return if (UserRole.fromDb(user.rol) == UserRole.MAESTRO) user.claseId else null
    }

    override fun observeRoster(classCode: String): Flow<List<StudentSummary>> {
        iniciarSincronizacion(classCode)
        return usuarioDao.observarResumenDeClase(classCode).map { rows ->
            rows.map { StudentSummary(it.id, it.nombre, it.correo, it.totalEvidencias, it.ultimaFecha) }
        }
    }

    override suspend fun studentName(studentId: Int): String? {
        val classCode = myClassCode() ?: return null
        val student = usuarioDao.buscarPorId(studentId) ?: return null
        val belongsToMyClass = UserRole.fromDb(student.rol) == UserRole.ESTUDIANTE && student.claseId == classCode
        return if (belongsToMyClass) student.nombreCompleto else null
    }

    override fun observeStudentEvidence(studentId: Int): Flow<List<EvidenceEntry>> =
        evidenceRepository.observeFor(studentId)

    override fun fileFor(entry: EvidenceEntry): File = evidenceRepository.fileFor(entry)

    /**
     * Escucha `usuarios where claseId == codigo` y va guardando cada estudiante en Room.
     * El roster que ve la pantalla sale de Room, así que se actualiza solo.
     */
    private fun iniciarSincronizacion(codigo: String) {
        if (codigo.isBlank() || !clasesSincronizadas.add(codigo)) return
        scope.launch {
            firestore.observarEstudiantes(codigo)
                .catch { error ->
                    Log.w(TAG, "No se pudo sincronizar la clase $codigo: ${error.message}")
                    clasesSincronizadas.remove(codigo)
                }
                .collect { estudiantes ->
                    estudiantes.forEach { estudiante ->
                        runCatching { mirror.guardar(estudiante) }
                            .onFailure { Log.w(TAG, "No se pudo guardar ${estudiante.correo}: ${it.message}") }
                    }
                }
        }
    }

    private companion object {
        const val TAG = "ClassRepository"
    }
}
