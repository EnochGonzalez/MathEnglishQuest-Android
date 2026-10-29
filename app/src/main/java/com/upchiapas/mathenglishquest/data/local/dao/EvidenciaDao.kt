package com.upchiapas.mathenglishquest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.upchiapas.mathenglishquest.data.local.entity.Evidencia
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenciaDao {
    @Insert
    suspend fun insertar(evidencia: Evidencia): Long

    /** De la más reciente a la más antigua; se actualiza sola cuando se guarda una foto nueva. */
    @Query("SELECT * FROM evidencias WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    fun observarPorUsuario(usuarioId: Int): Flow<List<Evidencia>>
}
