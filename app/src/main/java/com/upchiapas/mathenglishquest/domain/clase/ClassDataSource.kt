package com.upchiapas.mathenglishquest.domain.clase

import com.upchiapas.mathenglishquest.domain.portfolio.EvidenceEntry
import kotlinx.coroutines.flow.Flow
import java.io.File

/** De dónde sale la información de la clase. Es una interfaz para poder probar los ViewModels sin Android. */
interface ClassDataSource {
    /** Código de clase del maestro con sesión abierta; null si quien entró no es maestro. */
    suspend fun myClassCode(): String?

    fun observeRoster(classCode: String): Flow<List<StudentSummary>>

    /** Nombre del estudiante, solo si pertenece a la clase de este maestro; null en cualquier otro caso. */
    suspend fun studentName(studentId: Int): String?

    fun observeStudentEvidence(studentId: Int): Flow<List<EvidenceEntry>>

    fun fileFor(entry: EvidenceEntry): File
}
