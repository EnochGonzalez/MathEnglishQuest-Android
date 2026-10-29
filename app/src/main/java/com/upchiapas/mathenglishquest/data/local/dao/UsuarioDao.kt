package com.upchiapas.mathenglishquest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.upchiapas.mathenglishquest.data.local.entity.Usuario

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Usuario?

    /**
     * Estudiantes vinculados a una clase, con su total de evidencias y la fecha de la última.
     * Se actualiza sola cuando alguien se registra o sube una foto.
     */
    @Query(
        "SELECT u.id AS id, u.nombreCompleto AS nombre, u.correo AS correo, " +
            "COUNT(e.id) AS totalEvidencias, MAX(e.fecha) AS ultimaFecha " +
            "FROM usuarios u LEFT JOIN evidencias e ON e.usuarioId = u.id " +
            "WHERE u.rol = 'estudiante' AND u.claseId = :codigo " +
            "GROUP BY u.id"
    )
    fun observarResumenDeClase(codigo: String): Flow<List<ResumenEstudiante>>

    @Query("SELECT * FROM usuarios WHERE rol = 'maestro' AND claseId = :codigo LIMIT 1")
    suspend fun buscarMaestroPorCodigo(codigo: String): Usuario?
}
