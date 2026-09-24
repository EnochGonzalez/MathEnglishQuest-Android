package com.upchiapas.mathenglishquest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.upchiapas.mathenglishquest.data.local.entity.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios LIMIT 1")
    fun obtenerUsuarioActual(): Flow<Usuario?>
}