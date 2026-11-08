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

    /** Fotos del portafolio. Subcolección de cada clase: `clases/{codigo}/evidencias/{id}`. */
    const val COLECCION_EVIDENCIAS = "evidencias"

    /** Progreso del módulo del Dado, uno por usuario. El id del documento es el UID. */
    const val COLECCION_PROGRESO = "progreso"

    /** Carpeta de Firebase Storage donde se guardan las fotos: `evidencias/{uid}/archivo.jpg`. */
    const val CARPETA_EVIDENCIAS = "evidencias"

    object Usuario {
        const val NOMBRE = "nombreCompleto"
        const val CORREO = "correo"
        const val ROL = "rol"
        const val CLASE_ID = "claseId"
        const val CREADO_EN = "creadoEn"
        const val ACTUALIZADO_EN = "actualizadoEn"

        /** Cuántas evidencias ha subido (lo mantiene la app al guardar una foto). */
        const val TOTAL_EVIDENCIAS = "totalEvidencias"

        /** Fecha de la última evidencia, para ordenar el panel del maestro. */
        const val ULTIMA_EVIDENCIA = "ultimaEvidenciaEn"
    }

    object Clase {
        const val CODIGO = "codigo"
        const val MAESTRO_UID = "maestroUid"
        const val MAESTRO_NOMBRE = "maestroNombre"
        const val CREADO_EN = "creadoEn"
    }

    object Evidencia {
        const val UID = "uid"
        const val CLASE_ID = "claseId"
        const val RUTA_STORAGE = "rutaStorage"
        const val FECHA = "fecha"
        const val DIFICULTAD = "dificultad"
        const val CATEGORIAS = "categorias"
        const val EJERCICIO = "ejercicio"
    }

    object Progreso {
        const val HALF_HEARTS = "halfHearts"
        const val STREAK = "streak"
        const val MAX_UNLOCKED = "maxUnlockedLevel"
        const val CURRENT_LEVEL = "currentLevel"
        const val MODE = "mode"
        const val PROGRESSED = "hadProgressedBeyondEasy"
        const val NEGATIVES = "allowNegatives"
        const val ACTUALIZADO_EN = "actualizadoEn"
    }
}
