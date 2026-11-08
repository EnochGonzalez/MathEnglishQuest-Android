package com.upchiapas.mathenglishquest.data.repository

import com.upchiapas.mathenglishquest.data.local.dao.UsuarioDao
import com.upchiapas.mathenglishquest.data.local.entity.Usuario
import com.upchiapas.mathenglishquest.data.remote.model.UsuarioDoc

/**
 * Copia en Room un perfil que viene de Firestore ("espejo").
 *
 * Firestore es la fuente de verdad (la nube, compartida entre celulares). Room sigue
 * siendo el acceso rápido y sin internet para la interfaz, que trabaja con ids `Int`
 * y con las evidencias guardadas en el teléfono.
 */
class UsuarioMirror(private val usuarioDao: UsuarioDao) {

    /**
     * Crea o actualiza la copia local del perfil y devuelve el id local.
     * Si el correo ya existía en Room (cuenta vieja, anterior a Firebase), se reutiliza
     * esa fila para no perder sus evidencias.
     */
    suspend fun guardar(perfil: UsuarioDoc): Int {
        val existente = usuarioDao.buscarPorFirebaseUid(perfil.uid)
            ?: usuarioDao.buscarPorCorreo(perfil.correo)

        if (existente == null) {
            return usuarioDao.insertar(
                Usuario(
                    nombreCompleto = perfil.nombreCompleto,
                    correo = perfil.correo,
                    passwordHash = SIN_PASSWORD_LOCAL,
                    rol = perfil.rol,
                    claseId = perfil.claseId,
                    firebaseUid = perfil.uid
                )
            ).toInt()
        }

        val actualizado = existente.copy(
            nombreCompleto = perfil.nombreCompleto,
            correo = perfil.correo,
            rol = perfil.rol,
            claseId = perfil.claseId,
            firebaseUid = perfil.uid
        )
        usuarioDao.actualizar(actualizado)
        return actualizado.id
    }

    companion object {
        /** Las cuentas de Firebase no guardan contraseña en el celular: de eso se encarga Auth. */
        const val SIN_PASSWORD_LOCAL = ""
    }
}
