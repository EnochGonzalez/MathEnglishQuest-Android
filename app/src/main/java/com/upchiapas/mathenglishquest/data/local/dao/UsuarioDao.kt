package com.upchiapas.mathenglishquest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.upchiapas.mathenglishquest.data.local.entity.Usuario

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Usuario?

    @Query("SELECT * FROM usuarios WHERE rol = 'maestro' AND claseId = :codigo LIMIT 1")
    suspend fun buscarMaestroPorCodigo(codigo: String): Usuario?
}
