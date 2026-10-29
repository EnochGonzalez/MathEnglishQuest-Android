package com.upchiapas.mathenglishquest.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.upchiapas.mathenglishquest.data.local.SessionManager
import com.upchiapas.mathenglishquest.data.local.dao.EvidenciaDao
import com.upchiapas.mathenglishquest.data.local.entity.Evidencia
import com.upchiapas.mathenglishquest.domain.dado.EvidenceSaver
import com.upchiapas.mathenglishquest.domain.portfolio.EvidenceEntry
import com.upchiapas.mathenglishquest.utils.EvidenceFiles
import com.upchiapas.mathenglishquest.utils.ImageCompressor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import java.io.File

/**
 * Fotos de procedimiento: se comprimen, se guardan en la carpeta privada de la app y se registran en Room.
 * (Cuando entre Firebase, las fotos suben a Storage y esta clase cambia; el resto de la app no.)
 */
class EvidenceRepository(
    context: Context,
    private val evidenciaDao: EvidenciaDao,
    private val session: SessionManager
) : EvidenceSaver {

    private val appContext = context.applicationContext

    override suspend fun save(source: String, difficultyName: String, categories: List<String>): Boolean {
        val userId = session.userId()
        if (userId == null) {
            Log.e(TAG, "No hay una cuenta con sesión abierta: las evidencias solo se guardan para estudiantes con cuenta")
            return false
        }
        val uri = Uri.parse(source)

        val now = System.currentTimeMillis()
        val relativeName = "$EVIDENCE_DIR/u${userId}_$now.jpg"
        val file = File(appContext.filesDir, relativeName)

        if (!ImageCompressor.compressToFile(appContext, uri, file)) {
            Log.e(TAG, "La foto no se pudo comprimir ($source). Mira el mensaje anterior de ImageCompressor.")
            return false
        }

        return try {
            evidenciaDao.insertar(
                Evidencia(
                    usuarioId = userId,
                    rutaImagen = relativeName,
                    fecha = now,
                    dificultad = difficultyName,
                    categorias = categories.joinToString(SEPARATOR)
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
        return evidenciaDao.observarPorUsuario(userId).map { list -> list.map { it.toEntry() } }
    }

    fun fileFor(entry: EvidenceEntry): File = File(appContext.filesDir, entry.imageFileName)

    private fun Evidencia.toEntry() = EvidenceEntry(
        id = id,
        imageFileName = rutaImagen,
        dateMillis = fecha,
        difficulty = dificultad,
        categories = categorias.split(SEPARATOR).filter { it.isNotBlank() }
    )

    private companion object {
        const val TAG = "EvidenceRepository"
        const val EVIDENCE_DIR = "evidencias"
        const val SEPARATOR = "|"
    }
}
