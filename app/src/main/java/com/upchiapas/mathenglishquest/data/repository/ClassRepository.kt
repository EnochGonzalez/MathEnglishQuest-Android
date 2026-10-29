package com.upchiapas.mathenglishquest.data.repository

import com.upchiapas.mathenglishquest.data.local.SessionManager
import com.upchiapas.mathenglishquest.data.local.dao.UsuarioDao
import com.upchiapas.mathenglishquest.data.model.UserRole
import com.upchiapas.mathenglishquest.domain.clase.ClassDataSource
import com.upchiapas.mathenglishquest.domain.clase.StudentSummary
import com.upchiapas.mathenglishquest.domain.portfolio.EvidenceEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

/**
 * "Mi Clase" simulada en el mismo celular: lee las cuentas y las evidencias guardadas aquí.
 * (Con Firebase, esta clase cambia por una que lea de la nube; los ViewModels y las pantallas no.)
 */
class ClassRepository(
    private val usuarioDao: UsuarioDao,
    private val session: SessionManager,
    private val evidenceRepository: EvidenceRepository
) : ClassDataSource {

    override suspend fun myClassCode(): String? {
        val id = session.userId() ?: return null
        val user = usuarioDao.buscarPorId(id) ?: return null
        return if (UserRole.fromDb(user.rol) == UserRole.MAESTRO) user.claseId else null
    }

    override fun observeRoster(classCode: String): Flow<List<StudentSummary>> =
        usuarioDao.observarResumenDeClase(classCode).map { rows ->
            rows.map { StudentSummary(it.id, it.nombre, it.correo, it.totalEvidencias, it.ultimaFecha) }
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
}
