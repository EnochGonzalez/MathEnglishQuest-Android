package com.upchiapas.mathenglishquest.data.remote

/**
 * Nombres de las colecciones y de los campos de Firestore.
 *
 * Se centralizan aquí para que la app y la API del compañero hablen exactamente el mismo
 * esquema: si un campo cambia, se cambia en un solo lugar. El detalle de cada documento
 * está documentado en `docs/firestore.md`.
 */
object FirestoreSchema {

    /** Perfiles de usuario. El id del documento es el UID de Firebase Auth. */
    const val COLECCION_USUARIOS = "usuarios"

    /** Clases creadas por los maestros. El id del documento es el código de 6 caracteres. */
    const val COLECCION_CLASES = "clases"

    object Usuario {
        const val NOMBRE = "nombreCompleto"
        const val CORREO = "correo"
        const val ROL = "rol"
        const val CLASE_ID = "claseId"
        const val CREADO_EN = "creadoEn"
        const val ACTUALIZADO_EN = "actualizadoEn"
    }

    object Clase {
        const val CODIGO = "codigo"
        const val MAESTRO_UID = "maestroUid"
        const val MAESTRO_NOMBRE = "maestroNombre"
        const val CREADO_EN = "creadoEn"
    }
}
