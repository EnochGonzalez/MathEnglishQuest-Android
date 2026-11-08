package com.upchiapas.mathenglishquest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.upchiapas.mathenglishquest.data.local.entity.Evidencia
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenciaDao {
    @Insert
    suspend fun insertar(evidencia: Evidencia): Long

    @Update
    suspend fun actualizar(evidencia: Evidencia)

    /** De la más reciente a la más antigua; se actualiza sola cuando se guarda una foto nueva. */
    @Query("SELECT * FROM evidencias WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    fun observarPorUsuario(usuarioId: Int): Flow<List<Evidencia>>

    /** Se usa al sincronizar: evita duplicar una foto que ya vino de la nube. */
    @Query("SELECT * FROM evidencias WHERE firebaseId = :firebaseId LIMIT 1")
    suspend fun buscarPorFirebaseId(firebaseId: String): Evidencia?
}
