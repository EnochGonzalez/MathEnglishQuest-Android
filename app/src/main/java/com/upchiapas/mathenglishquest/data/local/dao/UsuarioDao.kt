package com.upchiapas.mathenglishquest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.upchiapas.mathenglishquest.data.local.entity.Usuario

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Update
    suspend fun actualizar(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE firebaseUid = :uid LIMIT 1")
    suspend fun buscarPorFirebaseUid(uid: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Usuario?

    /**
     * Estudiantes vinculados a una clase, con su total de evidencias y la fecha de la última.
     * El conteo viene del perfil (lo actualiza el celular del estudiante al subir la foto),
     * así que el maestro lo ve aunque la foto todavía no esté descargada en su teléfono.
     */
    @Query(
        "SELECT u.id AS id, u.nombreCompleto AS nombre, u.correo AS correo, " +
            "u.totalEvidencias AS totalEvidencias, u.ultimaEvidencia AS ultimaFecha " +
            "FROM usuarios u " +
            "WHERE u.rol = 'estudiante' AND u.claseId = :codigo " +
            "ORDER BY u.nombreCompleto COLLATE NOCASE"
    )
    fun observarResumenDeClase(codigo: String): Flow<List<ResumenEstudiante>>

    @Query("SELECT * FROM usuarios WHERE rol = 'maestro' AND claseId = :codigo LIMIT 1")
    suspend fun buscarMaestroPorCodigo(codigo: String): Usuario?
}
